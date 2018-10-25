import * as angular from "angular";
import {moduleName} from "./module-name";
import * as _ from 'underscore';
import {TransitionService, StateService} from "@uirouter/core";
import {DefaultTableOptionsService} from "../../services/TableOptionsService";
import {DefaultPaginationDataService} from "../../services/PaginationDataService";
import ServicesStatusData from "../services/ServicesStatusService";
import {Transition} from "@uirouter/core";
import './module-require';


export class controller implements ng.IComponentController{
pageName: string;
cardTitle: string;
//Page State
loading: boolean;
showProgress: boolean;
totalComponents: number;
serviceName: any;

//Pagination and view Type (list or table)
paginationData: any;
viewType: string;
currentPage: number;
filter: any;
sortOptions: any;
service: any;

$transition$: Transition;

static readonly $inject = ["$scope","$http","$filter","$interval","$timeout","$q","ServicesStatusData","TableOptionsService","PaginationDataService","StateService"];

$onInit() {
    this.ngOnInit();
}

ngOnInit() {
    this.pageName = 'service-details';
    this.cardTitle = 'Service Components';
    //Page State
    this.loading = true;
    this.showProgress = true;
    this.service = {components:[]};
    this.totalComponents = 0;
    this.serviceName = this.$transition$.params().serviceName;

        //Pagination and view Type (list or table)
    this.paginationData = this.paginationDataService.paginationData(this.pageName);
    this.paginationDataService.setRowsPerPageOptions(this.pageName,['5','10','20','50']);
    this.viewType = this.paginationDataService.viewType(this.pageName);
    this.currentPage = this.paginationDataService.currentPage(this.pageName)||1;
    this.filter = this.paginationDataService.filter(this.pageName);
    this.sortOptions = this.loadSortOptions();

    this.service = this.ServicesStatusData.services[this.serviceName];
        if(_.isEmpty(this.ServicesStatusData.services)){
            this.ServicesStatusData.fetchServiceStatus();
        }
}

constructor(private $scope: IScope,
            private $http: angular.IHttpService,
            private $filter: angular.IFilterService,
            private $interval: angular.IIntervalService,
            private $timeout: angular.ITimeoutService,
            private $q: angular.IQService,
            private ServicesStatusData: any,
            private tableOptionsService: DefaultTableOptionsService,
            private paginationDataService: DefaultPaginationDataService,
            private stateService: StateService){
                
                $scope.$watch(()=>{
                    return ServicesStatusData.services;
                },(newVal: any)=> {
                    if(newVal[this.serviceName]){
                        if(newVal[this.serviceName] != this.service){
                            this.service = newVal[this.serviceName];
                        }
                    }
                },true);

                $scope.$watch(()=>{
                    return this.viewType;
                },(newVal: any)=> {
                    this.onViewTypeChange(newVal);
                })
            }// end of constructor

          paginationId = function(){
            return this.paginationDataService.paginationId(this.pageName);
        }


        onViewTypeChange = (viewType: any) =>{
            this.paginationDataService.viewType(this.pageName, this.viewType);
        }

        //Tab Functions
        onOrderChange = (order: any) =>{
            this.paginationDataService.sort(this.pageName,order);
            this.tableOptionsService.setSortOption(this.pageName,order);
             //   return loadJobs(true).promise;
            //return this.deferred.promise;
        };

        onPaginationChange = (page: any, limit: any) =>{
            this.paginationDataService.currentPage(this.pageName,null,page);
            this.currentPage = page;
        };


             //Sort Functions
        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        loadSortOptions= function() {
            var options = {'Component Name': 'name', 'Components': 'componentsCount', 'Alerts': 'alertsCount', 'Update Date': 'latestAlertTimestamp'};
            var sortOptions = this.tableOptionsService.newSortOptions(this.pageName,options,'name','asc');
            this.tableOptionsService.initializeSortOption(this.pageName);
            return sortOptions;
        }

        serviceComponentDetails = function(event: any,component: any) {
            this.stateService.OpsManager().ServiceStatus().navigateToServiceComponentDetails(this.serviceName, component.name);
        }


        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        selectedTableOption = (option: any)=> {
            var sortString = this.tableOptionsService.toSortString(option);
            var savedSort = this.paginationDataService.sort(this.pageName, sortString);
            var updatedOption = this.tableOptionsService.toggleSort(this.pageName, option);
            this.tableOptionsService.setSortOption(this.pageName, sortString);
        }
}

const module = angular.module(moduleName).component("serviceHealthDetailsController", {
    controller: controller,
    bindings: {
        $transition$: "<"
    },
    controllerAs: "vm",
    templateUrl: "./service-detail.html"
});
export default module;
