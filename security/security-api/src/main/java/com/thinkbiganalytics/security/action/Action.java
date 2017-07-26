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

import java.util.Arrays;
import java.util.List;

/**
 * Identifies an action that that may be checked if permitted. Actions are hierarchical in
 * that a permitted action implies permission of the all of an action's parent actions.
 */
public interface Action {

    /**
     * Constructs a new action as a child of the given hierarchy chain of parent actions, if any.
     *
     * @param name    the name
     * @param title   the title
     * @param descr   the description
     * @param parents an ordered list of parent actions (if any) starting from the top
     * @return a new immutable action
     */
    static Action create(String name, String title, String descr, Action... parents) {
        return new ImmutableAction(name, title, descr, Arrays.asList(parents));
    }

    /**
     * Constructs a new action as a child of the given hierarchy chain of parent actions, if any.
     *
     * @param name    the name
     * @param parents an ordered list of parent actions (if any) starting from the top
     * @return a new immutable action
     */
    static Action create(String name, Action... parents) {
        return new ImmutableAction(name, name, "", Arrays.asList(parents));
    }
    
    /**
     * Constructs a new action as a child of the given hierarchy chain of parent actions, if any.
     *
     * @param name    the name
     * @param title   the title
     * @param descr   the description
     * @param parents an ordered list of parent actions (if any) starting from the top
     * @return a new immutable action
     */
    static Action create(String name, String title, String descr, List<Action> parents) {
        return new ImmutableAction(name, title, descr, parents);
    }
    
    /**
     * Constructs a new action as a child of the given hierarchy chain of parent actions, if any.
     *
     * @param name    the name
     * @param parents an ordered list of parent actions (if any) starting from the top
     * @return a new immutable action
     */
    static Action create(String name, List<Action> parents) {
        return new ImmutableAction(name, name, "", parents);
    }

    /**
     * @return the unique system name of this action
     */
    String getSystemName();

    /**
     * @return the human-readable title of this action
     */
    String getTitle();

    /**
     * @return a description of this action
     */
    String getDescription();

    /**
     * Returns the full hierarchy of this action starting from the top and including this action.
     *
     * @return the hierachy as a list
     */
    List<Action> getHierarchy();

    /**
     * Indicates whether this action implies the given action.  The given action is implied
     * if it is present in the hierarchy of this action.
     *
     * @param action the action to check
     * @return true if it is implied by this action
     */
    default boolean implies(Action action) {
        return getHierarchy().stream().anyMatch(a -> a.equals(action));
    }

    /**
     * Constructs a new child action of this action.
     *
     * @param name  the name
     * @param title the title
     * @param descr the description
     * @return a new immutable action
     */
    default Action subAction(String name, String title, String descr) {
        return create(name, title, descr, this);
    }

    /**
     * Constructs a new child action of this action.
     *
     * @param name the name
     * @return a new immutable action
     */
    default Action subAction(String name) {
        return create(name, name, "", this);
    }
}
