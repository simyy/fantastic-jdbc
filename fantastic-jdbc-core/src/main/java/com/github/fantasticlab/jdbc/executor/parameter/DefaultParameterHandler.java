package com.github.fantasticlab.jdbc.executor.parameter;


import com.github.fantasticlab.jdbc.executor.mapping.BoundSql;
import com.github.fantasticlab.jdbc.executor.mapping.MappedStatement;
import com.github.fantasticlab.jdbc.executor.mapping.ParameterMapping;
import com.github.fantasticlab.jdbc.executor.mapping.ParameterMode;
import com.github.fantasticlab.jdbc.util.reflection.MetaObject;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.executor.type.JdbcType;
import com.github.fantasticlab.jdbc.executor.type.TypeHandler;
import com.github.fantasticlab.jdbc.executor.type.TypeHandlerRegistry;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;


public class DefaultParameterHandler implements ParameterHandler {

    private final TypeHandlerRegistry typeHandlerRegistry;

    private final Object parameterObject;
    private BoundSql boundSql;
    private Configuration configuration;


    public DefaultParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        this.configuration = mappedStatement.getConfiguration();
        this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
        this.parameterObject = parameterObject;
        this.boundSql = boundSql;
    }

    @Override
    public void setParameters(PreparedStatement ps) throws SQLException {
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings != null) {
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() == ParameterMode.IN) {
                    Object value;
                    if (parameterObject == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                        // Recognized Type
                        value = parameterObject;
                    } else {
                        // Unknown Type must be a Object, then get by property name.
                        MetaObject metaObject = configuration.FACTORY.newMetaObject(parameterObject);
                        value = metaObject.getValue(parameterMapping.getProperty());
                    }
                    TypeHandler typeHandler = parameterMapping.getTypeHandler();
                    JdbcType jdbcType = parameterMapping.getJdbcType();
                    if (value == null && jdbcType == null) {
                        jdbcType = configuration.JDBC_TYPE_FOR_NULL;
                    }
                    typeHandler.setParameter(ps, i + 1, value, jdbcType);
                }
            }
        }
    }

}
