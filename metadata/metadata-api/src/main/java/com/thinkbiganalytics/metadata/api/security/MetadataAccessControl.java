/**
 * 
 */
package com.thinkbiganalytics.metadata.api.security;

import com.thinkbiganalytics.security.action.Action;

/**
 * Defines actions that may be permitted 
 */
public interface MetadataAccessControl {

    Action ACCESS_METADATA = Action.create("accessMetadata",
                                      "Access Kylo Metadata",
                                      "Allows the ability to view directly the data in the Kylo metadata store");
    Action ADMIN_METADATA = ACCESS_METADATA.subAction("adminMetadata",
                                                      "Administer Kylo Metadata",
                                                      "Allows the ability to directly manage the data in the Kylo metadata store");

}
