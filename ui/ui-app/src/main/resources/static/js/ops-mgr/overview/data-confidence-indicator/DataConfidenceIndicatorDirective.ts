import * as angular from "angular";
import {moduleName} from "../module-name";
import "pascalprecht.translate";
import * as _ from 'underscore';

export class controller implements ng.IComponentController{
refreshing: any;
dataLoaded: any;
dataMap: any;
allData: any;
chartApi: any;
chartOptions: any;
chartData: any[];
constructor(private $scope: any,
            private $element: any, 
            private $http: any,
            private $interval: any,
            private $mdDialog: any,
            private OpsManagerJobService: any,
            private OpsManagerDashboardService: any,
            private BroadcastService: any,
            private $filter: any){
                this.refreshing = false;
                this.dataLoaded = false;

                this.dataMap = {'Healthy':{count:0, color:'#5cb85c'},'Unhealthy':{count:0,color:'#a94442'}};

                /** All the data returned from the rest call
                 *
                 * @type {null}
                 */
                this.allData = null;
                this.chartApi = {};

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
                        donutRatio:0.65,
                        showLegend:false,
                        refreshDataOnly: false,
                        valueFormat: (d: any)=>{
                            return parseInt(d);
                        },
                        color:(d: any)=>{
                            if(d.key == 'Healthy'){
                                return '#009933';
                            }
                            else if( d.key== 'Unhealthy'){
                                return '#FF0000';
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

                this.init();

                $scope.$on('$destroy',  ()=> {
                    //cleanup
                });
         }// end of constructor
       
      openDetailsDialog = (key: any)=>{
                    this.$mdDialog.show({
                        controller:"DataConfidenceDetailsDialogController",
                        templateUrl: 'js/ops-mgr/overview/data-confidence-indicator/data-confidence-details-dialog.html',
                        parent: angular.element(document.body),
                        clickOutsideToClose: true,
                        fullscreen: true,
                        locals: {
                            status: key,
                            allChartData: this.allData
                        }
                    });
                }
        onHealthyClick = ()=>{
            if(this.dataMap.Healthy.count >0){
                this.openDetailsDialog('Healthy');
            }
        }

        onUnhealthyClick = ()=>{
            if(this.dataMap.Unhealthy.count >0){
                this.openDetailsDialog('Unhealthy');
            }
        }

        updateChart = ()=>{
            angular.forEach(this.chartData,(row: any,i: any)=>{
                row.value = this.dataMap[row.key].count;
            });
            var title = (this.dataMap.Healthy.count+this.dataMap.Unhealthy.count)+" " + this.$filter('translate')('Total');
            this.chartOptions.chart.title=title
            this.dataLoaded = true;
            if(this.chartApi.update) {
                this.chartApi.update();
            }
        }


        initializePieChart= function() {
            this.chartData.push({key: "Healthy", value: 0})
            this.chartData.push({key: "Unhealthy", value: 0})
        }


        getDataConfidenceSummary =  ()=> {
            if (this.refreshing == false) {
                this.refreshing = true;
                    var data = this.OpsManagerDashboardService.dashboard.dataConfidenceSummary;
                    if(angular.isDefined(data)) {
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


        init = function () {
            this.initializePieChart();
            this.watchDashboard();
        }
}

export class DataConfidenceDetailsDialogController implements ng.IComponentController{
jobs: any[];
    constructor(private $scope: any,
                private $mdDialog: any,
                private $interval: any,
                private StateService: any,
                private status: any,
                private allChartData:any){

                $scope.jobs = this.jobs;

                if(status == 'Unhealthy') {
                    $scope.css = "md-warn";
                    $scope.jobs = allChartData.failedJobs;
                } else {
                    $scope.css = "";
                    $scope.jobs = _.filter(allChartData.latestCheckDataFeeds,(job: any)=> {
                        return job.status != 'FAILED';
                    });
                }

                $scope.allChartData = allChartData;
                $scope.status = status;

                $scope.hide =  ()=> {
                    $mdDialog.hide();

                };

                $scope.gotoJobDetails = (jobExecutionId: any)=>{
                    $mdDialog.hide();
                    StateService.OpsManager().Job().navigateToJobDetails(jobExecutionId);
                }

                $scope.cancel =  ()=> {
                    $mdDialog.cancel();
                };
                }
}


angular.module(moduleName).controller('DataConfidenceDetailsDialogController', ["$scope","$mdDialog","$interval","StateService","status","allChartData",DataConfidenceDetailsDialogController]);
angular.module(moduleName).controller('DataConfidenceIndicatorController',["$scope","$element","$http","$interval","$mdDialog","OpsManagerJobService","OpsManagerDashboardService","BroadcastService", "$filter", controller]);
angular.module(moduleName)
        .directive('tbaDataConfidenceIndicator',[ ()=> {
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
                    }}]);