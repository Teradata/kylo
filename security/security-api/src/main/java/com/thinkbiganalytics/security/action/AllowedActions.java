/**
 * 
 */
package com.thinkbiganalytics.security.action;

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

import java.security.Principal;
import java.util.List;
import java.util.Set;

/**
 * Represents a set of actions that may be allowed or disallowed for a user or group.
 * It also supports checking whether certain actions are permitted based on the principals
 * of the active security context.
 * @author Sean Felten
 */
public interface AllowedActions {

    /**
     * Retrieves the set permitted actions available based on the current security context in effect.
     * @return the set of allowed actions
     */
    List<AllowableAction> getAvailableActions();
    
    void checkPermission(Set<Action> actions);
    
    void checkPermission(Action action, Action... more);
    
    boolean enable(Principal principal, Action action, Action... more);
    
    boolean enable(Principal principal, Set<Action> actions);
    
    boolean enableOnly(Principal principal, Action action, Action... more);
    
    boolean enableOnly(Principal principal, Set<Action> actions);
    
    boolean disable(Principal principal, Action action, Action... more);
    
    boolean disable(Principal principal, Set<Action> actions);
}
