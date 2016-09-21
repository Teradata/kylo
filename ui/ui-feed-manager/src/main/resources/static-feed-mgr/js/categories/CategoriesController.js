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

