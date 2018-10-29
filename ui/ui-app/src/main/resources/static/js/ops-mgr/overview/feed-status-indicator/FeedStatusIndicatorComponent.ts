import "pascalprecht.translate";
import { Component, Input } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import BroadcastService from "../../../services/broadcast-service";
import OpsManagerDashboardService from "../../services/OpsManagerDashboardService";
import { OpsManagerFeedService } from "../../services/OpsManagerFeedService";
import { TranslateService } from "@ngx-translate/core";

import 'd3';
import 'nvd3';

@Component({
    selector: 'tba-feed-status-indicator',
    templateUrl: 'js/ops-mgr/overview/feed-status-indicator/feed-status-indicator-template.html'
})
export default class FeedStatusIndicatorComponent {

    chartApi: any = {};
    dataLoaded: boolean = false;
    feedSummaryData: any = null;
    chartData: any[] = [];
    dataMap: any = {'Healthy':{count:0, color:'#009933'},'Unhealthy':{count:0,color:'#FF0000'}};
    chartOptions: any;
    @Input() panelTitle: string;

    ngOnInit() {

        this.chartOptions = {
            chart: {
                type: 'pieChart',
                x: (d: any)=>{return d.key;},
                y: (d: any)=>{return d.value;},
                showLabels: false,
                duration: 100,
                height:150,
                transitionDuration:500,
                labelThreshold: 0.01,
                labelSunbeamLayout: false,
                "margin":{"top":10,"right":10,"bottom":10,"left":10},
                donut:true,
                donutRatio:0.65,
                showLegend:false,
                refreshDataOnly: false,
                color:(d: any)=>{
                    return this.dataMap[d.key].color;
                },
                valueFormat: (d: any)=>{
                    return parseInt(d);
                },
                pie: {
                    dispatch: {
                        'elementClick': (e: any)=>{
                           this.onChartElementClick(e.data.key);
                        }
                    }
                },
                dispatch: {

                }
            }
        };

        this.initializePieChart();
        this.watchDashboard();

    }
    constructor(private http: HttpClient,
                private translate : TranslateService,
                private OpsManagerFeedService: OpsManagerFeedService,
                private OpsManagerDashboardService: OpsManagerDashboardService,
                private BroadcastService: BroadcastService){
       
        }// end of constructor

        initializePieChart () {
                this.chartData.push({key: "Healthy", value: 0})
                this.chartData.push({key: "Unhealthy", value: 0})
        }

        onHealthyClick () {
            this.OpsManagerDashboardService.selectFeedHealthTab('Healthy');
        }

        onUnhealthyClick () {
            this.OpsManagerDashboardService.selectFeedHealthTab('Unhealthy');
        }

    watchDashboard () {

            this.BroadcastService.subscribe(null, this.OpsManagerDashboardService.DASHBOARD_UPDATED,
                                            (dashboard: any)=>{
                this.dataMap.Unhealthy.count = this.OpsManagerDashboardService.feedUnhealthyCount;

                this.dataMap.Healthy.count = this.OpsManagerDashboardService.feedHealthyCount;
                this.feedSummaryData = this.OpsManagerDashboardService.feedSummaryData;
                this.updateChartData();
            });
        }

        onChartElementClick(key: any) {
            this.OpsManagerDashboardService.selectFeedHealthTab(key);
        }

        updateChartData () {
            this.chartData.forEach((row: any,i: any)=>{
                row.value = this.dataMap[row.key].count;
            });
            var title = (this.dataMap.Healthy.count+this.dataMap.Unhealthy.count)+" "+ this.translate.instant('Total');
            this.chartOptions.chart.title=title
            this.dataLoaded = true;
            if(this.chartApi.update) {
                this.chartApi.update();
            }
        }

        updateChart () {
            if(this.chartApi.update) {
                this.chartApi.update();
            }
        }

}
