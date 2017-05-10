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

import java.util.Collection;
import java.util.List;

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
    private static final SparkJobserverService sparkJobserverService = new SparkJobserverService();
    private static final String sparkJobserverUrl = "http://localhost:8089";
    private static final String syncTimeout = "600";

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

        String expected = "'Spark Jobserver Service' validated against 'sparkJobServerService' is invalid because Controller Service is not valid: 'Jobserver URL' is invalid because Jobserver URL is required";
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
     * Shutdown the runner
     */
    @After
    public void shutdown() {
        runner.shutdown();
    }
}