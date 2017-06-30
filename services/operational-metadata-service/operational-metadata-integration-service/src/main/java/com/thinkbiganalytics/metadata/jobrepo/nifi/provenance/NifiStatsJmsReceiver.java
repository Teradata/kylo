package com.thinkbiganalytics.metadata.jobrepo.nifi.provenance;

/*-
 * #%L
 * thinkbig-operational-metadata-integration-service
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

import com.thinkbiganalytics.activemq.config.ActiveMqConstants;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStatisticsProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStats;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiFeedProcessorStats;
import com.thinkbiganalytics.nifi.activemq.Queues;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStats;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**

 */
public class NifiStatsJmsReceiver {

    private static final Logger log = LoggerFactory.getLogger(NifiStatsJmsReceiver.class);

    @Inject
    private NifiFeedProcessorStatisticsProvider nifiEventStatisticsProvider;

    @Inject
    private MetadataAccess metadataAccess;

    @Inject
    private ProvenanceEventFeedUtil provenanceEventFeedUtil;


    @JmsListener(destination = Queues.PROVENANCE_EVENT_STATS_QUEUE, containerFactory = ActiveMqConstants.JMS_CONTAINER_FACTORY)
    public void receiveTopic(AggregatedFeedProcessorStatisticsHolder stats) {
        if (provenanceEventFeedUtil.isNifiFlowCacheAvailable()) {

            metadataAccess.commit(() -> {
                List<NifiFeedProcessorStats> summaryStats = createSummaryStats(stats);
                for (NifiFeedProcessorStats stat : summaryStats) {
                    nifiEventStatisticsProvider.create(stat);
                }
                return summaryStats;
            }, MetadataAccess.SERVICE);
        } else {
            log.info("NiFi is not up yet.  Sending back to JMS for later dequeue ");
            throw new JmsProcessingException("Unable to process Statistics Events.  NiFi is either not up, or there is an error trying to populate the Kylo NiFi Flow Cache. ");
        }

    }

    private List<NifiFeedProcessorStats> createSummaryStats(AggregatedFeedProcessorStatisticsHolder holder) {
        List<NifiFeedProcessorStats> nifiFeedProcessorStatsList = new ArrayList<>();
        holder.getFeedStatistics().values().stream().forEach(feedProcessorStats ->
                                                             {
                                                                 Long collectionIntervalMillis = feedProcessorStats.getCollectionIntervalMillis();
                                                                 String feedProcessorId = feedProcessorStats.getStartingProcessorId();
                                                                 String feedName = provenanceEventFeedUtil.getFeedName(feedProcessorId);  //ensure not null
                                                                 if (StringUtils.isNotBlank(feedName)) {
                                                                     String feedProcessGroupId = provenanceEventFeedUtil.getFeedProcessGroupId(feedProcessorId);

                                                                     feedProcessorStats.getProcessorStats().values().forEach(processorStats ->
                                                                                                                             {
                                                                                                                                 processorStats.getStats().values().stream().forEach(stats -> {
                                                                                                                                     NifiFeedProcessorStats
                                                                                                                                         nifiFeedProcessorStats =
                                                                                                                                         toSummaryStats(stats);
                                                                                                                                     nifiFeedProcessorStats.setFeedName(feedName);
                                                                                                                                     nifiFeedProcessorStats
                                                                                                                                         .setProcessorId(processorStats.getProcessorId());
                                                                                                                                     nifiFeedProcessorStats.setCollectionIntervalSeconds(
                                                                                                                                         (collectionIntervalMillis / 1000));
                                                                                                                                     String
                                                                                                                                         processorName =
                                                                                                                                         provenanceEventFeedUtil
                                                                                                                                             .getProcessorName(processorStats.getProcessorId());
                                                                                                                                     nifiFeedProcessorStats.setProcessorName(processorName);
                                                                                                                                     nifiFeedProcessorStats
                                                                                                                                         .setFeedProcessGroupId(feedProcessGroupId);
                                                                                                                                     nifiFeedProcessorStatsList.add(nifiFeedProcessorStats);
                                                                                                                                 });
                                                                                                                             });
                                                                 }

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
        nifiFeedProcessorStats.setCollectionTime(new DateTime(groupedStats.getTime()));
        nifiFeedProcessorStats.setMinEventTime(new DateTime(groupedStats.getMinTime()));
        nifiFeedProcessorStats.setMaxEventTime(new DateTime(groupedStats.getMaxTime()));
        nifiFeedProcessorStats.setJobsFailed(groupedStats.getJobsFailed());
        nifiFeedProcessorStats.setSuccessfulJobDuration(groupedStats.getSuccessfulJobDuration());
        nifiFeedProcessorStats.setJobDuration(groupedStats.getJobDuration());
        nifiFeedProcessorStats.setMaxEventId(groupedStats.getMaxEventId());
        nifiFeedProcessorStats.setFailedCount(groupedStats.getProcessorsFailed());
        if (provenanceEventFeedUtil.isFailure(groupedStats.getSourceConnectionIdentifier())) {
            nifiFeedProcessorStats.setFailedCount(groupedStats.getTotalCount() + groupedStats.getProcessorsFailed());
        }

        return nifiFeedProcessorStats;
    }
}
