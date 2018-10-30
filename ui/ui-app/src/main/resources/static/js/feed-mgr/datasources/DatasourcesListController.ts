import * as angular from 'angular';
import * as _ from "underscore";
import {AccessControlService} from '../../services/AccessControlService';
import {StateService} from '../../services/StateService';
import { DefaultPaginationDataService } from '../../services/PaginationDataService';
import { DefaultTableOptionsService } from '../../services/TableOptionsService';
import { DatasourcesService } from '../services/DatasourcesService';
import {AddButtonService} from '../../services/AddButtonService';
const moduleName = require('feed-mgr/datasources/module-name');

/**
 * Identifier for this page.
 * @type {string}
 */
const PAGE_NAME = "datasources";

export class DatasourcesListController {

    /**
    * Page title.
    * @type {string}
    */
    cardTitle: any = "Data Sources";
    currentPage: any;
    /**
    * List of data sources.
    * @type {Array.<Object>}
    */
    datasources: any[] = [];
    /**
    * Helper for table filtering.
    * @type {*}
    */
    filter: any;
    /**
    * Indicates that the data source is being loaded.
    * @type {boolean}
    */
    loading: boolean = true;
    /**
    * Identifier for this page.
    * @type {string}
    */
    pageName: string = PAGE_NAME;
    viewType: any;

    $onInit() {
        this.ngOnInit();
    }
    ngOnInit() {
        /**
        * Index of the current page.
        * @type {number}
        */
        this.currentPage = this.paginationDataService.currentPage(PAGE_NAME) || 1;
        this.filter = this.paginationDataService.filter(PAGE_NAME);
        /**
         * Type of view for the table.
         * @type {any}
         */
        this.viewType = this.paginationDataService.viewType(PAGE_NAME);

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
            .then((datasources: any) => {
                this.loading = false;
                this.datasources = datasources;
            });

    }


    static readonly $inject = ["$scope", "AccessControlService", "AddButtonService",
        "DatasourcesService", "PaginationDataService", "StateService",
        "TableOptionsService"]
    /**
     * Displays a list of data sources.
     *
     * @constructor
     * @param $scope the application model
     * @param {AccessControlService} AccessControlService the access control service
     * @param AddButtonService the Add button service
     * @param DatasourcesService the data sources service
     * @param PaginationDataService the table pagination service
     * @param StateService the page state service
     * @param TableOptionsService the table options service
     */
    constructor(private $scope: IScope, private accessControlService: AccessControlService, private addButtonService: AddButtonService
        , private datasourcesService: DatasourcesService, private paginationDataService: DefaultPaginationDataService, private stateService: StateService
        , private tableOptionsService: DefaultTableOptionsService) {

        // Notify pagination service of changes to view type
        $scope.$watch(() => {
            return this.viewType;
        }, (viewType: any) => {
            this.paginationDataService.viewType(PAGE_NAME, viewType);
        });
    };
    /**
         * Helper for table pagination.
         * @type {*}
         */
    paginationData = (() => {
        var paginationData = this.paginationDataService.paginationData(PAGE_NAME);
        this.paginationDataService.setRowsPerPageOptions(PAGE_NAME, ['5', '10', '20', '50']);
        return paginationData;
    })();

    /**
     * Options for sorting the table.
     * @type {*}
     */
    sortOptions = (() => {
        var fields = { "Name": "name", "Description": "description", "Related Feeds": "sourceForFeeds.length", "Type": "type" };
        var sortOptions = this.tableOptionsService.newSortOptions(PAGE_NAME, fields, "name", "asc");
        var currentOption = this.tableOptionsService.getCurrentSort(PAGE_NAME);
        if (currentOption) {
            this.tableOptionsService.saveSortOption(PAGE_NAME, currentOption)
        }
        return sortOptions;
    })();
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

    /**
     * Updates the order of the table.
     *
     * @param order the sort order
     */
    onOrderChange = (order: any) => {
        this.paginationDataService.sort(this.pageName, order);
        this.tableOptionsService.setSortOption(this.pageName, order);
    };

    /**
     * Updates the pagination of the table.
     *
     * @param page the page number
     */
    onPaginationChange = (page: any) => {
        this.paginationDataService.currentPage(this.pageName, null, page);
        this.currentPage = page;
    };

    /**
     * Updates the order of the table.
     *
     * @param option the sort order
     */
    selectedTableOption = (option: any) => {
        var sortString = this.tableOptionsService.toSortString(option);
        this.paginationDataService.sort(this.pageName, sortString);
        this.tableOptionsService.toggleSort(this.pageName, option);
        this.tableOptionsService.setSortOption(this.pageName, sortString);
    };

}
angular.module(moduleName).component("datasourcesListController", {
    templateUrl: "./list.html",
    controller: DatasourcesListController,
    controllerAs: "vm"
});
