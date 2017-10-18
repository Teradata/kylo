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


        //Pagination and view Type (list or table)
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        PaginationDataService.setRowsPerPageOptions(this.pageName,['5','10','20','50']);
        this.viewType = PaginationDataService.viewType(this.pageName);


        //Setup the Tabs
        var tabNames =  ['All','Running','Healthy','Unhealthy','Streaming'];
        this.tabs = TabService.registerTabs(this.pageName,tabNames, this.paginationData.activeTab);
        this.tabMetadata = TabService.metadata(this.pageName);

        this.sortOptions = loadSortOptions();

        this.filter = PaginationDataService.filter(self.pageName);


        this.paginationId = function(tab){
            return PaginationDataService.paginationId(self.pageName,tab.title);
        }
        this.currentPage = function(tab){
            return PaginationDataService.currentPage(self.pageName,tab.title);
        }

        this.onTabSelected = function(tab) {
            TabService.selectedTab(self.pageName,tab);

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
            PaginationDataService.sort(self.pageName,order);
            TableOptionsService.setSortOption(self.pageName,order);
        };

        this.onPaginationChange = function (page, limit) {
            var activeTab = TabService.getActiveTab(self.pageName);
            activeTab.currentPage = page;
            PaginationDataService.currentPage(self.pageName,activeTab.title,page);
        };

        this.feedDetails = function(event, feed){
            if(feed.stream) {
                StateService.OpsManager().Feed().navigateToFeedStats(feed.feed);
            }
            else {
                StateService.OpsManager().Feed().navigateToFeedDetails(feed.feed);
            }
        }


        //Sort Functions


        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        this.selectedTableOption = function(option) {
            var sortString = TableOptionsService.toSortString(option);
            PaginationDataService.sort(self.pageName,sortString);
            var updatedOption = TableOptionsService.toggleSort(self.pageName,option);
            TableOptionsService.setSortOption(self.pageName,sortString);
        }

        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        function loadSortOptions() {
            var options = {'Feed':'feed','Health':'healthText','Status':'displayStatus','Since':'timeSinceEndTime','Last Run Time':'runTime','Stream':'stream'};

            var sortOptions = TableOptionsService.newSortOptions(self.pageName,options,'feed','desc');
            var currentOption = TableOptionsService.getCurrentSort(self.pageName);
            if(currentOption) {
                TableOptionsService.saveSortOption(self.pageName,currentOption)
            }
            return sortOptions;
        }

        function loadTabData() {
            self.feeds = OpsManagerDashboardService.feedSummaryData
            //?? copy objects
             groupFeedsIntoTabs(self.feeds);
        }


        function groupFeedsIntoTabs(feeds){
            //first clear out the arrays
            TabService.clearTabs(self.pageName);

            var allTab = TabService.getTab(self.pageName,'All');
            var healthyTab = TabService.getTab(self.pageName,'Healthy');
            var unhealthyTab = TabService.getTab(self.pageName,'Unhealthy');
            var runningTab = TabService.getTab(self.pageName,'Running');
            var streamingTab = TabService.getTab(self.pageName,'Streaming');

            angular.forEach(feeds,function(feed,i){
                //add it to the All tab
                allTab.addContent(feed);
                 //Group by state (either WAITING or RUNNING
                var tabState = capitalize(feed.state.toLowerCase())

                //add the feedStatus field
                OpsManagerFeedService.decorateFeedSummary(feed);
                if(feed.stream == true && feed.feedHealth){
                    feed.runningCount = feed.feedHealth.runningCount;
                    if(feed.runningCount == null){
                        feed.runningCount =0;
                    }
                }

                if(tabState == 'Running'){
                    runningTab.addContent(feed);
                    feed.timeSinceEndTime = feed.runTime;
                    feed.runTimeString = '--';
                }
                if(feed.healthText.toLowerCase() == 'healthy'){
                    healthyTab.addContent(feed);
                }
                if(feed.healthText.toLowerCase() == 'unhealthy' || feed.healthText.toLowerCase() == 'unknown'){
                    unhealthyTab.addContent(feed);
                }
                if(feed.stream){
                    streamingTab.addContent(feed);
                }


            });


        }


        function watchDashboard() {
            BroadcastService.subscribe($scope,OpsManagerDashboardService.DASHBOARD_UPDATED,function(dashboard){
                loadTabData();
            });

            BroadcastService.subscribe($scope,OpsManagerDashboardService.FEED_SUMMARY_UPDATED,function(feedSummary){
                loadTabData();
            });

            BroadcastService.subscribe($scope,OpsManagerDashboardService.TAB_SELECTED,function(e,selectedTab){
               var tabData = _.find(self.tabs,function(tab){ return tab.title == selectedTab});
               if(tabData != undefined) {
                   var idx = _.indexOf(self.tabs,tabData);
                   self.tabMetadata.selectedIndex = idx;
                  // self.onTabSelected(tabData);
               }
            });
        }
        //Util Functions
        function capitalize(string) {
            return string.charAt(0).toUpperCase() + string.substring(1).toLowerCase();
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
