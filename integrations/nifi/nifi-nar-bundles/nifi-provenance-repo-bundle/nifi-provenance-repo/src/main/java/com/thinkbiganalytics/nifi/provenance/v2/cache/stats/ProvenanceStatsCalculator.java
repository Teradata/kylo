package com.thinkbiganalytics.nifi.provenance.v2.cache.stats;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.FeedProcessorStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.ProcessorStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.ProvenanceEventStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.StatsModel;
import com.thinkbiganalytics.nifi.provenance.v2.writer.ProvenanceEventActiveMqWriter;
import com.thinkbiganalytics.util.SpringApplicationContext;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 8/15/16.
 */
public class ProvenanceStatsCalculator {

    private static final Logger log = LoggerFactory.getLogger(ProvenanceStatsCalculator.class);

    private static ProvenanceStatsCalculator instance = new ProvenanceStatsCalculator();

    public static ProvenanceStatsCalculator instance() {
        return instance;
    }

    private DateTime firstEventTime;

    private DateTime lastSendTime;

    private Integer aggregationIntervalSeconds = 10;


    private final LoadingCache<String, ProcessorStats> processorStatsLoadingCache;

    private final LoadingCache<String, FeedProcessorStats> feedProcessorStatsLoadingCache;

    private Lock aggregatingStatsLock = null;

    private ProvenanceStatsCalculator() {

        processorStatsLoadingCache = CacheBuilder.newBuilder().recordStats().build(new CacheLoader<String, ProcessorStats>() {
                                                                                       @Override
                                                                                       public ProcessorStats load(String processorId) throws Exception {
                                                                                           ProcessorStats stats = new ProcessorStats(processorId);
                                                                                           return stats;
                                                                                        }
                                                                                   }
        );

        feedProcessorStatsLoadingCache = CacheBuilder.newBuilder().recordStats().build(new CacheLoader<String, FeedProcessorStats>() {
                                                                                           @Override
                                                                                           public FeedProcessorStats load(String feed) throws Exception {
                                                                                               FeedProcessorStats stats = new FeedProcessorStats(feed);
                                                                                               return stats;
                                                                                           }
                                                                                       }
        );
        this.aggregatingStatsLock = new ReentrantReadWriteLock(true).readLock();
        init();


    }

    /**
     * Check the current event date (incomind dateTime) and see if it falls outside of the batch aggregrationIntervalSeconds.  if so send stats to JMS
     */
    public boolean checkAndSend(DateTime dateTime) {
        //Send in batches
        DateTime startInterval = lastSendTime;
        DateTime endTime = startInterval.plusSeconds(aggregationIntervalSeconds);
        if (dateTime.isAfter(endTime)) {
            //Add ReentrantLock around this code since the timer thread will be hitting this as well
            this.aggregatingStatsLock.lock();
            try {
                //send everything in the cache and clear it
                //3 stats are collected
                //1 grouped by feed and processor
                //2 grouped by feed (high level feed stats)
                //3 grouped by procssors
                String collectionId = UUID.randomUUID().toString();
                List<AggregatedFeedProcessorStatistics>
                    feedProcessorStatistics =
                    feedProcessorStatsLoadingCache.asMap().values().stream().map(feedProcessorStats -> feedProcessorStats.getStats(collectionId, startInterval, endTime)).collect(Collectors.toList());

                List<AggregatedFeedStatistics> feedStatistics = new ArrayList<>();
                for(AggregatedFeedProcessorStatistics processorStatistics: feedProcessorStatistics){
                    String feedName = processorStatistics.getFeedName();
                    List<ProvenanceEventStats> feedEventStats = new ArrayList<>();
                    for(AggregatedProcessorStatistics aggregatedProcessorStatistics : processorStatistics.getProcessorStats().values()){
                        feedEventStats.addAll(aggregatedProcessorStatistics.getStats().getEventStatsList());
                    }
                    feedStatistics.add(new AggregatedFeedStatistics(feedName, new GroupedStats(collectionId, feedEventStats)));
                }


                List<AggregatedProcessorStatistics>
                    processorStatsForTime =
                    processorStatsLoadingCache.asMap().values().stream().map(processorStats -> processorStats.getStats(collectionId,startInterval, endTime)).collect(Collectors.toList());

                //TODO SEND TO JMS HERE!

                AggregatedFeedProcessorStatisticsHolder statisticsHolder = new AggregatedFeedProcessorStatisticsHolder();
                statisticsHolder.setMinTime(startInterval);
                statisticsHolder.setMaxTime(endTime);
                statisticsHolder.setCollectionId(collectionId);
                statisticsHolder.setCollectionInterval(aggregationIntervalSeconds);
                statisticsHolder.setStatistics(feedProcessorStatistics);

                ProvenanceEventActiveMqWriter activeMqWriter = null;
                try {
                    activeMqWriter = (ProvenanceEventActiveMqWriter) SpringApplicationContext.getBean(ProvenanceEventActiveMqWriter.class.getSimpleName());
                } catch (Exception e) {
                    log.error("UNABLE TO GET ACTIVEMQ bean from Spring!! ", e);
                }
                if (activeMqWriter != null) {
                    log.info("WRITING STATS to JMS");
                    activeMqWriter.writeStats(statisticsHolder);
                }
                //invalidate the caches
                processorStatsLoadingCache.invalidateAll();
                feedProcessorStatsLoadingCache.invalidateAll();
                lastSendTime = endTime;
            } finally {
                this.aggregatingStatsLock.unlock();
            }
            return true;
        }
        return false;


    }


    public void calculateStats(ProvenanceEventRecordDTO event) {
        if (firstEventTime == null) {
            firstEventTime = new DateTime(event.getEventTime());
            lastSendTime = firstEventTime;
        }
        checkAndSend(new DateTime(event.getEventTime()));

        //1 get Feed Name for event
        String feedName = event.getFlowFile().getFirstEvent().getComponentId(); //NifiFlowCache.instance().getFlow(event.getFlowFile()).getFeedName();
        ProvenanceEventStats eventStats = StatsModel.toProvenanceEventStats(feedName, event);



        feedProcessorStatsLoadingCache.getUnchecked(feedName).addEventStats(eventStats);
        processorStatsLoadingCache.getUnchecked(event.getComponentId()).addProvenanceEventStats(eventStats);


    }


    /**
     * Start a timer to run and get any leftover events and send to JMS This is where the events are not calculated because there is a long delay in provenacne events and they are still waiting in the
     * caches
     */
    private void init() {
        int interval = 10;
        Timer summaryTimer = new Timer();
        DateTime lastTime = null;
        TimerTask task = new TimerTask() {

            private DateTime startInterval;

            @Override
            public void run() {

                if (firstEventTime != null) {
                    checkAndSend(DateTime.now());
                }


            }
        };
        summaryTimer.schedule(task, 10 * 1000, interval*1000);
    }

}
