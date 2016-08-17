/**
 * 
 */
package com.thinkbiganalytics.security.action.config;

import com.thinkbiganalytics.security.action.AllowedActions;

/**
 *
 * @author Sean Felten
 */
public interface ActionGroupsBuilder {

    ActionsTreeBuilder<ActionGroupsBuilder> group(String name);
    
    AllowedActions build();
}
