/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.role;

/*-
 * #%L
 * kylo-metadata-modeshape
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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import com.thinkbiganalytics.metadata.api.MetadataException;
import com.thinkbiganalytics.metadata.api.security.RoleNotFoundException;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.common.SecurityPaths;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.role.SecurityRole;
import com.thinkbiganalytics.security.role.SecurityRoleProvider;

/**
 *
 */
public class JcrSecurityRoleProvider implements SecurityRoleProvider {

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRoleProvider#createRole(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public SecurityRole createRole(String entityName, String roleName, String title, String descr) {
        Session session = JcrMetadataAccess.getActiveSession();
        Path rolePath = SecurityPaths.rolePath(entityName, roleName);
        
        if (JcrUtil.hasNode(session, rolePath.toString())) {
            throw new SecurityRoleAlreadyExistsException(entityName, roleName);
        } else {
            if (! JcrUtil.hasNode(session, rolePath.getParent().toString())) {
                // TODO create new exception
                throw new RoleNotFoundException("No role entity found with the specified name: " + entityName);
            }
            
            Node entityNode = JcrUtil.getNode(session, rolePath.getParent().toString());
            JcrSecurityRole role = JcrUtil.getOrCreateNode(entityNode, roleName, JcrSecurityRole.NODE_TYPE, JcrSecurityRole.class);
            role.setTitle(title == null ? roleName : title);
            role.setDescription(descr);
            return role;
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRoleProvider#getRoles()
     */
    @Override
    public Map<String, List<SecurityRole>> getRoles() {
        HashMap<String, List<SecurityRole>> map = new HashMap<>();
        
        for (String entity : SecurityRole.ENTITIES) {
            map.put(entity,getEntityRoles(entity));
        }
        
        return map;
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRoleProvider#getRoles(java.lang.String)
     */
    @Override
    public List<SecurityRole> getEntityRoles(String entityName) {
        Session session = JcrMetadataAccess.getActiveSession();
        Path entityPath = SecurityPaths.roleEntityPath(entityName);
        
        if (! JcrUtil.hasNode(session, entityPath.toString())) {
            // TODO create new exception
            throw new RoleNotFoundException("No role entity found with the specified name: " + entityName);
        } else {
            Node entityNode = JcrUtil.getNode(session, entityPath.toString());
            NodeType type = JcrUtil.getNodeType(session, JcrSecurityRole.NODE_TYPE);
            return JcrUtil.getJcrObjects(entityNode, type, JcrSecurityRole.class).stream()
                            .map(SecurityRole.class::cast)
                            .collect(Collectors.toList());
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRoleProvider#getRole(java.lang.String, java.lang.String)
     */
    @Override
    public Optional<SecurityRole> getRole(String entityName, String roleName) {
        Session session = JcrMetadataAccess.getActiveSession();
        Path rolePath = SecurityPaths.rolePath(entityName, roleName);
        
        if (JcrUtil.hasNode(session, rolePath.toString())) {
            JcrSecurityRole role = JcrUtil.getJcrObject(JcrUtil.getNode(session, rolePath.toString()), JcrSecurityRole.class);
            return Optional.of(role);
        } else {
            if (! JcrUtil.hasNode(session, rolePath.getParent().toString())) {
                // TODO create new exception
                throw new RoleNotFoundException("No role entity found with the specified name: " + entityName);
            }
            
            return Optional.empty();
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRoleProvider#removeRole(java.lang.String, java.lang.String)
     */
    @Override
    public boolean removeRole(String entityName, String roleName) {
        Session session = JcrMetadataAccess.getActiveSession();
        Path rolePath = SecurityPaths.rolePath(entityName, roleName);
        
        if (JcrUtil.hasNode(session, rolePath.toString())) {
            Node entityNode = JcrUtil.getNode(session, rolePath.getParent().toString());
            return JcrUtil.removeNode(entityNode, roleName);
        } else {
            if (! JcrUtil.hasNode(session, rolePath.getParent().toString())) {
                // TODO create new exception
                throw new RoleNotFoundException("No role entity found with the specified name: " + entityName);
            }
            
            return false;
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRoleProvider#setPermissions(java.lang.String, java.lang.String, com.thinkbiganalytics.security.action.Action[])
     */
    @Override
    public Optional<SecurityRole> setPermissions(String entityName, String roleName, Action... actions) {
        return setPermissions(entityName, roleName, Arrays.asList(actions));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRoleProvider#setPermissions(java.lang.String, java.lang.String, java.util.Collection)
     */
    @Override
    public Optional<SecurityRole> setPermissions(String entityName, String roleName, Collection<Action> actions) {
        Session session = JcrMetadataAccess.getActiveSession();
        Path rolePath = SecurityPaths.rolePath(entityName, roleName);
        
        if (JcrUtil.hasNode(session, rolePath.toString())) {
            JcrSecurityRole role = JcrUtil.getJcrObject(JcrUtil.getNode(session, rolePath.toString()), JcrSecurityRole.class);
            
            role.setPermissions(actions);
            return Optional.of(role);
        } else {
            if (! JcrUtil.hasNode(session, rolePath.getParent().toString())) {
                // TODO create new exception
                throw new RoleNotFoundException("No role entity found with the specified name: " + entityName);
            }
            
            return Optional.empty();
        }

    }
}
