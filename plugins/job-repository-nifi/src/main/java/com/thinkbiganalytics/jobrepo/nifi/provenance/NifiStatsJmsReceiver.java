package com.thinkbiganalytics.jobrepo.nifi.provenance;

import com.thinkbiganalytics.activemq.config.ActiveMqConstants;
import com.thinkbiganalytics.jobrepo.config.OperationalMetadataAccess;
import com.thinkbiganalytics.jobrepo.jpa.NifiEventStatisticsProvider;
import com.thinkbiganalytics.jobrepo.jpa.NifiEventSummaryStats;
import com.thinkbiganalytics.nifi.activemq.Queues;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStats;

import org.springframework.jms.annotation.JmsListener;

import java.util.ArrayList;
import java.util.List;

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
            List<NifiEventSummaryStats> summaryStats = createSummaryStats(stats);
            for (NifiEventSummaryStats stat : summaryStats) {
                nifiEventStatisticsProvider.create(stat);
            }
            return summaryStats;
        });

    }

    private List<NifiEventSummaryStats> createSummaryStats(AggregatedFeedProcessorStatisticsHolder holder) {
        List<NifiEventSummaryStats> nifiEventSummaryStatsList = new ArrayList<>();
        holder.getStatistics().forEach(feedProcessorStats ->
                                       {
                                           String feedName = feedProcessorStats.getFeedName();
                                           feedProcessorStats.getProcessorStats().values().forEach(processorStats ->
                                                                                                   {
                                                                                                       NifiEventSummaryStats nifiEventSummaryStats = toSummaryStats(processorStats.getStats());
                                                                                                       nifiEventSummaryStats.setFeedName(feedName);
                                                                                                       nifiEventSummaryStats.setProcessorId(processorStats.getProcessorId());
                                                                                                       nifiEventSummaryStats.setFeedProcessGroupId(feedProcessorStats.getProcessGroup());
                                                                                                       nifiEventSummaryStatsList.add(nifiEventSummaryStats);
                                                                                                   });

                                       });
        return nifiEventSummaryStatsList;

    }

    private NifiEventSummaryStats toSummaryStats(GroupedStats groupedStats) {
        NifiEventSummaryStats nifiEventSummaryStats = new NifiEventSummaryStats();
        nifiEventSummaryStats.setTotalCount(groupedStats.getTotalCount());
        nifiEventSummaryStats.setFlowFilesFinished(groupedStats.getFlowFilesFinished());
        nifiEventSummaryStats.setFlowFilesStarted(groupedStats.getFlowFilesStarted());
        nifiEventSummaryStats.setCollectionId(groupedStats.getGroupKey());
        nifiEventSummaryStats.setBytesIn(groupedStats.getBytesIn());
        nifiEventSummaryStats.setBytesOut(groupedStats.getBytesOut());
        nifiEventSummaryStats.setDuration(groupedStats.getDuration());
        nifiEventSummaryStats.setJobsFinished(groupedStats.getJobsFinished());
        nifiEventSummaryStats.setJobsStarted(groupedStats.getJobsStarted());
        nifiEventSummaryStats.setProcessorsFailed(groupedStats.getProcessorsFailed());
        nifiEventSummaryStats.setCollectionTime(groupedStats.getTime());
        nifiEventSummaryStats.setMinEventTime(groupedStats.getMinTime());
        nifiEventSummaryStats.setMaxEventTime(groupedStats.getMaxTime());
        nifiEventSummaryStats.setJobsFailed(groupedStats.getJobsFailed());
        nifiEventSummaryStats.setSuccessfulJobDuration(groupedStats.getSuccessfulJobDuration());
        nifiEventSummaryStats.setJobDuration(groupedStats.getJobDuration());
        return nifiEventSummaryStats;
    }
}
