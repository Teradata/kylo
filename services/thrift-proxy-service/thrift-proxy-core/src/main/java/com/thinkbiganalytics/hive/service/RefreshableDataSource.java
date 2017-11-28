package com.thinkbiganalytics.hive.service;

/*-
 * #%L
 * thinkbig-thrift-proxy-core
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

import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.kerberos.KerberosUtil;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.sql.DataSource;

/**
 */
public class RefreshableDataSource implements DataSource {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RefreshableDataSource.class);

    private static final String DEFAULT_DATASOURCE_NAME = "DEFAULT";
    String propertyPrefix;
    @Autowired
    Environment env;
    private ConcurrentHashMap<String, DataSource> datasources = new ConcurrentHashMap<>();
    private AtomicBoolean isRefreshing = new AtomicBoolean(false);
    @Inject
    @Qualifier("kerberosHiveConfiguration")
    private KerberosTicketConfiguration kerberosTicketConfiguration;

    public RefreshableDataSource(String propertyPrefix) {
        this.propertyPrefix = propertyPrefix;
    }

    public void refresh() {
        if (isRefreshing.compareAndSet(false, true)) {
            log.info("REFRESHING DATASOURCE for {} ", propertyPrefix);
            boolean userImpersonationEnabled = Boolean.valueOf(env.getProperty("hive.userImpersonation.enabled"));
            if (userImpersonationEnabled && propertyPrefix.equals("hive.datasource")) {
                String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
                DataSource dataSource = create(true, currentUser);
                datasources.put(currentUser, dataSource);
            } else {
                DataSource dataSource = create(false, null);
                datasources.put(DEFAULT_DATASOURCE_NAME, dataSource);
            }
            isRefreshing.set(false);
        }
    }

    public boolean testConnection() throws SQLException {
        return testConnection(null, null);
    }

    public boolean testConnection(String username, String password) throws SQLException {
        boolean isValidConnection = false;

        try {
            isValidConnection = KerberosUtil.runWithOrWithoutKerberos(() -> {
                boolean valid = false;
                Connection connection = null;
                Statement statement = null;
                try {
                    String prefix = getPrefixWithTrailingDot();
                    String query = env.getProperty(prefix + "validationQuery");
                    connection = getConnectionForValidation();
                    statement = connection.createStatement();
                    statement.execute(query);
                    valid = true;
                } catch (SQLException e) {
                    DataSourceUtils.releaseConnection(connection, this.getDataSource());
                    throw e;
                } finally {
                    JdbcUtils.closeStatement(statement);
                    DataSourceUtils.releaseConnection(connection, this.getDataSource());
                }
                return valid;
            }, kerberosTicketConfiguration);
        } catch(Exception e) {
            // The utility method throws a Runtime Exception so we need to re-throw it as a SQL Exception in our case
            throw new SQLException(e);
        }
        return isValidConnection;
    }

    private Connection getConnectionForValidation() throws SQLException {
        if (getDataSource() == null) {
            refresh();
        }
        return KerberosUtil.getConnectionWithOrWithoutKerberos(getDataSource(), kerberosTicketConfiguration);
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
        boolean userImpersonationEnabled = Boolean.valueOf(env.getProperty("hive.userImpersonation.enabled"));
        if (userImpersonationEnabled && propertyPrefix.equals("hive.datasource")) {
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            return datasources.get(currentUser);
        } else {
            return datasources.get(DEFAULT_DATASOURCE_NAME);
        }
    }


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

    private String getPrefixWithTrailingDot() {
        String prefix = propertyPrefix.endsWith(".") ? propertyPrefix : propertyPrefix + ".";
        return prefix;
    }

    private DataSource create(boolean proxyUser, String principal) {
        String prefix = getPrefixWithTrailingDot();

        String driverClassName = env.getProperty(prefix + "driverClassName");
        String url = env.getProperty(prefix + "url");
        String password = env.getProperty(prefix + "password");
        String userName = env.getProperty(prefix + "username");

        if (proxyUser && propertyPrefix.equals("hive.datasource")) {
            userName = principal;
            url = url + ";hive.server2.proxy.user=" + principal;
        }
        log.debug("The JDBC URL is " + url + " --- User impersonation enabled: " + proxyUser);
        String username = userName;

        DataSource ds = DataSourceBuilder.create().driverClassName(driverClassName).url(url).username(username).password(password).build();
        return ds;
    }

}
