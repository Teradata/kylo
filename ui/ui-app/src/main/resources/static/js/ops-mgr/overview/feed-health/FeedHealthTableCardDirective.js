define(["require", "exports", "angular", "../module-name", "../../services/OpsManagerFeedService", "../../services/OpsManagerDashboardService", "../../services/TabService", "../../services/AlertsService", "underscore"], function (require, exports, angular, module_name_1, OpsManagerFeedService_1, OpsManagerDashboardService_1, TabService_1, AlertsService_1, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var FeedHealthTableCardController = /** @class */ (function () {
        function FeedHealthTableCardController($scope, $rootScope, $http, $interval, OpsManagerFeedService, OpsManagerDashboardService, TableOptionsService, PaginationDataService, TabService, AlertsService, StateService, BroadcastService) {
            var _this = this;
            this.$scope = $scope;
            this.$rootScope = $rootScope;
            this.$http = $http;
            this.$interval = $interval;
            this.OpsManagerFeedService = OpsManagerFeedService;
            this.OpsManagerDashboardService = OpsManagerDashboardService;
            this.TableOptionsService = TableOptionsService;
            this.PaginationDataService = PaginationDataService;
            this.TabService = TabService;
            this.AlertsService = AlertsService;
            this.StateService = StateService;
            this.BroadcastService = BroadcastService;
            this.onTabSelected = function (tab) {
                _this.TabService.selectedTab(_this.pageName, tab);
                if (_this.loaded || (!_this.loaded && !_this.OpsManagerDashboardService.isFetchingDashboard())) {
                    return _this.loadFeeds(true, true);
                }
            };
            this.onViewTypeChange = function (viewType) {
                _this.PaginationDataService.viewType(_this.pageName, _this.viewType);
            };
            this.onOrderChange = function (order) {
                _this.PaginationDataService.sort(_this.pageName, order);
                _this.TableOptionsService.setSortOption(_this.pageName, order);
                return _this.loadFeeds(true, true);
            };
            this.onPaginationChange = function (page, limit) {
                if (_this.viewType == 'list') {
                    if (_this.loaded) {
                        var activeTab = _this.TabService.getActiveTab(_this.pageName);
                        activeTab.currentPage = page;
                        return _this.loadFeeds(true, true);
                    }
                }
            };
            this.onTablePaginationChange = function (page, limit) {
                if (_this.viewType == 'table') {
                    var activeTab = _this.TabService.getActiveTab(_this.pageName);
                    if (_this.loaded) {
                        activeTab.currentPage = page;
                        return _this.loadFeeds(true, true);
                    }
                }
            };
            this.feedDetails = function (event, feed) {
                if (feed.stream) {
                    _this.StateService.OpsManager().Feed().navigateToFeedStats(feed.feed);
                }
                else {
                    _this.StateService.OpsManager().Feed().navigateToFeedDetails(feed.feed);
                }
            };
            /**
             * Called when a user Clicks on a table Option
             * @param option
             */
            this.selectedTableOption = function (option) {
                var sortString = _this.TableOptionsService.toSortString(option);
                _this.PaginationDataService.sort(_this.pageName, sortString);
                var updatedOption = _this.TableOptionsService.toggleSort(_this.pageName, option);
                _this.TableOptionsService.setSortOption(_this.pageName, sortString);
                return _this.loadFeeds(true, true);
            };
            /**
             * Build the possible Sorting Options
             * @returns {*[]}
             */
            this.loadSortOptions = function () {
                var options = { 'Feed': 'feed', 'Health': 'healthText', 'Status': 'displayStatus', 'Since': 'timeSinceEndTime', 'Last Run Time': 'runTime' };
                var sortOptions = this.TableOptionsService.newSortOptions(this.pageName, options, 'feed', 'desc');
                var currentOption = this.TableOptionsService.getCurrentSort(this.pageName);
                if (currentOption) {
                    this.TableOptionsService.saveSortOption(this.pageName, currentOption);
                }
                return sortOptions;
            };
            /**
             * Add additional data back to the data object.
             * @param feeds
             */
            this.mergeUpdatedFeeds = function (feeds) {
                var _this = this;
                var activeTab = this.TabService.getActiveTab(this.pageName);
                var tab = activeTab.title.toLowerCase();
                if (tab != 'All') {
                    angular.forEach(feeds, function (feed, feedName) {
                        var tabState = feed.state.toLowerCase();
                        if (tab == 'running') {
                            if (tabState == 'running') {
                                _this.dataMap[feed.feed] = feed;
                            }
                            else {
                                delete _this.dataMap[feed.feed];
                            }
                        }
                        else if (tab == 'healthy') {
                            if (feed.healthText.toLowerCase() == 'healthy') {
                                _this.dataMap[feed.feed] = feed;
                            }
                            else {
                                delete _this.dataMap[feed.feed];
                            }
                        }
                        else if (tab == 'unhealthy') {
                            if ((feed.healthText.toLowerCase() == 'unhealthy' || feed.healthText.toLowerCase() == 'unknown')) {
                                _this.dataMap[feed.feed] = feed;
                            }
                            else {
                                delete _this.dataMap[feed.feed];
                            }
                        }
                        else if (tab == 'stream') {
                            if (feed.stream) {
                                _this.dataMap[feed.feed] = feed;
                            }
                            else {
                                delete _this.dataMap[feed.feed];
                            }
                        }
                    });
                }
            };
            this.getFeedHealthQueryParams = function () {
                var limit = this.paginationData.rowsPerPage;
                var activeTab = this.TabService.getActiveTab(this.pageName);
                var tab = activeTab.title;
                var sort = this.PaginationDataService.sort(this.pageName);
                var start = (limit * activeTab.currentPage) - limit;
                return { limit: limit, fixedFilter: tab, sort: sort, start: start, filter: this.filter };
            };
            /**
             * Fetch and load the feeds
             * @param force (true to alwasy refresh, false or undefined to only refresh if not refreshing
             * @return {*|null}
             */
            this.loadFeeds = function (force, showProgress) {
                var _this = this;
                if ((angular.isDefined(force) && force == true) || !this.OpsManagerDashboardService.isFetchingFeedHealth()) {
                    this.OpsManagerDashboardService.setSkipDashboardFeedHealth(true);
                    this.refreshing = true;
                    if (showProgress) {
                        this.showProgress = true;
                    }
                    var queryParams = this.getFeedHealthQueryParams();
                    var limit = queryParams.limit;
                    var tab = queryParams.fixedFilter;
                    var sort = queryParams.sort;
                    var start = queryParams.start;
                    var filter = queryParams.filter;
                    this.OpsManagerDashboardService.updateFeedHealthQueryParams(tab, filter, start, limit, sort);
                    this.fetchFeedHealthPromise = this.OpsManagerDashboardService.fetchFeeds(tab, filter, start, limit, sort).then(function (response) {
                        _this.populateFeedData(tab);
                    }, function (err) {
                        _this.loaded = true;
                        var activeTab = _this.TabService.getActiveTab(_this.pageName);
                        activeTab.clearContent();
                        _this.showProgress = false;
                    });
                }
                return this.fetchFeedHealthPromise;
            };
            this.populateFeedData = function (tab) {
                var activeTab = this.TabService.getActiveTab(this.pageName);
                activeTab.clearContent();
                this.dataArray = this.OpsManagerDashboardService.feedsArray;
                _.each(this.dataArray, function (feed, i) {
                    activeTab.addContent(feed);
                });
                this.TabService.setTotal(this.pageName, activeTab.title, this.OpsManagerDashboardService.totalFeeds);
                this.loaded = true;
                this.showProgress = false;
            };
            this.watchDashboard = function () {
                var _this = this;
                this.BroadcastService.subscribe(this.$scope, this.OpsManagerDashboardService.DASHBOARD_UPDATED, function (event, dashboard) {
                    _this.populateFeedData();
                });
                /**
                 * If the Job Running KPI starts/finishes a job, update the Feed Health Card and add/remove the running state feeds
                 * so the cards are immediately in sync with each other
                 */
                this.BroadcastService.subscribe(this.$scope, this.OpsManagerDashboardService.FEED_SUMMARY_UPDATED, function (event, updatedFeeds) {
                    if (angular.isDefined(updatedFeeds) && angular.isArray(updatedFeeds) && updatedFeeds.length > 0) {
                        _this.mergeUpdatedFeeds(updatedFeeds);
                    }
                    else {
                        var activeTab = _this.TabService.getActiveTab(_this.pageName);
                        var tab = activeTab.title;
                        if (tab.toLowerCase() == 'running') {
                            _this.loadFeeds(false);
                        }
                    }
                });
                /**
                 * if a user clicks on the KPI for Healthy,Unhealty select the tab in the feed health card
                 */
                this.BroadcastService.subscribe(this.$scope, this.OpsManagerDashboardService.TAB_SELECTED, function (e, selectedTab) {
                    var tabData = _.find(_this.tabs, function (tab) { return tab.title == selectedTab; });
                    if (tabData != undefined) {
                        var idx = _.indexOf(_this.tabs, tabData);
                        //Setting the selected index will trigger the onTabSelected()
                        _this.tabMetadata.selectedIndex = idx;
                    }
                });
            };
            this.init = function () {
                _this.watchDashboard();
            };
            this.pageName = "feed-health";
            this.fetchFeedHealthPromise = null;
            //Pagination and view Type (list or table)
            this.paginationData = PaginationDataService.paginationData(this.pageName);
            PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50']);
            /**
             * the view either list, or table
             */
            this.viewType = PaginationDataService.viewType(this.pageName);
            //Setup the Tabs
            var tabNames = ['All', 'Running', 'Healthy', 'Unhealthy', 'Streaming'];
            /**
             * Create the Tab objects
             */
            this.tabs = TabService.registerTabs(this.pageName, tabNames, this.paginationData.activeTab);
            /**
             * Setup the metadata about the tabs
             */
            this.tabMetadata = TabService.metadata(this.pageName);
            this.sortOptions = this.loadSortOptions();
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
            this.filter = PaginationDataService.filter(this.pageName);
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
            this.paginationId = function (tab) {
                return PaginationDataService.paginationId(_this.pageName, tab.title);
            };
            /**
             * Refresh interval object for the feed health data
             * @type {null}
             */
            this.feedHealthInterval = null;
            $scope.$watch(function () {
                return _this.viewType;
            }, function (newVal) {
                _this.onViewTypeChange(newVal);
            });
            $scope.$watch(function () {
                return _this.paginationData.rowsPerPage;
            }, function (newVal, oldVal) {
                if (newVal != oldVal) {
                    if (_this.loaded) {
                        return _this.loadFeeds(false, true);
                    }
                }
            });
            $scope.$watch(function () {
                return _this.filter;
            }, function (newVal, oldVal) {
                if (newVal != oldVal) {
                    return _this.loadFeeds(true, true);
                }
            });
            $scope.$on('$destroy', function () {
                //cleanup
            });
            this.init();
        } // end of constructor
        return FeedHealthTableCardController;
    }());
    exports.default = FeedHealthTableCardController;
    angular.module(module_name_1.moduleName)
        .service('OpsManagerFeedService', ['$q', '$http', '$interval', '$timeout', 'HttpService', 'IconService', 'AlertsService', 'OpsManagerRestUrlService', OpsManagerFeedService_1.default])
        .service('OpsManagerDashboardService', ['$q', '$http', '$interval', '$timeout', 'HttpService', 'IconService', 'AlertsService', 'OpsManagerRestUrlService', 'BroadcastService', 'OpsManagerFeedService', OpsManagerDashboardService_1.default])
        .service("AlertsService", [AlertsService_1.default])
        .service('TabService', ['PaginationDataService', TabService_1.default])
        .controller('FeedHealthTableCardController', ["$scope", "$rootScope", "$http", "$interval",
        "OpsManagerFeedService", "OpsManagerDashboardService",
        "TableOptionsService", "PaginationDataService", "TabService",
        "AlertsService", "StateService", "BroadcastService", FeedHealthTableCardController]);
    angular.module(module_name_1.moduleName)
        .directive('tbaFeedHealthTableCard', [function () {
            return {
                restrict: "EA",
                bindToController: {
                    cardTitle: "@"
                },
                controllerAs: 'vm',
                scope: true,
                templateUrl: 'js/ops-mgr/overview/feed-health/feed-health-table-card-template.html',
                controller: "FeedHealthTableCardController",
                link: function ($scope, element, attrs, ctrl, transclude) {
                }
            };
        }]);
});
//# sourceMappingURL=FeedHealthTableCardDirective.js.map