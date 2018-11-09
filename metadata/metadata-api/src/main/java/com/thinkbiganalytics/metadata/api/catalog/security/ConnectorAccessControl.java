/**
 * 
 */
package com.thinkbiganalytics.metadata.api.catalog.security;

/*-
 * #%L
 * kylo-metadata-api
 * %%
 * Copyright (C) 2017 - 2018 ThinkBig Analytics, a Teradata Company
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
 *
 */
public interface ConnectorAccessControl {
    Action ACCESS_CONNECTOR = Action.create("accessConnector",
                                            "Access",
                                            "Allows the ability to see information about the connector");
    Action EDIT_CONNECTOR = ACCESS_CONNECTOR.subAction("editConnector",
                                                       "Edit",
                                                       "Allows editing of the information of the connector");
    Action CHANGE_PERMS = EDIT_CONNECTOR.subAction("changeConnectorPermissions",
                                                     "Change Permissions",
                                                     "Allows editing of the permissions that grant access to the connector");
    Action ACTIVATE_CONNECTOR = ACCESS_CONNECTOR.subAction("activateConnector",
                                                 "Activate/Deactivate",
                                                 "Allows activating and deactivating the connector");
    Action CREATE_DATA_SOURCE = ACCESS_CONNECTOR.subAction("createDataSource",
                                                           "Create Data Source",
                                                           "Allows creation of data sources for this type of connector");

}
