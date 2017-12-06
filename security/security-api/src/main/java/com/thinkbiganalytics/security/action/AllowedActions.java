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

import java.security.AccessControlException;
import java.security.Principal;
import java.util.List;
import java.util.Set;

/**
 * Represents a set of actions that may be allowed or disallowed for a user or group.
 * It also supports checking whether certain actions are permitted based on the principals
 * of the active security context.
 */
public interface AllowedActions {

    // TODO find a better place to define the types of entities that can have associated actions.
    String SERVICES = "services";
    String FEED = "feed";
    String CATEGORY = "category";
    String TEMPLATE = "template";
    String DATASOURCE = "datasource";

    
    /**
     * Retrieves the hierarchical set of allowable actions.
     *
     * @return the set of allowed actions
     */
    List<AllowableAction> getAvailableActions();

    /**
     * validate a user has a given permission(s)
     * @param action the action to check
     * @param more additional actions to check
     * @return true if user has the permission(s), false if not
     */
    boolean hasPermission(Action action, Action... more);

    /**
     * Checks whether the given actions are implied by this set of actions based on the current
     * security context, i.e. the principals associates with the current user executing the current thread.
     *
     * @param actions the actions to check
     * @throws AccessControlException thrown if any of the actions being checked are not present
     */
    void checkPermission(Set<Action> actions);

    /**
     * Checks whether the given actions are implied by this set of actions based on the current
     * security context, i.e. the principals associates with the current user executing the current thread.
     *
     * @param action the actions to check
     * @throws AccessControlException thrown if any of the actions being checked are not present
     */
    void checkPermission(Action action, Action... more);

    /**
     * Updates this object to enable the given action(s) for the specified principal.
     *
     * @param principal the principal to which the action(s) are granted
     * @param action    an action to grant
     * @param more      optional additional actions to grant
     * @return true if actions had not already been granted to that principal, otherwise false.
     */
    boolean enable(Principal principal, Action action, Action... more);

    /**
     * Updates this object to enable the given action(s) for the specified principal.
     *
     * @param principal the principal to which the action(s) are granted
     * @param actions   the set of actions to grant
     * @return true if actions had not already been granted to that principal, otherwise false.
     */
    boolean enable(Principal principal, Set<Action> actions);

    /**
     * Updates this object to enable only the given action(s) for the specified principal and disabling any others
     * the principal may have had enabled.
     *
     * @param principal the principal to which the action(s) are granted
     * @param action    an action to grant
     * @param more      optional additional actions to grant
     * @return true if actions had not already been granted to that principal, otherwise false.
     */
    boolean enableOnly(Principal principal, Action action, Action... more);

    /**
     * Updates this object to enable only the given action(s) for the specified principal and disabling any others
     * the principal may have had enabled.
     *
     * @param principal the principal to which the action(s) are granted
     * @param actions   the set of actions to grant
     * @return true if actions had not already been granted to that principal, otherwise false.
     */
    boolean enableOnly(Principal principal, Set<Action> actions);

    /**
     * Updates this object to match the given AllowedActions for the principal.
     *
     * @param principal the principal to which the action(s) are granted
     * @param actions   the set of actions to grant
     * @return true if actions had not already been granted to that principal, otherwise false.
     */
    boolean enableOnly(Principal principal, AllowedActions actions);

    /**
     * Enables all actions for the specified principals.
     *
     * @param principal the principal to which the actions are granted
     * @return true if not all actions had already been granted to that principal, otherwise false.
     */
    boolean enableAll(Principal principal);

    /**
     * Updates this object to disable the given action(s) for the specified principal.
     *
     * @param principal the principal to which the action(s) are revoked
     * @param action    an action to revoke
     * @param more      optional additional actions to revoke
     * @return true if actions at least 1 action has been revoked for that principal, otherwise false.
     */
    boolean disable(Principal principal, Action action, Action... more);

    /**
     * Updates this object to disable the given action(s) for the specified principal.
     *
     * @param principal the principal to which the action(s) are revoked
     * @param actions   the set of actions to revoke
     * @return true if actions at least 1 action has been revoked for that principal, otherwise false.
     */
    boolean disable(Principal principal, Set<Action> actions);

    /**
     * Updates this object disable all actions for the principal contained in the given AllowedActions.
     *
     * @param principal the principal to which the action(s) are revoked
     * @param actions   the set of actions to revoke
     * @return true if actions at least 1 action has been revoked for that principal, otherwise false.
     */
    boolean disable(Principal principal, AllowedActions actions);

    /**
     * Disables all actions for the specified principals.
     *
     * @param principal the principal to which the actions are revoked
     * @return true if not all actions had already been revoked from that principal, otherwise false.
     */
    boolean disableAll(Principal principal);

    /**
     * Gets the set of principals that have been granted permission to perform all of the specified action(s).
     * 
     * @param action an action
     * @param more optional list of additional actions
     * @return a set of principals allowed all of the actions
     */
    Set<Principal> getPrincipalsAllowedAll(Action action, Action... more);
    
    /**
     * Gets the set of principals that have been granted permission to perform all of the specified actions.
     * 
     * @param actions the set of actions
     * @return a set of principals allowed all of the actions
     */
    Set<Principal> getPrincipalsAllowedAll(Set<Action> actions);
    
    /**
     * Gets the set of principals that have been granted permission to perform any of the specified action(s).
     * 
     * @param action an action
     * @param more optional additional actions
     * @return a set of principals allowed at least one of the actions
     */
    Set<Principal> getPrincipalsAllowedAny(Action action, Action... more);
    
    /**
     * Gets the set of principals that have been granted permission to perform all of the specified actions.
     * 
     * @param actions the set of actions
     * @return a set of principals allowed all of the actions
     */
    Set<Principal> getPrincipalsAllowedAny(Set<Action> actions);
    
}
