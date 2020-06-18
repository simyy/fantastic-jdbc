package com.github.fantasticlab.jdbc.executor;

import com.github.fantasticlab.jdbc.executor.statement.StatementHandler;
import com.github.fantasticlab.jdbc.mapping.BoundSql;
import com.github.fantasticlab.jdbc.mapping.MappedStatement;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.session.ResultHandler;
import com.github.fantasticlab.jdbc.session.RowBounds;
import com.github.fantasticlab.jdbc.transaction.Transaction;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Slf4j
public class SimpleExecutor extends BaseExecutor {

    public SimpleExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
        this.configuration = configuration;
        this.transaction = transaction;
    }

    @Override
    public int doUpdate(MappedStatement ms, Object parameter) throws SQLException {
        Statement stmt = null;
        try {
            BoundSql boundSql = ms.getBoundSql(parameter);
            Configuration configuration = ms.getConfiguration();
            StatementHandler handler = configuration.FACTORY.newStatementHandler(
                    this, ms, parameter, RowBounds.DEFAULT, null, boundSql);
            stmt = prepareStatement(handler);
            return handler.update(stmt);
        } finally {
            closeStatement(stmt);
        }
    }

    @Override
    public <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        Statement stmt = null;
        try {
            BoundSql boundSql = ms.getBoundSql(parameter);
            Configuration configuration = ms.getConfiguration();
            StatementHandler handler = configuration.FACTORY.newStatementHandler(this, ms, parameter, rowBounds, resultHandler, boundSql);
            stmt = prepareStatement(handler);
            return handler.<E>query(stmt, resultHandler);
        } finally {
            closeStatement(stmt);
        }
    }

    @Override
    public void commit(boolean required) throws SQLException {
        checkClosed("Cannot commit, transaction is already closed.");
        transaction.commit();
    }

    @Override
    public void rollback(boolean required) throws SQLException {

    }

    @Override
    public void close(boolean forceRollback) {

    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public Transaction getTransaction() {
        checkClosed(null);
        return this.transaction;
    }

    @Override
    public List<BatchResult> flushStatements() throws SQLException {
        return null;
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
