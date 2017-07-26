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
 * Receives events when the state of a {@link SparkShellProcess} changes.
 */
public interface SparkShellProcessListener {

    /**
     * Called when a process is ready to process requests.
     *
     * @param process the Spark Shell process
     */
    void processReady(@Nonnull SparkShellProcess process);

    /**
     * Called when a new process has been created.
     *
     * @param process the Spark Shell process
     */
    void processStarted(@Nonnull SparkShellProcess process);

    /**
     * Called when a process is no longer able to process new requests.
     *
     * @param process the Spark Shell process
     */
    void processStopped(@Nonnull SparkShellProcess process);
}
