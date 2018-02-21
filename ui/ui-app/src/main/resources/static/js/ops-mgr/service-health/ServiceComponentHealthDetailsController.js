define(["require", "exports", "angular", "./module-name", "../services/ServicesStatusService"], function (require, exports, angular, module_name_1, ServicesStatusService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $http, $filter, $transition$, $interval, $timeout, $q, ServicesStatusData, TableOptionsService, PaginationDataService) {
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
            //Page State
            this.loading = true;
            this.showProgress = true;
            this.component = { alerts: [] };
            this.totalAlerts = 0;
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
                var options = { 'Component Name': 'componentName', 'Alert': 'alert', 'Update Date': 'latestAlertTimestamp' };
                var sortOptions = this.TableOptionsService.newSortOptions(this.pageName, options, 'latestAlertTimestamp', 'asc');
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
            this.pageName = 'service-component-details';
            this.cardTitle = 'Service Component Alerts';
            this.componentName = $transition$.params().componentName;
            this.serviceName = $transition$.params().serviceName;
            this.paginationData = PaginationDataService.paginationData(this.pageName);
            PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
            this.viewType = PaginationDataService.viewType(this.pageName);
            this.currentPage = PaginationDataService.currentPage(this.pageName) || 1;
            this.filter = PaginationDataService.filter(this.pageName);
            this.sortOptions = this.loadSortOptions();
            this.service = ServicesStatusData.services[this.serviceName];
            if (this.service) {
                this.component = this.service.componentMap[this.componentName];
            }
            ;
            $scope.$watch(function () { return ServicesStatusData.services; }, function (newVal) {
                if (newVal[_this.serviceName]) {
                    var updatedComponent = newVal[_this.serviceName].componentMap[_this.componentName];
                    if (updatedComponent != _this.component) {
                        _this.component = updatedComponent;
                    }
                }
            });
            $scope.$watch(function () { return _this.viewType; }, function (newVal) { _this.onViewTypeChange(newVal); });
        } // end of constructor
        return controller;
    }());
    exports.controller = controller;
    angular.module(module_name_1.moduleName)
        .service("ServicesStatusData", [ServicesStatusService_1.default])
        .controller('ServiceComponentHealthDetailsController', ["$scope", "$http", "$filter", "$transition$", "$interval",
        "$timeout", "$q", "ServicesStatusData", "TableOptionsService",
        "PaginationDataService", controller]);
});
//# sourceMappingURL=ServiceComponentHealthDetailsController.js.map