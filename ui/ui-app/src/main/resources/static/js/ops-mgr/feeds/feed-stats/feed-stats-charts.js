define(["require", "exports", "angular", "./module-name", "../../services/OpsManagerFeedService", "../../services/ProvenanceEventStatsService", "../../services/Nvd3ChartService", "underscore"], function (require, exports, angular, module_name_1, OpsManagerFeedService_1, ProvenanceEventStatsService_1, Nvd3ChartService_1, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var d3 = require('../../bower_components/d3');
    var controller = /** @class */ (function () {
        function controller($scope, $element, $http, $interval, $timeout, $q, $mdToast, ProvenanceEventStatsService, FeedStatsService, Nvd3ChartService, OpsManagerFeedService, StateService, $filter) {
            var _this = this;
            this.$scope = $scope;
            this.$element = $element;
            this.$http = $http;
            this.$interval = $interval;
            this.$timeout = $timeout;
            this.$q = $q;
            this.$mdToast = $mdToast;
            this.ProvenanceEventStatsService = ProvenanceEventStatsService;
            this.FeedStatsService = FeedStatsService;
            this.Nvd3ChartService = Nvd3ChartService;
            this.OpsManagerFeedService = OpsManagerFeedService;
            this.StateService = StateService;
            this.$filter = $filter;
            //// USER INTERACTIONS, buttons
            /**
             * When a user clicks the Refresh Button
             */
            this.onRefreshButtonClick = function () {
                _this.refresh();
            };
            /**
             * Navigate to the Feed Manager Feed Details
             * @param ev
             */
            this.gotoFeedDetails = function (ev) {
                if (_this.feed.feedId != undefined) {
                    _this.StateService.FeedManager().Feed().navigateToFeedDetails(_this.feed.feedId);
                }
            };
            /**
             * Show detailed Errors
             */
            this.viewNewFeedProcessorErrors = function () {
                _this.feedProcessorErrors.viewAllData();
            };
            this.toggleFeedProcessorErrorsRefresh = function (autoRefresh) {
                if (autoRefresh) {
                    _this.feedProcessorErrors.viewAllData();
                    _this.feedProcessorErrors.autoRefreshMessage = 'enabled';
                }
                else {
                    _this.feedProcessorErrors.autoRefreshMessage = 'disabled';
                }
            };
            /**
             * Called when a user click on the Reset Zoom button
             */
            this.onResetZoom = function () {
                if (_this.isZoomed) {
                    _this.initiatePreventZoom();
                    _this.resetZoom();
                    _this.feedChartOptions.chart.xDomain = [_this.minTime, _this.maxTime];
                    _this.feedChartOptions.chart.yDomain = [_this.minY, _this.maxY];
                    _this.feedChartApi.refresh();
                    _this.buildProcessorChartData();
                }
            };
            /**
             * prevent the initial zoom to fire in chart after reload
             */
            this.initiatePreventZoom = function () {
                var cancelled = false;
                if (angular.isDefined(_this.preventZoomPromise)) {
                    _this.$timeout.cancel(_this.preventZoomPromise);
                    _this.preventZoomPromise = undefined;
                    cancelled = true;
                }
                if (!_this.preventZoomChange || cancelled) {
                    _this.preventZoomChange = true;
                    _this.preventZoomPromise = _this.$timeout(function () {
                        _this.preventZoomChange = false;
                        _this.preventZoomPromise = undefined;
                    }, 1000);
                }
            };
            /**
             * Initialize the Charts
             */
            this.setupChartOptions = function () {
                _this.processorChartOptions = {
                    chart: {
                        type: 'multiBarHorizontalChart',
                        height: 400,
                        margin: {
                            top: 5,
                            right: 50,
                            bottom: 50,
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
                        interactiveLayer: { tooltip: { gravity: 's' } },
                        yAxis: {
                            axisLabel: _this.FeedStatsService.processorStatsFunctionMap[_this.selectedProcessorStatisticFunction].axisLabel,
                            tickFormat: function (d) {
                                return d3.format(',.2f')(d);
                            }
                        },
                        valueFormat: function (d) {
                            return d3.format(',.2f')(d);
                        },
                        noData: _this.$filter('translate')('view.feed-stats-charts.noData')
                    }
                };
                /**
                 * Help adjust the x axis label depending on time window
                 * @param d
                 */
                _this.timeSeriesXAxisLabel = function (d) {
                    var maxTime = 1000 * 60 * 60 * 12; //12 hrs
                    if (_this.timeDiff >= maxTime) {
                        //show the date if it spans larger than maxTime
                        return d3.time.format('%Y-%m-%d %H:%M')(new Date(d));
                    }
                    else {
                        return d3.time.format('%X')(new Date(d));
                    }
                };
                /**
                 * Prevent zooming into a level of detail that the data doesnt allow
                 * Stats > a day are aggregated up to the nearest hour
                 * Stats > 10 hours are aggregated up to the nearest minute
                 * If a user is looking at data within the 2 time frames above, prevent the zoom to a level greater than the hour/minute
                 * @param xDomain
                 * @param yDomain
                 * @return {boolean}
                 */
                _this.canZoom = function (xDomain, yDomain) {
                    var diff = _this.maxTime - _this.minTime;
                    var minX = Math.floor(xDomain[0]);
                    var maxX = Math.floor(xDomain[1]);
                    var zoomDiff = maxX - minX;
                    //everything above the day should be zoomed at the hour level
                    //everything above 10 hrs should be zoomed at the minute level
                    if (diff >= (1000 * 60 * 60 * 24)) {
                        if (zoomDiff < (1000 * 60 * 60)) {
                            return false; //prevent zooming!
                        }
                    }
                    else if (diff >= (1000 * 60 * 60 * 10)) {
                        // zoom at minute level
                        if (zoomDiff < (1000 * 60)) {
                            return false;
                        }
                    }
                    return true;
                };
                _this.feedChartOptions = {
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
                        clipEdge: false,
                        useInteractiveGuideline: true,
                        interactiveLayer: { tooltip: { gravity: 's' } },
                        valueFormat: function (d) {
                            return d3.format(',')(parseInt(d));
                        },
                        xAxis: {
                            axisLabel: _this.$filter('translate')('view.feed-stats-charts.Time'),
                            showMaxMin: false,
                            tickFormat: _this.timeSeriesXAxisLabel,
                            rotateLabels: -45
                        },
                        yAxis: {
                            axisLabel: _this.$filter('translate')('view.feed-stats-charts.FPS'),
                            axisLabelDistance: -10
                        },
                        legend: {
                            dispatch: {
                                stateChange: function (e) {
                                    _this.feedChartLegendState = e.disabled;
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
                            useFixedDomain: false,
                            zoomed: function (xDomain, yDomain) {
                                //zoomed will get called initially (even if not zoomed)
                                // because of this we need to check to ensure the 'preventZoomChange' flag was not triggered after initially refreshing the dataset
                                if (!_this.preventZoomChange) {
                                    _this.isZoomed = true;
                                    if (_this.canZoom(xDomain, yDomain)) {
                                        _this.zoomedMinTime = Math.floor(xDomain[0]);
                                        _this.zoomedMaxTime = Math.floor(xDomain[1]);
                                        _this.timeDiff = _this.zoomedMaxTime - _this.zoomedMinTime;
                                        var max1 = Math.ceil(yDomain[0]);
                                        var max2 = Math.ceil(yDomain[1]);
                                        _this.zoomMaxY = max2 > max1 ? max2 : max1;
                                    }
                                    return { x1: _this.zoomedMinTime, x2: _this.zoomedMaxTime, y1: yDomain[0], y2: yDomain[1] };
                                }
                                else {
                                    return { x1: _this.minTime, x2: _this.maxTime, y1: _this.minY, y2: _this.maxY };
                                }
                            },
                            unzoomed: function (xDomain, yDomain) {
                                return _this.resetZoom();
                            }
                        },
                        interactiveLayer2: {
                            dispatch: {
                                elementClick: function (t, u) { }
                            }
                        },
                        dispatch: {}
                    }
                };
            };
            /**
             * Reset the Zoom and return the x,y values pertaining to the min/max of the complete dataset
             * @return {{x1: *, x2: (*|number|endTime|{name, fn}|Number), y1: number, y2: (number|*)}}
             */
            this.resetZoom = function () {
                if (_this.isZoomed) {
                    _this.isZoomed = false;
                    _this.zoomedMinTime = _this.UNZOOMED_VALUE;
                    _this.zoomedMaxTime = _this.UNZOOMED_VALUE;
                    _this.minDisplayTime = _this.minTime;
                    _this.maxDisplayTime = _this.maxTime;
                    _this.timeDiff = _this.maxTime - _this.minTime;
                    return { x1: _this.minTime, x2: _this.maxTime, y1: _this.minY, y2: _this.maxY };
                }
            };
            this.changeZoom = function () {
                _this.timeDiff = _this.zoomedMaxTime - _this.zoomedMinTime;
                _this.autoRefresh = false;
                _this.isZoomed = true;
                _this.isAtInitialZoom = true;
                //    FeedStatsService.setTimeBoundaries(this.minTime, this.maxTime);
                _this.buildProcessorChartData();
                _this.minDisplayTime = _this.zoomedMinTime;
                _this.maxDisplayTime = _this.zoomedMaxTime;
                /*
               if(this.zoomedMinTime != UNZOOMED_VALUE) {
                    //reset x xaxis to the zoom values
                    this.feedChartOptions.chart.xDomain = [this.zoomedMinTime,this.zoomedMaxTime]
                    var y = this.zoomMaxY > 0 ? this.zoomMaxY : this.maxY;
                    this.feedChartOptions.chart.yDomain = [0,this.maxY]
                }
                else  {
                    this.feedChartOptions.chart.xDomain = [this.minTime,this.maxTime];
                    this.feedChartOptions.chart.yDomain = [0,this.maxY]
                }
               this.feedChartApi.update();
    */
            };
            /**
             * Cancel the zoom timeout watcher
             */
            this.cancelPreviousOnZoomed = function () {
                if (!_.isUndefined(_this.changeZoomPromise)) {
                    _this.$timeout.cancel(_this.changeZoomPromise);
                    _this.changeZoomPromise = undefined;
                }
            };
            this.onTimeFrameChanged = function () {
                if (!_.isUndefined(_this.timeFrameOptions)) {
                    _this.timeFrame = _this.timeFrameOptions[Math.floor(_this.timeFrameOptionIndex)].value;
                    _this.displayLabel = _this.timeFrame.label;
                    _this.isZoomed = false;
                    _this.zoomedMinTime = _this.UNZOOMED_VALUE;
                    _this.zoomedMaxTime = _this.UNZOOMED_VALUE;
                    _this.initiatePreventZoom();
                    _this.onTimeFrameChanged2(_this.timeFrame);
                }
            };
            /*   $scope.$watch(
                   //update time frame when slider is moved
                   function () {
                       return this.timeFrameOptionIndex;
                   },
                   function () {
                       if (!_.isUndefined(this.timeFrameOptions)) {
                           this.timeFrame = this.timeFrameOptions[Math.floor(this.timeFrameOptionIndex)].value;
                           this.displayLabel = this.timeFrame.label;
                           this.isZoomed = false;
                           this.zoomedMinTime = UNZOOMED_VALUE;
                           this.zoomedMaxTime = UNZOOMED_VALUE;
                           onTimeFrameChanged(this.timeFrame);
                       }
                   }
               );
               */
            this.refresh = function () {
                var to = new Date().getTime();
                var millis = _this.timeFrameOptions[_this.timeFrameOptionIndex].properties.millis;
                var from = to - millis;
                _this.minDisplayTime = from;
                _this.maxDisplayTime = to;
                _this.FeedStatsService.setTimeBoundaries(from, to);
                _this.buildChartData(true);
            };
            this.enableZoom = function () {
                _this.zoomEnabled = true;
                _this.feedChartOptions.chart.zoom.enabled = true;
                _this.forceChartRefresh = true;
                _this.forceXDomain = true;
            };
            this.disableZoom = function () {
                _this.resetZoom();
                _this.zoomEnabled = false;
                _this.feedChartOptions.chart.zoom.enabled = false;
                _this.forceChartRefresh = true;
            };
            this.onProcessorChartFunctionChanged = function () {
                _this.FeedStatsService.setSelectedChartFunction(_this.selectedProcessorStatisticFunction);
                var chartData = _this.FeedStatsService.changeProcessorChartDataFunction(_this.selectedProcessorStatisticFunction);
                _this.processorChartData[0].values = chartData.data;
                _this.FeedStatsService.updateBarChartHeight(_this.processorChartOptions, _this.processorChartApi, chartData.data.length, _this.selectedProcessorStatisticFunction);
            };
            this.buildChartData = function (timeIntervalChange) {
                if (!_this.FeedStatsService.isLoading()) {
                    timeIntervalChange = angular.isUndefined(timeIntervalChange) ? false : timeIntervalChange;
                    _this.feedTimeChartLoading = true;
                    _this.processChartLoading = true;
                    _this.buildProcessorChartData();
                    _this.buildFeedCharts();
                    _this.fetchFeedProcessorErrors(timeIntervalChange);
                }
                _this.getFeedHealth();
            };
            this.updateSuccessEventsPercentKpi = function () {
                if (_this.summaryStatsData.totalEvents == 0) {
                    _this.eventSuccessKpi.icon = 'remove';
                    _this.eventSuccessKpi.color = "#1f77b4";
                    _this.eventSuccessKpi.value = "--";
                }
                else {
                    var failed = _this.summaryStatsData.totalEvents > 0 ? (_this.summaryStatsData.failedEvents / _this.summaryStatsData.totalEvents).toFixed(2) * 100 : 0;
                    var value = (100 - failed).toFixed(0);
                    var icon = 'offline_pin';
                    var iconColor = "#3483BA";
                    _this.eventSuccessKpi.icon = icon;
                    _this.eventSuccessKpi.color = iconColor;
                    _this.eventSuccessKpi.value = value;
                }
            };
            this.updateFlowRateKpi = function () {
                _this.flowRateKpi.value = _this.summaryStatistics.flowsStartedPerSecond;
            };
            this.updateAvgDurationKpi = function () {
                var avgMillis = this.summaryStatistics.avgFlowDurationMilis;
                this.avgDurationKpi.value = this.DateTimeUtils(this.$filter('translate')).formatMillisAsText(avgMillis, false, true);
            };
            this.formatSecondsToMinutesAndSeconds = function (s) {
                return (s - (s %= 60)) / 60 + (9 < s ? ':' : ':0') + s;
            };
            this.updateSummaryKpis = function () {
                _this.updateFlowRateKpi();
                _this.updateSuccessEventsPercentKpi();
                _this.updateAvgDurationKpi();
            };
            this.buildProcessorChartData = function () {
                var values = [];
                _this.processChartLoading = true;
                var minTime = undefined;
                var maxTime = undefined;
                if (_this.isZoomed && _this.zoomedMinTime != _this.UNZOOMED_VALUE) {
                    //reset x xaxis to the zoom values
                    minTime = _this.zoomedMinTime;
                    maxTime = _this.zoomedMaxTime;
                }
                _this.$q.when(_this.FeedStatsService.fetchProcessorStatistics(minTime, maxTime)).then(function (response) {
                    _this.summaryStatsData = _this.FeedStatsService.summaryStatistics;
                    _this.updateSummaryKpis();
                    _this.processorChartData = _this.FeedStatsService.buildProcessorDurationChartData();
                    _this.FeedStatsService.updateBarChartHeight(_this.processorChartOptions, _this.processorChartApi, _this.processorChartData[0].values.length, _this.selectedProcessorStatisticFunction);
                    _this.processChartLoading = false;
                    _this.lastProcessorChartRefresh = new Date().getTime();
                    _this.lastRefreshTime = new Date();
                }, function () {
                    _this.processChartLoading = false;
                    _this.lastProcessorChartRefresh = new Date().getTime();
                });
            };
            this.buildFeedCharts = function () {
                _this.feedTimeChartLoading = true;
                _this.$q.when(_this.FeedStatsService.fetchFeedTimeSeriesData()).then(function (feedTimeSeries) {
                    _this.minTime = feedTimeSeries.time.startTime;
                    _this.maxTime = feedTimeSeries.time.endTime;
                    _this.timeDiff = _this.maxTime - _this.minTime;
                    var chartArr = [];
                    chartArr.push({
                        label: _this.$filter('translate')('view.feed-stats-charts.Completed'), color: '#3483BA', valueFn: function (item) {
                            return item.jobsFinishedPerSecond;
                        }
                    });
                    chartArr.push({
                        label: _this.$filter('translate')('view.feed-stats-charts.Started'), area: true, color: "#F08C38", valueFn: function (item) {
                            return item.jobsStartedPerSecond;
                        }
                    });
                    //preserve the legend selections
                    if (_this.feedChartLegendState.length > 0) {
                        _.each(chartArr, function (item, i) {
                            item.disabled = _this.feedChartLegendState[i];
                        });
                    }
                    _this.feedChartData = _this.Nvd3ChartService.toLineChartData(feedTimeSeries.raw.stats, chartArr, 'minEventTime', null, _this.minTime, _this.maxTime);
                    var max = _this.Nvd3ChartService.determineMaxY(_this.feedChartData);
                    if (_this.isZoomed) {
                        max = _this.zoomMaxY;
                    }
                    var maxChanged = _this.maxY < max;
                    _this.minY = 0;
                    _this.maxY = max;
                    if (max < 5) {
                        max = 5;
                    }
                    _this.feedChartOptions.chart.forceY = [0, max];
                    if (_this.feedChartOptions.chart.yAxis.ticks != max) {
                        _this.feedChartOptions.chart.yDomain = [0, max];
                        var ticks = max;
                        if (ticks > 8) {
                            ticks = 8;
                        }
                        if (angular.isUndefined(ticks) || ticks < 5) {
                            ticks = 5;
                        }
                        _this.feedChartOptions.chart.yAxis.ticks = ticks;
                    }
                    if (_this.isZoomed && (_this.forceXDomain == true || _this.zoomedMinTime != _this.UNZOOMED_VALUE)) {
                        //reset x xaxis to the zoom values
                        _this.feedChartOptions.chart.xDomain = [_this.zoomedMinTime, _this.zoomedMaxTime];
                        var y = _this.zoomMaxY > 0 ? _this.zoomMaxY : _this.maxY;
                        _this.feedChartOptions.chart.yDomain = [0, y];
                    }
                    else if (!_this.isZoomed && _this.forceXDomain) {
                        _this.feedChartOptions.chart.xDomain = [_this.minTime, _this.maxTime];
                        _this.feedChartOptions.chart.yDomain = [0, _this.maxY];
                    }
                    _this.initiatePreventZoom();
                    if (_this.feedChartApi && _this.feedChartApi.refresh && _this.feedChartApi.update) {
                        if (maxChanged || _this.forceChartRefresh) {
                            _this.feedChartApi.refresh();
                            _this.forceChartRefresh = false;
                        }
                        else {
                            _this.feedChartApi.update();
                        }
                    }
                    _this.feedTimeChartLoading = false;
                    _this.lastFeedTimeChartRefresh = new Date().getTime();
                }, function () {
                    _this.feedTimeChartLoading = false;
                    _this.lastFeedTimeChartRefresh = new Date().getTime();
                });
            };
            /**
             * fetch and append the errors to the FeedStatsService.feedProcessorErrors.data object
             * @param resetWindow optionally reset the feed errors to start a new array of errors in the feedProcessorErrors.data
             */
            this.fetchFeedProcessorErrors = function (resetWindow) {
                _this.feedProcessorErrorsLoading = true;
                _this.$q.when(_this.FeedStatsService.fetchFeedProcessorErrors(resetWindow)).then(function (feedProcessorErrors) {
                    _this.feedProcessorErrorsLoading = false;
                }, function (err) {
                    _this.feedProcessorErrorsLoading = false;
                });
            };
            /**
             * Gets the Feed Health
             */
            this.getFeedHealth = function () {
                var successFn = function (response) {
                    if (response.data) {
                        //transform the data for UI
                        if (response.data.feedSummary) {
                            angular.extend(_this.feed, response.data.feedSummary[0]);
                            _this.feed.feedId = _this.feed.feedHealth.feedId;
                            if (_this.feed.running) {
                                _this.feed.displayStatus = 'RUNNING';
                            }
                            else {
                                _this.feed.displayStatus = 'STOPPED';
                            }
                        }
                    }
                };
                var errorFn = function (err) {
                };
                _this.$http.get(_this.OpsManagerFeedService.SPECIFIC_FEED_HEALTH_URL(_this.feedName)).then(successFn, errorFn);
            };
            this.clearRefreshInterval = function () {
                if (_this.refreshInterval != null) {
                    _this.$interval.cancel(_this.refreshInterval);
                    _this.refreshInterval = null;
                }
            };
            this.setRefreshInterval = function () {
                _this.clearRefreshInterval();
                if (_this.autoRefresh) {
                    // anything below 5 minute interval to be refreshed every 5 seconds,
                    // anything above 5 minutes to be refreshed in proportion to its time span, i.e. the longer the time span the less it is refreshed
                    var option = _this.timeFramOptionsLookupMap[_this.timeFrame];
                    if (!_.isUndefined(option)) {
                        //timeframe option will be undefined when page loads for the first time
                        var refreshInterval = option.properties.millis / 60;
                        _this.refreshIntervalTime = refreshInterval < 5000 ? 5000 : refreshInterval;
                    }
                    if (_this.refreshIntervalTime) {
                        _this.refreshInterval = _this.$interval(function () {
                            _this.refresh();
                        }, _this.refreshIntervalTime);
                    }
                }
            };
            /**
             * Initialize the charts
             */
            this.initCharts = function () {
                _this.FeedStatsService.setFeedName(_this.feedName);
                _this.setupChartOptions();
                _this.onRefreshButtonClick();
                _this.dataLoaded = true;
            };
            /**
             * Fetch and load the Time slider options
             */
            this.loadTimeFrameOption = function () {
                _this.ProvenanceEventStatsService.getTimeFrameOptions().then(function (response) {
                    _this.timeFrameOptions = response.data;
                    _this.timeFrameOptionIndexLength = _this.timeFrameOptions.length;
                    _.each(response.data, function (labelValue) {
                        _this.timeFramOptionsLookupMap[labelValue.value] = labelValue;
                    });
                    _this.$timeout(function () {
                        //update initial slider position in UI
                        _this.timeFrameOptionIndex = _.findIndex(_this.timeFrameOptions, function (option) {
                            return option.value === _this.timeFrame;
                        });
                        _this.initCharts();
                    }, 1);
                });
            };
            /**
             * Initialize the page.  Called when first page loads to set it up
             */
            this.init = function () {
                _this.loadTimeFrameOption();
            };
            /**
     * When the slider is changed refresh the charts/data
     * @param timeFrame
     */
            this.onTimeFrameChanged2 = function (timeFrame) {
                if (_this.isZoomed) {
                    _this.resetZoom();
                }
                _this.isAtInitialZoom = true;
                _this.timeFrame = timeFrame;
                var millis = _this.timeFrameOptions[_this.timeFrameOptionIndex].properties.millis;
                if (millis >= _this.minZoomTime) {
                    _this.enableZoom();
                }
                else {
                    _this.disableZoom();
                }
                _this.clearRefreshInterval();
                _this.refresh();
                //disable refresh if > 30 min timeframe
                if (millis > (1000 * 60 * 30)) {
                    _this.autoRefresh = false;
                }
                else {
                    if (!_this.autoRefresh) {
                        _this.autoRefresh = true;
                    }
                    else {
                        _this.setRefreshInterval();
                    }
                }
            };
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
            this.timeFrame = 'FIVE_MIN';
            /**
             * Array of fixed times
             * @type {Array}
             */
            this.timeframeOptions = [];
            /**
             * last time the page was refreshed
             * @type {null}
             */
            this.lastRefreshTime = null;
            /**
             * map of the the timeFrame value to actual timeframe object (i.e. FIVE_MIN:{timeFrameObject})
             * @type {{}}
             */
            this.timeFramOptionsLookupMap = {};
            /**
             * The selected Time frame
             * @type {{}}
             */
            this.selectedTimeFrameOptionObject = {};
            /**
             * Flag to enable disable auto refresh
             * @type {boolean}
             */
            this.autoRefresh = true;
            /**
             * Flag to indicate if we are zoomed or not
             * @type {boolean}
             */
            this.isZoomed = false;
            /**
             * Zoom helper
             * @type {boolean}
             */
            this.isAtInitialZoom = true;
            /**
             * Difference in overall min/max time for the chart
             * Used to help calcuate the correct xXais label (i.e. for larger time periods show Date + time, else show time
             * @type {null}
             */
            this.timeDiff = null;
            /**
             * millis to wait after a zoom is complete to update the charts
             * @type {number}
             */
            this.ZOOM_DELAY = 700;
            /**
             * Constant set to indicate we are not zoomed
             * @type {number}
             */
            this.UNZOOMED_VALUE = -1;
            /**
             * After a chart is rendered it will always call the zoom function.
             * Flag to prevent the initial zoom from triggering after refresh of the chart
             * @type {boolean}
             */
            this.preventZoomChange = false;
            /**
             * Timeout promise to prevent zoom
             * @type {undefined}
             */
            this.preventZoomPromise = undefined;
            /**
             * The Min Date of data.  This will be the zoomed value if we are zooming, otherwise the min value in the dataset
             */
            this.minDisplayTime;
            /**
             * The max date of the data.  this will be the zoomed value if zooming otherwise the max value in the dataset
             */
            this.maxDisplayTime;
            /**
             * max Y value (when not zoomed)
             * @type {number}
             */
            this.maxY = 0;
            /**
             * max Y Value when zoomed
             * @type {number}
             */
            this.zoomMaxY = 0;
            /**
             * Min time frame to enable zooming.
             * Defaults to 30 min.
             * Anything less than this will not be zoomable
             * @type {number}
             */
            this.minZoomTime = 1000 * 60 * 30;
            /**
             * Flag to indicate if zooming is enabled.
             * Zooming is only enabled for this.minZoomTime or above
             *
             * @type {boolean}
             */
            this.zoomEnabled = false;
            /**
             * A bug in nvd3 charts exists where if the zoom is toggle to true it requires a force of the x axis when its toggled back to false upon every data refresh.
             * this flag will be triggered when the zoom enabled changes and from then on it will manually reset the x domain when the data refreshes
             * @type {boolean}
             */
            this.forceXDomain = false;
            /**
             * Flag to force the rendering of the chart to refresh
             * @type {boolean}
             */
            this.forceChartRefresh = false;
            /**
             * Summary stats
             * @type {*}
             */
            this.summaryStatistics = FeedStatsService.summaryStatistics;
            this.feedChartLegendState = [];
            this.feedChartData = [];
            this.feedChartApi = {};
            this.feedChartOptions = {};
            this.processorChartApi = {};
            this.processorChartData = [];
            this.processorChartOptions = {};
            this.selectedProcessorStatisticFunction = 'Average Duration';
            this.processorStatsFunctions = FeedStatsService.processorStatsFunctions();
            /**
             * The Feed we are looking at
             * @type {{displayStatus: string}}
             */
            this.feed = {
                displayStatus: ''
            };
            /**
             * Latest summary stats
             * @type {{}}
             */
            this.summaryStatsData = {};
            this.eventSuccessKpi = {
                value: 0,
                icon: '',
                color: ''
            };
            this.flowRateKpi = {
                value: 0,
                icon: 'tune',
                color: '#1f77b4'
            };
            this.avgDurationKpi = {
                value: 0,
                icon: 'access_time',
                color: '#1f77b4'
            };
            this.feedProcessorErrorsTable = {
                sortOrder: '-errorMessageTimestamp',
                filter: '',
                rowLimit: 5,
                page: 1
            };
            /**
             * Errors for th error table (if any)
             * @type {*}
             */
            this.feedProcessorErrors = FeedStatsService.feedProcessorErrors;
            /**
             * When a user changes the Processor drop down
             * @type {onProcessorChartFunctionChanged}
             */
            this.onProcessorChartFunctionChangedVar = this.onProcessorChartFunctionChanged;
            /**
           * Enable/disable the refresh interval
           */
            $scope.$watch(function () {
                return _this.autoRefresh;
            }, function (newVal, oldVal) {
                if (!_this.autoRefresh) {
                    _this.clearRefreshInterval();
                    //toast
                    $mdToast.show($mdToast.simple()
                        .textContent('Auto refresh disabled')
                        .hideDelay(3000));
                }
                else {
                    _this.setRefreshInterval();
                    $mdToast.show($mdToast.simple()
                        .textContent('Auto refresh enabled')
                        .hideDelay(3000));
                }
            });
            /**
             * Watch when a zoom is active.
             */
            $scope.$watch(function () {
                return _this.zoomedMinTime;
            }, function (newVal, oldVal) {
                if (!_.isUndefined(_this.zoomedMinTime) && _this.zoomedMinTime > 0) {
                    //  if (this.isAtInitialZoom) {
                    //      this.isAtInitialZoom = false;
                    // } else {
                    _this.cancelPreviousOnZoomed();
                    _this.changeZoomPromise = $timeout(_this.changeZoom, _this.ZOOM_DELAY);
                    // }
                }
            });
            //Load the page
            this.init();
            $scope.$on('$destroy', function () {
                _this.clearRefreshInterval();
                _this.cancelPreviousOnZoomed();
            });
        } // constructor ends here
        return controller;
    }());
    exports.default = controller;
    angular.module(module_name_1.moduleName)
        .service('ProvenanceEventStatsService', ['$http', '$q', 'OpsManagerRestUrlService', ProvenanceEventStatsService_1.default])
        .service('OpsManagerFeedService', ['$q', '$http', '$interval', '$timeout', 'HttpService', 'IconService', 'AlertsService', 'OpsManagerRestUrlService', OpsManagerFeedService_1.default])
        .service('Nvd3ChartService', ["$timeout", "$filter", Nvd3ChartService_1.default])
        .controller('FeedStatsChartsController', ["$scope", "$element", "$http", "$interval", "$timeout", "$q", "$mdToast",
        "ProvenanceEventStatsService", "FeedStatsService", "Nvd3ChartService", "OpsManagerFeedService",
        "StateService", "$filter", controller]);
    angular.module(module_name_1.moduleName)
        .directive('kyloFeedStatsCharts', [
        function () {
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
            };
        }
    ]);
});
//# sourceMappingURL=feed-stats-charts.js.map