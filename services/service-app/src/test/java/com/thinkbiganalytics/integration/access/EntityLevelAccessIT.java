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
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedMetadata;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.rest.model.RegisteredTemplate;
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

import static com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl.ACCESS_CATEGORIES;
import static com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl.ACCESS_FEEDS;
import static com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl.ACCESS_TEMPLATES;
import static com.thinkbiganalytics.feedmgr.security.FeedServicesAccessControl.FEEDS_SUPPORT;
import static com.thinkbiganalytics.integration.UserContext.User.ADMIN;
import static com.thinkbiganalytics.integration.UserContext.User.ANALYST;

/**
 * Asserts that Category, Template and Feeds are only accessible when given permission to do so.
 */
public class EntityLevelAccessIT extends IntegrationTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(EntityLevelAccessIT.class);

    private static final String GROUP_ANALYSTS = "analysts";
    private static final String SERVICES = "services";

    private FeedCategory category;
    private ExportImportTemplateService.ImportTemplate ingestTemplate;
    private FeedMetadata feed;

    @Override
    protected void cleanup() {
        runAs(ADMIN);

        super.cleanup();
        resetServicePermissionsForAnalysts();
    }

    @Test
    public void test() {
        createCategoryWithAdmin();
        assertCategoriesNotVisibleToAnalyst();

        grantCategoryServicePermissionsToAnalysts();
        assertCategoryNotVisibleToAnalyst();

//        grantCategoryEntityPermissionsToAnalysts();
//        assertCategoryIsVisibleToAnalyst();

        createTemplateWithAdmin();
        assertTemplatesNotVisibleToAnalyst();

        grantTemplatesServicePermissionsToAnalysts();
        assertTemplateNotVisibleToAnalyst();

//        grantTemplateEntityPermissionsToAnalysts();
//        assertTemplateIsVisibleToAnalyst();

        createFeedWithAdmin();
        assertFeedsNotVisibleToAnalyst();

        grantFeedServicePermissionsToAnalysts();
        assertFeedNotVisibleToAnalyst();

        grantFeedEntityPermissionToAnalysts();
        assertFeedIsVisibleToAnalyst();

        revokeFeedEntityPermissionFromAnalysts();
        assertFeedNotVisibleToAnalyst();

        resetServicePermissionsForAnalysts();
        assertCategoriesNotVisibleToAnalyst();
        assertTemplatesNotVisibleToAnalyst();
        assertFeedsNotVisibleToAnalyst();
    }

    private void assertFeedIsVisibleToAnalyst() {
        LOG.debug("EntityLevelAccessIT.assertFeedIsVisibleToAnalyst");
        runAs(ANALYST);
        FeedSummary[] feeds = getFeeds();
        Assert.assertEquals(1, feeds.length);
    }

    private void assertFeedNotVisibleToAnalyst() {
        LOG.debug("EntityLevelAccessIT.assertFeedNotVisibleToAnalyst");
        runAs(ANALYST);
        FeedSummary[] feeds = getFeeds();
        Assert.assertEquals(0, feeds.length);
    }

    private void grantFeedServicePermissionsToAnalysts() {
        LOG.debug("EntityLevelAccessIT.grantFeedServicePermissionsToAnalysts");
        grantServicePermissionsToAnalysts(ACCESS_FEEDS);
    }

    private void createFeedWithAdmin() {
        LOG.debug("EntityLevelAccessIT.createFeedWithAdmin");
        runAs(ADMIN);
        FeedMetadata feedRequest = getCreateFeedRequest(category, ingestTemplate, "Feed A");
        feed = createFeed(feedRequest);
    }

    private void assertTemplateNotVisibleToAnalyst() {
        LOG.debug("EntityLevelAccessIT.assertTemplateNotVisibleToAnalyst");
        runAs(ANALYST);
        RegisteredTemplate[] templates = getTemplates();
        Assert.assertEquals(0, templates.length);
    }

    private void grantTemplatesServicePermissionsToAnalysts() {
        LOG.debug("EntityLevelAccessIT.grantTemplatesServicePermissionsToAnalysts");
        grantServicePermissionsToAnalysts(ACCESS_TEMPLATES);
    }

    private void createTemplateWithAdmin() {
        LOG.debug("EntityLevelAccessIT.createTemplateWithAdmin");
        runAs(ADMIN);
        ingestTemplate = importDataIngestTemplate();
    }

    private void assertCategoryNotVisibleToAnalyst() {
        LOG.debug("EntityLevelAccessIT.assertCategoryNotVisibleToAnalyst");
        runAs(ANALYST);
        FeedCategory[] categories = getCategories();
        Assert.assertEquals(0, categories.length);
    }

    private void grantCategoryServicePermissionsToAnalysts() {
        LOG.debug("EntityLevelAccessIT.grantCategoryServicePermissionsToAnalysts");
        grantServicePermissionsToAnalysts(ACCESS_CATEGORIES);
    }

    private void createCategoryWithAdmin() {
        LOG.debug("EntityLevelAccessIT.createCategoryWithAdmin");
        runAs(ADMIN);
        category = createCategory("Entity Access Tests");
    }

    private void assertCategoriesNotVisibleToAnalyst() {
        LOG.debug("EntityLevelAccessIT.assertCategoriesNotVisibleToAnalyst");
        runAs(ANALYST);
        Response response = getCategoriesExpectingStatus(403);
        RestResponseStatus status  = response.as(RestResponseStatus.class);
        Assert.assertEquals("Not authorized to perform the action: Access Categories", status.getMessage());
    }

    private void assertTemplatesNotVisibleToAnalyst() {
        LOG.debug("EntityLevelAccessIT.assertTemplatesNotVisibleToAnalyst");
        runAs(ANALYST);
        Response response = getTemplatesExpectingStatus(403);
        RestResponseStatus status  = response.as(RestResponseStatus.class);
        Assert.assertEquals("Not authorized to perform the action: Access Templates", status.getMessage());
    }

    private void assertFeedsNotVisibleToAnalyst() {
        LOG.debug("EntityLevelAccessIT.assertFeedsNotVisibleToAnalyst");
        runAs(ANALYST);
        Response response = getFeedsExpectingStatus(403);
        RestResponseStatus status  = response.as(RestResponseStatus.class);
        Assert.assertEquals("Not authorized to perform the action: Access Feeds", status.getMessage());
    }

    private void grantServicePermissionsToAnalysts(com.thinkbiganalytics.security.action.Action accessTemplates) {
        runAs(ADMIN);
        ActionGroup actions = new ActionGroup(SERVICES);
        Action accessFeedsSupport = new Action(FEEDS_SUPPORT.getSystemName(), FEEDS_SUPPORT.getTitle(), FEEDS_SUPPORT.getDescription());
        Action accessFeedsAction = new Action(accessTemplates.getSystemName(), accessTemplates.getTitle(), accessTemplates.getDescription());
        accessFeedsSupport.addAction(accessFeedsAction);
        actions.addAction(accessFeedsSupport);
        PermissionsChange permissionsChange = new PermissionsChange(PermissionsChange.ChangeType.REPLACE, actions);
        permissionsChange.addGroup(GROUP_ANALYSTS);

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

    public void grantFeedEntityPermissionToAnalysts() {
        LOG.debug("EntityLevelAccessIT.grantFeedEntityPermissionToAnalysts");
        runAs(ADMIN);
        RoleMembershipChange roleChange = new RoleMembershipChange(RoleMembershipChange.ChangeType.REPLACE, "readOnly");
        roleChange.addGroup(GROUP_ANALYSTS);
        setFeedEntityPermissions(roleChange, feed.getFeedId());
    }

    public void revokeFeedEntityPermissionFromAnalysts() {
        LOG.debug("EntityLevelAccessIT.revokeFeedEntityPermissionToAnalysts");
        runAs(ADMIN);
        RoleMembershipChange roleChange = new RoleMembershipChange(RoleMembershipChange.ChangeType.REPLACE, "readOnly");
        setFeedEntityPermissions(roleChange, feed.getFeedId());
    }


}
