package com.thinkbiganalytics.db;

/*-
 * #%L
 * thinkbig-feed-manager-controller
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


import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

/**
 * A Connection Pooling service to return a new DataSource.
 * <p>Used for the {@code DBSchemaParser} class.</p>
 */
public class PoolingDataSourceService {

    /**
     * Cache of data sources.
     *
     * <p>Expires unused entries to avoid long-term caching of data sources with invalid credentials.</p>
     */
    private static final LoadingCache<DataSourceProperties, DataSource> DATA_SOURCES = CacheBuilder.newBuilder()
        .expireAfterAccess(60, TimeUnit.MINUTES)
        .build(new CacheLoader<DataSourceProperties, DataSource>() {
            @Override
            public DataSource load(@Nonnull final DataSourceProperties key) throws Exception {
                return createDatasource(key);
            }
        });

    /**
     * Gets the data source using the specified properties.
     *
     * @param props the data source properties
     * @return the data source
     */
    public static DataSource getDataSource(@Nonnull final DataSourceProperties props) {
        try {
            return DATA_SOURCES.get(props);
        } catch (final ExecutionException e) {
            if (e.getCause() != null) {
                throw Throwables.propagate(e.getCause());
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private static DataSource createDatasource(DataSourceProperties props) {
        DataSourceBuilder builder = DataSourceBuilder.create().url(props.getUrl()).username(props.getUser()).password(props.getPassword());
        if (StringUtils.isNotBlank(props.getDriverClassName())) {
            builder.driverClassName(props.getDriverClassName());
        }
        DataSource ds = builder.build();
        if (props.isTestOnBorrow() && StringUtils.isNotBlank(props.getValidationQuery())) {
            if (ds instanceof org.apache.tomcat.jdbc.pool.DataSource) {
                ((org.apache.tomcat.jdbc.pool.DataSource) ds).setTestOnBorrow(true);
                ((org.apache.tomcat.jdbc.pool.DataSource) ds).setValidationQuery(props.getValidationQuery());
            } else if (ds instanceof org.apache.commons.dbcp2.BasicDataSource) {
                ((org.apache.commons.dbcp2.BasicDataSource) ds).setValidationQuery(props.getValidationQuery());
                ((org.apache.commons.dbcp2.BasicDataSource) ds).setTestOnBorrow(true);
            } else if (ds instanceof org.apache.commons.dbcp.BasicDataSource) {
                ((org.apache.commons.dbcp.BasicDataSource) ds).setValidationQuery(props.getValidationQuery());
                ((org.apache.commons.dbcp.BasicDataSource) ds).setTestOnBorrow(true);
            }
        }
        return ds;
    }

    public static class DataSourceProperties {

        String user;
        String password;
        String url;
        String driverClassName;
        boolean testOnBorrow;
        String validationQuery;

        public DataSourceProperties(String user, String password, String url) {
            this.user = user;
            this.password = password;
            this.url = url;
        }

        public DataSourceProperties(String user, String password, String url, String driverClassName, boolean testOnBorrow, String validationQuery) {
            this.user = user;
            this.password = password;
            this.url = url;
            this.driverClassName = driverClassName;
            this.testOnBorrow = testOnBorrow;
            this.validationQuery = validationQuery;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isTestOnBorrow() {
            return testOnBorrow;
        }

        public void setTestOnBorrow(boolean testOnBorrow) {
            this.testOnBorrow = testOnBorrow;
        }

        public String getValidationQuery() {
            return validationQuery;
        }

        public void setValidationQuery(String validationQuery) {
            this.validationQuery = validationQuery;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public void setDriverClassName(String driverClassName) {
            this.driverClassName = driverClassName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            DataSourceProperties that = (DataSourceProperties) o;
            return Objects.equals(user, that.user) &&
                   Objects.equals(password, that.password) &&
                   Objects.equals(url, that.url) &&
                   Objects.equals(driverClassName, that.driverClassName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(user, password, url, driverClassName);
        }
    }

    /**
     * Instances of {@code PoolingDataSourceService} may not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private PoolingDataSourceService() {
        throw new UnsupportedOperationException();
    }
}
