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

import com.thinkbiganalytics.metadata.api.MetadataException;
import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertyConstants;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowableAction;
import com.thinkbiganalytics.security.action.AllowedActions;

import java.security.AccessControlException;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
            throw new MetadataException("Failed to retrieve the accessible functons", e);
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

    @Override
    public void checkPermission(Action action, Action... more) {
        Set<Action> actions = new HashSet<>(Arrays.asList(more));
        actions.add(action);
        checkPermission(actions);
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
    
    public JcrAllowedActions copy(Node allowedNode, Principal principal, String... privilegeNames) {
        try {
            for (Node actionNode : JcrUtil.getNodesOfType(getNode(), JcrAllowableAction.NODE_TYPE)) {
                copyAction(actionNode, allowedNode, principal, privilegeNames);
            }

            return new JcrAllowedActions(allowedNode);
        } catch (RepositoryException e) {
            throw new MetadataException("Failed to copy allowed actions", e);
        }
    }

    private Node copyAction(Node src, Node destParent, Principal principal, String... privilegeNames) throws RepositoryException {
        Node dest = JcrUtil.getOrCreateNode(destParent, src.getName(), JcrAllowableAction.NODE_TYPE);
        JcrPropertyUtil.copyProperty(src, dest, JcrPropertyConstants.TITLE);
        JcrPropertyUtil.copyProperty(src, dest, JcrPropertyConstants.DESCRIPTION);
        JcrAccessControlUtil.addPermissions(dest, principal, privilegeNames);

        for (Node child : JcrUtil.getNodesOfType(src, JcrAllowableAction.NODE_TYPE)) {
            copyAction(child, dest, principal, privilegeNames);
        }

        return dest;
    }

    private boolean togglePermission(Iterable<Action> actions, Principal principal, boolean add) {
        boolean result = false;

        for (Action action : actions) {
            result |= togglePermission(action, principal, add);
        }

        return result;
    }

    private boolean togglePermission(Action action, Principal principal, boolean add) {
        return findActionNode(action)
            .map(node -> {
                if (add) {
                    return JcrAccessControlUtil.addHierarchyPermissions(node, principal, this.node, Privilege.JCR_READ);
                } else {
                    return JcrAccessControlUtil.removeRecursivePermissions(node, JcrAllowableAction.NODE_TYPE, principal, Privilege.JCR_READ);
                }
            })
            .orElseThrow(() -> new AccessControlException("Not authorized to " + (add ? "enable" : "disable") + " the action: " + action));
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
