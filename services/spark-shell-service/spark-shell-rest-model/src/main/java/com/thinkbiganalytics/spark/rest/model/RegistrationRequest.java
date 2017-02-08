package com.thinkbiganalytics.spark.rest.model;

/*-
 * #%L
 * Spark Shell Service REST Model
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

/**
 * Request to register a Spark Shell process with Kylo.
 */
public class RegistrationRequest {

    /**
     * Remote hostname
     */
    private String host;

    /**
     * Remote port number
     */
    private int port;

    /**
     * Gets the hostname for communicating with the Spark Shell client.
     *
     * @return the remote hostname
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the hostname for communicating with the Spark Shell client.
     *
     * @param host the remote hostname
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the port number for communicating with the Spark Shell client.
     *
     * @return the remote port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port number for communicating with the Spark Shell client.
     *
     * @param port the remote port number
     */
    public void setPort(int port) {
        this.port = port;
    }
}
