define(['angular','feed-mgr/feeds/module-name'], function (angular,moduleName) {
    var controller = function($scope, $http, AccessControlService, RestUrlService, PaginationDataService, TableOptionsService, AddButtonService, FeedService, StateService,
                              EntityAccessControlService) {

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
        AccessControlService.getUserAllowedActions()
                .then(function(actionSet) {
                    if (AccessControlService.hasAction(AccessControlService.FEEDS_EDIT, actionSet.actions)) {
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
        }, function (newVal, oldValue) {
            PaginationDataService.filter(self.pageName, newVal)
            getFeeds();
        })

        this.onViewTypeChange = function(viewType) {
            PaginationDataService.viewType(this.pageName, self.viewType);
        }

        this.onOrderChange = function(order) {
            TableOptionsService.setSortOption(self.pageName, order);
            getFeeds();
        };

        this.onPaginationChange = function(page, limit) {
            PaginationDataService.currentPage(self.pageName, null, page);
            self.currentPage = page;
            getFeeds();
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
            getFeeds();
        }

        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        function loadSortOptions() {
            var options = {'Feed': 'feedName', 'State': 'state', 'Category': 'category.name', 'Last Modified': 'updateDate'};
//            var options = {'Feed': 'feedName', 'State': 'state', 'Category': 'category.name', 'Type': 'templateName', 'Last Modified': 'updateDate'};
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
                if (response.data) {
                	self.feedData = populateFeeds(response.data.data);
                	PaginationDataService.setTotal(self.pageName,response.data.recordsFiltered);
                } else {
                	self.feedData = [];
                }
            }
            
            var errorFn = function(err) {
                self.loading = false;
            }

        	var limit = PaginationDataService.rowsPerPage(self.pageName);
        	var start = (limit * self.currentPage) - limit;
        	var sort = self.paginationData.sort;
        	var filter = self.paginationData.filter;
            var params = {start: start, limit: limit, sort: sort, filter: filter};
            
            var promise = $http.get(RestUrlService.GET_FEEDS_URL, {params: params});
            promise.then(successFn, errorFn);
            return promise;

        }

        function populateFeeds(feeds) {
            var entityAccessControlled = AccessControlService.isEntityAccessControlled();
        	var simpleFeedData = [];
            
            angular.forEach(feeds, function(feed) {
                if (feed.state == 'ENABLED') {
                    feed.stateIcon = 'check_circle'
                } else {
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
                    updateDate: feed.updateDate,
                    allowEditDetails: !entityAccessControlled || FeedService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS, feed),
                    allowExport: !entityAccessControlled || FeedService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.FEED.EXPORT, feed)
                })
            });
            
            return simpleFeedData;
        }

//        getFeeds();

        // Fetch the allowed actions
        AccessControlService.getUserAllowedActions()
                .then(function(actionSet) {
                    self.allowExport = AccessControlService.hasAction(AccessControlService.FEEDS_EXPORT, actionSet.actions);
                });
    };


    angular.module(moduleName).controller('FeedsTableController',["$scope","$http","AccessControlService","RestUrlService","PaginationDataService","TableOptionsService","AddButtonService",
                                                                  "FeedService","StateService", "EntityAccessControlService", controller]);

});
