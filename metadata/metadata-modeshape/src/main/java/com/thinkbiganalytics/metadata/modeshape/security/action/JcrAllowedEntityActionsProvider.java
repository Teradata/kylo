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
import com.thinkbiganalytics.metadata.modeshape.security.ModeShapeAdminPrincipal;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedEntityActionsProvider;

import java.nio.file.Path;
import java.security.AccessControlException;
import java.security.Principal;
import java.util.Optional;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.Privilege;

import org.modeshape.jcr.security.SimplePrincipal;

/**
 *
 */
public class JcrAllowedEntityActionsProvider implements AllowedEntityActionsProvider {

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedEntityActionsProvider#getAvailavleActions(java.lang.String)
     */
    @Override
    public Optional<AllowedActions> getAvailableActions(String entityName) {
        Path modulePath = SecurityPaths.prototypeActionsPath(entityName);

        return getActions(entityName, modulePath);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedEntityActionsProvider#getAllowedActions(java.lang.String)
     */
    @Override
    public Optional<AllowedActions> getAllowedActions(String entityName) {
        Path modulePath = SecurityPaths.moduleActionPath(entityName);

        return getActions(entityName, modulePath);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedEntityActionsProvider#checkPermission(java.lang.String, com.thinkbiganalytics.security.action.Action)
     */
    @Override
    public void checkPermission(String moduleName, Action action) {
        getAllowedActions(moduleName)
            .map((allowed) -> {
                allowed.checkPermission(action);
                return null;
            })
            .orElseThrow(() -> new AccessControlException("No actions are defined for a madule named: " + moduleName));
    }
    
    public JcrAllowedActions createEntityAllowedActions(String entityName, Node destActionsNode) {
         return getAvailableActions(entityName)
             .map(protoAllowed -> { 
                     Principal mgmtPrincipal = new ModeShapeAdminPrincipal();
                     JcrAllowedActions jcrProtoAllowed = (JcrAllowedActions) protoAllowed;
                     JcrAllowedActions entityAllowed = jcrProtoAllowed.copy(destActionsNode, mgmtPrincipal, Privilege.JCR_ALL);
                     
                     JcrAccessControlUtil.addPermissions(destActionsNode, mgmtPrincipal, Privilege.JCR_ALL);
                     JcrAccessControlUtil.addPermissions(destActionsNode, SimplePrincipal.EVERYONE, Privilege.JCR_READ);
                     
                     for (Node actionNode : JcrUtil.getNodesOfType(destActionsNode, JcrAllowableAction.NODE_TYPE)) {
                         // Initially only allow the mgmt principal access to the actions themselves
                         JcrAccessControlUtil.addPermissions(actionNode, mgmtPrincipal, Privilege.JCR_ALL);
                     }
                     
                     return entityAllowed;
                 })
             .orElseThrow(() -> new MetadataRepositoryException("No prototype AllowedActions found for entity named: " + entityName));
                        
    }

    protected Optional<AllowedActions> getActions(String groupName, Path groupPath) {
        try {
            Session session = JcrMetadataAccess.getActiveSession();

            if (session.getRootNode().hasNode(groupPath.toString())) {
                Node node = session.getRootNode().getNode(groupPath.toString());
                JcrAllowedActions actions = new JcrAllowedActions(node);
                return Optional.of(actions);
            } else {
                return Optional.empty();
            }
        } catch (AccessDeniedException e) {
            return Optional.empty();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access allowable actions for module: " + groupName, e);
        }
    }

}
