package com.github.fantasticlab.jdbc.executor.resultset;


import com.github.fantasticlab.jdbc.exceptions.ResultSetException;
import com.github.fantasticlab.jdbc.executor.Executor;
import com.github.fantasticlab.jdbc.executor.parameter.ParameterHandler;
import com.github.fantasticlab.jdbc.mapping.*;
import com.github.fantasticlab.jdbc.session.ResultHandler;
import com.github.fantasticlab.jdbc.session.RowBounds;
import com.github.fantasticlab.jdbc.test.bean.User;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


/**
 * DefaultResultSetHandler is the default result handler.
 */
public class DefaultResultSetHandler implements ResultSetHandler {

    private MappedStatement mappedStatement;

    public DefaultResultSetHandler() {
    }

    public DefaultResultSetHandler(MappedStatement mappedStatement) {
        this.mappedStatement = mappedStatement;
    }

    @Override
    public <E> List<E> handleResultSets(Statement stmt) throws SQLException {
        Class clazz = mappedStatement.getResultMaps().get(0).getType();
        return handleResultSets(stmt, clazz);
    }

    @Override
    public <E> List<E> handleResultSets(Statement stmt, Class<E> clazz) throws SQLException {
        try
        {
            List<E> result = new ArrayList<>();
            ResultSet resultSet = stmt.getResultSet();
            if (null == resultSet) {
                return null;
            }
            while (resultSet.next()) {
                E entity = (E) clazz.newInstance();
                Field[] declaredFields = clazz.getDeclaredFields();

                for (Field field : declaredFields) {
                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();
                    /* Only implement the type of string and int, others TODO */
                    if (String.class.equals(fieldType)) {
                        field.set(entity, resultSet.getString(field.getName()));
                    } else if (int.class.equals(fieldType) || Integer.class.equals(fieldType)) {
                        field.set(entity, resultSet.getInt(field.getName()));
                    } else {
                        field.set(entity, resultSet.getObject(field.getName()));
                    }
                }
                result.add(entity);
            }
            return result;
        } catch (Exception e) {
            throw new ResultSetException("HandleResultSets error cause:" + e);
        }
    }
}
