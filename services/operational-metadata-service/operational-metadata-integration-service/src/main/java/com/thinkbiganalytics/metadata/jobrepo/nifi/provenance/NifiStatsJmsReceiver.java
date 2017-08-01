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

import com.thinkbiganalytics.jms.JmsConstants;
import com.thinkbiganalytics.jms.Queues;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStatisticsProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStats;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedStatisticsProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedStats;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiFeedProcessorStats;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiFeedStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolderV2;
import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStatsV2;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.BulletinDTO;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Inject
    private NifiFeedStatisticsProvider nifiFeedStatisticsProvider;

    @Inject
    private NifiBulletinExceptionExtractor nifiBulletinExceptionExtractor;

    private Long lastBulletinId = -1L;

    /**
     * Ensure the cache and NiFi are up, or if not ensure the data exists in the NiFi cache to be processed
     *
     * @param stats the stats to process
     */
    public boolean readyToProcess(AggregatedFeedProcessorStatisticsHolder stats) {
        return provenanceEventFeedUtil.isNifiFlowCacheAvailable() || (!provenanceEventFeedUtil.isNifiFlowCacheAvailable() && stats.getFeedStatistics().values().stream()
            .allMatch(feedProcessorStats -> {
                String feedProcessorId = feedProcessorStats.getStartingProcessorId();
                String feedName = provenanceEventFeedUtil.getFeedName(feedProcessorId);
                return StringUtils.isNotBlank(feedName);
            }));
    }


    @JmsListener(destination = Queues.PROVENANCE_EVENT_STATS_QUEUE, containerFactory = JmsConstants.JMS_CONTAINER_FACTORY)
    public void receiveTopic(AggregatedFeedProcessorStatisticsHolder stats) {
        if (readyToProcess(stats)) {

            metadataAccess.commit(() -> {
                List<NifiFeedProcessorStats> summaryStats = createSummaryStats(stats);

                List<JpaNifiFeedProcessorStats> failedStatsWithFlowFiles = new ArrayList<>();
                for (NifiFeedProcessorStats stat : summaryStats) {
                   NifiFeedProcessorStats savedStats = nifiEventStatisticsProvider.create(stat);
                   if(savedStats.getFailedCount() >0L && savedStats.getLatestFlowFileId() != null){
                       //offload the query to nifi and merge back in
                       failedStatsWithFlowFiles.add((JpaNifiFeedProcessorStats)savedStats);
                   }
                }
                if(stats instanceof AggregatedFeedProcessorStatisticsHolderV2) {
                    saveFeedStats((AggregatedFeedProcessorStatisticsHolderV2)stats);
                }
                if(!failedStatsWithFlowFiles.isEmpty()){
                    assignNiFiBulletinErrors(failedStatsWithFlowFiles);
                }
                return summaryStats;
            }, MetadataAccess.SERVICE);
        } else {
            log.info("NiFi is not up yet.  Sending back to JMS for later dequeue ");
            throw new JmsProcessingException("Unable to process Statistics Events.  NiFi is either not up, or there is an error trying to populate the Kylo NiFi Flow Cache. ");
        }

    }





    private void assignNiFiBulletinErrors(List<JpaNifiFeedProcessorStats> stats) {

        //might need to query with the 'after' parameter

        //group the FeedStats by processorId_flowfileId

        Map<String, Map<String, List<JpaNifiFeedProcessorStats>>> processorFlowFilesStats = stats.stream().filter(s->s.getProcessorId() != null).collect(Collectors.groupingBy(NifiFeedProcessorStats::getProcessorId,Collectors.groupingBy(NifiFeedProcessorStats::getLatestFlowFileId)));


       Set<String> processorIds = processorFlowFilesStats.keySet();
       //strip out those processorIds that are part of a reusable flow
        Set<String> nonReusableFlowProcessorIds = processorIds.stream().filter(processorId -> !provenanceEventFeedUtil.isReusableFlowProcessor(processorId)).collect(Collectors.toSet());

        //find all errors for the processors
        List<BulletinDTO> errors = nifiBulletinExceptionExtractor.getErrorBulletinsForProcessorId(processorIds, lastBulletinId);

        if(errors != null){
            Set<JpaNifiFeedProcessorStats> statsToUpdate = new HashSet<>();
            // first look for matching feed flow and processor ids.  otherwise look for processor id matches that are not part of reusable flows
            errors.stream().forEach(b -> {
                stats.stream().forEach(stat -> {
                    if(stat.getLatestFlowFileId() != null && b.getSourceId().equalsIgnoreCase(stat.getProcessorId()) && b.getMessage().contains(stat.getLatestFlowFileId())){
                        stat.setErrorMessageTimestamp(new DateTime(b.getTimestamp()));
                        stat.setErrorMessages(b.getMessage());
                        statsToUpdate.add(stat);
                    }
                    else if(nonReusableFlowProcessorIds.contains(b.getSourceId()) && b.getSourceId().equalsIgnoreCase(stat.getProcessorId())){
                        stat.setErrorMessageTimestamp(new DateTime(b.getTimestamp()));
                        stat.setErrorMessages(b.getMessage());
                        statsToUpdate.add(stat);
                    }
                });
            });
            lastBulletinId = errors.stream().mapToLong(b -> b.getId()).max().getAsLong();
            if(!statsToUpdate.isEmpty()) {
                nifiEventStatisticsProvider.save(new ArrayList<>(statsToUpdate));
            }
        }
    }




    /**
     * Save the running totals for the feed
     */
    private Map<String,JpaNifiFeedStats> saveFeedStats(AggregatedFeedProcessorStatisticsHolderV2 holder) {
       Map<String,JpaNifiFeedStats> feedStatsMap = new HashMap<>();
        if (holder.getProcessorIdRunningFlows() != null) {
            holder.getProcessorIdRunningFlows().entrySet().stream().forEach(e -> {
                String feedProcessorId = e.getKey();
                Long runningCount = e.getValue();
                String feedName = provenanceEventFeedUtil.getFeedName(feedProcessorId);  //ensure not null
                if (StringUtils.isNotBlank(feedName)) {
                    JpaNifiFeedStats stats = feedStatsMap.computeIfAbsent(feedName, name -> new JpaNifiFeedStats(feedName));
                    OpsManagerFeed opsManagerFeed = provenanceEventFeedUtil.getFeed(feedName);
                    if (opsManagerFeed != null) {
                        stats.setFeedId(new JpaNifiFeedStats.OpsManagerFeedId(opsManagerFeed.getId().toString()));
                    }
                    stats.addRunningFeedFlows(runningCount);
                    stats.setTime(DateTime.now().getMillis());
                }
            });
        }
        //group stats to save together by feed name
        if (!feedStatsMap.isEmpty()) {
            nifiFeedStatisticsProvider.saveLatestFeedStats(new ArrayList<>(feedStatsMap.values()));
        }
        return feedStatsMap;
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
                                                                                                                                     if(holder instanceof AggregatedFeedProcessorStatisticsHolderV2) {
                                                                                                                                         nifiFeedProcessorStats
                                                                                                                                             .setCollectionId(((AggregatedFeedProcessorStatisticsHolderV2)holder).getCollectionId());
                                                                                                                                     }
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
        if(groupedStats instanceof GroupedStatsV2) {
            nifiFeedProcessorStats.setLatestFlowFileId(((GroupedStatsV2)groupedStats).getLatestFlowFileId());
        }
        if (provenanceEventFeedUtil.isFailure(groupedStats.getSourceConnectionIdentifier())) {
            nifiFeedProcessorStats.setFailedCount(groupedStats.getTotalCount() + groupedStats.getProcessorsFailed());
        }

        return nifiFeedProcessorStats;
    }
}
