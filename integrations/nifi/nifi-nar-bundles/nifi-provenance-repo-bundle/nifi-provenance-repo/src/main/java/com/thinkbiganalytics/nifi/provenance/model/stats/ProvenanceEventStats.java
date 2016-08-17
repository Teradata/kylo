package com.thinkbiganalytics.nifi.provenance.model.stats;

import com.thinkbiganalytics.nifi.provenance.model.ProvenanceEventRecordDTO;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 8/16/16.
 */
public class ProvenanceEventStats extends BaseStatistics {

    private Long eventId;
    private String processorId;
    private String clusterNodeId;
    private String feedName;


    private String eventDetails;

    private String flowFileId;
    private String rootFlowFileId;
    private String rootProcessGroupId;


    public ProvenanceEventStats(String feedName, ProvenanceEventRecordDTO event) {
        this.feedName = feedName;
        this.eventId = event.getEventId();
        this.processorId = event.getComponentId();
        this.clusterNodeId = event.getClusterNodeId();
        this.time = new DateTime(event.getEventTime());
        this.duration = event.getEventDuration() != null ? event.getEventDuration() : 0L;
        this.bytesIn = event.getInputContentClaimFileSizeBytes() != null ? event.getInputContentClaimFileSizeBytes() : 0L;
        this.bytesOut = event.getOutputContentClaimFileSizeBytes() != null ? event.getOutputContentClaimFileSizeBytes() : 0L;
        this.flowFileId = event.getFlowFileUuid();
        this.rootFlowFileId = event.getFlowFile().getRootFlowFile().getId();
        this.eventDetails = event.getDetails();
        this.rootProcessGroupId = event.getFlowFile().getRootFlowFile().getFirstEvent().getGroupId();

    }

    public String getFeedName() {
        return feedName;
    }

    public String getFlowFileId() {
        return flowFileId;
    }

    public String getRootFlowFileId() {
        return rootFlowFileId;
    }

    public String getRootProcessGroupId() {
        return rootProcessGroupId;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getProcessorId() {
        return processorId;
    }

    public String getClusterNodeId() {
        return clusterNodeId;
    }

    public String getEventDetails() {
        return eventDetails;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProvenanceEventStats{");
        sb.append("eventId=").append(eventId);
        sb.append(", feedName='").append(feedName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
