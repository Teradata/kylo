/**
 * 
 */
package com.thinkbiganalytics.security.action.config;

import com.thinkbiganalytics.security.action.Action;

/**
 *
 * @author Sean Felten
 */
public interface ActionsTreeBuilder<P> {

    ActionsTreeBuilder<P> action(Action action);
    
    ActionBuilder<ActionsTreeBuilder<P>> action(String systemName);

    P add();
}
