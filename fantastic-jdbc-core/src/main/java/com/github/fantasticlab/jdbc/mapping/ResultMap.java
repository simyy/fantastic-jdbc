package com.github.fantasticlab.jdbc.mapping;


import java.util.*;

/**
 * 结果映射
 */
public class ResultMap {

    private String id;
    private Class<?> type;
    private List<ResultMapping> resultMappings;
    private List<ResultMapping> idResultMappings;
    private List<ResultMapping> propertyResultMappings;
    private Set<String> mappedColumns;

    private ResultMap() {
    }

    public static class Builder {

        private ResultMap resultMap = new ResultMap();

        public Builder(String id, Class<?> type, List<ResultMapping> resultMappings) {
            resultMap.id = id;
            resultMap.type = type;
            resultMap.resultMappings = resultMappings;
        }

        public Class<?> type() {
            return resultMap.type;
        }

        public ResultMap build() {
            if (resultMap.id == null) {
                throw new IllegalArgumentException("ResultMaps must have an id");
            }
            resultMap.mappedColumns = new HashSet<String>();
            resultMap.idResultMappings = new ArrayList<ResultMapping>();
            resultMap.propertyResultMappings = new ArrayList<ResultMapping>();
            for (ResultMapping resultMapping : resultMap.resultMappings) {
               final String column = resultMapping.getColumn();
                if (column != null) {
                    resultMap.mappedColumns.add(column.toUpperCase(Locale.ENGLISH));
                }
                if (resultMapping.getFlags().contains(ResultFlag.ID)) {
                    resultMap.idResultMappings.add(resultMapping);
                }
                resultMap.propertyResultMappings.add(resultMapping);
            }
            if (resultMap.idResultMappings.isEmpty()) {
                resultMap.idResultMappings.addAll(resultMap.resultMappings);
            }
            // lock down collections
            resultMap.resultMappings = Collections.unmodifiableList(resultMap.resultMappings);
            resultMap.idResultMappings = Collections.unmodifiableList(resultMap.idResultMappings);
            resultMap.propertyResultMappings = Collections.unmodifiableList(resultMap.propertyResultMappings);
            resultMap.mappedColumns = Collections.unmodifiableSet(resultMap.mappedColumns);
            return resultMap;
        }
    }

    public String getId() {
        return id;
    }

    public Class<?> getType() {
        return type;
    }

    public List<ResultMapping> getResultMappings() {
        return resultMappings;
    }

    public List<ResultMapping> getPropertyResultMappings() {
        return propertyResultMappings;
    }

    public List<ResultMapping> getIdResultMappings() {
        return idResultMappings;
    }

    public Set<String> getMappedColumns() {
        return mappedColumns;
    }

}
