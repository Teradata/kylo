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

import com.google.common.base.Stopwatch;
import com.thinkbiganalytics.nifi.provenance.ProvenanceEventRecordConverter;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedFeedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.AggregatedProcessorStatistics;
import com.thinkbiganalytics.nifi.provenance.model.stats.GroupedStats;
import com.thinkbiganalytics.nifi.provenance.util.ProvenanceEventUtil;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Created by sr186054 on 6/12/17.
 */
public class FeedStatistics {
    private static int limit = 3;

    private String feedProcessorId;

    private String processorId;


    private Map<String,ProvenanceEventRecordDTO> lastRecords = new ConcurrentHashMap<>(limit);

    private GroupedStats stats;

    private AggregatedProcessorStatistics feedProcessorStatistics;

    private ProvenanceEventRecordThrottle eventRecordThrottle;

    private String batchKey(ProvenanceEventRecord event, String feedFlowFileId, boolean isStartingFeedFlow){
        String key = event.getComponentId() + "-" + event.getEventType().name();
        if(isStartingFeedFlow){
            key +=eventTimeNearestSecond(event);
        }
        else {
            key +=":"+feedFlowFileId;
        }
        return  key;
    }


    private Long eventTimeNearestSecond(ProvenanceEventRecord event) {
            return new DateTime(event.getEventTime()).withMillis(0).getMillis();
    }


    public FeedStatistics(String feedProcessorId, String processorId) {
        this.feedProcessorId = feedProcessorId;
        this.processorId = processorId;
        stats = new GroupedStats();
        this.feedProcessorStatistics = new AggregatedProcessorStatistics(processorId,null, UUID.randomUUID().toString(),stats);
   //     eventRecordThrottle = new ProvenanceEventRecordThrottle(feedProcessorId+processorId,null,1000L,3);
    }

    public void addEvent(ProvenanceEventRecord event, Long eventId){
        Stopwatch totalTime = Stopwatch.createStarted();
        Stopwatch stopwatch = Stopwatch.createStarted();
        FeedEventStatistics.getInstance().calculateTimes(event,eventId);
        ConsumerStats.getInstance().addProcessTime(stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
        stopwatch.reset();
        ProvenanceEventRecordDTO eventRecordDTO = null;

        String feedFlowFileId = FeedEventStatistics.getInstance().getFeedFlowFileId(event);

        boolean isStartingFeedFlow = ProvenanceEventUtil.isStartingFeedFlow(event);
        String batchKey = batchKey(event,feedFlowFileId, isStartingFeedFlow);

        //always track drop events if its on a tracked feed
        boolean isDropEvent = ProvenanceEventUtil.isEndingFlowFileEvent(event);
        if(isDropEvent && FeedEventStatistics.getInstance().beforeProcessingIsLastEventForTrackedFeed(event,eventId)){
            batchKey += UUID.randomUUID().toString();
        }



       if(((!isStartingFeedFlow && FeedEventStatistics.getInstance().isTrackingDetails(event.getFlowFileUuid())) || (isStartingFeedFlow && lastRecords.size() < limit )) && !lastRecords.containsKey(batchKey)){
           stopwatch.start();
           // if we are tracking details send the event off for jms


           if(isStartingFeedFlow){
               FeedEventStatistics.getInstance().setTrackingDetails(event);
           }


            eventRecordDTO =   ProvenanceEventRecordConverter.convert(event);
          //  eventRecordDTO.setEventTime(event.getEventTime());
           eventRecordDTO.setEventId(eventId);
           eventRecordDTO.setIsStartOfJob(ProvenanceEventUtil.isStartingFeedFlow(event));


           eventRecordDTO.setJobFlowFileId(feedFlowFileId);
           eventRecordDTO.setFirstEventProcessorId(feedProcessorId);
           eventRecordDTO.setEventDuration(FeedEventStatistics.getInstance().getEventDuration(eventId));

           if (ProvenanceEventUtil.isFlowFileQueueEmptied(event)) {
               // a Drop event component id will be the connection, not the processor id. we will set the name of the component
               eventRecordDTO.setComponentName("FlowFile Queue emptied");
               eventRecordDTO.setIsFailure(true);
           }

           if(ProvenanceEventUtil.isTerminatedByFailureRelationship(event)){
               eventRecordDTO.setIsFailure(true);
           }


           lastRecords.put(batchKey,eventRecordDTO);
           ConsumerStats.getInstance().addConversionTime(stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));

       }
       else {
           FeedEventStatistics.getInstance().skip(event,eventId);
       }
       stopwatch.reset().start();

        FeedEventStatistics.getInstance().finishedEvent(event,eventId);

        boolean isEndingEvent = FeedEventStatistics.getInstance().isEndingFeedFlow(eventId);
       if(eventRecordDTO != null && isEndingEvent) {
           eventRecordDTO.setIsFinalJobEvent(isEndingEvent);
       }
        StatisticsConsumer.getInstance().add(stats,event,eventId);
       FeedEventStatistics.getInstance().cleanup(event,eventId);

           FeedEventStatistics.getInstance().cleanup(eventRecordDTO);


        ConsumerStats.getInstance().addEventTime(stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));

        ConsumerStats.getInstance().addTotalTime(totalTime.stop().elapsed(TimeUnit.MILLISECONDS));
        ConsumerStats.getInstance().incrementEventCount();

    }

    public boolean hasStats(){
        return stats.getTotalCount() >0;
    }

    public String getFeedProcessorId() {
        return feedProcessorId;
    }

    public String getProcessorId() {
        return processorId;
    }

    public Collection<ProvenanceEventRecordDTO> getEventsToSend(){
        return lastRecords.values();
    }

    public AggregatedProcessorStatistics getFeedProcessorStatistics(){
        return feedProcessorStatistics;
    }

    public GroupedStats getStats(){
        return stats;
    }

    public void clear(){
        lastRecords.clear();
        stats.clear();
    }

}
