package com.thinkbiganalytics.nifi.provenance.config;

import com.thinkbiganalytics.nifi.provenance.ProvenanceEventCollector;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventObjectFactory;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventObjectPool;
import com.thinkbiganalytics.nifi.provenance.ProvenanceFeedLookup;
import com.thinkbiganalytics.nifi.provenance.ProvenanceStatsCalculator;
import com.thinkbiganalytics.nifi.provenance.cache.FeedFlowFileCacheUtil;
import com.thinkbiganalytics.nifi.provenance.cache.FeedFlowFileGuavaCache;
import com.thinkbiganalytics.nifi.provenance.cache.FeedFlowFileMapDbCache;
import com.thinkbiganalytics.nifi.provenance.jms.ProvenanceEventActiveMqWriter;
import com.thinkbiganalytics.nifi.provenance.util.SpringApplicationContext;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring bean configuration for Kylo NiFi Provenance
 */
@Configuration
public class NifiProvenanceConfig {

    private static final Logger log = LoggerFactory.getLogger(NifiProvenanceConfig.class);

    @Bean
    public SpringApplicationContext springApplicationContext() {
        return new SpringApplicationContext();
    }

    @Bean
    public ProvenanceEventObjectPool provenanceEventObjectPool() {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxIdle(1000);
        config.setMaxTotal(1000);
        config.setMinIdle(1);
        config.setBlockWhenExhausted(false);
        config.setTestOnBorrow(false);
        config.setTestOnReturn(false);
        return new ProvenanceEventObjectPool(new ProvenanceEventObjectFactory(), config);
    }

    @Bean
    public FeedFlowFileMapDbCache feedFlowFileMapDbCache() {
        return new FeedFlowFileMapDbCache();
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
    public ProvenanceEventCollector provenanceEventCollector() {
        return new ProvenanceEventCollector(provenanceEventActiveMqWriter());
    }

    @Bean
    public ProvenanceStatsCalculator provenanceStatsCalculator() {
        return new ProvenanceStatsCalculator();
    }

    @Bean
    public ProvenanceFeedLookup provenanceFeedLookup() {
        return new ProvenanceFeedLookup();
    }

}
