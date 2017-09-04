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
import com.thinkbiganalytics.discovery.model.DefaultHiveSchema;
import com.thinkbiganalytics.discovery.schema.Field;
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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Basic Feed Integration Test which imports data index feed, creates category, imports data ingest template,
 * creates data ingest feed, runs the feed, validates number of executed jobs, validates validators and
 * standardisers have been applied by looking at profiler summary, validates total number and number of
 * valid and invalid rows, validates expected hive tables have been created and runs a simple hive
 * query and asserts the number of rows returned.
 *
 */
public class FeedIT extends IntegrationTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(FeedIT.class);

    private static final int FEED_COMPLETION_WAIT_DELAY = 180;
    private static final int VALID_RESULTS = 879;
    private static String FEED_NAME = "users_" + System.currentTimeMillis();


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

        //TODO replace import via AdminController with AdminControllerV2
        ExportImportTemplateService.ImportTemplate ingest = importDataIngestTemplate();

        //create standard ingest feed
        FeedMetadata feed = getCreateFeedRequest(category, ingest, FEED_NAME);
        FeedMetadata response = createFeed(feed).getFeedMetadata();
        Assert.assertEquals(feed.getFeedName(), response.getFeedName());

        waitForFeedToComplete();

        assertExecutedJobs(response);

        //TODO edit the feed / re-run / re-assert
    }

    @Override
    public void teardown() {
//        super.teardown();
    }

    @Override
    public void startClean() {
        super.startClean();
    }

//    @Test
    public void temp() {
//        FEED_NAME = "users_1504528826443";
//        assertHiveData();
    }


    private void waitForFeedToComplete() {
        //wait for feed completion by waiting for certain amount of time and then
        waitFor(FEED_COMPLETION_WAIT_DELAY, TimeUnit.SECONDS, "for feed to complete");
    }

    public void assertExecutedJobs(FeedMetadata feed) throws IOException {
        LOG.info("Asserting there are 2 completed jobs: userdata ingest job, index text service system jobs");
        DefaultExecutedJob[] jobs = getJobs();
        Assert.assertEquals(2, jobs.length);

        //TODO assert all executed jobs are successful

        DefaultExecutedJob ingest = Arrays.stream(jobs).filter(job -> ("functional_tests." + feed.getFeedName().toLowerCase()).equals(job.getFeedName())).findFirst().get();
        Assert.assertEquals(ExecutionStatus.COMPLETED, ingest.getStatus());
        Assert.assertEquals(ExitStatus.COMPLETED.getExitCode(), ingest.getExitCode());

        LOG.info("Asserting user data jobs has expected number of steps");
        DefaultExecutedJob job = getJobWithSteps(ingest.getExecutionId());
        Assert.assertEquals(ingest.getExecutionId(), job.getExecutionId());
        List<ExecutedStep> steps = job.getExecutedSteps();
        Assert.assertEquals(21, steps.size());
        for (ExecutedStep step : steps) {
            Assert.assertEquals(ExitStatus.COMPLETED.getExitCode(), step.getExitCode());
        }

        String feedId = feed.getFeedId();
        LOG.info("Asserting number of total/valid/invalid rows");
        Assert.assertEquals(1000, getTotalNumberOfRecords(feedId));
        Assert.assertEquals(VALID_RESULTS, getNumberOfValidRecords(feedId));
        Assert.assertEquals(121, getNumberOfInvalidRecords(feedId));

        assertValidatorsAndStandardisers(feedId);

        //TODO assert data via global search
        assertHiveData();
    }

    private void assertValidatorsAndStandardisers(String feedId) {
        LOG.info("Asserting Validators and Standardisers");

        String processingDttm = getProcessingDttm(feedId);

        assertNamesAreInUppercase(feedId, processingDttm);
        assertMultipleBase64Encodings(feedId, processingDttm);
        assertBinaryColumnData();

        assertValidatorResults(feedId, processingDttm, "LengthValidator", 47);
        assertValidatorResults(feedId, processingDttm, "NotNullValidator", 67);
        assertValidatorResults(feedId, processingDttm, "EmailValidator", 3);
        assertValidatorResults(feedId, processingDttm, "LookupValidator", 4);
        assertValidatorResults(feedId, processingDttm, "IPAddressValidator", 4);
    }

    private void assertHiveData() {
        assertHiveTables("functional_tests", FEED_NAME);
        getHiveSchema("functional_tests", FEED_NAME);
        List<HashMap<String, String>> rows = getHiveQuery("SELECT * FROM " + "functional_tests" + "." + FEED_NAME + " LIMIT 880");
        Assert.assertEquals(VALID_RESULTS, rows.size());
    }

    private void assertBinaryColumnData() {
        LOG.info("Asserting binary CC column data");
        DefaultHiveSchema schema = getHiveSchema("functional_tests", FEED_NAME);
        Field ccField = schema.getFields().stream().filter(field -> field.getName().equals("cc")).iterator().next();
        Assert.assertEquals("binary", ccField.getDerivedDataType());

        List<HashMap<String, String>> rows = getHiveQuery("SELECT cc FROM " + "functional_tests" + "." + FEED_NAME + " where id = 1");
        Assert.assertEquals(1, rows.size());
        HashMap<String, String> row = rows.get(0);

        // where TmpjMU9UVXlNVGcyTkRreU1ERXhOZz09 is double Base64 encoding for cc field of the first row (6759521864920116),
        // one base64 encoding by our standardiser and second base64 encoding by spring framework for returning binary data
        Assert.assertEquals("TmpjMU9UVXlNVGcyTkRreU1ERXhOZz09", row.get("cc"));
    }

    private void assertNamesAreInUppercase(String feedId, String processingDttm) {
        LOG.info("Asserting all names are in upper case");
        String topN = getProfileStatsForColumn(feedId, processingDttm, "TOP_N_VALUES", "first_name");
        Assert.assertTrue(CharMatcher.JAVA_LOWER_CASE.matchesNoneOf(topN));
    }

    private void assertMultipleBase64Encodings(String feedId, String processingDttm) {
        LOG.info("Asserting multiple base 64 encoding and decoding, which also operate on different data types (string and binary), produce expected initial human readable form");
        String countries = getProfileStatsForColumn(feedId, processingDttm, "TOP_N_VALUES", "country");
        Assert.assertTrue(countries.contains("China"));
        Assert.assertTrue(countries.contains("Indonesia"));
        Assert.assertTrue(countries.contains("Russia"));
        Assert.assertTrue(countries.contains("Philippines"));
        Assert.assertTrue(countries.contains("Brazil"));
    }
}
