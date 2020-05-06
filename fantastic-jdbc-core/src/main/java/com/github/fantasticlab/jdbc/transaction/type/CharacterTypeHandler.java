package com.github.fantasticlab.jdbc.transaction.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Clinton Begin
 */
public class CharacterTypeHandler extends BaseTypeHandler<Character> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Character parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.toString());
    }

    @Override
    public Character getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String columnValue = rs.getString(columnName);
        if (columnValue != null) {
            return columnValue.charAt(0);
        } else {
            return null;
        }
    }

    @Override
    public Character getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String columnValue = rs.getString(columnIndex);
        if (columnValue != null) {
            return columnValue.charAt(0);
        } else {
            return null;
        }
    }

}
