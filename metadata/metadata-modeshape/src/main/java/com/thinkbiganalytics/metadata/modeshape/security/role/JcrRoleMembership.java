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
import com.thinkbiganalytics.security.role.SecurityRole;

/**
 *
 */
public class JcrRoleMembership extends JcrObject implements RoleMembership {

    public static final String NODE_NAME = "tba:roleMemberships";
    public static final String NODE_TYPE = "tba:roleMembership";
    public static final String ROLE = "tba:role";
    public static final String GROUPS = "tba:groups";
    public static final String USERS = "tba:users";
    
    private JcrAllowedActions allowedActions;

    public static Object create(Node node, Node rNode) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Wraps a parent node containing the tba:accessControlled mixin.
     * @param node the parent entity node
     */
    public JcrRoleMembership(Node node, JcrAllowedActions allowed) {
        super(node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.RoleMembership#getRole()
     */
    @Override
    public SecurityRole getRole() {
        return JcrUtil.getReferencedObject(getNode(), ROLE, JcrSecurityRole.class);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.RoleMembership#getMembers()
     */
    @Override
    public Set<Principal> getMembers() {
        Stream<Principal> groups = JcrPropertyUtil.<String>getSetProperty(getNode(), GROUPS).stream().map(GroupPrincipal::new);
        Stream<Principal> users = JcrPropertyUtil.<String>getSetProperty(getNode(), USERS).stream().map(UsernamePrincipal::new);
        return Stream.concat(groups, users).collect(Collectors.toSet());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.RoleMembership#addMember(java.security.Principal)
     */
    @Override
    public void addMember(GroupPrincipal principal) {
        JcrPropertyUtil.addToSetProperty(getNode(), GROUPS, principal.getName());
        enable(principal);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.RoleMembership#removeMember(java.security.Principal)
     */
    @Override
    public void removeMember(GroupPrincipal principal) {
        JcrPropertyUtil.removeFromSetProperty(getNode(), GROUPS, principal.getName());
        disable(principal);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.RoleMembership#addMember(com.thinkbiganalytics.security.UsernamePrincipal)
     */
    @Override
    public void addMember(UsernamePrincipal principal) {
        JcrPropertyUtil.addToSetProperty(getNode(), USERS, principal.getName());
        enable(principal);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.RoleMembership#removeMember(com.thinkbiganalytics.security.UsernamePrincipal)
     */
    @Override
    public void removeMember(UsernamePrincipal principal) {
        JcrPropertyUtil.removeFromSetProperty(getNode(), USERS, principal.getName());
        disable(principal);
    }

    protected void enable(Principal principal) {
        JcrAllowedActions roleAllowed = (JcrAllowedActions) getRole().getAllowedActions();
        
        roleAllowed.getAvailableActions().stream()
                .flatMap(avail -> avail.stream())
                .forEach(action -> this.allowedActions.enable(principal, action));
    }

    protected void disable(Principal principal) {
        JcrAllowedActions roleAllowed = (JcrAllowedActions) getRole().getAllowedActions();
        
        roleAllowed.getAvailableActions().stream()
                .flatMap(avail -> avail.stream())
                .forEach(action -> this.allowedActions.disable(principal, action));
    }

}
