package com.thinkbiganalytics.nifi.provenance.model;

/*-
 * #%L
 * thinkbig-nifi-provenance-model
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thinkbiganalytics.nifi.provenance.model.util.LongIdGenerator;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by sr186054 on 6/1/17.
 */
public class FeedFlowFileJobTrackingStats implements Serializable{

    //track flows sent vs actual

    private Map<String,Integer> actualProcessorIdParentFlowFileCount = null;

    private Map<String,Integer> actualProcessorIdChildFlowFileCount = null;

    private Map<String,Integer> sentProcessorIdParentFlowFileCount = null;;

    private Map<String,Integer> sentProcessorIdChildFlowFileCount = null;

    private Map<String,Long> actualProcessorIdEventTime = null;

    private Map<String,Set<String>> actualFlowFilesProcessed = null;

    private Map<String,Integer> sentFlowFilesProcessedCount = null;

    private Set<String> updatedProcessors;

    private String feedFlowFileId;

    private String firstEventProcessorId;

    private String primaryRelatedBatchFeedFlow;

    public FeedFlowFileJobTrackingStats(){

    }

    public FeedFlowFileJobTrackingStats(String firstEventProcessorId,String feedFlowFileId){
        this.feedFlowFileId = feedFlowFileId;
        this.firstEventProcessorId =firstEventProcessorId;
    }

    public String getFirstEventProcessorId() {
        return firstEventProcessorId;
    }

    public void setFirstEventProcessorId(String firstEventProcessorId) {
        this.firstEventProcessorId = firstEventProcessorId;
    }

    public String getPrimaryRelatedBatchFeedFlow() {
        return primaryRelatedBatchFeedFlow;
    }

    public void setPrimaryRelatedBatchFeedFlow(String primaryRelatedBatchFeedFlow) {
        this.primaryRelatedBatchFeedFlow = primaryRelatedBatchFeedFlow;
    }

    private void addFlowFile(String processorId, String flowFileId){
        if(actualFlowFilesProcessed == null) {
            actualFlowFilesProcessed = new ConcurrentHashMap<>();
        }
        actualFlowFilesProcessed.computeIfAbsent(processorId, id -> new HashSet<String>()).add(flowFileId);
    }

    @JsonIgnore
    private void addParentFlowFiles(String processorId, Integer count){
        if(actualProcessorIdParentFlowFileCount == null){
            actualProcessorIdParentFlowFileCount = new HashMap<>();
        }
        add(actualProcessorIdParentFlowFileCount,processorId,count);
    }

    @JsonIgnore
    private void addChildFlowFiles(String processorId, Integer count){
        if(actualProcessorIdChildFlowFileCount == null){
            actualProcessorIdChildFlowFileCount = new HashMap<>();
        }
        add(actualProcessorIdChildFlowFileCount,processorId,count);
    }

    @JsonIgnore
    private void dirty(String processorId){
        (updatedProcessors == null ? updatedProcessors = new HashSet<String>() : updatedProcessors).add(processorId);
    }
    @JsonIgnore
    private void clean(String processorId){
        if(updatedProcessors != null) {
            updatedProcessors.remove(processorId);
        }
    }

    @JsonIgnore
    private Map<String,Integer> add(Map<String,Integer> map, String processorId, Integer count) {

        if(!map.containsKey(processorId)){
            map.put(processorId,count);
        }
        else {
            Integer newCount = map.get(processorId) + count;
            map.put(processorId,newCount);
        }
        return map;
    }

    public Integer getActualFlowFilesProcessed(String processorId) {
        Integer count = 0;
        if(actualFlowFilesProcessed != null){
            if(actualFlowFilesProcessed.containsKey(processorId)){
                return actualFlowFilesProcessed.get(processorId).size();
            }
        }
        return count;
    }

    public Integer getSentFlowFilesProcessed(String processorId) {
        Integer count = 0;
        if(sentProcessorIdChildFlowFileCount != null){
            if(sentProcessorIdChildFlowFileCount.containsKey(processorId)){
                return sentProcessorIdChildFlowFileCount.get(processorId);
            }
        }
        return count;
    }

    public Integer getActualParentFlowFileCount(String processorId){
         return actualProcessorIdParentFlowFileCount == null ? 0: actualProcessorIdParentFlowFileCount.getOrDefault(processorId, 0);
    }

    public Integer getActualChildFlowFileCount(String processorId){
        return actualProcessorIdChildFlowFileCount == null ? 0: actualProcessorIdChildFlowFileCount.getOrDefault(processorId, 0);
    }

    public Integer getSentParentFlowFileCount(String processorId){
        return sentProcessorIdParentFlowFileCount == null ? 0: sentProcessorIdParentFlowFileCount.getOrDefault(processorId, 0);
    }

    public Integer getSentChildFlowFileCount(String processorId){
        return sentProcessorIdChildFlowFileCount == null ? 0: sentProcessorIdChildFlowFileCount.getOrDefault(processorId, 0);
    }
    @JsonIgnore
    private void setSentParentFlowFileCount(String processorId, Integer count) {
        if(sentProcessorIdParentFlowFileCount == null){
            sentProcessorIdParentFlowFileCount = new HashMap<>();
        }
        sentProcessorIdParentFlowFileCount.put(processorId,count);

    }
    @JsonIgnore
    private void setSentChildFlowFileCount(String processorId, Integer count) {
        if(sentProcessorIdChildFlowFileCount == null){
            sentProcessorIdChildFlowFileCount = new HashMap<>();
        }
        sentProcessorIdChildFlowFileCount.put(processorId,count);

    }

    private void setSentFlowFilesProcessedCount(String processorId, Integer count){
        if(sentFlowFilesProcessedCount == null){
            sentFlowFilesProcessedCount = new HashMap<>();
        }
        sentFlowFilesProcessedCount.put(processorId,count);
    }

    @JsonIgnore
    public void trackExtendedAttributes(ProvenanceEventRecordDTO event){
        String processorId = event.getComponentId();
        if(event.getParentUuids() != null && !event.getParentUuids().isEmpty()){
           addParentFlowFiles(processorId,event.getParentUuids().size());
        }

        if(event.getChildUuids() != null && !event.getChildUuids().isEmpty()){
           addChildFlowFiles(processorId,event.getChildUuids().size());
        }
        addFlowFile(processorId,event.getFlowFileUuid());

        if(actualProcessorIdEventTime == null){
            actualProcessorIdEventTime = new HashMap<>();
        }
        actualProcessorIdEventTime.put(processorId,event.getEventTime());
        dirtyCheck(event);
    }

    @JsonIgnore
    public void markExtendedAttributesAsSent(ProvenanceEventRecordDTO event) {
      markExtendedAttributesAsSent(event,true);
    }

    private void markExtendedAttributesAsSent(ProvenanceEventRecordDTO event, boolean check) {
        String processorId = event.getComponentId();
        Integer actualParents = getActualParentFlowFileCount(processorId);
        Integer actualChildren = getActualChildFlowFileCount(processorId);
        Integer flowFilesProcessed = getActualFlowFilesProcessed(processorId);
        if(actualParents != 0 || actualChildren != 0 || flowFilesProcessed != 0) {
            if(event.getUpdatedAttributes() == null){
                event.setUpdatedAttributes(new HashMap<>());
            }
            if(actualParents  != 0) {
                event.setUpdatedAttribute(ProvenanceEventExtendedAttributes.PARENT_FLOW_FILES_COUNT.getDisplayName(), actualParents + "");
                setSentParentFlowFileCount(processorId, actualParents);
            }
            if(actualChildren != 0) {
                event.setUpdatedAttribute(ProvenanceEventExtendedAttributes.CHILD_FLOW_FILES_COUNT.getDisplayName(), actualChildren + "");
                setSentChildFlowFileCount(processorId, actualChildren);
            }
            if(flowFilesProcessed != 0) {
                event.setUpdatedAttribute(ProvenanceEventExtendedAttributes.FLOW_FILES_PROCESSED_COUNT.getDisplayName(), flowFilesProcessed+"");
                setSentFlowFilesProcessedCount(processorId,flowFilesProcessed);
            }



            if(check) {
                dirtyCheck(event);
            }
        }
    }


    @JsonIgnore
    private void dirtyCheck(ProvenanceEventRecordDTO event){
        String processorId = event.getComponentId();
        if(getActualChildFlowFileCount(processorId) != getSentChildFlowFileCount(processorId) || getActualParentFlowFileCount(processorId) != getSentParentFlowFileCount(processorId) || getActualFlowFilesProcessed(processorId) != getSentFlowFilesProcessed(processorId)) {
            dirty(processorId);
        }
        else {
            clean(processorId);
        }
    }

    @JsonIgnore
    public List<ProvenanceEventRecordDTO> getUpdatedProvenanceEvents(){
        List<ProvenanceEventRecordDTO>  list = null;
        if(updatedProcessors != null){
            list = new ArrayList<>();
            for(String processorId: updatedProcessors) {
                ProvenanceEventRecordDTO event = new ProvenanceEventRecordDTO();
                event.setComponentId(processorId);
                event.setFirstEventProcessorId(firstEventProcessorId);
                markExtendedAttributesAsSent(event,false);
                event.setEventTime(actualProcessorIdEventTime.get(processorId));
                event.setJobFlowFileId(feedFlowFileId);
                String ffId = feedFlowFileId;
               // event.setStreamingBatchFeedFlowFileId(ffId);
                event.setFlowFileUuid(ffId);
                event.setJobFlowFileId(ffId);

                event.setEventId(LongIdGenerator.nextId());
                event.setEventType("KYLO");
                event.setDetails("Job Tracking Stats Event");
                list.add(event);
            }
            //check
            list.stream().forEach(e -> dirtyCheck(e));
        }
        else {
            list = Collections.emptyList();
        }
        return list;
    }

}
