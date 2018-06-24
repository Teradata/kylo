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
import * as angular from 'angular';
import {moduleName} from './module-name';
import BroadcastConstants from "./BroadcastConstants";
import BroadcastService from "./broadcast-service";

export default class SideNavService{
    isLockOpen: any;
    hideSideNav: any;
    showSideNav: any;
 constructor (private BroadcastService: any){
      this.isLockOpen = true;

    this.hideSideNav = function(){
        if (this.isLockOpen) {
            this.isLockOpen = false;
            BroadcastService.notify(BroadcastConstants.CONTENT_WINDOW_RESIZED, null, 600);
        }

    }

    this.showSideNav = function () {
        if (!this.isLockOpen) {
            this.isLockOpen = true;
            BroadcastService.notify(BroadcastConstants.CONTENT_WINDOW_RESIZED, null, 600);
        }
    }
 }
}
angular.module(moduleName)
.service('BroadcastService', ["$rootScope", "$timeout",BroadcastService])
.service('SideNavService', ["BroadcastService",SideNavService]);
