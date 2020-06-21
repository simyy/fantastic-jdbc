package com.github.fantasticlab.jdbc.executor.mapping;


import lombok.Getter;

import java.util.List;

@Getter
public class BoundSql {

    /* Source SQL, such as "select * from user where id = ?" */
    private String sql;
    /* Parameter Mappings, such {property="id"} */
    private List<ParameterMapping> parameterMappings;
    /* Parameter Object, such as Map, int, string */
    private Object parameterObject;

    public BoundSql(String sql, List<ParameterMapping> parameterMappings, Object parameterObject) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.parameterObject = parameterObject;
    }


}
