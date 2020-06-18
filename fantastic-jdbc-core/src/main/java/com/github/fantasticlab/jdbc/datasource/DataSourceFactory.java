package com.github.fantasticlab.jdbc.datasource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;


public interface DataSourceFactory {

    DataSource getDataSource();

}
