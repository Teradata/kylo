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
            var _this = this;
            this.$scope = $scope;
            this.accessControlService = accessControlService;
            this.AddButtonService = AddButtonService;
            this.CategoriesService = CategoriesService;
            this.StateService = StateService;
            /**
            * List of categories.
            * @type {Array.<Object>}
            */
            this.categories = [];
            /**
            * Indicates that the category data is being loaded.
            * @type {boolean}
            */
            this.loading = true;
            /**
            * Query for filtering categories.
            * @type {string}
            */
            this.searchQuery = "";
            this.$scope.$watchCollection(function () { return CategoriesService.categories; }, function (newVal) { _this.categories = newVal; });
            $scope.getIconColorStyle = function (color) {
                return { 'fill': color };
            };
            // Register Add button
            this.accessControlService.getUserAllowedActions()
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
                _this.loading = false;
            });
        }
        ;
        /**
        * Navigates to the details page for the specified category.
        *
        * @param {Object} category the category
        */
        CategoriesController.prototype.editCategory = function (category) {
            this.StateService.FeedManager().Category().navigateToCategoryDetails(category.id);
        };
        ;
        CategoriesController.$inject = ["$scope", "AccessControlService", "AddButtonService", "CategoriesService", "StateService"];
        return CategoriesController;
    }());
    exports.CategoriesController = CategoriesController;
    angular.module(moduleName).component('CategoriesController', {
        controller: CategoriesController,
        controllerAs: "vm",
        templateUrl: 'js/feed-mgr/categories/categories.html'
    });
});
//# sourceMappingURL=CategoriesController.js.map