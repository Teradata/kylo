define(["require", "exports", "angular", "../../services/AccessControlService"], function (require, exports, angular, AccessControlService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/categories/module-name');
    var CategoryDetailsController = /** @class */ (function () {
        function CategoryDetailsController($scope, $q, CategoriesService, accessControlService) {
            var _this = this;
            this.$scope = $scope;
            this.$q = $q;
            this.CategoriesService = CategoriesService;
            this.accessControlService = accessControlService;
            /**
            * Indicates if the category is currently being loaded.
            * @type {boolean} {@code true} if the category is being loaded, or {@code false} if it has finished loading
            */
            this.loadingCategory = true;
            this.showAccessControl = false;
            this.model = {};
            $scope.$watch(function () {
                return CategoriesService.model;
            }, function (newModel, oldModel) {
                _this.model = newModel;
                if (oldModel && oldModel.id == null && newModel.id != null) {
                    _this.checkAccessControl();
                }
            }, true);
        }
        CategoryDetailsController.prototype.ngOnInit = function () {
            var _this = this;
            // Load the list of categories
            if (this.CategoriesService.categories.length === 0) {
                this.CategoriesService.reload().then(function () { return _this.onLoad(); });
            }
            else {
                this.onLoad();
            }
            this.checkAccessControl();
        };
        CategoryDetailsController.prototype.$onInit = function () {
            this.ngOnInit();
        };
        CategoryDetailsController.prototype.getIconColorStyle = function (iconColor) {
            return { 'fill': iconColor };
        };
        ;
        /**
        * Loads the category data once the list of categories has loaded.
        */
        CategoryDetailsController.prototype.onLoad = function () {
            var _this = this;
            if (angular.isString(this.$transition$.params().categoryId)) {
                this.model = this.CategoriesService.model = this.CategoriesService.findCategory(this.$transition$.params().categoryId);
                if (angular.isDefined(this.CategoriesService.model)) {
                    this.CategoriesService.model.loadingRelatedFeeds = true;
                    this.CategoriesService.populateRelatedFeeds(this.CategoriesService.model).then(function (category) {
                        category.loadingRelatedFeeds = false;
                    });
                }
                this.loadingCategory = false;
            }
            else {
                this.CategoriesService.getUserFields()
                    .then(function (userFields) {
                    _this.CategoriesService.model = _this.CategoriesService.newCategory();
                    _this.CategoriesService.model.userProperties = userFields;
                    _this.loadingCategory = false;
                });
            }
        };
        ;
        CategoryDetailsController.prototype.checkAccessControl = function () {
            var _this = this;
            if (this.accessControlService.isEntityAccessControlled()) {
                //Apply the entity access permissions... only showAccessControl if the user can change permissions
                this.$q.when(this.accessControlService.hasPermission(AccessControlService_1.default.CATEGORIES_ACCESS, this.model, AccessControlService_1.default.ENTITY_ACCESS.CATEGORY.CHANGE_CATEGORY_PERMISSIONS)).then(function (access) {
                    _this.showAccessControl = access;
                });
            }
        };
        /**
         * Manages the Category Details page for creating and editing categories.
         *
         * @param $scope the application model
         * @param $transition$ the URL parameters
         * @param CategoriesService the category service
         * @constructor
         */
        CategoryDetailsController.$inject = ["$scope", "$q", "CategoriesService", "AccessControlService"];
        return CategoryDetailsController;
    }());
    exports.CategoryDetailsController = CategoryDetailsController;
    angular.module(moduleName).component('categoryDetailsController', {
        bindings: {
            $transition$: "<"
        },
        controller: CategoryDetailsController,
        controllerAs: "vm",
        templateUrl: 'js/feed-mgr/categories/category-details.html'
    });
});
//# sourceMappingURL=category-details.js.map