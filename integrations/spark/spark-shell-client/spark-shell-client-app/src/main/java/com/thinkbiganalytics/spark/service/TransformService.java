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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.thinkbiganalytics.discovery.schema.QueryResultColumn;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClient;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogClientBuilder;
import com.thinkbiganalytics.kylo.catalog.api.KyloCatalogReader;
import com.thinkbiganalytics.kylo.catalog.rest.model.DataSource;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobRequest;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobResponse;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobResult;
import com.thinkbiganalytics.policy.rest.model.FieldPolicy;
import com.thinkbiganalytics.spark.DataSet;
import com.thinkbiganalytics.spark.SparkContextService;
import com.thinkbiganalytics.spark.dataprofiler.Profiler;
import com.thinkbiganalytics.spark.datavalidator.DataValidator;
import com.thinkbiganalytics.spark.metadata.Job;
import com.thinkbiganalytics.spark.metadata.ProfileStage;
import com.thinkbiganalytics.spark.metadata.QueryResultRowTransform;
import com.thinkbiganalytics.spark.metadata.ResponseStage;
import com.thinkbiganalytics.spark.metadata.SaveDataSetStage;
import com.thinkbiganalytics.spark.metadata.SaveJob;
import com.thinkbiganalytics.spark.metadata.SaveSqlStage;
import com.thinkbiganalytics.spark.metadata.ShellTransformStage;
import com.thinkbiganalytics.spark.metadata.SparkJob;
import com.thinkbiganalytics.spark.metadata.SparkJobResultSupplier;
import com.thinkbiganalytics.spark.metadata.SqlTransformStage;
import com.thinkbiganalytics.spark.metadata.TransformJob;
import com.thinkbiganalytics.spark.metadata.TransformScript;
import com.thinkbiganalytics.spark.metadata.ValidationStage;
import com.thinkbiganalytics.spark.model.SaveResult;
import com.thinkbiganalytics.spark.model.TransformResult;
import com.thinkbiganalytics.spark.repl.SparkScriptEngine;
import com.thinkbiganalytics.spark.rest.model.Datasource;
import com.thinkbiganalytics.spark.rest.model.JdbcDatasource;
import com.thinkbiganalytics.spark.rest.model.KyloCatalogReadRequest;
import com.thinkbiganalytics.spark.rest.model.PageSpec;
import com.thinkbiganalytics.spark.rest.model.SaveRequest;
import com.thinkbiganalytics.spark.rest.model.SaveResponse;
import com.thinkbiganalytics.spark.rest.model.TransformQueryResult;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.shell.CatalogDataSetProvider;
import com.thinkbiganalytics.spark.shell.CatalogDataSetProviderFactory;
import com.thinkbiganalytics.spark.shell.DatasourceProvider;
import com.thinkbiganalytics.spark.shell.DatasourceProviderFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hive.ql.session.SessionState;
import org.apache.spark.sql.types.StructType;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptException;

import scala.tools.nsc.interpreter.NamedParam;
import scala.tools.nsc.interpreter.NamedParamClass;

/**
 * A scheduled service that manages cached results of Spark jobs.
 */
// @Component  :: Removing @Component here because TransformService is built by SparkShellApp or initSession of SparkLivy
public class TransformService {

    private static final XLogger log = XLoggerFactory.getXLogger(TransformService.class);

    /**
     * Data set converter service
     */
    @Nonnull
    private final DataSetConverterService converterService;

    /**
     * Data source provider factory
     */
    @Nullable
    private DatasourceProviderFactory datasourceProviderFactory;


    @Nullable
    private CatalogDataSetProviderFactory catalogDataSetProviderFactory;

    /**
     * Script execution engine
     */
    @Nonnull
    private final SparkScriptEngine engine;

    /**
     * Hadoop file system
     */
    @Nullable
    private FileSystem hadoopFileSystem;

    /**
     * Profiler for column statistics.
     */
    @Nullable
    private Profiler profiler;

    /**
     * Hive SessionState for adding to other threads.
     */
    @Nullable
    private final SessionState sessionState = SessionState.get();

    /**
     * Provides access to the Spark context
     */
    @Nonnull
    private final SparkContextService sparkContextService;

    /**
     * Job tracker for transformations
     */
    @Nonnull
    private final JobTrackerService tracker;


    /**
     * Builder for the kylo client reader
     */
    @Nonnull
    private final KyloCatalogClientBuilder kyloCatalogClientBuilder;

    /**
     * Cache of transformations
     */
    @Nonnull
    private final Cache<String, TransformRequest> transformCache = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .maximumSize(100)
        .build();

    /**
     * Class for the transform script.
     */
    @Nonnull
    private final Class<? extends TransformScript> transformScriptClass;

    /**
     * Runs standardizers and validators on rows.
     */
    @Nullable
    private DataValidator validator;

    /**
     * Constructs a {@code TransformService} using the specified engine to execute scripts.
     *
     * @param transformScriptClass the parent class for Scala transform scripts
     * @param engine               the script engine
     * @param sparkContextService  the Spark context service
     * @param tracker              job tracker for transformations
     * @param converterService     data set converter service
     */
    public TransformService(@Nonnull final Class<? extends TransformScript> transformScriptClass, @Nonnull final SparkScriptEngine engine,
                            @Nonnull final SparkContextService sparkContextService, @Nonnull final JobTrackerService tracker, @Nonnull final DataSetConverterService converterService,
                            @Nonnull KyloCatalogClientBuilder kyloCatalogClientBuilder) {
        this.transformScriptClass = transformScriptClass;
        this.engine = engine;
        this.sparkContextService = sparkContextService;
        this.tracker = tracker;
        this.converterService = converterService;
        this.kyloCatalogClientBuilder = kyloCatalogClientBuilder;
    }

    /**
     * Gets the data source provider factory.
     *
     * @return the data source provider factory
     */
    @Nullable
    @SuppressWarnings("unused")
    public DatasourceProviderFactory getDatasourceProviderFactory() {
        return datasourceProviderFactory;
    }

    /**
     * Sets the data source provider factory.
     *
     * @param datasourceProviderFactory the data source provider factory
     */
    public void setDatasourceProviderFactory(@Nullable final DatasourceProviderFactory datasourceProviderFactory) {
        this.datasourceProviderFactory = datasourceProviderFactory;
    }


    @Nullable
    public CatalogDataSetProviderFactory getCatalogDataSetProviderFactory() {
        return catalogDataSetProviderFactory;
    }

    public void setCatalogDataSetProviderFactory(@Nullable CatalogDataSetProviderFactory catalogDataSetProviderFactory) {
        this.catalogDataSetProviderFactory = catalogDataSetProviderFactory;
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
        log.entry(request);

        // Handle async request
        if (request.isAsync()) {
            return cacheTransform(request);
        }

        // Execute script
        final DataSet dataSet = createShellTask(request);
        TransformResponse response = submitTransformJob(new ShellTransformStage(dataSet, converterService), request);
        updateTransformResponse(response, dataSet);
        return log.exit(response);
    }

    private void updateTransformResponse(TransformResponse response, DataSet dataSet) {
        final StructType schema = dataSet.schema();

        // Build response
        if (response.getStatus() != TransformResponse.Status.SUCCESS) {
            final String table = response.getTable();
            final TransformQueryResult partialResult = new TransformQueryResult();
            partialResult.setColumns(Arrays.<QueryResultColumn>asList(new QueryResultRowTransform(schema, table, converterService).columns()));

            response = new TransformResponse();
            response.setProgress(0.0);
            response.setResults(partialResult);
            response.setStatus(TransformResponse.Status.PENDING);
            response.setTable(table);
        }
    }

    /**
     * Gets the Hadoop file system.
     */
    @Nullable
    public FileSystem getFileSystem() {
        return hadoopFileSystem;
    }

    /**
     * Sets the Hadoop file system.
     */
    public void setFileSystem(@Nonnull final FileSystem hadoopFileSystem) {
        this.hadoopFileSystem = hadoopFileSystem;
    }

    /**
     * Gets the result of a save request.
     *
     * @param id     identifier for the save
     * @param remove {@code true} to remove the results from cache, or {@code false} to leave them in cache
     */
    @Nonnull
    public SaveJob getSaveJob(@Nonnull final String id, final boolean remove) {
        return getJob(tracker.getSaveJob(id), remove);
    }

    /**
     * Gets the result of a Spark job.
     */
    @Nonnull
    public SparkJob getSparkJob(@Nonnull final String id) {
        return getJob(tracker.getSparkJob(id), true);
    }

    /**
     * Gets the transformation job with the specified id.
     *
     * @param id the table with the results
     * @return the transformation job
     * @throws IllegalArgumentException if a job with the id does not exist
     */
    @Nonnull
    public TransformJob getTransformJob(@Nonnull final String id) {
        return getJob(tracker.getTransformJob(id), true);
    }

    /**
     * Gets the profiler for column statistics.
     *
     * @return the profiler
     */
    @Nullable
    @SuppressWarnings("unused")
    public Profiler getProfiler() {
        return profiler;
    }

    /**
     * Sets the profiler for column statistics.
     *
     * @param profiler the profiler
     */
    public void setProfiler(@Nullable final Profiler profiler) {
        this.profiler = profiler;
    }

    /**
     * Executes the specified SQL query and returns the result.
     *
     * @param request the transformation request
     * @return the transformation results
     * @throws IllegalStateException if this service is not running
     * @throws ScriptException       if the script cannot be executed
     */
    @Nonnull
    public TransformResponse query(@Nonnull final TransformRequest request) throws ScriptException {
        log.entry(request);

        // Handle async request
        if (request.isAsync()) {
            return cacheTransform(request);
        }

        // Execute query
        final TransformResponse response = submitTransformJob(createSqlTask(request), request);
        return log.exit(response);
    }

    /**
     * Executes and saves a Spark shell request.
     */
    @Nonnull
    public SaveResponse saveShell(@Nonnull final String id, @Nonnull final SaveRequest save) throws ScriptException {
        log.entry(id, save);

        final DataSet dataSet = createShellTask(getTransformRequest(id));
        final SaveResponse response = submitSaveJob(createSaveTask(save, new ShellTransformStage(dataSet, converterService)));
        return log.exit(response);
    }

    /**
     * Executes and saves a Spark SQL request.
     */
    @Nonnull
    public SaveResponse saveSql(@Nonnull final String id, @Nonnull final SaveRequest save) {
        log.entry(id, save);

        // Create task
        final Supplier<SaveResult> task;
        final TransformRequest transform = getTransformRequest(id);
        final JdbcDatasource transformDatasource = (transform.getDatasources() != null && transform.getDatasources().size() == 1 && transform.getDatasources().get(0) instanceof JdbcDatasource)
                                                   ? (JdbcDatasource) transform.getDatasources().get(0)
                                                   : null;
        if (transformDatasource != null && save.getJdbc() != null && Objects.equal(transformDatasource.getId(), save.getJdbc().getId())) {
            Preconditions.checkArgument(save.getTableName() != null, "Missing target table name.");
            task = new SaveSqlStage(save.getTableName(), transform.getScript(), save.getJdbc());
        } else {
            task = createSaveTask(save, createSqlTask(transform));
        }

        // Submit job
        final SaveResponse response = submitSaveJob(task);
        return log.exit(response);
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
    public SparkJobResponse submit(@Nonnull final SparkJobRequest request) throws ScriptException {
        log.entry(request);

        final Supplier<SparkJobResult> jobTask = createJobTask(request);
        final SparkJobResponse response = submitSparkJob(jobTask);
        return log.exit(response);
    }

    /**
     * Gets the data validator for cleansing rows.
     */
    @Nullable
    public DataValidator getValidator() {
        return validator;
    }

    /**
     * Sets the data validator for cleansing rows.
     */
    public void setValidator(@Nullable final DataValidator validator) {
        this.validator = validator;
    }

    /**
     * Converts the specified transformation request to a Scala script that can be executed by the script engine.
     *
     * @param request the transformation request
     * @return the Scala script
     */
    @Nonnull
    @VisibleForTesting
    String toTransformScript(@Nonnull final TransformRequest request) {
        final StringBuilder script = new StringBuilder();
        script.append("class Transform (sqlContext: org.apache.spark.sql.SQLContext, sparkContextService: com.thinkbiganalytics.spark.SparkContextService) extends ");
        script.append(transformScriptClass.getName());
        script.append("(sqlContext, sparkContextService) {\n");

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
        }

        script.append("}\n");
        script.append("new Transform(sqlContext, sparkContextService).run()\n");
        return script.toString();
    }


    public TransformResponse kyloReaderResponse(KyloCatalogReadRequest request) throws ScriptException {
        // Change class loader
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Ensure SessionState is valid
            if (SessionState.get() == null && sessionState != null) {
                SessionState.setCurrentSessionState(sessionState);
            }

            // Change class loader (after SessionState)
            Thread.currentThread().setContextClassLoader(engine.getClassLoader());

            // Build DataFrame
            KyloCatalogClient<?> client = kyloCatalogClientBuilder.build();
            KyloCatalogReader<?> reader = client.read().options(request.getOptions()).addJars(request.getJars()).addFiles(request.getFiles()).format(request.getFormat());
            final Object dataFrame;
            if (!request.getPaths().isEmpty()) {
                if (request.getPaths().size() > 1) {
                    dataFrame = reader.load(request.getPaths().toArray(new String[0]));
                } else {
                    dataFrame = reader.load(request.getPaths().get(0));
                }
            } else {
                dataFrame = reader.load();
            }

            // Read DataFrame
            final DataSet dataSet = sparkContextService.toDataSet(dataFrame);

            TransformResponse response = submitTransformJob(new ShellTransformStage(dataSet, converterService), request.getPageSpec());

            updateTransformResponse(response, dataSet);
            return log.exit(response);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    /**
     * Caches the specified transformation.
     */
    @Nonnull
    private TransformResponse cacheTransform(@Nonnull final TransformRequest request) {
        final String id = newTableName();
        transformCache.put(id, request);

        final TransformResponse response = new TransformResponse();
        response.setStatus(TransformResponse.Status.SUCCESS);
        response.setTable(id);
        return response;
    }

    /**
     * Creates a new Spark job.
     */
    @Nonnull
    @SuppressWarnings("RedundantThrows")
    private Supplier<SparkJobResult> createJobTask(@Nonnull final SparkJobRequest request) throws ScriptException {
        log.entry(request);

        // Build bindings list
        final List<NamedParam> bindings = new ArrayList<>();
        bindings.add(new NamedParamClass("sparkContextService", SparkContextService.class.getName(), sparkContextService));

        // Return task
        return new SparkJobResultSupplier(engine, request.getScript(), bindings);
    }

    /**
     * Creates a new save task.
     */
    @Nonnull
    public Supplier<SaveResult> createSaveTask(@Nonnull final SaveRequest request, @Nonnull final Supplier<TransformResult> transform) {
        Preconditions.checkState(hadoopFileSystem != null, "Saving is not enabled.");
        return Suppliers.compose(new SaveDataSetStage(request, hadoopFileSystem, converterService), transform);
    }

    /**
     * Creates a new Spark shell transformation.
     */
    @Nonnull
    private DataSet createShellTask(@Nonnull final TransformRequest request) throws ScriptException {
        log.entry(request);

        // Build bindings list
        final List<NamedParam> bindings = new ArrayList<>();
        bindings.add(new NamedParamClass("sparkContextService", SparkContextService.class.getName(), sparkContextService));

        if ((request.getDatasources() != null && !request.getDatasources().isEmpty()) || (request.getCatalogDataSources() != null && !request.getCatalogDataSources().isEmpty())) {
            if (datasourceProviderFactory != null) {
                List<Datasource> legacyDataSources = request.getDatasources() != null ? request.getDatasources() : new ArrayList<Datasource>();
                List<DataSource> catalogDataSources = request.getCatalogDataSources() != null ? request.getCatalogDataSources() : new ArrayList<DataSource>();

                final DatasourceProvider datasourceProvider = datasourceProviderFactory.getDatasourceProvider(legacyDataSources,catalogDataSources);
                bindings.add(new NamedParamClass("datasourceProvider", DatasourceProvider.class.getName() + "[org.apache.spark.sql.DataFrame]", datasourceProvider));
            } else {
                throw log.throwing(new ScriptException("Script cannot be executed because no data source provider factory is available."));
            }
        }

        if (request.getCatalogDatasets() != null && !request.getCatalogDatasets().isEmpty()) {

            if (catalogDataSetProviderFactory != null) {
                log.info("Creating new Shell task with {} data sets ", request.getCatalogDatasets().size());
                final CatalogDataSetProvider catalogDataSetProvider = catalogDataSetProviderFactory.getDataSetProvider(request.getCatalogDatasets());
                bindings.add(new NamedParamClass("catalogDataSetProvider", CatalogDataSetProvider.class.getName() + "[org.apache.spark.sql.DataFrame]", catalogDataSetProvider));
            } else {
                throw log.throwing(new ScriptException("Script cannot be executed because no data source provider factory is available."));
            }
        }

        // Ensure SessionState is valid
        if (SessionState.get() == null && sessionState != null) {
            SessionState.setCurrentSessionState(sessionState);
        }

        // Execute script
        final Object result;
        try {
            result = this.engine.eval(toTransformScript(request), bindings);
        } catch (final Exception cause) {
            throw log.throwing(new ScriptException(cause));
        }

        if (result instanceof DataSet) {
            return log.exit((DataSet) result);
        } else {
            throw log.throwing(new IllegalStateException("Unexpected script result type: " + (result != null ? result.getClass() : null)));
        }
    }

    /**
     * Creates a new Spark SQL transformation.
     */
    @Nonnull
    private Supplier<TransformResult> createSqlTask(@Nonnull final TransformRequest request) {
        log.entry(request);

        // Parse data source parameters
        if (request.getDatasources() == null || request.getDatasources().size() != 1 || !(request.getDatasources().get(0) instanceof JdbcDatasource)) {
            throw new IllegalArgumentException("Exactly one JDBC datasource is required.");
        }

        // Create task
        final Supplier<TransformResult> task = new SqlTransformStage(request.getScript(), (JdbcDatasource) request.getDatasources().get(0), engine.getSQLContext(), sparkContextService);
        return log.exit(task);
    }

    /**
     * Gets the job if available, and optionally removes it from the job tracker.
     */
    @Nonnull
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public <T extends Job> T getJob(@Nonnull final Optional<T> job, final boolean remove) {
        if (job.isPresent()) {
            if (job.get().isDone() && remove) {
                tracker.removeJob(job.get().getGroupId());
            }
            return job.get();
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Gets the cached transformation.
     */
    @Nonnull
    private TransformRequest getTransformRequest(@Nonnull final String id) {
        final TransformRequest request = transformCache.getIfPresent(id);
        if (request != null) {
            return request;
        } else {
            throw new IllegalArgumentException("Transformation is not available: " + id);
        }
    }

    /**
     * Gets the field policies from the specified transform request.
     */
    @Nullable
    private FieldPolicy[] getPolicies(@Nonnull final TransformRequest request) {
        return (request.getPolicies() != null) ? request.getPolicies().toArray(new FieldPolicy[0]) : null;
    }

    /**
     * Generates a new, unique table name.
     *
     * @return the table name
     * @throws IllegalStateException if a table name cannot be generated
     */
    private String newTableName() {
        for (int i = 0; i < 100; ++i) {
            final String name = UUID.randomUUID().toString();
            if (name.matches("^[a-fA-F].*")) {
                return name.replace("-", "");
            }
        }
        throw new IllegalStateException("Unable to generate a new table name");
    }

    /**
     * Submits the specified task for saving a transformation and returns the result.
     */
    @Nonnull
    public SaveResponse submitSaveJob(@Nonnull final Supplier<SaveResult> task) {
        log.entry(task);

        // Execute script
        final String table = newTableName();
        final SaveJob job = new SaveJob(table, task, engine.getSparkContext());
        tracker.submitJob(job);

        // Build response
        final SaveResponse response = new SaveResponse();
        response.setId(table);
        response.setProgress(0.0);
        response.setStatus(SaveResponse.Status.PENDING);
        return log.exit(response);
    }

    /**
     * Submits the specified job to be executed.
     */
    @Nonnull
    public SparkJobResponse submitSparkJob(@Nonnull final Supplier<SparkJobResult> task) {
        log.entry(task);

        // Execute script
        final String id = newTableName();
        final SparkJob job = new SparkJob(id, task, engine.getSparkContext());
        tracker.submitJob(job);

        // Build response
        final SparkJobResponse response = new SparkJobResponse();
        response.setId(id);
        response.setStatus(SparkJobResponse.Status.PENDING);
        return log.exit(response);
    }

    /**
     * Submits the specified task to be executed and returns the result.
     */
    @Nonnull
    private TransformResponse submitTransformJob(final Supplier<TransformResult> task, @Nonnull final TransformRequest request) throws ScriptException {

        final FieldPolicy[] policies = getPolicies(request);
        final PageSpec pageSpec = request.getPageSpec();

        log.entry(task, policies);

        // Prepare script
        Supplier<TransformResult> result = task;

        if (request.isDoValidate() && policies != null && policies.length > 0 && validator != null) {
            result = Suppliers.compose(new ValidationStage(policies, validator), result);
        }

        if (request.isDoProfile() && profiler != null) {
            result = Suppliers.compose(new ProfileStage(profiler), result);
        }

        return submitTransformJob(result, pageSpec);
    }


    /**
     * Submits the specified task to be executed and returns the result.
     */
    @Nonnull
    private TransformResponse submitTransformJob(final Supplier<TransformResult> result, @Nonnull final PageSpec pageSpec) throws ScriptException {

        // Execute script
        final String table = newTableName();
        final TransformJob job = new TransformJob(table, Suppliers.compose(new ResponseStage(table, converterService, pageSpec), result), engine.getSparkContext());
        tracker.submitJob(job);

        // Build response
        TransformResponse response;

        try {
            response = job.get(500, TimeUnit.MILLISECONDS);
            tracker.removeJob(table);
        } catch (final ExecutionException cause) {
            throw log.throwing(new ScriptException(cause));
        } catch (final InterruptedException | TimeoutException e) {
            log.trace("Timeout waiting for script result", e);
            response = new TransformResponse();
            response.setProgress(0.0);
            response.setStatus(TransformResponse.Status.PENDING);
            response.setTable(table);
        }

        return log.exit(response);
    }
}
