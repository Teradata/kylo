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

import com.beust.jcommander.Parameter;

/**
 * Command-line options for the Spark Shell client.
 */
public class SparkShellOptions {

    /**
     * Indicates the timeout is disabled
     */
    public static final int INDEFINITE_TIMEOUT = 0;

    /**
     * Indicates no port number was specified
     */
    public static final int NO_PORT = -1;

    /**
     * Time to wait for a request before terminating
     */
    @Parameter(names = "--idle-timeout", description = "Time to wait for a request before terminating")
    private int idleTimeout = INDEFINITE_TIMEOUT;

    /**
     * Maximum port number to listen on
     */
    @Parameter(names = "--port-max", description = "Maximum port number to listen on")
    private int portMax = NO_PORT;

    /**
     * Minimum port number to listen on
     */
    @Parameter(names = "--port-min", description = "Minimum port number to listen on")
    private int portMin = NO_PORT;

    /**
     * Registration server host:port
     */
    @Parameter(names = "--server", description = "Registration server host:port")
    private String server;

    /**
     * Indicates the amount of time in seconds to wait for a user request before terminating a Spark Shell process. A value of {@link #INDEFINITE_TIMEOUT} should disable the timeout.
     *
     * @return the idle timeout
     */
    public int getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Gets the maximum port number that a Spark Shell process may listen on.
     *
     * @return the maximum port number
     */
    public int getPortMax() {
        return portMax;
    }

    /**
     * Gets the minimum port number that a Spark Shell process may listen on.
     *
     * @return the minimum port number
     */
    public int getPortMin() {
        return portMin;
    }

    /**
     * Gets the registration server host and port.
     *
     * @return the registration server
     */
    public String getServer() {
        return server;
    }
}
