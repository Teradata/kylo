/**
 * 
 */
package com.thinkbiganalytics.security.action;

import java.security.Principal;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Sean Felten
 */
public interface AllowedActions {

    List<AllowableAction> getAvailableActions();
    
    boolean enable(AllowableAction action, Principal... principals);
    
    boolean enable(AllowableAction action, Set<Principal> principals);
    
    boolean disable(AllowableAction action, Principal... principals);
    
    boolean disable(AllowableAction action, Set<Principal> principals);
    
    void checkPermission(AllowableAction action, Principal... principals);
    
    void checkPermission(String actionPath, Principal... principals);
    
    void checkPermission(AllowableAction action);
    
    void checkPermission(String actionPath);
}
