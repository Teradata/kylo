/**
 *
 */
package com.thinkbiganalytics.metadata.core.feed;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedOperationStatusEvent;
import com.thinkbiganalytics.metadata.api.event.feed.PreconditionTriggerEvent;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

/**
 * @author Sean Felten
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

    public void checkPrecondition(String feedName) {
        List<Feed> feeds = feedProvider.getFeeds(feedProvider.feedCriteria().name(feedName).limit(1));

        if (!feeds.isEmpty()) {
            Feed<?> feed = feeds.get(0);

            checkPrecondition(feed);
        }
    }

    protected void checkPrecondition(Feed.ID feedId) {
        Feed<?> feed = feedProvider.getFeed(feedId);

        if (feed != null) {
            checkPrecondition(feed);
        }
    }

    protected void checkPrecondition(Feed<?> feed) {
        FeedPrecondition precond = feed.getPrecondition();

        if (precond != null) {
            log.debug("Checking precondition of feed: {} ({})", feed.getName(), feed.getId());

            ServiceLevelAgreement sla = precond.getAgreement();
            ServiceLevelAssessment assessment = this.assessor.assess(sla);

            ///precond.setLastAssessment(assessment);
            if (assessment.getResult() == AssessmentResult.SUCCESS) {
                log.info("Firing precondition trigger event for feed:{} ({})", feed.getName(), feed.getId());

                this.eventService.notify(new PreconditionTriggerEvent(feed.getId()));
            }
        }
    }


    private class FeedOperationListener implements MetadataEventListener<FeedOperationStatusEvent> {

        @Override
        public void notify(FeedOperationStatusEvent event) {
            FeedOperation.State state = event.getState();

            // TODO as precondition check criteria are not implemented yet, 
            // check all preconditions of feeds that have them.
            if (state == FeedOperation.State.SUCCESS) {
                metadata.read(() -> {
                    for (Feed<?> feed : feedProvider.getFeeds()) {
                        // Don't check the precondition of the feed that that generated this change event.
                        // TODO: this might not be the correct behavior but none of our current metrics
                        // need to be assessed when the feed itself containing the precondition has changed state.
                        if (! feed.getQualifiedName().equals(event.getFeedName())) {
                            checkPrecondition(feed);
                        }
                    }
                    return null;
                }, MetadataAccess.SERVICE);
            }
        }
    }
}
