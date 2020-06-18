package com.github.fantasticlab.jdbc.executor.parameter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface ParameterHandler {

    Object getParameterObject();

    void setParameters(PreparedStatement ps) throws SQLException;

}
