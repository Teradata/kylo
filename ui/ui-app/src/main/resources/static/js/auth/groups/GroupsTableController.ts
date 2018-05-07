import * as angular from 'angular';
import * as _ from 'underscore';

import {UserService} from "../services/UserService";
import {moduleName} from "../module-name";

const PAGE_NAME:string = "groups";
//const moduleName = require('auth/module-name');

export default class GroupsTableController implements ng.IComponentController {
  /**
     * Page title.
     * @type {string}
     */
    cardTitle:string = "Groups";

    /**
     * Index of the current page.
     * @type {number}
     */
    currentPage:any = this.PaginationDataService.currentPage(PAGE_NAME) || 1;

    /**
     * Helper for table filtering.
     * @type {*}
     */
    filter:any = this.PaginationDataService.filter(PAGE_NAME);
     /**
     * Indicates that the table data is being loaded.
     * @type {boolean}
     */
    loading:boolean = true;

    /**
     * Identifier for this page.
     * @type {string}
     */
    pageName = PAGE_NAME;


    /**
     * Helper for table pagination.
     * @type {*}
     */
    paginationData  = this.getPaginatedData();
    sortOptions     = this.getSortOptions();
   // selectedTableOption = this.getSelectedTableOption(this.$scope);
  
    getPaginatedData () {
        var paginationData = this.PaginationDataService.paginationData(PAGE_NAME);
        this.PaginationDataService.setRowsPerPageOptions(PAGE_NAME, ['5', '10', '20', '50']);
        return paginationData;
    }
    getSortOptions() {
       var fields = { "Title": "title", "Description": "description", "Members": "memberCount" };
       var sortOptions = this.TableOptionsService.newSortOptions(PAGE_NAME, fields, "title", "asc");  
        var currentOption = this.TableOptionsService.getCurrentSort(PAGE_NAME);
        
        if (currentOption) {
            this.TableOptionsService.saveSortOption(PAGE_NAME, currentOption)
        }
        return sortOptions;
    }
    /**
     * Updates the order of the table.
     *
     * @param option the sort order
     */
    selectedTableOption =(option: any)=> {
  // getSelectedTableOption(option: any) {
        /*  this.TableOptionsService.toSortString(option)
                .then((sortString: any)=> {
                    this.PaginationDataService.sort(this.pageName, sortString);
                    this.TableOptionsService.toggleSort(this.pageName, option);
                    this.TableOptionsService.setSortOption(this.pageName, sortString);
                });*/
        var sortString = this.TableOptionsService.toSortString(option);
      // alert(this.options);
        this.PaginationDataService.sort(this.pageName, sortString);
        this.TableOptionsService.toggleSort(this.pageName, option);
        this.TableOptionsService.setSortOption(this.pageName, sortString);
    };
      /**
     * List of groups.
     * @type {Array.<GroupPrincipal>}
     */
    groups:any = [];

   // options:any = this.TableOptionsService.currentOption;
    /**
     * Type of view for the table.
     * @type {any}
     */
    viewType:any = this.PaginationDataService.viewType(PAGE_NAME);
     /**
     * Gets the display name of the specified group. Defaults to the system name if the display name is blank.
     *
     * @param group the group
     * @returns {string} the display name
     */
    getDisplayName(group:any) {
        return (angular.isString(group.title) && group.title.length > 0) ? group.title : group.systemName;
    };

     /**
     * Updates the order of the table.
     *
     * @param order the sort order
     */
    onOrderChange=(order:any)=> {
        this.PaginationDataService.sort(this.pageName, order);
        this.TableOptionsService.setSortOption(this.pageName, order);
    };
 /**
     * Updates the pagination of the table.
     *
     * @param page the page number
     */
    onPaginationChange =(page:any)=> {
        this.PaginationDataService.currentPage(this.pageName, null, page);
        this.currentPage = page;
    };

  

    /**
         * Gets the title of the specified group. Defaults to the system name if the title is blank.
         *
         * @param group the group
         * @returns {string} the title
         */
    getTitle = function(group: any) {
            return (angular.isString(group.title) && group.title.length > 0) ? group.title : group.systemName;
        };
     /**
     * Navigates to the details page for the specified user.
     *
     * @param user the user
     */
    groupDetails (group:any) {
        this.StateService.Auth().navigateToGroupDetails(group.systemName);
    };
    
        constructor (
        private $scope:angular.IScope,
        private AddButtonService:any,
        private PaginationDataService:any,
        private StateService:any,
        private TableOptionsService:any,
        private UserService:any
    ) {

        // Notify pagination service of changes to view type
        this.$scope.$watch(() => {
            return this.viewType;
        }, (viewType) => {
            this.PaginationDataService.viewType(PAGE_NAME, viewType);
        });

        // Register Add button
        this.AddButtonService.registerAddButton('groups', () => {
            this.StateService.Auth().navigateToGroupDetails();
        });

        // Get the list of users and groups
        this.UserService.getGroups().then((groups:any) => {
             this.groups = groups;
             this.loading = false;
        });
             
    }
}
angular.module(moduleName)
    .controller("GroupsTableController", ["$scope","AddButtonService","PaginationDataService","StateService","TableOptionsService","UserService",GroupsTableController]);

