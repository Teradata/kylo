define(['angular','ops-mgr/jobs/module-name'], function (angular,moduleName) {

    var directive = function() {
        return {
            restrict: "EA",
            bindToController: {
                cardTitle: "@",
                refreshIntervalTime: "@",
                pageName: '@',
                feedFilter: '=',
                onJobAction: '&',
                hideFeedColumn: '=?',
                feed: '=?',
                filter:'=?',
                tab:'=?'
            },
            controllerAs: 'vm',
            scope: true,
            templateUrl: 'js/ops-mgr/jobs/jobs-template.html',
            controller: "JobsCardController",
            link: function($scope, element, attrs, controller) {

            }
        };
    }

    function JobsCardController($scope, $http,$mdDialog,$timeout,$mdMenu, $q, $mdToast, $mdPanel, OpsManagerJobService, TableOptionsService, PaginationDataService, StateService, IconService,
                                TabService,
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

        this.abandonAllMenuOption = {};

        this.additionalMenuOptions = loadAdditionalMenuOptions();

        this.selectedAdditionalMenuOption = selectedAdditionalMenuOption;

        var loaded = false;
        /**
         * The filter supplied in the page
         * @type {string}
         */
        this.filter = angular.isUndefined(this.filter) ? '' : this.filter;

        this.tab = angular.isUndefined(this.tab) ? '' : this.tab;


        BroadcastService.subscribe($scope, 'ABANDONED_ALL_JOBS', updateJobs);

        function updateJobs() {
            loadJobs(true);
        }

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
                console.log('filter changed ',newVal,oldVal)
                return loadJobs(true).promise;
            }
        });

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
            var activeTab= TabService.getActiveTab(self.pageName);
            //only trigger the reload if the initial page has been loaded.
            //md-data-table will call this function when the page initially loads and we dont want to have it run the query again.\
            //on load the query will be triggered via onTabSelected() method
            if(loaded) {
                activeTab.currentPage = page;
                PaginationDataService.currentPage(self.pageName, activeTab.title, page);
                return loadJobs(true).promise;
            }
        };


        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        function loadSortOptions() {
            var options = {'Job Name': 'jobName', 'Start Time': 'startTime', 'Status': 'status'};

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
            if (self.feed) {
                //only show the abandon all on the feeds page that are unhealthy
                options.push(TableOptionsService.newOption("Actions", 'actions_header', true, false))
                options.push(TableOptionsService.newOption("Abandon All", 'abandon_all', false, false));
            }
            return options;
        }

        function selectedAdditionalMenuOption(item) {
            if (item.type == 'abandon_all') {
                $mdMenu.hide();
                $mdDialog.show({
                    controller:"AbandonAllJobsDialogController",
                    templateUrl: 'js/ops-mgr/jobs/abandon-all-jobs-dialog.html',
                    parent: angular.element(document.body),
                    clickOutsideToClose: false,
                    fullscreen: true,
                    locals: {
                        feedName: self.feedFilter
                    }
                });

                OpsManagerJobService.abandonAllJobs(self.feedFilter, function () {
                    $mdDialog.hide();
                    BroadcastService.notify('ABANDONED_ALL_JOBS', {feed: self.feedFilter});
                    $mdToast.show(
                        $mdToast.simple()
                            .textContent('Abandoned all failed jobs for the feed')
                            .hideDelay(3000)
                    );
                },function(err){
                    $mdDialog.hide();
                    $mdToast.show(
                        $mdToast.simple()
                            .textContent('Unable to abandonal all jobs for the feed.  A unexpected error occurred.')
                            .hideDelay(3000)
                    );

                })
            }
        }

        /**
         *
         * @param options
         */
        this.onOptionsMenuOpen = function (options) {
            if (self.feed) {
                var abandonOption = _.find(options.additionalOptions, function (option) {
                    return option.type == 'abandon_all';
                });
                if (abandonOption != null && abandonOption != undefined) {
                    abandonOption.disabled = self.feed.healthText != 'UNHEALTHY';
                }
            }
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
                //if the filter doesnt contain an operator, then default it to look for the job name
                if (params.filter != '' && params.filter != null && !containsFilterOperator(params.filter)) {
                    params.filter = 'job=~%' + params.filter;
                }


                var query = tabTitle != 'All' ? tabTitle.toLowerCase() : '';

                $http.get(OpsManagerJobService.JOBS_QUERY_URL + "/" + query, {timeout: canceler.promise, params: params}).then(successFn, errorFn);
            }
            self.showProgress = true;

            return self.deferred;

        }

        function containsFilterOperator(filterStr) {
            var contains = false;
            var ops = ['==', '>', '<', '>=', '<=', '=~']
            for (var i = 0; i < ops.length; i++) {
                contains = filterStr.indexOf(ops[i]) >= 0;
                if (contains) {
                    break;
                }
            }
            return contains;
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
            loaded = true;
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
            if(job.stream){
                StateService.OpsManager().Feed().navigateToFeedStats(job.jobName);
            }else {
                StateService.OpsManager().Job().navigateToJobDetails(job.executionId);
            }
        }

        var getRunningJobExecutionData = function(instanceId, executionId) {
            var successFn = function(response) {
                transformJob(response.data);
                triggerJobActionListener("updated", response.data)
            };

            $http.get(OpsManagerJobService.LOAD_JOB_URL(executionId)).then(successFn);
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
            var xhr = OpsManagerJobService.restartJob(job.executionId, {}, function(response) {
                updateJob(instanceId, response.data)
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
            OpsManagerJobService.stopJob(job.executionId, {}, function(response) {
                updateJob(instanceId, response.data)
                //  getRunningJobExecutionData(instanceId,data.executionId);
            })
        };

        this.abandonJob = function(event, job) {
            event.stopPropagation();
            event.preventDefault();
            var instanceId = job.instanceId;
            clearRefreshTimeout(instanceId);
            triggerJobActionListener('abandonJob', job);
            OpsManagerJobService.abandonJob(job.executionId, {}, function(response) {
                updateJob(instanceId, response.data)
                triggerJobActionListener('abandonJob', response.data);
            })
        };

        this.failJob = function(event, job) {
            event.stopPropagation();
            event.preventDefault();
            var instanceId = job.executionId;
            clearRefreshTimeout(instanceId);
            triggerJobActionListener('failJob', job);
            OpsManagerJobService.failJob(job.executionId, {}, function(response) {
                updateJob(instanceId, response.data)
                triggerJobActionListener('failJob', response.data);
            })
        };

        this.filterHelpOperators = [];
        this.filterHelpFields = []
        this.filterHelpExamples = [];

        var newHelpItem = function (label, description) {
            return {displayName: label, description: description};
        }

        this.filterHelpOperators.push(newHelpItem("Equals", "=="));
        this.filterHelpOperators.push(newHelpItem("Like condition", "=~"));
        this.filterHelpOperators.push(newHelpItem("In Clause", "Comma separated surrounded with quote    ==\"value1,value2\"   "));
        this.filterHelpOperators.push(newHelpItem("Greater than, less than", ">,>=,<,<="));
        this.filterHelpOperators.push(newHelpItem("Multiple Filters", "Filers separated by a comma    field1==value,field2==value  "));

        this.filterHelpFields.push(newHelpItem("Filter on a feed name", "feed"));
        this.filterHelpFields.push(newHelpItem("Filter on a job name", "job"));
        this.filterHelpFields.push(newHelpItem("Filter on a job start time", "jobStartTime"));
        this.filterHelpFields.push(newHelpItem("Filter on a job end time", "jobEndTime"));
        this.filterHelpFields.push(newHelpItem("Filter on a job id", "executionId"));
        this.filterHelpFields.push(newHelpItem("Start time date part filters", "startYear,startMonth,startDay"));
        this.filterHelpFields.push(newHelpItem("End time date part filters", "endYear,endMonth,endDay"));

        this.filterHelpExamples.push(newHelpItem("Find job names that equal 'my.job1' ", "job==my.job1"));
        this.filterHelpExamples.push(newHelpItem("Find job names starting with 'my' ", "job=~my"));
        this.filterHelpExamples.push(newHelpItem("Find jobs for 'my.job1' or 'my.job2' ", "job==\"my.job1,my.job2\""));
        this.filterHelpExamples.push(newHelpItem("Find 'my.job1' starting in 2017 ", "job==my.job1,startYear==2017"));
        this.filterHelpExamples.push(newHelpItem("Find jobs that started on February 1st 2017", "startTime>=2017-02-01,startTime<2017-02-02"));

        this.showFilterHelpPanel = function (ev) {
            var position = $mdPanel.newPanelPosition()
                .relativeTo('.filter-help-button')
                .addPanelPosition($mdPanel.xPosition.ALIGN_END, $mdPanel.yPosition.BELOW);

            var config = {
                attachTo: angular.element(document.body),
                controller: 'JobFilterHelpPanelMenuCtrl',
                controllerAs: 'ctrl',
                templateUrl: 'js/ops-mgr/jobs/jobs-filter-help-template.html',
                panelClass: 'filter-help',
                position: position,
                locals: {
                    'filterHelpExamples': self.filterHelpExamples,
                    'filterHelpOperators': self.filterHelpOperators,
                    'filterHelpFields': self.filterHelpFields
                },
                openFrom: ev,
                clickOutsideToClose: true,
                escapeToClose: true,
                focusOnOpen: false,
                zIndex: 2
            };

            $mdPanel.open(config);
        };



        $scope.$on('$destroy', function() {
            clearAllTimeouts();
        });

        // Fetch allowed permissions
        AccessControlService.getUserAllowedActions()
                .then(function(actionSet) {
                    self.allowAdmin = AccessControlService.hasAction(AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
                });

        if(self.tab != ''){
            var index = _.indexOf(tabNames,self.tab);
            if(index >=0){
                self.tabMetadata.selectedIndex = index;
            }
        }


    }

    function JobFilterHelpPanelMenuCtrl(mdPanelRef) {
        this._mdPanelRef = mdPanelRef;

    }

    /**
     * The Controller used for the abandon all
     */
    var abandonAllDialogController = function ($scope, $mdDialog, $interval,feedName) {
        var self = this;

        $scope.feedName = feedName;
        $scope.message = "Abandoning the failed jobs for "+feedName;
        var counter = 0;
        var index = 0;
        var messages = [];
        messages.push("Still working. Abandoning the failed jobs for "+feedName);
        messages.push("Hang tight. Still working.")
        messages.push("Just a little while longer.")
        messages.push("Should be done soon.")
        messages.push("Still working.  Almost done.")
        messages.push("It's taking longer than expected.  Should be done soon.")
        messages.push("It's taking longer than expected.  Still working...")

        function updateMessage(){
            counter++;
            var len = messages.length;
            if(counter %2 == 0 && counter >2) {
                index = index < (len-1) ? index+1 : index;
            }
            $scope.message = messages[index];

        }
        var messageInterval = $interval(function() {
            updateMessage();

        },5000);

        function cancelMessageInterval(){
            if(messageInterval != null) {
                $interval.cancel(messageInterval);
            }
        }


        $scope.hide = function () {
            cancelMessageInterval();
            $mdDialog.hide();

        };

        $scope.cancel = function () {
            cancelMessageInterval();
            $mdDialog.cancel();
        };

    };

    angular.module(moduleName).controller('AbandonAllJobsDialogController', ["$scope","$mdDialog","$interval","feedName",abandonAllDialogController]);

    angular.module(moduleName).controller("JobFilterHelpPanelMenuCtrl", ["mdPanelRef",JobFilterHelpPanelMenuCtrl]);

    angular.module(moduleName).controller("JobsCardController", ["$scope","$http","$mdDialog","$timeout","$mdMenu","$q","$mdToast","$mdPanel","OpsManagerJobService","TableOptionsService","PaginationDataService","StateService","IconService","TabService","AccessControlService","BroadcastService",JobsCardController]);
    angular.module(moduleName).directive('tbaJobs', directive);
});

