package com.github.fantasticlab.jdbc.session;


import com.github.fantasticlab.jdbc.session.defaults.DefaultSqlSessionFactory;
import com.github.fantasticlab.jdbc.xml.XMLConfigBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SqlSessionFactoryBuilder {

    public static SqlSessionFactory build(String fileName) {
        InputStream inputStream = SqlSessionFactoryBuilder.class.getClassLoader().getResourceAsStream(fileName);
        return build(inputStream);
    }


    public static SqlSessionFactory build(InputStream inputStream) {
        XMLConfigBuilder parser = new XMLConfigBuilder(inputStream);
        return new DefaultSqlSessionFactory(parser.parse());
    }

    public static void main(String[] args) {
        SqlSessionFactory factory = SqlSessionFactoryBuilder.build("JdbcConfig.xml");
        SqlSession sqlSession = factory.openSession(false);
        assert sqlSession != null;
    }

}
