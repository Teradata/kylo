/**
 * 
 */
package com.thinkbiganalytics.security.role;

/*-
 * #%L
 * kylo-security-api
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowableAction;
import com.thinkbiganalytics.security.action.AllowedActions;

/**
 * An immutable implementation of AllowedActions suitable for making a snapshot of another
 * AllowedActions.  This implementation does not support changing any permissions of 
 * its contained AllowableActions.
 */
public class ImmutableAllowedActions implements AllowedActions {
    
    private final List<AllowableAction> availableActions;
    
    public ImmutableAllowedActions(final AllowedActions allowed) {
        List<AllowableAction> newActions = allowed.getAvailableActions().stream()
                        .map(action -> new ImmutableAllowableAction(allowed, action))
                        .collect(Collectors.toList());
        this.availableActions = Collections.unmodifiableList(newActions);
    }
    

    @Override
    public Set<Principal> getPrincipalsAllowedAll(Action action, Action... more) {
        return getPrincipalsAllowedAll(Stream.concat(Stream.of(action), Stream.of(more)).collect(Collectors.toSet()));
    }

    @Override
    public Set<Principal> getPrincipalsAllowedAll(Set<Action> actions) {
        Set<Principal> results = Collections.emptySet();
        this.availableActions.stream()
            .filter(actions::contains)
            .findAny()
            .map(ImmutableAllowableAction.class::cast)
            .ifPresent(ia -> results.addAll(ia.getPrincipals()));
        this.availableActions.stream()
            .filter(actions::contains)
            .map(ImmutableAllowableAction.class::cast)
            .forEach(ia -> results.retainAll(ia.getPrincipals()));
        return results;
    }

    @Override
    public Set<Principal> getPrincipalsAllowedAny(Action action, Action... more) {
        return getPrincipalsAllowedAny(Stream.concat(Stream.of(action), Stream.of(more)).collect(Collectors.toSet()));
    }

    @Override
    public Set<Principal> getPrincipalsAllowedAny(Set<Action> actions) {
        return this.availableActions.stream()
                        .filter(actions::contains)
                        .map(ImmutableAllowableAction.class::cast)
                        .flatMap(ia -> ia.getPrincipals().stream())
                        .collect(Collectors.toSet());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#getAvailableActions()
     */
    @Override
    public List<AllowableAction> getAvailableActions() {
        return availableActions;
    }

    @Override
    public boolean hasPermission(Action action, Action... more) {
        return  this.availableActions.stream()
            .anyMatch(a -> Stream.concat(Stream.of(action), Arrays.stream(more)).anyMatch(b -> a.implies(b)));
    }

    /* (non-Javadoc)
         * @see com.thinkbiganalytics.security.action.AllowedActions#checkPermission(java.util.Set)
         */
    @Override
    public void checkPermission(Set<Action> actions) {
        this.availableActions.stream()
            .anyMatch(a -> actions.stream().anyMatch(b -> a.implies(b)));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#checkPermission(com.thinkbiganalytics.security.action.Action, com.thinkbiganalytics.security.action.Action[])
     */
    @Override
    public void checkPermission(Action action, Action... more) {
        this.availableActions.stream()
        .anyMatch(a -> Stream.concat(Stream.of(action), Arrays.stream(more)).anyMatch(b -> a.implies(b)));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#enable(java.security.Principal, com.thinkbiganalytics.security.action.Action, com.thinkbiganalytics.security.action.Action[])
     */
    @Override
    public boolean enable(Principal principal, Action action, Action... more) {
        throw new UnsupportedOperationException("Changing permissions is not supported by this type of " + AllowedActions.class.getSimpleName());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#enable(java.security.Principal, java.util.Set)
     */
    @Override
    public boolean enable(Principal principal, Set<Action> actions) {
        throw new UnsupportedOperationException("Changing permissions is not supported by this type of " + AllowedActions.class.getSimpleName());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#enableOnly(java.security.Principal, com.thinkbiganalytics.security.action.Action, com.thinkbiganalytics.security.action.Action[])
     */
    @Override
    public boolean enableOnly(Principal principal, Action action, Action... more) {
        throw new UnsupportedOperationException("Changing permissions is not supported by this type of " + AllowedActions.class.getSimpleName());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#enableOnly(java.security.Principal, java.util.Set)
     */
    @Override
    public boolean enableOnly(Principal principal, Set<Action> actions) {
        throw new UnsupportedOperationException("Changing permissions is not supported by this type of " + AllowedActions.class.getSimpleName());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#enableOnly(java.security.Principal, com.thinkbiganalytics.security.action.AllowedActions)
     */
    @Override
    public boolean enableOnly(Principal principal, AllowedActions actions) {
        throw new UnsupportedOperationException("Changing permissions is not supported by this type of " + AllowedActions.class.getSimpleName());
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#enableAll(java.security.Principal)
     */
    @Override
    public boolean enableAll(Principal principal) {
        throw new UnsupportedOperationException("Changing permissions is not supported by this type of " + AllowedActions.class.getSimpleName());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#disable(java.security.Principal, com.thinkbiganalytics.security.action.Action, com.thinkbiganalytics.security.action.Action[])
     */
    @Override
    public boolean disable(Principal principal, Action action, Action... more) {
        throw new UnsupportedOperationException("Changing permissions is not supported by this type of " + AllowedActions.class.getSimpleName());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#disable(java.security.Principal, java.util.Set)
     */
    @Override
    public boolean disable(Principal principal, Set<Action> actions) {
        throw new UnsupportedOperationException("Changing permissions is not supported by this type of " + AllowedActions.class.getSimpleName());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#disable(java.security.Principal, com.thinkbiganalytics.security.action.AllowedActions)
     */
    @Override
    public boolean disable(Principal principal, AllowedActions actions) {
        throw new UnsupportedOperationException("Changing permissions is not supported by this type of " + AllowedActions.class.getSimpleName());
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#deisableAll(java.security.Principal)
     */
    @Override
    public boolean disableAll(Principal principal) {
        throw new UnsupportedOperationException("Changing permissions is not supported by this type of " + AllowedActions.class.getSimpleName());
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.availableActions.toString();
    }
}
