import * as angular from 'angular';
import * as _ from "underscore";
import {AccessControlService} from '../../../services/AccessControlService';
import  {EntityAccessControlService}  from '../../shared/entity-access-control/EntityAccessControlService';
const moduleName = require('../module-name');




export class CategoryAccessControlController {


    model: any;
    categoryAccessControlForm: any = {};
    /**
     * Indicates if the properties may be edited.
     */
    allowEdit: boolean = false;

    /**
     * Category data used in "edit" mode.
     * @type {CategoryModel}
     */
    editModel: any;

    /**
     * Indicates if the view is in "edit" mode.
     * @type {boolean} {@code true} if in "edit" mode or {@code false} if in "normal" mode
     */
    isEditable: any= false;

    /**
     * Indicates of the category is new.
     * @type {boolean}
     */
    isNew: boolean = true;

    static readonly $inject = ["$scope", "$q", "$mdToast", "CategoriesService", "AccessControlService", "EntityAccessControlService", "$mdDialog"];
    constructor(private $scope: IScope, private $q: angular.IQService, private $mdToast: angular.material.IToastService, private CategoriesService: any
        , private accessControlService: AccessControlService, private entityAccessControlService: EntityAccessControlService, private $mdDialog: angular.material.IDialogService) {


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

        $scope.$watch(
            () => {
                return CategoriesService.model.id
            },
            (newValue: any) => {
                this.isNew = !angular.isString(newValue)
            }
        );

        /**
         * Category data used in "normal" mode.
         * @type {CategoryModel}
         */
        this.model = CategoriesService.model;


        //Apply the entity access permissions
        $q.when(accessControlService.hasPermission(AccessControlService.CATEGORIES_EDIT, this.model,
                                                         AccessControlService.ENTITY_ACCESS.CATEGORY.CHANGE_CATEGORY_PERMISSIONS))
                                                                                                    .then((access: any) => {
            this.allowEdit = access;
        });

    }

    /**
         * Switches to "edit" mode.
         */
    onEdit() {
        this.editModel = angular.copy(this.model);
    };
    /**
         * Saves the category .
         */
    onSave() {
        var model = angular.copy(this.CategoriesService.model);
        model.roleMemberships = this.editModel.roleMemberships;
        model.feedRoleMemberships = this.editModel.feedRoleMemberships;
        model.owner = this.editModel.owner;
        model.allowIndexing = this.editModel.allowIndexing;
        this.entityAccessControlService.updateRoleMembershipsForSave(model.roleMemberships);
        this.entityAccessControlService.updateRoleMembershipsForSave(model.feedRoleMemberships);

        //TODO Open a Dialog showing Category is Saving progress
        this.CategoriesService.save(model).then((response: any) => {
            this.model = this.CategoriesService.model = response.data;
            //set the editable flag to false after the save is complete.
            //this will flip the directive to read only mode and call the entity-access#init() method to requery the accesss control for this entity
            this.isEditable = false;
            this.CategoriesService.update(response.data);
            this.$mdToast.show(
                this.$mdToast.simple()
                    .textContent('Saved the Category')
                    .hideDelay(3000)
            );
        }, (err: any) => {
            //keep editable active if an error occurred
            this.isEditable = true;
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

}

angular.module(moduleName).
    component("thinkbigCategoryAccessControl", {
        bindings: {
            stepIndex: '@'
        },
        controllerAs: 'vm',
        controller: CategoryAccessControlController,
        templateUrl: './category-access-control.html',
    });
