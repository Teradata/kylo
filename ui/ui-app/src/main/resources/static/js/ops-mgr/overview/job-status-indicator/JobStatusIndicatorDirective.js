define(["require", "exports", "angular", "../module-name", "underscore", "../../services/OpsManagerDashboardService", "../../services/OpsManagerJobService", "../../services/ChartJobStatusService"], function (require, exports, angular, module_name_1, _, OpsManagerDashboardService_1, OpsManagerJobService_1, ChartJobStatusService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $element, $http, $q, $interval, StateService, OpsManagerJobService, OpsManagerDashboardService, HttpService, ChartJobStatusService, BroadcastService) {
            var _this = this;
            this.$scope = $scope;
            this.$element = $element;
            this.$http = $http;
            this.$q = $q;
            this.$interval = $interval;
            this.StateService = StateService;
            this.OpsManagerJobService = OpsManagerJobService;
            this.OpsManagerDashboardService = OpsManagerDashboardService;
            this.HttpService = HttpService;
            this.ChartJobStatusService = ChartJobStatusService;
            this.BroadcastService = BroadcastService;
            this.updateChart = function () {
                if (_this.chartApi.update) {
                    _this.chartApi.update();
                }
            };
            this.chartClick = function () {
                _this.StateService.OpsManager().Job().navigateToJobs("Running", null);
            };
            this.getRunningFailedCounts = function () {
                var successFn = function (response) {
                    if (response) {
                        _this.updateCounts(response.data);
                        if (_this.runningCounts.length >= _this.maxDatapoints) {
                            _this.runningCounts.shift();
                            _this.chartData[0].values.shift();
                        }
                        var dataItem = { status: 'RUNNING_JOB_ACTIVITY', date: new Date().getTime(), count: _this.running };
                        _this.runningCounts.push(dataItem);
                        _this.addChartData(dataItem);
                        _this.dataLoaded = true;
                    }
                };
                var errorFn = function (data, status, headers, config) {
                    console.log("Error getting count by status: ", data, status);
                };
                _this.$http.get(_this.OpsManagerJobService.RUNNING_JOB_COUNTS_URL).then(successFn, errorFn);
            };
            this.refresh = function () {
                _this.getRunningFailedCounts();
            };
            this.updateCounts = function (responseData) {
                //zero them out
                _this.running = 0;
                _this.failed = 0;
                if (responseData) {
                    angular.forEach(responseData, function (statusCount, i) {
                        if (statusCount.status == 'RUNNING') {
                            _this.running += statusCount.count;
                        }
                        else if (statusCount.status == 'FAILED') {
                            _this.failed += statusCount.count;
                        }
                    });
                    _this.ensureFeedSummaryMatches(responseData);
                }
            };
            /**
             * Job Status Counts run every second.
             * Feed Healh/Running data runs every 5 seconds.
             * if the Job Status changes, update the summary data and notify the Feed Health Card
             * @param jobStatus the list of Job Status counts by feed
             */
            this.ensureFeedSummaryMatches = function (jobStatus) {
                var summaryData = _this.OpsManagerDashboardService.feedSummaryData;
                var feedSummaryUpdated = [];
                var runningFeedNames = [];
                var notify = false;
                _.each(jobStatus, function (feedJobStatusCounts) {
                    var feedSummary = summaryData[feedJobStatusCounts.feedName];
                    if (angular.isDefined(feedSummary)) {
                        var summaryState = feedSummary.state;
                        if (feedJobStatusCounts.status == "RUNNING") {
                            runningFeedNames.push(feedJobStatusCounts.feedName);
                        }
                        if (feedJobStatusCounts.status == "RUNNING" && summaryState != "RUNNING") {
                            //set it
                            feedSummary.state = "RUNNING";
                            feedSummary.runningCount = summaryData.count;
                            //trigger update of feed summary
                            feedSummaryUpdated.push(feedSummary);
                            notify = true;
                        }
                    }
                });
                //any of those that are not in the runningFeedNames are not running anymore
                var notRunning = _.difference(Object.keys(summaryData), runningFeedNames);
                _.each(notRunning, function (feedName) {
                    var summary = summaryData[feedName];
                    if (summary && summary.state == "RUNNING") {
                        summary.state = "WAITING";
                        summary.runningCount = 0;
                        feedSummaryUpdated.push(summary);
                        notify = true;
                    }
                });
                if (notify = true) {
                    _this.BroadcastService.notify(_this.OpsManagerDashboardService.FEED_SUMMARY_UPDATED, feedSummaryUpdated);
                }
            };
            this.addChartData = function (data) {
                if (_this.chartData.length > 0) {
                    _this.chartData[0].values.push([data.date, data.count]);
                }
                else {
                    var initialChartData = _this.ChartJobStatusService.toChartData([data]);
                    initialChartData[0].key = 'Running';
                    _this.chartData = initialChartData;
                }
                var max = d3.max(_this.runningCounts, function (d) {
                    return d.count;
                });
                if (max == undefined || max == 0) {
                    max = 1;
                }
                else {
                    max += 1;
                }
                if (_this.chartOptions.chart.yAxis.ticks != max) {
                    _this.chartOptions.chart.yDomain = [0, max];
                    var ticks = max;
                    if (ticks > 8) {
                        ticks = 8;
                    }
                    _this.chartOptions.chart.yAxis.ticks = ticks;
                }
            };
            this.createChartData = function (responseData) {
                _this.chartData = _this.ChartJobStatusService.toChartData(responseData);
                var max = d3.max(_this.runningCounts, function (d) {
                    return d.count;
                });
                if (max == undefined || max == 0) {
                    max = 1;
                }
                else {
                    max += 1;
                }
                _this.chartOptions.chart.yDomain = [0, max];
                _this.chartOptions.chart.yAxis.ticks = max;
                //  this.chartApi.update();
            };
            this.clearRefreshInterval = function () {
                if (_this.refreshInterval != null) {
                    _this.$interval.cancel(_this.refreshInterval);
                    _this.refreshInterval = null;
                }
            };
            this.setRefreshInterval = function () {
                _this.clearRefreshInterval();
                if (_this.refreshIntervalTime) {
                    _this.refreshInterval = _this.$interval(_this.refresh, _this.refreshIntervalTime);
                }
            };
            this.init = function () {
                _this.refresh();
                _this.setRefreshInterval();
            };
            this.refreshInterval = null;
            this.dataLoaded = false;
            this.chartApi = {};
            this.chartConfig = {};
            this.running = 0;
            this.failed = 0;
            this.chartData = [];
            this.runningCounts = [];
            var maxDatapoints = 20;
            this.chartOptions = {
                chart: {
                    type: 'lineChart',
                    margin: {
                        top: 5,
                        right: 5,
                        bottom: 10,
                        left: 20
                    },
                    x: function (d) { return d[0]; },
                    y: function (d) { return d[1]; },
                    useVoronoi: false,
                    clipEdge: false,
                    duration: 0,
                    height: 136,
                    useInteractiveGuideline: true,
                    xAxis: {
                        axisLabel: 'Time',
                        showMaxMin: false,
                        tickFormat: function (d) {
                            return d3.time.format('%X')(new Date(d));
                        }
                    },
                    yAxis: {
                        axisLabel: '',
                        "axisLabelDistance": -10,
                        showMaxMin: false,
                        tickSubdivide: 0,
                        ticks: 1
                    },
                    yDomain: [0, 1],
                    showLegend: false,
                    showXAxis: false,
                    showYAxis: true,
                    lines: {
                        dispatch: {
                            'elementClick': function (e) {
                                _this.chartClick();
                            }
                        }
                    },
                    dispatch: {}
                }
            };
            this.init();
            $scope.$on('$destroy', function () {
                _this.clearRefreshInterval();
            });
        } // end of constructor
        return controller;
    }());
    exports.default = controller;
    angular.module(module_name_1.moduleName)
        .service('OpsManagerDashboardService', ['$q', '$http', '$interval', '$timeout', 'HttpService', 'IconService', 'AlertsService', 'OpsManagerRestUrlService', 'BroadcastService', 'OpsManagerFeedService', OpsManagerDashboardService_1.default])
        .service('OpsManagerJobService', ['$q', '$http', '$log', 'HttpService', 'NotificationService', 'OpsManagerRestUrlService', OpsManagerJobService_1.default])
        .service('ChartJobStatusService', ["IconService", "Nvd3ChartService", ChartJobStatusService_1.default])
        .controller('JobStatusIndicatorController', ["$scope", "$element", "$http", "$q", "$interval", "StateService",
        "OpsManagerJobService", "OpsManagerDashboardService",
        "HttpService", "ChartJobStatusService", "BroadcastService", controller]);
    angular.module(module_name_1.moduleName)
        .directive('tbaJobStatusIndicator', [function () {
            return {
                restrict: "EA",
                scope: true,
                controllerAs: 'vm',
                bindToController: {
                    panelTitle: "@",
                    refreshIntervalTime: "=?"
                },
                templateUrl: 'js/ops-mgr/overview/job-status-indicator/job-status-indicator-template.html',
                controller: "JobStatusIndicatorController",
                link: function ($scope, element, attrs) {
                    $scope.$on('$destroy', function () {
                    });
                } //DOM manipulation\}
            };
        }]);
});
//# sourceMappingURL=JobStatusIndicatorDirective.js.map