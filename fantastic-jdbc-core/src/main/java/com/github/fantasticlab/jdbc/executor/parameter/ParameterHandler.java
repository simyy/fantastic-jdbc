package com.github.fantasticlab.jdbc.executor.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ParameterHandler {

    void setParameters(PreparedStatement ps) throws SQLException;

}
