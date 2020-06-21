package com.github.fantasticlab.jdbc.scripting;

import com.github.fantasticlab.jdbc.executor.mapping.BoundSql;
import com.github.fantasticlab.jdbc.executor.mapping.SqlSource;
import com.github.fantasticlab.jdbc.executor.mapping.SqlSourceBuilder;
import com.github.fantasticlab.jdbc.scripting.xmltags.DynamicContext;
import com.github.fantasticlab.jdbc.scripting.xmltags.SqlNode;
import com.github.fantasticlab.jdbc.session.Configuration;

/**
 * RawSqlSource is a static sql source,
 */
public class RawSqlSource implements SqlSource {

    private final SqlSource sqlSource;

    public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
        this(configuration, getSql(configuration, rootSqlNode), parameterType);
    }

    public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        sqlSource = sqlSourceParser.parse(sql, clazz);
    }

    private static String getSql(Configuration configuration, SqlNode rootSqlNode) {
        DynamicContext context = new DynamicContext(configuration, null);
        rootSqlNode.apply(context);
        return context.getSql();
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }

}
