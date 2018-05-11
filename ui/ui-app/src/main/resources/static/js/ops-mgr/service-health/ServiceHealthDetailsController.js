define(["require", "exports", "angular", "./module-name", "underscore"], function (require, exports, angular, module_name_1, _) {
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
            this.onViewTypeChange = function (viewType) {
                _this.paginationDataService.viewType(_this.pageName, _this.viewType);
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
            };
            //Sort Functions
            /**
             * Build the possible Sorting Options
             * @returns {*[]}
             */
            this.loadSortOptions = function () {
                var options = { 'Component Name': 'name', 'Components': 'componentsCount', 'Alerts': 'alertsCount', 'Update Date': 'latestAlertTimestamp' };
                var sortOptions = this.tableOptionsService.newSortOptions(this.pageName, options, 'name', 'asc');
                this.tableOptionsService.initializeSortOption(this.pageName);
                return sortOptions;
            };
            this.serviceComponentDetails = function (event, component) {
                this.stateService.OpsManager().ServiceStatus().navigateToServiceComponentDetails(this.serviceName, component.name);
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
            $scope.$watch(function () {
                return ServicesStatusData.services;
            }, function (newVal) {
                if (newVal[_this.serviceName]) {
                    if (newVal[_this.serviceName] != _this.service) {
                        _this.service = newVal[_this.serviceName];
                    }
                }
            }, true);
            $scope.$watch(function () {
                return _this.viewType;
            }, function (newVal) {
                _this.onViewTypeChange(newVal);
            });
        } // end of constructor
        controller.prototype.$onInit = function () {
            this.ngOnInit();
        };
        controller.prototype.ngOnInit = function () {
            this.pageName = 'service-details';
            this.cardTitle = 'Service Components';
            //Page State
            this.loading = true;
            this.showProgress = true;
            this.service = { components: [] };
            this.totalComponents = 0;
            this.serviceName = this.$transition$.params().serviceName;
            //Pagination and view Type (list or table)
            this.paginationData = this.paginationDataService.paginationData(this.pageName);
            this.paginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
            this.viewType = this.paginationDataService.viewType(this.pageName);
            this.currentPage = this.paginationDataService.currentPage(this.pageName) || 1;
            this.filter = this.paginationDataService.filter(this.pageName);
            this.sortOptions = this.loadSortOptions();
            this.service = this.ServicesStatusData.services[this.serviceName];
            if (_.isEmpty(this.ServicesStatusData.services)) {
                this.ServicesStatusData.fetchServiceStatus();
            }
        };
        controller.$inject = ["$scope", "$http", "$filter", "$interval", "$timeout", "$q", "ServicesStatusData", "TableOptionsService", "PaginationDataService", "StateService"];
        return controller;
    }());
    exports.controller = controller;
    angular.module(module_name_1.moduleName).component("serviceHealthDetailsController", {
        controller: controller,
        bindings: {
            $transition$: "<"
        },
        controllerAs: "vm",
        templateUrl: "js/ops-mgr/service-health/service-detail.html"
    });
});
//# sourceMappingURL=ServiceHealthDetailsController.js.map