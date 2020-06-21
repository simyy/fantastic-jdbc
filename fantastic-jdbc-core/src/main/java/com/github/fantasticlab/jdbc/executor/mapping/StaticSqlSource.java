package com.github.fantasticlab.jdbc.executor.mapping;

import com.github.fantasticlab.jdbc.session.Configuration;

import java.util.List;

/**
 * StaticSqlSource is
 */
public class StaticSqlSource implements SqlSource {


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
        return new BoundSql(sql, parameterMappings, parameterObject);
    }

}
