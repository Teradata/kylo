/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.action;

import javax.jcr.Node;
import javax.jcr.security.Privilege;

import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.config.ActionBuilder;
import com.thinkbiganalytics.security.action.config.ModuleActionsBuilder;
import com.thinkbiganalytics.security.action.config.ActionsTreeBuilder;

/**
 *
 * @author Sean Felten
 */
public class JcrActionTreeBuilder<P> extends JcrAbstractActionsBuilder implements ActionsTreeBuilder<P> {
    
    private Node actionsNode;
    private P parentBuilder;

    public JcrActionTreeBuilder(Node node, P parent) {
        this.actionsNode = node;
        this.parentBuilder = parent;
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.config.ActionsTreeBuilder#action(com.thinkbiganalytics.security.action.Action)
     */
    @Override
    public ActionBuilder<ActionsTreeBuilder<P>> action(Action action) {
        // TODO: validate action path
        return action(action.getSystemName());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.config.ActionsTreeBuilder#action(java.lang.String)
     */
    @Override
    public ActionBuilder<ActionsTreeBuilder<P>> action(String systemName) {
        Node actionNode = JcrUtil.getOrCreateNode(this.actionsNode, systemName, JcrAllowableAction.ALLOWABLE_ACTION);
        return new JcrActionBuilder<>(actionNode, this);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.config.ActionsTreeBuilder#add()
     */
    @Override
    public P add() {
//        JcrAccessControlUtil.addPermissions(this.actionsNode, getManagementPrincipal(), Privilege.JCR_ALL);
        return this.parentBuilder;
    }

}
