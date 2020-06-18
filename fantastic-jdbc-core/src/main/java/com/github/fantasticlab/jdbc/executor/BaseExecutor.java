package com.github.fantasticlab.jdbc.executor;


import com.github.fantasticlab.jdbc.mapping.MappedStatement;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.session.ResultHandler;
import com.github.fantasticlab.jdbc.session.RowBounds;
import com.github.fantasticlab.jdbc.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

public abstract class BaseExecutor implements Executor {

    protected Configuration configuration;
    protected Transaction transaction;
    protected boolean closed;


    protected BaseExecutor(Configuration configuration, Transaction transaction) {
        this.transaction = transaction;
        this.configuration = configuration;
        this.closed = false;
    }


    protected abstract int doUpdate(MappedStatement ms, Object parameter) throws SQLException;

    protected abstract <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException;


    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        checkClosed();
        return doUpdate(ms, parameter);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        checkClosed();
        return doQuery(ms, parameter, rowBounds, resultHandler);
    }

    @Override
    public void commit(boolean required) throws SQLException {
        checkClosed();
        flushStatements();
        if (required) {
            transaction.commit();
        }
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        if (!closed) {
            try {
                flushStatements();
            } finally {
                if (required) {
                    transaction.rollback();
                }
            }
        }
    }

    @Override
    public void close(boolean forceRollback) {
        try {
            try {
                rollback(forceRollback);
            } finally {
                if (transaction != null) {
                    transaction.close();
                }
            }
        } catch (SQLException e) {
            // Ignore.  There's nothing that can be done at this point.

        } finally {
            transaction = null;
            closed = true;
        }
    }

    @Override
    public List<BatchResult> flushStatements() throws SQLException {
        return null;
    }

    private void checkClosed() {
        if (closed) {
            throw new ExecutorException("Executor was closed.");
        }
    }

}
