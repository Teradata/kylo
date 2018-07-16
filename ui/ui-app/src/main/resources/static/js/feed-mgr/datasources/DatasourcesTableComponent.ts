import * as angular from 'angular';
import * as _ from "underscore";
import AccessControlService from '../../services/AccessControlService';
import StateService from '../../services/StateService';
import { DatasourcesService } from '../services/DatasourcesService';
import AddButtonService from '../../services/AddButtonService';
import { Component } from '@angular/core';
import { TdDataTableSortingOrder, ITdDataTableColumn, TdDataTableService, ITdDataTableSortChangeEvent } from '@covalent/core/data-table';
import { IPageChangeEvent } from '@covalent/core/paging';

/**
 * Identifier for this page.
 * @type {string}
 */
const PAGE_NAME = "datasources";

@Component({
    templateUrl : 'js/feed-mgr/datasources/list.html',
    selector : 'datasources-table'
})
export class DatasourcesTableComponent {

    /**
    * Page title.
    * @type {string}
    */
    cardTitle: any = "Data Sources";
    currentPage: number = 1;
    /**
    * List of data sources.
    * @type {Array.<Object>}
    */
    datasources: any[] = [];
    /**
    * Helper for table filtering.
    * @type {*}
    */
    filteredData: any[];
    filteredTotal: number = 0;
    /**
    * Indicates that the data source is being loaded.
    * @type {boolean}
    */
    loading: boolean = true;
    /**
    * Identifier for this page.
    * @type {string}
    */

    sortBy: string = 'name';

    sortOrder: TdDataTableSortingOrder = TdDataTableSortingOrder.Ascending;

    pageSize: number = 5;
    fromRow: number = 1;
    searchTerm: string = '';

    private columns: ITdDataTableColumn[] = [
        { name: 'name', label: 'Name', sortable: true, filter : true },
        { name: 'type', label: 'Type', sortable: true, filter : true},
        { name: 'description', label: 'Description', sortable: true, filter : true },
        { name: 'feeds', label: 'Related Feeds', sortable: true, filter : true },
    ];

    ngOnInit() {

        // Register Add button
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                if (this.accessControlService.hasAction(AccessControlService.DATASOURCE_EDIT, actionSet.actions)) {
                    this.addButtonService.registerAddButton("datasources", () => {
                        this.stateService.FeedManager().Datasource().navigateToDatasourceDetails(null);
                    });
                }
            });
        // Refresh list of data sources
        this.datasourcesService.findAll()
            .then((ds: any) => {
                ds = ds.map((d: any) => {
                    d.feeds = this.getRelatedFeedsCount(d);
                    return d;
                });
                this.datasources = ds;
                this.filteredData = this.datasources;
                this.filteredTotal = this.datasources.length;
                this.filter();
                this.loading = false;
            });
    }
    /**
     * Displays a list of data sources.
     *
     * @constructor
     * @param {AccessControlService} AccessControlService the access control service
     * @param AddButtonService the Add button service
     * @param DatasourcesService the data sources service
     * @param PaginationDataService the table pagination service
     * @param StateService the page state service
     * @param TableOptionsService the table options service
     */
    constructor(private accessControlService: AccessControlService, private addButtonService: AddButtonService
        , private datasourcesService: DatasourcesService, private stateService: StateService, private _dataTableService: TdDataTableService) {

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
        let newData: any[] = this.datasources;
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
        * Navigates to the details page for the specified data source.
        *
        * @param {Object} datasource the data source
        */
    editDatasource = (datasource: any) => {
        this.stateService.FeedManager().Datasource().navigateToDatasourceDetails(datasource.id);
    };

    /**
     * Gets the number of related feeds for the specified data source.
     *
     * @param {Object} datasource the data source
     * @returns {number} the number of related feeds
     */
    getRelatedFeedsCount = (datasource: any) => {
        return angular.isArray(datasource.sourceForFeeds) ? datasource.sourceForFeeds.length : 0;
    };
}