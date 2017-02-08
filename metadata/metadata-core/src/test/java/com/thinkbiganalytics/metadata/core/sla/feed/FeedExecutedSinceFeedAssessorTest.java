package com.thinkbiganalytics.metadata.core.sla.feed;

/*-
 * #%L
 * thinkbig-metadata-core
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

import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedCriteria;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.op.FeedOperation;
import com.thinkbiganalytics.metadata.api.op.FeedOperationsProvider;
import com.thinkbiganalytics.metadata.api.sla.FeedExecutedSinceFeed;
import com.thinkbiganalytics.metadata.sla.api.AssessmentResult;
import com.thinkbiganalytics.metadata.sla.api.Metric;
import com.thinkbiganalytics.metadata.sla.api.MetricAssessment;
import com.thinkbiganalytics.metadata.sla.spi.MetricAssessmentBuilder;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeedExecutedSinceFeedAssessorTest {

    private FeedExecutedSinceFeed metric;
    private TestMetricAssessmentBuilder builder;

    @Before
    public void setUp() {
        metric = new FeedExecutedSinceFeed("mainCategory.mainFeed", "triggeredCategory.triggeredFeed");
        builder = new TestMetricAssessmentBuilder();
    }

    /**
     * Use case: <br>
     * - feed b depends on a <br>
     * - feed a1 completes
     */
    @Test
    public void testTriggeredNeverRan() throws Exception {
        int triggeredFeedStartTime = -1;
        boolean isTriggeredFeedRunning = false;
        int mainFeedStopTime = 1;
        boolean isMainFeedRunning = false;

        assertResult(AssessmentResult.SUCCESS, triggeredFeedStartTime, isTriggeredFeedRunning, mainFeedStopTime, isMainFeedRunning);
    }

    /**
     * Use case: <br>
     * - feed c depends on both a and b <br>
     * - feed c never ran <br>
     * - feed b never ran <br>
     * - feed a completes <br>
     * - assessment for both feed "a" and "b" will be done on feed "a" completion <br>
     * - this is the assessment scenario for feed b <br>
     */
    @Test
    public void testMainNeverRan() throws Exception {
        int triggeredFeedStartTime = 1;
        boolean isTriggeredFeedRunning = false;
        int mainFeedStopTime = -1;
        boolean isMainFeedRunning = false;

        assertResult(AssessmentResult.FAILURE, triggeredFeedStartTime, isTriggeredFeedRunning, mainFeedStopTime, isMainFeedRunning);
    }

    /**
     * Use case:  <br>
     * - feed a1 triggers feed b1 and feed b1 completes  <br>
     * - feed a2 completes <br>
     */
    @Test
    public void testTriggeredStartedBeforeMainStopped() throws Exception {
        int triggeredFeedStartTime = 1;
        boolean isTriggeredFeedRunning = false;
        int mainFeedStopTime = 2;
        boolean isMainFeedRunning = false;

        assertResult(AssessmentResult.SUCCESS, triggeredFeedStartTime, isTriggeredFeedRunning, mainFeedStopTime, isMainFeedRunning);
    }

    /**
     * Use case:  <br>
     * - feed c depends on both a and b  <br>
     * - feed a completes <br>
     * - feed b completes <br>
     * - feed c is triggered and completes <br>
     * - feed a completes <br>
     * - this is the case for feed b assessment on feed a's completion <br>
     */
    @Test
    public void testTriggeredStartedAfterMainStopped() throws Exception {
        int triggeredFeedStartTime = 2;
        boolean isTriggeredFeedRunning = false;
        int mainFeedStopTime = 1;
        boolean isMainFeedRunning = false;

        assertResult(AssessmentResult.FAILURE, triggeredFeedStartTime, isTriggeredFeedRunning, mainFeedStopTime, isMainFeedRunning);
    }

    /**
     * Use case: <br>
     * - feed c depends on a and b <br>
     * - feed a1 completes <br>
     * - feed b1 completes <br>
     * - feed c1 is triggered and completes <br>
     * - feed a2 is started and is still running <br>
     * - feed b2 completes <br>
     * - feed b2 is started and is still running <br>
     * - feed a2 completes <br>
     * - we are now assessing feed b - c feed would have started before feed b stopped and feed b is still running <br>
     */
    @Test
    public void testTriggeredStartedBeforeMainStoppedAndMainIsRunning() throws Exception {

        int triggeredFeedStartTime = 1;
        boolean isTriggeredFeedRunning = false;
        int mainFeedStopTime = 2;
        boolean isMainFeedRunning = true;

        assertResult(AssessmentResult.SUCCESS, triggeredFeedStartTime, isTriggeredFeedRunning, mainFeedStopTime, isMainFeedRunning);
    }

    /**
     * Use Case:  <br>
     * - feed c depends on both a and b <br>
     * - feed a1 completes <br>
     * - feed b1 completes <br>
     * - feed c1 triggered <br>
     * - feed a2 started and is still running <br>
     * - feed b2 completes <br>
     * - we are now assessing feed a <br>
     */
    @Test
    public void testTriggeredStartedAfterMainStoppedAndMainIsRunning() throws Exception {
        int triggeredFeedStartTime = 2;
        boolean isTriggeredFeedRunning = false;
        int mainFeedStopTime = 1;
        boolean isMainFeedRunning = true;

        assertResult(AssessmentResult.FAILURE, triggeredFeedStartTime, isTriggeredFeedRunning, mainFeedStopTime, isMainFeedRunning);
    }

    /**
     * Use Case;  <br>
     * - feed a1 triggered feed b1 and b1 completes <br>
     * - feed a2 triggered feed b2 and b2 is still running <br>
     * - feed a3 triggered feed b3 <br>
     */
    @Test
    public void testTriggeredStartedBeforeMainStoppedAndTriggeredIsRunning() throws Exception {
        int triggeredFeedStartTime = 1;
        boolean isTriggeredFeedRunning = true;
        int mainFeedStopTime = 2;
        boolean isMainFeedRunning = false;

        assertResult(AssessmentResult.SUCCESS, triggeredFeedStartTime, isTriggeredFeedRunning, mainFeedStopTime, isMainFeedRunning);
    }

    /**
     * Use Case:  <br>
     * - feed c depends on both a and b <br>
     * - feed a1 completes <br>
     * - feed b1 completes <br>
     * - feed c1 is triggered and is still running <br>
     * - feed b2 completes and c1 is still running <br>
     * - we are now assessing a's preconditions <br>
     */
    @Test
    public void testTriggeredStartedAfterMainStoppedAndTriggeredIsRunning() throws Exception {

        int triggeredFeedStartTime = 2;
        boolean isTriggeredFeedRunning = true;
        int mainFeedStopTime = 1;
        boolean isMainFeedRunning = false;

        assertResult(AssessmentResult.FAILURE, triggeredFeedStartTime, isTriggeredFeedRunning, mainFeedStopTime, isMainFeedRunning);
    }

    /**
     * Use Case:  <br>
     * - feed a1 triggers feed b1 and b1 is still running <br>
     * - feed a2 starts and is still running <br>
     * - feed a3 completes <br>
     */
    @Test
    public void testTriggeredStartedBeforeMainStoppedAndBothTriggeredAndMainAreRunning() throws Exception {

        int triggeredFeedStartTime = 1;
        boolean isTriggeredFeedRunning = true;
        int mainFeedStopTime = 2;
        boolean isMainFeedRunning = true;

        assertResult(AssessmentResult.SUCCESS, triggeredFeedStartTime, isTriggeredFeedRunning, mainFeedStopTime, isMainFeedRunning);
    }

    /**
     * Use Case:  <br>
     * - its possible!  <br>
     * - feed c depends on both a and b <br>
     * - feed a1 completes, feed b1 completes, triggered feed c1 completes <br>
     * - feed a2 starts and is running <br>
     * - feed a3 completes, feed b2 completes, triggered feed c2 started and is still running <br>
     * - feed b3 completes <br>
     * - we are now assessing feed a where feed a2 is still running and c2 is still running and c2 started after last a3 <br>
     */
    @Test
    public void testTriggeredStartedAfterMainStoppedAndBothTriggeredAndMainAreRunning() throws Exception {
        int triggeredFeedStartTime = 2;
        boolean isTriggeredFeedRunning = true;
        int mainFeedStopTime = 1;
        boolean isMainFeedRunning = true;

        assertResult(AssessmentResult.FAILURE, triggeredFeedStartTime, isTriggeredFeedRunning, mainFeedStopTime, isMainFeedRunning);
    }

    private void assertResult(AssessmentResult expected, int triggeredFeedStartTime, boolean isTriggeredFeedRunning, int mainFeedStopTime, boolean isMainFeedRunning) {
        FeedExecutedSinceFeedAssessor assessor = setUpAssessor(triggeredFeedStartTime, isTriggeredFeedRunning, mainFeedStopTime, isMainFeedRunning);
        assessor.assess(metric, builder);
        Assert.assertEquals(expected, builder.getResult());
    }

    /**
     * @param triggeredFeedStartTime pass negative value for empty operations list
     * @param mainFeedStopTime       pass negative for empty operations list
     */
    private FeedExecutedSinceFeedAssessor setUpAssessor(int triggeredFeedStartTime, boolean isTriggeredFeedRunning,
                                                        int mainFeedStopTime, boolean isMainFeedRunning) {
        FeedCriteria dummyCriteria = mock(FeedCriteria.class);
        when(dummyCriteria.name(Mockito.anyString())).thenReturn(dummyCriteria);
        when(dummyCriteria.category(Mockito.anyString())).thenReturn(dummyCriteria);

        FeedProvider feedProvider = mock(FeedProvider.class);
        when(feedProvider.feedCriteria()).thenReturn(dummyCriteria);

        List<Feed> triggeredFeeds = new ArrayList<>();
        Feed triggeredFeed = mock(Feed.class);
        Feed.ID triggeredFeedId = mock(Feed.ID.class);
        when(triggeredFeed.getId()).thenReturn(triggeredFeedId);
        triggeredFeeds.add(triggeredFeed);

        List<Feed> mainFeeds = new ArrayList<>();
        Feed mainFeed = mock(Feed.class);
        Feed.ID mainFeedId = mock(Feed.ID.class);
        when(mainFeed.getId()).thenReturn(mainFeedId);
        mainFeeds.add(mainFeed);
        when(feedProvider.getFeeds(dummyCriteria)).thenReturn(mainFeeds, triggeredFeeds);

        FeedOperationsProvider opsProvider = mock(FeedOperationsProvider.class);

        List<FeedOperation> triggeredFeedOps = new ArrayList<>();
        if (triggeredFeedStartTime > 0) {
            FeedOperation triggeredOp = mock(FeedOperation.class);
            when(triggeredOp.getStartTime()).thenReturn(new DateTime(triggeredFeedStartTime));
            triggeredFeedOps.add(triggeredOp);
        }

        List<FeedOperation> mainFeedOps = new ArrayList<>();
        if (mainFeedStopTime > 0) {
            FeedOperation mainFeedOp = mock(FeedOperation.class);
            when(mainFeedOp.getStopTime()).thenReturn(new DateTime(mainFeedStopTime));
            mainFeedOps.add(mainFeedOp);
        }

        when(opsProvider.findLatestCompleted(mainFeedId)).thenReturn(mainFeedOps);
        when(opsProvider.findLatest(triggeredFeedId)).thenReturn(triggeredFeedOps);

        when(opsProvider.isFeedRunning(mainFeedId)).thenReturn(isMainFeedRunning);
        when(opsProvider.isFeedRunning(triggeredFeedId)).thenReturn(isTriggeredFeedRunning);

        return new FeedExecutedSinceFeedAssessor() {
            @Override
            protected FeedProvider getFeedProvider() {
                return feedProvider;
            }

            @Override
            protected FeedOperationsProvider getFeedOperationsProvider() {
                return opsProvider;
            }
        };
    }

    class TestMetricAssessmentBuilder implements MetricAssessmentBuilder<Serializable> {

        AssessmentResult result;
        String message;
        Metric metric;

        public AssessmentResult getResult() {
            return result;
        }

        public String getMessage() {
            return message;
        }

        public Metric getMetric() {
            return metric;
        }

        @Override
        public MetricAssessmentBuilder<Serializable> metric(Metric metric) {
            this.metric = metric;
            return this;
        }

        @Override
        public MetricAssessmentBuilder<Serializable> message(String descr) {
            this.message = descr;
            return this;
        }

        @Override
        public MetricAssessmentBuilder<Serializable> comparitor(Comparator<MetricAssessment<Serializable>> comp) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MetricAssessmentBuilder<Serializable> compareWith(Comparable<? extends Serializable> value, Comparable<? extends Serializable>[] otherValues) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MetricAssessmentBuilder<Serializable> data(Serializable data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MetricAssessmentBuilder<Serializable> result(AssessmentResult result) {
            this.result = result;
            return this;
        }
    }

}
