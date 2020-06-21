package com.github.fantasticlab.jdbc.session;


import com.github.fantasticlab.jdbc.xml.XMLConfigBuilder;

import java.io.InputStream;

/**
 * Use Builder Pattern to build a <code>SqlSessionFactory1</code>.
 *
 * <code>SqlSessionFactory1</code> provide two load way for XML config:
 * 1. build from resource;
 * 2. build from inputStream;
 *
 * Use {@code XMLConfigBuilder} to parse XML config.
 */
public class SqlSessionFactoryBuilder {

    public static SqlSessionFactory build(String resource) {
        InputStream inputStream = SqlSessionFactoryBuilder.class.getClassLoader().getResourceAsStream(resource);
        return build(inputStream);
    }

    public static SqlSessionFactory build(InputStream inputStream) {
        XMLConfigBuilder parser = new XMLConfigBuilder(inputStream);
        /* XMLConfigBuilder.parseConfiguration() is used to parse the xml config. */
        return new SqlSessionFactory(parser.parseConfiguration());
    }

    public static void main(String[] args) {
        SqlSessionFactory factory = SqlSessionFactoryBuilder.build("JdbcConfig.xml");
        SqlSession sqlSession = factory.openSession(false);
        assert sqlSession != null;
    }

}
