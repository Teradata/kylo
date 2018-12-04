import "pascalprecht.translate";
import * as _ from 'underscore';
import { Component, Inject } from "@angular/core";
import {OpsManagerDashboardService} from "../../services/OpsManagerDashboardService";
import {OpsManagerJobService} from "../../services/ops-manager-jobs.service";
import {BroadcastService} from "../../../services/broadcast-service";
import { TranslateService } from "@ngx-translate/core";
import {MatDialog, MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {StateService} from "../../../services/StateService";
import { ObjectUtils } from "../../../../lib/common/utils/object-utils";

@Component({
    selector: 'tba-data-confidence-indicator',
    templateUrl: './data-confidence-indicator-template.html'
})
export class DataConfidenceIndicatorComponent {
    refreshing: boolean = false;
    dataLoaded: boolean = false;
    dataMap: any = {'Healthy':{count:0, color:'#5cb85c'},'Unhealthy':{count:0,color:'#a94442'}};
    allData: any = null;
    chartApi: any = {};
    chartOptions: any;
    chartData: any[];

    ngOnInit() {

        this.chartOptions = {
            chart: {
                type: 'pieChart',
                x: (d: any)=>{return d.key;},
                y: (d: any)=>{return d.value;},
                showLabels: false,
                duration: 100,
                height:150,
                labelThreshold: 0.01,
                labelSunbeamLayout: false,
                "margin":{"top":10,"right":10,"bottom":10,"left":10},
                donut:true,
                donutRatio:0.62,
                showLegend:false,
                refreshDataOnly: false,
                valueFormat: (d: any)=>{
                    return parseInt(d);
                },
                color:(d: any)=>{
                    if(d.key == 'Healthy'){
                        return '#388e3c';
                    }
                    else if( d.key== 'Unhealthy'){
                        return '#b71c1c';
                    }
                },
                pie: {
                    dispatch: {
                        'elementClick': (e: any)=>{
                            this.openDetailsDialog(e.data.key);
                        }
                    }
                },
                dispatch: {

                }
            }
        };
        this.chartData = [];

        this.initializePieChart();
        this.watchDashboard();
        
    }

    constructor(private dialog: MatDialog,
                private OpsManagerJobService: OpsManagerJobService,
                private OpsManagerDashboardService: OpsManagerDashboardService,
                private BroadcastService: BroadcastService,
                private translate: TranslateService) {}// end of constructor
       
        openDetailsDialog(key: string) {

            let dialogRef = this.dialog.open(DataDetailsDialogComponent, {
                data: { status: key,
                        allChartData: this.allData },
                panelClass: "full-screen-dialog"
                });

        };
            
        onHealthyClick () {
            if(this.dataMap.Healthy.count >0){
                this.openDetailsDialog('Healthy');
            }
        }

        onUnhealthyClick () {
            if(this.dataMap.Unhealthy.count >0){
                this.openDetailsDialog('Unhealthy');
            }
        }

        updateChart () {
            this.chartData.forEach((row: any,i: any)=>{
                row.value = this.dataMap[row.key].count;
            });
            var title = (this.dataMap.Healthy.count+this.dataMap.Unhealthy.count)+" " + this.translate.instant('Total');
            this.chartOptions.chart.title=title
            this.dataLoaded = true;
            if(this.chartApi.update) {
                this.chartApi.update();
            }
        }

        initializePieChart () {
            this.chartData.push({key: "Healthy", value: 0})
            this.chartData.push({key: "Unhealthy", value: 0})
        }

        getDataConfidenceSummary () {
            if (this.refreshing == false) {
                this.refreshing = true;
                    var data = this.OpsManagerDashboardService.dashboard.dataConfidenceSummary;
                    if(ObjectUtils.isDefined(data)) {
                        this.allData = data;
                        if (this.dataLoaded == false) {
                            this.dataLoaded = true;
                        }
                        this.dataMap.Healthy.count = data.successCount;
                        this.dataMap.Unhealthy.count = data.failedCount;
                    }
                    this.updateChart();
                }
                 this.refreshing = false;
        }

        watchDashboard=function() {
            this.BroadcastService.subscribe(this.$scope,this.OpsManagerDashboardService.DASHBOARD_UPDATED,(dashboard: any)=>{
                this.getDataConfidenceSummary();
            });
        }
}

@Component({
    templateUrl: './data-confidence-details-dialog.html'
})
export class DataDetailsDialogComponent {

    jobs:any[];
    css: string;
    status: string;
    allChartData: any;
           
    constructor (private dialogRef: MatDialogRef<DataDetailsDialogComponent>,
                 @Inject(MAT_DIALOG_DATA) private data: any,
                 private stateService: StateService) {}

    ngOnInit() {

        this.allChartData = this.data.allChartData;
        this.status = this.data.status;

        if(status == 'Unhealthy') {
            this.css = "md-warn";
            this.jobs = this.allChartData.failedJobs;
        } else {
            this.css = "";
            this.jobs = _.filter(this.allChartData.latestCheckDataFeeds,(job: any)=> {
                return job.status != 'FAILED';
            });
        }

    }

    hide() {
        this.dialogRef.close();
    }

    cancel() {
        this.dialogRef.close();
    }

    gotoJobDetails(jobExecutionId: any) {
        this.dialogRef.close();
        this.stateService.OpsManager().Job().navigateToJobDetails(jobExecutionId);
    }
            
}
