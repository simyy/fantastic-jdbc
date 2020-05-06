package com.github.fantasticlab.jdbc.executor.resultset;


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
 * 默认Map结果处理器
 */
public class DefaultResultSetHandler implements ResultSetHandler {

    private Executor executor;
    private MappedStatement mappedStatement;
    private ParameterHandler parameterHandler;
    private ResultHandler resultHandler;
    private BoundSql boundSql;
    private RowBounds rowBounds;

    public DefaultResultSetHandler() {
    }

    public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, ParameterHandler parameterHandler, ResultHandler resultHandler, BoundSql boundSql, RowBounds rowBounds) {
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.parameterHandler = parameterHandler;
        this.resultHandler = resultHandler;
        this.boundSql = boundSql;
        this.rowBounds = rowBounds;
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
            if (null == resultSet)
            {
                return null;
            }

            while (resultSet.next())
            {
                // 通过反射实例化返回类
                E entity = (E) clazz.newInstance();
                Field[] declaredFields = clazz.getDeclaredFields();

                for (Field field : declaredFields)
                {
                    // 对成员变量赋值
                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();

                    // 目前只实现了string和int转换
                    if (String.class.equals(fieldType))
                    {
                        field.set(entity, resultSet.getString(field.getName()));
                    }
                    else if (int.class.equals(fieldType) || Integer.class.equals(fieldType))
                    {
                        field.set(entity, resultSet.getInt(field.getName()));
                    }
                    else
                    {
                        // 其他类型自己转换，这里就直接设置了
                        field.set(entity, resultSet.getObject(field.getName()));
                    }
                }
                result.add(entity);
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
