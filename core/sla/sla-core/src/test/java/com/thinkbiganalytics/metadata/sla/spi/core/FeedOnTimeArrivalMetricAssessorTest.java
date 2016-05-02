package com.thinkbiganalytics.metadata.sla.spi.core;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.text.ParseException;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.quartz.CronExpression;
import org.quartz.impl.calendar.HolidayCalendar;
import org.testng.Assert;

import com.thinkbiganalytics.calendar.HolidayCalendarService;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedFeed;
import com.thinkbiganalytics.jobrepo.repository.FeedRepository;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.core.FeedOnTimeArrivalMetric;
import com.thinkbiganalytics.metadata.sla.api.core.FeedOnTimeArrivalMetricAssessor;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DateTime.class, CronExpressionUtil.class})
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

    private int lateTimeGracePeriod = 4;

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
                Period.hours(lateTimeGracePeriod),
                Period.days(2),
                "USA");
    }

    @Test
    public void testMinuteBeforeLate() throws ParseException {
        DateTime feedEnd = this.lateTime.minusMinutes(1);
        ExecutedFeed feed = createExecutedFeed(feedEnd);
        when(this.feedRepository.findLastCompletedFeed("feed")).thenReturn(feed);
        when(this.calendar.isTimeIncluded(anyLong())).thenReturn(true);

        this.assessor.assess(metric, this.builder);

        verify(this.builder).result(AssessmentResult.SUCCESS);
    }

    @Test
    /**
     * test use case where the date window is still valid, but still no data has been found for the Feed.
     * This will return no Assessment result as it still could process data
     */
    public void testNoDataButStillBeforeLateTime() throws ParseException {

        PowerMockito.mockStatic(DateTime.class);
        PowerMockito.mockStatic(CronExpressionUtil.class);

        DateTime now = new DateTime().minusHours(2);

        //Some Cron Expression .. Mockito will overwrite
        CronExpression cron = new CronExpression("0 0 0/2 1/1 * ? *");

        //set the current time to a known time
        BDDMockito.given(DateTime.now()).willReturn(now);
        //set the previous fire date to a known time in the past,but within the window
        DateTime previousFireTime = new DateTime(now).minusHours(3);
        BDDMockito.given(CronExpressionUtil.getPreviousFireTime(cron)).willReturn(previousFireTime.toDate());
        //window is = (now - 3)  - (now -3) + lateTime)
        //Some Feed End Time to a time not within this window
        DateTime lastFeedTime = new DateTime().minusWeeks(2);
        ExecutedFeed feed = createExecutedFeed(lastFeedTime);
        when(this.feedRepository.findLastCompletedFeed("feed")).thenReturn(feed);

        this.metric = new FeedOnTimeArrivalMetric("feed", cron, Period.hours(lateTimeGracePeriod), Period.days(2),"USA");

        when(this.calendar.isTimeIncluded(anyLong())).thenReturn(true);
        this.assessor.assess(metric, this.builder);

        //assert values
        DateTime lateTime = previousFireTime.plusHours(lateTimeGracePeriod);
        Assert.assertTrue(now.isBefore(lateTime) && !(lastFeedTime.isAfter(previousFireTime) && lastFeedTime.isBefore(lateTime)));
        verify(this.builder);
    }

    @Test
    public void testSuccess() throws ParseException {

        PowerMockito.mockStatic(DateTime.class);
        PowerMockito.mockStatic(CronExpressionUtil.class);

        DateTime now = new DateTime().minusHours(2);

             //set the current time to a known time
        BDDMockito.given(DateTime.now()).willReturn(now);
        //set the previous fire date to a known time in the past,but within the window
        DateTime previousFireTime = new DateTime(now).minusHours(3);

        //Some Cron Expression .. Mockito will overwrite
        CronExpression cron = new CronExpression("0 0 0/2 1/1 * ? *");
        BDDMockito.given(CronExpressionUtil.getPreviousFireTime(cron)).willReturn(previousFireTime.toDate());
        //window is = (now - 3)  - (now -3) + lateTime)
        //Some Feed End Time to a time within this window
        DateTime lastFeedTime = new DateTime(previousFireTime).plus(lateTimeGracePeriod - 1);
        ExecutedFeed feed = createExecutedFeed(lastFeedTime);
        when(this.feedRepository.findLastCompletedFeed("feed")).thenReturn(feed);
        when(this.calendar.isTimeIncluded(anyLong())).thenReturn(true);
        this.metric = new FeedOnTimeArrivalMetric("feed", cron, Period.hours(lateTimeGracePeriod), Period.days(2),"USA");

        this.assessor.assess(metric, this.builder);

        //assert values
        DateTime lateTime = previousFireTime.plusHours(lateTimeGracePeriod);
        Assert.assertTrue((lastFeedTime.isAfter(previousFireTime) && lastFeedTime.isBefore(lateTime)));
        verify(this.builder.result(AssessmentResult.SUCCESS));
    }

    @Test
    public void testFailure() throws ParseException {

        PowerMockito.mockStatic(DateTime.class);
        PowerMockito.mockStatic(CronExpressionUtil.class);

        DateTime now = new DateTime().minusHours(2);

        //Some Cron Expression .. Mockito will overwrite
        CronExpression cron = new CronExpression("0 0 0/2 1/1 * ? *");

        //set the current time to a known time
        BDDMockito.given(DateTime.now()).willReturn(now);
        //set the previous fire date to a known time in the past
        DateTime previousFireTime = new DateTime(now).minusHours(4);
        BDDMockito.given(CronExpressionUtil.getPreviousFireTime(cron)).willReturn(previousFireTime.toDate());
        //window is = (now - 4)  - (now -4) + lateTimeGracePeriod)
        //Some Feed End Time to a time outside the window
        DateTime lastFeedTime = new DateTime(previousFireTime).minusHours(lateTimeGracePeriod + 1);
        ExecutedFeed feed = createExecutedFeed(lastFeedTime);
        when(this.feedRepository.findLastCompletedFeed("feed")).thenReturn(feed);
        when(this.calendar.isTimeIncluded(anyLong())).thenReturn(true);
        this.metric = new FeedOnTimeArrivalMetric("feed", cron, Period.hours(lateTimeGracePeriod), Period.days(2),"USA");

        this.assessor.assess(metric, this.builder);

        //assert values
        verify(this.builder.result(AssessmentResult.FAILURE));
    }

    @Test
    public void testMinuteAfterLate() throws ParseException {
        DateTime now = this.lateTime.plusMinutes(2);
        DateTime feedEnd = this.lateTime.plusMinutes(1);
        ExecutedFeed feed = createExecutedFeed(feedEnd);
        
        PowerMockito.mockStatic(DateTime.class);
        BDDMockito.given(DateTime.now()).willReturn(now);
        when(this.feedRepository.findLastCompletedFeed("feed")).thenReturn(feed);
        when(this.calendar.isTimeIncluded(anyLong())).thenReturn(true);

        this.assessor.assess(metric, this.builder);

        verify(this.builder).result(AssessmentResult.FAILURE);
    }

    @Test
    public void testLateButHoliday() throws ParseException {
        DateTime now = this.lateTime.plusMinutes(2);
        DateTime feedEnd = this.lateTime.plusMinutes(1);
        ExecutedFeed feed = createExecutedFeed(feedEnd);
        
        PowerMockito.mockStatic(DateTime.class);
        BDDMockito.given(DateTime.now()).willReturn(now);
        when(this.feedRepository.findLastCompletedFeed("feed")).thenReturn(feed);
        when(this.calendar.isTimeIncluded(anyLong())).thenReturn(false);

        this.assessor.assess(metric, this.builder);

        verify(this.builder).result(AssessmentResult.SUCCESS);
    }

    @Test
    public void testFeedNotFound() throws ParseException {
        when(this.feedRepository.findLastCompletedFeed("feed")).thenReturn(null);
        when(this.calendar.isTimeIncluded(anyLong())).thenReturn(true);
        this.assessor.assess(metric, this.builder);

        verify(this.builder).result(AssessmentResult.WARNING);
    }


    private ExecutedFeed createExecutedFeed(DateTime endTime) {
        ExecutedFeed feed = mock(ExecutedFeed.class);
        when(feed.getEndTime()).thenReturn(endTime);
//        ExecutedFeed feed = new ExecutedFeed();
//        feed.setEndTime(endTime);
        return feed;
    }
}
