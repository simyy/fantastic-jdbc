package com.github.fantasticlab.jdbc.scripting;


import com.github.fantasticlab.jdbc.executor.parameter.ParameterHandler;
import com.github.fantasticlab.jdbc.mapping.BoundSql;
import com.github.fantasticlab.jdbc.mapping.MappedStatement;
import com.github.fantasticlab.jdbc.mapping.SqlSource;
import com.github.fantasticlab.jdbc.scripting.xmltags.XMLScriptBuilder;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.xml.parsing.XNode;

/**
 * XML语言驱动
 */
public class XMLLanguageDriver implements LanguageDriver {

    @Override
    public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        //返回默认的参数处理器
        return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    }

    @Override
    public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
        //用XML脚本构建器解析
        XMLScriptBuilder builder = new XMLScriptBuilder(configuration, script, parameterType);
        return builder.parseScriptNode();
    }

}
