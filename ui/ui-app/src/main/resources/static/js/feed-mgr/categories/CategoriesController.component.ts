import * as angular from 'angular';
import { Component, Input, Inject, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import * as _ from "underscore";
import CategoriesService from '../services/CategoriesService';
import AccessControlService from '../../services/AccessControlService';
import AddButtonService from '../../services/AddButtonService';
import StateService from '../../services/StateService';
const moduleName = require('./module-name');

@Component({
    selector: 'categories-controller',
    templateUrl: 'js/feed-mgr/categories/categories.html',
    styles: [`
        mat-card {
            padding: 8px !important;
        }
        .feed-categories mat-card {
            border: 2px solid #EEEEEE;
            box-shadow: none;
        }
        .feed-categories mat-card mat-card-title {
            padding: 8px;
            max-height: 60px;
        }
        mat-card mat-card-title {
            display: -webkit-box;
            display: -webkit-flex;
            display: flex;
            -webkit-box-flex: 1;
            -webkit-flex: 1 1 auto;
            flex: 1 1 auto;
            -webkit-box-orient: horizontal;
            -webkit-box-direction: normal;
            -webkit-flex-direction: row;
            flex-direction: row;
        }
    `]
})
export class CategoriesControllerComponent{

    /**
    * List of categories.
    * @type {Array.<Object>}
    */
    categories: any = [];
    /**
    * Indicates that the category data is being loaded.
    * @type {boolean}
    */
    loading: boolean = true;
    /**
    * Query for filtering categories.
    * @type {string}
    */
    searchQuery: string = "";
    getIconColorStyle: any;

    ngOnInit(): void {
        this.getIconColorStyle = (color: any) => {
            return { 'fill': color };
        };

        // Register Add button
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) =>{
                if (this.accessControlService.hasAction(AccessControlService.CATEGORIES_EDIT, actionSet.actions)) {
                    this.addButtonService.registerAddButton('categories', ()=> {
                        this.stateService.FeedManager().Category().navigateToCategoryDetails(null);
                    });
                }
            });

        // Refresh list of categories
        this.categoriesService.reload()
            .then((categories:any) => {
                this.loading = false;
                this.categories = categories;
            });
    }

    /**
     * Displays a list of categories.
     *
     * @constructor
     * @param {AccessControlService} AccessControlService the access control service
     * @param AddButtonService the Add button service
     * @param CategoriesService the categories service
     * @param StateService the page state service
     */
    constructor(private accessControlService: AccessControlService, 
                private addButtonService: AddButtonService, 
                private stateService: StateService,
                private categoriesService: CategoriesService) {

                    // this.$scope.$watchCollection(
                    //     () => { return CategoriesService.categories },
                    //     (newVal: any) => { this.categories = newVal }
                    // );
                    
                }
    /**
    * Navigates to the details page for the specified category.
    *
    * @param {Object} category the category
    */
    editCategory(category: any) {
        this.stateService.FeedManager().Category().navigateToCategoryDetails(category.id);
    };

}

