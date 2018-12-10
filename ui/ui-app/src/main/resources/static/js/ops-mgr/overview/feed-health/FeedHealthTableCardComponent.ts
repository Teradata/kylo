import * as _ from 'underscore';
import {StateService} from "../../../services/StateService";
import {TabService} from "../../../services/tab.service";
import { DefaultPaginationDataService } from "../../../services/PaginationDataService";
import { DefaultTableOptionsService } from "../../../services/TableOptionsService";
import {OpsManagerFeedService} from "../../services/ops-manager-feed.service";
import { Component, Input } from "@angular/core";
import {OpsManagerDashboardService} from "../../services/OpsManagerDashboardService";
import {BroadcastService} from "../../../services/broadcast-service";
import { ObjectUtils } from "../../../../lib/common/utils/object-utils";
import { ITdDataTableColumn, TdDataTableService, ITdDataTableSortChangeEvent, TdDataTableSortingOrder } from "@covalent/core/data-table";
import { BaseFilteredPaginatedTableView } from "../../../common/filtered-paginated-table-view/BaseFilteredPaginatedTableView";
import { IPageChangeEvent } from "@covalent/core/paging";

@Component({
    selector: 'tba-feed-health-table-card',
    templateUrl:'./feed-health-table-card-template.html'
})
export class FeedHealthTableCardComponent extends BaseFilteredPaginatedTableView {
    pageName: string;
    fetchFeedHealthPromise: any;
    paginationData: any;
    tabNames:any[];
    tabs: any;    
    tabMetadata: any;
    dataMap: any = {};
    data: any;
    filterFeed: string = "";
    loaded: boolean = false;
    showProgress: any = false;
    sortFeed: any = {sortBy: "feed", sortOrder: TdDataTableSortingOrder.Ascending};
    @Input() cardTitle: string = "";

    columns: ITdDataTableColumn[] = [
        { name: 'feed',  label: 'Feed', sortable: true, filter: true },
        { name: 'healthText', label: 'Health', sortable: true, filter: true },
        { name: 'displayStatus', label: 'Status', sortable: true, filter: true},
        { name: 'timeSinceEndTime', label: 'Since', sortable: true, filter: true},
        { name: 'runTime', label: 'Last Run Time', sortable: true, filter: true}
      ];

    ngOnInit() {

        this.pageName="feed-health";
        this.fetchFeedHealthPromise = null;
        //Pagination and view Type (list or table)
        this.paginationData = this.paginationDataService.paginationData(this.pageName);
        this.paginationDataService.setRowsPerPageOptions(this.pageName,['5','10','20','50']);
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

        /**
         * object {data:{total:##,content:[]}}
         */
        this.data = this.tabService.tabPageData(this.pageName);

        /**
         * Refresh interval object for the feed health data
         * @type {null}
         */
        this.watchDashboard();
        this.loadFeeds(true, true);

    }

    constructor(private psManagerFeedService: OpsManagerFeedService,
                private opsManagerDashboardService: OpsManagerDashboardService,
                private tableOptionsService: DefaultTableOptionsService,
                private paginationDataService: DefaultPaginationDataService,
                private tabService: TabService,
                private stateService: StateService,
                private BroadcastService: BroadcastService,
                public _dataTableService: TdDataTableService) {
                    super(_dataTableService);
                } // end of constructor
  
        onTabSelected (tab: any) {
            this.tabs[tab].clearContent();
            this.tabService.selectedTab(this.pageName, this.tabs[tab]);
            if(this.loaded || (!this.loaded && !this.opsManagerDashboardService.isFetchingDashboard())) {
                return this.loadFeeds(true, true);
            }
        };        

        onSortChange (sortEvent: ITdDataTableSortChangeEvent) {
            this.sortFeed.sortBy = sortEvent.name;
            this.sortFeed.sortOrder = sortEvent.order;
            this.sort(sortEvent);
        }

        onPaginationChange (pagingEvent: IPageChangeEvent) {

            var activeTab= this.tabService.getActiveTab(this.pageName);
            if(activeTab.currentPage != pagingEvent.page) {
                activeTab.currentPage = pagingEvent.page;
                this.loadFeeds(true, true);
            }
            else {
                this.paginationData.rowsPerPage = pagingEvent.pageSize;
                this.loadFeeds(true, true);
                this.onPageSizeChange(pagingEvent);
            }
    
        }
    
        onSearchTable (searchTerm: string) {
            this.filterFeed = searchTerm;
            this.loadFeeds(true, true);
        }

        feedDetails (event: any) {
            this.stateService.FeedManager().Feed().navigateToFeedDefinition(event.row.feedHealth.feedId)
        }

        /**
         * Add additional data back to the data object.
         * @param feeds
         */
        mergeUpdatedFeeds (feeds: any) {
            var activeTab = this.tabService.getActiveTab(this.pageName);
           var tab = activeTab.title.toLowerCase();

           if (tab != 'All') {
               feeds.forEach((feed: any, feedName: any)=> {

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

        getFeedHealthQueryParams () {
            var limit = this.paginationData.rowsPerPage;
            var activeTab = this.tabService.getActiveTab(this.pageName);
            var tab = activeTab.title;
            var sort = this.sortFeed.sortBy;
            if (this.sortFeed.sortOrder == TdDataTableSortingOrder.Descending) {
                sort = "-" + sort;
            }
            var start = (limit * activeTab.currentPage) - limit;
            return {limit:limit,fixedFilter:tab,sort:sort,start:start,filter:this.filterFeed};
        }

        /**
         * Fetch and load the feeds
         * @param force (true to alwasy refresh, false or undefined to only refresh if not refreshing
         * @return {*|null}
         */
        loadFeeds(force: any, showProgress: any) {
            if((ObjectUtils.isDefined(force) && force == true) || !this.opsManagerDashboardService.isFetchingFeedHealth()) {
                this.opsManagerDashboardService.setSkipDashboardFeedHealth(true);
                if(showProgress){
                    this.showProgress = true;
                }
                var queryParams = this.getFeedHealthQueryParams();
                var limit = queryParams.limit;
                var tab =queryParams.fixedFilter;
                var sort = queryParams.sort;
                var start = queryParams.start;
                var filter = queryParams.filter;
                this.opsManagerDashboardService.updateFeedHealthQueryParams(tab,filter,start , limit, sort);
                this.fetchFeedHealthPromise =  this.opsManagerDashboardService.fetchFeeds(tab,filter,start , limit, sort)
                .then( (response: any)=> {
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
        
        populateFeedData(){
            var activeTab = this.tabService.getActiveTab(this.pageName);
            activeTab.clearContent();
            let dataArray = this.opsManagerDashboardService.feedsArray;
            dataArray.forEach((feed,i) => {
                activeTab.addContent(feed);
            });
            this.tabService.setTotal(this.pageName, activeTab.title, this.opsManagerDashboardService.totalFeeds)
            this.loaded = true;
            this.showProgress = false;

            super.setSortBy(this.sortFeed.sortBy);
            super.setSortOrder(this.sortFeed.sortOrder);
            super.setDataAndColumnSchema(this.tabService.getActiveTab(this.pageName).data.content,this.columns);
            super.filter();
        }

        watchDashboard() {
            this.BroadcastService.subscribe(null,
                                            this.opsManagerDashboardService.DASHBOARD_UPDATED,
                                            (event: any,dashboard: any)=>{
                this.populateFeedData();
            });
            /**
             * If the Job Running KPI starts/finishes a job, update the Feed Health Card and add/remove the running state feeds
             * so the cards are immediately in sync with each other
             */
            this.BroadcastService.subscribe(null,
                                            this.opsManagerDashboardService.FEED_SUMMARY_UPDATED,
                                            (event: any,updatedFeeds: any)=> {
                if (ObjectUtils.isDefined(updatedFeeds) && Array.isArray(updatedFeeds) && updatedFeeds.length > 0) {
                    this.mergeUpdatedFeeds(updatedFeeds);
                }
                else {
                    var activeTab = this.tabService.getActiveTab(this.pageName);
                    var tab = activeTab.title;
                    if(tab.toLowerCase() == 'running') {
                        this.loadFeeds(false, false);
                    }
                }
            });

            /**
             * if a user clicks on the KPI for Healthy,Unhealty select the tab in the feed health card
             */
            this.BroadcastService.subscribe(null,
                                            this.opsManagerDashboardService.TAB_SELECTED,
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

