define(["require", "exports", "angular", "../../services/AccessControlService"], function (require, exports, angular, AccessControlService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/categories/module-name');
    var CategoryDetailsController = /** @class */ (function () {
        /**
         * Manages the Category Details page for creating and editing categories.
         *
         * @param $scope the application model
         * @param $transition$ the URL parameters
         * @param CategoriesService the category service
         * @constructor
         */
        function CategoryDetailsController($scope, $transition$, $q, CategoriesService, accessControlService) {
            this.$scope = $scope;
            this.$transition$ = $transition$;
            this.$q = $q;
            this.CategoriesService = CategoriesService;
            this.accessControlService = accessControlService;
            var self = this;
            /**
             * Indicates if the category is currently being loaded.
             * @type {boolean} {@code true} if the category is being loaded, or {@code false} if it has finished loading
             */
            self.loadingCategory = true;
            self.showAccessControl = false;
            /**
             * Category data.
             * @type {CategoryModel}
             */
            self.model = {};
            $scope.$watch(function () {
                return CategoriesService.model;
            }, function (newModel, oldModel) {
                self.model = newModel;
                if (oldModel && oldModel.id == null && newModel.id != null) {
                    checkAccessControl();
                }
            }, true);
            /**
             * Loads the category data once the list of categories has loaded.
             */
            self.onLoad = function () {
                if (angular.isString($transition$.params().categoryId)) {
                    self.model = CategoriesService.model = CategoriesService.findCategory($transition$.params().categoryId);
                    if (angular.isDefined(CategoriesService.model)) {
                        CategoriesService.model.loadingRelatedFeeds = true;
                        CategoriesService.populateRelatedFeeds(CategoriesService.model).then(function (category) {
                            category.loadingRelatedFeeds = false;
                        });
                    }
                    self.loadingCategory = false;
                }
                else {
                    CategoriesService.getUserFields()
                        .then(function (userFields) {
                        CategoriesService.model = CategoriesService.newCategory();
                        CategoriesService.model.userProperties = userFields;
                        self.loadingCategory = false;
                    });
                }
            };
            self.getIconColorStyle = function (iconColor) {
                return { 'fill': iconColor };
            };
            // Load the list of categories
            if (CategoriesService.categories.length === 0) {
                CategoriesService.reload().then(self.onLoad);
            }
            else {
                self.onLoad();
            }
            function checkAccessControl() {
                if (accessControlService.isEntityAccessControlled()) {
                    //Apply the entity access permissions... only showAccessControl if the user can change permissions
                    $q.when(accessControlService.hasPermission(AccessControlService_1.default.CATEGORIES_ACCESS, self.model, AccessControlService_1.default.ENTITY_ACCESS.CATEGORY.CHANGE_CATEGORY_PERMISSIONS)).then(function (access) {
                        self.showAccessControl = access;
                    });
                }
            }
            checkAccessControl();
        }
        return CategoryDetailsController;
    }());
    exports.CategoryDetailsController = CategoryDetailsController;
    angular.module(moduleName).controller('CategoryDetailsController', ["$scope", "$transition$", "$q", "CategoriesService", "AccessControlService", CategoryDetailsController]);
});
//# sourceMappingURL=category-details.js.map