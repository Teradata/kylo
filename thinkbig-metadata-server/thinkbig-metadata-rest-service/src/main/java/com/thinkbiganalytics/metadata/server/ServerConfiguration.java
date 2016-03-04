/**
 * 
 */
package com.thinkbiganalytics.metadata.server;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.thinkbiganalytics.metadata.api.dataset.DatasetProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
import com.thinkbiganalytics.metadata.core.dataset.InMemoryDatasetProvider;
import com.thinkbiganalytics.metadata.core.feed.InMemoryFeedProvider;
import com.thinkbiganalytics.metadata.core.op.InMemoryDataOperationsProvider;
import com.thinkbiganalytics.metadata.event.ChangeEventDispatcher;
import com.thinkbiganalytics.metadata.event.EventsContiguration;
import com.thinkbiganalytics.metadata.event.SimpleChangeEventDispatcher;
import com.thinkbiganalytics.metadata.rest.RestConfiguration;

/**
 *
 * @author Sean Felten
 */
@Configuration
@EnableAutoConfiguration
@Import({ RestConfiguration.class, EventsContiguration.class })
public class ServerConfiguration {

    @Bean
    public FeedProvider feedProvider() {
        return new InMemoryFeedProvider();
    }

    @Bean
    public DatasetProvider datasetProvider() {
        return new InMemoryDatasetProvider();
    }
    
    @Bean
    public DataOperationsProvider dataOperationsProvider() {
        return new InMemoryDataOperationsProvider();
    }
    
    @Bean
    public ChangeEventDispatcher changeEventDispatcher() {
        return new SimpleChangeEventDispatcher();
    }
}
