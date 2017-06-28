package com.thinkbiganalytics.metadata.upgrade.v080;

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
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.feed.JcrFeed;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeException;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

@Component("upgradeAction080")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class UpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(UpgradeAction.class);

    private static final String CATEGORY_TYPE = "tba:category";
    private static final String FEED_TYPE = "tba:feed";
    private static final String UPGRADABLE_TYPE = "tba:upgradable";

    private static final String CAT_DETAILS_TYPE = "tba:categoryDetails";
    private static final String FEED_SUMMARY_TYPE = "tba:feedSummary";
    private static final String FEED_DETAILS_TYPE = "tba:feedDetails";
    private static final String FEED_DATA_TYPE = "tba:feedData";

    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.8", "0", "");
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.upgrade.UpgradeState#upgradeFrom(com.thinkbiganalytics.metadata.api.app.KyloVersion)
     */
    @Override
    public void upgradeTo(KyloVersion startingVersion) {
        log.info("Upgrading from version: " + startingVersion);

        Session session = JcrMetadataAccess.getActiveSession();
        Node feedsNode = JcrUtil.getNode(session, "metadata/feeds");

        int categoryCount = 0;
        int categoryFeedCount = 0;
        int totalFeedCount = 0;

        for (Node catNode : JcrUtil.getNodesOfType(feedsNode, CATEGORY_TYPE)) {
            String catName= "";
            try {
                catName = catNode != null ? catNode.getName() : "";
            }catch (RepositoryException e){
                // its fine to swallow this exception
            }

            log.info("Starting upgrading category: [{}] {}", ++categoryCount, catName);

            categoryFeedCount = 0;
            Node detailsNode = JcrUtil.getOrCreateNode(catNode, "tba:details", CAT_DETAILS_TYPE);
            moveProperty("tba:initialized", catNode, detailsNode);
            moveProperty("tba:securityGroups", catNode, detailsNode);

            for (Node feedNode : JcrUtil.getNodesOfType(catNode, FEED_TYPE)) {
                moveNode(session, feedNode, detailsNode);
                String feedName= "";
                try {
                    feedName = feedNode != null ? feedNode.getName() : "";
                }catch (RepositoryException e){
                    // its fine to swallow this exception
                }

                log.info("\tStarting upgrading feed: [{}] {}", ++categoryFeedCount, feedName);
                ++totalFeedCount;
                Node feedSummaryNode = JcrUtil.getOrCreateNode(feedNode, "tba:summary", FEED_SUMMARY_TYPE);
                Node feedDataNode = JcrUtil.getOrCreateNode(feedNode, "tba:data", FEED_DATA_TYPE);
                addMixin(feedNode, UPGRADABLE_TYPE);

                moveProperty(JcrFeed.SYSTEM_NAME, feedNode, feedSummaryNode);
                moveProperty("tba:category", feedNode, feedSummaryNode);
                moveProperty("tba:tags", feedNode, feedSummaryNode);
                moveProperty(JcrFeed.TITLE, feedNode, feedSummaryNode);
                moveProperty(JcrFeed.DESCRIPTION, feedNode, feedSummaryNode);

                if (JcrUtil.hasNode(feedNode, "tba:properties")) {
                    final Node feedPropertiesNode = JcrUtil.getNode(feedNode, "tba:properties");
                    moveNode(session, feedPropertiesNode, feedSummaryNode);
                }

                Node feedDetailsNode = JcrUtil.getOrCreateNode(feedSummaryNode, "tba:details", FEED_DETAILS_TYPE);

                moveProperty("tba:feedTemplate", feedNode, feedDetailsNode);
                moveProperty("tba:slas", feedNode, feedDetailsNode);
                moveProperty("tba:dependentFeeds", feedNode, feedDetailsNode, PropertyType.WEAKREFERENCE);
                moveProperty("tba:usedByFeeds", feedNode, feedDetailsNode, PropertyType.WEAKREFERENCE);
                moveProperty("tba:json", feedNode, feedDetailsNode);

                // Loop is needed because sns is specified for node type
                List<Node> feedSourceNodes = JcrUtil.getNodeList(feedNode, "tba:sources");
                for (Node feedSourceNode : feedSourceNodes) {
                    moveNode(session, feedSourceNode, feedDetailsNode);
                }

                // Loop is needed because sns is specified for node type
                List<Node> feedDestinationNodes = JcrUtil.getNodeList(feedNode, "tba:destinations");
                for (Node feedDestinationNode : feedDestinationNodes) {
                    moveNode(session, feedDestinationNode, feedDetailsNode);
                }

                if (JcrUtil.hasNode(feedNode, "tba:precondition")) {
                    Node feedPreconditionNode = JcrUtil.getNode(feedNode, "tba:precondition");
                    moveNode(session, feedPreconditionNode, feedDetailsNode);
                }

                moveProperty("tba:state", feedNode, feedDataNode);
                moveProperty("tba:schedulingPeriod", feedNode, feedDataNode);
                moveProperty("tba:schedulingStrategy", feedNode, feedDataNode);
                moveProperty("tba:securityGroups", feedNode, feedDataNode);

                if (JcrUtil.hasNode(feedNode, "tba:highWaterMarks")) {
                    Node feedWaterMarksNode = JcrUtil.getNode(feedNode, "tba:highWaterMarks");
                    moveNode(session, feedWaterMarksNode, feedDataNode);
                }

                if (JcrUtil.hasNode(feedNode, "tba:initialization")) {
                    Node feedInitializationNode = JcrUtil.getNode(feedNode, "tba:initialization");
                    moveNode(session, feedInitializationNode, feedDataNode);
                }

                removeMixin(feedNode, UPGRADABLE_TYPE);
                log.info("\tCompleted upgrading feed: " + feedName);
            }
            log.info("Completed upgrading category: " + catName);
        }

        // Update templates
        int templateCount = 0;
        final Node templatesNode = JcrUtil.getNode(session, "metadata/templates");

        for (Node templateNode : JcrUtil.getNodesOfType(templatesNode, "tba:feedTemplate")) {
            String templateName = "";
            try {
                templateName = templateNode != null ? templateNode.getName() : "";
            }catch (RepositoryException e){
                // its fine to swallow this exception
            }

            log.info("Starting upgrading template: [{}] {}", ++templateCount, templateName);
            JcrUtil.getOrCreateNode(templateNode, "tba:allowedActions", "tba:allowedActions");
            log.info("Completed upgrading template: " + templateName);
        }

        log.info("Upgrade complete for {} categories and {} feeds and {} templates", categoryCount, totalFeedCount, templateCount);
    }

    /**
     * Adds the specified mixin to the specified node.
     *
     * @param node      the target node
     * @param mixinName the name of the mixin
     */
    private void addMixin(@Nonnull final Node node, @Nonnull final String mixinName) {
        try {
            node.addMixin(mixinName);
        } catch (final RepositoryException e) {
            throw new UpgradeException("Failed to add mixin " + mixinName + " to node " + node, e);
        }
    }

    private void moveNode(Session session, Node node, Node parentNode) {
        try {
            if ((node != null) && (parentNode != null)) {
                final String srcPath = node.getParent().getPath() + "/" + StringUtils.substringAfterLast(node.getPath(), "/");  // Path may not be accurate if parent node moved recently
                session.move(srcPath, parentNode.getPath() + "/" + node.getName());
            }
        } catch (RepositoryException e) {
            throw new UpgradeException("Failed to moved node " + node + " under parent " + parentNode, e);
        }
    }

    /**
     * move a property from one node to another
     *
     * @param propName     the name of the property to move
     * @param fromNode     the node to move from
     * @param toNode       the node to move to
     * @param propertyType Optional.  This is the new property type, or null if unchanged
     */
    private void moveProperty(String propName, Node fromNode, Node toNode, Integer propertyType) {
        try {
            if ((fromNode != null) && (toNode != null)) {
                if (fromNode.hasProperty(propName)) {
                    Property prop = fromNode.getProperty(propName);

                    if (propertyType == null) {
                        propertyType = prop.getType();
                    }

                    if (propertyType != prop.getType()) {
                        log.info("Property type for {} on Node {} is changing from {} to {} ", propName, fromNode.getName(), prop.getType(), propertyType);
                    }

                    if (prop.isMultiple()) {
                        toNode.setProperty(propName, prop.getValues(), propertyType);
                    } else {
                        toNode.setProperty(propName, prop.getValue(), propertyType);
                    }

                    prop.remove();
                }
            }
        } catch (RepositoryException e) {
            throw new UpgradeException("Failed to moved property " + propName + " from " + fromNode + " to " + toNode, e);
        }
    }


    /**
     * Move a property from one node to another keeping the same property type
     *
     * @param propName the name of the property to move
     * @param fromNode the node to move the property from
     * @param toNode   the new node to move it to
     */
    private void moveProperty(String propName, Node fromNode, Node toNode) {
        moveProperty(propName, fromNode, toNode, null);
    }


    /**
     * Removes the specified mixin from the specified node.
     *
     * @param node      the target node
     * @param mixinName the name of the mixin
     */
    private void removeMixin(@Nonnull final Node node, @Nonnull final String mixinName) {
        try {
            node.removeMixin(mixinName);
        } catch (final RepositoryException e) {
            throw new UpgradeException("Failed to remove mixin " + mixinName + " from node " + node, e);
        }
    }
}
