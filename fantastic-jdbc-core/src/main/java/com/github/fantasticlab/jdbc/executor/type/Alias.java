package com.github.fantasticlab.jdbc.executor.type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Alias
 * 1> <typeAlias alias="Author" type="domain.blog.Author"/>
 * 2> @Alias("author") class ABC {}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Alias {
  public String value();
}
