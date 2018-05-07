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
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CsvFeedIT extends FeedITBase {

    private static final Logger LOG = LoggerFactory.getLogger(CsvFeedIT.class);
    private static final String CSV_FILE_NAME = "userdata1.csv";
    private static final int VALID_RESULTS = 879;

    @Test
    public void testCsvDataIngestFeed() throws Exception {
        dataIngestFeedBase();
    }

    @Test
    public void testCsvEditFeed() throws Exception {
        editFeed();
    }

    protected String getSamplesDir(){
        return SAMPLES_DIR + "/sample-data/csv/";
    }

    protected String getFileName() {
        return CSV_FILE_NAME;
    }

    protected List<Field> getFields() {
        List<Field> fields = new ArrayList<>();
        fields.add(newTimestampField("registration_dttm"));
        fields.add(newBigIntField("id"));
        fields.add(newField("first_name", "string"));
        fields.add(newField("second_name", "string"));
        fields.add(newField("email", "string"));
        fields.add(newField("gender", "string"));
        fields.add(newField("ip_address", "string"));
        fields.add(newBinaryField("cc"));
        fields.add(newField("country", "string"));
        fields.add(newField("birthdate", "string"));
        fields.add(newField("salary", "string"));

        return fields;
    }

    protected List<FieldPolicy> getFieldPolicies() {
        List<FieldPolicy> policies = new ArrayList<>();
        policies.add(newPolicyBuilder("registration_dttm").toPolicy());
        policies.add(newPolicyBuilder("id").toPolicy());
        policies.add(newPolicyBuilder("first_name").withStandardisation(toUpperCase).withProfile().withIndex().toPolicy());
        policies.add(newPolicyBuilder("second_name").withProfile().withIndex().toPolicy());
        policies.add(newPolicyBuilder("email").withValidation(email).toPolicy());
        policies.add(newPolicyBuilder("gender").withValidation(lookup, notNull).toPolicy());
        policies.add(newPolicyBuilder("ip_address").withValidation(ipAddress).toPolicy());
        policies.add(newPolicyBuilder("cc").withStandardisation(base64EncodeBinary).withProfile().toPolicy());
        policies.add(newPolicyBuilder("country").withStandardisation(base64EncodeBinary, base64DecodeBinary, base64EncodeString, base64DecodeString).withValidation(notNull, length).withProfile().toPolicy());
        policies.add(newPolicyBuilder("birthdate").toPolicy());
        policies.add(newPolicyBuilder("salary").toPolicy());

        return policies;
    }

    protected List<PartitionField> getPartitionFields() {
        List<PartitionField> partitions = new ArrayList<>();
        partitions.add(byYear("registration_dttm"));
        return partitions;
    }

    protected List<Tag> getTags() {
        List<Tag> tags = new ArrayList<>();
        tags.add(new DefaultTag("users"));
        tags.add(new DefaultTag("registrations"));
        return tags;
    }

    public void assertExecutedJobs(String feedName, String feedId) throws IOException {
        LOG.info("Asserting there are 2 completed jobs: userdata ingest job, index text service system jobs");
        DefaultExecutedJob[] jobs = getJobs(0,50,null,null);

        //TODO assert all executed jobs are successful
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
        Assert.assertEquals(121, getNumberOfInvalidRecords(feedId));

        assertValidatorsAndStandardisers(feedId, feedName);

        //TODO assert data via global search
        assertHiveData(feedName);
    }

    protected void assertValidatorsAndStandardisers(String feedId, String feedName) {
        LOG.info("Asserting Validators and Standardisers");

        String processingDttm = getProcessingDttm(feedId);

        assertNamesAreInUppercase(feedId, processingDttm);
        assertMultipleBase64Encodings(feedId, processingDttm);
        assertBinaryColumnData(feedName);

        assertValidatorResults(feedId, processingDttm, "LengthValidator", 47);
        assertValidatorResults(feedId, processingDttm, "NotNullValidator", 67);
        assertValidatorResults(feedId, processingDttm, "EmailValidator", 3);
        assertValidatorResults(feedId, processingDttm, "LookupValidator", 4);
        assertValidatorResults(feedId, processingDttm, "IPAddressValidator", 4);
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

    private void assertBinaryColumnData(String feedName) {
        LOG.info("Asserting binary CC column data");
        DefaultHiveSchema schema = getHiveSchema("functional_tests", feedName);
        Field ccField = schema.getFields().stream().filter(field -> field.getName().equals("cc")).iterator().next();
        Assert.assertEquals("binary", ccField.getDerivedDataType());

        List<HashMap<String, String>> rows = getHiveQuery("SELECT cc FROM " + "functional_tests" + "." + feedName + " where id = 1");
        Assert.assertEquals(1, rows.size());
        HashMap<String, String> row = rows.get(0);

        // where TmpjMU9UVXlNVGcyTkRreU1ERXhOZz09 is double Base64 encoding for cc field of the first row (6759521864920116),
        // one base64 encoding by our standardiser and second base64 encoding by spring framework for returning binary data
        Assert.assertEquals("TmpjMU9UVXlNVGcyTkRreU1ERXhOZz09", row.get("cc"));
    }

    protected int getValidResults(){
        return VALID_RESULTS;
    }

    protected Map<String, String> getTargetSourceFieldMap(){
        return null;
    }

    protected String getFileTemplateValue() {
        return "mydata\\d{1,3}.csv";
    }

    protected String getFeedFormat() {
        return "ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'\n WITH SERDEPROPERTIES " +
                "( 'separatorChar' = ',' ,'escapeChar' = '\\\\' ,'quoteChar' = '\\'') STORED AS TEXTFILE";
    }

    protected boolean skipHeader(){
        return true;
    }

    protected void customizeFeed(FeedMetadata feed){
        importSystemFeeds();
    }

    protected String getEditedFileName() {
        return "some-file.csv";
    }

    protected void editFieldPolicies(List<FieldPolicy> policies) {
        FieldPolicy id = policies.get(1);
        id.getValidation().add(notNull); //add new validator

        FieldPolicy firstName = policies.get(2);
        firstName.setProfile(false); //flip profiling

        FieldPolicy secondName = policies.get(3);
        secondName.setIndex(false); //flip indexing
        secondName.getStandardization().add(toUpperCase); //add new standardiser

        FieldPolicy email = policies.get(4);
        email.setValidation(Collections.emptyList()); //remove validators

        FieldPolicy gender = policies.get(5);
        FieldValidationRule lookup = gender.getValidation().get(0);
        lookup.getProperties().get(0).setValue("new value"); //change existing validator property
        gender.setProfile(true); //add profiling
        gender.setIndex(true); //add indexing

        FieldPolicy creditCard = policies.get(7);
        FieldStandardizationRule base64EncodeBinary = creditCard.getStandardization().get(0);
        base64EncodeBinary.getProperties().get(0).setValue("STRING"); //change existing standardiser property
    }

    protected void assertEditChanges(ArrayNode diffs) {
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/properties/0/value", getEditedFileName())));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/schedule/schedulingPeriod", "20 sec")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("remove", "/description")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("add", "/tags/1")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/dataOwner", "Some Other Guy")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("add", "/table/fieldPolicies/1/validation/0")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/table/fieldPolicies/2/profile", "false")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/table/fieldPolicies/3/index", "false")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("add", "/table/fieldPolicies/3/standardization/0")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("remove", "/table/fieldPolicies/4/validation/0")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/table/fieldPolicies/5/profile", "true")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/table/fieldPolicies/5/index", "true")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/table/fieldPolicies/5/validation/0/properties/0/value", "new value")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/table/fieldPolicies/7/standardization/0/properties/0/value", "STRING")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/table/fieldPolicies/8/standardization/0/properties/0/value", "STRING")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/table/targetMergeStrategy", "ROLLING_SYNC")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/table/fieldIndexString", "first_name,gender")));
        Assert.assertTrue(versionPatchContains(diffs, new Diff("replace", "/options/skipHeader", "false")));
    }

}
