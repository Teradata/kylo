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
import com.thinkbiganalytics.alerts.rest.controller.AlertsController;
import com.thinkbiganalytics.alerts.rest.model.AlertRange;
import com.thinkbiganalytics.discovery.model.DefaultDataTypeDescriptor;
import com.thinkbiganalytics.discovery.model.DefaultField;
import com.thinkbiganalytics.discovery.model.DefaultHiveSchema;
import com.thinkbiganalytics.discovery.model.DefaultTag;
import com.thinkbiganalytics.discovery.schema.Tag;
import com.thinkbiganalytics.feedmgr.rest.controller.AdminController;
import com.thinkbiganalytics.feedmgr.rest.controller.FeedCategoryRestController;
import com.thinkbiganalytics.feedmgr.rest.controller.FeedRestController;
import com.thinkbiganalytics.feedmgr.rest.controller.NifiIntegrationRestController;
import com.thinkbiganalytics.feedmgr.rest.controller.ServiceLevelAgreementRestController;
import com.thinkbiganalytics.feedmgr.rest.controller.TemplatesRestController;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSchedule;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.ImportTemplateOptions;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.rest.model.schema.PartitionField;
import com.thinkbiganalytics.feedmgr.rest.support.SystemNamingService;
import com.thinkbiganalytics.feedmgr.service.feed.importing.model.ImportFeed;
import com.thinkbiganalytics.feedmgr.service.template.importing.model.ImportTemplate;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementGroup;
import com.thinkbiganalytics.feedmgr.sla.ServiceLevelAgreementRule;
import com.thinkbiganalytics.hive.rest.controller.HiveRestController;
import com.thinkbiganalytics.jobrepo.query.model.DefaultExecutedJob;
import com.thinkbiganalytics.jobrepo.repository.rest.model.JobAction;
import com.thinkbiganalytics.jobrepo.rest.controller.JobsRestController;
import com.thinkbiganalytics.jobrepo.rest.controller.ServiceLevelAssessmentsController;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAgreement;
import com.thinkbiganalytics.metadata.rest.model.sla.ServiceLevelAssessment;
import com.thinkbiganalytics.metadata.sla.api.ObligationGroup;
import com.thinkbiganalytics.nifi.rest.model.NifiProperty;
import com.thinkbiganalytics.policy.rest.model.FieldRuleProperty;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.rest.model.search.SearchResult;
import com.thinkbiganalytics.rest.model.search.SearchResultImpl;
import com.thinkbiganalytics.scheduler.rest.controller.SchedulerRestController;
import com.thinkbiganalytics.scheduler.rest.model.ScheduleIdentifier;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

    private static final int PROCESSOR_STOP_WAIT_DELAY = 10;
    private static final String GET_FILE_LOG_ATTRIBUTE_TEMPLATE_ZIP = "get-file-log-attribute.template.zip";
    private static final String FUNCTIONAL_TESTS = "Functional Tests";

    protected static final String FILTER_BY_SUCCESS = "result%3D%3DSUCCESS";
    protected static final String FILTER_BY_FAILURE = "result%3D%3DFAILURE";
    protected static final String FILTER_BY_SLA_ID = "slaId%3D%3D";

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject
    private KyloConfig kyloConfig;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Inject
    private SshConfig sshConfig;

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

        startClean();
    }

    protected FeedMetadata createSimpleFeed(String feedName, String testFile) {
        FeedCategory category = createCategory(FUNCTIONAL_TESTS);
        ImportTemplate template = importSimpleTemplate();
        FeedMetadata request = makeCreateFeedRequest(category, template, feedName, testFile);
        FeedMetadata response = createFeed(request).getFeedMetadata();
        Assert.assertEquals(request.getFeedName(), response.getFeedName());
        return response;
    }

    protected FeedMetadata makeCreateFeedRequest(FeedCategory category, ImportTemplate template, String feedName, String testFile) {
        FeedMetadata feed = new FeedMetadata();
        feed.setFeedName(feedName);
        feed.setSystemFeedName(feedName.toLowerCase());
        feed.setCategory(category);
        feed.setTemplateId(template.getTemplateId());
        feed.setTemplateName(template.getTemplateName());
        feed.setDescription("Created by functional test");
        feed.setInputProcessorType("org.apache.nifi.processors.standard.GetFile");

        List<NifiProperty> properties = new ArrayList<>();
        NifiProperty fileFilter = new NifiProperty("764d053d-015e-1000-b8a2-763cd17080e1", "cffa8f24-d097-3c7a-7d04-26b7feff81ab", "File Filter", testFile);
        fileFilter.setProcessGroupName("NiFi Flow");
        fileFilter.setProcessorName("GetFile");
        fileFilter.setProcessorType("org.apache.nifi.processors.standard.GetFile");
        fileFilter.setTemplateValue("mydata\\d{1,3}.csv");
        fileFilter.setInputProperty(true);
        fileFilter.setUserEditable(true);
        properties.add(fileFilter);

        feed.setProperties(properties);

        FeedSchedule schedule = new FeedSchedule();
        schedule.setConcurrentTasks(1);
        schedule.setSchedulingPeriod("15 sec");
        schedule.setSchedulingStrategy("TIMER_DRIVEN");
        feed.setSchedule(schedule);

        feed.setDataOwner("Marketing");

        List<Tag> tags = new ArrayList<>();
        tags.add(new DefaultTag("functional tests"));
        tags.add(new DefaultTag("for category " + category.getName()));
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

    protected void copyDataToDropzone(String testFileName) {
        ssh("sudo touch /var/dropzone/" + testFileName);
        ssh("sudo chown -R nifi:nifi /var/dropzone");
    }

    protected void waitForFeedToComplete() {
        waitFor(20, TimeUnit.SECONDS, "for feed to complete");
    }

    protected List<FieldRuleProperty> newFieldRuleProperties(FieldRuleProperty... props) {
        List<FieldRuleProperty> lengthProps = new ArrayList<>(props.length);
        lengthProps.addAll(Arrays.asList(props));
        return lengthProps;
    }

    protected FieldRuleProperty newFieldRuleProperty(String name, String objectProperty, String value) {
        FieldRuleProperty list = new FieldRuleProperty();
        list.setName(name);
        list.setObjectProperty(objectProperty);
        list.setValue(value);
        return list;
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
        deleteExistingSla();
        disableExistingFeeds();
        deleteExistingFeeds();
        deleteExistingReusableVersionedFlows();
        deleteExistingTemplates();
        deleteExistingCategories();
        //TODO clean up Nifi too, i.e. templates, controller services, all of canvas
    }

    protected void deleteExistingSla() {
        LOG.info("Deleting existing SLAs");

        ServiceLevelAgreement[] agreements = getSla();
        for (ServiceLevelAgreement agreement : agreements) {
            deleteSla(agreement.getId());
        }
        agreements = getSla();
        Assert.assertTrue(agreements.length == 0);

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

    protected int getTotalNumberOfRecords(String feedId) {
        return getProfileSummary(feedId, "TOTAL_COUNT");
    }

    protected int getNumberOfValidRecords(String feedId) {
        return getProfileSummary(feedId, "VALID_COUNT");
    }

    protected int getNumberOfInvalidRecords(String feedId) {
        return getProfileSummary(feedId, "INVALID_COUNT");
    }

    protected String getProcessingDttm(String feedId) {
        return getJsonPathOfProfileSummary(feedId, "processing_dttm[0]");
    }

    protected int getProfileSummary(String feedId, String profileType) {
        return Integer.parseInt(getJsonPathOfProfileSummary(feedId, "find {entry ->entry.metrictype == '" + profileType + "'}.metricvalue"));
    }

    protected String getJsonPathOfProfileSummary(String feedId, String path) {
        Response response = given(FeedRestController.BASE)
            .when()
            .get(String.format("/%s/profile-summary", feedId));

        response.then().statusCode(HTTP_OK);

        return JsonPath.from(response.asString()).getString(path);
    }

    protected String getProfileStatsForColumn(String feedId, String processingDttm, String profileType, String column) {
        Response response = given(FeedRestController.BASE)
            .when()
            .get(String.format("/%s/profile-stats?processingdttm=%s", feedId, processingDttm));

        response.then().statusCode(HTTP_OK);

        String path = String.format("find {entry ->entry.metrictype == '%s' && entry.columnname == '%s'}.metricvalue", profileType, column);
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

    protected DefaultExecutedJob[] getJobs(String filter) {
        Response response = given(JobsRestController.BASE)
            .urlEncodingEnabled(false) //url encoding enabled false to avoid replacing percent symbols in url query part
            .when()
            .get("?filter=" + filter + "&limit=50&sort=-createdTime&start=0");

        response.then().statusCode(HTTP_OK);

        return JsonPath.from(response.asString()).getObject("data", DefaultExecutedJob[].class);
    }

    protected DefaultExecutedJob failJob(DefaultExecutedJob job) {
        Response response = given(JobsRestController.BASE)
            .body(new JobAction())
            .when()
            .post(String.format("/%s/fail", job.getInstanceId()));

        response.then().statusCode(HTTP_OK);

        return response.as(DefaultExecutedJob.class);
    }

    protected void abandonAllJobs(String categoryAndFeedName) {
        LOG.info("Abandon all jobs");

        Response response = given(JobsRestController.BASE)
            .when()
            .post(String.format("/abandon-all/%s", categoryAndFeedName));

        response.then().statusCode(HTTP_NO_CONTENT);
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

    protected FieldPolicyRuleBuilder newPolicyBuilder(String fieldName) {
        return new FieldPolicyRuleBuilder(fieldName);
    }

    protected DefaultField newStringField(String name) {
        return newNamedField(name, new DefaultDataTypeDescriptor(), "string");
    }

    protected DefaultField newTimestampField(String name) {
        return newNamedField(name, new DefaultDataTypeDescriptor(), "timestamp");
    }

    protected DefaultField newBinaryField(String name) {
        return newNamedField(name, new DefaultDataTypeDescriptor(), "binary");
    }

    protected DefaultField newBigIntField(String name) {
        DefaultDataTypeDescriptor numericDescriptor = new DefaultDataTypeDescriptor();
        numericDescriptor.setNumeric(true);

        return newNamedField(name, numericDescriptor, "bigint");
    }

    protected DefaultField newNamedField(String name, DefaultDataTypeDescriptor typeDescriptor, String type) {
        DefaultField field = new DefaultField();
        field.setName(name);
        field.setDerivedDataType(type);
        field.setDataTypeDescriptor(typeDescriptor);
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
        category.setSystemName(SystemNamingService.generateSystemName(name));
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


    protected ImportFeed importFeed(String feedPath) {
        LOG.info("Importing feed {}", feedPath);

        Response post = given(AdminController.BASE)
            .contentType("multipart/form-data")
            .multiPart(new File(feedPath))
            .multiPart("overwrite", true)
            .multiPart("importConnectingReusableFlow", ImportTemplateOptions.IMPORT_CONNECTING_FLOW.YES)
            .when().post(AdminController.IMPORT_FEED);

        post.then().statusCode(HTTP_OK);

        return post.as(ImportFeed.class);
    }

    protected ImportTemplate importSimpleTemplate() {
        URL resource = IntegrationTestBase.class.getResource(GET_FILE_LOG_ATTRIBUTE_TEMPLATE_ZIP);
        return importTemplate(resource.getPath());
    }

    protected ImportTemplate importTemplate(String templatePath) {
        LOG.info("Importing template {}", templatePath);

        Response post = given(AdminController.BASE)
            .contentType("multipart/form-data")
            .multiPart(new File(templatePath))
            .multiPart("overwrite", true)
            .multiPart("createReusableFlow", false)
            .multiPart("importConnectingReusableFlow", ImportTemplateOptions.IMPORT_CONNECTING_FLOW.YES)
            .when().post(AdminController.IMPORT_TEMPLATE);

        post.then().statusCode(HTTP_OK);

        return post.as(ImportTemplate.class);
    }

    protected FeedSummary[] getFeeds() {
        final ObjectMapper mapper = new ObjectMapper();
        SearchResult<Object> searchResult = getFeedsExpectingStatus(HTTP_OK).as(SearchResultImpl.class);
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

    protected DefaultHiveSchema getHiveSchema(String schemaName, String tableName) {
        LOG.info("Asserting hive schema");

        Response response = given(HiveRestController.BASE)
            .when()
            .get(String.format("/schemas/%s/tables/%s", schemaName, tableName));

        response.then().statusCode(HTTP_OK);
        return response.as(DefaultHiveSchema.class);
    }

    protected void assertHiveTables(final String schemaName, final String tableName) {
        LOG.info("Asserting hive tables");

        Response response = given(HiveRestController.BASE)
            .when()
            .get("/tables");

        response.then().statusCode(HTTP_OK);

        String[] tables = response.as(String[].class);
        List<String> tableNames = Arrays.asList(tables);
        Assert.assertTrue(tableNames.contains(schemaName + "." + tableName));
        Assert.assertTrue(tableNames.contains(schemaName + "." + tableName + "_feed"));
        Assert.assertTrue(tableNames.contains(schemaName + "." + tableName + "_profile"));
        Assert.assertTrue(tableNames.contains(schemaName + "." + tableName + "_valid"));
        Assert.assertTrue(tableNames.contains(schemaName + "." + tableName + "_invalid"));
    }

    protected List<HashMap<String, String>> getHiveQuery(String query) {
        LOG.info("Asserting hive query");

        int limit = 10;
        Response response = given(HiveRestController.BASE)
            .when()
            .get("/query-result?query=" + query);

        response.then().statusCode(HTTP_OK);

        return JsonPath.from(response.asString()).getList("rows");
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


    protected void assertValidatorResults(String feedId, String processingDttm, String validator, int invalidRowCount) {
        Response response = given(FeedRestController.BASE)
            .when()
            .get(String.format("/%s/profile-invalid-results?filter=%s&limit=100&processingdttm=%s", feedId, validator, processingDttm));

        response.then().statusCode(HTTP_OK);
        Object[] result = response.as(Object[].class);
        Assert.assertEquals(invalidRowCount, result.length);
    }

    protected ServiceLevelAgreementGroup createOneHourAgoFeedProcessingDeadlineSla(String feedName, String feedId) {
        LOG.info("Creating 'one hour ago' feed processing deadline SLA for feed " + feedName);
        return createFeedProcessingDeadlineSla(feedName, feedId, LocalDateTime.now().minusHours(1), "0");
    }

    protected ServiceLevelAgreementGroup createOneHourAheadFeedProcessingDeadlineSla(String feedName, String feedId) {
        LOG.info("Creating 'one hour ahead' feed processing deadline SLA for feed " + feedName);
        return createFeedProcessingDeadlineSla(feedName, feedId, LocalDateTime.now().plusHours(1), "24");
    }

    private ServiceLevelAgreementGroup createFeedProcessingDeadlineSla(String feedName, String feedId, LocalDateTime deadline, String noLaterThanHours) {
        int hourOfDay = deadline.get(ChronoField.HOUR_OF_DAY);
        int minuteOfHour = deadline.get(ChronoField.MINUTE_OF_HOUR);
        String cronExpression = String.format("0 %s %s 1/1 * ? *", minuteOfHour, hourOfDay);

        ServiceLevelAgreementGroup sla = new ServiceLevelAgreementGroup();
        String time = deadline.format(DateTimeFormatter.ofPattern("HH:mm"));
        sla.setName("Before " + time + " (cron: " + cronExpression + ")");
        sla.setDescription("The feed should complete before given date and time");
        List<ServiceLevelAgreementRule> rules = new ArrayList<>();
        ServiceLevelAgreementRule rule = new ServiceLevelAgreementRule();
        rule.setName("Feed Processing deadline");
        rule.setDisplayName("Feed Processing deadline");
        rule.setDescription("Ensure a Feed processes data by a specified time");
        rule.setObjectClassType("com.thinkbiganalytics.metadata.sla.api.core.FeedOnTimeArrivalMetric");
        rule.setObjectShortClassType("FeedOnTimeArrivalMetric");
        rule.setCondition(ObligationGroup.Condition.REQUIRED);

        rule.setProperties(newFieldRuleProperties(
            newFieldRuleProperty("FeedName", "feedName", feedName),
            newFieldRuleProperty("ExpectedDeliveryTime", "cronString", cronExpression),
            newFieldRuleProperty("NoLaterThanTime", "lateTime", noLaterThanHours),
            newFieldRuleProperty("NoLaterThanUnits", "lateUnits", "hrs")
        ));

        rules.add(rule);
        sla.setRules(rules);

        Response response = given(ServiceLevelAgreementRestController.V1_FEEDMGR_SLA)
            .body(sla)
            .when()
            .post(String.format("feed/%s", feedId));

        response.then().statusCode(HTTP_OK);

        return response.as(ServiceLevelAgreementGroup.class);
    }

    protected RestResponseStatus triggerSla(String slaName) {
        LOG.info("Triggering SLA " + slaName);

        ScheduleIdentifier si = new ScheduleIdentifier();
        si.setName(slaName);
        si.setGroup("SLA");

        Response response = given(SchedulerRestController.V1_SCHEDULER)
            .body(si)
            .when()
            .post("/jobs/trigger");

        response.then().statusCode(HTTP_OK);

        return response.as(RestResponseStatus.class);
    }

    protected ServiceLevelAssessment[] getServiceLevelAssessments(String filter) {
        LOG.info(String.format("Getting up to 50 SLA Assessments for filter %s", filter));

        Response response = given(ServiceLevelAssessmentsController.BASE)
            .urlEncodingEnabled(false) //url encoding enabled false to avoid replacing percent symbols in url query part
            .when()
            .get("?filter=" + filter + "&limit=50&sort=-createdTime&start=0");

        response.then().statusCode(HTTP_OK);

        SearchResult<Object> result = response.as(SearchResultImpl.class);
        final ObjectMapper mapper = new ObjectMapper();
        return result.getData().stream().map(o -> mapper.convertValue(o, ServiceLevelAssessment.class)).toArray(ServiceLevelAssessment[]::new);
    }

    protected ServiceLevelAgreement[] getSla() {
        LOG.info("Getting SLAs");

        Response response = given(ServiceLevelAgreementRestController.V1_FEEDMGR_SLA)
            .when()
            .get();

        response.then().statusCode(HTTP_OK);
        return response.as(ServiceLevelAgreement[].class);

    }

    protected void deleteSla(String slaId) {
        LOG.info("Deleting SLA " + slaId);

        Response response = given(ServiceLevelAgreementRestController.V1_FEEDMGR_SLA)
            .when()
            .delete(String.format("/%s", slaId));

        response.then().statusCode(HTTP_OK);
    }

    protected AlertRange getAlerts() {
        LOG.info("Getting up to 10 non-cleared Alerts");

        Response response = given(AlertsController.V1_ALERTS)
            .when()
            .get("?cleared=false&limit=10");

        response.then().statusCode(HTTP_OK);

        return response.as(AlertRange.class);

    }

}
