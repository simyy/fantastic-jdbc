package com.github.fantasticlab.jdbc.session;

import com.github.fantasticlab.jdbc.binding.MapperRegistry;
import com.github.fantasticlab.jdbc.executor.Executor;
import com.github.fantasticlab.jdbc.executor.SimpleExecutor;
import com.github.fantasticlab.jdbc.executor.parameter.ParameterHandler;
import com.github.fantasticlab.jdbc.executor.resultset.DefaultResultSetHandler;
import com.github.fantasticlab.jdbc.executor.resultset.ResultSetHandler;
import com.github.fantasticlab.jdbc.executor.statement.RoutingStatementHandler;
import com.github.fantasticlab.jdbc.executor.statement.StatementHandler;
import com.github.fantasticlab.jdbc.mapping.BoundSql;
import com.github.fantasticlab.jdbc.mapping.MappedStatement;
import com.github.fantasticlab.jdbc.mapping.ParameterMap;
import com.github.fantasticlab.jdbc.mapping.ResultMap;
import com.github.fantasticlab.jdbc.plugin.InterceptorChain;
import com.github.fantasticlab.jdbc.reflection.MetaObject;
import com.github.fantasticlab.jdbc.reflection.factory.DefaultObjectFactory;
import com.github.fantasticlab.jdbc.reflection.factory.ObjectFactory;
import com.github.fantasticlab.jdbc.reflection.wrapper.DefaultObjectWrapperFactory;
import com.github.fantasticlab.jdbc.reflection.wrapper.ObjectWrapperFactory;
import com.github.fantasticlab.jdbc.scripting.LanguageDriverRegistry;
import com.github.fantasticlab.jdbc.transaction.Transaction;
import com.github.fantasticlab.jdbc.transaction.type.JdbcType;
import com.github.fantasticlab.jdbc.transaction.type.TypeAliasRegistry;
import com.github.fantasticlab.jdbc.transaction.type.TypeHandlerRegistry;
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

    /* Global Variables */
    protected Properties variables = new Properties();

    /* The Scanner and Storage of Mapper Interface */
    protected final MapperRegistry mapperRegistry = new MapperRegistry(this);

    /* SQL Fragment Storage in Mapper.xml, which is imported by include in a SQL */
    protected final Map<String, XNode> sqlFragments = new HashMap<>();

    /* Mapped Statement Storage */
    protected Map<String, MappedStatement> mappedStatements = new HashMap<>();


    // 对象工厂
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    // 对象包装器工厂
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    // 类型关联注册器
    protected final TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();
    // 类型处理注册器
    protected final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

    // 语言注册器
    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

    // 结果映射
    protected final Map<String, ResultMap> resultMaps = new HashMap<>();
    // 参数映射
    protected final Map<String, ParameterMap> parameterMaps = new HashMap<>();

    // 拦截器链
    private InterceptorChain interceptorChain = new InterceptorChain();
    // 数据源
    private DataSource dataSource;
    protected Integer defaultStatementTimeout;

    protected JdbcType jdbcTypeForNull = JdbcType.OTHER;




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

    public void addMappedStatement(MappedStatement ms) {
        mappedStatements.put(ms.getId(), ms);
    }








    public StatementHandler newStatementHandler(Executor executor,
                                                MappedStatement mappedStatement,
                                                Object parameterObject,
                                                RowBounds rowBounds,
                                                ResultHandler resultHandler,
                                                BoundSql boundSql) {

        StatementHandler statementHandler = new RoutingStatementHandler(
                executor, mappedStatement, parameterObject, rowBounds, resultHandler, boundSql);
        return (StatementHandler) interceptorChain.pluginAll(statementHandler);
    }

    public ParameterHandler newParameterHandler(MappedStatement mappedStatement,
                                                Object parameterObject,
                                                BoundSql boundSql) {

        ParameterHandler parameterHandler = mappedStatement
                .getLang()
                .createParameterHandler(mappedStatement, parameterObject, boundSql);
        return (ParameterHandler) interceptorChain.pluginAll(parameterHandler);
    }

    //创建结果集处理器
    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ParameterHandler parameterHandler,
                                                ResultHandler resultHandler, BoundSql boundSql) {
        ResultSetHandler resultSetHandler = new DefaultResultSetHandler(executor, mappedStatement, parameterHandler, resultHandler, boundSql, rowBounds);
        return resultSetHandler;
    }

    public MappedStatement getMappedStatement(String id) {
        MappedStatement ms =  mappedStatements.getOrDefault(id, null);
        // 临时兼容
        ms.setLang(languageRegistry.getDefaultDriver());
        return ms;
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    public boolean hasStatement(String statementName) {
        return mappedStatements.containsKey(statementName);
    }

    public Executor newExecutor(Transaction transaction) {
        Executor executor;
        executor = new SimpleExecutor(this, transaction);
        executor = (Executor) interceptorChain.pluginAll(executor);
        return executor;
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

    public void addResultMap(ResultMap rm) {
        resultMaps.put(rm.getId(), rm);
    }

    public MetaObject newMetaObject(Object object) {
        return MetaObject.forObject(object, objectFactory, objectWrapperFactory);
    }

}
