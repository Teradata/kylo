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
    
    String getTitle();
    
    String getDescription();
    
    List<Action> getHierarchy();

    
    default boolean implies(Action action) {
        return getHierarchy().stream().anyMatch(a -> a.equals(action));
    }
    
    static Action create(String name, String title, String descr, Action... parents) {
        return new ImmutableAction(name, title, descr, Arrays.asList(parents));
    }
    
    static Action create(String name, Action... parents) {
        return new ImmutableAction(name, name, "", Arrays.asList(parents));
    }
    
    default Action subAction(String name, String title, String descr) {
        return create(name, title, descr, this);
    }
    
    default Action subAction(String name) {
        return create(name, name, "", this);
    }
}
