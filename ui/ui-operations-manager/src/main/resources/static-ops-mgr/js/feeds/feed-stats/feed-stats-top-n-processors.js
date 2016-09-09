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
            'Jobs Started': {
                axisLabel: 'Count', fn: function (stats) {
                    return stats.jobsStarted
                }
            },
            'Jobs Finished': {
                axisLabel: 'Count', fn: function (stats) {
                    return stats.jobsFinished
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
                    if (d.key == 'Successful Jobs') {
                        return '#009933';
                    }
                    else if (d.key == 'Failed Jobs') {
                        return '#FF0000';
                    }
                    else if (d.key == 'Running Jobs') {
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
                },
                "zoom": {
                    "enabled": true,
                    "verticalOff": true,
                    "unzoomEventType": "dblclick.zoom"
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
                },
                "zoom": {
                    "enabled": true,
                    "verticalOff": true,
                    "unzoomEventType": "dblclick.zoom"
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

        function loadTimeFrameOption() {
            ProvenanceEventStatsService.getTimeFrameOptions().then(function (opts) {
                console.log(opts);
                self.timeFrameOptions = opts.data;
            })
        }

        self.onTimeFrameChange = function () {
            clearRefreshInterval();
            buildChartData();
            setRefreshInterval();
        }

        loadTimeFrameOption();

        self.jobsStarted = 0;
        self.jobsFinished = 0;
        self.jobsFailed = 0;
        self.avgJobDuration = 0;
        self.totalProcessorSelectedFunctionValue

        //stats for pie chart
        self.jobsRunning = 0;
        self.jobsSuccess = 0;

        self.onProcessorChartFunctionChanged = function () {
            buildProcessorChartData();
        }

        function buildChartData() {
            buildProcessorChartData();
            buildFeedCharts();
        }

        function buildProcessorChartData() {
            var values = [];
            var processorNameCount = {};
            var processorIdNameMap = {};
            $q.when(ProvenanceEventStatsService.getFeedProcessorDuration(self.feedName, self.timeFrame)).then(function (processorStats) {
                var jobsStarted = 0;
                var jobsFinished = 0;
                var jobDuration = 0;
                var jobsFailed = 0;
                var jobsSuccess = 0;
                var jobsRunning = 0;
                var total = 0;
                _.each(processorStats.data, function (p) {

                    if (processorIdNameMap[p.processorId] == undefined) {
                        if (processorNameCount[p.processorName] == undefined) {
                            processorNameCount[p.processorName] = 0;
                            processorIdNameMap[p.processorId] = p.processorName;
                        }
                        else {
                            processorNameCount[p.processorName] = processorNameCount[p.processorName] + 1;
                            processorIdNameMap[p.processorId] = p.processorName + " - " + processorNameCount[p.processorName];
                        }
                    }
                    var processorName = processorIdNameMap[p.processorId];
                    var v = self.processorStatsFunctionMap[self.selectedProcessorStatisticFunction].fn(p);
                    values.push({label: processorName, value: v});
                    jobsStarted += p.jobsStarted;
                    jobsFinished += p.jobsFinished;
                    jobDuration += p.jobDuration;
                    jobsFailed += p.jobsFailed;
                    jobsSuccess = (jobsFinished - jobsFailed);
                    jobsRunning = (jobsStarted - jobsFinished) < 0 ? 0 : (jobsStarted - jobsFinished);
                    total += v;
                });
                var configMap = self.processorStatsFunctionMap[self.selectedProcessorStatisticFunction];

                self.dataLoaded = true;
                self.lastRefreshTime = new Date();
                var data = [{key: "Processor", "color": "#1f77b4", values: values}];
                self.processorDurationChartData = data
                self.jobsStarted = jobsStarted;
                self.jobsFinished = jobsFinished;
                self.jobsFailed = jobsFailed;
                self.jobsRunning = jobsRunning;
                self.jobsSuccess = jobsSuccess
                self.avgJobDuration = ((jobDuration / jobsFinished) / 1000).toFixed(2)
                if (configMap.valueFormatFn != undefined) {
                    total = configMap.valueFormatFn(total);
                }
                self.totalProcessorSelectedFunctionValue = total;

                self.statusPieChartData = [];
                self.statusPieChartData.push({key: "Successful Jobs", value: self.jobsSuccess})
                self.statusPieChartData.push({key: "Failed Jobs", value: self.jobsFailed})
                self.statusPieChartData.push({key: "Running Jobs", value: self.jobsRunning});
                if (self.chartApi && self.chartApi.update) {

                    self.processorDurationChartOptions.chart.yAxis.axisLabel = configMap.axisLabel
                    self.processorDurationChartOptions.chart.height = 50 * Object.keys(processorIdNameMap).length;
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
                    console.log('HEIGHT ', self.processorDurationChartOptions.chart.height)
                    self.chartApi.update();
                }

            });


        };

        function buildFeedCharts() {

            $q.when(ProvenanceEventStatsService.getFeedStatisticsOverTime(self.feedName, self.timeFrame)).then(function (feedStats) {

                buildFeedTimeChartData(feedStats.data);
                buildFeedBytesChartData(feedStats.data);
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

        function buildFeedBytesChartData(feedStats) {

            var chartArr = [];
            chartArr.push({label: 'Bytes In', value: 'bytesIn', color: '#009933', area: false});
            chartArr.push({label: 'Bytes Out', value: 'bytesOut', color: '#FD9B28', area: false});
            //preserve the legend selections
            if (feedBytesChartLegendState.length > 0) {
                _.each(chartArr, function (item, i) {
                    item.disabled = feedBytesChartLegendState[i];
                });
            }

            self.feedBytesChartData = Nvd3ChartService.toLineChartData(feedStats, chartArr, 'maxEventTime');

        }

        buildChartData();

        setRefreshInterval();

        function clearRefreshInterval() {
            if (self.refreshInterval != null) {
                $interval.cancel(self.refreshInterval);
                self.refreshInterval = null;
            }
        }

        function setRefreshInterval() {
            clearRefreshInterval();
            if (self.refreshIntervalTime) {
                self.refreshInterval = $interval(buildChartData, self.refreshIntervalTime);

            }
        }

        $scope.$on('$destroy', function () {
            clearRefreshInterval();
        });

    };

    angular.module(MODULE_OPERATIONS).controller('FeedStatsTopNProcessorsController', controller);

    angular.module(MODULE_OPERATIONS)
        .directive('tbaFeedStatsTopNProcessorsChart', directive);

}());