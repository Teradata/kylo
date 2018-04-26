define(["require", "exports", "angular", "../../services/AccessControlService"], function (require, exports, angular, AccessControlService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/categories/module-name');
    var CategoriesController = /** @class */ (function () {
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
        function CategoriesController($scope, accessControlService, AddButtonService, CategoriesService, StateService) {
            this.$scope = $scope;
            this.accessControlService = accessControlService;
            this.AddButtonService = AddButtonService;
            this.CategoriesService = CategoriesService;
            this.StateService = StateService;
            var self = this;
            /**
             * List of categories.
             * @type {Array.<Object>}
             */
            self.categories = [];
            $scope.$watchCollection(function () { return CategoriesService.categories; }, function (newVal) { self.categories = newVal; });
            $scope.getIconColorStyle = function (color) {
                return { 'fill': color };
            };
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
            self.editCategory = function (category) {
                StateService.FeedManager().Category().navigateToCategoryDetails(category.id);
            };
            // Register Add button
            accessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                if (accessControlService.hasAction(AccessControlService_1.default.CATEGORIES_EDIT, actionSet.actions)) {
                    AddButtonService.registerAddButton('categories', function () {
                        StateService.FeedManager().Category().navigateToCategoryDetails(null);
                    });
                }
            });
            // Refresh list of categories
            CategoriesService.reload()
                .then(function () {
                self.loading = false;
            });
        }
        ;
        return CategoriesController;
    }());
    exports.CategoriesController = CategoriesController;
    angular.module(moduleName).controller('CategoriesController', ["$scope", "AccessControlService", "AddButtonService", "CategoriesService", "StateService", CategoriesController]);
});
//# sourceMappingURL=CategoriesController.js.map