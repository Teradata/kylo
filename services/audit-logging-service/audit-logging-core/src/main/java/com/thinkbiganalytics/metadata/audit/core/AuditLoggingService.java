/**
 * 
 */
package com.thinkbiganalytics.metadata.audit.core;

import java.io.Serializable;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.audit.AuditLogProvider;
import com.thinkbiganalytics.metadata.api.event.MetadataEvent;
import com.thinkbiganalytics.metadata.api.event.MetadataEventListener;
import com.thinkbiganalytics.metadata.api.event.MetadataEventService;

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
    
    private MetadataEventListener<MetadataEvent<? extends Serializable>> listener;

    public AuditLoggingService() {
        // Creates a catch-all listener for every metadata event and logs it.
        // TODO: should we be more selective?
        this.listener = new MetadataEventListener<MetadataEvent<? extends Serializable>>() {
            @Override
            public void notify(MetadataEvent<? extends Serializable> event) {
                createAuditEntry(event);
            }
        };
    }
    
    /**
     * @param eventService
     */
    public void addListeners(MetadataEventService eventService) {
        eventService.addListener(listener);
    }
    
    /**
     * @param event
     */
    protected void createAuditEntry(MetadataEvent<? extends Serializable> event) {
        this.metadataAccess.commit(() -> {
            // Assume the toString() of the event's data contains the useful info for this event.
            log.debug("Audit: {} - {}", event.getData().getClass().getSimpleName(), event.getData().toString());
            provider.createEntry(event.getUserPrincipal(), event.getData().getClass().getSimpleName(), event.getData().toString());
        }, MetadataAccess.SERVICE);
        
    }

}
