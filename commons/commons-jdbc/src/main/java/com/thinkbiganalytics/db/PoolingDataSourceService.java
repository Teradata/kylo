package com.thinkbiganalytics.db;

/*-
 * #%L
 * kylo-commons-jdbc
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

/**
 * Maintains a pool of JDBC connections that can be reused within a limited timeframe.
 *
 * <p>Used for the {@code DBSchemaParser} class.</p>
 */
public class PoolingDataSourceService extends CacheLoader<com.thinkbiganalytics.db.DataSourceProperties, DataSource>
    implements RemovalListener<com.thinkbiganalytics.db.DataSourceProperties, DataSource> {

    private static final Logger log = LoggerFactory.getLogger(PoolingDataSourceService.class);

    /**
     * Cache of data sources.
     *
     * <p>Expires unused entries to avoid long-term caching of data sources, including those that have been deleted or contain invalid credentials.</p>
     */
    private static final LoadingCache<com.thinkbiganalytics.db.DataSourceProperties, DataSource> DATA_SOURCES;

    static {
        final PoolingDataSourceService service = new PoolingDataSourceService();
        DATA_SOURCES = CacheBuilder.newBuilder()
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .removalListener(service)
            .build(service);
    }

    /**
     * Gets the data source using the specified properties.
     *
     * @param props the data source properties
     * @return the data source
     */
    @Nonnull
    public static DataSource getDataSource(@Nonnull final com.thinkbiganalytics.db.DataSourceProperties props) {
        try {
            return DATA_SOURCES.get(props);
        } catch (final ExecutionException e) {
            throw Throwables.propagate((e.getCause() != null) ? e.getCause() : e);
        }
    }

    /**
     * Information for creating and identifying a JDBC data source.
     */
    @Deprecated
    public static class DataSourceProperties extends com.thinkbiganalytics.db.DataSourceProperties {

        public DataSourceProperties(String user, String password, String url) {
            super(user, password, url);
        }

        public DataSourceProperties(String user, String password, String url, String driverClassName, boolean testOnBorrow, String validationQuery) {
            super(user, password, url, driverClassName, testOnBorrow, validationQuery);
        }
    }

    /**
     * Instances should only be constructed by {@code PoolingDataSourceService}.
     */
    private PoolingDataSourceService() {
    }

    @Nonnull
    @Override
    public DataSource load(@Nonnull final com.thinkbiganalytics.db.DataSourceProperties props) {
        final DataSource dataSource = createDataSource(props);

        if (dataSource instanceof org.apache.tomcat.jdbc.pool.DataSource) {
            configureDataSource((org.apache.tomcat.jdbc.pool.DataSource) dataSource, props);
        } else if (dataSource instanceof org.apache.commons.dbcp2.BasicDataSource) {
            configureDataSource((org.apache.commons.dbcp2.BasicDataSource) dataSource, props);
        } else if (dataSource instanceof org.apache.commons.dbcp.BasicDataSource) {
            configureDataSource((org.apache.commons.dbcp.BasicDataSource) dataSource, props);
        } else {
            log.warn("DataSource not supported: {}", dataSource.getClass().getName());
        }

        return dataSource;
    }

    @Override
    public void onRemoval(@Nonnull final RemovalNotification<com.thinkbiganalytics.db.DataSourceProperties, DataSource> notification) {
        // Release DataSource resources
        final DataSource dataSource = notification.getValue();

        try {
            if (dataSource instanceof org.apache.tomcat.jdbc.pool.DataSource) {
                ((org.apache.tomcat.jdbc.pool.DataSource) dataSource).close();
            } else if (dataSource instanceof org.apache.commons.dbcp2.BasicDataSource) {
                ((org.apache.commons.dbcp2.BasicDataSource) dataSource).close();
            } else if (dataSource instanceof org.apache.commons.dbcp.BasicDataSource) {
                ((org.apache.commons.dbcp.BasicDataSource) dataSource).close();
            }
        } catch (final SQLException e) {
            log.debug("Failed to close DataSource: {}: {}", dataSource, e);
        }

        // Release DataSourceProperties resources
        final ClassLoader classLoader = (notification.getKey() != null) ? notification.getKey().getDriverClassLoader() : null;
        if (classLoader instanceof Closeable) {
            try {
                ((Closeable) classLoader).close();
            } catch (final IOException e) {
                log.debug("Failed to close ClassLoader for driver: {}: {}", notification.getKey().getDriverClassName(), e);
            }
        }
    }

    /**
     * Configures a Tomcat data source with the specified properties.
     */
    private void configureDataSource(@Nonnull final org.apache.tomcat.jdbc.pool.DataSource dataSource, @Nonnull final com.thinkbiganalytics.db.DataSourceProperties props) {
        dataSource.setTestOnBorrow(true);
        dataSource.setValidationQuery(props.getValidationQuery());

        if (props.getProperties() != null) {
            dataSource.setDbProperties(props.getProperties());
        }
        if (props.getDriverClassLoader() != null) {
            throw new IllegalStateException("Cannot set driver classloader on a Tomcat DataSource");
        }
    }

    /**
     * Configures an Apache Commons data source with the specified properties.
     */
    private void configureDataSource(@Nonnull final org.apache.commons.dbcp2.BasicDataSource dataSource, @Nonnull final com.thinkbiganalytics.db.DataSourceProperties props) {
        dataSource.setDriverClassLoader(props.getDriverClassLoader());
        dataSource.setValidationQuery(props.getValidationQuery());
        dataSource.setTestOnBorrow(true);

        if (props.getProperties() != null) {
            for (final Map.Entry<Object, Object> entry : props.getProperties().entrySet()) {
                dataSource.addConnectionProperty((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

    /**
     * Configures an Apache Commons data source with the specified properties.
     */
    private void configureDataSource(@Nonnull final org.apache.commons.dbcp.BasicDataSource dataSource, @Nonnull final com.thinkbiganalytics.db.DataSourceProperties props) {
        dataSource.setDriverClassLoader(props.getDriverClassLoader());
        dataSource.setValidationQuery(props.getValidationQuery());
        dataSource.setTestOnBorrow(true);

        if (props.getProperties() != null) {
            for (final Map.Entry<Object, Object> entry : props.getProperties().entrySet()) {
                dataSource.addConnectionProperty((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

    /**
     * Creates a new data source using the specified properties.
     */
    @Nonnull
    private DataSource createDataSource(@Nonnull final com.thinkbiganalytics.db.DataSourceProperties props) {
        final DataSourceBuilder builder = DataSourceBuilder.create().url(props.getUrl()).username(props.getUser()).password(props.getPassword());

        if (StringUtils.isNotBlank(props.getDriverClassName())) {
            builder.driverClassName(props.getDriverClassName());
        }
        if (props.getDriverClassLoader() != null) {
            builder.type(org.apache.commons.dbcp2.BasicDataSource.class);
        }

        return builder.build();
    }
}
