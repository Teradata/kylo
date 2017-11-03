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
            templateUrl: 'js/ops-mgr/feeds/feed-stats/feed-stats-charts.html',
            controller: "FeedStatsChartsController",
            link: function ($scope, element, attrs) {
                $scope.$on('$destroy', function () {

                });
            } //DOM manipulation\}
        }

    };

    var controller = function ($scope, $element, $http, $interval, $timeout, $q, ProvenanceEventStatsService, FeedStatsService, Nvd3ChartService, OpsManagerFeedService,StateService) {
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

        self.maxDataPointsOptions = [200, 400, 800, 1600, 3200, 6400];
        self.maxDataPoints = self.maxDataPointsOptions[1];
        self.timeFrame = 'FIVE_MIN';
        self.timeframeOptions = [];
        self.timeFrameOption = self.timeFrame;
        self.timeFrameOptionIndex = 0;
        self.timeFrameOptionIndexLength = 0;
        self.lastRefreshTime = null;
        self.timeFramOptionsLookupMap = {};
        self.selectedTimeFrameOptionObject = {};

        /**
         * Summary stats
         * @type {*}
         */
        self.summaryStatistics = FeedStatsService.summaryStatistics;


        var feedChartLegendState = [];
        this.feedChartData = [];
        this.feedChartApi = {};
        this.feedChartOptions ={};

        self.processorChartApi = {};
        self.processorChartData = [];
        self.processorChartOptions = {};
        self.selectedProcessorStatisticFunction = 'Average Duration';
        self.processorStatsFunctions = FeedStatsService.processorStatsFunctions();

        self.feed = {
            displayStatus:''
        };




        /**
         * Latest summary stats
         * @type {{}}
         */
        self.summaryStatsData = {};

        self.eventSuccessKpi = {
            value:0,
            icon:'',
            color:''
        };

        self.flowRateKpi = {
            value:0,
            icon: 'tune',
            color: '#1f77b4'
        };

        self.avgDurationKpi = {
            value:0,
            icon: 'access_time',
            color: '#1f77b4'
        };

        self.feedProcessorErrorsTable = {
            sortOrder:'-errorMessageTimestamp',
            filter:'',
            rowLimit:5,
            page:1
        };

        self.feedProcessorErrors = FeedStatsService.feedProcessorErrors;

        self.onTimeFrameOptionChange = function() {
            onTimeFrameClick(self.timeFrame);
        };

        self.onTimeFrameClick = onTimeFrameClick;

        self.onProcessorChartFunctionChanged = onProcessorChartFunctionChanged;


        self.gotoFeedDetails = function(ev){
            if(self.feed.feedId != undefined) {
                StateService.FeedManager().Feed().navigateToFeedDetails(self.feed.feedId);
            }
        };

        self.viewNewFeedProcessorErrors = function() {
            self.feedProcessorErrors.viewAllData();
        };

        self.toggleFeedProcessorErrorsRefresh = function(autoRefresh){
            if(autoRefresh){
                self.feedProcessorErrors.viewAllData();
                self.feedProcessorErrors.autoRefreshMessage ='enabled';
            }
            else {
                self.feedProcessorErrors.autoRefreshMessage ='disabled';
            }
        };


        function init(){
            FeedStatsService.setMaxDataPoints(self.maxDataPoints);
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




             self.feedChartOptions = {
                 chart: {
                     type: 'lineChart',
                     height: 450,
                     margin: {
                         top: 10,
                         right: 20,
                         bottom: 110,
                         left: 65
                     },
                     x: function (d) {
                         return d[0];
                     },
                     y: function (d) {
                         return d3.format('.2f')(d[1]);
                     },
                     showTotalInTooltip:true,
                     interpolate:'linear',
                     useVoronoi: false,
                     // clipEdge: false,
                     duration: 250,
                     useInteractiveGuideline: true,
                     interactiveLayer: {tooltip: {gravity: 's'}},
                     valueFormat: function (d) {
                         return d3.format(',')(parseInt(d))
                     },
                     xAxis: {
                         axisLabel: '',
                         showMaxMin: false,
                         tickFormat: function (d) {
                             return d3.time.format.utc("%Y-%m-%d %H:%M:%S")(new Date(d))
                         },
                         rotateLabels: -45
                     },
                     yAxis: {
                         axisLabel: 'Flows Per Second',
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
                 }

             };
         }




        function isLoading() {
            return FeedStatsService.isLoading();
        }

        function loadTimeFrameOption() {
            ProvenanceEventStatsService.getTimeFrameOptions().then(function (response) {
                self.timeFrameOptions = response.data;
                self.timeFrameOptionIndexLength = self.timeFrameOptions.length;
                $timeout(function() {
                    //update initial slider position in UI
                    self.timeFrameOptionIndex = _.findIndex(self.timeFrameOptions, function(option) {
                        return option.value === self.timeFrame;
                    });
                }, 1000);
                _.each(response.data, function (labelValue) {
                    self.timeFramOptionsLookupMap[labelValue.value] = labelValue;
                });
            });
        }
        $scope.$watch(
            //update time frame when slider is moved
            function() {
                return self.timeFrameOptionIndex;
            },
            function() {
                if (!_.isUndefined(self.timeFrameOptions)) {
                    self.timeFrame = self.timeFrameOptions[Math.floor(self.timeFrameOptionIndex)].value;
                    self.onTimeFrameOptionChange();
                }
            }
        );


        function onTimeFrameClick(timeFrame){
            self.timeFrame = timeFrame;
            clearRefreshInterval();
            //abort
            FeedStatsService.setTimeFrame(self.timeFrame);
            FeedStatsService.setMaxDataPoints(self.maxDataPoints);
            buildChartData(true);
            setRefreshInterval();
        }



        function onProcessorChartFunctionChanged() {
             FeedStatsService.setSelectedChartFunction(self.selectedProcessorStatisticFunction);
            var chartData=    FeedStatsService.changeProcessorChartDataFunction(self.selectedProcessorStatisticFunction);
            self.processorChartData[0].values = chartData.data;
            FeedStatsService.updateBarChartHeight(self.processorChartOptions, self.processorChartApi,chartData.data.length,self.selectedProcessorStatisticFunction);
        }

        function buildChartData(timeIntervalChange) {
            if (!isLoading()) {
                timeIntervalChange = angular.isUndefined(timeIntervalChange) ? false : timeIntervalChange;
                self.feedTimeChartLoading = true;
                self.processChartLoading = true;
                buildProcessorChartData();
                buildFeedCharts();
                fetchFeedProcessorErrors(timeIntervalChange);
            }
            getFeedHealth();
        }



        function updateSuccessEventsPercentKpi(){
            if(self.summaryStatsData.totalEvents == 0){
                self.eventSuccessKpi.icon = 'remove';
                self.eventSuccessKpi.color= "#1f77b4"
                self.eventSuccessKpi.value = "--";
            }
            else {
                var failed = self.summaryStatsData.totalEvents > 0 ? ((self.summaryStatsData.failedEvents / self.summaryStatsData.totalEvents)).toFixed(2) * 100 : 0;
                var value = (100 - failed).toFixed(0);
                var icon = 'offline_pin';
                var iconColor = "#3483BA"

                self.eventSuccessKpi.icon = icon;
                self.eventSuccessKpi.color = iconColor;
                self.eventSuccessKpi.value = value



            }
        }

        function updateFlowRateKpi(){
            self.flowRateKpi.value = self.summaryStatistics.flowsStartedPerSecond;
        }

        function updateAvgDurationKpi(){
            var avgMillis = self.summaryStatistics.avgFlowDurationMilis;
            self.avgDurationKpi.value = DateTimeUtils.formatMillisAsText(avgMillis,false,true);
        }

        function formatSecondsToMinutesAndSeconds(s){   // accepts seconds as Number or String. Returns m:ss
            return( s - ( s %= 60 )) / 60 + (9 < s ? ':' : ':0' ) + s ;
        }

        function updateSummaryKpis(){
            updateFlowRateKpi();
            updateSuccessEventsPercentKpi();
            updateAvgDurationKpi();
        }


        function buildProcessorChartData() {
            var values = [];
            self.processChartLoading = true;
            $q.when(FeedStatsService.fetchProcessorStatistics()).then(function (response) {
                self.summaryStatsData = FeedStatsService.summaryStatistics;
               updateSummaryKpis();
                self.processorChartData =  FeedStatsService.buildProcessorDurationChartData();


                FeedStatsService.updateBarChartHeight(self.processorChartOptions, self.processorChartApi,self.processorChartData[0].values.length,self.selectedProcessorStatisticFunction);
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

                self.minTime = feedTimeSeries.time.startTime;
                self.maxTime = feedTimeSeries.time.endTime;

                var chartArr = [];
                chartArr.push({label: 'Completed', color: '#3483BA', valueFn: function(item) {
                    if (item.timeInterval > item.collectionIntervalSeconds) {
                        return item.jobsFinishedPerSecond / item.timeInterval * item.collectionIntervalSeconds;
                    } else {
                        return item.jobsFinishedPerSecond;
                    }
                }});
                chartArr.push({label: 'Started', area:true, color: "#F08C38", valueFn: function (item) {
                    if (item.timeInterval > item.collectionIntervalSeconds) {
                        return item.jobsStartedPerSecond / item.timeInterval * item.collectionIntervalSeconds;
                    } else {
                        return item.jobsStartedPerSecond;
                    }
                }});
                //preserve the legend selections
                if (feedChartLegendState.length > 0) {
                    _.each(chartArr, function (item, i) {
                        item.disabled = feedChartLegendState[i];
                    });
                }

                self.feedChartData = Nvd3ChartService.toLineChartData(feedTimeSeries.raw.stats, chartArr, 'maxEventTime', null, self.minTime, self.maxTime, self.maxDataPoints);
                var max = 0;
                if(self.feedChartData && self.feedChartData[0]) {
                  max = d3.max(self.feedChartData[0].values, function (d) {
                        return d[1];
                    });
                }
                if(max == undefined || max ==0) {
                    max = 5;
                }
                else {
                    max *=1.2;
                }
                max = Math.round(max);
                self.feedChartOptions.chart.forceY = [0,max];
                if(self.feedChartOptions.chart.yAxis.ticks != max) {
                    self.feedChartOptions.chart.yDomain = [0, max];
                    var ticks = max;
                    if(ticks > 8){
                        ticks = 8;
                    }
                    self.feedChartOptions.chart.yAxis.ticks = ticks;
                }
                if(self.feedChartApi && self.feedChartApi.update) {
                    self.feedChartApi.update();
                }

                self.feedTimeChartLoading = false;
                self.lastFeedTimeChartRefresh = new Date().getTime();
            }, function () {
                self.feedTimeChartLoading = false;
                self.lastFeedTimeChartRefresh = new Date().getTime();
            });

        }

        /**
         * fetch and append the errors to the FeedStatsService.feedProcessorErrors.data object
         * @param resetWindow optionally reset the feed errors to start a new array of errors in the feedProcessorErrors.data
         */
        function fetchFeedProcessorErrors(resetWindow) {
            self.feedProcessorErrorsLoading = true;
            $q.when(FeedStatsService.fetchFeedProcessorErrors(resetWindow)).then(function (feedProcessorErrors) {
                self.feedProcessorErrorsLoading = false;
            },function(err) {
                self.feedProcessorErrorsLoading = false;
            });


        }


         function getFeedHealth(){
                var successFn = function (response) {
                    if (response.data) {
                        //transform the data for UI
                        if(response.data.feedSummary){
                            angular.extend(self.feed,response.data.feedSummary[0]);
                            self.feed.feedId = self.feed.feedHealth.feedId;
                            if (self.feed.running) {
                                self.feed.displayStatus = 'RUNNING';
                            }
                            else {
                                self.feed.displayStatus = 'STOPPED';
                            }
                        }

                    }
                }
                var errorFn = function (err) {
                }


                $http.get(OpsManagerFeedService.SPECIFIC_FEED_HEALTH_URL(self.feedName)).then( successFn, errorFn);
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
            // anything below 5 minute interval to be refreshed every 5 seconds,
            // anything above 5 minutes to be refreshed in proportion to its time span, i.e. the longer the time span the less it is refreshed
            var option = self.timeFramOptionsLookupMap[self.timeFrame];
            if (!_.isUndefined(option)) {
                //timeframe option will be undefined when page loads for the first time
                var refreshInterval = option.properties.millis / 60;
                self.refreshIntervalTime = refreshInterval < 5000 ? 5000 : refreshInterval;
            }
            if (self.refreshIntervalTime) {
                self.refreshInterval = $interval(function() {
                    buildChartData()}, self.refreshIntervalTime
                );

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

    angular.module(moduleName).controller('FeedStatsChartsController', ["$scope","$element","$http","$interval","$timeout","$q","ProvenanceEventStatsService","FeedStatsService","Nvd3ChartService","OpsManagerFeedService","StateService",controller]);

    angular.module(moduleName)
        .directive('kyloFeedStatsCharts', directive);

});
