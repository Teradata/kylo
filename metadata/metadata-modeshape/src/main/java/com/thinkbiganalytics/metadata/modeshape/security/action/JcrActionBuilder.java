/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.action;

import javax.jcr.Node;
import javax.jcr.security.Privilege;

import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertyConstants;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.config.ActionBuilder;

/**
 *
 * @author Sean Felten
 */
public class JcrActionBuilder<P> extends JcrAbstractActionsBuilder implements ActionBuilder<P> {
    
    private Node actionNode;
    private P parentBuilder;
    
    public JcrActionBuilder(Node node, P parent) {
        this.actionNode = node;
        this.parentBuilder = parent;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.config.ActionsTreeBuilder.ActionBuilder#title(java.lang.String)
     */
    @Override
    public ActionBuilder<P> title(String name) {
        JcrPropertyUtil.setProperty(this.actionNode, JcrPropertyConstants.TITLE, name);
        return this;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.config.ActionsTreeBuilder.ActionBuilder#description(java.lang.String)
     */
    @Override
    public ActionBuilder<P> description(String descr) {
        JcrPropertyUtil.setProperty(this.actionNode, JcrPropertyConstants.DESCRIPTION, descr);
        return this;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.config.ActionsTreeBuilder.ActionBuilder#subAction(java.lang.String)
     */
    @Override
    public ActionBuilder<ActionBuilder<P>> subAction(String name) {
        Node actionNode = JcrUtil.getOrCreateNode(this.actionNode, name, JcrAllowableAction.ALLOWABLE_ACTION);
        return new JcrActionBuilder<>(actionNode, this);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.config.ActionsTreeBuilder.ActionBuilder#add()
     */
    @Override
    public P add() {
//        JcrAccessControlUtil.addPermissions(this.actionNode, getManagementPrincipal(), Privilege.JCR_ALL);
        return this.parentBuilder;
    }

}
