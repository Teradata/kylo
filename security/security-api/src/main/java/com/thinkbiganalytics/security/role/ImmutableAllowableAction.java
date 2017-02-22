/**
 * 
 */
package com.thinkbiganalytics.security.role;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.thinkbiganalytics.security.action.Action;
import com.thinkbiganalytics.security.action.AllowableAction;

/**
 * An immutable AllowedAction suitable for creating a snapshot of another AllowableAction tree.
 */
public class ImmutableAllowableAction implements AllowableAction {

    private final Action action;
    private final List<AllowableAction> subactions = new ArrayList<>();
    
    public ImmutableAllowableAction(AllowableAction action) {
        super();
        this.action = new ImmutableAllowableAction(action);
        
        action.getSubActions().forEach(a -> subactions.add(new ImmutableAllowableAction(a)));
    }

    @Override
    public String getSystemName() {
        return this.action.getSystemName();
    }

    @Override
    public String getTitle() {
        return this.action.getTitle();
    }

    @Override
    public String getDescription() {
        return this.action.getDescription();
    }

    @Override
    public List<Action> getHierarchy() {
        return this.action.getHierarchy();
    }

    @Override
    public List<AllowableAction> getSubActions() {
        return this.subactions;
    }

    @Override
    public Stream<AllowableAction> stream() {
        return Stream.concat(Stream.of(this),
                             getSubActions().stream().flatMap(AllowableAction::stream));
    }

}
