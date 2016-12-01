/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.metadata.api.audit.AuditLogProvider;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class JpaAuditLogProviderTestConfig {

    @Bean
    public AuditLogProvider auditLogProvider(AuditLogRepository repository) {
        return new JpaAuditLogProvider(repository);
    }
}
