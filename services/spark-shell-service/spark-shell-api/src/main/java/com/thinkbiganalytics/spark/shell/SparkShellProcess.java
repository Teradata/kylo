package com.thinkbiganalytics.spark.shell;

import javax.annotation.Nonnull;

/**
 * A Spark Shell client running in a separate JVM.
 */
public interface SparkShellProcess {

    /**
     * Gets the hostname for communicating with this Spark Shell client.
     *
     * @return the hostname
     * @throws IllegalStateException if the Spark Shell client is not ready to receive commands
     */
    @Nonnull
    String getHostname();

    /**
     * Gets the port number fo communicating with this Spark Shell client.
     *
     * @return the port number
     * @throws IllegalStateException if the Spark Shell client is not ready to receive commands
     */
    int getPort();
}
