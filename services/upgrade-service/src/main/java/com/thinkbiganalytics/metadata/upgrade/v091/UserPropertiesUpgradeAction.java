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

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.metadata.api.feed.FeedProvider;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;


/**
 * Moves user properties from Feed into FeedDetails so that user properties are versioned with the rest of feed details
 */
@Component("UserPropertiesUpgradeAction091")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class UserPropertiesUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(UserPropertiesUpgradeAction.class);
    
    @Inject
    private FeedProvider feedProvider;
    
    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.9", "1", "");
    }

    @Override
    public void upgradeTo(final KyloVersion targetVersion) {
        log.info("Moving user properties into feed details so that they can be versioned with the rest of the feed. Target kylo version: {}", targetVersion);

        this.feedProvider.getFeeds().stream()
            .map(JcrFeed.class::cast)
            .forEach(this::moveUserProperties);
    }

    private void moveUserProperties(JcrFeed feed) {
        try {
            Node feedNode = feed.getNode();
            Node feedDetailsNode = feed.getFeedDetails().get().getNode();
            final PropertyIterator iterator = feedNode.getProperties();

            final String prefix = JcrMetadataAccess.USR_PREFIX + ":";
            while (iterator.hasNext()) {
                final Property property = iterator.nextProperty();
                if (property.getName().startsWith(prefix)) {
                    moveProperty(property, feedDetailsNode);
                }
            }
        } catch (RepositoryException e) {
            throw new IllegalStateException(String.format("Failed to move user properties for feed %s", feed.getName()), e);
        }
    }

    private void moveProperty(Property property, Node toNode) throws RepositoryException {
        if (property.isMultiple()) {
            toNode.setProperty(property.getName(), property.getValues(), property.getType());
        } else {
            toNode.setProperty(property.getName(), property.getValue(), property.getType());
        }
        property.remove();
    }

}
