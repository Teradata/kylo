import * as _ from "underscore";
import { HttpClient } from "@angular/common/http";
import {BroadcastService} from "../../../services/broadcast-service";
import {OpsManagerJobService} from "../../services/ops-manager-jobs.service";
import {OpsManagerDashboardService} from "../../services/OpsManagerDashboardService";
import {StateService} from "../../../services/StateService";
import { Component } from "@angular/core";
import { ObjectUtils } from "../../../../lib/common/utils/object-utils";
import {OpsManagerChartJobService} from "../../services/ops-manager-chart-job.service";
declare const d3: any;

@Component({
    selector: 'tba-job-status-indicator',
    templateUrl: './job-status-indicator-template.html'
})
export class JobStatusIndicatorComponent {

    refreshInterval: any = null;
    dataLoaded: boolean = false;
    chartApi: any ={};
    chartConfig: any = {};
    running: number = 0;
    failed: number = 0;
    chartData: any[] = [];
    runningCounts: any[] = [];
    maxDatapoints: number = 20;
    chartOptions: any;
    refreshIntervalTime: number=1000;

    ngOnInit() {

        this.chartOptions =  {
            chart: {
                type: 'lineChart',
                margin : {
                    top: 5,
                    right: 5,
                    bottom:10,
                    left: 20
                },
                x: (d: any)=>{return d[0];},
                y: (d: any)=>{return d[1];},
                useVoronoi: false,
                clipEdge: false,
                duration: 0,
                height:146,
                useInteractiveGuideline: true,
                xAxis: {
                    axisLabel: 'Time',
                    showMaxMin: false,
                    tickFormat: (d: any)=> {
                        return d3.time.format('%X')(new Date(d))
                    }
                },
                yAxis: {
                    axisLabel:'',
                    "axisLabelDistance": -10,
                    showMaxMin:false,
                    tickSubdivide:0,
                    ticks:1
                },
                yDomain:[0,1],
                showLegend:false,
                showXAxis:false,
                showYAxis:true,
                lines: {
                    dispatch: {
                        'elementClick':(e: any)=>{
                            this.chartClick();
                        }
                    }
                },
                dispatch: {

                }
            }
        };
if(this.refreshIntervalTime == undefined) {
    this.refreshIntervalTime = 1000;
}
        this.refresh();
        this.setRefreshInterval();

    }

    ngOnDestroy() {
        this.clearRefreshInterval();
    }

    constructor(
        private http: HttpClient,
        private StateService: StateService,
        private OpsManagerJobService: OpsManagerJobService,
        private OpsManagerDashboardService: OpsManagerDashboardService,
        private opsManagerChartJobService: OpsManagerChartJobService,
        private BroadcastService: BroadcastService) {}// end of constructor
     
        updateChart () {
            if(this.chartApi.update) {
                this.chartApi.update();
            }
        }

        chartClick () {
            this.StateService.OpsManager().Job().navigateToJobs("Running",null);
        }
        getRunningFailedCounts() {
                var successFn = (response: any)=> {
                    if(response){
                     this.updateCounts(response.data);
                        if(this.runningCounts.length >= this.maxDatapoints){
                            this.runningCounts.shift();
                            this.chartData[0].values.shift();
                        }
                        var dataItem = {status:'RUNNING_JOB_ACTIVITY',date:new Date().getTime(), count:this.running}
                        this.runningCounts.push(dataItem);
                        this.addChartData(dataItem);
                        this.dataLoaded = true;
                    }
                }

                var errorFn =  (data: any, status: any) =>{
                    console.log("Error getting count by status: ", data, status);
                }
                this.http.get(this.OpsManagerJobService.RUNNING_JOB_COUNTS_URL).toPromise().then(
                    (response : any) => {successFn(response)},
                    (error: any)=>{ errorFn(error, status)
                });

        }

        refresh () {
            this.getRunningFailedCounts();
        }

        updateCounts (responseData: any) {
            //zero them out
            this.running =0;
            this.failed = 0;
            if(responseData){
                responseData.forEach((statusCount: any,i: any)=>{
                    if(statusCount.status == 'RUNNING'){
                        this.running += statusCount.count;
                    }
                    else if(statusCount.status =='FAILED'){
                        this.failed += statusCount.count;
                    }
                });
                this.ensureFeedSummaryMatches(responseData);
            }
        }

        /**
         * Job Status Counts run every second.
         * Feed Healh/Running data runs every 5 seconds.
         * if the Job Status changes, update the summary data and notify the Feed Health Card
         * @param jobStatus the list of Job Status counts by feed
         */
        ensureFeedSummaryMatches (jobStatus: any) {
            var summaryData = this.OpsManagerDashboardService.feedSummaryData;
            var feedSummaryUpdated: any[] = [];
            var runningFeedNames: any[] = [];
            var notify = false;
            _.each(jobStatus,  (feedJobStatusCounts: any)=> {
                var feedSummary = summaryData[feedJobStatusCounts.feedName];
                if (ObjectUtils.isDefined(feedSummary)) {
                    var summaryState = feedSummary.state;
                    if (feedJobStatusCounts.status == "RUNNING") {
                        runningFeedNames.push(feedJobStatusCounts.feedName);
                    }
                    if (feedJobStatusCounts.status == "RUNNING" && summaryState != "RUNNING") {
                        //set it
                        feedSummary.state = "RUNNING"
                        feedSummary.runningCount = summaryData.count
                        //trigger update of feed summary
                        feedSummaryUpdated.push(feedSummary);
                        notify = true;
                    }
                }
            });
            //any of those that are not in the runningFeedNames are not running anymore
            var notRunning = _.difference(Object.keys(summaryData), runningFeedNames);
            _.each(notRunning,  (feedName: any)=> {
                var summary = summaryData[feedName];
                if (summary && summary.state == "RUNNING") {
                    summary.state = "WAITING";
                    summary.runningCount = 0;
                    feedSummaryUpdated.push(summary);
                    notify = true;
                }
            })

            if (notify = true) {
                this.BroadcastService.notify(this.OpsManagerDashboardService.FEED_SUMMARY_UPDATED,
                                         feedSummaryUpdated);
            }
        }


        addChartData (data: any) {
            if(this.chartData.length >0) {
                this.chartData[0].values.push([data.date, data.count]);
            }
            else {
               var initialChartData = this.opsManagerChartJobService.toChartData([data]);
                initialChartData[0].key = 'Running';
                this.chartData = initialChartData;
            }
            var max = d3.max(this.runningCounts, (d: any) =>{
                return d.count; } );
            if(max == undefined || max ==0) {
                max = 1;
            }
            else {
                max +=1;
            }
            if(this.chartOptions.chart.yAxis.ticks != max) {
                this.chartOptions.chart.yDomain = [0, max];
                var ticks = max;
                if(ticks > 8){
                    ticks = 8;
                }
                this.chartOptions.chart.yAxis.ticks = ticks;
            }
        }
        createChartData (responseData: any) {
            this.chartData = this.opsManagerChartJobService.toChartData(responseData);
            var max = d3.max(this.runningCounts, (d: any)=>{
                return d.count; } );
            if(max == undefined || max ==0) {
                max = 1;
            }
            else {
                max +=1;
            }
            this.chartOptions.chart.yDomain = [0, max];
            this.chartOptions.chart.yAxis.ticks =max;
            this.chartOptions.chart.color = (d: any)=>{
                return "#00B2B1";
            };
          //  this.chartApi.update();
        }


        clearRefreshInterval () {
            if(this.refreshInterval != null){
                clearInterval(this.refreshInterval);
                this.refreshInterval = null;
            }
        }

        setRefreshInterval () {
            this.clearRefreshInterval();
            if(this.refreshIntervalTime) {
                this.refreshInterval = setInterval(this.refresh.bind(this),this.refreshIntervalTime);
            }
        }

}
