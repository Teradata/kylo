package com.thinkbiganalytics.nifi.provenance.v2;


import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.sql.DataSource;

/**
 * simple Connection Pooling service to return a new DataSource
 */
public class PoolingDataSourceService {

    public static DataSource getDataSource(String connectURI, String user, String password) {

        ConnectionFactory connectionFactory =
                new DriverManagerConnectionFactory(connectURI, user, password);


        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(connectionFactory, null);


        ObjectPool<PoolableConnection> connectionPool =
                new GenericObjectPool<>(poolableConnectionFactory);

        // Set the factory's pool property to the owning pool
        poolableConnectionFactory.setPool(connectionPool);


        PoolingDataSource<PoolableConnection> dataSource =
                new PoolingDataSource<>(connectionPool);

        return dataSource;
    }
}