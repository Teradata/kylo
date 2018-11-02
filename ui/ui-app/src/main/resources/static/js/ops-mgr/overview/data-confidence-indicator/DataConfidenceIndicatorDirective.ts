import * as angular from "angular";
import {moduleName} from "../module-name";
import "pascalprecht.translate";
import * as _ from 'underscore';

export class controller implements ng.IComponentController{
refreshing: boolean = false;
dataLoaded: boolean = false;
dataMap: any = {'Healthy':{count:0, color:'#5cb85c'},'Unhealthy':{count:0,color:'#a94442'}};
allData: any = null;
chartApi: any = {};
chartOptions: any;
chartData: any[];

static readonly $inject = ["$scope","$element","$http","$interval","$mdDialog","OpsManagerJobService","OpsManagerDashboardService","BroadcastService", "$filter"];

$onInit() {
    this.ngOnInit();
}

ngOnInit() {

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
            donutRatio:0.62,
            showLegend:false,
            refreshDataOnly: false,
            valueFormat: (d: any)=>{
                return parseInt(d);
            },
            color:(d: any)=>{
                if(d.key == 'Healthy'){
                    return '#388e3c';
                }
                else if( d.key== 'Unhealthy'){
                    return '#b71c1c';
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

    this.initializePieChart();
    this.watchDashboard();
    
}

constructor(private $scope: IScope,
            private $element: JQuery, 
            private $http: angular.IHttpService,
            private $interval: angular.IIntervalService,
            private $mdDialog: angular.material.IDialogService,
            private OpsManagerJobService: any,
            private OpsManagerDashboardService: any,
            private BroadcastService: any,
            private $filter: angular.IFilterService){
                
                $scope.$on('$destroy',  ()=> {
                    //cleanup
                });
         }// end of constructor
       
      openDetailsDialog = (key: any)=>{
                    this.$mdDialog.show({
                        controller:["$scope","$mdDialog","$interval","StateService","status","allChartData",
                        ($scope: any,$mdDialog: any,$interval: any,StateService: any,status: any,allChartData:any)=>{
            
                            var jobs:any[];
                            $scope.jobs = jobs;
            
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
                        }],
                        templateUrl: './data-confidence-details-dialog.html',
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

}

angular.module(moduleName).component('tbaDataConfidenceIndicator',{
    controller: controller,
    bindings: {
        panelTitle: "@"
    },
    controllerAs: "vm",
    templateUrl: "./data-confidence-indicator-template.html"
});