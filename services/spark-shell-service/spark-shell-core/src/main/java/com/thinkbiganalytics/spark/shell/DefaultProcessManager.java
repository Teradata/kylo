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
import com.thinkbiganalytics.spark.conf.model.KerberosSparkProperties;
import com.thinkbiganalytics.spark.conf.model.SparkShellProperties;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Manages a single Kylo Spark Shell process that is shared across multiple users.
 */
public class DefaultProcessManager extends AbstractProcessManager {

    /**
     * The Kylo Spark Shell process
     */
    @Nullable
    private SparkLauncherSparkShellProcess userProcess;

    /**
     * Constructs a {@code DefaultProcessManager} with the specified configuration.
     *
     * @param properties         the Kylo Spark Shell configuration
     * @param kerberosProperties the Kerberos configuration for the Kylo Spark Shell client
     * @param users              the username / password mapping
     */
    public DefaultProcessManager(@Nonnull final SparkShellProperties properties, @Nonnull final KerberosSparkProperties kerberosProperties, @Nonnull final Properties users) {
        super(properties, kerberosProperties, users);
        Preconditions.checkArgument(!properties.isProxyUser(), "User impersonation is not supported by this userProcess manager.");
    }

    @Nonnull
    @Override
    public List<SparkShellProcess> getProcesses() {
        return (userProcess != null) ? Collections.singletonList(userProcess) : Collections.emptyList();
    }

    @Nonnull
    @Override
    protected Optional<SparkLauncherSparkShellProcess> getProcessByClientId(@Nonnull final String clientId) {
        return Optional.ofNullable(userProcess).filter(process -> clientId.equals(process.getClientId()));
    }

    @Nonnull
    @Override
    protected Optional<SparkLauncherSparkShellProcess> getProcessByUsername(@Nonnull final String username) {
        return Optional.ofNullable(userProcess);
    }

    @Override
    protected void setProcessForUser(@Nonnull final String username, @Nullable final SparkLauncherSparkShellProcess process) {
        this.userProcess = process;
    }
}
