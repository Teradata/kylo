(function() {
    /**
     * Manages the Category Properties section of the Category Details page.
     *
     * @constructor
     * @param $scope the application model
     * @param CategoriesService the category service
     */
    function CategoryPropertiesController($scope, $mdToast, CategoriesService) {
        var self = this;

        /**
         * Indicates if the edit icon is visible.
         * @type {boolean} {@code true} if the edit icon is visible, or {@code false} if hidden
         */
        self.allowEdit = false;
        $scope.$watch(
                function() { return CategoriesService.model.id; },
                function(newValue) { self.allowEdit = angular.isString(newValue); }
        );

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
            model.userProperties = self.editModel.userProperties;

            CategoriesService.save(model).then(function(response) {
                self.model = CategoriesService.model = response.data;
                CategoriesService.reload();
                $mdToast.show(
                    $mdToast.simple()
                        .textContent("Saved the Category")
                        .hideDelay(3000)
                );
            });
        }
    }

    /**
     * Creates a directive for the Category Properties section.
     *
     * @returns {Object} the directive
     */
    function thinkbigCategoryProperties() {
        return {
            controller: "CategoryPropertiesController",
            controllerAs: 'vm',
            restrict: "E",
            scope: {},
            templateUrl: "js/categories/details/category-properties.html"
        };
    }

    angular.module(MODULE_FEED_MGR).controller("CategoryPropertiesController", CategoryPropertiesController);
    angular.module(MODULE_FEED_MGR).directive("thinkbigCategoryProperties", thinkbigCategoryProperties);
})();
