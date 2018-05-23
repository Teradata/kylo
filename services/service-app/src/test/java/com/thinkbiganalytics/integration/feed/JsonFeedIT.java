package com.thinkbiganalytics.integration.feed;

/*-
 * #%L
 * kylo-service-app
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics
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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.CharMatcher;
import com.thinkbiganalytics.discovery.model.DefaultHiveSchema;
import com.thinkbiganalytics.discovery.model.DefaultTag;
import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.Tag;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.schema.PartitionField;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.integration.Diff;
import com.thinkbiganalytics.jobrepo.query.model.DefaultExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedStep;
import com.thinkbiganalytics.jobrepo.query.model.ExecutionStatus;
import com.thinkbiganalytics.jobrepo.query.model.ExitStatus;
import com.thinkbiganalytics.policy.rest.model.FieldPolicy;
import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JsonFeedIT extends FeedITBase{

    private static final Logger LOG = LoggerFactory.getLogger(JsonFeedIT.class);
    private static final String JSON_FILE_NAME = "books1.json";
    private static final int VALID_RESULTS = 7;

    @Test
    @Ignore //Ignore until missing JsonSerDe issue is fixed in Cloudera sandbox
    public void testJsonDataIngestFeed() throws Exception {
        dataIngestFeedBase();
    }

    @Test
    @Ignore //Ignore until missing JsonSerDe issue is fixed in Cloudera sandbox
    public void testJsonEditFeed() throws Exception {
        editFeed();
    }

    protected String getSamplesDir(){
        return SAMPLES_DIR + "/sample-data/json/";
    }

    protected String getFileName() {
        return JSON_FILE_NAME;
    }

    protected List<Field> getFields() {
        List<Field> fields = new ArrayList<>();
        fields.add(newField("id", "string"));
        fields.add(newField("cat", "string"));
        fields.add(newField("name", "string"));
        fields.add(newField("author", "string"));
        fields.add(newField("series_t", "string"));
        fields.add(newBigIntField("sequence_i"));
        fields.add(newField("genre_s", "string"));
        fields.add(newField("inStock", "boolean"));
        fields.add(newField("price", "double"));
        fields.add(newBigIntField("pages_i"));

        return fields;
    }

    protected List<FieldPolicy> getFieldPolicies() {
        List<FieldPolicy> policies = new ArrayList<>();
        policies.add(newPolicyBuilder("id").withValidation(notNull).withProfile().withIndex().toPolicy());
        policies.add(newPolicyBuilder("cat").withProfile().toPolicy());
        policies.add(newPolicyBuilder("name").withProfile().toPolicy());
        policies.add(newPolicyBuilder("author").withStandardisation(toUpperCase).withProfile().toPolicy());
        policies.add(newPolicyBuilder("series_t").withProfile().toPolicy());
        policies.add(newPolicyBuilder("sequence_i").withProfile().toPolicy());
        policies.add(newPolicyBuilder("genre_s").withProfile().toPolicy());
        policies.add(newPolicyBuilder("inStock").withProfile().toPolicy());
        policies.add(newPolicyBuilder("price").withProfile().toPolicy());
        policies.add(newPolicyBuilder("pages_i").withProfile().toPolicy());

        return policies;
    }

    protected Map<String, String> getTargetSourceFieldMap(){
        return new HashMap<>();
    }

    protected List<PartitionField> getPartitionFields() {
        return new ArrayList<>();
    }

    protected List<Tag> getTags() {
        List<Tag> tags = new ArrayList<>();
        tags.add(new DefaultTag("books"));
        return tags;
    }

    public void assertExecutedJobs(String feedName, String feedId) {
        LOG.info("Asserting there is 1 completed jobs: json ingest job");
        DefaultExecutedJob[] jobs = getJobs(0,50,null,null);

        //TODO assert all executed jobs are successful
        DefaultExecutedJob ingest = Arrays.stream(jobs).filter(job -> ("functional_tests." + feedName.toLowerCase()).equals(job.getFeedName())).findFirst().get();
        Assert.assertEquals(ExecutionStatus.COMPLETED, ingest.getStatus());
        Assert.assertEquals(ExitStatus.COMPLETED.getExitCode(), ingest.getExitCode());

        LOG.info("Asserting expected number of steps");
        DefaultExecutedJob job = getJobWithSteps(ingest.getExecutionId());
        Assert.assertEquals(ingest.getExecutionId(), job.getExecutionId());
        List<ExecutedStep> steps = job.getExecutedSteps();
        Assert.assertEquals(21, steps.size());
        for (ExecutedStep step : steps) {
            Assert.assertEquals(ExitStatus.COMPLETED.getExitCode(), step.getExitCode());
        }

        //TODO set skipHeader=false
        LOG.info("Asserting number of total/valid/invalid rows");
        Assert.assertEquals(7, getTotalNumberOfRecords(feedId));
        Assert.assertEquals(VALID_RESULTS, getNumberOfValidRecords(feedId));
        //TODO validate invalid records
        Assert.assertEquals(0, getNumberOfInvalidRecords(feedId));

//        assertValidatorsAndStandardisers(feedId, feedName);

        //TODO assert data via global search
        assertHiveData(feedName);
    }

    protected void assertValidatorsAndStandardisers(String feedId, String feedName) {

    }

    protected int getValidResults(){
        return VALID_RESULTS;
    }

    protected String getFileTemplateValue() {
        return "mydata\\d{1,3}.json";
    }

    protected String getFeedFormat() {
        return "ROW FORMAT SERDE 'org.apache.hive.hcatalog.data.JsonSerDe' STORED AS INPUTFORMAT " +
                "'org.apache.hadoop.mapred.TextInputFormat' OUTPUTFORMAT 'org.apache.hadoop.hive.ql.io.IgnoreKeyTextOutputFormat'";
    }

    protected boolean skipHeader(){
        return false;
    }

    protected void customizeFeed(FeedMetadata feed){
        feed.getOptions().setSkipHeader(skipHeader());
    }

    protected String getEditedFileName() {
        return "some-file.json";
    }

    protected void editFieldPolicies(List<FieldPolicy> policies) {
        FieldPolicy id = policies.get(0);
        id.setIndex(false); //flip indexing

        FieldPolicy cat = policies.get(1);
        cat.setProfile(false); //flip profiling

        FieldPolicy name = policies.get(2);
        name.getStandardization().add(toUpperCase); //add new standardiser
    }

    protected void assertEditChanges(ArrayNode diffs) {
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/properties/0/value", getEditedFileName())));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/schedule/schedulingPeriod", "20 sec")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("remove", "/description")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("add", "/tags/1")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/dataOwner", "Some Other Guy")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/table/fieldPolicies/0/index", "false")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/table/fieldPolicies/1/profile", "false")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("add", "/table/fieldPolicies/2/standardization/0")));
    }
}
