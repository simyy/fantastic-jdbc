//package com.github.fantasticlab.jdbc.executor;
//
//import com.github.fantasticlab.jdbc.executor.mapping.MappedStatement;
//import lombok.Data;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Data
//public class BatchResult {
//
//    private final MappedStatement mappedStatement;
//    private final String sql;
//    private final List<Object> parameterObjects;
//    private int[] updateCounts;
//
//    public BatchResult(MappedStatement mappedStatement, String sql) {
//        super();
//        this.mappedStatement = mappedStatement;
//        this.sql = sql;
//        this.parameterObjects = new ArrayList<Object>();
//    }
//
//    public BatchResult(MappedStatement mappedStatement, String sql, Object parameterObject) {
//        this(mappedStatement, sql);
//        addParameterObject(parameterObject);
//    }
//
//    public void addParameterObject(Object parameterObject) {
//        this.parameterObjects.add(parameterObject);
//    }
//
//}
