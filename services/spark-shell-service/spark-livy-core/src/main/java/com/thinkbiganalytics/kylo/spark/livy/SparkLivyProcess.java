package com.thinkbiganalytics.kylo.spark.livy;

/*-
 * #%L
 * kylo-spark-livy-core
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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

import com.google.common.util.concurrent.Uninterruptibles;
import com.thinkbiganalytics.kylo.spark.exceptions.LivyException;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

public class SparkLivyProcess implements SparkShellProcess {

    private static final XLogger logger = XLoggerFactory.getXLogger(SparkLivyProcess.class);

    private UUID id;

    final private String hostname;

    final private int port;

    private transient CountDownLatch startSignal;

    /**
     * Expected time for client to be ready, in milliseconds
     */
    private final long waitForStart;

    /**
     * Expected point in time for the client to be ready
     */
    private long readyTime;

    private SparkLivyProcess(String hostname, Integer port, Long waitForStart) {
        this.hostname = hostname;
        this.port = port;
        this.id = UUID.randomUUID();

        this.waitForStart = waitForStart;
    }

    /**
     * Causes any thread calling this method to wait uninterruptibly for the Livy session to be ready.
     *
     * @return {@code true} if the client is ready, or {@code false} otherwise
     */
    public synchronized boolean waitForStart() {
        final long remaining = readyTime - DateTimeUtils.currentTimeMillis();
        if (remaining > 0 && startSignal != null ) {
            boolean started = Uninterruptibles.awaitUninterruptibly(startSignal, remaining, TimeUnit.MILLISECONDS);
            logger.debug("Finished waiting for start.  started='{}'", started);
            return started;
        } else {
            return true;
        }
    }

    public void newSession() {
        this.startSignal = new CountDownLatch(1);
        this.readyTime = DateTimeUtils.currentTimeMillis() + waitForStart;
    }

    /**
     * Indicates to all waiting threads that the Spark Shell client is ready to receive requests.
     */
    public void sessionStarted() {
        if (startSignal != null) {
            startSignal.countDown();
        }
    }


    @Nonnull
    @Override
    public String getClientId() {
        return id.toString();
    }

    @Nonnull
    @Override
    public String getHostname() {
        return hostname;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    public static SparkLivyProcess newInstance(String hostname, Integer port, Long timeout) {
        if (!StringUtils.isNotEmpty(hostname)) {
            throw new LivyException("Attempt to contact Livy server when Livy hostname not configured");
        }
        if (port == null || port <= 0) {
            throw new LivyException("Attempt to contact Livy server when Livy port not configured, or invalid");
        }
        return new SparkLivyProcess(hostname, port, timeout);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SparkLivyProcess that = (SparkLivyProcess) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SparkLivyProcess{");
        sb.append("id=").append(id);
        sb.append('}');
        return sb.toString();
    }
}
