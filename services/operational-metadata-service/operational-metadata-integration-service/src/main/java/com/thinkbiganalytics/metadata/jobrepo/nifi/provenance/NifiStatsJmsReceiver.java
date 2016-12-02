package com.thinkbiganalytics.metadata.jobrepo.nifi.provenance;

import com.thinkbiganalytics.activemq.config.ActiveMqConstants;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStatisticsProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStats;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiFeedProcessorStats;
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
    private NifiFeedProcessorStatisticsProvider nifiEventStatisticsProvider;

    @Inject
    private MetadataAccess metadataAccess;


    @JmsListener(destination = Queues.PROVENANCE_EVENT_STATS_QUEUE, containerFactory = ActiveMqConstants.JMS_CONTAINER_FACTORY)
    public void receiveTopic(AggregatedFeedProcessorStatisticsHolder stats) {

        metadataAccess.commit(() -> {
            List<NifiFeedProcessorStats> summaryStats = createSummaryStats(stats);
            for (NifiFeedProcessorStats stat : summaryStats) {
                nifiEventStatisticsProvider.create(stat);
            }
            return summaryStats;
        }, MetadataAccess.SERVICE);

    }

    private List<NifiFeedProcessorStats> createSummaryStats(AggregatedFeedProcessorStatisticsHolder holder) {
        List<NifiFeedProcessorStats> nifiFeedProcessorStatsList = new ArrayList<>();
        holder.getFeedStatistics().values().stream().forEach(feedProcessorStats ->
                                                             {
                                                                 String feedName = feedProcessorStats.getFeedName();
                                                                 feedProcessorStats.getProcessorStats().values().forEach(processorStats ->
                                                                                                                         {
                                                                                                                             NifiFeedProcessorStats
                                                                                                                                 nifiFeedProcessorStats =
                                                                                                                                 toSummaryStats(processorStats.getStats());
                                                                                                                             nifiFeedProcessorStats.setFeedName(feedName);
                                                                                                                             nifiFeedProcessorStats.setProcessorId(processorStats.getProcessorId());
                                                                                                                             nifiFeedProcessorStats.setProcessorName(processorStats.getProcessorName());
                                                                                                                             nifiFeedProcessorStats
                                                                                                                                 .setFeedProcessGroupId(feedProcessorStats.getProcessGroup());
                                                                                                                             nifiFeedProcessorStatsList.add(nifiFeedProcessorStats);
                                                                                                                         });

                                                             });
        return nifiFeedProcessorStatsList;

    }

    private NifiFeedProcessorStats toSummaryStats(GroupedStats groupedStats) {
        NifiFeedProcessorStats nifiFeedProcessorStats = new JpaNifiFeedProcessorStats();
        nifiFeedProcessorStats.setTotalCount(groupedStats.getTotalCount());
        nifiFeedProcessorStats.setFlowFilesFinished(groupedStats.getFlowFilesFinished());
        nifiFeedProcessorStats.setFlowFilesStarted(groupedStats.getFlowFilesStarted());
        nifiFeedProcessorStats.setCollectionId(groupedStats.getGroupKey());
        nifiFeedProcessorStats.setBytesIn(groupedStats.getBytesIn());
        nifiFeedProcessorStats.setBytesOut(groupedStats.getBytesOut());
        nifiFeedProcessorStats.setDuration(groupedStats.getDuration());
        nifiFeedProcessorStats.setJobsFinished(groupedStats.getJobsFinished());
        nifiFeedProcessorStats.setJobsStarted(groupedStats.getJobsStarted());
        nifiFeedProcessorStats.setProcessorsFailed(groupedStats.getProcessorsFailed());
        nifiFeedProcessorStats.setCollectionTime(groupedStats.getTime());
        nifiFeedProcessorStats.setMinEventTime(groupedStats.getMinTime());
        nifiFeedProcessorStats.setMaxEventTime(groupedStats.getMaxTime());
        nifiFeedProcessorStats.setJobsFailed(groupedStats.getJobsFailed());
        nifiFeedProcessorStats.setSuccessfulJobDuration(groupedStats.getSuccessfulJobDuration());
        nifiFeedProcessorStats.setJobDuration(groupedStats.getJobDuration());
        return nifiFeedProcessorStats;
    }
}
