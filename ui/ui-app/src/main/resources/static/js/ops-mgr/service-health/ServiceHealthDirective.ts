import * as angular from "angular";
import {moduleName} from "./module-name";


export class controller implements ng.IComponentController{
        pageName: string;
        //Page State
        loading: boolean;
        showProgress: boolean;
        services: any[];
        allServices: any[];
        totalServices: any;
        serviceName: any;
         //Pagination and view Type (list or table)
         paginationData: any;
         viewType: any;
         currentPage: any;
         filter: any;
         sortOptions: any;
        

constructor(private $scope: any,
            private $http: any,
            private $filter: any,
            private $interval: any,
            private $timeout: any,
            private $q: any,
            private ServicesStatusData: any,
            private TableOptionsService: any,
            private PaginationDataService: any,
            private StateService: any){

                this.pageName = 'service-health';
                //Page State
                this.loading = true;
                this.showProgress = true;

                this.services = [];
                this.allServices = [];
                this.totalServices = 0;


                $scope.$watch(()=>{return this.filter;},(newVal: any)=>{
                        if(newVal && newVal != '') {
                    //     this.services = $filter('filter')(this.allServices, newVal);
                            this.totalServices = this.services.length;
                        }
                        else {
                        //    this.services = this.allServices;
                        }
                    })

                //Pagination and view Type (list or table)
                this.paginationData = PaginationDataService.paginationData(this.pageName);
                PaginationDataService.setRowsPerPageOptions(this.pageName,['5','10','20','50']);
                this.viewType = PaginationDataService.viewType(this.pageName);
                this.currentPage =PaginationDataService.currentPage(this.pageName)||1;
                this.filter = PaginationDataService.filter(this.pageName);
                this.sortOptions = this.loadSortOptions();

                //Load the data
                this.loadData();
                //Refresh Intervals
                
                this.setRefreshInterval();

                 $scope.$watch(()=>{ return this.viewType;
                    },(newVal: any)=> {
                        this.onViewTypeChange(newVal);
                    });

                $scope.$on('$destroy', ()=>{
                    this.clearRefreshInterval();
                });
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
            //   return loadJobs(true).promise;
        //return this.deferred.promise;
    };

    onPaginationChange = (page: any, limit: any) =>{
        this.PaginationDataService.currentPage(this.pageName,null,page);
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
            var sortOptions = this.TableOptionsService.newSortOptions(this.pageName,options,'serviceName','asc');
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
            this.StateService.OpsManager().ServiceStatus().navigateToServiceDetails(service.serviceName);
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
 angular.module(moduleName)
 .controller('ServiceHealthController', 
                                        ["$scope","$http","$filter","$interval","$timeout","$q",
                                        "ServicesStatusData","TableOptionsService","PaginationDataService",
                                        "StateService",controller]);

    angular.module(moduleName)
            .directive('tbaServiceHealth',()=>
                {
                return {
                    restrict: "EA",
                    bindToController: {
                        cardTitle: "@",
                        refreshIntervalTime:"@"
                    },
                    controllerAs: 'vm',
                    scope: true,
                    templateUrl: 'js/ops-mgr/service-health/service-health-template.html',
                    controller: "ServiceHealthController",
                    link: function ($scope: any, element: any, attrs: any, controller: any) {
                    }
                }
            }
    );