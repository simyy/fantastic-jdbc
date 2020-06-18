/*
 *    Copyright 2009-2012 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.github.fantasticlab.jdbc.scripting.xmltags;

import com.github.fantasticlab.jdbc.reflection.MetaObject;
import com.github.fantasticlab.jdbc.session.Configuration;
import ognl.OgnlContext;
import ognl.OgnlException;
import ognl.OgnlRuntime;
import ognl.PropertyAccessor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Clinton Begin
 */

/**
 * 动态上下文
 */
public class DynamicContext {

    public static final String PARAMETER_OBJECT_KEY = "_parameter";
    public static final String DATABASE_ID_KEY = "_databaseId";

    static {
        /* Ognl */
        OgnlRuntime.setPropertyAccessor(ContextMap.class, new ContextAccessor());
    }

    private final ContextMap bindings;
    private final StringBuilder sqlBuilder = new StringBuilder();
    private int uniqueNumber = 0;

    //在DynamicContext的构造函数中，根据传入的参数对象是否为Map类型，有两个不同构造ContextMap的方式。
    //而ContextMap作为一个继承了HashMap的对象，作用就是用于统一参数的访问方式：用Map接口方法来访问数据。
    //具体来说，当传入的参数对象不是Map类型时，Mybatis会将传入的POJO对象用MetaObject对象来封装，
    //当动态计算sql过程需要获取数据时，用Map接口的get方法包装 MetaObject对象的取值过程。
    public DynamicContext(Configuration configuration, Object parameterObject) {
        //绝大多数调用的地方parameterObject为null
        if (parameterObject != null && !(parameterObject instanceof Map)) {
            //如果是map型  ??  这句是 如果不是map型
            MetaObject metaObject = configuration.newMetaObject(parameterObject);
            bindings = new ContextMap(metaObject);
        } else {
            bindings = new ContextMap(null);
        }
        bindings.put(PARAMETER_OBJECT_KEY, parameterObject);
//        bindings.put(DATABASE_ID_KEY, configuration.getDatabaseId());
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public void bind(String name, Object value) {
        bindings.put(name, value);
    }

    public void appendSql(String sql) {
        sqlBuilder.append(sql);
        sqlBuilder.append(" ");
    }

    public String getSql() {
        return sqlBuilder.toString().trim();
    }

    public int getUniqueNumber() {
        return uniqueNumber++;
    }

    static class ContextMap extends HashMap<String, Object> {
        private static final long serialVersionUID = 2977601501966151582L;

        private MetaObject parameterMetaObject;

        public ContextMap(MetaObject parameterMetaObject) {
            this.parameterMetaObject = parameterMetaObject;
        }

        @Override
        public Object get(Object key) {
            String strKey = (String) key;
            //先去map里找
            if (super.containsKey(strKey)) {
                return super.get(strKey);
            }

            //如果没找到，再用ognl表达式去取值
            //如person[0].birthdate.year
            if (parameterMetaObject != null) {
                // issue #61 do not modify the context when reading
                return parameterMetaObject.getValue(strKey);
            }

            return null;
        }
    }

    /* ContextAccessor is a implement of PropertyAccessor */
    static class ContextAccessor implements PropertyAccessor {

        @Override
        public Object getProperty(Map context, Object target, Object name)
                throws OgnlException {
            Map map = (Map) target;
            Object result = map.get(name);
            if (result != null) {
                return result;
            }
            Object parameterObject = map.get(PARAMETER_OBJECT_KEY);
            if (parameterObject instanceof Map) {
                return ((Map) parameterObject).get(name);
            }
            return null;
        }

        @Override
        public void setProperty(Map context, Object target, Object name, Object value)
                throws OgnlException {
            Map<Object, Object> map = (Map<Object, Object>) target;
            map.put(name, value);
        }

        @Override
        public String getSourceAccessor(OgnlContext arg0, Object arg1, Object arg2) {
            return null;
        }

        @Override
        public String getSourceSetter(OgnlContext arg0, Object arg1, Object arg2) {
            return null;
        }
    }
}
