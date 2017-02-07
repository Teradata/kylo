/*-
 * #%L
 * thinkbig-ui-operations-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
(function () {

    var directive = function () {
        return {
            restrict: "EA",
            scope: {},
            bindToController: {
                panelTitle: "@",
                refreshIntervalTime: "@",
                feedName: '@'
            },
            controllerAs: 'vm',
            templateUrl: 'js/feeds/feed-stats/feed-stats-top-n-processors.html',
            controller: "FeedStatsTopNProcessorsController",
            link: function ($scope, element, attrs) {
                $scope.$on('$destroy', function () {

                });
            } //DOM manipulation\}
        }

    };

    var controller = function ($scope, $element, $http, $interval, $timeout, $q, ProvenanceEventStatsService, Nvd3ChartService) {
        var self = this;
        this.dataLoaded = false;

        /** flag when processor chart is loading **/
        this.processChartLoading = false;

        /**
         * the last time the data was refreshed
         * @type {null}
         */
        this.lastProcessorChartRefresh = null;

        /**
         * last time the execution graph was refreshed
         * @type {null}
         */
        this.lastFeedTimeChartRefresh = null;

        /** flag when the feed time chart is loading **/
        this.showFeedTimeChartLoading = true;

        this.showProcessorChartLoading = true;

        this.chartApi = {};

        this.statusPieChartApi = {};

        self.selectedProcessorStatisticFunction = 'Average Duration';

        self.processorStatsFunctionMap = {
            'Average Duration': {
                axisLabel: 'Time (sec)', fn: function (stats) {
                    return (stats.duration / stats.totalCount) / 1000
                }
            },
            'Bytes In': {
                axisLabel: 'Bytes', valueFormatFn: function (d) {
                    return bytesToString(d);
                }, fn: function (stats) {
                    return stats.bytesIn
                }
            },
            'Bytes Out': {
                axisLabel: 'Bytes', valueFormatFn: function (d) {
                    return bytesToString(d);
                }, fn: function (stats) {
                    return stats.bytesOut
                }
            },
            'Flow Files Started': {
                axisLabel: 'Count', fn: function (stats) {
                    return stats.flowFilesStarted
                }
            },
            'Flow Files Finished': {
                axisLabel: 'Count', fn: function (stats) {
                    return stats.flowFilesFinished
                }
            },
            'Flows Started': {
                axisLabel: 'Count', fn: function (stats) {
                    return stats.flowsStarted
                }
            },
            'Flows Finished': {
                axisLabel: 'Count', fn: function (stats) {
                    return stats.flowsFinished
                }
            },
            'Total Events': {
                axisLabel: 'Count', fn: function (stats) {
                    return stats.totalCount
                }
            }

        }

        var bytesToString = function (bytes) {

            var fmt = d3.format('.0f');
            if (bytes < 1024) {
                return fmt(bytes) + 'B';
            } else if (bytes < 1024 * 1024) {
                return fmt(bytes / 1024) + 'kB';
            } else if (bytes < 1024 * 1024 * 1024) {
                return fmt(bytes / 1024 / 1024) + 'MB';
            } else {
                return fmt(bytes / 1024 / 1024 / 1024) + 'GB';
            }

        }

        self.processorStatsFunctions = Object.keys(self.processorStatsFunctionMap);



        self.processorDurationChartOptions = {
            chart: {
                type: 'multiBarHorizontalChart',
                height: 600,
                margin: {
                    top: 5, //otherwise top of numeric value is cut off
                    right: 50,
                    bottom: 50, //otherwise bottom labels are not visible
                    left: 150
                },
                duration: 500,
                x: function (d) {
                    return d.label.length > 60 ? d.label.substr(0, 60) + "..." : d.label;
                },
                y: function (d) {
                    return d.value;
                },
                showControls: false,
                showValues: true,
                xAxis: {
                    showMaxMin: false
                },
                interactiveLayer: {tooltip: {gravity: 's'}},
                yAxis: {
                    axisLabel: self.processorStatsFunctionMap[self.selectedProcessorStatisticFunction].axisLabel,
                    tickFormat: function (d) {
                        return d3.format(',.2f')(d);
                    }
                },
                valueFormat: function (d) {
                    return d3.format(',.2f')(d);
                }
            }
        };

        self.processorDurationChartData = [];

        this.statusPieChartApi = {};

        this.statusPieChartOptions = {
            chart: {
                type: 'pieChart',
                x: function (d) {
                    return d.key;
                },
                y: function (d) {
                    return d.value;
                },
                showLabels: false,
                duration: 100,
                "height": 150,
                labelThreshold: 0.01,
                labelSunbeamLayout: false,
                interactiveLayer: {tooltip: {gravity: 's'}},
                "margin": {"top": 10, "right": 10, "bottom": 10, "left": 10},
                donut: false,
                // donutRatio: 0.65,
                showLegend: false,
                valueFormat: function (d) {
                    return parseInt(d);
                },
                color: function (d) {
                    if (d.key == 'Successful Flows') {
                        return '#009933';
                    }
                    else if (d.key == 'Failed Flows') {
                        return '#FF0000';
                    }
                    else if (d.key == 'Running Flows') {
                        return '#FF9901';
                    }
                },
                dispatch: {
                    renderEnd: function () {

                    }
                }
            }
        };

        var feedChartLegendState = []
        this.feedChartData = [];
        this.feedChartApi = {};
        this.feedChartOptions = {
            chart: {
                type: 'lineChart',
                height: 450,
                margin: {
                    top: 10,
                    right: 20,
                    bottom: 150,
                    left: 55
                },
                x: function (d) {
                    return d[0];
                },
                y: function (d) {
                    return d[1];
                },
                useVoronoi: false,
                clipEdge: false,
                duration: 250,
                useInteractiveGuideline: true,
                interactiveLayer: {tooltip: {gravity: 's'}},
                xAxis: {
                    axisLabel: 'Event Time',
                    showMaxMin: false,
                    tickFormat: function (d) {
                        return d3.time.format('%x %X')(new Date(d))
                    },
                    rotateLabels: -45
                },
                yAxis: {
                    axisLabel: 'Count',
                    axisLabelDistance: -10
                },
                legend: {
                    dispatch: {
                        stateChange: function (e) {
                            feedChartLegendState = e.disabled;
                        }
                    }
                },
                dispatch: {
                    renderEnd: function () {
                        // fixChartWidth();
                    }
                }
            },
            title: {
                enable: true,
                text: 'Job status over time'
            }

        };

        var feedBytesChartLegendState = []
        this.feedBytesChartData = [];
        this.feedBytesChartApi = {};
        this.feedBytesChartOptions = {
            chart: {
                type: 'lineChart',
                height: 450,
                margin: {
                    top: 10,
                    right: 20,
                    bottom: 150,
                    left: 100
                },
                x: function (d) {
                    return d[0];
                },
                y: function (d) {
                    return d[1];
                },
                useVoronoi: false,
                clipEdge: false,
                duration: 250,
                useInteractiveGuideline: true,
                interactiveLayer: {tooltip: {gravity: 's'}},
                valueFormat: function (d) {
                    return bytesToString(d);
                },
                xAxis: {
                    axisLabel: 'Bytes',
                    showMaxMin: false,
                    tickFormat: function (d) {
                        return d3.time.format('%x %X')(new Date(d))
                    },
                    rotateLabels: -45
                },
                yAxis: {
                    axisLabel: 'Total Bytes',
                    axisLabelDistance: -10,
                    tickFormat: function (d) {
                        return bytesToString(d);
                    }
                },
                legend: {
                    dispatch: {
                        stateChange: function (e) {
                            feedBytesChartLegendState = e.disabled;
                        }
                    }
                },
                dispatch: {
                    renderEnd: function () {
                        // fixChartWidth();
                    }
                }
            },
            title: {
                enable: true,
                text: 'Bytes in/out over time'
            }

        };









        self.timeframeOptions = [];
        self.timeFrame = 'DAY';
        self.lastRefreshTime = null;
        self.timeFramOptionsLookupMap = {};
        self.selectedTimeFrameOptionObject = {};

        function isLoading() {
            return self.processChartLoading || self.pieChartLoading;
        }

        function loadTimeFrameOption() {
            ProvenanceEventStatsService.getTimeFrameOptions().then(function (response) {
                self.timeFrameOptions = response.data;
                _.each(response.data, function (labelValue) {
                    self.timeFramOptionsLookupMap[labelValue.value] = labelValue;
                });
            })
        }

        self.onTimeFrameChange = function () {
            clearRefreshInterval();
            buildChartData();
            setRefreshInterval();
            //update selected timewindow
            self.selectedTimeFrameOptionObject = null;
            var timeFrameObject = self.timeFramOptionsLookupMap[self.timeFrame];
            if (timeFrameObject != null) {
                self.selectedTimeFrameOptionObject = timeFrameObject;
            }
            //  console.log('timeframe ', self.selectedTimeFrameOptionObject, self.timeFramOptionsLookupMap)
        }

        loadTimeFrameOption();
        self.minTime = null;
        self.maxTime = null;
        self.flowsStarted = 0;
        self.flowsFinished = 0;
        self.flowsFailed = 0;
        self.avgFlowDuration = 0;
        self.totalProcessorSelectedFunctionValue = 0;

        //stats for pie chart
        self.flowsRunning = 0;
        self.flowsSuccess = 0;

        self.onProcessorChartFunctionChanged = function () {
            buildProcessorChartData();
        }

        function buildChartData() {
            if (!isLoading()) {
                self.feedTimeChartLoading = true;
                self.processChartLoading = true;
                buildProcessorChartData();
                buildFeedCharts();
            }
        }

        var processorNameMap = {};

        function buildProcessorChartData() {
            var values = [];

            $q.when(ProvenanceEventStatsService.getFeedProcessorDuration(self.feedName, self.timeFrame)).then(function (processorStats) {
                var flowsStarted = 0;
                var flowsFinished = 0;
                var flowDuration = 0;
                var flowsFailed = 0;
                var flowsSuccess = 0;
                var flowsRunning = 0;
                var total = 0;
                _.each(processorStats.data, function (p) {
                    var key = p.processorName;
                    if (key == undefined || key == null) {
                        key = 'N/A';
                    }

                    var v = self.processorStatsFunctionMap[self.selectedProcessorStatisticFunction].fn(p);
                    values.push({label: key, value: v});
                    flowsStarted += p.jobsStarted;
                    flowsFinished += p.jobsFinished;
                    flowDuration += p.jobDuration;
                    flowsFailed += p.jobsFailed;
                    flowsSuccess = (flowsFinished - flowsFailed);
                    flowsRunning = (flowsStarted - flowsFinished) < 0 ? 0 : (flowsStarted - flowsFinished);
                    total += v;
                });
                var configMap = self.processorStatsFunctionMap[self.selectedProcessorStatisticFunction];

                values = _.sortBy(values, 'label');

                self.dataLoaded = true;
                self.lastRefreshTime = new Date();
                var data = [{key: "Processor", "color": "#1f77b4", values: values}];
                self.processorDurationChartData = data
                self.flowsStarted = flowsStarted;
                self.flowsFinished = flowsFinished;
                self.flowsFailed = flowsFailed;
                self.flowsRunning = flowsRunning;
                self.flowsSuccess = flowsSuccess
                self.avgFlowDuration = flowsFinished > 0 ? ((flowDuration / flowsFinished) / 1000).toFixed(2) : 0;
                if (configMap.valueFormatFn != undefined) {
                    total = configMap.valueFormatFn(total);
                }
                self.totalProcessorSelectedFunctionValue = total;

                self.statusPieChartData = [];
                self.statusPieChartData.push({key: "Successful Flows", value: self.flowsSuccess})
                self.statusPieChartData.push({key: "Failed Flows", value: self.flowsFailed})
                self.statusPieChartData.push({key: "Running Flows", value: self.flowsRunning});
                if (self.chartApi && self.chartApi.update) {

                    self.processorDurationChartOptions.chart.yAxis.axisLabel = configMap.axisLabel
                    self.processorDurationChartOptions.chart.height = 50 * values.length;
                    if (configMap.valueFormatFn != undefined) {
                        self.processorDurationChartOptions.chart.valueFormat = configMap.valueFormatFn;
                        self.processorDurationChartOptions.chart.yAxis.tickFormat = configMap.valueFormatFn;
                    }
                    else {
                        self.processorDurationChartOptions.chart.valueFormat = function (d) {
                            return d3.format(',.2f')(d);
                        };

                        self.processorDurationChartOptions.chart.yAxis.tickFormat = function (d) {
                            return d3.format(',.2f')(d);
                        }
                    }
                    self.chartApi.update();
                }
                self.processChartLoading = false;
                self.lastProcessorChartRefresh = new Date().getTime();

            }, function () {
                self.processChartLoading = false;
                self.lastProcessorChartRefresh = new Date().getTime();
            });


        };

        function buildFeedCharts() {

            $q.when(ProvenanceEventStatsService.getFeedStatisticsOverTime(self.feedName, self.timeFrame)).then(function (feedStats) {
                var timeArr = _.map(feedStats.data, function (item) {
                    return item.maxEventTime
                });
                self.minTime = ArrayUtils.min(timeArr);
                self.maxTime = ArrayUtils.max(timeArr);
                buildFeedTimeChartData(feedStats.data);
                self.feedTimeChartLoading = false;
                self.lastFeedTimeChartRefresh = new Date().getTime();
            }, function () {
                self.feedTimeChartLoading = false;
                self.lastFeedTimeChartRefresh = new Date().getTime();
            });

        }

        function buildFeedTimeChartData(feedStats) {

            var chartArr = [];
            chartArr.push({label: 'Completed', value: 'jobsFinished', color: '#009933'});
            chartArr.push({label: 'Failed', value: 'jobsFailed', color: '#FF0000'});
            //preserve the legend selections
            if (feedChartLegendState.length > 0) {
                _.each(chartArr, function (item, i) {
                    item.disabled = feedChartLegendState[i];
                });
            }

            self.feedChartData = Nvd3ChartService.toLineChartData(feedStats, chartArr, 'maxEventTime');

        }



        buildChartData();

        setRefreshInterval();

        function clearRefreshInterval() {
            if (self.refreshInterval != null) {
                $interval.cancel(self.refreshInterval);
                self.refreshInterval = null;
            }
            if (self.showProgressInterval != null && self.showProgressInterval != undefined) {
                $interval.cancel(self.showProgressInterval);
                self.showProgressInterval = null;
            }
        }

        function setRefreshInterval() {
            clearRefreshInterval();
            if (self.refreshIntervalTime) {
                self.refreshInterval = $interval(buildChartData, self.refreshIntervalTime);

                self.showProgressInterval = $interval(checkAndShowLoadingProgress, 1000);
            }
        }

        function showProcessorLoadingProgress() {
            if (self.lastProcessorChartRefresh == null) {
                return true;
            }
            else {
                var diff = new Date().getTime() - self.lastProcessorChartRefresh - self.refreshIntervalTime;
                //if its been more than 700 ms processing,
                if (diff > 700) {
                    return true;
                }
                else {
                    return false;
                }
            }
        }

        function showFeedTimeChartLoadingProgress() {
            if (self.lastProcessorChartRefresh == null) {
                return true;
            }
            else {
                var diff = new Date().getTime() - self.lastFeedTimeChartRefresh - self.refreshIntervalTime;
                //if its been more than 700 ms processing,
                if (diff > 700) {
                    return true;
                }
                else {
                    return false;
                }
            }
        }

        function checkAndShowLoadingProgress() {
            self.showFeedTimeChartLoading = showFeedTimeChartLoadingProgress();
            self.showProcessorChartLoading = showProcessorLoadingProgress();

        }


        $scope.$on('$destroy', function () {
            clearRefreshInterval();
        });

    };

    angular.module(MODULE_OPERATIONS).controller('FeedStatsTopNProcessorsController', controller);

    angular.module(MODULE_OPERATIONS)
        .directive('tbaFeedStatsTopNProcessorsChart', directive);

}());
