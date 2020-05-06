package com.github.fantasticlab.jdbc.mapping;

import com.github.fantasticlab.jdbc.session.Configuration;

import java.util.List;

/**
 * 静态SQL
 */
public class StaticSqlSource implements SqlSource {

    // SQL源码 参数用?替代
    private String sql;
    // 参数列表
    private List<ParameterMapping> parameterMappings;
    private Configuration configuration;

    public StaticSqlSource(Configuration configuration, String sql) {
        this(configuration, sql, null);
    }

    public StaticSqlSource(Configuration configuration, String sql, List<ParameterMapping> parameterMappings) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return new BoundSql(configuration, sql, parameterMappings, parameterObject);
    }

}
