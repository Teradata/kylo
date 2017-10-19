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

import com.google.common.collect.ImmutableMap;
import com.thinkbiganalytics.rest.JerseyClientConfig;
import com.thinkbiganalytics.rest.JerseyRestClient;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;

/**
 * Communicates with Spark Shell processes using Jersey REST clients.
 */
public class JerseySparkShellRestClient implements SparkShellRestClient {

    private static final Logger log = LoggerFactory.getLogger(JerseySparkShellRestClient.class);

    /**
     * Map of Spark Shell processes to Jersey REST clients
     */
    @Nonnull
    private final Map<SparkShellProcess, JerseyRestClient> clients = new WeakHashMap<>();

    @Nonnull
    @Override
    public Optional<TransformResponse> getTable(@Nonnull final SparkShellProcess process, @Nonnull final String table) {
        // Validate arguments
        if (!table.matches("^[a-f0-9-]+$")) {
            return Optional.empty();
        }

        // Query Spark Shell process
        try {
            return Optional.of(getClient(process).get("/api/v1/spark/shell/transform/" + table, ImmutableMap.of(), TransformResponse.class));
        } catch (final InternalServerErrorException e) {
            throw propagate(e);
        } catch (final NotFoundException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    public TransformResponse transform(@Nonnull final SparkShellProcess process, @Nonnull final TransformRequest request) {
        try {
            return getClient(process).post("/api/v1/spark/shell/transform", request, TransformResponse.class);
        } catch (final InternalServerErrorException e) {
            throw propagate(e);
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
        JerseyRestClient client = clients.get(process);

        if (client == null) {
            final JerseyClientConfig config = new JerseyClientConfig();
            config.setHost(process.getHostname());
            config.setPort(process.getPort());

            client = new JerseyRestClient(config);
            clients.put(process, client);
        }

        return client;
    }

    @Nonnull
    private SparkShellTransformException propagate(@Nonnull final InternalServerErrorException e) {
        try {
            throw new SparkShellTransformException(e.getResponse().readEntity(TransformResponse.class).getMessage());
        } catch (final Exception decodeEx) {
            log.debug("Failed to decode transform response: {}", e.getResponse().readEntity(String.class));
            throw new SparkShellTransformException(decodeEx);
        }
    }
}
