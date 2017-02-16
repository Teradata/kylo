/*-
 * #%L
 * thinkbig-ui-operations-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                cardTitle: "@",
                refreshIntervalTime:"=?"
            },
            controllerAs: 'vm',
            scope: true,
            templateUrl: 'js/overview/feed-health/feed-health-table-card-template.html',
            controller: "FeedHealthTableCardController",
            link: function ($scope, element, attrs,ctrl,transclude) {

            }
        };
    };

    var controller = function ($scope,$rootScope,$http,$interval, FeedData, TableOptionsService,PaginationDataService, TabService,AlertsService, StateService,EventService) {
        var self = this;
        this.pageName="feed-health";

        //Refresh Intervals
        this.setRefreshInterval = setRefreshInterval;
        this.clearRefreshInterval = clearRefreshInterval;


        //Pagination and view Type (list or table)
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        PaginationDataService.setRowsPerPageOptions(this.pageName,['5','10','20','50','All']);
        this.viewType = PaginationDataService.viewType(this.pageName);


        //Setup the Tabs
        var tabNames =  ['All','Running','Healthy','Unhealthy'];
        this.tabs = TabService.registerTabs(this.pageName,tabNames, this.paginationData.activeTab);
        this.tabMetadata = TabService.metadata(this.pageName);

        this.sortOptions = loadSortOptions();

        this.filter = PaginationDataService.filter(self.pageName);

        //Load the Feeds
        loadTabData();


        this.paginationId = function(tab){
            return PaginationDataService.paginationId(self.pageName,tab.title);
        }
        this.currentPage = function(tab){
            return PaginationDataService.currentPage(self.pageName,tab.title);
        }

        this.setRefreshInterval();

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
            StateService.navigateToFeedDetails(feed.feed);
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
            var options = {'Feed':'feed','Health':'healthText','Status':'displayStatus','Since':'timeSinceEndTime','Last Run Time':'runTime'};

            var sortOptions = TableOptionsService.newSortOptions(self.pageName,options,'feed','desc');
            var currentOption = TableOptionsService.getCurrentSort(self.pageName);
            if(currentOption) {
                TableOptionsService.saveSortOption(self.pageName,currentOption)
            }
            return sortOptions;
        }

        function loadTabData() {
            if(!self.refreshing) {
                self.refreshing = true;
                var successFn = function (response) {
                    var feeds = [];
                    if (response.data) {
                        //transform the data for UI
                        self.feeds = response.data.feedSummary;
                        groupFeedsIntoTabs(response.data.feedSummary);

                        if (self.loading) {
                            self.loading = false;
                        }
                        self.feeds = feeds;
                    }
                    self.refreshing = false;
                    finishedRequest();

                }
                var errorFn = function (err) {
                    finishedRequest();
                }
                FeedData.fetchFeedSummaryData().then( successFn, errorFn);
            }
        }

        function finishedRequest() {
            self.refreshing = false;
            self.showProgress = false;
            EventService.broadcastFeedHealthCardRendered();
        }



        function groupFeedsIntoTabs(feeds){
            //first clear out the arrays
            TabService.clearTabs(self.pageName);

            var allTab = TabService.getTab(self.pageName,'All');
            var healthyTab = TabService.getTab(self.pageName,'Healthy');
            var unhealthyTab = TabService.getTab(self.pageName,'Unhealthy');
            var runningTab = TabService.getTab(self.pageName,'Running');

            angular.forEach(feeds,function(feed,i){
                //add it to the All tab
                allTab.addContent(feed);
                 //Group by state (either WAITING or RUNNING
                var tabState = capitalize(feed.state.toLowerCase())

                //add the feedStatus field
                FeedData.decorateFeedSummary(feed);

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


            });


        }


        function clearRefreshInterval() {
            if (self.refreshInterval != null) {
                $interval.cancel(self.refreshInterval);
                self.refreshInterval = null;
            }
        }

        function setRefreshInterval() {
            self.clearRefreshInterval();
            if (self.refreshIntervalTime) {
                self.refreshInterval = $interval(loadTabData, self.refreshIntervalTime);

            }
        }
        //Util Functions
        function capitalize(string) {
            return string.charAt(0).toUpperCase() + string.substring(1).toLowerCase();
        }

        $scope.$on('$destroy', function(){
            clearRefreshInterval();
        });



    };


    angular.module(MODULE_OPERATIONS).controller('FeedHealthTableCardController', controller);

    angular.module(MODULE_OPERATIONS)
        .directive('tbaFeedHealthTableCard', directive);

})();
