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
public interface FeedServicesAccessControl {

    Action FEEDS_SUPPORT = Action.create("accessFeedsSupport",
                                         "Access Feed Support",
                                         "Allows access to feeds and feed-related functions");

    Action ACCESS_CATEGORIES = FEEDS_SUPPORT.subAction("accessCategories",
                                                       "Access Categories",
                                                       "Allows access to categories");
    Action EDIT_CATEGORIES = ACCESS_CATEGORIES.subAction("editCategories",
                                                         "Edit Categories",
                                                         "Allows creating and editing new categories");
    Action ADMIN_CATEGORIES = ACCESS_CATEGORIES.subAction("adminCategories",
                                                          "Administer Categories",
                                                          "Allows the administration of a category");

    Action ACCESS_FEEDS = FEEDS_SUPPORT.subAction("accessFeeds",
                                                  "Access Feeds",
                                                  "Allows access to feeds");
    Action EDIT_FEEDS = ACCESS_FEEDS.subAction("editFeeds",
                                                 "Edit Feeds",
                                                 "Allows editing of feeds");
    Action IMPORT_FEEDS = ACCESS_FEEDS.subAction("importFeeds",
                                                 "Import Feeds",
                                                 "Allows importing of previously exported feeds");
    Action EXPORT_FEEDS = ACCESS_FEEDS.subAction("exportFeeds",
                                                 "Export Feeds",
                                                 "Allows exporting feeds definitions");
    Action ADMIN_FEEDS = ACCESS_FEEDS.subAction("adminFeeds",
                                                "Administer Feeds",
                                                "Allows the administration of a feed");
    Action ACCESS_TABLES = FEEDS_SUPPORT.subAction("accessTables",
                                                       "Access Tables",
                                                       "Allows access to tables search");
    Action ACCESS_VISUAL_QUERY = FEEDS_SUPPORT.subAction("accessVisualQuery",
                                                   "Access Visual Query",
                                                   "Allows access to visual query data wrangler ");
    Action ACCESS_TEMPLATES = FEEDS_SUPPORT.subAction("accessTemplates",
                                                      "Access Templates",
                                                      "Allows access to feed templates");
    Action EDIT_TEMPLATES = ACCESS_TEMPLATES.subAction("editTemplates",
                                                       "Edit Templates",
                                                       "Allows creating and editing new feed templates");
    Action IMPORT_TEMPLATES = ACCESS_TEMPLATES.subAction("importTemplates",
                                                         "Import Templates",
                                                         "Allows importing of previously exported templates");
    Action EXPORT_TEMPLATES = ACCESS_TEMPLATES.subAction("exportTemplates",
                                                         "Export Templates",
                                                         "Allows exporting template definitions");
    Action ADMIN_TEMPLATES = ACCESS_TEMPLATES.subAction("adminTemplates",
                                                        "Administer Templates",
                                                        "Allows the administration of a feed template (enable/disable/delete)");

    Action ACCESS_DATASOURCES = FEEDS_SUPPORT.subAction("accessDatasources",
                                                        "Access Data Sources",
                                                        "Allows access to data sources");
    Action EDIT_DATASOURCES = ACCESS_DATASOURCES.subAction("editDatasources",
                                                           "Edit Data Sources",
                                                           "Allows creating and editing new data sources");
    Action ADMIN_DATASOURCES = ACCESS_DATASOURCES.subAction("adminDatasources",
                                                            "Administer Data Sources",
                                                            "Allows the administration of data sources");

    Action ACCESS_SERVICE_LEVEL_AGREEMENTS = FEEDS_SUPPORT.subAction("accessServiceLevelAgreements",
                                                                     "Access Service Level Agreements",
                                                                     "Allows access to service level agreements");

    Action EDIT_SERVICE_LEVEL_AGREEMENTS = ACCESS_SERVICE_LEVEL_AGREEMENTS.subAction("editServiceLevelAgreements",
                                                                                     "Edit Service Level Agreements",
                                                                                     "Allows create service level agreements");
}
