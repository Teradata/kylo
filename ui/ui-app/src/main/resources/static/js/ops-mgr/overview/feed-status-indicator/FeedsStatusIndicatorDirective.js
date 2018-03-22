define(["require", "exports", "angular", "../module-name", "pascalprecht.translate"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $element, $http, $interval, $timeout, OpsManagerFeedService, OpsManagerDashboardService, BroadcastService, $filter) {
            var _this = this;
            this.$scope = $scope;
            this.$element = $element;
            this.$http = $http;
            this.$interval = $interval;
            this.$timeout = $timeout;
            this.OpsManagerFeedService = OpsManagerFeedService;
            this.OpsManagerDashboardService = OpsManagerDashboardService;
            this.BroadcastService = BroadcastService;
            this.$filter = $filter;
            this.initializePieChart = function () {
                _this.chartData.push({ key: "Healthy", value: 0 });
                _this.chartData.push({ key: "Unhealthy", value: 0 });
            };
            this.onHealthyClick = function () {
                _this.OpsManagerDashboardService.selectFeedHealthTab('Healthy');
            };
            this.onUnhealthyClick = function () {
                _this.OpsManagerDashboardService.selectFeedHealthTab('Unhealthy');
            };
            this.init = function () {
                _this.initializePieChart();
                _this.watchDashboard();
            };
            this.watchDashboard = function () {
                _this.BroadcastService.subscribe(_this.$scope, _this.OpsManagerDashboardService.DASHBOARD_UPDATED, function (dashboard) {
                    _this.dataMap.Unhealthy.count = _this.OpsManagerDashboardService.feedUnhealthyCount;
                    _this.dataMap.Healthy.count = _this.OpsManagerDashboardService.feedHealthyCount;
                    _this.feedSummaryData = _this.OpsManagerDashboardService.feedSummaryData;
                    _this.updateChartData();
                });
            };
            this.onChartElementClick = function (key) {
                _this.OpsManagerDashboardService.selectFeedHealthTab(key);
            };
            this.updateChartData = function () {
                angular.forEach(_this.chartData, function (row, i) {
                    row.value = _this.dataMap[row.key].count;
                });
                var title = (_this.dataMap.Healthy.count + _this.dataMap.Unhealthy.count) + " " + _this.$filter('translate')('Total');
                _this.chartOptions.chart.title = title;
                _this.dataLoaded = true;
                if (_this.chartApi.update) {
                    _this.chartApi.update();
                }
            };
            this.updateChart = function () {
                if (_this.chartApi.update) {
                    _this.chartApi.update();
                }
            };
            this.chartApi = {};
            this.dataLoaded = false;
            this.feedSummaryData = null;
            this.chartData = [];
            this.dataMap = { 'Healthy': { count: 0, color: '#009933' }, 'Unhealthy': { count: 0, color: '#FF0000' } };
            this.chartOptions = {
                chart: {
                    type: 'pieChart',
                    x: function (d) { return d.key; },
                    y: function (d) { return d.value; },
                    showLabels: false,
                    duration: 100,
                    height: 150,
                    transitionDuration: 500,
                    labelThreshold: 0.01,
                    labelSunbeamLayout: false,
                    "margin": { "top": 10, "right": 10, "bottom": 10, "left": 10 },
                    donut: true,
                    donutRatio: 0.65,
                    showLegend: false,
                    refreshDataOnly: false,
                    color: function (d) {
                        return _this.dataMap[d.key].color;
                    },
                    valueFormat: function (d) {
                        return parseInt(d);
                    },
                    pie: {
                        dispatch: {
                            'elementClick': function (e) {
                                _this.onChartElementClick(e.data.key);
                            }
                        }
                    },
                    dispatch: {}
                }
            };
            this.init();
            $scope.$on('$destroy', function () {
            });
        } // end of constructor
        return controller;
    }());
    exports.default = controller;
    angular.module(module_name_1.moduleName)
        .controller('FeedStatusIndicatorController', ["$scope", "$element", "$http", "$interval", "$timeout", "OpsManagerFeedService", "OpsManagerDashboardService", "BroadcastService", "$filter", controller]);
    angular.module(module_name_1.moduleName)
        .directive('tbaFeedStatusIndicator', [function () {
            return {
                restrict: "EA",
                scope: true,
                controllerAs: 'vm',
                bindToController: {
                    panelTitle: "@"
                },
                templateUrl: 'js/ops-mgr/overview/feed-status-indicator/feed-status-indicator-template.html',
                controller: "FeedStatusIndicatorController",
                link: function ($scope, element, attrs) {
                }
            };
        }]);
});
//# sourceMappingURL=FeedsStatusIndicatorDirective.js.map