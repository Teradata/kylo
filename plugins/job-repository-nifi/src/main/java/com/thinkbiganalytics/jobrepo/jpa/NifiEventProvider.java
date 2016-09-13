package com.thinkbiganalytics.jobrepo.jpa;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.thinkbiganalytics.jobrepo.jpa.model.NifiEvent;
import com.thinkbiganalytics.json.ObjectMapperSerializer;
import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by sr186054 on 8/31/16.
 */
@Service
public class NifiEventProvider {

    @Autowired
    private JPAQueryFactory factory;

    private NifiEventRepository repository;

    @Autowired
    public NifiEventProvider(NifiEventRepository repository) {
        this.repository = repository;
    }


    public NifiEvent create(NifiEvent t) {
        return repository.save(t);
    }

    public NifiEvent create(ProvenanceEventRecordDTO t) {
        return this.create(toNifiEvent(t));
    }


    public static NifiEvent toNifiEvent(ProvenanceEventRecordDTO eventRecordDTO) {
        NifiEvent nifiEvent = new NifiEvent(new NifiEvent.NiFiEventPK(eventRecordDTO.getEventId(), eventRecordDTO.getFlowFileUuid()));
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
        String attributesJSON = ObjectMapperSerializer.serialize(eventRecordDTO.getAttributes());
        nifiEvent.setAttributesJson(attributesJSON);
        nifiEvent.setIsFinalJobEvent(eventRecordDTO.isFinalJobEvent());
        nifiEvent.setIsFailure(eventRecordDTO.isFailure());
        nifiEvent.setIsBatchJob(eventRecordDTO.isBatchJob());
        nifiEvent.setHasFailureEvents(eventRecordDTO.isHasFailedEvents());
        return nifiEvent;
    }

}
