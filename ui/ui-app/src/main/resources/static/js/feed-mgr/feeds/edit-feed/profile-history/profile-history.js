define(["require", "exports", "angular", "underscore", "pascalprecht.translate"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/edit-feed/module-name');
    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                selectedTabIndex: '='
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/feed-mgr/feeds/edit-feed/profile-history/profile-history.html',
            controller: "FeedProfileHistoryController",
            link: function ($scope, element, attrs, controller) {
            }
        };
    };
    var overflowScrollDirective = function ($window, $timeout) {
        return {
            restrict: "A",
            scope: {},
            link: function ($scope, $element, attrs) {
                function onResize() {
                    var $w = angular.element($window);
                    var height = $w.height();
                    if (attrs.parent) {
                        height = angular.element(attrs.parent).height();
                    }
                    if (attrs.offsetHeight) {
                        height -= parseInt(attrs.offsetHeight);
                    }
                    if (height <= 0) {
                        $timeout(function () {
                            onResize();
                        }, 10);
                    }
                    else {
                        $element.css('height', height);
                        $element.css('overflow', 'auto');
                    }
                }
                angular.element($window).on('resize.profilehistory', function () {
                    onResize();
                });
                onResize();
                $scope.$on('destroy', function () {
                    angular.element($window).unbind('resize.profilehistory');
                });
            }
        };
    };
    var FeedProfileItemController = /** @class */ (function () {
        function FeedProfileItemController($scope, $mdDialog, $mdToast, $http, StateService, FeedService, BroadcastService, feed, profileRow, currentTab) {
            this.$scope = $scope;
            this.$mdDialog = $mdDialog;
            this.$mdToast = $mdToast;
            this.$http = $http;
            this.StateService = StateService;
            this.FeedService = FeedService;
            this.BroadcastService = BroadcastService;
            this.feed = feed;
            this.profileRow = profileRow;
            this.currentTab = currentTab;
            $scope.feed = feed;
            $scope.profileRow = profileRow;
            $scope.processingdate = $scope.profileRow['DATE'];
            $scope.processingdttm = $scope.profileRow['PROCESSING_DTTM'];
            if (currentTab == 'valid') {
                $scope.selectedTabIndex = 1;
            }
            else if (currentTab == 'invalid') {
                $scope.selectedTabIndex = 2;
            }
            if (currentTab == 'profile-stats') {
                $scope.selectedTabIndex = 0;
            }
            $scope.hide = function ($event) {
                $mdDialog.hide();
            };
            $scope.done = function ($event) {
                $mdDialog.hide();
            };
            $scope.cancel = function ($event) {
                $mdDialog.hide();
            };
            $scope.renderPagination = false;
            BroadcastService.subscribe($scope, 'PROFILE_TAB_DATA_LOADED', function (tab) {
                $scope.renderPagination = true;
            });
            //Pagination DAta
            $scope.paginationData = {
                rowsPerPage: 50,
                currentPage: 1,
                rowsPerPageOptions: ['5', '10', '20', '50', '100']
            };
            $scope.paginationId = 'profile_stats_0';
            $scope.$watch('selectedTabIndex', function (newVal) {
                $scope.renderPagination = false;
                $scope.paginationId = 'profile_stats_' + newVal;
                $scope.paginationData.currentPage = 1;
            });
            $scope.onPaginationChange = function (page, limit) {
                $scope.paginationData.currentPage = page;
            };
        }
        return FeedProfileItemController;
    }());
    exports.FeedProfileItemController = FeedProfileItemController;
    var FeedProfileHistoryController = /** @class */ (function () {
        function FeedProfileHistoryController($scope, $http, $mdDialog, $filter, FeedService, RestUrlService, HiveService, StateService, Utils) {
            this.$scope = $scope;
            this.$http = $http;
            this.$mdDialog = $mdDialog;
            this.$filter = $filter;
            this.FeedService = FeedService;
            this.RestUrlService = RestUrlService;
            this.HiveService = HiveService;
            this.StateService = StateService;
            this.Utils = Utils;
            this.model = this.FeedService.editFeedModel;
            this.showSummary = true;
            this.profileSummary = [];
            this.loading = false;
            this.showNoResults = false;
        }
        FeedProfileHistoryController.prototype.getProfileHistory = function () {
            var _this = this;
            // console.log('getProfileHistory');
            this.loading = true;
            this.showNoResults = false;
            var successFn = function (response) {
                if (response.data.length == 0) {
                    _this.showNoResults = true;
                }
                var dataMap = {};
                var dataArr = [];
                var columns = _this.HiveService.getColumnNamesForQueryResult(response);
                if (columns != null) {
                    //get the keys into the map for the different columns
                    var dateColumn_1 = _.find(columns, function (column) {
                        return _this.Utils.strEndsWith(column, 'processing_dttm');
                    });
                    var metricTypeColumn_1 = _.find(columns, function (column) {
                        return _this.Utils.strEndsWith(column, 'metrictype');
                    });
                    var metricValueColumn_1 = _.find(columns, function (column) {
                        return _this.Utils.strEndsWith(column, 'metricvalue');
                    });
                    //group on date column
                    angular.forEach(response.data, function (row) {
                        var date = row[dateColumn_1];
                        if (dataMap[date] == undefined) {
                            var timeInMillis = _this.HiveService.getUTCTime(date);
                            var obj = { 'PROCESSING_DTTM': date, 'DATE_TIME': timeInMillis, 'DATE': new Date(timeInMillis) };
                            dataMap[date] = obj;
                            dataArr.push(obj);
                        }
                        var newRow = dataMap[date];
                        var metricType = row[metricTypeColumn_1];
                        var value = row[metricValueColumn_1];
                        if (value && metricType == 'MIN_TIMESTAMP' || metricType == 'MAX_TIMESTAMP') {
                            //first check to see if it is millis
                            if (!isNaN(value)) {
                                var dateStr = _this.$filter('date')(new Date(''), "yyyy-MM-dd"); //tmp was passed as which is not declared anywhere. was returning 'Invalid Date'// replaced by '' here
                                value = dateStr;
                            }
                            else {
                                value = value.substr(0, 10); //string the time off the string
                            }
                        }
                        newRow[metricType] = value;
                    });
                    //sort it desc
                    // dataArr = _.sortBy(dataArr,dateColumn).reverse();
                    dataArr = _.sortBy(dataArr, 'DATE_TIME').reverse();
                    _this.profileSummary = dataArr;
                }
                _this.loading = false;
            };
            var errorFn = function (err) {
                console.log('ERROR ', err);
                _this.loading = false;
            };
            var promise = this.$http.get(this.RestUrlService.FEED_PROFILE_SUMMARY_URL(this.model.id));
            promise.then(successFn, errorFn);
            return promise;
        };
        ;
        FeedProfileHistoryController.prototype.$onInit = function () {
            this.getProfileHistory();
        };
        FeedProfileHistoryController.prototype.onValidCountClick = function (row) {
            this.showProfileDialog('valid', row);
        };
        FeedProfileHistoryController.prototype.onInvalidCountClick = function (row) {
            this.showProfileDialog('invalid', row);
        };
        FeedProfileHistoryController.prototype.viewProfileStats = function (row) {
            this.showProfileDialog('profile-stats', row);
        };
        FeedProfileHistoryController.prototype.showProfileDialog = function (currentTab, profileRow) {
            console.log("showProfileDialog currentTag,profileRow", currentTab, profileRow);
            this.$mdDialog.show({
                controller: 'FeedProfileItemController',
                templateUrl: 'js/feed-mgr/feeds/edit-feed/profile-history/profile-history-dialog.html',
                parent: angular.element(document.body),
                clickOutsideToClose: false,
                fullscreen: true,
                locals: {
                    feed: this.model,
                    profileRow: profileRow,
                    currentTab: currentTab
                }
            }).then(function (msg) {
            }, function () {
            });
        };
        ;
        return FeedProfileHistoryController;
    }());
    exports.FeedProfileHistoryController = FeedProfileHistoryController;
    angular.module(moduleName).controller('FeedProfileItemController', ["$scope", "$mdDialog", "$mdToast", "$http", "StateService", "FeedService", "BroadcastService", "feed", "profileRow", "currentTab", FeedProfileItemController]);
    angular.module(moduleName)
        .directive('overflowScroll', ['$window', '$timeout', overflowScrollDirective]);
    angular.module(moduleName).controller('FeedProfileHistoryController', ["$scope", "$http", "$mdDialog", "$filter", "FeedService", "RestUrlService", "HiveService", "StateService", "Utils", FeedProfileHistoryController]);
    angular.module(moduleName)
        .directive('thinkbigFeedProfileHistory', directive);
});
//# sourceMappingURL=profile-history.js.map