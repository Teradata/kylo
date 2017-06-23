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
import java.util.List;

/**
 * Message to sync Spark Shell processes between nodes.
 */
public class SparkShellProcessSyncMessage implements Serializable {

    /**
     * Identifier for this message type
     */
    public static final String TYPE = "SPARK_SHELL_PROCESS_SYNC";

    private static final long serialVersionUID = -1946260847987738899L;

    /**
     * List of processes to update locally
     */
    private List<SparkShellProcess> processes;

    public List<SparkShellProcess> getProcesses() {
        return processes;
    }

    public void setProcesses(List<SparkShellProcess> processes) {
        this.processes = processes;
    }
}
