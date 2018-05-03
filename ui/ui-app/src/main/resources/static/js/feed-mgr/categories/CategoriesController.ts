import * as angular from 'angular';
import * as _ from "underscore";
import AccessControlService from '../../services/AccessControlService';
const moduleName = require('feed-mgr/categories/module-name');


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
    constructor(private $scope: any, private accessControlService: AccessControlService, private AddButtonService: any
        , private CategoriesService: any, private StateService: any) {

        this.$scope.$watchCollection(
            () => { return CategoriesService.categories },
            (newVal: any) => { this.categories = newVal }
        );

        $scope.getIconColorStyle = (color: any) => {
            return { 'fill': color };
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
angular.module(moduleName).controller('CategoriesController', ["$scope", "AccessControlService", "AddButtonService", "CategoriesService", "StateService", CategoriesController]);

