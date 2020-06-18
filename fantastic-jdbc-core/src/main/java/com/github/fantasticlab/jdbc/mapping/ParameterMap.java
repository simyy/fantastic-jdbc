package com.github.fantasticlab.jdbc.mapping;

import lombok.Getter;

import java.util.Collections;
import java.util.List;


@Getter
public class ParameterMap {

    private String id;
    private Class<?> type;
    private List<ParameterMapping> parameterMappings;

    private ParameterMap() {
    }

    public static class Builder {

        private ParameterMap parameterMap = new ParameterMap();

        public Builder(String id, Class<?> type, List<ParameterMapping> parameterMappings) {
            parameterMap.id = id;
            parameterMap.type = type;
            parameterMap.parameterMappings = parameterMappings;
        }

        public Class<?> type() {
            return parameterMap.type;
        }

        public ParameterMap build() {
            //lock down collections
            parameterMap.parameterMappings = Collections.unmodifiableList(parameterMap.parameterMappings);
            return parameterMap;
        }
    }

}
