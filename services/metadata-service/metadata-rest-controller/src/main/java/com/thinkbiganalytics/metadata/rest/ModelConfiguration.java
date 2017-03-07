/**
 * 
 */
package com.thinkbiganalytics.metadata.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration related to translation between the domain and REST models.
 */
@Configuration
public class ModelConfiguration {

    @Bean(name = "metadataModelTransform")
    public MetadataModelTransform metadataTransform() {
        return new MetadataModelTransform();
    }
}
