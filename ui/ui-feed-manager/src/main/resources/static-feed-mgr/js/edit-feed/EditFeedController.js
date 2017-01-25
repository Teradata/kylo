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
(function () {
    /**
     * Controller for the Edit Feed page.
     *
     * @constructor
     */
    var EditFeedController = function($scope, $http, $mdDialog, $stateParams, FeedService, RestUrlService, StateService, VisualQueryService) {
        var self = this;

        /**
         * Feed ID
         *
         * @type {string}
         */
        self.feedId = $stateParams.feedId;

        /**
         * Feed model
         *
         * @type {Object}
         */
        self.model = FeedService.createFeedModel;
        self.model.loaded = false;

        /**
         * Selected index for the stepper
         *
         * @type {number}
         */
        self.selectedStepIndex = 0;

        /**
         * Template for the stepper
         *
         * @type {string}
         */
        self.stepperUrl = "";

        /**
         * Total number of steps for the stepper
         *
         * @type {number}
         */
        self.totalSteps = 0;

        /**
         * Fetches and displays the feed.
         */
        self.init = function() {
            var successFn = function(response) {
                // Set model
                self.model = response.data;
                self.model.loaded = true;
                FeedService.createFeedModel = self.model;

                // Update stepper based on template
                if (self.model.registeredTemplate.defineTable) {
                    self.selectedStepIndex = 2;
                    self.stepperUrl = "js/define-feed/define-feed-stepper.html";
                    self.totalSteps = 6;
                } else if (self.model.registeredTemplate.dataTransformation) {
                    VisualQueryService.resetModel();
                    self.selectedStepIndex = 2;
                    self.stepperUrl = "js/define-feed/define-feed-data-transform-stepper.html";
                    self.totalSteps = 8;
                } else {
                    self.stepperUrl = "js/define-feed/define-feed-no-table-stepper.html";
                    self.totalSteps = 4;
                }
            };
            var errorFn = function() {
                var alert = $mdDialog.alert()
                        .parent($("body"))
                        .clickOutsideToClose(true)
                        .title("Unable to load feed details")
                        .textContent("Unable to load feed details. Please ensure that Apache NiFi is up and running and then refresh this page.")
                        .ariaLabel("Unable to load feed details")
                        .ok("Got it!");
                $mdDialog.show(alert);
            };

            $http.get(RestUrlService.GET_FEEDS_URL + "/" + self.feedId).then(successFn, errorFn);
        };

        /**
         * Resets the editor state.
         */
        this.cancelStepper = function() {
            FeedService.resetFeed();
            self.stepperUrl = "";
            StateService.navigateToFeeds();
        };

        // Initialize this instance
        self.init();
    };

    angular.module(MODULE_FEED_MGR).controller("EditFeedController", EditFeedController);
}());
