/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.metadata.api.audit.AuditLogProvider;

/**
 * Configures the AuditLogProvider implementation.
 * @author Sean Felten
 */
@Configuration
public class AuditLogProviderConfig {

    @Bean
    public AuditLogProvider auditLogProvider(AuditLogRepository repository) {
        return new JpaAuditLogProvider(repository);
    }
}
