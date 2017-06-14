package com.thinkbiganalytics.nifi.provenance.repo;

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

import com.thinkbiganalytics.nifi.provenance.jms.ProvenanceEventActiveMqWriter;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTOHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatisticsHolder;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStats;
import com.thinkbiganalytics.nifi.provenance.util.ProvenanceEventUtil;
import com.thinkbiganalytics.nifi.provenance.util.SpringApplicationContext;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

/**
 * Created by sr186054 on 6/12/17.
 */
public class FeedStatisticsManager {

    private static final Logger log = LoggerFactory.getLogger(FeedStatisticsManager.class);

    private Long sendJmsTimeMillis = 3000L; //every 3 seconds

    private Lock lock = new ReentrantLock();

    private Map<String, FeedStatistics> feedStatisticsMap = new ConcurrentHashMap<>();

    private BlockingQueue<JmsSender> jmsSenderBlockingQueue = new LinkedBlockingQueue<>();


    private boolean isTest = false;

    public void setTest(boolean isTest){
        this.isTest = isTest;
    }


    private void checkout(FeedEventStatistics feedEventStatistics){

    }

    List<ProvenanceEventRecordDTO> sentEvents = new ArrayList<>();

    List<GroupedStats> sentStats = new ArrayList<>();

    public FeedStatisticsManager(){
        init();
    }


    private void init(){
        initTimerThread();
    }


    public void addEvent(ProvenanceEventRecord event, Long eventId){
        lock.lock();
        try {
            //build up feed flow file map relationships
            boolean isStartingFeedFlow = ProvenanceEventUtil.isStartingFeedFlow(event);
            if (isStartingFeedFlow) {
                FeedEventStatistics.getInstance().checkAndAssignStartingFlowFile(event);
            }
            FeedEventStatistics.getInstance().assignParentsAndChildren(event);

            //generate statistics and process the event
            String feedProcessorId = FeedEventStatistics.getInstance().getFeedProcessorId(event);
            if(feedProcessorId != null) {
                String key = feedProcessorId + event.getComponentId();
                feedStatisticsMap.computeIfAbsent(key, feedStatisticsKey -> new FeedStatistics(feedProcessorId, event.getComponentId())).addEvent(event, eventId);
            }
            else {
                //UNABLE TO FIND data in maps
            }
        }finally {
            lock.unlock();
        }
    }

    public void send(){
        lock.lock();
        List<ProvenanceEventRecordDTO> eventsToSend = null;
        Map<String,AggregatedFeedProcessorStatistics> statsToSend = null;
        JmsSender jmsSender = null;
        try {
            //Gather Events and Stats to send Ops Manager
            eventsToSend = feedStatisticsMap.values().stream().flatMap(stats -> stats.getEventsToSend().stream()).collect(Collectors.toList());

            final String collectionId = UUID.randomUUID().toString();

            for(FeedStatistics feedStatistics : feedStatisticsMap.values()){
                if(feedStatistics.hasStats()){
                    if(statsToSend == null){
                        statsToSend = new ConcurrentHashMap<>();
                    }
                    AggregatedFeedProcessorStatistics feedProcessorStatistics =statsToSend.computeIfAbsent(feedStatistics.getFeedProcessorId(), feedProcessorId -> new AggregatedFeedProcessorStatistics(feedStatistics.getFeedProcessorId(), collectionId) );
                    AggregatedProcessorStatistics processorStatistics = feedProcessorStatistics.getProcessorStats().computeIfAbsent(feedStatistics.getProcessorId(),processorId ->new AggregatedProcessorStatistics(feedStatistics.getProcessorId(),null,collectionId));
                    StatisticsConsumer.getInstance().addStats1(processorStatistics.getStats(),feedStatistics.getStats());
                }
            }

            if ((eventsToSend != null && !eventsToSend.isEmpty()) || (statsToSend != null && !statsToSend.isEmpty()) ) {
                jmsSenderBlockingQueue.add(new JmsSender(eventsToSend,statsToSend.values()));
            }

        }finally{
            feedStatisticsMap.values().stream().forEach(stats -> stats.clear());
            lock.unlock();
        }

    }







    //jms thread

    /**
     * Start the timer thread
     */
    private void initTimerThread() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            send();
        }, sendJmsTimeMillis, sendJmsTimeMillis, TimeUnit.MILLISECONDS);


        ScheduledExecutorService jmsService = Executors.newSingleThreadScheduledExecutor();
        jmsService.submit(new JmsSenderConsumer(jmsSenderBlockingQueue));
    }

    public List<ProvenanceEventRecordDTO> getSentEvents() {
        return sentEvents;
    }

    public List<GroupedStats> getSentStats() {
        return sentStats;
    }
}
