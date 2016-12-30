(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                cardTitle: "@",
                pageName: '@'
            },
            controllerAs: 'vm',
            scope: true,
            templateUrl: 'js/alerts/alerts-table-template.html',
            controller: "AlertsTableController",
            link: function ($scope, element, attrs, controller) {

            }
        };
    }

    function JobsCardController($scope, $http, $stateParams, $interval, $timeout, $q, TableOptionsService, PaginationDataService, AlertsService, StateService, IconService, TabService,
                                AccessControlService) {
        var self = this;

        /**
         * Indicates that admin operations are allowed.
         * @type {boolean}
         */
        self.allowAdmin = false;

        if (this.hideFeedColumn == undefined) {
            this.hideFeedColumn = false;
        }

        this.pageName = angular.isDefined(this.pageName) ? this.pageName : 'alerts';
        //Page State
        this.loading = true;
        this.showProgress = true;

        //Pagination and view Type (list or table)
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', '100']);
        this.viewType = PaginationDataService.viewType(this.pageName);

        //Setup the Tabs
        var tabNames = ['All', 'Recent', 'Failures', 'Handled', 'Kylo']
        this.tabs = TabService.registerTabs(this.pageName, tabNames, this.paginationData.activeTab);
        this.tabMetadata = TabService.metadata(this.pageName);

        this.sortOptions = loadSortOptions();

        /**
         * The filter supplied in the page
         * @type {string}
         */
        this.filter = '';

        /**
         * Array holding onto the active alert promises
         * @type {Array}
         */
        this.activeAlertRequests = []

        this.paginationId = function (tab) {
            return PaginationDataService.paginationId(self.pageName, tab.title);
        }
        this.currentPage = function (tab) {
            return PaginationDataService.currentPage(self.pageName, tab.title);
        }

        $scope.$watch(function () {
            return self.viewType;
        }, function (newVal) {
            self.onViewTypeChange(newVal);
        });

        $scope.$watch(function () {
            return self.filter;
        }, function (newVal) {
            return loadAlerts(true).promise;
        })

        this.onViewTypeChange = function (viewType) {
            PaginationDataService.viewType(this.pageName, self.viewType);
        }

        //Tab Functions

        this.onTabSelected = function (tab) {
            TabService.selectedTab(self.pageName, tab);
            return loadAlerts(true).promise;
        };

        this.onOrderChange = function (order) {
            PaginationDataService.sort(self.pageName, order);
            TableOptionsService.setSortOption(self.pageName, order);
            return loadAlerts(true).promise;
            //return self.deferred.promise;
        };

        this.onPaginationChange = function (page, limit) {
            var activeTab = TabService.getActiveTab(self.pageName);
            activeTab.currentPage = page;
            PaginationDataService.currentPage(self.pageName, activeTab.title, page);
            return loadAlerts(true).promise;
        };

        //Sort Functions
        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        function loadSortOptions() {
            var options = {'Alert Name': 'name', 'Start Time': 'startTime', 'Alert Type': 'type', 'State': 'state'};

            var sortOptions = TableOptionsService.newSortOptions(self.pageName, options, 'startTime', 'desc');
            var currentOption = TableOptionsService.getCurrentSort(self.pageName);
            if (currentOption) {
                TableOptionsService.saveSortOption(self.pageName, currentOption)
            }
            return sortOptions;

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
            loadAlerts(true);
        }

        //Load Alerts

        function loadAlerts(force) {
            if (force || !self.refreshing) {

                if (force) {
                    //cancel any active requests
                    angular.forEach(self.activeAlertRequests, function (canceler, i) {
                        canceler.resolve();
                    });
                    self.activeAlertRequests = [];
                }
                var activeTab = TabService.getActiveTab(self.pageName);

                self.refreshing = true;
                var sortOptions = '';
                var tabTitle = activeTab.title;
                var filters = {tabTitle: tabTitle};
                var limit = self.paginationData.rowsPerPage;

                var start = (limit * activeTab.currentPage) - limit;

                var sort = PaginationDataService.sort(self.pageName);
                var canceler = $q.defer();

                var successFn = function (response) {
                    if (response.data) {
                        //transform the data for UI
                        transformAlertData(tabTitle, response.data.data);
                        TabService.setTotal(self.pageName, tabTitle, response.data.recordsFiltered)

                        if (self.loading) {
                            self.loading = false;
                        }
                    }

                    finishedRequest(canceler);

                }
                var errorFn = function (err) {
                    finishedRequest(canceler);
                }

                self.activeAlertRequests.push(canceler);
                self.deferred = canceler;
                self.promise = self.deferred.promise;
                var filter = self.filter;

                var params = {start: start, limit: limit, sort: sort, filter: filter};
                if (self.feedFilter) {
                    if (!params.filter) {
                        params.filter = '';
                    }
                    if (params.filter != '') {
                        params.filter += ',';
                    }
                }
                var query = tabTitle != 'All' ? tabTitle.toLowerCase() : '';

                ///TODO FILL IN THIS CALL OUT with the correct URL
                //$http.get(JobData.JOBS_QUERY_URL + "/" + query, {timeout: canceler.promise, params: params}).then(successFn, errorFn);

                //Remove this timeout below as its just dummy alert data
                $timeout(function () {

                    transformAlertData(tabTitle, [{name: "test alert", state: "Handled", startTime: new Date().getTime()}]);
                    TabService.setTotal(self.pageName, tabTitle, 1)
                    if (self.loading) {
                        self.loading = false;
                    }
                    finishedRequest(canceler);
                }, 2000);
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

            return alert;
        }

        /**
         * Navigate to the alert details page
         * @param event
         * @param alert
         */
        this.alertDetails = function (event, alert) {
            //   StateService.navigateToAlertDetails(alert.id);
        }

        $scope.$on('$destroy', function () {
        });

        // Fetch allowed permissions
        AccessControlService.getAllowedActions()
            .then(function (actionSet) {
                self.allowAdmin = AccessControlService.hasAction(AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
            });
    }

    angular.module(MODULE_OPERATIONS).controller("AlertsTableController", JobsCardController);
    angular.module(MODULE_OPERATIONS).directive('tbaAlertsTable', directive);
})();
