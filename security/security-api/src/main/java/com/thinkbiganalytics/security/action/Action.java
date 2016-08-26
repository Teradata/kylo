/**
 * 
 */
package com.thinkbiganalytics.security.action;

import java.util.Arrays;
import java.util.List;

/**
 * Identifies an action that that may be checked if permitted. 
 *
 * @author Sean Felten
 */
public interface Action {
    
    String getSystemName();

    List<Action> getHierarchy();
    
    default boolean implies(Action action) {
        return getHierarchy().stream().anyMatch(a -> a.equals(action));
    }
    
    default Action subAction(String name) {
        return create(name, this);
    }
    
    static Action create(String name, Action... parents) {
        return new ImmutableAction(name, Arrays.asList(parents));
    }
}
