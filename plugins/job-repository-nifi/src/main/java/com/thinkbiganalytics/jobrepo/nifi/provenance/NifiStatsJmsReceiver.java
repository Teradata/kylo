package com.thinkbiganalytics.jobrepo.nifi.provenance;

import com.thinkbiganalytics.activemq.config.ActiveMqConstants;
import com.thinkbiganalytics.jobrepo.config.OperationalMetadataAccess;
import com.thinkbiganalytics.jobrepo.jpa.NifiEventStatisticsProvider;
import com.thinkbiganalytics.jobrepo.jpa.NifiEventSummaryStats;
import com.thinkbiganalytics.nifi.activemq.Queues;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;

import org.springframework.jms.annotation.JmsListener;

import java.util.UUID;

import javax.inject.Inject;

/**
 * Created by sr186054 on 8/17/16.
 */
public class NifiStatsJmsReceiver {


    @Inject
    private NifiEventStatisticsProvider nifiEventStatisticsProvider;

    @Inject
    private OperationalMetadataAccess operationalMetadataAccess;


    @JmsListener(destination = Queues.PROVENANCE_EVENT_STATS_QUEUE, containerFactory = ActiveMqConstants.JMS_CONTAINER_FACTORY)
    public void receiveTopic(AggregatedFeedProcessorStatisticsHolder stats) {

        operationalMetadataAccess.commit(() -> {
            String feedName = UUID.randomUUID().toString();
            nifiEventStatisticsProvider.create(new NifiEventSummaryStats(feedName, UUID.randomUUID().toString()));
            return null;
        });

    }
}
