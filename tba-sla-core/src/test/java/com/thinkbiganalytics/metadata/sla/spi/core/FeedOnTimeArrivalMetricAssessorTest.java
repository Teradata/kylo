package com.thinkbiganalytics.metadata.sla.spi.core;

import com.thinkbiganalytics.calendar.HolidayCalendarService;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.core.FeedOnTimeArrivalMetric;
import com.thinkbiganalytics.metadata.sla.api.core.FeedOnTimeArrivalMetricAssessor;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.pipelinecontroller.repositories.FeedRepository;
import com.thinkbiganalytics.pipelinecontroller.rest.dataobjects.ExecutedFeed;
import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.quartz.CronExpression;
import org.quartz.impl.calendar.HolidayCalendar;

import java.text.ParseException;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class FeedOnTimeArrivalMetricAssessorTest {

    private DateTime lateTime;
    private FeedOnTimeArrivalMetric metric;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private HolidayCalendar calendar;

    @Mock
    private HolidayCalendarService calendarService;

    @Mock
    private MetricAssessmentBuilder builder;

    @InjectMocks
    private FeedOnTimeArrivalMetricAssessor assessor = new FeedOnTimeArrivalMetricAssessor();

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        when(this.calendarService.getCalendar(any(String.class))).thenReturn(this.calendar);
        when(this.builder.message(any(String.class))).thenReturn(this.builder);
        when(this.builder.metric(any(Metric.class))).thenReturn(this.builder);
        when(this.builder.result(any(AssessmentResult.class))).thenReturn(this.builder);

        CronExpression cron = new CronExpression("0 0 12 1/1 * ? *");  // Noon every day

        this.lateTime = new DateTime(CronExpressionUtil.getPreviousFireTime(cron)).plusHours(4);
        this.metric = new FeedOnTimeArrivalMetric("feed",
                cron,
                Period.hours(4),
                Period.days(2),
                "USA");
    }

    @Test
    public void testMinuteBeforeLate() throws ParseException {
        DateTime feedEnd = this.lateTime.minusMinutes(1);
        when(this.feedRepository.findLastCompletedFeed("feed")).thenReturn(createExecutedFeed(feedEnd));
        when(this.calendar.isTimeIncluded(anyLong())).thenReturn(true);

        this.assessor.assess(metric, this.builder);

        verify(this.builder).result(AssessmentResult.SUCCESS);
    }

    @Test
    public void testNewFeedSuccess() throws ParseException {
        DateTime feedEnd = null;

        CronExpression cron = new CronExpression("0 0 0/1 1/1 * ? *"); //fire every hour

        this.lateTime = new DateTime(CronExpressionUtil.getPreviousFireTime(cron)).plusHours(4);
        this.metric = new FeedOnTimeArrivalMetric("feed",
                cron,
                Period.hours(4),
                Period.days(2),
                "USA");


        when(this.calendar.isTimeIncluded(anyLong())).thenReturn(true);

        this.assessor.assess(metric, this.builder);

        verify(this.builder).result(AssessmentResult.SUCCESS);
    }

    @Test
    public void testNewFeedFailure() throws ParseException {
        DateTime feedEnd = null;

        CronExpression cron = new CronExpression("0 0 0/3 1/1 * ? *"); //fire every 3 hours

        this.lateTime = new DateTime(CronExpressionUtil.getPreviousFireTime(cron)).plusHours(1);
        this.metric = new FeedOnTimeArrivalMetric("feed",
                cron,
                Period.hours(1),
                Period.days(0),
                "USA");


        when(this.calendar.isTimeIncluded(anyLong())).thenReturn(true);

        this.assessor.assess(metric, this.builder);

        verify(this.builder).result(AssessmentResult.FAILURE);
    }

    @Test
    public void testMinuteAfterLate() throws ParseException {
        DateTime feedEnd = this.lateTime.plusMinutes(1);
        when(this.feedRepository.findLastCompletedFeed("feed")).thenReturn(createExecutedFeed(feedEnd));
        when(this.calendar.isTimeIncluded(anyLong())).thenReturn(true);

        this.assessor.assess(metric, this.builder);

        verify(this.builder).result(AssessmentResult.FAILURE);
    }

    @Test
    public void testLateButHoliday() throws ParseException {
        DateTime feedEnd = this.lateTime.plusMinutes(1);
        when(this.feedRepository.findLastCompletedFeed("feed")).thenReturn(createExecutedFeed(feedEnd));
        when(this.calendar.isTimeIncluded(anyLong())).thenReturn(false);

        this.assessor.assess(metric, this.builder);

        verify(this.builder).result(AssessmentResult.SUCCESS);
    }

    @Test
    public void testFeedNotFound() throws ParseException {
        when(this.feedRepository.findLastCompletedFeed("feed")).thenReturn(null);
        when(this.calendar.isTimeIncluded(anyLong())).thenReturn(true);
        this.assessor.assess(metric, this.builder);

        verify(this.builder).result(AssessmentResult.FAILURE);
    }


    private ExecutedFeed createExecutedFeed(final DateTime endTime) {
        ExecutedFeed feed = new ExecutedFeed();
        feed.setEndTime(endTime);
        return feed;
    }
}
