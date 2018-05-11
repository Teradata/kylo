define(["require", "exports", "angular", "./module-name"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $http, $filter, $interval, $timeout, $q, ServicesStatusData, tableOptionsService, paginationDataService, stateService) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.$filter = $filter;
            this.$interval = $interval;
            this.$timeout = $timeout;
            this.$q = $q;
            this.ServicesStatusData = ServicesStatusData;
            this.tableOptionsService = tableOptionsService;
            this.paginationDataService = paginationDataService;
            this.stateService = stateService;
            this.paginationId = function () {
                return this.paginationDataService.paginationId(this.pageName);
            };
            //Tab Functions
            this.onOrderChange = function (order) {
                _this.paginationDataService.sort(_this.pageName, order);
                _this.tableOptionsService.setSortOption(_this.pageName, order);
                //   return loadJobs(true).promise;
                //return this.deferred.promise;
            };
            this.onPaginationChange = function (page, limit) {
                _this.paginationDataService.currentPage(_this.pageName, null, page);
                _this.currentPage = page;
                // return loadJobs(true).promise;
            };
            //Sort Functions
            /**
             * Build the possible Sorting Options
             * @returns {*[]}
             */
            this.loadSortOptions = function () {
                var options = { 'Service Name': 'serviceName', 'Components': 'componentsCount', 'Alerts': 'alertsCount', 'Update Date': 'latestAlertTimestamp' };
                var sortOptions = this.tableOptionsService.newSortOptions(this.pageName, options, 'serviceName', 'asc');
                this.tableOptionsService.initializeSortOption(this.pageName);
                return sortOptions;
            };
            /**
             * Called when a user Clicks on a table Option
             * @param option
             */
            this.selectedTableOption = function (option) {
                var sortString = _this.tableOptionsService.toSortString(option);
                var savedSort = _this.paginationDataService.sort(_this.pageName, sortString);
                var updatedOption = _this.tableOptionsService.toggleSort(_this.pageName, option);
                _this.tableOptionsService.setSortOption(_this.pageName, sortString);
            };
            //Load Jobs
            this.loadData = function () {
                var successFn = function (data) {
                    _this.services = data;
                    _this.totalServices = _this.services.length;
                    _this.allServices = data;
                    _this.loading == false;
                    _this.showProgress = false;
                };
                var errorFn = function (err) {
                    console.log('error', err);
                };
                var finallyFn = function () {
                };
                //Only Refresh if the modal dialog does not have any open alerts
                _this.ServicesStatusData.fetchServiceStatus(successFn, errorFn);
            };
            this.serviceDetails = function (event, service) {
                this.stateService.OpsManager().ServiceStatus().navigateToServiceDetails(service.serviceName);
            };
            this.clearRefreshInterval = function () {
                if (this.refreshInterval != null) {
                    this.$interval.cancel(this.refreshInterval);
                    this.refreshInterval = null;
                }
            };
            this.setRefreshInterval = function () {
                this.clearRefreshInterval();
                if (this.refreshIntervalTime) {
                    this.refreshInterval = this.$interval(this.loadData, this.refreshIntervalTime);
                }
            };
            this.RefreshIntervalSet = this.setRefreshInterval();
            this.RefreshIntervalClear = this.clearRefreshInterval();
            $scope.$watch(function () { return _this.filter; }, function (newVal) {
                if (newVal && newVal != '') {
                    //     this.services = $filter('filter')(this.allServices, newVal);
                    _this.totalServices = _this.services.length;
                }
                else {
                    //    this.services = this.allServices;
                }
            });
            $scope.$watch(function () {
                return _this.viewType;
            }, function (newVal) {
                _this.onViewTypeChange(newVal);
            });
            $scope.$on('$destroy', function () {
                _this.clearRefreshInterval();
            });
        } // end of constructor
        controller.prototype.$onInit = function () {
            this.ngOnInit();
        };
        controller.prototype.ngOnInit = function () {
            this.pageName = 'service-health';
            //Page State
            this.loading = true;
            this.showProgress = true;
            this.services = [];
            this.allServices = [];
            this.totalServices = 0;
            //Pagination and view Type (list or table)
            this.paginationData = this.paginationDataService.paginationData(this.pageName);
            this.paginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
            this.viewType = this.paginationDataService.viewType(this.pageName);
            this.currentPage = this.paginationDataService.currentPage(this.pageName) || 1;
            this.filter = this.paginationDataService.filter(this.pageName);
            this.sortOptions = this.loadSortOptions();
            //Load the data
            this.loadData();
            //Refresh Intervals
            this.setRefreshInterval();
        };
        controller.prototype.onViewTypeChange = function (viewType) {
            this.paginationDataService.viewType(this.pageName, this.viewType);
        };
        controller.$inject = ["$scope", "$http", "$filter", "$interval", "$timeout", "$q", "ServicesStatusData", "TableOptionsService", "PaginationDataService", "StateService"];
        return controller;
    }());
    exports.controller = controller;
    angular.module(module_name_1.moduleName).component("tbaServiceHealth", {
        controller: controller,
        bindings: {
            cardTitle: "@",
            refreshIntervalTime: "@"
        },
        controllerAs: "vm",
        templateUrl: "js/ops-mgr/service-health/service-health-template.html"
    });
});
//# sourceMappingURL=ServiceHealthDirective.js.map