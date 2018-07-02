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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.CharMatcher;
import com.thinkbiganalytics.discovery.model.DefaultHiveSchema;
import com.thinkbiganalytics.discovery.model.DefaultTableSchema;
import com.thinkbiganalytics.discovery.model.DefaultTag;
import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.Tag;
import com.thinkbiganalytics.feedmgr.rest.model.EntityDifference;
import com.thinkbiganalytics.feedmgr.rest.model.EntityVersion;
import com.thinkbiganalytics.feedmgr.rest.model.EntityVersionDifference;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSchedule;
import com.thinkbiganalytics.feedmgr.rest.model.FeedVersions;
import com.thinkbiganalytics.feedmgr.rest.model.schema.FeedProcessingOptions;
import com.thinkbiganalytics.feedmgr.rest.model.schema.PartitionField;
import com.thinkbiganalytics.feedmgr.rest.model.schema.TableOptions;
import com.thinkbiganalytics.feedmgr.rest.model.schema.TableSetup;
import com.thinkbiganalytics.feedmgr.service.feed.importing.model.ImportFeed;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.integration.Diff;
import com.thinkbiganalytics.integration.IntegrationTestBase;
import com.thinkbiganalytics.jobrepo.query.model.DefaultExecutedJob;
import com.thinkbiganalytics.jobrepo.query.model.ExecutedStep;
import com.thinkbiganalytics.jobrepo.query.model.ExecutionStatus;
import com.thinkbiganalytics.jobrepo.query.model.ExitStatus;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.policy.rest.model.FieldPolicy;
import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;
import com.thinkbiganalytics.security.rest.model.User;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Basic Feed Integration Test which imports data index feed, creates category, imports data ingest template,
 * creates data ingest feed, runs the feed, validates number of executed jobs, validates validators and
 * standardisers have been applied by looking at profiler summary, validates total number and number of
 * valid and invalid rows, validates expected hive tables have been created and runs a simple hive
 * query and asserts the number of rows returned.
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class FeedITBase extends IntegrationTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(FeedITBase.class);

    protected static final String SAMPLES_DIR = "/samples";
    private static final String NIFI_FEED_SAMPLE_VERSION = "nifi-1.3";
    private static final String NIFI_TEMPLATE_SAMPLE_VERSION = "nifi-1.0";
    private static final String TEMPLATE_SAMPLES_DIR = SAMPLES_DIR + "/templates/" + NIFI_TEMPLATE_SAMPLE_VERSION + "/";
    private static final String FEED_SAMPLES_DIR = SAMPLES_DIR + "/feeds/" + NIFI_FEED_SAMPLE_VERSION + "/";
    protected static final String DATA_INGEST_ZIP = "data_ingest.zip";
    private static final String VAR_DROPZONE = "/var/dropzone";
    private static final int FEED_COMPLETION_WAIT_DELAY = 300;
    private static final String INDEX_TEXT_SERVICE_V2_FEED_ZIP = "index_text_service_v2.feed.zip";
    protected static String CATEGORY_NAME = "Functional Tests";

    private String sampleFeedsPath;
    protected String sampleTemplatesPath;
    private String usersDataPath;

    protected FieldStandardizationRule toUpperCase = new FieldStandardizationRule();
    protected FieldValidationRule email = new FieldValidationRule();
    protected FieldValidationRule lookup = new FieldValidationRule();
    protected FieldValidationRule notNull = new FieldValidationRule();
    protected FieldStandardizationRule ccMask = new FieldStandardizationRule();
    protected FieldStandardizationRule base64EncodeBinary = new FieldStandardizationRule();
    protected FieldStandardizationRule base64EncodeString = new FieldStandardizationRule();
    protected FieldStandardizationRule base64DecodeBinary = new FieldStandardizationRule();
    protected FieldStandardizationRule base64DecodeString = new FieldStandardizationRule();
    protected FieldValidationRule length = new FieldValidationRule();
    protected FieldValidationRule ipAddress = new FieldValidationRule();

    protected String createNewFeedName() {
        LocalDateTime now = LocalDateTime.now();
        String time = now.format(DateTimeFormatter.ofPattern("HH_mm_ss_SSS"));
        return "users_" + time;
    }

    protected void dataIngestFeedBase() throws Exception {
        prepare();

        copyDataToDropzone();

        //create new category
        FeedCategory category = createCategory(CATEGORY_NAME);

        ImportTemplate ingest = importDataIngestTemplate();

        //create standard ingest feed
        FeedMetadata feed = getCreateFeedRequest(category, ingest, createNewFeedName());
        customizeFeed(feed);
        FeedMetadata response = createFeed(feed).getFeedMetadata();
        Assert.assertEquals(feed.getFeedName(), response.getFeedName());

        waitForFeedToComplete();

        assertExecutedJobs(response.getFeedName(), response.getFeedId());

        failJobs(response.getCategoryAndFeedName());
        abandonAllJobs(response.getCategoryAndFeedName());
    }

    protected void editFeed() throws Exception {
        // Prepare environment
        prepare();

        final FeedCategory category = createCategory(CATEGORY_NAME);
        final ImportTemplate template = importDataIngestTemplate();

        // Create feed
        FeedMetadata feed = getCreateFeedRequest(category, template, createNewFeedName());
        feed.setDescription("Test feed");
        feed.setDataOwner("Some Guy");

        FeedMetadata response = createFeed(feed).getFeedMetadata();
        Assert.assertEquals(feed.getFeedName(), response.getFeedName());
        Assert.assertEquals(feed.getDataOwner(), response.getDataOwner());

        // Edit feed
        feed.setId(response.getId());
        feed.setFeedId(response.getFeedId());
        feed.setIsNew(false);
        feed.setDescription(null);
        feed.setDataOwner("Some Other Guy");
        NifiProperty fileFilter = feed.getProperties().get(0);
        fileFilter.setValue(getEditedFileName());

        List<FieldPolicy> policies = feed.getTable().getFieldPolicies();

        editFieldPolicies(policies);

        feed.getTable().setPrimaryKeyFields("id");
        feed.getOptions().setSkipHeader(false);
        feed.getTable().setTargetMergeStrategy("ROLLING_SYNC");
        feed.getTags().add(new DefaultTag("updated"));
        feed.getSchedule().setSchedulingPeriod("20 sec");

        response = createFeed(feed).getFeedMetadata();
        Assert.assertEquals(feed.getFeedName(), response.getFeedName());
        Assert.assertEquals(feed.getDescription(), response.getDescription());

        FeedVersions feedVersions = getVersions(feed.getFeedId());
        List<EntityVersion> versions = feedVersions.getVersions();
        Assert.assertEquals(2, versions.size());

        EntityVersionDifference entityDiff = getVersionDiff(feed.getFeedId(), versions.get(1).getId(), versions.get(0).getId());
        EntityDifference diff = entityDiff.getDifference();
        JsonNode patch = diff.getPatch();
        ArrayNode diffs = (ArrayNode) patch;
        assertEditChanges(diffs);
    }

    protected abstract void assertEditChanges(ArrayNode diffs);

    protected abstract void editFieldPolicies(List<FieldPolicy> policies);

    protected abstract String getEditedFileName();

    @Override
    public void startClean() {
        super.startClean();
    }

    protected void prepare() throws Exception {
        String path = getClass().getResource(".").toURI().getPath();
        String basedir = path.substring(0, path.indexOf("services"));
        sampleFeedsPath = basedir + FEED_SAMPLES_DIR;
        sampleTemplatesPath = basedir + TEMPLATE_SAMPLES_DIR;
        usersDataPath = basedir + getSamplesDir();

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

        ccMask.setName("Mask Last Four Digits");
        ccMask.setDisplayName("Mask Last Four Digits");
        ccMask.setDescription("Mask Last Four Digits");
        ccMask.setObjectClassType("com.thinkbiganalytics.policy.standardization.MaskLeavingLastFourDigitStandardizer");
        ccMask.setObjectShortClassType("MaskLeavingLastFourDigitStandardizer");
        ccMask.setProperties(newFieldRuleProperties(newFieldRuleProperty("Output", "maskLeavingLastFourDigitStandardizer", "STRING")));

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

        //import standard feedTemplate template
        ImportTemplate feedTemplate = importTemplate(templatePath);
        Assert.assertTrue(templatePath.contains(feedTemplate.getFileName()));
        Assert.assertTrue(feedTemplate.isSuccess());

        return feedTemplate;
    }

    protected void copyDataToDropzone() {
        LOG.info("Copying data to dropzone");

        //drop files in dropzone to run the feed
        //runCommandOnRemoteSystem(String.format("sudo chmod a+w %s", VAR_DROPZONE), IntegrationTestBase.APP_NIFI);
        copyFileLocalToRemote(usersDataPath + getFileName(), VAR_DROPZONE, IntegrationTestBase.APP_NIFI);
        runCommandOnRemoteSystem(String.format("chmod 777 %s/%s", VAR_DROPZONE, getFileName()), IntegrationTestBase.APP_NIFI);
    }

    protected void waitForFeedToComplete() {
        //wait for feed completion by waiting for certain amount of time and then
        waitFor(FEED_COMPLETION_WAIT_DELAY, TimeUnit.SECONDS, "for feed to complete");
    }

    protected void assertHiveData(String feedName) {
        assertHiveTables("functional_tests", feedName);
        getHiveSchema("functional_tests", feedName);
        List<HashMap<String, String>> rows = getHiveQuery("SELECT * FROM " + "functional_tests" + "." + feedName + " LIMIT 880");
        Assert.assertEquals(getValidResults(), rows.size());
    }

    protected void failJobs(String categoryAndFeedName) {
        LOG.info("Failing jobs");

        DefaultExecutedJob[] jobs = getJobs(0,50,"-startTime","jobInstance.feed.name%3D%3D" + categoryAndFeedName);
        Arrays.stream(jobs).map(this::failJob).forEach(job -> Assert.assertEquals(ExecutionStatus.FAILED, job.getStatus()));
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
        NifiProperty fileFilter = new NifiProperty("305363d8-015a-1000-0000-000000000000", "1f67e296-2ff8-4b5d-0000-000000000000", "File Filter", getFileName());
        fileFilter.setProcessGroupName("NiFi Flow");
        fileFilter.setProcessorName("Filesystem");
        fileFilter.setProcessorType("org.apache.nifi.processors.standard.GetFile");
        fileFilter.setTemplateValue(getFileTemplateValue());
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
        loadStrategy.setProcessGroupName("NiFi Flow");
        loadStrategy.setProcessorName("GetTableData");
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
        schema.setFields(getFields());

        table.setTableSchema(schema);
        table.setSourceTableSchema(schema);
        table.setFeedTableSchema(schema);
        table.setTargetMergeStrategy("DEDUPE_AND_MERGE");
        table.setFeedFormat(getFeedFormat());
        table.setTargetFormat("STORED AS ORC");

        List<FieldPolicy> policies = getFieldPolicies();
        table.setFieldPolicies(policies);

        table.setTargetSourceFieldMap(getTargetSourceFieldMap());

        List<PartitionField> partitions = getPartitionFields();
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

        List<Tag> tags = getTags();
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

    public abstract void assertExecutedJobs(String feedName, String feedId) throws IOException;

    protected abstract List<Tag> getTags();

    protected abstract List<PartitionField> getPartitionFields();

    protected abstract List<FieldPolicy> getFieldPolicies();

    protected abstract List<Field> getFields();

    protected abstract String getSamplesDir();

    protected abstract String getFileName();

    protected abstract String getFileTemplateValue();

    protected abstract String getFeedFormat();

    protected abstract int getValidResults();

    protected abstract Map<String, String> getTargetSourceFieldMap();

    protected abstract void assertValidatorsAndStandardisers(String feedId, String feedName);

    protected abstract boolean skipHeader();

    protected abstract void customizeFeed(FeedMetadata feed);

}
