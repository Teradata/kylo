/**
 * 
 */
package com.thinkbiganalytics.security.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Simple and immutable implementation of Action useful for creating constants.
 * @author Sean Felten
 */
public class ImmutableAction implements Action {

    private final String systemName;
    private final List<Action> hierarchy;
    
    public static ImmutableAction create(String name, Action... parents) {
        return new ImmutableAction(name, Arrays.asList(parents));
    }
    
    public ImmutableAction subAction(String name) {
        return new ImmutableAction(name, this.hierarchy);
    }

    public String getSystemName() {
        return systemName;
    }
    
    public List<Action> getHierarchy() {
        return hierarchy;
    }

    protected ImmutableAction(String systemName, List<Action> parents) {
        super();
        
        List<Action> list = new ArrayList<>(parents);
        list.add(this);
        
        this.systemName = systemName;
        this.hierarchy = Collections.unmodifiableList(list);
    }
    
    @Override
    public String toString() {
        return this.systemName;
    }
 }
