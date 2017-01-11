/**
 * 
 */
package com.thinkbiganalytics.alerts.api.core;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.joda.time.DateTime;

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.Alert.Level;
import com.thinkbiganalytics.alerts.api.Alert.State;
import com.thinkbiganalytics.alerts.api.AlertCriteria;

/**
 * A generic AlertCriteria implementation that simply records the criteria settings.
 * It is useful as a base implementation to be extended by AlertProviders, AlertSources, 
 * and AlertManagers.
 * <P>
 * This implementation also has a default predicate implementation but subclasses are not 
 * required to support or use this implementation.
 * 
 * @author Sean Felten
 */
public class BaseAlertCriteria implements AlertCriteria, Predicate<Alert> {
    
    private int limit = Integer.MAX_VALUE;
    private Set<URI> types = new HashSet<>();
    private Set<Alert.State> states = new HashSet<>();
    private Set<Alert.Level> levels = new HashSet<>();
    private DateTime afterTime;
    private DateTime beforeTime;
    private boolean includeCleared = false;
    
    
    /**
     * Transfers the contents of this criteria to the given criteria.
     */
    public AlertCriteria transfer(AlertCriteria criteria) {
        AtomicReference<AlertCriteria> updated = new AtomicReference<AlertCriteria>(criteria);
        updated.set(updated.get().limit(this.limit));
        updated.set(updated.get().after(this.afterTime));
        updated.set(updated.get().before(this.beforeTime));
        updated.set(updated.get().includedCleared(this.isIncludeCleared()));
        this.types.forEach((t) -> updated.set(updated.get().type(t)));
        this.states.forEach((s) -> updated.set(updated.get().state(s)));
        this.levels.forEach((l) -> updated.set(updated.get().level(l)));
        return updated.get();
    }
    
    
    /* (non-Javadoc)
     * @see java.util.function.Predicate#test(java.lang.Object)
     */
    @Override
    public boolean test(Alert alert) {
        if (this.types.size() > 0 && ! testTypes(alert)) return false;
        if (this.states.size() > 0 && ! testStates(alert)) return false;
        if (this.levels.size() > 0 && ! testLevels(alert)) return false;
        if (this.afterTime != null && ! testAfterTime(alert)) return false;
        if (this.beforeTime != null && ! testBeforeTime(alert)) return false;
        if (! this.testCleared(alert)) return false;
        
        return true;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertCriteria#limit(int)
     */
    @Override
    public AlertCriteria limit(int size) {
        this.limit = Math.max(0, size);
        return this;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertCriteria#type(java.net.URI)
     */
    @Override
    public AlertCriteria type(URI type, URI... others) {
        if (type != null) this.types.add(type);
        if (others != null) Arrays.stream(others).forEach(uri -> this.types.add(uri));
        return this;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertCriteria#state(com.thinkbiganalytics.alerts.api.Alert.State)
     */
    @Override
    public AlertCriteria state(State state, State... others) {
        if (state != null) this.states.add(state);
        if (others != null) Arrays.stream(others).forEach(s -> this.states.add(s));
        return this;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertCriteria#level(com.thinkbiganalytics.alerts.api.Alert.Level)
     */
    @Override
    public AlertCriteria level(Level level, Level... others) {
        if (level != null) this.levels.add(level);
        if (others != null) Arrays.stream(others).forEach(l -> this.levels.add(l));
        return this;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertCriteria#after(org.joda.time.DateTime)
     */
    @Override
    public AlertCriteria after(DateTime time) {
        this.afterTime = time;
        return this;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertCriteria#before(org.joda.time.DateTime)
     */
    @Override
    public AlertCriteria before(DateTime time) {
        this.beforeTime = time;
        return this;
    }
    
    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertCriteria#includedCleared(boolean)
     */
    @Override
    public AlertCriteria includedCleared(boolean flag) {
        this.includeCleared  = flag;
        return this;
    }


    protected boolean testTypes(Alert alert) {
        return this.types.stream().anyMatch(uri -> { 
            // A match means the type URIs are equal (handles opaque URIs) or the matching URI is a parent as defined by relativize.
            return uri.equals(alert.getType()) || uri.relativize(alert.getType()) != alert.getType();
        });
    }


    protected boolean testStates(Alert alert) {
        return this.states.contains(alert.getState());
    }


    protected boolean testLevels(Alert alert) {
        return this.levels.contains(alert.getLevel());
    }


    protected boolean testAfterTime(Alert alert) {
        return alert.getCreatedTime().isAfter(this.afterTime);
    }


    protected boolean testBeforeTime(Alert alert) {
        return alert.getCreatedTime().isBefore(this.beforeTime);
    }
    
    protected boolean testCleared(Alert alert) {
        return ! alert.isCleared() || this.includeCleared;
    }


    protected int getLimit() {
        return limit;
    }

    protected Set<URI> getTypes() {
        return types;
    }

    protected Set<Alert.State> getStates() {
        return states;
    }

    protected Set<Alert.Level> getLevels() {
        return levels;
    }

    protected DateTime getAfterTime() {
        return afterTime;
    }

    protected DateTime getBeforeTime() {
        return beforeTime;
    }

    protected boolean isIncludeCleared() {
        return includeCleared;
    }
}
