package com.github.fantasticlab.jdbc.mapping;


import com.github.fantasticlab.jdbc.executor.keygen.KeyGenerator;
import com.github.fantasticlab.jdbc.executor.keygen.NoKeyGenerator;
import com.github.fantasticlab.jdbc.scripting.LanguageDriver;
import com.github.fantasticlab.jdbc.session.Configuration;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 映射语句
 */
@Slf4j
public final class MappedStatement {

    private String resource;
    private Configuration configuration;
    private String id;
    private StatementType statementType;
    //SQL源码
    private SqlSource sqlSource;
    private ParameterMap parameterMap;
    private List<ResultMap> resultMaps;
    private SqlCommandType sqlCommandType;
    private KeyGenerator keyGenerator;
    private LanguageDriver lang;

    private MappedStatement() {
    }

    public static class Builder {
        private MappedStatement mappedStatement = new MappedStatement();

        public Builder(Configuration configuration, String id, SqlSource sqlSource, SqlCommandType sqlCommandType) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlSource = sqlSource;
            mappedStatement.statementType = StatementType.PREPARED; // todo
            mappedStatement.parameterMap =
                    new ParameterMap.Builder(
                            "defaultParameterMap",
                            null,
                            new ArrayList<>()).build();
            mappedStatement.resultMaps = new ArrayList<>();
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.keyGenerator = new NoKeyGenerator();
        }

        public Builder resource(String resource) {
            mappedStatement.resource = resource;
            return this;
        }

        public String id() {
            return mappedStatement.id;
        }

        public Builder parameterMap(ParameterMap parameterMap) {
            mappedStatement.parameterMap = parameterMap;
            return this;
        }

        public Builder resultMaps(List<ResultMap> resultMaps) {
            mappedStatement.resultMaps = resultMaps;
            return this;
        }

        public Builder statementType(StatementType statementType) {
            mappedStatement.statementType = statementType;
            return this;
        }

        public Builder keyGenerator(KeyGenerator keyGenerator) {
            mappedStatement.keyGenerator = keyGenerator;
            return this;
        }

        public MappedStatement build() {
            assert mappedStatement.configuration != null;
            assert mappedStatement.id != null;
            assert mappedStatement.sqlSource != null;
            mappedStatement.resultMaps = Collections.unmodifiableList(mappedStatement.resultMaps);
            return mappedStatement;
        }
    }

    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }

    public String getResource() {
        return resource;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getId() {
        return id;
    }

    public StatementType getStatementType() {
        return statementType;
    }

    public SqlSource getSqlSource() {
        return sqlSource;
    }

    public ParameterMap getParameterMap() {
        return parameterMap;
    }

    public List<ResultMap> getResultMaps() {
        return resultMaps;
    }

    public LanguageDriver getLang() {
        return lang;
    }

    public void setLang(LanguageDriver lang) {
        this.lang = lang;
    }

    public BoundSql getBoundSql(Object parameterObject) {
        //其实就是调用sqlSource.getBoundSql
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        //剩下的可以暂时忽略
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings == null || parameterMappings.isEmpty()) {
            boundSql = new BoundSql(configuration, boundSql.getSql(), parameterMap.getParameterMappings(), parameterObject);
        }

        // check for nested result maps in parameter mappings (issue #30)
        for (ParameterMapping pm : boundSql.getParameterMappings()) {
            String rmId = pm.getResultMapId();
            if (rmId != null) {
                ResultMap rm = configuration.getResultMap(rmId);
                if (rm != null) {
                }
            }
        }

        return boundSql;
    }

    private static String[] delimitedStringtoArray(String in) {
        if (in == null || in.trim().length() == 0) {
            return null;
        } else {
            return in.split(",");
        }
    }

}
