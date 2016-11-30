package com.thinkbiganalytics.spark.shell;

import com.thinkbiganalytics.spark.conf.model.SparkShellProperties;
import com.thinkbiganalytics.spark.conf.model.SparkShellServerProperties;
import com.thinkbiganalytics.spark.rest.model.RegistrationRequest;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Provides information on an externally-managed Spark Shell server process.
 */
public class ServerProcessManager implements SparkShellProcessManager {

    /** Spark Shell process */
    private final SparkShellProcess process;

    /**
     * Constructs a {@code ServerProcessManager} with the specified properties.
     *
     * @param properties the Spark Shell properties
     */
    public ServerProcessManager(@Nonnull final SparkShellProperties properties) {
        process = new SparkShellProcess() {
            @Nonnull
            @Override
            public String getHostname() {
                return Optional.ofNullable(properties.getServer()).map(SparkShellServerProperties::getHost).orElse("localhost");
            }

            @Override
            public int getPort() {
                return Optional.ofNullable(properties.getServer()).map(SparkShellServerProperties::getPort).orElse(8450);
            }
        };
    }

    @Nonnull
    @Override
    public SparkShellProcess getProcessForUser(@Nonnull final String username) throws InterruptedException {
        return process;
    }

    @Nonnull
    @Override
    public SparkShellProcess getSystemProcess() throws InterruptedException {
        return process;
    }

    @Override
    public void register(@Nonnull final String clientId, @Nonnull final String clientSecret, @Nonnull final RegistrationRequest registration) {
        // ignored
    }

    @Override
    public void start(@Nonnull final String username) throws IllegalStateException {
        // ignored
    }
}
