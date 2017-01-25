/*-
 * #%L
 * thinkbig-ui-feed-manager
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
/**
 * Used to store temporary state of the Edit Feed Nifi Properties
 * when a user clicks the Edit link for the Feed Details so the object can be passed to the template factory
 *
 */
angular.module(MODULE_FEED_MGR).service('EditFeedNifiPropertiesService', function (RestUrlService) {

    var self = this;
    this.editFeedModel = {};


});
