/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.action;

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

/**
 *
 * @author Sean Felten
 */
public class JcrAllowedActions extends JcrObject implements AllowedActions {
    
    public JcrAllowedActions(Node allowedActionsNode) {
        super(allowedActionsNode);
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#getAvailableActions()
     */
    @Override
    public List<AllowableAction> getAvailableActions() {
        try {
            NodeType type = JcrUtil.getNodeType(JcrMetadataAccess.getActiveSession(), JcrAllowableAction.ALLOWABLE_ACTION);
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

    @Override
    public void checkPermission(Action action, Action... more) {
        Set<Action> actions = new HashSet<>(Arrays.asList(more));
        actions.add(action);
        checkPermission(actions);
    }
    
    @Override
    public void checkPermission(Set<Action> actions) {
        Node current = getNode();
        
        for (Action action : actions) {
            for (Action parent : action.getHierarchy()) {
                if (! JcrUtil.hasNode(current, parent.getSystemName())) {
                    throw new AccessControlException("Not authorized to perform the action: " + action.getSystemName());
                }
                
                current = JcrUtil.getNode(current, parent.getSystemName());
            }
        }
    }
    
    public JcrAllowedActions copy(Node allowedNode, JcrAllowedActions src, Principal principal, String... privilegeNames) {
        try {
            for (Node actionNode : JcrUtil.getNodesOfType(src.getNode(), JcrAllowableAction.ALLOWABLE_ACTION)) {
                copyAction(actionNode, allowedNode, principal, privilegeNames);
            }
            
            return new JcrAllowedActions(allowedNode);
        } catch (RepositoryException e) {
            throw new MetadataException("Failed to copy allowed actions", e);
        }
    }

    private Node copyAction(Node src, Node destParent, Principal principal, String... privilegeNames) throws RepositoryException {
        Node dest = JcrUtil.getOrCreateNode(destParent, src.getName(), JcrAllowableAction.ALLOWABLE_ACTION);
        JcrPropertyUtil.copyProperty(src, dest, JcrPropertyConstants.TITLE);
        JcrPropertyUtil.copyProperty(src, dest, JcrPropertyConstants.DESCRIPTION);
        JcrAccessControlUtil.addPermissions(dest, principal, privilegeNames);
        
        for (Node child : JcrUtil.getNodesOfType(src, JcrAllowableAction.ALLOWABLE_ACTION)) {
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
                                return JcrAccessControlUtil.removeHierarchyPermissions(node, principal, this.node, Privilege.JCR_READ);
                            }
                        })
                    .orElseThrow(() -> new AccessControlException("Not authorized to " + (add ? "endable" : "disable") + " the action: " + action));
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
