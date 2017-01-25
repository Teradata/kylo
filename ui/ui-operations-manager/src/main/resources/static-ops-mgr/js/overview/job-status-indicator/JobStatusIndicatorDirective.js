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

    var directive = function () {
        return {
            restrict: "EA",
            scope:true,
            controllerAs:'vm',
            bindToController: {
                panelTitle: "@",
                refreshIntervalTime:"@"
            },
            templateUrl: 'js/overview/job-status-indicator/job-status-indicator-template.html',
            controller: "JobStatusIndicatorController",
            link: function ($scope, element, attrs) {
                $scope.$on('$destroy', function() {
                });
            } //DOM manipulation\}
        }

    };

    var controller = function ($scope,$element,$http, $q,$interval,JobData, HttpService,ChartJobStatusService,Utils, Nvd3ChartService) {
        var self = this;
        this.refreshInterval = null;
        this.dataLoaded = false;
        this.chartApi = {};
        this.chartConfig = {

        }

        this.running = 0;
        this.failed = 0;
        this.chartData = [];
        this.runningCounts = [];
        var maxDatapoints = 20;
        this.chartOptions =  {
            chart: {
                type: 'lineChart',
                margin : {
                    top: 5,
                    right: 5,
                    bottom:10,
                    left: 20
                },
                x: function(d){return d[0];},
                y: function(d){return d[1];},
                useVoronoi: false,
                clipEdge: false,
                duration: 0,
                height:136,
                useInteractiveGuideline: true,
                xAxis: {
                    axisLabel: 'Time',
                    showMaxMin: false,
                    tickFormat: function(d) {
                        return d3.time.format('%X')(new Date(d))
                    }
                },
                yAxis: {
                    axisLabel:'Running',
                    "axisLabelDistance": -10,
                    showMaxMin:false,
                    tickSubdivide:0,
                    ticks:1
                },
                yDomain:[0,1],
                showLegend:false,
                showXAxis:false,
                showYAxis:true,
                dispatch: {

                }
            }
        };
        this.updateChart = function(){
            if(self.chartApi.update) {
                self.chartApi.update();
            }
        }

        function getRunningFailedCounts() {
                var successFn = function (response) {
                    if(response){
                     updateCounts(response.data);
                        if(self.runningCounts.length >= maxDatapoints){
                            self.runningCounts.shift();
                            self.chartData[0].values.shift();
                        }
                        var dataItem = {status:'RUNNING_JOB_ACTIVITY',date:new Date().getTime(), count:self.running}
                        self.runningCounts.push(dataItem);
                        addChartData(dataItem);
                        self.dataLoaded = true;
                    }

                }

                var errorFn = function (data, status, headers, config) {
                    console.log("Error getting count by status: ", data, status);
                }



                $http.get(JobData.RUNNING_OR_FAILED_COUNTS_URL).then( successFn, errorFn);

        };

        function refresh(){
            getRunningFailedCounts();
        }

        function updateCounts(responseData) {
            //zero them out
            self.running =0;
            self.failed = 0;
            if(responseData){

                angular.forEach(responseData,function(statusCount,i){
                    if(statusCount.status == 'RUNNING'){
                        self.running = statusCount.count;
                    }
                    else if(statusCount.status =='FAILED'){
                        self.failed = statusCount.count;
                    }
                })
            }
        }


        function addChartData(data){
            if(self.chartData.length >0) {
                self.chartData[0].values.push([data.date, data.count]);
            }
            else {
               var initialChartData = ChartJobStatusService.toChartData([data]);
                initialChartData[0].key = 'Running';
                self.chartData = initialChartData;
            }
            var max = d3.max(self.runningCounts, function(d) {
                return d.count; } );
            if(max == undefined || max ==0) {
                max = 1;
            }
            else {
                max +=1;
            }
            if(self.chartOptions.chart.yAxis.ticks != max) {
                self.chartOptions.chart.yDomain = [0, max];
                var ticks = max;
                if(ticks > 8){
                    ticks = 8;
                }
                self.chartOptions.chart.yAxis.ticks = ticks;
            }
        }


        function createChartData(responseData){
            self.chartData = ChartJobStatusService.toChartData(responseData);
            var max = d3.max(self.runningCounts, function(d) {
                return d.count; } );
            if(max == undefined || max ==0) {
                max = 1;
            }
            else {
                max +=1;
            }
            self.chartOptions.chart.yDomain = [0, max];
            self.chartOptions.chart.yAxis.ticks =max;
          //  self.chartApi.update();
        }


        this.clearRefreshInterval = function() {
            if(self.refreshInterval != null){
                $interval.cancel(self.refreshInterval);
                self.refreshInterval = null;
            }
        }

        this.setRefreshInterval = function() {
            self.clearRefreshInterval();
            if(self.refreshIntervalTime) {
                self.refreshInterval = $interval(refresh,self.refreshIntervalTime);

            }
        }

        this.init = function () {
            refresh();
       //     self.getJobCountByStatus();
            self.setRefreshInterval();
        }

        this.init();

        $scope.$on('$destroy', function(){
            self.clearRefreshInterval();
        });
    };

    angular.module(MODULE_OPERATIONS).controller('JobStatusIndicatorController', controller);


    angular.module(MODULE_OPERATIONS)
        .directive('tbaJobStatusIndicator', directive);

}());

