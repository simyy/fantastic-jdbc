package com.github.fantasticlab.jdbc.test.dao;

import com.github.fantasticlab.jdbc.test.bean.User;

import java.util.List;

public interface UserMapper {
    Long insert(User user);
    User getUser(Long id);
    List<User> getAll();
    void updateUser(Long id);
}
