package com.thinkbiganalytics.kylo.spark.livy;

/*-
 * #%L
 * kylo-spark-livy-core
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.thinkbiganalytics.kylo.exceptions.LivyException;
import com.thinkbiganalytics.kylo.model.Statement;
import com.thinkbiganalytics.kylo.model.StatementsPost;
import com.thinkbiganalytics.kylo.model.enums.StatementState;
import com.thinkbiganalytics.kylo.spark.SparkException;
import com.thinkbiganalytics.kylo.utils.LivyRestModelTransformer;
import com.thinkbiganalytics.kylo.utils.LivyUtils;
import com.thinkbiganalytics.kylo.utils.ScalaScriptService;
import com.thinkbiganalytics.kylo.utils.ScriptGenerator;
import com.thinkbiganalytics.kylo.utils.StatementStateTranslator;
import com.thinkbiganalytics.rest.JerseyRestClient;
import com.thinkbiganalytics.spark.io.ZipStreamingOutput;
import com.thinkbiganalytics.spark.model.SaveResult;
import com.thinkbiganalytics.spark.rest.model.DataSources;
import com.thinkbiganalytics.spark.rest.model.KyloCatalogReadRequest;
import com.thinkbiganalytics.spark.rest.model.SaveRequest;
import com.thinkbiganalytics.spark.rest.model.SaveResponse;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;
import com.thinkbiganalytics.spark.rest.model.job.SparkJobRequest;
import com.thinkbiganalytics.spark.rest.model.job.SparkJobResponse;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;

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

    /**
     * Default file system
     */
    @Resource
    public FileSystem fileSystem;

    /**
     * Cache of transformIds
     */
    @Nonnull
    public final static Cache<TransformRequest, String> transformCache = CacheBuilder.newBuilder()
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

        // Request result from Livy
        final JerseyRestClient client = sparkLivyProcessManager.getClient(process);
        final Integer statementId = sparkLivyProcessManager.getStatementId(id);
        final Statement statement = client.get(String.format("/sessions/%s/statements/%s", sparkLivyProcessManager.getLivySessionId(process), statementId), Statement.class);
        sparkLivyProcessManager.setStatementId(id, statement.getId());
        logger.debug("getStatement id={}, spr={}", statement.getId(), statement);

        // Generate response
        final SparkJobResponse response = LivyRestModelTransformer.toJobResponse(id, statement);

        if (response.getStatus() != TransformResponse.Status.ERROR) {
            return Optional.of(response);
        } else {
            throw new SparkException(String.format("Unexpected error found in transform response:\n%s", response.getMessage()));
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
        logger.debug("downloadTransform(process,transformId,saveId) called");

        JerseyRestClient client = sparkLivyProcessManager.getClient(process);

        String script = scriptGenerator.script("getSaveResult", scalaStr(saveId));

        Statement statement = submitCode(client, script, process);

        if (statement.getState() == StatementState.running
            || statement.getState() == StatementState.waiting) {
            statement = getStatement(client, sparkLivyProcessManager.getLivySessionId(process), statement.getId());
            logger.debug("{}", statement);
        } else {
            throw new LivyException("Unexpected error");
        }

        URI uri = LivyRestModelTransformer.toUri(statement);
        SaveResult result = new SaveResult(new Path(uri));

        // 2. Create a response with data from filesysem
        if (result != null && result.getPath() != null) {
            return Optional.of(
                Response.ok(new ZipStreamingOutput(result.getPath(), fileSystem))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
                    // TODO: could not get access to HttpHeaders constant in javax.ws.rs:javax.ws.rs-api:2.0.1 even after importing
                    .header("Content-Disposition", "attachment; filename=\"" + saveId + ".zip\"")
                    .build()
            );
        } else {
            return Optional.of(
                createErrorResponse(Response.Status.NOT_FOUND, "download.notFound")
            );
        }
    }


    @Nonnull
    @Override
    public DataSources getDataSources(@Nonnull SparkShellProcess process) {
        logger.debug("getDataSources(process) called");

        JerseyRestClient client = sparkLivyProcessManager.getClient(process);

        String script = scriptGenerator.script("getDataSources");

        Statement statement = submitCode(client, script, process);

        if (statement.getState() == StatementState.running
            || statement.getState() == StatementState.waiting) {
            statement = getStatement(client, sparkLivyProcessManager.getLivySessionId(process), statement.getId());
            logger.debug("{}", statement);
        } else {
            throw new LivyException("Unexpected error");
        }

        return LivyRestModelTransformer.toDataSources(statement);
    }


    @Nonnull
    @Override
    public Optional<TransformResponse> getTransformResult(@Nonnull SparkShellProcess process, @Nonnull String
        transformId) {
        logger.debug("getTransformResult(process,table) called");

        JerseyRestClient client = sparkLivyProcessManager.getClient(process);

        Integer stmtId = sparkLivyProcessManager.getStatementId(transformId);

        Statement statement = client.get(String.format("/sessions/%s/statements/%s", sparkLivyProcessManager.getLivySessionId(process), stmtId),
                                         Statement.class);

        sparkLivyProcessManager.setStatementId(transformId, statement.getId());
        logger.debug("getStatement id={}, spr={}", statement.getId(), statement);
        TransformResponse response = LivyRestModelTransformer.toTransformResponse(statement, transformId);

        if (statement.getState() == StatementState.available && response.getStatus() == TransformResponse.Status.PENDING) {
            // TODO:: change this so that if transformId is a livyQueryID it knows what to do.  similiar to saveResponse

            // The result came back from Livy, but the transform result still needs to be fetched
            String script = scriptGenerator.script("getTransform", scalaStr(transformId));
            statement = submitCode(client, script, process);
            // associate transformId to this new query and get results on subsequent call
            sparkLivyProcessManager.setStatementId(transformId, statement.getId());
        } else if (response.getStatus() == TransformResponse.Status.ERROR) {
            throw new LivyException(String.format("Unexpected error found in transform response:\n%s", response.getMessage()));
        } // end if
        return Optional.of(response);
    }

    @Nonnull
    @Override
    public Optional<SaveResponse> getTransformSave(@Nonnull SparkShellProcess process, @Nonnull String
        transformId, @Nonnull String saveId) {
        logger.debug("getTransformResult(process,table) called");

        JerseyRestClient client = sparkLivyProcessManager.getClient(process);

        Statement statement;
        SaveResponse response;
        try {
            Integer stmtId = Integer.parseInt(saveId);
            statement = client.get(
                String.format("/sessions/%s/statements/%s", sparkLivyProcessManager.getLivySessionId(process), stmtId),
                Statement.class);
            logger.debug("getStatement id={}, spr={}", stmtId, statement);
            response = LivyRestModelTransformer.toSaveResponse(statement);
        } catch (NumberFormatException nfe) {
            // saveId is not an integer.  We have already processed Livy Response
            String script = scriptGenerator.script("getSave", saveId);

            statement = submitCode(client, script, process);
            response = LivyRestModelTransformer.toSaveResponse(statement);
        }

        return Optional.of(response);
    }


    private String toJsonInScalaString(Object obj) {
        String objAsJson = "{}";
        try {
            objAsJson = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("Unable to serialize '{}' to string: {}", obj.getClass(), obj);
        }

        return scalaStr(objAsJson);
    }


    @Nonnull
    @Override
    public SaveResponse saveTransform(@Nonnull SparkShellProcess process, @Nonnull String
        transformId, @Nonnull SaveRequest request) {
        logger.debug("saveTransform(process,transformId,saveRequest) called");

        JerseyRestClient client = sparkLivyProcessManager.getClient(process);

        String script = scriptGenerator.wrappedScript("submitSaveJob", "", "\n", toJsonInScalaString(request), transformId);

        Statement statement = submitCode(client, script, process);

        SaveResponse saveResponse = new SaveResponse();
        saveResponse.setId(statement.getId().toString());
        saveResponse.setStatus(SaveResponse.Status.LIVY_PENDING);
        saveResponse.setProgress(statement.getProgress());

        return saveResponse;
    }

    Statement submitCode(JerseyRestClient client, String script, SparkShellProcess process) {
        StatementsPost sp = new StatementsPost.Builder().code(script).kind("spark").build();

        Statement statement = client.post(String.format("/sessions/%s/statements", sparkLivyProcessManager.getLivySessionId(process)),
                                          sp,
                                          Statement.class);
        logger.debug("statement={}", statement);

        return statement;
    }


    @Nonnull
    @Override
    public TransformResponse transform(@Nonnull SparkShellProcess process, @Nonnull TransformRequest request) {
        logger.debug("transform(process,request) called");

        JerseyRestClient client = sparkLivyProcessManager.getClient(process);

        // a tablename will be calculated for the request
        String transformId = ScalaScriptService.newTableName();
        transformCache.put(request, transformId);

        String script = scalaScriptService.wrapScriptForLivy(request, transformId);

        Statement statement = submitCode(client, script, process);

        sparkLivyProcessManager.setStatementId(transformId, statement.getId());

        TransformResponse response = new TransformResponse();
        response.setStatus(StatementStateTranslator.translate(statement.getState()));
        response.setProgress(statement.getProgress());
        response.setTable(transformId);

        return response;
    }


    private static final ObjectMapper mapper = new ObjectMapper();

    @Nonnull
    public TransformResponse kyloCatalogTransform(@Nonnull final SparkShellProcess process, @Nonnull final KyloCatalogReadRequest request) {
        logger.debug("kyloCatalogTransform(process,kyloCatalogReadRequest) called");

        String script = scriptGenerator.wrappedScript("kyloCatalogTransform", "", "\n", toJsonInScalaString(request));
        logger.debug("scala str\n{}", script);

        JerseyRestClient client = sparkLivyProcessManager.getClient(process);

        Statement statement = submitCode(client, script, process);

        if (statement.getState() == StatementState.running
            || statement.getState() == StatementState.waiting) {
            statement = getStatement(client, sparkLivyProcessManager.getLivySessionId(process), statement.getId());
            logger.debug("{}", statement);
        } else {
            throw new LivyException("Unexpected error");
        }

        // call with null so a transformId will be generated for this query
        return LivyRestModelTransformer.toTransformResponse(statement, null);
    }

    // TODO: is there a better way to wait for a response than synchronous?  UI could poll?
    @VisibleForTesting
    Statement getStatement(JerseyRestClient jerseyClient, Integer sessionId, Integer stmtId) {
        return LivyUtils.getStatement(jerseyClient, sessionId, stmtId);
    }

    /**
     * Creates a scala string literal of the given object
     *
     * @return a literal that can be placed in a scala code snippet
     */
    private static String scalaStr(Object obj) {
        if (obj == null) {
            return "null";
        } else {
            return "\"\"\"" + String.valueOf(obj) + "\"\"\"";
        }
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
