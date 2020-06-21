package com.github.fantasticlab.jdbc.executor.statement;


import com.github.fantasticlab.jdbc.executor.Executor;
import com.github.fantasticlab.jdbc.executor.key.KeyGenerator;
import com.github.fantasticlab.jdbc.executor.resultset.DefaultResultSetHandler;
import com.github.fantasticlab.jdbc.executor.resultset.ResultSetHandler;
import com.github.fantasticlab.jdbc.executor.mapping.BoundSql;
import com.github.fantasticlab.jdbc.executor.mapping.MappedStatement;
import com.github.fantasticlab.jdbc.executor.mapping.RowBounds;
import com.github.fantasticlab.jdbc.test.bean.User;

import java.sql.*;
import java.util.List;


public class SimpleStatementHandler extends BaseStatementHandler {


    public SimpleStatementHandler(Executor executor,
                                  MappedStatement mappedStatement,
                                  Object parameter,
                                  RowBounds rowBounds,
                                  BoundSql boundSql) {
        super(executor, mappedStatement, parameter, rowBounds, boundSql);
    }

    @Override
    protected Statement instantiateStatement(Connection connection) throws SQLException {
        return connection.createStatement();
    }

    @Override
    public void parameterize(Statement statement) throws SQLException {
        // ignore
    }

    @Override
    public void batch(Statement statement) throws SQLException {
        String sql = boundSql.getSql();
        statement.addBatch(sql);
    }

    @Override
    public int update(Statement statement) throws SQLException {
          String sql = boundSql.getSql();
          statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
          Object parameterObject = boundSql.getParameterObject();
          // return primary key
          KeyGenerator keyGenerator = mappedStatement.getKeyGenerator();
          keyGenerator.processAfter(executor, mappedStatement, statement, parameterObject);
          return statement.getUpdateCount();
    }

    @Override
    public <E> List<E> query(Statement statement) throws SQLException {
        String sql = boundSql.getSql();
        statement.execute(sql);
        return resultSetHandler.<E>handleResultSets(statement);
    }

    public static void main(String[] args) throws SQLException {


        Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8",
                "root",
                "123");
        Statement stmt = con.createStatement();

        stmt.executeQuery("select * from user where id = 1");

        ResultSet resultSet = stmt.getResultSet();
        assert resultSet != null;

        ResultSetHandler resultSetHandler = new DefaultResultSetHandler();
        List<User> users = resultSetHandler.handleResultSets(stmt, User.class);
        assert users != null;

    }
}
