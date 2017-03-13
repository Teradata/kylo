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
    angular.module(moduleName).factory("WindowUnloadService", ["$rootScope","$window",function ($rootScope, $window) {
        /**
         * Listens and responds to window unload events.
         *
         * @readonly
         * @type {Object}
         */
        var WindowUnloadService = {
            /**
             * Confirmation text to be displayed on an unload event.
             *
             * @private
             * @type {string|null}
             */
            text_: null,

            /**
             * Disables the confirmation dialog.
             */
            clear: function () {
                this.text_ = null;
            },

            /**
             * Called to setup the confirmation dialog on an unload event.
             *
             * @param {Event} e the unload event
             * @returns {string|null} the dialog text to be displayed, or {@code null} to allow the event
             */
            onBeforeUnload: function (e) {
                if (this.text_ !== null) {
                    e.returnValue = this.text_;
                }
                return this.text_;
            },

            /**
             * Enables the confirmation dialog and sets the dialog text.
             *
             * @param {string} text the dialog text
             */
            setText: function (text) {
                this.text_ = text;
            },

            /**
             * Called when changing states.
             *
             * @param {Event} e the state change event
             */
            shouldChangeState: function (e) {
                if (this.text_ === null || confirm(this.text_)) {
                    this.clear();
                } else {
                    e.preventDefault();
                }
            }
        };

        // Setup event listeners
        //@TODO CHANGE to use $transitions$
        $rootScope.$on("$stateChangeStart", angular.bind(WindowUnloadService, WindowUnloadService.shouldChangeState));
        $window.onbeforeunload = angular.bind(WindowUnloadService, WindowUnloadService.onBeforeUnload);

        // Return service
        return WindowUnloadService;
    }]);
});