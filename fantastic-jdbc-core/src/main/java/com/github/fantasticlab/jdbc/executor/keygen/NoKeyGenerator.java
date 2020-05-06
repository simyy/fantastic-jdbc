package com.github.fantasticlab.jdbc.executor.keygen;


import com.github.fantasticlab.jdbc.executor.Executor;
import com.github.fantasticlab.jdbc.mapping.MappedStatement;

import java.sql.Statement;


/**
 * 不用键值生成器
 */
public class NoKeyGenerator implements KeyGenerator {

    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        // Do Nothing
    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        // Do Nothing
    }

}
