/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.action;

import java.util.List;
import java.util.stream.Collectors;

import javax.jcr.Node;

import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertyConstants;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.AllowableAction;

/**
 *
 * @author Sean Felten
 */
public class JcrAllowableAction extends JcrObject implements AllowableAction {

    public JcrAllowableAction(Node node) {
        super(node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowableAction#getSystemName()
     */
    @Override
    public String getSystemName() {
        return JcrPropertyUtil.getName(this.node);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowableAction#getTitle()
     */
    @Override
    public String getTitle() {
        return JcrPropertyUtil.getString(node, JcrPropertyConstants.TITLE);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowableAction#getDescription()
     */
    @Override
    public String getDescription() {
        return JcrPropertyUtil.getString(node, JcrPropertyConstants.DESCRIPTION);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowableAction#getSubFunctions()
     */
    @Override
    public List<AllowableAction> getSubFunctions() {
        return JcrUtil.getJcrObjects(this.node, JcrAllowableAction.class).stream().collect(Collectors.toList());
    }

}
