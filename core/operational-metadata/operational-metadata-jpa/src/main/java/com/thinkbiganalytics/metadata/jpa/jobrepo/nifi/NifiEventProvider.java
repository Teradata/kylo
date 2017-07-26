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
import com.thinkbiganalytics.metadata.api.common.ItemLastModifiedProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEvent;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Provider creating and accessing the {@link JpaNifiEvent}
 */
@Deprecated
@Service
public class NifiEventProvider {


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
        nifiEvent.setEventTime(new DateTime(eventRecordDTO.getEventTime()));
        nifiEvent.setJobFlowFileId(eventRecordDTO.getJobFlowFileId());
        nifiEvent.setFeedProcessGroupId(eventRecordDTO.getFeedProcessGroupId());
        nifiEvent.setEventType(eventRecordDTO.getEventType());
        nifiEvent.setProcessorId(eventRecordDTO.getComponentId());
        nifiEvent.setProcessorName(eventRecordDTO.getComponentName());
        nifiEvent.setIsFailure(eventRecordDTO.isFailure());
        //  nifiEvent.setEventDetails(eventRecordDTO.getDetails());
        //   nifiEvent.setEventDuration(eventRecordDTO.getEventDuration());
        //  nifiEvent.setFileSize(eventRecordDTO.getFileSize());
        // nifiEvent.setFileSizeBytes(eventRecordDTO.getFileSizeBytes());
        // nifiEvent.setParentFlowFileIds(StringUtils.join(eventRecordDTO.getParentFlowFileIds(), ","));
        // nifiEvent.setChildFlowFileIds(StringUtils.join(eventRecordDTO.getChildUuids(), ","));

        // nifiEvent.setIsStartOfJob(eventRecordDTO.isStartOfJob());
        // nifiEvent.setIsEndOfJob(eventRecordDTO.isFinalJobEvent());
        //nifiEvent.setSourceConnectionId(eventRecordDTO.getSourceConnectionIdentifier());
        //   String attributesJSON = ObjectMapperSerializer.serialize(eventRecordDTO.getAttributeMap());
        //   nifiEvent.setAttributesJson(attributesJSON);
        //  nifiEvent.setIsFinalJobEvent(eventRecordDTO.isFinalJobEvent());
        //
        //   nifiEvent.setIsBatchJob(eventRecordDTO.isBatchJob());
        //  nifiEvent.setHasFailureEvents(eventRecordDTO.isHasFailedEvents());
        //    nifiEvent.setClusterNodeAddress(eventRecordDTO.getClusterNodeAddress());
        //    nifiEvent.setClusterNodeId(eventRecordDTO.getClusterNodeId());
        return nifiEvent;
    }

    public NifiEvent create(NifiEvent t) {
        return repository.save((JpaNifiEvent) t);
    }

    public NifiEvent create(ProvenanceEventRecordDTO t) {
        return this.create(toNifiEvent(t));
    }

    public boolean exists(ProvenanceEventRecordDTO eventRecordDTO) {
        return repository.exists(new JpaNifiEvent.NiFiEventPK(eventRecordDTO.getEventId(), eventRecordDTO.getFlowFileUuid()));
    }


}
