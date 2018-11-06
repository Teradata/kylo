/**
 * 
 */
package com.thinkbiganalytics.metadata.api.catalog.security;

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
