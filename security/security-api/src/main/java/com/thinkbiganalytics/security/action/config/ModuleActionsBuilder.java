/**
 * 
 */
package com.thinkbiganalytics.security.action.config;

import com.thinkbiganalytics.security.action.AllowedActions;

/**
 *
 * @author Sean Felten
 */
public interface ModuleActionsBuilder {

    ActionsTreeBuilder<ModuleActionsBuilder> group(String name);
    
    AllowedActions build();
}
