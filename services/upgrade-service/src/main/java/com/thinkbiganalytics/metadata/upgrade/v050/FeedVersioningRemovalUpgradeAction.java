/**
 * 
 */
package com.thinkbiganalytics.metadata.upgrade.v050;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinitionTemplate;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;

import org.modeshape.jcr.api.nodetype.NodeTypeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.thinkbiganalytics.KyloVersion;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrVersionUtil;
import com.thinkbiganalytics.server.upgrade.KyloUpgrader;
import com.thinkbiganalytics.server.upgrade.UpgradeException;
import com.thinkbiganalytics.server.upgrade.UpgradeState;

/**
 * Adds the services-level permissions for the feed manager.
 */
@Component("feedVersioningRemovalUpgradeAction050")
@Profile(KyloUpgrader.KYLO_UPGRADE)
public class FeedVersioningRemovalUpgradeAction implements UpgradeState {

    private static final Logger log = LoggerFactory.getLogger(FeedVersioningRemovalUpgradeAction.class);

    @Override
    public boolean isTargetVersion(KyloVersion version) {
        return version.matches("0.5", "0", "");
    }

    @Override
    public void upgradeTo(KyloVersion startingVersion) {
        log.info("Removing feed versioning: {}", startingVersion);
        
        Session session = JcrMetadataAccess.getActiveSession();
        try {
            Node feedsNode = session.getRootNode().getNode("metadata/feeds");

            NodeTypeManager typeMgr = (NodeTypeManager) session.getWorkspace().getNodeTypeManager();
            NodeType currentFeedType = typeMgr.getNodeType("tba:feed");
            List<String> currentSupertypes = Arrays.asList(currentFeedType.getDeclaredSupertypeNames());

            if (currentSupertypes.contains("mix:versionable")) {
                log.info("Removing versionable feed type {} ", currentFeedType);
                // Remove feed version history
                for (Node catNode : JcrUtil.getNodesOfType(feedsNode, "tba:category")) {
                    for (Node feedNode : JcrUtil.getNodesOfType(catNode, "tba:feed")) {
                        log.debug("Removing prior versions of feed: {}.{}", catNode.getName(), feedNode.getName());
                        if (JcrVersionUtil.isVersionable(feedNode)) {
                            VersionManager versionManager = session.getWorkspace().getVersionManager();
                            VersionHistory versionHistory = versionManager.getVersionHistory(feedNode.getPath());
                            VersionIterator vIt = versionHistory.getAllVersions();
                            int count = 0;
                            String last = "";

                            while (vIt.hasNext()) {
                                Version version = vIt.nextVersion();
                                String versionName = version.getName();
                                String baseVersion = "";
                                if (!"jcr:rootVersion".equals(versionName)) {
                                    //baseVersion requires actual versionable node to get the base version name
                                    baseVersion = JcrVersionUtil.getBaseVersion(feedNode).getName();
                                }
                                if (!"jcr:rootVersion".equals(versionName) && !versionName.equalsIgnoreCase(baseVersion)) {
                                    last = version.getName();
                                    // removeVersion writes directly to workspace, no session.save is necessary
                                    versionHistory.removeVersion(version.getName());
                                    count++;
                                }
                            }

                            if (count > 0) {
                                log.info("Removed {} versions through {} of feed {}", count, last, feedNode.getName());
                            } else {
                                log.debug("Feed {} had no versions", feedNode.getName());
                            }
                        }
                    }
                }

                // Redefine the NodeType of tba:feed to remove versionable but retain the versionable properties with weaker constraints
                // Retaining the properties seems to override some residual properties on feed nodes that causes a failure later.
                // In particular, jcr:predecessors was accessed later but redefining all mix:versionable properties to be safe.
                NodeTypeTemplate template = typeMgr.createNodeTypeTemplate(currentFeedType);
                List<String> newSupertypes = currentSupertypes.stream().filter(type -> !type.equals("mix:versionable")).collect(Collectors.toList());

                template.setDeclaredSuperTypeNames(newSupertypes.toArray(new String[newSupertypes.size()]));

                @SuppressWarnings("unchecked")
                List<PropertyDefinitionTemplate> propTemplates = template.getPropertyDefinitionTemplates();
                PropertyDefinitionTemplate prop = typeMgr.createPropertyDefinitionTemplate();
                prop.setName("jcr:versionHistory");
                prop.setRequiredType(PropertyType.WEAKREFERENCE);
                propTemplates.add(prop);
                prop = typeMgr.createPropertyDefinitionTemplate();
                prop.setName("jcr:baseVersion");
                prop.setRequiredType(PropertyType.WEAKREFERENCE);
                propTemplates.add(prop);
                prop = typeMgr.createPropertyDefinitionTemplate();
                prop.setName("jcr:predecessors");
                prop.setRequiredType(PropertyType.WEAKREFERENCE);
                prop.setMultiple(true);
                propTemplates.add(prop);
                prop = typeMgr.createPropertyDefinitionTemplate();
                prop.setName("jcr:mergeFailed");
                prop.setRequiredType(PropertyType.WEAKREFERENCE);
                propTemplates.add(prop);
                prop = typeMgr.createPropertyDefinitionTemplate();
                prop.setName("jcr:activity");
                prop.setRequiredType(PropertyType.WEAKREFERENCE);
                propTemplates.add(prop);
                prop = typeMgr.createPropertyDefinitionTemplate();
                prop.setName("jcr:configuration");
                prop.setRequiredType(PropertyType.WEAKREFERENCE);
                propTemplates.add(prop);

                log.info("Replacing the versionable feed type '{}' with a non-versionable type", currentFeedType);
                NodeType newType = typeMgr.registerNodeType(template, true);
                log.info("Replaced with new feed type '{}' with a non-versionable type", newType);

                // This step may not be necessary.
                for (Node catNode : JcrUtil.getNodesOfType(feedsNode, "tba:category")) {
                    for (Node feedNode : JcrUtil.getNodesOfType(catNode, "tba:feed")) {
                        feedNode.setPrimaryType(newType.getName());
                        // log.info("Replaced type of node {}", feedNode);

                        if (feedNode.hasProperty("jcr:predecessors")) {
                            feedNode.getProperty("jcr:predecessors").setValue(new Value[0]);
                            ;
                            feedNode.getProperty("jcr:predecessors").remove();
                        }
                    }
                }
            }
        } catch (RepositoryException e) {
            log.error("Failure while attempting to remove versioning from feeds", e);
            throw new UpgradeException("Failure while attempting to remove versioning from feeds", e);
        }

    }

}
