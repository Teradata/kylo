define(["require", "exports", "angular", "../../../services/AccessControlService"], function (require, exports, angular, AccessControlService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/categories/module-name');
    var CategoryAccessControlController = /** @class */ (function () {
        function CategoryAccessControlController($scope, $q, $mdToast, CategoriesService, accessControlService, EntityAccessControlService, $mdDialog) {
            var _this = this;
            this.$scope = $scope;
            this.$q = $q;
            this.$mdToast = $mdToast;
            this.CategoriesService = CategoriesService;
            this.accessControlService = accessControlService;
            this.EntityAccessControlService = EntityAccessControlService;
            this.$mdDialog = $mdDialog;
            this.categoryAccessControlForm = {};
            /**
             * Indicates if the properties may be edited.
             */
            this.allowEdit = false;
            /**
             * Indicates if the view is in "edit" mode.
             * @type {boolean} {@code true} if in "edit" mode or {@code false} if in "normal" mode
             */
            this.isEditable = false;
            /**
             * Indicates of the category is new.
             * @type {boolean}
             */
            this.isNew = true;
            this.model = CategoriesService.model;
            if (CategoriesService.model.roleMemberships == undefined) {
                CategoriesService.model.roleMemberships = this.model.roleMemberships = [];
            }
            if (CategoriesService.model.feedRoleMemberships == undefined) {
                CategoriesService.model.feedRoleMemberships = this.model.feedRoleMemberships = [];
            }
            /**
             * Category data used in "edit" mode.
             * @type {CategoryModel}
             */
            this.editModel = CategoriesService.newCategory();
            $scope.$watch(function () {
                return CategoriesService.model.id;
            }, function (newValue) {
                _this.isNew = !angular.isString(newValue);
            });
            /**
             * Category data used in "normal" mode.
             * @type {CategoryModel}
             */
            this.model = CategoriesService.model;
            //Apply the entity access permissions
            $q.when(accessControlService.hasPermission(AccessControlService_1.default.CATEGORIES_EDIT, this.model, AccessControlService_1.default.ENTITY_ACCESS.CATEGORY.CHANGE_CATEGORY_PERMISSIONS))
                .then(function (access) {
                _this.allowEdit = access;
            });
        }
        /**
             * Switches to "edit" mode.
             */
        CategoryAccessControlController.prototype.onEdit = function () {
            this.editModel = angular.copy(this.model);
        };
        ;
        /**
             * Saves the category .
             */
        CategoryAccessControlController.prototype.onSave = function () {
            var _this = this;
            var model = angular.copy(this.CategoriesService.model);
            model.roleMemberships = this.editModel.roleMemberships;
            model.feedRoleMemberships = this.editModel.feedRoleMemberships;
            model.owner = this.editModel.owner;
            model.allowIndexing = this.editModel.allowIndexing;
            this.EntityAccessControlService.updateRoleMembershipsForSave(model.roleMemberships);
            this.EntityAccessControlService.updateRoleMembershipsForSave(model.feedRoleMemberships);
            //TODO Open a Dialog showing Category is Saving progress
            this.CategoriesService.save(model).then(function (response) {
                _this.model = _this.CategoriesService.model = response.data;
                //set the editable flag to false after the save is complete.
                //this will flip the directive to read only mode and call the entity-access#init() method to requery the accesss control for this entity
                _this.isEditable = false;
                _this.CategoriesService.update(response.data);
                _this.$mdToast.show(_this.$mdToast.simple()
                    .textContent('Saved the Category')
                    .hideDelay(3000));
            }, function (err) {
                //keep editable active if an error occurred
                _this.isEditable = true;
                _this.$mdDialog.show(_this.$mdDialog.alert()
                    .clickOutsideToClose(true)
                    .title("Save Failed")
                    .textContent("The category '" + model.name + "' could not be saved. " + err.data.message)
                    .ariaLabel("Failed to save category")
                    .ok("Got it!"));
            });
        };
        ;
        CategoryAccessControlController.$inject = ["$scope", "$q", "$mdToast", "CategoriesService", "AccessControlService", "EntityAccessControlService", "$mdDialog"];
        return CategoryAccessControlController;
    }());
    exports.CategoryAccessControlController = CategoryAccessControlController;
    angular.module(moduleName).
        component("thinkbigCategoryAccessControl", {
        bindings: {
            stepIndex: '@'
        },
        controllerAs: 'vm',
        controller: CategoryAccessControlController,
        templateUrl: 'js/feed-mgr/categories/details/category-access-control.html',
    });
});
//# sourceMappingURL=category-access-control.js.map