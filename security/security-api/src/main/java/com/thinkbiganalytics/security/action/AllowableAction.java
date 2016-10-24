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

    /**
     * @return the set of direct sub-actions defined below this action
     */
    List<AllowableAction> getSubActions();
    
    /**
     * Streams the full sub-action tree defined below this action 
     * using pre-order traversal.
     * .
     * @return a stream of sub-actions of this action
     */
    Stream<AllowableAction> stream();
}
