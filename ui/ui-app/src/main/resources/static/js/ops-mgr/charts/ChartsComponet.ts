import {OpsManagerJobService} from "../services/ops-manager-jobs.service";
import PivotTableUtil from "./PivotTableUtil";
import * as _ from "underscore";
import * as moment from "moment";
import * as $ from "jquery";
import { Component, OnInit, OnDestroy, ViewEncapsulation } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import {OpsManagerRestUrlService} from '../services/OpsManagerRestUrlService';
import "jquery";
import "jquery-ui";
import "pivottable";
import "pivottable-c3-renderers";
import '../../../bower_components/c3/c3.css';

@Component({
    selector: 'ops-mgr-charts',
    templateUrl: './charts.html',
    encapsulation: ViewEncapsulation.None,
    styleUrls:['../../../bower_components/c3/c3.css']
})
export class ChartsComponent implements OnInit, OnDestroy {
    selectedFeedNames: string[] = ['ALL'];
    startDate: any = null;
    endDate: any = null;
    limitRows: number = 500;
    limitOptions: number[] = [200, 500, 1000, 5000, 10000];
    message: string = '';
    isWarning: boolean = false;
    filtered: boolean = false;
    loading: boolean = false;
    pivotConfig: any;
    responseData: any;
    feedNames: any;
    lastRefreshed: any;

    constructor(
        private http: HttpClient,
        private OpsManagerJobService: OpsManagerJobService,
        private opsManagerURLService: OpsManagerRestUrlService) {

        this.pivotConfig = {//rendererName:"Job Details", 
            aggregatorName: "Average",
            vals: ["Duration (min)"],
            rendererName: "Stacked Bar Chart",
            cols: ["Start Date"], rows: ["Feed Name"],
            unusedAttrsVertical: false
        };



    }// end of Constructor

    ngOnInit() {
        this.refreshPivotTable();
        this.onWindowResize();
        this.getFeedNames();
    }
    ngOnDestroy() {
        $(window).off("resize.doResize");
    }
    removeAllFromArray(arr: any) {
        if (arr != null && arr.length > 0 && _.indexOf(arr, 'All') >= 0) {
            return _.without(arr, 'All');
        }
        else {
            return arr;
        }

    }

    refreshPivotTable() {
        var successFn = (response: any) => {
            this.responseData = response;
            if (this.responseData.length >= this.limitRows && this.filtered == true) {
                this.message = "Warning. Only returned the first " + this.limitRows + " records. Either increase the limit or modify the filter."
                this.isWarning = true;
            }
            else {
                this.message = 'Showing ' + this.responseData.length + ' jobs';
            }
            this.loading = false;
            this.renderPivotTable(this.responseData);

        };
        var errorFn = (err: any) => {
            console.log('error', err)
        }
        var finallyFn = () => {

        }

        var addToFilter = (filterStr: any, addStr: any) => {
            if (filterStr != null && filterStr != "") {
                filterStr += ",";
            }
            filterStr += addStr;
            return filterStr;
        }

        var formParams = new HttpParams();
        var startDateSet = false;
        var endDateSet = false;
        this.filtered = false;
        formParams.append('limit',this.limitRows.toString());
        formParams.append('sort','-executionid');
        var filter = "";
        if (!_.includes(this.selectedFeedNames, 'ALL') && this.selectedFeedNames.length > 0) {
            filter = addToFilter(filter, "feedName==\"" + this.selectedFeedNames.join(',') + "\"");
            this.filtered = true;
        }
        if (this.startDate != null && this.startDate !== '') {
            var m = moment(this.startDate);
            var filterStr = 'startTimeMillis>' + m.toDate().getTime();
            filter = addToFilter(filter, filterStr)
            this.filtered = true;
            startDateSet = true;
        }
        if (this.endDate != null && this.endDate !== '') {
            var m = moment(this.endDate);
            var filterStr = 'startTimeMillis<' + m.toDate().getTime();
            filter = addToFilter(filter, filterStr)
            this.filtered = true;
            endDateSet = true;
        }
        if (startDateSet && !endDateSet || startDateSet && endDateSet) {
            formParams.append('sort','executionid');
        }
        formParams.append('filter',filter);


        $("#charts_tab_pivot_chart").html('<div class="bg-info">Rendering Pivot Table...</div>')
        this.loading = true;
        this.http.get(this.OpsManagerJobService.JOBS_CHARTS_QUERY_URL, {params : formParams}).subscribe(successFn,errorFn,finallyFn);
    }


    getFeedNames() {

        var successFn = (response: any) => {
            if (response) {

                this.feedNames = _.unique(response);
                this.feedNames.unshift('All');
            }
        }
        var errorFn = (err: any) => {
        }
        var finallyFn = () => {

        }
        this.http.get(this.opsManagerURLService.FEED_NAMES_URL).toPromise().then(successFn, errorFn);
    }


    renderPivotTable(tableData: any) {


        var hideColumns = ["exceptions", "executionContext", "jobParameters", "lastUpdated", "executedSteps", "jobConfigurationName", "executionId", "instanceId", "jobId", "latest", "exitStatus"];

        var pivotNameMap = {
            "startTime": {
                name: "Start Time", fn: (val: any) => {
                    return new Date(val);
                }
            },
            "endTime": {
                name: "End Time", fn: (val: any) => {
                    return new Date(val);
                }
            },
            "runTime": {
                name: "Duration (min)", fn: (val: any) => {
                    return val / 1000 / 60;
                }
            }

        };

        var pivotData = PivotTableUtil.transformToPivotTable(tableData, hideColumns, pivotNameMap);

        var renderers = $.extend(($ as any).pivotUtilities.renderers,
            ($ as any).pivotUtilities.c3_renderers);
        var derivers = ($ as any).pivotUtilities.derivers;
        var width = this.getWidth();
        var height = this.getHeight();

        ($("#charts_tab_pivot_chart") as any).pivotUI(pivotData, {
            onRefresh: (config: any) => {
                var config_copy = JSON.parse(JSON.stringify(config));
                //delete some values which are functions
                delete config_copy["aggregators"];
                delete config_copy["renderers"];
                delete config_copy["derivedAttributes"];
                //delete some bulky default values
                delete config_copy["rendererOptions"];
                delete config_copy["localeStrings"];
                this.pivotConfig = config_copy;
                this.assignLabels();
            },
            renderers: renderers,
            rendererOptions: { c3: { size: { width: width, height: height } } },
            derivedAttributes: {
                "Start Date": ($ as any).pivotUtilities.derivers.dateFormat("Start Time", "%y-%m-%d"),
                "End Date": ($ as any).pivotUtilities.derivers.dateFormat("End Time", "%y-%m-%d"),
                "Duration (sec)": (mp: any) => { return mp["Duration (min)"] * 60; }
            },
            rendererName: this.pivotConfig.rendererName,
            aggregatorName: this.pivotConfig.aggregatorName,
            vals: this.pivotConfig.vals,
            // rendererName: this.pivotConfig.rendererName,
            cols: this.pivotConfig.cols, rows: this.pivotConfig.rows,
            unusedAttrsVertical: this.pivotConfig.unusedAttrsVertical
        }, true);
        this.lastRefreshed = new Date(); 

    }
    getWidth() {
        var sideNav = $('md-sidenav').width();
        if ($('.toggle-side-nav').is(':visible')) {
            sideNav = 0;
        }
        var rightCard = $('.filter-chart').width();
        return $(window).innerWidth() - (sideNav + 400) - rightCard;
    }

    getHeight() {
        var header = $('page-header').height();
        var height = $(window).innerHeight() - (header + 450);
        if (height < 400) {
            height = 400;
        }
        return height;
    }

    onWindowResize() {
        $(window).on("resize.doResize", _.debounce(() => {

            if (this.lastRefreshed) {
                $("#charts_tab_pivot_chart").html('Rendering Chart ...');
                this.renderPivotTable(this.responseData);
            }
        }, 100));
    }

    assignLabels() {

        if ($('.pivot-label').length == 0) {
            $('.pvtUi').find('tbody:first').prepend('<tr><td><div class="pivot-label accent-color-3">Chart Type</div></td><td><div class="pivot-label accent-color-3">Attributes (drag and drop to customize the chart)</div></td></tr>');
            $('.pvtAggregator').parents('tr:first').before('<tr><td style="font-size:3px;">&nbsp;</td><td style="font-size:3px;">&nbsp;<td></tr>')
            $('.pvtAggregator').parents('td:first').css('padding-bottom', '10px')
            $('.pvtAggregator').before('<div class="pivot-label accent-color-3" style="padding-bottom:8px;">Aggregrator</div>');
            $('.pvtRenderer').parent().css('vertical-align', 'top')
            $('.pvtRenderer').parent().css('vertical-align', 'top');
            var selectWidth = $('.pvtAggregator').width();
            $('#charts_tab_pivot_chart').find('select').css('width', selectWidth);
            $('.pvtCols').css('vertical-align', 'top');
        }
    }

    limitOptionChanged(event: any) {
        this.limitRows = event.value;
    }

    feedsSelected(event: any) {
        this.selectedFeedNames = event.value;
    }

}

