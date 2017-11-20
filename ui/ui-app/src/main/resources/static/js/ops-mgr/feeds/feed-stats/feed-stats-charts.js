define(['angular', 'ops-mgr/feeds/feed-stats/module-name'], function (angular, moduleName) {

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

    var controller = function ($scope, $element, $http, $interval, $timeout, $q, $mdToast,ProvenanceEventStatsService, FeedStatsService, Nvd3ChartService, OpsManagerFeedService, StateService) {
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

        /**
         * Initial Time Frame setting
         * @type {string}
         */
        self.timeFrame = 'FIVE_MIN';
        /**
         * Array of fixed times
         * @type {Array}
         */
        self.timeframeOptions = [];
        /**
         * last time the page was refreshed
         * @type {null}
         */
        self.lastRefreshTime = null;
        /**
         * map of the the timeFrame value to actual timeframe object (i.e. FIVE_MIN:{timeFrameObject})
         * @type {{}}
         */
        self.timeFramOptionsLookupMap = {};
        /**
         * The selected Time frame
         * @type {{}}
         */
        self.selectedTimeFrameOptionObject = {};
        /**
         * Flag to enable disable auto refresh
         * @type {boolean}
         */
        self.autoRefresh = true;

        /**
         * Flag to indicate if we are zoomed or not
         * @type {boolean}
         */
        self.isZoomed = false;

        /**
         * Zoom helper
         * @type {boolean}
         */
        self.isAtInitialZoom = true;

        /**
         * Difference in overall min/max time for the chart
         * Used to help calcuate the correct xXais label (i.e. for larger time periods show Date + time, else show time
         * @type {null}
         */
        self.timeDiff = null;

        /**
         * millis to wait after a zoom is complete to update the charts
         * @type {number}
         */
        var ZOOM_DELAY = 700;

        /**
         * Constant set to indicate we are not zoomed
         * @type {number}
         */
        var UNZOOMED_VALUE = -1;

        /**
         * After a chart is rendered it will always call the zoom function.
         * Flag to prevent the initial zoom from triggering after refresh of the chart
         * @type {boolean}
         */
        self.preventZoomChange = false;

        /**
         * Timeout promise to prevent zoom
         * @type {undefined}
         */
        self.preventZoomPromise = undefined;

        /**
         * The Min Date of data.  This will be the zoomed value if we are zooming, otherwise the min value in the dataset
         */
        self.minDisplayTime;

        /**
         * The max date of the data.  this will be the zoomed value if zooming otherwise the max value in the dataset
         */
        self.maxDisplayTime;

        /**
         * max Y value (when not zoomed)
         * @type {number}
         */
        self.maxY = 0;

        /**
         * max Y Value when zoomed
         * @type {number}
         */
        self.zoomMaxY = 0;

        /**
         * Min time frame to enable zooming.
         * Defaults to 30 min.
         * Anything less than this will not be zoomable
         * @type {number}
         */
        self.minZoomTime = 1000*60*30;
        /**
         * Flag to indicate if zooming is enabled.
         * Zooming is only enabled for self.minZoomTime or above
         *
         * @type {boolean}
         */
        self.zoomEnabled = false;

        /**
         * A bug in nvd3 charts exists where if the zoom is toggle to true it requires a force of the x axis when its toggled back to false upon every data refresh.
         * this flag will be triggered when the zoom enabled changes and from then on it will manually reset the x domain when the data refreshes
         * @type {boolean}
         */
        self.forceXDomain = false;

        /**
         * Flag to force the rendering of the chart to refresh
         * @type {boolean}
         */
        self.forceChartRefresh = false;

        /**
         * Summary stats
         * @type {*}
         */
        self.summaryStatistics = FeedStatsService.summaryStatistics;

        var feedChartLegendState = [];
        this.feedChartData = [];
        this.feedChartApi = {};
        this.feedChartOptions = {};

        self.processorChartApi = {};
        self.processorChartData = [];
        self.processorChartOptions = {};
        self.selectedProcessorStatisticFunction = 'Average Duration';
        self.processorStatsFunctions = FeedStatsService.processorStatsFunctions();

        /**
         * The Feed we are looking at
         * @type {{displayStatus: string}}
         */
        self.feed = {
            displayStatus: ''
        };

        /**
         * Latest summary stats
         * @type {{}}
         */
        self.summaryStatsData = {};

        self.eventSuccessKpi = {
            value: 0,
            icon: '',
            color: ''
        };

        self.flowRateKpi = {
            value: 0,
            icon: 'tune',
            color: '#1f77b4'
        };

        self.avgDurationKpi = {
            value: 0,
            icon: 'access_time',
            color: '#1f77b4'
        };

        self.feedProcessorErrorsTable = {
            sortOrder: '-errorMessageTimestamp',
            filter: '',
            rowLimit: 5,
            page: 1
        };

        /**
         * Errors for th error table (if any)
         * @type {*}
         */
        self.feedProcessorErrors = FeedStatsService.feedProcessorErrors;


        //// USER INTERACTIONS, buttons


        /**
         * When a user clicks the Refresh Button
         */
        self.onRefreshButtonClick = function () {
            refresh();
        };


        /**
         * When a user changes the Processor drop down
         * @type {onProcessorChartFunctionChanged}
         */
        self.onProcessorChartFunctionChanged = onProcessorChartFunctionChanged;

        /**
         * Navigate to the Feed Manager Feed Details
         * @param ev
         */
        self.gotoFeedDetails = function (ev) {
            if (self.feed.feedId != undefined) {
                StateService.FeedManager().Feed().navigateToFeedDetails(self.feed.feedId);
            }
        };

        /**
         * Show detailed Errors
         */
        self.viewNewFeedProcessorErrors = function () {
            self.feedProcessorErrors.viewAllData();
        };

        self.toggleFeedProcessorErrorsRefresh = function (autoRefresh) {
            if (autoRefresh) {
                self.feedProcessorErrors.viewAllData();
                self.feedProcessorErrors.autoRefreshMessage = 'enabled';
            }
            else {
                self.feedProcessorErrors.autoRefreshMessage = 'disabled';
            }
        };

        /**
         * Called when a user click on the Reset Zoom button
         */
        self.onResetZoom = function() {
            if(self.isZoomed) {
                initiatePreventZoom();
                resetZoom();
                self.feedChartOptions.chart.xDomain = [self.minTime,self.maxTime]
                self.feedChartOptions.chart.yDomain = [self.minY,self.maxY]
                self.feedChartApi.refresh();
                buildProcessorChartData();
            }
        }

        /**
         * prevent the initial zoom to fire in chart after reload
         */
        function initiatePreventZoom(){
            var cancelled = false;
            if(angular.isDefined(self.preventZoomPromise)) {
               $timeout.cancel(self.preventZoomPromise);
                self.preventZoomPromise = undefined;
                cancelled =true;
            }
                    if(!self.preventZoomChange || cancelled) {
                        self.preventZoomChange = true;
                         self.preventZoomPromise =   $timeout(function () {
                         self.preventZoomChange = false;
                         self.preventZoomPromise = undefined;
                        }, 1000);
                    }
        }



        /**
         * Initialize the Charts
         */
        function setupChartOptions() {
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

            /**
             * Help adjust the x axis label depending on time window
             * @param d
             */
            function timeSeriesXAxisLabel(d){
                var maxTime = 1000*60*60*12; //12 hrs
                if(self.timeDiff >=maxTime ){
                    //show the date if it spans larger than maxTime
                    return d3.time.format('%Y-%m-%d %H:%M')(new Date(d))
                }
                else {
                    return d3.time.format('%X')(new Date(d))
                }
            }

            /**
             * Prevent zooming into a level of detail that the data doesnt allow
             * Stats > a day are aggregated up to the nearest hour
             * Stats > 10 hours are aggregated up to the nearest minute
             * If a user is looking at data within the 2 time frames above, prevent the zoom to a level greater than the hour/minute
             * @param xDomain
             * @param yDomain
             * @return {boolean}
             */
            function canZoom(xDomain, yDomain) {

                var diff = self.maxTime - self.minTime;

                var minX  = Math.floor(xDomain[0]);
                var maxX = Math.floor(xDomain[1]);
                var zoomDiff = maxX - minX;
                //everything above the day should be zoomed at the hour level
                //everything above 10 hrs should be zoomed at the minute level
                if(diff >= (1000*60*60*24)){
                    if(zoomDiff < (1000*60*60)){
                        return false   //prevent zooming!
                    }
                }
                else if(diff >= (1000*60*60*10)) {
                    // zoom at minute level
                    if(zoomDiff < (1000*60)){
                        return false;
                    }
                }
                return true;

            }

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
                    showTotalInTooltip: true,
                    interpolate: 'linear',
                    useVoronoi: false,
                    duration: 250,
                    clipEdge:false,
                    useInteractiveGuideline: true,
                    interactiveLayer: {tooltip: {gravity: 's'}},
                    valueFormat: function (d) {
                        return d3.format(',')(parseInt(d))
                    },
                    xAxis: {
                        axisLabel: '',
                        showMaxMin: false,
                        tickFormat: timeSeriesXAxisLabel,
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
                    //https://github.com/krispo/angular-nvd3/issues/548
                    zoom: {
                        enabled: false,
                        scale: 1,
                        scaleExtent: [1, 50],
                        verticalOff: true,
                        unzoomEventType: 'dblclick.zoom',
                        useFixedDomain:false,
                        zoomed: function(xDomain, yDomain) {
                            //zoomed will get called initially (even if not zoomed)
                            // because of this we need to check to ensure the 'preventZoomChange' flag was not triggered after initially refreshing the dataset
                            if(!self.preventZoomChange) {
                                self.isZoomed = true;
                                if(canZoom(xDomain,yDomain)) {
                                    self.zoomedMinTime = Math.floor(xDomain[0]);
                                    self.zoomedMaxTime = Math.floor(xDomain[1]);
                                    self.timeDiff = self.zoomedMaxTime - self.zoomedMinTime;
                                    var max1 = Math.ceil(yDomain[0]);
                                    var max2 = Math.ceil(yDomain[1]);
                                    self.zoomMaxY = max2 > max1 ? max2 : max1;

                                }
                                return {x1: self.zoomedMinTime, x2: self.zoomedMaxTime, y1: yDomain[0], y2: yDomain[1]};
                            }
                            else {
                                return {x1: self.minTime, x2: self.maxTime, y1: self.minY, y2: self.maxY}
                            }
                        },
                        unzoomed: function(xDomain, yDomain) {
                       return  resetZoom();
                        }
                        },
                    interactiveLayer: {
                        dispatch: {
                            elementClick: function (t, u) {}
                        }
                    },
                    dispatch: {

                    }
                }

            };
        }

        /**
         * Reset the Zoom and return the x,y values pertaining to the min/max of the complete dataset
         * @return {{x1: *, x2: (*|number|endTime|{name, fn}|Number), y1: number, y2: (number|*)}}
         */
        function resetZoom() {
            if(self.isZoomed) {
                self.isZoomed = false;
                self.zoomedMinTime = UNZOOMED_VALUE;
                self.zoomedMaxTime = UNZOOMED_VALUE;
                self.minDisplayTime=self.minTime;
                self.maxDisplayTime =self.maxTime;
                self.timeDiff = self.maxTime - self.minTime;
                return {x1: self.minTime, x2: self.maxTime, y1: self.minY, y2: self.maxY}
            }
        }


        var changeZoom = function() {
            self.timeDiff = self.zoomedMaxTime- self.zoomedMinTime;
            self.autoRefresh = false;
            self.isZoomed = true;
            self.isAtInitialZoom = true;

        //    FeedStatsService.setTimeBoundaries(self.minTime, self.maxTime);
            buildProcessorChartData();
            self.minDisplayTime=self.zoomedMinTime;
            self.maxDisplayTime =self.zoomedMaxTime

            /*
           if(self.zoomedMinTime != UNZOOMED_VALUE) {
                //reset x xaxis to the zoom values
                self.feedChartOptions.chart.xDomain = [self.zoomedMinTime,self.zoomedMaxTime]
                var y = self.zoomMaxY > 0 ? self.zoomMaxY : self.maxY;
                self.feedChartOptions.chart.yDomain = [0,self.maxY]
            }
            else  {
                self.feedChartOptions.chart.xDomain = [self.minTime,self.maxTime];
                self.feedChartOptions.chart.yDomain = [0,self.maxY]
            }
           self.feedChartApi.update();
*/





        };

        /**
         * Cancel the zoom timeout watcher
         */
        function cancelPreviousOnZoomed() {
            if (!_.isUndefined(self.changeZoomPromise)) {
                $timeout.cancel(self.changeZoomPromise);
                self.changeZoomPromise = undefined;
            }
        }


        self.onTimeFrameChanged = function(){
            if (!_.isUndefined(self.timeFrameOptions)) {
                self.timeFrame = self.timeFrameOptions[Math.floor(self.timeFrameOptionIndex)].value;
                self.displayLabel = self.timeFrame.label;
                self.isZoomed = false;
                self.zoomedMinTime = UNZOOMED_VALUE;
                self.zoomedMaxTime = UNZOOMED_VALUE;
                initiatePreventZoom();
                onTimeFrameChanged(self.timeFrame);

            }
        }

     /*   $scope.$watch(
            //update time frame when slider is moved
            function () {
                return self.timeFrameOptionIndex;
            },
            function () {
                if (!_.isUndefined(self.timeFrameOptions)) {
                    self.timeFrame = self.timeFrameOptions[Math.floor(self.timeFrameOptionIndex)].value;
                    self.displayLabel = self.timeFrame.label;
                    self.isZoomed = false;
                    self.zoomedMinTime = UNZOOMED_VALUE;
                    self.zoomedMaxTime = UNZOOMED_VALUE;
                    onTimeFrameChanged(self.timeFrame);
                }
            }
        );
        */

        /**
         * Enable/disable the refresh interval
         */
        $scope.$watch(
            function () {
                return self.autoRefresh;
            },
            function (newVal, oldVal) {
                if (!self.autoRefresh) {
                    clearRefreshInterval();
                    //toast
                    $mdToast.show(
                        $mdToast.simple()
                            .textContent('Auto refresh disabled')
                            .hideDelay(3000)
                    );
                } else {
                    setRefreshInterval();
                    $mdToast.show(
                        $mdToast.simple()
                            .textContent('Auto refresh enabled')
                            .hideDelay(3000)
                    );
                }
            }
        );

        /**
         * Watch when a zoom is active.
         */
        $scope.$watch(
            function () {
                return self.zoomedMinTime;
            },
            function (newVal, oldVal) {
                if (!_.isUndefined(self.zoomedMinTime) && self.zoomedMinTime > 0) {
                  //  if (self.isAtInitialZoom) {
                  //      self.isAtInitialZoom = false;
                   // } else {
                        cancelPreviousOnZoomed();
                        self.changeZoomPromise = $timeout(changeZoom, ZOOM_DELAY);
                   // }
                }
            }
        );

        function refresh(){
            var to = new Date().getTime();
            var millis = self.timeFrameOptions[self.timeFrameOptionIndex].properties.millis;
            var from = to - millis;
            self.minDisplayTime = from;

            self.maxDisplayTime = to;

            FeedStatsService.setTimeBoundaries(from, to);
            buildChartData(true);
        }


        /**
         * When the slider is changed refresh the charts/data
         * @param timeFrame
         */
        function onTimeFrameChanged(timeFrame) {
            if(self.isZoomed){
                resetZoom();
            }
            self.isAtInitialZoom = true;
            self.timeFrame = timeFrame;
            var millis = self.timeFrameOptions[self.timeFrameOptionIndex].properties.millis;
            if(millis >= self.minZoomTime){
              enableZoom();
            }
            else {

                disableZoom();
            }
            clearRefreshInterval();
            refresh();

            //disable refresh if > 30 min timeframe
            if(millis >(1000*60*30)){
                self.autoRefresh = false;
            }
            else {
                if(!self.autoRefresh) {
                    self.autoRefresh = true;
                }
                else {
                    setRefreshInterval();

                }
            }


        }

        function enableZoom(){
            self.zoomEnabled = true;
            self.feedChartOptions.chart.zoom.enabled=true;
            self.forceChartRefresh = true;
            self.forceXDomain = true;

        }

        function disableZoom(){
            resetZoom();
            self.zoomEnabled = false;
            self.feedChartOptions.chart.zoom.enabled=false;
            self.forceChartRefresh = true;
        }


        function onProcessorChartFunctionChanged() {
            FeedStatsService.setSelectedChartFunction(self.selectedProcessorStatisticFunction);
            var chartData = FeedStatsService.changeProcessorChartDataFunction(self.selectedProcessorStatisticFunction);
            self.processorChartData[0].values = chartData.data;
            FeedStatsService.updateBarChartHeight(self.processorChartOptions, self.processorChartApi, chartData.data.length, self.selectedProcessorStatisticFunction);
        }

        function buildChartData(timeIntervalChange) {
            if (!FeedStatsService.isLoading()) {
                timeIntervalChange = angular.isUndefined(timeIntervalChange) ? false : timeIntervalChange;
                self.feedTimeChartLoading = true;
                self.processChartLoading = true;
                buildProcessorChartData();
                buildFeedCharts();
                fetchFeedProcessorErrors(timeIntervalChange);
            }
            getFeedHealth();
        }

        function updateSuccessEventsPercentKpi() {
            if (self.summaryStatsData.totalEvents == 0) {
                self.eventSuccessKpi.icon = 'remove';
                self.eventSuccessKpi.color = "#1f77b4"
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

        function updateFlowRateKpi() {
            self.flowRateKpi.value = self.summaryStatistics.flowsStartedPerSecond;
        }

        function updateAvgDurationKpi() {
            var avgMillis = self.summaryStatistics.avgFlowDurationMilis;
            self.avgDurationKpi.value = DateTimeUtils.formatMillisAsText(avgMillis, false, true);
        }

        function formatSecondsToMinutesAndSeconds(s) {   // accepts seconds as Number or String. Returns m:ss
            return ( s - ( s %= 60 )) / 60 + (9 < s ? ':' : ':0' ) + s;
        }

        function updateSummaryKpis() {
            updateFlowRateKpi();
            updateSuccessEventsPercentKpi();
            updateAvgDurationKpi();
        }

        function buildProcessorChartData() {
            var values = [];
            self.processChartLoading = true;
            var minTime = undefined;
            var maxTime = undefined;
            if(self.isZoomed && self.zoomedMinTime != UNZOOMED_VALUE) {
                //reset x xaxis to the zoom values
                minTime=self.zoomedMinTime;
                maxTime =self.zoomedMaxTime
            }
            $q.when(FeedStatsService.fetchProcessorStatistics(minTime,maxTime)).then(function (response) {
                self.summaryStatsData = FeedStatsService.summaryStatistics;
                updateSummaryKpis();
                self.processorChartData = FeedStatsService.buildProcessorDurationChartData();

                FeedStatsService.updateBarChartHeight(self.processorChartOptions, self.processorChartApi, self.processorChartData[0].values.length, self.selectedProcessorStatisticFunction);
                self.processChartLoading = false;
                self.lastProcessorChartRefresh = new Date().getTime();
                self.lastRefreshTime = new Date();
            }, function () {
                self.processChartLoading = false;
                self.lastProcessorChartRefresh = new Date().getTime();
            });
        }

        function buildFeedCharts() {

            self.feedTimeChartLoading = true;
            $q.when(FeedStatsService.fetchFeedTimeSeriesData()).then(function (feedTimeSeries) {

                self.minTime = feedTimeSeries.time.startTime;
                self.maxTime = feedTimeSeries.time.endTime;
                self.timeDiff = self.maxTime - self.minTime;

                var chartArr = [];
                chartArr.push({
                    label: 'Completed', color: '#3483BA', valueFn: function (item) {
                            return item.jobsFinishedPerSecond;
                    }
                });
                chartArr.push({
                    label: 'Started', area: true, color: "#F08C38", valueFn: function (item) {
                            return item.jobsStartedPerSecond;
                    }
                });
                //preserve the legend selections
                if (feedChartLegendState.length > 0) {
                    _.each(chartArr, function (item, i) {
                        item.disabled = feedChartLegendState[i];
                    });
                }

                self.feedChartData = Nvd3ChartService.toLineChartData(feedTimeSeries.raw.stats, chartArr, 'minEventTime', null,self.minTime, self.maxTime);
                var max = Nvd3ChartService.determineMaxY(self.feedChartData);
                if(self.isZoomed) {
                    max = self.zoomMaxY;
                }
                var maxChanged =  self.maxY < max;
                self.minY = 0;
                self.maxY = max;
                if(max <5){
                    max = 5;
                }


                self.feedChartOptions.chart.forceY = [0, max];
                if (self.feedChartOptions.chart.yAxis.ticks != max) {
                    self.feedChartOptions.chart.yDomain = [0, max];
                    var ticks = max;
                    if (ticks > 8) {
                        ticks = 8;
                    }
                    if(angular.isUndefined(ticks) || ticks <5){
                        ticks = 5;
                    }
                    self.feedChartOptions.chart.yAxis.ticks = ticks;
                }

                if(self.isZoomed && (self.forceXDomain == true || self.zoomedMinTime != UNZOOMED_VALUE)) {
                    //reset x xaxis to the zoom values
                    self.feedChartOptions.chart.xDomain = [self.zoomedMinTime,self.zoomedMaxTime]
                    var y = self.zoomMaxY > 0 ? self.zoomMaxY : self.maxY;
                    self.feedChartOptions.chart.yDomain = [0,y]
                }
                else  if(!self.isZoomed && self.forceXDomain){
                    self.feedChartOptions.chart.xDomain = [self.minTime,self.maxTime];
                    self.feedChartOptions.chart.yDomain = [0,self.maxY]
                }

                initiatePreventZoom();
                if (self.feedChartApi && self.feedChartApi.refresh  && self.feedChartApi.update) {
                      if(maxChanged || self.forceChartRefresh ) {
                          self.feedChartApi.refresh();
                          self.forceChartRefresh = false;
                      }
                      else {
                            self.feedChartApi.update();
                      }
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
            }, function (err) {
                self.feedProcessorErrorsLoading = false;
            });

        }

        /**
         * Gets the Feed Health
         */
        function getFeedHealth() {
            var successFn = function (response) {
                if (response.data) {
                    //transform the data for UI
                    if (response.data.feedSummary) {
                        angular.extend(self.feed, response.data.feedSummary[0]);
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

            $http.get(OpsManagerFeedService.SPECIFIC_FEED_HEALTH_URL(self.feedName)).then(successFn, errorFn);
        }


        function clearRefreshInterval() {
            if (self.refreshInterval != null) {
                $interval.cancel(self.refreshInterval);
                self.refreshInterval = null;
            }
        }

        function setRefreshInterval() {
            clearRefreshInterval();

            if (self.autoRefresh ) {
                // anything below 5 minute interval to be refreshed every 5 seconds,
                // anything above 5 minutes to be refreshed in proportion to its time span, i.e. the longer the time span the less it is refreshed
                var option = self.timeFramOptionsLookupMap[self.timeFrame];
                if (!_.isUndefined(option)) {
                    //timeframe option will be undefined when page loads for the first time
                    var refreshInterval = option.properties.millis / 60;
                    self.refreshIntervalTime = refreshInterval < 5000 ? 5000 : refreshInterval;
                }
                if (self.refreshIntervalTime) {
                    self.refreshInterval = $interval(function () {
                        refresh();
                        }, self.refreshIntervalTime
                    );
                }
            }
        }


        /**
         * Initialize the charts
         */
        function initCharts() {
            FeedStatsService.setFeedName(self.feedName);
            setupChartOptions();
            self.onRefreshButtonClick();
            self.dataLoaded = true;
        }

        /**
         * Fetch and load the Time slider options
         */
        function loadTimeFrameOption() {
            ProvenanceEventStatsService.getTimeFrameOptions().then(function (response) {
                self.timeFrameOptions = response.data;
                self.timeFrameOptionIndexLength = self.timeFrameOptions.length;
                _.each(response.data, function (labelValue) {
                    self.timeFramOptionsLookupMap[labelValue.value] = labelValue;
                });
                $timeout(function () {
                    //update initial slider position in UI
                    self.timeFrameOptionIndex = _.findIndex(self.timeFrameOptions, function (option) {
                        return option.value === self.timeFrame;
                    });
                    initCharts();
                }, 1);
            });
        }



        /**
         * Initialize the page.  Called when first page loads to set it up
         */
        function init(){
            loadTimeFrameOption();
        }

        //Load the page
        init();

        $scope.$on('$destroy', function () {
            clearRefreshInterval();
            cancelPreviousOnZoomed();
        });

    };

    angular.module(moduleName).controller('FeedStatsChartsController',
        ["$scope", "$element", "$http", "$interval", "$timeout", "$q","$mdToast", "ProvenanceEventStatsService", "FeedStatsService", "Nvd3ChartService", "OpsManagerFeedService", "StateService", controller]);

    angular.module(moduleName)
        .directive('kyloFeedStatsCharts', directive);

});
