/**
 * 
 */
package com.thinkbiganalytics.metadata.upgrade.version_0_7_1;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkbiganalytics.metadata.api.app.KyloVersion;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.upgrade.UpgradeException;
import com.thinkbiganalytics.metadata.upgrade.UpgradeState;

/**
 *
 */
public class UpgradeAction implements UpgradeState {
    
    private static final Logger log = LoggerFactory.getLogger(UpgradeAction.class);
    
    private static final String CATEGORY_TYPE = "tba:category";
    private static final String FEED_TYPE = "tba:feed";

    private static final String CAT_DETALS_TYPE = "tba:categoryDetails";
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
        if (getStartingVersion().equals(startingVersion)) {
            log.info("Upgrading from version: " + startingVersion);
            
//            Session session = JcrMetadataAccess.getActiveSession();
//            Node feedsNode = JcrUtil.getNode(session, "metadata/feeds");
//            
//            for (Node catNode : JcrUtil.getNodesOfType(feedsNode, CATEGORY_TYPE)) {
//                Node detailsNode = JcrUtil.getOrCreateNode(catNode, "tba:details", CAT_DETALS_TYPE);
//                moveProperty("tba:initialized", catNode, detailsNode);
//                moveProperty("tba:securityGroups", catNode, detailsNode);
//                
//                for (Node feedNode : JcrUtil.getNodesOfType(catNode, FEED_TYPE)) {
//                    moveNode(session, feedNode, detailsNode);
//                    // Upgrade the feed node or the transaction will fail since the constraints will be violated.
//                }
//            }
            
            log.info("Upgrad complete");
        }
    }

    private void moveNode(Session session, Node node, Node parentNode) {
        try {
            session.move(node.getPath(), parentNode.getPath() + "/" + node.getName());
        } catch (RepositoryException e) {
            throw new UpgradeException("Failed to moved node " + node + " under parent " + parentNode);
        }
    }

    private void moveProperty(String propName, Node fromNode, Node toNode) {
        try {
            if (fromNode.hasProperty(propName)) {
                Property prop = fromNode.getProperty(propName);
                if (prop.isMultiple()) {
                    toNode.setProperty(propName, prop.getValues());
                } else {
                    toNode.setProperty(propName, prop.getValue());
                }
                prop.remove();
            }
        } catch (RepositoryException e) {
            throw new UpgradeException("Failed to moved property " + propName + " from " + fromNode + " to " + toNode);
        }
    }

}
