/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.generic;

import org.springframework.context.annotation.Bean;

import com.thinkbiganalytics.metadata.api.extension.ExtensibleEntityProvider;
import com.thinkbiganalytics.metadata.api.extension.ExtensibleTypeProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.extension.JcrExtensibleEntityProvider;
import com.thinkbiganalytics.metadata.modeshape.extension.JcrExtensibleTypeProvider;


/**
 * Overrides the mock beans for use by JcrExtensibleProvidersTest.
 */
public class JcrExtensibleProvidersTestConfig {

    @Bean
    private ExtensibleTypeProvider typeProvider() {
        return new JcrExtensibleTypeProvider();
    }

    @Bean
    private ExtensibleEntityProvider entityProvider() {
        return new JcrExtensibleEntityProvider();
    }

    @Bean
    private JcrMetadataAccess metadata() {
        return new JcrMetadataAccess();
    }
}
