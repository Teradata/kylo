package com.thinkbiganalytics.nifi.v2.metadata;

/*-
 * #%L
 * thinkbig-nifi-core-processors
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

import com.thinkbiganalytics.metadata.api.feed.FeedProperties;
import com.thinkbiganalytics.metadata.rest.model.event.FeedCleanupTriggerEvent;
import com.thinkbiganalytics.nifi.core.api.cleanup.CleanupEventService;
import com.thinkbiganalytics.nifi.core.api.cleanup.CleanupListener;
import com.thinkbiganalytics.nifi.core.api.metadata.KyloNiFiFlowProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;

import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.MockProcessContext;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public class TriggerCleanupTest {

    /**
     * Identifier for the cleanup event service
     */
    private static final String CLEANUP_SERVICE_IDENTIFIER = "MockCleanupEventService";

    /**
     * Identifier for the metadata provider service
     */
    private static final String METADATA_SERVICE_IDENTIFIER = "MockMetadataProviderService";

    /**
     * Test runner
     */
    private final TestRunner runner = TestRunners.newTestRunner(TriggerCleanup.class);

    /**
     * Initialize instance variables.
     */
    @Before
    public void setUp() throws Exception {
        // Setup services
        final CleanupEventService cleanupService = new MockCleanupEventService();
        final MetadataProviderService metadataService = new MockMetadataProviderService();

        // Setup test runner
        runner.addControllerService(CLEANUP_SERVICE_IDENTIFIER, cleanupService);
        runner.addControllerService(METADATA_SERVICE_IDENTIFIER, metadataService);
        runner.enableControllerService(cleanupService);
        runner.enableControllerService(metadataService);
        runner.setProperty(TriggerCleanup.CLEANUP_SERVICE, CLEANUP_SERVICE_IDENTIFIER);
        runner.setProperty(TriggerCleanup.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);
    }

    /**
     * Verify property validators.
     */
    @Test
    public void testValidators() {
        // Test with no properties
        runner.removeProperty(TriggerCleanup.CLEANUP_SERVICE);
        runner.removeProperty(TriggerCleanup.METADATA_SERVICE);
        runner.enqueue(new byte[0]);
        Set<String> results = ((MockProcessContext) runner.getProcessContext()).validate().stream().map(Object::toString).collect(Collectors.toSet());
        Assert.assertEquals(2, results.size());
        Assert.assertTrue(results.contains("'Feed Cleanup Event Service' is invalid because Feed Cleanup Event Service is required"));
        Assert.assertTrue(results.contains("'Metadata Provider Service' is invalid because Metadata Provider Service is required"));

        // Test with valid properties
        runner.setProperty(TriggerCleanup.CLEANUP_SERVICE, CLEANUP_SERVICE_IDENTIFIER);
        runner.setProperty(TriggerCleanup.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);
        runner.enqueue(new byte[0]);
        Assert.assertEquals(0, ((MockProcessContext) runner.getProcessContext()).validate().size());
    }

    /**
     * Verify scheduling the processor.
     */
    @Test
    public void testScheduled() {
        runner.setProperty(TriggerCleanup.CATEGORY_NAME, "cat");
        runner.setProperty(TriggerCleanup.FEED_NAME, "feed");
        runner.enqueue(new byte[0]);
        runner.run();
    }

    /**
     * Verify exception when scheduling the processor.
     */
    @Test(expected = AssertionError.class)
    public void testTriggeredWithException() {
        runner.setProperty(TriggerCleanup.CATEGORY_NAME, "invalid");
        runner.setProperty(TriggerCleanup.FEED_NAME, "invalid");
        ((TriggerCleanup) runner.getProcessor()).triggered(new FeedCleanupTriggerEvent("FEEDID"));
        runner.run();
    }

    /**
     * Verify triggering the processor.
     */
    @Test
    public void testTriggered() {
        runner.setProperty(TriggerCleanup.CATEGORY_NAME, "cat");
        runner.setProperty(TriggerCleanup.FEED_NAME, "feed");
        ((TriggerCleanup) runner.getProcessor()).triggered(new FeedCleanupTriggerEvent("FEEDID"));
        runner.run();

        List<MockFlowFile> flowFiles = runner.getFlowFilesForRelationship(TriggerCleanup.REL_SUCCESS);
        Assert.assertEquals(1, flowFiles.size());
        flowFiles.get(0).assertAttributeEquals("category", "cat");
        flowFiles.get(0).assertAttributeEquals("feed", "feed");
    }

    /**
     * Verify triggering the processor when the cleanup property is disabled.
     */
    @Test
    public void testTriggeredWhenDisabled() {
        runner.setProperty(TriggerCleanup.CATEGORY_NAME, "cat");
        runner.setProperty(TriggerCleanup.FEED_NAME, "disabled");
        ((TriggerCleanup) runner.getProcessor()).triggered(new FeedCleanupTriggerEvent("FEEDID"));
        runner.run();
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(TriggerCleanup.REL_SUCCESS).size());
    }

    /**
     * Verify exception when the metadata service is unavailable.
     */
    @Test(expected = AssertionError.class)
    public void testTriggeredWhenUnavailable() {
        runner.setProperty(TriggerCleanup.CATEGORY_NAME, "cat");
        runner.setProperty(TriggerCleanup.FEED_NAME, "unavailable");
        ((TriggerCleanup) runner.getProcessor()).triggered(new FeedCleanupTriggerEvent("FEEDID"));
        runner.run();
        Assert.assertEquals(0, runner.getFlowFilesForRelationship(TriggerCleanup.REL_SUCCESS).size());
    }

    /**
     * A mock implementation of {@link MetadataProviderService} for testing.
     */
    private static class MockMetadataProviderService extends AbstractControllerService implements MetadataProviderService {

        @Override
        public MetadataProvider getProvider() {
            final MetadataProvider provider = Mockito.mock(MetadataProvider.class);
            Mockito.when(provider.getFeedId(Mockito.anyString(), Mockito.anyString())).then(invocation -> {
                if ("invalid".equals(invocation.getArgumentAt(0, String.class))) {
                    throw new IllegalArgumentException();
                }
                return invocation.getArgumentAt(1, String.class);
            });
            Mockito.when(provider.getFeedProperties(Mockito.anyString())).then(invocation -> {
                final String feedId = invocation.getArgumentAt(0, String.class);
                if ("disabled".equals(feedId)) {
                    return new Properties();
                }
                if ("unavailable".equals(feedId)) {
                    return null;
                }
                Properties properties = new Properties();
                properties.setProperty(FeedProperties.CLEANUP_ENABLED, "true");
                return properties;
            });
            return provider;
        }


        @Override
        public KyloNiFiFlowProvider getKyloNiFiFlowProvider() {
            return null;
        }

        @Override
        public MetadataRecorder getRecorder() {
            return null;
        }
    }

    /**
     * A mock implementation of {@link CleanupEventService} for testing.
     */
    private class MockCleanupEventService extends AbstractControllerService implements CleanupEventService {

        @Override
        public void addListener(@Nonnull final String category, @Nonnull final String feedName, @Nonnull final CleanupListener listener) {
            Assert.assertEquals(runner.getProcessor(), listener);
        }

        @Override
        public void removeListener(@Nonnull final CleanupListener listener) {
            Assert.assertEquals(runner.getProcessor(), listener);
        }
    }
}
