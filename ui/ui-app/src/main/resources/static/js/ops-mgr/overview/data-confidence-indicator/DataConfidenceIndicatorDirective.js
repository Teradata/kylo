define(['angular','ops-mgr/overview/module-name'], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            scope: true,
            bindToController: {
                panelTitle: "@"
            },
            controllerAs: 'vm',
            templateUrl: 'js/ops-mgr/overview/data-confidence-indicator/data-confidence-indicator-template.html',
            controller: "DataConfidenceIndicatorController",
            link: function ($scope, element, attrs) {
                $scope.$on('$destroy', function () {

                });
            } //DOM manipulation\}
        }

    };

    var controller = function ($scope, $element, $http, $interval,$mdDialog, OpsManagerJobService,OpsManagerDashboardService,BroadcastService) {
        var self = this;
        this.refreshing = false;
        this.dataLoaded = false;

        this.dataMap = {'Healthy':{count:0, color:'#5cb85c'},'Unhealthy':{count:0,color:'#a94442'}};

        /** All the data returned from the rest call
         *
         * @type {null}
         */
        self.allData = null;
        this.chartApi = {};

        this.chartOptions = {
            chart: {
                type: 'pieChart',
                x: function(d){return d.key;},
                y: function(d){return d.value;},
                showLabels: false,
                duration: 100,
                height:150,
                labelThreshold: 0.01,
                labelSunbeamLayout: false,
                "margin":{"top":10,"right":10,"bottom":10,"left":10},
                donut:true,
                donutRatio:0.65,
                showLegend:false,
                refreshDataOnly: false,
                valueFormat: function(d){
                    return parseInt(d);
                },
                color:function(d){
                    if(d.key == 'Healthy'){
                        return '#009933';
                    }
                    else if( d.key== 'Unhealthy'){
                        return '#FF0000';
                    }
                },
                pie: {
                    dispatch: {
                        'elementClick': function(e){
                            self.openDetailsDialog(e.data.key);
                        }
                    }
                },
                dispatch: {

                }
            }
        };
        this.chartData = [];

        this.openDetailsDialog = function(key){
            $mdDialog.show({
                controller:"DataConfidenceDetailsDialogController",
                templateUrl: 'js/ops-mgr/overview/data-confidence-indicator/data-confidence-details-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: true,
                fullscreen: true,
                locals: {
                    status: key,
                    allChartData: self.allData
                }
            });
        }

        this.onHealthyClick = function(){
            if(self.dataMap.Healthy.count >0){
                self.openDetailsDialog('Healthy');
            }
        }

        this.onUnhealthyClick = function(){
            if(self.dataMap.Unhealthy.count >0){
                self.openDetailsDialog('Unhealthy');
            }
        }



        this.updateChart = function(){
            angular.forEach(self.chartData,function(row,i){
                row.value = self.dataMap[row.key].count;
            });
            var title = (self.dataMap.Healthy.count+self.dataMap.Unhealthy.count)+" Total";
            self.chartOptions.chart.title=title
            self.dataLoaded = true;
            if(self.chartApi.update) {
                self.chartApi.update();
            }
        }


        function initializePieChart() {
            self.chartData.push({key: "Healthy", value: 0})
            self.chartData.push({key: "Unhealthy", value: 0})
        }


        this.getDataConfidenceSummary = function () {
            if (self.refreshing == false) {
                self.refreshing = true;
                    var data = OpsManagerDashboardService.dashboard.dataConfidenceSummary;
                    if(angular.isDefined(data)) {
                        self.allData = data;
                        if (self.dataLoaded == false) {
                            self.dataLoaded = true;
                        }
                        self.dataMap.Healthy.count = data.successCount;
                        self.dataMap.Unhealthy.count = data.failedCount;
                    }
                    self.updateChart();
                }
                 self.refreshing = false;
        }

        function watchDashboard() {
            BroadcastService.subscribe($scope,OpsManagerDashboardService.DASHBOARD_UPDATED,function(dashboard){
                self.getDataConfidenceSummary();
            });
        }


        this.init = function () {
            initializePieChart();
           watchDashboard();
        }

        this.init();

        $scope.$on('$destroy', function () {
            //cleanup
        });
    };


    var DataConfidenceDetailsDialogController = function ($scope, $mdDialog, $interval,StateService,status,allChartData) {
        var self = this;

        $scope.jobs = [];

        if(status == 'Unhealthy') {
            $scope.css = "md-warn";
            $scope.jobs = allChartData.failedJobs;
        } else {
            $scope.css = "";
            $scope.jobs = _.filter(allChartData.latestCheckDataFeeds,function(job) {
                return job.status != 'FAILED';
            });
        }

        $scope.allChartData = allChartData;
        $scope.status = status;

        $scope.hide = function () {
            $mdDialog.hide();

        };

        $scope.gotoJobDetails = function(jobExecutionId){
            $mdDialog.hide();
            StateService.OpsManager().Job().navigateToJobDetails(jobExecutionId);
        }

        $scope.cancel = function () {
            $mdDialog.cancel();
        };

    };

    angular.module(moduleName).controller('DataConfidenceDetailsDialogController', ["$scope","$mdDialog","$interval","StateService","status","allChartData",DataConfidenceDetailsDialogController]);




    angular.module(moduleName).controller('DataConfidenceIndicatorController',["$scope","$element","$http","$interval","$mdDialog","OpsManagerJobService","OpsManagerDashboardService","BroadcastService", controller]);


    angular.module(moduleName)
        .directive('tbaDataConfidenceIndicator', directive);

});
