define(["require", "exports", "angular", "../module-name", "underscore", "pascalprecht.translate"], function (require, exports, angular, module_name_1, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $element, $http, $interval, $mdDialog, OpsManagerJobService, OpsManagerDashboardService, BroadcastService, $filter) {
            var _this = this;
            this.$scope = $scope;
            this.$element = $element;
            this.$http = $http;
            this.$interval = $interval;
            this.$mdDialog = $mdDialog;
            this.OpsManagerJobService = OpsManagerJobService;
            this.OpsManagerDashboardService = OpsManagerDashboardService;
            this.BroadcastService = BroadcastService;
            this.$filter = $filter;
            this.openDetailsDialog = function (key) {
                _this.$mdDialog.show({
                    controller: "DataConfidenceDetailsDialogController",
                    templateUrl: 'js/ops-mgr/overview/data-confidence-indicator/data-confidence-details-dialog.html',
                    parent: angular.element(document.body),
                    clickOutsideToClose: true,
                    fullscreen: true,
                    locals: {
                        status: key,
                        allChartData: _this.allData
                    }
                });
            };
            this.onHealthyClick = function () {
                if (_this.dataMap.Healthy.count > 0) {
                    _this.openDetailsDialog('Healthy');
                }
            };
            this.onUnhealthyClick = function () {
                if (_this.dataMap.Unhealthy.count > 0) {
                    _this.openDetailsDialog('Unhealthy');
                }
            };
            this.updateChart = function () {
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
            this.initializePieChart = function () {
                this.chartData.push({ key: "Healthy", value: 0 });
                this.chartData.push({ key: "Unhealthy", value: 0 });
            };
            this.getDataConfidenceSummary = function () {
                if (_this.refreshing == false) {
                    _this.refreshing = true;
                    var data = _this.OpsManagerDashboardService.dashboard.dataConfidenceSummary;
                    if (angular.isDefined(data)) {
                        _this.allData = data;
                        if (_this.dataLoaded == false) {
                            _this.dataLoaded = true;
                        }
                        _this.dataMap.Healthy.count = data.successCount;
                        _this.dataMap.Unhealthy.count = data.failedCount;
                    }
                    _this.updateChart();
                }
                _this.refreshing = false;
            };
            this.watchDashboard = function () {
                var _this = this;
                this.BroadcastService.subscribe(this.$scope, this.OpsManagerDashboardService.DASHBOARD_UPDATED, function (dashboard) {
                    _this.getDataConfidenceSummary();
                });
            };
            this.init = function () {
                this.initializePieChart();
                this.watchDashboard();
            };
            this.refreshing = false;
            this.dataLoaded = false;
            this.dataMap = { 'Healthy': { count: 0, color: '#5cb85c' }, 'Unhealthy': { count: 0, color: '#a94442' } };
            /** All the data returned from the rest call
             *
             * @type {null}
             */
            this.allData = null;
            this.chartApi = {};
            this.chartOptions = {
                chart: {
                    type: 'pieChart',
                    x: function (d) { return d.key; },
                    y: function (d) { return d.value; },
                    showLabels: false,
                    duration: 100,
                    height: 150,
                    labelThreshold: 0.01,
                    labelSunbeamLayout: false,
                    "margin": { "top": 10, "right": 10, "bottom": 10, "left": 10 },
                    donut: true,
                    donutRatio: 0.65,
                    showLegend: false,
                    refreshDataOnly: false,
                    valueFormat: function (d) {
                        return parseInt(d);
                    },
                    color: function (d) {
                        if (d.key == 'Healthy') {
                            return '#009933';
                        }
                        else if (d.key == 'Unhealthy') {
                            return '#FF0000';
                        }
                    },
                    pie: {
                        dispatch: {
                            'elementClick': function (e) {
                                _this.openDetailsDialog(e.data.key);
                            }
                        }
                    },
                    dispatch: {}
                }
            };
            this.chartData = [];
            this.init();
            $scope.$on('$destroy', function () {
                //cleanup
            });
        } // end of constructor
        return controller;
    }());
    exports.controller = controller;
    var DataConfidenceDetailsDialogController = /** @class */ (function () {
        function DataConfidenceDetailsDialogController($scope, $mdDialog, $interval, StateService, status, allChartData) {
            this.$scope = $scope;
            this.$mdDialog = $mdDialog;
            this.$interval = $interval;
            this.StateService = StateService;
            this.status = status;
            this.allChartData = allChartData;
            $scope.jobs = this.jobs;
            if (status == 'Unhealthy') {
                $scope.css = "md-warn";
                $scope.jobs = allChartData.failedJobs;
            }
            else {
                $scope.css = "";
                $scope.jobs = _.filter(allChartData.latestCheckDataFeeds, function (job) {
                    return job.status != 'FAILED';
                });
            }
            $scope.allChartData = allChartData;
            $scope.status = status;
            $scope.hide = function () {
                $mdDialog.hide();
            };
            $scope.gotoJobDetails = function (jobExecutionId) {
                $mdDialog.hide();
                StateService.OpsManager().Job().navigateToJobDetails(jobExecutionId);
            };
            $scope.cancel = function () {
                $mdDialog.cancel();
            };
        }
        return DataConfidenceDetailsDialogController;
    }());
    exports.DataConfidenceDetailsDialogController = DataConfidenceDetailsDialogController;
    angular.module(module_name_1.moduleName).controller('DataConfidenceDetailsDialogController', ["$scope", "$mdDialog", "$interval", "StateService", "status", "allChartData", DataConfidenceDetailsDialogController]);
    angular.module(module_name_1.moduleName).controller('DataConfidenceIndicatorController', ["$scope", "$element", "$http", "$interval", "$mdDialog", "OpsManagerJobService", "OpsManagerDashboardService", "BroadcastService", "$filter", controller]);
    angular.module(module_name_1.moduleName)
        .directive('tbaDataConfidenceIndicator', [function () {
            return {
                restrict: "EA",
                scope: true,
                bindToController: {
                    panelTitle: "@"
                },
                controllerAs: 'vm',
                templateUrl: 'js/ops-mgr/overview/data-confidence-indicator/data-confidence-indicator-template.html',
                controller: "DataConfidenceIndicatorController",
                link: function ($scope, element, attrs) {
                    $scope.$on('$destroy', function () {
                    });
                } //DOM manipulation\}
            };
        }]);
});
//# sourceMappingURL=DataConfidenceIndicatorDirective.js.map