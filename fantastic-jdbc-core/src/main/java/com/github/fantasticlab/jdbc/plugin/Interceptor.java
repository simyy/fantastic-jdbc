package com.github.fantasticlab.jdbc.plugin;

import java.util.Properties;

/**
 * 拦截器
 */
public interface Interceptor {

    Object intercept(Invocation invocation) throws Throwable;

    Object plugin(Object target);

    void setProperties(Properties properties);

}
