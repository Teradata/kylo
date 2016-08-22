/**
 * 
 */
package com.thinkbiganalytics.security.action;

import java.util.List;

/**
 * Describes an action that may be authorized for a user or group.
 * @author Sean Felten
 */
public interface AllowableAction extends Action {
    
    String getTitle();
    
    String getDescription();

    List<AllowableAction> getSubActions();
}
