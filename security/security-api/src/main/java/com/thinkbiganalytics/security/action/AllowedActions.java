/**
 * 
 */
package com.thinkbiganalytics.security.action;

import java.security.Principal;
import java.util.List;
import java.util.Set;

/**
 * Represents a set of actions that may be allowed or disallowed for a user or group.
 * It also supports checking whether certain actions are permitted based on the principals
 * of the active security context.
 * @author Sean Felten
 */
public interface AllowedActions {

    /**
     * Retrieves the set permitted actions available based on the current security context in effect.
     * @return the set of allowed actions
     */
    List<AllowableAction> getAvailableActions();
    
    void checkPermission(Set<Action> actions);
    
    void checkPermission(Action action, Action... more);
    
    boolean enable(Principal principal, Action action, Action... more);
    
    boolean enable(Principal principal, Set<Action> actions);
    
    boolean enableOnly(Principal principal, Action action, Action... more);
    
    boolean enableOnly(Principal principal, Set<Action> actions);
    
    boolean disable(Principal principal, Action action, Action... more);
    
    boolean disable(Principal principal, Set<Action> actions);
}
