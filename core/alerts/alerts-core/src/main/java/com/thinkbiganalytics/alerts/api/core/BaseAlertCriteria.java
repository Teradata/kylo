/**
 *
 */
package com.thinkbiganalytics.alerts.api.core;

/*-
 * #%L
 * thinkbig-alerts-core
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

import com.thinkbiganalytics.alerts.api.Alert;
import com.thinkbiganalytics.alerts.api.Alert.Level;
import com.thinkbiganalytics.alerts.api.Alert.State;
import com.thinkbiganalytics.alerts.api.AlertCriteria;

import org.joda.time.DateTime;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 * A generic AlertCriteria implementation that simply records the criteria settings.
 * It is useful as a base implementation to be extended by AlertProviders, AlertSources,
 * and AlertManagers.
 * <P>
 * This implementation also has a default predicate implementation but subclasses are not
 * required to support or use this implementation.
 */
public class BaseAlertCriteria implements AlertCriteria, Predicate<Alert> {

    private int limit = Integer.MAX_VALUE;
    private Set<URI> types = new HashSet<>();
    private Set<String> subtypes = new HashSet<>();
    private Set<Alert.State> states = new HashSet<>();
    private Set<Alert.Level> levels = new HashSet<>();
    private DateTime afterTime;
    private DateTime beforeTime;
    private DateTime modifiedAfterTime;
    private DateTime modifiedBeforeTime;
    private boolean includeCleared = false;
    private boolean asServiceAccount = false;
    private boolean onlyIfChangesDetected = false;
    private String orFilter;


    /**
     * Transfers the contents of this criteria to the given criteria.
     */
    public AlertCriteria transfer(AlertCriteria criteria) {
        AtomicReference<AlertCriteria> updated = new AtomicReference<AlertCriteria>(criteria);
        updated.set(updated.get().limit(this.limit));
        updated.set(updated.get().after(this.afterTime));
        updated.set(updated.get().before(this.beforeTime));
        updated.set(updated.get().modifiedAfter(this.modifiedAfterTime));
        updated.set(updated.get().modifiedBefore(this.modifiedBeforeTime));
        updated.set(updated.get().includedCleared(this.isIncludeCleared()));
        this.types.forEach((t) -> updated.set(updated.get().type(t)));
        this.subtypes.forEach((t) -> updated.set(updated.get().subtype(t)));
        this.states.forEach((s) -> updated.set(updated.get().state(s)));
        this.levels.forEach((l) -> updated.set(updated.get().level(l)));
        updated.set(updated.get().orFilter(orFilter));
        updated.set(updated.get().asServiceAccount(this.asServiceAccount));
        updated.set(updated.get().onlyIfChangesDetected(this.onlyIfChangesDetected));
        return updated.get();
    }

    /* (non-Javadoc)
     * @see java.util.function.Predicate#test(java.lang.Object)
     */
    @Override
    public boolean test(Alert alert) {
        if (this.types.size() > 0 && !testTypes(alert)) {
            return false;
        }
        if (this.states.size() > 0 && !testStates(alert)) {
            return false;
        }
        if (this.levels.size() > 0 && !testLevels(alert)) {
            return false;
        }
        if (this.afterTime != null && !testAfterTime(alert)) {
            return false;
        }
        if (this.beforeTime != null && !testBeforeTime(alert)) {
            return false;
        }
        if (this.modifiedAfterTime != null && !testModifiedAfterTime(alert)) {
            return false;
        }
        if (this.modifiedBeforeTime != null && !testModifiedBeforeTime(alert)) {
            return false;
        }
        if (!this.testCleared(alert)) {
            return false;
        }

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
        if (type != null) {
            this.types.add(type);
        }
        if (others != null) {
            Arrays.stream(others).forEach(uri -> this.types.add(uri));
        }
        return this;
    }

    public AlertCriteria subtype(String subtype, String... others) {
        if (subtype != null) {
            this.subtypes.add(subtype);
        }
        if (others != null) {
            Arrays.stream(others).forEach(otherType -> this.subtypes.add(otherType));
        }
        return this;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertCriteria#state(com.thinkbiganalytics.alerts.api.Alert.State)
     */
    @Override
    public AlertCriteria state(State state, State... others) {
        if (state != null) {
            this.states.add(state);
        }
        if (others != null) {
            Arrays.stream(others).forEach(s -> this.states.add(s));
        }
        return this;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertCriteria#level(com.thinkbiganalytics.alerts.api.Alert.Level)
     */
    @Override
    public AlertCriteria level(Level level, Level... others) {
        if (level != null) {
            this.levels.add(level);
        }
        if (others != null) {
            Arrays.stream(others).forEach(l -> this.levels.add(l));
        }
        return this;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertCriteria#after(org.joda.time.DateTime)
     */
    @Override
    public AlertCriteria after(DateTime time) {
        if (time != null) {
            this.afterTime = time;
        }
        return this;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertCriteria#before(org.joda.time.DateTime)
     */
    @Override
    public AlertCriteria before(DateTime time) {
        if (time != null) {
            this.beforeTime = time;
        }
        return this;
    }

    @Override
    public AlertCriteria modifiedBefore(DateTime time) {
        if (time != null) {
            this.modifiedBeforeTime = time;
        }
        return this;
    }

    @Override
    public AlertCriteria modifiedAfter(DateTime time) {
        if (time != null) {
            this.modifiedAfterTime = time;
        }
        return this;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.alerts.api.AlertCriteria#includedCleared(boolean)
     */
    @Override
    public AlertCriteria includedCleared(boolean flag) {
        this.includeCleared = flag;
        return this;
    }

    @Override
    public AlertCriteria orFilter(String orFilter) {
        this.orFilter = orFilter;
        return this;
    }

    public AlertCriteria asServiceAccount(boolean serviceAccount) {
        this.asServiceAccount = serviceAccount;
        return this;
    }

    public AlertCriteria onlyIfChangesDetected(boolean onlyIfChangesDetected) {
        this.onlyIfChangesDetected = onlyIfChangesDetected;
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

    protected boolean testModifiedAfterTime(Alert alert) {
        return alert.getModifiedTime().isAfter(this.modifiedAfterTime);
    }


    protected boolean testModifiedBeforeTime(Alert alert) {
        return alert.getModifiedTime().isBefore(this.modifiedBeforeTime);
    }


    protected boolean testCleared(Alert alert) {
        return !alert.isCleared() || this.includeCleared;
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

    protected DateTime getModifiedAfterTime() {
        return modifiedAfterTime;
    }

    protected DateTime getModifiedBeforeTime() {
        return modifiedBeforeTime;
    }

    protected boolean isIncludeCleared() {
        return includeCleared;
    }

    public Set<String> getSubtypes() {
        return subtypes;
    }

    public String getOrFilter() {
        return orFilter;
    }

    public boolean isAsServiceAccount() {
        return asServiceAccount;
    }

    public boolean isOnlyIfChangesDetected() {
        return onlyIfChangesDetected;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BaseAlertCriteria{");
        sb.append("limit=").append(limit);
        sb.append(", types=").append(types);
        sb.append(", subtypes=").append(subtypes);
        sb.append(", states=").append(states);
        sb.append(", levels=").append(levels);
        sb.append(", afterTime=").append(afterTime);
        sb.append(", beforeTime=").append(beforeTime);
        sb.append(", modifiedAfterTime=").append(modifiedAfterTime);
        sb.append(", modifiedBeforeTime=").append(modifiedBeforeTime);
        sb.append(", includeCleared=").append(includeCleared);
        sb.append(", asServiceAccount=").append(asServiceAccount);
        sb.append(", onlyIfChangesDetected=").append(onlyIfChangesDetected);
        sb.append(", orFilter='").append(orFilter).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
