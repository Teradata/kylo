/**
 * 
 */
package com.thinkbiganalytics.metadata.modeshape.security;

import java.security.AccessControlException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.thinkbiganalytics.metadata.api.MetadataAccess;
import com.thinkbiganalytics.security.AccessController;
import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowedModuleActionsProvider;

/**
 *
 * @author Sean Felten
 */
public class DefaultAccessController implements AccessController {
    
    @Inject
    private MetadataAccess metadata;
    
    @Inject
    private AllowedModuleActionsProvider actionsProvider;

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.AccessController#checkPermission(java.lang.String, com.thinkbiganalytics.security.action.Action, com.thinkbiganalytics.security.action.Action[])
     */
    @Override
    public void checkPermission(String moduleName, Action action, Action... others) {
        checkPermission(moduleName, Stream.concat(Stream.of(action), 
                                                  Arrays.stream(others)).collect(Collectors.toSet()));
    }
    

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.AccessController#checkPermission(java.lang.String, java.util.Set)
     */
    @Override
    public void checkPermission(String moduleName, Set<Action> actions) {
        this.metadata.read(() -> {
            return this.actionsProvider.getAllowedActions(moduleName)
                .map((allowed) -> {
                        allowed.checkPermission(actions);
                        return moduleName;
                    })
                .<AccessControlException>orElseThrow(() -> new AccessControlException("No actions are defined for the module named: " + moduleName));
        });
    }

}
