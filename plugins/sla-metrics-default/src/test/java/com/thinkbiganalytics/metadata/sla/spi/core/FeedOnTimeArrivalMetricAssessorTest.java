package com.thinkbiganalytics.metadata.sla.spi.core;

/*-
 * #%L
 * thinkbig-sla-metrics-default
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.MetadataAction;
import com.thinkbiganalytics.metadata.api.MetadataCommand;
import com.thinkbiganalytics.metadata.api.MetadataRollbackAction;
import com.thinkbiganalytics.metadata.api.MetadataRollbackCommand;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.core.FeedOnTimeArrivalMetric;
import com.thinkbiganalytics.metadata.sla.api.core.FeedOnTimeArrivalMetricAssessor;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;
import com.thinkbiganalytics.scheduler.util.CronExpressionUtil;

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
import org.testng.Assert;

import java.security.Principal;
import java.text.ParseException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DateTime.class, CronExpressionUtil.class})
public class    FeedOnTimeArrivalMetricAssessorTest {

    private DateTime lateTime;
    private FeedOnTimeArrivalMetric metric;

    @Mock
    private OpsManagerFeedProvider feedProvider;

    @Mock
    private MetricAssessmentBuilder builder;

    private MetadataAccess metadataAccess = new MockMetadataAccess();

    @InjectMocks
    private FeedOnTimeArrivalMetricAssessor assessor = new FeedOnTimeArrivalMetricAssessor();

    private int lateTimeGracePeriod = 4;


    @Before
    public void setUp() throws Exception {
        initMocks(this);

        when(this.builder.message(any(String.class))).thenReturn(this.builder);
        when(this.builder.metric(any(Metric.class))).thenReturn(this.builder);
        when(this.builder.result(any(AssessmentResult.class))).thenReturn(this.builder);

        this.assessor.setMetadataAccess(this.metadataAccess);

        CronExpression cron = new CronExpression("0 0 12 1/1 * ? *");  // Noon every day

        this.lateTime = new DateTime(CronExpressionUtil.getPreviousFireTime(cron)).plusHours(4);
        this.metric = new FeedOnTimeArrivalMetric("feed",
                                                  cron,
                                                  Period.hours(lateTimeGracePeriod));
    }

    @Test
    public void testMinuteBeforeLate() throws ParseException {
        DateTime feedEnd = this.lateTime.minusMinutes(1);
        when(this.feedProvider.getLastActiveTimeStamp("feed")).thenReturn(feedEnd);

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
        when(this.feedProvider.getLastActiveTimeStamp("feed")).thenReturn(lastFeedTime);

        this.metric = new FeedOnTimeArrivalMetric("feed", cron, Period.hours(lateTimeGracePeriod));

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
        when(this.feedProvider.getLastActiveTimeStamp("feed")).thenReturn(lastFeedTime);
        this.metric = new FeedOnTimeArrivalMetric("feed", cron, Period.hours(lateTimeGracePeriod));

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
        when(this.feedProvider.getLastActiveTimeStamp("feed")).thenReturn(lastFeedTime);
        this.metric = new FeedOnTimeArrivalMetric("feed", cron, Period.hours(lateTimeGracePeriod));

        this.assessor.assess(metric, this.builder);

        //assert values
        verify(this.builder.result(AssessmentResult.FAILURE));
    }

    @Test
    public void testMinuteAfterLate() throws ParseException {
        DateTime now = this.lateTime.plusMinutes(2);
        DateTime feedEnd = this.lateTime.plusMinutes(1);

        PowerMockito.mockStatic(DateTime.class);
        BDDMockito.given(DateTime.now()).willReturn(now);

        when(this.feedProvider.getLastActiveTimeStamp("feed")).thenReturn(feedEnd);

        this.assessor.assess(metric, this.builder);
        // data is late by 1 min, but it has a 4 hr grace period
        verify(this.builder).result(AssessmentResult.FAILURE);
    }


    @Test
    public void testFeedNotFound() throws ParseException {
        when(this.feedProvider.getLastActiveTimeStamp("feed")).thenReturn(null);
        this.assessor.assess(metric, this.builder);

        verify(this.builder).result(AssessmentResult.WARNING);
    }

    public class MockMetadataAccess implements MetadataAccess {

        public MockMetadataAccess() {

        }

        @Override
        public <R> R commit(MetadataCommand<R> cmd, Principal... principals) {
            try {
                return cmd.execute();
            } catch (Exception e) {

            }
            return null;
        }

        @Override
        public <R> R commit(MetadataCommand<R> cmd, MetadataRollbackCommand rollbackCmd, Principal... principals) {
            try {
                return cmd.execute();
            } catch (Exception e) {

            }
            return null;
        }

        @Override
        public void commit(MetadataAction cmd, Principal... principals) {
            try {
                cmd.execute();
            } catch (Exception e) {

            }
        }

        @Override
        public void commit(MetadataAction cmd, MetadataRollbackAction rollbackAction, Principal... principals) {
            try {
                cmd.execute();
            } catch (Exception e) {

            }
        }

        @Override
        public <R> R read(MetadataCommand<R> cmd, Principal... principals) {
            try {
                return cmd.execute();
            } catch (Exception e) {

            }
            return null;
        }

        @Override
        public void read(MetadataAction cmd, Principal... principals) {
            try {
                cmd.execute();
            } catch (Exception e) {

            }
        }
    }

}
