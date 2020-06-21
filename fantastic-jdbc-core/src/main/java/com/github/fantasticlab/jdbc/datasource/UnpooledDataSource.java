package com.github.fantasticlab.jdbc.datasource;

import com.github.fantasticlab.jdbc.util.io.Resources;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * UnpooledDataSource is a implement of {@code DataSource}.
 * Use {@code DriverManager.getDrivers()} to load JDBC drivers,
 * and {@code initializeDriver} will be invoked in {@code setProperties}.
 */
public class UnpooledDataSource implements DataSource {

    private Properties driverProperties;
    private static Map<String, Driver> registeredDrivers = new ConcurrentHashMap<String, Driver>();

    private String driver;
    private String url;
    private String username;
    private String password;

    private Boolean autoCommit;
    private Integer defaultTransactionIsolationLevel;

    static {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            registeredDrivers.put(driver.getClass().getName(), driver);
        }
    }

    public UnpooledDataSource() {
    }

    public void setProperties(Properties driverProperties) throws SQLException {
        this.driverProperties = driverProperties;
        this.driver = (String) this.driverProperties.get("driver");
        this.url = (String) this.driverProperties.get("url");
        this.username = (String) this.driverProperties.get("username");
        this.password = (String) this.driverProperties.get("password");
        initializeDriver();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return doGetConnection(username, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return doGetConnection(username, password);
    }

    @Override
    public void setLoginTimeout(int loginTimeout) throws SQLException {
        DriverManager.setLoginTimeout(loginTimeout);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter logWriter) throws SQLException {
        DriverManager.setLogWriter(logWriter);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    private Connection doGetConnection(String username, String password) throws SQLException {
        Properties props = new Properties();
        if (driverProperties != null) {
            props.putAll(driverProperties);
        }
        if (username != null) {
            props.setProperty("user", username);
        }
        if (password != null) {
            props.setProperty("password", password);
        }
        return doGetConnection(props);
    }

    private Connection doGetConnection(Properties properties) throws SQLException {
        initializeDriver();
        Connection connection = DriverManager.getConnection(url, properties);
        configureConnection(connection);
        return connection;
    }

    private synchronized void initializeDriver() throws SQLException {
        if (!registeredDrivers.containsKey(driver)) {
            try {
                Class<?> driverType = Resources.classForName(driver);
                // DriverManager requires the driver to be loaded via the system ClassLoader.
                // http://www.kfu.com/~nsayer/Java/dyn-jdbc.html
                Driver driverInstance = (Driver)driverType.newInstance();
                DriverManager.registerDriver(driverInstance);
                registeredDrivers.put(driver, driverInstance);
            } catch (Exception e) {
                throw new SQLException("Error setting driver on UnpooledDataSource. Cause: " + e);
            }
        }
    }

    private void configureConnection(Connection conn) throws SQLException {
        if (autoCommit != null && autoCommit != conn.getAutoCommit()) {
            conn.setAutoCommit(autoCommit);
        }
        if (defaultTransactionIsolationLevel != null) {
            conn.setTransactionIsolation(defaultTransactionIsolationLevel);
        }
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper.");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    public static void main(String[] args) throws SQLException {
        /**
         * db.driver=com.mysql.jdbc.Driver
         * db.url=jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8
         * db.username=root
         * db.password=123
         */
        DataSource dataSource = new UnpooledDataSource();

        Properties driverProperties = new Properties();
        driverProperties.setProperty("driver", "com.mysql.jdbc.Driver");
        driverProperties.setProperty("url", "jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8");
        driverProperties.setProperty("username", "root");
        driverProperties.setProperty("password", "123");

        ((UnpooledDataSource) dataSource).setProperties(driverProperties);

        Connection connection = dataSource.getConnection();
        assert connection != null;


    }

}
