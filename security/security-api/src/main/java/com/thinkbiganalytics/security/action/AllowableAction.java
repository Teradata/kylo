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

import java.util.List;
import java.util.stream.Stream;

/**
 * Describes an action that may be authorized for a user or group.
 */
public interface AllowableAction extends Action {

    /**
     * @return the set of direct sub-actions defined below this action
     */
    List<AllowableAction> getSubActions();

    /**
     * Streams the full sub-action tree defined below this action using pre-order traversal.
     *
     * @return a stream of sub-actions of this action
     */
    Stream<AllowableAction> stream();
}
