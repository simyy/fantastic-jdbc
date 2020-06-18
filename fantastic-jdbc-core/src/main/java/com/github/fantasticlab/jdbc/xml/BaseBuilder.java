package com.github.fantasticlab.jdbc.xml;

import com.github.fantasticlab.jdbc.mapping.ParameterMode;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.executor.type.JdbcType;
import com.github.fantasticlab.jdbc.executor.type.TypeAliasRegistry;
import com.github.fantasticlab.jdbc.xml.parsing.ParsingException;

/**
 * BaseBuilder is the base builder for xml builders.
 */
public abstract class BaseBuilder {

    protected final Configuration configuration;
    protected final TypeAliasRegistry typeAliasRegistry;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = this.configuration.getTypeAliasRegistry();
    }

    public Configuration getConfiguration() {
        return configuration;
    }


    protected JdbcType resolveJdbcType(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return JdbcType.valueOf(alias);
        } catch (IllegalArgumentException e) {
            throw new ParsingException("Error resolving JdbcType. Cause: " + e, e);
        }
    }

    protected ParameterMode resolveParameterMode(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return ParameterMode.valueOf(alias);
        } catch (IllegalArgumentException e) {
            throw new ParsingException("Error resolving ParameterMode. Cause: " + e, e);
        }
    }

    protected Class<?> resolveClass(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return resolveAlias(alias);
        } catch (Exception e) {
            throw new ParsingException("Error resolving class. Cause: " + e, e);
        }
    }

    protected Class<?> resolveAlias(String alias) {
        return typeAliasRegistry.resolveAlias(alias);
    }
}
