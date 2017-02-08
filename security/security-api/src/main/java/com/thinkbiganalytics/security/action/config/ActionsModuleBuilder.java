/**
 *
 */
package com.thinkbiganalytics.security.action.config;

/*-
 * #%L
 * thinkbig-security-api
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.thinkbiganalytics.security.action.AllowedActions;

/**
 * A builder used to construct/augment an action hierarchy tree of a module.
 * Used by system components/plugins when they are initialized to augments the set of actions available
 * to users based on that component's access control requirements.
 */
public interface ActionsModuleBuilder {

    /**
     * Starts a new builder to add an action hierarchy to the actions of the module with the given name.
     */
    ActionsTreeBuilder<ActionsModuleBuilder> module(String name);

    /**
     * Builds the action hierarchy tree on the module and returns the resulting AllowedActions.
     */
    AllowedActions build();
}
