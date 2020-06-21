package com.github.fantasticlab.jdbc.executor;

import com.github.fantasticlab.jdbc.exceptions.ExecutorException;
import com.github.fantasticlab.jdbc.executor.statement.StatementHandler;
import com.github.fantasticlab.jdbc.executor.mapping.BoundSql;
import com.github.fantasticlab.jdbc.executor.mapping.MappedStatement;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.executor.mapping.RowBounds;
import com.github.fantasticlab.jdbc.session.transaction.Transaction;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Slf4j
public class SimpleExecutor implements Executor {

    private Configuration configuration;
    private Transaction transaction;
    private boolean closed = false;

    public SimpleExecutor(Configuration configuration, Transaction transaction) {
        this.configuration = configuration;
        this.transaction = transaction;
    }

    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        checkClosed("Cannot update, transaction is already closed.");
        Statement stmt = null;
        try {
            BoundSql boundSql = ms.getBoundSql(parameter);
            Configuration configuration = ms.getConfiguration();
            StatementHandler handler = configuration.FACTORY.newStatementHandler(
                    this, ms, parameter, RowBounds.DEFAULT, boundSql);
            stmt = prepareStatement(handler);
            return handler.update(stmt);
        } finally {
            closeStatement(stmt);
        }
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException {
        checkClosed("Cannot query, transaction is already closed.");
        Statement stmt = null;
        try {
            BoundSql boundSql = ms.getBoundSql(parameter);
            Configuration configuration = ms.getConfiguration();
            StatementHandler handler = configuration.FACTORY.newStatementHandler(this, ms, parameter, rowBounds, boundSql);
            stmt = prepareStatement(handler);
            return handler.<E>query(stmt);
        } finally {
            closeStatement(stmt);
        }
    }

    @Override
    public void commit(boolean required) throws SQLException {
        checkClosed("Cannot commit, transaction is already closed.");
//        flushStatements();
        if (required) {
            transaction.commit();
        }
    }

    @Override
    public void rollback(boolean required) throws SQLException {
        if (!closed) {
            try {
//                flushStatements();
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
    public boolean isClosed() {
        return closed;
    }

    @Override
    public Transaction getTransaction() {
        checkClosed(null);
        return this.transaction;
    }

    private void checkClosed(String errMsg) {
        if (closed) {
            throw new ExecutorException(
                    errMsg == null ? "Executor was closed." : errMsg);
        }
    }

    private void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }

    private Statement prepareStatement(StatementHandler handler) throws SQLException {
        Statement stmt;
        Connection connection = transaction.getConnection();
        stmt = handler.prepare(connection);
        handler.parameterize(stmt);
        return stmt;
    }

}
