/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.function;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.Privilege;

import com.thinkbiganalytics.metadata.api.MetadataException;
import com.thinkbiganalytics.metadata.modeshape.common.JcrObject;
import com.thinkbiganalytics.metadata.modeshape.security.JcrAccessControlUtil;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.security.action.AllowableAction;
import com.thinkbiganalytics.security.action.AllowedActions;

/**
 *
 * @author Sean Felten
 */
public abstract class JcrAllowedActions extends JcrObject implements AllowedActions {
    
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
     * @see com.thinkbiganalytics.security.action.AllowedActions#enableAccess(com.thinkbiganalytics.security.action.AllowableAction, java.security.Principal[])
     */
    @Override
    public boolean enable(AllowableAction action, Principal... principals) {
        return enable(action, Arrays.stream(principals).collect(Collectors.toSet()));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#enableAccess(com.thinkbiganalytics.security.action.AllowableAction, java.util.Set)
     */
    @Override
    public boolean enable(AllowableAction action, Set<Principal> principals) {
        JcrAllowableAction jcrAction = (JcrAllowableAction) action;
        Node functNode = jcrAction.getNode();
        boolean adds = false;
        
        for (Principal principal : principals) {
            adds |= JcrAccessControlUtil.addHierarchyPermissions(functNode, principal, this.node, Privilege.JCR_READ);
        }
        
        return adds;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#disableAccess(com.thinkbiganalytics.security.action.AllowableAction, java.security.Principal[])
     */
    @Override
    public boolean disable(AllowableAction action, Principal... principals) {
        return disable(action, Arrays.stream(principals).collect(Collectors.toSet()));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#disableAccess(com.thinkbiganalytics.security.action.AllowableAction, java.util.Set)
     */
    @Override
    public boolean disable(AllowableAction action, Set<Principal> principals) {
        JcrAllowableAction jcrAction = (JcrAllowableAction) action;
        Node functNode = jcrAction.getNode();
        boolean removes = false;
        
        for (Principal principal : principals) {
            removes |= JcrAccessControlUtil.removeHierarchyPermissions(functNode, principal, this.node, Privilege.JCR_READ);
        }
        
        return removes;
    }
    
    @Override
    public void checkPermission(AllowableAction action, Principal... principals) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void checkPermission(String actionPath, Principal... principals) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void checkPermission(AllowableAction action) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void checkPermission(String actionPath) {
        // TODO Auto-generated method stub
        
    }

    public List<JcrAllowableAction> initializeActions(Session session) {
        List<JcrAllowableAction> roots = new ArrayList<>();
        
        roots.add(addAction(session, AUTH_MGT, "Authentication Management", "The ability to manage autorization of allowable applcation actions"));
        roots.add(addAction(session, AUTH_MGT, "Authentication Management", "The ability to manage autorization of allowable applcation actions"));
        roots.add(addAction(session, AUTH_MGT, "Authentication Management", "The ability to manage autorization of allowable applcation actions"));
        roots.add(addAction(session, AUTH_MGT, "Authentication Management", "The ability to manage autorization of allowable applcation actions"));
        
        return roots;
    }

    
    protected JcrAllowableAction addAction(Session session, Path path, String title, String descr) {
        try {
            Node funcRootNode = session.getRootNode().getNode(FUNCTIONS_ROOT.toString());
    
            // TODO Auto-generated method stub
            return null;
        } catch (RepositoryException e) {
            throw new MetadataException("Failed to build allowable actions tree", e);
        }
    }


    public static final Path FUNCTIONS_ROOT = Paths.get("allowableActions");
    public static final Path AUTH_MGT = FUNCTIONS_ROOT.resolve(Paths.get("authMgmt"));
}
