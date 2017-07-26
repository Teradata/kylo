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
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.thinkbiganalytics.spark.conf.model.KerberosSparkProperties;
import com.thinkbiganalytics.spark.conf.model.SparkShellProperties;
import com.thinkbiganalytics.spark.rest.model.RegistrationRequest;
import com.thinkbiganalytics.spark.shell.cluster.SparkShellClusterDelegate;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PreDestroy;

/**
 * Manages multiple Kylo Spark Shell processes.
 *
 * <p>A unique username and password is generated for each process to communicate with the Kylo Services process.</p>
 */
public abstract class AbstractProcessManager implements ApplicationRunner, SparkShellClusterDelegate, SparkShellProcessListener, SparkShellProcessManager {

    private static final Logger log = LoggerFactory.getLogger(AbstractProcessManager.class);

    /**
     * Kylo Spark Shell client configuration
     */
    @Nonnull
    protected final SparkShellProperties clientProperties;

    /**
     * Thread pool for starting and managing processes
     */
    @Nonnull
    private final ScheduledExecutorService executor;

    /**
     * Kerberos configuration for Spark Shell client
     */
    @Nullable
    protected final KerberosSparkProperties kerberos;

    /**
     * List of process listeners
     */
    @Nonnull
    private final List<SparkShellProcessListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * System Kylo Spark Shell process
     */
    @Nullable
    private SparkLauncherSparkShellProcess systemProcess;

    /**
     * Username to password mapping
     */
    @Nonnull
    private final Properties users;

    /**
     * Constructs an {@code AbstractProcessManager} with the specified configuration.
     *
     * @param sparkShellProperties the Kylo Spark Shell client configuration
     * @param kerberosProperties   the Kerberos configuration for the Kylo Spark Shell client
     * @param users                the username to password mapping
     */
    public AbstractProcessManager(@Nonnull final SparkShellProperties sparkShellProperties, @Nonnull final KerberosSparkProperties kerberosProperties, @Nonnull final Properties users) {
        this.clientProperties = sparkShellProperties;
        this.users = users;

        // Verify Kerberos properties
        if (kerberosProperties.isKerberosEnabled()) {
            Preconditions.checkState(StringUtils.isNoneBlank(kerberosProperties.getKeytabLocation()), "The kerberos.spark.keytabLocation property cannot be blank when Kerberos is enabled.");
            Preconditions.checkState(StringUtils.isNoneBlank(kerberosProperties.getKerberosPrincipal()), "The kerberos.spark.kerberosPrincipal property cannot be blank when Kerberos is enabled.");
            kerberos = kerberosProperties;
        } else {
            kerberos = null;
        }

        // Create the scheduler
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("spark-shell-pool-%d")
            .build();
        executor = new ScheduledThreadPoolExecutor(1, threadFactory);
    }

    @Override
    public void addListener(@Nonnull final SparkShellProcessListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(@Nonnull final SparkShellProcessListener listener) {
        listeners.remove(listener);
    }

    @Nonnull
    @Override
    public SparkShellProcess getProcessForUser(@Nonnull final String username) throws InterruptedException {
        start(username);

        final Optional<SparkLauncherSparkShellProcess> process = getProcessByUsername(username);
        if (process.isPresent()) {
            if (process.get().waitForReady()) {
                return process.get();
            } else {
                removeProcess(process.get().getClientId());
            }
        }
        throw new IllegalStateException("Failed to start Kylo Spark Shell service");
    }

    @Nonnull
    @Override
    public synchronized SparkShellProcess getSystemProcess() {
        // Create process if not started
        if (systemProcess == null) {
            try {
                systemProcess = createProcessBuilder(null).master("local").deployMode("client").idleTimeout(0, TimeUnit.SECONDS).build();
            } catch (final IOException e) {
                log.error("Failed to start system Spark Shell process", e);
                throw new IllegalStateException("Failed to start system Spark Shell process", e);
            }

            systemProcess.addListener(this);
            users.put(systemProcess.getClientId(), systemProcess.getClientSecret());
            listeners.forEach(listener -> listener.processStarted(systemProcess));
        }

        // Wait for ready
        if (systemProcess.waitForReady()) {
            return systemProcess;
        } else {
            final SparkLauncherSparkShellProcess process = systemProcess;
            stopProcess(process);
            return process;
        }
    }

    @Override
    public void processReady(@Nonnull final SparkShellProcess process) {
        listeners.forEach(listener -> listener.processReady(process));
    }

    @Override
    public void processStarted(@Nonnull final SparkShellProcess process) {
        listeners.forEach(listener -> listener.processReady(process));
    }

    @Override
    public void processStopped(@Nonnull final SparkShellProcess process) {
        if (process instanceof SparkLauncherSparkShellProcess) {
            final SparkLauncherSparkShellProcess launcherProcess = (SparkLauncherSparkShellProcess) process;
            launcherProcess.removeListener(this);
            users.remove(launcherProcess.getClientId());

            if (launcherProcess.getUsername() == null) {
                systemProcess = null;
            } else {
                setProcessForUser(launcherProcess.getUsername(), null);
            }
        }

        listeners.forEach(listener -> listener.processStopped(process));
    }

    @Override
    public void register(@Nonnull final String clientId, @Nonnull final RegistrationRequest registration) {
        final Optional<SparkLauncherSparkShellProcess> clientProcess = getProcessByClientId(clientId);
        if (clientProcess.isPresent()) {
            clientProcess.get().setHostname(registration.getHost());
            clientProcess.get().setPort(registration.getPort());
            clientProcess.get().setReady(true);
        } else if (systemProcess != null && clientId.equals(systemProcess.getClientId())) {
            systemProcess.setHostname(registration.getHost());
            systemProcess.setPort(registration.getPort());
            systemProcess.setReady(true);
        } else {
            log.warn("Tried to register unknown Spark Shell client: {}", clientId);
        }
    }

    @Override
    public void run(final ApplicationArguments args) throws Exception {
        executor.schedule(this::getSystemProcess, 0, TimeUnit.SECONDS);
    }

    @Override
    public void start(@Nonnull final String username) {
        if (!getProcessByUsername(username).isPresent()) {
            final SparkLauncherSparkShellProcess process;
            try {
                process = createProcessBuilder(username).build();
                process.addListener(this);
                process.setUsername(username);
                users.put(process.getClientId(), process.getClientSecret());
            } catch (final IOException e) {
                log.error("Failed to start Spark Shell process", e);
                throw new IllegalStateException("Failed to start Spark Shell process", e);
            }

            setProcessForUser(username, process);
            listeners.forEach(listener -> listener.processStarted(process));
        }
    }

    @Override
    public void updateProcess(@Nonnull final SparkShellProcess process) {
        if (process instanceof SparkLauncherSparkShellProcess) {
            final SparkLauncherSparkShellProcess changedProcess = (SparkLauncherSparkShellProcess) process;
            final Optional<SparkLauncherSparkShellProcess> existingProcess = getProcessByClientId(process.getClientId());

            if (existingProcess.isPresent()) {
                if (changedProcess.isReady()) {
                    final boolean setReady = !existingProcess.get().isReady();
                    existingProcess.get().setHostname(changedProcess.getHostname());
                    existingProcess.get().setPort(changedProcess.getPort());
                    if (setReady) {
                        existingProcess.get().setReady(false);
                    }
                }
            } else if (changedProcess.getUsername() != null) {
                setProcessForUser(changedProcess.getUsername(), changedProcess);
            } else {
                log.warn("Received Spark Shell process without username: {}", process);
            }
        } else {
            log.warn("Received invalid Spark Shell process: {}", process);
        }
    }

    @Override
    public void removeProcess(@Nonnull final String clientId) {
        final Optional<SparkLauncherSparkShellProcess> process = getProcessByClientId(clientId);

        if (process.isPresent()) {
            final SparkLauncherSparkShellProcess userProcess = process.get();
            if (userProcess.getUsername() != null) {
                setProcessForUser(userProcess.getUsername(), null);
            }
            stopProcess(userProcess);
        } else {
            log.warn("Spark Shell client not found for clientId: {}", clientId);
        }
    }

    /**
     * Creates a new Spark Shell client process builder for the specified user.
     *
     * @param username the name of the user
     * @return a process builder
     */
    @Nonnull
    protected SparkShellProcessBuilder createProcessBuilder(@Nullable final String username) {
        final SparkShellProcessBuilder builder = SparkShellProcessBuilder.create(clientProperties);
        if (kerberos != null && (username == null || !clientProperties.isProxyUser())) {
            builder.addSparkArg("--keytab", kerberos.getKeytabLocation());
            builder.addSparkArg("--principal", kerberos.getKerberosPrincipal());
        }
        return builder;
    }

    /**
     * Gets the Spark Shell process with the specified client identifier.
     *
     * @param clientId the client identifier
     * @return the Spark Shell process, if exists
     */
    @Nonnull
    protected abstract Optional<SparkLauncherSparkShellProcess> getProcessByClientId(@Nonnull String clientId);

    /**
     * Gets the Spark Shell process for the specified user.
     *
     * @param username the user who will be using the Spark Shell process
     * @return the Spark Shell process, if exists
     */
    @Nonnull
    protected abstract Optional<SparkLauncherSparkShellProcess> getProcessByUsername(@Nonnull String username);

    /**
     * Sets the process for the specified user.
     *
     * @param username the user's name
     * @param process  the Spark Shell client process, or {@code null} if the process terminated
     */
    protected abstract void setProcessForUser(@Nonnull String username, @Nullable SparkLauncherSparkShellProcess process);

    /**
     * Cleans up resources used by this process manager.
     *
     * @throws InterruptedException if interrupted
     */
    @PreDestroy
    void shutdown() throws InterruptedException {
        // Stop the system process
        if (systemProcess != null) {
            systemProcess.destroy();
        }

        // Stop user processes
        getProcesses().forEach(process -> {
            if (process instanceof SparkLauncherSparkShellProcess) {
                ((SparkLauncherSparkShellProcess) process).destroy();
            }
        });

        // Stop the executor
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    /**
     * Stops the specified Spark Shell process.
     *
     * @param process the process to be stopped
     */
    private void stopProcess(@Nonnull final SparkLauncherSparkShellProcess process) {
        process.stop();
        executor.schedule(process::destroy, clientProperties.getClientTimeout(), TimeUnit.SECONDS);
    }
}
