/*
 * Copyright 2012-2014 MyBatis.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.fantasticlab.jdbc.scripting.defaults;

import com.github.fantasticlab.jdbc.mapping.BoundSql;
import com.github.fantasticlab.jdbc.mapping.SqlSource;
import com.github.fantasticlab.jdbc.mapping.SqlSourceBuilder;
import com.github.fantasticlab.jdbc.scripting.xmltags.DynamicContext;
import com.github.fantasticlab.jdbc.scripting.xmltags.SqlNode;
import com.github.fantasticlab.jdbc.session.Configuration;

import java.util.HashMap;

/**
 * Static SqlSource. It is faster than {@link DynamicSqlSource} because mappings are 
 * calculated during startup.
 * 
 * @since 3.2.0
 * @author Eduardo Macarron
 */

/**
 * 原始SQL源码，比DynamicSqlSource快
 */
public class RawSqlSource implements SqlSource {

  private final SqlSource sqlSource;

  public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
    this(configuration, getSql(configuration, rootSqlNode), parameterType);
  }

  public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
    SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
    Class<?> clazz = parameterType == null ? Object.class : parameterType;
    sqlSource = sqlSourceParser.parse(sql, clazz, new HashMap<String, Object>());
  }

  private static String getSql(Configuration configuration, SqlNode rootSqlNode) {
    DynamicContext context = new DynamicContext(configuration, null);
    rootSqlNode.apply(context);
    return context.getSql();
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    return sqlSource.getBoundSql(parameterObject);
  }

}
