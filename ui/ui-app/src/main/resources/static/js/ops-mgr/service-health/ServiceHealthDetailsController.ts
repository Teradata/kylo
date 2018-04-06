import * as angular from "angular";
import {moduleName} from "./module-name";
import * as _ from 'underscore';

export class controller implements ng.IComponentController{
pageName: string;
cardTitle: string;
//Page State
loading: boolean;
showProgress: boolean;
totalComponents: any;
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
            private PaginationDataService: any,
            private StateService: any){
                this.pageName = 'service-details';
                this.cardTitle = 'Service Components';
                //Page State
                this.loading = true;
                this.showProgress = true;
                this.service = {components:[]};
                this.totalComponents = 0;
                this.serviceName = $transition$.params().serviceName;

                  //Pagination and view Type (list or table)
                this.paginationData = PaginationDataService.paginationData(this.pageName);
                PaginationDataService.setRowsPerPageOptions(this.pageName,['5','10','20','50']);
                this.viewType = PaginationDataService.viewType(this.pageName);
                this.currentPage =PaginationDataService.currentPage(this.pageName)||1;
                this.filter = PaginationDataService.filter(this.pageName);
                this.sortOptions = this.loadSortOptions();

                this.service = ServicesStatusData.services[this.serviceName];
                    if(_.isEmpty(ServicesStatusData.services)){
                        ServicesStatusData.fetchServiceStatus();
                    }


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
        };


             //Sort Functions
        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        loadSortOptions= function() {
            var options = {'Component Name': 'name', 'Components': 'componentsCount', 'Alerts': 'alertsCount', 'Update Date': 'latestAlertTimestamp'};
            var sortOptions = this.TableOptionsService.newSortOptions(this.pageName,options,'name','asc');
            this.TableOptionsService.initializeSortOption(this.pageName);
            return sortOptions;
        }

        serviceComponentDetails = function(event: any,component: any) {
            this.StateService.OpsManager().ServiceStatus().navigateToServiceComponentDetails(this.serviceName, component.name);
        }


        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        selectedTableOption = (option: any)=> {
            var sortString = this.TableOptionsService.toSortString(option);
            var savedSort = this.PaginationDataService.sort(this.pageName, sortString);
            var updatedOption = this.TableOptionsService.toggleSort(this.pageName, option);
            this.TableOptionsService.setSortOption(this.pageName, sortString);
        }
}
angular.module(moduleName)
        .controller('ServiceHealthDetailsController',
                                    ["$scope","$http","$filter","$transition$","$interval",
                                    "$timeout","$q","ServicesStatusData","TableOptionsService",
                                    "PaginationDataService","StateService",controller]);
