define(["require", "exports", "angular", "./module-name", "underscore", "pascalprecht.translate"], function (require, exports, angular, module_name_1, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var JobsCardController = /** @class */ (function () {
        function JobsCardController($scope, $http, $mdDialog, $timeout, $mdMenu, $q, $mdToast, $mdPanel, OpsManagerJobService, TableOptionsService, PaginationDataService, StateService, IconService, TabService, AccessControlService, BroadcastService, OpsManagerRestUrlService) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.$mdDialog = $mdDialog;
            this.$timeout = $timeout;
            this.$mdMenu = $mdMenu;
            this.$q = $q;
            this.$mdToast = $mdToast;
            this.$mdPanel = $mdPanel;
            this.OpsManagerJobService = OpsManagerJobService;
            this.TableOptionsService = TableOptionsService;
            this.PaginationDataService = PaginationDataService;
            this.StateService = StateService;
            this.IconService = IconService;
            this.TabService = TabService;
            this.AccessControlService = AccessControlService;
            this.BroadcastService = BroadcastService;
            this.OpsManagerRestUrlService = OpsManagerRestUrlService;
            this.updateJobs = function () {
                _this.loadJobs(true);
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
                _this.TabService.selectedTab(_this.pageName, tab);
                return _this.loadJobs(true).promise;
            };
            this.onOrderChange = function (order) {
                _this.PaginationDataService.sort(_this.pageName, order);
                _this.TableOptionsService.setSortOption(_this.pageName, order);
                return _this.loadJobs(true).promise;
                //return this.deferred.promise;
            };
            this.onPaginationChange = function (page, limit) {
                var activeTab = _this.TabService.getActiveTab(_this.pageName);
                //only trigger the reload if the initial page has been loaded.
                //md-data-table will call this function when the page initially loads and we dont want to have it run the query again.\
                //on load the query will be triggered via onTabSelected() method
                if (_this.loaded) {
                    activeTab.currentPage = page;
                    _this.PaginationDataService.currentPage(_this.pageName, activeTab.title, page);
                    return _this.loadJobs(true).promise;
                }
            };
            /**
             * Build the possible Sorting Options
             * @returns {*[]}
             */
            this.loadSortOptions = function () {
                var options = { 'Job Name': 'jobName', 'Start Time': 'startTime', 'Status': 'status' };
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
                if (_this.feed) {
                    //only show the abandon all on the feeds page that are unhealthy
                    options.push(_this.TableOptionsService.newOption("Actions", 'actions_header', true, false));
                    options.push(_this.TableOptionsService.newOption("Abandon All", 'abandon_all', false, false));
                }
                return options;
            };
            this.selectedAdditionalMenuOption = function (item) {
                if (item.type == 'abandon_all') {
                    _this.$mdMenu.hide();
                    _this.$mdDialog.show({
                        controller: "AbandonAllJobsDialogController",
                        templateUrl: 'js/ops-mgr/jobs/abandon-all-jobs-dialog.html',
                        parent: angular.element(document.body),
                        clickOutsideToClose: false,
                        fullscreen: true,
                        locals: {
                            feedName: _this.feedFilter
                        }
                    });
                    _this.OpsManagerJobService.abandonAllJobs(_this.feedFilter, function () {
                        _this.$mdDialog.hide();
                        _this.BroadcastService.notify('ABANDONED_ALL_JOBS', { feed: _this.feedFilter });
                        _this.$mdToast.show(_this.$mdToast.simple()
                            .textContent('Abandoned all failed jobs for the feed')
                            .hideDelay(3000));
                    }, function (err) {
                        _this.$mdDialog.hide();
                        _this.$mdToast.show(_this.$mdToast.simple()
                            .textContent('Unable to abandonal all jobs for the feed.  A unexpected error occurred.')
                            .hideDelay(3000));
                    });
                }
            };
            /**
             *
             * @param options
             */
            this.onOptionsMenuOpen = function (options) {
                if (_this.feed) {
                    var abandonOption = _.find(options.additionalOptions, function (option) {
                        return option.type == 'abandon_all';
                    });
                    if (abandonOption != null && abandonOption != undefined) {
                        abandonOption.disabled = _this.feed.healthText != 'UNHEALTHY';
                    }
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
                _this.loadJobs(true);
            };
            //Load Jobs
            this.loadJobs = function (force) {
                if (force || !_this.refreshing) {
                    if (force) {
                        angular.forEach(_this.activeJobRequests, function (canceler, i) {
                            canceler.resolve();
                        });
                        _this.activeJobRequests = [];
                    }
                    _this.clearAllTimeouts();
                    var activeTab = _this.TabService.getActiveTab(_this.pageName);
                    _this.showProgress = true;
                    var sortOptions = '';
                    var tabTitle = activeTab.title;
                    var filters = { tabTitle: tabTitle };
                    var limit = _this.paginationData.rowsPerPage;
                    var start = (limit * activeTab.currentPage) - limit; //this.query.page(this.selectedTab));
                    var sort = _this.PaginationDataService.sort(_this.pageName);
                    var canceler = _this.$q.defer();
                    var transformJobs = function (response) {
                        //transform the data for UI
                        _this.transformJobData(tabTitle, response.data.data);
                        _this.TabService.setTotal(_this.pageName, tabTitle, response.data.recordsFiltered);
                        if (_this.loading) {
                            _this.loading = false;
                        }
                        _this.finishedRequest(canceler);
                    };
                    var successFn = function (response) {
                        if (response.data) {
                            _this.fetchFeedNames(response).then(transformJobs);
                        }
                    };
                    var errorFn = function (err) {
                        _this.finishedRequest(canceler);
                    };
                    _this.activeJobRequests.push(canceler);
                    _this.deferred = canceler;
                    _this.promise = _this.deferred.promise;
                    var filter = _this.filter;
                    var params = { start: start, limit: limit, sort: sort, filter: filter };
                    if (_this.feedFilter) {
                        if (!params.filter) {
                            params.filter = '';
                        }
                        if (params.filter != '') {
                            params.filter += ',';
                        }
                        params.filter += "jobInstance.feed.name==" + _this.feedFilter;
                    }
                    //if the filter doesnt contain an operator, then default it to look for the job name
                    if (params.filter != '' && params.filter != null && !_this.containsFilterOperator(params.filter)) {
                        params.filter = 'job=~%' + params.filter;
                    }
                    var query = tabTitle != 'All' ? tabTitle.toLowerCase() : '';
                    _this.$http.get(_this.OpsManagerJobService.JOBS_QUERY_URL + "/" + query, { timeout: canceler.promise, params: params }).then(successFn, errorFn);
                }
                _this.showProgress = true;
                return _this.deferred;
            };
            this.fetchFeedNames = function (response) {
                var deferred = _this.$q.defer();
                _this.showProgress = true;
                var jobs = response.data.data;
                if (jobs.length > 0) {
                    //_.uniq method to remove duplicates, there may be multiple jobs for the same feed
                    var feedNames = _.uniq(_.map(jobs, function (job) {
                        return job.feedName;
                    }));
                    var namesPromise = _this.$http.post(_this.OpsManagerRestUrlService.FEED_SYSTEM_NAMES_TO_DISPLAY_NAMES_URL, feedNames);
                    namesPromise.then(function (result) {
                        _.each(jobs, function (job) {
                            job.displayName = _.find(result.data, function (systemNameToDisplayName) {
                                return systemNameToDisplayName.key === job.feedName;
                            });
                        });
                        _this.showProgress = false;
                        deferred.resolve(response);
                    }, function (err) {
                        console.error('Failed to receive feed names', err);
                        _this.showProgress = false;
                        deferred.resolve(response);
                    });
                }
                else {
                    deferred.resolve(response);
                }
                return deferred.promise;
            };
            this.containsFilterOperator = function (filterStr) {
                var contains = false;
                var ops = ['==', '>', '<', '>=', '<=', '=~'];
                for (var i = 0; i < ops.length; i++) {
                    contains = filterStr.indexOf(ops[i]) >= 0;
                    if (contains) {
                        break;
                    }
                }
                return contains;
            };
            this.updateJob = function (instanceId, newJob) {
                _this.clearErrorMessage(instanceId);
                _this.getRunningJobExecutionData(instanceId, newJob.executionId);
            };
            this.clearErrorMessage = function (instanceId) {
                var existingJob = _this.jobIdMap[instanceId];
                if (existingJob) {
                    existingJob.errorMessage = '';
                }
            };
            this.addJobErrorMessage = function (instanceId, message) {
                var existingJob = _this.jobIdMap[instanceId];
                if (existingJob) {
                    existingJob.errorMessage = message;
                }
            };
            this.finishedRequest = function (canceler) {
                var index = _.indexOf(_this.activeJobRequests, canceler);
                if (index >= 0) {
                    _this.activeJobRequests.splice(index, 1);
                }
                canceler.resolve();
                canceler = null;
                _this.refreshing = false;
                _this.showProgress = false;
                _this.loaded = true;
            };
            this.transformJobData = function (tabTitle, jobs) {
                //first clear out the arrays
                _this.jobIdMap = {};
                _this.TabService.clearTabs(_this.pageName);
                angular.forEach(jobs, function (job, i) {
                    var transformedJob = _this.transformJob(job);
                    _this.TabService.addContent(_this.pageName, tabTitle, transformedJob);
                });
                return jobs;
            };
            this.clearAllTimeouts = function () {
                angular.forEach(_this.timeoutMap, function (timeoutInstance, instanceId) {
                    _this.$timeout.cancel(timeoutInstance);
                    delete _this.timeoutMap[instanceId];
                });
                _this.timeoutMap = {};
            };
            this.clearRefreshTimeout = function (instanceId) {
                var timeoutInstance = _this.timeoutMap[instanceId];
                if (timeoutInstance) {
                    _this.$timeout.cancel(timeoutInstance);
                    delete _this.timeoutMap[instanceId];
                }
            };
            this.transformJob = function (job) {
                job.errorMessage = '';
                var executionId = job.executionId;
                var instanceId = job.instanceId;
                job.icon = _this.IconService.iconForJobStatus(job.displayStatus);
                if (_this.jobIdMap[job.instanceId] == undefined) {
                    _this.jobIdMap[job.instanceId] = job;
                }
                else {
                    angular.extend(_this.jobIdMap[job.instanceId], job);
                }
                var shouldRefresh = false;
                if (job.status == 'STARTING' || job.status == 'STARTED' || job.status == 'STOPPING') {
                    shouldRefresh = true;
                }
                var wasRefreshing = _this.timeoutMap[instanceId];
                if (!shouldRefresh) {
                    _this.$timeout(function () {
                        _this.triggerJobActionListener("updateEnd", job);
                    }, 10);
                }
                _this.clearRefreshTimeout(instanceId);
                //Refresh the Job Row if needed
                if (shouldRefresh) {
                    _this.timeoutMap[instanceId] = _this.$timeout(function () {
                        _this.getRunningJobExecutionData(instanceId, executionId);
                    }, 1000);
                }
                return job;
            };
            //Util Functions
            this.capitalize = function (string) {
                return string.charAt(0).toUpperCase() + string.substring(1).toLowerCase();
            };
            this.jobDetails = function (event, job) {
                if (job.stream) {
                    _this.StateService.OpsManager().Feed().navigateToFeedStats(job.jobName);
                }
                else {
                    _this.StateService.OpsManager().Job().navigateToJobDetails(job.executionId);
                }
            };
            this.getRunningJobExecutionData = function (instanceId, executionId) {
                var successFn = function (response) {
                    _this.transformJob(response.data);
                    _this.triggerJobActionListener("updated", response.data);
                };
                _this.$http.get(_this.OpsManagerJobService.LOAD_JOB_URL(executionId)).then(successFn);
            };
            this.triggerJobActionListener = function (action, job) {
                if (_this.onJobAction && angular.isFunction(_this.onJobAction)) {
                    _this.onJobAction({ action: action, job: job });
                }
            };
            this.restartJob = function (event, job) {
                event.stopPropagation();
                event.preventDefault();
                var executionId = job.executionId;
                var instanceId = job.instanceId;
                _this.clearRefreshTimeout(instanceId);
                _this.triggerJobActionListener('restartJob', job);
                var xhr = _this.OpsManagerJobService.restartJob(job.executionId, {}, function (response) {
                    _this.updateJob(instanceId, response.data);
                    //  getRunningJobExecutionData(instanceId,data.executionId);
                }, function (errMsg) {
                    _this.addJobErrorMessage(executionId, errMsg);
                });
            };
            this.stopJob = function (event, job) {
                event.stopPropagation();
                event.preventDefault();
                var instanceId = job.instanceId;
                _this.clearRefreshTimeout(instanceId);
                _this.triggerJobActionListener('stopJob', job);
                _this.OpsManagerJobService.stopJob(job.executionId, {}, function (response) {
                    _this.updateJob(instanceId, response.data);
                    //  getRunningJobExecutionData(instanceId,data.executionId);
                });
            };
            this.abandonJob = function (event, job) {
                event.stopPropagation();
                event.preventDefault();
                var instanceId = job.instanceId;
                _this.clearRefreshTimeout(instanceId);
                _this.triggerJobActionListener('abandonJob', job);
                _this.OpsManagerJobService.abandonJob(job.executionId, {}, function (response) {
                    _this.updateJob(instanceId, response.data);
                    _this.triggerJobActionListener('abandonJob', response.data);
                });
            };
            this.failJob = function (event, job) {
                event.stopPropagation();
                event.preventDefault();
                var instanceId = job.executionId;
                _this.clearRefreshTimeout(instanceId);
                _this.triggerJobActionListener('failJob', job);
                _this.OpsManagerJobService.failJob(job.executionId, {}, function (response) {
                    _this.updateJob(instanceId, response.data);
                    _this.triggerJobActionListener('failJob', response.data);
                });
            };
            this.newHelpItem = function (label, description) {
                return { displayName: label, description: description };
            };
            this.showFilterHelpPanel = function (ev) {
                var position = _this.$mdPanel.newPanelPosition()
                    .relativeTo('.filter-help-button')
                    .addPanelPosition(_this.$mdPanel.xPosition.ALIGN_END, _this.$mdPanel.yPosition.BELOW);
                var config = {
                    attachTo: angular.element(document.body),
                    controller: 'JobFilterHelpPanelMenuCtrl',
                    controllerAs: 'ctrl',
                    templateUrl: 'js/ops-mgr/jobs/jobs-filter-help-template.html',
                    panelClass: 'filter-help',
                    position: position,
                    locals: {
                        'filterHelpExamples': _this.filterHelpExamples,
                        'filterHelpOperators': _this.filterHelpOperators,
                        'filterHelpFields': _this.filterHelpFields
                    },
                    openFrom: ev,
                    clickOutsideToClose: true,
                    escapeToClose: true,
                    focusOnOpen: false,
                    zIndex: 2
                };
                _this.$mdPanel.open(config);
            };
            /**
            * Indicates that admin operations are allowed.
            * @type {boolean}
            */
            this.allowAdmin = false;
            if (this.hideFeedColumn == undefined) {
                this.hideFeedColumn = false;
            }
            this.pageName = angular.isDefined(this.pageName) ? this.pageName : 'jobs';
            //Page State
            this.loading = true;
            this.showProgress = true;
            //map of jobInstanceId to the Job
            this.jobIdMap = {};
            //Track active requests and be able to cancel them if needed
            this.activeJobRequests = [];
            //Track those Jobs who are refreshing because they are running
            this.timeoutMap = {};
            //Pagination and view Type (list or table)
            this.paginationData = PaginationDataService.paginationData(this.pageName);
            PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', '100']);
            this.viewType = PaginationDataService.viewType(this.pageName);
            //Setup the Tabs
            var tabNames = ['All', 'Running', 'Failed', 'Completed', 'Abandoned']; //, 'Stopped'];
            this.tabs = TabService.registerTabs(this.pageName, tabNames, this.paginationData.activeTab);
            this.tabMetadata = TabService.metadata(this.pageName);
            this.sortOptions = this.loadSortOptions();
            this.abandonAllMenuOption = {};
            this.additionalMenuOptions = this.loadAdditionalMenuOptions();
            this.selectedAdditionalMenuOptionVar = this.selectedAdditionalMenuOption;
            var loaded = false;
            /**
             * The filter supplied in the page
             * @type {string}
             */
            this.filter = angular.isUndefined(this.filter) ? '' : this.filter;
            this.tab = angular.isUndefined(this.tab) ? '' : this.tab;
            BroadcastService.subscribe($scope, 'ABANDONED_ALL_JOBS', this.updateJobs);
            $scope.$watch(function () {
                return _this.viewType;
            }, function (newVal) {
                _this.onViewTypeChange(newVal);
            });
            $scope.$watch(function () {
                return _this.filter;
            }, function (newVal, oldVal) {
                if (newVal != oldVal) {
                    console.log('filter changed ', newVal, oldVal);
                    return _this.loadJobs(true).promise;
                }
            });
            this.filterHelpOperators = [];
            this.filterHelpFields = [];
            this.filterHelpExamples = [];
            this.filterHelpOperators.push(this.newHelpItem("Equals", "=="));
            this.filterHelpOperators.push(this.newHelpItem("Like condition", "=~"));
            this.filterHelpOperators.push(this.newHelpItem("In Clause", "Comma separated surrounded with quote    ==\"value1,value2\"   "));
            this.filterHelpOperators.push(this.newHelpItem("Greater than, less than", ">,>=,<,<="));
            this.filterHelpOperators.push(this.newHelpItem("Multiple Filters", "Filers separated by a comma    field1==value,field2==value  "));
            this.filterHelpFields.push(this.newHelpItem("Filter on a feed name", "feed"));
            this.filterHelpFields.push(this.newHelpItem("Filter on a job name", "job"));
            this.filterHelpFields.push(this.newHelpItem("Filter on a job start time", "jobStartTime"));
            this.filterHelpFields.push(this.newHelpItem("Filter on a job end time", "jobEndTime"));
            this.filterHelpFields.push(this.newHelpItem("Filter on a job id", "executionId"));
            this.filterHelpFields.push(this.newHelpItem("Start time date part filters", "startYear,startMonth,startDay"));
            this.filterHelpFields.push(this.newHelpItem("End time date part filters", "endYear,endMonth,endDay"));
            this.filterHelpExamples.push(this.newHelpItem("Find job names that equal 'my.job1' ", "job==my.job1"));
            this.filterHelpExamples.push(this.newHelpItem("Find job names starting with 'my' ", "job=~my"));
            this.filterHelpExamples.push(this.newHelpItem("Find jobs for 'my.job1' or 'my.job2' ", "job==\"my.job1,my.job2\""));
            this.filterHelpExamples.push(this.newHelpItem("Find 'my.job1' starting in 2017 ", "job==my.job1,startYear==2017"));
            this.filterHelpExamples.push(this.newHelpItem("Find jobs that started on February 1st 2017", "startTime>=2017-02-01,startTime<2017-02-02"));
            $scope.$on('$destroy', function () {
                _this.clearAllTimeouts();
            });
            // Fetch allowed permissions
            AccessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                _this.allowAdmin = AccessControlService.hasAction(AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
            });
            if (this.tab != '') {
                var index = _.indexOf(tabNames, this.tab);
                if (index >= 0) {
                    this.tabMetadata.selectedIndex = index;
                }
            }
        } // end of constructor
        return JobsCardController;
    }());
    exports.JobsCardController = JobsCardController;
    var JobFilterHelpPanelMenuCtrl = /** @class */ (function () {
        function JobFilterHelpPanelMenuCtrl(mdPanelRef) {
            this.mdPanelRef = mdPanelRef;
            this._mdPanelRef = mdPanelRef;
        }
        return JobFilterHelpPanelMenuCtrl;
    }());
    exports.JobFilterHelpPanelMenuCtrl = JobFilterHelpPanelMenuCtrl;
    /**
         * The Controller used for the abandon all
         */
    var abandonAllDialogController = /** @class */ (function () {
        function abandonAllDialogController($scope, $mdDialog, $interval, feedName) {
            var _this = this;
            this.$scope = $scope;
            this.$mdDialog = $mdDialog;
            this.$interval = $interval;
            this.feedName = feedName;
            this.updateMessage = function () {
                _this.counter++;
                var len = _this.messages.length;
                if (_this.counter % 2 == 0 && _this.counter > 2) {
                    _this.index = _this.index < (len - 1) ? _this.index + 1 : _this.index;
                }
                _this.$scope.message = _this.messages[_this.index];
            };
            this.cancelMessageInterval = function () {
                if (_this.messageInterval != null) {
                    _this.$interval.cancel(_this.messageInterval);
                }
            };
            $scope.feedName = feedName;
            $scope.message = "Abandoning the failed jobs for " + feedName;
            this.counter = 0;
            this.index = 0;
            this.messages = [];
            this.messages.push("Still working. Abandoning the failed jobs for " + feedName);
            this.messages.push("Hang tight. Still working.");
            this.messages.push("Just a little while longer.");
            this.messages.push("Should be done soon.");
            this.messages.push("Still working.  Almost done.");
            this.messages.push("It's taking longer than expected.  Should be done soon.");
            this.messages.push("It's taking longer than expected.  Still working...");
            this.messageInterval = $interval(function () { _this.updateMessage(); }, 5000);
            $scope.hide = function () {
                _this.cancelMessageInterval();
                $mdDialog.hide();
            };
            $scope.cancel = function () {
                _this.cancelMessageInterval();
                $mdDialog.cancel();
            };
        }
        return abandonAllDialogController;
    }());
    exports.abandonAllDialogController = abandonAllDialogController;
    angular.module(module_name_1.moduleName).controller('AbandonAllJobsDialogController', ["$scope", "$mdDialog", "$interval", "feedName", abandonAllDialogController]);
    angular.module(module_name_1.moduleName).controller("JobFilterHelpPanelMenuCtrl", ["mdPanelRef", JobFilterHelpPanelMenuCtrl]);
    angular.module(module_name_1.moduleName)
        .controller("JobsCardController", ["$scope", "$http", "$mdDialog", "$timeout", "$mdMenu", "$q", "$mdToast",
        "$mdPanel", "OpsManagerJobService", "TableOptionsService",
        "PaginationDataService", "StateService", "IconService", "TabService",
        "AccessControlService", "BroadcastService", "OpsManagerRestUrlService", JobsCardController]);
    angular.module(module_name_1.moduleName).directive('tbaJobs', [
        function () {
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
                    filter: '=?',
                    tab: '=?'
                },
                controllerAs: 'vm',
                scope: true,
                templateUrl: 'js/ops-mgr/jobs/jobs-template.html',
                controller: "JobsCardController",
                link: function ($scope, element, attrs, controller) {
                }
            };
        }
    ]);
});
//# sourceMappingURL=JobsDirective.js.map