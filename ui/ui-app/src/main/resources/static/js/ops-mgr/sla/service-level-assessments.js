define(['angular','ops-mgr/sla/module-name'], function (angular,moduleName) {

    var directive = function() {
        return {
            restrict: "EA",
            bindToController: {
                cardTitle: "@",
                pageName: '@',
                filter:'@'
            },
            controllerAs: 'vm',
            scope: true,
            templateUrl: 'js/ops-mgr/sla/service-level-assessments-template.html',
            controller: "ServiceLevelAssessmentsController",
            link: function($scope, element, attrs, controller) {

            }
        };
    }

    function controller($scope, $http,$timeout, $q, $mdToast, $mdPanel, OpsManagerRestUrlService, TableOptionsService, PaginationDataService, StateService, IconService,
                                TabService,
                                AccessControlService, BroadcastService ) {
        var self = this;

        /**
         * Indicates that admin operations are allowed.
         * @type {boolean}
         */
        self.allowAdmin = false;

        //Track active requests and be able to cancel them if needed
        this.activeRequests = [];

        this.pageName = angular.isDefined(this.pageName) ? this.pageName : 'service-level-assessments';
        //Page State
        this.loading = true;
        this.showProgress = true;

        //Pagination and view Type (list or table)
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', '100']);
        this.viewType = PaginationDataService.viewType(this.pageName);

        //Setup the Tabs
        var tabNames = ['All', 'Failure', 'Warning','Success']
        this.tabs = TabService.registerTabs(this.pageName, tabNames, this.paginationData.activeTab);
        this.tabMetadata = TabService.metadata(this.pageName);

        this.sortOptions = loadSortOptions();

        var loaded = false;

        /**
         * The filter supplied in the page
         * @type {string}
         */
        this.filter = angular.isUndefined(this.filter) ? '' : this.filter;

        this.paginationId = function(tab) {
            return PaginationDataService.paginationId(self.pageName, tab.title);
        }
        this.currentPage = function(tab) {
            return PaginationDataService.currentPage(self.pageName, tab.title);
        }

        $scope.$watch(function() {
            return self.viewType;
        }, function(newVal) {
            self.onViewTypeChange(newVal);
        });


        $scope.$watch(function() {
            return self.filter;
        }, function (newVal, oldVal) {
            if (newVal != oldVal) {
                return loadAssessments(true).promise;
            }

        })

        this.onViewTypeChange = function(viewType) {
            PaginationDataService.viewType(this.pageName, self.viewType);
        }

        //Tab Functions

        this.onTabSelected = function(tab) {
            TabService.selectedTab(self.pageName, tab);
            return loadAssessments(true).promise;
        };

        this.onOrderChange = function(order) {
            PaginationDataService.sort(self.pageName, order);
            TableOptionsService.setSortOption(self.pageName, order);
            return loadAssessments(true).promise;
            //return self.deferred.promise;
        };

        function onPaginate(page,limit){
            var activeTab = TabService.getActiveTab(self.pageName);
            //only trigger the reload if the initial page has been loaded.
            //md-data-table will call this function when the page initially loads and we dont want to have it run the query again.\
            //on load the query will be triggered via onTabSelected() method
            if(loaded) {
                activeTab.currentPage = page;
                PaginationDataService.currentPage(self.pageName, activeTab.title, page);
                return loadAssessments(true).promise;
            }
        }

        this.onPaginationChange = function(page, limit) {
            if(self.viewType == 'list') {
                onPaginate(page,limit);
            }
        };

        this.onDataTablePaginationChange = function(page, limit) {
            if(self.viewType == 'table') {
                onPaginate(page,limit);
            }
        };

        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        function loadSortOptions() {
            var options = {'Name': 'serviceLevelAgreementDescription.name', 'Time':'createdTime','Status': 'result'};

            var sortOptions = TableOptionsService.newSortOptions(self.pageName, options, 'createdTime', 'desc');
            var currentOption = TableOptionsService.getCurrentSort(self.pageName);
            if (currentOption) {
                TableOptionsService.saveSortOption(self.pageName, currentOption)
            }
            return sortOptions;
        }


        this.assessmentDetails = function(event, assessment) {
                StateService.OpsManager().Sla().navigateToServiceLevelAssessment(assessment.id);
        }




        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        this.selectedTableOption = function(option) {
            var sortString = TableOptionsService.toSortString(option);
            PaginationDataService.sort(self.pageName, sortString);
            var updatedOption = TableOptionsService.toggleSort(self.pageName, option);
            TableOptionsService.setSortOption(self.pageName, sortString);
            loadAssessments(true);
        }

        //Load Jobs

        function loadAssessments(force) {
            if (force || !self.refreshing) {

                if (force) {
                    angular.forEach(self.activeRequests, function(canceler, i) {
                        canceler.resolve();
                    });
                    self.activeRequests = [];
                }
                var activeTab = TabService.getActiveTab(self.pageName);

                self.refreshing = true;
                var sortOptions = '';
                var tabTitle = activeTab.title;
                var filters = {tabTitle: tabTitle};
                var limit = self.paginationData.rowsPerPage;

                var start = (limit * activeTab.currentPage) - limit; //self.query.page(self.selectedTab));

                var sort = PaginationDataService.sort(self.pageName);
                var canceler = $q.defer();
                var successFn = function(response) {
                    if (response.data) {
                        transformAssessments(tabTitle,response.data.data)
                        TabService.setTotal(self.pageName, tabTitle, response.data.recordsFiltered)

                        if (self.loading) {
                            self.loading = false;
                        }
                    }

                    finishedRequest(canceler);

                }
                var errorFn = function(err) {
                    finishedRequest(canceler);
                }
                var finallyFn = function() {

                }
                self.activeRequests.push(canceler);
                self.deferred = canceler;
                self.promise = self.deferred.promise;
                var filter = self.filter;
                if(tabTitle.toUpperCase() != 'ALL'){
                    if(filter != null && angular.isDefined(filter) && filter != '') {
                        filter +=','
                    }
                    filter += 'result=='+tabTitle.toUpperCase();
                }

                var params = {start: start, limit: limit, sort: sort, filter:filter};


                $http.get(OpsManagerRestUrlService.LIST_SLA_ASSESSMENTS_URL, {timeout: canceler.promise, params: params}).then(successFn, errorFn);
            }
            self.showProgress = true;

            return self.deferred;

        }

        function transformAssessments(tabTitle, assessments) {
            //first clear out the arrays
            TabService.clearTabs(self.pageName);
            angular.forEach(assessments, function(assessment, i) {
                TabService.addContent(self.pageName, tabTitle, assessment);
            });
            return assessments;

        }


        function finishedRequest(canceler) {
            var index = _.indexOf(self.activeRequests, canceler);
            if (index >= 0) {
                self.activeRequests.splice(index, 1);
            }
            canceler.resolve();
            canceler = null;
            self.refreshing = false;
            self.showProgress = false;
            loaded = true;
        }



        function clearRefreshTimeout(instanceId) {
            var timeoutInstance = self.timeoutMap[instanceId];
            if (timeoutInstance) {
                $timeout.cancel(timeoutInstance);
                delete self.timeoutMap[instanceId];
            }
        }




        // Fetch allowed permissions
        AccessControlService.getUserAllowedActions()
            .then(function(actionSet) {
                self.allowAdmin = AccessControlService.hasAction(AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
            });
    }


    angular.module(moduleName).controller("ServiceLevelAssessmentsController", ["$scope","$http","$timeout","$q","$mdToast","$mdPanel","OpsManagerRestUrlService","TableOptionsService","PaginationDataService","StateService","IconService","TabService","AccessControlService","BroadcastService",controller]);
    angular.module(moduleName).directive('kyloServiceLevelAssessments', directive);
});

