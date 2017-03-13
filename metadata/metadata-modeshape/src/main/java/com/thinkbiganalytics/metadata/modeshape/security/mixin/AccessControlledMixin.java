/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.mixin;

import javax.jcr.Node;
import javax.jcr.security.Privilege;

import com.thinkbiganalytics.metadata.api.security.AccessControlled;
import com.thinkbiganalytics.metadata.api.security.RoleAssignments;
import com.thinkbiganalytics.metadata.modeshape.common.mixin.NodeEntityMixin;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.UsernamePrincipal;
import com.thinkbiganalytics.security.action.AllowedActions;

/**
 *
 */
public interface AccessControlledMixin extends AccessControlled, NodeEntityMixin {
    
    default RoleAssignments getRoleAssignments() {
//      return JcrUtil.getPropertyObjectSet(node, JcrRoleAssignments.NODE_NAME, JcrRoleAssignments.class).stream()
//                      .map(RoleAssignments.class::cast)
//                      .collect(Collectors.toSet());
      return null;
    }
    
    @Override
    default AllowedActions getAllowedActions() {
        return getJcrAllowedActions();
    }

    default JcrAllowedActions getJcrAllowedActions() {
        Node allowedNode = JcrUtil.getNode(getNode(), JcrAllowedActions.NODE_NAME);
        return JcrUtil.createJcrObject(allowedNode, getJcrAllowedActionsType());
    }

    default void setupAccessControl(JcrAllowedActions prototype, UsernamePrincipal owner) {
        JcrAllowedActions allowed = getJcrAllowedActions();
        allowed = prototype.copy(allowed.getNode(), owner, Privilege.JCR_ALL);
        allowed.setupAccessControl(owner);
    }

    Class<? extends JcrAllowedActions> getJcrAllowedActionsType();
}
