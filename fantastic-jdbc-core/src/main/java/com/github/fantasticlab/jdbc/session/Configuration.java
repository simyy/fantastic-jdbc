package com.github.fantasticlab.jdbc.session;

import com.github.fantasticlab.jdbc.mapper.MapperRegistry;
import com.github.fantasticlab.jdbc.executor.Executor;
import com.github.fantasticlab.jdbc.executor.SimpleExecutor;
import com.github.fantasticlab.jdbc.executor.parameter.ParameterHandler;
import com.github.fantasticlab.jdbc.executor.resultset.DefaultResultSetHandler;
import com.github.fantasticlab.jdbc.executor.resultset.ResultSetHandler;
import com.github.fantasticlab.jdbc.executor.statement.RoutingStatementHandler;
import com.github.fantasticlab.jdbc.executor.statement.StatementHandler;
import com.github.fantasticlab.jdbc.executor.mapping.BoundSql;
import com.github.fantasticlab.jdbc.executor.mapping.MappedStatement;
import com.github.fantasticlab.jdbc.executor.mapping.ParameterMap;
import com.github.fantasticlab.jdbc.executor.mapping.ResultMap;
import com.github.fantasticlab.jdbc.executor.mapping.RowBounds;
import com.github.fantasticlab.jdbc.plugin.InterceptorChain;
import com.github.fantasticlab.jdbc.util.reflection.MetaObject;
import com.github.fantasticlab.jdbc.util.reflection.factory.DefaultObjectFactory;
import com.github.fantasticlab.jdbc.util.reflection.factory.ObjectFactory;
import com.github.fantasticlab.jdbc.util.reflection.wrapper.DefaultObjectWrapperFactory;
import com.github.fantasticlab.jdbc.util.reflection.wrapper.ObjectWrapperFactory;
import com.github.fantasticlab.jdbc.scripting.LanguageDriver;
import com.github.fantasticlab.jdbc.scripting.XMLLanguageDriver;
import com.github.fantasticlab.jdbc.session.transaction.Transaction;
import com.github.fantasticlab.jdbc.executor.type.JdbcType;
import com.github.fantasticlab.jdbc.executor.type.TypeAliasRegistry;
import com.github.fantasticlab.jdbc.executor.type.TypeHandlerRegistry;
import com.github.fantasticlab.jdbc.xml.parsing.XNode;
import lombok.Data;

import javax.sql.DataSource;
import java.util.*;

/**
 * Configuration is the core component in fantastic-jdbc,
 * which contains all the config,
 * such as {@code MapperRegistry}, {@code DataSource} and so on.
 */
@Data
public class Configuration {

    public Factory FACTORY = new Factory(this);
    public final JdbcType JDBC_TYPE_FOR_NULL = JdbcType.OTHER;


    /* XML LanguageDriver */
    protected LanguageDriver languageDriver = new XMLLanguageDriver();

    /* Global Variables */
    protected Properties variables = new Properties();

    /* A factory for connections to the physical data source, such as UnpooledDataSource */
    protected DataSource dataSource;

    /* The Scanner and Storage of Mapper Interface */
    protected final MapperRegistry mapperRegistry = new MapperRegistry();

    /* SQL Fragment Storage in Mapper.xml, which is imported by include in a SQL */
    protected final Map<String, XNode> sqlFragments = new HashMap<>();

    /* Result Mapping, Key is the id Of ResultMap, and Val is  */
    protected final Map<String, ResultMap> resultMaps = new HashMap<>();

    /* Mapped Statement Storage */
    protected Map<String, MappedStatement> mappedStatements = new HashMap<>();

    /* Interceptor Chain */
    private InterceptorChain interceptorChain = new InterceptorChain();

    /* Object Factory */
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();



    // 类型关联注册器
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    // 类型处理注册器
    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

    // 参数映射
    protected final Map<String, ParameterMap> parameterMaps = new HashMap<>();
    // 数据源
    protected Integer defaultStatementTimeout;




    public void addVariables(Properties properties) {
        if (properties == null) {
            return;
        }
        variables.putAll(properties);
    }

    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }

    public void addMappers(String packageName) {
        mapperRegistry.addMappers(packageName);
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    public void addMappedStatement(MappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }

    public MappedStatement getMappedStatement(String id) {
        return mappedStatements.getOrDefault(id, null);
    }

    public void addResultMap(ResultMap rm) {
        resultMaps.put(rm.getId(), rm);
    }



    /* Configuration Factory */
    public static class Factory {

        private Configuration configuration;

        public Factory(Configuration configuration) {
            this.configuration = configuration;
        }

        /* Init a Executor */
        public Executor newExecutor(Transaction transaction) {
            Executor executor = new SimpleExecutor(configuration, transaction);
            return (Executor) configuration.interceptorChain.pluginAll(executor);
        }

        /* Init a StatementHandler */
        public StatementHandler newStatementHandler(Executor executor,
                                                    MappedStatement mappedStatement,
                                                    Object parameterObject,
                                                    RowBounds rowBounds,
                                                    BoundSql boundSql) {

            StatementHandler statementHandler = new RoutingStatementHandler(
                    executor, mappedStatement, parameterObject, rowBounds, boundSql);
            return (StatementHandler) configuration.interceptorChain.pluginAll(statementHandler);
        }

        /* Init a MetaObject */
        public MetaObject newMetaObject(Object object) {
            return MetaObject.forObject(object,
                    configuration.objectFactory, configuration.objectWrapperFactory);
        }

        /* Init a ParameterHandler */
        public ParameterHandler newParameterHandler(MappedStatement mappedStatement,
                                                    Object parameterObject,
                                                    BoundSql boundSql) {

            ParameterHandler parameterHandler = configuration.languageDriver
                    .createParameterHandler(mappedStatement, parameterObject, boundSql);
            return (ParameterHandler) configuration.interceptorChain.pluginAll(parameterHandler);
        }

        /* Init a ResultSetHandler */
        public ResultSetHandler newResultSetHandler(MappedStatement mappedStatement) {
            ResultSetHandler resultSetHandler = new DefaultResultSetHandler(mappedStatement);
            return resultSetHandler;
        }
    }







    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    public boolean hasStatement(String statementName) {
        return mappedStatements.containsKey(statementName);
    }


    public void addParameterMap(ParameterMap pm) {
        parameterMaps.put(pm.getId(), pm);
    }

    public ParameterMap getParameterMap(String id) {
        return parameterMaps.get(id);
    }

    public boolean hasResultMap(String id) {
        return resultMaps.containsKey(id);
    }

    public ResultMap getResultMap(String id) {
        return resultMaps.get(id);
    }



}
