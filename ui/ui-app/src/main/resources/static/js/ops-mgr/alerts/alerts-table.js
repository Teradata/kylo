define(["require", "exports", "angular", "../module-name", "underscore", "../services/OpsManagerRestUrlService"], function (require, exports, angular, module_name_1, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var AlertsTableController = /** @class */ (function () {
        function AlertsTableController($scope, $http, $q, TableOptionsService, PaginationDataService, StateService, TabService, OpsManagerRestUrlService) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.$q = $q;
            this.TableOptionsService = TableOptionsService;
            this.PaginationDataService = PaginationDataService;
            this.StateService = StateService;
            this.TabService = TabService;
            this.OpsManagerRestUrlService = OpsManagerRestUrlService;
            this.initAlertTypes = function () {
                _this.$http.get(_this.OpsManagerRestUrlService.ALERT_TYPES).then(function (response) {
                    _this.alertTypes = _this.alertTypes.concat(response.data);
                });
            };
            this.onFilterAlertTypeChange = function (alertType) {
                _this.loadAlerts(true);
            };
            this.onFilterAlertStateChange = function (alertState) {
                _this.loadAlerts(true);
            };
            this.paginationId = function (tab) {
                return _this.PaginationDataService.paginationId(_this.pageName, tab.title);
            };
            this.currentPage = function (tab) {
                return _this.PaginationDataService.currentPage(_this.pageName, tab.title);
            };
            this.onViewTypeChange = function (viewType) {
                _this.PaginationDataService.viewType(_this.pageName, _this.viewType);
            };
            //Tab Functions
            this.onTabSelected = function (tab) {
                _this.newestTime = null;
                _this.oldestTime = null;
                _this.PaginationDataService.currentPage(_this.pageName, tab.title, 1);
                _this.TabService.selectedTab(_this.pageName, tab);
                return _this.loadAlerts().promise;
            };
            this.onOrderChange = function (order) {
                _this.PaginationDataService.sort(_this.pageName, order);
                _this.TableOptionsService.setSortOption(_this.pageName, order);
                return _this.loadAlerts().promise;
                //return this.deferred.promise;
            };
            this.onPaginationChange = function (page, limit) {
                var activeTab = _this.TabService.getActiveTab(_this.pageName);
                var prevPage = _this.PaginationDataService.currentPage(_this.pageName, activeTab.title);
                // Current page number is only used for comparison in determining the direction, i.e. the value is not relevant.
                if (prevPage > page) {
                    _this.direction = _this.PAGE_DIRECTION.backward;
                }
                else if (prevPage < page) {
                    _this.direction = _this.PAGE_DIRECTION.forward;
                }
                else {
                    _this.direction = _this.PAGE_DIRECTION.none;
                }
                _this.PaginationDataService.currentPage(_this.pageName, activeTab.title, page);
                return _this.loadAlerts(_this.direction).promise;
            };
            //Sort Functions
            /**
             * Build the possible Sorting Options
             * @returns {*[]}
             */
            this.loadSortOptions = function () {
                var options = { 'Start Time': 'startTime', 'Level': 'level', 'State': 'state' };
                var sortOptions = _this.TableOptionsService.newSortOptions(_this.pageName, options, 'startTime', 'desc');
                var currentOption = _this.TableOptionsService.getCurrentSort(_this.pageName);
                if (currentOption) {
                    _this.TableOptionsService.saveSortOption(_this.pageName, currentOption);
                }
                return sortOptions;
            };
            /**
             * Loads the additional menu options that appear in the more_vert options
             * @returns {Array}
             */
            this.loadAdditionalMenuOptions = function () {
                var options = [];
                options.push(_this.TableOptionsService.newOption("Actions", 'actions_header', true, false));
                options.push(_this.TableOptionsService.newOption("Show Cleared", 'showCleared', false, false));
                return options;
            };
            this.selectedAdditionalMenuOption = function (item) {
                if (item.type == 'showCleared') {
                    _this.showCleared = !_this.showCleared;
                    if (_this.showCleared) {
                        item.label = "Hide Cleared";
                    }
                    else {
                        item.label = "Show Cleared";
                    }
                    _this.loadAlerts();
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
                _this.loadAlerts();
            };
            //Load Alerts
            this.loadAlerts = function (direction) {
                if (direction == undefined) {
                    direction = _this.PAGE_DIRECTION.none;
                }
                if (!_this.refreshing) {
                    //cancel any active requests
                    angular.forEach(_this.activeAlertRequests, function (canceler, i) {
                        canceler.resolve();
                    });
                    _this.activeAlertRequests = [];
                    var activeTab = _this.TabService.getActiveTab(_this.pageName);
                    _this.refreshing = true;
                    var sortOptions = '';
                    var tabTitle = activeTab.title;
                    var filters = { tabTitle: tabTitle };
                    var limit = _this.paginationData.rowsPerPage;
                    //                var start = start + limit;
                    var sort = _this.PaginationDataService.sort(_this.pageName);
                    var canceler = _this.$q.defer();
                    var successFn = function (response) {
                        if (response.data) {
                            var alertRange = response.data;
                            var total = 0;
                            if (angular.isDefined(alertRange.size)) {
                                if (direction === _this.PAGE_DIRECTION.forward || direction === _this.PAGE_DIRECTION.none) {
                                    total = (_this.PaginationDataService.currentPage(_this.pageName, activeTab.title) - 1) * _this.PaginationDataService.rowsPerPage(_this.pageName) + alertRange.size + 1;
                                }
                                else {
                                    total = _this.PaginationDataService.currentPage(_this.pageName, activeTab.title) * _this.PaginationDataService.rowsPerPage(_this.pageName) + 1;
                                }
                            }
                            else {
                                total = (_this.PaginationDataService.currentPage(_this.pageName, activeTab.title) - 1) * _this.PaginationDataService.rowsPerPage(_this.pageName) + 1;
                            }
                            _this.newestTime = angular.isDefined(alertRange.newestTime) ? alertRange.newestTime : 0;
                            _this.oldestTime = angular.isDefined(alertRange.oldestTime) ? alertRange.oldestTime : 0;
                            //transform the data for UI
                            _this.transformAlertData(tabTitle, angular.isDefined(alertRange.alerts) ? alertRange.alerts : []);
                            _this.TabService.setTotal(_this.pageName, tabTitle, total);
                            if (_this.loading) {
                                _this.loading = false;
                            }
                        }
                        _this.finishedRequest(canceler);
                    };
                    var errorFn = function (err) {
                        _this.finishedRequest(canceler);
                    };
                    _this.activeAlertRequests.push(canceler);
                    _this.deferred = canceler;
                    _this.promise = _this.deferred.promise;
                    var filter = _this.filter;
                    var params = { limit: limit };
                    // Get the next oldest or next newest alerts depending on paging direction.
                    if (direction == _this.PAGE_DIRECTION.forward) {
                        if (_this.oldestTime !== null) {
                            // Filter alerts to those created before the oldest alert of the previous results
                            params.before = _this.oldestTime;
                        }
                    }
                    else if (direction == _this.PAGE_DIRECTION.backward) {
                        if (_this.newestTime !== null) {
                            // Filter alerts to those created after the newest alert of the previous results
                            params.after = _this.newestTime;
                        }
                    }
                    else {
                        if (_this.newestTime !== null && _this.newestTime !== 0) {
                            // Filter alerts to the current results
                            params.before = _this.newestTime + 1;
                        }
                    }
                    params.cleared = _this.showCleared;
                    if (tabTitle != 'All') {
                        params.level = tabTitle;
                    }
                    if (filter != '') {
                        params.filter = filter;
                    }
                    if (_this.filterAlertType.label != 'ALL') {
                        if (params.filter == undefined) {
                            params.filter = _this.filterAlertType.type;
                        }
                        else {
                            params.filter += ',' + _this.filterAlertType.type;
                        }
                    }
                    if (_this.filterAlertState.label != 'ALL') {
                        params.state = _this.filterAlertState.label;
                    }
                    _this.$http.get(_this.OpsManagerRestUrlService.ALERTS_URL, { timeout: canceler.promise, params: params }).then(successFn, errorFn);
                }
                _this.showProgress = true;
                return _this.deferred;
            };
            /**
             * Called when the Server finishes.
             * @param canceler
             */
            this.finishedRequest = function (canceler) {
                var index = _.indexOf(_this.activeAlertRequests, canceler);
                if (index >= 0) {
                    _this.activeAlertRequests.splice(index, 1);
                }
                canceler.resolve();
                canceler = null;
                _this.refreshing = false;
                _this.showProgress = false;
            };
            /**
             * Transform the array of alerts for the selected Tab coming from the server to the UI model
             * @param tabTitle
             * @param alerts
             */
            this.transformAlertData = function (tabTitle, alerts) {
                //first clear out the arrays
                _this.TabService.clearTabs(_this.pageName);
                angular.forEach(alerts, function (alert, i) {
                    var transformedAlert = _this.transformAlert(alert);
                    _this.TabService.addContent(_this.pageName, tabTitle, transformedAlert);
                });
            };
            /**
             * Transform the alert coming from the server to a UI model
             * @param alert
             * @returns {*}
             */
            this.transformAlert = function (alert) {
                alert.typeDisplay = alert.type;
                if (alert.typeDisplay.indexOf("http://kylo.io/alert/alert/") == 0) {
                    alert.typeDisplay = alert.typeDisplay.substring("http://kylo.io/alert/alert/".length);
                    alert.typeDisplay = alert.typeDisplay.split("/").join(" ");
                }
                else if (alert.typeDisplay.indexOf("http://kylo.io/alert/") == 0) {
                    alert.typeDisplay = alert.typeDisplay.substring("http://kylo.io/alert/".length);
                    alert.typeDisplay = alert.typeDisplay.split("/").join(" ");
                }
                return alert;
            };
            /**
             * Navigate to the alert details page
             * @param event
             * @param alert
             */
            this.alertDetails = function (event, alert) {
                _this.StateService.OpsManager().Alert().navigateToAlertDetails(alert.id);
            };
            this.pageName = angular.isDefined(this.pageName) ? this.pageName : 'alerts';
            //Page State
            this.loading = true;
            this.showProgress = true;
            //Pagination and view Type (list or table)
            this.paginationData = PaginationDataService.paginationData(this.pageName);
            PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', '100']);
            this.viewType = PaginationDataService.viewType(this.pageName);
            //Setup the Tabs
            this.tabNames = ['All', 'INFO', 'WARNING', 'MINOR', 'MAJOR', 'CRITICAL', 'FATAL'];
            this.tabs = TabService.registerTabs(this.pageName, this.tabNames, this.paginationData.activeTab);
            this.tabMetadata = TabService.metadata(this.pageName);
            this.sortOptions = this.loadSortOptions();
            this.PAGE_DIRECTION = { forward: 'f', backward: 'b', none: 'n' };
            this.additionalMenuOptions = this.loadAdditionalMenuOptions();
            this.selectedAdditionalMenuOptionVar = this.selectedAdditionalMenuOption;
            this.ALL_ALERT_TYPES_FILTER = { label: "ALL", type: "" };
            this.filterAlertType = this.ALL_ALERT_TYPES_FILTER;
            this.alertTypes = [this.ALL_ALERT_TYPES_FILTER];
            var UNHANDLED_FILTER = { label: 'UNHANDLED' };
            this.filterAlertState = UNHANDLED_FILTER;
            this.alertStates = [{ label: 'ALL' }, { label: 'HANDLED' }, UNHANDLED_FILTER];
            this.initAlertTypes();
            this.showCleared = false;
            /**
             * The filter supplied in the page
             * @type {string}
             */
            this.filter = angular.isDefined(this.query) ? this.query : '';
            $scope.$watch(function () {
                return _this.filter;
            }, function (newVal, oldVal) {
                if (newVal != oldVal) {
                    return _this.loadAlerts(true).promise;
                }
            });
            /**
            * Array holding onto the active alert promises
            * @type {Array}
            */
            this.activeAlertRequests = [];
            /**
             * The time of the newest alert from the last server response.
             * @type {number|null}
             */
            this.newestTime = null;
            /**
             * The time of the oldest alert from the last server response.
             * @type {number|null}
             */
            $scope.$watch(function () {
                return _this.viewType;
            }, function (newVal) {
                _this.onViewTypeChange(newVal);
            });
            /**
             * This will be called the first time the page loads and then whenever the filter changes.
             *
             */
            $scope.$watch(function () {
                return _this.filter;
            }, function (newVal) {
                return _this.loadAlerts().promise;
            });
            this.oldestTime = null;
            $scope.$on('$destroy', function () {
            });
        } // end of constructor
        return AlertsTableController;
    }());
    exports.AlertsTableController = AlertsTableController;
    var AlertsController = /** @class */ (function () {
        function AlertsController($transition$) {
            this.$transition$ = $transition$;
            this.query = $transition$.params().query;
        }
        return AlertsController;
    }());
    exports.AlertsController = AlertsController;
    angular.module(module_name_1.moduleName).controller("AlertsController", ["$transition$", AlertsController]);
    angular.module(module_name_1.moduleName)
        .controller("AlertsTableController", ["$scope", "$http", "$q", "TableOptionsService", "PaginationDataService", "StateService",
        "TabService", "OpsManagerRestUrlService", AlertsTableController]);
    angular.module(module_name_1.moduleName).directive("tbaAlertsTable", [
        function () {
            return {
                restrict: "E",
                bindToController: {
                    cardTitle: "@",
                    pageName: '@',
                    query: "=",
                },
                controllerAs: 'vm',
                scope: {},
                templateUrl: 'js/ops-mgr/alerts/alerts-table-template.html',
                controller: "AlertsTableController",
                link: function ($scope, element, attrs, controller) {
                }
            };
        }
    ]);
});
//# sourceMappingURL=alerts-table.js.map