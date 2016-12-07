/**
 * 
 */
package com.thinkbiganalytics.metadata.audit.core;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.audit.AuditLogProvider;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;
import com.thinkbiganalytics.metadata.api.event.feed.FeedChangeEvent;
import com.thinkbiganalytics.metadata.api.event.template.TemplateChangeEvent;

/**
 * A service responsible for producing audit log entries from things like metadata events
 * and annotated methods.
 * 
 * @author Sean Felten
 */
public class AuditLoggingService {
    
    private static final Logger log = LoggerFactory.getLogger(AuditLoggingService.class);
    
    @Inject
    private AuditLogProvider provider;
    
    @Inject 
    private MetadataAccess metadataAccess;
    
    public AuditLoggingService() {
    }
    
    /**
     * @param eventService
     */
    public void addListeners(MetadataEventService eventService) {
        eventService.addListener(new FeedChangeEventListener());
        eventService.addListener(new TemplateChangeEventListener());
    }
    
    private class FeedChangeEventListener implements MetadataEventListener<FeedChangeEvent> {
        @Override
        public void notify(FeedChangeEvent event) {
            metadataAccess.commit(() -> {
                log.debug("Audit: {} - {}", event.getData().getClass().getSimpleName(), event.getData().toString());
                provider.createEntry(event.getUserPrincipal(), 
                                     event.getData().getClass().getSimpleName(), 
                                     event.getData().toString(),
                                     event.getData().getFeedId().toString());
            }, MetadataAccess.SERVICE);
        }
    }
    
    private class TemplateChangeEventListener implements MetadataEventListener<TemplateChangeEvent> {
        @Override
        public void notify(TemplateChangeEvent event) {
            metadataAccess.commit(() -> {
                log.debug("Audit: {} - {}", event.getData().getClass().getSimpleName(), event.getData().toString());
                provider.createEntry(event.getUserPrincipal(), 
                                     event.getData().getClass().getSimpleName(), 
                                     event.getData().toString(),
                                     event.getData().getTemplateId().toString());   
            }, MetadataAccess.SERVICE);
        }
    }
}
