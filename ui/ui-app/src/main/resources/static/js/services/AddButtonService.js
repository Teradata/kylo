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

define(['angular','services/module-name'], function (angular,moduleName) {
    angular.module(moduleName).service('AddButtonService', ["BroadcastService",function (BroadcastService) {
        var self = this;

        function AddButtonServiceTag() {
        }

        this.__tag = new AddButtonServiceTag();

        this.addButtons = {};
        this.NEW_ADD_BUTTON_EVENT = 'newAddButton'
        this.HIDE_ADD_BUTTON_EVENT = 'hideAddButton'
        this.SHOW_ADD_BUTTON_EVENT = 'showAddButton'

        this.registerAddButton = function (state, action) {
            self.addButtons[state] = action;
            BroadcastService.notify(self.NEW_ADD_BUTTON_EVENT, state);
        }
        this.hideAddButton = function () {
            BroadcastService.notify(self.HIDE_ADD_BUTTON_EVENT);
        }
        this.showAddButton = function () {
            BroadcastService.notify(self.SHOW_ADD_BUTTON_EVENT);
        }
        this.isShowAddButton = function (state) {
            return self.addButtons[state] != undefined;
        }
        this.unregisterAddButton = function (state) {
            self.addButtons[state];
        }
        this.onClick = function (state) {

            var action = self.addButtons[state];
            if (action) {
                action();
            }
        }

    }]);
});