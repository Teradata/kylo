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

import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.pool.KeyedObjectPoolFactory;

import java.sql.SQLException;

/**
 * a refreshable data source provides additional functionality over a basic data source that allows the connection to be maintained
 */
public final class RefreshableDataSource extends BasicDataSource {

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
    public RefreshableDataSource(String driverClassName,
                                 String url,
                                 String username,
                                 String password,
                                 ClassLoader driverClassLoader,
                                 String validationQuery,
                                 Integer validationQueryTimeout,
                                 Long maxWait,
                                 Integer maxActive) {
        super();
        setDriverClassName(driverClassName);
        setUrl(url);
        setUsername(username);
        setPassword(password);
        setDriverClassLoader(driverClassLoader);
        if ((validationQuery != null) && !validationQuery.isEmpty()) {
            setValidationQuery(validationQuery);
            setTestOnBorrow(true);
        }
        if (validationQueryTimeout != null) {
            setValidationQueryTimeout(validationQueryTimeout);
        }
        if (maxActive != null) {
            setMaxActive(maxActive);
        }
        if (maxWait != null) {
            setMaxWait(maxWait);
        }
    }

    @Override
    protected void createPoolableConnectionFactory(
            ConnectionFactory driverConnectionFactory,
            KeyedObjectPoolFactory statementPoolFactory,
            AbandonedConfig configuration) throws SQLException {
        try {
            PoolableConnectionFactory connectionFactory =
                    new HivePoolableConnectionFactory(driverConnectionFactory,
                            connectionPool,
                            statementPoolFactory,
                            validationQuery,
                            validationQueryTimeout,
                            connectionInitSqls,
                            defaultReadOnly,
                            defaultAutoCommit,
                            defaultTransactionIsolation,
                            defaultCatalog,
                            configuration);
            validateConnectionFactory(connectionFactory);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Cannot create Hive connection factory", e);
        }
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
        private Integer validationQueryTimeout;
        private Integer maxActive;
        private Long maxWait;

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

        public Builder validationQueryTimeout(Integer validationQueryTimeout) {
            this.validationQueryTimeout = validationQueryTimeout;
            return this;
        }

        public Builder maxActive(Integer maxActive) {
            this.maxActive = maxActive;
            return this;
        }

        public Builder maxWait(Long maxWait) {
            this.maxWait = maxWait;
            return this;
        }

        public RefreshableDataSource build() {
            return new RefreshableDataSource(
                    driverClassName,
                    url,
                    username,
                    password,
                    driverClassLoader,
                    validationQuery,
                    validationQueryTimeout,
                    maxWait,
                    maxActive);
        }

    }

}
