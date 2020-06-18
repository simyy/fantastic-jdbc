package com.github.fantasticlab.jdbc.datasource;

import com.github.fantasticlab.jdbc.session.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * Use Factory Pattern to implement a {@code DataSourceFactory},
 * which can get a {@code UnpooledDataSource}.
 */
public class UnpooledDataSourceFactory implements DataSourceFactory {

    protected UnpooledDataSource dataSource;

    protected Properties driverProperties;

    private static final String PROPS_PREFIX = "db.";

    public UnpooledDataSourceFactory(Configuration configuration, Properties xmlProperties) throws SQLException {
        dataSource = new UnpooledDataSource();
        driverProperties = new Properties();
        /* First priority is xml config, then global config */
        if (xmlProperties == null) {
            Properties properties = configuration.getVariables();
            for (Object key : properties.keySet()) {
                String propertyName = (String) key;
                if (propertyName.startsWith(PROPS_PREFIX)) {
                    String value = properties.getProperty(propertyName);
                    driverProperties.setProperty(propertyName.substring(PROPS_PREFIX.length()), value);
                }
            }
        } else {
            driverProperties = xmlProperties;
        }
        dataSource.setProperties(driverProperties);
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }
}
