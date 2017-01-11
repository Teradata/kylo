/*
 * Copyright (c) 2015.
 */
(function() {

    var directive = function() {
        return {
            restrict: "EA",
            bindToController: {
                cardTitle: "@",
                refreshIntervalTime: "@",
                pageName: '@',
                feedFilter: '=',
                onJobAction: '&',
                hideFeedColumn: '=?'
            },
            controllerAs: 'vm',
            scope: true,
            templateUrl: 'js/jobs/jobs-template.html',
            controller: "JobsCardController",
            link: function($scope, element, attrs, controller) {

            }
        };
    }

    function JobsCardController($scope, $http, $stateParams, $interval, $timeout, $q, JobData, TableOptionsService, PaginationDataService, AlertsService, StateService, IconService, TabService,
                                AccessControlService, BroadcastService) {
        var self = this;

        /**
         * Indicates that admin operations are allowed.
         * @type {boolean}
         */
        self.allowAdmin = false;

        if (this.hideFeedColumn == undefined) {
            this.hideFeedColumn = false;
        }

        this.pageName = angular.isDefined(this.pageName) ? this.pageName : 'jobs';
        //Page State
        this.loading = true;
        this.showProgress = true;

        //map of jobInstanceId to the Job
        var jobIdMap = {};

        //Track active requests and be able to cancel them if needed
        this.activeJobRequests = [];

        //Track those Jobs who are refreshing because they are running
        this.timeoutMap = {};

        //Pagination and view Type (list or table)
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', '100']);
        this.viewType = PaginationDataService.viewType(this.pageName);

        //Setup the Tabs
        var tabNames = ['All', 'Running', 'Failed', 'Completed', 'Abandoned'] //, 'Stopped'];
        this.tabs = TabService.registerTabs(this.pageName, tabNames, this.paginationData.activeTab);
        this.tabMetadata = TabService.metadata(this.pageName);

        this.sortOptions = loadSortOptions();

        /**
         * The filter supplied in the page
         * @type {string}
         */
        this.filter = '';

        BroadcastService.subscribe($scope, 'ABANDONED_ALL_JOBS', updateJobs);

        function updateJobs() {
            loadJobs(true);
        }

        //Load the data
        //   loadJobs();

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
        }, function(newVal) {
            return loadJobs(true).promise;
        })

        this.onViewTypeChange = function(viewType) {
            PaginationDataService.viewType(this.pageName, self.viewType);
        }

        //Tab Functions

        this.onTabSelected = function(tab) {
            TabService.selectedTab(self.pageName, tab);
            return loadJobs(true).promise;
        };

        this.onOrderChange = function(order) {
            PaginationDataService.sort(self.pageName, order);
            TableOptionsService.setSortOption(self.pageName, order);
            return loadJobs(true).promise;
            //return self.deferred.promise;
        };

        this.onPaginationChange = function(page, limit) {
            var activeTab = TabService.getActiveTab(self.pageName);
            activeTab.currentPage = page;
            PaginationDataService.currentPage(self.pageName, activeTab.title, page);
            return loadJobs(true).promise;
        };

        //Sort Functions
        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        function loadSortOptions() {
            var options = {'Job Name': 'jobName', 'Start Time': 'startTime', 'Run Time': 'runTime', 'Status': 'status'};

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
        this.selectedTableOption = function(option) {
            var sortString = TableOptionsService.toSortString(option);
            PaginationDataService.sort(self.pageName, sortString);
            var updatedOption = TableOptionsService.toggleSort(self.pageName, option);
            TableOptionsService.setSortOption(self.pageName, sortString);
            loadJobs(true);
        }

        //Load Jobs

        function loadJobs(force) {
            if (force || !self.refreshing) {

                if (force) {
                    angular.forEach(self.activeJobRequests, function(canceler, i) {
                        canceler.resolve();
                    });
                    self.activeJobRequests = [];
                }
                clearAllTimeouts();
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
                        //transform the data for UI
                        transformJobData(tabTitle, response.data.data);
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
                self.activeJobRequests.push(canceler);
                self.deferred = canceler;
                self.promise = self.deferred.promise;
                var filter = self.filter;

                var params = {start: start, limit: limit, sort: sort, filter:filter};
                if (self.feedFilter) {
                    if(!params.filter){
                        params.filter = '';
                    }
                    if(params.filter != ''){
                        params.filter +=',';
                    }
                    params.filter += "jobInstance.feed.name=="+self.feedFilter;
                }
                var query = tabTitle != 'All' ? tabTitle.toLowerCase() : '';
               // console.log('QUERY WITH ',params)

                $http.get(JobData.JOBS_QUERY_URL + "/" + query, {timeout: canceler.promise, params: params}).then(successFn, errorFn);
            }
            self.showProgress = true;

            return self.deferred;

        }

        function updateJob(instanceId, newJob) {
            clearErrorMessage(instanceId);
            getRunningJobExecutionData(instanceId, newJob.executionId);

        }

        function clearErrorMessage(instanceId) {
            var existingJob = jobIdMap[instanceId];
            if (existingJob) {
                existingJob.errorMessage = '';
            }
        }

        function addJobErrorMessage(instanceId, message) {
            var existingJob = jobIdMap[instanceId];
            if (existingJob) {
                existingJob.errorMessage = message;
            }
        }

        function finishedRequest(canceler) {
            var index = _.indexOf(self.activeJobRequests, canceler);
            if (index >= 0) {
                self.activeJobRequests.splice(index, 1);
            }
            canceler.resolve();
            canceler = null;
            self.refreshing = false;
            self.showProgress = false;
        }

        function transformJobData(tabTitle, jobs) {
            //first clear out the arrays
            jobIdMap = {};
            TabService.clearTabs(self.pageName);
            angular.forEach(jobs, function(job, i) {
                var transformedJob = transformJob(job);
                TabService.addContent(self.pageName, tabTitle, transformedJob);
            });
            return jobs;

        }

        function clearAllTimeouts() {
            angular.forEach(self.timeoutMap, function(timeoutInstance, instanceId) {
                $timeout.cancel(timeoutInstance);
                delete self.timeoutMap[instanceId];
            });
            self.timeoutMap = {};

        }

        function clearRefreshTimeout(instanceId) {
            var timeoutInstance = self.timeoutMap[instanceId];
            if (timeoutInstance) {
                $timeout.cancel(timeoutInstance);
                delete self.timeoutMap[instanceId];
            }
        }

        function transformJob(job) {
            job.errorMessage = '';
            var executionId = job.executionId;
            var instanceId = job.instanceId;

            job.icon = IconService.iconForJobStatus(job.displayStatus);

            if (jobIdMap[job.instanceId] == undefined) {
                jobIdMap[job.instanceId] = job;
            }
            else {
                angular.extend(jobIdMap[job.instanceId], job);
            }

            var shouldRefresh = false;
            if (job.status == 'STARTING' || job.status == 'STARTED' || job.status == 'STOPPING') {
                shouldRefresh = true;
            }

            var wasRefreshing = self.timeoutMap[instanceId];
            if (!shouldRefresh) {
                $timeout(function() {
                    triggerJobActionListener("updateEnd", job);
                }, 10);
            }

            clearRefreshTimeout(instanceId);

            //Refresh the Job Row if needed
            if (shouldRefresh) {
                self.timeoutMap[instanceId] = $timeout(function() {
                    getRunningJobExecutionData(instanceId, executionId)
                }, 1000);
            }

            return job;
        }

        //Util Functions
        function capitalize(string) {
            return string.charAt(0).toUpperCase() + string.substring(1).toLowerCase();
        }

        this.jobDetails = function(event, job) {
            StateService.navigateToJobDetails(job.executionId);
        }

        var getRunningJobExecutionData = function(instanceId, executionId) {
            var successFn = function(response) {
                transformJob(response.data);
                triggerJobActionListener("updated", response.data)
            };

            $http.get(JobData.LOAD_JOB_URL(executionId)).then(successFn);
        }

        function triggerJobActionListener(action, job) {
            if (self.onJobAction && angular.isFunction(self.onJobAction)) {
                self.onJobAction({action: action, job: job});
            }
        }

        this.restartJob = function(event, job) {
            event.stopPropagation();
            event.preventDefault();
            var executionId = job.executionId;
            var instanceId = job.instanceId;
            clearRefreshTimeout(instanceId);
            triggerJobActionListener('restartJob', job);
            var xhr = JobData.restartJob(job.executionId, {}, function(data) {
                updateJob(instanceId, data)
                //  getRunningJobExecutionData(instanceId,data.executionId);
            }, function(errMsg) {
                addJobErrorMessage(executionId, errMsg);
            });
        };

        this.stopJob = function(event, job) {
            event.stopPropagation();
            event.preventDefault();
            var instanceId = job.instanceId;
            clearRefreshTimeout(instanceId);
            triggerJobActionListener('stopJob', job);
            JobData.stopJob(job.executionId, {}, function(data) {
                updateJob(instanceId, data)
                //  getRunningJobExecutionData(instanceId,data.executionId);
            })
        };

        this.abandonJob = function(event, job) {
            event.stopPropagation();
            event.preventDefault();
            var instanceId = job.instanceId;
            clearRefreshTimeout(instanceId);
            triggerJobActionListener('abandonJob', job);
            JobData.abandonJob(job.executionId, {}, function(data) {
                updateJob(instanceId, data)
                triggerJobActionListener('abandonJob', data);
            })
        };

        this.failJob = function(event, job) {
            event.stopPropagation();
            event.preventDefault();
            var instanceId = job.executionId;
            clearRefreshTimeout(instanceId);
            triggerJobActionListener('failJob', job);
            JobData.failJob(job.executionId, {}, function(data) {
                updateJob(instanceId, data)
                triggerJobActionListener('failJob', job);
            })
        };

        $scope.$on('$destroy', function() {
            clearAllTimeouts();
        });

        // Fetch allowed permissions
        AccessControlService.getAllowedActions()
                .then(function(actionSet) {
                    self.allowAdmin = AccessControlService.hasAction(AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
                });
    }

    angular.module(MODULE_OPERATIONS).controller("JobsCardController", JobsCardController);
    angular.module(MODULE_OPERATIONS).directive('tbaJobs', directive);
})();
