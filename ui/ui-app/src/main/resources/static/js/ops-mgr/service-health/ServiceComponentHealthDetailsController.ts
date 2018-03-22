import * as angular from "angular";
import {moduleName} from "./module-name";
import ServicesStatusData from "../services/ServicesStatusService";

export class controller implements ng.IComponentController{

        pageName: string;
        cardTitle: string;
        //Page State
        loading: boolean = true;
        showProgress: boolean = true;
        component: any = {alerts:[]};
        totalAlerts: any = 0;
        componentName : any;
        serviceName: any;

        //Pagination and view Type (list or table)
        paginationData: any;
        viewType: any;
        currentPage: any;
        filter: any;
        sortOptions: any;
        service: any;
        
constructor(private $scope: any,
            private $http: any,
            private $filter: any,
            private $transition$: any,
            private $interval: any,
            private $timeout: any,
            private $q: any,
            private ServicesStatusData: any,
            private TableOptionsService: any,
            private PaginationDataService: any){

            this.pageName = 'service-component-details';
            this.cardTitle = 'Service Component Alerts';
            this.componentName= $transition$.params().componentName;
            this.serviceName = $transition$.params().serviceName;
            this.paginationData = PaginationDataService.paginationData(this.pageName);
            PaginationDataService.setRowsPerPageOptions(this.pageName,['5','10','20','50']);
            this.viewType = PaginationDataService.viewType(this.pageName);
            this.currentPage = PaginationDataService.currentPage(this.pageName)||1;
            this.filter = PaginationDataService.filter(this.pageName);
            this.sortOptions = this.loadSortOptions();
            this.service = ServicesStatusData.services[this.serviceName];

            if(this.service){
                this.component = this.service.componentMap[this.componentName];
            };

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
            return this.PaginationDataService.paginationId(this.pageName);
        }

        onViewTypeChange = (viewType: any) =>{
            this.PaginationDataService.viewType(this.pageName, this.viewType);
        }

        //Tab Functions
        onOrderChange = (order: any) =>{
            this.PaginationDataService.sort(this.pageName,order);
            this.TableOptionsService.setSortOption(this.pageName,order);
        };

        onPaginationChange = (page: any, limit: any) =>{
            this.PaginationDataService.currentPage(this.pageName,null,page);
            this.currentPage = page;
        };

        //Sort Functions
        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        loadSortOptions= function() {
            var options = {'Component Name':'componentName','Alert':'alert','Update Date':'latestAlertTimestamp'};

            var sortOptions = this.TableOptionsService.newSortOptions(this.pageName,options,'latestAlertTimestamp','asc');
            this.TableOptionsService.initializeSortOption(this.pageName);
            return sortOptions;

        }

        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        selectedTableOption = (option: any) =>{
            var sortString = this.TableOptionsService.toSortString(option);
            var savedSort = this.PaginationDataService.sort(this.pageName, sortString);
            var updatedOption = this.TableOptionsService.toggleSort(this.pageName, option);
            this.TableOptionsService.setSortOption(this.pageName, sortString);
        }
}

 angular.module(moduleName)
        .controller('ServiceComponentHealthDetailsController',
                                        ["$scope","$http","$filter","$transition$","$interval",
                                        "$timeout","$q","ServicesStatusData","TableOptionsService",
                                        "PaginationDataService",controller]);

