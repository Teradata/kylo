package com.thinkbiganalytics.spark.shell;

/*-
 * #%L
 * Spark Shell Service API
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

import javax.annotation.Nonnull;

/**
 * A Spark Shell client running in a separate JVM.
 */
public interface SparkShellProcess {

    /**
     * Gets the client identifier for this process.
     *
     * @return a unique identifier for this client
     */
    @Nonnull
    String getClientId();

    /**
     * Gets the hostname for communicating with this Spark Shell client.
     *
     * @return the hostname
     * @throws IllegalStateException if the Spark Shell client is not ready to receive commands
     */
    @Nonnull
    String getHostname();

    /**
     * Gets the port number for communicating with this Spark Shell client.
     *
     * @return the port number
     * @throws IllegalStateException if the Spark Shell client is not ready to receive commands
     */
    int getPort();

    /**
     * Indicates that this process is managed locally and not by another node in a cluster.
     *
     * @return {@code true} if this process runs locally, or {@code false} otherwise
     */
    boolean isLocal();
}
