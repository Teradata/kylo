/**
 * 
 */
package com.thinkbiganalytics.security.action.config;

/**
 *
 * @author Sean Felten
 */
public interface ActionsTreeBuilder<P> {

    ActionBuilder<ActionsTreeBuilder<P>> action(String systemName);

    P add();
}
