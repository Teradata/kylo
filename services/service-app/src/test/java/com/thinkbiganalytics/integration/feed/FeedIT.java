package com.thinkbiganalytics.integration.feed;

/*-
 * #%L
 * kylo-feed-manager-controller
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.CharMatcher;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.thinkbiganalytics.feedmgr.rest.controller.FeedRestController;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.service.template.ExportImportTemplateService;
import com.thinkbiganalytics.integration.IntegrationTestBase;
import com.thinkbiganalytics.jobrepo.query.model.DefaultExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.DefaultExecutedStep;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedStep;
import com.thinkbiganalytics.jobrepo.query.model.ExecutionStatus;
import com.thinkbiganalytics.jobrepo.query.model.ExitStatus;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Basic Feed Integration Test which imports two system feeds, creates category, imports data ingest template,
 * creates data ingest feed, runs the feed, validates number of executed jobs, validates validators and
 * standardisers have been applied by looking at profiler summary, validates total number and number of
 * valid and invalid rows, validates expected hive tables have been created and runs a simple hive
 * query and asserts the number of rows returned.
 *
 */
public class FeedIT extends IntegrationTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(FeedIT.class);

    private static final int FEED_COMPLETION_WAIT_DELAY = 180;


    @Override
    protected void configureObjectMapper(ObjectMapper om) {
        SimpleModule m = new SimpleModule();
        m.addAbstractTypeMapping(ExecutedStep.class, DefaultExecutedStep.class);
        om.registerModule(m);
    }

    @Test
    public void testDataIngestFeed() throws IOException {
        importSystemFeeds();

        copyDataToDropzone();

        //create new category
        FeedCategory category = createCategory("Functional Tests");

        ExportImportTemplateService.ImportTemplate ingest = importDataIngestTemplate();

        //create standard ingest feed
        FeedMetadata feed = getCreateFeedRequest(category, ingest, "Users1");
        FeedMetadata response = createFeed(feed).getFeedMetadata();
        Assert.assertEquals(feed.getFeedName(), response.getFeedName());

        waitForFeedToComplete();

        assertExecutedJobs(response);

        //TODO edit the feed / re-run / re-assert
    }

//    @Override
//    protected void cleanup() {
        //do nothing to run temp() test
//    }


//    @Test
    public void temp() {
        importSystemFeeds();
    }

    private void waitForFeedToComplete() {
        //wait for feed completion by waiting for certain amount of time and then
        waitFor(FEED_COMPLETION_WAIT_DELAY, TimeUnit.SECONDS, "for feed to complete");
    }

    public void assertExecutedJobs(FeedMetadata feed) throws IOException {
        LOG.info("Asserting there are 3 completed jobs: userdata ingest job, schema and text system jobs");
        DefaultExecutedJob[] jobs = getJobs();
        Assert.assertEquals(3, jobs.length);

        DefaultExecutedJob ingest = Arrays.stream(jobs).filter(job -> ("functional_tests." + feed.getFeedName().toLowerCase()).equals(job.getFeedName())).findFirst().get();
        Assert.assertEquals(ExecutionStatus.COMPLETED, ingest.getStatus());
        Assert.assertEquals(ExitStatus.COMPLETED.getExitCode(), ingest.getExitCode());

        LOG.info("Asserting user data jobs has expected number of steps");
        DefaultExecutedJob job = getJobWithSteps(ingest.getExecutionId());
        Assert.assertEquals(ingest.getExecutionId(), job.getExecutionId());
        List<ExecutedStep> steps = job.getExecutedSteps();
        Assert.assertEquals(24, steps.size());
        for (ExecutedStep step : steps) {
            Assert.assertEquals(ExitStatus.COMPLETED.getExitCode(), step.getExitCode());
        }

        LOG.info("Asserting number of total/valid/invalid rows");
        Assert.assertEquals(1001, getTotalNumberOfRecords(feed.getFeedId()));
        Assert.assertEquals(984, getNumberOfValidRecords(feed.getFeedId()));
        Assert.assertEquals(17, getNumberOfInvalidRecords(feed.getFeedId()));

        assertNamesAreInUppercase(feed.getFeedId());

        assertHiveData();

        //TODO assert data via global search

    }

    private void assertHiveData() {
        assertHiveTables("functional_tests", "users1");
        assertHiveSchema("functional_tests", "users1");
        assertHiveQuery("functional_tests", "users1");
    }

    private void assertNamesAreInUppercase(String feedId) {
        LOG.info("Asserting all names are in upper case");

        String processingDttm = getProcessingDttm(feedId);

        Response response = given(FeedRestController.BASE)
            .when()
            .get(String.format("/%s/profile-stats?processingdttm=%s", feedId, processingDttm));

        response.then().statusCode(HTTP_OK);

        String topN = JsonPath.from(response.asString()).getString("find {entry ->entry.metrictype == 'TOP_N_VALUES' && entry.columnname == 'first_name'}.metricvalue");
        Assert.assertTrue(CharMatcher.JAVA_LOWER_CASE.matchesNoneOf(topN));
    }
}
