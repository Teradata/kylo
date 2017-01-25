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
     * Manages the Related Feeds section of the Category Details page.
     *
     * @constructor
     * @param $scope the application model
     * @param CategoriesService the category service
     * @param StateService the URL service
     */
    function CategoryFeedsController($scope, CategoriesService, StateService) {
        var self = this;

        /**
         * Category data.
         * @type {CategoryModel}
         */
        self.model = CategoriesService.model;

        /**
         * Navigates to the specified feed.
         *
         * @param {Object} feed the feed to navigate to
         */
        self.onFeedClick = function(feed) {
            StateService.navigateToFeedDetails(feed.id);
        };
    }

    /**
     * Creates a directive for the Related Feeds section.
     *
     * @returns {Object} the directive
     */
    function thinkbigCategoryFeeds() {
        return {
            controller: "CategoryFeedsController",
            controllerAs: "vm",
            restrict: "E",
            scope: {},
            templateUrl: "js/categories/details/category-feeds.html"
        };
    }

    angular.module(MODULE_FEED_MGR).controller('CategoryFeedsController', CategoryFeedsController);
    angular.module(MODULE_FEED_MGR).directive('thinkbigCategoryFeeds', thinkbigCategoryFeeds);
})();
