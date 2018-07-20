import * as angular from 'angular';
import AccessControlService from '../../services/AccessControlService';
import { Component, Inject } from "@angular/core";
import {CategoriesService} from "../services/CategoriesService";
import { Transition, StateService } from '@uirouter/core';

// const moduleName = require('feed-mgr/categories/module-name');

@Component({
    selector: 'category-details-controller',
    templateUrl: 'js/feed-mgr/categories/category-details.html'
})
export class CategoryDetailsController {

    /**
    * Indicates if the category is currently being loaded.
    * @type {boolean} {@code true} if the category is being loaded, or {@code false} if it has finished loading
    */
    loadingCategory: boolean = true;
    showAccessControl: boolean = false;
    model: any = {};
    
    /**
     * Manages the Category Details page for creating and editing categories.
     *
     * @param $scope the application model
     * @param $transition$ the URL parameters
     * @param CategoriesService the category service
     * @constructor
     */
    constructor(private categoriesService: CategoriesService, 
                private accessControlService: AccessControlService,
                @Inject("$injector") private $injector: any,
                private state: StateService) {

        // $scope.$watch(
        //     () => {
        //         return categoriesService.model
        //     },
        //     (newModel: any, oldModel: any) => {
        //         this.model = newModel;
        //         if (oldModel && oldModel.id == null && newModel.id != null) {
        //             this.checkAccessControl();
        //         }
        //     },
        //     true
        // );
    }

    ngOnInit() {

        // Load the list of categories
        if (this.categoriesService.categories.length === 0) {
            this.categoriesService.reload().then(() => this.onLoad());
        } else {
            this.onLoad();
        }
        this.checkAccessControl();
    }

    getIconColorStyle(iconColor: any) {
        return { 'fill': iconColor };
    };
    /**
    * Loads the category data once the list of categories has loaded.
    */
    onLoad=()=> {
        if (angular.isString(this.state.params.categoryId)) {
            this.model = this.categoriesService.model = this.categoriesService.findCategory(this.state.params.categoryId);
            if (angular.isDefined(this.categoriesService.model)) {
                this.categoriesService.model["loadingRelatedFeeds"] = true;
                this.categoriesService.populateRelatedFeeds(this.categoriesService.model).then((category: any) => {
                    category.loadingRelatedFeeds = false;
                });
            }
            this.loadingCategory = false;
        } else {
            this.categoriesService.getUserFields()
                .then((userFields: any) => {
                    this.categoriesService.model = this.categoriesService.newCategory();
                    this.categoriesService.model["userProperties"] = userFields;
                    this.loadingCategory = false;
                });
        }
    };
    checkAccessControl=() =>{
        if (this.accessControlService.isEntityAccessControlled()) {
            //Apply the entity access permissions... only showAccessControl if the user can change permissions
            this.$injector.get("$q").when(this.accessControlService.hasPermission(AccessControlService.CATEGORIES_ACCESS, this.model, AccessControlService.ENTITY_ACCESS.CATEGORY.CHANGE_CATEGORY_PERMISSIONS)).then(
                (access: any) => {
                    this.showAccessControl = access;
                });
        }
    }

}