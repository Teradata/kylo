/**
 * 
 */
package com.thinkbiganalytics.metadata.jpa.feed.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.thinkbiganalytics.metadata.api.feed.security.FeedOpsAccessControlProvider;

/**
 *
 */
@Configuration
public class FeedOpsAccessControlConfig {

    @Bean
    public FeedOpsAccessControlProvider feedOpsAccessControlProvider() {
        return new JpaFeedOpsAccessControlProvider();
    }
}
