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

        /**
         * Loads the category data once the list of categories has loaded.
         */
        self.onLoad = function() {
            if (angular.isString($stateParams.categoryId)) {
                self.model = CategoriesService.model = CategoriesService.findCategory($stateParams.categoryId);
                CategoriesService.getRelatedFeeds(CategoriesService.model);
            }
            self.loadingCategory = false;
        };

        // Load the list of categories
        CategoriesService.model = CategoriesService.newCategory();

        if (CategoriesService.categories.length === 0) {
            CategoriesService.reload().then(self.onLoad);
        } else {
            self.onLoad();
        }
    }

    angular.module(MODULE_FEED_MGR).controller('CategoryDetailsController', CategoryDetailsController);
}());
