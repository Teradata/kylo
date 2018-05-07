import * as angular from "angular";
import {moduleName} from "../module-name";
import "pascalprecht.translate";



export default class controller implements ng.IComponentController {
    chartApi: any;
    dataLoaded: any;
    feedSummaryData: any;
    chartData: any[];
    dataMap: any;
    chartOptions: any;
    constructor(private $scope: any,
                private $element: any,
                private $http: any,
                private $interval: any,
                private $timeout: any,
                private OpsManagerFeedService: any,
                private OpsManagerDashboardService: any,
                private BroadcastService: any,
                private $filter: any){
       this.chartApi = {};
        this.dataLoaded = false;
        this.feedSummaryData = null;
        this.chartData = [];
        this.dataMap = {'Healthy':{count:0, color:'#009933'},'Unhealthy':{count:0,color:'#FF0000'}};

        this.chartOptions = {
            chart: {
                type: 'pieChart',
                x: (d: any)=>{return d.key;},
                y: (d: any)=>{return d.value;},
                showLabels: false,
                duration: 100,
                height:150,
                transitionDuration:500,
                labelThreshold: 0.01,
                labelSunbeamLayout: false,
                "margin":{"top":10,"right":10,"bottom":10,"left":10},
                donut:true,
                donutRatio:0.65,
                showLegend:false,
                refreshDataOnly: false,
                color:(d: any)=>{
                    return this.dataMap[d.key].color;
                },
                valueFormat: (d: any)=>{
                    return parseInt(d);
                },
                pie: {
                    dispatch: {
                        'elementClick': (e: any)=>{
                           this.onChartElementClick(e.data.key);
                        }
                    }
                },
                dispatch: {

                }
            }
        };

            this.init();
            $scope.$on('$destroy',  ()=> {
            });
        }// end of constructor

        initializePieChart=()=> {
                this.chartData.push({key: "Healthy", value: 0})
                this.chartData.push({key: "Unhealthy", value: 0})
        }

        onHealthyClick = ()=>{
            this.OpsManagerDashboardService.selectFeedHealthTab('Healthy');
        }

        onUnhealthyClick = ()=>{
            this.OpsManagerDashboardService.selectFeedHealthTab('Unhealthy');
        }

        init=()=>{
            this.initializePieChart();
            this.watchDashboard();
        }


    watchDashboard=()=> {
            this.BroadcastService.subscribe(this.$scope,
                                            this.OpsManagerDashboardService.DASHBOARD_UPDATED,
                                            (dashboard: any)=>{
                this.dataMap.Unhealthy.count = this.OpsManagerDashboardService.feedUnhealthyCount;

                this.dataMap.Healthy.count = this.OpsManagerDashboardService.feedHealthyCount;
                this.feedSummaryData = this.OpsManagerDashboardService.feedSummaryData;
                this.updateChartData();
            });
        }

        onChartElementClick=(key: any)=>{
            this.OpsManagerDashboardService.selectFeedHealthTab(key);
        }

        updateChartData= ()=>{
            angular.forEach(this.chartData,(row: any,i: any)=>{
                row.value = this.dataMap[row.key].count;
            });
            var title = (this.dataMap.Healthy.count+this.dataMap.Unhealthy.count)+" "+ this.$filter('translate')('Total');
            this.chartOptions.chart.title=title
            this.dataLoaded = true;
            if(this.chartApi.update) {
                this.chartApi.update();
            }
        }

        updateChart = ()=>{
            if(this.chartApi.update) {
                this.chartApi.update();
            }
        }

}

angular.module(moduleName)
   .controller('FeedStatusIndicatorController', ["$scope","$element","$http","$interval","$timeout","OpsManagerFeedService","OpsManagerDashboardService","BroadcastService","$filter",controller]);


    angular.module(moduleName)
        .directive('tbaFeedStatusIndicator', [()=> {
        
        return {
            restrict: "EA",
            scope:true,
            controllerAs:'vm',
            bindToController: {
                panelTitle: "@"
            },
            templateUrl: 'js/ops-mgr/overview/feed-status-indicator/feed-status-indicator-template.html',
            controller: "FeedStatusIndicatorController",
            link: function ($scope: any, element: any, attrs: any) {

            }
        }
        }]);
