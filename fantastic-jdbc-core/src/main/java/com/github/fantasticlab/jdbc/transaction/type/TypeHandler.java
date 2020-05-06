package com.github.fantasticlab.jdbc.transaction.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public interface TypeHandler<T> {

    /**
     * 设置参数：位置i替换为parameter
     *
     * @param ps
     * @param i
     * @param parameter
     * @param jdbcType
     * @throws SQLException
     */
    void setParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException;

    /**
     * 从结果集种获取列名为columnName的结果
     *
     * @param rs
     * @param columnName
     * @return
     * @throws SQLException
     */
    T getResult(ResultSet rs, String columnName) throws SQLException;

    /**
     * 从结果集种获取第columnIndex列的结果
     *
     * @param rs
     * @param columnIndex
     * @return
     * @throws SQLException
     */
    T getResult(ResultSet rs, int columnIndex) throws SQLException;

}
