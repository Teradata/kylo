/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security.action;

import java.nio.file.Path;
import java.util.Optional;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.thinkbiganalytics.metadata.modeshape.JcrMetadataAccess;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.common.SecurityPaths;
import com.thinkbiganalytics.security.action.AllowableAction;
import com.thinkbiganalytics.security.action.AllowedActions;
import com.thinkbiganalytics.security.action.AllowedModuleActionsProvider;

/**
 *
 * @author Sean Felten
 */
public class JcrAllowedModuleActionsProvider implements AllowedModuleActionsProvider {

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedModuleActionsProvider#getAvailavleActions(java.lang.String)
     */
    @Override
    public Optional<AllowedActions> getAvailavleActions(String moduleName) {
        Path modulePath = SecurityPaths.prototypeActionsPath(moduleName);
        
        return getActions(moduleName, modulePath);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedModuleActionsProvider#getAllowedActions(java.lang.String)
     */
    @Override
    public Optional<AllowedActions> getAllowedActions(String moduleName) {
        Path modulePath = SecurityPaths.moduleActionPath(moduleName);
        
        return getActions(moduleName, modulePath);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedModuleActionsProvider#checkPermission(java.lang.String, com.thinkbiganalytics.security.action.AllowableAction)
     */
    @Override
    public void checkPermission(String moduleName, AllowableAction action) {
        // TODO Auto-generated method stub

    }

    protected Optional<AllowedActions> getActions(String moduleName, Path modulePath) {
        try {
            Session session = JcrMetadataAccess.getActiveSession();
            
            if (session.getRootNode().hasNode(modulePath.toString())) {
                Node node = session.getRootNode().getNode(modulePath.toString());
                JcrAllowedActions actions = new JcrAllowedActions(node);
                return Optional.of(actions);
            } else {
                return Optional.empty();
            }
        } catch (AccessDeniedException e) { 
            return Optional.empty();
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to access allowable actions for module: " + moduleName, e);
        }
    }

}
