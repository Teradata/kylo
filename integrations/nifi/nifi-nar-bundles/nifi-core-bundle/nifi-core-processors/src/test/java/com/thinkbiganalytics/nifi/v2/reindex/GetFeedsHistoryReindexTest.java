package com.thinkbiganalytics.nifi.v2.reindex;

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

import com.thinkbiganalytics.metadata.rest.model.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.feed.FeedCategory;
import com.thinkbiganalytics.metadata.rest.model.feed.reindex.FeedsForDataHistoryReindex;
import com.thinkbiganalytics.metadata.rest.model.feed.reindex.HistoryReindexingState;
import com.thinkbiganalytics.metadata.rest.model.feed.reindex.HistoryReindexingStatus;
import com.thinkbiganalytics.nifi.core.api.metadata.KyloNiFiFlowProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProvider;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataProviderService;
import com.thinkbiganalytics.nifi.core.api.metadata.MetadataRecorder;

import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link GetFeedsHistoryReindex} processor<br>
 */
public class GetFeedsHistoryReindexTest {

    @SuppressWarnings("EmptyMethod")
    @Before
    public void setUp() {
        //Nothing for now
    }

    @Test
    public void testTwoFeedsToReindex() throws Exception {
        final String METADATA_SERVICE_IDENTIFIER = "MockMetadataProviderService_TwoFeedsToReindex";
        final TestRunner runner = TestRunners.newTestRunner(GetFeedsHistoryReindex.class);
        final MetadataProviderService metadataService = new MockMetadataProviderService_TwoFeedsToReindex();
        runner.addControllerService(METADATA_SERVICE_IDENTIFIER, metadataService);
        runner.enableControllerService(metadataService);
        runner.setProperty(GetFeedsHistoryReindex.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);

        runner.run(1);
        runner.assertQueueEmpty();
        runner.assertTransferCount(GetFeedsHistoryReindex.REL_FOUND, 2);
        runner.assertTransferCount(GetFeedsHistoryReindex.REL_NOT_FOUND, 0);
        runner.assertTransferCount(GetFeedsHistoryReindex.REL_FAILURE, 0);
        runner.assertTransferCount(GetFeedsHistoryReindex.REL_ORIGINAL, 1);
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(GetFeedsHistoryReindex.REL_FOUND);

        for (int i = 0; i < results.size(); i++) {
            MockFlowFile resultFlowFile = results.get(i);
            resultFlowFile.assertAttributeExists(GetFeedsHistoryReindex.FEED_ID_FOR_HISTORY_REINDEX_KEY);
            resultFlowFile.assertAttributeExists(GetFeedsHistoryReindex.FEED_SYSTEM_NAME_FOR_HISTORY_REINDEX_KEY);
            resultFlowFile.assertAttributeExists(GetFeedsHistoryReindex.FEED_CATEGORY_SYSTEM_NAME_FOR_HISTORY_REINDEX_KEY);
            resultFlowFile.assertAttributeExists(GetFeedsHistoryReindex.FEED_STATUS_FOR_HISTORY_REINDEX_KEY);
            resultFlowFile.assertAttributeExists(GetFeedsHistoryReindex.FEED_LAST_MODIFIED_UTC_FOR_HISTORY_REINDEX_KEY);
            resultFlowFile.assertAttributeExists(GetFeedsHistoryReindex.FEEDS_TOTAL_COUNT_FOR_HISTORY_REINDEX_KEY);
            resultFlowFile.assertAttributeExists(GetFeedsHistoryReindex.FEEDS_TOTAL_IDS_FOR_HISTORY_REINDEX_KEY);
            resultFlowFile.assertAttributeExists(GetFeedsHistoryReindex.FEEDS_CHECK_TIME_UTC_FOR_HISTORY_REINDEX_KEY);

            resultFlowFile.assertAttributeEquals(GetFeedsHistoryReindex.FEED_ID_FOR_HISTORY_REINDEX_KEY, "feed-" + i + "-id");
            resultFlowFile.assertAttributeEquals(GetFeedsHistoryReindex.FEED_SYSTEM_NAME_FOR_HISTORY_REINDEX_KEY, "feed-" + i + "-systemName");
            resultFlowFile.assertAttributeEquals(GetFeedsHistoryReindex.FEED_CATEGORY_SYSTEM_NAME_FOR_HISTORY_REINDEX_KEY, "cat-" + i + "-systemName");
            resultFlowFile.assertAttributeEquals(GetFeedsHistoryReindex.FEED_STATUS_FOR_HISTORY_REINDEX_KEY, "DIRTY");
            if (i == 0) {
                String feedLastModifiedUtcForHistoryReindexActualValue = resultFlowFile.getAttribute(GetFeedsHistoryReindex.FEED_LAST_MODIFIED_UTC_FOR_HISTORY_REINDEX_KEY);
                String feedLastModifiedUtcForHistoryReindexExpectedValuePrefixWithoutTimeZone = "2017-12-18T14:53:24.013";
                assertTrue(feedLastModifiedUtcForHistoryReindexActualValue.contains(feedLastModifiedUtcForHistoryReindexExpectedValuePrefixWithoutTimeZone));
            } else if (i == 1) {
                String feedLastModifiedUtcForHistoryReindexActualValue = resultFlowFile.getAttribute(GetFeedsHistoryReindex.FEED_LAST_MODIFIED_UTC_FOR_HISTORY_REINDEX_KEY);
                String feedLastModifiedUtcForHistoryReindexExpectedValuePrefixWithoutTimeZone = "2017-12-17T09:35:45.335";
                assertTrue(feedLastModifiedUtcForHistoryReindexActualValue.contains(feedLastModifiedUtcForHistoryReindexExpectedValuePrefixWithoutTimeZone));
            }
            resultFlowFile.assertAttributeEquals(GetFeedsHistoryReindex.FEEDS_TOTAL_COUNT_FOR_HISTORY_REINDEX_KEY, String.valueOf(2));
            resultFlowFile.assertAttributeEquals(GetFeedsHistoryReindex.FEEDS_TOTAL_IDS_FOR_HISTORY_REINDEX_KEY, "[feed-0-id, feed-1-id]");
            resultFlowFile.assertAttributeNotEquals(GetFeedsHistoryReindex.FEEDS_CHECK_TIME_UTC_FOR_HISTORY_REINDEX_KEY, null);
        }
    }


    @Test
    public void testNoFeedsToReindex() throws Exception {
        final String METADATA_SERVICE_IDENTIFIER = "MockMetadataProviderService_NoFeedsToIndex";
        final TestRunner runner = TestRunners.newTestRunner(GetFeedsHistoryReindex.class);
        final MetadataProviderService metadataService = new MockMetadataProviderService_NoFeedsToReindex();
        runner.addControllerService(METADATA_SERVICE_IDENTIFIER, metadataService);
        runner.enableControllerService(metadataService);
        runner.setProperty(GetFeedsHistoryReindex.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);

        runner.run(1);
        runner.assertQueueEmpty();
        runner.assertTransferCount(GetFeedsHistoryReindex.REL_FOUND, 0);
        runner.assertTransferCount(GetFeedsHistoryReindex.REL_NOT_FOUND, 1);
        runner.assertTransferCount(GetFeedsHistoryReindex.REL_FAILURE, 0);
        runner.assertTransferCount(GetFeedsHistoryReindex.REL_ORIGINAL, 0);

        List<MockFlowFile> results = runner.getFlowFilesForRelationship(GetFeedsHistoryReindex.REL_NOT_FOUND);
        MockFlowFile resultFlowFile = results.get(0);

        resultFlowFile.assertAttributeExists(GetFeedsHistoryReindex.FEEDS_TOTAL_COUNT_FOR_HISTORY_REINDEX_KEY);
        resultFlowFile.assertAttributeExists(GetFeedsHistoryReindex.FEEDS_TOTAL_IDS_FOR_HISTORY_REINDEX_KEY);
        resultFlowFile.assertAttributeExists(GetFeedsHistoryReindex.FEEDS_CHECK_TIME_UTC_FOR_HISTORY_REINDEX_KEY);

        resultFlowFile.assertAttributeEquals(GetFeedsHistoryReindex.FEEDS_TOTAL_COUNT_FOR_HISTORY_REINDEX_KEY, String.valueOf(0));
        resultFlowFile.assertAttributeEquals(GetFeedsHistoryReindex.FEEDS_TOTAL_IDS_FOR_HISTORY_REINDEX_KEY, "[]");
        resultFlowFile.assertAttributeNotEquals(GetFeedsHistoryReindex.FEEDS_CHECK_TIME_UTC_FOR_HISTORY_REINDEX_KEY, null);
    }

    @Test
    public void testErrorGettingFeedsToReindex() throws Exception {
        final String METADATA_SERVICE_IDENTIFIER = "MockMetadataProviderService_ErrorGettingFeedsToReindex";
        final TestRunner runner = TestRunners.newTestRunner(GetFeedsHistoryReindex.class);
        final MetadataProviderService metadataService = new MockMetadataProviderService_ErrorGettingFeedsToReindex();

        runner.addControllerService(METADATA_SERVICE_IDENTIFIER, metadataService);
        runner.enableControllerService(metadataService);
        runner.setProperty(GetFeedsHistoryReindex.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);

        runner.run(1);
        runner.assertQueueEmpty();
        runner.assertTransferCount(GetFeedsHistoryReindex.REL_FOUND, 0);
        runner.assertTransferCount(GetFeedsHistoryReindex.REL_NOT_FOUND, 0);
        runner.assertTransferCount(GetFeedsHistoryReindex.REL_FAILURE, 1);
        runner.assertTransferCount(GetFeedsHistoryReindex.REL_ORIGINAL, 0);

        List<MockFlowFile> results = runner.getFlowFilesForRelationship(GetFeedsHistoryReindex.REL_FAILURE);
        MockFlowFile resultFlowFile = results.get(0);
        resultFlowFile.assertAttributeNotExists(GetFeedsHistoryReindex.FEEDS_TOTAL_COUNT_FOR_HISTORY_REINDEX_KEY);
        resultFlowFile.assertAttributeNotExists(GetFeedsHistoryReindex.FEEDS_TOTAL_IDS_FOR_HISTORY_REINDEX_KEY);
        resultFlowFile.assertAttributeNotExists(GetFeedsHistoryReindex.FEEDS_CHECK_TIME_UTC_FOR_HISTORY_REINDEX_KEY);
    }

    @Test
    public void testGeneralExceptionGettingFeedsToReindex() throws Exception {
        final String METADATA_SERVICE_IDENTIFIER = "MockMetadataProviderService_GeneralExceptionGettingFeedsToReindex";
        final TestRunner runner = TestRunners.newTestRunner(GetFeedsHistoryReindex.class);
        final MetadataProviderService metadataService = new MockMetadataProviderService_GeneralExceptionGettingFeedsToReindex();

        runner.addControllerService(METADATA_SERVICE_IDENTIFIER, metadataService);
        runner.enableControllerService(metadataService);
        runner.setProperty(GetFeedsHistoryReindex.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);

        runner.run(1);
        runner.assertQueueEmpty();
        runner.assertTransferCount(GetFeedsHistoryReindex.REL_FOUND, 0);
        runner.assertTransferCount(GetFeedsHistoryReindex.REL_NOT_FOUND, 0);
        runner.assertTransferCount(GetFeedsHistoryReindex.REL_FAILURE, 1);
        runner.assertTransferCount(GetFeedsHistoryReindex.REL_ORIGINAL, 0);

        List<MockFlowFile> results = runner.getFlowFilesForRelationship(GetFeedsHistoryReindex.REL_FAILURE);
        MockFlowFile resultFlowFile = results.get(0);
        resultFlowFile.assertAttributeNotExists(GetFeedsHistoryReindex.FEEDS_TOTAL_COUNT_FOR_HISTORY_REINDEX_KEY);
        resultFlowFile.assertAttributeNotExists(GetFeedsHistoryReindex.FEEDS_TOTAL_IDS_FOR_HISTORY_REINDEX_KEY);
        resultFlowFile.assertAttributeNotExists(GetFeedsHistoryReindex.FEEDS_CHECK_TIME_UTC_FOR_HISTORY_REINDEX_KEY);
    }


    private static class MockMetadataProviderService_TwoFeedsToReindex extends AbstractControllerService implements MetadataProviderService {

        @Override
        public MetadataProvider getProvider() {
            final MetadataProvider provider = Mockito.mock(MetadataProvider.class);
            Mockito.when(provider.getFeedsForHistoryReindexing()).then(invocation -> {

                final Feed feed0 = Mockito.mock(Feed.class);
                final FeedCategory feedCategory0 = Mockito.mock(FeedCategory.class);
                final HistoryReindexingStatus historyReindexingStatus0 = Mockito.mock(HistoryReindexingStatus.class);

                Mockito.when(feed0.getId()).thenReturn("feed-0-id");
                Mockito.when(feed0.getSystemName()).thenReturn("feed-0-systemName");
                Mockito.when(feed0.getCurrentHistoryReindexingStatus()).thenReturn(historyReindexingStatus0);
                Mockito.when(historyReindexingStatus0.getHistoryReindexingState()).thenReturn(HistoryReindexingState.DIRTY);
                Mockito.when(historyReindexingStatus0.getLastModifiedTimestamp()).thenReturn(new DateTime().withDate(2017, 12, 18).withTime(14, 53, 24, 13));
                Mockito.when(feed0.getCategory()).thenReturn(feedCategory0);
                Mockito.when(feedCategory0.getSystemName()).thenReturn("cat-0-systemName");

                final Feed feed1 = Mockito.mock(Feed.class);
                final FeedCategory feedCategory1 = Mockito.mock(FeedCategory.class);
                final HistoryReindexingStatus historyReindexingStatus1 = Mockito.mock(HistoryReindexingStatus.class);

                Mockito.when(feed1.getId()).thenReturn("feed-1-id");
                Mockito.when(feed1.getSystemName()).thenReturn("feed-1-systemName");
                Mockito.when(feed1.getCurrentHistoryReindexingStatus()).thenReturn(historyReindexingStatus1);
                Mockito.when(historyReindexingStatus1.getHistoryReindexingState()).thenReturn(HistoryReindexingState.DIRTY);
                Mockito.when(historyReindexingStatus1.getLastModifiedTimestamp()).thenReturn(new DateTime().withDate(2017, 12, 17).withTime(9, 35, 45, 335));
                Mockito.when(feed1.getCategory()).thenReturn(feedCategory1);
                Mockito.when(feedCategory1.getSystemName()).thenReturn("cat-1-systemName");

                Set<Feed> feedsForHistoryReindexing = new LinkedHashSet<>();
                feedsForHistoryReindexing.add(feed0);
                feedsForHistoryReindexing.add(feed1);
                return new FeedsForDataHistoryReindex(feedsForHistoryReindexing);
            });
            return provider;
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

    private static class MockMetadataProviderService_NoFeedsToReindex extends AbstractControllerService implements MetadataProviderService {

        @Override
        public MetadataProvider getProvider() {
            final MetadataProvider provider = Mockito.mock(MetadataProvider.class);
            Mockito.when(provider.getFeedsForHistoryReindexing()).then(invocation -> {
                Set<Feed> feedsForHistoryReindexing = new LinkedHashSet<>();
                return new FeedsForDataHistoryReindex(feedsForHistoryReindexing);
            });
            return provider;
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

    private static class MockMetadataProviderService_ErrorGettingFeedsToReindex extends AbstractControllerService implements MetadataProviderService {

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

    private static class MockMetadataProviderService_GeneralExceptionGettingFeedsToReindex extends AbstractControllerService implements MetadataProviderService {

        @Override
        public MetadataProvider getProvider() {
            final MetadataProvider provider = Mockito.mock(MetadataProvider.class);
            Mockito.when(provider.getFeedsForHistoryReindexing()).then(invocation -> {
                throw new RuntimeException("General exception getting list of feeds for history reindexing");
            });
            return provider;
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
