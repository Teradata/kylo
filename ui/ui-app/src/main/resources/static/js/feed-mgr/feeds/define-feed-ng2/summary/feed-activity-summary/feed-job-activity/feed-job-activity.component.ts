import {Component, Injector, Input, OnInit} from "@angular/core";
import {BroadcastService} from "../../../../../../services/broadcast-service";
import { TranslateService } from "@ngx-translate/core";
import { HttpClient } from "@angular/common/http";

// d3 and nvd3 should be included somewhere
import 'd3';
import 'nvd3';
import {OperationsRestUrlConstants} from "../../../../../../services/operations-rest-url-constants";
import {OpsManagerChartJobService} from "../../../../../../ops-mgr/services/ops-manager-chart-job.service";
import {Feed} from "../../../../../model/feed/feed.model";
import {MatButtonToggleChange} from "@angular/material";
import {KyloIcons} from "../../../../../../kylo-utils/kylo-icons";

declare const d3: any;

@Component({
    selector: "feed-job-activity",
    templateUrl: "./feed-job-activity.component.html"
})
export class FeedJobActivityComponent implements OnInit {

    dataLoaded: any;
    dateSelection: string = "1-M";
    chartData: any[];
   // chartApi: any;
    chartOptions: any;
    fixChartWidthCounter: any;
    fixChartWidthTimeout: any;
    loading: any;

    @Input()
    feedName: any;

    @Input()
    feed:Feed;

    kyloIcons = KyloIcons;

    ngOnInit() {
        if(!this.feedName) {
            this.feedName = this.feed.getFullName()
        }

        this.dataLoaded = false;
        this.chartData = [];
     //   this.chartApi = {};
        this.chartOptions = {
            chart: {
                type: 'lineChart',
                height: 250,
                margin: {
                    top: 5,
                    right: 10,
                    bottom: 40,
                    left: 50
                },
                x: (d: any) => { return d[0]; },
                y: (d: any) => { return d[1]; },
                useVoronoi: false,
                clipEdge: false,
                duration: 250,
                useInteractiveGuideline: true,
                noData: this.translate.instant('views.views.FeedActivityDirective.noData'),
                xAxis: {
                    axisLabel: this.translate.instant('views.FeedActivityDirective.Date'),
                    showMaxMin: false,
                    tickFormat: (d: any) => {
                        return d3.time.format('%x')(new Date(d))
                    }
                },
                yAxis: {
                    axisLabel: this.translate.instant('views.FeedActivityDirective.Count'),
                    axisLabelDistance: -10
                },
                dispatch: {
                    renderEnd: () => {
                        this.fixChartWidth();
                    }
                }
            }
        };
        this.changeDate('1-M');
    }

    private broadcastService: BroadcastService;
    constructor(
        private opsManagerChartJobService: OpsManagerChartJobService,
        private $$angularInjector: Injector,
        private translate: TranslateService,
        private http: HttpClient) {

        this.broadcastService = $$angularInjector.get("BroadcastService");
        this.broadcastService.subscribe(null, 'ABANDONED_ALL_JOBS', () =>{this.updateCharts()});
    }// end of constructor


    onDateSelectionChange($event:MatButtonToggleChange){
        //console.log("changed date ",$event)
        this.changeDate($event.value)
    }
    changeDate(date: string) {
        this.dateSelection = date;
        this.query();
    };
    updateCharts() {
        this.query();
        this.updateChart();
    };
    updateChart() {
     //   if (this.chartApi.update) {
     //       this.chartApi.update();
   //     }
    };
    fixChartWidth() {
        var chartWidth = parseInt($($('.nvd3-svg')[0]).find('rect:first').attr('width'));
        if (chartWidth < 100) {
            this.updateChart();
            if (this.fixChartWidthCounter == undefined) {
                this.fixChartWidthCounter = 0;
            }
            this.fixChartWidthCounter++;
            if (this.fixChartWidthTimeout) {
                clearTimeout(this.fixChartWidthTimeout);
            }
            if (this.fixChartWidthCounter < 1000) {
                this.fixChartWidthTimeout = setTimeout(() => {
                    this.fixChartWidth();
                }, 10);
            }
        }
        else {
            if (this.fixChartWidthTimeout) {
                clearTimeout(this.fixChartWidthTimeout);
            }
            this.fixChartWidthCounter = 0;
        }
    };
    createChartData(responseData: any) {
        this.chartData = this.opsManagerChartJobService.toChartData(responseData);

    };
    query() {
        //parse the date selection
        var interval = parseInt(this.dateSelection.substring(0, this.dateSelection.indexOf('-')));
        var datePart = this.dateSelection.substring(this.dateSelection.indexOf('-') + 1);

        var successFn = (response: any) => {
            if (response) {
                //transform the data for UI
                this.createChartData(response);
                if (this.loading) {
                    this.loading = false;
                }
                if (!this.dataLoaded && response.length == 0) {
                    setTimeout(() => {
                        this.dataLoaded = true;
                    }, 500)
                }
                else {
                    this.dataLoaded = true;
                }
            }
            else {

            }
        }
        var errorFn = (err: any) => {
            console.log("ERROR cant get feed activity",err)
        }
        var finallyFn = () => {
        }

        this.http.get(OperationsRestUrlConstants.FEED_DAILY_STATUS_COUNT_URL(this.feedName), { params: { "period": interval + datePart } }).subscribe(successFn, errorFn);
    };
}