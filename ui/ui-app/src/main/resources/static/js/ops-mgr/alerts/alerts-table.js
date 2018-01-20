define(['angular','ops-mgr/alerts/module-name'], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "E",
            bindToController: {
                cardTitle: "@",
                pageName: '@',
                query:"=",
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/ops-mgr/alerts/alerts-table-template.html',
            controller: "AlertsTableController",
            link: function ($scope, element, attrs, controller) {

            }
        };
    };

    function AlertsTableController($scope, $http, $q, TableOptionsService, PaginationDataService, StateService,  TabService,OpsManagerRestUrlService) {
        var self = this;

        this.pageName = angular.isDefined(this.pageName) ? this.pageName : 'alerts';
        //Page State
        this.loading = true;
        this.showProgress = true;

        //Pagination and view Type (list or table)
        this.paginationData = PaginationDataService.paginationData(this.pageName);

        PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', '100']);
        this.viewType = PaginationDataService.viewType(this.pageName);

        //Setup the Tabs
        var tabNames = ['All', 'INFO', 'WARNING', 'MINOR', 'MAJOR', 'CRITICAL', 'FATAL'];

        this.tabs = TabService.registerTabs(this.pageName, tabNames, this.paginationData.activeTab);

        this.tabMetadata = TabService.metadata(this.pageName);

        this.sortOptions = loadSortOptions();

        var PAGE_DIRECTION = {forward: 'f', backward: 'b', none: 'n'};

        this.additionalMenuOptions = loadAdditionalMenuOptions();

        this.selectedAdditionalMenuOption = selectedAdditionalMenuOption;

        var ALL_ALERT_TYPES_FILTER = {label:"ALL",type:""};
        self.filterAlertType = ALL_ALERT_TYPES_FILTER;

        self.alertTypes = [ALL_ALERT_TYPES_FILTER];

        var UNHANDLED_FILTER = {label:'UNHANDLED'};
        self.filterAlertState = UNHANDLED_FILTER;

        self.alertStates = [{label:'ALL'},{label:'HANDLED'},UNHANDLED_FILTER]



        initAlertTypes();

        this.showCleared = false;

        /**
         * The filter supplied in the page
         * @type {string}
         */
        this.filter =  angular.isDefined(self.query) ? self.query : '';

        $scope.$watch(function() {
            return self.filter;
        }, function (newVal, oldVal) {
            if (newVal != oldVal) {
                return loadAlerts(true).promise;
            }

        });



        function initAlertTypes() {
            $http.get(OpsManagerRestUrlService.ALERT_TYPES).then(function(response) {
                self.alertTypes = self.alertTypes.concat(response.data);
            });
        }

        self.onFilterAlertTypeChange = function(alertType){
            loadAlerts(true);
        }

        self.onFilterAlertStateChange = function(alertState){
            loadAlerts(true);
        }

        /**
         * Array holding onto the active alert promises
         * @type {Array}
         */
        this.activeAlertRequests = [];

        /**
         * The time of the newest alert from the last server response.
         * @type {number|null}
         */
        self.newestTime = null;

        /**
         * The time of the oldest alert from the last server response.
         * @type {number|null}
         */
        self.oldestTime = null;

        this.paginationId = function (tab) {
            return PaginationDataService.paginationId(self.pageName, tab.title);
        };
        this.currentPage = function (tab) {
            return PaginationDataService.currentPage(self.pageName, tab.title);
        };

        $scope.$watch(function () {
            return self.viewType;
        }, function (newVal) {
            self.onViewTypeChange(newVal);
        });

        /**
         * This will be called the first time the page loads and then whenever the filter changes.
         *
         */
        $scope.$watch(function () {
            return self.filter;
        }, function (newVal) {
            return loadAlerts().promise;
        });

        this.onViewTypeChange = function (viewType) {
            PaginationDataService.viewType(this.pageName, self.viewType);
        };

        //Tab Functions

        this.onTabSelected = function(tab) {
            self.newestTime = null;
            self.oldestTime = null;
            PaginationDataService.currentPage(self.pageName, tab.title, 1);
            TabService.selectedTab(self.pageName, tab);
            return loadAlerts().promise;
        };

        this.onOrderChange = function (order) {
            PaginationDataService.sort(self.pageName, order);
            TableOptionsService.setSortOption(self.pageName, order);
            return loadAlerts().promise;
            //return self.deferred.promise;
        };

        this.onPaginationChange = function (page, limit) {
            var activeTab = TabService.getActiveTab(self.pageName);
            var prevPage = PaginationDataService.currentPage(self.pageName, activeTab.title);

            // Current page number is only used for comparison in determining the direction, i.e. the value is not relevant.
            if (prevPage > page) {
                direction = PAGE_DIRECTION.backward;
            } else if (prevPage < page) {
            	direction = PAGE_DIRECTION.forward;
            } else {
            	direction = PAGE_DIRECTION.none;
            }

            PaginationDataService.currentPage(self.pageName, activeTab.title, page);
            return loadAlerts(direction).promise;
        };


        //Sort Functions
        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        function loadSortOptions() {
            var options = {'Start Time': 'startTime', 'Level': 'level', 'State': 'state'};

            var sortOptions = TableOptionsService.newSortOptions(self.pageName, options, 'startTime', 'desc');
            var currentOption = TableOptionsService.getCurrentSort(self.pageName);
            if (currentOption) {
                TableOptionsService.saveSortOption(self.pageName, currentOption)
            }
            return sortOptions;

        }

        /**
         * Loads the additional menu options that appear in the more_vert options
         * @returns {Array}
         */
        function loadAdditionalMenuOptions() {
            var options = [];
                options.push(TableOptionsService.newOption("Actions", 'actions_header', true, false))
                options.push(TableOptionsService.newOption("Show Cleared", 'showCleared', false, false));
            return options;
        }


        function selectedAdditionalMenuOption(item) {
            if (item.type == 'showCleared') {
                self.showCleared = !self.showCleared;
                if(self.showCleared) {
                    item.label = "Hide Cleared";
                }
                else {
                    item.label = "Show Cleared";
                }
                loadAlerts();
            }

        }


        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        this.selectedTableOption = function (option) {
            var sortString = TableOptionsService.toSortString(option);
            PaginationDataService.sort(self.pageName, sortString);
            var updatedOption = TableOptionsService.toggleSort(self.pageName, option);
            TableOptionsService.setSortOption(self.pageName, sortString);
            loadAlerts();
        };



        //Load Alerts

        function loadAlerts(direction) {
            if (direction == undefined) {
                direction = PAGE_DIRECTION.none;
            }

            if (!self.refreshing) {
                    //cancel any active requests
                    angular.forEach(self.activeAlertRequests, function (canceler, i) {
                        canceler.resolve();
                    });
                    self.activeAlertRequests = [];

                var activeTab = TabService.getActiveTab(self.pageName);

                self.refreshing = true;
                var sortOptions = '';
                var tabTitle = activeTab.title;
                var filters = {tabTitle: tabTitle};
                var limit = self.paginationData.rowsPerPage;
//                var start = start + limit;

                var sort = PaginationDataService.sort(self.pageName);
                var canceler = $q.defer();

                var successFn = function (response) {
                    if (response.data) {
                        var alertRange = response.data;
                        var total = 0;

                        if (angular.isDefined(alertRange.size)) {
                            if (direction === PAGE_DIRECTION.forward || direction === PAGE_DIRECTION.none) {
                                total = (PaginationDataService.currentPage(self.pageName, activeTab.title) - 1) * PaginationDataService.rowsPerPage(self.pageName) + alertRange.size + 1;
                            } else {
                                total = PaginationDataService.currentPage(self.pageName, activeTab.title) * PaginationDataService.rowsPerPage(self.pageName) + 1;
                            }
                        } else {
                            total = (PaginationDataService.currentPage(self.pageName, activeTab.title) - 1) * PaginationDataService.rowsPerPage(self.pageName) + 1;
                        }

                        self.newestTime = angular.isDefined(alertRange.newestTime) ? alertRange.newestTime : 0;
                        self.oldestTime = angular.isDefined(alertRange.oldestTime) ? alertRange.oldestTime : 0;

                        //transform the data for UI
                        transformAlertData(tabTitle, angular.isDefined(alertRange.alerts) ? alertRange.alerts : []);
                        TabService.setTotal(self.pageName, tabTitle, total);

                        if (self.loading) {
                            self.loading = false;
                        }
                    }

                    finishedRequest(canceler);

                };
                var errorFn = function (err) {
                    finishedRequest(canceler);
                };

                self.activeAlertRequests.push(canceler);
                self.deferred = canceler;
                self.promise = self.deferred.promise;

                var filter = self.filter;

                var params = {limit: limit};

                // Get the next oldest or next newest alerts depending on paging direction.
                if (direction == PAGE_DIRECTION.forward) {
                	if (self.oldestTime !== null) {
                		// Filter alerts to those created before the oldest alert of the previous results
                		params.before = self.oldestTime;
                	}
                } else if (direction == PAGE_DIRECTION.backward) {
                	if (self.newestTime !== null) {
                		// Filter alerts to those created after the newest alert of the previous results
                		params.after = self.newestTime;
                	}
                } else {
                    if (self.newestTime !== null && self.newestTime !== 0) {
                        // Filter alerts to the current results
                        params.before = self.newestTime + 1;
                    }
                }
                params.cleared = self.showCleared;

                if (tabTitle != 'All') {
                	params.level=tabTitle;
                }
                if(filter != '') {
                    params.filter = filter;
                }

                if(self.filterAlertType.label != 'ALL'){
                    if(params.filter == undefined){
                        params.filter = self.filterAlertType.type;
                    }
                    else {
                        params.filter+=','+self.filterAlertType.type;
                    }
                }


                if(self.filterAlertState.label != 'ALL'){
                    params.state = self.filterAlertState.label;
                }


                $http.get(OpsManagerRestUrlService.ALERTS_URL, {timeout: canceler.promise, params: params}).then(successFn, errorFn);
            }
            self.showProgress = true;

            return self.deferred;

        }

        /**
         * Called when the Server finishes.
         * @param canceler
         */
        function finishedRequest(canceler) {
            var index = _.indexOf(self.activeAlertRequests, canceler);
            if (index >= 0) {
                self.activeAlertRequests.splice(index, 1);
            }
            canceler.resolve();
            canceler = null;
            self.refreshing = false;
            self.showProgress = false;
        }

        /**
         * Transform the array of alerts for the selected Tab coming from the server to the UI model
         * @param tabTitle
         * @param alerts
         */
        function transformAlertData(tabTitle, alerts) {
            //first clear out the arrays

            TabService.clearTabs(self.pageName);
            angular.forEach(alerts, function (alert, i) {
                var transformedAlert = transformAlert(alert);
                TabService.addContent(self.pageName, tabTitle, transformedAlert);
            });

        }

        /**
         * Transform the alert coming from the server to a UI model
         * @param alert
         * @returns {*}
         */
        function transformAlert(alert) {
            alert.typeDisplay = alert.type;
            if(alert.typeDisplay.indexOf("http://kylo.io/alert/alert/") == 0){
                alert.typeDisplay = alert.typeDisplay.substring("http://kylo.io/alert/alert/".length);
                alert.typeDisplay= alert.typeDisplay.split("/").join(" ");
            }
            else if(alert.typeDisplay.indexOf("http://kylo.io/alert/") == 0){
                alert.typeDisplay = alert.typeDisplay.substring("http://kylo.io/alert/".length);
                alert.typeDisplay = alert.typeDisplay.split("/").join(" ");
            }
            return alert;
        }

        /**
         * Navigate to the alert details page
         * @param event
         * @param alert
         */
        this.alertDetails = function (event, alert) {
            StateService.OpsManager().Alert().navigateToAlertDetails(alert.id);
        };

        $scope.$on('$destroy', function () {
        });
    }

    function AlertsController($transition$) {
        this.query = $transition$.params().query;
    }
    angular.module(moduleName).controller("AlertsController",["$transition$",AlertsController]);
    angular.module(moduleName).controller("AlertsTableController", ["$scope","$http","$q","TableOptionsService","PaginationDataService","StateService","TabService","OpsManagerRestUrlService",AlertsTableController]);
    angular.module(moduleName).directive("tbaAlertsTable", directive);
});
