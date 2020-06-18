package com.github.fantasticlab.jdbc.session.defaults;

import com.github.fantasticlab.jdbc.executor.Executor;
import com.github.fantasticlab.jdbc.session.*;
import com.github.fantasticlab.jdbc.transaction.Transaction;
import com.github.fantasticlab.jdbc.transaction.JdbcTransaction;

/**
 * DefaultSqlSessionFactory is a implement of SqlSessionFactory.
 * In DefaultSqlSessionFactory, {@code Configuration} is an application context.
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession(boolean autocommit) {
        Transaction tx = new JdbcTransaction(configuration.getDataSource(), TransactionIsolationLevel.READ_COMMITTED, autocommit);
        return new DefaultSqlSession(this.configuration, configuration.FACTORY.newExecutor(tx));
    }
}
