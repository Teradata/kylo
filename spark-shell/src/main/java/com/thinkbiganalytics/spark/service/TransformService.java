package com.thinkbiganalytics.spark.service;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.script.ScriptException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.cache.Weigher;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.thinkbiganalytics.db.model.query.QueryResult;
import com.thinkbiganalytics.spark.metadata.TransformRequest;
import com.thinkbiganalytics.spark.metadata.TransformResponse;
import com.thinkbiganalytics.spark.repl.ScriptEngine;
import com.thinkbiganalytics.spark.util.HiveUtils;

import scala.tools.nsc.interpreter.NamedParam;
import scala.tools.nsc.interpreter.NamedParamClass;

/**
 * A scheduled service that manages a database containing cached results of Spark jobs.
 *
 * <p>Before calling {@link #stopAsync()}, all Spark should should be cancelled to ensure that no additional tables will be
 * created.</p>
 */
public class TransformService extends AbstractScheduledService {

    private static final Logger log = LoggerFactory.getLogger(TransformService.class);

    /** Name of the database */
    private static final String DATABASE = "spark_shell_temp";

    /** Time in seconds after last use for a table to expire */
    private static final long EXPIRE_TIME = 3600L;

    /** Minimum table size in bytes, to ensure there aren't too many tables */
    private static final int MIN_BYTES = 1048576;

    /** Maximum database size in bytes (soft limit) */
    private static final long MAX_BYTES = 10737418240L;

    /** Tables with cached results */
    @Nonnull
    private final TableCache cache = new TableCache();

    /** Script execution engine */
    @Nonnull
    private final ScriptEngine engine;

    /**
     * Constructs a {@code TransformService} using the specified engine to execute scripts.
     *
     * @param engine the script engine
     */
    public TransformService(@Nonnull final ScriptEngine engine) {
        this.engine = engine;
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
    public TransformResponse execute(@Nonnull final TransformRequest request)
            throws ScriptException {
        log.trace("entry params({})", request);

        // Verify state
        if (!isRunning()) {
            IllegalStateException e = new IllegalStateException("Transform service has not been started");
            log.error("Throwing {}", e);
            throw e;
        }

        // Generate destination
        String table = UUID.randomUUID().toString().replace("-", "");
        this.cache.put(table, MIN_BYTES);

        // Execute script
        List<NamedParam> bindings = ImmutableList.of((NamedParam) new NamedParamClass("database", "String", DATABASE),
                new NamedParamClass("tableName", "String", table));

        Object results = this.engine.eval(toScript(request), bindings);

        // Update table weight
        try {
            updateWeight(table, this.engine.getSQLContext());
        } catch (Exception e) {
            log.warn("Failed to update table weight: {}", e.toString());
        }

        // Build response
        TransformResponse response = new TransformResponse();
        response.setStatus(TransformResponse.Status.SUCCESS);
        response.setTable(table);

        if (request.isSendResults()) {
            response.setResults((QueryResult) results);
        }

        log.trace("exit with({})", response);
        return response;
    }

    /**
     * Gets the script engine used by this service.
     *
     * @return the script engine
     */
    public ScriptEngine getEngine() {
        return this.engine;
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
        SQLContext context = this.engine.getSQLContext();
        context.sql("CREATE DATABASE IF NOT EXISTS " + HiveUtils.quoteIdentifier(DATABASE));

        // Drop existing tables
        List<Row> tables = context.sql("SHOW TABLES IN " + HiveUtils.quoteIdentifier(DATABASE))
                .collectAsList();

        for (Row table : tables) {
            dropTable(table.getString(0), context);
        }

        log.trace("exit");
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
        script.append("new Transform(tableName, ");
        script.append(request.isSendResults());
        script.append(", sqlContext).run()\n");

        return script.toString();
    }

    /**
     * Drops the table with the specified name using the specified context.
     *
     * @param name    the table name
     * @param context the SQL context
     */
    private void dropTable(@Nonnull final String name, @Nonnull final SQLContext context) {
        String identifier = HiveUtils.quoteIdentifier(DATABASE, name);
        log.debug("Dropping Hive table {}", identifier);
        context.sql("DROP TABLE IF EXISTS " + identifier);
    }

    /**
     * Updates the weight of the specified table in the cache using the current SQL context.
     *
     * @param table   the table name
     * @param context the SQL context
     */
    private void updateWeight(@Nonnull final String table, @Nonnull final SQLContext context) {
        // Select service database
        context.sql("USE " + HiveUtils.quoteIdentifier(DATABASE));

        // Find size of table
        String identifier = HiveUtils.quoteIdentifier(table);
        List<Row> properties = this.engine.getSQLContext().sql("SHOW TBLPROPERTIES " + identifier).collectAsList();
        int size = -1;

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

        // Update cache
        this.cache.put(table, Math.max(size, MIN_BYTES));
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
