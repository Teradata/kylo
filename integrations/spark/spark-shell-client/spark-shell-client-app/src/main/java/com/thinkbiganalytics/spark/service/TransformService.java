package com.thinkbiganalytics.spark.service;

/*-
 * #%L
 * thinkbig-spark-shell-client-app
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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.cache.Weigher;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.thinkbiganalytics.hive.util.HiveUtils;
import com.thinkbiganalytics.kerberos.KerberosTicketConfiguration;
import com.thinkbiganalytics.kerberos.KerberosTicketGenerator;
import com.thinkbiganalytics.spark.SparkContextService;
import com.thinkbiganalytics.spark.metadata.TransformJob;
import com.thinkbiganalytics.spark.repl.SparkScriptEngine;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.shell.DatasourceProvider;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.script.ScriptException;

import scala.Option;
import scala.tools.nsc.interpreter.NamedParam;
import scala.tools.nsc.interpreter.NamedParamClass;

/**
 * A scheduled service that manages a database containing cached results of Spark jobs.
 *
 * <p>Before calling {@link #stopAsync()}, all Spark should should be cancelled to ensure that no additional tables will be
 * created.</p>
 */
@Component
public class TransformService extends AbstractScheduledService {

    private static final Logger log = LoggerFactory.getLogger(TransformService.class);

    /**
     * Name of the database
     */
    private static final String DATABASE = "spark_shell_temp";

    /**
     * Time in seconds after last use for a table to expire
     */
    private static final long EXPIRE_TIME = 3600L;

    /**
     * Minimum table size in bytes, to ensure there aren't too many tables
     */
    private static final int MIN_BYTES = 1048576;

    /**
     * Maximum database size in bytes (soft limit)
     */
    private static final long MAX_BYTES = 10737418240L;
    /**
     * Tables with cached results
     */
    @Nonnull
    private final TableCache cache = new TableCache();
    /**
     * Kerberos authentication configuration
     */
    private KerberosTicketConfiguration kerberosTicketConfiguration;
    /**
     * Script execution engine
     */
    @Autowired
    private SparkScriptEngine engine;

    /**
     * Job tracker for transformations
     */
    @Autowired
    private TransformJobTracker tracker;

    /**
     * Provides access to the Spark context
     */
    @Autowired
    private SparkContextService scs;

    public TransformService() {
        //required when created by Spring
    }

    /**
     * Constructs a {@code TransformService} using the specified engine to execute scripts.
     *
     * @param engine                      the script engine
     * @param kerberosTicketConfiguration Kerberos authentication configuration
     * @param tracker                     job tracker
     */
    public TransformService(@Nonnull final SparkScriptEngine engine, @Nonnull final KerberosTicketConfiguration kerberosTicketConfiguration, @Nonnull final TransformJobTracker tracker) {
        this();
        this.engine = engine;
        this.kerberosTicketConfiguration = kerberosTicketConfiguration;
        this.tracker = tracker;
    }

    /**
     * Executes the specified transformation and returns the name of the Hive table containing the results.
     *
     * @param request the transformation request
     * @return the Hive table containing the results
     * @throws IllegalStateException if this service is not running
     * @throws ScriptException       if the script cannot be executed
     */
    @Nonnull
    public TransformResponse execute(@Nonnull final TransformRequest request) throws ScriptException {
        log.trace("entry params({})", request);

        // Verify state
        if (!isRunning()) {
            IllegalStateException e = new IllegalStateException("Transform service has not been started");
            log.error("Throwing {}", e);
            throw e;
        }

        // Generate destination
        String table = newTableName();
        this.cache.put(table, MIN_BYTES);

        // Build bindings list
        final List<NamedParam> bindings = new ArrayList<>();
        bindings.add(new NamedParamClass("database", "String", DATABASE));
        bindings.add(new NamedParamClass("tableName", "String", table));

        if (request.getDatasources() != null && !request.getDatasources().isEmpty()) {
            final DatasourceProvider datasourceProvider = new DatasourceProvider(request.getDatasources());
            bindings.add(new NamedParamClass("datasourceProvider", DatasourceProvider.class.getName(), datasourceProvider));
        }

        // Execute script
        Object result = this.engine.eval(toScript(request), bindings);

        TransformJob job;
        if (result instanceof Callable) {
            @SuppressWarnings("unchecked")
            Callable<TransformResponse> callable = (Callable) result;
            job = new TransformJob(table, callable, engine.getSparkContext());
            tracker.submitJob(job);
        } else {
            IllegalStateException e = new IllegalStateException("Unexpected script result type: " + (result != null ? result.getClass() : null));
            log.error("Throwing {}", e);
            throw e;
        }

        // Update table weight
        try {
            updateWeight(table, this.engine.getSQLContext());
        } catch (Exception e) {
            log.warn("Failed to update table weight: {}", e.toString());
        }

        // Build response
        TransformResponse response;

        try {
            response = job.get(500, TimeUnit.MILLISECONDS);
            tracker.removeJob(table);
        } catch (ExecutionException cause) {
            ScriptException e = new ScriptException(cause);
            log.error("Throwing {}", e);
            throw e;
        } catch (Exception cause) {
            response = new TransformResponse();
            response.setProgress(0.0);
            response.setStatus(TransformResponse.Status.PENDING);
            response.setTable(table);
        }

        log.trace("exit with({})", response);
        return response;
    }

    /**
     * Gets the transformation job with the specified id.
     *
     * @param id the table with the results
     * @return the transformation job
     * @throws IllegalArgumentException if a job with the id does not exist
     */
    @Nonnull
    public TransformJob getJob(@Nonnull final String id) {
        Option<TransformJob> job = tracker.getJob(id);
        if (job.isDefined()) {
            if (job.get().isDone()) {
                tracker.removeJob(id);
            }
            return job.get();
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    protected void runOneIteration() throws Exception {
        log.trace("entry");

        // Clean-up cache
        this.cache.cleanUp();

        // Drop expired tables
        SQLContext context = this.engine.getSQLContext();
        Iterator<String> iter = this.cache.getExpired();

        while (iter.hasNext()) {
            dropTable(iter.next(), context);
        }

        log.trace("exit");
    }

    @Nonnull
    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(EXPIRE_TIME, EXPIRE_TIME, TimeUnit.SECONDS);
    }

    @Override
    protected void shutDown() {
        log.trace("entry");
        log.info("Stopping transform service");

        // Drop all tables
        SQLContext context = this.engine.getSQLContext();
        Iterator<String> iter = this.cache.getAll();

        while (iter.hasNext()) {
            dropTable(iter.next(), context);
        }

        log.trace("exit");
    }

    @Override
    protected void startUp() {
        log.trace("entry");
        log.info("Starting transform service");

        // Create database
        if (kerberosTicketConfiguration.isKerberosEnabled()) {
            createDatabaseWithKerberos();
        } else {
            createDatabaseWithoutKerberos();
        }

        // Add tracker
        tracker.addSparkListener(engine);

        log.trace("exit");
    }

    /**
     * Creates a database for storing temporary results.
     */
    void createDatabaseWithoutKerberos() {
        SQLContext context = this.engine.getSQLContext();
        scs.sql(context, "CREATE DATABASE IF NOT EXISTS " + HiveUtils.quoteIdentifier(DATABASE));

        // Drop existing tables
        List<Row> tables = scs.sql(context, "SHOW TABLES IN " + HiveUtils.quoteIdentifier(DATABASE)).collectAsList();

        for (Row table : tables) {
            dropTable(table.getString(0), context);
        }
    }

    /**
     * Creates a database for storing temporary results.
     */
    private void createDatabaseWithKerberos() {
        log.info("Initializing the database using Kerberos ");

        try {
            UserGroupInformation userGroupInformation;
            List<Row> tables;
            KerberosTicketGenerator t = new KerberosTicketGenerator();
            userGroupInformation = t.generateKerberosTicket(kerberosTicketConfiguration);
            userGroupInformation.doAs(new PrivilegedExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    SQLContext context = engine.getSQLContext();
                    scs.sql(context, "CREATE DATABASE IF NOT EXISTS " + HiveUtils.quoteIdentifier(DATABASE));

                    // Drop existing tables
                    List<Row> tables = scs.sql(context, "SHOW TABLES IN " + HiveUtils.quoteIdentifier(DATABASE)).collectAsList();

                    for (Row table : tables) {
                        dropTable(table.getString(0), context);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Error creating the database using Kerberos authentication", e);
        }
    }

    /**
     * Converts the specified transformation request to a Scala script that can be executed by the script engine.
     *
     * @param request the transformation request
     * @return the Scala script
     */
    @Nonnull
    protected String toScript(@Nonnull final TransformRequest request) {
        StringBuilder script = new StringBuilder();
        script.append("class Transform (destination: String, sendResults: Boolean, sqlContext: org.apache.spark.sql.SQLContext)");
        script.append(" extends com.thinkbiganalytics.spark.metadata.TransformScript(destination, sendResults, sqlContext) {\n");

        script.append("override def dataFrame: org.apache.spark.sql.DataFrame = {");
        script.append(request.getScript());
        script.append("}\n");

        if (request.getParent() != null) {
            script.append("override def parentDataFrame: org.apache.spark.sql.DataFrame = {");
            script.append(request.getParent().getScript());
            script.append("}\n");
            script.append("override def parentTable: String = {\"");
            script.append(StringEscapeUtils.escapeJava(request.getParent().getTable()));
            script.append("\"}\n");

            this.cache.touch(request.getParent().getTable());
        }

        script.append("}\n");
        script.append("new Transform(tableName, true, sqlContext).run()\n");

        return script.toString();
    }

    /**
     * Drops the table with the specified name using the specified context.
     *
     * @param name    the table name
     * @param context the SQL context
     */
    private void dropTable(@Nonnull final String name, @Nonnull final SQLContext context) {
        log.debug("Dropping cached table {}", name);

        // Remove from tracker
        tracker.removeJob(name);

        // Check if table is cached
        boolean isCached = false;

        try {
            isCached = context.isCached(name);
        } catch (Exception e) {
            // ignored
        }

        // Drop table
        try {
            if (isCached) {
                context.uncacheTable(name);
            } else {
                String identifier = HiveUtils.quoteIdentifier(DATABASE, name);
                scs.sql(context, "DROP TABLE IF EXISTS " + identifier);
            }
        } catch (Exception e) {
            log.warn("Unable to drop cached table {}: {}", name, e);
        }
    }

    /**
     * Generates a new, unique table name.
     *
     * @return the table name
     * @throws IllegalStateException if a table name cannot be generated
     */
    private String newTableName() {
        for (int i = 0; i < 100; ++i) {
            String name = UUID.randomUUID().toString();
            if (name.matches("^[a-fA-F].*")) {
                return name.replace("-", "");
            }
        }
        throw new IllegalStateException("Unable to generate a new table name");
    }

    /**
     * Updates the weight of the specified table in the cache using the current SQL context.
     *
     * @param table   the table name
     * @param context the SQL context
     */
    private void updateWeight(@Nonnull final String table, @Nonnull final SQLContext context) {
        int size = -1;

        // Get size for Hive tables
        if (!context.isCached(table)) {
            // Select service database
            context.sql("USE " + HiveUtils.quoteIdentifier(DATABASE));

            // Find size of table
            String identifier = HiveUtils.quoteIdentifier(table);
            List<Row> properties = scs.sql(this.engine.getSQLContext(), "SHOW TBLPROPERTIES " + identifier).collectAsList();

            for (Row row : properties) {
                String property = row.getString(0);
                if (property.startsWith("totalSize\t")) {
                    int tabIndex = property.indexOf('\t') + 1;
                    size = Integer.parseInt(property.substring(tabIndex));
                }
            }

            if (size == -1) {
                log.warn("Unknown table size for {}", identifier);
            }
        }

        // Update cache
        this.cache.put(table, Math.max(size, MIN_BYTES));
    }

    /**
     * Sets the Kerberos authentication configuration.
     *
     * @param kerberosTicketConfiguration the Kerberos authentication configuration
     */
    public void setKerberosTicketConfiguration(@Nonnull final KerberosTicketConfiguration kerberosTicketConfiguration) {
        this.kerberosTicketConfiguration = kerberosTicketConfiguration;
    }

    /**
     * A cache of Hive tables containing results of previous Spark jobs.
     */
    static class TableCache implements RemovalListener<String, Integer>, Weigher<String, Integer> {

        /**
         * Tables with cached results
         */
        @Nonnull
        private final Cache<String, Integer> active;

        /**
         * Expired tables that should be deleted
         */
        @Nonnull
        private final Queue<String> expired = Queues.newConcurrentLinkedQueue();

        /**
         * Constructs a {@code TableCache}.
         */
        TableCache() {
            this.active = CacheBuilder.newBuilder()
                .expireAfterAccess(EXPIRE_TIME, TimeUnit.SECONDS)
                .maximumWeight(MAX_BYTES)
                .removalListener(this)
                .weigher(this)
                .build();
        }

        /**
         * Performs any pending maintenance operations needed by the cache.
         */
        public void cleanUp() {
            this.active.cleanUp();
        }

        /**
         * Iterates over the list of all tables. There is no guarantee that the tables exist.
         *
         * @return all tables
         */
        public Iterator<String> getAll() {
            Iterator<String> active = this.active.asMap().keySet().iterator();
            return Iterators.concat(active, getExpired());
        }

        /**
         * Iterates over the list of expired tables. The tables are guaranteed to no longer be active but are not guaranteed to
         * exist.
         *
         * @return the expired tables
         */
        public Iterator<String> getExpired() {
            return new AbstractIterator<String>() {
                @Override
                protected String computeNext() {
                    String name;

                    do {
                        name = expired.poll();
                        if (name == null) {
                            return endOfData();
                        } else if (active.getIfPresent(name) != null) {
                            name = null;
                        }
                    }
                    while (name == null);

                    return name;
                }
            };
        }

        @Override
        public void onRemoval(@Nonnull final RemovalNotification<String, Integer> notification) {
            this.expired.add(notification.getKey());
        }

        /**
         * Associates the specified byte size with the specified table name in this cache, overwriting if necessary.
         *
         * @param name the table name
         * @param size the table size in bytes
         */
        public void put(@Nonnull final String name, final int size) {
            this.active.put(name, size);
        }

        /**
         * Sets the specified table as being active.
         *
         * @param name the table name
         */
        public void touch(@Nonnull final String name) {
            this.active.getIfPresent(name);
        }

        @Override
        public int weigh(@Nonnull final String key, @Nonnull final Integer value) {
            return value;
        }
    }
}
