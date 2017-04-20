package com.thinkbiganalytics.metadata.server;

/*-
 * #%L
 * thinkbig-metadata-rest-controller
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.MetadataAction;
import com.thinkbiganalytics.metadata.api.MetadataCommand;
import com.thinkbiganalytics.metadata.api.MetadataExecutionException;
import com.thinkbiganalytics.metadata.api.MetadataRollbackAction;
import com.thinkbiganalytics.metadata.api.MetadataRollbackCommand;
import com.thinkbiganalytics.metadata.api.datasource.DatasourceProvider;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.core.dataset.InMemoryDatasourceProvider;
import com.thinkbiganalytics.metadata.core.feed.InMemoryFeedProvider;
import com.thinkbiganalytics.metadata.core.sla.TestMetricAssessor;
import com.thinkbiganalytics.metadata.core.sla.WithinScheduleAssessor;
import com.thinkbiganalytics.metadata.core.sla.feed.DatasourceUpdatedSinceAssessor;
import com.thinkbiganalytics.metadata.core.sla.feed.DatasourceUpdatedSinceFeedExecutedAssessor;
import com.thinkbiganalytics.metadata.core.sla.feed.FeedExecutedSinceFeedAssessor;
import com.thinkbiganalytics.metadata.core.sla.feed.FeedExecutedSinceScheduleAssessor;
import com.thinkbiganalytics.metadata.event.jms.JmsChangeEventDispatcher;
import com.thinkbiganalytics.metadata.event.jms.MetadataJmsConfig;
import com.thinkbiganalytics.metadata.event.reactor.ReactorConfiguration;
import com.thinkbiganalytics.metadata.modeshape.ModeShapeEngineConfig;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAgreementProvider;
import com.thinkbiganalytics.metadata.sla.spi.ServiceLevelAssessor;
import com.thinkbiganalytics.metadata.sla.spi.core.InMemorySLAProvider;
import com.thinkbiganalytics.metadata.sla.spi.core.SimpleServiceLevelAssessor;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import java.security.Principal;

@Configuration
@EnableAutoConfiguration
@Import({ReactorConfiguration.class, MetadataJmsConfig.class, ModeShapeEngineConfig.class})
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
    public MetadataAccess metadataAccess() {
        // Transaction behavior not enforced in memory-only mode;
        return new MetadataAccess() {
            @Override
            public <R> R commit(MetadataCommand<R> cmd, Principal... principals) {
                try {
                    return cmd.execute();
                } catch (Exception e) {
                    throw new MetadataExecutionException(e);
                }
            }

            @Override
            public <R> R read(MetadataCommand<R> cmd, Principal... principals) {
                try {
                    return cmd.execute();
                } catch (Exception e) {
                    throw new MetadataExecutionException(e);
                }
            }

            @Override
            public void commit(MetadataAction action, Principal... principals) {
                try {
                    action.execute();
                } catch (Exception e) {
                    throw new MetadataExecutionException(e);
                }
            }

            @Override
            public <R> R commit(MetadataCommand<R> cmd, MetadataRollbackCommand rollbackCmd, Principal... principals) {
                return commit(cmd, principals);
            }

            @Override
            public void commit(MetadataAction action, MetadataRollbackAction rollbackAction, Principal... principals) {
                commit(action, principals);
            }

            @Override
            public void read(MetadataAction cmd, Principal... principals) {
                try {
                    cmd.execute();
                } catch (Exception e) {
                    throw new MetadataExecutionException(e);
                }
            }
        };
    }

    @Bean
    public JmsChangeEventDispatcher changeEventDispatcher() {
        return new JmsChangeEventDispatcher();
    }


    @Profile("metadata.memory-only")
    public ServiceLevelAgreementProvider slaProvider() {
        return new InMemorySLAProvider();
    }

    @Bean
    @Profile("metadata.memory-only")
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
    public ServerConfigurationInitialization serverConfigurationInitialization() {
        return new ServerConfigurationInitialization();
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

    @Bean
    public MetricAssessor<?, ?> testMetricAssessor() {
        return new TestMetricAssessor();
    }
}
