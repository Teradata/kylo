package com.thinkbiganalytics.metadata.api.datasource.security;

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
