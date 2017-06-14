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

import com.thinkbiganalytics.spark.conf.model.SparkShellProperties;
import com.thinkbiganalytics.spark.conf.model.SparkShellServerProperties;
import com.thinkbiganalytics.spark.rest.model.RegistrationRequest;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Provides information on an externally-managed Spark Shell server process.
 */
public class ServerProcessManager implements SparkShellProcessManager {

    /**
     * Spark Shell process
     */
    @Nonnull
    private final SparkShellProcess process;

    /**
     * Constructs a {@code ServerProcessManager} with the specified properties.
     *
     * @param properties the Spark Shell properties
     */
    public ServerProcessManager(@Nonnull final SparkShellProperties properties) {
        final Optional<SparkShellServerProperties> server = Optional.ofNullable(properties.getServer());

        // Create process using defaults if necessary
        final String hostname = server.map(SparkShellServerProperties::getHost).orElse("localhost");
        final int port = server.map(SparkShellServerProperties::getPort).orElse(8450);
        process = new SimpleSparkShellProcess(hostname, port);
    }

    @Override
    public void addListener(@Nonnull final SparkShellProcessListener listener) {
        // ignored
    }

    @Override
    public void removeListener(@Nonnull final SparkShellProcessListener listener) {
        // ignored
    }

    @Nonnull
    @Override
    public SparkShellProcess getProcessForUser(@Nonnull final String username) throws InterruptedException {
        return process;
    }

    @Nonnull
    @Override
    public SparkShellProcess getSystemProcess() throws InterruptedException {
        return process;
    }

    @Override
    public void register(@Nonnull final String clientId, @Nonnull final RegistrationRequest registration) {
        // ignored
    }

    @Override
    public void start(@Nonnull final String username) {
        // ignored
    }
}
