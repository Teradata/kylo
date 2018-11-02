import * as angular from 'angular';
import * as _ from "underscore";
import {AccessControlService} from '../../services/AccessControlService';
const moduleName = require('./module-name');

export class CategoriesController {

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

    static readonly $inject = ["$scope", "AccessControlService", "AddButtonService", "CategoriesService", "StateService"];

    /**
     * Displays a list of categories.
     *
     * @constructor
     * @param $scope the application model
     * @param {AccessControlService} AccessControlService the access control service
     * @param AddButtonService the Add button service
     * @param CategoriesService the categories service
     * @param StateService the page state service
     */
    constructor(private $scope: IScope, private accessControlService: AccessControlService, private AddButtonService: any
        , private CategoriesService: any, private StateService: any) {

        this.$scope.$watchCollection(
            () => { return CategoriesService.categories },
            (newVal: any) => { this.categories = newVal }
        );

        $scope.getIconColorStyle = (color: any) => {
            let fillColor = (!color || color == '' ? '#90CAF9' : color);
            return { 'fill': fillColor };
        };

        $scope.getColorStyle = (color: any) => {
            let fillColor = (!color || color == '' ? '#90CAF9' : color);
            return { 'background-color': fillColor };
        };

        // Register Add button
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) =>{
                if (accessControlService.hasAction(AccessControlService.CATEGORIES_EDIT, actionSet.actions)) {
                    AddButtonService.registerAddButton('categories', ()=> {
                        StateService.FeedManager().Category().navigateToCategoryDetails(null);
                    });
                }
            });

        // Refresh list of categories
        CategoriesService.reload()
            .then(() => {
                this.loading = false;
            });
    };
    /**
    * Navigates to the details page for the specified category.
    *
    * @param {Object} category the category
    */
    editCategory(category: any) {
        this.StateService.FeedManager().Category().navigateToCategoryDetails(category.id);
    };

}
const module = angular.module(moduleName).component('categoriesController',  {
    controller: CategoriesController,
    controllerAs: "vm",
    templateUrl: './categories.html'   
});

export default module;