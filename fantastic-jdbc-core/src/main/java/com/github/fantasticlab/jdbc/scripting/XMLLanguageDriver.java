package com.github.fantasticlab.jdbc.scripting;


import com.github.fantasticlab.jdbc.executor.parameter.DefaultParameterHandler;
import com.github.fantasticlab.jdbc.executor.parameter.ParameterHandler;
import com.github.fantasticlab.jdbc.mapping.BoundSql;
import com.github.fantasticlab.jdbc.mapping.MappedStatement;
import com.github.fantasticlab.jdbc.mapping.SqlSource;
import com.github.fantasticlab.jdbc.scripting.xmltags.XMLScriptBuilder;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.xml.parsing.XNode;

/**
 * XMLLanguageDriver is a driver for XML.
 */
public class XMLLanguageDriver implements LanguageDriver {

    @Override
    public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    }

    @Override
    public SqlSource createSqlSource(Configuration configuration, XNode context, Class<?> parameterType) {
        XMLScriptBuilder builder = new XMLScriptBuilder(configuration, context, parameterType);
        return builder.parseScriptNode();
    }

}
