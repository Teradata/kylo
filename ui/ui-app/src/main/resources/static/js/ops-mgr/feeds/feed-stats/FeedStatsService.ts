///<reference path="../../../common/utils/ArrayUtils.ts"/>
import * as angular from 'angular';
import {moduleName} from "./module-name";
import "pascalprecht.translate";
import ProvenanceEventStatsService from "../../services/ProvenanceEventStatsService";
import * as _ from "underscore";
const d3 = require('d3');

export class FeedStatsService {

    DEFAULT_CHART_FUNCTION: string = 'Average Duration';
    processorStatsFunctionMap: any;
    fromMillis: any;
    toMillis: any;
    feedName: string = '';
    processorStatistics: any;       
    summaryStatistics: any;
    nullVar: any | null;
    loadingFeedTimeSeriesData:boolean = false;
    loadingProcessorStatistics:boolean = false;
    feedTimeSeries:any;
    feedProcessorErrors:any;
    lastSummaryStats:any[] = [];
    keepLastSummary:number = 20;

    constructor(private $q: any,
        private ProvenanceEventStatsService: any){
            this.init();
    }

    init() {
        this.DEFAULT_CHART_FUNCTION = 'Average Duration';
        this.processorStatsFunctionMap = {
            'Average Duration': {
                axisLabel: 'Time (sec)', fn: (stats: any) => {
                    return (stats.duration / stats.totalCount) / 1000
                }
            },
            'Bytes In': {
                axisLabel: 'Bytes', valueFormatFn: (d: any) => {
                    return this.bytesToString(d);
                }, fn: (stats: any) => {
                    return stats.bytesIn
                }
            },
            'Bytes Out': {
                axisLabel: 'Bytes', valueFormatFn: (d: any) => {
                    return this.bytesToString(d);
                }, fn: function (stats: any) {
                    return stats.bytesOut
                }
            }, /*
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
                axisLabel: 'Count', valueFormatFn: (d: any) => {
                    return d3.format(',')(parseInt(d))
                }, fn: (stats: any) => {
                    return stats.jobsStarted != undefined ? stats.jobsStarted : 0;
                }
            },
            'Flows Finished': {
                axisLabel: 'Count', valueFormatFn: (d: any) => {
                    return d3.format(',')(parseInt(d))
                }, fn: (stats: any) => {
                    return stats.jobsFinished != undefined ? stats.jobsFinished : 0;
                }
            },
            'Total Events': {
                axisLabel: 'Count', valueFormatFn: (d: any) => {
                    return d3.format(',')(parseInt(d))
                }, fn: (stats: any) => {
                    return stats.totalCount
                }
            },
            'Failed Events': {
                axisLabel: 'Count', valueFormatFn: (d: any) => {
                    return d3.format(',')(parseInt(d))
                }, fn: (stats: any) => {
                    return stats.failedCount
                }
            }
        };



        this.processorStatistics = {
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
        }

        this.summaryStatistics = {
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
        }

        this.loadingFeedTimeSeriesData = false;

        this.feedTimeSeries = {
            lastRefreshTime: '',
            time: {startTime: 0, endTime: 0},
            minTime: 0,
            maxTime: 0,
            raw: [],
            chartData: []
        }

        this.feedProcessorErrors= {
            time: {startTime: 0, endTime: 0},
            autoRefresh:true,
            autoRefreshMessage:"enabled",
            allData:[],
            visibleData: [],
            newErrorCount:0,
            viewAllData:function(){

                var index = this.visibleData.length;
                if(this.allData.length > index) {
                    var rest = _.rest(this.allData,index);
                    _.each(rest, (item) =>{
                        this.visibleData.push(item);
                    });
                    this.newErrorCount = 0;
                }
            }
        }
    }





    setTimeBoundaries(from:any, to: any) :void {
        this.fromMillis = from;
        this.toMillis = to;
    }

            setFeedName(feedName: any) {
                this.feedName = feedName;
            }

            setSelectedChartFunction (selectedChartFunction: any) {
                this.processorStatistics.selectedChartFunction = selectedChartFunction
            }

            processorStatsFunctions() {
                return Object.keys(this.processorStatsFunctionMap);
            }

            isLoading(){
                return this.loadingProcessorStatistics || this.loadingFeedTimeSeriesData;
            }

            fetchProcessorStatistics(minTime: any, maxTime: any) {
                this.loadingProcessorStatistics = true;
                var deferred = this.$q.defer();

                var selectedChartFunction = this.processorStatistics.selectedChartFunction;

                if(angular.isUndefined(minTime)){
                    minTime = this.fromMillis;
                }
                if(angular.isUndefined(maxTime)){
                    maxTime = this.toMillis;
                }
                this.$q.when(this.ProvenanceEventStatsService.getFeedProcessorDuration(this.feedName, minTime,maxTime)).then((response: any) => {
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
                    var chartData = {};
                    var processorDuration: any[] = [];

                    var chartDataCallbackFunction = (key: any, p: any) =>{
                        flowsStarted += p.jobsStarted;
                        flowsFinished += p.jobsFinished;
                        flowDuration += p.jobDuration;
                        flowsFailed += p.jobsFailed;
                        totalEvents += p.totalCount;
                        failedEvents += p.failedCount;
                        flowsSuccess = (flowsFinished - flowsFailed);
                        var duration = this.processorStatsFunctionMap['Average Duration'].fn(p);
                        processorDuration.push({label: key, value: duration});
                    }

                    chartData = this.getChartData(processorStatsContainer, selectedChartFunction, chartDataCallbackFunction);

                    var topNProcessorDurationData = _.sortBy(processorDuration, 'value').reverse();
                    topNProcessorDurationData = _.first(topNProcessorDurationData, this.processorStatistics.topN);

                    var timeDiffMillis = processorStatsContainer.endTime - processorStatsContainer.startTime;
                    var secondsDiff = timeDiffMillis / 1000;
                    var flowsStartedPerSecondStr = secondsDiff > 0 ? ((flowsStarted / secondsDiff)).toFixed(2) : 0;

                    this.processorStatistics.raw = processorStatsContainer;
                    var summary = this.summaryStatistics;

                    summary.time.startTime = processorStatsContainer.startTime;
                    summary.time.endTime = processorStatsContainer.endTime;
                    summary.flowsStarted = flowsStarted;
                    summary.flowsFinished = flowsFinished;
                    summary.flowsFailed = flowsFailed;
                    summary.totalEvents = totalEvents;
                    summary.failedEvents = failedEvents;
                    summary.flowsSuccess = flowsSuccess;
                    summary.flowsStartedPerSecond = flowsStartedPerSecondStr != 0 ? parseFloat(flowsStartedPerSecondStr.toString()) : flowsStartedPerSecond;
                    summary.avgFlowDurationMilis = (flowsFinished > 0 ? (flowDuration / flowsFinished) : 0);
                    summary.avgFlowDuration = flowsFinished > 0 ? ((flowDuration / flowsFinished) / 1000).toFixed(2) : 0;

                    summary.avgFlowDuration = summary.avgFlowDuration != 0 ? parseFloat(summary.avgFlowDuration) : 0;

                    this.processorStatistics.chartData = chartData;

                    this.processorStatistics.topNProcessorDurationData = topNProcessorDurationData

                    var refreshTime = new Date().getTime();
                    this.processorStatistics.lastRefreshTime = refreshTime;
                    summary.lastRefreshTime = refreshTime;

                    var lastSummaryStats = angular.copy(this.summaryStatistics);
                    if (this.lastSummaryStats.length >= this.keepLastSummary) {
                        this.lastSummaryStats.shift();
                    }

                    this.lastSummaryStats.push({date: refreshTime, data: lastSummaryStats});

                    this.loadingProcessorStatistics = false;
                    deferred.resolve(this.processorStatistics);

                }, (err: any)=> {
                    this.loadingProcessorStatistics = false;
                    var refreshTime = new Date().getTime();
                    this.processorStatistics.lastRefreshTime = refreshTime;
                    this.summaryStatistics.lastRefreshTime = refreshTime;
                    deferred.reject(err);
                });
                return deferred.promise;
            }
            changeProcessorChartDataFunction (selectedChartFunction: any){
                var chartData = this.getChartData(this.processorStatistics.raw, selectedChartFunction);
                this.processorStatistics.chartData = chartData;
                return chartData;
            }
            updateBarChartHeight(chartOptions: any, chartApi: any, rows: any, chartFunction: any) {
                var configMap = this.processorStatsFunctionMap[chartFunction];

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
                    chartOptions.chart.valueFormat =  (d: any)=> {
                        return d3.format(',.2f')(d);
                    };

                    chartOptions.chart.yAxis.tickFormat = (d: any)=>{
                        return d3.format(',.2f')(d);
                    }
                }
                if (chartApi && chartApi.update) {
                    chartApi.update();
                    if(heightChanged) {
                        chartApi.refresh();
                    }
                }
            }
            buildProcessorDurationChartData() {
                var chartData = this.processorStatistics.chartData;
                var values = chartData.data;
                var data = [{key: "Processor", "color": "#F08C38", values: values}];
                return data;

            }
            buildStatusPieChart(){
                var statusPieChartData = [];
                statusPieChartData.push({key: "Successful Flows", value: this.summaryStatistics.flowsSuccess})
                statusPieChartData.push({key: "Running Flows", value: this.summaryStatistics.flowsRunning});
                return statusPieChartData;
            }

            buildEventsPieChart(){
                var eventsPieChartData = [];
                var success = this.summaryStatistics.totalEvents - this.summaryStatistics.failedEvents;
                eventsPieChartData.push({key: "Success", value: success})
                eventsPieChartData.push({key: "Failed", value: this.summaryStatistics.failedEvents})
                return eventsPieChartData;
            }
            
            emptyFeedTimeSeriesObject(eventTime: any, feedName: any){
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
                    "id":this.nullVar, // null,
                    "feedName": feedName,
                    "processorId":this.nullVar, // null,
                    "processorName": this.nullVar, // null,
                    "feedProcessGroupId": this.nullVar, // null,
                    "collectionTime": this.nullVar, // null,
                    "collectionId": this.nullVar, // null,
                    "resultSetCount": this.nullVar, // null,
                    "jobsStartedPerSecond": 0,
                    "jobsFinishedPerSecond": 0,
                    "collectionIntervalSeconds": this.nullVar, // null,
                }
            }

            fetchFeedTimeSeriesData() {
                var deferred = this.$q.defer();
                this.loadingFeedTimeSeriesData = true;
                this.$q.when(this.ProvenanceEventStatsService.getFeedStatisticsOverTime(this.feedName, this.fromMillis, this.toMillis)).then((response: any) => {

                    var statsContainer = response.data;
                    if (statsContainer.stats == null) {
                        statsContainer.stats = [];
                    }

                    var timeArr = _.map(statsContainer.stats, (item: any)=> {
                        return item.minEventTime != null ? item.minEventTime : item.maxEventTime;
                    });


                    this.summaryStatistics.flowsRunning = statsContainer.runningFlows;

                    this.feedTimeSeries.minTime = ArrayUtils.min(timeArr);
                    this.feedTimeSeries.maxTime = ArrayUtils.max(timeArr);

                    this.feedTimeSeries.time.startTime = statsContainer.startTime;
                    this.feedTimeSeries.time.endTime = statsContainer.endTime;
                    this.feedTimeSeries.raw = statsContainer;

                    this.loadingFeedTimeSeriesData = false;
                    this.feedTimeSeries.lastRefreshTime = new Date().getTime();
                    deferred.resolve(this.feedTimeSeries);
                }, (err: any) => {
                    this.loadingFeedTimeSeriesData = false;
                    this.feedTimeSeries.lastRefreshTime = new Date().getTime();
                    deferred.reject(err)
                });
                return deferred.promise;
            }

            fetchFeedProcessorErrors(resetWindow: any){
                var deferred = this.$q.defer();
                //reset the collection if we are looking for a new window
                if(resetWindow){
                    this.feedProcessorErrors.allData = [];
                    this.feedProcessorErrors.visibleData = [];
                    this.feedProcessorErrors.time.startTime = null;
                    this.feedProcessorErrors.newErrorCount = 0;

                }
                this.$q.when(this.ProvenanceEventStatsService.getFeedProcessorErrors(this.feedName, this.fromMillis, this.toMillis, this.feedProcessorErrors.time.endTime)).then((response: any) =>{


                    var container = response.data;
                    var addToVisible = this.feedProcessorErrors.autoRefresh || this.feedProcessorErrors.visibleData.length ==0;
                    if (container != null && container.errors != null) {
                        _.each(container.errors,  (error) => {
                            //append to errors list
                            this.feedProcessorErrors.allData.push(error);
                            if(addToVisible) {
                                this.feedProcessorErrors.visibleData.push(error);
                            }
                        });
                    }
                    this.feedProcessorErrors.newErrorCount = this.feedProcessorErrors.allData.length - this.feedProcessorErrors.visibleData.length;
                    this.feedProcessorErrors.time.startTime = container.startTime;
                    this.feedProcessorErrors.time.endTime = container.endTime;
                    deferred.resolve(container);
                }, (err: any)=> {

                    deferred.reject(err)
                });
                return deferred.promise;

            }

/**
 * Gets Chart Data using a function from the (processorStatsFunctionMap) above
 * @param functionName a name of the function
 * @return {{chartFunction: *, total: number, totalFormatted: number, data: Array}}
 */
private getChartData(rawData: any, functionName: any,callbackFn?: any) :any{
    var processorStats = rawData.stats;
    var values: any[] = [];
    var total = 0;
    var totalFormatted = 0;
    var chartFunctionData = this.processorStatsFunctionMap[functionName];
    if(chartFunctionData == null || chartFunctionData == undefined){
        functionName = this.DEFAULT_CHART_FUNCTION;
        chartFunctionData = this.processorStatsFunctionMap[functionName];
    }

    _.each(processorStats, (p: any)=> {
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

private bytesToString(bytes: any) {

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



    static factory() {
        let instance = ($q: angular.IQService, ProvenanceEventStatsService: any) =>
             new FeedStatsService($q,ProvenanceEventStatsService);

        return instance;
    }

}

angular.module(moduleName).factory('FeedStatsService', ["$q","ProvenanceEventStatsService",FeedStatsService.factory()]);
