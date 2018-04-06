import * as angular from 'angular';
import {moduleName} from "./module-name";
import "pascalprecht.translate";
declare const d3: any;

export class controller implements ng.IComponentController{
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
    feedName: any;
    constructor(private $scope: any,
                private $http: any,
                private $interval: any,
                private $timeout: any,
                private $q: any,
                private Utils: any,
                private OpsManagerFeedService: any,
                private TableOptionsService: any,
                private PaginationDataService: any,
                private StateService: any,
                private ChartJobStatusService: any,
                private BroadcastService: any, 
                private $filter: any){

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
                x: (d: any)=>{return d[0];},
                y: (d: any)=>{return d[1];},
                useVoronoi: false,
                clipEdge: false,
                duration: 250,
                useInteractiveGuideline: true,
                noData:  $filter('translate')('views.views.FeedActivityDirective.noData'),                
                xAxis: {
                    axisLabel: $filter('translate')('views.FeedActivityDirective.Date'),
                    showMaxMin: false,
                    tickFormat: (d: any)=> {
                        return d3.time.format('%x')(new Date(d))
                    }
                },
                yAxis: {
                    axisLabel: $filter('translate')('views.FeedActivityDirective.Count'),
                    axisLabelDistance: -10
                },
                dispatch: {
                    renderEnd: ()=> {
                        this.fixChartWidth();
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

        BroadcastService.subscribe($scope, 'ABANDONED_ALL_JOBS', this.updateCharts);

    $scope.$watch(()=>{
            return this.dateSelection;
        },(newVal: any)=> {
            this.parseDatePart();
            this.query();
        });
    }// end of constructor
    
     updateCharts=()=> {
            this.query();
            this.updateChart();
     }

    updateChart = ()=>{
            if(this.chartApi.update) {
                this.chartApi.update();
            }
        };

     fixChartWidth=()=> {
            var chartWidth = parseInt($($('.nvd3-svg')[0]).find('rect:first').attr('width'));
            if(chartWidth < 100){
                this.updateChart();
                if(this.fixChartWidthCounter == undefined) {
                    this.fixChartWidthCounter = 0;
                }
                this.fixChartWidthCounter++;
                if(this.fixChartWidthTimeout){
                    this.$timeout.cancel(this.fixChartWidthTimeout);
                }
                if(this.fixChartWidthCounter < 1000) {
                    this.fixChartWidthTimeout = this.$timeout(()=> {
                        this.fixChartWidth();
                    }, 10);
                }
            }
            else {
                if(this.fixChartWidthTimeout){
                    this.$timeout.cancel(this.fixChartWidthTimeout);
                }
                this.fixChartWidthCounter = 0;
            }
        }

        

        createChartData=(responseData: any)=>{
                this.chartData = this.ChartJobStatusService.toChartData(responseData);

        }

        parseDatePart=()=>{
            var interval = parseInt(this.dateSelection.substring(0,this.dateSelection.indexOf('-')));
            var datePart = this.dateSelection.substring(this.dateSelection.indexOf('-')+1);
            this.datePart = datePart
            this.interval = interval;
        }

        query=()=>{
            var successFn = (response: any)=> {
                if (response.data) {
                    //transform the data for UI
                    this.createChartData(response.data);
                    if (this.loading) {
                        this.loading = false;
                    }
                    if(!this.dataLoaded && response.data.length ==0){
                        setTimeout(()=>{
                            this.dataLoaded =true;
                        },500)
                    }
                   else {
                        this.dataLoaded =true;
                    }
                }
            }
            var errorFn = (err: any)=> {
            }
            var finallyFn = ()=> {
            }

            this.$http.get(this.OpsManagerFeedService.DAILY_STATUS_COUNT_URL(this.feedName),{params:{"period":this.interval+this.datePart}}).then( successFn, errorFn);
        }
}

  angular.module(moduleName)
  .controller('FeedActivityController', ["$scope","$http","$interval","$timeout","$q","Utils",
  "OpsManagerFeedService","TableOptionsService","PaginationDataService","StateService",
  "ChartJobStatusService","BroadcastService","$filter",controller]);

    angular.module(moduleName)
        .directive('tbaFeedActivity', [ (Utils: any)=> {
                    return {
                            restrict: "EA",
                            bindToController: {
                            feedName:"="
                            },
                            controllerAs: 'vm',
                            scope: {},
                            templateUrl: 'js/ops-mgr/feeds/feed-activity-template.html',
                            controller: "FeedActivityController",
                            link:  ($scope: any, element: any, attrs: any, controller: any)=> {
                            },
                            compile: ()=> {
                                return (scope: any, element: any, attr: any) =>{ //postCompile = 
                                // Utils.replaceWithChild(element);
                                };
                            }
                        };
                }
        ]);