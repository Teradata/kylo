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

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.metadata.api.MetadataException;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertyConstants;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowableAction;
import com.thinkbiganalytics.security.action.AllowedActions;

import org.apache.commons.lang3.ArrayUtils;

import java.security.AccessControlException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.security.Privilege;

/**
 *
 */
public class JcrAllowedActions extends JcrObject implements AllowedActions {

    public static final String NODE_NAME = "tba:allowedActions";
    public static final String NODE_TYPE = "tba:allowedActions";

    /**
     * The normal privileges used when a principal is having an action permission granted or revoked
     */
    private static final String[] GRANT_PRIVILEGES = new String[]{Privilege.JCR_READ};
    /**
     * The privileges used when a principal is having an action permission granted or revoked that
     * enables management of the actions (i.e. enable permissions for other principals)
     */
    // TODO: Replace with just JCR_ALL
    private static final String[] ADMIN_PRIVILEGES = new String[]{Privilege.JCR_READ, Privilege.JCR_READ_ACCESS_CONTROL, Privilege.JCR_MODIFY_ACCESS_CONTROL, Privilege.JCR_ALL};


    public JcrAllowedActions(Node allowedActionsNode) {
        super(allowedActionsNode);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#getAvailableActions()
     */
    @Override
    public List<AllowableAction> getAvailableActions() {
        try {
            NodeType type = JcrUtil.getNodeType(JcrMetadataAccess.getActiveSession(), JcrAllowableAction.NODE_TYPE);
            return JcrUtil.getJcrObjects(this.node, type, JcrAllowableAction.class).stream().collect(Collectors.toList());
        } catch (Exception e) {
            throw new MetadataException("Failed to retrieve the accessible functions", e);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#enableAccess(com.thinkbiganalytics.security.action.Action, java.security.Principal[])
     */
    @Override
    public boolean enable(Principal principal, Action action, Action... more) {
        Set<Action> actions = new HashSet<>(Arrays.asList(more));
        actions.add(action);
        return enable(principal, actions);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#enableAccess(com.thinkbiganalytics.security.action.Action, java.util.Set)
     */
    @Override
    public boolean enable(Principal principal, Set<Action> actions) {
        return togglePermission(actions, principal, true);
    }

    @Override
    public boolean enableOnly(Principal principal, Action action, Action... more) {
        Set<Action> actions = new HashSet<>(Arrays.asList(more));
        actions.add(action);

        return enableOnly(principal, actions);
    }

    @Override
    public boolean enableOnly(Principal principal, Set<Action> actions) {
        final AtomicBoolean result = new AtomicBoolean(false);

        getAvailableActions().stream().forEach(available -> {
            available.stream().forEach(child -> {
                if (actions.contains(child)) {
                    result.set(togglePermission(child, principal, true) || result.get());
                } else {
                    togglePermission(child, principal, false);
                }
            });
        });

        return result.get();
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#enableOnly(java.security.Principal, com.thinkbiganalytics.security.action.AllowedActions)
     */
    @Override
    public boolean enableOnly(Principal principal, AllowedActions actions) {
        return enableOnly(principal,
                          actions.getAvailableActions().stream()
                              .flatMap(avail -> avail.stream())
                              .collect(Collectors.toSet()));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#enableAll(java.security.Principal)
     */
    @Override
    public boolean enableAll(Principal principal) {
        return enableOnly(principal,
                          getAvailableActions().stream()
                              .flatMap(avail -> avail.stream())
                              .collect(Collectors.toSet()));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#disableAccess(com.thinkbiganalytics.security.action.Action, java.security.Principal[])
     */
    @Override
    public boolean disable(Principal principal, Action action, Action... more) {
        Set<Action> actions = new HashSet<>(Arrays.asList(more));
        actions.add(action);
        return disable(principal, actions);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#disableAccess(com.thinkbiganalytics.security.action.Action, java.util.Set)
     */
    @Override
    public boolean disable(Principal principal, Set<Action> actions) {
        return togglePermission(actions, principal, false);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#disable(java.security.Principal, com.thinkbiganalytics.security.action.AllowedActions)
     */
    @Override
    public boolean disable(Principal principal, AllowedActions actions) {
        return disable(principal,
                       actions.getAvailableActions().stream()
                           .flatMap(avail -> avail.stream())
                           .collect(Collectors.toSet()));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#deisableAll(java.security.Principal)
     */
    @Override
    public boolean disableAll(Principal principal) {
        return disable(principal,
                       getAvailableActions().stream()
                           .flatMap(avail -> avail.stream())
                           .collect(Collectors.toSet()));
    }

    /**
     * validate a user has a given permission(s)
     *
     * @param action the action to check
     * @param more   additional actions to check
     * @return true if user has the permission(s), false if not
     */
    public boolean hasPermission(Action action, Action... more) {
        Set<Action> actions = new HashSet<>(Arrays.asList(more));
        actions.add(action);
        try {
            checkPermission(actions);
        } catch (AccessControlException e) {
            return false;
        }
        return true;
    }

    @Override
    public void checkPermission(Action action, Action... more) {
        checkPermission(Stream.concat(Stream.of(action), Stream.of(more)).collect(Collectors.toSet()));
    }

    @Override
    public void checkPermission(Set<Action> actions) {
        for (Action action : actions) {
            Node current = getNode();
            for (Action parent : action.getHierarchy()) {
                if (!JcrUtil.hasNode(current, parent.getSystemName())) {
                    throw new AccessControlException("Not authorized to perform the action: " + action.getTitle());
                }

                current = JcrUtil.getNode(current, parent.getSystemName());
            }
        }
    }

    public void removeAccessControl(Principal owner) {
        JcrAccessControlUtil.clearRecursivePermissions(getNode(), JcrAllowableAction.NODE_TYPE);
    }

    public void setupAccessControl(Principal owner) {
        JcrAccessControlUtil.addRecursivePermissions(getNode(), JcrAllowableAction.NODE_TYPE, MetadataAccess.ADMIN, Privilege.JCR_ALL);
    }

    public JcrAllowedActions copy(Node allowedNode, Principal principal, String... privilegeNames) {
        return copy(allowedNode, false, principal, privilegeNames);
    }

    public JcrAllowedActions copy(Node allowedNode, boolean includeDescr, Principal principal, String... privilegeNames) {
        try {
            // TODO Remove extraneous nodes in destination
//            for (Node existing : JcrUtil.getIterableChildren(allowedNode)) {
//                existing.remove();
//            }

            JcrAccessControlUtil.addPermissions(allowedNode, principal, privilegeNames);

            for (Node actionNode : JcrUtil.getNodesOfType(getNode(), JcrAllowableAction.NODE_TYPE)) {
                copyAction(actionNode, allowedNode, includeDescr, principal, privilegeNames);
            }

            return new JcrAllowedActions(allowedNode);
        } catch (RepositoryException e) {
            throw new MetadataException("Failed to copy allowed actions", e);
        }
    }
    
    @Override
    public Set<Principal> getPrincipalsAllowedAll(Action action, Action... more) {
        return getPrincipalsAllowedAll(Stream.concat(Stream.of(action), Stream.of(more)).collect(Collectors.toSet()));
    }

    @Override
    public Set<Principal> getPrincipalsAllowedAll(Set<Action> actions) {
        Set<Principal> results = Collections.emptySet();
        
        Iterator<Action> itr = actions.iterator();
        if (itr.hasNext()) {
            results = findActionNode(itr.next())
                            .map(node -> JcrAccessControlUtil.getAllPrivileges(node).keySet())
                            .orElse(Collections.emptySet());
        }
        
        while (results.size() > 0 && itr.hasNext()) {
            Set<Principal> principals = findActionNode(itr.next())
                            .map(node -> JcrAccessControlUtil.getAllPrivileges(node).keySet())
                            .orElse(Collections.emptySet());
            results.retainAll(principals);
        }
        
        return results;
    }

    @Override
    public Set<Principal> getPrincipalsAllowedAny(Action action, Action... more) {
        return getPrincipalsAllowedAny(Stream.concat(Stream.of(action), Stream.of(more)).collect(Collectors.toSet()));
    }

    @Override
    public Set<Principal> getPrincipalsAllowedAny(Set<Action> actions) {
        return actions.stream()
            .flatMap(action -> 
                findActionNode(action)
                            .map(node -> JcrAccessControlUtil.getAllPrivileges(node).keySet())
                            .orElse(Collections.emptySet())
                            .stream())
            .collect(Collectors.toSet());
    }
    
    public Stream<Action> streamActions() {
        return getAvailableActions().stream()
                        .map(JcrAllowableAction.class::cast)
                        .flatMap(a -> a.stream());
    }

    public Stream<Action> streamLeafActions() {
        return getAvailableActions().stream()
                        .map(JcrAllowableAction.class::cast)
                        .flatMap(a -> a.streamLeafActions());
    }
    
    protected Set<Action> getEnabledActions(Principal principal) {
        return getAvailableActions().stream()
            .flatMap(avail -> avail.stream())
            .filter(action -> JcrAccessControlUtil.hasAnyPermission(((JcrAllowableAction) action).getNode(), principal, Privilege.JCR_READ, Privilege.JCR_ALL))
            .collect(Collectors.toSet());
    }

    private Node copyAction(Node src, Node destParent, boolean includeDescr, Principal principal, String... privilegeNames) throws RepositoryException {
        Node dest = JcrUtil.getOrCreateNode(destParent, src.getName(), JcrAllowableAction.NODE_TYPE);

        if (includeDescr) {
            JcrPropertyUtil.copyProperty(src, dest, JcrPropertyConstants.TITLE);
            JcrPropertyUtil.copyProperty(src, dest, JcrPropertyConstants.DESCRIPTION);
        }

        JcrAccessControlUtil.addPermissions(dest, principal, privilegeNames);

        for (Node child : JcrUtil.getNodesOfType(src, JcrAllowableAction.NODE_TYPE)) {
            copyAction(child, dest, includeDescr, principal, privilegeNames);
        }

        return dest;
    }

    private boolean togglePermission(Iterable<Action> actions, Principal principal, boolean enable) {
        boolean result = false;

        for (Action action : actions) {
            result |= togglePermission(action, principal, enable);
        }

        return result;
    }

    private boolean togglePermission(Action action, Principal principal, boolean enable) {
        boolean isAdminAction = isAdminAction(action);
        boolean result = true;

        if (isAdminAction) {
            if (enable) {
                // If this actions is a permission management action then grant this principal admin privileges to the whole tree.
                result = JcrAccessControlUtil.addRecursivePermissions(getNode(), JcrAllowableAction.NODE_TYPE, principal, ADMIN_PRIVILEGES);
            } else {
                // Remove admin privileges but keep grant privileges if needed
                isAdminAction = getEnabledActions(principal).stream().allMatch(action::equals);  // has non-admin action remaining?
                final String[] privileges = isAdminAction ? ADMIN_PRIVILEGES : ArrayUtils.removeElements(ADMIN_PRIVILEGES, GRANT_PRIVILEGES);
                result = JcrAccessControlUtil.removeRecursivePermissions(getNode(), JcrAllowableAction.NODE_TYPE, principal, privileges);
                JcrAccessControlUtil.removePermissions(getNode(), principal, privileges);
            }
        }
        if (!isAdminAction) {
            result &= findActionNode(action)
                .map(node -> {
                    if (enable) {
                        return JcrAccessControlUtil.addHierarchyPermissions(node, principal, this.node, GRANT_PRIVILEGES);
                    } else {
                        return JcrAccessControlUtil.removeRecursivePermissions(node, JcrAllowableAction.NODE_TYPE, principal, GRANT_PRIVILEGES);
                    }
                })
                .orElseThrow(() -> new AccessControlException("Not authorized to " + (enable ? "enable" : "disable") + " the action: " + action));
        }

        return result;
    }

    /**
     * return true if the specified action represents the ability to manage the permissions
     * of this object on behalf of other principals.  The default is false.  Subclasses
     * should override this method to indicate which of their specific actions are
     * considered admin actions.
     */
    protected boolean isAdminAction(Action action) {
        // Assume false
        return false;
    }

    private Optional<Node> findActionNode(Action action) {
        Node current = getNode();

        for (Action pathAction : action.getHierarchy()) {
            if (JcrUtil.hasNode(current, pathAction.getSystemName())) {
                current = JcrUtil.getNode(current, pathAction.getSystemName());
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(current);
    }
}
