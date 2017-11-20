define(['angular','ops-mgr/overview/module-name'], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                cardTitle: "@"
            },
            controllerAs: 'vm',
            scope: true,
            templateUrl: 'js/ops-mgr/overview/feed-health/feed-health-table-card-template.html',
            controller: "FeedHealthTableCardController",
            link: function ($scope, element, attrs,ctrl,transclude) {

            }
        };
    };

    var controller = function ($scope,$rootScope,$http,$interval, OpsManagerFeedService, OpsManagerDashboardService,TableOptionsService,PaginationDataService, TabService,AlertsService, StateService,BroadcastService) {
        var self = this;
        this.pageName="feed-health";

        this.fetchFeedHealthPromise = null;


        //Pagination and view Type (list or table)
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        PaginationDataService.setRowsPerPageOptions(this.pageName,['5','10','20','50']);

        /**
         * the view either list, or table
         */
        this.viewType = PaginationDataService.viewType(this.pageName);


        //Setup the Tabs
        var tabNames =  ['All','Running','Healthy','Unhealthy','Streaming'];

        /**
         * Create the Tab objects
         */
        this.tabs = TabService.registerTabs(this.pageName,tabNames, this.paginationData.activeTab);

        /**
         * Setup the metadata about the tabs
         */
        this.tabMetadata = TabService.metadata(this.pageName);

        this.sortOptions = loadSortOptions();

        /**
         * The object[feedName] = feed
         * @type {{}}
         */
        this.dataMap = {};

        /**
         * object {data:{total:##,content:[]}}
         */
        this.data = TabService.tabPageData(this.pageName);

        /**
         * filter used for this card
         */
        this.filter = PaginationDataService.filter(self.pageName)

        /**
         * Flag to indicate the page successfully loaded for the first time and returned data in the card
         * @type {boolean}
         */
        var loaded = false;

        /**
         * Flag to indicate loading/fetching data
         * @type {boolean}
         */
        self.showProgress = false;

        /**
         * The pagination Id
         * @param tab optional tab to designate the pagination across tabs.
         */
        this.paginationId = function(tab) {
            return PaginationDataService.paginationId(self.pageName, tab.title);
        }


        /**
         * Refresh interval object for the feed health data
         * @type {null}
         */
        var feedHealthInterval = null;


        this.onTabSelected = function(tab) {
            TabService.selectedTab(self.pageName, tab);
            if(loaded || (!loaded && !OpsManagerDashboardService.isFetchingDashboard())) {
                return loadFeeds(true, true);
            }
        };


        $scope.$watch(function(){
            return self.viewType;
        },function(newVal) {
            self.onViewTypeChange(newVal);
        })

        this.onViewTypeChange = function(viewType) {
            PaginationDataService.viewType(this.pageName, self.viewType);
        }

        this.onOrderChange = function (order) {
            PaginationDataService.sort(self.pageName, order);
            TableOptionsService.setSortOption(self.pageName,order);
            return loadFeeds(true,true);
        };

        this.onPaginationChange = function (page, limit) {
            if( self.viewType == 'list') {
                if (loaded) {
                    var activeTab= TabService.getActiveTab(self.pageName);
                    activeTab.currentPage = page;
                    return loadFeeds(true, true);
                }
            }
        };

        this.onTablePaginationChange = function(page, limit){
            if( self.viewType == 'table') {
                var activeTab= TabService.getActiveTab(self.pageName);
                if (loaded) {
                    activeTab.currentPage = page;
                    return loadFeeds(true, true);
                }
            }
        }

        $scope.$watch(function() {
            return self.paginationData.rowsPerPage;
        }, function (newVal, oldVal) {
            if (newVal != oldVal) {
                if (loaded) {
                    return loadFeeds(false,true);
                }
            }
        });

        this.feedDetails = function(event, feed){
            if(feed.stream) {
                StateService.OpsManager().Feed().navigateToFeedStats(feed.feed);
            }
            else {
                StateService.OpsManager().Feed().navigateToFeedDetails(feed.feed);
            }
        }

        $scope.$watch(function() {
            return self.filter;
        }, function (newVal, oldVal) {
            if (newVal != oldVal) {
                return loadFeeds(true, true);
            }
        });


        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        this.selectedTableOption = function(option) {
            var sortString = TableOptionsService.toSortString(option);
            PaginationDataService.sort(self.pageName,sortString);
            var updatedOption = TableOptionsService.toggleSort(self.pageName,option);
            TableOptionsService.setSortOption(self.pageName,sortString);
            return loadFeeds(true,true);
        }

        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        function loadSortOptions() {
            var options = {'Feed':'feed','Health':'healthText','Status':'displayStatus','Since':'timeSinceEndTime','Last Run Time':'runTime'};

            var sortOptions = TableOptionsService.newSortOptions(self.pageName,options,'feed','desc');
            var currentOption = TableOptionsService.getCurrentSort(self.pageName);
            if(currentOption) {
                TableOptionsService.saveSortOption(self.pageName,currentOption)
            }
            return sortOptions;
        }

        /**
         * Add additional data back to the data object.
         * @param feeds
         */
        function mergeUpdatedFeeds(feeds) {
             var activeTab = TabService.getActiveTab(self.pageName);
            var tab = activeTab.title.toLowerCase();

            if (tab != 'All') {
                angular.forEach(feeds, function (feed, feedName) {

                    var tabState = feed.state.toLowerCase()
                    if(tab == 'running'){
                        if(tabState == 'running'){
                            self.dataMap[feed.feed] = feed;
                        }
                        else {
                            delete self.dataMap[feed.feed];
                        }

                    }
                    else if(tab == 'healthy') {
                        if (feed.healthText.toLowerCase() == 'healthy') {
                            self.dataMap[feed.feed] = feed;
                        } else {
                            delete self.dataMap[feed.feed];
                        }

                    }
                    else if(tab == 'unhealthy') {
                        if ((feed.healthText.toLowerCase() == 'unhealthy' || feed.healthText.toLowerCase() == 'unknown')) {
                            self.dataMap[feed.feed] = feed;
                        }
                        else {
                            delete self.dataMap[feed.feed];
                        }
                    }
                    else if(tab == 'stream') {
                        if (feed.stream) {
                            self.dataMap[feed.feed] = feed;
                        }
                        else {
                            delete self.dataMap[feed.feed];
                        }
                    }
                });

            }
        }
        function getFeedHealthQueryParams(){
            var limit = self.paginationData.rowsPerPage;
            var activeTab = TabService.getActiveTab(self.pageName);
            var tab = activeTab.title;
            var sort = PaginationDataService.sort(self.pageName);
            var start = (limit * activeTab.currentPage) - limit;
            return {limit:limit,fixedFilter:tab,sort:sort,start:start,filter:self.filter};
        }

            /**
             * Fetch and load the feeds
             * @param force (true to alwasy refresh, false or undefined to only refresh if not refreshing
             * @return {*|null}
             */
        function loadFeeds(force, showProgress){
          if((angular.isDefined(force) && force == true) || !OpsManagerDashboardService.isFetchingFeedHealth()) {
              OpsManagerDashboardService.setSkipDashboardFeedHealth(true);
              self.refreshing = true;
              if(showProgress){
                  self.showProgress = true;
              }
              var queryParams = getFeedHealthQueryParams();
              var limit = queryParams.limit;
              var tab =queryParams.fixedFilter;
              var sort = queryParams.sort;
              var start = queryParams.start;
              var filter = queryParams.filter;
              OpsManagerDashboardService.updateFeedHealthQueryParams(tab,filter,start , limit, sort);
              self.fetchFeedHealthPromise =  OpsManagerDashboardService.fetchFeeds(tab,filter,start , limit, sort).then(function (response) {
                    populateFeedData(tab);
              },
              function(err){
                  loaded = true;
                  var activeTab = TabService.getActiveTab(self.pageName);
                  activeTab.clearContent();
                  self.showProgress = false;
              });
          }
            return self.fetchFeedHealthPromise;

        }


        function populateFeedData(tab){
            var activeTab = TabService.getActiveTab(self.pageName);
            activeTab.clearContent();
            self.dataArray = OpsManagerDashboardService.feedsArray;
            _.each(self.dataArray,function(feed,i) {
                activeTab.addContent(feed);
            });
            TabService.setTotal(self.pageName, activeTab.title, OpsManagerDashboardService.totalFeeds)
            loaded = true;
            self.showProgress = false;
        }


        function watchDashboard() {
            BroadcastService.subscribe($scope,OpsManagerDashboardService.DASHBOARD_UPDATED,function(event,dashboard){
                populateFeedData();
            });
            /**
             * If the Job Running KPI starts/finishes a job, update the Feed Health Card and add/remove the running state feeds
             * so the cards are immediately in sync with each other
             */
            BroadcastService.subscribe($scope,OpsManagerDashboardService.FEED_SUMMARY_UPDATED,function(event,updatedFeeds) {
                if (angular.isDefined(updatedFeeds) && angular.isArray(updatedFeeds) && updatedFeeds.length > 0) {
                    mergeUpdatedFeeds(updatedFeeds);
                }
                else {
                    var activeTab = TabService.getActiveTab(self.pageName);
                    var tab = activeTab.title;
                    if(tab.toLowerCase() == 'running') {
                        loadFeeds(false);
                    }
                }
            });

            /**
             * if a user clicks on the KPI for Healthy,Unhealty select the tab in the feed health card
             */
            BroadcastService.subscribe($scope,OpsManagerDashboardService.TAB_SELECTED,function(e,selectedTab){
               var tabData = _.find(self.tabs,function(tab){ return tab.title == selectedTab});
               if(tabData != undefined) {
                   var idx = _.indexOf(self.tabs,tabData);
                   //Setting the selected index will trigger the onTabSelected()
                   self.tabMetadata.selectedIndex = idx;
               }
            });
        }



        $scope.$on('$destroy', function(){
            //cleanup

        });

        function init() {
            watchDashboard();
        }

        init();



    };


    angular.module(moduleName).controller('FeedHealthTableCardController', ["$scope","$rootScope","$http","$interval","OpsManagerFeedService","OpsManagerDashboardService","TableOptionsService","PaginationDataService","TabService","AlertsService","StateService","BroadcastService",controller]);

    angular.module(moduleName)
        .directive('tbaFeedHealthTableCard', directive);

});
