//package com.github.fantasticlab.jdbc.transaction.type;
//
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//
//@MappedTypes(String.class)
//@MappedJdbcTypes(value = {JdbcType.CHAR, JdbcType.VARCHAR}, includeNullJdbcType = true)
//public class StringTrimmingTypeHandler implements TypeHandler<String> {
//
//    @Override
//    public void setParameter(PreparedStatement ps, int i, String parameter,
//                             JdbcType jdbcType) throws SQLException {
//        ps.setString(i, trim(parameter));
//    }
//
//    @Override
//    public String getResult(ResultSet rs, String columnName) throws SQLException {
//        return trim(rs.getString(columnName));
//    }
//
//    @Override
//    public String getResult(ResultSet rs, int columnIndex) throws SQLException {
//        return trim(rs.getString(columnIndex));
//    }
//
//    private String trim(String s) {
//        if (s == null) {
//            return null;
//        } else {
//            return s.trim();
//        }
//    }
//}
