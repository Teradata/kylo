import * as angular from "angular";
import {moduleName} from "../module-name";
import "pascalprecht.translate";
import * as _ from 'underscore';
import OpsManagerDashboardService from "../../services/OpsManagerDashboardService";
import ServicesStatusData from "../../services/ServicesStatusService";
import * as moment from "moment";

export default class controller implements ng.IComponentController{
dataLoaded: any;
chartApi: any;
chartOptions: any;
chartData: any[];
indicator: any;
openAlerts: any[] = [];
counts: any;
percent: any;
dateTime: any;
allServices: any;
grouped: any;
healthClass: any;
validateTitleTimeout: any;

    constructor(private $scope: any,
                private $element: any,
                private $http: any,
                private $mdDialog: any,
                private $mdPanel: any,
                private $interval: any,
                private $timeout: any,
                private ServicesStatusData: any,
                private OpsManagerDashboardService: any,
                private BroadcastService: any,
                private $filter: any){
           this.dataLoaded = false;
           this.chartApi = {};

            this.chartOptions = {
                chart: {
                    type: 'pieChart',
                    x: (d: any)=>{return d.key;},
                    y: (d: any)=>{return d.value;},
                    showLabels: false,
                    duration: 100,
                    "height": 150,
                    labelThreshold: 0.01,
                    labelSunbeamLayout: false,
                    "margin":{"top":10,"right":10,"bottom":10,"left":10},
                    donut:true,
                    donutRatio:0.65,
                    showLegend:false,
                    valueFormat: (d: any)=>{
                        return parseInt(d);
                    },
                    color:(d: any)=>{
                        if(d.key == 'HEALTHY'){
                            return '#009933';
                        }
                        else if( d.key== 'UNHEALTHY'){
                            return '#FF0000';
                        }
                        else if(d.key == 'WARNING') {
                            return '#FF9901';
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
                        renderEnd:  ()=> {

                        }
                    }
                }
            };
        this.chartData = [];
        this.chartData.push({key: "HEALTHY", value: 0});
        this.chartData.push({key: "UNHEALTHY", value: 0});
        this.chartData.push({key: "WARNING", value: 0});

         this.validateTitle();

          this.indicator = {
            openAlerts: [],
            toggleComponentAlert:  (event: any, component: any)=> {
                var target = event.target;
                var parentTdWidth = $(target).parents('td:first').width();
                component.alertDetailsStyle = 'width:' + parentTdWidth + 'px;';
                if (component.showAlerts == true) {

                    var alertIndex = _.indexOf(this.indicator.openAlerts, component);
                    if (alertIndex >= 0) {
                        this.indicator.openAlerts.splice(alertIndex, 1);
                    }
                    component.showAlerts = false;
                }
                else {
                    this.indicator.openAlerts.push(component);
                    component.showAlerts = true;
                }
            },
            allServices: [],
            counts: {errorCount: 0, allCount: 0, upCount: 0, downCount: 0, warningCount: 0},
            grouped: {
                "HEALTHY": {label: "Healthy", styleClass: "status-healthy", count: 0, data: []},
                "WARNING": {label: "Warnings", styleClass: "status-warnings", count: 0, data: []},
                "UNHEALTHY": {label: "UNHEALTHY", styleClass: "status-errors", count: 0, data: []}
            },
            percent: 0,
            dateTime: null,
            reset:  ()=> {
                this.openAlerts = [];
                this.counts = {errorCount: 0, allCount: 0, upCount: 0, downCount: 0, warningCount: 0};
                this.percent = 0;
                this.dateTime = null;
                this.allServices = [];
                angular.forEach(this.indicator.grouped,  (groupData, status)=> {
                    groupData.data = [];
                    groupData.count = 0;
                })
            },
            addService:  (service: any)=>{
                var displayState = service.state == "UP" ? "HEALTHY" : (service.state == "DOWN" ? "UNHEALTHY" : service.state);
                this.indicator.grouped[displayState].data.push(service);
                this.indicator.grouped[displayState].count++;
                service.latestAlertTimeAgo = null;
                //update timeAgo text
                if (service.latestAlertTimestamp != null) {
                    service.latestAlertTimeAgo = moment(service.latestAlertTimestamp).from(moment());
                }
            },
            checkToShowClusterName:(service: any)=>{
              if(service && service.components){
                  var componentNames = _.map(service.components,(component: any)=>{
                      return component.name;
                  });
                  var unique = _.uniq(componentNames);
                  if(componentNames.length != unique.length){
                      service.showClusterName = true;
                  }
                  else {
                      service.showClusterName = false;
                  }
              }
            },
            addServices: (services: any) =>{
                if (this.openAlerts.length == 0) {
                    this.indicator.reset();
                    this.allServices = services;
                    angular.forEach(services,  (service: any, i: any)=> {
                        this.indicator.addService(service);
                        service.componentCount = service.components.length;
                        service.healthyComponentCount = service.healthyComponents.length;
                        service.unhealthyComponentCount = service.unhealthyComponents.length;
                        this.indicator.checkToShowClusterName(service);
                    });

                    this.indicator.updateCounts();
                    this.indicator.updatePercent();


                    this.dateTime = new Date();
                }
            },
            updateCounts:()=>{
                this.counts.upCount = this.indicator.grouped["HEALTHY"].count;
                this.counts.allCount = this.allServices.length;
                this.counts.downCount = this.indicator.grouped["UNHEALTHY"].count;
                this.counts.warningCount = this.indicator.grouped["WARNING"].count;
                this.counts.errorCount = this.counts.downCount + this.counts.warningCount;
                angular.forEach(this.chartData,(item,i)=> {
                    item.value = this.indicator.grouped[item.key].count;
                })
                this.chartOptions.chart.title = this.counts.allCount+" "+ $filter('translate')('Total');
            },
            updatePercent: ()=> {
                if (this.counts.upCount > 0) {
                    this.percent = (this.counts.upCount / this.counts.allCount) * 100;
                    this.percent = Math.round(this.percent);
                }
                if (this.percent <= 50) {
                    this.healthClass = "errors";
                }
                else if (this.percent < 100) {
                    this.healthClass = "warnings";
                }
                else {
                    this.healthClass = "success";
                }
            }
        }

        $scope.$on('$destroy',  ()=> {
            //cleanup
        });
        this.init();
    }// end of constructor

      watchDashboard=()=> {
        this.BroadcastService.subscribe(this.$scope,
                                        this.OpsManagerDashboardService.DASHBOARD_UPDATED,
                                        (dashboard: any)=>{

                this.ServicesStatusData.transformServicesResponse(this.OpsManagerDashboardService.dashboard.serviceStatus);
                var services = this.ServicesStatusData.services;
                var servicesArr = [];
                for(var k in services) {
                    servicesArr.push(services[k]);
                }
                this.indicator.addServices(servicesArr);
                this.dataLoaded = true;
            });
        }


        openDetailsDialog = (key: any)=>{
            this.$mdDialog.show({
                controller:"ServicesDetailsDialogController",
                templateUrl: 'js/ops-mgr/overview/services-indicator/services-details-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: true,
                fullscreen: true,
                locals: {
                    status: key,
                    selectedStatusData: this.indicator.grouped[key]
                }
            });
        }



        updateChart = ()=>{
            var title = (this.indicator.counts.allCount)+" " + this.$filter('translate')('Total');
            this.chartOptions.chart.title=title
            if(this.chartApi.update) {
                this.chartApi.update();
            }
        }

        validateTitle=()=>{
            if(this.validateTitleTimeout != null){
                this.$timeout.cancel(this.validateTitleTimeout);
            }
            var txt = this.$element.find('.nv-pie-title').text();
            if($.trim(txt) == "0 Total" && this.indicator.counts.allCount >0){
                this.updateChart();
            }
            this.$timeout(()=> {this.validateTitle() },1000);

        }

        init=()=>{
            this.watchDashboard();
        }       
}



export class servicesDetailsDialogController implements ng.IComponentController{
constructor(private $scope: any,
            private $mdDialog: any,
            private $interval: any,
            private StateService: any,
            private status: any,
            private selectedStatusData: any){
                $scope.css = status == "UNHEALTHY" ? "md-warn" : "";
                $scope.status = status
                $scope.services = selectedStatusData.data;

                _.each($scope.services,(service: any)=> {
                    service.componentMessage = null;
                    if(service.components.length ==1){
                        service.componentName = service.components[0].name;
                        service.componentMessage =service.components[0].message;
                    }
                });

                $scope.hide =  ()=> {
                    $mdDialog.hide();

                };

                $scope.gotoServiceDetails = (serviceName: any)=>{
                    $mdDialog.hide();
                    StateService.OpsManager().ServiceStatus().navigateToServiceDetails(serviceName);
                }

                $scope.cancel = ()=> {
                    $mdDialog.cancel();
                };
            }
}

angular.module(moduleName)
angular.module(moduleName).controller('ServicesDetailsDialogController', 
                                        ["$scope","$mdDialog","$interval","StateService","status",
                                        "selectedStatusData",servicesDetailsDialogController]);
angular.module(moduleName)
.service('OpsManagerDashboardService',['$q', '$http', '$interval', '$timeout', 'HttpService', 'IconService',
                                     'AlertsService', 'OpsManagerRestUrlService','BroadcastService',
                                     'OpsManagerFeedService',OpsManagerDashboardService])
.service('ServicesStatusData',["$q", '$http', '$interval', '$timeout', 'AlertsService', 'IconService', 
                                'OpsManagerRestUrlService',ServicesStatusData])
.controller('ServicesIndicatorController', ["$scope","$element","$http",
                                        "$mdDialog","$mdPanel","$interval","$timeout",
                                        "ServicesStatusData","OpsManagerDashboardService",
                                        "BroadcastService",'$filter',controller]);

    angular.module(moduleName)
        .directive('tbaServicesIndicator', [()=>
          {
              return {
            restrict: "EA",
            scope: {},
            bindToController: {
                panelTitle: "@"
            },
            controllerAs: 'vm',
            templateUrl: 'js/ops-mgr/overview/services-indicator/services-indicator-template.html',
            controller: "ServicesIndicatorController",
            link: function ($scope: any, element: any, attrs: any) {
                $scope.$on('$destroy',  ()=> {

                });
            } //DOM manipulation\}
        }
        
          }]);