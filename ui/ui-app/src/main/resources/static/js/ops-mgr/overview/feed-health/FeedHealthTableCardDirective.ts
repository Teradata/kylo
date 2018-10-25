import * as angular from "angular";
import {moduleName} from "../module-name";
import * as _ from 'underscore';
import {StateService} from "../../../services/StateService";
import {TabService} from "../../../services/tab.service";
import { DefaultPaginationDataService } from "../../../services/PaginationDataService";
import { DefaultTableOptionsService } from "../../../services/TableOptionsService";
import {OpsManagerFeedService} from "../../services/ops-manager-feed.service";

export default class FeedHealthTableCardController implements ng.IComponentController{
    pageName: any;
    fetchFeedHealthPromise: any;
    paginationData: any;
    viewType:any;
    tabNames:any[];
    tabs: any;    
    tabMetadata: any;
    sortOptions:any;
    dataMap: any;
    data: any;
    filter: any;
    loaded: any;
    showProgress: any;
    paginationId: any;
    feedHealthInterval: any;

static readonly $inject = ["$scope","$rootScope","$http","$interval",
                            "OpsManagerFeedService","OpsManagerDashboardService",
                            "TableOptionsService","PaginationDataService","TabService",
                            "StateService","BroadcastService"];

$onInit() {
    this.ngOnInit();
}

ngOnInit() {

    this.pageName="feed-health";
         this.fetchFeedHealthPromise = null;
        //Pagination and view Type (list or table)
        this.paginationData = this.paginationDataService.paginationData(this.pageName);
        this.paginationDataService.setRowsPerPageOptions(this.pageName,['5','10','20','50']);
        /**
         * the view either list, or table
         */
        this.viewType = this.paginationDataService.viewType(this.pageName);
        //Setup the Tabs
        var tabNames =  ['All','Running','Healthy','Unhealthy','Streaming'];
        /**
         * Create the Tab objects
         */
        this.tabs = this.tabService.registerTabs(this.pageName,tabNames, this.paginationData.activeTab);

        /**
         * Setup the metadata about the tabs
         */
        this.tabMetadata = this.tabService.metadata(this.pageName);

        this.sortOptions = this.loadSortOptions();

        /**
         * The object[feedName] = feed
         * @type {{}}
         */
        this.dataMap = {};        
        /**
         * object {data:{total:##,content:[]}}
         */
        this.data = this.tabService.tabPageData(this.pageName);
        /**
         * filter used for this card
         */
        this.filter = this.paginationDataService.filter(this.pageName)

        /**
         * Flag to indicate the page successfully loaded for the first time and returned data in the card
         * @type {boolean}
         */
        this.loaded = false;

        /**
         * Flag to indicate loading/fetching data
         * @type {boolean}
         */
        this.showProgress = false;

        /**
         * The pagination Id
         * @param tab optional tab to designate the pagination across tabs.
         */
        this.paginationId = (tab: any)=> {
            return this.paginationDataService.paginationId(this.pageName, tab.title);
        }


        /**
         * Refresh interval object for the feed health data
         * @type {null}
         */
        this.feedHealthInterval = null;

        this.watchDashboard();

}

constructor(private $scope: IScope,
        private $rootScope: IScope,
        private $http: angular.IHttpService,
        private $interval: angular.IIntervalService,
        private opsManagerFeedService: OpsManagerFeedService,
        private OpsManagerDashboardService: any,
        private tableOptionsService: DefaultTableOptionsService,
        private paginationDataService: DefaultPaginationDataService,
        private tabService: TabService,
        private stateService: StateService,
        private BroadcastService: any
        ){
         
        $scope.$watch(()=>{
                    return this.viewType;
                },(newVal: any)=> {
                    this.onViewTypeChange(newVal);
                });


        $scope.$watch(()=> {
                    return this.paginationData.rowsPerPage;
                },  (newVal: any, oldVal: any)=> {
                    if (newVal != oldVal) {
                        if (this.loaded) {
                            return this.loadFeeds(false,true);
                        }
                    }
                });


                        $scope.$watch(()=> {
                            return this.filter;
                        },  (newVal: any, oldVal: any) =>{
                            if (newVal != oldVal) {
                                return this.loadFeeds(true, true);
                            }
                        });

                        
        $scope.$on('$destroy', ()=>{
            //cleanup

        });

        } // end of constructor
  


        onTabSelected = (tab: any) =>{
            tab.clearContent();
            this.tabService.selectedTab(this.pageName, tab);
            if(this.loaded || (!this.loaded && !this.OpsManagerDashboardService.isFetchingDashboard())) {
                return this.loadFeeds(true, true);
            }
        };        

        onViewTypeChange = (viewType: any)=> {
            this.paginationDataService.viewType(this.pageName, this.viewType);
        }

        onOrderChange = (order: any)=> {
            this.paginationDataService.sort(this.pageName, order);
            this.tableOptionsService.setSortOption(this.pageName,order);
            return this.loadFeeds(true,true);
        };

        onPaginationChange =  (page: any, limit: any)=> {
            if( this.viewType == 'list') {
                if (this.loaded) {
                    var activeTab= this.tabService.getActiveTab(this.pageName);
                    activeTab.currentPage = page;
                    return this.loadFeeds(true, true);
                }
            }
        };

        onTablePaginationChange = (page: any, limit: any)=>{
            if( this.viewType == 'table') {
                var activeTab= this.tabService.getActiveTab(this.pageName);
                if (this.loaded) {
                    activeTab.currentPage = page;
                    return this.loadFeeds(true, true);
                }
            }
        }

        feedDetails = (event: any, feed: any)=>{
            this.stateService.FeedManager().Feed().navigateToFeedDefinition(feed.feedHealth.feedId)
        }
        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        selectedTableOption = (option: any)=> {
            var sortString = this.tableOptionsService.toSortString(option);
            this.paginationDataService.sort(this.pageName,sortString);
            var updatedOption = this.tableOptionsService.toggleSort(this.pageName,option);
            this.tableOptionsService.setSortOption(this.pageName,sortString);
            return this.loadFeeds(true,true);
        }

        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
         loadSortOptions=function() {
            var options = {'Feed':'feed','Health':'healthText','Status':'displayStatus','Since':'timeSinceEndTime','Last Run Time':'runTime'};

            var sortOptions = this.tableOptionsService.newSortOptions(this.pageName,options,'feed','desc');
            var currentOption = this.tableOptionsService.getCurrentSort(this.pageName);
            if(currentOption) {
                this.tableOptionsService.saveSortOption(this.pageName,currentOption)
            }
            return sortOptions;
        }

        /**
         * Add additional data back to the data object.
         * @param feeds
         */
         mergeUpdatedFeeds=function(feeds: any) {
             var activeTab = this.tabService.getActiveTab(this.pageName);
            var tab = activeTab.title.toLowerCase();

            if (tab != 'All') {
                angular.forEach(feeds,  (feed: any, feedName: any)=> {

                    var tabState = feed.state.toLowerCase()
                    if(tab == 'running'){
                        if(tabState == 'running'){
                            this.dataMap[feed.feed] = feed;
                        }
                        else {
                            delete this.dataMap[feed.feed];
                        }

                    }
                    else if(tab == 'healthy') {
                        if (feed.healthText.toLowerCase() == 'healthy') {
                            this.dataMap[feed.feed] = feed;
                        } else {
                            delete this.dataMap[feed.feed];
                        }

                    }
                    else if(tab == 'unhealthy') {
                        if ((feed.healthText.toLowerCase() == 'unhealthy' || feed.healthText.toLowerCase() == 'unknown')) {
                            this.dataMap[feed.feed] = feed;
                        }
                        else {
                            delete this.dataMap[feed.feed];
                        }
                    }
                    else if(tab == 'stream') {
                        if (feed.stream) {
                            this.dataMap[feed.feed] = feed;
                        }
                        else {
                            delete this.dataMap[feed.feed];
                        }
                    }
                });

            }
        }
        getFeedHealthQueryParams= function(){
            var limit = this.paginationData.rowsPerPage;
            var activeTab = this.tabService.getActiveTab(this.pageName);
            var tab = activeTab.title;
            var sort = this.paginationDataService.sort(this.pageName);
            var start = (limit * activeTab.currentPage) - limit;
            return {limit:limit,fixedFilter:tab,sort:sort,start:start,filter:this.filter};
        }

            /**
             * Fetch and load the feeds
             * @param force (true to alwasy refresh, false or undefined to only refresh if not refreshing
             * @return {*|null}
             */
         loadFeeds=function(force: any, showProgress: any){
          if((angular.isDefined(force) && force == true) || !this.OpsManagerDashboardService.isFetchingFeedHealth()) {
              this.OpsManagerDashboardService.setSkipDashboardFeedHealth(true);
              this.refreshing = true;
              if(showProgress){
                  this.showProgress = true;
              }
              var queryParams = this.getFeedHealthQueryParams();
              var limit = queryParams.limit;
              var tab =queryParams.fixedFilter;
              var sort = queryParams.sort;
              var start = queryParams.start;
              var filter = queryParams.filter;
              this.OpsManagerDashboardService.updateFeedHealthQueryParams(tab,filter,start , limit, sort);
              this.fetchFeedHealthPromise =  this.OpsManagerDashboardService.fetchFeeds(tab,filter,start , limit, sort).then( (response: any)=> {
              },
              (err: any)=>{
                  this.loaded = true;
                  var activeTab = this.tabService.getActiveTab(this.pageName);
                  activeTab.clearContent();
                  this.showProgress = false;
              });
          }
            return this.fetchFeedHealthPromise;

        }


        populateFeedData=function(tab: any){
            var activeTab = this.tabService.getActiveTab(this.pageName);
            activeTab.clearContent();
            this.dataArray = this.OpsManagerDashboardService.feedsArray;
            _.each(this.dataArray,function(feed,i) {
                activeTab.addContent(feed);
            });
            this.tabService.setTotal(this.pageName, activeTab.title, this.OpsManagerDashboardService.totalFeeds)
            this.loaded = true;
            this.showProgress = false;
        }


        watchDashboard=function() {
            this.BroadcastService.subscribe(this.$scope,
                                            this.OpsManagerDashboardService.DASHBOARD_UPDATED,
                                            (event: any,dashboard: any)=>{
                this.populateFeedData();
            });
            /**
             * If the Job Running KPI starts/finishes a job, update the Feed Health Card and add/remove the running state feeds
             * so the cards are immediately in sync with each other
             */
            this.BroadcastService.subscribe(this.$scope,
                                            this.OpsManagerDashboardService.FEED_SUMMARY_UPDATED,
                                            (event: any,updatedFeeds: any)=> {
                if (angular.isDefined(updatedFeeds) && angular.isArray(updatedFeeds) && updatedFeeds.length > 0) {
                    this.mergeUpdatedFeeds(updatedFeeds);
                }
                else {
                    var activeTab = this.tabService.getActiveTab(this.pageName);
                    var tab = activeTab.title;
                    if(tab.toLowerCase() == 'running') {
                        this.loadFeeds(false);
                    }
                }
            });

            /**
             * if a user clicks on the KPI for Healthy,Unhealty select the tab in the feed health card
             */
            this.BroadcastService.subscribe(this.$scope,
                                            this.OpsManagerDashboardService.TAB_SELECTED,
                                            (e: any,selectedTab: any)=>{
               var tabData = _.find(this.tabs,(tab: any)=>{ return tab.title == selectedTab});
               if(tabData != undefined) {
                   var idx = _.indexOf(this.tabs,tabData);
                   //Setting the selected index will trigger the onTabSelected()
                   this.tabMetadata.selectedIndex = idx;
               }
            });
        }

}

 angular.module(moduleName).component('tbaFeedHealthTableCard',{
     controller: FeedHealthTableCardController,
     bindings: {
        cardTitle: "@"
     },
     controllerAs: "vm",
     templateUrl: "./feed-health-table-card-template.html"
 });
