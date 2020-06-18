package com.github.fantasticlab.jdbc.executor.parameter;


import com.github.fantasticlab.jdbc.mapping.BoundSql;
import com.github.fantasticlab.jdbc.mapping.MappedStatement;
import com.github.fantasticlab.jdbc.mapping.ParameterMapping;
import com.github.fantasticlab.jdbc.mapping.ParameterMode;
import com.github.fantasticlab.jdbc.reflection.MetaObject;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.executor.type.JdbcType;
import com.github.fantasticlab.jdbc.executor.type.TypeHandler;
import com.github.fantasticlab.jdbc.executor.type.TypeHandlerRegistry;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;


public class DefaultParameterHandler implements ParameterHandler {

    private final TypeHandlerRegistry typeHandlerRegistry;

    private final MappedStatement mappedStatement;
    private final Object parameterObject;
    private BoundSql boundSql;
    private Configuration configuration;


    public DefaultParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        this.mappedStatement = mappedStatement;
        this.configuration = mappedStatement.getConfiguration();
        this.typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
        this.parameterObject = parameterObject;
        this.boundSql = boundSql;
    }

    @Override
    public Object getParameterObject() {
        return parameterObject;
    }

    //设置参数
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
                        MetaObject metaObject = configuration.newMetaObject(parameterObject);
                        value = metaObject.getValue(parameterMapping.getProperty());
                    }
                    TypeHandler typeHandler = parameterMapping.getTypeHandler();
                    JdbcType jdbcType = parameterMapping.getJdbcType();
                    if (value == null && jdbcType == null) {
                        //不同类型的set方法不同，所以委派给子类的setParameter方法
                        jdbcType = configuration.getJdbcTypeForNull();
                    }
                    typeHandler.setParameter(ps, i + 1, value, jdbcType);
                }
            }
        }
    }

}
