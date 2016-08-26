/**
 * 
 */
package com.thinkbiganalytics.security.action;

import java.util.List;
import java.util.stream.Stream;

/**
 * Describes an action that may be authorized for a user or group.
 * @author Sean Felten
 */
public interface AllowableAction extends Action {
    
    String getTitle();
    
    String getDescription();

    List<AllowableAction> getSubActions();
    
    Stream<AllowableAction> stream();
}
