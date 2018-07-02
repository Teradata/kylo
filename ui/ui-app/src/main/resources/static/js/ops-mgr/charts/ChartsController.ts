import * as angular from 'angular';
import {moduleName} from "./module-name";
import PivotTableUtil from "./PivotTableUtil";
import * as _ from "underscore";
import * as moment from "moment";

export class controller implements ng.IComponentController{
    selectedFeedNames: any;
    startDate: any;
    endDate: any;
    limitRows: any;
    limitOptions: any;
    message: any;
    isWarning: any;
    filtered: any;
    loading: any;
    pivotConfig: any;
    responseData: any;
    currentRequest: any;
    feedNames: any;

constructor(private $scope: any,
            private $element: any,
            private $http: any,
            private HttpService: any,
            private OpsManagerJobService: any,
            private OpsManagerFeedService: any){
        this.selectedFeedNames = ['All'];
        this.startDate = null;
        this.endDate = null;
        this.limitRows = 500;
        this.limitOptions=[200,500,1000,5000,10000];
        this.message = '';
        this.isWarning = false;
        this.filtered = false;
        this.loading = false;

        this.pivotConfig = {//rendererName:"Job Details", 
            aggregatorName: "Average",
            vals: ["Duration (min)"],
            rendererName: "Stacked Bar Chart",
            cols: ["Start Date"], rows: ["Feed Name"],
            unusedAttrsVertical: false};

        this.refreshPivotTable();
        this.onWindowResize();
        this.getFeedNames();

         $scope.$on("$destroy",()=>{
            $(window).off("resize.doResize"); //remove the handler added earlier
        });
}// end of Constructor
        removeAllFromArray = (arr: any)=>{
            if(arr != null && arr.length >0 && _.indexOf(arr,'All') >=0){
                return _.without(arr,'All');
            }
            else {
                return arr;
            }

        }

        refreshPivotTable = ()=> {
            var successFn = (response: any)=> {
                this.responseData = response.data;
                var data = response.data;
                if(this.responseData .length >= this.limitRows && this.filtered == true){
                    this.message = "Warning. Only returned the first "+this.limitRows+" records. Either increase the limit or modify the filter."
                    this.isWarning = true;
                }
                else {
                    this.message = 'Showing '+data.length+' jobs';
                }
                this.loading = false;
                this.renderPivotTable(data);

            };
            var errorFn =  (err: any)=> {
                console.log('error', err)
            }
            var finallyFn = ()=> {

            }

           var addToFilter = (filterStr: any, addStr: any)=> {
                if (filterStr != null && filterStr != "") {
                    filterStr += ",";
                }
                filterStr += addStr;
                return filterStr;
            }

            var formParams = {};
            var startDateSet = false;
            var endDateSet = false;
            this.filtered = false;
            formParams['limit'] = this.limitRows;
            formParams['sort'] = '-executionid';
            var filter = "";
            if(!_.contains(this.selectedFeedNames,'All') && this.selectedFeedNames.length >0){
                filter = addToFilter(filter, "feedName==\"" + this.selectedFeedNames.join(',') + "\"");
                this.filtered = true;
            }
            if(this.startDate != null && this.startDate !== '') {
                var m =  moment(this.startDate);
                var filterStr = 'startTimeMillis>' + m.toDate().getTime();
                filter = addToFilter(filter, filterStr)
                this.filtered = true;
                startDateSet = true;
            }
            if(this.endDate != null && this.endDate !== '') {
                var m =  moment(this.endDate);
                var filterStr = 'startTimeMillis<' + m.toDate().getTime();
                filter = addToFilter(filter, filterStr)
                this.filtered = true;
                endDateSet = true;
            }
            if(startDateSet && !endDateSet || startDateSet && endDateSet){
                formParams['sort'] = 'executionid';
            }
            formParams['filter'] = filter;


            $("#charts_tab_pivot_chart").html('<div class="bg-info"><i class="fa fa-refresh fa-spin"></i> Rendering Pivot Table...</div>')
            var rqst = this.HttpService.newRequestBuilder(this.OpsManagerJobService.JOBS_CHARTS_QUERY_URL).params(formParams).success(successFn).error(errorFn).finally(finallyFn).build();
            this.currentRequest = rqst;
            this.loading = true;
        }


        getFeedNames=()=>{

            var successFn = (response: any)=> {
                if (response.data) {

                    this.feedNames = _.unique(response.data);
                    this.feedNames.unshift('All');
                }
            }
            var errorFn = (err: any)=> {
            }
            var finallyFn = ()=> {

            }
            this.$http.get(this.OpsManagerFeedService.FEED_NAMES_URL).then( successFn, errorFn);
        }


        renderPivotTable = (tableData: any)=> {


            var hideColumns = ["exceptions", "executionContext", "jobParameters", "lastUpdated", "executedSteps", "jobConfigurationName","executionId","instanceId","jobId","latest","exitStatus"];

            var pivotNameMap = {
                "startTime": {
                    name: "Start Time", fn: (val: any)=> {
                        return new Date(val);
                    }
                },
                "endTime": {
                    name: "End Time", fn: (val: any)=> {
                        return new Date(val);
                    }
                },
                "runTime": {
                    name: "Duration (min)", fn: (val: any)=> {
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
                onRefresh:(config: any)=>{
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
                rendererOptions:{c3:{size:{width:width,height:height}}},
                derivedAttributes: {
                    "Start Date": ($ as any).pivotUtilities.derivers.dateFormat("Start Time", "%y-%m-%d"),
                    "End Date": ($ as any).pivotUtilities.derivers.dateFormat("End Time", "%y-%m-%d"),
                    "Duration (sec)": (mp: any)=>{ return mp["Duration (min)"] * 60;}
                },
                rendererName: this.pivotConfig.rendererName,
                aggregatorName: this.pivotConfig.aggregatorName,
                vals: this.pivotConfig.vals,
               // rendererName: this.pivotConfig.rendererName,
                cols: this.pivotConfig.cols, rows: this.pivotConfig.rows,
                unusedAttrsVertical: this.pivotConfig.unusedAttrsVertical
            },true);
            this.$scope.lastRefreshed = new Date();

        }
        getWidth=()=> {
            var sideNav = $('md-sidenav').width();
            if($('.toggle-side-nav').is(':visible')){
                sideNav = 0;
            }
            var rightCard = $('.filter-chart').width();
            return $(window).innerWidth() -(sideNav + 400) - rightCard;
        }

        getHeight=()=> {
          var header = $('page-header').height();
            var height = $(window).innerHeight() - (header + 450);
            if(height <400 ) {
                height = 400;
            }
            return height;
        }

        onWindowResize=()=> {
            $(window).on("resize.doResize", _.debounce( ()=>{

                this.$scope.$apply(()=>{
                    if(this.$scope.lastRefreshed) {
                        $("#charts_tab_pivot_chart").html('Rendering Chart ...');
                        this.renderPivotTable(this.responseData);
                    }
                });
            },100));
        }

  assignLabels=()=> {

            if($('.pivot-label').length == 0) {
                $('.pvtUi').find('tbody:first').prepend('<tr><td><div class="pivot-label accent-color-3">Chart Type</div></td><td><div class="pivot-label accent-color-3">Attributes (drag and drop to customize the chart)</div></td></tr>');
                $('.pvtAggregator').parents('tr:first').before('<tr><td style="font-size:3px;">&nbsp;</td><td style="font-size:3px;">&nbsp;<td></tr>')
                $('.pvtAggregator').parents('td:first').css('padding-bottom','10px')
                $('.pvtAggregator').before('<div class="pivot-label accent-color-3" style="padding-bottom:8px;">Aggregrator</div>');
                $('.pvtRenderer').parent().css('vertical-align', 'top')
                $('.pvtRenderer').parent().css('vertical-align', 'top');
                var selectWidth = $('.pvtAggregator').width();
                $('#charts_tab_pivot_chart').find('select').css('width',selectWidth);
                $('.pvtCols').css('vertical-align','top');
            }
        }

}

angular.module(moduleName)
 .controller('ChartsController',
["$scope","$element","$http","HttpService","OpsManagerJobService","OpsManagerFeedService",controller]);
