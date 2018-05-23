import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/categories/module-name');

/**
 * Creates a directive for the Category Definition section.
 *
 * @returns {Object} the directive
 */
function thinkbigCategoryDefinition() {
    return {
        controller: "CategoryDefinitionController",
        controllerAs: "vm",
        restrict: "E",
        scope: {},
        templateUrl: "js/feed-mgr/categories/details/category-definition.html"
    };
}


export class CategoryDefinitionController {
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
        categoryForm:any;


        /**
         * Indicates if the category definition may be edited.
         * @type {boolean}
         */
        allowEdit:boolean;

        /**
         * Indicates the user has the permission to delete
         * @type {boolean}
         */
        allowDelete:boolean;

        /**
         * Category data used in "edit" mode.
         * @type {CategoryModel}
         */
        editModel:any;

        /**
         * Indicates if the view is in "edit" mode.
         * @type {boolean} {@code true} if in "edit" mode or {@code false} if in "normal" mode
         */
        isEditable:any;

        /**
         * Category data used in "normal" mode.
         * @type {CategoryModel}
         */
        model:any;

        categorySecurityGroups:any;
        securityGroupChips:any;
        securityGroupsEnabled:boolean;
        systemNameEditable:boolean;

        getSystemNameDescription:any;
        isNewCategory:any;
        isSystemNameEditable:any;
        hasFeeds:any;
        hasNoFeeds:any;
        allowEditSystemName:any;
        splitSecurityGroups:any;
        canDelete:any;
        onCancel:any;
        onDelete:any;
        onEdit:any;
        validateDisplayAndSystemName:any;
        validateDisplayName:any;
        validateSystemName:any;
        onSave:any;
        showIconPicker:any;
        getIconColorStyle:any;

     constructor(private $scope:any, private $mdDialog:any, private $mdToast:any, private $q:any, private $timeout:any
        , private $window:any, private AccessControlService:any, private EntityAccessControlService:any
        , private CategoriesService:any, private StateService:any, private FeedSecurityGroups:any, private FeedService:any) {
        var self = this;

        /**
         * The Angular form for validation
         * @type {{}}
         */
        self.categoryForm = {};

        /**
         * Prevent users from creating categories with these names
         * @type {string[]}
         */
        var reservedCategoryNames = ['thinkbig']

        /**
         * Indicates if the category definition may be edited.
         * @type {boolean}
         */
        self.allowEdit = false;

        /**
         * Indicates the user has the permission to delete
         * @type {boolean}
         */
        self.allowDelete = false;

        /**
         * Category data used in "edit" mode.
         * @type {CategoryModel}
         */
        self.editModel = angular.copy(CategoriesService.model);

        /**
         * Indicates if the view is in "edit" mode.
         * @type {boolean} {@code true} if in "edit" mode or {@code false} if in "normal" mode
         */
        self.isEditable = !angular.isString(CategoriesService.model.id);

        /**
         * Category data used in "normal" mode.
         * @type {CategoryModel}
         */
        self.model = CategoriesService.model;

        this.categorySecurityGroups = FeedSecurityGroups;
        self.securityGroupChips = {};
        self.securityGroupChips.selectedItem = null;
        self.securityGroupChips.searchText = null;
        self.securityGroupsEnabled = false;
        self.systemNameEditable = false;

        FeedSecurityGroups.isEnabled().then(function (isValid:any) {
                self.securityGroupsEnabled = isValid;
            }
        );

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
        self.getSystemNameDescription = function() {
            // console.log("self.isNewCategory() = " + self.isNewCategory());
            // console.log("self.isSystemNameEditable() = " + self.isSystemNameEditable());
            // console.log("self.hasFeeds() = " + self.hasFeeds());

            if (!self.isNewCategory() && !self.isSystemNameEditable() && self.hasNoFeeds()) {
                return "Can be customized";
            }
            if (!self.isNewCategory() && !self.isSystemNameEditable() && self.hasFeeds()) {
                return "Cannot be customized because category has feeds";
            }
            if (!self.isNewCategory() && self.isSystemNameEditable() && self.hasNoFeeds()) {
                return "System name is now editable";
            }
            if (!self.isNewCategory() && self.isSystemNameEditable() && self.hasFeeds()) {
                return ""; //invalid state, cannot be both editable and have feeds!
            }
            if (self.isNewCategory() && !self.isSystemNameEditable() && self.hasNoFeeds()) {
                return "Auto generated from category name, can be customized";
            }
            if (self.isNewCategory() && !self.isSystemNameEditable() && self.hasFeeds()) {
                return ""; //invalid state, cannot be new and already have feeds
            }
            if (self.isNewCategory() && self.isSystemNameEditable() && self.hasNoFeeds()) {
                return "System name is now editable";
            }
            if (self.isNewCategory() && self.isSystemNameEditable() && self.hasFeeds()) {
                return ""; //invalid state, cannot be new with feeds
            }
            return "";
        };

        self.isNewCategory = function() {
            return self.editModel.id == undefined;
        };

        self.isSystemNameEditable = function() {
            return self.systemNameEditable;
        };

        self.hasFeeds = function() {
            return !self.hasNoFeeds();
        };

        self.hasNoFeeds = function() {
            return (!angular.isArray(self.model.relatedFeedSummaries) || self.model.relatedFeedSummaries.length === 0);
        };

        self.allowEditSystemName = function() {
            self.systemNameEditable = true;
            $timeout(function() {
                var systemNameInput = $window.document.getElementById("systemName");
                if(systemNameInput) {
                    systemNameInput.focus();
                }
            });
        };

        self.splitSecurityGroups = function () {
            if (self.model.securityGroups) {
                return _.map(self.model.securityGroups, function (securityGroup:any) {
                    return securityGroup.name
                }).join(",");
            }
        };

        /**
         * Indicates if the category can be deleted.
         * @return {boolean} {@code true} if the category can be deleted, or {@code false} otherwise
         */
        self.canDelete = function () {
            return self.allowDelete && (angular.isString(self.model.id) && self.hasNoFeeds());
        };

        /**
         * Returns to the category list page if creating a new category.
         */
        self.onCancel = function () {
            self.systemNameEditable = false;
            if (!angular.isString(self.model.id)) {
                StateService.FeedManager().Category().navigateToCategories();
            }
        };

        /**
         * Deletes this category.
         */
        self.onDelete = function () {
            var name = self.editModel.name;
            CategoriesService.delete(self.editModel).then(function () {
                self.systemNameEditable = false;
                CategoriesService.reload();
                $mdToast.show(
                    $mdToast.simple()
                        .textContent('Successfully deleted the category ' + name)
                        .hideDelay(3000)
                );
                //redirect
                StateService.FeedManager().Category().navigateToCategories();
            }, function (err:any) {
                $mdDialog.show(
                    $mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title('Unable to delete the category')
                        .textContent('Unable to delete the category ' + name + ". " + err.data.message)
                        .ariaLabel('Unable to delete the category')
                        .ok('Got it!')
                );
            });
        };

        /**
         * Switches to "edit" mode.
         */
        self.onEdit = function () {
            self.editModel = angular.copy(self.model);
        };

        /**
         * Check for duplicate display and system names.
         */
        self.validateDisplayAndSystemName = function() {
            var displayNameExists = false;
            var systemNameExists = false;
            var newDisplayName = self.editModel.name;

            FeedService.getSystemName(newDisplayName)
                .then(function (response:any) {
                    var systemName = response.data;
                    if (self.isNewCategory() && !self.isSystemNameEditable()) {
                        self.editModel.systemName = systemName;
                    }

                    displayNameExists = _.some(CategoriesService.categories, function (category:any) {
                        return  (self.editModel.id == null || (self.editModel.id != null && category.id != self.editModel.id)) && category.name === newDisplayName;
                    });

                    systemNameExists = _.some(CategoriesService.categories, function (category:any) {
                        return  (self.editModel.id == null || (self.editModel.id != null && category.id != self.editModel.id)) && category.systemName === self.editModel.systemName;
                    });

                    var reservedCategoryDisplayName = newDisplayName && _.indexOf(reservedCategoryNames, newDisplayName.toLowerCase()) >= 0;
                    var reservedCategorySystemName = self.editModel.systemName && _.indexOf(reservedCategoryNames, self.editModel.systemName.toLowerCase()) >= 0;

                    if (self.categoryForm) {
                        if (self.categoryForm['categoryName']) {
                            self.categoryForm['categoryName'].$setValidity('duplicateDisplayName', !displayNameExists);
                            self.categoryForm['categoryName'].$setValidity('reservedCategoryName', !reservedCategoryDisplayName);
                        }
                        if (self.categoryForm['systemName']) {
                            self.categoryForm['systemName'].$setValidity('duplicateSystemName', !systemNameExists);
                            self.categoryForm['systemName'].$setValidity('reservedCategoryName', !reservedCategorySystemName);
                        }
                    }
                });
        };

        /**
         * Check for duplicate display and system names.
         */
        self.validateDisplayName = function() {
            var nameExists = false;
            var newName = self.editModel.name;

            FeedService.getSystemName(newName)
                .then(function (response:any) {
                    var systemName = response.data;
                    if (self.isNewCategory() && !self.isSystemNameEditable()) {
                        self.editModel.systemName = systemName;
                    }

                    nameExists = _.some(CategoriesService.categories, function (category:any) {
                        return  (self.editModel.id == null || (self.editModel.id != null && category.id != self.editModel.id)) && category.name === newName;
                    });

                    var reservedCategoryDisplayName = newName && _.indexOf(reservedCategoryNames, newName.toLowerCase()) >= 0;

                    if (self.categoryForm) {
                        if (self.categoryForm['categoryName']) {
                            self.categoryForm['categoryName'].$setValidity('duplicateDisplayName', !nameExists);
                            self.categoryForm['categoryName'].$setValidity('reservedCategoryName', !reservedCategoryDisplayName);
                        }
                    }
                });
        };

        /**
         * Check for duplicate display and system names.
         */
        self.validateSystemName = function() {
            var nameExists = false;
            var newName = self.editModel.systemName;

            FeedService.getSystemName(newName)
                .then(function (response:any) {
                    var systemName = response.data;

                    nameExists = _.some(CategoriesService.categories, function (category:any) {
                        return  (self.editModel.id == null || (self.editModel.id != null && category.id != self.editModel.id)) && category.systemName === systemName;
                    });

                    var reservedCategorySystemName = self.editModel.systemName && _.indexOf(reservedCategoryNames, self.editModel.systemName.toLowerCase()) >= 0;
                    var invalidName = newName !== systemName;
                    if (self.categoryForm) {
                        if (self.categoryForm['systemName']) {
                            self.categoryForm['systemName'].$setValidity('invalidName', !invalidName);
                            self.categoryForm['systemName'].$setValidity('duplicateSystemName', !nameExists);
                            self.categoryForm['systemName'].$setValidity('reservedCategoryName', !reservedCategorySystemName);
                        }
                    }
                });
        };

        /**
         * Saves the category definition.
         */
        self.onSave = function () {
            var model = angular.copy(CategoriesService.model);
            model.name = self.editModel.name;
            model.systemName = self.editModel.systemName;
            model.description = self.editModel.description;
            model.icon = self.editModel.icon;
            model.iconColor = self.editModel.iconColor;
            model.userProperties = (self.model.id === null) ? self.editModel.userProperties : null;
            model.securityGroups = self.editModel.securityGroups;
            model.allowIndexing = self.editModel.allowIndexing;

            CategoriesService.save(model).then(function (response:any) {
                self.systemNameEditable = false;
                CategoriesService.update(response.data);
                self.model = CategoriesService.model = response.data;
                $mdToast.show(
                    $mdToast.simple()
                        .textContent('Saved the Category')
                        .hideDelay(3000)
                );
                checkAccessPermissions();
            }, function (err:any) {
                $mdDialog.show(
                    $mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("Save Failed")
                        .textContent("The category '" + model.name + "' could not be saved. " + err.data.message)
                        .ariaLabel("Failed to save category")
                        .ok("Got it!")
                );
            });
        };

        /**
         * Shows the icon picker dialog.
         */
        self.showIconPicker = function () {
            var self = this;
            $mdDialog.show({
                controller: 'IconPickerDialog',
                templateUrl: 'js/common/icon-picker-dialog/icon-picker-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    iconModel: self.editModel
                }
            })
                .then(function (msg:any) {
                    if (msg) {
                        self.editModel.icon = msg.icon;
                        self.editModel.iconColor = msg.color;
                    }
                });
        };

        self.getIconColorStyle = function(iconColor:any) {
            return  {'fill': iconColor};
        };

        function checkAccessPermissions() {
            //Apply the entity access permissions
            $q.when(AccessControlService.hasPermission(AccessControlService.CATEGORIES_EDIT, self.model, AccessControlService.ENTITY_ACCESS.CATEGORY.EDIT_CATEGORY_DETAILS)).then(function (access:any) {
                self.allowEdit = access;
            });

            $q.when(AccessControlService.hasPermission(AccessControlService.CATEGORIES_EDIT, self.model, AccessControlService.ENTITY_ACCESS.CATEGORY.DELETE_CATEGORY)).then(function (access:any) {
                self.allowDelete = access;
            });
        }

        checkAccessPermissions();

        // Fetch the existing categories
        CategoriesService.reload().then(function (response:any) {
            if (self.editModel) {
                self.validateDisplayName();
                self.validateSystemName();
            }
        });

        // Watch for changes to name
        $scope.$watch(
            function () {
                return self.editModel.name
            },
            self.validateDisplayName
        );
        // Watch for changes to system name
        $scope.$watch(
            function () {
                return self.editModel.systemName
            },
            self.validateSystemName
        );
    }


}

angular.module(moduleName).controller('CategoryDefinitionController',
    ["$scope", "$mdDialog", "$mdToast", "$q", "$timeout", "$window", "AccessControlService", "EntityAccessControlService", "CategoriesService", "StateService", "FeedSecurityGroups", "FeedService",
     CategoryDefinitionController]);
angular.module(moduleName).directive('thinkbigCategoryDefinition', thinkbigCategoryDefinition);
