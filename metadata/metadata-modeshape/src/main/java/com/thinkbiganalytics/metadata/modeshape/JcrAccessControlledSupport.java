/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape;

import java.nio.file.attribute.UserPrincipal;

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.api.security.AccessControlled;
import com.thinkbiganalytics.metadata.api.security.RoleAssignments;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.security.action.JcrAllowedActions;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.AllowedActions;

/**
 * Support for JcrObjects that implement AccessControlled and whose JCR type uses the mix:accessControlled mixin.
 */
public class JcrAccessControlledSupport extends JcrObject implements AccessControlled {
    
    /**
     * @param node
     */
    public JcrAccessControlledSupport(Node node) {
        super(node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.AccessControlled#getRoleAssignments()
     */
    @Override
    public RoleAssignments getRoleAssignments() {
//        return JcrUtil.getPropertyObjectSet(node, JcrRoleAssignments.NODE_NAME, JcrRoleAssignments.class).stream()
//                        .map(RoleAssignments.class::cast)
//                        .collect(Collectors.toSet());
        return null;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.metadata.api.security.AccessControlled#getAllowedActions()
     */
    @Override
    public AllowedActions getAllowedActions() {
        Node allowedNode = JcrUtil.getNode(node, JcrAllowedActions.NODE_NAME);
        return JcrUtil.createJcrObject(allowedNode, JcrAllowedActions.class);
    }

    
    public void initializeAction(JcrAllowedActions prototype, UserPrincipal user) {
        
    }
}
