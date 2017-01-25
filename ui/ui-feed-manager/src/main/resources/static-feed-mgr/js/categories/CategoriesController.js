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
     * Displays a list of categories.
     *
     * @constructor
     * @param $scope the application model
     * @param {AccessControlService} AccessControlService the access control service
     * @param AddButtonService the Add button service
     * @param CategoriesService the categories service
     * @param StateService the page state service
     */
    var CategoriesController = function($scope, AccessControlService, AddButtonService, CategoriesService, StateService) {
        var self = this;

        /**
         * List of categories.
         * @type {Array.<Object>}
         */
        self.categories = [];
        $scope.$watchCollection(
                function() {return CategoriesService.categories},
                function(newVal) {self.categories = newVal}
        );

        /**
         * Indicates that the category data is being loaded.
         * @type {boolean}
         */
        self.loading = true;

        /**
         * Query for filtering categories.
         * @type {string}
         */
        self.searchQuery = "";

        /**
         * Navigates to the details page for the specified category.
         *
         * @param {Object} category the category
         */
        self.editCategory = function(category) {
            StateService.navigateToCategoryDetails(category.id);
        };

        // Register Add button
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    if (AccessControlService.hasAction(AccessControlService.CATEGORIES_EDIT, actionSet.actions)) {
                        AddButtonService.registerAddButton('categories', function() {
                            StateService.navigateToCategoryDetails(null);
                        });
                    }
                });

        // Refresh list of categories
        CategoriesService.reload()
                .then(function() {
                    self.loading = false;
                });
    };

    angular.module(MODULE_FEED_MGR).controller('CategoriesController', CategoriesController);
}());

