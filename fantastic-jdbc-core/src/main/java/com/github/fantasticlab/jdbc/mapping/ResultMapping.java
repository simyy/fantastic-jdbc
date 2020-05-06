package com.github.fantasticlab.jdbc.mapping;

import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.transaction.type.JdbcType;
import com.github.fantasticlab.jdbc.transaction.type.TypeHandler;
import com.github.fantasticlab.jdbc.transaction.type.TypeHandlerRegistry;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * 结果映射
 */
@Data
public class ResultMapping {

    private Configuration configuration;
    private String property;
    private String column;
    private Class<?> javaType;
    private JdbcType jdbcType;
    private TypeHandler<?> typeHandler;
    private List<ResultFlag> flags;
    private String resultSet;

    ResultMapping() {
    }

    public static class Builder {
        private ResultMapping resultMapping = new ResultMapping();

        public Builder(Configuration configuration, String property, String column, TypeHandler<?> typeHandler) {
            this(configuration, property);
            resultMapping.column = column;
            resultMapping.typeHandler = typeHandler;
        }

        public Builder(Configuration configuration, String property, String column, Class<?> javaType) {
            this(configuration, property);
            resultMapping.column = column;
            resultMapping.javaType = javaType;
        }

        public Builder(Configuration configuration, String property) {
            resultMapping.configuration = configuration;
            resultMapping.property = property;
            resultMapping.flags = new ArrayList<ResultFlag>();
        }

        public Builder javaType(Class<?> javaType) {
            resultMapping.javaType = javaType;
            return this;
        }

        public Builder jdbcType(JdbcType jdbcType) {
            resultMapping.jdbcType = jdbcType;
            return this;
        }

        public Builder resultSet(String resultSet) {
            resultMapping.resultSet = resultSet;
            return this;
        }

        public Builder flags(List<ResultFlag> flags) {
            resultMapping.flags = flags;
            return this;
        }

        public Builder typeHandler(TypeHandler<?> typeHandler) {
            resultMapping.typeHandler = typeHandler;
            return this;
        }

        public ResultMapping build() {
            // lock down collections
            resultMapping.flags = Collections.unmodifiableList(resultMapping.flags);
            resolveTypeHandler();
            validate();
            return resultMapping;
        }

        //一些验证逻辑,验证result map有没有写错
        private void validate() {
            if (resultMapping.getResultSet() != null) {
                int numColums = 0;
                if (resultMapping.column != null) {
                    numColums = resultMapping.column.split(",").length;
                }
                int numForeignColumns = 0;
                if (numColums != numForeignColumns) {
                    throw new IllegalStateException("There should be the same number of columns and foreignColumns in property " + resultMapping.property);
                }
            }
        }

        private void resolveTypeHandler() {
            if (resultMapping.typeHandler == null && resultMapping.javaType != null) {
                Configuration configuration = resultMapping.configuration;
                TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
                resultMapping.typeHandler = typeHandlerRegistry.getTypeHandler(resultMapping.javaType, resultMapping.jdbcType);
            }
        }

        public Builder column(String column) {
            resultMapping.column = column;
            return this;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResultMapping that = (ResultMapping) o;

        if (property == null || !property.equals(that.property)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        if (property != null) {
            return property.hashCode();
        } else if (column != null) {
            return column.hashCode();
        } else {
            return 0;
        }
    }

}
