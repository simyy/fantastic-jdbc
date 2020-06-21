package com.github.fantasticlab.jdbc.session;

import com.github.fantasticlab.jdbc.exceptions.TooManyResultsException;
import com.github.fantasticlab.jdbc.executor.Executor;
import com.github.fantasticlab.jdbc.exceptions.ExecutorException;
import com.github.fantasticlab.jdbc.executor.mapping.MappedStatement;
import com.github.fantasticlab.jdbc.executor.mapping.RowBounds;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * DefaultSqlSession use {@code Executor} to implement specific functions.
 */
public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;
    private Executor executor;


    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }


    @Override
    public <T> T selectOne(String statement, Object parameter) {
        List<T> list = this.<T>selectList(statement, parameter, RowBounds.DEFAULT);
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() > 1) {
            throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
        } else {
            return null;
        }
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds) {
        try {
            MappedStatement ms = configuration.getMappedStatement(statement);
            return executor.query(ms, wrapCollection(parameter), rowBounds);
        } catch (Exception e) {
            throw new ExecutorException("Error querying database.  Cause: " + e, e);
        } finally {
            // ignore ErrorContext
        }
    }

    @Override
    public void select(String statement, Object parameter, RowBounds rowBounds) {
        try {
            MappedStatement ms = configuration.getMappedStatement(statement);
            executor.query(ms, wrapCollection(parameter), rowBounds);
        } catch (Exception e) {
            throw new ExecutorException("Error querying database.  Cause: " + e, e);
        } finally {
            // ignore ErrorContext
        }
    }

    @Override
    public int update(String statement, Object parameter) {
        try {
            MappedStatement ms = configuration.getMappedStatement(statement);
            return executor.update(ms, wrapCollection(parameter));
        } catch (Exception e) {
            throw new ExecutorException("Error updating database.  Cause: " + e, e);
        } finally {
            // ignore ErrorContext
        }
    }

    @Override
    public int insert(String statement, Object parameter) {
        return update(statement, parameter);
    }

    @Override
    public void commit() {
        try {
            executor.commit(true);
        } catch (Exception e) {
            throw new ExecutorException("Error committing transaction.  Cause: " + e, e);
        } finally {
            // ignore ErrorContext
        }
    }

    @Override
    public void rollback() {
        try {
            executor.rollback(true);
        } catch (Exception e) {
            throw new ExecutorException("Error rolling back transaction.  Cause: " + e, e);
        } finally {
            // ignore ErrorContext
        }
    }

    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.<T>getMapper(type, this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    private Object wrapCollection(final Object object) {
        if (object instanceof Collection) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("collection", object);
            if (object instanceof List) {
                map.put("list", object);
            }
            return map;
        } else if (object != null && object.getClass().isArray()) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("array", object);
            return map;
        }
        return object;
    }
}
