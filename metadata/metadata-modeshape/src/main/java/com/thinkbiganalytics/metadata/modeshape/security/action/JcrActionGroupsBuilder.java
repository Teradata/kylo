/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.action;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.Privilege;

import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.config.ActionGroupsBuilder;
import com.thinkbiganalytics.security.action.config.ActionsTreeBuilder;

/**
 *
 * @author Sean Felten
 */
public class JcrActionGroupsBuilder extends JcrAbstractActionsBuilder implements ActionGroupsBuilder {
    
    public static final String ALLOWED_ACTIONS = "tba:allowedActions";
    
    private final String groupsNodePath;
    private Node groupsNode;
    
    
    public JcrActionGroupsBuilder(String path) {
        this.groupsNodePath = path;
    }
    
    public JcrActionGroupsBuilder(Node groupsNode) {
        this((String) null);
        this.groupsNode = groupsNode;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.config.ActionGroupsBuilder#group(java.lang.String)
     */
    @Override
    public ActionsTreeBuilder<ActionGroupsBuilder> group(String name) {
        Session session = JcrMetadataAccess.getActiveSession();
        
        try {
            this.groupsNode = this.groupsNode == null ? session.getRootNode().getNode(this.groupsNodePath) : this.groupsNode;
            Node actionsNode = JcrUtil.getOrCreateNode(groupsNode, name, ALLOWED_ACTIONS);
            
            return new JcrActionTreeBuilder<>(actionsNode, this);
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access root node for allowable actions", e);
        }
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.config.ActionGroupsBuilder#build()
     */
    @Override
    public AllowedActions build() {
        JcrAccessControlUtil.addPermissions(this.groupsNode, this.managementPrincipal, Privilege.JCR_ALL);
        return new JcrAllowedActions(this.groupsNode);
    }

}
