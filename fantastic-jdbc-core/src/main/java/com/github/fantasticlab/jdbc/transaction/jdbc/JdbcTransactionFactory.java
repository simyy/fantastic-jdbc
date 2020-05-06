package com.github.fantasticlab.jdbc.transaction.jdbc;


import com.github.fantasticlab.jdbc.session.TransactionIsolationLevel;
import com.github.fantasticlab.jdbc.transaction.Transaction;
import com.github.fantasticlab.jdbc.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

public class JdbcTransactionFactory implements TransactionFactory {

    @Override
    public void setProperties(Properties props) {
    }

    @Override
    public Transaction newTransaction(Connection conn) {
        return new JdbcTransaction(conn);
    }

    @Override
    public Transaction newTransaction(DataSource ds, boolean autoCommit) {
        return new JdbcTransaction(ds, TransactionIsolationLevel.NONE, autoCommit);
    }

    @Override
    public Transaction newTransaction(DataSource ds, TransactionIsolationLevel level, boolean autoCommit) {
        return new JdbcTransaction(ds, level, autoCommit);
    }
}
