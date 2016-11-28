package com.thinkbiganalytics.spark.shell;

import com.thinkbiganalytics.spark.rest.model.RegistrationRequest;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Handles starting and stopping instances of the Spark Shell process.
 */
public interface SparkShellProcessManager {

    /**
     * Waits for a Spark Shell process to start for the specified user.
     *
     * @param username the user who will be using the Spark Shell process
     * @return the Spark Shell process
     * @throws IllegalStateException if a Spark Shell process cannot be started
     * @throws InterruptedException if the current thread is interrupted while it is waiting
     */
    SparkShellProcess getProcessForUser(@Nonnull String username) throws InterruptedException;

    /**
     * Registers the specified Spark Shell process for handling user requests.
     *
     * <p>This method is not required to be implemented for every process manager. When a Spark Shell process is run on a remote machine then a process manager may require it to register once it
     * has finished starting.</p>
     *
     * @param clientId the Spark Shell client id
     * @param clientSecret the Spark Shell client secret
     * @param registration the Spark Shell registration request
     * @throws IllegalArgumentException if the client id or secret is not recognized
     * @throws UnsupportedOperationException if this process manager does not support registration
     */
    void register(@Nonnull String clientId, @Nonnull String clientSecret, @Nonnull RegistrationRequest registration);

    /**
     * Starts a new Spark Shell process for the specified user if one is not already running.
     *
     * <p>This method will return immediately but it may take a minute or two for the Spark Shell process to startup.</p>
     *
     * @param username the user who will be using the Spark Shell process
     * @throws IllegalStateException if a Spark Shell process cannot be started
     */
    void start(@Nonnull String username) throws IllegalStateException;
}
