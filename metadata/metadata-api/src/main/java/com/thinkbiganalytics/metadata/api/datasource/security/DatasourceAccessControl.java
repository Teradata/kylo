package com.thinkbiganalytics.metadata.api.datasource.security;

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
 * Permissions that can be granted to users for specific data sources.
 */
public interface DatasourceAccessControl {

    Action ACCESS_DATASOURCE = Action.create("accessDatasource",
                                             "Access Datasource",
                                             "Allows the ability to see basic summary information and to use the data source in data transformations");
    Action EDIT_SUMMARY = ACCESS_DATASOURCE.subAction("editDatasourceSummary",
                                                      "Edit Summary",
                                                      "Allows editing of the summary information about the data source");
    Action ACCESS_DETAILS = ACCESS_DATASOURCE.subAction("accessDatasourceDetails",
                                                        "Access Details",
                                                        "Allows viewing the full details about the data source");
    Action EDIT_DETAILS = ACCESS_DETAILS.subAction("editDatasourceDetails",
                                                   "Edit Details",
                                                   "Allows editing of the details about the data source");
    Action DELETE = ACCESS_DETAILS.subAction("deleteDatasource",
                                             "Delete",
                                             "Allows deleting the data source");
    Action CHANGE_PERMS = ACCESS_DATASOURCE.subAction("changeDatasourcePermissions",
                                                      "Change Permissions",
                                                      "Allows editing of the permissions that grant access to the data source");
}
