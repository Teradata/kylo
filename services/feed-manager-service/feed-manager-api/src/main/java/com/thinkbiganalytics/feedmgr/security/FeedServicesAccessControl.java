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
                                                       "Allows access to categories and their metadata");

    Action EDIT_CATEGORIES = ACCESS_CATEGORIES.subAction("editCategories",
                                                         "Edit Categories",
                                                         "Allows creating, updating and deleting categories");

    Action ADMIN_CATEGORIES = ACCESS_CATEGORIES.subAction("adminCategories",
                                                          "Administer Categories",
                                                          "Allows updating category metadata");

    Action ACCESS_FEEDS = FEEDS_SUPPORT.subAction("accessFeeds",
                                                  "Access Feeds",
                                                  "Allows access to feeds and their metadata");

    Action EDIT_FEEDS = ACCESS_FEEDS.subAction("editFeeds",
                                               "Edit Feeds",
                                               "Allows creating, updating, enabling and disabling feeds");

    Action IMPORT_FEEDS = ACCESS_FEEDS.subAction("importFeeds",
                                                 "Import Feeds",
                                                 "Allows importing of previously exported feeds along with their associated templates (.zip files)");

    Action EXPORT_FEEDS = ACCESS_FEEDS.subAction("exportFeeds",
                                                 "Export Feeds",
                                                 "Allows exporting feeds definitions with their associated templates (.zip files)");

    Action ADMIN_FEEDS = ACCESS_FEEDS.subAction("adminFeeds",
                                                "Administer Feeds",
                                                "Allows deleting feeds and editing feed metadata");

    Action ACCESS_TABLES = FEEDS_SUPPORT.subAction("accessTables",
                                                   "Access Tables",
                                                   "Allows listing and querying Hive tables");

    Action ACCESS_VISUAL_QUERY = FEEDS_SUPPORT.subAction("accessVisualQuery",
                                                         "Access Visual Query",
                                                         "Allows access to visual query data wrangler");

    Action ACCESS_TEMPLATES = FEEDS_SUPPORT.subAction("accessTemplates",
                                                      "Access Templates",
                                                      "Allows access to feed templates");

    Action EDIT_TEMPLATES = ACCESS_TEMPLATES.subAction("editTemplates",
                                                       "Edit Templates",
                                                       "Allows creating, updating, deleting and sequencing feed templates");

    Action IMPORT_TEMPLATES = ACCESS_TEMPLATES.subAction("importTemplates",
                                                         "Import Templates",
                                                         "Allows importing of previously exported templates (.xml and .zip files)");

    Action EXPORT_TEMPLATES = ACCESS_TEMPLATES.subAction("exportTemplates",
                                                         "Export Templates",
                                                         "Allows exporting template definitions (.zip files)");

    Action ADMIN_TEMPLATES = ACCESS_TEMPLATES.subAction("adminTemplates",
                                                        "Administer Templates",
                                                        "Allows enabling and disabling feed templates");


    Action ACCESS_DATASOURCES = FEEDS_SUPPORT.subAction("accessDatasources",
                                                        "Access Data Sources",
                                                        "Allows (a) access to data sources (b) viewing tables and schemas from a data source (c) using a data source in transformation feed");

    Action EDIT_DATASOURCES = ACCESS_DATASOURCES.subAction("editDatasources",
                                                           "Edit Data Sources",
                                                           "Allows creating and editing data sources");

    Action ADMIN_DATASOURCES = ACCESS_DATASOURCES.subAction("adminDatasources",
                                                            "Administer Data Sources",
                                                            "Allows getting data source details with sensitive info");

    Action ACCESS_SERVICE_LEVEL_AGREEMENTS = FEEDS_SUPPORT.subAction("accessServiceLevelAgreements",
                                                                     "Access Service Level Agreements",
                                                                     "Allows access to service level agreements");

    Action EDIT_SERVICE_LEVEL_AGREEMENTS = ACCESS_SERVICE_LEVEL_AGREEMENTS.subAction("editServiceLevelAgreements",
                                                                                     "Edit Service Level Agreements",
                                                                                     "Allows creating and editing service level agreements");

    Action EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE = ACCESS_SERVICE_LEVEL_AGREEMENTS.subAction("editServiceLevelAgreementEmailTemplate",
                                                                                     "Edit Service Level Agreement Email Templates",
                                                                                     "Allows creating and editing service level agreement email templates");

    Action ACCESS_GLOBAL_SEARCH = FEEDS_SUPPORT.subAction("accessSearch",
                                                          "Access Global Search",
                                                          "Allows access to search all indexed columns");
}
