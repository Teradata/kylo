package com.thinkbiganalytics.integration;

/*-
 * #%L
 * kylo-commons-test
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.util.concurrent.Uninterruptibles;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.internal.mapping.Jackson2Mapper;
import com.jayway.restassured.mapper.factory.Jackson2ObjectMapperFactory;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.thinkbiganalytics.discovery.model.DefaultDataTypeDescriptor;
import com.thinkbiganalytics.discovery.model.DefaultField;
import com.thinkbiganalytics.discovery.model.DefaultTableSchema;
import com.thinkbiganalytics.discovery.model.DefaultTag;
import com.thinkbiganalytics.discovery.schema.Field;
import com.thinkbiganalytics.discovery.schema.Tag;
import com.thinkbiganalytics.feedmgr.rest.controller.AdminController;
import com.thinkbiganalytics.feedmgr.rest.controller.FeedCategoryRestController;
import com.thinkbiganalytics.feedmgr.rest.controller.FeedRestController;
import com.thinkbiganalytics.feedmgr.rest.controller.NifiIntegrationRestController;
import com.thinkbiganalytics.feedmgr.rest.controller.TemplatesRestController;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSchedule;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.schema.FeedProcessingOptions;
import com.thinkbiganalytics.feedmgr.rest.model.schema.PartitionField;
import com.thinkbiganalytics.feedmgr.rest.model.schema.TableOptions;
import com.thinkbiganalytics.feedmgr.rest.model.schema.TableSetup;
import com.thinkbiganalytics.feedmgr.service.feed.ExportImportFeedService;
import com.thinkbiganalytics.feedmgr.service.template.ExportImportTemplateService;
import com.thinkbiganalytics.hive.rest.controller.HiveRestController;
import com.thinkbiganalytics.jobrepo.query.model.DefaultExecutedJob;
import com.thinkbiganalytics.jobrepo.rest.controller.JobsRestController;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.policy.rest.model.FieldPolicy;
import com.thinkbiganalytics.policy.rest.model.FieldStandardizationRule;
import com.thinkbiganalytics.policy.rest.model.FieldValidationRule;
import com.thinkbiganalytics.rest.model.search.SearchResult;
import com.thinkbiganalytics.rest.model.search.SearchResultImpl;
import com.thinkbiganalytics.security.rest.controller.AccessControlController;
import com.thinkbiganalytics.security.rest.model.ActionGroup;
import com.thinkbiganalytics.security.rest.model.PermissionsChange;
import com.thinkbiganalytics.security.rest.model.RoleMembershipChange;
import com.thinkbiganalytics.security.rest.model.User;

import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.web.api.dto.PortDTO;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.optional.ssh.SSHBase;
import org.apache.tools.ant.taskdefs.optional.ssh.SSHExec;
import org.apache.tools.ant.taskdefs.optional.ssh.Scp;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Superclass for all functional tests.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {IntegrationTestConfig.class})
public class IntegrationTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestBase.class);

    protected static final String SAMPLES_DIR = "/samples";
    protected static final String DATA_SAMPLES_DIR = SAMPLES_DIR + "/sample-data/csv/";
    protected static final String TEMPLATE_SAMPLES_DIR = SAMPLES_DIR + "/templates/nifi-1.0/";
    protected static final String FEED_SAMPLES_DIR = SAMPLES_DIR + "/feeds/nifi-1.0/";
    private static final int PROCESSOR_STOP_WAIT_DELAY = 10;
    protected static final String DATA_INGEST_ZIP = "data_ingest.zip";
    private static final String VAR_DROPZONE = "/var/dropzone";
    private static final String USERDATA1_CSV = "userdata1.csv";


    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject
    private KyloConfig kyloConfig;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject
    private SshConfig sshConfig;

    private String feedsPath;
    private String templatesPath;
    private String usersDataPath;

    private FieldStandardizationRule toUpperCase = new FieldStandardizationRule();
    private FieldValidationRule email = new FieldValidationRule();

    protected void runAs(UserContext.User user) {
        UserContext.setUser(user);
    }

    @Before
    public void setupRestAssured() throws URISyntaxException {
        UserContext.setUser(UserContext.User.ADMIN);

        RestAssured.baseURI = kyloConfig.getProtocol() + kyloConfig.getHost();
        RestAssured.port = kyloConfig.getPort();
        RestAssured.basePath = kyloConfig.getBasePath();

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        Jackson2ObjectMapperFactory factory = (aClass, s) -> {
            ObjectMapper om = new ObjectMapper();
            om.registerModule(new JodaModule());
            om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
            om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            configureObjectMapper(om);

            return om;
        };
        com.jayway.restassured.mapper.ObjectMapper objectMapper = new Jackson2Mapper(factory);
        RestAssured.objectMapper(objectMapper);

        String path = getClass().getResource(".").toURI().getPath();
        String basedir = path.substring(0, path.indexOf("services"));
        feedsPath = basedir + FEED_SAMPLES_DIR;
        templatesPath = basedir + TEMPLATE_SAMPLES_DIR;
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

        startClean();
    }

    @After
    public void teardown() {
        cleanup();
    }

    protected void startClean() {
        cleanup();
    }

    /**
     * Do nothing implementation, but subclasses may override to add extra
     * json serialisation/de-serialisation modules
     */
    protected void configureObjectMapper(ObjectMapper om) {

    }

    protected RequestSpecification given() {
        return given(null);
    }

    protected RequestSpecification given(String base) {
        if (base != null) {
            RestAssured.basePath = kyloConfig.getBasePath() + base;
        }

        String username = UserContext.getUser().getUsername();
        LOG.info("Making request as " + username);

        return RestAssured.given()
            .log().method().log().path()
            .auth().preemptive().basic(username, UserContext.getUser().getPassword())
            .contentType("application/json");
    }

    protected final void scp(final String localFile, final String remoteDir) {
        Scp scp = new Scp() {
            @Override
            public String toString() {
                return String.format("scp -P%s %s %s@%s:%s", sshConfig.getPort(), localFile, sshConfig.getUsername(), sshConfig.getHost(), remoteDir);
            }
        };
        setupSshConnection(scp);
        scp.setLocalFile(localFile);
        scp.setTodir(String.format("%s@%s:%s", sshConfig.getUsername(), sshConfig.getHost(), remoteDir));
        scp.execute();
    }

    protected final String ssh(final String command) {
        SSHExec ssh = new SSHExec() {
            @Override
            public String toString() {
                return String.format("ssh -p %s %s@%s %s", sshConfig.getPort(), sshConfig.getUsername(), sshConfig.getHost(), command);
            }
        };
        setupSshConnection(ssh);
        ssh.setOutputproperty("output");
        ssh.setCommand(command);
        ssh.execute();
        return ssh.getProject().getProperty("output");
    }

    private void setupSshConnection(SSHBase ssh) {
        ssh.setTrust(true);
        ssh.setProject(new Project());
        ssh.setKnownhosts(sshConfig.getKnownHosts());
        ssh.setVerbose(true);
        ssh.setHost(sshConfig.getHost());
        ssh.setPort(sshConfig.getPort());
        ssh.setUsername(sshConfig.getUsername());
        if (StringUtils.isNotBlank(sshConfig.getPassword())) {
            ssh.setPassword(sshConfig.getPassword());
        }
        if (StringUtils.isNotBlank(sshConfig.getKeyfile())) {
            ssh.setKeyfile(sshConfig.getKeyfile());
        }
        LOG.info(ssh.toString());
    }

    protected void waitFor(int delay, TimeUnit timeUnit, String msg) {
        LOG.info("Waiting {} {} {}...", delay, timeUnit, msg);
        Uninterruptibles.sleepUninterruptibly(delay, timeUnit);
        LOG.info("Finished waiting {} {} {}", delay, timeUnit, msg);
    }

    protected PortDTO[] getReusableInputPorts() {
        Response response = given(NifiIntegrationRestController.BASE)
            .when()
            .get(NifiIntegrationRestController.REUSABLE_INPUT_PORTS);

        response.then().statusCode(HTTP_OK);

        return response.as(PortDTO[].class);
    }

    protected void cleanup() {
        disableExistingFeeds();
        deleteExistingFeeds();
        deleteExistingReusableVersionedFlows();
        deleteExistingTemplates();
        deleteExistingCategories();
        //TODO clean up Nifi too, i.e. templates, controller services, all of canvas
    }

    protected void disableExistingFeeds() {
        LOG.info("Disabling existing feeds");

        //start clean - disable all feeds before deleting them - this
        // will give time for processors to stop before they are deleted, otherwise
        // will get an error if processor is still running while we try to delete the process group
        FeedSummary[] feeds = getFeeds();
        for (FeedSummary feed : feeds) {
            disableFeed(feed.getFeedId());
        }
        if (feeds.length > 0) {
            //give time for processors to stop
            waitFor(PROCESSOR_STOP_WAIT_DELAY, TimeUnit.SECONDS, "for processors to stop");
        }
    }

    protected void deleteExistingFeeds() {
        LOG.info("Deleting existing feeds");

        //start clean - delete all feeds
        FeedSummary[] feeds = getFeeds();
        for (FeedSummary feed : feeds) {
            deleteFeed(feed.getFeedId());
        }
        feeds = getFeeds();
        Assert.assertTrue(feeds.length == 0);
    }

    protected void deleteExistingCategories() {
        LOG.info("Deleting existing categories");

        //start clean - delete all categories if there
        FeedCategory[] categories = getCategories();
        for (FeedCategory category : categories) {
            deleteCategory(category.getId());
        }
        categories = getCategories();
        Assert.assertTrue(categories.length == 0);
    }

    protected void deleteExistingTemplates() {
        LOG.info("Deleting existing templates");

        //start clean - delete all templates if there
        RegisteredTemplate[] templates = getTemplates();
        for (RegisteredTemplate template : templates) {
            deleteTemplate(template.getId());
        }
        //assert there are no templates
        templates = getTemplates();
        Assert.assertTrue(templates.length == 0);
    }

    protected void deleteExistingReusableVersionedFlows() {
        LOG.info("Deleting existing reusable versioned flows");

        //otherwise if we don't delete each time we import a new template
        // exiting templates are versioned off and keep piling up
        PortDTO[] ports = getReusableInputPorts();
        for (PortDTO port : ports) {
            deleteVersionedNifiFlow(port.getParentGroupId());
        }
    }

    protected void deleteVersionedNifiFlow(String groupId) {
        LOG.info("Deleting versioned nifi flow {}", groupId);

        Response response = given(NifiIntegrationRestController.BASE)
            .when()
            .get("/cleanup-versions/" + groupId);

        response.then().statusCode(HTTP_OK);
    }

    protected ExportImportTemplateService.ImportTemplate importDataIngestTemplate() {
        return importFeedTemplate(DATA_INGEST_ZIP);
    }

    protected ExportImportTemplateService.ImportTemplate importFeedTemplate(String templateName) {
        LOG.info("Importing feed template {}", templateName);

        //get number of templates already there
        int existingTemplateNum = getTemplates().length;

        //import standard feedTemplate template
        ExportImportTemplateService.ImportTemplate feedTemplate = importTemplate(templateName);
        Assert.assertEquals(templateName, feedTemplate.getFileName());
        Assert.assertTrue(feedTemplate.isSuccess());

        //assert new template is there
        RegisteredTemplate[] templates = getTemplates();
        Assert.assertTrue(templates.length == existingTemplateNum + 1);
        return feedTemplate;
    }

    protected void importSystemFeeds() {
        ExportImportFeedService.ImportFeed textIndex = importFeed("index_text_service_elasticsearch.feed.zip");
        enableFeed(textIndex.getNifiFeed().getFeedMetadata().getFeedId());
    }

    protected int getTotalNumberOfRecords(String feedId) {
        return getMetricvalueOfMetricType(feedId, "TOTAL_COUNT");
    }

    protected int getNumberOfValidRecords(String feedId) {
        return getMetricvalueOfMetricType(feedId, "VALID_COUNT");
    }

    protected int getNumberOfInvalidRecords(String feedId) {
        return getMetricvalueOfMetricType(feedId, "INVALID_COUNT");
    }

    protected String getProcessingDttm(String feedId) {
        return getJsonPathOfProfileSummary(feedId, "processing_dttm[0]");
    }

    protected int getMetricvalueOfMetricType(String feedId, String metricType) {
        return Integer.parseInt(getJsonPathOfProfileSummary(feedId, "find {entry ->entry.metrictype == '" + metricType + "'}.metricvalue"));
    }

    protected String getJsonPathOfProfileSummary(String feedId, String path) {
        Response response = given(FeedRestController.BASE)
            .when()
            .get(String.format("/%s/profile-summary", feedId));

        response.then().statusCode(HTTP_OK);

        return JsonPath.from(response.asString()).getString(path);
    }


    protected DefaultExecutedJob getJobWithSteps(long executionId) {
        //http://localhost:8400/proxy/v1/jobs
        Response response = given(JobsRestController.BASE)
            .when()
            .get(String.format("/%s?includeSteps=true", executionId));

        response.then().statusCode(HTTP_OK);

        return response.as(DefaultExecutedJob.class);
    }

    protected DefaultExecutedJob[] getJobs() {
        //http://localhost:8400/proxy/v1/jobs
        Response response = given(JobsRestController.BASE)
            .when()
            .get();

        response.then().statusCode(HTTP_OK);

        return JsonPath.from(response.asString()).getObject("data", DefaultExecutedJob[].class);
    }

    protected NifiFeed createFeed(FeedMetadata feed) {
        LOG.info("Creating feed {}", feed.getFeedName());

        Response response = given(FeedRestController.BASE)
            .body(feed)
            .when()
            .post();

        response.then().statusCode(HTTP_OK);

        return response.as(NifiFeed.class);
    }

    protected PartitionField byYear(String fieldName) {
        PartitionField part = new PartitionField();
        part.setSourceField(fieldName);
        part.setField(fieldName + "_year");
        part.setFormula("year");
        part.setSourceDataType("timestamp");
        return part;
    }

    protected FieldPolicy newPolicyWithValidation(String fieldName, FieldValidationRule... rules) {
        FieldPolicy policy = newEmptyPolicy(fieldName);
        List<FieldValidationRule> validationRules = new ArrayList<>();
        if (rules != null) {
            Collections.addAll(validationRules, rules);
        }
        policy.setValidation(validationRules);
        return policy;
    }

    protected FieldPolicy newPolicyWithProfileAndIndex(String fieldName, FieldStandardizationRule... rules) {
        FieldPolicy policy = newEmptyPolicy(fieldName);
        policy.setProfile(true);
        policy.setIndex(true);
        List<FieldStandardizationRule> standardisation = new ArrayList<>();
        if (rules != null) {
            Collections.addAll(standardisation, rules);
        }
        policy.setStandardization(standardisation);
        return policy;
    }

    protected FieldPolicy newEmptyPolicy(String fieldName) {
        FieldPolicy policy = new FieldPolicy();
        policy.setFieldName(fieldName);
        policy.setFeedFieldName(fieldName);
        return policy;
    }

    protected DefaultField newStringField(String name) {
        return newNamedField(name, new DefaultDataTypeDescriptor(), "string");
    }

    protected DefaultField newTimestampField(String name) {
        return newNamedField(name, new DefaultDataTypeDescriptor(), "timestamp");
    }

    protected DefaultField newBigIntField(String name) {
        DefaultDataTypeDescriptor numericDescriptor = new DefaultDataTypeDescriptor();
        numericDescriptor.setNumeric(true);

        return newNamedField(name, numericDescriptor, "bigint");
    }

    protected DefaultField newNamedField(String name, DefaultDataTypeDescriptor numericDescriptor, String bigint) {
        DefaultField field = new DefaultField();
        field.setName(name);
        field.setDerivedDataType(bigint);
        field.setDataTypeDescriptor(numericDescriptor);
        return field;
    }


    protected FeedCategory[] getCategories() {
        Response response = getCategoriesExpectingStatus(HTTP_OK);
        return response.as(FeedCategory[].class);
    }

    protected Response getCategoriesExpectingStatus(int expectedStatusCode) {
        Response response = given(FeedCategoryRestController.BASE)
            .when()
            .get();

        response.then().statusCode(expectedStatusCode);
        return response;
    }

    protected FeedCategory getorCreateCategoryByName(String name) {
        Response response = getCategoryByName(name);
        if(response.statusCode() == HTTP_BAD_REQUEST){
            return createCategory(name);
        }
        else {
            return response.as(FeedCategory.class);
        }
    }

    protected Response getCategoryByName(String categoryName) {
        String url = String.format("/by-name/%s", categoryName);
        Response response = given(FeedCategoryRestController.BASE)
            .when()
            .get(url);
        return response;
    }

    protected void deleteCategory(String id) {
        LOG.info("Deleting category {}", id);

        String url = String.format("/%s", id);
        Response response = given(FeedCategoryRestController.BASE)
            .when()
            .delete(url);

        response.then().statusCode(HTTP_OK);
    }

    protected FeedCategory createCategory(String name) {
        LOG.info("Creating category {}", name);

        FeedCategory category = new FeedCategory();
        category.setName(name);
        category.setDescription("this category was created by functional test");
        category.setIcon("account_balance");
        category.setIconColor("#FF8A65");

        Response response = given(FeedCategoryRestController.BASE)
            .body(category)
            .when()
            .post();

        response.then().statusCode(HTTP_OK);

        return response.as(FeedCategory.class);
    }


    protected ExportImportFeedService.ImportFeed importFeed(String feedName) {
        LOG.info("Importing feed {}", feedName);

        Response post = given(AdminController.BASE)
            .contentType("multipart/form-data")
            .multiPart(new File(feedsPath + feedName))
            .multiPart("overwrite", true)
            .multiPart("importConnectingReusableFlow", ImportTemplateOptions.IMPORT_CONNECTING_FLOW.YES)
            .when().post(AdminController.IMPORT_FEED);

        post.then().statusCode(HTTP_OK);

        return post.as(ExportImportFeedService.ImportFeed.class);
    }

    protected ExportImportTemplateService.ImportTemplate importTemplate(String templateName) {
        Response post = given(AdminController.BASE)
            .contentType("multipart/form-data")
            .multiPart(new File(templatesPath + templateName))
            .multiPart("overwrite", true)
            .multiPart("createReusableFlow", false)
            .multiPart("importConnectingReusableFlow", ImportTemplateOptions.IMPORT_CONNECTING_FLOW.YES)
            .when().post(AdminController.IMPORT_TEMPLATE);

        post.then().statusCode(HTTP_OK);

        return post.as(ExportImportTemplateService.ImportTemplate.class);
    }

    protected FeedSummary[] getFeeds() {
        final ObjectMapper mapper = new ObjectMapper();
        SearchResult searchResult = getFeedsExpectingStatus(HTTP_OK).as(SearchResultImpl.class);
        return searchResult.getData().stream().map(o -> mapper.convertValue(o, FeedSummary.class)).toArray(FeedSummary[]::new);
    }

    protected Response getFeedsExpectingStatus(int expectedStatusCode) {
        Response response = given(FeedRestController.BASE)
            .when()
            .get();

        response.then().statusCode(expectedStatusCode);
        return response;
    }

    protected void disableFeed(String feedId) {
        LOG.info("Disabling feed {}", feedId);

        Response response = disableFeedExpecting(feedId, HTTP_OK);

        FeedSummary feed = response.as(FeedSummary.class);
        Assert.assertEquals(Feed.State.DISABLED.name(), feed.getState());
    }

    protected Response disableFeedExpecting(String feedId, int statusCode) {
        String url = String.format("/disable/%s", feedId);
        Response response = given(FeedRestController.BASE)
            .when()
            .post(url);

        response.then().statusCode(statusCode);
        return response;
    }

    protected void enableFeed(String feedId) {
        LOG.info("Enabling feed {}", feedId);

        Response response = enableFeedExpecting(feedId, HTTP_OK);

        FeedSummary feed = response.as(FeedSummary.class);
        Assert.assertEquals(Feed.State.ENABLED.name(), feed.getState());
    }

    protected Response enableFeedExpecting(String feedId, int statusCode) {
        String url = String.format("/enable/%s", feedId);
        Response response = given(FeedRestController.BASE)
            .when()
            .post(url);

        response.then().statusCode(statusCode);
        return response;
    }

    protected void deleteFeed(String feedId) {
        LOG.info("Deleting feed {}", feedId);
        deleteFeedExpecting(feedId, HTTP_NO_CONTENT);
    }

    protected void deleteFeedExpecting(String feedId, int statusCode) {
        String url = String.format("/%s", feedId);
        Response response = given(FeedRestController.BASE)
            .when()
            .delete(url);

//        if (response.statusCode() == 409) {
//            RestResponseStatus responseStatus = response.body().as(RestResponseStatus.class);
        //todo find id of referring feed and delete it if failed here because the feed is referenced by other feed
//        } else {
        response.then().statusCode(statusCode);
//        }
    }

    protected void exportFeed(String feedId) {
        Response response = exportFeedExpecting(feedId, HTTP_OK);
        response.then().contentType(MediaType.APPLICATION_OCTET_STREAM);
    }

    protected Response exportFeedExpecting(String feedId, int code) {
        Response response = given(AdminController.BASE)
            .when()
            .get("/export-feed/" + feedId);

        response.then().statusCode(code);
        return response;
    }

    protected RegisteredTemplate[] getTemplates() {
        return getTemplatesExpectingStatus(HTTP_OK).as(RegisteredTemplate[].class);
    }

    protected Response getTemplatesExpectingStatus(int expectedStatusCode) {
        Response response = given(TemplatesRestController.BASE)
            .when().get(TemplatesRestController.REGISTERED);

        response.then().statusCode(expectedStatusCode);
        return response;
    }

    protected void deleteTemplate(String templateId) {
        LOG.info("Deleting template {}", templateId);

        String url = String.format("/registered/%s/delete", templateId);
        Response response = given(TemplatesRestController.BASE)
            .when()
            .delete(url);

        response.then().statusCode(HTTP_OK);
    }

    protected void assertHiveSchema(String schemaName, String tableName) {
        LOG.info("Asserting hive schema");

        Response response = given(HiveRestController.BASE)
            .when()
            .get(String.format("/schemas/%s/tables/%s", schemaName, tableName));

        response.then().statusCode(HTTP_OK);
    }

    protected void assertHiveTables(final String schemaName, final String tableName) {
        LOG.info("Asserting hive tables");

        Response response = given(HiveRestController.BASE)
            .when()
            .get("/tables");

        response.then().statusCode(HTTP_OK);

        String[] tables = response.as(String[].class);
        Assert.assertEquals(5, tables.length);

        List<String> tableNames = Arrays.asList(tables);
        Assert.assertTrue(tableNames.contains(schemaName + "." + tableName));
        Assert.assertTrue(tableNames.contains(schemaName + "." + tableName + "_feed"));
        Assert.assertTrue(tableNames.contains(schemaName + "." + tableName + "_profile"));
        Assert.assertTrue(tableNames.contains(schemaName + "." + tableName + "_valid"));
        Assert.assertTrue(tableNames.contains(schemaName + "." + tableName + "_invalid"));
    }

    protected void assertHiveQuery(String schemaName, String tableName) {
        LOG.info("Asserting hive query");

        int limit = 10;
        Response response = given(HiveRestController.BASE)
            .when()
            .get("/query-result?query=SELECT * FROM " + schemaName + "." + tableName + " LIMIT " + limit);

        response.then().statusCode(HTTP_OK);

        List rows = JsonPath.from(response.asString()).getList("rows");
        Assert.assertEquals(limit, rows.size());
    }

    protected FeedMetadata getCreateFeedRequest(FeedCategory category, ExportImportTemplateService.ImportTemplate template, String name) {
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
        fields.add(newStringField("last_name"));
        fields.add(newStringField("email"));
        fields.add(newStringField("gender"));
        fields.add(newStringField("ip_address"));
        fields.add(newStringField("cc"));
        fields.add(newStringField("country"));
        fields.add(newStringField("birthdate"));
        fields.add(newStringField("salary"));
        fields.add(newStringField("title"));
        fields.add(newStringField("comments"));
        schema.setFields(fields);

        table.setTableSchema(schema);
        table.setSourceTableSchema(schema);
        table.setFeedTableSchema(schema);
        table.setTargetMergeStrategy("DEDUPE_AND_MERGE");
        table.setFeedFormat(
            "ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'\n WITH SERDEPROPERTIES ( 'separatorChar' = ',' ,'escapeChar' = '\\\\' ,'quoteChar' = '\\'') STORED AS TEXTFILE");
        table.setTargetFormat("STORED AS ORC");

        List<FieldPolicy> policies = new ArrayList<>();
        policies.add(newEmptyPolicy("registration_dttm"));
        policies.add(newEmptyPolicy("id"));
        policies.add(newPolicyWithProfileAndIndex("first_name", toUpperCase));
        policies.add(newPolicyWithProfileAndIndex("last_name"));
        policies.add(newPolicyWithValidation("email", email));
        policies.add(newEmptyPolicy("gender"));
        policies.add(newEmptyPolicy("ip_address"));
        policies.add(newEmptyPolicy("cc"));
        policies.add(newEmptyPolicy("country"));
        policies.add(newEmptyPolicy("birthdate"));
        policies.add(newEmptyPolicy("salary"));
        policies.add(newEmptyPolicy("title"));
        policies.add(newEmptyPolicy("comments"));
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

    protected void copyDataToDropzone() {
        LOG.info("Copying data to dropzone");

        //drop files in dropzone to run the feed
        ssh(String.format("sudo chmod a+w %s", VAR_DROPZONE));
        scp(usersDataPath + USERDATA1_CSV, VAR_DROPZONE);
        ssh(String.format("sudo chown -R nifi:nifi %s", VAR_DROPZONE));
    }

    protected ActionGroup getServicePermissions(String group) {
        Response allowed = given(AccessControlController.BASE)
            .when()
            .get("/services/allowed?group=" + group);

        allowed.then().statusCode(HTTP_OK);
        return allowed.as(ActionGroup.class);
    }

    protected ActionGroup setServicePermissions(PermissionsChange permissionsChange) {
        Response response = given(AccessControlController.BASE)
            .body(permissionsChange)
            .when()
            .post("/services/allowed");

        response.then().statusCode(HTTP_OK);

        return response.as(ActionGroup.class);
    }

    protected ActionGroup setFeedEntityPermissions(RoleMembershipChange roleChange, String feedId) {
        Response response = setFeedEntityPermissionsExpectingStatus(roleChange, feedId, HTTP_OK);
        return response.as(ActionGroup.class);
    }

    protected Response setFeedEntityPermissionsExpectingStatus(RoleMembershipChange roleChange, String feedId, int httpStatus) {
        Response response = given(FeedRestController.BASE)
            .body(roleChange)
            .when()
            .post(String.format("/%s/roles", feedId));

        response.then().statusCode(httpStatus);
        return response;
    }

    protected ActionGroup setCategoryEntityPermissions(RoleMembershipChange roleChange, String categoryId) {
        Response response = setCategoryEntityPermissionsExpectingStatus(roleChange, categoryId, HTTP_OK);
        return response.as(ActionGroup.class);
    }

    protected Response setCategoryEntityPermissionsExpectingStatus(RoleMembershipChange roleChange, String categoryId, int httpStatus) {
        Response response = given(FeedCategoryRestController.BASE)
            .body(roleChange)
            .when()
            .post(String.format("/%s/roles", categoryId));

        response.then().statusCode(httpStatus);
        return response;
    }


}
