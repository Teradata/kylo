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

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;

import org.apache.spark.launcher.SparkAppHandle;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A Kylo Spark Shell process that is managed by the Spark Launcher.
 *
 * <p>This class is thread-safe.</p>
 */
@SuppressWarnings("WeakerAccess")
public class SparkLauncherSparkShellProcess implements Serializable, SparkAppHandle.Listener, SparkShellProcess {

    private static final Logger log = LoggerFactory.getLogger(SparkLauncherSparkShellProcess.class);
    private static final long serialVersionUID = -8503739977231360074L;

    /**
     * Spark Shell client identifier
     */
    @Nonnull
    private final String clientId;

    /**
     * Spark Shell client secret
     */
    @Nonnull
    private final String clientSecret;

    /**
     * Spark launcher application handle
     */
    @Nullable
    private transient SparkAppHandle handle;

    /**
     * List of process listeners
     */
    @Nullable
    private transient List<SparkShellProcessListener> listeners;

    /**
     * Hostname of the Spark Shell client
     */
    @Nullable
    private String hostname;

    /**
     * Port number of the Spark Shell client
     */
    private int port;

    /**
     * Expected time for client to be ready
     */
    private final long readyTime;

    /**
     * Latch for application startup
     */
    @Nullable
    private transient CountDownLatch startSignal;

    /**
     * User associated with this process
     */
    @Nullable
    private String username;

    /**
     * Constructs a {@code SparkLauncherSparkShellProcess} with the specified process.
     *
     * @param handle       the Spark Launcher app handle
     * @param clientId     the client identifier
     * @param clientSecret the client secret
     * @param timeout      the maximum time to wait for the client to be ready
     * @param unit         the time unit of the {@code timeout} argument
     */
    public SparkLauncherSparkShellProcess(@Nonnull final SparkAppHandle handle, @Nonnull final String clientId, @Nonnull final String clientSecret, final long timeout, @Nonnull final TimeUnit unit) {
        this.handle = handle;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        readyTime = DateTimeUtils.currentTimeMillis() + unit.toMillis(timeout);

        // Listen for state changes
        handle.addListener(this);
    }

    /**
     * Adds a process listener to this process.
     */
    public synchronized void addListener(@Nonnull final SparkShellProcessListener listener) {
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<>();
        }
        listeners.add(listener);
    }

    /**
     * Removes a process listener from this process.
     */
    public void removeListener(@Nonnull final SparkShellProcessListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Tries to kill the process. This will not send a {@link #stop()} message to the process so it's recommended that users first try to stop the process cleanly and only resort to this
     * method if that fails.
     */
    public void destroy() {
        // Reset ready state
        hostname = null;
        if (startSignal != null) {
            startSignal.countDown();
        }

        // Kill process
        if (handle != null) {
            handle.kill();
        }
    }

    /**
     * Gets the Spark Shell client identifier.
     */
    @Nonnull
    public String getClientId() {
        return clientId;
    }

    /**
     * Gets the Spark Shell client secret.
     */
    @Nonnull
    public String getClientSecret() {
        return clientSecret;
    }

    @Nonnull
    @Override
    public String getHostname() {
        if (waitForReady() && hostname != null) {
            return hostname;
        } else {
            throw new IllegalStateException("Spark Shell client did not start within the expected time.");
        }
    }

    @Override
    public int getPort() {
        if (waitForReady()) {
            return port;
        } else {
            throw new IllegalStateException("Spark Shell client did not start within the expected time.");
        }
    }

    /**
     * Gets the name of the user who owns this process.
     *
     * @return the user's name, or {@code null} if owned by the system
     */
    @Nullable
    public String getUsername() {
        return username;
    }

    @Override
    public void infoChanged(final SparkAppHandle sparkAppHandle) {
        Preconditions.checkArgument(sparkAppHandle == null || handle == sparkAppHandle, "Handle does not match Spark Shell process");
        if (handle != null) {
            log.debug("Spark Shell client [{}] application id: {}", clientId, handle.getAppId());
        } else {
            log.debug("Spark Shell client [{}] info changed", clientId);
        }
    }

    @Override
    public boolean isLocal() {
        return (handle != null && username != null);
    }

    /**
     * Indicates that this client is ready to process requests.
     */
    public boolean isReady() {
        return (hostname != null);
    }

    /**
     * Sets the hostname for communicating with this Spark Shell client.
     *
     * @param hostname the hostname
     */
    public void setHostname(@Nonnull final String hostname) {
        this.hostname = hostname;
    }

    /**
     * Sets the port number for communicating with this Spark Shell client.
     *
     * @param port the port number
     */
    public void setPort(final int port) {
        this.port = port;
    }

    /**
     * Indicates to all waiting threads that the Spark Shell client is ready to receive requests.
     *
     * @param fireEvent {@code true} to fire the {@link SparkShellProcessListener#processReady(SparkShellProcess)} event
     */
    public void setReady(final boolean fireEvent) {
        if (startSignal != null) {
            startSignal.countDown();
        }
        if (listeners != null && fireEvent) {
            listeners.forEach(listener -> listener.processReady(this));
        }
    }

    /**
     * Sets the name of the user who owns this process.
     */
    public void setUsername(@Nonnull final String username) {
        this.username = Objects.requireNonNull(username);
    }

    @Override
    public void stateChanged(final SparkAppHandle sparkAppHandle) {
        Preconditions.checkArgument(sparkAppHandle == null || handle == sparkAppHandle, "Handle does not match Spark Shell process");

        if (handle != null) {
            log.debug("Spark Shell client [{}] state changed: {}", clientId, handle.getState());
            if (handle.getState().isFinal()) {
                if (startSignal != null) {
                    startSignal.countDown();
                }
                if (listeners != null) {
                    listeners.forEach(listener -> listener.processStopped(this));
                }
            }
        } else {
            log.debug("Spark Shell client [{}] state changed: {}", clientId);
        }
    }

    /**
     * Asks the process to stop. This is best-effort since the process may fail to receive or act on the command.
     */
    public void stop() {
        // Reset ready state
        hostname = null;
        if (startSignal != null) {
            startSignal.countDown();
        }

        // Stop process
        if (handle != null) {
            handle.stop();
        }
    }

    @Override
    public String toString() {
        return "SparkLauncherSparkShellProcess{" +
               "clientId='" + clientId + '\'' +
               ", hostname='" + hostname + '\'' +
               ", port=" + port +
               ", username='" + username + '\'' +
               '}';
    }

    /**
     * Waits uninterruptibly for the Spark Shell client to be ready.
     *
     * @return {@code true} if the client is ready, or {@code false} otherwise
     */
    public boolean waitForReady() {
        if (hostname == null) {
            final long remaining = readyTime - DateTimeUtils.currentTimeMillis();
            if (remaining > 0) {
                if (startSignal == null) {
                    startSignal = new CountDownLatch(1);
                }
                Uninterruptibles.awaitUninterruptibly(startSignal, remaining, TimeUnit.MILLISECONDS);
            }
        }
        return isReady();
    }
}
