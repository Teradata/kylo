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
 * Used to programmatically open/close the side nav bar
 */
define(['angular','services/module-name'], function (angular,moduleName) {

    return angular.module(moduleName).service('SideNavService', ["BroadcastService", function (BroadcastService) {

    var self = this;
    this.isLockOpen = true;

    this.hideSideNav = function(){
        if (self.isLockOpen) {
            self.isLockOpen = false;
            BroadcastService.notify(BroadcastConstants.CONTENT_WINDOW_RESIZED, null, 600);
        }

    }

    this.showSideNav = function () {
        if (!self.isLockOpen) {
            self.isLockOpen = true;
            BroadcastService.notify(BroadcastConstants.CONTENT_WINDOW_RESIZED, null, 600);
        }

    }

}]);
});