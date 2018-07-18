import * as angular from 'angular';
import * as _ from "underscore";
import AccessControlService from '../../services/AccessControlService';
import StateService from '../../services/StateService';
import { DatasourcesService } from '../services/DatasourcesService';
import AddButtonService from '../../services/AddButtonService';
import { Component } from '@angular/core';
import { TdDataTableSortingOrder, ITdDataTableColumn, TdDataTableService, ITdDataTableSortChangeEvent } from '@covalent/core/data-table';
import { IPageChangeEvent } from '@covalent/core/paging';
import { BaseFilteredPaginatedTableView } from '../../common/filtered-paginated-table-view/BaseFilteredPaginatedTableView';

/**
 * Identifier for this page.
 * @type {string}
 */
const PAGE_NAME = "datasources";

@Component({
    templateUrl : 'js/feed-mgr/datasources/list.html',
    selector : 'datasources-table'
})
export class DatasourcesTableComponent extends BaseFilteredPaginatedTableView{

    /**
    * Page title.
    * @type {string}
    */
    cardTitle: any = "Data Sources";
    /**
    * Indicates that the data source is being loaded.
    * @type {boolean}
    */
    loading: boolean = true;

    public columns: ITdDataTableColumn[] = [
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
                super.setSortBy('name');
                super.setDataAndColumnSchema(ds,this.columns);
                super.filter();
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
        , private datasourcesService: DatasourcesService, private stateService: StateService, public _dataTableService: TdDataTableService) {
            super(_dataTableService);
    };
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