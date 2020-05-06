package com.github.fantasticlab.jdbc.session;

public interface SqlSessionFactory {

    SqlSession openSession(boolean autocommit);

}
