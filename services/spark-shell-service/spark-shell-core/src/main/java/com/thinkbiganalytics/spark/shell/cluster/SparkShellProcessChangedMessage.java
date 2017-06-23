package com.thinkbiganalytics.spark.shell.cluster;

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

import com.thinkbiganalytics.spark.shell.SparkShellProcess;

import java.io.Serializable;

/**
 * Message to indicate a change to a Spark Shell process
 */
public class SparkShellProcessChangedMessage implements Serializable {

    /**
     * Identifier for this message type
     */
    public static final String TYPE = "SPARK_SHELL_PROCESS_CHANGED";

    private static final long serialVersionUID = 3673405070014990855L;

    /**
     * Client identifier
     */
    private String clientId;

    /**
     * Spark Shell process information
     */
    private SparkShellProcess process;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public SparkShellProcess getProcess() {
        return process;
    }

    public void setProcess(SparkShellProcess process) {
        this.process = process;
    }
}
