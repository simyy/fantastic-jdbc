package com.github.fantasticlab.jdbc.executor.keygen;

import com.github.fantasticlab.jdbc.executor.Executor;
import com.github.fantasticlab.jdbc.mapping.MappedStatement;

import java.sql.Statement;

public interface KeyGenerator {

  void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

  void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter);

}
