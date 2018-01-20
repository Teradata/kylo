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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SetSavepointTest {

    private TestRunner runner;
    private SavepointController service;
    private SavepointProvider provider;
    private String savepointId;

    @Before
    public void setup() throws InitializationException {
        runner = TestRunners.newTestRunner(SetSavepoint.class);

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
        runner.setProperty(SetSavepoint.EXPIRATION_DURATION, "24h");
        runner.setProperty(SetSavepoint.SAVEPOINT_ID, "${savepointid}");

        this.savepointId = "sp1";
        runner.setThreadCount(1);

        // Setup existing cache entry.
        provider = service.getProvider();
    }

    private void enqueue(String savepointId) {
        final Map<String, String> props1 = new HashMap<>();
        props1.put("savepointid", savepointId);
        runner.enqueue(new byte[]{}, props1);
    }

    @Test
    public void testExpired() throws InitializationException, IOException, InvalidLockException, InterruptedException {

        // Set expiration to 2s
        runner.setProperty(SetSavepoint.EXPIRATION_DURATION, "2s");
        enqueue("sp1");

        final TestIteration testIteration = new TestIteration();
        testIteration.expectedQueueSize = 1;
        testIteration.expectedRetry.add("1");
        testIteration.run();

        // Expect it to be expired queue
        Thread.sleep(3000L);
        testIteration.expectedQueueSize = 0;
        testIteration.expectedExpired.add("1");
        testIteration.run();
    }

    @Test
    public void testRetryCount() throws InitializationException, IOException, InvalidLockException, InterruptedException, InvalidSetpointException {

        // Set expiration to 2s
        enqueue("sp1");

        final TestIteration testIteration = new TestIteration();
        testIteration.expectedQueueSize = 1;
        testIteration.expectedRetry.add("1");
        testIteration.run();

        for (int i=1; i <= 5; i++) {

            Lock lock = provider.lock(savepointId);
            provider.retry(savepointId, lock);
            provider.unlock(lock);
            testIteration.doNotClearRunnerState();
            testIteration.expectedQueueSize = 1;
            testIteration.expectedRetry.add("1");
            testIteration.run();

            String retries = runner.getFlowFilesForRelationship(SetSavepoint.REL_TRY).get(0).getAttribute(SetSavepoint.SAVEPOINT_RETRY_COUNT);
            assertEquals(String.valueOf(i), retries);
            runner.clearTransferState();
        }
    }

    @Test
    public void testRetry() throws InitializationException, IOException, InvalidLockException, InvalidSetpointException {

        enqueue("sp1");

        final TestIteration testIteration = new TestIteration();
        testIteration.expectedQueueSize = 1;
        testIteration.expectedRetry.add("1");
        testIteration.run();

        Lock lock = provider.lock(savepointId);
        assertNotNull("Expecting lock", lock);
        provider.retry(savepointId, lock);
        provider.unlock(lock);

        testIteration.expectedRetry.add("1");
        testIteration.expectedQueueSize = 1;
        testIteration.run();

        lock = provider.lock(savepointId);
        assertNotNull("Expecting lock", lock);
        provider.release(savepointId, lock);
        provider.unlock(lock);

        testIteration.expectedReleased.add("1");
        testIteration.expectedQueueSize = 0;
        testIteration.run();

        String retryCount = testIteration.released.get(0).getAttribute(SetSavepoint.SAVEPOINT_RETRY_COUNT);
        assertEquals("Expecting retry count of 1", "1", retryCount);
    }

    @Test
    public void testWaitRelease() throws InitializationException, IOException, InvalidLockException, InvalidSetpointException {

        enqueue("sp1");

        final TestIteration testIteration = new TestIteration();
        testIteration.expectedQueueSize = 1;
        testIteration.expectedRetry.add("1");
        testIteration.run();

        testIteration.expectedQueueSize = 1;
        testIteration.run();

        Lock lock = provider.lock(savepointId);
        assertNotNull("Expecting lock", lock);
        provider.release(savepointId, lock);
        provider.unlock(lock);

        testIteration.expectedQueueSize = 0;
        testIteration.expectedReleased.add("1");
        testIteration.run();
    }

    @Test
    /**
     * Expect arrival on new setpoint will release prior setpoints
     */
    public void testReleaseOn2ndStep() throws InitializationException, IOException, InvalidLockException, InvalidSetpointException {

        // Simulate an existing setpoint by another processor
        Lock lock = provider.lock(savepointId);
        provider.register(savepointId, "processor1", "flowFile1", lock);
        provider.unlock(lock);

        enqueue(savepointId);

        final TestIteration testIteration = new TestIteration();
        testIteration.expectedQueueSize = 1;
        testIteration.expectedRetry.add("1");
        testIteration.run();

        SavepointEntry entry = provider.lookupEntry(savepointId);
        Assert.assertEquals(SavepointEntry.SavePointState.RELEASE, entry.getState("processor1"));
    }

    private class TestIteration {

        final List<MockFlowFile> released = new ArrayList<>();
        final List<MockFlowFile> retry = new ArrayList<>();
        final List<MockFlowFile> failed = new ArrayList<>();
        final List<MockFlowFile> expired = new ArrayList<>();

        final List<String> expectedReleased = new ArrayList<>();
        final List<String> expectedRetry = new ArrayList<>();
        final List<String> expectedFailed = new ArrayList<>();
        final List<String> expectedExpired = new ArrayList<>();
        int expectedQueueSize = 0;
        boolean clearRunnerState = true;

        void doNotClearRunnerState()  {
            clearRunnerState = false;
        }

        void run() {
            released.clear();
            retry.clear();
            failed.clear();
            expired.clear();

            runner.run();

            released.addAll(runner.getFlowFilesForRelationship(SetSavepoint.REL_RELEASE));
            retry.addAll(runner.getFlowFilesForRelationship(SetSavepoint.REL_TRY));
            failed.addAll(runner.getFlowFilesForRelationship(SetSavepoint.REL_FAILURE));
            expired.addAll(runner.getFlowFilesForRelationship(SetSavepoint.REL_EXPIRED));
            int queueSize = runner.getQueueSize().getObjectCount();

            assertEquals(expectedQueueSize, queueSize);
            assertEquals(expectedReleased.size(), released.size());
            assertEquals(expectedRetry.size(), retry.size());
            assertEquals(expectedFailed.size(), failed.size());
            assertEquals(expectedExpired.size(), expired.size());

            if (clearRunnerState) {
                runner.clearTransferState();
            }

            expectedQueueSize = 0;
            expectedReleased.clear();
            expectedRetry.clear();
            expectedFailed.clear();
            expectedExpired.clear();
        }
    }


}
