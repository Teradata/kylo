define(["require", "exports", "angular", "./module-name", "pascalprecht.translate"], function (require, exports, angular, module_name_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $http, $interval, $timeout, $q, Utils, OpsManagerFeedService, TableOptionsService, PaginationDataService, StateService, ChartJobStatusService, BroadcastService, $filter) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.$interval = $interval;
            this.$timeout = $timeout;
            this.$q = $q;
            this.Utils = Utils;
            this.OpsManagerFeedService = OpsManagerFeedService;
            this.TableOptionsService = TableOptionsService;
            this.PaginationDataService = PaginationDataService;
            this.StateService = StateService;
            this.ChartJobStatusService = ChartJobStatusService;
            this.BroadcastService = BroadcastService;
            this.$filter = $filter;
            this.updateCharts = function () {
                _this.query();
                _this.updateChart();
            };
            this.updateChart = function () {
                if (_this.chartApi.update) {
                    _this.chartApi.update();
                }
            };
            this.fixChartWidth = function () {
                var chartWidth = parseInt($($('.nvd3-svg')[0]).find('rect:first').attr('width'));
                if (chartWidth < 100) {
                    _this.updateChart();
                    if (_this.fixChartWidthCounter == undefined) {
                        _this.fixChartWidthCounter = 0;
                    }
                    _this.fixChartWidthCounter++;
                    if (_this.fixChartWidthTimeout) {
                        _this.$timeout.cancel(_this.fixChartWidthTimeout);
                    }
                    if (_this.fixChartWidthCounter < 1000) {
                        _this.fixChartWidthTimeout = _this.$timeout(function () {
                            _this.fixChartWidth();
                        }, 10);
                    }
                }
                else {
                    if (_this.fixChartWidthTimeout) {
                        _this.$timeout.cancel(_this.fixChartWidthTimeout);
                    }
                    _this.fixChartWidthCounter = 0;
                }
            };
            this.createChartData = function (responseData) {
                _this.chartData = _this.ChartJobStatusService.toChartData(responseData);
            };
            this.parseDatePart = function () {
                var interval = parseInt(_this.dateSelection.substring(0, _this.dateSelection.indexOf('-')));
                var datePart = _this.dateSelection.substring(_this.dateSelection.indexOf('-') + 1);
                _this.datePart = datePart;
                _this.interval = interval;
            };
            this.query = function () {
                var successFn = function (response) {
                    if (response.data) {
                        //transform the data for UI
                        _this.createChartData(response.data);
                        if (_this.loading) {
                            _this.loading = false;
                        }
                        if (!_this.dataLoaded && response.data.length == 0) {
                            setTimeout(function () {
                                _this.dataLoaded = true;
                            }, 500);
                        }
                        else {
                            _this.dataLoaded = true;
                        }
                    }
                };
                var errorFn = function (err) {
                };
                var finallyFn = function () {
                };
                _this.$http.get(_this.OpsManagerFeedService.DAILY_STATUS_COUNT_URL(_this.feedName), { params: { "period": _this.interval + _this.datePart } }).then(successFn, errorFn);
            };
            this.pageName = 'feed-activity';
            this.dataLoaded = false;
            this.dateSelection = '1-M';
            this.chartData = [];
            this.chartApi = {};
            this.chartOptions = {
                chart: {
                    type: 'lineChart',
                    height: 250,
                    margin: {
                        top: 10,
                        right: 20,
                        bottom: 40,
                        left: 55
                    },
                    x: function (d) { return d[0]; },
                    y: function (d) { return d[1]; },
                    useVoronoi: false,
                    clipEdge: false,
                    duration: 250,
                    useInteractiveGuideline: true,
                    noData: $filter('translate')('views.views.FeedActivityDirective.noData'),
                    xAxis: {
                        axisLabel: $filter('translate')('views.FeedActivityDirective.Date'),
                        showMaxMin: false,
                        tickFormat: function (d) {
                            return d3.time.format('%x')(new Date(d));
                        }
                    },
                    yAxis: {
                        axisLabel: $filter('translate')('views.FeedActivityDirective.Count'),
                        axisLabelDistance: -10
                    },
                    dispatch: {
                        renderEnd: function () {
                            _this.fixChartWidth();
                        }
                    }
                }
            };
            /*  zoom: {
             enabled: true,
             scaleExtent: [1, 10],
             useFixedDomain: false,
             useNiceScale: false,
             horizontalOff: false,
             verticalOff: true,
             unzoomEventType: 'dblclick.zoom'
             }*/
            BroadcastService.subscribe($scope, 'ABANDONED_ALL_JOBS', this.updateCharts);
            $scope.$watch(function () {
                return _this.dateSelection;
            }, function (newVal) {
                _this.parseDatePart();
                _this.query();
            });
        } // end of constructor
        return controller;
    }());
    exports.controller = controller;
    angular.module(module_name_1.moduleName)
        .controller('FeedActivityController', ["$scope", "$http", "$interval", "$timeout", "$q", "Utils",
        "OpsManagerFeedService", "TableOptionsService", "PaginationDataService", "StateService",
        "ChartJobStatusService", "BroadcastService", "$filter", controller]);
    angular.module(module_name_1.moduleName)
        .directive('tbaFeedActivity', [function (Utils) {
            return {
                restrict: "EA",
                bindToController: {
                    feedName: "="
                },
                controllerAs: 'vm',
                scope: {},
                templateUrl: 'js/ops-mgr/feeds/feed-activity-template.html',
                controller: "FeedActivityController",
                link: function ($scope, element, attrs, controller) {
                },
                compile: function () {
                    return function (scope, element, attr) {
                        // Utils.replaceWithChild(element);
                    };
                }
            };
        }
    ]);
});
//# sourceMappingURL=FeedActivityDirective.js.map