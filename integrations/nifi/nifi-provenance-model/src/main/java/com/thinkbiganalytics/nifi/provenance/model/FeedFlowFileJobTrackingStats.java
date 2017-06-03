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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private Set<String> dirtyProcessors;

    @JsonIgnore
    private FeedFlowFile feedFlowFile;

public FeedFlowFileJobTrackingStats(){

}
public FeedFlowFileJobTrackingStats(FeedFlowFile feedFlowFile){
    this.feedFlowFile = feedFlowFile;
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
        (dirtyProcessors == null ? dirtyProcessors = new HashSet<String>() : dirtyProcessors).add(processorId);
    }
    @JsonIgnore
    private void clean(String processorId){
        if(dirtyProcessors != null) {
            dirtyProcessors.remove(processorId);
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

    @JsonIgnore
    public void trackExtendedAttributes(ProvenanceEventRecordDTO event){
        String processorId = event.getComponentId();
        if(event.getParentUuids() != null && !event.getParentUuids().isEmpty()){
           addParentFlowFiles(processorId,event.getParentUuids().size());
        }

        if(event.getChildUuids() != null && !event.getChildUuids().isEmpty()){
           addChildFlowFiles(processorId,event.getChildUuids().size());
        }

        dirtyCheck(processorId);
    }

    @JsonIgnore
    public void markExtendedAttributesAsSent(ProvenanceEventRecordDTO event) {
        String processorId = event.getComponentId();
        Integer actualParents = getActualParentFlowFileCount(processorId);
        Integer actualChildren = getActualChildFlowFileCount(processorId);
        if(actualParents != 0 || actualChildren != 0) {
            if(event.getUpdatedAttributes() == null){
                event.setUpdatedAttributes(new HashMap<>());
            }
            event.setUpdatedAttribute(ProvenanceEventExtendedAttributes.PARENT_FLOW_FILES_COUNT.getDisplayName(), actualParents + "");
            event.setUpdatedAttribute(ProvenanceEventExtendedAttributes.CHILD_FLOW_FILES_COUNT.getDisplayName(), actualChildren + "");
            setSentParentFlowFileCount(processorId, actualParents);
            setSentChildFlowFileCount(processorId, actualChildren);
            dirtyCheck(processorId);
        }
    }


    @JsonIgnore
    private void dirtyCheck(String processorId){
        if(getActualChildFlowFileCount(processorId) != getSentChildFlowFileCount(processorId) || getActualParentFlowFileCount(processorId) != getSentParentFlowFileCount(processorId)){
            dirty(processorId);
        }
        else {
            clean(processorId);
        }
    }

    @JsonIgnore
    public List<ProvenanceEventRecordDTO> getDirtyProcessorProvenanceEvents(){
        List<ProvenanceEventRecordDTO>  list = null;
        if(dirtyProcessors != null){
            list = new ArrayList<>();
            for(String processorId: dirtyProcessors) {
                ProvenanceEventRecordDTO event = new ProvenanceEventRecordDTO();
                event.setFeedFlowFile(feedFlowFile);
                event.setComponentId(processorId);
                markExtendedAttributesAsSent(event);
                event.setJobFlowFileId(feedFlowFile.getId());
                String ffId = event.getFeedFlowFile().getId();
                if (event.getFeedFlowFile().hasRelatedBatchFlows()) {
                     ffId = event.getFeedFlowFile().getPrimaryRelatedBatchFeedFlow();
                    if (ffId != null) {
                        event.setStreamingBatchFeedFlowFileId(ffId);
                    }
                }
                event.setFlowFileUuid(ffId);
                event.setEventId(-1L);
                event.setEventType("KYLO");
                event.setDetails("Job Tracking Stats Event");
                list.add(event);
            }
        }
        else {
            list = Collections.emptyList();
        }
        return list;
    }

}
