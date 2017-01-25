/**
 * 
 */
package com.thinkbiganalytics.jobrepo.security;

/*-
 * #%L
 * thinkbig-job-repository-api
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
 * Describes the permissible actions related to operational services (feed processing management,
 * job execution history, etc.)
 * 
 * @author Sean Felten
 */
public interface OperationsAccessControl {

    // TODO are there other levels of access besides the ability to view or administer operational
    // functions?
    public static final Action ACCESS_OPS = Action.create("accessOperations",
                                                          "Access Operational information",
                                                          "Allows access to operational information like active feeds and execution history, etc.");
    public static final Action ADMIN_OPS = ACCESS_OPS.subAction("adminOperations",
                                                                "Administer Operations",
                                                                "Allows administration of operations, such as stopping and abandoning them.");
}
