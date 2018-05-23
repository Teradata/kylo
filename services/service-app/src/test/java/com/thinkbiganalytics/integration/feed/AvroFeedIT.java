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
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.schema.PartitionField;
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
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AvroFeedIT extends FeedITBase {

    private static final Logger LOG = LoggerFactory.getLogger(AvroFeedIT.class);
    private static final String AVRO_FILE_NAME = "userdata1.avro";
    private static final int VALID_RESULTS = 656;

    @Test
    public void testAvroCreateFeed() throws Exception {
        dataIngestFeedBase();
    }

    @Test
    public void testAvroEditFeed() throws Exception {
        editFeed();
    }

    protected String getSamplesDir(){
        return SAMPLES_DIR + "/sample-data/avro/";
    }

    protected String getFileName() {
        return AVRO_FILE_NAME;
    }

    protected List<Field> getFields() {
        List<Field> fields = new ArrayList<>();
        fields.add(newField("registration_dttm", "string"));
        fields.add(newBigIntField("id"));
        fields.add(newField("first_name", "string"));
        fields.add(newField("second_name", "string"));
        fields.add(newField("email", "string"));
        fields.add(newField("gender", "string"));
        fields.add(newField("ip_address", "string"));
        fields.add(newBigIntField("cc"));
        fields.add(newField("country", "string"));
        fields.add(newField("birthdate", "string"));
        fields.add(newField("salary", "double"));
        fields.add(newField("title", "string"));
        fields.add(newField("comments", "string"));

        return fields;
    }

    protected List<FieldPolicy> getFieldPolicies() {
        List<FieldPolicy> policies = new ArrayList<>();
        policies.add(newPolicyBuilder("registration_dttm").toPolicy());
        policies.add(newPolicyBuilder("id").toPolicy());
        policies.add(newPolicyBuilder("first_name").withStandardisation(toUpperCase).withProfile().withIndex().toPolicy());
        policies.add(newPolicyBuilder("second_name").withStandardisation(toUpperCase).withProfile().withIndex().toPolicy());
        policies.add(newPolicyBuilder("email").withValidation(email).toPolicy());
        policies.add(newPolicyBuilder("gender").withValidation(notNull).toPolicy());
        policies.add(newPolicyBuilder("ip_address").withValidation(ipAddress).toPolicy());
        policies.add(newPolicyBuilder("cc").withValidation(notNull).withProfile().toPolicy());
        policies.add(newPolicyBuilder("country").toPolicy());
        policies.add(newPolicyBuilder("birthdate").toPolicy());
        policies.add(newPolicyBuilder("salary").toPolicy());
        policies.add(newPolicyBuilder("title").toPolicy());
        policies.add(newPolicyBuilder("comments").toPolicy());

        return policies;
    }

    protected List<PartitionField> getPartitionFields() {
        List<PartitionField> partitions = new ArrayList<>();
        return partitions;
    }

    protected List<Tag> getTags() {
        List<Tag> tags = new ArrayList<>();
        tags.add(new DefaultTag("users"));
        tags.add(new DefaultTag("registrations"));
        return tags;
    }

    public void assertExecutedJobs(String feedName, String feedId) throws IOException {
        LOG.info("Asserting Avro userdata ingest job is complete");
        DefaultExecutedJob[] jobs = getJobs(0,50,null,null);

        DefaultExecutedJob ingest = Arrays.stream(jobs).filter(job -> ("functional_tests." + feedName.toLowerCase()).equals(job.getFeedName())).findFirst().get();
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

        LOG.info("Asserting number of total/valid/invalid rows");
        Assert.assertEquals(1000, getTotalNumberOfRecords(feedId));
        Assert.assertEquals(VALID_RESULTS, getNumberOfValidRecords(feedId));
        Assert.assertEquals(344, getNumberOfInvalidRecords(feedId));

        assertValidatorsAndStandardisers(feedId, feedName);

        //TODO assert data via global search
        assertHiveData(feedName);
    }

    protected void assertValidatorsAndStandardisers(String feedId, String feedName) {
        LOG.info("Asserting Validators and Standardisers");

        String processingDttm = getProcessingDttm(feedId);

        assertNamesAreInUppercase(feedId, processingDttm);
    }

    private void assertNamesAreInUppercase(String feedId, String processingDttm) {
        LOG.info("Asserting all names are in upper case");
        String topN = getProfileStatsForColumn(feedId, processingDttm, "TOP_N_VALUES", "first_name");
        Assert.assertTrue(CharMatcher.JAVA_LOWER_CASE.matchesNoneOf(topN));
    }

    protected int getValidResults(){
        return VALID_RESULTS;
    }

    protected Map<String, String> getTargetSourceFieldMap(){
        return null;
    }

    protected String getFileTemplateValue() {
        return "mydata\\d{1,3}.avro";
    }

    protected String getFeedFormat() {
        return "STORED AS AVRO";
    }

    protected boolean skipHeader(){
        return false;
    }

    protected void customizeFeed(FeedMetadata feed){
        feed.getOptions().setSkipHeader(skipHeader());
    }

    protected String getEditedFileName() {
        return "some-file.avro";
    }

    protected void editFieldPolicies(List<FieldPolicy> policies) {
        FieldPolicy id = policies.get(1);
        id.getValidation().add(notNull); //add new validator

        FieldPolicy firstName = policies.get(2);
        firstName.setProfile(false); //flip profiling

        FieldPolicy gender = policies.get(5);
        FieldValidationRule lookup = gender.getValidation().get(0);
        lookup.getProperties().get(0).setValue("new value"); //change existing validator property
        gender.setProfile(true); //add profiling
        gender.setIndex(true); //add indexing
    }

    protected void assertEditChanges(ArrayNode diffs) {
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/properties/0/value", getEditedFileName())));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/schedule/schedulingPeriod", "20 sec")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("remove", "/description")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("add", "/tags/1")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/dataOwner", "Some Other Guy")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("add", "/table/fieldPolicies/1/validation/0")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/table/fieldPolicies/2/profile", "false")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/table/fieldPolicies/5/profile", "true")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/table/fieldPolicies/5/index", "true")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/table/fieldPolicies/5/validation/0/properties/0/value", "new value")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/table/targetMergeStrategy", "ROLLING_SYNC")));
    }

}
