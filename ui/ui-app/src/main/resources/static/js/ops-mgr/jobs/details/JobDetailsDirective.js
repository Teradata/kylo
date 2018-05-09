define(["require", "exports", "angular", "./module-name", "underscore", "pascalprecht.translate"], function (require, exports, angular, module_name_1, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var PageState = /** @class */ (function () {
        function PageState() {
        }
        PageState.prototype.showLoading = function () {
            this.refreshing = false;
            this.loading = true;
            this.showProgress = true;
        };
        PageState.prototype.isRefreshing = function () {
            return this.refreshing;
        };
        PageState.prototype.isLoading = function () {
            return this.loading;
        };
        PageState.prototype.finishedLoading = function () {
            this.loading = false;
        };
        PageState.prototype.finished = function () {
            this.refreshing = false;
            this.showProgress = false;
            this.loading = false;
        };
        return PageState;
    }());
    var Step = /** @class */ (function () {
        function Step() {
        }
        return Step;
    }());
    var Job = /** @class */ (function () {
        function Job() {
        }
        return Job;
    }());
    var StepWithTitle = /** @class */ (function () {
        function StepWithTitle(title, content) {
            this.title = title;
            this.content = content;
        }
        return StepWithTitle;
    }());
    var JobWithTitle = /** @class */ (function () {
        function JobWithTitle(title, content) {
            this.title = title;
            this.content = content;
        }
        return JobWithTitle;
    }());
    var TabAnimationControl = /** @class */ (function () {
        function TabAnimationControl($timeout) {
            this.$timeout = $timeout;
        }
        /**
         * Might not be needed
         */
        TabAnimationControl.prototype.disableTabAnimation = function () {
            angular.element('.job-details-tabs').addClass('no-animation');
        };
        TabAnimationControl.prototype.enableTabAnimation = function () {
            if (this.enableTabAnimationTimeout) {
                this.$timeout.cancel(this.enableTabAnimationTimeout);
            }
            this.enableTabAnimationTimeout = this.$timeout(function () {
                angular.element('.job-details-tabs').removeClass('no-animation');
            }, 1000);
        };
        return TabAnimationControl;
    }());
    var JobDetailsDirectiveController = /** @class */ (function () {
        function JobDetailsDirectiveController($scope, $http, $state, $interval, $timeout, $q, $mdToast, OpsManagerRestUrlService, OpsManagerJobService, IconService, AccessControlService, AngularModuleExtensionService, $filter) {
            var _this = this;
            this.$scope = $scope;
            this.$http = $http;
            this.$state = $state;
            this.$interval = $interval;
            this.$timeout = $timeout;
            this.$q = $q;
            this.$mdToast = $mdToast;
            this.OpsManagerRestUrlService = OpsManagerRestUrlService;
            this.OpsManagerJobService = OpsManagerJobService;
            this.IconService = IconService;
            this.AccessControlService = AccessControlService;
            this.AngularModuleExtensionService = AngularModuleExtensionService;
            this.$filter = $filter;
            /**
             * Flag for admin controls
             */
            this.allowAdmin = false;
            /**
             * Track loading, progress
             * @type {PageState}
             */
            this.pageState = new PageState();
            /**
             * Track active requests and be able to cancel them if needed
             */
            this.activeJobRequests = [];
            /**
             * Array of all the steps
             */
            this.allSteps = [];
            this.UNKNOWN_JOB_EXECUTION_ID = "UNKNOWN";
            //Refresh Intervals
            this.refreshTimeout = null;
            /**
             * Flag indicating the loading of the passed in JobExecutionId was unable to bring back data
             * @type {boolean}
             */
            this.unableToFindJob = false;
            /**
             * Show the Job Params in the Job Details tab
             * @type {boolean}
             */
            this.showJobParameters = true;
            /**
             * Should we show the log ui buttons
             * @type {boolean}
             */
            this.logUiEnabled = false;
            this.pageState.showLoading();
            this.jobData = new Job();
            this.stepData = {};
            this.jobTab = { title: 'JOB', content: this.jobData };
            this.tabAnimationControl = new TabAnimationControl(this.$timeout);
            var cssStatus = {
                'success': ['COMPLETED', 'STARTING', 'STARTED', 'EXECUTING'],
                'error': ['FAILED'],
                'warn': ['STOPPING', 'STOPPED', 'WARNING'],
                'abandoned': ['ABANDONED'],
                'unknown': ['UNKNOWN']
            };
            this.statusCssMap = {};
            _.each(cssStatus, function (arr, key) {
                _.each(arr, function (status, i) {
                    _this.statusCssMap[status] = key;
                });
            });
            $scope.$on("$destroy", this.ngOnDestroy.bind(this));
        }
        JobDetailsDirectiveController.prototype.$onInit = function () {
            this.ngOnInit();
        };
        JobDetailsDirectiveController.prototype.$onDestroy = function () {
            this.ngOnDestroy();
        };
        JobDetailsDirectiveController.prototype.ngOnInit = function () {
            var _this = this;
            this.jobExecutionId = parseInt(this.executionId);
            //init the log ui flag
            this.logUiEnabled = this.AngularModuleExtensionService.stateExists("log-ui");
            // Fetch allowed permissions
            this.AccessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                _this.allowAdmin = _this.AccessControlService.hasAction(_this.AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
            });
            this.loadJobData();
        };
        JobDetailsDirectiveController.prototype.ngOnDestroy = function () {
            this.cancelLoadJobDataTimeout();
        };
        JobDetailsDirectiveController.prototype.abandonJob = function (event) {
            var _this = this;
            event.stopPropagation();
            event.preventDefault();
            var executionId = this.jobData.executionId;
            this.OpsManagerJobService.abandonJob(this.jobData.executionId, { includeSteps: true }, function (response) {
                _this.updateJob(executionId, response.data);
            });
        };
        JobDetailsDirectiveController.prototype.failJob = function (event) {
            var _this = this;
            event.stopPropagation();
            event.preventDefault();
            var executionId = this.jobData.executionId;
            var _fail = function () {
                _this.OpsManagerJobService.failJob(_this.jobData.executionId, { includeSteps: true }, function (response) {
                    _this.updateJob(executionId, response.data);
                });
            };
            if (this.jobData.renderTriggerRetry) {
                this.triggerSavepointReleaseFailure(_fail);
            }
            else {
                _fail();
            }
        };
        ;
        JobDetailsDirectiveController.prototype.restartJob = function (event) {
            var _this = this;
            event.stopPropagation();
            event.preventDefault();
            var executionId = this.jobData.executionId;
            this.OpsManagerJobService.restartJob(this.jobData.executionId, { includeSteps: true }, function (response) {
                _this.updateJob(executionId, response.data);
            }, function (errMsg) {
                _this.addJobErrorMessage(errMsg);
            });
        };
        ;
        JobDetailsDirectiveController.prototype.triggerSavepointRetry = function () {
            var _this = this;
            if (angular.isDefined(this.jobData.triggerRetryFlowfile)) {
                this.jobData.renderTriggerRetry = false;
                this.$http.post(this.OpsManagerRestUrlService.TRIGGER_SAVEPOINT_RETRY(this.jobExecutionId, this.jobData.triggerRetryFlowfile), null).then(function () {
                    _this.$mdToast.show(_this.$mdToast.simple()
                        .textContent('Triggered the retry')
                        .hideDelay(3000));
                    _this.loadJobData(true);
                });
            }
        };
        JobDetailsDirectiveController.prototype.triggerSavepointReleaseFailure = function (callbackFn) {
            var _this = this;
            if (angular.isDefined(this.jobData.triggerRetryFlowfile)) {
                this.$http.post(this.OpsManagerRestUrlService.TRIGGER_SAVEPOINT_RELEASE(this.jobExecutionId, this.jobData.triggerRetryFlowfile), null).then(function (response) {
                    _this.$mdToast.show(_this.$mdToast.simple()
                        .textContent('Triggered the release and failure')
                        .hideDelay(3000));
                    if (angular.isDefined(callbackFn)) {
                        callbackFn();
                    }
                    _this.loadJobData(true);
                });
            }
        };
        JobDetailsDirectiveController.prototype.navigateToLogs = function (jobStartTime, jobEndTime) {
            this.$state.go("log-ui", { startTime: jobStartTime, endTime: jobEndTime, showCustom: true });
        };
        JobDetailsDirectiveController.prototype.navigateToLogsForStep = function (failedStep) {
            var previousStep = '';
            for (var title in this.stepData) {
                var step = this.stepData[title];
                if (failedStep.title == title) {
                    break;
                }
                previousStep = step;
            }
            this.$state.go("log-ui", { startTime: previousStep.startTime, endTime: failedStep.content.endTime, showCustom: true });
        };
        //Tab Functions
        JobDetailsDirectiveController.prototype.toggleJobParameters = function (name) {
            if (name == 'JobParameters') {
                this.showJobParameters = true;
            }
            else {
                this.showJobParameters = false;
            }
        };
        JobDetailsDirectiveController.prototype.cancelLoadJobDataTimeout = function () {
            if (this.refreshTimeout != null) {
                this.$timeout.cancel(this.refreshTimeout);
                this.refreshTimeout = null;
            }
        };
        //Load Feeds
        JobDetailsDirectiveController.prototype.loadJobData = function (force) {
            var _this = this;
            this.cancelLoadJobDataTimeout();
            if (force || !this.pageState.refreshing) {
                this.unableToFindJob = false;
                if (force) {
                    angular.forEach(this.activeJobRequests, function (canceler, i) {
                        canceler.resolve();
                    });
                    this.activeJobRequests = [];
                }
                this.pageState.refreshing = true;
                var sortOptions = '';
                var canceler = this.$q.defer();
                var successFn = function (response) {
                    if (response.data) {
                        //transform the data for UI
                        _this.transformJobData(response.data);
                        if (response.data.running == true || response.data.stopping == true) {
                            _this.cancelLoadJobDataTimeout();
                            _this.refreshTimeout = _this.$timeout(function () {
                                _this.loadJobData();
                            }, 1000);
                        }
                        _this.pageState.finishedLoading();
                    }
                    else {
                        _this.unableToFindJob = true;
                    }
                    _this.finishedRequest(canceler);
                };
                var errorFn = function (err) {
                    _this.finishedRequest(canceler);
                    _this.unableToFindJob = true;
                    _this.addJobErrorMessage(err);
                };
                var finallyFn = function () {
                };
                this.activeJobRequests.push(canceler);
                this.deferred = canceler;
                var params = { 'includeSteps': true };
                this.$http.get(this.OpsManagerJobService.LOAD_JOB_URL(this.jobExecutionId), { timeout: canceler.promise, params: params }).then(successFn, errorFn);
            }
            return this.deferred;
        };
        JobDetailsDirectiveController.prototype.finishedRequest = function (canceler) {
            var index = _.indexOf(this.activeJobRequests, canceler);
            if (index >= 0) {
                this.activeJobRequests.splice(index, 1);
            }
            this.tabAnimationControl.enableTabAnimation();
            canceler.resolve();
            canceler = null;
            this.pageState.finished();
        };
        JobDetailsDirectiveController.prototype.mapToArray = function (map, obj, type, fieldName, removeKeys) {
            if (removeKeys == undefined) {
                removeKeys = [];
            }
            var arr = [];
            var renderTriggerSavepointRetry = false;
            var jobComplete = false;
            for (var key in map) {
                if (_.indexOf(removeKeys, key) == -1) {
                    if (map.hasOwnProperty(key)) {
                        arr.push({ key: key, value: map[key] });
                        if (type == 'JOB' && fieldName == 'executionContextArray') {
                            if (key == 'kylo.job.finished') {
                                jobComplete = true;
                            }
                            if (!renderTriggerSavepointRetry) {
                                renderTriggerSavepointRetry = this.checkTriggerSavepoint(obj, key, map[key]);
                            }
                        }
                    }
                }
            }
            if (type == 'JOB' && fieldName == 'executionContextArray' && (!renderTriggerSavepointRetry || jobComplete)) {
                obj.renderTriggerRetry = false;
            }
            obj[fieldName] = arr;
        };
        JobDetailsDirectiveController.prototype.checkTriggerSavepoint = function (job, key, value) {
            if (key == 'savepoint.trigger.flowfile' && angular.isDefined(value)) {
                {
                    job.renderTriggerRetry = true;
                    job.triggerRetryFlowfile = value;
                    return true;
                }
            }
            return false;
        };
        JobDetailsDirectiveController.prototype.assignParameterArray = function (obj, type) {
            if (obj) {
                if (obj.jobParameters) {
                    this.mapToArray(obj.jobParameters, obj, type, 'jobParametersArray');
                }
                else {
                    obj['jobParametersArray'] = [];
                }
                if (obj.executionContext) {
                    this.mapToArray(obj.executionContext, obj, type, 'executionContextArray', ['batch.stepType', 'batch.taskletType']);
                }
                else {
                    obj['executionContextArray'] = [];
                }
            }
        };
        JobDetailsDirectiveController.prototype.cssClassForDisplayStatus = function (displayStatus) {
            return this.statusCssMap[displayStatus];
        };
        JobDetailsDirectiveController.prototype.transformJobData = function (job) {
            var _this = this;
            this.assignParameterArray(job, 'JOB');
            job.name = job.jobName;
            job.running = false;
            job.stopping = false;
            job.exitDescription = job.exitStatus;
            if (job.exitDescription == undefined || job.exitDescription == '') {
                job.exitDescription = this.$filter('translate')('views.JobDetailsDirective.Nda');
            }
            job.tabIcon = undefined;
            var iconStyle = this.IconService.iconStyleForJobStatus(job.displayStatus);
            var icon = this.IconService.iconForJobStatus(job.displayStatus);
            job.cssStatusClass = this.cssClassForDisplayStatus(job.displayStatus);
            if (job.status == "STARTED") {
                job.running = true;
            }
            if (job.status == 'STOPPING') {
                job.stopping = true;
            }
            job.statusIcon = icon;
            job.tabIconStyle = iconStyle;
            angular.extend(this.jobData, job);
            if (job.executedSteps) {
                //sort by start time then eventId
                job.executedSteps = _.chain(job.executedSteps).sortBy('nifiEventId').sortBy('startTime').value();
                angular.forEach(job.executedSteps, function (step, i) {
                    var stepName = "Step " + (i + 1);
                    if (_this.stepData[stepName] == undefined) {
                        _this.stepData[stepName] = new Step();
                        _this.allSteps.push({ title: stepName, content: _this.stepData[stepName] });
                    }
                    angular.extend(_this.stepData[stepName], _this.transformStep(step));
                });
            }
        };
        JobDetailsDirectiveController.prototype.transformStep = function (step) {
            step.name = step.stepName;
            step.running = false;
            step.tabIcon = undefined;
            if (step.runTime == undefined && step.endTime && step.startTime) {
                step.runTime = step.endTime - step.startTime;
            }
            if (step.endTime == undefined && step.startTime) {
                step.running = true;
                if (step.runTime == undefined) {
                    step.runTime = new Date().getTime() - step.startTime;
                }
            }
            step.displayStatus = step.exitCode;
            if (step.exitDescription == undefined || step.exitDescription == '') {
                step.exitDescription = this.$filter('translate')('views.JobDetailsDirective.Nda');
            }
            var style = this.IconService.iconStyleForJobStatus(step.displayStatus);
            var icon = this.IconService.iconForJobStatus(step.displayStatus);
            step.cssStatusClass = this.cssClassForDisplayStatus(step.displayStatus);
            step.statusIcon = icon;
            if (step.displayStatus == 'FAILED' || step.displayStatus == 'EXECUTING' || step.displayStatus == 'WARNING') {
                step.tabIconStyle = style;
                step.tabIcon = icon;
            }
            if (step.startTime == null || step.startTime == undefined) {
                step.disabled = true;
            }
            else {
                step.disabled = false;
            }
            this.assignParameterArray(step, 'STEP');
            return step;
        };
        JobDetailsDirectiveController.prototype.updateJob = function (executionId, job) {
            this.clearErrorMessage();
            var existingJob = this.jobData;
            if (existingJob && executionId == job.executionId) {
                this.transformJobData(job);
            }
            else {
                this.tabAnimationControl.disableTabAnimation();
                this.loadJobExecution(job.executionId);
            }
        };
        JobDetailsDirectiveController.prototype.loadJobExecution = function (executionId) {
            var _this = this;
            this.jobExecutionId = executionId;
            //reset steps
            var len = this.allSteps.length;
            while (len > 1) {
                this.allSteps.splice(len - 1, 1);
                len = this.allSteps.length;
            }
            //clear out all the steps
            angular.forEach(Object.keys(this.stepData), function (stepName, i) {
                delete _this.stepData[stepName];
            });
            this.loadJobData(true);
        };
        JobDetailsDirectiveController.prototype.addJobErrorMessage = function (errMsg) {
            var existingJob = this.jobData;
            if (existingJob) {
                existingJob.errorMessage = errMsg;
            }
        };
        JobDetailsDirectiveController.prototype.clearErrorMessage = function () {
            var existingJob = this.jobData;
            if (existingJob) {
                existingJob.errorMessage = '';
            }
        };
        JobDetailsDirectiveController.$inject = ["$scope", "$http", "$state", "$interval", "$timeout", "$q",
            "$mdToast", "OpsManagerRestUrlService",
            "OpsManagerJobService", "IconService", "AccessControlService", "AngularModuleExtensionService",
            "$filter"];
        return JobDetailsDirectiveController;
    }());
    exports.JobDetailsDirectiveController = JobDetailsDirectiveController;
    angular.module(module_name_1.moduleName)
        .controller("JobDetailsDirectiveController", JobDetailsDirectiveController);
    angular.module(module_name_1.moduleName).directive("tbaJobDetails", [
        function () {
            return {
                restrict: "EA",
                bindToController: {
                    cardTitle: "@",
                    executionId: '='
                },
                controllerAs: 'vm',
                scope: {},
                templateUrl: 'js/ops-mgr/jobs/details/job-details-template.html',
                controller: "JobDetailsDirectiveController",
                link: function ($scope, element, attrs, controller) {
                }
            };
        }
    ]);
});
//# sourceMappingURL=JobDetailsDirective.js.map