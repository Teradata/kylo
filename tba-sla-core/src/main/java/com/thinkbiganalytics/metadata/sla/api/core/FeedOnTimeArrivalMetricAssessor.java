/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api.core;

import java.util.Date;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.quartz.Calendar;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;
import com.thinkbiganalytics.pipelinecontroller.repositories.FeedRepository;
import com.thinkbiganalytics.pipelinecontroller.rest.dataobjects.ExecutedFeed;

/**
 *
 * @author Sean Felten
 */
public class FeedOnTimeArrivalMetricAssessor implements MetricAssessor<FeedOnTimeArrivalMetric> {
    
    @Inject
    private FeedRepository reedRepository;
    
    @Inject
    @Named("holidayCalanders")
    private Map<String, Calendar> holidayCalendars;
    

    public FeedOnTimeArrivalMetricAssessor() {
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.spi.MetricAssessor#accepts(com.thinkbiganalytics.metadata.sla.api.Metric)
     */
    @Override
    public boolean accepts(Metric metric) {
        return metric instanceof FeedOnTimeArrivalMetric;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.sla.spi.MetricAssessor#assess(com.thinkbiganalytics.metadata.sla.api.Metric, com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder)
     */
    @Override
    public void assess(FeedOnTimeArrivalMetric metric, MetricAssessmentBuilder builder) {
        String feedName = metric.getFeedName();
        ExecutedFeed feed = this.reedRepository.findLastCompletedFeed(feedName);
        DateTime lastFeedTime = feed.getEndTime();
        
        Calendar calendar = this.holidayCalendars.get(metric.getClaendarName());
        DateTime midnight = DateTime.now().withTimeAtStartOfDay();
        Date expectedDate = metric.getExpectedExpression().getNextValidTimeAfter(midnight.toDate());
        DateTime lateTime = new DateTime(expectedDate).plus(metric.getLatePeriod());
        DateTime asOfTime = new DateTime(expectedDate).minus(metric.getAsOfPeriod());
        boolean isHodiday = calendar.isTimeIncluded(asOfTime.getMillis());
        
        builder.metric(metric);
        
        if (isHodiday) {
            builder.message("No data expected for feed " + feedName + " due to a holiday");
            builder.result(AssessmentResult.SUCCESS);
        } else if (lastFeedTime.isAfter(midnight) && lastFeedTime.isBefore(lateTime)) {
            builder.message("Data for feed " + feedName + " arrived on " + lastFeedTime + ", which was before late time: " + lateTime);
            builder.result(AssessmentResult.SUCCESS);
        } else {
            builder.message("Data for feed " + feedName + " has not arrived before the late time: " + lateTime);
            builder.result(AssessmentResult.FAILURE);
        }
    }

}
