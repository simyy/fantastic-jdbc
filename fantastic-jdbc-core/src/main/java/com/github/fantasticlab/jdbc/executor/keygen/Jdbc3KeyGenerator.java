package com.github.fantasticlab.jdbc.executor.keygen;

import com.github.fantasticlab.jdbc.executor.Executor;
import com.github.fantasticlab.jdbc.mapping.MappedStatement;

import java.sql.Statement;

/**
 * MySQL auto increment primary key
 */
public class Jdbc3KeyGenerator implements KeyGenerator {

    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        // ignore
    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        // TODO
    }
}
