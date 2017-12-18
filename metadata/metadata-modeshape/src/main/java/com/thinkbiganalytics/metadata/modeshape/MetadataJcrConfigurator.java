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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.Privilege;

import org.modeshape.jcr.api.Workspace;
import org.modeshape.jcr.security.SimplePrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.PostMetadataConfigAction;
import com.thinkbiganalytics.metadata.modeshape.common.SecurityPaths;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.security.ModeShapeAdminPrincipal;
import com.thinkbiganalytics.security.role.SecurityRole;

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
                ensureAccessControl(session);
                ensureIndexes(session);
            } catch (RepositoryException e) {
                throw new MetadataRepositoryException("Could not create initial JCR metadata", e);
            }
        }, MetadataAccess.SERVICE);

        this.configured.set(true);
        firePostConfigActions();
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
    
    private void ensureIndexes(Session session) throws RepositoryException {
        Workspace workspace = (Workspace) session.getWorkspace();
        log.info("Indexing users and groups");
        workspace.reindex("/users");
        log.info("Finished indexing users");
        workspace.reindex("/groups");
        log.info("Finished indexing groups");
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
        JcrAccessControlUtil.addPermissions(prototypesNode, SimplePrincipal.EVERYONE, Privilege.JCR_READ);
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

        if (!session.getRootNode().hasNode("metadata/security")) {
            session.getRootNode().addNode("metadata/security", "tba:securityFolder");
        }
        if (!session.getRootNode().hasNode("metadata/security/prototypes")) {
            session.getRootNode().addNode("metadata/security/prototypes", "tba:prototypesFolder");
        }

        //ensure the datasources exist in prototypes
        if (!session.getRootNode().hasNode("metadata/security/prototypes/datasource")) {
            session.getRootNode().addNode("metadata/security/prototypes/datasource", "tba:allowedActions");
        }

        if (!session.getRootNode().hasNode("metadata/security/roles")) {
            session.getRootNode().addNode("metadata/security/roles", "tba:rolesFolder");
        }

        if (!session.getRootNode().hasNode("metadata/domainTypes")) {
            session.getRootNode().addNode("metadata/domainTypes", "tba:domainTypesFolder");
        }

        //ensure the role paths exist for the entities
        for (String entity : SecurityRole.ENTITIES) {
            String entityPath = "metadata/security/roles/" + entity;
            if (!session.getRootNode().hasNode(entityPath)) {
                session.getRootNode().addNode(entityPath, "tba:rolesFolder");
            }
        }
    }

}
