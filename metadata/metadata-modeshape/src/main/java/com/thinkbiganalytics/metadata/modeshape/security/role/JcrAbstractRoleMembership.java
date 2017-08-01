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

import java.security.Principal;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.api.security.RoleMembership;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.GroupPrincipal;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowableAction;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.role.SecurityRole;

/**
 *
 */
public abstract class JcrAbstractRoleMembership extends JcrObject implements RoleMembership {

    private static final UsernamePrincipal[] NO_USERS = new UsernamePrincipal[0];
    private static final GroupPrincipal[] NO_GROUPS = new GroupPrincipal[0];
    public static final String NODE_NAME = "tba:roleMemberships";
    public static final String NODE_TYPE = "tba:roleMembership";
    public static final String ROLE = "tba:role";
    public static final String GROUPS = "tba:groups";
    public static final String USERS = "tba:users";

    public static <M extends JcrAbstractRoleMembership> M create(Node parentNode, Node roleNode, Class<M> memberClass, Object... args) {
        // This is a no-op if the membership already exists in the SNS nodes
        return JcrUtil.getNodeList(parentNode, NODE_NAME).stream()
                        .map(node -> JcrUtil.getJcrObject(node, memberClass, args))
                        .filter(memshp -> memshp.getRole().getSystemName().equals(JcrUtil.getName(roleNode)))
                        .findFirst()
                        .orElseGet(() -> {
                            return JcrUtil.addJcrObject(parentNode, NODE_NAME, NODE_TYPE, memberClass, roleNode, args);
                        });
    }

    public static void remove(Node parentNode, Node roleNode) {
        JcrUtil.getNodeList(parentNode, NODE_NAME).forEach(node -> {
            JcrEntityRoleMembership membership = JcrUtil.getJcrObject(node, JcrEntityRoleMembership.class, (JcrAllowedActions) null);
            
            if (membership.getRole().getSystemName().equals(JcrUtil.getName(roleNode))) {
                JcrUtil.removeNode(node);
            }
        });
    }

    public static void removeAll(Node parentNode) {
        JcrUtil.getNodeList(parentNode, NODE_NAME).forEach(node -> JcrUtil.removeNode(node));
    }

    public static <M extends JcrAbstractRoleMembership> Optional<M> find(Node parentNode, String roleName, Class<M> memberClass, Object... args) {
        return JcrUtil.getNodeList(parentNode, NODE_NAME).stream()
                        .map(node -> JcrUtil.getJcrObject(node, memberClass, args))
                        .filter(memshp -> memshp.getRole().getSystemName().equals(roleName))
                        .findFirst();
    }

    
    public JcrAbstractRoleMembership(Node node) {
        super(node);
    }

    
    @Override
    public SecurityRole getRole() {
        return JcrUtil.getReferencedObject(getNode(), ROLE, JcrSecurityRole.class);
    }

    @Override
    public Set<Principal> getMembers() {
        Stream<? extends Principal> groups = streamGroups();
        Stream<? extends Principal> users = streamUsers();
        return Stream.concat(groups, users).collect(Collectors.toSet());
    }

    @Override
    public void setMemebers(UsernamePrincipal... principals) {
        Set<UsernamePrincipal> newMembers = Arrays.stream(principals).collect(Collectors.toSet());
        Set<UsernamePrincipal> oldMembers = streamUsers().collect(Collectors.toSet());
        
        newMembers.stream()
            .filter(u -> ! oldMembers.contains(u))
            .forEach(this::addMember);
        
        oldMembers.stream()
            .filter(u -> ! newMembers.contains(u))
            .forEach(this::removeMember);
    }

    @Override
    public void setMemebers(GroupPrincipal... principals) {
        Set<GroupPrincipal> newMembers = Arrays.stream(principals).collect(Collectors.toSet());
        Set<GroupPrincipal> oldMembers = streamGroups().collect(Collectors.toSet());
        
        newMembers.stream()
            .filter(u -> ! oldMembers.contains(u))
            .forEach(this::addMember);
        
        oldMembers.stream()
            .filter(u -> ! newMembers.contains(u))
            .forEach(this::removeMember);
    }

    @Override
    public void addMember(GroupPrincipal principal) {
        JcrPropertyUtil.addToSetProperty(getNode(), GROUPS, principal.getName());
        enable(principal);
    }

    @Override
    public void addMember(UsernamePrincipal principal) {
        JcrPropertyUtil.addToSetProperty(getNode(), USERS, principal.getName());
        enable(principal);
    }

    @Override
    public void removeMember(GroupPrincipal principal) {
        disable(principal);
        JcrPropertyUtil.removeFromSetProperty(getNode(), GROUPS, principal.getName());
    }

    @Override
    public void removeMember(UsernamePrincipal principal) {
        disable(principal);
        JcrPropertyUtil.removeFromSetProperty(getNode(), USERS, principal.getName());
    }

    @Override
    public void removeAllMembers() {
        setMemebers(NO_USERS);
        setMemebers(NO_GROUPS);
    }

    
    /**
     * Enable any permissions for this principal after it was added as a role member of this RoleMembership.
     * @param principal the added principal
     */
    protected abstract void enable(Principal principal);

    /**
     * Disable any permissions for this principal after it was removed as a role member of this RoleMembership.
     * @param principal the removed principal
     */
    protected abstract void disable(Principal principal);

    /**
     * A convenience method for enabling permissions on the given AllowedActions for the added principal.
     */
    protected void enable(Principal principal, AllowedActions allowed) {
        JcrAllowedActions roleAllowed = (JcrAllowedActions) getRole().getAllowedActions();
        Set<Action> actions = roleAllowed.getAvailableActions().stream()
                        .flatMap(avail -> avail.stream())
                        .collect(Collectors.toSet());
        
        allowed.enable(principal, actions);
    }

    /**
     * A convenience method for disabling permissions on the given AllowedActions for the removed principal.
     */
    protected void disable(Principal principal, Stream<RoleMembership> allMemberships, AllowedActions allowed) {
        SecurityRole thisRole = getRole();
        
        // Get all actions allowed by all other memberships of this principal besides this one.
        Set<AllowableAction> otherAllowables = allMemberships
                        .filter(membership -> membership.getMembers().contains(principal))
                        .map(membership -> membership.getRole())
                        .filter(role -> ! role.getSystemName().equals(thisRole.getSystemName()))
                        .flatMap(role -> role.getAllowedActions().getAvailableActions().stream())
                        .flatMap(avail -> avail.stream())
                        .collect(Collectors.toSet());
        
        // Disable only the actions not permitted by any other role memberships for this principal
        Set<Action> disabled = thisRole.getAllowedActions().getAvailableActions().stream()
                        .flatMap(avail -> avail.stream())
                        .filter(action -> ! otherAllowables.contains(action))
                        .collect(Collectors.toSet());
        
        allowed.disable(principal, disabled);
    }

    protected Stream<UsernamePrincipal> streamUsers() {
        return JcrPropertyUtil.<String>getSetProperty(getNode(), USERS).stream().map(UsernamePrincipal::new);
    }

    protected Stream<GroupPrincipal> streamGroups() {
        return JcrPropertyUtil.<String>getSetProperty(getNode(), GROUPS).stream().map(GroupPrincipal::new);
    }
}
