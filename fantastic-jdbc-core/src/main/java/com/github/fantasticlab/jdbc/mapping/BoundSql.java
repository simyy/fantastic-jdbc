package com.github.fantasticlab.jdbc.mapping;


import com.github.fantasticlab.jdbc.session.Configuration;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class BoundSql {

    private String sql;
    private List<ParameterMapping> parameterMappings;
    private Object parameterObject;
    private Map<String, Object> additionalParameters;
//    private MetaObject metaParameters;

    public BoundSql(Configuration configuration, String sql, List<ParameterMapping> parameterMappings, Object parameterObject) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.parameterObject = parameterObject;
        this.additionalParameters = new HashMap<String, Object>();
//    this.metaParameters = configuration.newMetaObject(additionalParameters);
    }

//    public boolean hasAdditionalParameter(String name) {
//        return metaParameters.hasGetter(name);
//    }
//
//    public void setAdditionalParameter(String name, Object value) {
//        metaParameters.setValue(name, value);
//    }
//
//    public Object getAdditionalParameter(String name) {
//        return metaParameters.getValue(name);
//    }
}
