define(['angular','ops-mgr/feeds/feed-stats/module-name'], function (angular,moduleName) {

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
            templateUrl: 'js/ops-mgr/feeds/feed-stats/feed-stats-grid-list.html',
            controller: "FeedStatsGridListController",
            link: function ($scope, element, attrs) {
                $scope.$on('$destroy', function () {

                });
            } //DOM manipulation\}
        }

    };

    var controller = function ($scope, $element, $http, $interval, $timeout, $q, ProvenanceEventStatsService, FeedStatsService, Nvd3ChartService) {
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



        this.statusPieChartApi = {};

        self.timeframeOptions = [];
        self.timeFrame = 'FIVE_MIN';
        self.lastRefreshTime = null;
        self.timeFramOptionsLookupMap = {};
        self.selectedTimeFrameOptionObject = {};

        /**
         * Summary stats
         * @type {*}
         */
        self.summaryStatistics = FeedStatsService.summaryStatistics;



        var feedChartLegendState = []
        this.feedChartData = [];
        this.feedChartApi = {};
        this.feedChartOptions ={};

        self.processorChartApi = {};
        self.processorChartData = [];
        self.processorChartOptions = {};
        self.selectedProcessorStatisticFunction = 'Average Duration';
        self.processorStatsFunctions = FeedStatsService.processorStatsFunctions();

        self.topN =FeedStatsService.processorStatistics.topN;
        self.topNProcessorChartApi = {};
        self.topNProcessorChartData = [];
        self.topNProcessorChartOptions = {};

        self.statusPieChartApi = {};
        self.statusPieChartOptions = {};
        self.statusPieChartData = [];


        self.eventsPieChartApi = {};
        self.eventsPieChartOptions = {};
        self.eventsPieChartData = [];


        var summaryStatisticsMaxDataPoints = 20;
        self.summaryStatisticsChartData = [];
        self.summaryStatisticsChartOptions = {};
        self.summaryStatisticsChartApi = {};

        /**
         * Latest summary stats
         * @type {{}}
         */
        self.summaryStatsData = {};
        self.failedEventsPercent = 0;


        self.onTimeFrameChange = onTimeFrameChange;

        self.onProcessorChartFunctionChanged = onProcessorChartFunctionChanged;



        function init(){
            FeedStatsService.setTimeFrame(self.timeFrame);
            FeedStatsService.setFeedName(self.feedName);
            setupChartOptions();
            loadTimeFrameOption();
            buildChartData();
            setRefreshInterval();
            self.dataLoaded = true;
        }

        init();

         function setupChartOptions(){
             self.processorChartOptions = {
                 chart: {
                     type: 'multiBarHorizontalChart',
                     height: 400,
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
                         axisLabel: FeedStatsService.processorStatsFunctionMap[self.selectedProcessorStatisticFunction].axisLabel,
                         tickFormat: function (d) {
                             return d3.format(',.2f')(d);
                         }
                     },
                     valueFormat: function (d) {
                         return d3.format(',.2f')(d);
                     }
                 }
             };

             self.topNProcessorChartOptions = {
                 chart: {
                     type: 'multiBarHorizontalChart',
                     height: 200,
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
                         axisLabel: FeedStatsService.averageDurationFunction.axisLabel,
                         tickFormat: function (d) {
                             return d3.format(',.2f')(d);
                         }
                     },
                     valueFormat: function (d) {
                         return d3.format(',.2f')(d);
                     }
                 }
             };



             self.statusPieChartOptions = {
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



             self.eventsPieChartOptions = {
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
                     "height": 100,
                     labelThreshold: 0.01,
                     labelSunbeamLayout: false,
                     interactiveLayer: {tooltip: {gravity: 's'}},
                     "margin": {"top": 10, "right": 10, "bottom": 10, "left": 10},
                     donut: false,
                     // donutRatio: 0.65,
                     showLegend: false,
                     valueFormat: function (d) {
                         return d3.format(',')(parseInt(d))
                     },
                     color: function (d) {
                         if (d.key == 'Success') {
                             return '#009933';
                         }
                         else if (d.key == 'Failed') {
                             return '#FF0000';
                         }
                     },
                     dispatch: {
                         renderEnd: function () {

                         }
                     }
                 }
             };


             self.summaryStatisticsChartOptions = {

                 chart: {
                     type: 'lineChart',
                     margin : {
                         top: 5,
                         right: 5,
                         bottom:10,
                         left: 20
                     },
                     x: function(d){return d[0];},
                     y: function(d){return d[1];},
                     useVoronoi: false,
                     clipEdge: false,
                     duration: 0,
                     height:200,
                     useInteractiveGuideline: true,
                     xAxis: {
                         axisLabel: 'Time',
                         showMaxMin: false,
                         tickFormat: function(d) {
                             return d3.time.format('%X')(new Date(d))
                         },
                         rotateLabels: -45
                     },
                     yAxis: {
                         axisLabel:'Running',
                         "axisLabelDistance": -10,
                         showMaxMin:false,
                         tickSubdivide:0,
                         ticks:1
                     },
                     yDomain:[0,1],
                     showLegend:false,
                     showXAxis:false,
                     showYAxis:true,
                     dispatch: {

                     }
                 }

             }


             self.feedChartOptions = {
                 chart: {
                     type: 'stackedAreaChart',
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
                     showControls:true,
                     showTotalInTooltip:true,
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
                     text: 'Flows over time'
                 }

             };
         }




        function isLoading() {
            return FeedStatsService.isLoading();
        }

        function loadTimeFrameOption() {
            ProvenanceEventStatsService.getTimeFrameOptions().then(function (response) {
                self.timeFrameOptions = response.data;
                _.each(response.data, function (labelValue) {
                    self.timeFramOptionsLookupMap[labelValue.value] = labelValue;
                });
            })
        }

        function onTimeFrameChange() {
            clearRefreshInterval();
            //abort
            FeedStatsService.setTimeFrame(self.timeFrame);
            buildChartData();
            setRefreshInterval();

        }



        function onProcessorChartFunctionChanged() {
             FeedStatsService.setSelectedChartFunction(self.selectedProcessorStatisticFunction);
         var chartData=    FeedStatsService.changeProcessorChartDataFunction(self.selectedProcessorStatisticFunction);
         self.processorChartData[0].values = chartData.data;
            FeedStatsService.updateBarChartHeight(self.processorChartOptions, self.processorChartApi,chartData.data.length,self.selectedProcessorStatisticFunction);
        }

        function buildChartData() {
            if (!isLoading()) {
                self.feedTimeChartLoading = true;
                self.processChartLoading = true;
                buildProcessorChartData();
                buildFeedCharts();
            }
        }


        function buildProcessorChartData() {
            var values = [];
            self.processChartLoading = true;
            $q.when(FeedStatsService.fetchProcessorStatistics()).then(function (response) {
                self.summaryStatsData = FeedStatsService.summaryStatistics;
                self.failedEventsPercent  =  self.summaryStatsData.totalEvents > 0 ? ((self.summaryStatsData.failedEvents / self.summaryStatsData.totalEvents)).toFixed(2) : 0;
                self.processorChartData =  FeedStatsService.buildProcessorDurationChartData();
                self.statusPieChartData = FeedStatsService.buildStatusPieChart();
                self.eventsPieChartData = FeedStatsService.buildEventsPieChart();
                var values = FeedStatsService.processorStatistics.topNProcessorDurationData;
                var topNProcessorDurationChart = [{key: "Processor", "color": "#1f77b4", values: values}];
                self.topNProcessorChartData = topNProcessorDurationChart;

               var lastData = buildLastStatsChartData('Average Duration',"#1f77b4",'avgFlowDuration')
               self.summaryStatisticsChartData = lastData;

                FeedStatsService.updateBarChartHeight(self.processorChartOptions, self.processorChartApi,values.length,self.selectedProcessorStatisticFunction);
                FeedStatsService.updateBarChartHeight(self.topNProcessorChartOptions,self.topNProcessorChartApi,values.length,'Average Duration');
                self.processChartLoading = false;
                self.lastProcessorChartRefresh = new Date().getTime();
                self.lastRefreshTime = new Date();
            },function() {
                self.processChartLoading = false;
                self.lastProcessorChartRefresh = new Date().getTime();
            });
        }

        function buildLastStatsChartData(label,color,key) {
            var lastStatData = FeedStatsService.lastSummaryStats;
            var lastStatChartData = [];

            _.each(lastStatData,function(item) {
                lastStatChartData.push([item.date,item.data[key]]);
            })
            var lastData = [{key: label, "color": color, area: true, values: lastStatChartData}];
            return lastData;
        }

        function buildFeedCharts() {

            self.feedTimeChartLoading = true;
            $q.when(FeedStatsService.fetchFeedTimeSeriesData()).then(function (feedTimeSeries) {


                self.minTime = feedTimeSeries.minTime;
                self.maxTime = feedTimeSeries.maxTime;

                var chartArr = [];
                chartArr.push({label: 'Completed', value: 'jobsFinished', color: '#009933'});
                chartArr.push({label: 'Started', value: 'jobsStarted', color: '#FF9901'});
                //preserve the legend selections
                if (feedChartLegendState.length > 0) {
                    _.each(chartArr, function (item, i) {
                        item.disabled = feedChartLegendState[i];
                    });
                }

                self.feedChartData = Nvd3ChartService.toLineChartData(feedTimeSeries.raw.stats, chartArr, 'maxEventTime');


                self.feedTimeChartLoading = false;
                self.lastFeedTimeChartRefresh = new Date().getTime();
            }, function () {
                self.feedTimeChartLoading = false;
                self.lastFeedTimeChartRefresh = new Date().getTime();
            });

        }




        function addSummaryStatisticsChartData(data){

            if(self.summaryStatsticsChartData.length >0) {
                self.summaryStatsticsChartData[0].values.push([data.date, data.count]);
            }
            else {
                var initialChartData = ChartJobStatusService.toChartData([data]);
                initialChartData[0].key = 'Running';
                self.chartData = initialChartData;
            }
            var max = d3.max(self.runningCounts, function(d) {
                return d.count; } );
            if(max == undefined || max ==0) {
                max = 1;
            }
            else {
                max +=1;
            }
            if(self.chartOptions.chart.yAxis.ticks != max) {
                self.chartOptions.chart.yDomain = [0, max];
                var ticks = max;
                if(ticks > 8){
                    ticks = 8;
                }
                self.chartOptions.chart.yAxis.ticks = ticks;
            }
        }




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
            if (self.lastFeedTimeChartRefresh == null) {
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

    angular.module(moduleName).controller('FeedStatsGridListController', ["$scope","$element","$http","$interval","$timeout","$q","ProvenanceEventStatsService","FeedStatsService","Nvd3ChartService",controller]);

    angular.module(moduleName)
        .directive('kyloFeedStatsGridList', directive);

});
