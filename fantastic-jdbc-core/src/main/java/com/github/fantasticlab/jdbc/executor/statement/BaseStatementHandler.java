package com.github.fantasticlab.jdbc.executor.statement;


import com.github.fantasticlab.jdbc.executor.Executor;
import com.github.fantasticlab.jdbc.exceptions.ExecutorException;
import com.github.fantasticlab.jdbc.executor.parameter.ParameterHandler;
import com.github.fantasticlab.jdbc.executor.resultset.ResultSetHandler;
import com.github.fantasticlab.jdbc.executor.mapping.BoundSql;
import com.github.fantasticlab.jdbc.executor.mapping.MappedStatement;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.executor.mapping.RowBounds;
import lombok.Data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Data
public abstract class BaseStatementHandler implements StatementHandler {

    protected Configuration configuration;
    /* Process ResultSet from DB */
    protected ResultSetHandler resultSetHandler;
    /* Process Parameter from Input */
    protected ParameterHandler parameterHandler;

    protected Executor executor;
    protected MappedStatement mappedStatement;
    protected RowBounds rowBounds;
    protected BoundSql boundSql;


    protected BaseStatementHandler(Executor executor,
                                   MappedStatement mappedStatement,
                                   Object parameterObject,
                                   RowBounds rowBounds,
                                   BoundSql boundSql) {

        this.configuration = mappedStatement.getConfiguration();
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.rowBounds = rowBounds;
        this.boundSql = boundSql;

        this.parameterHandler = configuration.FACTORY.newParameterHandler(mappedStatement, parameterObject, boundSql);
        this.resultSetHandler = configuration.FACTORY.newResultSetHandler(mappedStatement);
    }

    @Override
    public Statement prepare(Connection connection) throws SQLException {
        Statement statement = null;
        try {
              statement = instantiateStatement(connection);
              return statement;
        } catch (SQLException e) {
              closeStatement(statement);
              throw e;
        } catch (Exception e) {
              closeStatement(statement);
              throw new ExecutorException("Error preparing statement.  Cause: " + e, e);
        }
    }

    protected abstract Statement instantiateStatement(Connection connection) throws SQLException;

    protected void closeStatement(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
          //ignore
        }
    }
}
