/**
 * 
 */
package com.thinkbiganalytics.metadata.event.jms;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.Topic;

import com.thinkbiganalytics.activemq.SendJmsMessage;
import com.thinkbiganalytics.metadata.api.dataset.Dataset;
import com.thinkbiganalytics.metadata.api.op.ChangeSet;
import com.thinkbiganalytics.metadata.api.op.ChangedContent;
import com.thinkbiganalytics.metadata.core.feed.FeedPreconditionService;
import com.thinkbiganalytics.metadata.core.feed.PreconditionEvent;
import com.thinkbiganalytics.metadata.core.feed.PreconditionListener;
import com.thinkbiganalytics.metadata.event.SimpleChangeEventDispatcher;
import com.thinkbiganalytics.metadata.rest.Model;
import com.thinkbiganalytics.metadata.rest.model.event.DatasourceChangeEvent;
import com.thinkbiganalytics.metadata.rest.model.feed.Feed;

/**
 *
 * @author Sean Felten
 */
public class JmsChangeEventDispatcher extends SimpleChangeEventDispatcher implements PreconditionListener {
    
    // TODO listen for change events and write to datasourceChangeTopic?
    
    @Inject
    @Named("preconditionTriggerTopic")
    private Topic preconditionTriggerTopic;
    
    @Inject
    private FeedPreconditionService preconditionService;
    
//    @Inject
//    private SendJmsMessage jmsSender;
    
    @PostConstruct
    public void listenForPreconditions() {
        this.preconditionService.addListener(this);
    }

    @Override
    public void triggered(PreconditionEvent preEvent) {
        Feed feed = Model.DOMAIN_TO_FEED.apply(preEvent.getFeed());
        DatasourceChangeEvent dsEvent = new DatasourceChangeEvent(feed);
        
        for (ChangeSet<Dataset, ChangedContent> cs : preEvent.getChanges()) {
            com.thinkbiganalytics.metadata.rest.model.op.Dataset dset = Model.DOMAIN_TO_DATASET.apply(cs);
            dsEvent.addDataset(dset);
        }
        
//        this.jmsSender.sendObject(preconditionTriggerTopic, dsEvent);
    }
}
