package com.github.fantasticlab.jdbc;

import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.session.SqlSession;
import com.github.fantasticlab.jdbc.session.SqlSessionFactory;
import com.github.fantasticlab.jdbc.session.SqlSessionFactoryBuilder;
import com.github.fantasticlab.jdbc.test.bean.User;
import com.github.fantasticlab.jdbc.test.dao.UserMapper;

/**
 * Hello world!
 *
 */
public class Main {


    public static void main( String[] args ) {

        System.out.println( "Hello World!" );

        SqlSessionFactory factory = SqlSessionFactoryBuilder.build("JdbcConfig.xml");
        SqlSession sqlSession = factory.openSession(false);
        assert sqlSession != null;

        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        assert userMapper != null;

        userMapper.getUser(1l);

        User user = new User();
        user.setName("test111");
        Long id = userMapper.insert(user);
        assert id != null;

        user = userMapper.getUser(id);
        assert user != null;

    }
}
