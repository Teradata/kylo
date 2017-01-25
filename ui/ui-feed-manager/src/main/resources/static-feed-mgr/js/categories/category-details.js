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
     * Manages the Category Details page for creating and editing categories.
     *
     * @param $scope the application model
     * @param $stateParams the URL parameters
     * @param CategoriesService the category service
     * @constructor
     */
    function CategoryDetailsController($scope, $stateParams, CategoriesService) {
        var self = this;

        /**
         * Indicates if the category is currently being loaded.
         * @type {boolean} {@code true} if the category is being loaded, or {@code false} if it has finished loading
         */
        self.loadingCategory = true;

        /**
         * Category data.
         * @type {CategoryModel}
         */
        self.model = {};
        $scope.$watch(
                function() {return CategoriesService.model},
                function(model) {self.model = model},
                true
        );

        /**
         * Loads the category data once the list of categories has loaded.
         */
        self.onLoad = function() {
            if (angular.isString($stateParams.categoryId)) {
                self.model = CategoriesService.model = CategoriesService.findCategory($stateParams.categoryId);
                CategoriesService.getRelatedFeeds(CategoriesService.model);
                self.loadingCategory = false;
            } else {
                CategoriesService.getUserFields()
                        .then(function(userFields) {
                            CategoriesService.model = CategoriesService.newCategory();
                            CategoriesService.model.userProperties = userFields;
                            self.loadingCategory = false;
                        });
            }
        };

        // Load the list of categories
        if (CategoriesService.categories.length === 0) {
            CategoriesService.reload().then(self.onLoad);
        } else {
            self.onLoad();
        }
    }

    angular.module(MODULE_FEED_MGR).controller('CategoryDetailsController', CategoryDetailsController);
}());
