package com.github.fantasticlab.jdbc.executor;

import com.github.fantasticlab.jdbc.executor.mapping.MappedStatement;
import com.github.fantasticlab.jdbc.executor.mapping.RowBounds;
import com.github.fantasticlab.jdbc.session.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * {@code MappedStatement} Executor,
 * contains basic execute and transaction operate.
 */
public interface Executor {

    int update(MappedStatement ms, Object parameter) throws SQLException;

    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds)  throws SQLException;

    void commit(boolean required) throws SQLException;

    void rollback(boolean required) throws SQLException;

    void close(boolean forceRollback);

    boolean isClosed();

    Transaction getTransaction();

//    List<BatchResult> flushStatements() throws SQLException;

}
