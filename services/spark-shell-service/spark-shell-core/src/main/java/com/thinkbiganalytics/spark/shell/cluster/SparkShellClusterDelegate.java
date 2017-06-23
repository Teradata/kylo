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

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Coordinates with a Kylo cluster to manage Spark Shell processes.
 */
public interface SparkShellClusterDelegate {

    /**
     * Gets the list of Spark Shell processes.
     */
    @Nonnull
    List<SparkShellProcess> getProcesses();

    /**
     * Updates the local copy of the specified process.
     *
     * @param process the new process information
     */
    void updateProcess(@Nonnull SparkShellProcess process);

    /**
     * Removes the local copy of the specified process.
     *
     * @param clientId the client identifier
     */
    void removeProcess(@Nonnull String clientId);
}
