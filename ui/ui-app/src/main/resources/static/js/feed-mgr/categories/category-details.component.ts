import * as angular from 'angular';
import AccessControlService from '../../services/AccessControlService';
import { Component, Inject } from "@angular/core";
import CategoriesService from "../services/CategoriesService";
import { Transition, StateService } from '@uirouter/core';
import { Subscription } from 'rxjs/Subscription';

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
    
    subscription: Subscription;

    /**
     * Manages the Category Details page for creating and editing categories.
     *
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
        if (this.categoriesService.categories.length === 0) {
            this.categoriesService.reload().subscribe(() => this.onLoad());
        } else {
            this.onLoad();
        }
        this.checkAccessControl();
        
        this.subscription = this.categoriesService.modelSubject.subscribe((newModel: any) => {
            let oldModel = this.model;
            if (oldModel && oldModel.id == null && newModel.id != null) {
                this.checkAccessControl();
            }
        });

    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
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
            this.categoriesService.setModel(this.categoriesService.model);
            if (angular.isDefined(this.categoriesService.model)) {
                this.categoriesService.model["loadingRelatedFeeds"] = true;
                this.categoriesService.setModel(this.categoriesService.model);
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
                    this.categoriesService.setModel(this.categoriesService.model);
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