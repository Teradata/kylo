package com.thinkbiganalytics.spark.shell;

import com.google.common.collect.ImmutableMap;
import com.thinkbiganalytics.rest.JerseyClientConfig;
import com.thinkbiganalytics.rest.JerseyRestClient;
import com.thinkbiganalytics.spark.rest.model.TransformRequest;
import com.thinkbiganalytics.spark.rest.model.TransformResponse;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;
import javax.ws.rs.NotFoundException;

/**
 * Communicates with Spark Shell processes using Jersey REST clients.
 */
public class JerseySparkShellRestClient implements SparkShellRestClient {

    /** Map of Spark Shell processes to Jersey REST clients */
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
        } catch (final NotFoundException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    public TransformResponse transform(@Nonnull final SparkShellProcess process, @Nonnull final TransformRequest request) {
        return getClient(process).post("/api/v1/spark/shell/transform", request, TransformResponse.class);
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
}
