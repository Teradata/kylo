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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.nifi.core.api.spring.SpringContextService;
import com.thinkbiganalytics.nifi.savepoint.api.SavepointReplayEventConsumer;
import com.thinkbiganalytics.nifi.v2.core.savepoint.DistributedSavepointController;
import com.thinkbiganalytics.nifi.v2.core.savepoint.InvalidLockException;
import com.thinkbiganalytics.nifi.v2.core.savepoint.InvalidSetpointException;
import com.thinkbiganalytics.nifi.v2.core.savepoint.Lock;
import com.thinkbiganalytics.nifi.v2.core.savepoint.SavepointController;
import com.thinkbiganalytics.nifi.v2.core.savepoint.SavepointEntry;
import com.thinkbiganalytics.nifi.v2.core.savepoint.SavepointProvider;

import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.distributed.cache.client.DistributedMapCacheClient;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.BeansException;

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

    /**
     * Identifier for the spring context service
     */
    private static final String SPRING_SERVICE_IDENTIFIER = "springContextService";

    /**
     * Mock event consumer
     */
    private final SavepointReplayEventConsumer savepointReplayEventConsumer = Mockito.mock(SavepointReplayEventConsumer.class);

    @Before
    public void setup() throws InitializationException {
        runner = TestRunners.newTestRunner(SetSavepoint.class);

        final SpringContextService springService = new MockSpringContextService();

        DistributedMapCacheClient client = new MockDistributedMapCacheClient();

        final Map<String, String> clientProperties = new HashMap<>();
        runner.addControllerService("client", client, clientProperties);
        runner.enableControllerService(client);

        DistributedSavepointController service = new DistributedSavepointController();
        final Map<String, String> serviceProperties = new HashMap<>();
        serviceProperties.put("distributed-cache-service", "client");

        runner.addControllerService("service", service, serviceProperties);
        runner.addControllerService(SPRING_SERVICE_IDENTIFIER, springService);
        runner.setProperty(service, DistributedSavepointController.SPRING_SERVICE, SPRING_SERVICE_IDENTIFIER);
        runner.enableControllerService(springService);

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
    public void testDeserializer() throws  Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        String sValue = "{\"processorLists\":[{\"processorId\":\"d4de4ba6-94d5-3937-6198-312f10c44f04\",\"state\":\"WAIT\",\"flowFileId\":\"587d8f60-ae9b-41a2-8263-6807b801cbbe\"}]}";
        SavepointEntry s1 = ObjectMapperSerializer.deserialize(sValue,SavepointEntry.class);
        SavepointEntry s2 = objectMapper.readValue(sValue,SavepointEntry.class);
        int i = 0;

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

        for (int i = 1; i <= 5; i++) {

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
        provider.release(savepointId, lock, true);
        provider.unlock(lock);

        testIteration.expectedReleasedSuccess.add("1");
        testIteration.expectedQueueSize = 0;
        testIteration.run();

        String retryCount = testIteration.releasedSuccess.get(0).getAttribute(SetSavepoint.SAVEPOINT_RETRY_COUNT);
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
        provider.release(savepointId, lock, true);
        provider.unlock(lock);

        testIteration.expectedQueueSize = 0;
        testIteration.expectedReleasedSuccess.add("1");
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
        Assert.assertEquals(SavepointEntry.SavePointState.RELEASE_SUCCESS, entry.getState("processor1"));
    }


    @Test
    public void testReleaseFailure() throws InitializationException, IOException, InvalidLockException, InvalidSetpointException {

        enqueue("sp1");

        final TestIteration testIteration = new TestIteration();
        testIteration.expectedQueueSize = 1;
        testIteration.expectedRetry.add("1");
        testIteration.run();

        testIteration.expectedQueueSize = 1;
        testIteration.run();

        Lock lock = provider.lock(savepointId);
        assertNotNull("Expecting lock", lock);
        provider.release(savepointId, lock, false);
        provider.unlock(lock);

        testIteration.expectedQueueSize = 0;
        testIteration.expectedReleasedFailures.add("1");
        testIteration.run();

    }

    private class TestIteration {

        final List<MockFlowFile> releasedSuccess = new ArrayList<>();
        final List<MockFlowFile> releasedFailures = new ArrayList<>();
        final List<MockFlowFile> retry = new ArrayList<>();
        final List<MockFlowFile> failed = new ArrayList<>();
        final List<MockFlowFile> expired = new ArrayList<>();

        final List<String> expectedReleasedSuccess = new ArrayList<>();
        final List<String> expectedReleasedFailures = new ArrayList<>();
        final List<String> expectedRetry = new ArrayList<>();
        final List<String> expectedFailed = new ArrayList<>();
        final List<String> expectedExpired = new ArrayList<>();
        int expectedQueueSize = 0;
        boolean clearRunnerState = true;

        void doNotClearRunnerState() {
            clearRunnerState = false;
        }

        void run() {
            releasedSuccess.clear();
            releasedFailures.clear();
            retry.clear();
            failed.clear();
            expired.clear();

            runner.run();

            releasedSuccess.addAll(runner.getFlowFilesForRelationship(SetSavepoint.REL_RELEASE_SUCCESS));
            releasedFailures.addAll(runner.getFlowFilesForRelationship(SetSavepoint.REL_RELEASE_FAILURE));
            retry.addAll(runner.getFlowFilesForRelationship(SetSavepoint.REL_TRY));
            failed.addAll(runner.getFlowFilesForRelationship(SetSavepoint.REL_FAILURE));
            expired.addAll(runner.getFlowFilesForRelationship(SetSavepoint.REL_EXPIRED));
            int queueSize = runner.getQueueSize().getObjectCount();

            assertEquals(expectedQueueSize, queueSize);
            assertEquals(expectedReleasedSuccess.size(), releasedSuccess.size());
            assertEquals(expectedReleasedFailures.size(), releasedFailures.size());
            assertEquals(expectedRetry.size(), retry.size());
            assertEquals(expectedFailed.size(), failed.size());
            assertEquals(expectedExpired.size(), expired.size());

            if (clearRunnerState) {
                runner.clearTransferState();
            }

            expectedQueueSize = 0;
            expectedReleasedSuccess.clear();
            expectedReleasedFailures.clear();
            expectedRetry.clear();
            expectedFailed.clear();
            expectedExpired.clear();
        }
    }


    /**
     * A mock implementation of {@link SpringContextService}.
     */
    private class MockSpringContextService extends AbstractControllerService implements SpringContextService {

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getBean(Class<T> requiredType) throws BeansException {
            if (SavepointReplayEventConsumer.class.equals(requiredType)) {
                return (T) savepointReplayEventConsumer;
            }
            throw new IllegalArgumentException();
        }

        @Override
        public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
            throw new UnsupportedOperationException();
        }

        /* (non-Javadoc)
         * @see com.thinkbiganalytics.nifi.core.api.spring.SpringContextService#isInitialized()
         */
        @Override
        public boolean isInitialized() {
            return true;
        }
    }


}
