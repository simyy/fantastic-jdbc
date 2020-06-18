package com.github.fantasticlab.jdbc.mapping;

public interface SqlSource {

    BoundSql getBoundSql(Object parameterObject);

}
