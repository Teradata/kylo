import * as angular from "angular";
import {moduleName} from "./module-name";
import * as _ from 'underscore';
import OpsManagerRestUrlService from "../services/OpsManagerRestUrlService";
import IconService from "../services/IconStatusService";
import TabService from "../services/TabService";

export class controller implements ng.IComponentController{
pageName: any = angular.isDefined(this.pageName) ? this.pageName : 'service-level-assessments';
loaded: boolean = false;
refreshing: any;
deferred: any;
showProgress: boolean;
promise: any;
loading: boolean;
/**
 * The filter supplied in the page
 * @type {string}
 */
filter: any;
viewType: any;
paginationId: any;
currentPage: any;
//Track active requests and be able to cancel them if needed
activeRequests: any[] = [];
paginationData: any;
tabs: any;
tabMetadata: any;
tabNames : any[] = ['All', 'Failure', 'Warning','Success'];
sortOptions: any; 
allowAdmin: boolean;
constructor(private $scope: angular.IScope,
            private $http: any,
            private $timeout: any,
            private $q: any,
            private $mdToast: any,
            private $mdPanel: any,
            private OpsManagerRestUrlService: any,
            private TableOptionsService: any,
            private PaginationDataService: any,
            private StateService: any,
            private IconService: any,
            private TabService: any,
            private AccessControlService: any,
            private BroadcastService: any){

            this.filter = angular.isUndefined(this.filter) ? '' : this.filter;
            this.viewType = PaginationDataService.viewType(this.pageName);
            this.paginationId = (tab: any) =>{
                return PaginationDataService.paginationId(this.pageName, tab.title);
            }
            this.currentPage  = (tab: any)=> {
                return PaginationDataService.currentPage(this.pageName, tab.title);
            }
            $scope.$watch(()=> {return this.viewType;}, 
                                    (newVal: any)=> {this.onViewTypeChange(newVal);}
                               );

            $scope.$watch(()=> {return this.filter;}, 
                                    (newVal: any, oldVal: any)=> {
                                                        if (newVal != oldVal) {
                                                            return this.loadAssessments(true).promise;
                                                        }

                            });
                //Pagination and view Type (list or table)
                this.paginationData = this.PaginationDataService.paginationData(this.pageName);
                this.PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', '100']);
            
                //Setup the Tabs
                // let tabNames = ['All', 'Failure', 'Warning','Success'];
                this.tabs = this.TabService.registerTabs(this.pageName, this.tabNames, this.paginationData.activeTab);
                this.tabMetadata= this.TabService.metadata(this.pageName);
                this.sortOptions= this.loadSortOptions();
                /**
                 * Indicates that admin operations are allowed.
                 * @type {boolean}
                */
                this.allowAdmin = false;
        

                //Page State
                this.loading = true;
                this.showProgress= true;
                        // Fetch allowed permissions
                AccessControlService.getUserAllowedActions()
                        .then((actionSet: any)=>{
                            this.allowAdmin = AccessControlService.hasAction(AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
                        });

        
        } // end of constructor
         /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        loadSortOptions(){
            var options = {'Name': 'serviceLevelAgreementDescription.name', 'Time':'createdTime','Status': 'result'};

            var sortOptions = this.TableOptionsService.newSortOptions(this.pageName, options, 'createdTime', 'desc');
            var currentOption = this.TableOptionsService.getCurrentSort(this.pageName);
            if (currentOption) {
                this.TableOptionsService.saveSortOption(this.pageName, currentOption)
            }
            return sortOptions;
        }
        
       onViewTypeChange=(viewType: any) =>{
            this.PaginationDataService.viewType(this.pageName, viewType);
        }

        //Tab Functions

        onTabSelected=(tab: any)=>{
            this.TabService.selectedTab(this.pageName, tab);
            return this.loadAssessments(true).promise;
        };

        onOrderChange=(order: any)=>{
            this.PaginationDataService.sort(this.pageName, order);
            this.TableOptionsService.setSortOption(this.pageName, order);
            return this.loadAssessments(true).promise;
            //return self.deferred.promise;
        };

        onPaginate=(page: any,limit: any)=>{
            var activeTab = this.TabService.getActiveTab(this.pageName);
            //only trigger the reload if the initial page has been loaded.
            //md-data-table will call this function when the page initially loads and we dont want to have it run the query again.\
            //on load the query will be triggered via onTabSelected() method
            if(this.loaded) {
                activeTab.currentPage = page;
                this.PaginationDataService.currentPage(this.pageName, activeTab.title, page);
                return this.loadAssessments(true).promise;
            }
        }

        onPaginationChange=(page: any, limit: any)=> {
            if(this.viewType == 'list') {
                this.onPaginate(page,limit);
            }
        };

        onDataTablePaginationChange= (page: any, limit: any)=>{
            if(this.viewType == 'table') {
               this.onPaginate(page,limit);
            }
        };




        assessmentDetails= (event: any, assessment: any)=> {
                this.StateService.OpsManager().Sla().navigateToServiceLevelAssessment(assessment.id);
        }

                /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        selectedTableOption= (option: any)=> {
            var sortString = this.TableOptionsService.toSortString(option);
            this.PaginationDataService.sort(this.pageName, sortString);
            var updatedOption = this.TableOptionsService.toggleSort(this.pageName, option);
            this.TableOptionsService.setSortOption(this.pageName, sortString);
            this.loadAssessments(true);
        }

        //Load Jobs

        loadAssessments= (force: any) =>{
            if (force || !this.refreshing) {

                if (force) {
                    angular.forEach(this.activeRequests, function(canceler, i) {
                        canceler.resolve();
                    });
                    this.activeRequests = [];
                }
                var activeTab = this.TabService.getActiveTab(this.pageName);

                this.refreshing = true;
                var sortOptions = '';
                var tabTitle = activeTab.title;
                var filters = {tabTitle: tabTitle};
                var limit = this.paginationData.rowsPerPage;

                var start = (limit * activeTab.currentPage) - limit; //self.query.page(self.selectedTab));

                var sort = this.PaginationDataService.sort(this.pageName);
                var canceler = this.$q.defer();
                var successFn=(response: any)=>{
                    if (response.data) {
                        this.transformAssessments(tabTitle,response.data.data)
                        this.TabService.setTotal(this.pageName, tabTitle, response.data.recordsFiltered)

                        if (this.loading) {
                            this.loading = false;
                        }
                    }

                    this.finishedRequest(canceler);

                }
                var errorFn=(err: any)=>{
                    this.finishedRequest(canceler);
                }
                var finallyFn= ()=>{
                }
                this.activeRequests.push(canceler);
                this.deferred = canceler;
                this.promise = this.deferred.promise;
                var filter = this.filter;
                if(tabTitle.toUpperCase() != 'ALL'){
                    if(filter != null && angular.isDefined(filter) && filter != '') {
                        filter +=','
                    }
                    filter += 'result=='+tabTitle.toUpperCase();
                }
                var params = {start: start, limit: limit, sort: sort, filter:filter};
                this.$http.get(this.OpsManagerRestUrlService.LIST_SLA_ASSESSMENTS_URL, 
                               {timeout: canceler.promise, params: params})
                           .then(successFn, errorFn);
            }
            this.showProgress = true;
            return this.deferred;
        }

        transformAssessments (tabTitle: any, assessments: any){
            //first clear out the arrays
            this.TabService.clearTabs(this.pageName);
            angular.forEach(assessments, (assessment, i) =>{
                this.TabService.addContent(this.pageName, tabTitle, assessment);
            });
            return assessments;

        }


        finishedRequest(canceler: any){
            var index = _.indexOf(this.activeRequests, canceler);
            if (index >= 0) {
                this.activeRequests.splice(index, 1);
            }
            canceler.resolve();
            canceler = null;
            this.refreshing = false;
            this.showProgress = false;
            this.loaded = true;
        }




}
angular.module(moduleName)
        .controller("ServiceLevelAssessmentsController", ["$scope","$http","$timeout","$q","$mdToast","$mdPanel","OpsManagerRestUrlService","TableOptionsService","PaginationDataService","StateService","IconService","TabService","AccessControlService","BroadcastService",controller]);

angular.module(moduleName).directive("kyloServiceLevelAssessments", //[this.thinkbigPermissionsTable]);
  [ () => {  return {
            restrict: "EA",
            bindToController: {
                cardTitle: "@",
                pageName: '@',
                filter:'@'
            },
            controllerAs: 'vm',
            scope: true,
            templateUrl: 'js/ops-mgr/sla/service-level-assessments-template.html',
            controller: "ServiceLevelAssessmentsController",
            link: function($scope, element, attrs, controller) {

            }
        };
  }]);