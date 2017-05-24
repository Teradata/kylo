package com.thinkbiganalytics.metadata.jpa.jobrepo.nifi;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.metadata.api.common.ItemLastModified;
import com.thinkbiganalytics.metadata.api.common.ItemLastModifiedProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEvent;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Provider creating and accessing the {@link JpaNifiEvent}
 */
@Service
public class NifiEventProvider {

    public static String ITEM_LAST_MODIFIED_KEY = "NIFI_EVENT";

    @Autowired
    private JPAQueryFactory factory;

    private NifiEventRepository repository;

    @Inject
    private ItemLastModifiedProvider itemLastModifiedProvider;

    @Autowired
    public NifiEventProvider(NifiEventRepository repository) {
        this.repository = repository;
    }

    public static NifiEvent toNifiEvent(ProvenanceEventRecordDTO eventRecordDTO) {
        JpaNifiEvent nifiEvent = new JpaNifiEvent(new JpaNifiEvent.NiFiEventPK(eventRecordDTO.getEventId(), eventRecordDTO.getFlowFileUuid()));
        nifiEvent.setFeedName(eventRecordDTO.getFeedName());
        nifiEvent.setEventTime(eventRecordDTO.getEventTime());
        nifiEvent.setEventDetails(eventRecordDTO.getDetails());
        nifiEvent.setEventDuration(eventRecordDTO.getEventDuration());
        nifiEvent.setFeedProcessGroupId(eventRecordDTO.getFeedProcessGroupId());
        nifiEvent.setEventType(eventRecordDTO.getEventType());
        nifiEvent.setProcessorId(eventRecordDTO.getComponentId());
        nifiEvent.setProcessorName(eventRecordDTO.getComponentName());
        nifiEvent.setFileSize(eventRecordDTO.getFileSize());
        nifiEvent.setFileSizeBytes(eventRecordDTO.getFileSizeBytes());
        nifiEvent.setParentFlowFileIds(StringUtils.join(eventRecordDTO.getParentFlowFileIds(), ","));
        nifiEvent.setChildFlowFileIds(StringUtils.join(eventRecordDTO.getChildUuids(), ","));
        nifiEvent.setJobFlowFileId(eventRecordDTO.getJobFlowFileId());
        nifiEvent.setIsStartOfJob(eventRecordDTO.isStartOfJob());
        nifiEvent.setIsEndOfJob(eventRecordDTO.isEndOfJob());
        nifiEvent.setSourceConnectionId(eventRecordDTO.getSourceConnectionIdentifier());
        String attributesJSON = ObjectMapperSerializer.serialize(eventRecordDTO.getAttributeMap());
        nifiEvent.setAttributesJson(attributesJSON);
        nifiEvent.setIsFinalJobEvent(eventRecordDTO.isFinalJobEvent());
        nifiEvent.setIsFailure(eventRecordDTO.isFailure());
        nifiEvent.setIsBatchJob(eventRecordDTO.isBatchJob());
        nifiEvent.setHasFailureEvents(eventRecordDTO.isHasFailedEvents());
        nifiEvent.setClusterNodeAddress(eventRecordDTO.getClusterNodeAddress());
        nifiEvent.setClusterNodeId(eventRecordDTO.getClusterNodeId());
        return nifiEvent;
    }

    public NifiEvent create(NifiEvent t) {
        itemLastModifiedProvider.update(getLastModifiedKey(t.getClusterNodeId()),t.getEventId().toString());
        return repository.save((JpaNifiEvent) t);
    }

    public NifiEvent create(ProvenanceEventRecordDTO t) {
        return this.create(toNifiEvent(t));
    }

    public boolean exists(ProvenanceEventRecordDTO eventRecordDTO) {
        return repository.exists(new JpaNifiEvent.NiFiEventPK(eventRecordDTO.getEventId(), eventRecordDTO.getFlowFileUuid()));
    }



    private String getLastModifiedKey(String clusterId) {
        if(StringUtils.isBlank(clusterId) || "Node".equalsIgnoreCase(clusterId)){
            return ITEM_LAST_MODIFIED_KEY;
        }
        else {
            return ITEM_LAST_MODIFIED_KEY + "-" + clusterId;
        }
    }

    public Long findLastProcessedEventId(String clusterNodeId) {
        ItemLastModified lastModified = itemLastModifiedProvider.findByKey(getLastModifiedKey(clusterNodeId));
        if(lastModified != null){
            return Long.parseLong(lastModified.getValue());
        }
        return null;
    }

    public Long resetLastProcessedEventId(String clusterNodeId) {
        ItemLastModified lastModified =  itemLastModifiedProvider.update(getLastModifiedKey(clusterNodeId),"0");
        return lastModified != null ? Long.parseLong(lastModified.getValue()) : null;
    }

}
