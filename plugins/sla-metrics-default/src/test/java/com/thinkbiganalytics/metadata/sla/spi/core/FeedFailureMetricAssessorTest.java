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

import com.thinkbiganalytics.app.ServicesApplicationStartup;
import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeed;
import com.thinkbiganalytics.metadata.api.feed.OpsManagerFeedProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionContextValue;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionParameter;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobExecutionProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.job.BatchJobInstance;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiEventJobExecution;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStatisticsProvider;
import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStats;
import com.thinkbiganalytics.metadata.api.jobrepo.step.BatchStepExecution;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.core.FeedFailedMetric;
import com.thinkbiganalytics.metadata.sla.api.core.FeedFailureMetricAssessor;
import com.thinkbiganalytics.metadata.sla.api.core.FeedFailureService;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DateTime.class})
public class FeedFailureMetricAssessorTest {

    private FeedFailedMetric metric;

    @Mock
    private BatchJobExecutionProvider jobExecutionProvider;

    @Mock
    private MetricAssessmentBuilder builder;


    @Mock
    ServicesApplicationStartup servicesApplicationStartup;

    @Mock
    private OpsManagerFeedProvider feedProvider;


    @Mock
    private NifiFeedProcessorStatisticsProvider nifiFeedProcessorStatisticsProvider;

    private MetadataAccess metadataAccess = new MockMetadataAccess();

    @InjectMocks
    private FeedFailureMetricAssessor assessor = new FeedFailureMetricAssessor();

    @InjectMocks
    private FeedFailureService feedFailureService;


    @Test
    public void testFaiure1() {

    }


    @Before
    public void setUp() {
        initMocks(this);

        when(this.builder.message(any(String.class))).thenReturn(this.builder);
        when(this.builder.metric(any(Metric.class))).thenReturn(this.builder);
        when(this.builder.result(any(AssessmentResult.class))).thenReturn(this.builder);

        this.feedFailureService.setMetadataAccess(this.metadataAccess);
        this.assessor.setFeedFailureService(feedFailureService);

        this.metric = new FeedFailedMetric();

    }


    /**
     * Test streaming feed where jobs come in as success, failure, success.
     * This should fail
     */
    @Test
    public void testStreamingFailure() throws ParseException {

        DateTime lastAssessedTime = DateTime.now();
        String feedName = "feed";
        List<NifiFeedProcessorStats> streamingStats = new ArrayList<>();
        boolean isStream = true;
        this.metric.setFeedName(feedName);

        when(feedProvider.findByName(feedName)).thenReturn(newOpsManagerFeed(feedName, isStream));

        //completed, failed, completed.
        //should fail
        DateTime startingEvent = DateTime.now().minusMinutes(5);
        streamingStats.add(newStats(feedName, startingEvent, 0L));
        streamingStats.add(newStats(feedName, startingEvent.plusMinutes(2), 2L));
        streamingStats.add(newStats(feedName, startingEvent.plusMinutes(4), 0L));

        when(this.nifiFeedProcessorStatisticsProvider.findLatestFinishedStatsSince(Mockito.anyString(), Mockito.any(DateTime.class))).thenReturn(streamingStats);
        this.assessor.assess(metric, this.builder);
        verify(this.builder).result(AssessmentResult.FAILURE);
    }

    /**
     * Test streaming jobs containing success then failure
     */
    @Test
    public void testStreamingFailure2() throws ParseException {

        DateTime lastAssessedTime = DateTime.now();
        String feedName = "feed";
        List<NifiFeedProcessorStats> streamingStats = new ArrayList<>();
        boolean isStream = true;
        this.metric.setFeedName(feedName);

        when(feedProvider.findByName(feedName)).thenReturn(newOpsManagerFeed(feedName, isStream));

        //completed, failed, completed.
        //should fail
        DateTime startingEvent = DateTime.now().minusMinutes(5);
        streamingStats.add(newStats(feedName, startingEvent, 0L));
        streamingStats.add(newStats(feedName, startingEvent.plusMinutes(2), 5L));

        when(this.nifiFeedProcessorStatisticsProvider.findLatestFinishedStatsSince(Mockito.anyString(), Mockito.any(DateTime.class))).thenReturn(streamingStats);

        this.assessor.assess(metric, this.builder);

        verify(this.builder).result(AssessmentResult.FAILURE);
    }


    /**
     * Test successful streaming jobs
     */
    @Test
    public void testStreamingSuccess() throws ParseException {
        DateTime lastAssessedTime = DateTime.now();
        String feedName = "feed";
        List<NifiFeedProcessorStats> streamingStats = new ArrayList<>();
        boolean isStream = true;
        this.metric.setFeedName(feedName);

        when(feedProvider.findByName(feedName)).thenReturn(newOpsManagerFeed(feedName, isStream));

        //completed, failed, completed.
        //should fail
        DateTime startingEvent = DateTime.now().minusMinutes(5);
        streamingStats.add(newStats(feedName, startingEvent, 0L));
        streamingStats.add(newStats(feedName, startingEvent.plusMinutes(4), 0L));

        when(this.nifiFeedProcessorStatisticsProvider.findLatestFinishedStatsSince(Mockito.anyString(), Mockito.any(DateTime.class))).thenReturn(streamingStats);
        this.assessor.assess(metric, this.builder);

        verify(this.builder).result(AssessmentResult.SUCCESS);
    }

    /**
     * Tests no streaming feed jobs
     */
    @Test
    public void testNoStreamingFeeds() throws ParseException {
        DateTime lastAssessedTime = DateTime.now();
        String feedName = "feed";
        List<NifiFeedProcessorStats> streamingStats = new ArrayList<>();
        boolean isStream = true;
        this.metric.setFeedName(feedName);

        when(feedProvider.findByName(feedName)).thenReturn(newOpsManagerFeed(feedName, isStream));

        when(this.nifiFeedProcessorStatisticsProvider.findLatestFinishedStatsSince(Mockito.anyString(), Mockito.any(DateTime.class))).thenReturn(streamingStats);
        this.assessor.assess(metric, this.builder);

        verify(this.builder).result(AssessmentResult.SUCCESS);
    }

    /**
     * Tests when there are no batch jobs.
     */
    @Test
    public void testNoBatchJobs() throws ParseException {
        DateTime lastAssessedTime = DateTime.now();
        String feedName = "feed";
        List<NifiFeedProcessorStats> streamingStats = new ArrayList<>();
        boolean isStream = false;
        this.metric.setFeedName(feedName);

        when(feedProvider.findByName(feedName)).thenReturn(newOpsManagerFeed(feedName, isStream));

        //completed, failed, completed.
        //should fail
        DateTime startingEvent = DateTime.now().minusMinutes(5);

        List<? extends BatchJobExecution> batchJobs = new ArrayList<>();

        Mockito.when(this.jobExecutionProvider.findLatestFinishedJobForFeedSince(Mockito.anyString(), Mockito.any(DateTime.class))).thenAnswer(x -> batchJobs);
        Mockito.when(this.jobExecutionProvider.findLatestFinishedJobForFeed(Mockito.anyString())).thenAnswer(x -> null);
        this.assessor.assess(metric, this.builder);

        verify(this.builder).result(AssessmentResult.SUCCESS);
    }


    /**
     * Tests when no new jobs have been run since the last time, but the last job execution was failed
     */
    @Test
    public void testExistingBatchFailure() throws ParseException {
        DateTime lastAssessedTime = DateTime.now();
        String feedName = "feed";
        List<NifiFeedProcessorStats> streamingStats = new ArrayList<>();
        boolean isStream = false;
        this.metric.setFeedName(feedName);

        when(feedProvider.findByName(feedName)).thenReturn(newOpsManagerFeed(feedName, isStream));

        DateTime startingEvent = DateTime.now().minusMinutes(5);
        List<? extends BatchJobExecution> batchJobs = new ArrayList<>();
        BatchJobExecution jobExecution = newBatchJob(feedName, startingEvent, true);
        Mockito.when(this.jobExecutionProvider.findLatestFinishedJobForFeedSince(Mockito.anyString(), Mockito.any(DateTime.class))).thenAnswer(x -> batchJobs);
        Mockito.when(this.jobExecutionProvider.findLatestFinishedJobForFeed(Mockito.anyString())).thenAnswer(x -> jobExecution);

        this.assessor.assess(metric, this.builder);

        verify(this.builder).result(AssessmentResult.FAILURE);
    }


    /**
     * Test Batch Jobs that come in as success, failed, success.
     * This should fail
     */
    @Test
    public void testBatchFailure() throws ParseException {

        DateTime lastAssessedTime = DateTime.now();
        String feedName = "feed";
        List<NifiFeedProcessorStats> streamingStats = new ArrayList<>();
        boolean isStream = false;
        this.metric.setFeedName(feedName);

        when(feedProvider.findByName(feedName)).thenReturn(newOpsManagerFeed(feedName, isStream));

        //completed, failed, completed.
        //should fail
        DateTime startingEvent = DateTime.now().minusMinutes(5);

        List<? extends BatchJobExecution> batchJobs = new ArrayList<>();
        batchJobs.add(newBatchJob(feedName, startingEvent, false));
        batchJobs.add(newBatchJob(feedName, startingEvent.plusMinutes(2), true));
        batchJobs.add(newBatchJob(feedName, startingEvent.plusMinutes(4), false));

        Mockito.when(this.jobExecutionProvider.findLatestFinishedJobForFeedSince(Mockito.anyString(), Mockito.any(DateTime.class))).thenAnswer(x -> batchJobs);

        this.assessor.assess(metric, this.builder);
        verify(this.builder).result(AssessmentResult.FAILURE);
    }


    /**
     * Test batch jobs that come in  as succes, failure
     * This should fail
     */
    @Test
    public void testBatchFailure2() throws ParseException {

        DateTime lastAssessedTime = DateTime.now();
        String feedName = "feed";
        List<NifiFeedProcessorStats> streamingStats = new ArrayList<>();
        boolean isStream = false;
        this.metric.setFeedName(feedName);

        when(feedProvider.findByName(feedName)).thenReturn(newOpsManagerFeed(feedName, isStream));

        //completed, failed, completed.
        //should fail
        DateTime startingEvent = DateTime.now().minusMinutes(5);

        List<? extends BatchJobExecution> batchJobs = new ArrayList<>();
        batchJobs.add(newBatchJob(feedName, startingEvent, false));
        batchJobs.add(newBatchJob(feedName, startingEvent.plusMinutes(2), true));

        Mockito.when(this.jobExecutionProvider.findLatestFinishedJobForFeedSince(Mockito.anyString(), Mockito.any(DateTime.class))).thenAnswer(x -> batchJobs);

        this.assessor.assess(metric, this.builder);
        verify(this.builder).result(AssessmentResult.FAILURE);
    }

    /**
     * Test batch jobs that come in as succes, success
     */
    @Test
    public void testBatchSuccess() throws ParseException {

        DateTime lastAssessedTime = DateTime.now();
        String feedName = "feed";
        List<NifiFeedProcessorStats> streamingStats = new ArrayList<>();
        boolean isStream = false;
        this.metric.setFeedName(feedName);

        when(feedProvider.findByName(feedName)).thenReturn(newOpsManagerFeed(feedName, isStream));

        //completed, failed, completed.
        //should fail
        DateTime startingEvent = DateTime.now().minusMinutes(5);

        List<? extends BatchJobExecution> batchJobs = new ArrayList<>();
        batchJobs.add(newBatchJob(feedName, startingEvent, false));
        batchJobs.add(newBatchJob(feedName, startingEvent.plusMinutes(2), false));

        Mockito.when(this.jobExecutionProvider.findLatestFinishedJobForFeedSince(Mockito.anyString(), Mockito.any(DateTime.class))).thenAnswer(x -> batchJobs);

        this.assessor.assess(metric, this.builder);
        verify(this.builder).result(AssessmentResult.SUCCESS);
    }

    private OpsManagerFeed newOpsManagerFeed(String feedName, boolean isStream) {
        MockOpsManagerFeed feed = new MockOpsManagerFeed();
        feed.setName(feedName);
        feed.setStream(isStream);
        return feed;
    }

    private NifiFeedProcessorStats newStats(String feedName, DateTime eventTime, Long failedCount) {
        NifiFeedProcessorStats stats = new MockNifiFeedProcessorStats();
        stats.setFeedName(feedName);
        stats.setMinEventTime(eventTime);
        stats.setMaxEventTime(eventTime);
        stats.setFailedCount(failedCount);
        return stats;
    }

    private <T extends BatchJobExecution> T newBatchJob(String feedName, DateTime eventTime, boolean failed) {

        MockBatchJobExecution job = new MockBatchJobExecution();
        job.setEndTime(eventTime);
        if (failed) {
            job.setStatus(BatchJobExecution.JobStatus.FAILED);
        } else {
            job.setStatus(BatchJobExecution.JobStatus.COMPLETED);
        }
        return (T) job;

    }

    private class MockOpsManagerFeed implements OpsManagerFeed {

        private String name;
        private boolean stream;
        private FeedType feedType;
        private Long timeBetweenBatchJobs;


        @Override
        public ID getId() {
            return null;
        }

        @Override
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean isStream() {
            return stream;
        }

        public void setStream(boolean stream) {
            this.stream = stream;
        }

        @Override
        public FeedType getFeedType() {
            return feedType;
        }

        public void setFeedType(FeedType feedType) {
            this.feedType = feedType;
        }

        @Override
        public Long getTimeBetweenBatchJobs() {
            return timeBetweenBatchJobs;
        }

        public void setTimeBetweenBatchJobs(Long timeBetweenBatchJobs) {
            this.timeBetweenBatchJobs = timeBetweenBatchJobs;
        }
    }


    private class MockNifiFeedProcessorStats implements NifiFeedProcessorStats {

        private String feedName;
        private DateTime minEventTime;
        private Long failedCount;

        @Override
        public String getProcessorName() {
            return null;
        }

        @Override
        public void setProcessorName(String processorName) {

        }

        @Override
        public String getFeedName() {
            return null;
        }

        @Override
        public void setFeedName(String feedName) {

        }

        @Override
        public String getProcessorId() {
            return null;
        }

        @Override
        public void setProcessorId(String processorId) {

        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public void setId(String id) {

        }

        @Override
        public String getFeedProcessGroupId() {
            return null;
        }

        @Override
        public void setFeedProcessGroupId(String feedProcessGroupId) {

        }

        @Override
        public String getCollectionId() {
            return null;
        }

        @Override
        public void setCollectionId(String collectionId) {

        }

        @Override
        public Long getDuration() {
            return null;
        }

        @Override
        public void setDuration(Long duration) {

        }

        @Override
        public Long getBytesIn() {
            return null;
        }

        @Override
        public void setBytesIn(Long bytesIn) {

        }

        @Override
        public Long getBytesOut() {
            return null;
        }

        @Override
        public void setBytesOut(Long bytesOut) {

        }

        @Override
        public Long getTotalCount() {
            return null;
        }

        @Override
        public void setTotalCount(Long totalCount) {

        }

        @Override
        public Long getFailedCount() {
            return failedCount;
        }

        @Override
        public void setFailedCount(Long failedCount) {
            this.failedCount = failedCount;
        }

        @Override
        public Long getJobsStarted() {
            return null;
        }

        @Override
        public void setJobsStarted(Long jobsStarted) {

        }

        @Override
        public Long getJobsFinished() {
            return null;
        }

        @Override
        public void setJobsFinished(Long jobsFinished) {

        }

        @Override
        public Long getJobsFailed() {
            return null;
        }

        @Override
        public void setJobsFailed(Long jobsFailed) {

        }

        @Override
        public Long getJobDuration() {
            return null;
        }

        @Override
        public void setJobDuration(Long jobDuration) {

        }

        @Override
        public Long getSuccessfulJobDuration() {
            return null;
        }

        @Override
        public void setSuccessfulJobDuration(Long successfulJobDuration) {

        }

        @Override
        public Long getProcessorsFailed() {
            return null;
        }

        @Override
        public void setProcessorsFailed(Long processorsFailed) {

        }

        @Override
        public Long getFlowFilesStarted() {
            return null;
        }

        @Override
        public void setFlowFilesStarted(Long flowFilesStarted) {

        }

        @Override
        public Long getFlowFilesFinished() {
            return null;
        }

        @Override
        public void setFlowFilesFinished(Long flowFilesFinished) {

        }

        @Override
        public DateTime getCollectionTime() {
            return null;
        }

        @Override
        public void setCollectionTime(DateTime collectionTime) {

        }

        @Override
        public DateTime getMinEventTime() {
            return minEventTime;
        }

        @Override
        public void setMinEventTime(DateTime minEventTime) {
            this.minEventTime = minEventTime;
        }


        @Override
        public Long getMinEventTimeMillis() {
            return minEventTime != null ? minEventTime.getMillis() : null;
        }

        @Override
        public void setMinEventTimeMillis(Long minEventTimeMillis) {

        }

        @Override
        public DateTime getMaxEventTime() {
            return minEventTime;
        }

        @Override
        public void setMaxEventTime(DateTime maxEventTime) {

        }

        @Override
        public Long getMaxEventId() {
            return null;
        }

        @Override
        public void setMaxEventId(Long maxEventId) {

        }

        @Override
        public String getClusterNodeId() {
            return null;
        }

        @Override
        public void setClusterNodeId(String clusterNodeId) {

        }

        @Override
        public String getClusterNodeAddress() {
            return null;
        }

        @Override
        public void setClusterNodeAddress(String clusterNodeAddress) {

        }

        @Override
        public Long getCollectionIntervalSeconds() {
            return null;
        }

        @Override
        public void setCollectionIntervalSeconds(Long collectionIntervalSeconds) {

        }

        @Override
        public BigDecimal getJobsStartedPerSecond() {
            return null;
        }

        @Override
        public void setJobsStartedPerSecond(BigDecimal jobsStartedPerSecond) {

        }

        @Override
        public BigDecimal getJobsFinishedPerSecond() {
            return null;
        }

        @Override
        public void setJobsFinishedPerSecond(BigDecimal jobsFinishedPerSecond) {

        }

        @Override
        public String getLatestFlowFileId() {
            return null;
        }

        @Override
        public void setLatestFlowFileId(String flowFileId) {

        }

        @Override
        public String getErrorMessages() {
            return null;
        }

        @Override
        public DateTime getErrorMessageTimestamp() {
            return null;
        }
    }


    private class MockBatchJobExecution implements BatchJobExecution {

        private DateTime endTime;
        private JobStatus status;
        private boolean stream;

        @Override
        public BatchJobInstance getJobInstance() {
            return null;
        }

        @Override
        public Long getJobExecutionId() {
            return null;
        }

        @Override
        public Long getVersion() {
            return null;
        }

        @Override
        public DateTime getCreateTime() {
            return null;
        }

        @Override
        public void setCreateTime(DateTime createTime) {

        }

        @Override
        public DateTime getStartTime() {
            return null;
        }

        @Override
        public void setStartTime(DateTime startTime) {

        }

        @Override
        public DateTime getEndTime() {
            return endTime;
        }

        @Override
        public void setEndTime(DateTime endTime) {
            this.endTime = endTime;
        }

        @Override
        public JobStatus getStatus() {
            return status;
        }

        @Override
        public void setStatus(JobStatus status) {
            this.status = status;
        }

        @Override
        public ExecutionConstants.ExitCode getExitCode() {
            return null;
        }

        @Override
        public void setExitCode(ExecutionConstants.ExitCode exitCode) {

        }

        @Override
        public String getExitMessage() {
            return null;
        }

        @Override
        public void setExitMessage(String exitMessage) {

        }

        @Override
        public DateTime getLastUpdated() {
            return null;
        }

        @Override
        public Set<? extends BatchJobExecutionParameter> getJobParameters() {
            return null;
        }

        @Override
        public Map<String, String> getJobParametersAsMap() {
            return null;
        }

        @Override
        public Set<BatchStepExecution> getStepExecutions() {
            return null;
        }

        @Override
        public Set<BatchJobExecutionContextValue> getJobExecutionContext() {
            return null;
        }

        @Override
        public Map<String, String> getJobExecutionContextAsMap() {
            return null;
        }

        @Override
        public NifiEventJobExecution getNifiEventJobExecution() {
            return null;
        }

        @Override
        public void setNifiEventJobExecution(NifiEventJobExecution nifiEventJobExecution) {

        }

        @Override
        public boolean isFailed() {
            return JobStatus.FAILED == status;
        }

        @Override
        public boolean isSuccess() {
            return JobStatus.COMPLETED == status;
        }

        @Override
        public boolean isFinished() {
            return JobStatus.COMPLETED == status;
        }

        @Override
        public boolean isStream() {
            return stream;
        }

        public void setStream(boolean stream) {
            this.stream = stream;
        }
    }
}

