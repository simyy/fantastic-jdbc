package com.github.fantasticlab.jdbc.executor.mapping;

public interface SqlSource {

    BoundSql getBoundSql(Object parameterObject);

}
