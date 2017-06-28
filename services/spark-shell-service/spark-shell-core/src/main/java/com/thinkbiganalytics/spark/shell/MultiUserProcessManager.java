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

import com.google.common.collect.ImmutableList;
import com.thinkbiganalytics.spark.conf.model.KerberosSparkProperties;
import com.thinkbiganalytics.spark.conf.model.SparkShellProperties;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manages a separate Kylo Spark Shell process for each user.
 */
public class MultiUserProcessManager extends AbstractProcessManager {

    private static final Logger log = LoggerFactory.getLogger(MultiUserProcessManager.class);

    /**
     * Maps client identifiers to a Spark Shell process
     */
    @Nonnull
    private final Map<String, SparkLauncherSparkShellProcess> clientIdMap = new ConcurrentHashMap<>();

    /**
     * Timestamp when {@code kinit} should be run next
     */
    private long kerberosNextInit;

    /**
     * Maps usernames to a Spark Shell process
     */
    @Nonnull
    private final Map<String, SparkLauncherSparkShellProcess> userMap = new ConcurrentHashMap<>();

    /**
     * Suffix to append to usernames
     */
    @Nonnull
    private String usernameSuffix;

    /**
     * Constructs a {@code MultiUserProcessManager} with the specified configuration.
     *
     * @param sparkShellProperties the Kylo Spark Shell configuration
     * @param kerberosProperties   the Kerberos configuration for the Kylo Spark Shell client
     * @param users                the username / password mapping
     */
    public MultiUserProcessManager(@Nonnull final SparkShellProperties sparkShellProperties, @Nonnull final KerberosSparkProperties kerberosProperties, @Nonnull final Properties users) {
        super(sparkShellProperties, kerberosProperties, users);
        usernameSuffix = StringUtils.isNotEmpty(kerberosProperties.getRealm()) ? kerberosProperties.getRealm() : "";
    }

    @Nonnull
    @Override
    public List<SparkShellProcess> getProcesses() {
        return ImmutableList.copyOf(clientIdMap.values());
    }

    @Nonnull
    @Override
    protected SparkShellProcessBuilder createProcessBuilder(@Nullable final String username) {
        final SparkShellProcessBuilder builder = super.createProcessBuilder(username);
        if (clientProperties.isProxyUser() && username != null) {
            refreshKerberosTicket();
            builder.addSparkArg("--proxy-user", username + usernameSuffix);
        }
        return builder;
    }

    @Nonnull
    @Override
    protected Optional<SparkLauncherSparkShellProcess> getProcessByClientId(@Nonnull final String clientId) {
        return Optional.ofNullable(clientIdMap.get(clientId));
    }

    @Nonnull
    @Override
    protected Optional<SparkLauncherSparkShellProcess> getProcessByUsername(@Nonnull final String username) {
        return Optional.ofNullable(userMap.get(username));
    }

    @Override
    protected void setProcessForUser(@Nonnull final String username, @Nullable final SparkLauncherSparkShellProcess process) {
        if (process == null) {
            getProcessByUsername(username)
                .ifPresent(userProcess -> {
                    clientIdMap.remove(userProcess.getClientId());
                    userMap.remove(username);
                });
        } else {
            clientIdMap.put(process.getClientId(), process);
            userMap.put(username, process);
        }
    }

    /**
     * Calls kinit to request a new Kerberos ticket if the previous one is about to expire.
     */
    private void refreshKerberosTicket() {
        // Determine if a new ticket is needed
        if (kerberos == null || kerberos.getInitInterval() <= 0 || kerberosNextInit > DateTimeUtils.currentTimeMillis()) {
            return;
        }

        // Build executor
        final Executor executor = new DefaultExecutor();

        final ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();
        executor.setProcessDestroyer(processDestroyer);

        final Logger outputLogger = LoggerFactory.getLogger(getClass().getName() + ".kinit");
        final LoggerOutputStream outputStream = new LoggerOutputStream(outputLogger);
        final PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);

        final ExecuteWatchdog watchdog = new ExecuteWatchdog(TimeUnit.SECONDS.toMillis(kerberos.getInitTimeout()));
        executor.setWatchdog(watchdog);

        // Run kinit to acquire a new ticket
        final CommandLine command = new CommandLine("kinit").addArgument("-kt").addArgument(kerberos.getKeytabLocation()).addArgument(kerberos.getKerberosPrincipal());
        log.debug("Acquiring a new Kerberos ticket with command: {}", command);

        int exitCode;
        try {
            exitCode = executor.execute(command);
        } catch (final IOException e) {
            log.error("Failed to execute kinit", e);
            exitCode = -1;
        }

        // Record next time to acquire ticket
        if (!executor.isFailure(exitCode)) {
            kerberosNextInit = DateTimeUtils.currentTimeMillis() + TimeUnit.SECONDS.toMillis(kerberos.getInitInterval());
        } else {
            if (watchdog.killedProcess()) {
                log.error("Failed to acquire a Kerberos ticket within the allotted time: {}", kerberos.getInitTimeout());
            } else {
                log.error("Kinit exited with non-zero status: {}", exitCode);
            }
            kerberosNextInit = DateTimeUtils.currentTimeMillis() + TimeUnit.SECONDS.toMillis(kerberos.getRetryInterval());
            throw new IllegalStateException("Failed to acquire a Kerberos ticket");
        }
    }
}
