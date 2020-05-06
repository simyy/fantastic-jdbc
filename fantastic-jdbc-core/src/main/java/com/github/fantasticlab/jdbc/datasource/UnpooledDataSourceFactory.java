package com.github.fantasticlab.jdbc.datasource;

import com.github.fantasticlab.jdbc.session.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

public class UnpooledDataSourceFactory implements DataSourceFactory {

    protected UnpooledDataSource dataSource;

    private static final String PROPS_PREFIX = "db.";

    public UnpooledDataSourceFactory() {
        dataSource = new UnpooledDataSource();
    }

    public UnpooledDataSourceFactory(Configuration configuration) throws SQLException {
        dataSource = new UnpooledDataSource();
        Properties properties = configuration.getVariables();
        Properties driverProperties = new Properties();
        for (Object key : properties.keySet()) {
            String propertyName = (String) key;
            if (propertyName.startsWith(PROPS_PREFIX)) {
                String value = properties.getProperty(propertyName);
                driverProperties.setProperty(propertyName.substring(PROPS_PREFIX.length()), value);
            }
        }
        setProperties(driverProperties);
    }

    @Override
    public void setProperties(Properties driverProperties) throws SQLException {
        dataSource.setProperties(driverProperties);
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }
}
