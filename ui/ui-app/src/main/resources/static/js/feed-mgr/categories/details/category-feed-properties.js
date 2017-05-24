define(['angular', 'feed-mgr/categories/module-name'], function (angular, moduleName) {
    /**
     * Manages the Category Feed Properties section of the Category Details page.
     *
     * @constructor
     * @param $scope the application model
     * @param $mdToast the toast service
     * @param {AccessControlService} AccessControlService the access control service
     * @param CategoriesService the category service
     */
    function CategoryFeedPropertiesController($scope, $mdToast, $q, AccessControlService, EntityAccessControlService, CategoriesService) {
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
            function () {
                return CategoriesService.model.id
            },
            function (newValue) {
                self.isNew = !angular.isString(newValue)
            }
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
        self.onEdit = function () {
            self.editModel = angular.copy(self.model);
        };

        /**
         * Saves the category properties.
         */
        self.onSave = function () {
            var model = angular.copy(CategoriesService.model);
            model.id = self.model.id;
            model.userFields = self.editModel.userFields;
            model.userProperties = null;

            CategoriesService.save(model).then(function (response) {
                self.model = CategoriesService.model = response.data;
                CategoriesService.reload();
                $mdToast.show(
                    $mdToast.simple()
                        .textContent('Saved the Category')
                        .hideDelay(3000)
                );
            }, function (err) {
                $mdDialog.show(
                    $mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("Save Failed")
                        .textContent("The category '" + model.name + "' could not be saved. " + err.data.message)
                        .ariaLabel("Failed to save category")
                        .ok("Got it!")
                );
            });
        };

        //Apply the entity access permissions
        $q.when(AccessControlService.hasPermission(AccessControlService.CATEGORIES_ADMIN,self.model,AccessControlService.ENTITY_ACCESS.CATEGORY.EDIT_CATEGORY_DETAILS)).then(function(access) {
            self.allowEdit = access;
        });

        /*
         // Fetch the allowed actions
         AccessControlService.getAllowedActions()
         .then(function(actionSet) {
         self.allowEdit = AccessControlService.hasAction(AccessControlService.CATEGORIES_ADMIN, actionSet.actions);
         });
         */
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
            templateUrl: 'js/feed-mgr/categories/details/category-feed-properties.html'
        };
    }

    angular.module(moduleName).controller('CategoryFeedPropertiesController',
        ["$scope", "$mdToast", "$q", "AccessControlService", "EntityAccessControlService", "CategoriesService", CategoryFeedPropertiesController]);
    angular.module(moduleName).directive('thinkbigCategoryFeedProperties', thinkbigFeedCategoryProperties);
});
