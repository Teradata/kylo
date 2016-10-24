/**
 * 
 */
package com.thinkbiganalytics.feedmgr.security;

import com.thinkbiganalytics.security.action.Action;

/**
 * Actions involving feeds.
 * 
 * @author Sean Felten
 */
public interface FeedsAccessControl {

    static final Action FEEDS_SUPPORT = Action.create("accessFeedsSupport",
                                                      "Access Feed Support",
                                                      "Allows access to feeds and feed-related functions");

    static final Action ACCESS_CATEGORIES = FEEDS_SUPPORT.subAction("accessCategories",
                                                                    "Access Feeds",
                                                                    "Allows access to feeds");
    static final Action EDIT_CATEGORIES = ACCESS_CATEGORIES.subAction("editCategories",
                                                                      "Edit Feeds",
                                                                      "Allows creating and editing new feeds");
    static final Action ADMIN_CATEGORIES = ACCESS_CATEGORIES.subAction("adminCategories",
                                                                       "Import Feeds",
                                                                       "Allows importing of previously exported feeds");

    static final Action ACCESS_FEEDS = FEEDS_SUPPORT.subAction("accessFeeds",
                                                               "Access Feeds",
                                                               "Allows access to feeds");
    static final Action EDIT_FEEDS = ACCESS_FEEDS.subAction("editFeeds",
                                                            "Edit Feeds",
                                                            "Allows creating and editing new feeds");
    static final Action IMPORT_FEEDS = ACCESS_FEEDS.subAction("importFeeds",
                                                              "Import Feeds",
                                                              "Allows importing of previously exported feeds");
    static final Action EXPORT_FEEDS = ACCESS_FEEDS.subAction("exportFeeds",
                                                              "Export Feeds",
                                                              "Allows exporting feeds definitions");
    static final Action ADMIN_FEEDS = ACCESS_FEEDS.subAction("adminFeeds",
                                                             "Administer Feeds",
                                                             "Allows the administration of any feed; even those created by others");

    static final Action ACCESS_TEMPLATES = FEEDS_SUPPORT.subAction("accessTemplates",
                                                                   "Access Templates",
                                                                   "Allows access to feed templates");
    static final Action EDIT_TEMPLATES = ACCESS_TEMPLATES.subAction("editTemplates",
                                                                    "Edit Templates",
                                                                    "Allows creating and editing new feed templates");
    static final Action IMPORT_TEMPLATES = ACCESS_TEMPLATES.subAction("importTemplates",
                                                                      "Import Templates",
                                                                      "Allows importing of previously exported templates");
    static final Action EXPORT_TEMPLATES = ACCESS_TEMPLATES.subAction("exportTemplates",
                                                                      "Export Templates",
                                                                      "Allows exporting template definitions");
    static final Action ADMIN_TEMPLATES = ACCESS_TEMPLATES.subAction("adminTemplates",
                                                                     "Administer Templates",
                                                                     "Allows the administration of any feed template; even those created by others");

}
