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
(function() {

    /**
     * Displays the Visual Query page.
     *
     * @param {Object} $scope the application model
     * @param SideNavService the sidebar navigation service
     * @param StateService the state service
     * @param VisualQueryService the visual query service
     * @constructor
     */
    function VisualQueryController($scope, SideNavService, StateService, VisualQueryService) {
        var self = this;

        /**
         * The visual query model.
         * @type {Object}
         */
        self.model = VisualQueryService.model;

        /**
         * Navigates to the Feeds page when the stepper is cancelled.
         */
        self.cancelStepper = function() {
            VisualQueryService.resetModel();
            StateService.navigateToHome();
        };

        // Manage the sidebar navigation
        SideNavService.hideSideNav();

        $scope.$on("$destroy", function() {
            SideNavService.showSideNav();
        });
    }

    angular.module(MODULE_FEED_MGR).controller("VisualQueryController", VisualQueryController);
}());
