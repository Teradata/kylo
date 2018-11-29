import * as angular from "angular";
import {moduleName} from "./module-name";
import * as _ from 'underscore';
import OpsManagerRestUrlService from "../services/OpsManagerRestUrlService";
import IconService from "../services/IconStatusService";
import {TabService} from "../../services/tab.service";
import {AccessControlService} from "../../services/AccessControlService";
import { DefaultTableOptionsService } from "../../services/TableOptionsService";
import {BroadcastService} from "../../services/broadcast-service";
import { DefaultPaginationDataService } from "../../services/PaginationDataService";

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
filter: string;
viewType: string;
paginationId: any;
currentPage: any;
//Track active requests and be able to cancel them if needed
activeRequests: any[] = [];
paginationData: any;
tabs: any;
tabMetadata: any;
tabNames : string[] = ['All', 'Failure', 'Warning','Success'];
sortOptions: any; 
allowAdmin: boolean;

static readonly $inject = ["$scope","$http","$timeout","$q","$mdToast","$mdPanel","OpsManagerRestUrlService","TableOptionsService","PaginationDataService","StateService","IconService","TabService","AccessControlService","BroadcastService"];

$onInit() {
    this.ngOnInit();
}

ngOnInit() {

    this.filter = angular.isUndefined(this.filter) ? '' : this.filter;
    this.viewType = this.paginationDataService.viewType(this.pageName);
    this.paginationId = (tab: any) =>{
        return this.paginationDataService.paginationId(this.pageName, tab.title);
    }
    this.currentPage  = (tab: any)=> {
        return this.paginationDataService.currentPage(this.pageName, tab.title);
    }

    //Pagination and view Type (list or table)
    this.paginationData = this.paginationDataService.paginationData(this.pageName);
    this.paginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', '100']);

    //Setup the Tabs
    // let tabNames = ['All', 'Failure', 'Warning','Success'];
    this.tabs = this.tabService.registerTabs(this.pageName, this.tabNames, this.paginationData.activeTab);
    this.tabMetadata= this.tabService.metadata(this.pageName);
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
    this.accessControlService.getUserAllowedActions()
            .then((actionSet: any)=>{
                this.allowAdmin = this.accessControlService.hasAction(AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
            });

}

constructor(private $scope: angular.IScope,
            private $http: angular.IHttpService,
            private $timeout: angular.ITimeoutService,
            private $q: angular.IQService,
            private $mdToast: angular.material.IToastService,
            private $mdPanel: angular.material.IPanelService,
            private opsManagerRestUrlService: OpsManagerRestUrlService,
            private tableOptionsService: DefaultTableOptionsService,
            private paginationDataService: DefaultPaginationDataService,
            private StateService: any,
            private iconService: IconService,
            private tabService: TabService,
            private accessControlService: AccessControlService,
            private broadcastService: BroadcastService){

            $scope.$watch(()=> {return this.viewType;},
                                    (newVal: any)=> {this.onViewTypeChange(newVal);}
                               );

            $scope.$watch(()=> {return this.filter;},
                                    (newVal: any, oldVal: any)=> {
                                                        if (newVal != oldVal) {
                                                            return this.loadAssessments(true).promise;
                                                        }

                            });

        } // end of constructor
         /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        loadSortOptions(){
            var options = {'Name': 'serviceLevelAgreementDescription.name', 'Time':'createdTime','Status': 'result'};

            var sortOptions = this.tableOptionsService.newSortOptions(this.pageName, options, 'createdTime', 'desc');
            var currentOption = this.tableOptionsService.getCurrentSort(this.pageName);
            if (currentOption) {
                this.tableOptionsService.saveSortOption(this.pageName, currentOption)
            }
            return sortOptions;
        }
        
       onViewTypeChange=(viewType: any) =>{
            this.paginationDataService.viewType(this.pageName, viewType);
        }

        //Tab Functions

        onTabSelected=(tab: any)=>{
            this.tabService.selectedTab(this.pageName, tab);
            return this.loadAssessments(true).promise;
        };

        onOrderChange=(order: any)=>{
            this.paginationDataService.sort(this.pageName, order);
            this.tableOptionsService.setSortOption(this.pageName, order);
            return this.loadAssessments(true).promise;
            //return self.deferred.promise;
        };

        onPaginate=(page: any,limit: any)=>{
            var activeTab = this.tabService.getActiveTab(this.pageName);
            //only trigger the reload if the initial page has been loaded.
            //md-data-table will call this function when the page initially loads and we dont want to have it run the query again.\
            //on load the query will be triggered via onTabSelected() method
            if(this.loaded) {
                activeTab.currentPage = page;
                this.paginationDataService.currentPage(this.pageName, activeTab.title, page);
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
            var sortString = this.tableOptionsService.toSortString(option);
            this.paginationDataService.sort(this.pageName, sortString);
            var updatedOption = this.tableOptionsService.toggleSort(this.pageName, option);
            this.tableOptionsService.setSortOption(this.pageName, sortString);
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
                var activeTab = this.tabService.getActiveTab(this.pageName);

                this.refreshing = true;
                var sortOptions = '';
                var tabTitle = activeTab.title;
                var filters = {tabTitle: tabTitle};
                var limit = this.paginationData.rowsPerPage;

                var start = (limit * activeTab.currentPage) - limit; //self.query.page(self.selectedTab));

                var sort = this.paginationDataService.sort(this.pageName);
                var canceler = this.$q.defer();
                var successFn=(response: any)=>{
                    if (response.data) {
                        this.transformAssessments(tabTitle,response.data.data)
                        this.tabService.setTotal(this.pageName, tabTitle, response.data.recordsFiltered)

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
                this.$http.get(this.opsManagerRestUrlService.LIST_SLA_ASSESSMENTS_URL,
                               {timeout: canceler.promise, params: params})
                           .then(successFn, errorFn);
            }
            this.showProgress = true;
            return this.deferred;
        }

        transformAssessments (tabTitle: any, assessments: any){
            //first clear out the arrays
            this.tabService.clearTabs(this.pageName);
            angular.forEach(assessments, (assessment, i) =>{
                this.tabService.addContent(this.pageName, tabTitle, assessment);
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

const module = angular.module(moduleName).component("kyloServiceLevelAssessments", {
    controller: controller,
    bindings: {
        cardTitle: "@",
        pageName: '@',
        filter:'@'
    },
    controllerAs: "vm",
    templateUrl: "./service-level-assessments-template.html"
});
export default module;