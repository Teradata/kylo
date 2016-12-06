/**
 * 
 */
package com.thinkbiganalytics.metadata.audit.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.metadata.api.event.MetadataEventService;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class AuditLoggingServiceConfig {

    @Bean
    public AuditLoggingService auditLoggingService(MetadataEventService eventService) {
        AuditLoggingService auditService = new AuditLoggingService();
        auditService.addListeners(eventService);
        return auditService;
    }
}
