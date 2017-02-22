/**
 * 
 */
package com.thinkbiganalytics.security.role;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowableAction;
import com.thinkbiganalytics.security.action.AllowedActions;

/**
 * An immutable implementation of AllowedActions suitable for making a snapshot of another
 * AllowedActions.  This implementation does not support changing any permissions of for
 * its contained AllowableActions.
 */
public class ImmutableAllowedActions implements AllowedActions {
    
    private final List<AllowableAction> availableActions;
    
    public ImmutableAllowedActions(AllowedActions allowed) {
        this(allowed.getAvailableActions());
    }
    
    public ImmutableAllowedActions(List<AllowableAction> actions) {
        this.availableActions = Collections.unmodifiableList(actions);
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.AllowedActions#getAvailableActions()
     */
    @Override
    public List<AllowableAction> getAvailableActions() {
        return availableActions;
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

}
