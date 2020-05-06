package com.github.fantasticlab.jdbc.scripting;

import java.util.HashMap;
import java.util.Map;

/**
 * 语言注册器
 */
public class LanguageDriverRegistry {

    private final Map<Class<?>, LanguageDriver> LANGUAGE_DRIVER_MAP = new HashMap<Class<?>, LanguageDriver>();

    private Class<?> defaultDriverClass = XMLLanguageDriver.class;


    public LanguageDriverRegistry() {
        try {
            LANGUAGE_DRIVER_MAP.put(defaultDriverClass, (LanguageDriver) defaultDriverClass.newInstance());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void register(Class<?> cls) {
        if (cls == null) {
            throw new ScriptingException("null is not a valid Language Driver");
        }
        if (!LanguageDriver.class.isAssignableFrom(cls)) {
            throw new ScriptingException(cls.getName() + " does not implements " + LanguageDriver.class.getName());
        }
        LanguageDriver driver = LANGUAGE_DRIVER_MAP.get(cls);
        if (driver == null) {
            try {
                driver = (LanguageDriver) cls.newInstance();
                LANGUAGE_DRIVER_MAP.put(cls, driver);
            } catch (Exception ex) {
                throw new ScriptingException("Failed to load language driver for " + cls.getName(), ex);
            }
        }
    }

    public LanguageDriver getDriver(Class<?> cls) {
        return LANGUAGE_DRIVER_MAP.get(cls);
    }

    public LanguageDriver getDefaultDriver() {
        return getDriver(getDefaultDriverClass());
    }

    public Class<?> getDefaultDriverClass() {
        return defaultDriverClass;
    }

    public void setDefaultDriverClass(Class<?> defaultDriverClass) {
        register(defaultDriverClass);
        this.defaultDriverClass = defaultDriverClass;
    }

}
