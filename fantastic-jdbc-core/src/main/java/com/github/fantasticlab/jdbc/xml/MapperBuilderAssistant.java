package com.github.fantasticlab.jdbc.xml;

import com.github.fantasticlab.jdbc.executor.keygen.KeyGenerator;
import com.github.fantasticlab.jdbc.mapping.*;
import com.github.fantasticlab.jdbc.reflection.MetaClass;
import com.github.fantasticlab.jdbc.scripting.LanguageDriver;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.transaction.type.JdbcType;
import com.github.fantasticlab.jdbc.transaction.type.TypeHandler;
import com.github.fantasticlab.jdbc.xml.parsing.ParsingException;

import java.util.*;

/**
 * 映射构建器
 */
public class MapperBuilderAssistant extends BaseBuilder {

    private String currentNamespace;
    private String resource;

    public MapperBuilderAssistant(Configuration configuration, String resource) {
        super(configuration);
        this.resource = resource;
    }

    public String getCurrentNamespace() {
        return currentNamespace;
    }

    public void setCurrentNamespace(String currentNamespace) {
        if (currentNamespace == null) {
            throw new ParsingException("The mapper element requires a namespace attribute to be specified.");
        }

        if (this.currentNamespace != null && !this.currentNamespace.equals(currentNamespace)) {
            throw new ParsingException("Wrong namespace. Expected '"
                    + this.currentNamespace + "' but found '" + currentNamespace + "'.");
        }

        this.currentNamespace = currentNamespace;
    }

    public String applyCurrentNamespace(String base, boolean isReference) {
        if (base == null) {
            return null;
        }
        if (isReference) {
            // is it qualified with any namespace yet?
            if (base.contains(".")) {
                return base;
            }
        } else {
            // is it qualified with this namespace yet?
            if (base.startsWith(currentNamespace + ".")) {
                return base;
            }
            if (base.contains(".")) {
                throw new ParsingException("Dots are not allowed in element names, please remove it from " + base);
            }
        }
        return currentNamespace + "." + base;
    }

    public ParameterMap addParameterMap(String id, Class<?> parameterClass, List<ParameterMapping> parameterMappings) {
        id = applyCurrentNamespace(id, false);
        ParameterMap.Builder parameterMapBuilder = new ParameterMap.Builder(id, parameterClass, parameterMappings);
        ParameterMap parameterMap = parameterMapBuilder.build();
        configuration.addParameterMap(parameterMap);
        return parameterMap;
    }

    public ParameterMapping buildParameterMapping(
            Class<?> parameterType,
            String property,
            Class<?> javaType,
            JdbcType jdbcType,
            String resultMap,
            ParameterMode parameterMode,
            Class<? extends TypeHandler<?>> typeHandler,
            Integer numericScale) {
        resultMap = applyCurrentNamespace(resultMap, true);

        // Class parameterType = parameterMapBuilder.type();
        Class<?> javaTypeClass = resolveParameterJavaType(parameterType, property, javaType, jdbcType);
        TypeHandler<?> typeHandlerInstance = resolveTypeHandler(javaTypeClass, typeHandler);

        ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, javaTypeClass);
        builder.jdbcType(jdbcType);
        builder.resultMapId(resultMap);
        builder.mode(parameterMode);
        builder.numericScale(numericScale);
        builder.typeHandler(typeHandlerInstance);
        return builder.build();
    }

    public ResultMap addResultMap(String id, Class<?> type, List<ResultMapping> resultMappings) {
        id = applyCurrentNamespace(id, false);
        ResultMap.Builder resultMapBuilder = new ResultMap.Builder(id, type, resultMappings);
        ResultMap resultMap = resultMapBuilder.build();
        configuration.addResultMap(resultMap);
        return resultMap;
    }

    public MappedStatement addMappedStatement(
            String id,
            SqlSource sqlSource,
            SqlCommandType sqlCommandType,
            Class<?> parameterType,
            String resultMap,
            Class<?> resultType,
            KeyGenerator keyGenerator) {

        // package_prefix.sql_id
        id = applyCurrentNamespace(id, false);
        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;

        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlSource, sqlCommandType);
        statementBuilder.resource(resource);
        statementBuilder.keyGenerator(keyGenerator);
        setStatementParameterMap(parameterType, statementBuilder);
        setStatementResultMap(resultMap, resultType, statementBuilder);

        MappedStatement statement = statementBuilder.build();
        statement.setLang(configuration.getLanguageRegistry().getDefaultDriver());
        configuration.addMappedStatement(statement);
        return statement;
    }

    private <T> T valueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }


    private void setStatementParameterMap(Class<?> parameterTypeClass,
                                          MappedStatement.Builder statementBuilder) {

       if (parameterTypeClass != null) {
            List<ParameterMapping> parameterMappings = new ArrayList<ParameterMapping>();
            ParameterMap.Builder inlineParameterMapBuilder = new ParameterMap.Builder(
                    statementBuilder.id() + "-Inline",
                    parameterTypeClass,
                    parameterMappings);
            statementBuilder.parameterMap(inlineParameterMapBuilder.build());
        }
    }

    // TODO
    private void setStatementResultMap(String resultMap,
                                       Class<?> resultType,
                                       MappedStatement.Builder statementBuilder) {

        resultMap = applyCurrentNamespace(resultMap, true);
        List<ResultMap> resultMaps = new ArrayList<ResultMap>();
        if (resultMap != null) {
            //2.1 resultMap是高级功能
            String[] resultMapNames = resultMap.split(",");
            for (String resultMapName : resultMapNames) {
                try {
                    resultMaps.add(configuration.getResultMap(resultMapName.trim()));
                } catch (IllegalArgumentException e) {
                    throw new ParsingException("Could not find result map " + resultMapName, e);
                }
            }
        } else if (resultType != null) {
            //2.2 resultType,一般用这个足矣了
            //<select id="selectUsers" resultType="User">
            //这种情况下,MyBatis 会在幕后自动创建一个 ResultMap,基于属性名来映射列到 JavaBean 的属性上。
            //如果列名没有精确匹配,你可以在列名上使用 select 字句的别名来匹配标签。
            //创建一个inline result map, 把resultType设上就OK了，
            //然后后面被DefaultResultSetHandler.createResultObject()使用
            //DefaultResultSetHandler.getRowValue()使用
            ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(
                    statementBuilder.id() + "-Inline",
                    resultType,
                    new ArrayList<ResultMapping>());
            resultMaps.add(inlineResultMapBuilder.build());
        }
        statementBuilder.resultMaps(resultMaps);
    }

    public ResultMapping buildResultMapping(Class<?> resultType,
                                            String property,
                                            String column,
                                            Class<?> javaType,
                                            JdbcType jdbcType,
                                            Class<? extends TypeHandler<?>> typeHandler,
                                            List<ResultFlag> flags,
                                            String resultSet) {

        Class<?> javaTypeClass = resolveResultJavaType(resultType, property, javaType);
        TypeHandler<?> typeHandlerInstance = resolveTypeHandler(javaTypeClass, typeHandler);
        ResultMapping.Builder builder = new ResultMapping.Builder(configuration, property, column, javaTypeClass);
        builder.jdbcType(jdbcType);
        builder.resultSet(resultSet);
        builder.typeHandler(typeHandlerInstance);
        builder.flags(flags == null ? new ArrayList<ResultFlag>() : flags);
        return builder.build();
    }

    private Set<String> parseMultipleColumnNames(String columnName) {
        Set<String> columns = new HashSet<String>();
        if (columnName != null) {
            if (columnName.indexOf(',') > -1) {
                StringTokenizer parser = new StringTokenizer(columnName, "{}, ", false);
                while (parser.hasMoreTokens()) {
                    String column = parser.nextToken();
                    columns.add(column);
                }
            } else {
                columns.add(columnName);
            }
        }
        return columns;
    }

    private Class<?> resolveResultJavaType(Class<?> resultType, String property, Class<?> javaType) {
        if (javaType == null && property != null) {
            try {
                MetaClass metaResultType = MetaClass.forClass(resultType);
                javaType = metaResultType.getSetterType(property);
            } catch (Exception e) {
                //ignore, following null check statement will deal with the situation
            }
        }
        if (javaType == null) {
            javaType = Object.class;
        }
        return javaType;
    }

    private Class<?> resolveParameterJavaType(Class<?> resultType, String property, Class<?> javaType, JdbcType jdbcType) {
        if (javaType == null) {
            if (JdbcType.CURSOR.equals(jdbcType)) {
                javaType = java.sql.ResultSet.class;
            } else if (Map.class.isAssignableFrom(resultType)) {
                javaType = Object.class;
            } else {
                MetaClass metaResultType = MetaClass.forClass(resultType);
                javaType = metaResultType.getGetterType(property);
            }
        }
        if (javaType == null) {
            javaType = Object.class;
        }
        return javaType;
    }

    public LanguageDriver getLanguageDriver(Class<?> langClass) {
        if (langClass != null) {
            //注册语言驱动
            configuration.getLanguageRegistry().register(langClass);
        } else {
            //如果为null，则取得默认驱动（mybatis3.2以前大家一直用的方法）
//            langClass = configuration.getLanguageRegistry().getDefaultDriverClass();
            configuration.getLanguageRegistry()
                    .setDefaultDriverClass(
                            configuration.getLanguageRegistry()
                                    .getDefaultDriverClass());
        }
        //再去调configuration
        return configuration.getLanguageRegistry().getDriver(langClass);
    }
}
