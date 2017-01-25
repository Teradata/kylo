/*-
 * #%L
 * thinkbig-ui-operations-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
(function () {

    var controller = function($scope, $element,$http,HttpService, JobData, FeedData, Utils ){
        var self = this;
        this.selectedFeedNames = ['All'];
        this.startDate = null;
        this.endDate = null;
        this.limitRows = 500;
        this.limitOptions=[200,500,1000,5000,10000];
        this.message = '';
        this.isWarning = false;
        this.filtered = false;
        this.loading = false;


        this.pivotConfig = {rendererName:"Job Details", aggregatorName: "Average",
            vals: ["Duration (min)"],
            rendererName: "Stacked Bar Chart",
            cols: ["Start Date"], rows: ["Feed Name"],
            unusedAttrsVertical: false};

        var removeAllFromArray = function(arr){
            if(arr != null && arr.length >0 && _.indexOf(arr,'All') >=0){
                return _.without(arr,'All');
            }
            else {
                return arr;
            }

        }


        this.refreshPivotTable = function () {
            var successFn = function (data) {
                self.responseData = data;
                if(data.length >= self.limitRows && self.filtered == true){
                    self.message = "Warning. Only returned the first "+self.limitRows+" records. Either increase the limit or modify the filter."
                    self.isWarning = true;
                }
                else {
                    self.message = 'Showing '+data.length+' jobs';
                }
                self.loading = false;
                self.renderPivotTable(data);

            };
            var errorFn = function (err) {
                console.log('error', err)
            }
            var finallyFn = function () {

            }

            var formParams = {};
            var startDateSet = false;
            var endDateSet = false;
            self.filtered = false;
            formParams['limit'] = self.limitRows;
            formParams['sort'] = '-executionid';
            if(!_.contains(self.selectedFeedNames,'All') && self.selectedFeedNames.length >0){
                formParams['feedName'] = self.selectedFeedNames.join(',');
                self.filtered = true;
            }
            if(self.startDate != null && self.startDate !== '') {
                formParams['starttime>'] = new moment(self.startDate).format('MM/DD/YYYY');
                self.filtered = true;
                startDateSet = true;
            }
            if(self.endDate != null && self.endDate !== '') {
                formParams['starttime<'] =new moment(self.endDate).format('MM/DD/YYYY');
                self.filtered = true;
                endDateSet = true;
            }
            if(startDateSet && !endDateSet || startDateSet && endDateSet){
                formParams['sort'] = 'executionid';
            }


            $("#charts_tab_pivot_chart").html('<div class="bg-info"><i class="fa fa-refresh fa-spin"></i> Rendering Pivot Table...</div>')
            var rqst = HttpService.newRequestBuilder(JobData.JOBS_CHARTS_QUERY_URL).params(formParams).success(successFn).error(errorFn).finally(finallyFn).build();
            this.currentRequest = rqst;
            this.loading = true;
        }


        function getFeedNames(){

            var successFn = function (response) {
                if (response.data) {

                    self.feedNames = response.data;
                    self.feedNames.unshift('All');
                }
            }
            var errorFn = function (err) {
            }
            var finallyFn = function () {

            }
            $http.get(FeedData.FEED_NAMES_URL).then( successFn, errorFn);
        }


        this.renderPivotTable = function (tableData) {


            var hideColumns = ["exceptions", "executionContext", "jobParameters", "lastUpdated", "executedSteps", "jobConfigurationName","executionId","instanceId","jobId","latest","exitStatus"];

            var pivotNameMap = {
                "startTime": {
                    name: "Start Time", fn: function (val) {
                        return new Date(val);
                    }
                },
                "endTime": {
                    name: "End Time", fn: function (val) {
                        return new Date(val);
                    }
                },
                "runTime": {
                    name: "Duration (min)", fn: function (val) {
                        return val / 1000 / 60;
                    }
                }

            };

            var pivotData = PivotTableUtil.transformToPivotTable(tableData, hideColumns, pivotNameMap);

            var renderers = $.extend($.pivotUtilities.renderers,
                $.pivotUtilities.c3_renderers);
            var derivers = $.pivotUtilities.derivers;
            var width = getWidth();
            var height = getHeight();

            $("#charts_tab_pivot_chart").pivotUI(pivotData, {
                onRefresh:function(config){
                    var config_copy = JSON.parse(JSON.stringify(config));
                    //delete some values which are functions
                    delete config_copy["aggregators"];
                    delete config_copy["renderers"];
                    delete config_copy["derivedAttributes"];
                    //delete some bulky default values
                    delete config_copy["rendererOptions"];
                    delete config_copy["localeStrings"];
                    self.pivotConfig = config_copy;
                    assignLabels();
                },
                renderers: renderers,
                rendererOptions:{c3:{size:{width:width,height:height}}},
                derivedAttributes: {
                    "Start Date": $.pivotUtilities.derivers.dateFormat("Start Time", "%y-%m-%d"),
                    "End Date": $.pivotUtilities.derivers.dateFormat("End Time", "%y-%m-%d"),
                    "Duration (sec)": function (mp) {
                        return mp["Duration (min)"] * 60;
                    }
                },
                rendererName: self.pivotConfig.rendererName,
                aggregatorName: self.pivotConfig.aggregatorName,
                vals: self.pivotConfig.vals,
                rendererName: self.pivotConfig.rendererName,
                cols: self.pivotConfig.cols, rows: self.pivotConfig.rows,
                unusedAttrsVertical: self.pivotConfig.unusedAttrsVertical
            },true);
            $scope.lastRefreshed = new Date();

        }
        function getWidth() {
            var sideNav = $('md-sidenav').width();
            if($('.toggle-side-nav').is(':visible')){
                sideNav = 0;
            }
            var rightCard = $('.filter-chart').width();
            return $(window).innerWidth() -(sideNav + 400) - rightCard;
;        }

        function getHeight() {
          var header = $('page-header').height();
            var height = $(window).innerHeight() - (header + 450);
            if(height <400 ) {
                height = 400;
            }
            return height;
        }

        function onWindowResize() {
            $(window).on("resize.doResize", _.debounce(function (){

                $scope.$apply(function(){
                    if($scope.lastRefreshed) {
                        $("#charts_tab_pivot_chart").html('Rendering Chart ...');
                        self.renderPivotTable(self.responseData);
                    }
                });
            },100));
        }

       this.refreshPivotTable();
        onWindowResize();
        getFeedNames();

        function assignLabels() {

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


        $scope.$on("$destroy",function (){
            $(window).off("resize.doResize"); //remove the handler added earlier
        });

    };

    angular.module(MODULE_OPERATIONS).controller('ChartsController',controller);



}());
