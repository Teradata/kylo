package com.thinkbiganalytics.nifi.provenance;/*-
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
import com.thinkbiganalytics.nifi.provenance.repo.FeedEventStatistics;
import com.thinkbiganalytics.nifi.provenance.repo.FeedStatisticsManager;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.provenance.ProvenanceEventType;
import org.apache.nifi.provenance.StandardProvenanceEventRecord;
import org.joda.time.DateTime;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sr186054 on 6/8/17.
 */
public class SimulateNiFiFlow {

    AtomicLong eventId = new AtomicLong(0);

    private FeedStatisticsManager feedStatisticsManager;


    public SimulateNiFiFlow(FeedStatisticsManager feedStatisticsManager) {
        this.feedStatisticsManager = feedStatisticsManager;
    }

    private String componentId1 = UUID.randomUUID().toString();
    private String componentId2 = UUID.randomUUID().toString();
    private String componentId3 = UUID.randomUUID().toString();


    private String componentIdSplit1 = UUID.randomUUID().toString();
    private String componentIdSplit2 = UUID.randomUUID().toString();
    private String componentIdSplitParent = UUID.randomUUID().toString();
    private String componentIdSplitChild1 = UUID.randomUUID().toString();
    private String componentIdSplitChild2 = UUID.randomUUID().toString();
    private String componentIdSplitFlowEnd = UUID.randomUUID().toString();

    Map<Long,ProvenanceEventRecord> events = new HashMap<>();
    private List<Long> timeToProcess = new ArrayList<>();

    private String componentType = "com.thinkbiganalytics.ComponentType";
    public void create() {


        String flowFileId = UUID.randomUUID().toString();
        Long entryDate = DateTime.now().getMillis();
        ProvenanceEventRecord start =  buildEvent(componentId1,ProvenanceEventType.CREATE,flowFileId,entryDate);
        ProvenanceEventRecord next =  buildEvent(componentId2,ProvenanceEventType.ATTRIBUTES_MODIFIED,flowFileId,entryDate);
        ProvenanceEventRecord last =  buildEvent(componentId3,ProvenanceEventType.DROP,flowFileId,entryDate);
        addToFlow(start);
        addToFlow(next,1000L);
        addToFlow(last,1000L);

    }

    public void createSplit() {

        String flowFileId = UUID.randomUUID().toString();
        Long entryDate = DateTime.now().getMillis();
        ProvenanceEventRecord start =  buildEvent(componentIdSplit1,ProvenanceEventType.CREATE,flowFileId,entryDate);
        ProvenanceEventRecord next =  buildEvent(componentIdSplit2,ProvenanceEventType.ATTRIBUTES_MODIFIED,flowFileId,entryDate);
        List<ProvenanceEventRecord> split =  buildSplitEvent(componentIdSplitParent,flowFileId,entryDate,3);

        ProvenanceEventRecord last =  buildEvent(componentIdSplitFlowEnd,ProvenanceEventType.DROP,flowFileId,entryDate);
        addToFlow(start);
        addToFlow(next,1000L);
        ProvenanceEventRecord parentSplit = split.stream().filter(e -> e.getEventType().equals(ProvenanceEventType.CLONE)).findFirst().orElse(null);
        split.stream().forEach(e->addToFlow(e));
        addToFlow(last,1000L);
        //drop the children
        for(String childId : parentSplit.getChildUuids()){
            ProvenanceEventRecord childEnd =  buildEvent(componentIdSplitChild2,ProvenanceEventType.DROP,childId,entryDate);
            addToFlow(childEnd,1000L);
        }

    }

private ProvenanceEventRecord applySleepTime(ProvenanceEventRecord eventRecord, Long sleepTime, boolean applyToFlowFileEntryDate){
    if(sleepTime >=0L){
        Long time = eventRecord.getEventTime()+sleepTime;
            StandardProvenanceEventRecord.Builder  builder = new  StandardProvenanceEventRecord.Builder().fromEvent(eventRecord).setEventTime(time);
            if(applyToFlowFileEntryDate){
                builder.setFlowFileEntryDate(time);
            }
            return builder.build();
    }
    else {
        return eventRecord;
    }

}

    private boolean addToFlow(ProvenanceEventRecord event){
            return addToFlow(event,0L);
    }

    private boolean addToFlow(ProvenanceEventRecord event, Long sleepTime){
        return addToFlow(event,sleepTime,false);
    }

    private boolean addToFlow(ProvenanceEventRecord event, Long sleepTime,boolean applyToFlowFileEntryDate){
        boolean process = true;
        Long id = eventId.incrementAndGet();


        event = applySleepTime(event,sleepTime,applyToFlowFileEntryDate);
        events.put(id,event);
        long start = System.currentTimeMillis();
        feedStatisticsManager.addEvent(event,id);
        timeToProcess.add((System.currentTimeMillis() - start));

        return process;
    }

    public Map<Long, ProvenanceEventRecord> getEvents() {
        return events;
    }

    private ProvenanceEventRecord buildEvent(String componentId, ProvenanceEventType type, String flowfileId, Long entryDate){
        StandardProvenanceEventRecord.Builder  builder = new  StandardProvenanceEventRecord.Builder();
        ProvenanceEventRecord eventRecord = builder.setEventTime(System.currentTimeMillis())
            .setFlowFileEntryDate(entryDate)
            .setComponentId(componentId)
            .setComponentType(componentType)
            .setCurrentContentClaim("container", "section", "identifier", 0L, 0L)
            .setFlowFileUUID(flowfileId)
            .setEventType(type)
            .build();

        return eventRecord;
    }

    public Double averageTime(){
        return timeToProcess.stream().mapToLong(t->t).average().getAsDouble();
    }


    private List<ProvenanceEventRecord> buildSplitEvent(String componentId, String flowfileId, Long entryDate, Integer children){

        List<ProvenanceEventRecord> allFlowFiles = new ArrayList<>();

        List<ProvenanceEventRecord> childFlowFiles = new ArrayList<>();

        List<String> parentFlowFileIds = new ArrayList<>();
        parentFlowFileIds.add(flowfileId);

        List<String> childFlowFileIds = new ArrayList<>();
        for(int i =0; i< children; i++){
            String ffId = UUID.randomUUID().toString();
            ProvenanceEventRecord updateEvent =  buildEvent(componentIdSplitChild1,ProvenanceEventType.ATTRIBUTES_MODIFIED,ffId,DateTime.now().getMillis());
            childFlowFileIds.add(ffId);
            childFlowFiles.add(updateEvent);
        }


        StandardProvenanceEventRecord.Builder  builder = new  StandardProvenanceEventRecord.Builder();



        builder.setEventTime(System.currentTimeMillis())
            .setFlowFileEntryDate(entryDate)
            .setEventType(ProvenanceEventType.CLONE)
            .setComponentId(componentId)
            .setComponentType(componentType)
            .setCurrentContentClaim("container", "section", "identifier", 0L, 0L)
            .setFlowFileUUID(flowfileId);

        childFlowFileIds.stream().forEach(id -> builder.addChildUuid(id));
        parentFlowFileIds.stream().forEach(id -> builder.addParentUuid(id));
            //.setChildUuids(childFlowFileIds)
            //.setParentUuids(parentFlowFileIds)
        ProvenanceEventRecord eventRecord = builder.build();


        allFlowFiles.add(eventRecord);
        allFlowFiles.addAll(childFlowFiles);



        return allFlowFiles;
    }



    public Long getSkippedCount() {
        return FeedEventStatistics.getInstance().getSkippedEvents();
    }
}
