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

import com.thinkbiganalytics.spark.rest.model.RegistrationRequest;

import javax.annotation.Nonnull;

/**
 * Handles starting and stopping instances of the Spark Shell process.
 */
public interface SparkShellProcessManager {

    /**
     * Adds a listener for receiving process state change events.
     */
    void addListener(@Nonnull SparkShellProcessListener listener);

    /**
     * Removes the listener from this process manager.
     */
    void removeListener(@Nonnull SparkShellProcessListener listener);

    /**
     * Waits for a Spark Shell process to start for the specified user.
     *
     * @param username the user who will be using the Spark Shell process
     * @return the Spark Shell process
     * @throws IllegalStateException if a Spark Shell process cannot be started
     * @throws InterruptedException  if the current thread is interrupted while it is waiting
     */
    @Nonnull
    SparkShellProcess getProcessForUser(@Nonnull String username) throws InterruptedException;

    /**
     * Waits for the system Spark Shell process to start.
     *
     * @return the system Spark Shell process
     * @throws IllegalStateException if a Spark Shell process cannot be started
     * @throws InterruptedException  if the current thread is interrupted while it is waiting
     */
    @Nonnull
    SparkShellProcess getSystemProcess() throws InterruptedException;

    /**
     * Registers the specified Spark Shell process for handling user requests.
     *
     * <p>This method is not required to be implemented for every process manager. When a Spark Shell process is run on a remote machine then a process manager may require it to register once it
     * has finished starting.</p>
     *
     * @param clientId     the Spark Shell client id
     * @param registration the Spark Shell registration request
     * @throws IllegalArgumentException      if the client id or secret is not recognized
     * @throws UnsupportedOperationException if this process manager does not support registration
     */
    void register(@Nonnull String clientId, @Nonnull RegistrationRequest registration);

    /**
     * Starts a new Spark Shell process for the specified user if one is not already running.
     *
     * <p>This method will return immediately but it may take a minute or two for the Spark Shell process to startup.</p>
     *
     * @param username the user who will be using the Spark Shell process
     * @throws IllegalStateException if a Spark Shell process cannot be started
     */
    void start(@Nonnull String username);
}
