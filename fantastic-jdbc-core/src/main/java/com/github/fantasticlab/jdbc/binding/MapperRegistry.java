package com.github.fantasticlab.jdbc.binding;

import com.github.fantasticlab.jdbc.io.ResolverUtil;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.session.SqlSession;
import com.github.fantasticlab.jdbc.xml.XMLMapperBuilder;

import java.util.*;

public class MapperRegistry {

    private Configuration config;
    private final Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<Class<?>, MapperProxyFactory<?>>();

    public MapperRegistry(Configuration config) {
        this.config = config;
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
        }
        try {
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new BindingException("Error getting mapper instance. Cause: " + e, e);
        }
    }
  
    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    public <T> void addMapper(Class<T> type) {
        if (type.isInterface()) {
            if (hasMapper(type)) {
                throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
            }
            knownMappers.put(type, new MapperProxyFactory<T>(type));
        }
    }


    public Collection<Class<?>> getMappers() {
        return Collections.unmodifiableCollection(knownMappers.keySet());
    }

    public void addMappers(String packageName, Class<?> superType) {
        // 查找packageName包下所有是superType的类
        ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<Class<?>>();
        resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
        Set<Class<? extends Class<?>>> mapperSet = resolverUtil.getClasses();
        for (Class<?> mapperClass : mapperSet) {
            addMapper(mapperClass);
        }
    }


    public void addMappers(String packageName) {
        addMappers(packageName, Object.class);
    }
  
}
