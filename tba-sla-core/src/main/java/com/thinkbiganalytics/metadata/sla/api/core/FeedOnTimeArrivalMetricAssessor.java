/**
 * 
 */
package com.thinkbiganalytics.metadata.sla.api.core;

import java.text.ParseException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.quartz.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.ServiceLevelAssessmentException;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;
import com.thinkbiganalytics.pipelinecontroller.repositories.FeedRepository;
import com.thinkbiganalytics.pipelinecontroller.rest.dataobjects.ExecutedFeed;
import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;

/**
 *
 * @author Sean Felten
 */
public class FeedOnTimeArrivalMetricAssessor implements MetricAssessor<FeedOnTimeArrivalMetric> {
    private static final Logger LOG = LoggerFactory.getLogger(FeedOnTimeArrivalMetricAssessor.class);
    
    @Inject
    private FeedRepository feedRepository;
    
    @Inject
    @Named("holidayCalanders")
    private Map<String, Calendar> holidayCalendars;
    

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
        LOG.debug("Assessing metric: " + metric);
        
        Calendar calendar = this.holidayCalendars.get(metric.getCalendarName());
        
        String feedName = metric.getFeedName();
        ExecutedFeed feed = this.feedRepository.findLastCompletedFeed(feedName);
        DateTime lastFeedTime = feed.getEndTime();
        
        try {
            DateTime expectedTime = new DateTime(CronExpressionUtil.getPreviousFireTime(metric.getExpectedExpression()));
            DateTime lateTime = expectedTime.plus(metric.getLatePeriod());
            DateTime asOfTime = expectedTime.minus(metric.getAsOfPeriod());
            boolean isHodiday = calendar.isTimeIncluded(asOfTime.getMillis());
            
            builder.metric(metric);
            
            if (isHodiday) {
                builder.message("No data expected for feed " + feedName + " due to a holiday");
                builder.result(AssessmentResult.SUCCESS);
            } else if (lastFeedTime.isBefore(lateTime)) {
                builder.message("Data for feed " + feedName + " arrived on " + lastFeedTime + ", which was before late time: " + lateTime);
                builder.result(AssessmentResult.SUCCESS);
            } else {
                builder.message("Data for feed " + feedName + " has not arrived before the late time: " + lateTime);
                builder.result(AssessmentResult.FAILURE);
            }
        } catch (ParseException e) {
            LOG.error("The cron expression configured for the feed " + metric.getFeedName() 
                + " is invalid: " + metric.getExpectedExpression());
            throw new ServiceLevelAssessmentException("Unavble to assess metric: " + metric, e);
        }
    }
    
    public void setHolidayCalendars(Map<String, Calendar> holidayCalendars) {
        this.holidayCalendars = holidayCalendars;
    }

}
