package com.github.fantasticlab.jdbc.session;

import com.github.fantasticlab.jdbc.session.transaction.Transaction;
import com.github.fantasticlab.jdbc.session.transaction.JdbcTransaction;

/**
 * SqlSessionFactory1 is a Factory for {@code SqlSession},
 * which is implemented by <strong>Factory Pattern</strong>.
 * {@code Configuration} is the core context for application.
 */
public class SqlSessionFactory {

    private Configuration configuration;

    public SqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    public SqlSession openSession(boolean autocommit) {
        Transaction tx = new JdbcTransaction(configuration.getDataSource(), TransactionIsolationLevel.READ_COMMITTED, autocommit);
        return new DefaultSqlSession(this.configuration, configuration.FACTORY.newExecutor(tx));
    }
}
