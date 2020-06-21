package com.github.fantasticlab.jdbc.scripting;

import com.github.fantasticlab.jdbc.executor.parameter.ParameterHandler;
import com.github.fantasticlab.jdbc.executor.mapping.BoundSql;
import com.github.fantasticlab.jdbc.executor.mapping.MappedStatement;
import com.github.fantasticlab.jdbc.executor.mapping.SqlSource;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.xml.parsing.XNode;

/**
 * LanguageDriver is to resolve SQL configuration.
 */
public interface LanguageDriver {

    ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);

    SqlSource createSqlSource(Configuration configuration, XNode context, Class<?> parameterType);

}
