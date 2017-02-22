/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.api.security.RoleAssignments;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.security.role.JcrSecurityRole;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.user.JcrUserGroup;
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.role.SecurityRole;

/**
 *
 */
public class JcrRoleAssignments extends JcrObject implements RoleAssignments {

    public static final String NODE_NAME = "tba:allowedActions";
    public static final String MEMBERSHIPS = "tba:roleMemberships";
    public static final String MEMBERSHIP_TYPE = "tba:roleMembership";
    public static final String ROLE = "tba:role";
    public static final String GROUPS = "tba:groups";
    public static final String USERS = "tba:users";
    
    /**
     * Wraps a parent node containing the tba:accessControlled mixin.
     * @param node the parent entity node
     */
    public JcrRoleAssignments(Node node) {
        super(node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.RoleAssignments#getRole()
     */
    @Override
    public Set<SecurityRole> getAssignedRoles() {
        return streamMemberships()
                .map(node -> new JcrSecurityRole(JcrPropertyUtil.getProperty(node, ROLE)))
                .collect(Collectors.toSet());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.RoleAssignments#getMembers()
     */
    @Override
    public Set<Principal> getMembers(SecurityRole role) {
        JcrSecurityRole jcrRole = (JcrSecurityRole) role;
        
        return streamMemberships()
                .filter(node -> JcrPropertyUtil.isReferencing(node, ROLE, jcrRole.getNode()))
                .findFirst()
                .map(node -> extractMembers(node))
                .orElse(Collections.emptySet());
    }

    protected Stream<Node> streamMemberships() {
        return StreamSupport.stream(JcrUtil.getInterableChildren(node, MEMBERSHIPS).spliterator(), false);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.RoleAssignments#addMember(java.security.Principal)
     */
    @Override
    public void addMember(SecurityRole role, GroupPrincipal principal) {
        JcrSecurityRole jcrRole = (JcrSecurityRole) role;
        Node membershipNode = createOrAddMembership(jcrRole.getNode(), principal);
        
//        JcrPropertyUtil.addToSetProperty(node, GROUPS, principal.getName(), true);
    }

    /**
     * @param node
     * @param principal
     * @return
     */
    private Node createOrAddMembership(Node node, GroupPrincipal principal) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.RoleAssignments#removeMember(java.security.Principal)
     */
    @Override
    public void removeMember(SecurityRole role, GroupPrincipal principal) {
        JcrPropertyUtil.removeFromSetProperty(node, GROUPS, principal.getName());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.RoleAssignments#addMember(com.thinkbiganalytics.security.UsernamePrincipal)
     */
    @Override
    public void addMember(SecurityRole role, UsernamePrincipal principal) {
        JcrPropertyUtil.addToSetProperty(node, USERS, principal.getName());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.RoleAssignments#removeMember(com.thinkbiganalytics.security.UsernamePrincipal)
     */
    @Override
    public void removeMember(SecurityRole role, UsernamePrincipal principal) {
        JcrPropertyUtil.removeFromSetProperty(node, USERS, principal.getName());
    }

    private Set<Principal> extractMembers(Node node) {
        Stream<Principal> groups = JcrPropertyUtil.<String>getSetProperty(node, GROUPS).stream()
                        .map(str -> new GroupPrincipal(str));
        Stream<Principal> users = JcrPropertyUtil.<String>getSetProperty(node, USERS).stream()
                        .map(str -> new UsernamePrincipal(str));
        return Stream.concat(groups, users).collect(Collectors.toSet());
    }

}
