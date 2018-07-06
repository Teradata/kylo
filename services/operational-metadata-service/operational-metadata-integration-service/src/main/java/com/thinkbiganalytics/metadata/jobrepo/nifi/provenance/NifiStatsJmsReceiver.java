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

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.EvictingQueue;
import com.thinkbiganalytics.cluster.ClusterMessage;
import com.thinkbiganalytics.cluster.ClusterService;
import com.thinkbiganalytics.cluster.ClusterServiceMessageReceiver;
import com.thinkbiganalytics.jms.JmsConstants;
import com.thinkbiganalytics.jms.Queues;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorErrors;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStatisticsProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStats;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedStatisticsProvider;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiFeedProcessorStats;
import com.thinkbiganalytics.metadata.jpa.jobrepo.nifi.JpaNifiFeedStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolderV2;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolderV3;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsV2;
import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStatsV2;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.BulletinDTO;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

/**
 */
public class NifiStatsJmsReceiver implements ClusterServiceMessageReceiver {

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

    @Inject
    private FeedStatsUpdater feedStatsUpdater;

    public static String NIFI_FEED_PROCESSOR_ERROR_CLUSTER_TYPE = "NIFI_FEED_PROCESSOR_ERROR";

    @Inject
    private ClusterService clusterService;

    @Value("${kylo.ops.mgr.stats.nifi.bulletins.mem.size:30}")
    private Integer errorsToStorePerFeed = 30;

    @Value("${kylo.ops.mgr.stats.nifi.bulletins.persist:false}")
    private boolean persistErrors = false;

    @Value("${kylo.ops.mgr.stats.nifi.bulletins.enabled:false}")
    private boolean bulletinsEnabled = false;

    @Value("${kylo.ops.mgr.feed-stats.defer-update:true}")
    private boolean deferFeedUpdate = true;

    private LoadingCache<String,Queue<NifiFeedProcessorErrors>> feedProcessorErrors = CacheBuilder.newBuilder().build(new CacheLoader<String, Queue<NifiFeedProcessorErrors>>() {
        @Override
        public Queue<NifiFeedProcessorErrors> load(String feedName) throws Exception {
            return EvictingQueue.create(errorsToStorePerFeed);
        }

    });

    private Long lastBulletinId = -1L;

    /**
     * get Errors in memory for a feed
     *
     * @param feedName       the feed name
     * @param afterTimestamp and optional timestamp to look after
     */
    public List<NifiFeedProcessorErrors> getErrorsForFeed(String feedName, Long afterTimestamp) {
        return getErrorsForFeed(feedName, afterTimestamp, null);
    }

    public List<NifiFeedProcessorErrors> getErrorsForFeed(String feedName, Long startTime, Long endTime) {
        List<NifiFeedProcessorErrors> errors = null;
        Queue<NifiFeedProcessorErrors> queue = feedProcessorErrors.getUnchecked(feedName);
        if (queue != null) {
            if (startTime != null && endTime != null) {

                if (queue != null) {
                    errors =
                        queue.stream().filter(error -> error.getErrorMessageTimestamp().getMillis() >= startTime && error.getErrorMessageTimestamp().getMillis() <= endTime)
                            .collect(Collectors.toList());
                }
            } else if (startTime == null && endTime != null) {
                errors = queue.stream().filter(error -> error.getErrorMessageTimestamp().getMillis() <= endTime).collect(Collectors.toList());
            } else if (startTime != null && endTime == null) {
                errors = queue.stream().filter(error -> error.getErrorMessageTimestamp().getMillis() >= startTime).collect(Collectors.toList());
            } else {
                errors = new ArrayList<>();
                errors.addAll(queue);
            }
        } else {
            errors = Collections.emptyList();
        }

        return errors;
    }

    private String getFeedName(AggregatedFeedProcessorStatistics feedProcessorStatistics) {
        String feedName = null;
        String feedProcessorId = feedProcessorStatistics.getStartingProcessorId();
        if (feedProcessorStatistics instanceof AggregatedFeedProcessorStatisticsV2) {
            feedName = ((AggregatedFeedProcessorStatisticsV2) feedProcessorStatistics).getFeedName();
        }
        if (feedProcessorId != null && feedName == null) {
            feedName = provenanceEventFeedUtil.getFeedName(feedProcessorId);
        }
        return feedName;
    }

    /**
     * Ensure the cache and NiFi are up, or if not ensure the data exists in the NiFi cache to be processed
     *
     * @param stats the stats to process
     */
    public boolean readyToProcess(AggregatedFeedProcessorStatisticsHolder stats) {
        return provenanceEventFeedUtil.isNifiFlowCacheAvailable() || (!provenanceEventFeedUtil.isNifiFlowCacheAvailable() && stats.getFeedStatistics().values().stream()
            .allMatch(feedProcessorStats -> StringUtils.isNotBlank(getFeedName(feedProcessorStats))));
    }


    @JmsListener(destination = Queues.PROVENANCE_EVENT_STATS_QUEUE, containerFactory = JmsConstants.JMS_CONTAINER_FACTORY)
    public void receiveTopic(AggregatedFeedProcessorStatisticsHolder stats) {
        if (readyToProcess(stats)) {
            Stopwatch recieveTopicStopwatch = Stopwatch.createStarted();

            metadataAccess.commit(() -> {
                List<NifiFeedProcessorStats> summaryStats = createSummaryStats(stats);

                List<JpaNifiFeedProcessorStats> failedStatsWithFlowFiles = new ArrayList<>();
                for (NifiFeedProcessorStats stat : summaryStats) {
                    NifiFeedProcessorStats savedStats = nifiEventStatisticsProvider.create(stat);
                    if (savedStats.getFailedCount() > 0L && savedStats.getLatestFlowFileId() != null) {
                        //offload the query to nifi and merge back in
                        failedStatsWithFlowFiles.add((JpaNifiFeedProcessorStats) savedStats);
                    }
                }
                if(stats instanceof AggregatedFeedProcessorStatisticsHolderV2) {
                   Map<String,JpaNifiFeedStats> updatedFeedStats = saveFeedStats((AggregatedFeedProcessorStatisticsHolderV2)stats, summaryStats,!deferFeedUpdate);

                }
                if( bulletinsEnabled && !failedStatsWithFlowFiles.isEmpty()){
                    assignNiFiBulletinErrors(failedStatsWithFlowFiles);
                }
                return summaryStats;
            }, MetadataAccess.SERVICE);
            recieveTopicStopwatch.stop();
            log.debug("Time to recieveTopic {} ms ",recieveTopicStopwatch.elapsed(TimeUnit.MILLISECONDS));
        } else {
            log.info("NiFi is not up yet.  Sending back to JMS for later dequeue ");
            throw new JmsProcessingException("Unable to process Statistics Events.  NiFi is either not up, or there is an error trying to populate the Kylo NiFi Flow Cache. ");
        }
    }


    private void assignNiFiBulletinErrors(List<JpaNifiFeedProcessorStats> stats) {

        //might need to query with the 'after' parameter

        //group the FeedStats by processorId_flowfileId

        Map<String, Map<String, List<JpaNifiFeedProcessorStats>>>
            processorFlowFilesStats =
            stats.stream().filter(s -> s.getProcessorId() != null)
                .collect(Collectors.groupingBy(NifiFeedProcessorStats::getProcessorId, Collectors.groupingBy(NifiFeedProcessorStats::getLatestFlowFileId)));

        Set<String> processorIds = processorFlowFilesStats.keySet();
        //strip out those processorIds that are part of a reusable flow
        Set<String> nonReusableFlowProcessorIds = processorIds.stream().filter(processorId -> !provenanceEventFeedUtil.isReusableFlowProcessor(processorId)).collect(Collectors.toSet());

        //find all errors for the processors
        List<BulletinDTO> errors = nifiBulletinExceptionExtractor.getErrorBulletinsForProcessorId(processorIds, lastBulletinId);

        if (errors != null && !errors.isEmpty()) {
            Set<JpaNifiFeedProcessorStats> statsToUpdate = new HashSet<>();
            // first look for matching feed flow and processor ids.  otherwise look for processor id matches that are not part of reusable flows
            errors.stream().forEach(b -> {
                stats.stream().forEach(stat -> {
                    if (stat.getLatestFlowFileId() != null && b.getSourceId().equalsIgnoreCase(stat.getProcessorId()) && b.getMessage().contains(stat.getLatestFlowFileId())) {
                        stat.setErrorMessageTimestamp(getAdjustBulletinDateTime(b));
                        stat.setErrorMessages(b.getMessage());
                        addFeedProcessorError(stat);
                        statsToUpdate.add(stat);
                    } else if (nonReusableFlowProcessorIds.contains(b.getSourceId()) && b.getSourceId().equalsIgnoreCase(stat.getProcessorId())) {
                        stat.setErrorMessageTimestamp(getAdjustBulletinDateTime(b));
                        stat.setErrorMessages(b.getMessage());
                        addFeedProcessorError(stat);
                        statsToUpdate.add(stat);
                    }
                });
            });
            lastBulletinId = errors.stream().mapToLong(b -> b.getId()).max().getAsLong();

            if (!statsToUpdate.isEmpty()) {
                notifyClusterOfFeedProcessorErrors(statsToUpdate);
                if (persistErrors) {
                    nifiEventStatisticsProvider.save(new ArrayList<>(statsToUpdate));
                }
            }
        }
    }

    /**
     * the BulletinDTO comes back from nifi as a Date object in the year 1970
     * We need to convert this to the current date and account for DST
     *
     * @param b the bulletin
     */
    private DateTime getAdjustBulletinDateTime(BulletinDTO b) {
        DateTimeZone defaultZone = DateTimeZone.getDefault();

        int currentOffsetMillis = defaultZone.getOffset(DateTime.now().getMillis());
        double currentOffsetHours = (double) currentOffsetMillis / 1000d / 60d / 60d;

        long bulletinOffsetMillis = DateTimeZone.getDefault().getOffset(b.getTimestamp().getTime());

        double bulletinOffsetHours = (double) bulletinOffsetMillis / 1000d / 60d / 60d;

        DateTime adjustedTime = new DateTime(b.getTimestamp()).withDayOfYear(DateTime.now().getDayOfYear()).withYear(DateTime.now().getYear());
        int adjustedHours = 0;
        if (currentOffsetHours != bulletinOffsetHours) {
            adjustedHours = new Double(bulletinOffsetHours - currentOffsetHours).intValue();
            adjustedTime = adjustedTime.plusHours(-adjustedHours);
        }
        return adjustedTime;
    }

    private void addFeedProcessorError(NifiFeedProcessorErrors error) {
        Queue<NifiFeedProcessorErrors> q = feedProcessorErrors.getUnchecked(error.getFeedName());
        if (q != null) {
            q.add(error);
        }
    }


    /**
     * Save the running totals for the feed
     */
    private Map<String, JpaNifiFeedStats> saveFeedStats(AggregatedFeedProcessorStatisticsHolderV2 holder, List<NifiFeedProcessorStats> summaryStats,boolean saveStats) {
        Map<String, JpaNifiFeedStats> feedStatsMap = new HashMap<>();

        if (summaryStats != null) {
            Map<String, Long> feedLatestTimestamp = summaryStats.stream().collect(Collectors.toMap(NifiFeedProcessorStats::getFeedName, stats -> stats.getMinEventTime().getMillis(), Long::max));
            feedLatestTimestamp.entrySet().stream().forEach(e -> {
                String feedName = e.getKey();
                Long timestamp = e.getValue();
                JpaNifiFeedStats stats = feedStatsMap.computeIfAbsent(feedName, name -> new JpaNifiFeedStats(feedName));
                OpsManagerFeed opsManagerFeed = provenanceEventFeedUtil.getFeed(feedName);
                if (opsManagerFeed != null) {
                    stats.setFeedId(new JpaNifiFeedStats.OpsManagerFeedId(opsManagerFeed.getId().toString()));
                }
                stats.setLastActivityTimestamp(timestamp);
            });
        }
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
                    if (holder instanceof AggregatedFeedProcessorStatisticsHolderV3) {
                        stats.setTime(((AggregatedFeedProcessorStatisticsHolderV3) holder).getTimestamp());
                        if (stats.getLastActivityTimestamp() == null) {
                            stats.setLastActivityTimestamp(((AggregatedFeedProcessorStatisticsHolderV3) holder).getTimestamp());
                        }
                    } else {
                        stats.setTime(DateTime.now().getMillis());
                    }
                    if (stats.getLastActivityTimestamp() == null) {
                        log.warn("The JpaNifiFeedStats.lastActivityTimestamp for the feed {} is NULL.  The JMS Class was: {}", feedName, holder.getClass().getSimpleName());
                    }
                }
            });
        }

        if (!feedStatsMap.isEmpty()) {
            if(saveStats) {
                nifiFeedStatisticsProvider.saveLatestFeedStats(new ArrayList<>(feedStatsMap.values()));
                if (log.isDebugEnabled()) {
                    log.debug("Saving Stats for {} ", feedStatsMap.values().stream().map(s -> s.toString()).collect(Collectors.joining("\n")));
                }
            }
            else {
                feedStatsUpdater.updateStats(feedStatsMap);
            }
            nifiFeedStatisticsProvider.saveLatestFeedStats(new ArrayList<>(feedStatsMap.values()));
        }
        return feedStatsMap;
    }






    private List<NifiFeedProcessorStats> createSummaryStats(AggregatedFeedProcessorStatisticsHolder holder) {
        List<NifiFeedProcessorStats> nifiFeedProcessorStatsList = new ArrayList<>();
        holder.getFeedStatistics().values().stream().forEach(feedProcessorStats -> {
            Long collectionIntervalMillis = feedProcessorStats.getCollectionIntervalMillis();
            String feedProcessorId = feedProcessorStats.getStartingProcessorId();
            String feedName = getFeedName(feedProcessorStats);
            if (StringUtils.isNotBlank(feedName)) {
                String feedProcessGroupId = provenanceEventFeedUtil.getFeedProcessGroupId(feedProcessorId);

                feedProcessorStats.getProcessorStats().values().forEach(processorStats -> {
                    processorStats.getStats().values().stream().forEach(stats -> {
                        NifiFeedProcessorStats
                            nifiFeedProcessorStats =
                            toSummaryStats(stats);
                        nifiFeedProcessorStats.setFeedName(feedName);
                        nifiFeedProcessorStats
                            .setProcessorId(processorStats.getProcessorId());
                        nifiFeedProcessorStats.setCollectionIntervalSeconds(
                            (collectionIntervalMillis / 1000));
                        if (holder instanceof AggregatedFeedProcessorStatisticsHolderV2) {
                            nifiFeedProcessorStats
                                .setCollectionId(((AggregatedFeedProcessorStatisticsHolderV2) holder).getCollectionId());
                        }
                        String
                            processorName =
                            provenanceEventFeedUtil
                                .getProcessorName(processorStats.getProcessorId());
                        if (processorName == null) {
                            processorName = processorStats.getProcessorName();
                        }
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
        if (groupedStats instanceof GroupedStatsV2) {
            nifiFeedProcessorStats.setLatestFlowFileId(((GroupedStatsV2) groupedStats).getLatestFlowFileId());
        }
        if (provenanceEventFeedUtil.isFailure(groupedStats.getSourceConnectionIdentifier())) {
            nifiFeedProcessorStats.setFailedCount(groupedStats.getTotalCount() + groupedStats.getProcessorsFailed());
        }

        return nifiFeedProcessorStats;
    }


    public boolean isPersistErrors() {
        return persistErrors;
    }

    @Override
    public void onMessageReceived(String from, ClusterMessage message) {

        if (message != null && NIFI_FEED_PROCESSOR_ERROR_CLUSTER_TYPE.equalsIgnoreCase(message.getType())) {
            NifiFeedProcessorStatsErrorClusterMessage content = (NifiFeedProcessorStatsErrorClusterMessage) message.getMessage();
            if (content != null && content.getErrors() != null) {
                content.getErrors().stream().forEach(error -> addFeedProcessorError(error));
            }
        }
    }

    private void notifyClusterOfFeedProcessorErrors(Set<? extends NifiFeedProcessorErrors> errors) {
        if (clusterService.isClustered()) {
            clusterService.sendMessageToOthers(NIFI_FEED_PROCESSOR_ERROR_CLUSTER_TYPE, new NifiFeedProcessorStatsErrorClusterMessage(errors));
        }
    }
}
