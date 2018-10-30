import * as angular from 'angular';
import * as _ from "underscore";
import {AccessControlService} from '../../../services/AccessControlService';
import {EntityAccessControlService} from '../../shared/entity-access-control/EntityAccessControlService';
const moduleName = require('../module-name');

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
        categoryForm:any = {};


        /**
         * Indicates if the category definition may be edited.
         * @type {boolean}
         */
        allowEdit:boolean = false;

        /**
         * Indicates the user has the permission to delete
         * @type {boolean}
         */
        allowDelete:boolean = false;

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
        securityGroupsEnabled:boolean = false;
        systemNameEditable:boolean = false;
        /**
         * Prevent users from creating categories with these names
         * @type {string[]}
         */
        reservedCategoryNames:any = ['thinkbig']

     static readonly $inject = ["$scope", "$mdDialog", "$mdToast", "$q", "$timeout",
                                "$window", "AccessControlService", "EntityAccessControlService",
                                 "CategoriesService", "StateService", "FeedSecurityGroups", "FeedService"];

     constructor(private $scope:IScope, private $mdDialog:angular.material.IDialogService, private $mdToast:angular.material.IToastService, private $q:angular.IQService, private $timeout:angular.ITimeoutService
        , private $window:angular.IWindowService, private accessControlService:AccessControlService, private entityAccessControlService:EntityAccessControlService
        , private CategoriesService:any, private StateService:any, private FeedSecurityGroups:any, private FeedService:any) {

        this.editModel = angular.copy(CategoriesService.model);

        this.isEditable = !angular.isString(CategoriesService.model.id);

        this.model = CategoriesService.model;

        this.categorySecurityGroups = FeedSecurityGroups;
        this.securityGroupChips = {};
        this.securityGroupChips.selectedItem = null;
        this.securityGroupChips.searchText = null;

        FeedSecurityGroups.isEnabled().then( (isValid:any) => {
                this.securityGroupsEnabled = isValid;
            }
        );

        this.checkAccessPermissions();

        // Fetch the existing categories
        CategoriesService.reload().then((response:any) =>{
            if (this.editModel) {
                this.validateDisplayName();
                this.validateSystemName();
            }
        });

        // Watch for changes to name
        $scope.$watch(
            () =>{
                return this.editModel.name
            },
            () =>{
                this.validateDisplayName()
            }
        );
        // Watch for changes to system name
        $scope.$watch(
            () =>{
                return this.editModel.systemName
            },
            () =>{
                this.validateSystemName()
            }
        );
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
        getSystemNameDescription() {
            // console.log("this.isNewCategory() = " + this.isNewCategory());
            // console.log("this.isSystemNameEditable() = " + this.isSystemNameEditable());
            // console.log("this.hasFeeds() = " + this.hasFeeds());

            if (!this.isNewCategory() && !this.isSystemNameEditable() && this.hasNoFeeds()) {
                return "Can be customized";
            }
            if (!this.isNewCategory() && !this.isSystemNameEditable() && this.hasFeeds()) {
                return "Cannot be customized because category has feeds";
            }
            if (!this.isNewCategory() && this.isSystemNameEditable() && this.hasNoFeeds()) {
                return "System name is now editable";
            }
            if (!this.isNewCategory() && this.isSystemNameEditable() && this.hasFeeds()) {
                return ""; //invalid state, cannot be both editable and have feeds!
            }
            if (this.isNewCategory() && !this.isSystemNameEditable() && this.hasNoFeeds()) {
                return "Auto generated from category name, can be customized";
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

        isNewCategory() {
            return this.editModel.id == undefined;
        };

        isSystemNameEditable() {
            return this.systemNameEditable;
        };

        hasFeeds() {
            return !this.hasNoFeeds();
        };

        hasNoFeeds() {
            return (!angular.isArray(this.model.relatedFeedSummaries) || this.model.relatedFeedSummaries.length === 0);
        };

        allowEditSystemName() {
            this.systemNameEditable = true;
            this.$timeout(()=> {
                var systemNameInput = this.$window.document.getElementById("systemName");
                if(systemNameInput) {
                    systemNameInput.focus();
                }
            });
        };

        splitSecurityGroups() {
            if (this.model.securityGroups) {
                return _.map(this.model.securityGroups, (securityGroup:any) =>{
                    return securityGroup.name
                }).join(",");
            }
        };

        /**
         * Indicates if the category can be deleted.
         * @return {boolean} {@code true} if the category can be deleted, or {@code false} otherwise
         */
        canDelete() {
            return this.allowDelete && (angular.isString(this.model.id) && this.hasNoFeeds());
        };

        /**
         * Returns to the category list page if creating a new category.
         */
        onCancel() {
            this.systemNameEditable = false;
            if (!angular.isString(this.model.id)) {
                this.StateService.FeedManager().Category().navigateToCategories();
            }
        };

        /**
         * Deletes this category.
         */
        onDelete() {
            var name = this.editModel.name;
            this.CategoriesService.delete(this.editModel).then( () => {
                this.systemNameEditable = false;
                this.CategoriesService.reload();
                this.$mdToast.show(
                    this.$mdToast.simple()
                        .textContent('Successfully deleted the category ' + name)
                        .hideDelay(3000)
                );
                //redirect
                this.StateService.FeedManager().Category().navigateToCategories();
            }, (err:any) => {
                this.$mdDialog.show(
                    this.$mdDialog.alert()
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
        onEdit() {
            this.editModel = angular.copy(this.model);
        };

        /**
         * Check for duplicate display and system names.
         */
        validateDisplayAndSystemName() {
            var displayNameExists = false;
            var systemNameExists = false;
            var newDisplayName = this.editModel.name;

            this.FeedService.getSystemName(newDisplayName)
                .then( (response:any) =>{
                    var systemName = response.data;
                    if (this.isNewCategory() && !this.isSystemNameEditable()) {
                        this.editModel.systemName = systemName;
                    }

                    displayNameExists = _.some(this.CategoriesService.categories, (category:any) =>{
                        return  (this.editModel.id == null || (this.editModel.id != null && category.id != this.editModel.id)) && category.name === newDisplayName;
                    });

                    systemNameExists = _.some(this.CategoriesService.categories, (category:any) =>{
                        return  (this.editModel.id == null || (this.editModel.id != null && category.id != this.editModel.id)) && category.systemName === this.editModel.systemName;
                    });

                    var reservedCategoryDisplayName = newDisplayName && _.indexOf(this.reservedCategoryNames, newDisplayName.toLowerCase()) >= 0;
                    var reservedCategorySystemName = this.editModel.systemName && _.indexOf(this.reservedCategoryNames, this.editModel.systemName.toLowerCase()) >= 0;

                    if (this.categoryForm) {
                        if (this.categoryForm['categoryName']) {
                            this.categoryForm['categoryName'].$setValidity('duplicateDisplayName', !displayNameExists);
                            this.categoryForm['categoryName'].$setValidity('reservedCategoryName', !reservedCategoryDisplayName);
                        }
                        if (this.categoryForm['systemName']) {
                            this.categoryForm['systemName'].$setValidity('duplicateSystemName', !systemNameExists);
                            this.categoryForm['systemName'].$setValidity('reservedCategoryName', !reservedCategorySystemName);
                        }
                    }
                });
        };

        /**
         * Check for duplicate display and system names.
         */
        validateDisplayName() {
            var nameExists = false;
            var newName = this.editModel.name;

            this.FeedService.getSystemName(newName)
                .then((response:any) =>{
                    var systemName = response.data;
                    if (this.isNewCategory() && !this.isSystemNameEditable()) {
                        this.editModel.systemName = systemName;
                    }

                    nameExists = _.some(this.CategoriesService.categories, (category:any) =>{
                        return  (this.editModel.id == null || (this.editModel.id != null && category.id != this.editModel.id)) && category.name === newName;
                    });

                    var reservedCategoryDisplayName = newName && _.indexOf(this.reservedCategoryNames, newName.toLowerCase()) >= 0;

                    if (this.categoryForm) {
                        if (this.categoryForm['categoryName']) {
                            this.categoryForm['categoryName'].$setValidity('duplicateDisplayName', !nameExists);
                            this.categoryForm['categoryName'].$setValidity('reservedCategoryName', !reservedCategoryDisplayName);
                        }
                    }
                });
        };

        /**
         * Check for duplicate display and system names.
         */
        validateSystemName() {
            var nameExists = false;
            var newName = this.editModel.systemName;

            this.FeedService.getSystemName(newName)
                .then((response:any) =>{
                    var systemName = response.data;

                    nameExists = _.some(this.CategoriesService.categories, (category:any) =>{
                        return  (this.editModel.id == null || (this.editModel.id != null && category.id != this.editModel.id)) && category.systemName === systemName;
                    });

                    var reservedCategorySystemName = this.editModel.systemName && _.indexOf(this.reservedCategoryNames, this.editModel.systemName.toLowerCase()) >= 0;
                    var invalidName = newName !== systemName;
                    if (this.categoryForm) {
                        if (this.categoryForm['systemName']) {
                            this.categoryForm['systemName'].$setValidity('invalidName', !invalidName);
                            this.categoryForm['systemName'].$setValidity('duplicateSystemName', !nameExists);
                            this.categoryForm['systemName'].$setValidity('reservedCategoryName', !reservedCategorySystemName);
                        }
                    }
                });
        };

        /**
         * Saves the category definition.
         */
        onSave() {
            var model = angular.copy(this.CategoriesService.model);
            model.name = this.editModel.name;
            model.systemName = this.editModel.systemName;
            model.description = this.editModel.description;
            model.icon = this.editModel.icon;
            model.iconColor = this.editModel.iconColor;
            model.userProperties = (this.model.id === null) ? this.editModel.userProperties : null;
            model.securityGroups = this.editModel.securityGroups;
            model.allowIndexing = this.editModel.allowIndexing;

            this.CategoriesService.save(model).then((response:any) =>{
                this.systemNameEditable = false;
                this.CategoriesService.update(response.data);
                this.model = this.CategoriesService.model = response.data;
                this.$mdToast.show(
                    this.$mdToast.simple()
                        .textContent('Saved the Category')
                        .hideDelay(3000)
                );
                this.checkAccessPermissions();
            }, (err:any) =>{
                this.$mdDialog.show(
                    this.$mdDialog.alert()
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
        showIconPicker() {
            var self = this;
            this.$mdDialog.show({
                controller: 'IconPickerDialog',
                templateUrl: '../../../common/icon-picker-dialog/icon-picker-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    iconModel: this.editModel
                }
            })
                .then((msg:any) =>{
                    if (msg) {
                        this.editModel.icon = msg.icon;
                        this.editModel.iconColor = msg.color;
                    }
                });
        };

        getIconColorStyle(iconColor:any) {
            return  {'fill': iconColor};
        };

        checkAccessPermissions() {
            //Apply the entity access permissions
            this.$q.when(this.accessControlService.hasPermission(AccessControlService.CATEGORIES_EDIT, this.model, AccessControlService.ENTITY_ACCESS.CATEGORY.EDIT_CATEGORY_DETAILS)).then((access:any) =>{
                this.allowEdit = access;
            });

            this.$q.when(this.accessControlService.hasPermission(AccessControlService.CATEGORIES_EDIT, this.model, AccessControlService.ENTITY_ACCESS.CATEGORY.DELETE_CATEGORY)).then((access:any) =>{
                this.allowDelete = access;
            });
        }
    }
    angular.module(moduleName).component("thinkbigCategoryDefinition",{
        controller: CategoryDefinitionController,
        controllerAs: "vm",
        templateUrl: "./category-definition.html"
});

