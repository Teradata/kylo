define(["require", "exports", "angular", "./module-name", "../services/ServicesStatusService", "../services/AlertsService"], function (require, exports, angular, module_name_1, ServicesStatusService_1, AlertsService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $http, $filter, $interval, $timeout, $q, ServicesStatusData, TableOptionsService, PaginationDataService, AlertsService, StateService) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.$filter = $filter;
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
                // return loadJobs(true).promise;
            };
            //Sort Functions
            /**
             * Build the possible Sorting Options
             * @returns {*[]}
             */
            this.loadSortOptions = function () {
                var options = { 'Service Name': 'serviceName', 'Components': 'componentsCount', 'Alerts': 'alertsCount', 'Update Date': 'latestAlertTimestamp' };
                var sortOptions = this.TableOptionsService.newSortOptions(this.pageName, options, 'serviceName', 'asc');
                this.TableOptionsService.initializeSortOption(this.pageName);
                return sortOptions;
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
                this.StateService.OpsManager().ServiceStatus().navigateToServiceDetails(service.serviceName);
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
            this.pageName = 'service-health';
            //Page State
            this.loading = true;
            this.showProgress = true;
            this.services = [];
            this.allServices = [];
            this.totalServices = 0;
            $scope.$watch(function () { return _this.filter; }, function (newVal) {
                if (newVal && newVal != '') {
                    //     this.services = $filter('filter')(this.allServices, newVal);
                    _this.totalServices = _this.services.length;
                }
                else {
                    //    this.services = this.allServices;
                }
            });
            //Pagination and view Type (list or table)
            this.paginationData = PaginationDataService.paginationData(this.pageName);
            PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
            this.viewType = PaginationDataService.viewType(this.pageName);
            this.currentPage = PaginationDataService.currentPage(this.pageName) || 1;
            this.filter = PaginationDataService.filter(this.pageName);
            this.sortOptions = this.loadSortOptions();
            //Load the data
            this.loadData();
            //Refresh Intervals
            this.setRefreshInterval();
            $scope.$watch(function () {
                return _this.viewType;
            }, function (newVal) {
                _this.onViewTypeChange(newVal);
            });
            $scope.$on('$destroy', function () {
                _this.clearRefreshInterval();
            });
        } // end of constructor
        return controller;
    }());
    exports.controller = controller;
    angular.module(module_name_1.moduleName)
        .service("ServicesStatusData", ["$q", '$http', '$interval', '$timeout', 'AlertsService', 'IconService',
        'OpsManagerRestUrlService', ServicesStatusService_1.default])
        .service("AlertsService", [AlertsService_1.default])
        .controller('ServiceHealthController', ["$scope", "$http", "$filter", "$interval", "$timeout", "$q",
        "ServicesStatusData", "TableOptionsService", "PaginationDataService",
        "AlertsService", "StateService", controller]);
    angular.module(module_name_1.moduleName)
        .directive('tbaServiceHealth', function () {
        return {
            restrict: "EA",
            bindToController: {
                cardTitle: "@",
                refreshIntervalTime: "@"
            },
            controllerAs: 'vm',
            scope: true,
            templateUrl: 'js/ops-mgr/service-health/service-health-template.html',
            controller: "ServiceHealthController",
            link: function ($scope, element, attrs, controller) {
            }
        };
    });
});
//# sourceMappingURL=ServiceHealthDirective.js.map