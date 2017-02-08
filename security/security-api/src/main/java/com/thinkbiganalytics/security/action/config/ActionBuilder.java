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
 * A builder for constructing a new action in an action hierarchy.
 */
public interface ActionBuilder<P> {

    /**
     * Sets the action title.
     */
    ActionBuilder<P> title(String name);

    /**
     * Sets the action description.
     */
    ActionBuilder<P> description(String name);

    /**
     * Starts a new builder for a sub-action of the one being built
     * with this builder.  The system name from the supplied
     * action will be used for the system name of the new sub-action.
     */
    ActionBuilder<ActionBuilder<P>> subAction(Action action);

    /**
     * Starts a new builder for a sub-action of the one being built
     * with this builder; using the provided system name for the new sub-action.
     */
    ActionBuilder<ActionBuilder<P>> subAction(String systemName);

    P add();
}
