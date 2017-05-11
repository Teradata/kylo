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
import com.thinkbiganalytics.feedmgr.service.template.ExportImportTemplateService;
import com.thinkbiganalytics.integration.IntegrationTestBase;
import com.thinkbiganalytics.integration.UserContext;
import com.thinkbiganalytics.rest.model.RestResponseStatus;
import com.thinkbiganalytics.security.rest.model.Action;
import com.thinkbiganalytics.security.rest.model.ActionGroup;
import com.thinkbiganalytics.security.rest.model.PermissionsChange;
import com.thinkbiganalytics.security.rest.model.RoleMembershipChange;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Asserts that Category, Template and Feeds are only accessible when given permission to do so.
 */
public class EntityLevelAccessIT extends IntegrationTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(EntityLevelAccessIT.class);

    public static final String GROUP_ANALYSTS = "analysts";

    private FeedCategory category;
    private ExportImportTemplateService.ImportTemplate ingestTemplate;
    private FeedMetadata feed;

    @Test
    public void test() {
        createWithAdmin();
        assertNotVisibleForUserAnalyst();
        grantReadAccessToGroupAnalysts();
        assertVisibleForAnalyst();
    }

//    @Test
    public void temp() {
//        allowAccessToFeed("");
    }

    @Override
    protected void cleanup() {
        UserContext.setUser(UserContext.User.ADMIN);

        super.cleanup();
        resetPermissionsForGroup(GROUP_ANALYSTS);
    }

    private void resetPermissionsForGroup(String groupName) {
        ActionGroup actions = new ActionGroup("services");
        PermissionsChange permissionsChange = new PermissionsChange(PermissionsChange.ChangeType.REPLACE, actions);
        permissionsChange.addGroup(groupName);

        setServicePermissions(permissionsChange);
    }

    public void assertVisibleForAnalyst() {
        LOG.info("Asserting feeds are visible to user 'analyst'");

        UserContext.setUser(UserContext.User.ANALYST);

        FeedSummary[] feeds = getFeeds();
        Assert.assertTrue(feeds.length > 0);
    }

    public void grantReadAccessToGroupAnalysts() {
        LOG.info("Granting access to user group Analysts");

        UserContext.setUser(UserContext.User.ADMIN);

        addServicePermissions();
        addEntityPermissions();
    }

    private void addEntityPermissions() {
        RoleMembershipChange roleChange = new RoleMembershipChange(RoleMembershipChange.ChangeType.REPLACE, "readOnly");
        roleChange.addGroup(GROUP_ANALYSTS);

        setFeedEntityPermissions(roleChange, feed.getFeedId());
    }

    private void addServicePermissions() {
        ActionGroup actions = new ActionGroup("services");
        Action accessFeedsSupport = new Action("accessFeedsSupport", "Access Feed Support", "Allows access to feeds and feed-related functions");
        Action accessFeedsAction = new Action("accessFeeds", "Access Feeds", "Allows access to feeds");
        accessFeedsSupport.addAction(accessFeedsAction);
        actions.addAction(accessFeedsSupport);
        PermissionsChange permissionsChange = new PermissionsChange(PermissionsChange.ChangeType.REPLACE, actions);
        permissionsChange.addGroup(GROUP_ANALYSTS);

        setServicePermissions(permissionsChange);
    }

    public void assertNotVisibleForUserAnalyst() {
        LOG.info("Asserting categories, templates and feeds are not visible to user 'analyst'");

        UserContext.setUser(UserContext.User.ANALYST);

        assertCategoriesNotVisibleToAnalyst();
        assertTemplatesNotVisibleToAnalyst();
        assertFeedsNotVisibleToAnalyst();
    }

    private void assertCategoriesNotVisibleToAnalyst() {
        Response response = getCategoriesExpectingStatus(403);
        RestResponseStatus status  = response.as(RestResponseStatus.class);
        Assert.assertEquals("Not authorized to perform the action: Access Categories", status.getMessage());
    }

    private void assertTemplatesNotVisibleToAnalyst() {
        Response response = getTemplatesExpectingStatus(403);
        RestResponseStatus status  = response.as(RestResponseStatus.class);
        Assert.assertEquals("Not authorized to perform the action: Access Templates", status.getMessage());
    }

    private void assertFeedsNotVisibleToAnalyst() {
        Response response = getFeedsExpectingStatus(403);
        RestResponseStatus status  = response.as(RestResponseStatus.class);
        Assert.assertEquals("Not authorized to perform the action: Access Feeds", status.getMessage());
    }

    private void createWithAdmin() {
        LOG.info("Creating category, template and feed as admin");

        category = createCategory("Entity Access Tests");
        ingestTemplate = importDataIngestTemplate();

        //create standard ingest feed
        FeedMetadata feedRequest = getCreateFeedRequest(category, ingestTemplate, "Feed A");
        feed = createFeed(feedRequest);
    }

}
