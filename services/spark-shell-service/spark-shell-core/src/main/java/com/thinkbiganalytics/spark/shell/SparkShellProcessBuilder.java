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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.thinkbiganalytics.KyloVersionUtil;
import com.thinkbiganalytics.spark.conf.model.SparkShellProperties;
import com.thinkbiganalytics.spark.shell.locator.CodeSourceAppLocator;
import com.thinkbiganalytics.spark.shell.locator.EnvironmentAppLocator;

import org.apache.spark.launcher.SparkAppHandle;
import org.apache.spark.launcher.SparkLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Builds Spark Shell client processes.
 */
@SuppressWarnings("WeakerAccess")
public class SparkShellProcessBuilder {

    private static final Logger log = LoggerFactory.getLogger(SparkShellProcessBuilder.class);

    /**
     * Name of the client id environment variable
     */
    private static final String CLIENT_ID = "KYLO_CLIENT_ID";

    /**
     * Name of the client secret environment variable
     */
    private static final String CLIENT_SECRET = "KYLO_CLIENT_SECRET";

    /**
     * Set of Spark argument switches (args requiring no value)
     */
    private static final Set<String> SWITCHES = Stream.of("--supervise", "-v").collect(Collectors.toSet());

    /**
     * Creates a new {@code SparkShellProcessBuilder} using the specified Spark Shell properties.
     *
     * @param clientProperties the Spark Shell properties
     * @return a new Spark Shell process builder
     */
    public static SparkShellProcessBuilder create(@Nonnull final SparkShellProperties clientProperties) {
        final SparkShellProcessBuilder builder = new SparkShellProcessBuilder()
            .clientTimeout(clientProperties.getClientTimeout(), TimeUnit.SECONDS)
            .deployMode(clientProperties.getDeployMode())
            .idleTimeout(clientProperties.getIdleTimeout(), TimeUnit.SECONDS)
            .master(clientProperties.getMaster())
            .portMax(clientProperties.getPortMax())
            .portMin(clientProperties.getPortMin())
            .verbose(clientProperties.isVerbose());

        if (clientProperties.getAppResource() != null) {
            builder.appResource(clientProperties.getAppResource());
        }
        if (clientProperties.getFiles() != null) {
            builder.addFiles(clientProperties.getFiles());
        }
        if (clientProperties.getJars() != null) {
            builder.addJars(clientProperties.getJars());
        }
        if (clientProperties.getJavaHome() != null) {
            builder.javaHome(clientProperties.getJavaHome());
        }
        if (clientProperties.getPropertiesFile() != null) {
            builder.propertiesFile(clientProperties.getPropertiesFile());
        }
        if (clientProperties.getRegistrationKeystorePath() != null) {
            builder.registrationKeystore(clientProperties.getRegistrationKeystorePath(), clientProperties.getRegistrationKeystorePassword());
        }
        if (clientProperties.getRegistrationUrl() != null) {
            builder.registrationUrl(clientProperties.getRegistrationUrl());
        }
        if (clientProperties.getSparkArgs() != null) {
            builder.addSparkArgs(clientProperties.getSparkArgs());
        }
        if (clientProperties.getSparkHome() != null) {
            builder.sparkHome(clientProperties.getSparkHome());
        }

        return builder;
    }

    /**
     * Main application resource
     */
    @Nullable
    private String appResource;

    /**
     * Client identifier
     */
    @Nonnull
    private final String clientId;

    /**
     * Client secret
     */
    @Nonnull
    private final String clientSecret;

    /**
     * Time in seconds to wait for a new process to finish starting up
     */
    private long clientTimeout = 120;

    /**
     * Spark deploy mode
     */
    @Nullable
    private String deployMode;

    /**
     * Time in seconds to wait for a user request
     */
    private long idleTimeout = 900;

    /**
     * Spark Submit launcher
     */
    @Nonnull
    private final SparkLauncher launcher;

    /**
     * Spark master
     */
    @Nullable
    private String master;

    /**
     * Custom Spark installation location
     */
    @Nullable
    private String sparkHome;

    /**
     * Maximum port that Spark Shell may listen on
     */
    private int portMax = 45999;

    /**
     * Minimum port that Spark Shell may listen on
     */
    private int portMin = 45000;

    /**
     * Password for keystore
     */
    @Nullable
    private String registrationKeystorePassword;

    /**
     * Path to keystore
     */
    @Nullable
    private String registrationKeystorePath;

    /**
     * Registration URL
     */
    @Nullable
    private String registrationUrl;

    /**
     * Constructs a {@code SparkShellProcessBuilder}.
     */
    public SparkShellProcessBuilder() {
        // Generate client id and secret
        clientId = UUID.randomUUID().toString();
        clientSecret = UUID.randomUUID().toString();

        // Create Spark Launcher
        final Map<String, String> env = ImmutableMap.<String, String>builder()
            .put(CLIENT_ID, clientId)
            .put(CLIENT_SECRET, clientSecret)
            .build();
        launcher = new SparkLauncher(env)
            .setConf("spark.driver.userClassPathFirst", "true")
            .setConf("spark.yarn.appMasterEnv." + CLIENT_ID, clientId)
            .setConf("spark.yarn.appMasterEnv." + CLIENT_SECRET, clientSecret)
            .setMainClass("com.thinkbiganalytics.spark.SparkShellApp");
    }

    /**
     * Adds a file to be submitted with the application.
     */
    @Nonnull
    public SparkShellProcessBuilder addFile(@Nonnull final String file) {
        launcher.addFile(file);
        return this;
    }

    /**
     * Adds the list of files to be submitted with the application.
     */
    @Nonnull
    public SparkShellProcessBuilder addFiles(@Nonnull final Iterable<String> files) {
        files.forEach(this::addFile);
        return this;
    }

    /**
     * Adds the comma-separated list of files to be submitted with the application.
     */
    @Nonnull
    public SparkShellProcessBuilder addFiles(@Nonnull final String files) {
        return addFiles(Arrays.asList(files.split(",")));
    }

    /**
     * Adds a jar file to be submitted with the application.
     */
    @Nonnull
    public SparkShellProcessBuilder addJar(@Nonnull final String jar) {
        launcher.addJar(jar);
        return this;
    }

    /**
     * Adds the list of jar files to be submitted with the application.
     */
    @Nonnull
    public SparkShellProcessBuilder addJars(@Nonnull final Iterable<String> jars) {
        jars.forEach(this::addJar);
        return this;
    }

    /**
     * Adds the comma-separated list of jar files to be submitted with the application.
     */
    @Nonnull
    public SparkShellProcessBuilder addJars(@Nonnull final String jars) {
        return addJars(Arrays.asList(jars.split(",")));
    }

    /**
     * Adds a no-value argument to the Spark invocation.
     */
    @Nonnull
    public SparkShellProcessBuilder addSparkArg(@Nonnull final String arg) {
        launcher.addSparkArg(arg);
        return this;
    }

    /**
     * Adds an argument with a value to the Spark invocation.
     */
    @Nonnull
    public SparkShellProcessBuilder addSparkArg(@Nonnull final String name, @Nonnull final String value) {
        launcher.addSparkArg(name, value);
        return this;
    }

    /**
     * Adds the specified arguments to the Spark invocation.
     */
    @Nonnull
    public SparkShellProcessBuilder addSparkArgs(@Nonnull final Iterable<String> args) {
        final PeekingIterator<String> iter = Iterators.peekingIterator(args.iterator());
        while (iter.hasNext()) {
            final String arg = iter.next();
            if (!SWITCHES.contains(arg) && iter.hasNext()) {
                addSparkArg(arg, iter.next());
            } else {
                addSparkArg(arg);
            }
        }
        return this;
    }

    /**
     * Adds the specified arguments to the Spark invocation.
     */
    @Nonnull
    public SparkShellProcessBuilder addSparkArgs(@Nonnull final String args) {
        return addSparkArgs(Arrays.asList(args.split(" ")));
    }

    /**
     * Sets the main application resource.
     */
    @Nonnull
    public SparkShellProcessBuilder appResource(@Nonnull final String resource) {
        appResource = Objects.requireNonNull(resource);
        return this;
    }

    /**
     * Sets the amount of time to wait for the Spark Shell process to finish starting up.
     *
     * @param timeout the duration
     * @param unit    the unit for the {@code duration}
     * @return this builder
     */
    @Nonnull
    public SparkShellProcessBuilder clientTimeout(final long timeout, @Nonnull final TimeUnit unit) {
        clientTimeout = unit.toSeconds(timeout);
        return this;
    }

    /**
     * Sets whether to launch a Spark Shell process locally ({@code client}) or on one of the working machines inside the cluster ({@code cluster}).
     *
     * @param mode the Spark deploy mode
     * @return this builder
     */
    @Nonnull
    public SparkShellProcessBuilder deployMode(@Nonnull final String mode) {
        deployMode = mode;
        launcher.setDeployMode(mode);
        return this;
    }

    /**
     * Sets the amount of time is seconds to wait for a user request before terminating a Spark Shell process.
     *
     * @param timeout the duration
     * @param unit    the unit for the {@code duration}
     * @return this builder
     */
    @Nonnull
    public SparkShellProcessBuilder idleTimeout(final long timeout, @Nonnull final TimeUnit unit) {
        idleTimeout = unit.toSeconds(timeout);
        return this;
    }

    /**
     * Sets the {@code JAVA_HOME} for launching the Spark application.
     */
    @Nonnull
    public SparkShellProcessBuilder javaHome(@Nonnull final String javaHome) {
        launcher.setJavaHome(javaHome);
        return this;
    }

    /**
     * Sets whether to run Spark executors locally ({@code local}) or inside a YARN cluster ({@code yarn}).
     *
     * @param master the Spark master
     * @return this builder
     */
    @Nonnull
    public SparkShellProcessBuilder master(@Nonnull final String master) {
        this.master = master;
        launcher.setMaster(master);
        return this;
    }

    /**
     * Sets the maximum port number that a Spark Shell process may listen on.
     *
     * @param port the maximum port number
     * @return this builder
     */
    @Nonnull
    public SparkShellProcessBuilder portMax(final int port) {
        portMax = port;
        return this;
    }

    /**
     * Sets the minimum port number that a Spark Shell process may listen on.
     *
     * @param port the minimum port number
     * @return this builder
     */
    @Nonnull
    public SparkShellProcessBuilder portMin(final int port) {
        portMin = port;
        return this;
    }

    /**
     * Sets a custom properties file with Spark configuration for the application.
     */
    @Nonnull
    public SparkShellProcessBuilder propertiesFile(@Nonnull final String file) {
        launcher.setPropertiesFile(file);
        return this;
    }

    /**
     * Sets the keystore path and password to use to access the registration URL.
     */
    @Nonnull
    public SparkShellProcessBuilder registrationKeystore(@Nonnull final String path, @Nullable final String password) {
        registrationKeystorePath = Objects.requireNonNull(path);
        registrationKeystorePassword = password;
        return this;
    }

    /**
     * Sets the registration URL.
     */
    @Nonnull
    public SparkShellProcessBuilder registrationUrl(@Nonnull final String url) {
        registrationUrl = Objects.requireNonNull(url);
        return this;
    }

    /**
     * Sets a custom Spark installation location for the application.
     */
    @Nonnull
    public SparkShellProcessBuilder sparkHome(@Nonnull final String sparkHome) {
        this.sparkHome = Objects.requireNonNull(sparkHome);
        launcher.setSparkHome(sparkHome);
        return this;
    }

    /**
     * Enables verbose reporting for Spark Submit.
     */
    @Nonnull
    public SparkShellProcessBuilder verbose(final boolean verbose) {
        launcher.setVerbose(verbose);
        return this;
    }

    /**
     * Creates a new Spark Shell process.
     *
     * @throws IllegalStateException if additional configuration is required
     * @throws IOException           if the process cannot be created
     */
    @Nonnull
    public SparkLauncherSparkShellProcess build() throws IOException {
        launcher.addAppArgs("--idle-timeout", Long.toString(idleTimeout), "--port-max", Integer.toString(portMax), "--port-min", Integer.toString(portMin), "--server-url", getRegistrationUrl());
        launcher.setAppResource(getAppResource());

        if (registrationKeystorePath != null) {
            if (Objects.equals(master, "yarn") && Objects.equals(deployMode, "cluster")) {
                launcher.addAppArgs("--server-keystore-path", new File(registrationKeystorePath).getName());
                launcher.addFile(registrationKeystorePath);
            } else {
                launcher.addAppArgs("--server-keystore-path", registrationKeystorePath);
            }
            if (registrationKeystorePassword != null) {
                launcher.addAppArgs("--server-keystore-password", registrationKeystorePassword);
            }
        }
        if (sparkHome == null) {
            launcher.setSparkHome(getSparkHome());
        }

        final SparkAppHandle sparkAppHandle = launcher.startApplication();
        return new SparkLauncherSparkShellProcess(sparkAppHandle, clientId, clientSecret, clientTimeout, TimeUnit.SECONDS);
    }

    /**
     * Determines the location of the kylo-spark-shell-client jar.
     *
     * @throws IllegalStateException if the jar cannot be located
     */
    String getAppResource() {
        if (appResource == null) {
            final String resourceName = "kylo-spark-shell-client-v" + getSparkVersion() + "-" + KyloVersionUtil.getBuildVersion() + ".jar";
            appResource = Stream.of(new EnvironmentAppLocator(), new CodeSourceAppLocator())
                .map(locator -> locator.locate(resourceName))
                .map(file -> file.filter(File::exists).map(File::getPath).orElse(null))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to find the Kylo Spark Shell Client app. Either set it explicitly or use the KYLO_SERVICES_HOME environment variable."));
        }
        return appResource;
    }

    /**
     * Gets the registration URL.
     *
     * @throws IllegalStateException if the registration URL has not been set
     */
    String getRegistrationUrl() {
        if (registrationUrl == null) {
            log.error("Unable to determine registration server. Set the registration server explicitly.");
            throw new IllegalStateException("Hostname is not available");
        }
        return registrationUrl;
    }

    /**
     * Gets the path to the Spark home.
     *
     * @throws IllegalStateException if the Spark home directory cannot be found
     */
    String getSparkHome() {
        if (sparkHome == null) {
            try {
                sparkHome = SparkClientUtil.getSparkHome();
            } catch (final IllegalStateException e) {
                log.error("Unable to determine Spark home", e);
                throw new IllegalStateException("Unable to determine Spark home. Either set is explicitly or use the SPARK_HOME environment variable.");
            }
        }
        return sparkHome;
    }

    /**
     * Gets the Spark major version number.
     *
     * @throws IllegalStateException if the Spark version cannot be determined
     */
    String getSparkVersion() {
        // Set the custom Spark home
        if (sparkHome != null) {
            SparkClientUtil.setSparkHome(sparkHome);
        }

        // Get major version number
        try {
            return SparkClientUtil.getMajorVersion();
        } catch (final IllegalStateException e) {
            log.error("Unable to determine Spark version", e);
            throw new IllegalStateException("Unable to determine Spark version. Set the app resource explicitly.");
        }
    }
}
