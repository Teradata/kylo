package com.thinkbiganalytics.nifi.core.api.metadata;

/*-
 * #%L
 * thinkbig-nifi-core-service-api
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

import com.thinkbiganalytics.metadata.rest.model.feed.InitializationStatus;
import com.thinkbiganalytics.metadata.rest.model.feed.reindex.FeedDataHistoryReindexParams;
import com.thinkbiganalytics.metadata.rest.model.feed.reindex.HistoryReindexingStatus;

import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.ProcessSession;

import java.util.Optional;

/**
 * Records metadata that will eventually be committed to the metadata store; sometimes only
 * upon a flow's successful completion.
 */
public interface MetadataRecorder {

    FlowFile loadWaterMark(ProcessSession session,
                           FlowFile ff,
                           String feedId,
                           String waterMarkName,
                           String parameterName,
                           String initialValue) throws WaterMarkActiveException;

    FlowFile cancelAndLoadWaterMark(ProcessSession session, 
                                    FlowFile outputFF, 
                                    String feedId, 
                                    String waterMark, 
                                    String parameterName, 
                                    String initialValue) throws WaterMarkActiveException;

    FlowFile recordWaterMark(ProcessSession session,
                             FlowFile ff,
                             String feedId,
                             String waterMarkName,
                             String parameterName,
                             String newValue);

    FlowFile commitWaterMark(ProcessSession session, FlowFile ff, String feedId, String waterMarkName);

    FlowFile commitAllWaterMarks(ProcessSession session, FlowFile ff, String feedId);

    FlowFile releaseWaterMark(ProcessSession session, FlowFile ff, String feedId, String waterMarkName);

    FlowFile releaseAllWaterMarks(ProcessSession session, FlowFile ff, String feedId);
    
    boolean cancelWaterMark(String feedId, String waterMark);


    Optional<InitializationStatus> getInitializationStatus(String feedId);

    InitializationStatus startFeedInitialization(String feedId);

    InitializationStatus completeFeedInitialization(String feedId);

    InitializationStatus failFeedInitialization(String feedId);


    void updateFeedStatus(ProcessSession session, FlowFile ff, String statusMsg);

    /**
     * Update the history reindexing status of a feed
     * @param feedId feed id for which status should be updated
     * @param historyReindexingStatus The new {@link HistoryReindexingStatus}
     * @returns {@link FeedDataHistoryReindexParams} updated status of feed and columns to index
     */
    FeedDataHistoryReindexParams updateFeedHistoryReindexing(String feedId, HistoryReindexingStatus historyReindexingStatus);

}
