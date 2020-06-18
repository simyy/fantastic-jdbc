package com.github.fantasticlab.jdbc.binding;

import com.github.fantasticlab.jdbc.io.ResolverUtil;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.session.SqlSession;
import com.github.fantasticlab.jdbc.xml.XMLMapperBuilder;

import java.util.*;

/**
 * Mapper Registry is a sql mapping storage.
 * <strong>knownMappers</strong> is a map,
 * which key is the interface of mapper such as,
 * --------------------------------
 *  public interface UserMapper {
 *      Long insert(User user);
 *      User getUser(Long id);
 *      List<User> getAll();
 *      void updateUser(Long id);
 *  }
 * --------------------------------
 *  and value is {@code MapperProxyFactory}.
 */
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
        return mapperProxyFactory.newInstance(sqlSession);
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
        } else {
            throw new BindingException("Type " + type + " is unable to load because of not interface");
        }

    }

    public void addMappers(String packageName, Class<?> superType) {
        /* Scan all the class which has a superType from a target package */
        ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<Class<?>>();
        resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
        Set<Class<? extends Class<?>>> mapperSet = resolverUtil.getClasses();
        for (Class<?> mapperClass : mapperSet) {
            addMapper(mapperClass);
        }
    }


    public void addMappers(String packageName) {
        /* The param of Object.class is used to scan all the class from the mapper package. */
        addMappers(packageName, Object.class);
    }
  
}
