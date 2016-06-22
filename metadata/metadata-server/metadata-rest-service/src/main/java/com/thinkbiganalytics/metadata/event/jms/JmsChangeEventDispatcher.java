/**
 * 
 */
package com.thinkbiganalytics.metadata.event.jms;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.Topic;

import org.springframework.jms.core.JmsMessagingTemplate;

import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.PreconditionTriggerEvent;
import com.thinkbiganalytics.metadata.rest.model.event.FeedPreconditionTriggerEvent;

/**
 *
 * @author Sean Felten
 */
public class JmsChangeEventDispatcher {
    
    @Inject
    @Named("preconditionTriggerTopic")
    private Topic preconditionTriggerTopic;
    
    @Inject
    @Named("metadataMessagingTemplate")
    private JmsMessagingTemplate jmsMessagingTemplate;
    
    @Inject
    private MetadataEventService eventService;
    
    private PreconditionListener listener = new PreconditionListener();
    
    @PostConstruct
    public void addEventListener() {
        this.eventService.addListener(this.listener);
    }
    
    @PreDestroy
    public void removeEventListener() {
        this.eventService.removeListener(this.listener);
    }
    
    private class PreconditionListener implements MetadataEventListener<PreconditionTriggerEvent> {
        @Override
        public void notify(PreconditionTriggerEvent event) {
            FeedPreconditionTriggerEvent triggerEv = new FeedPreconditionTriggerEvent(event.getData().toString());
            
            jmsMessagingTemplate.convertAndSend(preconditionTriggerTopic, triggerEv);
        }
    }
}
