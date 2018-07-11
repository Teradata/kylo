import * as angular from 'angular';
import * as _ from 'underscore';
import UserService from "../services/UserService";
import AddButtonService from  "../../services/AddButtonService";
import {DefaultPaginationDataService} from  "../../services/PaginationDataService";
import {DefaultTableOptionsService} from  "../../services/TableOptionsService";
// import "../module";
// import "../module-require";
import { Component } from '@angular/core';
import StateService from '../../services/StateService';
import { TdDataTableSortingOrder, ITdDataTableSortChangeEvent, ITdDataTableColumn, TdDataTableService } from '@covalent/core/data-table';
import { IPageChangeEvent } from '@covalent/core/paging';
/**
 * Identifier for this page.
 * @type {string}
 */
const PAGE_NAME:string = "groups";
@Component({
    templateUrl: "js/auth/groups/groups-table.html",
    selector: 'groups-Table'
})
export default class GroupsTableComponent {
    /**
     * Page title.
     * @type {string}
     */
    cardTitle:string = "Groups";
    /**
     * Index of the current page.
     * @type {number}
     */
    currentPage:number = 1;
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
     * List of groups.
     * @type {Array.<GroupPrincipal>}
     */
    groups:any[] = [];

    filteredData: any[];
    filteredTotal: number = 0;

    sortBy: string = 'title';

    sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

    pageSize: number = 5;
    fromRow: number = 1;
    searchTerm: string = '';
    
    private columns: ITdDataTableColumn[] = [
        { name: 'title', label: 'Title', sortable: true, filter : true },
        { name: 'description', label: 'Description', sortable: true, filter : true},
        { name: 'memberCount', label: 'Members', sortable: true, filter : true },
    ];

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
        this.stateService.Auth.navigateToGroupDetails(group.row.systemName);
    };

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
        let newData: any[] = this.groups;
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


    ngOnInit() {
        // Register Add button
        this.addButtonService.registerAddButton('groups', () => {
            this.stateService.Auth.navigateToGroupDetails();
        });
        // Get the list of users and groups
        this.userService.getGroups().then((groups:any) => {
             groups = groups.map((g: any) => {
                g.displayName = this.getTitle(g);
                return g;
            });
             this.groups = groups;
             this.filteredData = this.groups;
            this.filteredTotal = this.groups.length;
            this.filter();
             this.loading = false;
        });
    }
    /**
     * Displays a list of groups in a table.
     *
     * @constructor
     * @param AddButtonService the Add button service
     * @param StateService the page state service
     * @param UserService the user service
     */
        constructor (
        private addButtonService:AddButtonService,
        private stateService:StateService,
        private userService:UserService,
        private _dataTableService: TdDataTableService
    ) {
     }
}
