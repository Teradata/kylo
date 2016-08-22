/**
 * 
 */
package com.thinkbiganalytics.security.action;

import java.security.Principal;
import java.util.List;
import java.util.Set;

/**
 * Represents a set of actions that may be allowed or disallowed for a user or group.
 * It also supports checking whether and action is permissible based on the current state
 * of allowed actions.
 * @author Sean Felten
 */
public interface AllowedActions {

    List<AllowableAction> getAvailableActions();
    
    boolean enable(Action action, Principal... principals);
    
    boolean enable(Action action, Set<Principal> principals);
    
    boolean disable(Action action, Principal... principals);
    
    boolean disable(Action action, Set<Principal> principals);
    
//    void checkPermission(Action action, Principal... principals);
    
    void checkPermission(Action action);
}
