import * as angular from 'angular';
import * as _ from 'underscore';
import UserService from "../services/UserService";
import AddButtonService from "../../services/AddButtonService";
import { DefaultPaginationDataService } from "../../services/PaginationDataService";
import StateService from "../../services/StateService";
import { DefaultTableOptionsService } from "../../services/TableOptionsService";
import "../module";
import "../module-require";
// import {moduleName} from "../module-name";
import { Component } from '@angular/core';
import { ITdDataTableColumn, ITdDataTableSortChangeEvent, TdDataTableSortingOrder, TdDataTableService } from '@covalent/core/data-table';
import { IPageChangeEvent } from '@covalent/core/paging';
const PAGE_NAME: string = "users";


@Component({
    selector: 'users-Table',
    templateUrl: "js/auth/users/users-table.html"
})
export default class UsersTableComponent {
    /**
     * Page title.
     * @type {string}
     */
    cardTitle: string = "Users";

    /**
     * Index of the current page.
     * @type {number}
     */
    currentPage: number = 1;


    /**
     * Mapping of group names to group metadata.
     * @type {Object.<string, GroupPrincipal>}
     */
    groups: any = {};

    /**
     * Indicates that the table data is being loaded.
     * @type {boolean}
     */
    loading: boolean = true;

    /**
     * List of users.
     * @type {Array.<UserPrincipal>}
     */
    users: any = [];

    filteredData: any[];
    filteredTotal: number = 0;

    sortBy: string = 'displayName';

    sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

    pageSize: number = 5;
    fromRow: number = 1;
    searchTerm: string = '';

    private columns: ITdDataTableColumn[] = [
        { name: 'displayName', label: 'Display Name', sortable: true, filter : true },
        { name: 'email', label: 'Email Address', sortable: true, filter : true},
        { name: 'status', label: 'Status', sortable: true, filter : true },
        { name: 'groups', label: 'Groups', sortable: true, filter : true },
    ];

    ngOnInit() {
        // Get the list of users and groups
        this.UserService.getGroups().then((groups: any) => {
            this.groups = {};
            angular.forEach(groups, (group: any) => {
                this.groups[group.systemName] = group;
            });
        });
        this.UserService.getUsers().then((users: any) => {

            users = users.map((u: any) => {
                u.displayName = this.getDisplayName(u);
                u.email = u.email;
                u.status = u.enabled ? 'Active' : 'Disabled';
                u.groups = this.getGroupTitles(u);
                return u;
            });
            this.users = users;
            this.filteredData = this.users;
            this.filteredTotal = this.users.length;
            this.filter();
            this.loading = false;
        });
    }

    constructor(
        private AddButtonService: AddButtonService,
        private stateService: StateService,
        private UserService: UserService,
        private _dataTableService: TdDataTableService
    ) {

        // Register Add button
        this.AddButtonService.registerAddButton('users', () => {
            this.stateService.Auth.navigateToUserDetails();
        });

    }

    onPageSizeChange(pagingEvent: IPageChangeEvent): void {
        this.fromRow = pagingEvent.fromRow;
        this.currentPage = pagingEvent.page;
        this.pageSize = pagingEvent.pageSize;
        this.filter();
    }

    search(searchTerm: string): void {
        this.searchTerm = searchTerm;
        this.filter();
    }

    sort(sortEvent: ITdDataTableSortChangeEvent): void {
        this.sortBy = sortEvent.name;
        this.sortOrder = sortEvent.order;
        this.filter();
    }

    filter(): void {
        let newData: any[] = this.users;
        let excludedColumns: string[] = this.columns
            .filter((column: ITdDataTableColumn) => {
                return ((column.filter === undefined && column.hidden === true) ||
                    (column.filter !== undefined && column.filter === false));
            }).map((column: ITdDataTableColumn) => {
                return column.name;
            });
        newData = this._dataTableService.filterData(newData, this.searchTerm, true, excludedColumns);
        this.filteredTotal = newData.length;
        newData = this._dataTableService.sortData(newData, this.sortBy, this.sortOrder);
        newData = this._dataTableService.pageData(newData, this.fromRow, this.currentPage * this.pageSize);
        this.filteredData = newData;
    }


    /**
     * Gets the display name of the specified user. Defaults to the system name if the display name is blank.
     *
     * @param user the user
     * @returns {string} the display name
     */
    getDisplayName(user: any) {
        return (angular.isString(user.displayName) && user.displayName.length > 0) ? user.displayName : user.systemName;
    };

    /**
     * Gets the title for each group the user belongs to.
     *
     * @param user the user
     * @returns {Array.<string>} the group titles
     */
    getGroupTitles(user: any) {
        return _.map(user.groups, (group: any) => {
            if (angular.isDefined(this.groups[group]) && angular.isString(this.groups[group].title)) {
                return this.groups[group].title;
            } else {
                return group;
            }
        });
    };
    /**
     * Navigates to the details page for the specified user.
     *
     * @param user the user
     */
    userDetails(user: any) {
        this.stateService.Auth.navigateToUserDetails(user.row.systemName);
    };
}
