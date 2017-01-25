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
     * Manages the Category Feed Properties section of the Category Details page.
     *
     * @constructor
     * @param $scope the application model
     * @param $mdToast the toast service
     * @param {AccessControlService} AccessControlService the access control service
     * @param CategoriesService the category service
     */
    function CategoryFeedPropertiesController($scope, $mdToast, AccessControlService, CategoriesService) {
        var self = this;

        /**
         * Indicates if the properties may be edited.
         */
        self.allowEdit = false;

        /**
         * Category data used in "edit" mode.
         * @type {CategoryModel}
         */
        self.editModel = CategoriesService.newCategory();

        /**
         * Indicates if the view is in "edit" mode.
         * @type {boolean} {@code true} if in "edit" mode or {@code false} if in "normal" mode
         */
        self.isEditable = false;

        /**
         * Indicates of the category is new.
         * @type {boolean}
         */
        self.isNew = true;
        $scope.$watch(
                function() {return CategoriesService.model.id},
                function(newValue) {self.isNew = !angular.isString(newValue)}
        );

        /**
         * Indicates if the properties are valid and can be saved.
         * @type {boolean} {@code true} if all properties are valid, or {@code false} otherwise
         */
        self.isValid = true;

        /**
         * Category data used in "normal" mode.
         * @type {CategoryModel}
         */
        self.model = CategoriesService.model;

        /**
         * Switches to "edit" mode.
         */
        self.onEdit = function() {
            self.editModel = angular.copy(self.model);
        };

        /**
         * Saves the category properties.
         */
        self.onSave = function() {
            var model = angular.copy(CategoriesService.model);
            model.id = self.model.id;
            model.userFields = self.editModel.userFields;
            model.userProperties = null;

            CategoriesService.save(model).then(function(response) {
                self.model = CategoriesService.model = response.data;
                CategoriesService.reload();
                $mdToast.show(
                    $mdToast.simple()
                        .textContent('Saved the Category')
                        .hideDelay(3000)
                );
            });
        };

        // Fetch the allowed actions
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    self.allowEdit = AccessControlService.hasAction(AccessControlService.CATEGORIES_ADMIN, actionSet.actions);
                });
    }

    /**
     * Creates a directive for the Category Feed Properties section.
     *
     * @returns {Object} the directive
     */
    function thinkbigFeedCategoryProperties() {
        return {
            controller: "CategoryFeedPropertiesController",
            controllerAs: 'vm',
            restrict: "E",
            scope: {},
            templateUrl: 'js/categories/details/category-feed-properties.html'
        };
    }

    angular.module(MODULE_FEED_MGR).controller('CategoryFeedPropertiesController', CategoryFeedPropertiesController);
    angular.module(MODULE_FEED_MGR).directive('thinkbigCategoryFeedProperties', thinkbigFeedCategoryProperties);
})();
