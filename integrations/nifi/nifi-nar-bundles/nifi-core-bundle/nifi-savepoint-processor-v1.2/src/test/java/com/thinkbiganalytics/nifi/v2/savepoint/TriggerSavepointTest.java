package com.thinkbiganalytics.nifi.v2.savepoint;

/*-
 * #%L
 * kylo-nifi-core-processors
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

import org.apache.nifi.distributed.cache.client.DistributedMapCacheClient;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TriggerSavepointTest {

    private TestRunner runner;
    private SavepointController service;
    // Setup existing cache entry.
    private SavepointProvider provider;

    @Before
    public void setup() throws InitializationException {
        runner = TestRunners.newTestRunner(TriggerSavepoint.class);


        DistributedMapCacheClient client = new MockDistributedMapCacheClient();

        final Map<String, String> clientProperties = new HashMap<>();
        runner.addControllerService("client", client, clientProperties);
        runner.enableControllerService(client);

        DistributedSavepointController service= new DistributedSavepointController();
        final Map<String, String> serviceProperties = new HashMap<>();
        serviceProperties.put("distributed-cache-service", "client");

        runner.addControllerService("service", service, serviceProperties);
        runner.enableControllerService(service);
        runner.setProperty(SetSavepoint.SAVEPOINT_SERVICE, "service");

        runner.setProperty(TriggerSavepoint.SAVEPOINT_ID, "${savepointid}");
        runner.setProperty(TriggerSavepoint.BEHAVIOR, TriggerSavepoint.RETRY);

        this.provider = service.getProvider();

    }

    @Test
    public void testRetryPenalty() throws InitializationException, IOException, InvalidLockException, InterruptedException {

        final String savepointId = "sp1";
        final Map<String, String> props1 = new HashMap<>();
        props1.put("savepointid", savepointId);
        runner.enqueue(new byte[]{}, props1);

        TestIteration iteration = new TestIteration();
        iteration.expectedPenalizedCount = 1;
        iteration.expectedQueueSize = 1;
        iteration.run();
    }

    @Test
    public void testRetry2ndAttempt() throws InitializationException, IOException, InvalidLockException, InterruptedException {

        final String savepointId = "sp1";
        final Map<String, String> props1 = new HashMap<>();
        props1.put("savepointid", savepointId);
        props1.put(TriggerSavepoint.SAVEPOINT_RETRY_MARKER, "1");
        runner.enqueue(new byte[]{}, props1);

        Lock lock = provider.lock(savepointId);
        provider.register(savepointId, "p1", "flowFile1", lock);
        provider.unlock(lock);

        TestIteration iteration = new TestIteration();
        iteration.expectedPenalizedCount = 0;
        iteration.expectedQueueSize = 0;
        iteration.expectedSuccess.add("1");
        iteration.run();
    }

    @Test
    public void testRetryMaxAttempts() throws InitializationException, IOException, InvalidLockException, InterruptedException {

        final String savepointId = "sp1";
        final Map<String, String> props1 = new HashMap<>();
        props1.put("savepointid", savepointId);
        props1.put(TriggerSavepoint.SAVEPOINT_RETRY_MARKER, "1");
        props1.put(SetSavepoint.SAVEPOINT_RETRY_COUNT, "11");
        runner.enqueue(new byte[]{}, props1);

        TestIteration iteration = new TestIteration();
        iteration.expectedPenalizedCount = 0;
        iteration.expectedQueueSize = 0;
        iteration.expectedMaxRetries.add("1");
        iteration.run();
    }

    @Test
    public void testReleaseSuccess() throws InitializationException, IOException, InvalidLockException, InterruptedException {

        final String savepointId = "sp1";
        final Map<String, String> props1 = new HashMap<>();
        props1.put("savepointid", savepointId);
        runner.enqueue(new byte[]{}, props1);
        runner.setProperty(TriggerSavepoint.BEHAVIOR, TriggerSavepoint.RELEASE);

        Lock lock = provider.lock(savepointId);
        provider.register(savepointId, "p1", "flowFile1", lock);
        provider.unlock(lock);

        TestIteration iteration = new TestIteration();
        iteration.expectedPenalizedCount = 0;
        iteration.expectedSuccess.add("1");
        iteration.run();
    }

    @Test
    public void testFailBehavior() throws InitializationException, IOException, InvalidLockException, InterruptedException {

        final String savepointId = "sp1";
        final Map<String, String> props1 = new HashMap<>();
        props1.put("savepointid", savepointId);
        runner.enqueue(new byte[]{}, props1);

        runner.setProperty(TriggerSavepoint.BEHAVIOR, TriggerSavepoint.FAIL);

        Lock lock = provider.lock(savepointId);
        provider.register(savepointId, "p1", "flowFile1", lock);
        provider.unlock(lock);

        TestIteration iteration = new TestIteration();
        iteration.expectedPenalizedCount = 0;
        iteration.expectedFailed.add("1");
        iteration.run();
    }

    @Test
    public void testFailureRetriesExceeded() throws InitializationException, IOException, InvalidLockException, InterruptedException {

        final String savepointId = "sp1";
        final Map<String, String> props1 = new HashMap<>();
        props1.put("savepointid", savepointId);
        props1.put(TriggerSavepoint.SAVEPOINT_TRIGGER_FAILURE_COUNT, String.valueOf(TriggerSavepoint.MAX_FAILURES_ALLOWED+1));
        props1.put(TriggerSavepoint.SAVEPOINT_RETRY_MARKER, "1");
        runner.enqueue(new byte[]{}, props1);

        TestIteration iteration = new TestIteration();
        iteration.expectedFailed.add("1");
        iteration.run();
    }

    private class TestIteration {

        final List<MockFlowFile> success = new ArrayList<>();
        final List<MockFlowFile> failed = new ArrayList<>();
        final List<MockFlowFile> maxRetries = new ArrayList<>();

        final List<String> expectedSuccess = new ArrayList<>();
        final List<String> expectedFailed = new ArrayList<>();
        final List<String> expectedMaxRetries = new ArrayList<>();
        int expectedQueueSize = 0;
        int expectedPenalizedCount = 0;

        void run() {
            success.clear();
            failed.clear();
            maxRetries.clear();

            runner.run();

            success.addAll(runner.getFlowFilesForRelationship(TriggerSavepoint.REL_SUCCESS));
            failed.addAll(runner.getFlowFilesForRelationship(TriggerSavepoint.REL_FAILURE));
            maxRetries.addAll(runner.getFlowFilesForRelationship(TriggerSavepoint.REL_MAX_RETRIES_EXCEEDED));

            int queueSize = runner.getQueueSize().getObjectCount();
            assertEquals(expectedPenalizedCount, runner.getPenalizedFlowFiles().size());
            assertEquals(expectedQueueSize, queueSize);
            assertEquals(expectedSuccess.size(), success.size());
            assertEquals(expectedFailed.size(), failed.size());
            assertEquals(expectedMaxRetries.size(), maxRetries.size());

            runner.clearTransferState();
            expectedSuccess.clear();
            expectedFailed.clear();
            expectedMaxRetries.clear();
            expectedPenalizedCount = 0;
            expectedPenalizedCount = 0;
        }
    }
}
