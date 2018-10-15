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

import javax.annotation.Nonnull;

/**
 * A simple Kylo Spark Shell process where the hostname and port are constants.
 */
public class SimpleSparkShellProcess implements SparkShellProcess {

    /**
     * Host where Spark Shell is running
     */
    @Nonnull
    private final String hostname;

    /**
     * Port where Spark Shell is listening
     */
    private final int port;

    /**
     * Constructs a {@code SimpleSparkShellProcess} with the specified hostname and port.
     *
     * @param hostname host where Spark Shell is running
     * @param port port where Spark Shell is listening
     */
    public SimpleSparkShellProcess(@Nonnull final String hostname, final int port) {
        this.hostname = hostname;
        this.port = port;
    }

    @Nonnull
    @Override
    public String getClientId() {
        return "SIMPLE";
    }

    @Nonnull
    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isLocal() {
        return false;
    }
}
