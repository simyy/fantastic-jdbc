package com.github.fantasticlab.jdbc.executor.statement;

import com.github.fantasticlab.jdbc.executor.Executor;
import com.github.fantasticlab.jdbc.mapping.BoundSql;
import com.github.fantasticlab.jdbc.mapping.MappedStatement;
import com.github.fantasticlab.jdbc.session.ResultHandler;
import com.github.fantasticlab.jdbc.session.RowBounds;

import java.sql.*;
import java.util.List;

public class PreparedStatementHandler extends BaseStatementHandler {


    public PreparedStatementHandler(Executor executor,
                                    MappedStatement mappedStatement,
                                    Object parameterObject,
                                    RowBounds rowBounds,
                                    ResultHandler resultHandler,
                                    BoundSql boundSql) {

        super(executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
    }

    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        String sql = boundSql.getSql();
        return connection.prepareStatement(sql);
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        parameterHandler.setParameters((PreparedStatement) statement);
    }

    @Override
    public void batch(Statement statement) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.addBatch();
    }

    @Override
    public int update(Statement statement) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ps.execute();
        int rows = ps.getUpdateCount();
        return rows;
    }

    @Override
    public <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException {
        PreparedStatement ps = (PreparedStatement) statement;
        ResultSet rs = ps.executeQuery();
        return resultSetHandler.<E> handleResultSets(ps);
    }
}
