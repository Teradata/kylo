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
import { BaseFilteredPaginatedTableView } from '../../common/filtered-paginated-table-view/BaseFilteredPaginatedTableView';
const PAGE_NAME: string = "users";


@Component({
    selector: 'users-Table',
    templateUrl: "js/auth/users/users-table.html"
})
export default class UsersTableComponent extends BaseFilteredPaginatedTableView{
    /**
     * Page title.
     * @type {string}
     */
    cardTitle: string = "Users";
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

    public columns: ITdDataTableColumn[] = [
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
            super.setSortBy('displayName');
            super.setDataAndColumnSchema(users,this.columns);
            super.filter();
            this.loading = false;
        });
    }

    constructor(
        private AddButtonService: AddButtonService,
        private stateService: StateService,
        private UserService: UserService,
        public _dataTableService: TdDataTableService
    ) {
        super(_dataTableService);
        // Register Add button
        this.AddButtonService.registerAddButton('users', () => {
            this.stateService.Auth.navigateToUserDetails();
        });

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
