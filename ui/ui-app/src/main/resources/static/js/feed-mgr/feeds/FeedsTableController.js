define(["require", "exports", "angular", "pascalprecht.translate"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('./module-name');
    var FeedsTableController = /** @class */ (function () {
        function FeedsTableController($scope, $http, AccessControlService, RestUrlService, PaginationDataService, TableOptionsService, AddButtonService, FeedService, StateService, $filter, EntityAccessControlService) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.AccessControlService = AccessControlService;
            this.RestUrlService = RestUrlService;
            this.PaginationDataService = PaginationDataService;
            this.TableOptionsService = TableOptionsService;
            this.AddButtonService = AddButtonService;
            this.FeedService = FeedService;
            this.StateService = StateService;
            this.$filter = $filter;
            this.EntityAccessControlService = EntityAccessControlService;
            this.allowExport = false;
            this.feedData = [];
            this.loading = false;
            this.loaded = false;
            this.cardTitle = "";
            //Pagination DAta
            this.pageName = "feeds";
            this.paginationData = this.PaginationDataService.paginationData(this.pageName);
            this.paginationId = 'feeds';
            this.currentPage = this.PaginationDataService.currentPage(this.pageName) || 1;
            this.viewType = this.PaginationDataService.viewType(this.pageName);
            this.sortOptions = this.loadSortOptions();
            this.filter = null;
            this.feedDetails = function ($event, feed) {
                if (feed !== undefined) {
                    _this.StateService.FeedManager().Feed().navigateToFeedDetails(feed.id);
                }
            };
            // Register Add button
            AccessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                if (AccessControlService.hasAction(AccessControlService.FEEDS_EDIT, actionSet.actions)) {
                    AddButtonService.registerAddButton("feeds", function () {
                        FeedService.resetFeed();
                        StateService.FeedManager().Feed().navigateToDefineFeed();
                    });
                }
            });
            PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
            this.filter = PaginationDataService.filter(this.pageName);
            this.cardTitle = $filter('translate')('views.main.feeds-title');
            $scope.$watch(function () {
                return _this.viewType;
            }, function (newVal) {
                _this.onViewTypeChange(newVal);
            });
            $scope.$watch(function () {
                return _this.filter;
            }, function (newVal, oldValue) {
                if (newVal != oldValue || (!_this.loaded && !_this.loading)) {
                    PaginationDataService.filter(_this.pageName, newVal);
                    _this.getFeeds();
                }
            });
            // Fetch the allowed actions
            AccessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                _this.allowExport = AccessControlService.hasAction(AccessControlService.FEEDS_EXPORT, actionSet.actions);
            });
        }
        FeedsTableController.prototype.onViewTypeChange = function (viewType) {
            this.PaginationDataService.viewType(this.pageName, this.viewType);
        };
        FeedsTableController.prototype.onOrderChange = function (order) {
            this.TableOptionsService.setSortOption(this.pageName, order);
            this.getFeeds();
        };
        ;
        FeedsTableController.prototype.onPaginate = function (page, limit) {
            this.PaginationDataService.currentPage(this.pageName, null, page);
            this.currentPage = page;
            //only trigger the reload if the initial page has been loaded.
            //md-data-table will call this function when the page initially loads and we dont want to have it run the query again.\
            if (this.loaded) {
                this.getFeeds();
            }
        };
        FeedsTableController.prototype.onPaginationChange = function (page, limit) {
            if (this.viewType == 'list') {
                this.onPaginate(page, limit);
            }
        };
        ;
        FeedsTableController.prototype.onDataTablePaginationChange = function (page, limit) {
            if (this.viewType == 'table') {
                this.onPaginate(page, limit);
            }
        };
        ;
        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        FeedsTableController.prototype.selectedTableOption = function (option) {
            var sortString = this.TableOptionsService.toSortString(option);
            var savedSort = this.PaginationDataService.sort(this.pageName, sortString);
            var updatedOption = this.TableOptionsService.toggleSort(this.pageName, option);
            this.TableOptionsService.setSortOption(this.pageName, sortString);
            this.getFeeds();
        };
        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        FeedsTableController.prototype.loadSortOptions = function () {
            var options = { 'Feed': 'feedName', 'State': 'state', 'Category': 'category.name', 'Last Modified': 'updateDate' };
            var sortOptions = this.TableOptionsService.newSortOptions(this.pageName, options, 'updateDate', 'desc');
            this.TableOptionsService.initializeSortOption(this.pageName);
            return sortOptions;
        };
        FeedsTableController.prototype.getFeeds = function () {
            var _this = this;
            this.loading = true;
            var successFn = function (response) {
                _this.loading = false;
                if (response.data) {
                    _this.feedData = _this.populateFeeds(response.data.data);
                    _this.PaginationDataService.setTotal(_this.pageName, response.data.recordsFiltered);
                    _this.loaded = true;
                }
                else {
                    _this.feedData = [];
                }
            };
            var errorFn = function (err) {
                _this.loading = false;
                _this.loaded = true;
            };
            var limit = this.PaginationDataService.rowsPerPage(this.pageName);
            var start = limit == 'All' ? 0 : (limit * this.currentPage) - limit;
            var sort = this.paginationData.sort;
            var filter = this.paginationData.filter;
            var params = { start: start, limit: limit, sort: sort, filter: filter };
            var promise = this.$http.get(this.RestUrlService.GET_FEEDS_URL, { params: params });
            promise.then(successFn, errorFn);
            return promise;
        };
        FeedsTableController.prototype.populateFeeds = function (feeds) {
            var _this = this;
            var entityAccessControlled = this.AccessControlService.isEntityAccessControlled();
            var simpleFeedData = [];
            angular.forEach(feeds, function (feed) {
                if (feed.state == 'ENABLED') {
                    feed.stateIcon = 'check_circle';
                }
                else {
                    feed.stateIcon = 'block';
                }
                simpleFeedData.push({
                    templateId: feed.templateId,
                    templateName: feed.templateName,
                    exportUrl: _this.RestUrlService.ADMIN_EXPORT_FEED_URL + "/" + feed.id,
                    id: feed.id,
                    active: feed.active,
                    state: feed.state,
                    stateIcon: feed.stateIcon,
                    feedName: feed.feedName,
                    category: { name: feed.categoryName, icon: feed.categoryIcon, iconColor: feed.categoryIconColor },
                    updateDate: feed.updateDate,
                    allowEditDetails: !entityAccessControlled || _this.FeedService.hasEntityAccess(_this.EntityAccessControlService.ENTITY_ACCESS.FEED.EDIT_FEED_DETAILS, feed),
                    allowExport: !entityAccessControlled || _this.FeedService.hasEntityAccess(_this.EntityAccessControlService.ENTITY_ACCESS.FEED.EXPORT, feed)
                });
            });
            return simpleFeedData;
        };
        return FeedsTableController;
    }());
    exports.default = FeedsTableController;
    angular.module(moduleName)
        .controller('FeedsTableController', ["$scope", "$http", "AccessControlService", "RestUrlService", "PaginationDataService",
        "TableOptionsService", "AddButtonService", "FeedService", "StateService", '$filter', "EntityAccessControlService",
        FeedsTableController]);
});
//# sourceMappingURL=FeedsTableController.js.map