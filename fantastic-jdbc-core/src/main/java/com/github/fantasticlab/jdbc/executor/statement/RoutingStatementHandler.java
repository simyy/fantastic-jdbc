package com.github.fantasticlab.jdbc.executor.statement;

import com.github.fantasticlab.jdbc.executor.Executor;
import com.github.fantasticlab.jdbc.executor.parameter.ParameterHandler;
import com.github.fantasticlab.jdbc.mapping.BoundSql;
import com.github.fantasticlab.jdbc.mapping.MappedStatement;
import com.github.fantasticlab.jdbc.session.ResultHandler;
import com.github.fantasticlab.jdbc.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class RoutingStatementHandler implements StatementHandler {

    private StatementHandler delegate;

    public RoutingStatementHandler(Executor executor,
                                   MappedStatement ms,
                                   Object parameter,
                                   RowBounds rowBounds,
                                   ResultHandler resultHandler,
                                   BoundSql boundSql) {

        switch (ms.getStatementType()) {
            case STATEMENT:
                delegate = new SimpleStatementHandler(
                        executor, ms, parameter, rowBounds, resultHandler, boundSql);
                break;
            case PREPARED:
                delegate = new PreparedStatementHandler(
                        executor, ms, parameter, rowBounds, resultHandler, boundSql);
                break;
            default:
                throw new RuntimeException("Unknown statement type: " + ms.getStatementType());

        }
    }

    @Override
    public Statement prepare(Connection connection) throws SQLException {
        return delegate.prepare(connection);
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        delegate.parameterize(statement);
    }

    @Override
    public void batch(Statement statement) throws SQLException {
        delegate.batch(statement);
    }

    @Override
    public int update(Statement statement) throws SQLException {
        return delegate.update(statement);
    }

    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        return delegate.query(statement, resultHandler);
    }

    @Override
    public BoundSql getBoundSql() {
        return delegate.getBoundSql();
    }

    @Override
    public ParameterHandler getParameterHandler() {
        return delegate.getParameterHandler();
    }
}
