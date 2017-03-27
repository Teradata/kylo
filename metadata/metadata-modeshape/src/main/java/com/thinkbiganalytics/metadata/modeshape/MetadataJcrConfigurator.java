/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape;

/*-
 * #%L
 * thinkbig-metadata-modeshape
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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.modeshape.common.SecurityPaths;
import com.thinkbiganalytics.metadata.modeshape.extension.ExtensionsConstants;
import com.thinkbiganalytics.metadata.modeshape.security.AdminCredentials;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.security.ModeShapeAdminPrincipal;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrVersionUtil;

import org.modeshape.jcr.api.nodetype.NodeTypeManager;
import org.modeshape.jcr.security.SimplePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeTemplate;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.nodetype.PropertyDefinitionTemplate;
import javax.jcr.security.Privilege;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;

/**
 *
 */
public class MetadataJcrConfigurator {

    private static final Logger log = LoggerFactory.getLogger(MetadataJcrConfigurator.class);
    private final AtomicBoolean configured = new AtomicBoolean(false);
    @Inject
    private MetadataAccess metadataAccess;
    private List<PostMetadataConfigAction> postConfigActions = new ArrayList<>();

    public MetadataJcrConfigurator(List<PostMetadataConfigAction> actions) {
        this.postConfigActions.addAll(actions);
        this.postConfigActions.sort(new AnnotationAwareOrderComparator());
    }

    public void configure() {
        this.metadataAccess.commit(() -> {
            try {
                Session session = JcrMetadataAccess.getActiveSession();
                ensureLayout(session);
                ensureTypes(session);
                ensureAccessControl(session);
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Could not create initial JCR metadata", e);
            }
        }, MetadataAccess.SERVICE);

        this.metadataAccess.commit(() -> {
            try {
                Session session = JcrMetadataAccess.getActiveSession();
                removeVersionableFeedType(session);
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Could remove versioning from feeds", e);
            }
        }, MetadataAccess.SERVICE);

        this.configured.set(true);
       firePostConfigActions();
    }

    private void removeVersionableFeedType(Session session) throws RepositoryException {
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
                    if (JcrUtil.isVersionable(feedNode)) {
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
    }

    private void firePostConfigActions() {
        for (PostMetadataConfigAction action : this.postConfigActions) {
            // TODO: catch exceptions and continue?  Currently propagates runtime exceptions and will fail startup.
            action.run();
        }
    }

    public boolean isConfigured() {
        return this.configured.get();
    }

    private void ensureAccessControl(Session session) throws RepositoryException {
        if (!session.getRootNode().hasNode(SecurityPaths.SECURITY.toString())) {
            session.getRootNode().addNode(SecurityPaths.SECURITY.toString(), "tba:securityFolder");
        }

        Node prototypesNode = session.getRootNode().getNode(SecurityPaths.PROTOTYPES.toString());

        // Uncommenting below will remove all access control action configuration (DEV ONLY.)
        // TODO a proper migration should be implemented to in case the action hierarchy
        // has changed and the currently permitted actions need to be updated.
//        for (Node protoNode : JcrUtil.getNodesOfType(prototypesNode, "tba:allowedActions")) {
//            for (Node actionsNode : JcrUtil.getNodesOfType(protoNode, "tba:allowableAction")) {
//                actionsNode.remove();
//            }
//            
//            String modulePath = SecurityPaths.moduleActionPath(protoNode.getName()).toString();
//            
//            if (session.getRootNode().hasNode(modulePath)) {
//                Node moduleNode = session.getRootNode().getNode(modulePath);
//                
//                for (Node actionsNode : JcrUtil.getNodesOfType(moduleNode, "tba:allowableAction")) {
//                    actionsNode.remove();
//                }
//            }
//        }

        JcrAccessControlUtil.addPermissions(prototypesNode, new ModeShapeAdminPrincipal(), Privilege.JCR_ALL);
        JcrAccessControlUtil.addPermissions(prototypesNode, AdminCredentials.getPrincipal(), Privilege.JCR_ALL);
        JcrAccessControlUtil.addPermissions(prototypesNode, SimplePrincipal.EVERYONE, Privilege.JCR_READ);
    }


    protected void ensureTypes(Session session) throws RepositoryException {
        Node typesNode = session.getRootNode().getNode(ExtensionsConstants.TYPES);
        NodeTypeManager typeMgr = (NodeTypeManager) session.getWorkspace().getNodeTypeManager();
        NodeTypeIterator typeItr = typeMgr.getPrimaryNodeTypes();
        NodeType extensionsType = typeMgr.getNodeType(ExtensionsConstants.EXTENSIBLE_ENTITY_TYPE);

        while (typeItr.hasNext()) {
            NodeType type = (NodeType) typeItr.next();

            if (type.isNodeType(ExtensionsConstants.EXTENSIBLE_ENTITY_TYPE) &&
                !type.equals(extensionsType) &&
                !typesNode.hasNode(type.getName())) {
                Node descrNode = typesNode.addNode(type.getName(), ExtensionsConstants.TYPE_DESCRIPTOR_TYPE);

                descrNode.setProperty("jcr:title", simpleName(type.getName()));
                descrNode.setProperty("jcr:description", "");

                PropertyDefinition[] defs = type.getPropertyDefinitions();

                for (PropertyDefinition def : defs) {
                    String fieldName = def.getName();
                    String prefix = namePrefix(fieldName);

                    if (!ExtensionsConstants.STD_PREFIXES.contains(prefix) && !descrNode.hasNode(fieldName)) {
                        Node propNode = descrNode.addNode(def.getName(), ExtensionsConstants.FIELD_DESCRIPTOR_TYPE);
                        propNode.setProperty("jcr:title", def.getName().replace("^.*:", ""));
                        propNode.setProperty("jcr:description", "");
                    }
                }
            }
        }

        NodeIterator nodeItr = typesNode.getNodes();

        while (nodeItr.hasNext()) {
            Node typeNode = (Node) nodeItr.next();

            if (!typeMgr.hasNodeType(typeNode.getName())) {
                typeNode.remove();
            }
        }
    }

    protected void ensureLayout(Session session) throws RepositoryException {
        if (!session.getRootNode().hasNode("metadata")) {
            session.getRootNode().addNode("metadata", "tba:metadataFolder");
        }

        if (!session.getRootNode().hasNode("users")) {
            session.getRootNode().addNode("users", "tba:usersFolder");
        }

        if (!session.getRootNode().hasNode("groups")) {
            session.getRootNode().addNode("groups", "tba:groupsFolder");
        }

        // TODO Temporary to cleanup schemas which had the category folder auto-created.
        if (session.getRootNode().hasNode("metadata/feeds/category")) {
            session.getRootNode().getNode("metadata/feeds/category").remove();
        }

        if (!session.getRootNode().hasNode("metadata/hadoopSecurityGroups")) {
            session.getRootNode().getNode("metadata").addNode("hadoopSecurityGroups");
        }

        if (!session.getRootNode().hasNode("metadata/datasourceDefinitions")) {
            session.getRootNode().addNode("metadata", "tba:datasourceDefinitionsFolder");
        }
        if (!session.getRootNode().hasNode("metadata/datasources/derived")) {

            if (!session.getRootNode().hasNode("metadata/datasources")) {
                session.getRootNode().addNode("metadata", "datasources");
            }
            session.getRootNode().getNode("metadata/datasources").addNode("derived");
        }
    }

    private String namePrefix(String name) {
        Matcher m = ExtensionsConstants.NAME_PATTERN.matcher(name);

        if (m.matches()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    private String simpleName(String name) {
        Matcher m = ExtensionsConstants.NAME_PATTERN.matcher(name);

        if (m.matches()) {
            return m.group(2);
        } else {
            return null;
        }
    }

}
