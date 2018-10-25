import * as angular from "angular";
import {moduleName} from "./module-name";
import ServicesStatusData from "../services/ServicesStatusService";
import {Transition} from "@uirouter/core";
import {DefaultTableOptionsService} from "../../services/TableOptionsService";
import {DefaultPaginationDataService} from "../../services/PaginationDataService";
import './module-require';

export class controller implements ng.IComponentController{

        pageName: string;
        cardTitle: string;
        //Page State
        loading: boolean = true;
        showProgress: boolean = true;
        component: any = {alerts:[]};
        totalAlerts: number = 0;
        componentName : any;
        serviceName: any;

        //Pagination and view Type (list or table)
        paginationData: any;
        viewType: string;
        currentPage: number;
        filter: any;
        sortOptions: any;
        service: any;
        
        $transition$: Transition;
        static readonly $inject = ["$scope","$http","$filter","$interval","$timeout","$q","ServicesStatusData","TableOptionsService","PaginationDataService"];
        
        $onInit() {
            this.ngOnInit();
        }
        
        ngOnInit() {

            this.pageName = 'service-component-details';
            this.cardTitle = 'Service Component Alerts';
            this.componentName= this.$transition$.params().componentName;
            this.serviceName = this.$transition$.params().serviceName;
            this.paginationData = this.paginationDataService.paginationData(this.pageName);
            this.paginationDataService.setRowsPerPageOptions(this.pageName,['5','10','20','50']);
            this.viewType = this.paginationDataService.viewType(this.pageName);
            this.currentPage = this.paginationDataService.currentPage(this.pageName)||1;
            this.filter = this.paginationDataService.filter(this.pageName);
            this.sortOptions = this.loadSortOptions();
            this.service = this.ServicesStatusData.services[this.serviceName];

            if(this.service){
                this.component = this.service.componentMap[this.componentName];
            };

        }

        constructor(private $scope: IScope,
            private $http: angular.IHttpService,
            private $filter: angular.IFilterService,
            private $interval: angular.IIntervalService,
            private $timeout: angular.ITimeoutService,
            private $q: angular.IQService,
            private ServicesStatusData: any,
            private tableOptionsService: DefaultTableOptionsService,
            private paginationDataService: DefaultPaginationDataService){

            $scope.$watch(()=>{return ServicesStatusData.services;}
                                ,(newVal:  any)=> {
                                        if(newVal[this.serviceName]){
                                            var updatedComponent = newVal[this.serviceName].componentMap[this.componentName];
                                            if(updatedComponent != this.component){
                                                this.component = updatedComponent;
                                            }
                                        }
                                    }
            );            

            $scope.$watch(()=>{ return this.viewType; },
                          (newVal: any)=> { this.onViewTypeChange(newVal);}
                        )
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
            var options = {'Component Name':'componentName','Alert':'alert','Update Date':'latestAlertTimestamp'};

            var sortOptions = this.tableOptionsService.newSortOptions(this.pageName,options,'latestAlertTimestamp','asc');
            this.tableOptionsService.initializeSortOption(this.pageName);
            return sortOptions;

        }

        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        selectedTableOption = (option: any) =>{
            var sortString = this.tableOptionsService.toSortString(option);
            var savedSort = this.paginationDataService.sort(this.pageName, sortString);
            var updatedOption = this.tableOptionsService.toggleSort(this.pageName, option);
            this.tableOptionsService.setSortOption(this.pageName, sortString);
        }
}

const module = angular.module(moduleName).component("serviceComponentHealthDetailsController", {
    controller: controller,
    bindings: {
        $transition$: "<"
    },
    controllerAs: "vm",
    templateUrl: "./service-component-detail.html"
});
export default module;

