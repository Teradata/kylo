package com.thinkbiganalytics.metadata.upgrade.version_0_7_1;

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

import com.thinkbiganalytics.metadata.api.app.KyloVersion;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.upgrade.UpgradeException;
import com.thinkbiganalytics.metadata.upgrade.UpgradeState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

public class UpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(UpgradeAction.class);

    private static final String CATEGORY_TYPE = "tba:category";
    private static final String FEED_TYPE = "tba:feed";
    private static final String UPGRADABLE_TYPE = "tba:upgradable";

    private static final String CAT_DETAILS_TYPE = "tba:categoryDetails";
    private static final String FEED_SUMMARY_TYPE = "tba:feedSummary";
    private static final String FEED_DETAILS_TYPE = "tba:feedDetails";
    private static final String FEED_DATA_TYPE = "tba:feedData";

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.upgrade.UpgradeState#getStartingVersion()
     */
    @Override
    public KyloVersion getStartingVersion() {
        return asVersion("0.7", "1");
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.upgrade.UpgradeState#upgradeFrom(com.thinkbiganalytics.metadata.api.app.KyloVersion)
     */
    @Override
    public void upgradeFrom(KyloVersion startingVersion) {
      //  if (getStartingVersion().equals(startingVersion)) {
            log.info("Upgrading from version: " + startingVersion);

            Session session = JcrMetadataAccess.getActiveSession();
            Node feedsNode = JcrUtil.getNode(session, "metadata/feeds");

            int categoryCount = 0;
            int categoryFeedCount = 0;
            int totalFeedCount = 0;

            for (Node catNode : JcrUtil.getNodesOfType(feedsNode, CATEGORY_TYPE)) {
                log.info("Starting upgrading category: [{}] {}", ++categoryCount, catNode);

                categoryFeedCount = 0;
                Node detailsNode = JcrUtil.getOrCreateNode(catNode, "tba:details", CAT_DETAILS_TYPE);
                moveProperty("tba:initialized", catNode, detailsNode);
                moveProperty("tba:securityGroups", catNode, detailsNode);

                for (Node feedNode : JcrUtil.getNodesOfType(catNode, FEED_TYPE)) {
                    moveNode(session, feedNode, detailsNode);

                    log.info("\tStarting upgrading feed: [{}] {}", ++categoryFeedCount, feedNode);
                    ++totalFeedCount;
                    Node feedSummaryNode = JcrUtil.getOrCreateNode(feedNode, "tba:summary", FEED_SUMMARY_TYPE);
                    Node feedDataNode = JcrUtil.getOrCreateNode(feedNode, "tba:data", FEED_DATA_TYPE);
                    addMixin(feedNode, UPGRADABLE_TYPE);

                    moveProperty("tba:systemName", feedNode, feedSummaryNode);
                    moveProperty("tba:category", feedNode, feedSummaryNode);
                    moveProperty("tba:tags", feedNode, feedSummaryNode);

                    if (JcrUtil.hasNode(feedNode, "tba:properties")) {
                        final Node feedPropertiesNode = JcrUtil.getNode(feedNode, "tba:properties");
                        moveNode(session, feedPropertiesNode, feedSummaryNode);
                    }

                    Node feedDetailsNode = JcrUtil.getOrCreateNode(feedSummaryNode, "tba:details", FEED_DETAILS_TYPE);

                    moveProperty("tba:feedTemplate", feedNode, feedDetailsNode);
                    moveProperty("tba:slas", feedNode, feedDetailsNode);
                    moveProperty("tba:dependentFeeds", feedNode, feedDetailsNode);
                    moveProperty("tba:usedByFeeds", feedNode, feedDetailsNode);
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
                    log.info("\tCompleted upgrading feed: " + feedNode);
                }
                log.info("Completed upgrading category: " + catNode);
            }

            log.info("Upgrade complete for {} categories and {} feeds", categoryCount, totalFeedCount);
      //  }
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
                final String srcPath = node.getParent().getPath() + "/" + node.getName();  // Path may not be accurate if parent node moved recently
                session.move(srcPath, parentNode.getPath() + "/" + node.getName());
            }
        } catch (RepositoryException e) {
            throw new UpgradeException("Failed to moved node " + node + " under parent " + parentNode, e);
        }
    }

    private void moveProperty(String propName, Node fromNode, Node toNode) {
        try {
            if ((fromNode != null) && (toNode != null)) {
                if (fromNode.hasProperty(propName)) {
                    Property prop = fromNode.getProperty(propName);
                    if (prop.isMultiple()) {
                        toNode.setProperty(propName, prop.getValues());
                    } else {
                        toNode.setProperty(propName, prop.getValue());
                    }
                    prop.remove();
                }
            }
        } catch (RepositoryException e) {
            throw new UpgradeException("Failed to moved property " + propName + " from " + fromNode + " to " + toNode, e);
        }
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
