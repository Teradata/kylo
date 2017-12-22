package com.thinkbiganalytics.spark.shell;

/*-
 * #%L
 * Spark Shell Core
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

import com.thinkbiganalytics.rest.JerseyClientConfig;
import com.thinkbiganalytics.rest.JerseyRestClient;
import com.thinkbiganalytics.spark.rest.model.SaveRequest;
import com.thinkbiganalytics.spark.rest.model.SaveResponse;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Communicates with Spark Shell processes using Jersey REST clients.
 */
public class JerseySparkShellRestClient implements SparkShellRestClient {

    private static final Logger log = LoggerFactory.getLogger(JerseySparkShellRestClient.class);

    /**
     * Path to the query endpoint.
     */
    private static final String QUERY_PATH = "/api/v1/spark/shell/query";

    /**
     * Pattern for matching table IDs.
     */
    private static final Pattern TABLE_PATTERN = Pattern.compile("^[a-f0-9-]+$");

    /**
     * Path to the transform endpoint.
     */
    private static final String TRANSFORM_PATH = "/api/v1/spark/shell/transform";

    /**
     * Map of Spark Shell processes to Jersey REST clients
     */
    @Nonnull
    private final Map<SparkShellProcess, JerseyRestClient> clients = new WeakHashMap<>();

    @Nonnull
    @Override
    public Optional<Response> downloadQuery(@Nonnull final SparkShellProcess process, @Nonnull final String queryId, @Nonnull final String saveId) {
        return getDownload(process, queryId, saveId, QUERY_PATH);
    }

    @Nonnull
    @Override
    public Optional<Response> downloadTransform(@Nonnull final SparkShellProcess process, @Nonnull final String transformId, @Nonnull final String saveId) {
        return getDownload(process, transformId, saveId, TRANSFORM_PATH);
    }

    @Nonnull
    @Override
    public List<String> getDataSources(@Nonnull final SparkShellProcess process) {
        final GenericType<List<String>> stringListType = new GenericType<List<String>>() {
        };
        return getClient(process).get("/api/v1/spark/shell/data-sources", Collections.emptyMap(), stringListType);
    }

    @Nonnull
    @Override
    public Optional<TransformResponse> getQueryResult(@Nonnull final SparkShellProcess process, @Nonnull final String table) {
        return getResult(process, table, QUERY_PATH);
    }

    @Nonnull
    @Override
    public Optional<SaveResponse> getQuerySave(@Nonnull final SparkShellProcess process, @Nonnull final String queryId, @Nonnull final String saveId) {
        return getSave(process, queryId, saveId, QUERY_PATH);
    }

    @Nonnull
    @Override
    public Optional<TransformResponse> getTransformResult(@Nonnull final SparkShellProcess process, @Nonnull final String table) {
        return getResult(process, table, TRANSFORM_PATH);
    }

    @Nonnull
    @Override
    public Optional<SaveResponse> getTransformSave(@Nonnull final SparkShellProcess process, @Nonnull final String transformId, @Nonnull final String saveId) {
        return getSave(process, transformId, saveId, TRANSFORM_PATH);
    }

    @Nonnull
    @Override
    public TransformResponse query(@Nonnull SparkShellProcess process, @Nonnull TransformRequest request) {
        try {
            return getClient(process).post(QUERY_PATH, request, TransformResponse.class);
        } catch (final InternalServerErrorException e) {
            throw propagateTransform(e);
        }
    }

    @Nonnull
    @Override
    public SaveResponse saveQuery(@Nonnull final SparkShellProcess process, @Nonnull final String id, @Nonnull final SaveRequest request) {
        final String fullPath = String.format("%s/%s/save", QUERY_PATH, id);

        try {
            return getClient(process).post(fullPath, request, SaveResponse.class);
        } catch (final InternalServerErrorException e) {
            throw propagateSave(e);
        } catch (final NotFoundException e) {
            throw new IllegalArgumentException();
        }
    }

    @Nonnull
    @Override
    public SaveResponse saveTransform(@Nonnull final SparkShellProcess process, @Nonnull final String id, @Nonnull final SaveRequest request) {
        final String fullPath = String.format("%s/%s/save", TRANSFORM_PATH, id);

        try {
            return getClient(process).post(fullPath, request, SaveResponse.class);
        } catch (final InternalServerErrorException e) {
            throw propagateSave(e);
        } catch (final NotFoundException e) {
            throw new IllegalArgumentException();
        }
    }

    @Nonnull
    @Override
    public TransformResponse transform(@Nonnull final SparkShellProcess process, @Nonnull final TransformRequest request) {
        try {
            return getClient(process).post(TRANSFORM_PATH, request, TransformResponse.class);
        } catch (final InternalServerErrorException e) {
            throw propagateTransform(e);
        }
    }

    /**
     * Gets or creates a Jersey REST client for the specified Spark Shell process.
     *
     * @param process the Spark Shell process
     * @return the Jersey REST client
     */
    @Nonnull
    private JerseyRestClient getClient(@Nonnull final SparkShellProcess process) {
        return clients.computeIfAbsent(process, target -> {
            final JerseyClientConfig config = new JerseyClientConfig();
            config.setHost(target.getHostname());
            config.setPort(target.getPort());
            return new JerseyRestClient(config);
        });
    }

    /**
     * Downloads the results of a save running on the specified Spark Shell process.
     *
     * @param process     the Spark Shell process
     * @param transformId the transform identifier
     * @param saveId      the save identifier
     * @return the results, if the save exists
     */
    @Nonnull
    private Optional<Response> getDownload(@Nonnull final SparkShellProcess process, @Nonnull final String transformId, @Nonnull final String saveId, @Nonnull final String path) {
        // Validate arguments
        if (!TABLE_PATTERN.matcher(transformId).matches() || !TABLE_PATTERN.matcher(saveId).matches()) {
            return Optional.empty();
        }

        // Query Spark Shell process
        final String fullPath = String.format("%s/%s/save/%s/zip", path, transformId, saveId);
        final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>(Collections.singletonMap(HttpHeaders.ACCEPT, MediaType.APPLICATION_OCTET_STREAM));

        try {
            return Optional.of(getClient(process).getWithHeaders(fullPath, headers, Collections.emptyMap(), Response.class));
        } catch (final NotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets the result of the specified transformation.
     *
     * @param process Spark Shell process
     * @param table   table ID
     * @param path    Spark Shell REST API path
     * @return the transform response
     */
    @Nonnull
    private Optional<TransformResponse> getResult(@Nonnull final SparkShellProcess process, @Nonnull final String table, @Nonnull final String path) {
        // Validate arguments
        if (!TABLE_PATTERN.matcher(table).matches()) {
            return Optional.empty();
        }

        // Query Spark Shell process
        final String fullPath = String.format("%s/%s", path, table);
        final GenericType<TransformResponse> type = new GenericType<TransformResponse>() {
        };

        try {
            return Optional.of(getClient(process).get(fullPath, Collections.emptyMap(), type));
        } catch (final InternalServerErrorException e) {
            throw propagateTransform(e);
        } catch (final NotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Gets the result of the specified save.
     *
     * @param process     Spark Shell process
     * @param transformId transform identifier
     * @param saveId      save identifier
     * @param path        Spark Shell REST API path
     * @return the save response
     */
    @Nonnull
    private Optional<SaveResponse> getSave(@Nonnull final SparkShellProcess process, @Nonnull final String transformId, @Nonnull final String saveId, @Nonnull final String path) {
        // Validate arguments
        if (!TABLE_PATTERN.matcher(transformId).matches() || !TABLE_PATTERN.matcher(saveId).matches()) {
            return Optional.empty();
        }

        // Query Spark Shell process
        final String fullPath = String.format("%s/%s/save/%s", path, transformId, saveId);
        final GenericType<SaveResponse> type = new GenericType<SaveResponse>() {
        };

        try {
            return Optional.of(getClient(process).get(fullPath, Collections.emptyMap(), type));
        } catch (final InternalServerErrorException e) {
            throw propagateSave(e);
        } catch (final NotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Propagates the cause of the specified internal server error.
     */
    @Nonnull
    private SparkShellSaveException propagateSave(@Nonnull final InternalServerErrorException e) {
        final SaveResponse response;
        try {
            response = e.getResponse().readEntity(SaveResponse.class);
        } catch (final Exception decodeEx) {
            log.debug("Failed to decode transform response: {}", e.getResponse().readEntity(String.class));
            throw new SparkShellSaveException(decodeEx);
        }

        throw new SparkShellSaveException(response.getMessage(), response.getId());
    }

    /**
     * Propagates the cause of the specified internal server error.
     */
    @Nonnull
    private SparkShellTransformException propagateTransform(@Nonnull final InternalServerErrorException e) {
        final TransformResponse response;
        try {
            response = e.getResponse().readEntity(TransformResponse.class);
        } catch (final Exception decodeEx) {
            log.debug("Failed to decode transform response: {}", e.getResponse().readEntity(String.class));
            throw new SparkShellTransformException(decodeEx);
        }

        throw new SparkShellTransformException(response.getMessage());
    }
}
