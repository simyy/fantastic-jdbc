package com.github.fantasticlab.jdbc.xml;

import com.github.fantasticlab.jdbc.executor.key.KeyGenerator;
import com.github.fantasticlab.jdbc.executor.mapping.*;
import com.github.fantasticlab.jdbc.util.reflection.MetaClass;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.executor.type.JdbcType;
import com.github.fantasticlab.jdbc.xml.parsing.ParsingException;

import java.util.*;

/**
 * MapperBuilderAssistant is builder for {@code MappedStatement}.
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

    public void addResultMap(String id, Class<?> type, List<ResultMapping> resultMappings) {
        id = applyCurrentNamespace(id, false);
        ResultMap.Builder resultMapBuilder = new ResultMap.Builder(id, type, resultMappings);
        ResultMap resultMap = resultMapBuilder.build();
        configuration.addResultMap(resultMap);
    }

    public void addMappedStatement(String id,
                                   SqlSource sqlSource,
                                   SqlCommandType sqlCommandType,
                                   Class<?> parameterType,
                                   Class<?> resultType,
                                   KeyGenerator keyGenerator) {
        /* Apply format as "namespace.id", such as com.github.fantasticlab.jdbc.test.bean.User.insert */
        id = applyCurrentNamespace(id, false);
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(
                configuration, id, sqlSource, sqlCommandType);
        statementBuilder.resource(resource);
        statementBuilder.keyGenerator(keyGenerator);
        /* Set Parameter of Statement */
        setStatementParameterMap(parameterType, statementBuilder);
        /* Set ResultType of Statement */
        setStatementResultType(resultType, statementBuilder);
        /* Add MappedStatement */
        configuration.addMappedStatement(statementBuilder.build());
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

    //2.2 resultType,一般用这个足矣了
    //<select id="selectUsers" resultType="User">
    //这种情况下,MyBatis 会在幕后自动创建一个 ResultMap,基于属性名来映射列到 JavaBean 的属性上。
    //如果列名没有精确匹配,你可以在列名上使用 select 字句的别名来匹配标签。
    //创建一个inline result map, 把resultType设上就OK了，
    //然后后面被DefaultResultSetHandler.createResultObject()使用
    //DefaultResultSetHandler.getRowValue()使用
    private void setStatementResultType(Class<?> resultType,
                                        MappedStatement.Builder statementBuilder) {
        List<ResultMap> resultMaps = new ArrayList<ResultMap>();
        ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(
                statementBuilder.id() + "-Inline",
                resultType,
                new ArrayList<>());
        resultMaps.add(inlineResultMapBuilder.build());
        statementBuilder.resultMaps(resultMaps);
    }

    public ResultMapping buildResultMapping(Class<?> resultType,
                                            String property,
                                            String column,
                                            Class<?> javaType,
                                            JdbcType jdbcType,
                                            List<ResultFlag> flags) {

        Class<?> javaTypeClass = resolveResultJavaType(resultType, property, javaType);
        ResultMapping.Builder builder = new ResultMapping.Builder(configuration, property, column, javaTypeClass);
        builder.jdbcType(jdbcType);
        builder.flags(flags == null ? new ArrayList<>() : flags);
        return builder.build();
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

}
