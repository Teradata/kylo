package com.thinkbiganalytics.kylo.spark.livy;

/*-
 * #%L
 * kylo-spark-livy-server
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


import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.thinkbiganalytics.kylo.spark.SparkException;
import com.thinkbiganalytics.kylo.spark.client.LivyClient;
import com.thinkbiganalytics.kylo.spark.client.model.LivyServer;
import com.thinkbiganalytics.kylo.spark.config.LivyProperties;
import com.thinkbiganalytics.kylo.spark.exceptions.LivyUserException;
import com.thinkbiganalytics.kylo.spark.model.Statement;
import com.thinkbiganalytics.kylo.spark.model.StatementsPost;
import com.thinkbiganalytics.kylo.spark.model.enums.StatementState;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobRequest;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobResponse;
import com.thinkbiganalytics.kylo.utils.LivyRestModelTransformer;
import com.thinkbiganalytics.kylo.utils.ScalaScriptService;
import com.thinkbiganalytics.kylo.utils.ScalaScriptUtils;
import com.thinkbiganalytics.kylo.utils.ScriptGenerator;
import com.thinkbiganalytics.kylo.utils.StatementStateTranslator;
import com.thinkbiganalytics.rest.JerseyRestClient;
import com.thinkbiganalytics.spark.io.ZipStreamingOutput;
import com.thinkbiganalytics.spark.model.SaveResult;
import com.thinkbiganalytics.spark.rest.model.DataSources;
import com.thinkbiganalytics.spark.rest.model.KyloCatalogReadRequest;
import com.thinkbiganalytics.spark.rest.model.SaveRequest;
import com.thinkbiganalytics.spark.rest.model.SaveResponse;
import com.thinkbiganalytics.spark.rest.model.ServerStatusResponse;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;

import org.apache.commons.lang3.Validate;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class SparkLivyRestClient implements SparkShellRestClient {

    private static final XLogger logger = XLoggerFactory.getXLogger(SparkLivyRestClient.class);

    @Resource
    private SparkLivyProcessManager sparkLivyProcessManager;

    @Resource
    private ScriptGenerator scriptGenerator;

    @Resource
    private ScalaScriptService scalaScriptService;

    @Resource
    private LivyClient livyClient;

    @Resource
    private LivyServer livyServer;

    @Resource
    private LivyProperties livyProperties;

    /**
     * Default file system
     */
    @Resource
    public FileSystem fileSystem;

    /**
     * Cache of transformIds
     */
    @Nonnull
    final static Cache<TransformRequest, String> transformCache = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .maximumSize(100)
        .build();

    /**
     * Cache of transformId => saveid
     */
    @Nonnull
    final static Cache<String, Integer> transformIdsToLivyId = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .maximumSize(100)
        .build();

    @Override
    public SparkJobResponse createJob(@Nonnull final SparkShellProcess process, @Nonnull final SparkJobRequest request) {
        logger.entry(process, request);

        // Execute job script
        final JerseyRestClient client = sparkLivyProcessManager.getClient(process);
        final String script = scalaScriptService.wrapScriptForLivy(request);
        final Statement statement = submitCode(client, script, process);

        final String jobId = ScalaScriptService.newTableName();
        sparkLivyProcessManager.setStatementId(jobId, statement.getId());

        // Generate response
        final SparkJobResponse response = new SparkJobResponse();
        response.setId(jobId);
        response.setStatus(StatementStateTranslator.translate(statement.getState()));
        return logger.exit(response);
    }

    @Nonnull
    @Override
    public Optional<Response> downloadQuery(@Nonnull SparkShellProcess process, @Nonnull String queryId, @Nonnull String saveId) {
        throw new UnsupportedOperationException();
    }


    @Nonnull
    @Override
    public Optional<TransformResponse> getQueryResult(@Nonnull SparkShellProcess process, @Nonnull String id) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Optional<SparkJobResponse> getJobResult(@Nonnull final SparkShellProcess process, @Nonnull final String id) {
        logger.entry(process, id);
        Validate.isInstanceOf(SparkLivyProcess.class, process, "SparkLivyRestClient.getJobResult called on non Livy Process");
        SparkLivyProcess sparkLivyProcess = (SparkLivyProcess) process;

        // Request result from Livy
        final JerseyRestClient client = sparkLivyProcessManager.getClient(process);
        final Integer statementId = sparkLivyProcessManager.getStatementId(id);

        final Statement statement = livyClient.getStatement(client, sparkLivyProcess, statementId);
        sparkLivyProcessManager.setStatementId(id, statement.getId());

        // Generate response
        final SparkJobResponse response = LivyRestModelTransformer.toJobResponse(id, statement);

        if (response.getStatus() != TransformResponse.Status.ERROR) {
            return logger.exit(Optional.of(response));
        } else {
            throw logger.throwing(new SparkException(String.format("Unexpected error found in transform response:\n%s",
                                                                   response.getMessage())));
        }
    }

    @Nonnull
    @Override
    public Optional<SaveResponse> getQuerySave(@Nonnull SparkShellProcess process, @Nonnull String
        queryId, @Nonnull String saveId) {
        throw new UnsupportedOperationException();
    }


    @Nonnull
    @Override
    public TransformResponse query(@Nonnull SparkShellProcess process, @Nonnull TransformRequest request) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public SaveResponse saveQuery(@Nonnull SparkShellProcess process, @Nonnull String id, @Nonnull SaveRequest
        request) {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Optional<Response> downloadTransform(@Nonnull SparkShellProcess process, @Nonnull String transformId, @Nonnull String saveId) {
        // 1. get "SaveResult" serialized from Livy
        logger.entry(process, transformId, saveId);

        JerseyRestClient client = sparkLivyProcessManager.getClient(process);

        String script = scriptGenerator.script("getSaveResult", ScalaScriptUtils.scalaStr(saveId));

        Statement statement = submitCode(client, script, process);

        if (statement.getState() == StatementState.running
            || statement.getState() == StatementState.waiting) {
            statement = pollStatement(client, process, statement.getId());
        } else {
            throw logger.throwing(new LivyUserException("livy.unexpected_error"));
        }

        URI uri = LivyRestModelTransformer.toUri(statement);
        SaveResult result = new SaveResult(new Path(uri));

        // 2. Create a response with data from filesysem
        if (result.getPath() != null) {
            Optional<Response> response =
                Optional.of(
                    Response.ok(new ZipStreamingOutput(result.getPath(), fileSystem))
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + saveId + ".zip\"")
                        .build());
            return logger.exit(response);
        } else {
            return logger.exit(Optional.of(
                createErrorResponse(Response.Status.NOT_FOUND, "download.notFound"))
            );
        }

    }


    @Nonnull
    @Override
    public DataSources getDataSources(@Nonnull SparkShellProcess process) {
        logger.entry(process);

        JerseyRestClient client = sparkLivyProcessManager.getClient(process);

        String script = scriptGenerator.script("getDataSources");

        Statement statement = submitCode(client, script, process);

        if (statement.getState() == StatementState.running
            || statement.getState() == StatementState.waiting) {
            statement = pollStatement(client, process, statement.getId());
        } else {
            throw logger.throwing(new LivyUserException("livy.unexpected_error"));
        }

        return logger.exit(LivyRestModelTransformer.toDataSources(statement));
    }


    @Nonnull
    @Override
    public Optional<TransformResponse> getTransformResult(@Nonnull SparkShellProcess process, @Nonnull String
        transformId) {
        logger.entry(process, transformId);
        Validate.isInstanceOf(SparkLivyProcess.class, process, "SparkLivyRestClient.getTransformResult called on non Livy Process");
        SparkLivyProcess sparkLivyProcess = (SparkLivyProcess) process;

        JerseyRestClient client = sparkLivyProcessManager.getClient(process);

        Integer stmtId = sparkLivyProcessManager.getStatementId(transformId);

        Statement statement = livyClient.getStatement(client, sparkLivyProcess, stmtId);

        sparkLivyProcessManager.setStatementId(transformId, statement.getId());

        TransformResponse response = LivyRestModelTransformer.toTransformResponse(statement, transformId);

        if (statement.getState() == StatementState.available && response.getStatus() == TransformResponse.Status.PENDING) {
            // TODO:: change this so that if transformId is a livyQueryID it knows what to do. similar to saveResponse.  A good first stab at this is at KYLO-2639-withLivyId
            // The result came back from Livy, but the transform result still needs to be fetched
            String script = scriptGenerator.script("getTransform", ScalaScriptUtils.scalaStr(transformId));
            statement = submitCode(client, script, process);
            // associate transformId to this new query and get results on subsequent call
            sparkLivyProcessManager.setStatementId(transformId, statement.getId());
        } else if (response.getStatus() == TransformResponse.Status.ERROR) {
            logger.error(String.format("Unexpected error found in transform response:\n%s", response.getMessage()));
            throw new LivyUserException("livy.transform_error");
        } // end if
        return logger.exit(Optional.of(response));
    }

    @Nonnull
    @Override
    public Optional<SaveResponse> getTransformSave(@Nonnull SparkShellProcess process, @Nonnull String
        transformId, @Nonnull String saveId) {
        logger.entry(process, transformId, saveId);
        Validate.isInstanceOf(SparkLivyProcess.class, process, "SparkLivyRestClient.getTransformSave called on non Livy Process");
        SparkLivyProcess sparkLivyProcess = (SparkLivyProcess) process;

        JerseyRestClient client = sparkLivyProcessManager.getClient(process);

        SaveResponse response;
        Integer stmtId = transformIdsToLivyId.getIfPresent(transformId);
        if (stmtId != null) {
            Statement statement = livyClient.getStatement(client, sparkLivyProcess, stmtId);
            response = LivyRestModelTransformer.toSaveResponse(statement);
            if (statement.getState() == StatementState.available) {
                transformIdsToLivyId.invalidate(transformId);
            }
        } else {
            // saveId is not an integer.  We have already processed Livy Response
            String script = scriptGenerator.script("getSave", saveId);

            Statement statement = submitCode(client, script, process);
            response = LivyRestModelTransformer.toSaveResponse(statement);
            response.setId(saveId);
            // remember the true id.  how? a cache.. it's not gonna get communicated back to us from UI..
            transformIdsToLivyId.put(transformId, statement.getId());
        }

        return logger.exit(Optional.of(response));
    }


    @Nonnull
    @Override
    public SaveResponse saveTransform(@Nonnull SparkShellProcess process, @Nonnull String
        transformId, @Nonnull SaveRequest request) {
        logger.entry(process, transformId, request);

        JerseyRestClient client = sparkLivyProcessManager.getClient(process);

        String script = scriptGenerator.wrappedScript("submitSaveJob", "", "\n", ScalaScriptUtils.toJsonInScalaString(request), transformId);

        Statement statement = submitCode(client, script, process);
        statement = pollStatement(client, process, statement.getId());
        SaveResponse saveResponse = LivyRestModelTransformer.toSaveResponse(statement);

        transformIdsToLivyId.put(transformId, statement.getId());

        return logger.exit(saveResponse);
    }

    private Statement submitCode(JerseyRestClient client, String script, SparkShellProcess process) {
        Validate.isInstanceOf(SparkLivyProcess.class, process, "SparkLivyRestClient.submitCode called on non Livy Process");
        SparkLivyProcess sparkLivyProcess = (SparkLivyProcess) process;

        StatementsPost sp = new StatementsPost.Builder().code(script).kind("spark").build();

        return livyClient.postStatement(client, sparkLivyProcess, sp);
    }


    @Nonnull
    @Override
    public TransformResponse transform(@Nonnull SparkShellProcess process, @Nonnull TransformRequest request) {
        logger.entry(process, request);

        JerseyRestClient client = sparkLivyProcessManager.getClient(process);

        // a tablename will be calculated for the request
        String transformId = ScalaScriptService.newTableName();
        transformCache.put(request, transformId);

        String script = scalaScriptService.wrapScriptForLivy(request, transformId);

        Statement statement = submitCode(client, script, process);

        // check the server for script result.  If polling limit reach just return to UI with PENDING status.
        statement = pollStatement(client, process, statement.getId(), livyProperties.getPollingLimit());

        sparkLivyProcessManager.setStatementId(transformId, statement.getId());

        return logger.exit(LivyRestModelTransformer.toTransformResponse(statement, transformId));


    }


    @Nonnull
    public TransformResponse kyloCatalogTransform(@Nonnull final SparkShellProcess process, @Nonnull final KyloCatalogReadRequest request) {
        logger.entry(process, request);

        String script = scriptGenerator.wrappedScript("kyloCatalogTransform", "", "\n", ScalaScriptUtils.toJsonInScalaString(request));
        logger.debug("scala str\n{}", script);

        JerseyRestClient client = sparkLivyProcessManager.getClient(process);

        Statement statement = submitCode(client, script, process);

        if (statement.getState() == StatementState.running
            || statement.getState() == StatementState.waiting) {
            statement = pollStatement(client, process, statement.getId());
        } else {
            throw logger.throwing(new LivyUserException("livy.unexpected_error"));
        }

        // call with null so a transformId will be generated for this query
        return logger.exit(LivyRestModelTransformer.toTransformResponse(statement, null));
    }


    @Nonnull
    @Override
    public ServerStatusResponse serverStatus(SparkShellProcess sparkShellProcess) {
        logger.entry(sparkShellProcess);

        if (sparkShellProcess instanceof SparkLivyProcess) {
            return logger.exit(LivyRestModelTransformer.toServerStatusResponse(livyServer, ((SparkLivyProcess) sparkShellProcess).getSessionId()));
        } else {
            throw logger.throwing(new IllegalStateException("SparkLivyRestClient.serverStatus called on non Livy Process"));
        }
    }


    @VisibleForTesting
    Statement pollStatement(JerseyRestClient jerseyClient, SparkShellProcess sparkShellProcess, Integer stmtId) {
        Validate.isInstanceOf(SparkLivyProcess.class, sparkShellProcess, "SparkLivyRestClient.pollStatement called on non Livy Process");
        SparkLivyProcess sparkLivyProcess = (SparkLivyProcess) sparkShellProcess;

        return livyClient.pollStatement(jerseyClient, sparkLivyProcess, stmtId);
    }


    private Statement pollStatement(JerseyRestClient jerseyClient, SparkShellProcess sparkShellProcess, Integer stmtId, Long wait) {
        Validate.isInstanceOf(SparkLivyProcess.class, sparkShellProcess, "SparkLivyRestClient.pollStatement called on non Livy Process");
        SparkLivyProcess sparkLivyProcess = (SparkLivyProcess) sparkShellProcess;

        return livyClient.pollStatement(jerseyClient, sparkLivyProcess, stmtId, wait);
    }

    /**
     * Creates a response for the specified message.
     *
     * @param status  the response status
     * @param message the message text
     * @return the response
     */
    private static Response createErrorResponse(@Nonnull final Response.Status status, @Nonnull final String message) {
        final TransformResponse entity = new TransformResponse();
        entity.setMessage(message);
        entity.setStatus(TransformResponse.Status.ERROR);
        return Response.status(status).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).entity(entity).build();
    }

}
