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

import com.thinkbiganalytics.policy.rest.model.FieldPolicy;
import com.thinkbiganalytics.spark.SparkContextService;
import com.thinkbiganalytics.spark.dataprofiler.Profiler;
import com.thinkbiganalytics.spark.datavalidator.DataValidator;
import com.thinkbiganalytics.spark.metadata.TransformJob;
import com.thinkbiganalytics.spark.metadata.TransformScript;
import com.thinkbiganalytics.spark.repl.SparkScriptEngine;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.shell.DatasourceProvider;
import com.thinkbiganalytics.spark.shell.DatasourceProviderFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptException;

import scala.Option;
import scala.tools.nsc.interpreter.NamedParam;
import scala.tools.nsc.interpreter.NamedParamClass;

/**
 * A scheduled service that manages cached results of Spark jobs.
 */
@Component
public class TransformService {

    private static final Logger log = LoggerFactory.getLogger(TransformService.class);

    /**
     * Data source provider factory
     */
    @Nullable
    private DatasourceProviderFactory datasourceProviderFactory;

    /**
     * Script execution engine
     */
    @Nonnull
    private final SparkScriptEngine engine;

    /**
     * Profiler for column statistics.
     */
    @Nullable
    private Profiler profiler;

    /**
     * Provides access to the Spark context
     */
    @Nonnull
    private final SparkContextService sparkContextService;

    /**
     * Job tracker for transformations
     */
    @Nonnull
    private final TransformJobTracker tracker;

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
     */
    public TransformService(@Nonnull final Class<? extends TransformScript> transformScriptClass, @Nonnull final SparkScriptEngine engine, @Nonnull final SparkContextService sparkContextService,
                            @Nonnull final TransformJobTracker tracker) {
        this.transformScriptClass = transformScriptClass;
        this.engine = engine;
        this.sparkContextService = sparkContextService;
        this.tracker = tracker;
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

        // Generate destination
        final String table = newTableName();

        // Build bindings list
        final List<NamedParam> bindings = new ArrayList<>();
        bindings.add(new NamedParamClass("policies", "Array[" + FieldPolicy.class.getName() + "]", getPolicies(request)));
        bindings.add(new NamedParamClass("profiler", Profiler.class.getName(), profiler));
        bindings.add(new NamedParamClass("sparkContextService", SparkContextService.class.getName(), sparkContextService));
        bindings.add(new NamedParamClass("tableName", "String", table));
        bindings.add(new NamedParamClass("validator", DataValidator.class.getName(), validator));

        if (request.getDatasources() != null && !request.getDatasources().isEmpty()) {
            if (datasourceProviderFactory != null) {
                final DatasourceProvider datasourceProvider = datasourceProviderFactory.getDatasourceProvider(request.getDatasources());
                bindings.add(new NamedParamClass("datasourceProvider", DatasourceProvider.class.getName() + "[org.apache.spark.sql.DataFrame]", datasourceProvider));
            } else {
                final ScriptException e = new ScriptException("Script cannot be executed because no data source provider factory is available.");
                log.error("Throwing {}", e);
                throw e;
            }
        }

        // Execute script
        final Object result = this.engine.eval(toScript(request), bindings);

        final TransformJob job;
        if (result instanceof Callable) {
            @SuppressWarnings("unchecked") final Callable<TransformResponse> callable = (Callable) result;
            job = new TransformJob(table, callable, engine.getSparkContext());
            tracker.submitJob(job);
        } else {
            final IllegalStateException e = new IllegalStateException("Unexpected script result type: " + (result != null ? result.getClass() : null));
            log.error("Throwing {}", e);
            throw e;
        }

        // Build response
        TransformResponse response;

        try {
            response = job.get(500, TimeUnit.MILLISECONDS);
            tracker.removeJob(table);
        } catch (final ExecutionException cause) {
            final ScriptException e = new ScriptException(cause);
            log.error("Throwing {}", e);
            throw e;
        } catch (final InterruptedException | TimeoutException e) {
            log.trace("Timeout waiting for script result", e);
            if (result instanceof TransformResponse) {
                response = (TransformResponse) result;
            } else {
                response = new TransformResponse();
                response.setProgress(0.0);
                response.setStatus(TransformResponse.Status.PENDING);
                response.setTable(table);
            }
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
        final Option<TransformJob> job = tracker.getJob(id);
        if (job.isDefined()) {
            if (job.get().isDone()) {
                tracker.removeJob(id);
            }
            return job.get();
        } else {
            throw new IllegalArgumentException();
        }
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
     * Gets the data validator for cleansing rows.
     */
    @Nullable
    public DataValidator getValidator() {
        return validator;
    }

    /**
     * Sets the data validator for cleansing rows.
     */
    public void setValidator(@Nullable DataValidator validator) {
        this.validator = validator;
    }

    /**
     * Converts the specified transformation request to a Scala script that can be executed by the script engine.
     *
     * @param request the transformation request
     * @return the Scala script
     */
    @Nonnull
    String toScript(@Nonnull final TransformRequest request) {
        final StringBuilder script = new StringBuilder();
        script.append("class Transform (destination: String, policies: Array[com.thinkbiganalytics.policy.rest.model.FieldPolicy], validator: com.thinkbiganalytics.spark.datavalidator.DataValidator,"
                      + " profiler: com.thinkbiganalytics.spark.dataprofiler.Profiler, sqlContext: org.apache.spark.sql.SQLContext,"
                      + " sparkContextService: com.thinkbiganalytics.spark.SparkContextService) extends ");
        script.append(transformScriptClass.getName());
        script.append("(destination, policies, validator, profiler, sqlContext, sparkContextService) {\n");

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
        script.append("new Transform(tableName, policies, validator, profiler, sqlContext, sparkContextService).run()\n");

        return script.toString();
    }

    /**
     * Gets the field policies from the specified transform request.
     */
    @Nullable
    private FieldPolicy[] getPolicies(@Nonnull final TransformRequest request) {
        return (request.getPolicies() != null) ? request.getPolicies().toArray(new FieldPolicy[request.getPolicies().size()]) : null;
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
}
