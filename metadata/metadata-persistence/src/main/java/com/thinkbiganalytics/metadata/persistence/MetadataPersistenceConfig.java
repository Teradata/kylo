/**
 * 
 */
package com.thinkbiganalytics.metadata.persistence;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.thinkbiganalytics.metadata.api.MetadataAccess;

/**
 *
 * @author Sean Felten
 */
@Configuration
public class MetadataPersistenceConfig {

    @Bean
    @Primary
    public MetadataAccess aggregateMetadataAccess() {
        return new AggregateMetadataAccess();
    }
}
