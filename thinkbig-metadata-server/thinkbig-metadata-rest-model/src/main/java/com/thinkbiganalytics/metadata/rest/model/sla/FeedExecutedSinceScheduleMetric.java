/**
 * 
 */
package com.thinkbiganalytics.metadata.rest.model.sla;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;

/**
 *
 * @author Sean Felten
 */
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeedExecutedSinceScheduleMetric extends DependentFeedMetric {

    private String cronSchedule;
    
    public static FeedExecutedSinceScheduleMetric named(String feedName, String schedule) {
        FeedExecutedSinceScheduleMetric m = new FeedExecutedSinceScheduleMetric();
        m.setDependentFeedName(feedName);
        m.setCronSchedule(schedule);
        return m;
    }
    
    public static FeedExecutedSinceScheduleMetric id(String id, String schedule) {
        FeedExecutedSinceScheduleMetric m = new FeedExecutedSinceScheduleMetric();
        m.setDependentFeedId(id);
        m.setCronSchedule(schedule);
        return m;
    }

    public FeedExecutedSinceScheduleMetric() {
        super();
    }
    
    public String getCronSchedule() {
        return cronSchedule;
    }

    public void setCronSchedule(String cronSchedule) {
        this.cronSchedule = cronSchedule;
    }
}
