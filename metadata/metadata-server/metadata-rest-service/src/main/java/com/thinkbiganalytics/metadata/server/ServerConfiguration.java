/**
 * 
 */
package com.thinkbiganalytics.metadata.server;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import com.thinkbiganalytics.metadata.api.Command;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.DataOperationsProvider;
import com.thinkbiganalytics.metadata.core.dataset.InMemoryDatasourceProvider;
import com.thinkbiganalytics.metadata.core.feed.FeedPreconditionService;
import com.thinkbiganalytics.metadata.core.feed.InMemoryFeedProvider;
import com.thinkbiganalytics.metadata.core.feed.precond.DatasourceUpdatedSinceAssessor;
import com.thinkbiganalytics.metadata.core.feed.precond.DatasourceUpdatedSinceFeedExecutedAssessor;
import com.thinkbiganalytics.metadata.core.feed.precond.FeedExecutedSinceFeedAssessor;
import com.thinkbiganalytics.metadata.core.feed.precond.FeedExecutedSinceScheduleAssessor;
import com.thinkbiganalytics.metadata.core.feed.precond.WithinScheduleAssessor;
import com.thinkbiganalytics.metadata.core.op.InMemoryDataOperationsProvider;
import com.thinkbiganalytics.metadata.event.ChangeEventDispatcher;
import com.thinkbiganalytics.metadata.event.ReactorContiguration;
import com.thinkbiganalytics.metadata.event.jms.JmsChangeEventDispatcher;
import com.thinkbiganalytics.metadata.event.jms.MetadataJmsConfig;
import com.thinkbiganalytics.metadata.jpa.JpaConfiguration;
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
@Import({ RestConfiguration.class, ReactorContiguration.class, MetadataJmsConfig.class, JpaConfiguration.class })
//@Import({ RestConfiguration.class, ReactorContiguration.class, MetadataJmsConfig.class, ModeShapeEngineConfig.class })
public class ServerConfiguration {
    
    @Bean
    @Profile("metadata.memory-only")
    public FeedProvider feedProvider() {
        return new InMemoryFeedProvider();
    }

    @Bean
    @Profile("metadata.memory-only")
    public DatasourceProvider datasetProvider() {
        return new InMemoryDatasourceProvider();
    }
    
    @Bean
    @Profile("metadata.memory-only")
    public DataOperationsProvider dataOperationsProvider() {
        return new InMemoryDataOperationsProvider();
    }
    
    @Bean
    @Profile("metadata.memory-only")
    public MetadataAccess metadataAccess() {
        // Transaction behavior not enforced in memory-only mode;
        return new MetadataAccess() {
            @Override
            public <R> R commit(Command<R> cmd) {
                return cmd.execute();
            }

            @Override
            public <R> R read(Command<R> cmd) {
                return cmd.execute();
            }
        };
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
    @Profile("metadata.memory-only")
    public ServiceLevelAgreementProvider slaProvider() {
        return new InMemorySLAProvider();
    }

    @Bean
    public ServiceLevelAssessor slaAssessor() {
        SimpleServiceLevelAssessor assr = new SimpleServiceLevelAssessor();
        assr.registerMetricAssessor(datasetUpdatedSinceMetricAssessor());
        assr.registerMetricAssessor(feedExecutedSinceFeedMetricAssessor());
        assr.registerMetricAssessor(feedExecutedSinceScheduleMetricAssessor());
        assr.registerMetricAssessor(datasourceUpdatedSinceFeedExecutedAssessor());
        assr.registerMetricAssessor(withinScheduleAssessor());
        
        return assr;
    }
    
    @Bean
    public MetricAssessor<?, ?> feedExecutedSinceFeedMetricAssessor() {
        return new FeedExecutedSinceFeedAssessor();
    }
    
    @Bean
    public MetricAssessor<?, ?> datasetUpdatedSinceMetricAssessor() {
        return new DatasourceUpdatedSinceAssessor();
    }
    
    @Bean
    public MetricAssessor<?, ?> datasourceUpdatedSinceFeedExecutedAssessor() {
        return new DatasourceUpdatedSinceFeedExecutedAssessor();
    }
    
    @Bean
    public MetricAssessor<?, ?> feedExecutedSinceScheduleMetricAssessor() {
        return new FeedExecutedSinceScheduleAssessor();
    }
    
    @Bean
    public MetricAssessor<?, ?> withinScheduleAssessor() {
        return new WithinScheduleAssessor();
    }
}
