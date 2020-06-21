package com.github.fantasticlab.jdbc.executor.statement;

import com.github.fantasticlab.jdbc.executor.parameter.ParameterHandler;
import com.github.fantasticlab.jdbc.executor.mapping.BoundSql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * StatementHandler is a interface for statement execute,
 * and it contains multi operations, such as
 * 1> <strong>prepare</strong>
 * 2> <strong>parameterize</strong>
 * 3> <strong>batch|update|query</strong>
 */
public interface StatementHandler {

    Statement prepare(Connection connection) throws SQLException;

    void parameterize(Statement statement) throws SQLException;

    void batch(Statement statement) throws SQLException;

    int update(Statement statement) throws SQLException;

    <E> List<E> query(Statement statement) throws SQLException;

    BoundSql getBoundSql();

    ParameterHandler getParameterHandler();
}
