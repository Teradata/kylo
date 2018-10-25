import * as angular from "angular";
import {moduleName} from "./module-name";
import {DefaultTableOptionsService} from "../../services/TableOptionsService";
import {DefaultPaginationDataService} from "../../services/PaginationDataService";
import {StateService} from "../../services/StateService";
export class controller implements ng.IComponentController{
        pageName: string;
        //Page State
        loading: boolean;
        showProgress: boolean;
        services: any[];
        allServices: any[];
        totalServices: number;
        serviceName: string;
         //Pagination and view Type (list or table)
         paginationData: any;
         viewType: string;
         currentPage: number;
         filter: any;
         sortOptions: any;
        
         static readonly $inject = ["$scope","$http","$filter","$interval","$timeout","$q","ServicesStatusData","TableOptionsService","PaginationDataService","StateService"];

        $onInit() {
            this.ngOnInit();
        }
        
         ngOnInit() {

            this.pageName = 'service-health';
            //Page State
            this.loading = true;
            this.showProgress = true;

            this.services = [];
            this.allServices = [];
            this.totalServices = 0;

            //Pagination and view Type (list or table)
            this.paginationData = this.paginationDataService.paginationData(this.pageName);
            this.paginationDataService.setRowsPerPageOptions(this.pageName,['5','10','20','50']);
            this.viewType = this.paginationDataService.viewType(this.pageName);
            this.currentPage =this.paginationDataService.currentPage(this.pageName)||1;
            this.filter =this. paginationDataService.filter(this.pageName);
            this.sortOptions = this.loadSortOptions();

            //Load the data
            this.loadData();
            //Refresh Intervals
            
            this.setRefreshInterval();

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

            $scope.$watch(()=>{return this.filter;},(newVal: any)=>{
                if(newVal && newVal != '') {
            //     this.services = $filter('filter')(this.allServices, newVal);
                    this.totalServices = this.services.length;
                }
                else {
                //    this.services = this.allServices;
                }
            })

            $scope.$watch(()=>{ return this.viewType;
            },(newVal: any)=> {
                this.onViewTypeChange(newVal);
            });

            $scope.$on('$destroy', ()=>{
                this.clearRefreshInterval();
            });
        }// end of constructor


    paginationId = function(){
            return this.paginationDataService.paginationId(this.pageName);
        }

    onViewTypeChange (viewType: any) {
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
        // return loadJobs(true).promise;
    };


       //Sort Functions
        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        loadSortOptions= function() {
            var options = {'Service Name':'serviceName','Components':'componentsCount','Alerts':'alertsCount','Update Date':'latestAlertTimestamp'};
            var sortOptions = this.tableOptionsService.newSortOptions(this.pageName,options,'serviceName','asc');
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

           //Load Jobs

        loadData=()=> {
                var successFn = (data: any)=> {
                    this.services = data;
                    this.totalServices = this.services.length;
                    this.allServices = data;
                    this.loading == false;
                    this.showProgress = false;
                }
                var errorFn = (err: any)=> {
                    console.log('error', err);
                }
                var finallyFn =  ()=> {

                }
                //Only Refresh if the modal dialog does not have any open alerts
            this.ServicesStatusData.fetchServiceStatus(successFn,errorFn);

        }



        serviceDetails = function(event: any, service: any){
            this.stateService.OpsManager().ServiceStatus().navigateToServiceDetails(service.serviceName);
        }

        clearRefreshInterval=function() {
            if (this.refreshInterval != null) {
                this.$interval.cancel(this.refreshInterval);
                this.refreshInterval = null;
            }
        }

        setRefreshInterval=function() {
            this.clearRefreshInterval();
            if (this.refreshIntervalTime) {
                this.refreshInterval = this.$interval(this.loadData, this.refreshIntervalTime);
            }
        }

         RefreshIntervalSet: any = this.setRefreshInterval();
         RefreshIntervalClear: any = this.clearRefreshInterval();
}

angular.module(moduleName).component("tbaServiceHealth", {
    controller: controller,
    bindings: {
        cardTitle: "@",
        refreshIntervalTime:"@"
    },
    controllerAs: "vm",
    templateUrl: "./service-health-template.html"
});