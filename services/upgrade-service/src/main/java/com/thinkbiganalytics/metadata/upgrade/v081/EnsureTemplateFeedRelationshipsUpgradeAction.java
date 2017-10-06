package com.thinkbiganalytics.metadata.upgrade.v081;

/*-
 * #%L
 * kylo-operational-metadata-upgrade-service
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

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryProvider;
import com.thinkbiganalytics.metadata.api.category.security.CategoryAccessControl;
import com.thinkbiganalytics.metadata.api.datasource.security.DatasourceAccessControl;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.feed.security.FeedAccessControl;
import com.thinkbiganalytics.metadata.api.security.RoleNotFoundException;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.api.template.security.TemplateAccessControl;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplate;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowableAction;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.jcr.ItemNotFoundException;

/**
 * This action is upgraded both on upgrade to v0.8.1 and during a fresh install.
 */
@Component("ensureTemplateFeedRelationshipsUpgradeAction081")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class EnsureTemplateFeedRelationshipsUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(EnsureTemplateFeedRelationshipsUpgradeAction.class);

    @Inject
    private CategoryProvider categoryProvider;
    @Inject
    private FeedProvider feedProvider;
    @Inject
    private FeedManagerTemplateProvider feedManagerTemplateProvider;
    @Inject
    private SecurityRoleProvider roleProvider;
    @Inject
    private AllowedEntityActionsProvider actionsProvider;
    @Inject
    private AccessController accessController;
    

    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.8", "1", "");
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.upgrade.UpgradeState#upgradeFrom(com.thinkbiganalytics.metadata.api.app.KyloVersion)
     */
    @Override
    public void upgradeTo(KyloVersion startingVersion) {
        log.info("Upgrading template feed relationships from version: " + startingVersion);
        
        ensureFeedTemplateFeedRelationships();
    }
    
    private void ensureFeedTemplateFeedRelationships() {
        //ensure the templates have the feed relationships
        List<Feed> feeds = feedProvider.findAll();
        if (feeds != null) {
            feeds.stream().forEach(feed -> {
                FeedManagerTemplate template = feed.getTemplate();
                if (template != null) {
                    //ensure the template has feeds.
                    List<Feed> templateFeeds = null;
                    try {
                        templateFeeds = template.getFeeds();
                    } catch (MetadataRepositoryException e) {
                        //templateFeeds are weak references.
                        //if the template feeds return itemNotExists we need to reset it
                        Throwable rootCause = ExceptionUtils.getRootCause(e);
                        if (rootCause != null && rootCause instanceof ItemNotFoundException) {
                            //reset the reference collection.  It will be rebuilt in the subsequent call
                            JcrPropertyUtil.removeAllFromSetProperty(((JcrFeedTemplate) template).getNode(), JcrFeedTemplate.FEEDS);
                        }
                    }
                    if (templateFeeds == null || !templateFeeds.contains(feed)) {
                        log.info("Updating relationship temlate: {} -> feed: {}", template.getName(), feed.getName());
                        template.addFeed(feed);
                        feedManagerTemplateProvider.update(template);
                    }
                }
            });

        }

        feedProvider.populateInverseFeedDependencies();
    }
}
