package com.thinkbiganalytics.nifi.v2.thrift;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Workaround which allows to use URLClassLoader for JDBC driver loading.
 * (Because the DriverManager will refuse to use a driver not loaded by the system ClassLoader.)
 */
class DriverShim implements Driver {
    private Driver driver;

    DriverShim(Driver d) {
        this.driver = d;
    }

    @Override
    public boolean acceptsURL(String u) throws SQLException {
        return this.driver.acceptsURL(u);
    }

    @Override
    public Connection connect(String u, Properties p) throws SQLException {
        return this.driver.connect(u, p);
    }

    @Override
    public int getMajorVersion() {
        return this.driver.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return this.driver.getMinorVersion();
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
        return this.driver.getPropertyInfo(u, p);
    }

    @Override
    public boolean jdbcCompliant() {
        return this.driver.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return driver.getParentLogger();
    }

}
