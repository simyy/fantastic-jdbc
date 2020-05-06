package com.github.fantasticlab.jdbc.transaction.type;

import java.sql.*;


public class ArrayTypeHandler extends BaseTypeHandler<Object> {

    public ArrayTypeHandler() {
        super();
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        ps.setArray(i, (Array) parameter);
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Array array = rs.getArray(columnName);
        return array == null ? null : array.getArray();
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Array array = rs.getArray(columnIndex);
        return array == null ? null : array.getArray();
    }

}
