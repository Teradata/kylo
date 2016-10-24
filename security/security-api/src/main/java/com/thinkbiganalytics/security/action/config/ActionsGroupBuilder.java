/**
 * 
 */
package com.thinkbiganalytics.security.action.config;

import com.thinkbiganalytics.security.action.AllowedActions;

/**
 *
 * @author Sean Felten
 */
public interface ActionsGroupBuilder {

    ActionsTreeBuilder<ActionsGroupBuilder> group(String name);
    
    AllowedActions build();
}
