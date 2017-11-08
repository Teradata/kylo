/**
 *
 */
package com.thinkbiganalytics.metadata.modeshape.security.action;

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

import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.SecurityPaths;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.config.ActionsModuleBuilder;
import com.thinkbiganalytics.security.action.config.ActionsTreeBuilder;

import org.modeshape.jcr.security.SimplePrincipal;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.Privilege;

/**
 *
 */
public class JcrActionsGroupBuilder extends JcrAbstractActionsBuilder implements ActionsModuleBuilder {

    private final String protoModulesPath;
    private Node groupsNode;
    private Node protoActionsNode;
//    private Node actionsNode;


    public JcrActionsGroupBuilder(String protoPath) {
        this.protoModulesPath = protoPath;
    }

    public JcrActionsGroupBuilder(Node groupsNode) {
        this((String) null);
        this.groupsNode = groupsNode;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.config.ActionsModuleBuilder#group(java.lang.String)
     */
    @Override
    public ActionsTreeBuilder<ActionsModuleBuilder> module(String name) {
        Session session = JcrMetadataAccess.getActiveSession();

        try {
            Node securityNode = session.getRootNode().getNode(SecurityPaths.SECURITY.toString());
            this.groupsNode = this.groupsNode == null || !this.groupsNode.getSession().isLive()? session.getRootNode().getNode(this.protoModulesPath) : this.groupsNode;
            this.protoActionsNode = JcrUtil.getOrCreateNode(groupsNode, name, JcrAllowedActions.NODE_TYPE);
//            this.actionsNode = JcrUtil.getOrCreateNode(securityNode, name, JcrAllowedActions.NODE_TYPE);

            return new JcrActionTreeBuilder<>(protoActionsNode, this);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access root node for allowable actions", e);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.config.ActionsModuleBuilder#build()
     */
    @Override
    public AllowedActions build() {
        try {
            Session session = this.protoActionsNode.getSession();

            JcrAccessControlUtil.addPermissions(this.protoActionsNode, this.managementPrincipal, Privilege.JCR_ALL);
            JcrAccessControlUtil.addPermissions(this.protoActionsNode, new UsernamePrincipal(session.getUserID()), Privilege.JCR_ALL);
            JcrAccessControlUtil.addPermissions(this.protoActionsNode, SimplePrincipal.EVERYONE, Privilege.JCR_READ);

            JcrAllowedActions protoAllowed = new JcrAllowedActions(this.protoActionsNode);
//            JcrAllowedActions allowed = protoAllowed.copy(this.actionsNode, this.managementPrincipal, Privilege.JCR_ALL);
//
//            JcrAccessControlUtil.addPermissions(this.actionsNode, this.managementPrincipal, Privilege.JCR_ALL);
//            JcrAccessControlUtil.addPermissions(this.actionsNode, SimplePrincipal.EVERYONE, Privilege.JCR_READ);
//
//            for (Node actionNode : JcrUtil.getNodesOfType(this.actionsNode, JcrAllowableAction.NODE_TYPE)) {
//                // Initially only allow the mgmt principal access to the actions themselves
//                JcrAccessControlUtil.addPermissions(actionNode, this.managementPrincipal, Privilege.JCR_ALL);
//            }

            return protoAllowed;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to build action", e);
        }
    }

}
