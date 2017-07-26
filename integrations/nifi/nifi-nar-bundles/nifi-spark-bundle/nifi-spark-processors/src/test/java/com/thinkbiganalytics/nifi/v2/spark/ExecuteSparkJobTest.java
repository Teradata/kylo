package com.thinkbiganalytics.nifi.v2.spark;

/*-
 * #%L
 * kylo-nifi-spark-processors
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

import com.thinkbiganalytics.nifi.core.api.metadata.KyloNiFiFlowProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;

import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.util.MockProcessContext;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class ExecuteSparkJobTest {

    /**
     * Identifier for the metadata provider service
     */
    private static final String METADATA_SERVICE_IDENTIFIER = "MockMetadataProviderService";

    /**
     * Test runner
     */
    private final TestRunner runner = TestRunners.newTestRunner(ExecuteSparkJob.class);

    /**
     * Initialize instance variables.
     */
    @Before
    public void setUp() {
        // Setup test runner
        runner.setProperty(ExecuteSparkJob.APPLICATION_JAR, "file:///home/app.jar");
        runner.setProperty(ExecuteSparkJob.MAIN_CLASS, "com.example.App");
        runner.setProperty(ExecuteSparkJob.MAIN_ARGS, "run");
        runner.setProperty(ExecuteSparkJob.SPARK_APPLICATION_NAME, "MyApp");
    }

    /**
     * Verify validators pass with default values from this test class.
     */
    @Test
    public void testValidators() {
        runner.enqueue(new byte[0]);
        Assert.assertEquals(0, ((MockProcessContext) runner.getProcessContext()).validate().size());
    }

    /**
     * Verify validators for Data Sources property.
     */
    @Test
    public void testValidatorsForDatasources() throws Exception {
        // Test UUID list validator
        runner.setProperty(ExecuteSparkJob.DATASOURCES, "INVALID");
        runner.enqueue(new byte[0]);

        Set<String> results = ((MockProcessContext) runner.getProcessContext()).validate().stream().map(Object::toString).collect(Collectors.toSet());
        Assert.assertEquals(1, results.size());
        Assert.assertTrue(results.contains("'Data Sources' validated against 'INVALID' is invalid because not a list of UUIDs"));

        // Test missing metadata service
        runner.setProperty(ExecuteSparkJob.DATASOURCES, "87870c7e-8ae8-4db4-9959-c2f5a9496833");
        runner.enqueue(new byte[0]);

        results = ((MockProcessContext) runner.getProcessContext()).validate().stream().map(Object::toString).collect(Collectors.toSet());
        Assert.assertEquals(1, results.size());
        Assert.assertTrue(results.contains("'Metadata Service' is invalid because Metadata Service is required when Data Sources is not empty"));

        // Test with one UUID
        final MetadataProviderService metadataService = new MockMetadataProviderService();
        runner.addControllerService(METADATA_SERVICE_IDENTIFIER, metadataService);
        runner.enableControllerService(metadataService);
        runner.setProperty(ExecuteSparkJob.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);

        runner.enqueue(new byte[0]);
        Assert.assertEquals(0, ((MockProcessContext) runner.getProcessContext()).validate().size());

        // Test with two UUIDs
        runner.setProperty(ExecuteSparkJob.DATASOURCES, "87870c7e-8ae8-4db4-9959-c2f5a9496833,e4562514-8e06-459a-8ea9-1e2630c852f9");
        runner.enqueue(new byte[0]);
        Assert.assertEquals(0, ((MockProcessContext) runner.getProcessContext()).validate().size());

        // Test with expression
        runner.setProperty(ExecuteSparkJob.DATASOURCES, "${metadata.dataTransformation.datasourceIds}");
        runner.enqueue(new byte[0], Collections.singletonMap("metadata.dataTransformation.datasourceIds", "87870c7e-8ae8-4db4-9959-c2f5a9496833"));
        Assert.assertEquals(0, ((MockProcessContext) runner.getProcessContext()).validate().size());
    }

    /**
     * Verify validators for required properties.
     */
    @Test
    public void testValidatorsWithRequired() {
        runner.removeProperty(ExecuteSparkJob.APPLICATION_JAR);
        runner.removeProperty(ExecuteSparkJob.MAIN_CLASS);
        runner.removeProperty(ExecuteSparkJob.MAIN_ARGS);
        runner.removeProperty(ExecuteSparkJob.SPARK_APPLICATION_NAME);
        runner.enqueue(new byte[0]);

        final Set<String> results = ((MockProcessContext) runner.getProcessContext()).validate().stream().map(Object::toString).collect(Collectors.toSet());
        Assert.assertEquals(4, results.size());
        Assert.assertTrue(results.contains("'ApplicationJAR' is invalid because ApplicationJAR is required"));
        Assert.assertTrue(results.contains("'MainClass' is invalid because MainClass is required"));
        Assert.assertTrue(results.contains("'MainArgs' is invalid because MainArgs is required"));
        Assert.assertTrue(results.contains("'Spark Application Name' is invalid because Spark Application Name is required"));
    }

    /**
     * A mock implementation of {@link MetadataProviderService} for testing.
     */
    private static class MockMetadataProviderService extends AbstractControllerService implements MetadataProviderService {

        @Override
        public MetadataProvider getProvider() {
            return null;
        }

        @Override
        public MetadataRecorder getRecorder() {
            return null;
        }

        @Override
        public KyloNiFiFlowProvider getKyloNiFiFlowProvider() {
            return null;
        }
    }
}
