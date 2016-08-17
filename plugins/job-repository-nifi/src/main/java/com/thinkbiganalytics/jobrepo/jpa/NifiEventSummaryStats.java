package com.thinkbiganalytics.jobrepo.jpa;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by sr186054 on 8/17/16.
 */

@Entity
@Table(name = "NIFI_EVENT_SUMMARY_STATS")
public class NifiEventSummaryStats {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    @Column(name = "id", unique = true)
    private String id;

    @Column(name = "FM_FEED_NAME")
    private String feedName;

    @Column(name = "NIFI_PROCESSOR_ID")
    private String processorId;

    public NifiEventSummaryStats(String feedName, String processorId) {
        this.feedName = feedName;
        this.processorId = processorId;
    }

    public NifiEventSummaryStats() {
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getProcessorId() {
        return processorId;
    }

    public void setProcessorId(String processorId) {
        this.processorId = processorId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
