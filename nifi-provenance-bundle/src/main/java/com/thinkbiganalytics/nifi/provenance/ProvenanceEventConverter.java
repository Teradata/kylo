package com.thinkbiganalytics.nifi.provenance;

import org.apache.nifi.provenance.ProvenanceEventRecord;
import org.apache.nifi.util.FormatUtils;
import org.apache.nifi.web.api.dto.provenance.AttributeDTO;
import org.apache.nifi.web.api.dto.provenance.ProvenanceEventDTO;

import java.text.Collator;
import java.util.*;

/**
 * Created by sr186054 on 3/3/16.
 */
public class ProvenanceEventConverter {

    /**
     * Creates a ProvenanceEventDTO for the specified ProvenanceEventRecord.
     *
     * @param event event
     * @return event
     */
    public static ProvenanceEventDTO convert(final ProvenanceEventRecord event) {

            // convert the attributes
            final Comparator<AttributeDTO> attributeComparator = new Comparator<AttributeDTO>() {
                @Override
                public int compare(AttributeDTO a1, AttributeDTO a2) {
                    return Collator.getInstance(Locale.US).compare(a1.getName(), a2.getName());
                }
            };

            final SortedSet<AttributeDTO> attributes = new TreeSet<>(attributeComparator);

            final Map<String, String> updatedAttrs = event.getUpdatedAttributes();
            final Map<String, String> previousAttrs = event.getPreviousAttributes();

            // add previous attributes that haven't been modified.
            for (final Map.Entry<String, String> entry : previousAttrs.entrySet()) {
                // don't add any attributes that have been updated; we will do that next
                if (updatedAttrs.containsKey(entry.getKey())) {
                    continue;
                }

                final AttributeDTO attribute = new AttributeDTO();
                attribute.setName(entry.getKey());
                attribute.setValue(entry.getValue());
                attribute.setPreviousValue(entry.getValue());
                attributes.add(attribute);
            }

            // Add all of the update attributes
            for (final Map.Entry<String, String> entry : updatedAttrs.entrySet()) {
                final AttributeDTO attribute = new AttributeDTO();
                attribute.setName(entry.getKey());
                attribute.setValue(entry.getValue());
                attribute.setPreviousValue(previousAttrs.get(entry.getKey()));
                attributes.add(attribute);
            }

            // build the event dto
            final ProvenanceEventDTO dto = new ProvenanceEventDTO();
            dto.setId(String.valueOf(event.getEventId()));
            dto.setAlternateIdentifierUri(event.getAlternateIdentifierUri());
            dto.setAttributes(attributes);
            dto.setTransitUri(event.getTransitUri());
            dto.setEventId(event.getEventId());
            dto.setEventTime(new Date(event.getEventTime()));
            dto.setEventType(event.getEventType().name());
            dto.setFileSize(FormatUtils.formatDataSize(event.getFileSize()));
            dto.setFileSizeBytes(event.getFileSize());
            dto.setComponentId(event.getComponentId());
            dto.setComponentType(event.getComponentType());
            dto.setSourceSystemFlowFileId(event.getSourceSystemFlowFileIdentifier());
            dto.setFlowFileUuid(event.getFlowFileUuid());
            dto.setRelationship(event.getRelationship());
            dto.setDetails(event.getDetails());

             // content
          //  dto.setContentEqual(contentAvailability.isContentSame());
           // dto.setInputContentAvailable(contentAvailability.isInputAvailable());
            dto.setInputContentClaimSection(event.getPreviousContentClaimSection());
            dto.setInputContentClaimContainer(event.getPreviousContentClaimContainer());
            dto.setInputContentClaimIdentifier(event.getPreviousContentClaimIdentifier());
            dto.setInputContentClaimOffset(event.getPreviousContentClaimOffset());
            dto.setInputContentClaimFileSizeBytes(event.getPreviousFileSize());
          //  dto.setOutputContentAvailable(contentAvailability.isOutputAvailable());
            dto.setOutputContentClaimSection(event.getContentClaimSection());
            dto.setOutputContentClaimContainer(event.getContentClaimContainer());
            dto.setOutputContentClaimIdentifier(event.getContentClaimIdentifier());
            dto.setOutputContentClaimOffset(event.getContentClaimOffset());
            dto.setOutputContentClaimFileSize(FormatUtils.formatDataSize(event.getFileSize()));
            dto.setOutputContentClaimFileSizeBytes(event.getFileSize());

            // format the previous file sizes if possible
            if (event.getPreviousFileSize() != null) {
                dto.setInputContentClaimFileSize(FormatUtils.formatDataSize(event.getPreviousFileSize()));
            }

            // replay
        //    dto.setReplayAvailable(contentAvailability.isReplayable());
         //   dto.setReplayExplanation(contentAvailability.getReasonNotReplayable());
            dto.setSourceConnectionIdentifier(event.getSourceQueueIdentifier());


            // event duration
            if (event.getEventDuration() >= 0) {
                dto.setEventDuration(event.getEventDuration());
            }

            // lineage duration
            if (event.getLineageStartDate() > 0) {
                final long lineageDuration = event.getEventTime() - event.getLineageStartDate();
                dto.setLineageDuration(lineageDuration);
            }

            // parent uuids
            final List<String> parentUuids = new ArrayList<>(event.getParentUuids());
            Collections.sort(parentUuids, Collator.getInstance(Locale.US));
            dto.setParentUuids(parentUuids);

            // child uuids
            final List<String> childUuids = new ArrayList<>(event.getChildUuids());
            Collections.sort(childUuids, Collator.getInstance(Locale.US));
            dto.setChildUuids(childUuids);

            return dto;



    }
}
