package com.thinkbiganalytics.metadata.jobrepo;


/*-
 * #%L
 * thinkbig-operational-metadata-integration-service
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


import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedChangeEvent;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.metadata.jobrepo.nifi.provenance.ProvenanceEventFeedUtil;

import org.joda.time.DateTime;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by sr186054 on 6/17/17.
 * This is not used anymore.
 * The NiFiStatsJmsReceiver takes care of setting the streaming job as started/stopped
 */
@Deprecated
public class StreamingFeedService {

    @Inject
    private MetadataEventService metadataEventService;

    @Inject
    protected MetadataAccess metadataAccess;

    @Inject
    private BatchJobExecutionProvider batchJobExecutionProvider;

    @Inject
    OpsManagerFeedProvider opsManagerFeedProvider;

    @Inject
    ProvenanceEventFeedUtil provenanceEventFeedUtil;

    /**
     * Event listener for precondition events
     */
    private final MetadataEventListener<FeedChangeEvent> feedPropertyChangeListener = new FeedChangeEventDispatcher();


    @PostConstruct
    public void addEventListener() {
        metadataEventService.addListener(feedPropertyChangeListener);
    }


    private class FeedChangeEventDispatcher implements MetadataEventListener<FeedChangeEvent> {


        @Override
        public void notify(@Nonnull final FeedChangeEvent metadataEvent) {
            Optional<String> feedName = metadataEvent.getData().getFeedName();
            Feed.State state = metadataEvent.getData().getFeedState();
            if (feedName.isPresent()) {
                metadataAccess.commit(() -> {
                    OpsManagerFeed feed = opsManagerFeedProvider.findByNameWithoutAcl(feedName.get());
                    if (feed != null && feed.isStream()) {
                        //update the job status
                        BatchJobExecution jobExecution = batchJobExecutionProvider.findLatestJobForFeed(feedName.get());
                        if(jobExecution != null) {
                            if (state.equals(Feed.State.ENABLED)) {
                                jobExecution.setStatus(BatchJobExecution.JobStatus.STARTED);
                                jobExecution.setExitCode(ExecutionConstants.ExitCode.EXECUTING);
                                jobExecution.setStartTime(DateTime.now());
                            } else {
                                jobExecution.setStatus(BatchJobExecution.JobStatus.STOPPED);
                                jobExecution.setExitCode(ExecutionConstants.ExitCode.COMPLETED);
                                jobExecution.setEndTime(DateTime.now());
                            }
                            batchJobExecutionProvider.save(jobExecution);
                        }
                    }
                }, MetadataAccess.SERVICE);
            }
        }

    }

}
