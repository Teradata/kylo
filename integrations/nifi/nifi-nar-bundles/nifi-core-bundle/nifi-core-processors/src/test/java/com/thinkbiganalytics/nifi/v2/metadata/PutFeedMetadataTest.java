package com.thinkbiganalytics.nifi.v2.metadata;

import com.thinkbiganalytics.metadata.api.feed.FeedProperties;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;

import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Properties;
import static org.junit.Assert.*;

/**
 * Created by Jeremy Merrifield on 10/6/16.
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
    public void testTriggered() {
        runner.setProperty(PutFeedMetadata.CATEGORY_NAME, "cat");
        runner.setProperty(PutFeedMetadata.FEED_NAME, "feed");
        runner.setProperty(PutFeedMetadata.NAMESPACE, "registration");
        runner.setProperty("testProperty1", "myValue1");
        runner.setProperty("testProperty2", "myValue2");
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
            Mockito.when(provider.updateFeedProperties(Mockito.anyString(), Mockito.any(Properties.class))).then( invocation -> {
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
        public MetadataRecorder getRecorder() {
            return null;
        }
    }

}
