package com.thinkbiganalytics.nifi.provenance;

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.util.FormatUtils;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Convert a {@link ProvenanceEventRecord} to a {@link ProvenanceEventRecordDTO} object
 */
public class ProvenanceEventRecordConverter implements Serializable {


    public static ProvenanceEventRecordDTO getPooledObject(ProvenanceEventObjectPool pool, final ProvenanceEventRecord event) throws Exception {
        ProvenanceEventRecordDTO dto = pool.borrowObject();
        populateEvent(dto, event);
        return dto;

    }


    public static void populateEvent(ProvenanceEventRecordDTO dto, ProvenanceEventRecord event) {

        final Map<String, String> updatedAttrs = event.getUpdatedAttributes();
        final Map<String, String> previousAttrs = event.getPreviousAttributes();

        dto.setId(String.valueOf(event.getEventId()));
        dto.setEventId(event.getEventId());
        dto.setEventTime(new DateTime(event.getEventTime()));
        dto.setEventType(event.getEventType().name());
        dto.setFileSize(FormatUtils.formatDataSize(event.getFileSize()));
        dto.setFileSizeBytes(event.getFileSize());
        dto.setComponentId(event.getComponentId());
        dto.setComponentType(event.getComponentType());
        dto.setFlowFileUuid(event.getFlowFileUuid());
        dto.setDetails(event.getDetails());
        dto.setRelationship(event.getRelationship());

        dto.setUpdatedAttributes(updatedAttrs);
        dto.setPreviousAttributes(previousAttrs);
        dto.setAttributeMap(event.getAttributes());

        dto.setInputContentClaimFileSizeBytes(event.getPreviousFileSize());
        dto.setOutputContentClaimFileSize(FormatUtils.formatDataSize(event.getFileSize()));
        dto.setOutputContentClaimFileSizeBytes(event.getFileSize());

        if (event.getPreviousFileSize() != null) {
            dto.setInputContentClaimFileSize(FormatUtils.formatDataSize(event.getPreviousFileSize()));
        }

        dto.setSourceConnectionIdentifier(event.getSourceQueueIdentifier());

        dto.setStartTime(new DateTime(event.getLineageStartDate()));

        final List<String> parentUuids = new ArrayList<>(event.getParentUuids());
        dto.setParentUuids(parentUuids);

        final List<String> childUuids = new ArrayList<>(event.getChildUuids());
        dto.setChildUuids(childUuids);
    }

}
