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

import com.thinkbiganalytics.metadata.rest.model.feed.reindex.FeedDataHistoryReindexParams;
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

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link UpdateFeedHistoryReindex} processor <br>
 */
public class UpdateFeedHistoryReindexTest {

    @SuppressWarnings("EmptyMethod")
    @Before
    public void setUp() {
        //Nothing for now
    }

    @Test
    public void setFeedStatus_ToDirty() throws Exception {
        final String METADATA_SERVICE_IDENTIFIER = "MockMetadataProviderService_SetFeedToDirty";
        final TestRunner runner = TestRunners.newTestRunner(UpdateFeedHistoryReindex.class);
        final MetadataProviderService metadataService = new MockMetadataProviderService_SetFeedToDirty();
        runner.addControllerService(METADATA_SERVICE_IDENTIFIER, metadataService);
        runner.enableControllerService(metadataService);
        runner.setProperty(UpdateFeedHistoryReindex.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);
        runner.setProperty(UpdateFeedHistoryReindex.FEED_ID, "feed-0-id");
        runner.setProperty(UpdateFeedHistoryReindex.FEED_REINDEX_STATUS, "DIRTY");

        runner.run(1);
        runner.assertQueueEmpty();
        runner.assertTransferCount(UpdateFeedHistoryReindex.REL_SUCCESS, 1);
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(UpdateFeedHistoryReindex.REL_SUCCESS);

        MockFlowFile resultFlowFile = results.get(0);
        resultFlowFile.assertAttributeExists(UpdateFeedHistoryReindex.UPDATED_FEED_INFO_FOR_HISTORY_REINDEX_KEY);
        resultFlowFile.assertAttributeExists(UpdateFeedHistoryReindex.UPDATED_FEED_STATUS_FOR_HISTORY_REINDEX_KEY);
        resultFlowFile.assertAttributeExists(UpdateFeedHistoryReindex.UPDATED_TIME_UTC_FOR_HISTORY_REINDEX_KEY);
        resultFlowFile.assertAttributeExists(UpdateFeedHistoryReindex.UPDATED_INDEX_COLUMNS_STRING_FOR_HISTORY_REINDEX_KEY);

        resultFlowFile.assertAttributeEquals(UpdateFeedHistoryReindex.UPDATED_FEED_INFO_FOR_HISTORY_REINDEX_KEY,
                                             "feed id: feed-0-id, feed name: cat-0-system-name.feed-0-system-name");

        resultFlowFile.assertAttributeEquals(UpdateFeedHistoryReindex.UPDATED_FEED_STATUS_FOR_HISTORY_REINDEX_KEY,
                                             "DIRTY");

        String updatedTimeUtcForHistoryReindexActualValue = resultFlowFile.getAttribute(UpdateFeedHistoryReindex.UPDATED_TIME_UTC_FOR_HISTORY_REINDEX_KEY);
        String updatedTimeUtcForHistoryReindexExpectedValueWithoutTimeZone = "2017-12-21T11:43:23.345";
        assertTrue(updatedTimeUtcForHistoryReindexActualValue.contains(updatedTimeUtcForHistoryReindexExpectedValueWithoutTimeZone));

        resultFlowFile.assertAttributeEquals(UpdateFeedHistoryReindex.UPDATED_INDEX_COLUMNS_STRING_FOR_HISTORY_REINDEX_KEY,
                                             "col1,col2,col3");
    }

    @Test
    public void setFeedStatus_ThrowsGeneralException() throws Exception {
        final String METADATA_SERVICE_IDENTIFIER = "MockMetadataProviderService_ThrowsGeneralException";
        final TestRunner runner = TestRunners.newTestRunner(UpdateFeedHistoryReindex.class);
        final MetadataProviderService metadataService = new MockMetadataProviderService_ThrowsGeneralException();
        runner.addControllerService(METADATA_SERVICE_IDENTIFIER, metadataService);
        runner.enableControllerService(metadataService);
        runner.setProperty(UpdateFeedHistoryReindex.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);
        runner.setProperty(UpdateFeedHistoryReindex.FEED_ID, "feed-0-id");
        runner.setProperty(UpdateFeedHistoryReindex.FEED_REINDEX_STATUS, "DIRTY");

        runner.run(1);
        runner.assertQueueEmpty();
        runner.assertTransferCount(UpdateFeedHistoryReindex.REL_FAILURE, 1);
    }

    @Test
    public void setFeedStatus_InvalidState() throws Exception {
        final String METADATA_SERVICE_IDENTIFIER = "MockMetadataProviderService_Minimal";
        final TestRunner runner = TestRunners.newTestRunner(UpdateFeedHistoryReindex.class);
        final MetadataProviderService metadataService = new MockMetadataProviderService_Minimal();
        runner.addControllerService(METADATA_SERVICE_IDENTIFIER, metadataService);
        runner.enableControllerService(metadataService);
        runner.setProperty(UpdateFeedHistoryReindex.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);
        runner.setProperty(UpdateFeedHistoryReindex.FEED_ID, "feed-0-id");
        runner.setProperty(UpdateFeedHistoryReindex.FEED_REINDEX_STATUS, "NOT_VALID_STATE");

        runner.assertNotValid();
    }

    @Test
    public void setFeedStatus_ValidStateDirty() throws Exception {
        final String METADATA_SERVICE_IDENTIFIER = "MockMetadataProviderService_Minimal";
        final TestRunner runner = TestRunners.newTestRunner(UpdateFeedHistoryReindex.class);
        final MetadataProviderService metadataService = new MockMetadataProviderService_Minimal();
        runner.addControllerService(METADATA_SERVICE_IDENTIFIER, metadataService);
        runner.enableControllerService(metadataService);
        runner.setProperty(UpdateFeedHistoryReindex.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);
        runner.setProperty(UpdateFeedHistoryReindex.FEED_ID, "feed-0-id");
        runner.setProperty(UpdateFeedHistoryReindex.FEED_REINDEX_STATUS, "DIRTY");

        runner.assertValid();
    }

    @Test
    public void setFeedStatus_ValidStateNeverRun() throws Exception {
        final String METADATA_SERVICE_IDENTIFIER = "MockMetadataProviderService_Minimal";
        final TestRunner runner = TestRunners.newTestRunner(UpdateFeedHistoryReindex.class);
        final MetadataProviderService metadataService = new MockMetadataProviderService_Minimal();
        runner.addControllerService(METADATA_SERVICE_IDENTIFIER, metadataService);
        runner.enableControllerService(metadataService);
        runner.setProperty(UpdateFeedHistoryReindex.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);
        runner.setProperty(UpdateFeedHistoryReindex.FEED_ID, "feed-0-id");
        runner.setProperty(UpdateFeedHistoryReindex.FEED_REINDEX_STATUS, "NEVER_RUN");

        runner.assertValid();
    }

    @Test
    public void setFeedStatus_ValidStateInProgress() throws Exception {
        final String METADATA_SERVICE_IDENTIFIER = "MockMetadataProviderService_Minimal";
        final TestRunner runner = TestRunners.newTestRunner(UpdateFeedHistoryReindex.class);
        final MetadataProviderService metadataService = new MockMetadataProviderService_Minimal();
        runner.addControllerService(METADATA_SERVICE_IDENTIFIER, metadataService);
        runner.enableControllerService(metadataService);
        runner.setProperty(UpdateFeedHistoryReindex.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);
        runner.setProperty(UpdateFeedHistoryReindex.FEED_ID, "feed-0-id");
        runner.setProperty(UpdateFeedHistoryReindex.FEED_REINDEX_STATUS, "IN_PROGRESS");

        runner.assertValid();
    }

    @Test
    public void setFeedStatus_ValidStateCompletedWithFailure() throws Exception {
        final String METADATA_SERVICE_IDENTIFIER = "MockMetadataProviderService_Minimal";
        final TestRunner runner = TestRunners.newTestRunner(UpdateFeedHistoryReindex.class);
        final MetadataProviderService metadataService = new MockMetadataProviderService_Minimal();
        runner.addControllerService(METADATA_SERVICE_IDENTIFIER, metadataService);
        runner.enableControllerService(metadataService);
        runner.setProperty(UpdateFeedHistoryReindex.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);
        runner.setProperty(UpdateFeedHistoryReindex.FEED_ID, "feed-0-id");
        runner.setProperty(UpdateFeedHistoryReindex.FEED_REINDEX_STATUS, "COMPLETED_WITH_FAILURE");

        runner.assertValid();
    }

    @Test
    public void setFeedStatus_ValidStateCompletedWithSuccess() throws Exception {
        final String METADATA_SERVICE_IDENTIFIER = "MockMetadataProviderService_Minimal";
        final TestRunner runner = TestRunners.newTestRunner(UpdateFeedHistoryReindex.class);
        final MetadataProviderService metadataService = new MockMetadataProviderService_Minimal();
        runner.addControllerService(METADATA_SERVICE_IDENTIFIER, metadataService);
        runner.enableControllerService(metadataService);
        runner.setProperty(UpdateFeedHistoryReindex.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);
        runner.setProperty(UpdateFeedHistoryReindex.FEED_ID, "feed-0-id");
        runner.setProperty(UpdateFeedHistoryReindex.FEED_REINDEX_STATUS, "COMPLETED_WITH_SUCCESS");

        runner.assertValid();
    }

    @Test
    public void setFeedStatus_ValidStateExpressionLanguage() throws Exception {
        final String METADATA_SERVICE_IDENTIFIER = "MockMetadataProviderService_Minimal";
        final TestRunner runner = TestRunners.newTestRunner(UpdateFeedHistoryReindex.class);
        final MetadataProviderService metadataService = new MockMetadataProviderService_Minimal();
        runner.addControllerService(METADATA_SERVICE_IDENTIFIER, metadataService);
        runner.enableControllerService(metadataService);
        runner.setProperty(UpdateFeedHistoryReindex.METADATA_SERVICE, METADATA_SERVICE_IDENTIFIER);
        runner.setProperty(UpdateFeedHistoryReindex.FEED_ID, "feed-0-id");
        runner.setProperty(UpdateFeedHistoryReindex.FEED_REINDEX_STATUS, "${history.reindex.feed.status.for.update}");

        runner.assertValid();
    }

    private static class MockMetadataProviderService_Minimal extends AbstractControllerService implements MetadataProviderService {

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

    private static class MockMetadataProviderService_ThrowsGeneralException extends AbstractControllerService implements MetadataProviderService {

        @Override
        public MetadataProvider getProvider() {
            return null;
        }

        @Override
        public MetadataRecorder getRecorder() {
            final MetadataRecorder recorder = Mockito.mock(MetadataRecorder.class);
            Mockito.when(recorder.updateFeedHistoryReindexing(anyString(), any(HistoryReindexingStatus.class))).then(invocation -> {
                throw new RuntimeException("General exception updating reindexing status of feed");
            });
            return recorder;
        }

        @Override
        public KyloNiFiFlowProvider getKyloNiFiFlowProvider() {
            return null;
        }
    }

    private static class MockMetadataProviderService_SetFeedToDirty extends AbstractControllerService implements MetadataProviderService {

        @Override
        public MetadataProvider getProvider() {
            return null;
        }

        @Override
        public MetadataRecorder getRecorder() {
            final MetadataRecorder recorder = Mockito.mock(MetadataRecorder.class);
            Mockito.when(recorder.updateFeedHistoryReindexing(anyString(), any(HistoryReindexingStatus.class))).then(invocation -> {
                Object[] args = invocation.getArguments();
                String argFeedId = (String) args[0];
                HistoryReindexingStatus historyReindexingStatus = (HistoryReindexingStatus) args[1];

                FeedDataHistoryReindexParams feedDataHistoryReindexParams = new FeedDataHistoryReindexParams();
                feedDataHistoryReindexParams.setFeedId(argFeedId);
                feedDataHistoryReindexParams.setCategorySystemName("cat-0-system-name");    //will come from modeshape
                feedDataHistoryReindexParams.setFeedSystemName("feed-0-system-name");       //will come from modeshape
                HistoryReindexingStatus historyReindexingStatusUpdated = new HistoryReindexingStatus(
                    historyReindexingStatus.getHistoryReindexingState(), new DateTime().withDate(2017, 12, 21).withTime(11, 43, 23, 345)
                ); //time of update will come from modeshape
                feedDataHistoryReindexParams.setHistoryReindexingStatus(historyReindexingStatusUpdated);
                feedDataHistoryReindexParams.setCommaSeparatedColumnsForIndexing("col1,col2,col3"); //will come from modeshape
                return feedDataHistoryReindexParams;
            });
            return recorder;
        }

        @Override
        public KyloNiFiFlowProvider getKyloNiFiFlowProvider() {
            return null;
        }
    }
}
