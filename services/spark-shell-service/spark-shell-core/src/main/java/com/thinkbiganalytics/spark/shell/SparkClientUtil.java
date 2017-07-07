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

import com.thinkbiganalytics.spark.shell.locator.CdhSparkHomeLocator;
import com.thinkbiganalytics.spark.shell.locator.EnvironmentSparkHomeLocator;
import com.thinkbiganalytics.spark.shell.locator.HdpSparkHomeLocator;
import com.thinkbiganalytics.spark.shell.locator.SparkHomeLocator;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility functions for using Spark.
 */
public final class SparkClientUtil {

    /**
     * Cached Spark major version number
     */
    @Nullable
    private static String majorVersion;

    /**
     * Cache Spark home location
     */
    @Nullable
    private static String sparkHome;

    /**
     * Gets the major version number from the Spark client.
     */
    public static String getMajorVersion() {
        if (majorVersion == null) {
            try {
                majorVersion = StringUtils.substringBefore(getVersion(), ".");
            } catch (final IOException e) {
                throw new IllegalStateException("Unable to determine Spark version");
            }
        }
        return majorVersion;
    }

    /**
     * Gets the Spark client home directory.
     */
    public static String getSparkHome() {
        if (sparkHome == null) {
            sparkHome = Stream.of(new EnvironmentSparkHomeLocator(), new CdhSparkHomeLocator(), new HdpSparkHomeLocator())
                .map(SparkHomeLocator::locate)
                .filter(file -> file.map(fp -> new File(fp, "bin/spark-submit")).map(File::exists).orElse(false))
                .map(Optional::get)
                .map(File::getAbsolutePath)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to determine Spark home"));
        }
        return sparkHome;
    }

    /**
     * Sets the Spark client home directory.
     *
     * @param sparkHome the directory
     */
    public static void setSparkHome(@Nonnull final String sparkHome) {
        final String previousValue = SparkClientUtil.sparkHome;
        if (previousValue == null || !previousValue.equals(sparkHome)) {
            SparkClientUtil.majorVersion = null;
            SparkClientUtil.sparkHome = sparkHome;
        }
    }

    /**
     * Gets the Spark version string by executing {@code spark-submit}.
     *
     * @throws IOException if the version string cannot be obtained
     */
    private static String getVersion() throws IOException {
        // Build spark-submit process
        final String sparkSubmitCommand = new StringJoiner(File.separator).add(getSparkHome()).add("bin").add("spark-submit").toString();
        final Process process = new ProcessBuilder()
            .command(sparkSubmitCommand, "--version")
            .redirectErrorStream(true)
            .start();

        // Wait for process to complete
        boolean exited;
        try {
            exited = process.waitFor(10, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            exited = !process.isAlive();
        }

        if (!exited) {
            throw new IOException("Timeout waiting for Spark version");
        }

        // Read stdout
        final byte[] bytes = new byte[1024];
        final int length = process.getInputStream().read(bytes);

        final String output = new String(bytes, 0, length, "UTF-8");
        final Matcher matcher = Pattern.compile("version ([\\d+.]+)").matcher(output);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalStateException("Unable to determine version from Spark Submit");
        }
    }

    /**
     * Instances of {@code SparkClientUtil} should not be constructed.
     *
     * @throws UnsupportedOperationException always
     */
    private SparkClientUtil() {
        throw new UnsupportedOperationException();
    }
}
