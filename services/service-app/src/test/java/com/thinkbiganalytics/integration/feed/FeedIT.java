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
import com.thinkbiganalytics.discovery.model.DefaultTableSchema;
import com.thinkbiganalytics.discovery.model.DefaultTag;
import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.Tag;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSchedule;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.schema.FeedProcessingOptions;
import com.thinkbiganalytics.feedmgr.rest.model.schema.PartitionField;
import com.thinkbiganalytics.feedmgr.rest.model.schema.TableOptions;
import com.thinkbiganalytics.feedmgr.rest.model.schema.TableSetup;
import com.thinkbiganalytics.feedmgr.service.feed.importing.model.ImportFeed;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.integration.IntegrationTestBase;
import com.thinkbiganalytics.jobrepo.query.model.DefaultExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.DefaultExecutedStep;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedStep;
import com.thinkbiganalytics.jobrepo.query.model.ExecutionStatus;
import com.thinkbiganalytics.jobrepo.query.model.ExitStatus;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.policy.rest.model.FieldPolicy;
import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;
import com.thinkbiganalytics.security.rest.model.User;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private static final String SAMPLES_DIR = "/samples";
    private static final String DATA_SAMPLES_DIR = SAMPLES_DIR + "/sample-data/csv/";
    private static final String NIFI_FEED_SAMPLE_VERSION = "nifi-1.3";
    private static final String NIFI_TEMPLATE_SAMPLE_VERSION = "nifi-1.0";
    private static final String TEMPLATE_SAMPLES_DIR = SAMPLES_DIR + "/templates/" + NIFI_TEMPLATE_SAMPLE_VERSION + "/";
    private static final String FEED_SAMPLES_DIR = SAMPLES_DIR + "/feeds/" + NIFI_FEED_SAMPLE_VERSION + "/";
    protected static final String DATA_INGEST_ZIP = "data_ingest.zip";
    private static final String VAR_DROPZONE = "/var/dropzone";
    private static final String USERDATA1_CSV = "userdata1.csv";
    private static final int FEED_COMPLETION_WAIT_DELAY = 180;
    private static final int VALID_RESULTS = 879;
    private static final String INDEX_TEXT_SERVICE_V2_FEED_ZIP = "index_text_service_v2.feed.zip";
    private static String FEED_NAME = "users_" + System.currentTimeMillis();
    private static String CATEGORY_NAME = "Functional Tests";

    private String sampleFeedsPath;
    protected String sampleTemplatesPath;
    private String usersDataPath;

    private FieldStandardizationRule toUpperCase = new FieldStandardizationRule();
    private FieldValidationRule email = new FieldValidationRule();
    private FieldValidationRule lookup = new FieldValidationRule();
    private FieldValidationRule notNull = new FieldValidationRule();
    private FieldStandardizationRule base64EncodeBinary = new FieldStandardizationRule();
    private FieldStandardizationRule base64EncodeString = new FieldStandardizationRule();
    private FieldStandardizationRule base64DecodeBinary = new FieldStandardizationRule();
    private FieldStandardizationRule base64DecodeString = new FieldStandardizationRule();
    private FieldValidationRule length = new FieldValidationRule();
    private FieldValidationRule ipAddress = new FieldValidationRule();

    @Override
    protected void configureObjectMapper(ObjectMapper om) {
        SimpleModule m = new SimpleModule();
        m.addAbstractTypeMapping(ExecutedStep.class, DefaultExecutedStep.class);
        om.registerModule(m);
    }

    @Test
    public void testDataIngestFeed() throws Exception {
        prepare();

        importSystemFeeds();

        copyDataToDropzone();

        //create new category
        FeedCategory category = createCategory(CATEGORY_NAME);

        ImportTemplate ingest = importDataIngestTemplate();

        //create standard ingest feed
        FeedMetadata feed = getCreateFeedRequest(category, ingest, FEED_NAME);
        FeedMetadata response = createFeed(feed).getFeedMetadata();
        Assert.assertEquals(feed.getFeedName(), response.getFeedName());

        waitForFeedToComplete();

        assertExecutedJobs(response);

        failJobs(response.getCategoryAndFeedName());
        abandonAllJobs(response.getCategoryAndFeedName());
    }

    @Test
    public void testEditFeed() throws Exception {
        // Prepare environment
        prepare();

        final FeedCategory category = createCategory(CATEGORY_NAME);
        final ImportTemplate template = importDataIngestTemplate();

        // Create feed
        FeedMetadata feed = getCreateFeedRequest(category, template, FEED_NAME);
        feed.setDescription("Test feed");

        FeedMetadata response = createFeed(feed).getFeedMetadata();
        Assert.assertEquals(feed.getFeedName(), response.getFeedName());

        // Edit feed
        feed = response;
        feed.setDescription(null);

        response = createFeed(feed).getFeedMetadata();
        Assert.assertEquals(feed.getFeedName(), response.getFeedName());
        Assert.assertEquals(feed.getDescription(), response.getDescription());
    }

    @Override
    public void startClean() {
        super.startClean();
    }

//    @Test
    public void temp() {
//        failJobs("system.index_text_service");
    }

    protected void prepare() throws Exception {
        String path = getClass().getResource(".").toURI().getPath();
        String basedir = path.substring(0, path.indexOf("services"));
        sampleFeedsPath = basedir + FEED_SAMPLES_DIR;
        sampleTemplatesPath = basedir + TEMPLATE_SAMPLES_DIR;
        usersDataPath = basedir + DATA_SAMPLES_DIR;

        toUpperCase.setName("Uppercase");
        toUpperCase.setDisplayName("Uppercase");
        toUpperCase.setDescription("Convert string to uppercase");
        toUpperCase.setObjectClassType("com.thinkbiganalytics.policy.standardization.UppercaseStandardizer");
        toUpperCase.setObjectShortClassType("UppercaseStandardizer");

        email.setName("email");
        email.setDisplayName("Email");
        email.setDescription("Valid email address");
        email.setObjectClassType("com.thinkbiganalytics.policy.validation.EmailValidator");
        email.setObjectShortClassType("EmailValidator");

        ipAddress.setName("IP Address");
        ipAddress.setDisplayName("IP Address");
        ipAddress.setDescription("Valid IP address");
        ipAddress.setObjectClassType("com.thinkbiganalytics.policy.validation.IPAddressValidator");
        ipAddress.setObjectShortClassType("IPAddressValidator");

        lookup.setName("lookup");
        lookup.setDisplayName("Lookup");
        lookup.setDescription("Must be contained in the list");
        lookup.setObjectClassType("com.thinkbiganalytics.policy.validation.LookupValidator");
        lookup.setObjectShortClassType("LookupValidator");
        lookup.setProperties(newFieldRuleProperties(newFieldRuleProperty("List", "lookupList", "Male,Female")));

        base64DecodeBinary.setName("Base64 Decode");
        base64DecodeBinary.setDisplayName("Base64 Decode");
        base64DecodeBinary.setDescription("Base64 decode a string or a byte[].  Strings are evaluated using the UTF-8 charset");
        base64DecodeBinary.setObjectClassType("com.thinkbiganalytics.policy.standardization.Base64Decode");
        base64DecodeBinary.setObjectShortClassType("Base64Decode");
        base64DecodeBinary.setProperties(newFieldRuleProperties(newFieldRuleProperty("Output", "base64Output", "BINARY")));

        base64DecodeString.setName("Base64 Decode");
        base64DecodeString.setDisplayName("Base64 Decode");
        base64DecodeString.setDescription("Base64 decode a string or a byte[].  Strings are evaluated using the UTF-8 charset");
        base64DecodeString.setObjectClassType("com.thinkbiganalytics.policy.standardization.Base64Decode");
        base64DecodeString.setObjectShortClassType("Base64Decode");
        base64DecodeString.setProperties(newFieldRuleProperties(newFieldRuleProperty("Output", "base64Output", "STRING")));

        base64EncodeBinary.setName("Base64 Encode");
        base64EncodeBinary.setDisplayName("Base64 Encode");
        base64EncodeBinary.setDescription("Base64 encode a string or a byte[].  Strings are evaluated using the UTF-8 charset.  String output is urlsafe");
        base64EncodeBinary.setObjectClassType("com.thinkbiganalytics.policy.standardization.Base64Encode");
        base64EncodeBinary.setObjectShortClassType("Base64Encode");
        base64EncodeBinary.setProperties(newFieldRuleProperties(newFieldRuleProperty("Output", "base64Output", "BINARY")));

        base64EncodeString.setName("Base64 Encode");
        base64EncodeString.setDisplayName("Base64 Encode");
        base64EncodeString.setDescription("Base64 encode a string or a byte[].  Strings are evaluated using the UTF-8 charset.  String output is urlsafe");
        base64EncodeString.setObjectClassType("com.thinkbiganalytics.policy.standardization.Base64Encode");
        base64EncodeString.setObjectShortClassType("Base64Encode");
        base64EncodeString.setProperties(newFieldRuleProperties(newFieldRuleProperty("Output", "base64Output", "STRING")));

        notNull.setName("Not Null");
        notNull.setDisplayName("Not Null");
        notNull.setDescription("Validate a value is not null");
        notNull.setObjectClassType("com.thinkbiganalytics.policy.validation.NotNullValidator");
        notNull.setObjectShortClassType("NotNullValidator");
        notNull.setProperties(newFieldRuleProperties(newFieldRuleProperty("EMPTY_STRING", "allowEmptyString", "false"),
                                                     newFieldRuleProperty("TRIM_STRING", "trimString", "true")));

        length.setName("Length");
        length.setDisplayName("Length");
        length.setDescription("Validate String falls between desired length");
        length.setObjectClassType("com.thinkbiganalytics.policy.validation.LengthValidator");
        length.setObjectShortClassType("LengthValidator");
        length.setProperties(newFieldRuleProperties(newFieldRuleProperty("Max Length", "maxLength", "15"),
                                                    newFieldRuleProperty("Min Length", "minLength", "5")));
    }


    protected void importSystemFeeds() {
        ImportFeed textIndex = importFeed(sampleFeedsPath + INDEX_TEXT_SERVICE_V2_FEED_ZIP);
        enableFeed(textIndex.getNifiFeed().getFeedMetadata().getFeedId());
    }

    protected ImportTemplate importDataIngestTemplate() {
        return importFeedTemplate(sampleTemplatesPath + DATA_INGEST_ZIP);
    }

    protected ImportTemplate importFeedTemplate(String templatePath) {
        LOG.info("Importing feed template {}", templatePath);

        //get number of templates already there
        int existingTemplateNum = getTemplates().length;

        //import standard feedTemplate template
        ImportTemplate feedTemplate = importTemplate(templatePath);
        Assert.assertTrue(templatePath.contains(feedTemplate.getFileName()));
        Assert.assertTrue(feedTemplate.isSuccess());

        //assert new template is there
        RegisteredTemplate[] templates = getTemplates();
        Assert.assertTrue(templates.length == existingTemplateNum + 1);
        return feedTemplate;
    }

    protected void copyDataToDropzone() {
        LOG.info("Copying data to dropzone");

        //drop files in dropzone to run the feed
        ssh(String.format("sudo chmod a+w %s", VAR_DROPZONE));
        scp(usersDataPath + USERDATA1_CSV, VAR_DROPZONE);
        ssh(String.format("sudo chown -R nifi:nifi %s", VAR_DROPZONE));
    }


    protected void waitForFeedToComplete() {
        //wait for feed completion by waiting for certain amount of time and then
        waitFor(FEED_COMPLETION_WAIT_DELAY, TimeUnit.SECONDS, "for feed to complete");
    }

    protected void failJobs(String categoryAndFeedName) {
        LOG.info("Failing jobs");

        DefaultExecutedJob[] jobs = getJobs("jobInstance.feed.name%3D%3D" + categoryAndFeedName + "&limit=5&sort=-startTime&start=0");
        Arrays.stream(jobs).map(this::failJob).forEach(job -> Assert.assertEquals(ExecutionStatus.FAILED, job.getStatus()));
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

    protected FeedMetadata getCreateFeedRequest(FeedCategory category, ImportTemplate template, String name) throws Exception {
        FeedMetadata feed = new FeedMetadata();
        feed.setFeedName(name);
        feed.setSystemFeedName(name.toLowerCase());
        feed.setCategory(category);
        feed.setTemplateId(template.getTemplateId());
        feed.setTemplateName(template.getTemplateName());
        feed.setDescription("Created by functional test");
        feed.setInputProcessorType("org.apache.nifi.processors.standard.GetFile");

        List<NifiProperty> properties = new ArrayList<>();
        NifiProperty fileFilter = new NifiProperty("305363d8-015a-1000-0000-000000000000", "1f67e296-2ff8-4b5d-0000-000000000000", "File Filter", USERDATA1_CSV);
        fileFilter.setProcessGroupName("NiFi Flow");
        fileFilter.setProcessorName("Filesystem");
        fileFilter.setProcessorType("org.apache.nifi.processors.standard.GetFile");
        fileFilter.setTemplateValue("mydata\\d{1,3}.csv");
        fileFilter.setInputProperty(true);
        fileFilter.setUserEditable(true);
        properties.add(fileFilter);

        NifiProperty inputDir = new NifiProperty("305363d8-015a-1000-0000-000000000000", "1f67e296-2ff8-4b5d-0000-000000000000", "Input Directory", VAR_DROPZONE);
        inputDir.setProcessGroupName("NiFi Flow");
        inputDir.setProcessorName("Filesystem");
        inputDir.setProcessorType("org.apache.nifi.processors.standard.GetFile");
        inputDir.setInputProperty(true);
        inputDir.setUserEditable(true);
        properties.add(inputDir);

        NifiProperty loadStrategy = new NifiProperty("305363d8-015a-1000-0000-000000000000", "6aeabec7-ec36-4ed5-0000-000000000000", "Load Strategy", "FULL_LOAD");
        loadStrategy.setProcessorType("com.thinkbiganalytics.nifi.v2.ingest.GetTableData");
        properties.add(loadStrategy);

        feed.setProperties(properties);

        FeedSchedule schedule = new FeedSchedule();
        schedule.setConcurrentTasks(1);
        schedule.setSchedulingPeriod("15 sec");
        schedule.setSchedulingStrategy("TIMER_DRIVEN");
        feed.setSchedule(schedule);

        TableSetup table = new TableSetup();
        DefaultTableSchema schema = new DefaultTableSchema();
        schema.setName("test1");
        List<Field> fields = new ArrayList<>();
        fields.add(newTimestampField("registration_dttm"));
        fields.add(newBigIntField("id"));
        fields.add(newStringField("first_name"));
        fields.add(newStringField("second_name"));
        fields.add(newStringField("email"));
        fields.add(newStringField("gender"));
        fields.add(newStringField("ip_address"));
        fields.add(newBinaryField("cc"));
        fields.add(newStringField("country"));
        fields.add(newStringField("birthdate"));
        fields.add(newStringField("salary"));
        schema.setFields(fields);

        table.setTableSchema(schema);
        table.setSourceTableSchema(schema);
        table.setFeedTableSchema(schema);
        table.setTargetMergeStrategy("DEDUPE_AND_MERGE");
        table.setFeedFormat(
            "ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'\n WITH SERDEPROPERTIES ( 'separatorChar' = ',' ,'escapeChar' = '\\\\' ,'quoteChar' = '\\'') STORED AS TEXTFILE");
        table.setTargetFormat("STORED AS ORC");

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
        table.setFieldPolicies(policies);

        List<PartitionField> partitions = new ArrayList<>();
        partitions.add(byYear("registration_dttm"));
        table.setPartitions(partitions);

        TableOptions options = new TableOptions();
        options.setCompressionFormat("SNAPPY");
        options.setAuditLogging(true);
        table.setOptions(options);

        table.setTableType("SNAPSHOT");
        feed.setTable(table);
        feed.setOptions(new FeedProcessingOptions());
        feed.getOptions().setSkipHeader(true);

        feed.setDataOwner("Marketing");

        List<Tag> tags = new ArrayList<>();
        tags.add(new DefaultTag("users"));
        tags.add(new DefaultTag("registrations"));
        feed.setTags(tags);

        User owner = new User();
        owner.setSystemName("dladmin");
        owner.setDisplayName("Data Lake Admin");
        Set<String> groups = new HashSet<>();
        groups.add("admin");
        groups.add("user");
        owner.setGroups(groups);
        feed.setOwner(owner);

        return feed;
    }

}
