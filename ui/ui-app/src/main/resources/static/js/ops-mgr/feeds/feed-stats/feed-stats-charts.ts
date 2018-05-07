import * as angular from 'angular';
import {moduleName} from "./module-name";
//import OpsManagerFeedService from "../../services/OpsManagerFeedService";
//import Nvd3ChartService from "../../services/Nvd3ChartService";
//import {FeedStatsService} from "./FeedStatsService"
const d3 = require('d3');
import * as _ from "underscore";
import * as moment from "moment";

export default class controller{


    dataLoaded:boolean = false;
    /** flag when processor chart is loading **/
    processChartLoading:boolean = false;
    /**
     * the last time the data was refreshed
     * @type {null}
     */
    lastProcessorChartRefresh: any = null;
    /**
     * last time the execution graph was refreshed
     * @type {null}
     */
    lastFeedTimeChartRefresh:any = null;

    /** flag when the feed time chart is loading **/
    showFeedTimeChartLoading:boolean = false;

    showProcessorChartLoading:boolean = false;

    statusPieChartApi:any = {};

    /**
     * Initial Time Frame setting
     * @type {string}
     */
     timeFrame:string = 'FIVE_MIN';
    /**
     * Array of fixed times
     * @type {Array}
     */
    timeframeOptions:any[] = [];
    /**
     * last time the page was refreshed
     * @type {null}
     */
    lastRefreshTime:any = null;
    /**
     * map of the the timeFrame value to actual timeframe object (i.e. FIVE_MIN:{timeFrameObject})
     * @type {{}}
     */
    timeFramOptionsLookupMap:any = {};
    /**
     * The selected Time frame
     * @type {{}}
     */
    selectedTimeFrameOptionObject:any = {};
    /**
     * Flag to enable disable auto refresh
     * @type {boolean}
     */
    autoRefresh:boolean = true;

    /**
     * Flag to indicate if we are zoomed or not
     * @type {boolean}
     */
     isZoomed:boolean = false;

    /**
     * Zoom helper
     * @type {boolean}
     */
     isAtInitialZoom:boolean = true;

    /**
     * Difference in overall min/max time for the chart
     * Used to help calcuate the correct xXais label (i.e. for larger time periods show Date + time, else show time
     * @type {null}
     */
    timeDiff:number = null;

    /**
     * millis to wait after a zoom is complete to update the charts
     * @type {number}
     */
     ZOOM_DELAY:number = 700;

    /**
     * Constant set to indicate we are not zoomed
     * @type {number}
     */
    UNZOOMED_VALUE:number = -1;

    /**
     * After a chart is rendered it will always call the zoom function.
     * Flag to prevent the initial zoom from triggering after refresh of the chart
     * @type {boolean}
     */
    preventZoomChange:boolean = false;

    /**
     * Timeout promise to prevent zoom
     * @type {undefined}
     */
    preventZoomPromise:angular.IPromise<any> = undefined;

    /**
     * The Min Date of data.  This will be the zoomed value if we are zooming, otherwise the min value in the dataset
     */
    minDisplayTime:any;

    /**
     * The max date of the data.  this will be the zoomed value if zooming otherwise the max value in the dataset
     */
    maxDisplayTime:any;

    /**
     * max Y value (when not zoomed)
     * @type {number}
     */
    maxY:number = 0;

    minY:number = 0;

    /**
     * max Y Value when zoomed
     * @type {number}
     */
    zoomMaxY:number = 0;

    zoomMinY:number = 0;

    /**
     * Min time frame to enable zooming.
     * Defaults to 30 min.
     * Anything less than this will not be zoomable
     * @type {number}
     */
    minZoomTime:number = 1000*60*30;
    /**
     * Flag to indicate if zooming is enabled.
     * Zooming is only enabled for this.minZoomTime or above
     *
     * @type {boolean}
     */
    zoomEnabled:boolean = false;

    /**
     * A bug in nvd3 charts exists where if the zoom is toggle to true it requires a force of the x axis when its toggled back to false upon every data refresh.
     * this flag will be triggered when the zoom enabled changes and from then on it will manually reset the x domain when the data refreshes
     * @type {boolean}
     */
    forceXDomain:boolean = false;

    /**
     * Flag to force the rendering of the chart to refresh
     * @type {boolean}
     */
    forceChartRefresh:boolean = false;


    /**
     * Summary stats should come from the service
     * @type {*}
     */
    summaryStatistics:any;

    feedChartLegendState:any[] = [];
    feedChartData:any[] = [];
    feedChartApi:any = {};
    feedChartOptions:any = {};

    processorChartApi:any = {};
    processorChartData:any[] = [];
    processorChartOptions:any = {};
    selectedProcessorStatisticFunction:string = 'Average Duration';
    processorStatsFunctions:any;

    /**
     * The Feed we are looking at
     * @type {{displayStatus: string}}
     */
    feed:any  = {
        displayStatus: ''
    };

    /**
     * Latest summary stats
     * @type {{}}
     */
    summaryStatsData:any = {};

    eventSuccessKpi:any = {
        value: 0,
        icon: '',
        color: ''
    };

    flowRateKpi:any = {
        value: 0,
        icon: 'tune',
        color: '#1f77b4'
    };

    avgDurationKpi:any = {
        value: 0,
        icon: 'access_time',
        color: '#1f77b4'
    };

    /**
     * Errors for th error table (if any)
     * @type {*}
     */
    feedProcessorErrorsTable:any = {
        sortOrder: '-errorMessageTimestamp',
        filter: '',
        rowLimit: 5,
        page: 1
    };

    feedProcessorErrors: any;
    zoomedMinTime: any;
    zoomedMaxTime: any;
    changeZoomPromise: any;
    minTime: any;
    maxTime: any;
    timeFrameOptions: any;
    timeFrameOptionIndex: any;
    displayLabel: string;
    feedTimeChartLoading: any;
    feedProcessorErrorsLoading: any;
    feedName: string;
    refreshInterval: angular.IPromise<any>;
    refreshIntervalTime: number;
    timeFrameOptionIndexLength: any;
    
    constructor(private $scope: angular.IScope,
                private $element: angular.IAugmentedJQuery,
                private $http: angular.IHttpService,
                private $interval: angular.IIntervalService,
                private $timeout: angular.ITimeoutService,
                private $q: angular.IQService,
                private $mdToast: angular.material.IToastService,
                private ProvenanceEventStatsService: any,
                private FeedStatsService: any,
                private Nvd3ChartService: any,
                private OpsManagerFeedService: any,
                private StateService: any,
                private $filter: angular.IFilterService){


        this.showFeedTimeChartLoading = true;

        this.showProcessorChartLoading = true;

        this.summaryStatistics = FeedStatsService.summaryStatistics;
        this.processorStatsFunctions = FeedStatsService.processorStatsFunctions();
        this.feedProcessorErrors = FeedStatsService.feedProcessorErrors;




    }
        /**
         * When a user clicks the Refresh Button
         */
        onRefreshButtonClick () {
            this.refresh();
        };

        
        /**
         * Navigate to the Feed Manager Feed Details
         * @param ev
         */
        gotoFeedDetails(ev: any){
            if (this.feed.feedId != undefined) {
                this.StateService.FeedManager().Feed().navigateToFeedDetails(this.feed.feedId);
            }
        };

        /**
         * Show detailed Errors
         */
        viewNewFeedProcessorErrors (){
            this.feedProcessorErrors.viewAllData();
        };

        toggleFeedProcessorErrorsRefresh(autoRefresh: boolean) {
            if (autoRefresh) {
                this.feedProcessorErrors.viewAllData();
                this.feedProcessorErrors.autoRefreshMessage = 'enabled';
            }
            else {
                this.feedProcessorErrors.autoRefreshMessage = 'disabled';
            }
        };

        /**
         * Called when a user click on the Reset Zoom button
         */
        onResetZoom(){
            if(this.isZoomed) {
                this.initiatePreventZoom();
                this.resetZoom();
                this.feedChartOptions.chart.xDomain = [this.minTime,this.maxTime]
                this.feedChartOptions.chart.yDomain = [this.minY,this.maxY]
                this.feedChartApi.refresh();
                this.buildProcessorChartData();
            }
        }

        /**
         * prevent the initial zoom to fire in chart after reload
         */
        initiatePreventZoom(){
            var cancelled = false;
            if(angular.isDefined(this.preventZoomPromise)) {
               this.$timeout.cancel(this.preventZoomPromise);
                this.preventZoomPromise = undefined;
                cancelled =true;
            }
                    if(!this.preventZoomChange || cancelled) {
                        this.preventZoomChange = true;
                         this.preventZoomPromise =   this.$timeout(()=> {
                         this.preventZoomChange = false;
                         this.preventZoomPromise = undefined;
                        }, 1000);
                    }
        }


    /**
     * Help adjust the x axis label depending on time window
     * @param d
     */
    private timeSeriesXAxisLabel(d:number){
        var maxTime = 1000*60*60*12; //12 hrs
        if(this.timeDiff >=maxTime ){
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
    private canZoom(xDomain:number[], yDomain:number[]) {

        var diff = this.maxTime - this.minTime;

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

    };


        /**
         * Initialize the Charts
         */
        setupChartOptions() {
            let self = this;
            this.processorChartOptions = {
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
                    x: (d: any)=> {
                        return d.label.length > 60 ? d.label.substr(0, 60) + "..." : d.label;
                    },
                    y: (d: any)=> {
                        return d.value;
                    },
                    showControls: false,
                    showValues: true,
                    xAxis: {
                        showMaxMin: false
                    },
                    interactiveLayer: {tooltip: {gravity: 's'}},
                    yAxis: {
                        axisLabel: self.FeedStatsService.processorStatsFunctionMap[self.selectedProcessorStatisticFunction].axisLabel,
                        tickFormat: (d: any)=> {
                            return d3.format(',.2f')(d);
                        }
                    },
                    valueFormat: (d: any)=> {
                        return d3.format(',.2f')(d);
                    },
                     noData: self.$filter('translate')('view.feed-stats-charts.noData')
                }
            }


            this.feedChartOptions = {
                chart: {
                    type: 'lineChart',
                    height: 450,
                    margin: {
                        top: 10,
                        right: 20,
                        bottom: 110,
                        left: 65
                    },
                    x: (d: any)=> {
                        return d[0];
                    },
                    y: (d: any)=> {
                        return d3.format('.2f')(d[1]);
                    },
                    showTotalInTooltip: true,
                    interpolate: 'linear',
                    useVoronoi: false,
                    duration: 250,
                    clipEdge:false,
                    useInteractiveGuideline: true,
                    interactiveLayer: {tooltip: {gravity: 's'}},
                    valueFormat: (d: any)=> {
                        return d3.format(',')(parseInt(d))
                    },
                    xAxis: {
                        axisLabel: self.$filter('translate')('view.feed-stats-charts.Time'),
                        showMaxMin: false,
                        tickFormat: (d:number) =>self.timeSeriesXAxisLabel(d),
                        rotateLabels: -45
                    },
                    yAxis: {
                        axisLabel: this.$filter('translate')('view.feed-stats-charts.FPS'),
                        axisLabelDistance: -10
                    },
                    legend: {
                        dispatch: {
                            stateChange: (e: any)=>{
                                self.feedChartLegendState = e.disabled;
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
                        zoomed: (xDomain: any, yDomain: any)=> {
                            //zoomed will get called initially (even if not zoomed)
                            // because of this we need to check to ensure the 'preventZoomChange' flag was not triggered after initially refreshing the dataset
                            if(!self.preventZoomChange) {
                                self.isZoomed = true;
                                if(self.canZoom(xDomain,yDomain)) {
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
                        unzoomed: (xDomain: any, yDomain: any)=> {
                            return self.resetZoom();
                        }
                        },
                     interactiveLayer2: { //interactiveLayer
                        dispatch: {
                            elementClick:  (t: any, u: any)=> {}
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
        resetZoom(){
            if(this.isZoomed) {
                this.isZoomed = false;
                this.zoomedMinTime = this.UNZOOMED_VALUE;
                this.zoomedMaxTime = this.UNZOOMED_VALUE;
                this.minDisplayTime=this.minTime;
                this.maxDisplayTime =this.maxTime;
                this.timeDiff = this.maxTime - this.minTime;
                return {x1: this.minTime, x2: this.maxTime, y1: this.minY, y2: this.maxY}
            }
        }


        changeZoom(){
            this.timeDiff = this.zoomedMaxTime- this.zoomedMinTime;
            this.autoRefresh = false;
            this.isZoomed = true;
            this.isAtInitialZoom = true;

        //    FeedStatsService.setTimeBoundaries(this.minTime, this.maxTime);
            this.buildProcessorChartData();
            this.minDisplayTime=this.zoomedMinTime;
            this.maxDisplayTime =this.zoomedMaxTime

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





        }

        /**
         * Cancel the zoom timeout watcher
         */
        cancelPreviousOnZoomed(){
            if (!_.isUndefined(this.changeZoomPromise)) {
                this.$timeout.cancel(this.changeZoomPromise);
                this.changeZoomPromise = undefined;
            }
        }


        onTimeFrameChanged(){
            if (!_.isUndefined(this.timeFrameOptions)) {
                this.timeFrame = this.timeFrameOptions[Math.floor(this.timeFrameOptionIndex)].value;
                this.displayLabel = this.timeFrameOptions[Math.floor(this.timeFrameOptionIndex)].label;
                this.isZoomed = false;
                this.zoomedMinTime = this.UNZOOMED_VALUE;
                this.zoomedMaxTime = this.UNZOOMED_VALUE;
                this.initiatePreventZoom();
                this.onTimeFrameChanged2(this.timeFrame);

            }
        }

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

      

        refresh(){
            var to = new Date().getTime();
            var millis = this.timeFrameOptions[this.timeFrameOptionIndex].properties.millis;
            var from = to - millis;
            this.minDisplayTime = from;

            this.maxDisplayTime = to;

            this.FeedStatsService.setTimeBoundaries(from, to);
            this.buildChartData(true);
        }

        enableZoom(){
            this.zoomEnabled = true;
            this.feedChartOptions.chart.zoom.enabled=true;
            this.forceChartRefresh = true;
            this.forceXDomain = true;

        }

        disableZoom(){
            this.resetZoom();
            this.zoomEnabled = false;
            this.feedChartOptions.chart.zoom.enabled=false;
            this.forceChartRefresh = true;
        }

    /**
     * When a user changes the Processor drop down
     * @type {onProcessorChartFunctionChanged}
     */
        onProcessorChartFunctionChanged(){
            this.FeedStatsService.setSelectedChartFunction(this.selectedProcessorStatisticFunction);
            var chartData = this.FeedStatsService.changeProcessorChartDataFunction(this.selectedProcessorStatisticFunction);
            this.processorChartData[0].values = chartData.data;
            this.FeedStatsService.updateBarChartHeight(this.processorChartOptions, this.processorChartApi, chartData.data.length, this.selectedProcessorStatisticFunction);
        }

        buildChartData(timeIntervalChange: boolean){
            if (!this.FeedStatsService.isLoading()) {
                timeIntervalChange = angular.isUndefined(timeIntervalChange) ? false : timeIntervalChange;
                this.feedTimeChartLoading = true;
                this.processChartLoading = true;
                this.buildProcessorChartData();
                this.buildFeedCharts();
                this.fetchFeedProcessorErrors(timeIntervalChange);
            }
            this.getFeedHealth();
        }

        updateSuccessEventsPercentKpi() {
            if (this.summaryStatsData.totalEvents == 0) {
                this.eventSuccessKpi.icon = 'remove';
                this.eventSuccessKpi.color = "#1f77b4"
                this.eventSuccessKpi.value = "--";
            }
            else {
                var failed = this.summaryStatsData.totalEvents > 0 ? (<any>(this.summaryStatsData.failedEvents / this.summaryStatsData.totalEvents)).toFixed(2) * 100 : 0;
                var value = (100 - failed).toFixed(0);
                var icon = 'offline_pin';
                var iconColor = "#3483BA"

                this.eventSuccessKpi.icon = icon;
                this.eventSuccessKpi.color = iconColor;
                this.eventSuccessKpi.value = value

            }
        }

        updateFlowRateKpi() {
            this.flowRateKpi.value = this.summaryStatistics.flowsStartedPerSecond;
        }

        updateAvgDurationKpi(){
            var avgMillis = this.summaryStatistics.avgFlowDurationMilis;
            this.avgDurationKpi.value = DateTimeUtils(this.$filter('translate')).formatMillisAsText(avgMillis,false,true);
        }

        formatSecondsToMinutesAndSeconds(s:number) {   // accepts seconds as Number or String. Returns m:ss
            return ( s - ( s %= 60 )) / 60 + (9 < s ? ':' : ':0' ) + s;
        }

        updateSummaryKpis() {
            this.updateFlowRateKpi();
            this.updateSuccessEventsPercentKpi();
            this.updateAvgDurationKpi();
        }

        buildProcessorChartData() {
            var values = [];
            this.processChartLoading = true;
            var minTime = undefined;
            var maxTime = undefined;
            if(this.isZoomed && this.zoomedMinTime != this.UNZOOMED_VALUE) {
                //reset x xaxis to the zoom values
                minTime=this.zoomedMinTime;
                maxTime =this.zoomedMaxTime
            }
            this.$q.when( this.FeedStatsService.fetchProcessorStatistics(minTime,maxTime)).then((response: any)=> {
                this.summaryStatsData = this.FeedStatsService.summaryStatistics;
                this.updateSummaryKpis();
                this.processorChartData = this.FeedStatsService.buildProcessorDurationChartData();

                this.FeedStatsService.updateBarChartHeight(this.processorChartOptions, this.processorChartApi, this.processorChartData[0].values.length, this.selectedProcessorStatisticFunction);
                this.processChartLoading = false;
                this.lastProcessorChartRefresh = new Date().getTime();
                this.lastRefreshTime = new Date();
            },  ()=> {
                this.processChartLoading = false;
                this.lastProcessorChartRefresh = new Date().getTime();
            });
        }

        buildFeedCharts() {

            this.feedTimeChartLoading = true;
            this.$q.when( this.FeedStatsService.fetchFeedTimeSeriesData()).then( (feedTimeSeries: any)=> {

                this.minTime = feedTimeSeries.time.startTime;
                this.maxTime = feedTimeSeries.time.endTime;
                this.timeDiff = this.maxTime - this.minTime;

                var chartArr = [];
                chartArr.push({
                    label: this.$filter('translate')('view.feed-stats-charts.Completed'), color: '#3483BA', valueFn: function (item: any) {
                            return item.jobsFinishedPerSecond;
                    }
                });
                chartArr.push({
                    label: this.$filter('translate')('view.feed-stats-charts.Started'), area: true, color: "#F08C38", valueFn: function (item: any) {
                            return item.jobsStartedPerSecond;
                    }
                });
                //preserve the legend selections
                if ( this.feedChartLegendState.length > 0) {
                    _.each(chartArr, (item:any, i: any)=> {
                        item.disabled =  this.feedChartLegendState[i];
                    });
                }

                this.feedChartData = this.Nvd3ChartService.toLineChartData(feedTimeSeries.raw.stats, chartArr, 'minEventTime', null,this.minTime, this.maxTime);
                var max = this.Nvd3ChartService.determineMaxY(this.feedChartData);
                if(this.isZoomed) {
                    max = this.zoomMaxY;
                }
                var maxChanged =  this.maxY < max;
                this.minY = 0;
                this.maxY = max;
                if(max <5){
                    max = 5;
                }


                this.feedChartOptions.chart.forceY = [0, max];
                if (this.feedChartOptions.chart.yAxis.ticks != max) {
                    this.feedChartOptions.chart.yDomain = [0, max];
                    var ticks = max;
                    if (ticks > 8) {
                        ticks = 8;
                    }
                    if(angular.isUndefined(ticks) || ticks <5){
                        ticks = 5;
                    }
                    this.feedChartOptions.chart.yAxis.ticks = ticks;
                }

                if(this.isZoomed && (this.forceXDomain == true || this.zoomedMinTime !=this.UNZOOMED_VALUE)) {
                    //reset x xaxis to the zoom values
                    this.feedChartOptions.chart.xDomain = [this.zoomedMinTime,this.zoomedMaxTime]
                    var y = this.zoomMaxY > 0 ? this.zoomMaxY : this.maxY;
                    this.feedChartOptions.chart.yDomain = [0,y]
                }
                else  if(!this.isZoomed && this.forceXDomain){
                    this.feedChartOptions.chart.xDomain = [this.minTime,this.maxTime];
                    this.feedChartOptions.chart.yDomain = [0,this.maxY]
                }

                this.initiatePreventZoom();
                if (this.feedChartApi && this.feedChartApi.refresh  && this.feedChartApi.update) {
                      if(maxChanged || this.forceChartRefresh ) {
                          this.feedChartApi.refresh();
                          this.forceChartRefresh = false;
                      }
                      else {
                            this.feedChartApi.update();
                      }
                }

                this.feedTimeChartLoading = false;
                this.lastFeedTimeChartRefresh = new Date().getTime();
            }, ()=> {
                this.feedTimeChartLoading = false;
                this.lastFeedTimeChartRefresh = new Date().getTime();
            });

        }

        /**
         * fetch and append the errors to the FeedStatsService.feedProcessorErrors.data object
         * @param resetWindow optionally reset the feed errors to start a new array of errors in the feedProcessorErrors.data
         */
        fetchFeedProcessorErrors(resetWindow: any) {
            this.feedProcessorErrorsLoading = true;
            this.$q.when(this.FeedStatsService.fetchFeedProcessorErrors(resetWindow)).then((feedProcessorErrors: any)=> {
                this.feedProcessorErrorsLoading = false;
            }, (err: any)=> {
                this.feedProcessorErrorsLoading = false;
            });

        }

        /**
         * Gets the Feed Health
         */
        getFeedHealth(){
            var successFn = (response: any)=> {
                if (response.data) {
                    //transform the data for UI
                    if (response.data.feedSummary) {
                        angular.extend(this.feed, response.data.feedSummary[0]);
                        this.feed.feedId = this.feed.feedHealth.feedId;
                        if (this.feed.running) {
                            this.feed.displayStatus = 'RUNNING';
                        }
                        else {
                            this.feed.displayStatus = 'STOPPED';
                        }
                    }

                }
            }
            var errorFn = (err: any)=> {
            }

            this.$http.get(this.OpsManagerFeedService.SPECIFIC_FEED_HEALTH_URL(this.feedName)).then(successFn, errorFn);
        }


        clearRefreshInterval() {
            if (this.refreshInterval != null) {
                this.$interval.cancel(this.refreshInterval);
                this.refreshInterval = null;
            }
        }

        setRefreshInterval() {
            this.clearRefreshInterval();

            if (this.autoRefresh ) {
                // anything below 5 minute interval to be refreshed every 5 seconds,
                // anything above 5 minutes to be refreshed in proportion to its time span, i.e. the longer the time span the less it is refreshed
                var option = this.timeFramOptionsLookupMap[this.timeFrame];
                if (!_.isUndefined(option)) {
                    //timeframe option will be undefined when page loads for the first time
                    var refreshInterval = option.properties.millis / 60;
                    this.refreshIntervalTime = refreshInterval < 5000 ? 5000 : refreshInterval;
                }
                if (this.refreshIntervalTime) {
                    this.refreshInterval = this.$interval(()=> {
                        this.refresh();
                        }, this.refreshIntervalTime
                    );
                }
            }
        }


        /**
         * Initialize the charts
         */
        initCharts() {
            this.FeedStatsService.setFeedName(this.feedName);
            this.setupChartOptions();
            this.onRefreshButtonClick();
            this.dataLoaded = true;
        }

        /**
         * Fetch and load the Time slider options
         */
        loadTimeFrameOption() {
            this.ProvenanceEventStatsService.getTimeFrameOptions().then((response: any)=> {
                this.timeFrameOptions = response.data;
                this.timeFrameOptionIndexLength = this.timeFrameOptions.length;
                _.each(response.data, (labelValue: any)=> {
                    this.timeFramOptionsLookupMap[labelValue.value] = labelValue;
                });
                this.$timeout(()=> {
                    //update initial slider position in UI
                    this.timeFrameOptionIndex = _.findIndex(this.timeFrameOptions,  (option: any)=> {
                        return option.value === this.timeFrame;
                    });
                    this.initCharts();
                }, 1);
            });
        }


    /**
     * When the controller is ready, initialize
     */
    $onInit(): void {
        /**
         * Enable/disable the refresh interval
         */
        this.$scope.$watch(
            ()=> {
                return this.autoRefresh;
            },
            (newVal: any, oldVal: any)=> {
                if (!this.autoRefresh) {
                    this.clearRefreshInterval();
                    //toast
                    this.$mdToast.show(
                        this.$mdToast.simple()
                            .textContent('Auto refresh disabled')
                            .hideDelay(3000)
                    );
                } else {
                    this.setRefreshInterval();
                    this.$mdToast.show(
                        this.$mdToast.simple()
                            .textContent('Auto refresh enabled')
                            .hideDelay(3000)
                    );
                }
            }
        );

        /**
         * Watch when a zoom is active.
         */
        this.$scope.$watch(
            ()=> {
                return this.zoomedMinTime;
            },
            (newVal: any, oldVal: any)=> {
                if (!_.isUndefined(this.zoomedMinTime) && this.zoomedMinTime > 0) {
                    //  if (this.isAtInitialZoom) {
                    //      this.isAtInitialZoom = false;
                    // } else {
                    this.cancelPreviousOnZoomed();
                    this.changeZoomPromise = this.$timeout(this.changeZoom, this.ZOOM_DELAY);
                    // }
                }
            }
        );


        this.$scope.$on('$destroy',  ()=> {
            this.clearRefreshInterval();
            this.cancelPreviousOnZoomed();
        });
        this.loadTimeFrameOption();
    }


                /**
         * When the slider is changed refresh the charts/data
         * @param timeFrame
         */
        onTimeFrameChanged2(timeFrame?: any) {
            if(this.isZoomed){
                this.resetZoom();
            }
            this.isAtInitialZoom = true;
            this.timeFrame = timeFrame;
            var millis = this.timeFrameOptions[this.timeFrameOptionIndex].properties.millis;
            if(millis >= this.minZoomTime){
              this.enableZoom();
            }
            else {
                this.disableZoom();
            }
            this.clearRefreshInterval();
            this.refresh();

            //disable refresh if > 30 min timeframe
            if(millis >(1000*60*30)){
                this.autoRefresh = false;
            }
            else {
                if(!this.autoRefresh) {
                    this.autoRefresh = true;
                }
                else {
                    this.setRefreshInterval();

                }
            }


        }
}


angular.module(moduleName)
 .controller('FeedStatsChartsController',
        ["$scope", "$element", "$http", "$interval", "$timeout", "$q","$mdToast", 
        "ProvenanceEventStatsService", "FeedStatsService", "Nvd3ChartService", "OpsManagerFeedService", 
        "StateService", "$filter", controller]);

    angular.module(moduleName)
        .directive('kyloFeedStatsCharts', [
            ()=> {
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

            }
        ]);