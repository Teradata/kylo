/**
 *
 */
package com.thinkbiganalytics.feedmgr.security;

/*-
 * #%L
 * thinkbig-feed-manager-api
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
 * Actions involving feeds.
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
