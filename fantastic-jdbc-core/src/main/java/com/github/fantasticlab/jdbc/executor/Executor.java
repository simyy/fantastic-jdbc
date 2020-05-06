package com.github.fantasticlab.jdbc.executor;

import com.github.fantasticlab.jdbc.mapping.MappedStatement;
import com.github.fantasticlab.jdbc.session.ResultHandler;
import com.github.fantasticlab.jdbc.session.RowBounds;
import com.github.fantasticlab.jdbc.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

public interface Executor {

    int update(MappedStatement ms, Object parameter) throws SQLException;

    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler)  throws SQLException;

    void commit(boolean required) throws SQLException;

    void rollback(boolean required) throws SQLException;

    void close(boolean forceRollback);

    boolean isClosed();

    Transaction getTransaction();

    List<BatchResult> flushStatements() throws SQLException;

}
