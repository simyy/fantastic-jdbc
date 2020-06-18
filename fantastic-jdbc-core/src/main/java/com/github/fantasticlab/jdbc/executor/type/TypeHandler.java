package com.github.fantasticlab.jdbc.executor.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public interface TypeHandler<T> {

    void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

    T getResult(ResultSet rs, String columnName) throws SQLException;

    T getResult(ResultSet rs, int columnIndex) throws SQLException;

}
