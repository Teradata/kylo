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

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * a refreshable data source provides additional functionality over a basic data source that allows the connection to be maintained
 */
public class RefreshableDataSource extends BasicDataSource {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RefreshableDataSource.class);
    private AtomicReference<DataSource> target = new AtomicReference<>();
    private AtomicBoolean isRefreshing = new AtomicBoolean(false);
    private String driverClassName;
    private String url;
    private String username;
    private String password;
    private ClassLoader driverClassLoader;
    private String validationQuery;
    private Long validationQueryTimeout;

    // single thread executor service that will kill threads on application shutdown
    private ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).build());
    private ExecutorService executorForCleanup = Executors.newFixedThreadPool(4, new ThreadFactoryBuilder().setDaemon(true).build());

    /**
     * default constructor takes the parameters needed to keep connections refreshed
     *
     * @param driverClassName   the driver class name
     * @param url               the JDBC url
     * @param username          the user name
     * @param password          the user password
     * @param driverClassLoader the driver class loader
     * @param validationQuery   the query used to test connections
     */
    public RefreshableDataSource(String driverClassName, String url, String username, String password,
                                 ClassLoader driverClassLoader, String validationQuery, Long validationQueryTimeout) {
        this.driverClassName = driverClassName;
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassLoader = driverClassLoader;
        this.validationQuery = validationQuery;
        this.validationQueryTimeout = validationQueryTimeout;
        refresh();
    }

    /**
     * called to refresh the connection if needed
     */
    public void refresh() {
        if (isRefreshing.compareAndSet(false, true)) {
            log.info("REFRESHING DATASOURCE for {} ", this.url);
            target.set(create());
            isRefreshing.set(false);
        } else {
            //unable to refresh.  Refresh already in progress
        }
    }

    /**
     * test the connection to see if it can be used to communicate with the JDBC source
     *
     * @return true if the connection is alive
     * @throws SQLException if the connection is not alive
     */
    public boolean testConnection() throws SQLException {
        return testConnection(null, null);
    }

    /**
     * test the connection to see if it can be used to communicate with the JDBC source
     *
     * @param username a username to connect with if needed
     * @param password a password to connect with if needed
     * @return true if the connection is alive
     */
    public synchronized boolean testConnection(String username, String password) {
        // Get a connection to test
        final Connection connection;
        try {
            connection = (StringUtils.isNotBlank(username) || StringUtils.isNotBlank(password)) ? getConnectionForValidation(username, password) : getConnectionForValidation();
            log.info("connection obtained by RefreshableDatasource");
        } catch (final SQLException e) {
            log.warn("A database access error occurred when getting a connection for JDBC URL: ", url, e);
            return false;
        }

        // Test the connection using different methods
        boolean asyncCleanup = false;
        Statement statement = null;

        try {
            // Method 1: Test using driver method
            try {
                // can throw "java.sql.SQLException: Method not supported"; ignore and try other methods if so
                final boolean isValid = connection.isValid(validationQueryTimeout.intValue());
                if (!isValid) {
                    log.info("Connection obtained for JDBC URL was not valid: {}", url);
                    return false;
                }
            } catch (final SQLException e) {
                log.debug("The isValid() method is not supported for the JDBC URL: {}", url);
            }

            // Method 2: Test with a statement and query timeout
            try {
                statement = connection.createStatement();
            } catch (final SQLException e) {
                log.warn("A database access error occurred when getting a statement for JDBC URL: {}", url, e);
                return false;
            }

            try {
                statement.setQueryTimeout(validationQueryTimeout.intValue());  // throws method not supported if Hive driver
                try {
                    statement.execute(validationQuery);  // executes if no exception from setQueryTimeout
                    return true;
                } catch (final SQLException e) {
                    log.debug("Failed to execute validation query for JDBC URL: {}", url, e);
                    log.info("Connection obtained for JDBC URL was not valid: {}", url);
                    return false;
                }
            } catch (final SQLException e) {
                log.warn("The Statement.setQueryTimeout() method is not supported for the JDBC URL: {}", url);
            }

            // Method 3: Test with a statement and a timer
            asyncCleanup = true;
            boolean isValid;

            try {
                isValid = validateQueryWithTimeout(statement, validationQuery, validationQueryTimeout.intValue());
            } catch (final SQLException e) {
                log.debug("Failed to execute validation query for JDBC URL: {}", url, e);
                isValid = false;
            }

            if (!isValid) {
                log.info("Connection obtained for JDBC URL was not valid: {}", url);
            }
            return isValid;
        } finally {
            connectionCleanup(connection, statement, asyncCleanup);
        }
    }

    /**
     * If this method is called we should be able to assume we've cleaned up the resources.
     */
    private synchronized void connectionCleanup(final Connection connection, final Statement statement, boolean useBackgroundThread) {
        if (useBackgroundThread) {
            Callable<Boolean> callable = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    if (statement != null) {
                        log.debug("Cleanup Executor about to call statement.close()");
                        statement.close();
                    }
                    if (connection != null) {
                        log.debug("Cleanup Executor about to call connection.close()");
                        connection.close();
                    }
                    log.debug("Cleanup Executor completed.");
                    return true;
                }
            };

            // throw it into a background thread that will wait a long time for statement.close and connection.close to complete
            // this will allow the current thread to overwrite statement and connection without making the current thread wait
            // on garbage collecting the objects
            log.info("Cleaning up the current connection using a background thread.");
            if (log.isDebugEnabled()) {
                // since we submit and forget, it could be possible that some other connections are waiting clean up going
                // in.  Seems highly unlikely in observed scenarios.
                log.debug("Cleanup Executor at '{}' active threads prior to initiating clean up", ((ThreadPoolExecutor) executorForCleanup).getActiveCount());
            }

            executorForCleanup.submit(callable);
        } else {
            try {
                //  clean up if query not interrupted within system defined default timeout (15 minutes observed)
                if (statement != null) {
                    log.debug("RefreshableDataSource about to call statement.close() in current thread");
                    statement.close();
                }
                if (connection != null) {
                    log.debug("RefreshableDataSource about to call connection.close() in current thread");
                    connection.close();
                }
            } catch (SQLException se) {
                // log and swallow
                log.error("Ignoring SQLException since it should just be an indicator that the current connection is not usable and we should refresh.", se);
            }
        }
    }

    /**
     * @param statement       statement handle to use for execution
     * @param validationQuery query to use to check the connection
     * @param timeout         time, in seconds, to wait for the query to complete
     * @return true if query had to be timed out, false otherwise.
     */
    private synchronized boolean validateQueryWithTimeout(final Statement statement, final String validationQuery, int timeout) throws SQLException {
        log.info("perform validation query in RefreshableDatasource.executeWithTimeout()");
        final Stopwatch timer = Stopwatch.createStarted();
        try {
            executor.submit(() -> statement.execute(validationQuery)).get(timeout, TimeUnit.SECONDS);
            log.info("validation query returned from RefreshableDatasource.executeWithTimeout() in {}", timer.stop());
            return true;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Unlikely scenario that query thread was interrupted. Application going down?", e);
            throw new SQLException(e);
        } catch (final TimeoutException e) {
            return false;
        } catch (final Exception e) {
            Throwables.propagateIfInstanceOf(e.getCause(), SQLException.class);
            throw Throwables.propagate(e.getCause());
        }
    }

    private Connection getConnectionForValidation() throws SQLException {
        return getDataSource().getConnection();
    }

    private Connection getConnectionForValidation(String username, String password) throws SQLException {
        return getDataSource().getConnection();
    }

    private synchronized Connection testAndRefreshIfInvalid() throws SQLException {
        if (!testConnection()) {
            refresh();
        }
        return getConnectionForValidation();
    }

    private synchronized Connection testAndRefreshIfInvalid(String username, String password) throws SQLException {
        if (!testConnection(username, password)) {
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

    /**
     * A builder class for collecting required parameters to create/maintain a connection
     **/
    public static class Builder {

        private String driverClassName;
        private String url;
        private String username;
        private String password;
        private ClassLoader driverClassLoader;
        private String validationQuery;
        private Long validationQueryTimeout;

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

        public Builder validationQueryTimeout(Long validationQueryTimeout) {
            this.validationQueryTimeout = validationQueryTimeout;
            return this;
        }

        public RefreshableDataSource build() {
            return new RefreshableDataSource(driverClassName, url, username, password, driverClassLoader, validationQuery, validationQueryTimeout);
        }

    }

}
