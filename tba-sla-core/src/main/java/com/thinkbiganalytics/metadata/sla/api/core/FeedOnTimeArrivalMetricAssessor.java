/**
 *
 */
package com.thinkbiganalytics.metadata.sla.api.core;

import com.thinkbiganalytics.calendar.HolidayCalendarService;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessor;
import com.thinkbiganalytics.pipelinecontroller.repositories.FeedRepository;
import com.thinkbiganalytics.pipelinecontroller.rest.dataobjects.ExecutedFeed;
import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;
import org.joda.time.DateTime;
import org.quartz.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author Sean Felten
 */
public class FeedOnTimeArrivalMetricAssessor implements MetricAssessor<FeedOnTimeArrivalMetric> {
    private static final Logger LOG = LoggerFactory.getLogger(FeedOnTimeArrivalMetricAssessor.class);

    @Inject
    private FeedRepository feedRepository;

    @Inject
    private HolidayCalendarService calendarService;


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
    @SuppressWarnings("unchecked")
    public void assess(FeedOnTimeArrivalMetric metric, MetricAssessmentBuilder builder) {
        LOG.debug("Assessing metric: ", metric);

        builder.metric(metric);

        Calendar calendar = getCalendar(metric);
        String feedName = metric.getFeedName();
        ExecutedFeed feed = this.feedRepository.findLastCompletedFeed(feedName);


        DateTime lastFeedTime = null;
        if (feed != null) {
            lastFeedTime = feed.getEndTime();
        }
        Date expectedDate = CronExpressionUtil.getPreviousFireTime(metric.getExpectedExpression());
        DateTime expectedTime = new DateTime(expectedDate);
        DateTime lateTime = expectedTime.plus(metric.getLatePeriod());
        DateTime asOfTime = expectedTime.minus(metric.getAsOfPeriod());
        boolean isHoliday = !calendar.isTimeIncluded(asOfTime.getMillis());

        builder.compareWith(expectedDate, feedName);

        if (isHoliday) {
            LOG.debug("No data expected for feed {} due to a holiday", feedName);
            builder.message("No data expected for feed " + feedName + " due to a holiday")
                    .result(AssessmentResult.SUCCESS);
        } else if (lastFeedTime == null && (DateTime.now().isAfter(lateTime) || DateTime.now().isEqual(lateTime))) {
            LOG.debug("No feed with the specified name could be found: {}", feedName);
            builder.message("No feed with the specified name could be found: " + feedName)
                    .result(AssessmentResult.FAILURE);
            return;
        } else if (lastFeedTime == null && DateTime.now().isBefore(lateTime)) {
            LOG.debug("No feed with the specified name could be found: {}, but the it is still before the late time of {}", feedName,lateTime);
            builder.message("No feed with the specified name could be found: " + feedName + ", but the it is still before the late time of " + lateTime)
                    .result(AssessmentResult.SUCCESS);
        } else if (lastFeedTime != null && lastFeedTime.isAfter(expectedTime) && lastFeedTime.isBefore(lateTime)) {
            LOG.debug("Data for feed {} arrived on {}, which was before late time: ", feedName, lastFeedTime, lateTime);

            builder.message("Data for feed " + feedName + " arrived on " + lastFeedTime + ", which was before late time: " + lateTime)
                    .result(AssessmentResult.SUCCESS);
        } else {
            LOG.debug("Data for feed {} has not arrived before the late time: ", feedName, lateTime);

            builder.message("Data for feed " + feedName + " has not arrived before the late time: " + lateTime + "\n The last successful feed was on " + lastFeedTime)
                    .result(AssessmentResult.FAILURE);
        }
    }

    private Calendar getCalendar(FeedOnTimeArrivalMetric metric) {
        return this.calendarService.getCalendar(metric.getCalendarName());
    }

}
