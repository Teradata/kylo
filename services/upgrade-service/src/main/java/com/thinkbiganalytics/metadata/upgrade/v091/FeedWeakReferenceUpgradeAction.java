package com.thinkbiganalytics.metadata.upgrade.v091;

/*-
 * #%L
 * kylo-upgrade-service
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

import javax.inject.Inject;
import javax.jcr.Node;

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.api.template.FeedManagerTemplateProvider;
import com.thinkbiganalytics.metadata.modeshape.feed.FeedDetails;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplate;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;


/**
 * Removes category node reference property from all feeds.  A feed's category is now only accessed through
 * the feed node's parentage.
 */
@Component("feedWeakReferenceUpgradeAction091")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class FeedWeakReferenceUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(FeedWeakReferenceUpgradeAction.class);
    
    @Inject
    private FeedManagerTemplateProvider templateProvider;

    @Inject
    private FeedProvider feedProvider;

    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.9", "1", "");
    }

    @Override
    public void upgradeTo(final KyloVersion targetVersion) {
        log.info("Updating template feed references: {}", targetVersion);
        
        this.templateProvider.findAll().stream()
            .map(JcrFeedTemplate.class::cast)
            .map(JcrFeedTemplate::getNode)
            .forEach(templateNode -> {
                Set<Node> feedNodes = JcrPropertyUtil.getSetProperty(templateNode, JcrFeedTemplate.FEEDS);
                JcrPropertyUtil.removeAllFromSetProperty(templateNode, JcrFeedTemplate.FEEDS);
                feedNodes.forEach(feedNode -> JcrPropertyUtil.addToSetProperty(templateNode, JcrFeedTemplate.FEEDS, feedNode, true));
            });
        
        this.feedProvider.findAll().stream()
            .map(JcrFeed.class::cast)
            .map(JcrFeed::getFeedDetails)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(FeedDetails::getNode)
            .forEach(detailsNode -> {
                Node templateNode = JcrPropertyUtil.getProperty(detailsNode, FeedDetails.TEMPLATE);
                JcrPropertyUtil.setProperty(detailsNode, FeedDetails.TEMPLATE, null);
                JcrPropertyUtil.setWeakReferenceProperty(detailsNode, FeedDetails.TEMPLATE, templateNode);
            });
    }
}
