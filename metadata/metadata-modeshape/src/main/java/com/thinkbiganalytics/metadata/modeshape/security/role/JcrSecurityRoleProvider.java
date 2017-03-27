/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.role;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.Session;

import com.thinkbiganalytics.metadata.api.MetadataException;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.common.SecurityPaths;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAbstractActionsBuilder;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrActionTreeBuilder;
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
        
        if (JcrUtil.hasNode(JcrMetadataAccess.getActiveSession(), rolePath.toString())) {
            throw new SecurityRoleAlreadyExistsException(entityName, roleName);
        } else {
            if (! JcrUtil.hasNode(session, rolePath.getParent().toString())) {
                // TODO create new exception
                throw new MetadataException("No role entity found with the specified name: " + entityName);
            }
            
            Node entityNode = JcrUtil.getNode(session, rolePath.getParent().toString());
            return JcrUtil.getOrCreateNode(entityNode, roleName, JcrSecurityRole.NODE_TYPE, JcrSecurityRole.class);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.role.SecurityRoleProvider#getRoles(java.lang.String)
     */
    @Override
    public List<SecurityRole> getRoles(String entityName) {
        Session session = JcrMetadataAccess.getActiveSession();
        Path entityPath = SecurityPaths.roleEntityPath(entityName);
        
        if (JcrUtil.hasNode(JcrMetadataAccess.getActiveSession(), entityPath.toString())) {
            // TODO create new exception
            throw new MetadataException("No role entity found with the specified name: " + entityName);
        } else {
            Node entityNode = JcrUtil.getNode(session, entityPath.getParent().toString());
            return JcrUtil.getJcrObjects(entityNode, JcrSecurityRole.NODE_TYPE, JcrSecurityRole.class).stream()
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
        
        if (JcrUtil.hasNode(JcrMetadataAccess.getActiveSession(), rolePath.toString())) {
            JcrSecurityRole role = JcrUtil.getJcrObject(JcrUtil.getNode(session, rolePath.toString()), JcrSecurityRole.class);
            return Optional.of(role);
        } else {
            if (! JcrUtil.hasNode(session, rolePath.getParent().toString())) {
                // TODO create new exception
                throw new MetadataException("No role entity found with the specified name: " + entityName);
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
        
        if (JcrUtil.hasNode(JcrMetadataAccess.getActiveSession(), rolePath.toString())) {
            Node entityNode = JcrUtil.getNode(session, rolePath.getParent().toString());
            return JcrUtil.removeNode(entityNode, roleName);
        } else {
            if (! JcrUtil.hasNode(session, rolePath.getParent().toString())) {
                // TODO create new exception
                throw new MetadataException("No role entity found with the specified name: " + entityName);
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
        
        if (JcrUtil.hasNode(JcrMetadataAccess.getActiveSession(), rolePath.toString())) {
            JcrSecurityRole role = JcrUtil.getJcrObject(JcrUtil.getNode(session, rolePath.toString()), JcrSecurityRole.class);
            Node actionsNode = role.getAllowedActionsNode();
            
            JcrActionTreeBuilder<JcrAbstractActionsBuilder> bldr = new JcrActionTreeBuilder<>(actionsNode, null);
            actions.forEach(action -> bldr.action(action));
            bldr.add();
            
            return Optional.of(role);
        } else {
            if (! JcrUtil.hasNode(session, rolePath.getParent().toString())) {
                // TODO create new exception
                throw new MetadataException("No role entity found with the specified name: " + entityName);
            }
            
            return Optional.empty();
        }

    }
}
