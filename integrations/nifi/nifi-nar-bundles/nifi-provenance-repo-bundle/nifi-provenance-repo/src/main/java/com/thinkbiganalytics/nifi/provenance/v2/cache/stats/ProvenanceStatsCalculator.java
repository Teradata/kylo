package com.thinkbiganalytics.nifi.provenance.v2.cache.stats;

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.StatisticsUtil;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.ProvenanceEventStats;
import com.thinkbiganalytics.nifi.provenance.model.stats.StatsModel;
import com.thinkbiganalytics.nifi.provenance.v2.writer.ProvenanceEventActiveMqWriter;
import com.thinkbiganalytics.util.SpringApplicationContext;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    private DateTime lastSendTime = null;

    private Integer aggregationIntervalSeconds = 10;

    private final List<ProvenanceEventStats> eventStatistics;

    private Lock aggregatingStatsLock = null;

    private ProvenanceEventActiveMqWriter provenanceEventActiveMqWriter;

    private AtomicBoolean autowired = new AtomicBoolean(false);

    private AtomicInteger nextSerialNumber = new AtomicInteger(0);
    private ProvenanceStatsCalculator() {

        eventStatistics = Collections.synchronizedList(new ArrayList());

        this.aggregatingStatsLock = new ReentrantReadWriteLock(true).readLock();
        provenanceEventActiveMqWriter = new ProvenanceEventActiveMqWriter();
        checkAndAutowire();
        log.info("init stats ... current Id {} ", nextSerialNumber.get());
        init();
    }

    private void checkAndAutowire() {
        if (autowired.compareAndSet(false, true)) {
            log.info("CHECK AND attempt to Autowire ActiveMqWriter");
            //   SpringApplicationListener.addObjectToAutowire("provenanceEventActiveMqWriter", provenanceEventActiveMqWriter);
            SpringApplicationContext.getInstance().autowire("provenanceEventActiveMqWriter", provenanceEventActiveMqWriter);
            Object bean = SpringApplicationContext.getInstance().getBean("provenanceEventActiveMqWriter");
            log.info("AutowireResult: " + autowired + " " + bean);
            autowired.set(bean != null);
        }
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
            List<ProvenanceEventStats> statsToSend = null;
            try {
                //send everything in the cache and clear it
                String collectionId = UUID.randomUUID().toString();

                //1 get stats within time period
                List<ProvenanceEventStats> statistics = new ArrayList<>(eventStatistics);
                statsToSend = StatisticsUtil.getEventStatsBeforeOrEqualTo(statistics, endTime);
                  if (statsToSend.size() > 0) {
                    //2 group them by feed and then by processor
                    log.info("About to aggregate and send  {} event statistics out of {} for window: {} - {} ", statsToSend.size(), statistics.size(), startInterval, endTime);
                    List<AggregatedFeedProcessorStatistics>
                        feedProcessorStatistics = StatisticsUtil.aggregateStatsByFeedAndProcessor(statsToSend, collectionId);

                    //TODO SEND TO JMS HERE!

                    AggregatedFeedProcessorStatisticsHolder statisticsHolder = new AggregatedFeedProcessorStatisticsHolder();
                    statisticsHolder.setMinTime(startInterval);
                    statisticsHolder.setMaxTime(endTime);
                    statisticsHolder.setCollectionId(collectionId);
                    statisticsHolder.setCollectionInterval(aggregationIntervalSeconds);
                    statisticsHolder.setStatistics(feedProcessorStatistics);

                    if (provenanceEventActiveMqWriter != null) {
                        // log.info("WRITING STATS to JMS");
                        checkAndAutowire();
                        provenanceEventActiveMqWriter.writeStats(statisticsHolder);

                        //invalidate the ones that were sent
                        eventStatistics.removeAll(statsToSend);
                    }
                }
            } catch (Exception e) {
                log.error("ERROR Aggregating Statistics for window: {} - {}, {} events. Exception: {} ", startInterval, endTime, (statsToSend != null ? statsToSend.size() : "NULL"), e);

            } finally {
                lastSendTime = endTime;
                this.aggregatingStatsLock.unlock();
            }
            return true;
        }
        return false;


    }


    /**
     * Converts the incoming ProvenanceEvent into an object that can be used to gather statistics (ProvenanceEventStats)
     * @param event
     */
    public void calculateStats(ProvenanceEventRecordDTO event) {
        if (firstEventTime == null) {
            firstEventTime = event.getEventTime();
            lastSendTime = firstEventTime;
        }

        checkAndSend(event.getEventTime());


        String feedName = event.getFeedName() == null ? event.getFlowFile().getFeedName() : event.getFeedName();
        if (feedName != null) {
            try {
                ProvenanceEventStats eventStats = StatsModel.toProvenanceEventStats(feedName, event);
                eventStatistics.add(eventStats);

            } catch (Exception e) {
                log.error("Unable to add Statistics for Event {}.  Exception: {} ", event, e.getMessage(), e);
            }
        } else {
            log.error("Unable to add Statistics for Event {}.  Unable to find feed for event ", event);
        }


    }


    private int serialNumber() {
        return nextSerialNumber.getAndIncrement();
    }

    /**
     * Start a timer to run and get any leftover events and send to JMS This is where the events are not calculated because there is a long delay in provenacne events and they are still waiting in the
     * caches
     */
    private void init() {
        int interval = aggregationIntervalSeconds;
        Timer summaryTimer = new Timer("Statistics-Thread-" + serialNumber());
        DateTime lastTime = null;
        TimerTask task = new TimerTask() {

            private DateTime startInterval;

            @Override
            public void run() {

                if (lastSendTime != null) {
                    checkAndSend(DateTime.now());
                }


            }
        };
        summaryTimer.schedule(task, interval * 1000, interval * 1000);
    }

}
