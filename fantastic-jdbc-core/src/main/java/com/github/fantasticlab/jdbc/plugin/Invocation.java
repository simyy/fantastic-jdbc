package com.github.fantasticlab.jdbc.plugin;

import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


@Data
public class Invocation {

    private Object target;
    private Method method;
    private Object[] args;

    public Invocation(Object target, Method method, Object[] args) {
        this.target = target;
        this.method = method;
        this.args = args;
    }

    public Object proceed() throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target, args);
    }

}
