package com.thinkbiganalytics.metadata.api.feed.security;

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
 * Actions involving an individual feed.
 */
public interface FeedAccessControl {
    
    Action ACCESS_FEED = Action.create("accessFeed",
                                            "Access Feed",
                                            "Allows the ability to view the feed and see basic summary information about it");
    Action EDIT_SUMMARY = ACCESS_FEED.subAction("editFeedSummary",
                                                 "Edit Summary",
                                                 "Allows editing of the summary information about the feed");
    Action ACCESS_DETAILS = ACCESS_FEED.subAction("accessFeedDetails",
                                                 "Access Details",
                                                 "Allows viewing the full details about the feed");
    Action EDIT_DETAILS = ACCESS_DETAILS.subAction("editFeedDetails",
                                                   "Edit Details",
                                                    "Allows editing of the details about the feed");
    Action DELETE = ACCESS_DETAILS.subAction("deleteFeed",
                                             "Delete",
                                             "Allows deleting the feed");
    Action ENABLE_DISABLE = ACCESS_DETAILS.subAction("enableFeed",
                                                     "Enable/Disable",
                                                     "Allows enabling and disabling the feed");
    Action EXPORT = ACCESS_DETAILS.subAction("exportFeed",
                                             "Export",
                                             "Allows exporting the feed");
//    Action SCHEDULE_FEED = ACCESS_DETAILS.subAction("scheduleFeed",
//                                                    "Change Schedule",
//                                                    "Allows the ability to change the execution schedule of the feed");
    Action ACCESS_OPS = ACCESS_FEED.subAction("accessFeedOperations",
                                                   "Access Operations",
                                                   "Allows the ability to see the operational history of the feed");
    Action CHANGE_PERMS = ACCESS_FEED.subAction("changeFeedPermissions",
                                                "Change Permissions",
                                                "Allows editing of the permissions that grant access to the feed");
}
