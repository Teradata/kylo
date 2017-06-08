package com.thinkbiganalytics.spark;

/*-
 * #%L
 * kylo-spark-shell-client-app
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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.thinkbiganalytics.rest.JerseyClientConfig;
import com.thinkbiganalytics.rest.JerseyRestClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.Response;

/**
 * Registers this Spark Shell client with a remote Kylo services server.
 */
public class RemoteClientRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RemoteClientRunner.class);

    /**
     * Spark Shell client port number
     */
    private final int localPort;

    /**
     * Registration server
     */
    @Nullable
    private final String server;

    /**
     * Constructs a {@code RemoteClientRunner} with the specified command-line options and port number.
     *
     * @param parameters the command-line options
     * @param serverPort the Spark Shell client port number
     */
    public RemoteClientRunner(@Nonnull final SparkShellOptions parameters, final int serverPort) {
        server = parameters.getServer();
        localPort = serverPort;
    }

    /**
     * Registers this Spark Shell client with the remote Kylo services server.
     */
    public void register() {
        Preconditions.checkState(server != null, "Registration server is not available.");

        // Parse server address
        final String[] serverAddress = server.split(":", 2);
        Preconditions.checkArgument(serverAddress.length == 2, "Not a valid registration server address: %s", server);

        final int port;
        try {
            port = Integer.parseInt(serverAddress[1]);
        } catch (final NumberFormatException e) {
            throw new NumberFormatException("Not a valid registration server address: " + server);
        }

        // Find client id and secret
        final String clientId = System.getenv("KYLO_CLIENT_ID");
        Preconditions.checkNotNull(clientId, "Environment variable is not defined: KYLO_CLIENT_ID");

        final String clientSecret = System.getenv("KYLO_CLIENT_SECRET");
        Preconditions.checkNotNull(clientSecret, "Environment variable is not defined: KYLO_CLIENT_SECRET");

        // Register with server
        final JerseyClientConfig config = new JerseyClientConfig(serverAddress[0], clientId, clientSecret);
        config.setPort(port);

        final JerseyRestClient client = getRestClient(config);
        final String hostName = getHostName();

        log.info("Registering client {} at {}:{} with server {}.", clientId, hostName, localPort, server);
        final Response response = client.post("/proxy/v1/spark/shell/register", ImmutableMap.of("host", hostName, "port", localPort));

        if (response != null && response.getStatus() >= 200 && response.getStatus() < 300) {
            log.info("Successfully registered client.");
        } else {
            log.info("Registration failed with response: {}", response);
            throw new IllegalStateException("Failed to register with server");
        }
    }

    @Override
    public void run(@Nonnull final ApplicationArguments args) throws Exception {
        log.trace("run - entry with ({})", args);

        if (server != null) {
            try {
                register();
            } catch (final Exception e) {
                // Registration failed. No requests will be received so might as well exit.
                log.error("Failed to register Spark Shell client: {}", e.toString(), e);
                Runtime.getRuntime().exit(100);
            }
        } else {
            log.info("Remote server address not set. Skipping registration.");
        }

        log.trace("run - exit");
    }

    /**
     * Gets the hostname of this Spark Shell client.
     *
     * @return the local hostname
     */
    String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            throw new IllegalStateException("Hostname is not available");
        }
    }

    /**
     * Creates a REST client for communicating with the Kylo services server.
     *
     * @param config the REST client configuration
     * @return the REST client
     */
    JerseyRestClient getRestClient(@Nonnull final JerseyClientConfig config) {
        return new JerseyRestClient(config);
    }
}
