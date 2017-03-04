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

define(['angular','feed-mgr/feeds/define-feed/module-name'], function (angular,moduleName) {

    var controller;
    controller = function ($scope, $q, $http, $mdToast,$transition$, RestUrlService, $transition$, FeedService, StateService) {
        var self = this;
       // self.model = $transition$.params().feedModel;
       // self.error = $transition$.params().error;
        self.model = $transition$.params().feedModel;
        self.error = $transition$.params().error;

        self.isValid = self.error == null;

        /**
         * Gets the feed id from the FeedService
         * @returns {*}
         */
        function getFeedId() {
            var feedId = self.model != null ? self.model.id : null;
            if (feedId == null && FeedService.createFeedModel != null) {
                feedId = FeedService.createFeedModel.id;
            }
            if (feedId == null && FeedService.editFeedModel != null) {
                feedId = FeedService.editFeedModel.id;
            }
            return feedId;
        }

        /**
         * Navigate to the Feed Details SLA tab
         */
        this.onAddServiceLevelAgreement = function () {
            //navigate to Feed Details and move to the 3 tab (SLA)
            var feedId = getFeedId();
            StateService.FeedManager().Feed().navigateToFeedDetails(feedId, 3);
        }
        this.onViewDetails = function () {
            StateService.FeedManager().Sla().navigateToServiceLevelAgreements();
        }

        /**
         * Navigate to the Feed Details first tab
         */
        this.onViewDetails = function () {
            var feedId = getFeedId();
            StateService.FeedManager().Feed().navigateToFeedDetails(feedId, 0);
        }

        /**
         * Navigate to the Feed List page
         */
        this.onViewFeedsList = function () {
            FeedService.resetFeed();
            StateService.FeedManager().Feed().navigateToFeeds();
        }

        this.gotIt = function () {
            self.onViewFeedsList();
        }

    };


    angular.module(moduleName).controller('DefineFeedCompleteController', ["$scope","$q","$http","$mdToast","$transition$","RestUrlService","$transition$","FeedService","StateService",controller]);

});


