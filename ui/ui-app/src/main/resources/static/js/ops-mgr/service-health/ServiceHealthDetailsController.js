define(["require", "exports", "angular", "./module-name", "underscore", "../services/ServicesStatusService", "../services/AlertsService"], function (require, exports, angular, module_name_1, _, ServicesStatusService_1, AlertsService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $http, $filter, $transition$, $interval, $timeout, $q, ServicesStatusData, TableOptionsService, PaginationDataService, AlertsService, StateService) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.$filter = $filter;
            this.$transition$ = $transition$;
            this.$interval = $interval;
            this.$timeout = $timeout;
            this.$q = $q;
            this.ServicesStatusData = ServicesStatusData;
            this.TableOptionsService = TableOptionsService;
            this.PaginationDataService = PaginationDataService;
            this.AlertsService = AlertsService;
            this.StateService = StateService;
            this.paginationId = function () {
                return this.PaginationDataService.paginationId(this.pageName);
            };
            this.onViewTypeChange = function (viewType) {
                _this.PaginationDataService.viewType(_this.pageName, _this.viewType);
            };
            //Tab Functions
            this.onOrderChange = function (order) {
                _this.PaginationDataService.sort(_this.pageName, order);
                _this.TableOptionsService.setSortOption(_this.pageName, order);
                //   return loadJobs(true).promise;
                //return this.deferred.promise;
            };
            this.onPaginationChange = function (page, limit) {
                _this.PaginationDataService.currentPage(_this.pageName, null, page);
                _this.currentPage = page;
            };
            //Sort Functions
            /**
             * Build the possible Sorting Options
             * @returns {*[]}
             */
            this.loadSortOptions = function () {
                var options = { 'Component Name': 'name', 'Components': 'componentsCount', 'Alerts': 'alertsCount', 'Update Date': 'latestAlertTimestamp' };
                var sortOptions = this.TableOptionsService.newSortOptions(this.pageName, options, 'latestAlertTimestamp', 'asc');
                this.TableOptionsService.initializeSortOption(this.pageName);
                return sortOptions;
            };
            this.serviceComponentDetails = function (event, component) {
                this.StateService.OpsManager().ServiceStatus().navigateToServiceComponentDetails(this.serviceName, component.name);
            };
            /**
             * Called when a user Clicks on a table Option
             * @param option
             */
            this.selectedTableOption = function (option) {
                var sortString = _this.TableOptionsService.toSortString(option);
                var savedSort = _this.PaginationDataService.sort(_this.pageName, sortString);
                var updatedOption = _this.TableOptionsService.toggleSort(_this.pageName, option);
                _this.TableOptionsService.setSortOption(_this.pageName, sortString);
            };
            this.pageName = 'service-details';
            this.cardTitle = 'Service Components';
            //Page State
            this.loading = true;
            this.showProgress = true;
            this.service = { components: [] };
            this.totalComponents = 0;
            this.serviceName = $transition$.params().serviceName;
            //Pagination and view Type (list or table)
            this.paginationData = PaginationDataService.paginationData(this.pageName);
            PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
            this.viewType = PaginationDataService.viewType(this.pageName);
            this.currentPage = PaginationDataService.currentPage(this.pageName) || 1;
            this.filter = PaginationDataService.filter(this.pageName);
            this.sortOptions = this.loadSortOptions();
            this.service = ServicesStatusData.services[this.serviceName];
            if (_.isEmpty(ServicesStatusData.services)) {
                ServicesStatusData.fetchServiceStatus();
            }
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
        return controller;
    }());
    exports.controller = controller;
    angular.module(module_name_1.moduleName)
        .service("ServicesStatusData", [ServicesStatusService_1.default])
        .service("AlertsService", [AlertsService_1.default])
        .controller('ServiceHealthDetailsController', ["$scope", "$http", "$filter", "$transition$", "$interval",
        "$timeout", "$q", "ServicesStatusData", "TableOptionsService",
        "PaginationDataService", "AlertsService", "StateService", controller]);
});
//# sourceMappingURL=ServiceHealthDetailsController.js.map