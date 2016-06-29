/**
 * 
 */
package com.thinkbiganalytics.metadata.core.feed;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

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

/**
 *
 * @author Sean Felten
 */
public class FeedPreconditionService {

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
        
        if (! feeds.isEmpty()) {
            Feed<?> feed = feeds.get(0);
            
            if (feed.getPrecondition() != null) {
                checkPrecondition(feed);
            }
        }
    }
    
    protected void checkPrecondition(Feed.ID feedId) {
        Feed<?> feed = feedProvider.getFeed(feedId);

        if (feed != null && feed.getPrecondition() != null) {
            checkPrecondition(feed);
        }
    }
    
    protected void checkPrecondition(Feed<?> feed) {
        FeedPrecondition precond = feed.getPrecondition();
        
        if (precond != null) {
            ServiceLevelAgreement sla = precond.getAgreement();
            ServiceLevelAssessment assessment = this.assessor.assess(sla);
            
            precond.setLastAssessment(assessment);
            if (assessment.getResult() == AssessmentResult.SUCCESS) {
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
                        checkPrecondition(feed);
                    }
                    return null;
                });
            }
        }
    }
}
