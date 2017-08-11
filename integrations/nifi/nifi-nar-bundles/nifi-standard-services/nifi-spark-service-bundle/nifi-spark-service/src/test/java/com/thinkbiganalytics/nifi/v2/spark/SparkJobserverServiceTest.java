package com.thinkbiganalytics.nifi.v2.spark;

/*-
 * #%L
 * thinkbig-nifi-core-service
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

import com.bluebreezecf.tools.sparkjobserver.api.ISparkJobServerClient;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.util.MockProcessContext;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class SparkJobserverServiceTest {

    /**
     * Identifier for the spark jobserver service
     */
    private static final String SPARK_JOBSERVER_SERVICE_IDENTIFIER = "sparkJobServerService";

    /**
     * Processor property for the cleanup event service
     */
    private static final PropertyDescriptor SPARK_JOBSERVER_SERVICE_PROPERTY = new PropertyDescriptor.Builder()
        .name("Spark Jobserver Service")
        .description("Provides long running spark contexts and shared RDDs using Spark jobserver.")
        .identifiesControllerService(SparkJobserverService.class)
        .required(true)
        .build();

    /**
     * Spark Jobserver service for testing
     */
    @Mock
    private ISparkJobServerClient client;

    @InjectMocks
    private final SparkJobserverService sparkJobserverService = new SparkJobserverService();
    private static final String sparkJobserverUrl = "http://localhost:8089";
    private static final String syncTimeout = "600";

    /**
     * Default Context Creation Properties
     */
    private static final String numExecutors = "1";
    private static final String memPerNode = "512m";
    private static final String numCPUCores = "2";
    private static final SparkContextType sparkContextType = SparkContextType.SPARK_CONTEXT;
    private static final int contextTimeout = 0;
    private static boolean async = false;

    /**
     * Test runner
     */
    private final TestRunner runner = TestRunners.newTestRunner(new AbstractProcessor() {
        @Override
        protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
            return ImmutableList.of(SPARK_JOBSERVER_SERVICE_PROPERTY);
        }

        @Override
        public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
            // do nothing
        }
    });

    /**
     * Initialize instance variables.
     */
    @Before
    public void setUp() throws Exception {

        // Setup test runner
        runner.addControllerService(SPARK_JOBSERVER_SERVICE_IDENTIFIER, sparkJobserverService);
        runner.setProperty(SPARK_JOBSERVER_SERVICE_PROPERTY, SPARK_JOBSERVER_SERVICE_IDENTIFIER);
        runner.setProperty(sparkJobserverService, SparkJobserverService.JOBSERVER_URL, sparkJobserverUrl);
        runner.setProperty(sparkJobserverService, SparkJobserverService.SYNC_TIMEOUT, syncTimeout);
        runner.enableControllerService(sparkJobserverService);
    }

    /**
     * Verify property validators.
     */
    @Test
    public void testValidators() {
        // Test with no properties
        runner.disableControllerService(sparkJobserverService);
        runner.setProperty(sparkJobserverService, SparkJobserverService.JOBSERVER_URL, (String) null);
        runner.setProperty(sparkJobserverService, SparkJobserverService.SYNC_TIMEOUT, (String) null);
        runner.enableControllerService(sparkJobserverService);
        runner.enqueue(new byte[0]);
        Collection<ValidationResult> results = ((MockProcessContext) runner.getProcessContext()).validate();
        Assert.assertEquals(1, results.size());

        String expected = "'Spark Jobserver Service' validated against 'sparkJobServerService' is invalid because Controller Service is not valid: 'Jobserver URL' is invalid because Jobserver URL is"
                          + " required";
        Assert.assertEquals(expected, Iterables.getOnlyElement(results).toString());

        // Test with valid properties
        runner.disableControllerService(sparkJobserverService);
        runner.setProperty(sparkJobserverService, SparkJobserverService.JOBSERVER_URL, sparkJobserverUrl);
        runner.setProperty(sparkJobserverService, SparkJobserverService.SYNC_TIMEOUT, syncTimeout);
        runner.enableControllerService(sparkJobserverService);
        runner.enqueue(new byte[0]);
        results = ((MockProcessContext) runner.getProcessContext()).validate();
        Assert.assertEquals(0, results.size());
    }

    /**
     * Verify creating Spark Context and then deleting the Context
     */
    @Test
    public void testContextCreationAndDeletion() throws Exception {

        // Test creating context
        String contextName = "testContextCreationAndDeletion";

        doReturn(true).when(client).createContext(anyString(), any());
        boolean created = sparkJobserverService.createContext(contextName, numExecutors, memPerNode, numCPUCores, sparkContextType, contextTimeout, async);
        Assert.assertTrue(created);

        doReturn(true).when(client).deleteContext(contextName);
        boolean deleted = sparkJobserverService.deleteContext(contextName);
        Assert.assertTrue(deleted);
    }

    /**
     * Verify two threads attempting to create a Spark Context at once
     */
    @Test
    public void testDuplicateContextCreation() throws Exception {
        // Test creating context
        String contextName = "testDuplicateContextCreation";

        doReturn(true).when(client).createContext(anyString(), any());
        CreateSparkContext createSparkContext1 = new CreateSparkContext(contextName, numExecutors, memPerNode, numCPUCores, sparkContextType, contextTimeout, async, sparkJobserverService);
        Thread thread1 = new Thread(createSparkContext1);

        CreateSparkContext createSparkContext2 = new CreateSparkContext(contextName, numExecutors, memPerNode, numCPUCores, sparkContextType, contextTimeout, async, sparkJobserverService);
        Thread thread2 = new Thread(createSparkContext2);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        doReturn(Collections.singletonList(contextName + "@" + sparkJobserverService.getUuid())).when(client).getContexts();
        boolean contextExists = sparkJobserverService.checkIfContextExists(contextName);

        Assert.assertTrue(contextExists);
    }

    /**
     * Verify creating a Spark Context which times out and gets deleted
     */
    @Test
    public void testContextTimeout() throws Exception {
        // Test creating context
        String contextName = "testContextTimeout";
        int contextTimeout = 1;

        doReturn(true).when(client).createContext(anyString(), any());
        sparkJobserverService.createContext(contextName, numExecutors, memPerNode, numCPUCores, sparkContextType, contextTimeout, async);
        doReturn(Collections.singletonList(contextName)).when(client).getContexts();
        doReturn(true).when(client).deleteContext(contextName);

        doReturn(new ArrayList<>()).when(client).getContexts();
        boolean contextExists = sparkJobserverService.checkIfContextExists(contextName);
        Assert.assertFalse(contextExists);
    }

    /**
     * Verify creating two Spark Contexts
     */
    @Test
    public void testMultipleContextCreation() throws Exception {
        // Test creating context
        String contextOneName = "testMultipleContextCreationOne";
        String contextTwoName = "testMultipleContextCreationTwo";

        doReturn(true).when(client).createContext(anyString(), any());

        CreateSparkContext createSparkContext1 = new CreateSparkContext(contextOneName, numExecutors, memPerNode, numCPUCores, sparkContextType, contextTimeout, async, sparkJobserverService);
        Thread thread1 = new Thread(createSparkContext1);

        CreateSparkContext createSparkContext2 = new CreateSparkContext(contextTwoName, numExecutors, memPerNode, numCPUCores, sparkContextType, contextTimeout, async, sparkJobserverService);
        Thread thread2 = new Thread(createSparkContext2);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        doReturn(Arrays.asList(contextOneName + "@" + sparkJobserverService.getUuid(), contextTwoName + "@" + sparkJobserverService.getUuid())).when(client).getContexts();

        boolean contextOneExists = sparkJobserverService.checkIfContextExists(contextOneName);
        boolean contextTwoExists = sparkJobserverService.checkIfContextExists(contextTwoName);

        doReturn(true).when(client).deleteContext(contextOneName);
        doReturn(true).when(client).deleteContext(contextTwoName);

        sparkJobserverService.deleteContext(contextOneName);
        sparkJobserverService.deleteContext(contextTwoName);

        Assert.assertTrue(contextOneExists && contextTwoExists);
    }

    /**
     * Shutdown the runner
     */
    @After
    public void shutdown() {
        runner.shutdown();
    }

}
