import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/datasources/module-name');

    /**
     * Identifier for this page.
     * @type {string}
     */
    const PAGE_NAME = "datasources";

export class DatasourcesListController{


    cardTitle:any;
    currentPage:any;
    datasources:any;
    filter:any;
    loading:any;
    pageName:any;
    paginationData:any;
    sortOptions:any;
    viewType:any;
    editDatasource:any;
    getRelatedFeedsCount:any;
    onOrderChange:any;
    onPaginationChange:any;
    selectedTableOption:any;
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
    constructor (private $scope:any, private AccessControlService:any, private AddButtonService:any
        , private DatasourcesService:any, private PaginationDataService:any, private StateService:any
        , private TableOptionsService:any) {
        var self = this;

        /**
         * Page title.
         * @type {string}
         */
        self.cardTitle = "Data Sources";

        /**
         * Index of the current page.
         * @type {number}
         */
        self.currentPage = PaginationDataService.currentPage(PAGE_NAME) || 1;

        /**
         * List of data sources.
         * @type {Array.<Object>}
         */
        self.datasources = [];

        /**
         * Helper for table filtering.
         * @type {*}
         */
        self.filter = PaginationDataService.filter(PAGE_NAME);

        /**
         * Indicates that the data source is being loaded.
         * @type {boolean}
         */
        self.loading = true;

        /**
         * Identifier for this page.
         * @type {string}
         */
        self.pageName = PAGE_NAME;

        /**
         * Helper for table pagination.
         * @type {*}
         */
        self.paginationData = (function () {
            var paginationData = PaginationDataService.paginationData(PAGE_NAME);
            PaginationDataService.setRowsPerPageOptions(PAGE_NAME, ['5', '10', '20', '50']);
            return paginationData;
        })();

        /**
         * Options for sorting the table.
         * @type {*}
         */
        self.sortOptions = (function () {
            var fields = {"Name": "name", "Description": "description", "Related Feeds": "sourceForFeeds.length", "Type": "type"};
            var sortOptions = TableOptionsService.newSortOptions(PAGE_NAME, fields, "name", "asc");
            var currentOption = TableOptionsService.getCurrentSort(PAGE_NAME);
            if (currentOption) {
                TableOptionsService.saveSortOption(PAGE_NAME, currentOption)
            }
            return sortOptions;
        })();

        /**
         * Type of view for the table.
         * @type {any}
         */
        self.viewType = PaginationDataService.viewType(PAGE_NAME);

        /**
         * Navigates to the details page for the specified data source.
         *
         * @param {Object} datasource the data source
         */
        self.editDatasource = function (datasource:any) {
            StateService.FeedManager().Datasource().navigateToDatasourceDetails(datasource.id);
        };

        /**
         * Gets the number of related feeds for the specified data source.
         *
         * @param {Object} datasource the data source
         * @returns {number} the number of related feeds
         */
        self.getRelatedFeedsCount = function (datasource:any) {
            return angular.isArray(datasource.sourceForFeeds) ? datasource.sourceForFeeds.length : 0;
        };

        /**
         * Updates the order of the table.
         *
         * @param order the sort order
         */
        self.onOrderChange = function (order:any) {
            PaginationDataService.sort(self.pageName, order);
            TableOptionsService.setSortOption(self.pageName, order);
        };

        /**
         * Updates the pagination of the table.
         *
         * @param page the page number
         */
        self.onPaginationChange = function (page:any) {
            PaginationDataService.currentPage(self.pageName, null, page);
            self.currentPage = page;
        };

        /**
         * Updates the order of the table.
         *
         * @param option the sort order
         */
        self.selectedTableOption = function (option:any) {
            var sortString = TableOptionsService.toSortString(option);
            PaginationDataService.sort(self.pageName, sortString);
            TableOptionsService.toggleSort(self.pageName, option);
            TableOptionsService.setSortOption(self.pageName, sortString);
        };

        // Notify pagination service of changes to view type
        $scope.$watch(function () {
            return self.viewType;
        }, function (viewType:any) {
            PaginationDataService.viewType(PAGE_NAME, viewType);
        });

        // Register Add button
        AccessControlService.getUserAllowedActions()
            .then(function (actionSet:any) {
                if (AccessControlService.hasAction(AccessControlService.DATASOURCE_EDIT, actionSet.actions)) {
                    AddButtonService.registerAddButton("datasources", function () {
                        StateService.FeedManager().Datasource().navigateToDatasourceDetails(null);
                    });
                }
            });

        // Refresh list of data sources
        DatasourcesService.findAll()
            .then(function (datasources:any) {
                self.loading = false;
                self.datasources = datasources;
            });
    };

}
angular.module(moduleName).controller("DatasourcesListController", ["$scope", "AccessControlService", "AddButtonService", "DatasourcesService", "PaginationDataService", "StateService",
                                                                    "TableOptionsService", DatasourcesListController]);
