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
import { BaseFilteredPaginatedTableView } from '../../common/filtered-paginated-table-view/BaseFilteredPaginatedTableView';
import { ObjectUtils } from '../../common/utils/object-utils';
/**
 * Identifier for this page.
 * @type {string}
 */
const PAGE_NAME:string = "groups";
@Component({
    templateUrl: "js/auth/groups/groups-table.html",
    selector: 'groups-Table'
})
export default class GroupsTableComponent extends BaseFilteredPaginatedTableView{
    /**
     * Page title.
     * @type {string}
     */
    cardTitle:string = "Groups";
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
    
    public columns: ITdDataTableColumn[] = [
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
        return (ObjectUtils.isString(group.title) && group.title.length > 0) ? group.title : group.systemName;
    };
     /**
     * Navigates to the details page for the specified user.
     *
     * @param user the user
     */
    groupDetails=(group:any)=>{
        this.stateService.Auth.navigateToGroupDetails(group.row.systemName);
    };

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
            super.setSortBy('title');
            super.setDataAndColumnSchema(groups,this.columns);
            super.filter();
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
        public _dataTableService: TdDataTableService
    ) {
        super(_dataTableService);
     }
}
