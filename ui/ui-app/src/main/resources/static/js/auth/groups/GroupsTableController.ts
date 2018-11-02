import * as angular from 'angular';
import * as _ from 'underscore';
import {moduleName} from "../module-name";
import UserService from "../services/UserService";
import {AddButtonService} from  "../../services/AddButtonService";
import {DefaultPaginationDataService} from  "../../services/PaginationDataService";
import {StateService} from  "../../services/StateService";
import {DefaultTableOptionsService} from  "../../services/TableOptionsService";
import "../module";
import "../module-require";
/**
 * Identifier for this page.
 * @type {string}
 */
const PAGE_NAME:string = "groups";

export class GroupsTableController implements ng.IComponentController {
    /**
     * Page title.
     * @type {string}
     */
    cardTitle:string = "Groups";
    /**
     * Index of the current page.
     * @type {number}
     */
    currentPage:number = this.PaginationDataService.currentPage(PAGE_NAME) || 1;
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
    pageName:string = PAGE_NAME;
    /**
     * Helper for table pagination.
     * @type {*}
     */
    paginationData = this.getPaginatedData();
    sortOptions    = this.getSortOptions();
    /**
     * List of groups.
     * @type {Array.<GroupPrincipal>}
     */
    groups:any[] = [];
    // options:any = this.TableOptionsService.currentOption;
    /**
     * Type of view for the table
     * @type {any}
     */
    viewType:any = this.PaginationDataService.viewType(PAGE_NAME);
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
     * @param option the sort order
     */
    selectedTableOption =(option: any)=> {
        var sortString = this.TableOptionsService.toSortString(option);
        this.PaginationDataService.sort(this.pageName, sortString);
        this.TableOptionsService.toggleSort(this.pageName, option);
        this.TableOptionsService.setSortOption(this.pageName, sortString);
    };
    /**
     * Gets the display name of the specified group. Defaults to the system name if the display name is blank.
     * @param group the group
     * @returns {string} the display name
     */
    getDisplayName(group:any) {
        return (angular.isString(group.title) && group.title.length > 0) ? group.title : group.systemName;
    };
     /**
     * Updates the order of the table.
     * @param order the sort order
     */
    onOrderChange=(order:any)=> {
        this.PaginationDataService.sort(this.pageName, order);
        this.TableOptionsService.setSortOption(this.pageName, order);
    };
    /**
     * Updates the pagination of the table.
     * @param page the page number
     */
    onPaginationChange =(page:any)=> {
        this.PaginationDataService.currentPage(this.pageName, null, page);
        this.currentPage = page;
    };

    /**
     * Gets the title of the specified group. Defaults to the system name if the title is blank.
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
    groupDetails=(group:any)=>{
        this.StateService.Auth.navigateToGroupDetails(group.systemName);
    };
    //$inject = ["$scope","AddButtonService","PaginationDataService","StateService","TableOptionsService","UserService"];
    static readonly $inject = ["$scope","AddButtonService","PaginationDataService","StateService","TableOptionsService","UserService"];
    /**
     * Displays a list of groups in a table.
     *
     * @constructor
     * @param $scope the application model
     * @param AddButtonService the Add button service
     * @param PaginationDataService the table pagination service
     * @param StateService the page state service
     * @param TableOptionsService the table options service
     * @param UserService the user service
     */
    constructor (
        private $scope:angular.IScope,
        private AddButtonService:AddButtonService,
        private PaginationDataService:DefaultPaginationDataService,
        private StateService:StateService,
        private TableOptionsService:DefaultTableOptionsService,
        private UserService:UserService
    ) {
        // Notify pagination service of changes to view type
        this.$scope.$watch(() => { //$onChanges()
            return this.viewType;
        }, (viewType) => {
            PaginationDataService.viewType(PAGE_NAME, viewType);
        });
        // Register Add button
        AddButtonService.registerAddButton('groups', () => {
            StateService.Auth.navigateToGroupDetails();
        });
        // Get the list of users and groups
        UserService.getGroups().then((groups:any) => {
             this.groups = groups;
             this.loading = false;
        });
    }
}
const module = angular.module(moduleName)
.component("groupsTableController", {
        controller: GroupsTableController,
        controllerAs: "vm",
        templateUrl: "./groups-table.html"
    });
//.controller("GroupsTableController", GroupsTableController]);
export default module;