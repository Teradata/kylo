package com.thinkbiganalytics.nifi.provenance.model.stats;

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

    public ProvenanceEventStats() {

    }

    public ProvenanceEventStats(String feedName) {
        this.feedName = feedName;
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


    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public void setClusterNodeId(String clusterNodeId) {
        this.clusterNodeId = clusterNodeId;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public void setEventDetails(String eventDetails) {
        this.eventDetails = eventDetails;
    }

    public void setFlowFileId(String flowFileId) {
        this.flowFileId = flowFileId;
    }

    public void setRootFlowFileId(String rootFlowFileId) {
        this.rootFlowFileId = rootFlowFileId;
    }

    public void setRootProcessGroupId(String rootProcessGroupId) {
        this.rootProcessGroupId = rootProcessGroupId;
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
