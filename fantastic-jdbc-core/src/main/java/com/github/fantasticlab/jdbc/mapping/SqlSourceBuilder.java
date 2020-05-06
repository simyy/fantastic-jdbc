package com.github.fantasticlab.jdbc.mapping;

import com.github.fantasticlab.jdbc.reflection.MetaClass;
import com.github.fantasticlab.jdbc.reflection.MetaObject;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.transaction.type.JdbcType;
import com.github.fantasticlab.jdbc.xml.BaseBuilder;
import com.github.fantasticlab.jdbc.xml.parsing.GenericTokenParser;
import com.github.fantasticlab.jdbc.xml.parsing.ParsingException;
import com.github.fantasticlab.jdbc.xml.parsing.TokenHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQL源码构建器
 */
public class SqlSourceBuilder extends BaseBuilder {

    private static final String parameterProperties = "jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName";

    public SqlSourceBuilder(Configuration configuration) {
        super(configuration);
    }

    public SqlSource parse(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) {
        ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType, additionalParameters);
        // 普通记号解析器，处理#{}参数
        GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);
        String sql = parser.parse(originalSql);
        // 处理结束返回静态SQL
        return new StaticSqlSource(configuration, sql, handler.getParameterMappings());
    }

    //参数映射记号处理器，静态内部类
    private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

        private List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();
        private Class<?> parameterType;
        private MetaObject metaParameters;

        public ParameterMappingTokenHandler(Configuration configuration,
                                            Class<?> parameterType,
                                            Map<String, Object> additionalParameters) {
            super(configuration);
            this.parameterType = parameterType;
            this.metaParameters = configuration.newMetaObject(additionalParameters);
        }

        public List<ParameterMapping> getParameterMappings() {
            return parameterMappings;
        }

        @Override
        public String handleToken(String content) {
            // #{} 替换为 ?, 参数增加到mappings存储
            parameterMappings.add(buildParameterMapping(content));
            return "?";
        }

        //构建参数映射
        private ParameterMapping buildParameterMapping(String content) {
            // 解析参数#{property,jdbcType=VARCHAR}
            Map<String, String> propertiesMap = new ParameterExpression(content);
            Class<?> propertyType;
            MetaClass metaClass = MetaClass.forClass(parameterType);
            String property = propertiesMap.get("property");
            if (metaClass.hasGetter(property)) {
                propertyType = metaClass.getGetterType(property);
            } else {
                propertyType = Object.class;
            }

            ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);
            Class<?> javaType = propertyType;
            String typeHandlerAlias = null;
            for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
                String name = entry.getKey();
                String value = entry.getValue();
                if ("javaType".equals(name)) {
                    javaType = resolveClass(value);
                    builder.javaType(javaType);
                } else if ("jdbcType".equals(name)) {
                    builder.jdbcType(resolveJdbcType(value));
                } else if ("mode".equals(name)) {
                    builder.mode(resolveParameterMode(value));
                } else if ("numericScale".equals(name)) {
                    builder.numericScale(Integer.valueOf(value));
                } else if ("resultMap".equals(name)) {
                    builder.resultMapId(value);
                } else if ("typeHandler".equals(name)) {
                    typeHandlerAlias = value;
                } else if ("jdbcTypeName".equals(name)) {
                    builder.jdbcTypeName(value);
                } else if ("property".equals(name)) {
                    // Do Nothing
                } else if ("expression".equals(name)) {
                    throw new ParsingException("Expression based parameters are not supported yet");
                } else {
                    throw new ParsingException("An invalid property '" + name + "' was found in mapping #{" + content + "}.  Valid properties are " + parameterProperties);
                }
            }
            return builder.build();
        }

        private Map<String, String> parseParameterMapping(String content) {
            try {
                return new ParameterExpression(content);
            } catch (ParsingException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new ParsingException("Parsing error was found in mapping #{" + content + "}.  Check syntax #{property|(expression), var1=value1, var2=value2, ...} ", ex);
            }
        }
    }

}
