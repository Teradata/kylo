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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple and immutable implementation of Action useful for creating constants.
 */
public class ImmutableAction implements Action {

    private final String systemName;
    private final String title;
    private final String description;
    private final List<Action> hierarchy;
    private final int hash;


    protected ImmutableAction(String systemName, String title, String descr, List<Action> parents) {
        super();

        List<Action> list = new ArrayList<>(parents);
        list.add(this);

        this.systemName = systemName;
        this.title = title;
        this.description = descr;
        this.hierarchy = Collections.unmodifiableList(new ArrayList<>(list));
        // The hash only needs to be generated once since this object is immutable.
        // This action's hash value is a hash of its and all its parents system names.
        this.hash = this.hierarchy.stream()
            .map(a -> a.getSystemName())
            .collect(Collectors.toList())
            .hashCode();
    }

    /**
     * Constructs a new action as a child of the given hierarchy chain of parent actions, if any.
     *
     * @param name    the name
     * @param title   the title
     * @param descr   the description
     * @param parents an order list of parent actions (if any) starting from the top
     * @return a new immutable action
     */
    public static ImmutableAction create(String name, String title, String descr, Action... parents) {
        return new ImmutableAction(name, title, descr, Arrays.asList(parents));
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.Action#subAction(java.lang.String, java.lang.String, java.lang.String)
     */
    public ImmutableAction subAction(String name, String title, String descr) {
        return new ImmutableAction(name, title, descr, this.hierarchy);
    }

    public String getSystemName() {
        return systemName;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.Action#getTitle()
     */
    @Override
    public String getTitle() {
        return this.title;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.Action#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    /* (non-Javadoc)
     * @see com.thinkbiganalytics.security.action.Action#getHierarchy()
     */
    public List<Action> getHierarchy() {
        return hierarchy;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.systemName;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.hash;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Action && obj.hashCode() == this.hash;
    }
}
