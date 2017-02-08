/**
 *
 */
package com.thinkbiganalytics.security.action.config;

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

import com.thinkbiganalytics.security.action.Action;

/**
 * A builder used to construct an hierarchy subtree of actions.
 */
public interface ActionsTreeBuilder<P> {

    /**
     * Adds a new action hierarchy constructed using the fields and
     * action hierarchy provided by the specified action.  This will
     * add new parent actions for every action in the hierarchy of
     * the action argument that does not already exist.
     * <p>
     * Note that this method is equivalent to using the builder
     * returned from action(String) on each of the argument action's
     * parents and filling in the fields from those actions's properties.
     */
    ActionsTreeBuilder<P> action(Action action);

    /**
     * Creates a builder used to construct a new action with the given system name.
     */
    ActionBuilder<ActionsTreeBuilder<P>> action(String systemName);

    /**
     * Adds the newly constructed action(s) to the over all actions hierarchy.
     */
    P add();
}
