package com.thinkbiganalytics.nifi.v2.thrift;

/*-
 * #%L
 * thinkbig-nifi-hadoop-service
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * Created by sr186054 on 9/13/16.
 */
public class RefreshableDataSource extends BasicDataSource {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RefreshableDataSource.class);

    private AtomicReference<DataSource> target = new AtomicReference<DataSource>();

    private AtomicBoolean isRefreshing = new AtomicBoolean(false);

    private String driverClassName;
    private String url;
    private String username;
    private String password;
    private ClassLoader driverClassLoader;
    private String validationQuery;

    public RefreshableDataSource(String driverClassName, String url, String username, String password, ClassLoader driverClassLoader, String validationQuery) {
        this.driverClassName = driverClassName;
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassLoader = driverClassLoader;
        this.validationQuery = validationQuery;
        refresh();
    }


    public RefreshableDataSource() {

    }

    public void refresh() {
        if (isRefreshing.compareAndSet(false, true)) {
            log.info("REFRESHING DATASOURCE for {} ", this.url);
            target.set(create());
            isRefreshing.set(false);
        } else {
            //unable to refresh.  Refresh already in progress
        }
    }

    public boolean testConnection() throws SQLException {
        return testConnection(null, null);
    }

    public boolean testConnection(String username, String password) throws SQLException {
        boolean valid = false;
        Connection connection = null;
        Statement statement = null;
        try {

            if (StringUtils.isNotBlank(username) || StringUtils.isNotBlank(password)) {
                connection = getConnectionForValidation(username, password);
            } else {
                connection = getConnectionForValidation();
            }
            statement = connection.createStatement();
            statement.execute(validationQuery);
            valid = true;
        } catch (SQLException e) {
            throw e;
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
        return valid;

    }

    private Connection getConnectionForValidation() throws SQLException {
        return getDataSource().getConnection();
    }

    private Connection getConnectionForValidation(String username, String password) throws SQLException {
        return getDataSource().getConnection();
    }

    private Connection testAndRefreshIfInvalid() throws SQLException {

        try {
            testConnection();
        } catch (SQLException e) {
            refresh();
        }
        return getConnectionForValidation();
    }

    private Connection testAndRefreshIfInvalid(String username, String password) throws SQLException {

        try {
            testConnection(username, password);
        } catch (SQLException e) {
            refresh();
        }
        return getConnectionForValidation();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return testAndRefreshIfInvalid();


    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return testAndRefreshIfInvalid(username, password);
    }

    private DataSource getDataSource() {
        return target.get();
    }

    //Rest of DataSource methods

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return getDataSource().getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        getDataSource().setLogWriter(out);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return getDataSource().getLoginTimeout();
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        getDataSource().setLoginTimeout(seconds);
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return getDataSource().getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return getDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return getDataSource().isWrapperFor(iface);
    }

    private DataSource create() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setDriverClassLoader(driverClassLoader);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    public static class Builder {

        private String driverClassName;
        private String url;
        private String username;
        private String password;
        private ClassLoader driverClassLoader;
        private String validationQuery;

        public Builder driverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder driverClassLoader(ClassLoader classLoader) {
            this.driverClassLoader = classLoader;
            return this;
        }

        public Builder validationQuery(String validationQuery) {
            this.validationQuery = validationQuery;
            return this;
        }

        public RefreshableDataSource build() {
            return new RefreshableDataSource(driverClassName, url, username, password, driverClassLoader, validationQuery);
        }

    }

}
