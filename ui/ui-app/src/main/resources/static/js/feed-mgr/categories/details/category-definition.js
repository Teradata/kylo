define(["require", "exports", "angular", "underscore", "../../../services/AccessControlService"], function (require, exports, angular, _, AccessControlService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/categories/module-name');
    var CategoryDefinitionController = /** @class */ (function () {
        function CategoryDefinitionController($scope, $mdDialog, $mdToast, $q, $timeout, $window, accessControlService, EntityAccessControlService, CategoriesService, StateService, FeedSecurityGroups, FeedService) {
            var _this = this;
            this.$scope = $scope;
            this.$mdDialog = $mdDialog;
            this.$mdToast = $mdToast;
            this.$q = $q;
            this.$timeout = $timeout;
            this.$window = $window;
            this.accessControlService = accessControlService;
            this.EntityAccessControlService = EntityAccessControlService;
            this.CategoriesService = CategoriesService;
            this.StateService = StateService;
            this.FeedSecurityGroups = FeedSecurityGroups;
            this.FeedService = FeedService;
            /**
             * Manages the Category Definition section of the Category Details page.
             *
             * @constructor
             * @param $scope the application model
             * @param $mdDialog the Angular Material dialog service
             * @param $mdToast the Angular Material toast service
             * @param {AccessControlService} AccessControlService the access control service
             * @param CategoriesService the category service
             * @param StateService the URL service
             * @param FeedSecurityGroups the feed security groups service
             * @param FeedService the feed service
             */
            /**
             * The Angular form for validation
             * @type {{}}
             */
            this.categoryForm = {};
            /**
             * Indicates if the category definition may be edited.
             * @type {boolean}
             */
            this.allowEdit = false;
            /**
             * Indicates the user has the permission to delete
             * @type {boolean}
             */
            this.allowDelete = false;
            this.securityGroupsEnabled = false;
            this.systemNameEditable = false;
            /**
             * Prevent users from creating categories with these names
             * @type {string[]}
             */
            this.reservedCategoryNames = ['thinkbig'];
            this.editModel = angular.copy(CategoriesService.model);
            this.isEditable = !angular.isString(CategoriesService.model.id);
            this.model = CategoriesService.model;
            this.categorySecurityGroups = FeedSecurityGroups;
            this.securityGroupChips = {};
            this.securityGroupChips.selectedItem = null;
            this.securityGroupChips.searchText = null;
            FeedSecurityGroups.isEnabled().then(function (isValid) {
                _this.securityGroupsEnabled = isValid;
            });
            this.checkAccessPermissions();
            // Fetch the existing categories
            CategoriesService.reload().then(function (response) {
                if (_this.editModel) {
                    _this.validateDisplayName();
                    _this.validateSystemName();
                }
            });
            // Watch for changes to name
            $scope.$watch(function () {
                return _this.editModel.name;
            }, function () {
                _this.validateDisplayName();
            });
            // Watch for changes to system name
            $scope.$watch(function () {
                return _this.editModel.systemName;
            }, function () {
                _this.validateSystemName();
            });
        }
        /**
             * System Name states:
             * !new 0, !editable 0, !feeds 0 - not auto generated, can change
             * !new 0, !editable 0,  feeds 1 - not auto generated, cannot change
             * !new 0,  editable 1, !feeds 0 - not auto generated, editable
             * !new 0,  editable 1,  feeds 1 - invalid state (cannot be editable with feeds)
             *  new 1, !editable 0, !feeds 0 - auto generated, can change
             *  new 1, !editable 0,  feeds 1 - invalid state (new cannot be with feeds)
             *  new 1,  editable 1, !feeds 0 - not auto generated, editable
             *  new 1,  editable 1,  feeds 1 - invalid state (cannot be editable with feeds)
             */
        CategoryDefinitionController.prototype.getSystemNameDescription = function () {
            // console.log("this.isNewCategory() = " + this.isNewCategory());
            // console.log("this.isSystemNameEditable() = " + this.isSystemNameEditable());
            // console.log("this.hasFeeds() = " + this.hasFeeds());
            if (!this.isNewCategory() && !this.isSystemNameEditable() && this.hasNoFeeds()) {
                return "Can be customised";
            }
            if (!this.isNewCategory() && !this.isSystemNameEditable() && this.hasFeeds()) {
                return "Cannot be customised because Category has Feeds";
            }
            if (!this.isNewCategory() && this.isSystemNameEditable() && this.hasNoFeeds()) {
                return "System name is now editable";
            }
            if (!this.isNewCategory() && this.isSystemNameEditable() && this.hasFeeds()) {
                return ""; //invalid state, cannot be both editable and have feeds!
            }
            if (this.isNewCategory() && !this.isSystemNameEditable() && this.hasNoFeeds()) {
                return "Auto generated from Category Name, can be customised";
            }
            if (this.isNewCategory() && !this.isSystemNameEditable() && this.hasFeeds()) {
                return ""; //invalid state, cannot be new and already have feeds
            }
            if (this.isNewCategory() && this.isSystemNameEditable() && this.hasNoFeeds()) {
                return "System name is now editable";
            }
            if (this.isNewCategory() && this.isSystemNameEditable() && this.hasFeeds()) {
                return ""; //invalid state, cannot be new with feeds
            }
            return "";
        };
        ;
        CategoryDefinitionController.prototype.isNewCategory = function () {
            return this.editModel.id == undefined;
        };
        ;
        CategoryDefinitionController.prototype.isSystemNameEditable = function () {
            return this.systemNameEditable;
        };
        ;
        CategoryDefinitionController.prototype.hasFeeds = function () {
            return !this.hasNoFeeds();
        };
        ;
        CategoryDefinitionController.prototype.hasNoFeeds = function () {
            return (!angular.isArray(this.model.relatedFeedSummaries) || this.model.relatedFeedSummaries.length === 0);
        };
        ;
        CategoryDefinitionController.prototype.allowEditSystemName = function () {
            var _this = this;
            this.systemNameEditable = true;
            this.$timeout(function () {
                var systemNameInput = _this.$window.document.getElementById("systemName");
                if (systemNameInput) {
                    systemNameInput.focus();
                }
            });
        };
        ;
        CategoryDefinitionController.prototype.splitSecurityGroups = function () {
            if (this.model.securityGroups) {
                return _.map(this.model.securityGroups, function (securityGroup) {
                    return securityGroup.name;
                }).join(",");
            }
        };
        ;
        /**
         * Indicates if the category can be deleted.
         * @return {boolean} {@code true} if the category can be deleted, or {@code false} otherwise
         */
        CategoryDefinitionController.prototype.canDelete = function () {
            return this.allowDelete && (angular.isString(this.model.id) && this.hasNoFeeds());
        };
        ;
        /**
         * Returns to the category list page if creating a new category.
         */
        CategoryDefinitionController.prototype.onCancel = function () {
            this.systemNameEditable = false;
            if (!angular.isString(this.model.id)) {
                this.StateService.FeedManager().Category().navigateToCategories();
            }
        };
        ;
        /**
         * Deletes this category.
         */
        CategoryDefinitionController.prototype.onDelete = function () {
            var _this = this;
            var name = this.editModel.name;
            this.CategoriesService.delete(this.editModel).then(function () {
                _this.systemNameEditable = false;
                _this.CategoriesService.reload();
                _this.$mdToast.show(_this.$mdToast.simple()
                    .textContent('Successfully deleted the category ' + name)
                    .hideDelay(3000));
                //redirect
                _this.StateService.FeedManager().Category().navigateToCategories();
            }, function (err) {
                _this.$mdDialog.show(_this.$mdDialog.alert()
                    .clickOutsideToClose(true)
                    .title('Unable to delete the category')
                    .textContent('Unable to delete the category ' + name + ". " + err.data.message)
                    .ariaLabel('Unable to delete the category')
                    .ok('Got it!'));
            });
        };
        ;
        /**
         * Switches to "edit" mode.
         */
        CategoryDefinitionController.prototype.onEdit = function () {
            this.editModel = angular.copy(this.model);
        };
        ;
        /**
         * Check for duplicate display and system names.
         */
        CategoryDefinitionController.prototype.validateDisplayAndSystemName = function () {
            var _this = this;
            var displayNameExists = false;
            var systemNameExists = false;
            var newDisplayName = this.editModel.name;
            this.FeedService.getSystemName(newDisplayName)
                .then(function (response) {
                var systemName = response.data;
                if (_this.isNewCategory() && !_this.isSystemNameEditable()) {
                    _this.editModel.systemName = systemName;
                }
                displayNameExists = _.some(_this.CategoriesService.categories, function (category) {
                    return (_this.editModel.id == null || (_this.editModel.id != null && category.id != _this.editModel.id)) && category.name === newDisplayName;
                });
                systemNameExists = _.some(_this.CategoriesService.categories, function (category) {
                    return (_this.editModel.id == null || (_this.editModel.id != null && category.id != _this.editModel.id)) && category.systemName === _this.editModel.systemName;
                });
                var reservedCategoryDisplayName = newDisplayName && _.indexOf(_this.reservedCategoryNames, newDisplayName.toLowerCase()) >= 0;
                var reservedCategorySystemName = _this.editModel.systemName && _.indexOf(_this.reservedCategoryNames, _this.editModel.systemName.toLowerCase()) >= 0;
                if (_this.categoryForm) {
                    if (_this.categoryForm['categoryName']) {
                        _this.categoryForm['categoryName'].$setValidity('duplicateDisplayName', !displayNameExists);
                        _this.categoryForm['categoryName'].$setValidity('reservedCategoryName', !reservedCategoryDisplayName);
                    }
                    if (_this.categoryForm['systemName']) {
                        _this.categoryForm['systemName'].$setValidity('duplicateSystemName', !systemNameExists);
                        _this.categoryForm['systemName'].$setValidity('reservedCategoryName', !reservedCategorySystemName);
                    }
                }
            });
        };
        ;
        /**
         * Check for duplicate display and system names.
         */
        CategoryDefinitionController.prototype.validateDisplayName = function () {
            var _this = this;
            var nameExists = false;
            var newName = this.editModel.name;
            this.FeedService.getSystemName(newName)
                .then(function (response) {
                var systemName = response.data;
                if (_this.isNewCategory() && !_this.isSystemNameEditable()) {
                    _this.editModel.systemName = systemName;
                }
                nameExists = _.some(_this.CategoriesService.categories, function (category) {
                    return (_this.editModel.id == null || (_this.editModel.id != null && category.id != _this.editModel.id)) && category.name === newName;
                });
                var reservedCategoryDisplayName = newName && _.indexOf(_this.reservedCategoryNames, newName.toLowerCase()) >= 0;
                if (_this.categoryForm) {
                    if (_this.categoryForm['categoryName']) {
                        _this.categoryForm['categoryName'].$setValidity('duplicateDisplayName', !nameExists);
                        _this.categoryForm['categoryName'].$setValidity('reservedCategoryName', !reservedCategoryDisplayName);
                    }
                }
            });
        };
        ;
        /**
         * Check for duplicate display and system names.
         */
        CategoryDefinitionController.prototype.validateSystemName = function () {
            var _this = this;
            var nameExists = false;
            var newName = this.editModel.systemName;
            this.FeedService.getSystemName(newName)
                .then(function (response) {
                var systemName = response.data;
                nameExists = _.some(_this.CategoriesService.categories, function (category) {
                    return (_this.editModel.id == null || (_this.editModel.id != null && category.id != _this.editModel.id)) && category.systemName === systemName;
                });
                var reservedCategorySystemName = _this.editModel.systemName && _.indexOf(_this.reservedCategoryNames, _this.editModel.systemName.toLowerCase()) >= 0;
                var invalidName = newName !== systemName;
                if (_this.categoryForm) {
                    if (_this.categoryForm['systemName']) {
                        _this.categoryForm['systemName'].$setValidity('invalidName', !invalidName);
                        _this.categoryForm['systemName'].$setValidity('duplicateSystemName', !nameExists);
                        _this.categoryForm['systemName'].$setValidity('reservedCategoryName', !reservedCategorySystemName);
                    }
                }
            });
        };
        ;
        /**
         * Saves the category definition.
         */
        CategoryDefinitionController.prototype.onSave = function () {
            var _this = this;
            var model = angular.copy(this.CategoriesService.model);
            model.name = this.editModel.name;
            model.systemName = this.editModel.systemName;
            model.description = this.editModel.description;
            model.icon = this.editModel.icon;
            model.iconColor = this.editModel.iconColor;
            model.userProperties = (this.model.id === null) ? this.editModel.userProperties : null;
            model.securityGroups = this.editModel.securityGroups;
            model.allowIndexing = this.editModel.allowIndexing;
            this.CategoriesService.save(model).then(function (response) {
                _this.systemNameEditable = false;
                _this.CategoriesService.update(response.data);
                _this.model = _this.CategoriesService.model = response.data;
                _this.$mdToast.show(_this.$mdToast.simple()
                    .textContent('Saved the Category')
                    .hideDelay(3000));
                _this.checkAccessPermissions();
            }, function (err) {
                _this.$mdDialog.show(_this.$mdDialog.alert()
                    .clickOutsideToClose(true)
                    .title("Save Failed")
                    .textContent("The category '" + model.name + "' could not be saved. " + err.data.message)
                    .ariaLabel("Failed to save category")
                    .ok("Got it!"));
            });
        };
        ;
        /**
         * Shows the icon picker dialog.
         */
        CategoryDefinitionController.prototype.showIconPicker = function () {
            var _this = this;
            var self = this;
            this.$mdDialog.show({
                controller: 'IconPickerDialog',
                templateUrl: 'js/common/icon-picker-dialog/icon-picker-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    iconModel: this.editModel
                }
            })
                .then(function (msg) {
                if (msg) {
                    _this.editModel.icon = msg.icon;
                    _this.editModel.iconColor = msg.color;
                }
            });
        };
        ;
        CategoryDefinitionController.prototype.getIconColorStyle = function (iconColor) {
            return { 'fill': iconColor };
        };
        ;
        CategoryDefinitionController.prototype.checkAccessPermissions = function () {
            var _this = this;
            //Apply the entity access permissions
            this.$q.when(this.accessControlService.hasPermission(AccessControlService_1.default.CATEGORIES_EDIT, this.model, AccessControlService_1.default.ENTITY_ACCESS.CATEGORY.EDIT_CATEGORY_DETAILS)).then(function (access) {
                _this.allowEdit = access;
            });
            this.$q.when(this.accessControlService.hasPermission(AccessControlService_1.default.CATEGORIES_EDIT, this.model, AccessControlService_1.default.ENTITY_ACCESS.CATEGORY.DELETE_CATEGORY)).then(function (access) {
                _this.allowDelete = access;
            });
        };
        CategoryDefinitionController.$inject = ["$scope", "$mdDialog", "$mdToast", "$q", "$timeout",
            "$window", "AccessControlService", "EntityAccessControlService",
            "CategoriesService", "StateService", "FeedSecurityGroups", "FeedService"];
        return CategoryDefinitionController;
    }());
    exports.CategoryDefinitionController = CategoryDefinitionController;
    angular.module(moduleName).controller('CategoryDefinitionController', ["$scope", "$mdDialog", "$mdToast", "$q", "$timeout", "$window", "AccessControlService", "EntityAccessControlService", "CategoriesService", "StateService", "FeedSecurityGroups", "FeedService",
        CategoryDefinitionController]);
    angular.module(moduleName).component("thinkbigCategoryDefinition", {
        controller: "CategoryDefinitionController",
        controllerAs: "vm",
        templateUrl: "js/feed-mgr/categories/details/category-definition.html"
    });
});
//# sourceMappingURL=category-definition.js.map