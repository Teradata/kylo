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


import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobRequest;
import com.thinkbiganalytics.kylo.spark.rest.model.job.SparkJobResponse;
import com.thinkbiganalytics.spark.shell.SparkShellProcess;
import com.thinkbiganalytics.spark.shell.SparkShellRestClient;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class BatchJobSupplierTest {

    /**
     * Mock response with <i>ERROR</i> status
     */
    private static final SparkJobResponse ERROR_RESPONSE;

    /**
     * Mock response with <i>PENDING</i> status
     */
    private static final SparkJobResponse PENDING_RESPONSE;

    /**
     * Mock response with <i>SUCCESS</i> status
     */
    private static final SparkJobResponse SUCCESS_RESPONSE;

    static {
        ERROR_RESPONSE = new SparkJobResponse();
        ERROR_RESPONSE.setId("mock-error");
        ERROR_RESPONSE.setStatus(SparkJobResponse.Status.ERROR);

        PENDING_RESPONSE = new SparkJobResponse();
        PENDING_RESPONSE.setId("mock-pending");
        PENDING_RESPONSE.setStatus(SparkJobResponse.Status.PENDING);

        SUCCESS_RESPONSE = new SparkJobResponse();
        SUCCESS_RESPONSE.setId("mock-success");
        SUCCESS_RESPONSE.setStatus(SparkJobResponse.Status.SUCCESS);
    }

    /**
     * Mock Spark Shell process
     */
    private final SparkShellProcess process = Mockito.mock(SparkShellProcess.class);

    /**
     * Mock Spark job request
     */
    private final SparkJobRequest request = new SparkJobRequest();

    /**
     * Mock Spark Shell REST client
     */
    private final SparkShellRestClient restClient = Mockito.mock(SparkShellRestClient.class);

    /**
     * Total number of milliseconds spent sleeping
     */
    private final AtomicLong sleepMillis = new AtomicLong();

    /**
     * Verify handling <i>ERROR</i> responses.
     */
    @Test
    public void testError() {
        // Mock rest client
        Mockito.when(restClient.createJob(process, request)).thenReturn(ERROR_RESPONSE);

        // Test success response
        final SparkJobResponse response = new MockBatchJob().get();
        Assert.assertEquals(ERROR_RESPONSE, response);
        Assert.assertEquals(0, sleepMillis.get());
    }

    /**
     * Verify handling <i>PENDING</i> responses.
     */
    @Test
    public void testPending() {
        // Mock rest client
        Mockito.when(restClient.createJob(process, request)).thenReturn(PENDING_RESPONSE);
        Mockito.when(restClient.getJobResult(process, PENDING_RESPONSE.getId())).thenReturn(Optional.of(PENDING_RESPONSE)).thenReturn(Optional.of(SUCCESS_RESPONSE));

        // Test success response
        final MockBatchJob job = new MockBatchJob();
        job.setPollInterval(200, TimeUnit.MILLISECONDS);

        final SparkJobResponse response = job.get();
        Assert.assertEquals(SUCCESS_RESPONSE, response);
        Assert.assertEquals(400, sleepMillis.get());
    }

    /**
     * Verify handling <i>SUCCESS</i> responses.
     */
    @Test
    public void testSuccess() {
        // Mock rest client
        Mockito.when(restClient.createJob(process, request)).thenReturn(SUCCESS_RESPONSE);

        // Test success response
        final SparkJobResponse response = new MockBatchJob().get();
        Assert.assertEquals(SUCCESS_RESPONSE, response);
        Assert.assertEquals(0, sleepMillis.get());
    }

    /**
     * Mock {@code BatchJobSupplier} for testing.
     */
    private class MockBatchJob extends BatchJobSupplier {

        public MockBatchJob() {
            super(request, process, restClient);
        }

        @Override
        protected void sleep(final long millis) {
            sleepMillis.addAndGet(millis);
        }
    }
}
