package com.github.fantasticlab.jdbc.executor.resultset;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


public interface ResultSetHandler {

    <E> List<E> handleResultSets(Statement stmt) throws SQLException;

    <E> List<E> handleResultSets(Statement stmt, Class<E> clazz) throws SQLException;

}
