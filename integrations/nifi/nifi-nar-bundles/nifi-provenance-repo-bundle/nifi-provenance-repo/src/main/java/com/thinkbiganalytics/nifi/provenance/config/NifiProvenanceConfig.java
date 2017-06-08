package com.thinkbiganalytics.nifi.provenance.config;

/*-
 * #%L
 * thinkbig-nifi-provenance-repo
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

import com.thinkbiganalytics.nifi.provenance.BatchEventsBySampling;
import com.thinkbiganalytics.nifi.provenance.BatchProvenanceEvents;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventCollector;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventObjectFactory;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventObjectPool;
import com.thinkbiganalytics.nifi.provenance.ProvenanceStatsCalculator;
import com.thinkbiganalytics.nifi.provenance.cache.FeedFlowFileCacheUtil;
import com.thinkbiganalytics.nifi.provenance.cache.FeedFlowFileGuavaCache;
import com.thinkbiganalytics.nifi.provenance.cache.FeedFlowFileMapDbCache;
import com.thinkbiganalytics.nifi.provenance.jms.ProvenanceEventActiveMqWriter;;
import com.thinkbiganalytics.nifi.provenance.util.SpringApplicationContext;

import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Spring bean configuration for Kylo NiFi Provenance
 */
@Configuration
public class NifiProvenanceConfig {

    private static final Logger log = LoggerFactory.getLogger(NifiProvenanceConfig.class);

    public static final Integer PROVENANCE_EVENT_OBJECT_POOL_SIZE = 500;
    /**
     * location of where map db should store the persist cache file to disk
     **/
    @Value("${kylo.provenance.feedflowfile.mapdb.cache.location:/opt/nifi/feed-flowfile-cache.db}")
    private String feedFlowFileMapDbCacheLocation;

    @Bean
    public SpringApplicationContext springApplicationContext() {
        return new SpringApplicationContext();
    }

    /**
     * The KyloProvenanceEventReportingTask will override these defaults based upon its batch property ("Processing batch size")
     *
     * @return an object pool for processing ProvenanceEventRecordDTO objects
     */
    @Bean
    public ProvenanceEventObjectPool provenanceEventObjectPool() {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(PROVENANCE_EVENT_OBJECT_POOL_SIZE);
        config.setMaxIdle(PROVENANCE_EVENT_OBJECT_POOL_SIZE);
        config.setBlockWhenExhausted(false);
        config.setTestOnBorrow(false);
        config.setTestOnReturn(false);
        AbandonedConfig abandonedConfig = new AbandonedConfig();
        return new ProvenanceEventObjectPool(new ProvenanceEventObjectFactory(), config,abandonedConfig);
    }


    @Bean
    public FeedFlowFileMapDbCache feedFlowFileMapDbCache() {
        String location = feedFlowFileMapDbCacheLocation;
        return new FeedFlowFileMapDbCache(location);
    }

    @Bean
    public FeedFlowFileGuavaCache feedFlowFileGuavaCache() {
        return new FeedFlowFileGuavaCache();
    }

    @Bean
    public FeedFlowFileCacheUtil feedFlowFileCacheUtil() {
        return new FeedFlowFileCacheUtil();
    }

    @Bean
    public ProvenanceEventActiveMqWriter provenanceEventActiveMqWriter() {
        return new ProvenanceEventActiveMqWriter();
    }

    @Bean
    public ProvenanceEventCollector provenanceEventCollectorV3() {
        return new ProvenanceEventCollector();
    }


    @Bean
    public ProvenanceStatsCalculator provenanceStatsCalculator() {
        return new ProvenanceStatsCalculator();
    }



    @Bean(name = "kyloProvenanceProcessingTaskExecutor" )
    public ThreadPoolTaskExecutor kyloProvenanceProcessingTaskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(1);
        pool.setMaxPoolSize(1);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }

    @Bean
    BatchProvenanceEvents batchProvenanceEvents(){
        return new BatchEventsBySampling();
    }

}
