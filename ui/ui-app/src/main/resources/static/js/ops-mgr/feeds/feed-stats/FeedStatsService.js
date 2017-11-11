define(['angular','ops-mgr/feeds/feed-stats/module-name'], function (angular,moduleName) {

    angular.module(moduleName).factory('FeedStatsService', ["$q","ProvenanceEventStatsService", function ($q,ProvenanceEventStatsService) {

        var DEFAULT_CHART_FUNCTION = 'Average Duration';
        var processorStatsFunctionMap = {
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
            },/*
            'Flow Files Started': {
                axisLabel: 'Count', valueFormatFn:function(d){
                    return d3.format(',')(parseInt(d))
                }, fn: function (stats) {
                    return stats.flowFilesStarted
                }
            },
            'Flow Files Finished': {
                axisLabel: 'Count',valueFormatFn:function(d){
                    return d3.format(',')(parseInt(d))
                }, fn: function (stats) {
                    return stats.flowFilesFinished
                }
            },
            */
            'Flows Started': {
                axisLabel: 'Count', valueFormatFn:function(d){
                    return d3.format(',')(parseInt(d))
                },fn: function (stats) {
                    return stats.jobsStarted != undefined ? stats.jobsStarted : 0;
                }
            },
            'Flows Finished': {
                axisLabel: 'Count',valueFormatFn:function(d){
                    return d3.format(',')(parseInt(d))
                }, fn: function (stats) {
                    return stats.jobsFinished != undefined ? stats.jobsFinished : 0;
                }
            },
            'Total Events': {
                axisLabel: 'Count',valueFormatFn:function(d){
                    return d3.format(',')(parseInt(d))
                }, fn: function (stats) {
                    return stats.totalCount
                }
            },
            'Failed Events': {
                axisLabel: 'Count',valueFormatFn:function(d){
                    return d3.format(',')(parseInt(d))
                }, fn: function (stats) {
                    return stats.failedCount
                }
            }

        };

        /**
         * Gets Chart Data using a function from the (processorStatsFunctionMap) above
         * @param functionName a name of the function
         * @return {{chartFunction: *, total: number, totalFormatted: number, data: Array}}
         */
        function getChartData(rawData, functionName,callbackFn){
            var processorStats = rawData.stats;
            var values = [];
            var total = 0;
            var totalFormatted = 0;
            var chartFunctionData = processorStatsFunctionMap[functionName];
            if(chartFunctionData == null || chartFunctionData == undefined){
                functionName = DEFAULT_CHART_FUNCTION;
                chartFunctionData = processorStatsFunctionMap[functionName];
            }

            _.each(processorStats, function (p) {
                var key = p.processorName;
                if (key == undefined || key == null) {
                    key = 'N/A';
                }

                var v = chartFunctionData.fn(p);
                values.push({label: key, value: v});
                total +=v;
                if(callbackFn != undefined){
                    callbackFn(key,p);
                }
            });
            totalFormatted = total;
            if (chartFunctionData && chartFunctionData.valueFormatFn != undefined && angular.isFunction(chartFunctionData.valueFormatFn)) {
                totalFormatted = chartFunctionData.valueFormatFn(total);
            }
            return {
                chartFunction:functionName,
                total:total,
                totalFormatted:totalFormatted,
                data:values
            }
        }

        function bytesToString(bytes) {

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


        var keepLastSummary = 20;

        var data = {
            feedName: '',
            loadingProcessorStatistics: false,
            processorStatistics: {
                topN: 3,
                lastRefreshTime: '',
                raw: {},
                chartData: {
                    chartFunction: '',
                    total: 0,
                    totalFormatted: '',
                    data: []
                },
                selectedChartFunction: '',
                topNProcessorDurationData: []
            },
            summaryStatistics: {
                lastRefreshTime: '',
                time: {startTime: 0, endTime: 0},
                flowsStartedPerSecond: 0,
                flowsStarted: 0,
                flowsFinished: 0,
                flowDuration: 0,
                flowsFailed: 0,
                totalEvents: 0,
                failedEvents: 0,
                flowsSuccess: 0,
                flowsRunning: 0,
                avgFlowDuration: 0
            },
            loadingFeedTimeSeriesData: false,
            feedTimeSeries: {
                lastRefreshTime: '',
                time: {startTime: 0, endTime: 0},
                minTime: 0,
                maxTime: 0,
                raw: [],
                chartData: []
            },
            feedProcessorErrors: {
                time: {startTime: 0, endTime: 0},
                autoRefresh:true,
                autoRefreshMessage:"enabled",
                allData: [],
                visibleData: [],
                newErrorCount:0,
                viewAllData:function(){
                    var self = this;
                    var index = this.visibleData.length;
                    if(this.allData.length > index) {
                        var rest = _.rest(this.allData,index);
                        _.each(rest, function (item) {
                            self.visibleData.push(item);
                        });
                        this.newErrorCount = 0;
                    }
                }
            },
            lastSummaryStats: [],
            setTimeBoundaries: function(from, to) {
                this.fromMillis = from;
                this.toMillis = to;
            },
            setFeedName: function (feedName) {
                this.feedName = feedName;
            },
            setSelectedChartFunction: function (selectedChartFunction) {
                this.processorStatistics.selectedChartFunction = selectedChartFunction
            },
            formatBytesToString: bytesToString,
            processorStatsFunctionMap: processorStatsFunctionMap,

            processorStatsFunctions: function () {
                return Object.keys(processorStatsFunctionMap);
            },
            averageDurationFunction: processorStatsFunctionMap[DEFAULT_CHART_FUNCTION],
            isLoading: function () {
                return this.loadingProcessorStatistics || this.loadingFeedTimeSeriesData;
            },
            fetchProcessorStatistics: function (minTime, maxTime) {
                var self = this;
                this.loadingProcessorStatistics = true;
                var deferred = $q.defer();

                var selectedChartFunction = self.processorStatistics.selectedChartFunction;

                if(angular.isUndefined(minTime)){
                    minTime = self.fromMillis;
                }
                if(angular.isUndefined(maxTime)){
                    maxTime = self.toMillis;
                }
                $q.when(ProvenanceEventStatsService.getFeedProcessorDuration(self.feedName, minTime,maxTime)).then(function (response) {
                    var processorStatsContainer = response.data;

                    var processorStats = processorStatsContainer.stats;

                    //  topNProcessorDuration(processorStats);
                    var flowsStartedPerSecond = 0;
                    var flowsStarted = 0;
                    var flowsFinished = 0;
                    var flowDuration = 0;
                    var flowsFailed = 0;
                    var totalEvents = 0;
                    var failedEvents = 0;
                    var flowsSuccess = 0;
                    var chartData = [];
                    var processorDuration = [];

                    var chartDataCallbackFunction = function (key, p) {
                        flowsStarted += p.jobsStarted;
                        flowsFinished += p.jobsFinished;
                        flowDuration += p.jobDuration;
                        flowsFailed += p.jobsFailed;
                        totalEvents += p.totalCount;
                        failedEvents += p.failedCount;
                        flowsSuccess = (flowsFinished - flowsFailed);
                        var duration = processorStatsFunctionMap['Average Duration'].fn(p);
                        processorDuration.push({label: key, value: duration});
                    }

                    chartData = getChartData(processorStatsContainer, selectedChartFunction, chartDataCallbackFunction);

                    var topNProcessorDurationData = _.sortBy(processorDuration, 'value').reverse();
                    topNProcessorDurationData = _.first(topNProcessorDurationData, self.processorStatistics.topN);

                    var timeDiffMillis = processorStatsContainer.endTime - processorStatsContainer.startTime;
                    var secondsDiff = timeDiffMillis / 1000;
                    flowsStartedPerSecond = secondsDiff > 0 ? ((flowsStarted / secondsDiff)).toFixed(2) : 0;

                    self.processorStatistics.raw = processorStatsContainer;
                    var summary = self.summaryStatistics;

                    summary.time.startTime = processorStatsContainer.startTime;
                    summary.time.endTime = processorStatsContainer.endTime;
                    summary.flowsStarted = flowsStarted;
                    summary.flowsFinished = flowsFinished;
                    summary.flowsFailed = flowsFailed;
                    summary.totalEvents = totalEvents;
                    summary.failedEvents = failedEvents;
                    summary.flowsSuccess = flowsSuccess;
                    summary.flowsStartedPerSecond = flowsStartedPerSecond != 0 ? parseFloat(flowsStartedPerSecond) : flowsStartedPerSecond;
                    summary.avgFlowDurationMilis = parseInt(flowsFinished > 0 ? (flowDuration / flowsFinished) : 0);
                    summary.avgFlowDuration = flowsFinished > 0 ? ((flowDuration / flowsFinished) / 1000).toFixed(2) : 0;

                    summary.avgFlowDuration = summary.avgFlowDuration != 0 ? parseFloat(summary.avgFlowDuration) : 0;

                    self.processorStatistics.chartData = chartData;

                    self.processorStatistics.topNProcessorDurationData = topNProcessorDurationData

                    var refreshTime = new Date().getTime();
                    self.processorStatistics.lastRefreshTime = refreshTime;
                    summary.lastRefreshTime = refreshTime;

                    var lastSummaryStats = angular.copy(self.summaryStatistics);
                    if (self.lastSummaryStats.length >= keepLastSummary) {
                        self.lastSummaryStats.shift();
                    }

                    self.lastSummaryStats.push({date: refreshTime, data: lastSummaryStats});

                    self.loadingProcessorStatistics = false;
                    deferred.resolve(self.processorStatistics);

                }, function (err) {
                    self.loadingProcessorStatistics = false;
                    var refreshTime = new Date().getTime();
                    self.processorStatistics.lastRefreshTime = refreshTime;
                    self.summaryStatistics.lastRefreshTime = refreshTime;
                    deferred.reject(err);
                });
                return deferred.promise;
            },
            changeProcessorChartDataFunction: function (selectedChartFunction) {
                var chartData = getChartData(this.processorStatistics.raw, selectedChartFunction);
                this.processorStatistics.chartData = chartData;
                return chartData;
            },
            updateBarChartHeight: function (chartOptions, chartApi, rows, chartFunction) {
                var configMap = processorStatsFunctionMap[chartFunction];

                chartOptions.chart.yAxis.axisLabel = configMap.axisLabel
                var chartHeight = 35 * rows;
                var prevHeight = chartOptions.chart.height;
                chartOptions.chart.height = chartHeight < 200 ? 200 : chartHeight;
                var heightChanged = prevHeight != chartOptions.chart.height;
                if (configMap && configMap.valueFormatFn != undefined) {
                    chartOptions.chart.valueFormat = configMap.valueFormatFn;
                    chartOptions.chart.yAxis.tickFormat = configMap.valueFormatFn;
                }
                else {
                    chartOptions.chart.valueFormat = function (d) {
                        return d3.format(',.2f')(d);
                    };

                    chartOptions.chart.yAxis.tickFormat = function (d) {
                        return d3.format(',.2f')(d);
                    }
                }
                if (chartApi && chartApi.update) {
                    chartApi.update();
                    if(heightChanged) {
                        chartApi.refresh();
                    }
                }
            },
            buildProcessorDurationChartData: function () {
                var chartData = this.processorStatistics.chartData;
                var values = chartData.data;
                var data = [{key: "Processor", "color": "#F08C38", values: values}];
                return data;

            },
            buildStatusPieChart: function () {
                var self = this;
                var statusPieChartData = [];
                statusPieChartData.push({key: "Successful Flows", value: self.summaryStatistics.flowsSuccess})
                statusPieChartData.push({key: "Running Flows", value: self.summaryStatistics.flowsRunning});
                return statusPieChartData;
            },

            buildEventsPieChart: function () {
                var self = this;
                var eventsPieChartData = [];
                var success = self.summaryStatistics.totalEvents - self.summaryStatistics.failedEvents;
                eventsPieChartData.push({key: "Success", value: success})
                eventsPieChartData.push({key: "Failed", value: self.summaryStatistics.failedEvents})
                return eventsPieChartData;
            },
            emptyFeedTimeSeriesObject: function (eventTime, feedName) {
                return {
                    "duration": 0,
                    "minEventTime": eventTime,
                    "maxEventTime": eventTime,
                    "bytesIn": 0,
                    "bytesOut": 0,
                    "totalCount": 0,
                    "failedCount": 0,
                    "jobsStarted": 0,
                    "jobsFinished": 0,
                    "jobsFailed": 0,
                    "jobDuration": 0,
                    "successfulJobDuration": 0,
                    "processorsFailed": 0,
                    "flowFilesStarted": 0,
                    "flowFilesFinished": 0,
                    "maxEventId": 0,
                    "id": null,
                    "feedName": feedName,
                    "processorId": null,
                    "processorName": null,
                    "feedProcessGroupId": null,
                    "collectionTime": null,
                    "collectionId": null,
                    "resultSetCount": null,
                    "jobsStartedPerSecond": 0,
                    "jobsFinishedPerSecond": 0,
                    "collectionIntervalSeconds": null
                }
            },

            fetchFeedTimeSeriesData: function () {
                var deferred = $q.defer();
                var self = this;
                this.loadingFeedTimeSeriesData = true;
                $q.when(ProvenanceEventStatsService.getFeedStatisticsOverTime(self.feedName, self.fromMillis, self.toMillis)).then(function (response) {

                    var statsContainer = response.data;
                    if (statsContainer.stats == null) {
                        statsContainer.stats = [];
                    }

                    var timeArr = _.map(statsContainer.stats, function (item) {
                        return item.minEventTime != null ? item.minEventTime : item.maxEventTime;
                    });


                    self.summaryStatistics.flowsRunning = statsContainer.runningFlows;

                    self.feedTimeSeries.minTime = ArrayUtils.min(timeArr);
                    self.feedTimeSeries.maxTime = ArrayUtils.max(timeArr);

                    self.feedTimeSeries.time.startTime = statsContainer.startTime;
                    self.feedTimeSeries.time.endTime = statsContainer.endTime;
                    self.feedTimeSeries.raw = statsContainer;

                    self.loadingFeedTimeSeriesData = false;
                    self.feedTimeSeries.lastRefreshTime = new Date().getTime();
                    deferred.resolve(self.feedTimeSeries);
                }, function (err) {
                    self.loadingFeedTimeSeriesData = false;
                    self.feedTimeSeries.lastRefreshTime = new Date().getTime();
                    deferred.reject(err)
                });
                return deferred.promise;
            },
            fetchFeedProcessorErrors: function (resetWindow) {
                var deferred = $q.defer();
                var self = this;
                //reset the collection if we are looking for a new window
                if(resetWindow){
                    self.feedProcessorErrors.allData = [];
                    self.feedProcessorErrors.visibleData = [];
                    self.feedProcessorErrors.time.startTime = null;
                    self.feedProcessorErrors.newErrorCount = 0;

                }
                $q.when(ProvenanceEventStatsService.getFeedProcessorErrors(self.feedName, self.fromMillis, self.toMillis, self.feedProcessorErrors.time.endTime)).then(function (response) {


                    var container = response.data;
                    var addToVisible = self.feedProcessorErrors.autoRefresh || self.feedProcessorErrors.visibleData.length ==0;
                    if (container != null && container.errors != null) {
                        _.each(container.errors, function (error) {
                            //append to errors list
                            self.feedProcessorErrors.allData.push(error);
                            if(addToVisible) {
                                self.feedProcessorErrors.visibleData.push(error);
                            }
                        });
                    }
                    self.feedProcessorErrors.newErrorCount = self.feedProcessorErrors.allData.length - self.feedProcessorErrors.visibleData.length;
                    self.feedProcessorErrors.time.startTime = container.startTime;
                    self.feedProcessorErrors.time.endTime = container.endTime;
                    deferred.resolve(container);
                }, function (err) {

                    deferred.reject(err)
                });
                return deferred.promise;

            }
        }
            

        
        
        return data;
        
        
        

    }]);
});
