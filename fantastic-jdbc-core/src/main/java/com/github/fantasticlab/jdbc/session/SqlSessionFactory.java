package com.github.fantasticlab.jdbc.session;

/**
 * SqlSessionFactory is a Factory for SqlSession,
 * which is implemented by <strong>Factory Pattern</strong>.
 *
 * SqlSessionFactory.openSession is used to get a {@code SqlSession} from SqlSessionFactory.
 *
 */
public interface SqlSessionFactory {

    SqlSession openSession(boolean autocommit);

}
