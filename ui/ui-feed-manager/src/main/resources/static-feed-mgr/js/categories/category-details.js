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
