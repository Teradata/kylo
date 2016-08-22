/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.action;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.common.JcrPropertyConstants;
import com.thinkbiganalytics.metadata.modeshape.support.JcrPropertyUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowableAction;

/**
 *
 * @author Sean Felten
 */
public class JcrAllowableAction extends JcrObject implements AllowableAction {
    
    public static final String ALLOWABLE_ACTION = "tba:allowableAction";

    public JcrAllowableAction(Node node) {
        super(node);
    }
    
    @Override
    public List<Action> getHierarchy() {
        try {
            List<Action> list = new ArrayList<>();
            Node current = getNode();
            
            while (current.getPrimaryNodeType().isNodeType(ALLOWABLE_ACTION)) {
                list.add(0, JcrUtil.createJcrObject(current, JcrAllowableAction.class));
            }
            
            return list;
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to derive the perentage of action node: " + getNode(), e);
        }
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
    public List<AllowableAction> getSubActions() {
        return JcrUtil.getJcrObjects(this.node, JcrAllowableAction.class).stream().collect(Collectors.toList());
    }
}
