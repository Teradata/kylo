package com.thinkbiganalytics.nifi.v2.thrift;

/*-
 * #%L
 * kylo-nifi-hadoop-service
 *
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

import org.apache.commons.dbcp.AbandonedConfig;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.ObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An extension to {@link PoolableConnectionFactory} so that we can test connection when borrow it from the pool
 *
 * Created by Binh Van Nguyen (binhnv80@gmail.com)
 */
public class HivePoolableConnectionFactory extends PoolableConnectionFactory {
    public HivePoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, boolean defaultReadOnly, boolean defaultAutoCommit) {
        super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit);
    }

    public HivePoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, Collection connectionInitSqls, boolean defaultReadOnly, boolean defaultAutoCommit) {
        super(connFactory, pool, stmtPoolFactory, validationQuery, connectionInitSqls, defaultReadOnly, defaultAutoCommit);
    }

    public HivePoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, int validationQueryTimeout, boolean defaultReadOnly, boolean defaultAutoCommit) {
        super(connFactory, pool, stmtPoolFactory, validationQuery, validationQueryTimeout, defaultReadOnly, defaultAutoCommit);
    }

    public HivePoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, int validationQueryTimeout, Collection connectionInitSqls, boolean defaultReadOnly, boolean defaultAutoCommit) {
        super(connFactory, pool, stmtPoolFactory, validationQuery, validationQueryTimeout, connectionInitSqls, defaultReadOnly, defaultAutoCommit);
    }

    public HivePoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation) {
        super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation);
    }

    public HivePoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, Collection connectionInitSqls, boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation) {
        super(connFactory, pool, stmtPoolFactory, validationQuery, connectionInitSqls, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation);
    }

    public HivePoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, int validationQueryTimeout, boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation) {
        super(connFactory, pool, stmtPoolFactory, validationQuery, validationQueryTimeout, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation);
    }

    public HivePoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, int validationQueryTimeout, Collection connectionInitSqls, boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation) {
        super(connFactory, pool, stmtPoolFactory, validationQuery, validationQueryTimeout, connectionInitSqls, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation);
    }

    public HivePoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, boolean defaultReadOnly, boolean defaultAutoCommit, AbandonedConfig config) {
        super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit, config);
    }

    public HivePoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation, AbandonedConfig config) {
        super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation, config);
    }

    public HivePoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation, String defaultCatalog, AbandonedConfig config) {
        super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation, defaultCatalog, config);
    }

    public HivePoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, Boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation, String defaultCatalog, AbandonedConfig config) {
        super(connFactory, pool, stmtPoolFactory, validationQuery, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation, defaultCatalog, config);
    }

    public HivePoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, Collection connectionInitSqls, Boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation, String defaultCatalog, AbandonedConfig config) {
        super(connFactory, pool, stmtPoolFactory, validationQuery, connectionInitSqls, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation, defaultCatalog, config);
    }

    public HivePoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, int validationQueryTimeout, Boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation, String defaultCatalog, AbandonedConfig config) {
        super(connFactory, pool, stmtPoolFactory, validationQuery, validationQueryTimeout, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation, defaultCatalog, config);
    }

    public HivePoolableConnectionFactory(ConnectionFactory connFactory, ObjectPool pool, KeyedObjectPoolFactory stmtPoolFactory, String validationQuery, int validationQueryTimeout, Collection connectionInitSqls, Boolean defaultReadOnly, boolean defaultAutoCommit, int defaultTransactionIsolation, String defaultCatalog, AbandonedConfig config) {
        super(connFactory, pool, stmtPoolFactory, validationQuery, validationQueryTimeout, connectionInitSqls, defaultReadOnly, defaultAutoCommit, defaultTransactionIsolation, defaultCatalog, config);
    }

    private static final Logger log = LoggerFactory.getLogger(HivePoolableConnectionFactory.class);

    // single thread executor service that will kill threads on application shutdown
    private ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).build());

    /**
     * Validate a connection
     *
     * {@link org.apache.hive.jdbc.HiveStatement} doesn't support setQueryTimeout but this
     * method is used in {@link PoolableConnectionFactory} if validation query timeout is provided
     * so we need to overwrite this method to provide our custom validation. We still validate
     * connection with validation query but we don't use driver's timeout.
     *
     * @param conn A connection to validate
     * @throws SQLException An exception if there is any error
     */
    @Override
    public void validateConnection(Connection conn) throws SQLException {
        if (_validationQueryTimeout <= 0) {
            super.validateConnection(conn);
        } else {
            validateConnectionWithTimeout(conn);
        }
    }

    private synchronized void validateConnectionWithTimeout(Connection conn) throws SQLException {
        final Stopwatch timer = Stopwatch.createStarted();

        try(Statement statement = conn.createStatement()) {
            executor.submit(() -> statement.execute(_validationQuery))
                    .get(_validationQueryTimeout, TimeUnit.SECONDS);
            log.debug("Validation query finished in {}", timer.stop());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException(e);
        } catch (final TimeoutException e) {
            throw new SQLException(e);
        } catch (final Exception e) {
            Throwables.propagateIfInstanceOf(e.getCause(), SQLException.class);
            throw Throwables.propagate(e.getCause());
        }
    }
}
