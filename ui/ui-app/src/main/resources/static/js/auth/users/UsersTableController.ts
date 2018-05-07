import * as angular from 'angular';
import * as _ from 'underscore';
import {UserService} from "../services/UserService";
const PAGE_NAME:string = "users";
import {moduleName} from "../module-name";
//const moduleName = require('auth/module-name');
export default class UsersTableController implements ng.IComponentController {

    /**
     * Page title.
     * @type {string}
     */
    cardTitle:string = "Users";

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
     * Mapping of group names to group metadata.
     * @type {Object.<string, GroupPrincipal>}
     */
    groups:any = {};

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
    paginationData = this.getPaginatedData()

    sortOptions = this.getSortOptions();
    getPaginatedData () {
        var paginationData = this.PaginationDataService.paginationData(PAGE_NAME);
        this.PaginationDataService.setRowsPerPageOptions(PAGE_NAME, ['5', '10', '20', '50']);
        return paginationData;
    }

    getSortOptions() {
        var fields = {"Display Name": "displayName", "Email Address": "email", "State": "enabled", "Groups": "groups"};
        var sortOptions = this.TableOptionsService.newSortOptions(PAGE_NAME, fields, "displayName", "asc");
        var currentOption = this.TableOptionsService.getCurrentSort(PAGE_NAME);
        if (currentOption) {
            this.TableOptionsService.saveSortOption(PAGE_NAME, currentOption)
        }
        return sortOptions;
    }


    /**
     * List of users.
     * @type {Array.<UserPrincipal>}
     */
    users:any = [];

    /**
     * Type of view for the table.
     * @type {any}
     */
    viewType:any = this.PaginationDataService.viewType(PAGE_NAME);

    /**
     * Gets the display name of the specified user. Defaults to the system name if the display name is blank.
     *
     * @param user the user
     * @returns {string} the display name
     */
    getDisplayName(user:any) {
        return (angular.isString(user.displayName) && user.displayName.length > 0) ? user.displayName : user.systemName;
    };

    /**
     * Gets the title for each group the user belongs to.
     *
     * @param user the user
     * @returns {Array.<string>} the group titles
     */
    getGroupTitles(user:any) {
        return _.map(user.groups, (group:any) => {
            if (angular.isDefined(this.groups[group]) && angular.isString(this.groups[group].title)) {
                return this.groups[group].title;
            } else {
                return group;
            }
        });
    };

    /**
     * Updates the order of the table.
     *
     * @param order the sort order
     */
    onOrderChange=(order:any)=>{
        this.PaginationDataService.sort(this.pageName, order);
        this.TableOptionsService.setSortOption(this.pageName, order);
    };

    /**
     * Updates the pagination of the table.
     *
     * @param page the page number
     */
    onPaginationChange= (page:any)=> {
        this.PaginationDataService.currentPage(this.pageName, null, page);
        this.currentPage = page;
    };

    /**
     * Updates the order of the table.
     *
     * @param option the sort order
     */
    selectedTableOption =(option:any) =>{
        var sortString = this.TableOptionsService.toSortString(option);
        this.PaginationDataService.sort(this.pageName, sortString);
        this.TableOptionsService.toggleSort(this.pageName, option);
        this.TableOptionsService.setSortOption(this.pageName, sortString);
    };

    /**
     * Navigates to the details page for the specified user.
     *
     * @param user the user
     */
    userDetails (user:any) {
        this.StateService.Auth().navigateToUserDetails(user.systemName);
    };

    constructor (
        private $scope:angular.IScope,
        private AddButtonService:any,
        private PaginationDataService:any,
        private StateService:any,
        private TableOptionsService:any,
        private UserService:any //UserService
    ) {
        // Notify pagination service of changes to view type
        this.$scope.$watch(() => {
            return this.viewType;
        }, (viewType) => {
            this.PaginationDataService.viewType(PAGE_NAME, viewType);
        });

        // Register Add button
        this.AddButtonService.registerAddButton('users', () => {
            this.StateService.Auth().navigateToUserDetails();
        });

        // Get the list of users and groups
        this.UserService.getGroups().then((groups:any) => {
            this.groups = {};
            angular.forEach(groups, (group:any) => {
                this.groups[group.systemName] = group;
            });
        });
        this.UserService.getUsers().then((users:any) => {
            this.users = users;
            this.loading = false;
        });
    }
}
angular.module(moduleName)
       .controller("UsersTableController",
                     ["$scope",
                     "AddButtonService",
                     "PaginationDataService",
                     "StateService",
                     "TableOptionsService",
                     "UserService",
                      UsersTableController]
                    )
        ;

