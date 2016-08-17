/**
 * 
 */
package com.thinkbiganalytics.security.action.config;

/**
 *
 * @author Sean Felten
 */
public interface ActionBuilder<P> {

    ActionBuilder<P> title(String name);

    ActionBuilder<P> description(String name);

    ActionBuilder<ActionBuilder<P>> subAction(String name);

    P add();
}
