/**
 *
 */
package com.thinkbiganalytics.metadata.core.feed;

/*-
 * #%L
 * thinkbig-feed-manager-core
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
import com.thinkbiganalytics.metadata.api.event.feed.FeedOperationStatusEvent;
import com.thinkbiganalytics.metadata.api.event.feed.OperationStatus;
import com.thinkbiganalytics.metadata.api.event.feed.PreconditionTriggerEvent;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.metadata.api.sla.FeedExecutedSinceFeed;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

/**
 * Service for assessing {@link FeedPrecondition}
 */
public class FeedPreconditionService {

    private static final Logger log = LoggerFactory.getLogger(FeedPreconditionService.class);

    @Inject
    private ServiceLevelAssessor assessor;

    @Inject
    private FeedProvider feedProvider;

    @Inject
    private MetadataAccess metadata;

    @Inject
    private MetadataEventService eventService;

    private FeedOperationListener listener = new FeedOperationListener();

    @PostConstruct
    public void addEventListener() {
        this.eventService.addListener(this.listener);
    }

    @PreDestroy
    public void removeEventListener() {
        this.eventService.removeListener(this.listener);
    }


    public ServiceLevelAssessment assess(FeedPrecondition precond) {
        ServiceLevelAgreement sla = precond.getAgreement();
        return this.assessor.assess(sla);
    }

    private void checkPrecondition(Feed feed, OperationStatus operationStatus) {
        FeedPrecondition precond = feed.getPrecondition();

        if (precond != null) {
            log.debug("Checking precondition of feed: {} ({})", feed.getName(), feed.getId());

            ServiceLevelAgreement sla = precond.getAgreement();
            boolean isAssess = sla.getAllMetrics().stream()
                .anyMatch(metric -> isMetricDependentOnStatus(metric, operationStatus));

            if (isAssess) {
                ServiceLevelAssessment assessment = this.assessor.assess(sla);

                if (assessment.getResult() == AssessmentResult.SUCCESS) {
                    log.info("Firing precondition trigger event for feed:{} ({})", feed.getName(), feed.getId());
                    this.eventService.notify(new PreconditionTriggerEvent(feed.getId()));
                }
            } else {
                log.debug("Feed {}.{} does not depend on feed {}", feed.getCategory(), feed.getName(), operationStatus.getFeedName());
            }
        }
    }

    /**
     * To avoid feeds being triggered by feeds they do not depend on
     */
    private boolean isMetricDependentOnStatus(Metric metric, OperationStatus operationStatus) {
        return !(metric instanceof FeedExecutedSinceFeed) || operationStatus.getFeedName().equalsIgnoreCase(((FeedExecutedSinceFeed) metric).getCategoryAndFeed());
    }

    private class FeedOperationListener implements MetadataEventListener<FeedOperationStatusEvent> {

        @Override
        public void notify(FeedOperationStatusEvent event) {
            FeedOperation.State state = event.getData().getState();

            // TODO as precondition check criteria are not implemented yet, 
            // check all preconditions of feeds that have them.
            if (state == FeedOperation.State.SUCCESS) {
                metadata.read(() -> {
                    for (Feed feed : feedProvider.findPreconditionedFeeds()) {
                        // Don't check the precondition of the feed that that generated this change event.
                        // TODO: this might not be the correct behavior but none of our current metrics
                        // need to be assessed when the feed itself containing the precondition has changed state.
                        if (!feed.getQualifiedName().equals(event.getData().getFeedName())) {
                            checkPrecondition(feed, event.getData());
                        }
                    }
                }, MetadataAccess.SERVICE);
            }
        }
    }
}
