import "pascalprecht.translate";
import { Component, Input, OnInit } from "@angular/core";
import Utils from "../../services/Utils";
import ChartJobStatusService from "../services/ChartJobStatusService";
import BroadcastService from "../../services/broadcast-service";
import { TranslateService } from "@ngx-translate/core";
import { HttpClient } from "@angular/common/http";
import OpsManagerRestUrlService from "../services/OpsManagerRestUrlService";

// d3 and nvd3 should be included somewhere
import 'd3';
import 'nvd3';

declare const d3: any;

@Component({
    selector: "tba-feed-activity",
    templateUrl: "js/ops-mgr/feeds/feed-activity-template.html",
    styles: [`.btn-border {
        background-color: white;
        text-align: center !important;
        height: 36px;
        font-size: 12px;
        padding: 0;
        color: #666666;
        border: 1px solid #d7d7d7;
        line-height: inherit;
    }`]
})
export class FeedActivityComponent implements OnInit {
    pageName: any;
    dataLoaded: any;
    dateSelection: any;
    chartData: any[];
    chartApi: any;
    chartOptions: any;
    fixChartWidthCounter: any;
    fixChartWidthTimeout: any;
    datePart: any;
    interval: any;
    loading: any;
    @Input() feedName: any;

    ngOnInit() {
        this.pageName = 'feed-activity';
        this.dataLoaded = false;
        this.chartData = [];
        this.chartApi = {};
        this.chartOptions = {
            chart: {
                type: 'lineChart',
                height: 250,
                margin: {
                    top: 10,
                    right: 20,
                    bottom: 40,
                    left: 55
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

    constructor(
        private Utils: Utils,
        private opsManagerRestUrlService: OpsManagerRestUrlService,
        private chartJobStatusService: ChartJobStatusService,
        private broadcastService: BroadcastService,
        private translate: TranslateService,
        private http: HttpClient) {

        this.broadcastService.subscribe(null, 'ABANDONED_ALL_JOBS', () =>{this.updateCharts()});
    }// end of constructor
    changeDate(date: string) {
        this.dateSelection = date;
        this.parseDatePart();
        this.query();
    };
    updateCharts() {
        this.query();
        this.updateChart();
    };
    updateChart() {
        if (this.chartApi.update) {
            this.chartApi.update();
        }
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
        this.chartData = this.chartJobStatusService.toChartData(responseData);

    };
    parseDatePart() {
        var interval = parseInt(this.dateSelection.substring(0, this.dateSelection.indexOf('-')));
        var datePart = this.dateSelection.substring(this.dateSelection.indexOf('-') + 1);
        this.datePart = datePart
        this.interval = interval;
    };
    query() {
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
        }
        var errorFn = (err: any) => {
        }
        var finallyFn = () => {
        }

        this.http.get(this.opsManagerRestUrlService.FEED_DAILY_STATUS_COUNT_URL(this.feedName), { params: { "period": this.interval + this.datePart } }).toPromise().then(successFn, errorFn);
    };
}