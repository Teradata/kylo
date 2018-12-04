package com.thinkbiganalytics.kylo.spark.job.tasks;

/*-
 * #%L
 * Spark Shell Core
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

import com.thinkbiganalytics.kylo.spark.SparkException;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobRequest;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobResponse;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Uses the Spark Shell API to execute a batch job.
 */
public class BatchJobSupplier implements ChainableSupplier<SparkJobResponse> {

    /**
     * Spark Shell process
     */
    @Nonnull
    private final SparkShellProcess process;

    /**
     * Spark job request
     */
    @Nonnull
    private final SparkJobRequest request;

    /**
     * Spark Shell REST client
     */
    @Nonnull
    private final SparkShellRestClient restClient;

    /**
     * Interval in milliseconds to poll the REST client
     */
    private long pollInterval = 1000;

    /**
     * Constructs a {@code BatchJobSupplier}.
     */
    public BatchJobSupplier(@Nonnull final SparkJobRequest request, @Nonnull final SparkShellProcess process, @Nonnull final SparkShellRestClient restClient) {
        this.request = request;
        this.process = process;
        this.restClient = restClient;
    }

    @Nonnull
    @Override
    public SparkJobResponse get() {
        // Create the job
        SparkJobResponse response = restClient.createJob(process, request);

        // Wait for job to complete
        while (response.getStatus() == SparkJobResponse.Status.PENDING) {
            sleep(pollInterval);
            response = restClient.getJobResult(process, response.getId()).orElseThrow(NoSuchElementException::new);
        }

        return response;
    }

    /**
     * Sets the interval for polling the Spark Shell REST client.
     */
    public void setPollInterval(final long time, @Nonnull final TimeUnit unit) {
        this.pollInterval = unit.toMillis(time);
    }

    /**
     * Causes the thread to sleep for the specified number of milliseconds.
     *
     * @throws SparkException if interrupted
     */
    protected void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            throw new SparkException("job.cancelled");
        }
    }
}
