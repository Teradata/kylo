define(['angular','feed-mgr/feeds/module-name'], function (angular,moduleName) {
    var controller = function($scope, $http, AccessControlService, RestUrlService, PaginationDataService, TableOptionsService, AddButtonService, FeedService, StateService) {

        var self = this;

        /**
         * Indicates if feeds are allowed to be exported.
         * @type {boolean}
         */
        self.allowExport = false;

        self.feedData = [];
        this.loading = true;
        this.cardTitle = 'Feeds';

        // Register Add button
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    if (AccessControlService.hasAction(AccessControlService.FEEDS_CREATE, actionSet.actions)) {
                        AddButtonService.registerAddButton("feeds", function() {
                            FeedService.resetFeed();
                            StateService.FeedManager().Feed().navigateToDefineFeed()
                        });
                    }
                });

        //Pagination DAta
        this.pageName = "feeds";
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        this.paginationId = 'feeds';
        PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', 'All']);
        this.currentPage = PaginationDataService.currentPage(self.pageName) || 1;
        this.viewType = PaginationDataService.viewType(this.pageName);
        this.sortOptions = loadSortOptions();

        this.filter = PaginationDataService.filter(self.pageName);

        $scope.$watch(function() {
            return self.viewType;
        }, function(newVal) {
            self.onViewTypeChange(newVal);
        })

        $scope.$watch(function () {
            return self.filter;
        }, function (newVal) {
            PaginationDataService.filter(self.pageName, newVal)
        })

        this.onViewTypeChange = function(viewType) {
            PaginationDataService.viewType(this.pageName, self.viewType);
        }

        this.onOrderChange = function(order) {
            PaginationDataService.sort(self.pageName, order);
            TableOptionsService.setSortOption(self.pageName, order);
        };

        this.onPaginationChange = function(page, limit) {
            PaginationDataService.currentPage(self.pageName, null, page);
            self.currentPage = page;
        };

        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        this.selectedTableOption = function(option) {
            var sortString = TableOptionsService.toSortString(option);
            var savedSort = PaginationDataService.sort(self.pageName, sortString);
            var updatedOption = TableOptionsService.toggleSort(self.pageName, option);
            TableOptionsService.setSortOption(self.pageName, sortString);
        }

        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        function loadSortOptions() {
            var options = {'Feed': 'feedName', 'State': 'state', 'Category': 'category.name', 'Type': 'templateName', 'Last Modified': 'updateDate'};
            var sortOptions = TableOptionsService.newSortOptions(self.pageName, options, 'updateDate', 'desc');
            TableOptionsService.initializeSortOption(self.pageName);
            return sortOptions;
        }

        this.feedDetails = function($event, feed) {
            if(feed !== undefined) {
                StateService.FeedManager().Feed().navigateToFeedDetails(feed.id);
            }
        }

        function getFeeds() {

            var successFn = function(response) {
                self.loading = false;
                //simplify feedData
                var simpleFeedData = [];
                if (response.data) {
                    angular.forEach(response.data, function(feed) {
                        if (feed.state == 'ENABLED') {
                            feed.stateIcon = 'check_circle'
                        }
                        else {
                            feed.stateIcon = 'block'
                        }
                        simpleFeedData.push({
                            templateId: feed.templateId,
                            templateName: feed.templateName,
                            exportUrl: RestUrlService.ADMIN_EXPORT_FEED_URL + "/" + feed.id,
                            id: feed.id,
                            active: feed.active,
                            state: feed.state,
                            stateIcon: feed.stateIcon,
                            feedName: feed.feedName,
                            category: {name: feed.categoryName, icon: feed.categoryIcon, iconColor: feed.categoryIconColor},
                            updateDate: feed.updateDate
                        })
                    });
                }
                self.feedData = simpleFeedData;
            }
            var errorFn = function(err) {
                self.loading = false;

            }
            var promise = $http.get(RestUrlService.GET_FEEDS_URL);
            promise.then(successFn, errorFn);
            return promise;

        }

        getFeeds();

        // Fetch the allowed actions
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    self.allowExport = AccessControlService.hasAction(AccessControlService.FEEDS_EXPORT, actionSet.actions);
                });
    };


        angular.module(moduleName).controller('FeedsTableController',["$scope","$http","AccessControlService","RestUrlService","PaginationDataService","TableOptionsService","AddButtonService","FeedService","StateService", controller]);

});
