package com.github.fantasticlab.jdbc.session.defaults;

import com.github.fantasticlab.jdbc.executor.Executor;
import com.github.fantasticlab.jdbc.session.*;
import com.github.fantasticlab.jdbc.transaction.Transaction;
import com.github.fantasticlab.jdbc.transaction.TransactionFactory;
import com.github.fantasticlab.jdbc.transaction.jdbc.JdbcTransactionFactory;

public class DefaultSqlSessionFactory implements SqlSessionFactory {


    private Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession(boolean autoCommint) {
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Transaction tx = transactionFactory.newTransaction(configuration.getDataSource(), TransactionIsolationLevel.READ_COMMITTED, autoCommint);
        final Executor executor = configuration.newExecutor(tx);
        return new DefaultSqlSession(this.configuration, executor);
    }
}
