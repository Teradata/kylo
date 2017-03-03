define(['angular','ops-mgr/feeds/module-name'], function (angular,moduleName) {

    var directive = function (Utils) {
        return {
            restrict: "EA",
            bindToController: {
            feedName:"="
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/ops-mgr/feeds/feed-activity-template.html',
            controller: "FeedActivityController",
            link: function ($scope, element, attrs, controller) {

            },
            compile: function() {
                return function postCompile(scope, element, attr) {
                  // Utils.replaceWithChild(element);
                };
            }

        };
    }

    var controller = function ($scope,$http, $interval, $timeout, $q,Utils,OpsManagerFeedService, TableOptionsService, PaginationDataService, AlertsService, StateService, ChartJobStatusService, BroadcastService) {
        var self = this;
        this.pageName = 'feed-activity';
        this.dataLoaded = false;
        this.dateSelection = '1-M';
        this.chartData = [];
        this.chartApi = {};
        this.chartOptions =  {
            chart: {
                type: 'lineChart',
                height: 250,
                margin : {
                    top: 10,
                    right: 20,
                    bottom: 40,
                    left: 55
                },
                x: function(d){return d[0];},
                y: function(d){return d[1];},
                useVoronoi: false,
                clipEdge: false,
                duration: 250,
                useInteractiveGuideline: true,
                xAxis: {
                    axisLabel: 'Date',
                    showMaxMin: false,
                    tickFormat: function(d) {
                        return d3.time.format('%x')(new Date(d))
                    }
                },
                yAxis: {
                    axisLabel:'Count',
                    axisLabelDistance: -10
                },
                dispatch: {
                    renderEnd: function () {
                        fixChartWidth();
                    }
                }
            }
        };
        /*  zoom: {
         enabled: true,
         scaleExtent: [1, 10],
         useFixedDomain: false,
         useNiceScale: false,
         horizontalOff: false,
         verticalOff: true,
         unzoomEventType: 'dblclick.zoom'
         }*/

        BroadcastService.subscribe($scope, 'ABANDONED_ALL_JOBS', updateCharts);

        function updateCharts() {
            query();
            self.updateChart();
        }

        this.updateChart = function(){
            if(self.chartApi.update) {
                self.chartApi.update();
            }
        };

        function fixChartWidth() {
            var chartWidth = parseInt($($('.nvd3-svg')[0]).find('rect:first').attr('width'));
            if(chartWidth < 100){
                self.updateChart();
                if(self.fixChartWidthCounter == undefined) {
                    self.fixChartWidthCounter = 0;
                }
                self.fixChartWidthCounter++;
                if(self.fixChartWidthTimeout){
                    $timeout.cancel(self.fixChartWidthTimeout);
                }
                if(self.fixChartWidthCounter < 1000) {
                    self.fixChartWidthTimeout = $timeout(function () {
                        fixChartWidth();
                    }, 10);
                }
            }
            else {
                if(self.fixChartWidthTimeout){
                    $timeout.cancel(self.fixChartWidthTimeout);
                }
                self.fixChartWidthCounter = 0;
            }
        }

        $scope.$watch(function(){
            return self.dateSelection;
        },function(newVal) {
            parseDatePart();
            query();
        });

        function createChartData(responseData){
                self.chartData = ChartJobStatusService.toChartData(responseData);

        }

        function parseDatePart(){
            var interval = parseInt(self.dateSelection.substring(0,self.dateSelection.indexOf('-')));
            var datePart = self.dateSelection.substring(self.dateSelection.indexOf('-')+1);

            self.datePart = datePart
            self.interval = interval;
        }

        function query(){

            var successFn = function (response) {

                if (response.data) {
                    //transform the data for UI
                    createChartData(response.data);
                    if (self.loading) {
                        self.loading = false;
                    }
                    if(!self.dataLoaded && response.data.length ==0){
                        setTimeout(function(){
                            self.dataLoaded =true;
                        },500)
                    }
                   else {
                        self.dataLoaded =true;
                    }
                }


            }
            var errorFn = function (err) {
            }
            var finallyFn = function () {

            }

            $http.get(OpsManagerFeedService.DAILY_STATUS_COUNT_URL(self.feedName),{params:{"period":self.interval+self.datePart}}).then( successFn, errorFn);

        }


    };


    angular.module(moduleName).controller('FeedActivityController', ["$scope","$http","$interval","$timeout","$q","Utils","OpsManagerFeedService","TableOptionsService","PaginationDataService","AlertsService","StateService","ChartJobStatusService","BroadcastService",controller]);

    angular.module(moduleName)
        .directive('tbaFeedActivity', directive);

});
