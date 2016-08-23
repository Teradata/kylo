/**
 * 
 */
package com.thinkbiganalytics.security.action.config;

import com.thinkbiganalytics.security.action.Action;

/**
 *
 * @author Sean Felten
 */
public interface ActionBuilder<P> {

    ActionBuilder<P> title(String name);

    ActionBuilder<P> description(String name);
    
    ActionBuilder<ActionBuilder<P>> subAction(Action action);

    ActionBuilder<ActionBuilder<P>> subAction(String name);

    P add();
}
