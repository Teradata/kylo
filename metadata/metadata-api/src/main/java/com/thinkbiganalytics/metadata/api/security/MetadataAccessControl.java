package com.thinkbiganalytics.metadata.api.security;

/*-
 * #%L
 * kylo-metadata-api
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
 * Defines actions that may be permitted 
 */
public interface MetadataAccessControl {

    Action ACCESS_METADATA = Action.create("accessMetadata",
                                      "Access Kylo Metadata",
                                      "Allows the ability to view and query directly the data in the Kylo metadata store, including extensible types");

    Action ADMIN_METADATA = ACCESS_METADATA.subAction("adminMetadata",
                                                      "Administer Kylo Metadata",
                                                      "Allows the ability to directly manage the data in the Kylo metadata store (edit raw metadata, create/update/delete extensible types, update feed status events)");

}
