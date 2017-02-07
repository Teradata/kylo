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
import com.thinkbiganalytics.nifi.core.api.metadata.KyloNiFiFlowProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;

import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Properties;

/**
 */
public class PutFeedMetadataTest {

    private static final String METADATA_SERVICE_IDENTIFIER = "MockMetadataProviderService";

    private final TestRunner runner = TestRunners.newTestRunner(PutFeedMetadata.class);


    @Before
    public void setUp() throws Exception {
        // Setup services
        final MetadataProviderService metadataService = new MockMetadataProviderService();

        // Setup test runner
        runner.addControllerService(METADATA_SERVICE_IDENTIFIER, metadataService);
        runner.enableControllerService(metadataService);
        runner.setProperty(TriggerCleanup.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);
    }

    @Test
    public void testValidationPasses() {
        runner.setProperty(PutFeedMetadata.CATEGORY_NAME, "cat");
        runner.setProperty(PutFeedMetadata.FEED_NAME, "feed");
        runner.setProperty(PutFeedMetadata.NAMESPACE, "registration");
        runner.setProperty("testProperty1", "myValue1");
        runner.setProperty("testProperty2", "myValue2");
        runner.run();

    }

    @Test(expected = AssertionError.class)
    public void testValidationFailsForInvalidCharacterInFieldName() {
        runner.setProperty(PutFeedMetadata.CATEGORY_NAME, "cat");
        runner.setProperty(PutFeedMetadata.FEED_NAME, "feed");
        runner.setProperty(PutFeedMetadata.NAMESPACE, "registration");
        runner.setProperty("$testProperty1", "myValue1");
        runner.run();

    }

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
            Mockito.when(provider.updateFeedProperties(Mockito.anyString(), Mockito.any(Properties.class))).then(invocation -> {
                Properties properties = new Properties();
                properties.setProperty("TestUpdate", "worked");
                return properties;
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

}
