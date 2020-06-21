package com.github.fantasticlab.jdbc.mapper;

import com.github.fantasticlab.jdbc.annotations.Param;
import com.github.fantasticlab.jdbc.exceptions.BindingException;
import com.github.fantasticlab.jdbc.executor.mapping.MappedStatement;
import com.github.fantasticlab.jdbc.executor.mapping.SqlCommandType;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.executor.mapping.RowBounds;
import com.github.fantasticlab.jdbc.session.SqlSession;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.*;


public class MapperMethod {


    private final SqlCommand command;
    private final MethodSignature method;


    public MapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
        this.command = new SqlCommand(config, mapperInterface, method);
        this.method = new MethodSignature(method);
    }

    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result;
        if (SqlCommandType.INSERT == command.getType()) {
            Object param = method.convertArgsToSqlCommandParam(args);
            result = rowCountResult(sqlSession.insert(command.getName(), param));

        } else if (SqlCommandType.UPDATE == command.getType()) {
            Object param = method.convertArgsToSqlCommandParam(args);
            result = rowCountResult(sqlSession.update(command.getName(), param));

        } else if (SqlCommandType.SELECT == command.getType()) {
              if (method.isReturnsVoid() && method.hasResultHandler()) {
                  executeWithResultHandler(sqlSession, args);
                  result = null;
              } else if (method.isReturnsMany()) {
                  result = executeForMany(sqlSession, args);
              } else {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = sqlSession.selectOne(command.getName(), param);
              }

        } else {
            throw new BindingException("Unknown execution method for: " + command.getName());
        }

        return result;
    }

    private Object rowCountResult(int rowCount) {
        final Object result;
        if (method.isReturnsVoid()) {
            result = null;
        } else if (Integer.class.equals(method.getReturnType()) || Integer.TYPE.equals(method.getReturnType())) {
            result = Integer.valueOf(rowCount);
        } else if (Long.class.equals(method.getReturnType()) || Long.TYPE.equals(method.getReturnType())) {
            result = Long.valueOf(rowCount);
        } else if (Boolean.class.equals(method.getReturnType()) || Boolean.TYPE.equals(method.getReturnType())) {
            result = Boolean.valueOf(rowCount > 0);
        } else {
            throw new BindingException("Mapper method '" + command.getName() + "' has an unsupported return type: " + method.getReturnType());
        }
        return result;
    }

    private void executeWithResultHandler(SqlSession sqlSession, Object[] args) {
//        MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(command.getName());
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            sqlSession.select(command.getName(), param, rowBounds);
        } else {
            sqlSession.select(command.getName(), param, RowBounds.DEFAULT);
        }
    }

    private <E> Object executeForMany(SqlSession sqlSession, Object[] args) { List<E> result;
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            result = sqlSession.<E>selectList(command.getName(), param, rowBounds);
        } else {
            result = sqlSession.<E>selectList(command.getName(), param, RowBounds.DEFAULT);
        }
        return result;
    }


    @Data
    public static class SqlCommand {

        private final String name;
        private final SqlCommandType type;

        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
            String statementName = mapperInterface.getName() + "." + method.getName();
            MappedStatement ms = null;

            if (configuration.hasStatement(statementName)) {
                ms = configuration.getMappedStatement(statementName);

            } else if (!mapperInterface.equals(method.getDeclaringClass().getName())) {
                String parentStatementName = method.getDeclaringClass().getName() + "." + method.getName();
                if (configuration.hasStatement(parentStatementName)) {
                    ms = configuration.getMappedStatement(parentStatementName);
                }
            }
            if (ms == null) {
                throw new BindingException("Invalid bound statement (not found): " + statementName);
            }
            name = ms.getId();
            type = ms.getSqlCommandType();
            if (type == SqlCommandType.UNKNOWN) {
                throw new BindingException("Unknown execution method for: " + name);
            }
        }
    }

    @Data
    public static class MethodSignature {

        private final boolean returnsMany;
        private final boolean returnsVoid;
        private final Class<?> returnType;
        private final Integer rowBoundsIndex;
        private final SortedMap<Integer, String> params;
        private final boolean hasNamedParameters;

        public MethodSignature(Method method) {
            this.returnType = method.getReturnType();
            this.returnsVoid = void.class.equals(this.returnType);
            this.returnsMany = Collection.class.isAssignableFrom(this.returnType);
            this.hasNamedParameters = hasNamedParams(method);

            this.rowBoundsIndex = getUniqueParamIndex(method, RowBounds.class);
            this.params = Collections.unmodifiableSortedMap(getParams(method, this.hasNamedParameters));
        }

        public Object convertArgsToSqlCommandParam(Object[] args) {
            final int paramCount = params.size();
            if (args == null || paramCount == 0) {
                return null;

            } else if (!hasNamedParameters && paramCount == 1) {
                return args[params.keySet().iterator().next().intValue()];

            } else {
                 final Map<String, Object> param = new HashMap<>();
                 int i = 0;
                 for (Map.Entry<Integer, String> entry : params.entrySet()) {
                     param.put(entry.getValue(), args[entry.getKey().intValue()]);
                     final String genericParamName = "param" + String.valueOf(i + 1);
                     if (!param.containsKey(genericParamName)) {
                         param.put(genericParamName, args[entry.getKey()]);
                    }
                    i++;
                 }
            return param;
          }
        }

        public boolean hasRowBounds() {
          return rowBoundsIndex != null;
        }

        public RowBounds extractRowBounds(Object[] args) {
          return hasRowBounds() ? (RowBounds) args[rowBoundsIndex] : null;
        }

        public boolean hasResultHandler() {
          return false;
        }

        private Integer getUniqueParamIndex(Method method, Class<?> paramType) {
            Integer index = null;
            final Class<?>[] argTypes = method.getParameterTypes();
            for (int i = 0; i < argTypes.length; i++) {
                if (paramType.isAssignableFrom(argTypes[i])) {
                    if (index == null) {
                        index = i;
                    } else {
                        throw new BindingException(method.getName() + " cannot have multiple " + paramType.getSimpleName() + " parameters");
                    }
                }
            }
            return index;
        }

        private SortedMap<Integer, String> getParams(Method method, boolean hasNamedParameters) {
            final SortedMap<Integer, String> params = new TreeMap<Integer, String>();
            final Class<?>[] argTypes = method.getParameterTypes();
            for (int i = 0; i < argTypes.length; i++) {
                if (!RowBounds.class.isAssignableFrom(argTypes[i])) {
                    String paramName = String.valueOf(params.size());
                    if (hasNamedParameters) {
                        paramName = getParamNameFromAnnotation(method, i, paramName);
                    }
                    params.put(i, paramName);
                }
            }
            return params;
        }

        private String getParamNameFromAnnotation(Method method, int i, String paramName) {
          final Object[] paramAnnos = method.getParameterAnnotations()[i];
          for (Object paramAnno : paramAnnos) {
            if (paramAnno instanceof Param) {
              paramName = ((Param) paramAnno).value();
            }
          }
          return paramName;
        }

        private boolean hasNamedParams(Method method) {
            boolean hasNamedParams = false;
            final Object[][] paramAnnos = method.getParameterAnnotations();
            for (Object[] paramAnno : paramAnnos) {
                for (Object aParamAnno : paramAnno) {
                    if (aParamAnno instanceof Param) {
                        hasNamedParams = true;
                        break;
                    }
                }
            }
            return hasNamedParams;
        }
    }
}
