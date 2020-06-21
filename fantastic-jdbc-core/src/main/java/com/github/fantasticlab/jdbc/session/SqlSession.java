package com.github.fantasticlab.jdbc.session;

import com.github.fantasticlab.jdbc.executor.mapping.RowBounds;

import java.util.List;


/**
 * SqlSession is a SQL request manager.
 */
public interface SqlSession {

    <T> T selectOne(String statement, Object parameter);

    <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds);

    void select(String statement, Object parameter, RowBounds rowBounds);

    int update(String statement, Object parameter);

    int insert(String statement, Object parameter);

    void commit();

    void rollback();

    <T> T getMapper(Class<T> type);

    Configuration getConfiguration();
}
