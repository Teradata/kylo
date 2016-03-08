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
import com.thinkbiganalytics.metadata.core.feed.FeedPreconditionService;
import com.thinkbiganalytics.metadata.core.feed.InMemoryFeedProvider;
import com.thinkbiganalytics.metadata.core.feed.precond.DatasetUpdatedSinceMetricAssessor;
import com.thinkbiganalytics.metadata.core.feed.precond.FeedExecutedSinceFeedMetricAssessor;
import com.thinkbiganalytics.metadata.core.feed.precond.WithinScheduleAssessor;
import com.thinkbiganalytics.metadata.core.op.InMemoryDataOperationsProvider;
import com.thinkbiganalytics.metadata.event.ChangeEventDispatcher;
import com.thinkbiganalytics.metadata.event.ReactorContiguration;
import com.thinkbiganalytics.metadata.event.jms.JmsChangeEventDispatcher;
import com.thinkbiganalytics.metadata.event.jms.MetadataJmsConfig;
import com.thinkbiganalytics.metadata.rest.RestConfiguration;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;
import com.thinkbiganalytics.metadata.sla.spi.core.InMemorySLAProvider;
import com.thinkbiganalytics.metadata.sla.spi.core.SimpleServiceLevelAssessor;

/**
 *
 * @author Sean Felten
 */
@Configuration
@EnableAutoConfiguration
@Import({ RestConfiguration.class, ReactorContiguration.class, MetadataJmsConfig.class })
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
        return new JmsChangeEventDispatcher();
//        return new SimpleChangeEventDispatcher();
    }
    
    @Bean
    public FeedPreconditionService feedPreconditionService() {
        return new FeedPreconditionService();
    }
    
    // SLA config
    @Bean
    public ServiceLevelAgreementProvider slaProvider() {
        return new InMemorySLAProvider();
    }

    @Bean
    public ServiceLevelAssessor slaAssessor() {
        SimpleServiceLevelAssessor assr = new SimpleServiceLevelAssessor();
        assr.registerMetricAssessor(datasetUpdatedSinceMetricAssessor());
        assr.registerMetricAssessor(feedExecutedSinceFeedMetricAssessor());
        assr.registerMetricAssessor(withinScheduleAssessor());
        
        return assr;
    }
    
    @Bean
    public MetricAssessor<?, ?> feedExecutedSinceFeedMetricAssessor() {
        return new FeedExecutedSinceFeedMetricAssessor();
    }
    
    @Bean
    public MetricAssessor<?, ?> datasetUpdatedSinceMetricAssessor() {
        return new DatasetUpdatedSinceMetricAssessor();
    }
    
    @Bean
    public MetricAssessor<?, ?> withinScheduleAssessor() {
        return new WithinScheduleAssessor();
    }
}
