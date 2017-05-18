package com.thinkbiganalytics.integration.access;

/*-
 * #%L
 * kylo-service-app
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

import com.jayway.restassured.response.Response;
import com.thinkbiganalytics.feedmgr.rest.controller.AdminController;
import com.thinkbiganalytics.feedmgr.rest.controller.FeedRestController;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.NifiFeed;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
import com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl;
import com.thinkbiganalytics.feedmgr.service.template.ExportImportTemplateService;
import com.thinkbiganalytics.integration.IntegrationTestBase;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.security.rest.model.Action;
import com.thinkbiganalytics.security.rest.model.ActionGroup;
import com.thinkbiganalytics.security.rest.model.PermissionsChange;
import com.thinkbiganalytics.security.rest.model.RoleMembershipChange;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;

import static com.thinkbiganalytics.integration.UserContext.User.ADMIN;
import static com.thinkbiganalytics.integration.UserContext.User.ANALYST;
import static java.net.HttpURLConnection.HTTP_FORBIDDEN;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Asserts that Category, Template and Feeds are only accessible when given permission to do so.
 */
public class EntityLevelAccessIT extends IntegrationTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(EntityLevelAccessIT.class);

    private static final String GROUP_ANALYSTS = "analysts";
    private static final String SERVICES = "services";
    private static final String PERMISSION_READ_ONLY = "readOnly";
    private static final String PERMISSION_EDITOR = "editor";
    private static final String PERMISSION_ADMIN = "admin";
    private static final String PERMISSION_FEED_CREATOR = "feedCreator";

    private FeedCategory category;
    private ExportImportTemplateService.ImportTemplate ingestTemplate;
    private FeedMetadata feed;

    @Test
    public void test() {
        createCategoryWithAdmin();
        assertAnalystCantAccessCategories();
//        assertCategoryNotEditableForAnalyst();

        grantAccessCategoriesToAnalysts();
        assertAnalystCanAccessCategoriesButCantSeeCategory();

//        grantCategoryEntityPermissionsToAnalysts();
//        assertCategoryIsVisibleToAnalyst();

        createTemplateWithAdmin();
        assertAnalystCantAccessTemplates();

        grantAccessTemplatesToAnalysts();
        assertAnalystCanAccessTemplatesButCantSeeTemplate();

//        grantTemplateEntityPermissionsToAnalysts();
//        assertTemplateIsVisibleToAnalyst();

        createFeedWithAdmin();
        assertAnalystCantAccessFeeds();
        assertAnalystCantEditFeed();
        assertAnalystCantExportFeed(HTTP_FORBIDDEN);
        assertAnalystCantDisableEnableFeed(HTTP_FORBIDDEN);
        assertAnalystCantEditFeedPermissions(HTTP_FORBIDDEN);
        assertAnalystCantDeleteFeed(HTTP_FORBIDDEN);
//        assertAnalystCantAccessFeedOperations(HTTP_FORBIDDEN);

        grantAccessFeedsToAnalysts();
        assertAnalystCanAccessFeedsButCantSeeFeed();
        assertAnalystCantEditFeed();
        assertAnalystCantExportFeed(HTTP_FORBIDDEN);
        assertAnalystCantDisableEnableFeed(HTTP_FORBIDDEN);
        assertAnalystCantEditFeedPermissions(HTTP_FORBIDDEN);
        assertAnalystCantDeleteFeed(HTTP_FORBIDDEN);

        grantFeedEntityPermissionToAnalysts(PERMISSION_READ_ONLY);
        assertAnalystCanSeeFeed();
        assertAnalystCantEditFeed();
        assertAnalystCantExportFeed(HTTP_FORBIDDEN);
        assertAnalystCantDisableEnableFeed(HTTP_FORBIDDEN);
        assertAnalystCantEditFeedPermissions(HTTP_FORBIDDEN);
        assertAnalystCantDeleteFeed(HTTP_FORBIDDEN);

        grantFeedEntityPermissionToAnalysts(PERMISSION_EDITOR);
        assertAnalystCanSeeFeed();
        assertAnalystCantEditFeed(); //cant edit feed until required service permissions are added for feed, category, template and entity access to category
        grantEditFeedsAndCategoryEntityAccessToAnalyst();
//        assertAnalystCanEditFeed(); //todo strangely this works at this point via UI, but doesn't work here
        assertAnalystCanDisableEnableFeed();
        assertAnalystCantExportFeed(HTTP_FORBIDDEN);
        grantTemplateAndFeedExportToAnalysts();
        assertAnalystCanExportFeed();
        assertAnalystCantEditFeedPermissions(HTTP_FORBIDDEN);
        assertAnalystCantDeleteFeed(HTTP_FORBIDDEN);

        grantFeedEntityPermissionToAnalysts(PERMISSION_ADMIN);
        assertAnalystCanSeeFeed();
//        assertAnalystCanEditFeed();
        assertAnalystCanExportFeed();
        assertAnalystCanDisableEnableFeed();
        grantAdminFeedsToAnalysts();
//        assertAnalystCanEditFeedPermissions(); // todo caused by KYLO-645

        revokeFeedEntityPermissionsFromAnalysts();
        assertAnalystCanAccessFeedsButCantSeeFeed();
//        assertAnalystCantEditFeed();
        assertAnalystCantExportFeed(HTTP_NOT_FOUND);
        assertAnalystCantDisableEnableFeed(HTTP_NOT_FOUND);
        assertAnalystCantEditFeedPermissions(HTTP_NOT_FOUND);
        assertAnalystCantDeleteFeed(HTTP_NOT_FOUND);

        grantFeedEntityPermissionToAnalysts(PERMISSION_ADMIN);
        grantCategoryEntityPermissionToAnalysts(PERMISSION_ADMIN); //to delete a feed one has to have an Admin permission to the category too
        grantAdminFeedsToAnalysts();
//        assertAnalystCanDeleteFeed(); //todo times out here and in UI at this point

        resetServicePermissionsForAnalysts();
        assertAnalystCantAccessCategories();
        assertAnalystCantAccessTemplates();
        assertAnalystCantAccessFeeds();
    }

    @Override
    public void teardown() {
        LOG.debug("EntityLevelAccessIT.teardown");
//        super.teardown();
    }

    @Override
    protected void cleanup() {
        runAs(ADMIN);

        super.cleanup();
        resetServicePermissionsForAnalysts();
    }

//    @Test
    public void temp() {
        category = new FeedCategory();
        category.setId("69d3430a-8ed5-4b58-abfe-1a6855cb056a");

        feed = new FeedMetadata();
        feed.setFeedId("e5344629-a966-4f73-a9b8-8f4646126b76");

        resetServicePermissionsForAnalysts();
        assertAnalystCantAccessCategories();
        assertAnalystCantAccessTemplates();
        assertAnalystCantAccessFeeds();

        revokeFeedEntityPermissionsFromAnalysts();
    }


    private void assertAnalystCantDeleteFeed(int statusCode) {
        LOG.debug("EntityLevelAccessIT.assertAnalystCantDeleteFeed");

        runAs(ANALYST);

        Response response = given(FeedRestController.BASE)
            .when()
            .delete(feed.getFeedId());

        response.then().statusCode(statusCode);
    }

    private void assertAnalystCanDeleteFeed() {
        LOG.debug("EntityLevelAccessIT.assertAnalystCanDeleteFeed");

        runAs(ANALYST);

        disableFeed(feed.getFeedId());
        deleteFeed(feed.getFeedId());

        FeedSummary[] feeds = getFeeds();
        Assert.assertEquals(0, feeds.length);
    }

    private void assertAnalystCantEditFeedPermissions(int status) {
        LOG.debug("EntityLevelAccessIT.assertAnalystCantEditFeedPermissions");

        runAs(ANALYST);
        RoleMembershipChange roleChange = new RoleMembershipChange(RoleMembershipChange.ChangeType.REPLACE, PERMISSION_ADMIN);
        roleChange.addGroup(GROUP_ANALYSTS);
        setFeedEntityPermissionsExpectingStatus(roleChange, feed.getFeedId(), status);
    }

    private void assertAnalystCanEditFeedPermissions() {
        LOG.debug("EntityLevelAccessIT.assertAnalystCanEditFeedPermissions");

        runAs(ANALYST);
        RoleMembershipChange roleChange = new RoleMembershipChange(RoleMembershipChange.ChangeType.REPLACE, PERMISSION_ADMIN);
        roleChange.addGroup(GROUP_ANALYSTS);
        setFeedEntityPermissions(roleChange, feed.getFeedId());
    }

    private void assertAnalystCantDisableEnableFeed(int code) {
        LOG.debug("EntityLevelAccessIT.assertAnalystCantDisableEnableFeed");

        runAs(ANALYST);

        Response disableResponse = given(FeedRestController.BASE)
            .when()
            .post("/disable/" + feed.getFeedId());
        disableResponse.then().statusCode(code);

        Response enableResponse = given(FeedRestController.BASE)
            .when()
            .post("/enable/" + feed.getFeedId());
        enableResponse.then().statusCode(code);
    }

    private void assertAnalystCanDisableEnableFeed() {
        LOG.debug("EntityLevelAccessIT.assertAnalystCanDisableEnableFeed");

        runAs(ANALYST);

        disableFeed(feed.getFeedId());
        enableFeed(feed.getFeedId());
    }

    private void assertAnalystCantExportFeed(int code) {
        LOG.debug("EntityLevelAccessIT.assertAnalystCantExportFeed");

        runAs(ANALYST);

        Response response = given(AdminController.BASE)
            .when()
            .get("/export-feed/" + feed.getFeedId());

        response.then().statusCode(code);
    }

    private void assertAnalystCanExportFeed() {
        LOG.debug("EntityLevelAccessIT.assertAnalystCanExportFeed");

        runAs(ANALYST);

        Response response = given(AdminController.BASE)
            .when()
            .get("/export-feed/" + feed.getFeedId());

        response.then().statusCode(HTTP_OK);
        response.then().contentType(MediaType.APPLICATION_OCTET_STREAM);

    }

    private void assertAnalystCantEditFeed() {
        LOG.debug("EntityLevelAccessIT.assertAnalystCantEditFeed");

        runAs(ANALYST);

        FeedMetadata createFeedRequest = getCreateFeedRequest(category, ingestTemplate, feed.getFeedName());
        createFeedRequest.setDescription("New Description");
        NifiFeed feed = createFeed(createFeedRequest);
        Assert.assertEquals(1, feed.getErrorMessages().size());
        Assert.assertEquals("Error saving Feed Not authorized to perform the action: Edit Feeds", feed.getErrorMessages().get(0));
    }

    private void assertAnalystCanEditFeed() {
        LOG.debug("EntityLevelAccessIT.assertAnalystCanEditFeed");

        runAs(ANALYST);

        FeedMetadata editFeedRequest = getCreateFeedRequest(category, ingestTemplate, feed.getFeedName());
        editFeedRequest.setId(feed.getId());
        editFeedRequest.setFeedId(feed.getFeedId());
        editFeedRequest.setDescription("New Description");
        editFeedRequest.setIsNew(false);
        NifiFeed feed = createFeed(editFeedRequest);
        Assert.assertEquals(0, feed.getErrorMessages().size());
    }

    private void assertAnalystCanAccessFeedsButCantSeeFeed() {
        LOG.debug("EntityLevelAccessIT.assertAnalystCanAccessFeedsButCantSeeFeed");
        runAs(ANALYST);
        FeedSummary[] feeds = getFeeds();
        Assert.assertEquals(0, feeds.length);
    }

    private void assertAnalystCanSeeFeed() {
        LOG.debug("EntityLevelAccessIT.assertAnalystCanSeeFeed");
        runAs(ANALYST);
        FeedSummary[] feeds = getFeeds();
        Assert.assertEquals(1, feeds.length);
    }

    private void createFeedWithAdmin() {
        LOG.debug("EntityLevelAccessIT.createFeedWithAdmin");
        runAs(ADMIN);
        FeedMetadata feedRequest = getCreateFeedRequest(category, ingestTemplate, "Feed A");
        feed = createFeed(feedRequest).getFeedMetadata();
    }

    private void assertAnalystCanAccessTemplatesButCantSeeTemplate() {
        LOG.debug("EntityLevelAccessIT.assertAnalystCanAccessTemplatesButCantSeeTemplate");
        runAs(ANALYST);
        RegisteredTemplate[] templates = getTemplates();
        Assert.assertEquals(0, templates.length);
    }

    private void createTemplateWithAdmin() {
        LOG.debug("EntityLevelAccessIT.createTemplateWithAdmin");
        runAs(ADMIN);
        ingestTemplate = importDataIngestTemplate();
    }

    private void assertAnalystCanAccessCategoriesButCantSeeCategory() {
        LOG.debug("EntityLevelAccessIT.assertAnalystCanAccessCategoriesButCantSeeCategory");
        runAs(ANALYST);
        FeedCategory[] categories = getCategories();
        Assert.assertEquals(0, categories.length);
    }

    private void grantEditFeedsAndCategoryEntityAccessToAnalyst() {
        // Editing a feed requires service access to template, category, feed and edit feed actions.
        // Editing a feed also requires 'feed creator' role on specific category

        LOG.debug("EntityLevelAccessIT.grantEditFeedsAndCategoryEntityAccessToAnalyst");
        runAs(ADMIN);

        Action feedsSupport = createAction(FeedServicesAccessControl.FEEDS_SUPPORT);
        Action accessFeeds = createAction(FeedServicesAccessControl.ACCESS_FEEDS);
        accessFeeds.addAction(createAction(FeedServicesAccessControl.EDIT_FEEDS));
        feedsSupport.addAction(accessFeeds);

        grantServiceActionToAnalysts(feedsSupport);

        grantCategoryEntityPermissionToAnalysts(PERMISSION_FEED_CREATOR);
    }

    private void grantTemplateAndFeedExportToAnalysts() {
        LOG.debug("EntityLevelAccessIT.grantTemplateAndFeedExportToAnalysts");
        runAs(ADMIN);
        Action feedsSupport = createAction(FeedServicesAccessControl.FEEDS_SUPPORT);
        Action accessFeeds = createAction(FeedServicesAccessControl.ACCESS_FEEDS);
        accessFeeds.addAction(createAction(FeedServicesAccessControl.EXPORT_FEEDS));

        Action accessTemplates = createAction(FeedServicesAccessControl.ACCESS_TEMPLATES);
        accessTemplates.addAction(createAction(FeedServicesAccessControl.EXPORT_TEMPLATES));
        feedsSupport.addAction(accessTemplates);
        feedsSupport.addAction(accessFeeds);

        grantServiceActionToAnalysts(feedsSupport);
    }

    private void grantAdminFeedsToAnalysts() {
        LOG.debug("EntityLevelAccessIT.grantAdminFeedsToAnalysts");
        runAs(ADMIN);
        Action feedsSupport = createAction(FeedServicesAccessControl.FEEDS_SUPPORT);
        Action accessFeeds = createAction(FeedServicesAccessControl.ACCESS_FEEDS);
        accessFeeds.addAction(createAction(FeedServicesAccessControl.ADMIN_FEEDS));

        feedsSupport.addAction(accessFeeds);

        grantServiceActionToAnalysts(feedsSupport);
    }

    private void grantAccessTemplatesToAnalysts() {
        LOG.debug("EntityLevelAccessIT.grantAccessTemplatesToAnalysts");
        runAs(ADMIN);
        Action feedsSupport = createAction(FeedServicesAccessControl.FEEDS_SUPPORT);
        feedsSupport.addAction(createAction(FeedServicesAccessControl.ACCESS_TEMPLATES));
        grantServiceActionToAnalysts(feedsSupport);
    }

    private void grantAccessCategoriesToAnalysts() {
        LOG.debug("EntityLevelAccessIT.grantAccessCategoriesToAnalysts");
        runAs(ADMIN);
        Action feedsSupport = createAction(FeedServicesAccessControl.FEEDS_SUPPORT);
        feedsSupport.addAction(createAction(FeedServicesAccessControl.ACCESS_CATEGORIES));
        grantServiceActionToAnalysts(feedsSupport);
    }

    private void grantAccessFeedsToAnalysts() {
        LOG.debug("EntityLevelAccessIT.grantAccessFeedsToAnalysts");
        Action feedsSupport = createAction(FeedServicesAccessControl.FEEDS_SUPPORT);
        feedsSupport.addAction(createAction(FeedServicesAccessControl.ACCESS_FEEDS));
        grantServiceActionToAnalysts(feedsSupport);
    }

    private void createCategoryWithAdmin() {
        LOG.debug("EntityLevelAccessIT.createCategoryWithAdmin");
        runAs(ADMIN);
        category = createCategory("Entity Access Tests");
    }

    private void assertAnalystCantAccessCategories() {
        LOG.debug("EntityLevelAccessIT.assertAnalystCantAccessCategories");
        runAs(ANALYST);
        Response response = getCategoriesExpectingStatus(HTTP_FORBIDDEN);
        RestResponseStatus status  = response.as(RestResponseStatus.class);
        Assert.assertEquals("Not authorized to perform the action: Access Categories", status.getMessage());
    }

    private void assertAnalystCantAccessTemplates() {
        LOG.debug("EntityLevelAccessIT.assertAnalystCantAccessTemplates");
        runAs(ANALYST);
        Response response = getTemplatesExpectingStatus(HTTP_FORBIDDEN);
        RestResponseStatus status  = response.as(RestResponseStatus.class);
        Assert.assertEquals("Not authorized to perform the action: Access Templates", status.getMessage());
    }

    private void assertAnalystCantAccessFeeds() {
        LOG.debug("EntityLevelAccessIT.assertAnalystCantAccessFeeds");
        runAs(ANALYST);
        Response response = getFeedsExpectingStatus(HTTP_FORBIDDEN);
        RestResponseStatus status  = response.as(RestResponseStatus.class);
        Assert.assertEquals("Not authorized to perform the action: Access Feeds", status.getMessage());
    }

    private void grantServiceActionToAnalysts(Action action) {
        LOG.debug("EntityLevelAccessIT.grantServiceActionToAnalysts");
        runAs(ADMIN);
        ActionGroup actions = new ActionGroup(SERVICES);
        actions.addAction(action);
        PermissionsChange permissionsChange = new PermissionsChange(PermissionsChange.ChangeType.REPLACE, actions);
        permissionsChange.addGroup(GROUP_ANALYSTS);

        permissionsChange.union(getServicePermissions(GROUP_ANALYSTS));

        setServicePermissions(permissionsChange);
    }

    private void resetServicePermissionsForAnalysts() {
        LOG.debug("EntityLevelAccessIT.resetServicePermissionsForAnalysts");
        runAs(ADMIN);
        ActionGroup actions = new ActionGroup(SERVICES);
        PermissionsChange permissionsChange = new PermissionsChange(PermissionsChange.ChangeType.REPLACE, actions);
        permissionsChange.addGroup(GROUP_ANALYSTS);
        setServicePermissions(permissionsChange);
    }

    private void grantFeedEntityPermissionToAnalysts(String roleName) {
        LOG.debug("EntityLevelAccessIT.grantFeedEntityPermissionToAnalysts " + roleName);
        runAs(ADMIN);
        RoleMembershipChange roleChange = new RoleMembershipChange(RoleMembershipChange.ChangeType.REPLACE, roleName);
        roleChange.addGroup(GROUP_ANALYSTS);
        setFeedEntityPermissions(roleChange, feed.getFeedId());
    }

    private void grantCategoryEntityPermissionToAnalysts(String roleName) {
        LOG.debug("EntityLevelAccessIT.grantCategoryEntityPermissionToAnalysts " + roleName);
        runAs(ADMIN);
        RoleMembershipChange roleChange = new RoleMembershipChange(RoleMembershipChange.ChangeType.REPLACE, roleName);
        roleChange.addGroup(GROUP_ANALYSTS);
        setCategoryEntityPermissions(roleChange, category.getId());
    }

    private void revokeFeedEntityPermissionsFromAnalysts() {
        LOG.debug("EntityLevelAccessIT.revokeFeedEntityPermissionsFromAnalysts");
        runAs(ADMIN);

        RoleMembershipChange roleChange = new RoleMembershipChange(RoleMembershipChange.ChangeType.REPLACE, PERMISSION_READ_ONLY);
        setFeedEntityPermissions(roleChange, feed.getFeedId());

        roleChange = new RoleMembershipChange(RoleMembershipChange.ChangeType.REPLACE, PERMISSION_EDITOR);
        setFeedEntityPermissions(roleChange, feed.getFeedId());

        roleChange = new RoleMembershipChange(RoleMembershipChange.ChangeType.REPLACE, PERMISSION_ADMIN);
        setFeedEntityPermissions(roleChange, feed.getFeedId());
    }

    private static Action createAction(com.thinkbiganalytics.security.action.Action feedsSupport) {
        return new Action(feedsSupport.getSystemName(), feedsSupport.getTitle(), feedsSupport.getDescription());
    }


}
