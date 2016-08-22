/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.action;

import java.security.AccessControlException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.security.Privilege;
import javax.jcr.version.VersionException;

import com.thinkbiganalytics.metadata.api.MetadataException;
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
            return JcrUtil.getJcrObjects(this.node, JcrAllowableAction.class).stream().collect(Collectors.toList());
        } catch (Exception e) {
            throw new MetadataException("Failed to retrieve the accessible functons", e);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#enableAccess(com.thinkbiganalytics.security.action.Action, java.security.Principal[])
     */
    @Override
    public boolean enable(Action action, Principal... principals) {
        return enable(action, Arrays.stream(principals).collect(Collectors.toSet()));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#enableAccess(com.thinkbiganalytics.security.action.Action, java.util.Set)
     */
    @Override
    public boolean enable(Action action, Set<Principal> principals) {
        return togglePermission(action, principals, true);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#disableAccess(com.thinkbiganalytics.security.action.Action, java.security.Principal[])
     */
    @Override
    public boolean disable(Action action, Principal... principals) {
        return disable(action, Arrays.stream(principals).collect(Collectors.toSet()));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#disableAccess(com.thinkbiganalytics.security.action.Action, java.util.Set)
     */
    @Override
    public boolean disable(Action action, Set<Principal> principals) {
        return togglePermission(action, principals, false);
    }

    @Override
    public void checkPermission(Action action) {
        Node current = getNode();
        
        for (Action pathAction : action.getHierarchy()) {
            if (! JcrUtil.hasNode(current, pathAction.getSystemName())) {
                throw new AccessControlException("Not authorized to perform the action: " + action.getSystemName());
            }
            
            current = JcrUtil.getNode(current, pathAction.getSystemName());
        }
    }
    
    public JcrAllowedActions copy(Node allowedNode, JcrAllowedActions src) {
        try {
            for (Node actionNode : JcrUtil.getNodesOfType(src.getNode(), JcrAllowableAction.ALLOWABLE_ACTION)) {
                copyAction(actionNode, allowedNode);
            }
            
            return new JcrAllowedActions(allowedNode);
        } catch (RepositoryException e) {
            throw new MetadataException("Failed to copy allowed actions", e);
        }
    }

    private Node copyAction(Node src, Node destParent) throws RepositoryException {
        Node dest = destParent.addNode(src.getName(), JcrAllowableAction.ALLOWABLE_ACTION);
        JcrPropertyUtil.copyProperty(src, dest, JcrPropertyConstants.TITLE);
        JcrPropertyUtil.copyProperty(src, dest, JcrPropertyConstants.DESCRIPTION);
        
        for (Node child : JcrUtil.getNodesOfType(src, JcrAllowableAction.ALLOWABLE_ACTION)) {
            copyAction(child, dest);
        }
        
        return dest;
    }

    private boolean togglePermission(Action action, Set<Principal> principals, boolean add) {
        return findActionNode(action)
                .map(node -> {
                        boolean changed = false;
                        
                        for (Principal principal : principals) {
                            if (add) {
                                changed |= JcrAccessControlUtil.addHierarchyPermissions(node, principal, this.node, Privilege.JCR_READ);
                            } else {
                                changed |= JcrAccessControlUtil.removeHierarchyPermissions(node, principal, this.node, Privilege.JCR_READ);
                            }
                        }
                        
                        return changed;
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
