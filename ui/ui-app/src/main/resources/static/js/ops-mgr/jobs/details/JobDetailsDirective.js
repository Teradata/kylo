define(["require", "exports", "angular", "./module-name", "underscore", "../../../services/AccessControlService", "pascalprecht.translate"], function (require, exports, angular, module_name_1, _, AccessControlService_1) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var JobDetailsDirectiveController = /** @class */ (function () {
        function JobDetailsDirectiveController($scope, $http, $state, $interval, $timeout, $q, $mdToast, OpsManagerRestUrlService, OpsManagerJobService, IconService, accessControlService, AngularModuleExtensionService, $filter) {
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
            this.accessControlService = accessControlService;
            this.AngularModuleExtensionService = AngularModuleExtensionService;
            this.$filter = $filter;
            this.init = function () {
                var executionId = _this.executionId;
                _this.jobExecutionId = executionId;
                _this.relatedJob = _this.jobExecutionId;
                _this.loadJobData();
                //   loadRelatedJobs();
            };
            this.nextTab = function () {
                _this.tabMetadata.selectedIndex = Math.min(_this.tabMetadata.selectedIndex + 1, 2);
            };
            this.previousTab = function () {
                _this.tabMetadata.selectedIndex = Math.max(_this.tabMetadata.selectedIndex - 1, 0);
            };
            this.logUiEnabled = function () {
                _this.logUiEnabledVar = _this.AngularModuleExtensionService.stateExists("log-ui");
            };
            this.navigateToLogs = function (jobStartTime, jobEndTime) {
                _this.$state.go("log-ui", { startTime: jobStartTime, endTime: jobEndTime, showCustom: true });
            };
            this.navigateToLogsForStep = function (failedStep) {
                var previousStep = '';
                for (var title in _this.stepData) {
                    var step = _this.stepData[title];
                    if (failedStep.title == title) {
                        break;
                    }
                    previousStep = step;
                }
                _this.$state.go("log-ui", { startTime: previousStep.startTime, endTime: failedStep.content.endTime, showCustom: true });
            };
            //Tab Functions
            this.toggleJobParameters = function (name) {
                if (name == 'JobParameters') {
                    _this.showJobParameters = true;
                }
                else {
                    _this.showJobParameters = false;
                }
            };
            this.selectFirstTab = function () {
                _this.tabMetadata.selectedIndex = 0;
                // this.selectedTab = this.jobTabs[0];
            };
            this.cancelLoadJobDataTimeout = function () {
                if (_this.refreshTimeout != null) {
                    _this.$timeout.cancel(_this.refreshTimeout);
                    _this.refreshTimeout = null;
                }
            };
            //Load Feeds
            this.loadJobData = function (force) {
                _this.cancelLoadJobDataTimeout();
                if (force || !_this.refreshing) {
                    _this.unableToFindJob = false;
                    if (force) {
                        angular.forEach(_this.activeJobRequests, function (canceler, i) {
                            canceler.resolve();
                        });
                        _this.activeJobRequests = [];
                    }
                    _this.refreshing = true;
                    var sortOptions = '';
                    var canceler = _this.$q.defer();
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
                            if (_this.loading) {
                                _this.loading = false;
                            }
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
                    _this.activeJobRequests.push(canceler);
                    _this.deferred = canceler;
                    var params = { 'includeSteps': true };
                    _this.$http.get(_this.OpsManagerJobService.LOAD_JOB_URL(_this.jobExecutionId), { timeout: canceler.promise, params: params }).then(successFn, errorFn);
                }
                return _this.deferred;
            };
            this.finishedRequest = function (canceler) {
                var index = _.indexOf(_this.activeJobRequests, canceler);
                if (index >= 0) {
                    _this.activeJobRequests.splice(index, 1);
                }
                _this.enableTabAnimation();
                canceler.resolve();
                canceler = null;
                _this.refreshing = false;
                _this.showProgress = false;
            };
            this.loadRelatedJobs = function (setExecutionId) {
                var successFn = function (response) {
                    if (response.data) {
                        _this.relatedJobs = response.data;
                        if (setExecutionId) {
                            _this.relatedJob = setExecutionId;
                        }
                        _this.updateRelatedJobIndex();
                    }
                };
                var errorFn = function (err) {
                };
                //todo uncomment once related job are linked and working
                // $http.get(OpsManagerJobService.RELATED_JOBS_URL(this.jobExecutionId)).then(successFn, errorFn);
            };
            this.updateRelatedJobIndex = function () {
                if (_this.relatedJob) {
                    angular.forEach(_this.relatedJobs, function (job, i) {
                        if (job.jobExecutionId == _this.relatedJob) {
                            _this.relatedJobIndex = i;
                        }
                    });
                }
            };
            this.disableTabAnimation = function () {
                angular.element('.job-details-tabs').addClass('no-animation');
            };
            this.changeRelatedJob = function (relatedJob) {
                //remove animation for load
                _this.disableTabAnimation();
                _this.loadJobExecution(relatedJob);
            };
            this.mapToArray = function (map, obj, fieldName, removeKeys) {
                if (removeKeys == undefined) {
                    removeKeys = [];
                }
                var arr = [];
                for (var key in map) {
                    if (_.indexOf(removeKeys, key) == -1) {
                        if (map.hasOwnProperty(key)) {
                            arr.push({ key: key, value: map[key] });
                        }
                    }
                }
                obj[fieldName] = arr;
            };
            this.assignParameterArray = function (obj) {
                if (obj) {
                    if (obj.jobParameters) {
                        _this.mapToArray(obj.jobParameters, obj, 'jobParametersArray');
                    }
                    else {
                        obj['jobParametersArray'] = [];
                    }
                    if (obj.executionContext) {
                        _this.mapToArray(obj.executionContext, obj, 'executionContextArray', ['batch.stepType', 'batch.taskletType']);
                    }
                    else {
                        obj['executionContextArray'] = [];
                    }
                }
            };
            this.cssClassForDisplayStatus = function (displayStatus) {
                var cssStatus = {
                    'success': ['COMPLETED', 'STARTING', 'STARTED', 'EXECUTING'],
                    'error': ['FAILED'],
                    'warn': ['STOPPING', 'STOPPED', 'WARNING'],
                    'abandoned': ['ABANDONED'],
                    'unknown': ['UNKNOWN']
                };
                _this.statusCssMap = {};
                _.each(cssStatus, function (arr, key) {
                    _.each(arr, function (status, i) {
                        _this.statusCssMap[status] = key;
                    });
                });
                return _this.statusCssMap[displayStatus];
            };
            this.transformJobData = function (job) {
                _this.assignParameterArray(job);
                job.name = job.jobName;
                job.running = false;
                job.stopping = false;
                job.exitDescription = job.exitStatus;
                if (job.exitDescription == undefined || job.exitDescription == '') {
                    job.exitDescription = _this.$filter('translate')('views.JobDetailsDirective.Nda');
                }
                job.tabIcon = undefined;
                var iconStyle = _this.IconService.iconStyleForJobStatus(job.displayStatus);
                var icon = _this.IconService.iconForJobStatus(job.displayStatus);
                job.cssStatusClass = _this.cssClassForDisplayStatus(job.displayStatus);
                if (job.status == "STARTED") {
                    job.running = true;
                }
                if (job.status == 'STOPPING') {
                    job.stopping = true;
                }
                job.statusIcon = icon;
                job.tabIconStyle = iconStyle;
                angular.extend(_this.jobData, job);
                if (job.executedSteps) {
                    //Sort first by NiFi Event Id (only if its there and >=0 )
                    // then by the start time
                    job.executedSteps.sort(function (a, b) {
                        function compareValues(a1, b1) {
                            if (a1 > b1)
                                return 1;
                            if (b1 > a1)
                                return -1;
                            return 0;
                        }
                        var startTimeA = a['startTime'];
                        var startTimeB = b['startTime'];
                        var eventIdA = a['nifiEventId'];
                        var eventIdB = b['nifiEventId'];
                        var compareEventIdA = eventIdA != undefined && eventIdA >= 0 - 1;
                        var compareEventIdB = eventIdB != undefined && eventIdB >= 0 - 1;
                        var compare = 0;
                        if (compareEventIdA && compareEventIdB) {
                            compare = compareValues(eventIdA, eventIdB);
                        }
                        if (compare == 0) {
                            compare = compareValues(startTimeA, startTimeB);
                        }
                        return compare;
                    });
                    angular.forEach(job.executedSteps, function (step, i) {
                        var stepName = "Step " + (i + 1);
                        if (_this.stepData[stepName] == undefined) {
                            _this.stepData[stepName] = {};
                            _this.allSteps.push({ title: stepName, content: _this.stepData[stepName] });
                        }
                        angular.extend(_this.stepData[stepName], _this.transformStep(step));
                    });
                }
            };
            this.transformStep = function (step) {
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
                    step.exitDescription = _this.$filter('translate')('views.JobDetailsDirective.Nda');
                }
                var style = _this.IconService.iconStyleForJobStatus(step.displayStatus);
                var icon = _this.IconService.iconForJobStatus(step.displayStatus);
                step.cssStatusClass = _this.cssClassForDisplayStatus(step.displayStatus);
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
                _this.assignParameterArray(step);
                return step;
            };
            this.clearRefreshInterval = function () {
                if (_this.refreshInterval != null) {
                    _this.$interval.cancel(_this.refreshInterval);
                    _this.refreshInterval = null;
                }
            };
            this.setRefreshInterval = function () {
                _this.clearRefreshInterval();
                if (_this.refreshIntervalTime) {
                    _this.refreshInterval = _this.$interval(_this.loadJobs, _this.refreshIntervalTime);
                }
            };
            //Util Functions
            this.capitalize = function (string) {
                return string.charAt(0).toUpperCase() + string.substring(1).toLowerCase();
            };
            this.updateJob = function (executionId, job) {
                _this.clearErrorMessage();
                var existingJob = _this.jobData;
                if (existingJob && executionId == job.executionId) {
                    _this.transformJobData(job);
                }
                else {
                    _this.disableTabAnimation();
                    _this.loadJobExecution(job.executionId);
                }
            };
            this.loadJobExecution = function (executionId) {
                _this.jobExecutionId = executionId;
                //reset steps
                var len = _this.allSteps.length;
                while (len > 1) {
                    _this.allSteps.splice(len - 1, 1);
                    len = _this.allSteps.length;
                }
                //clear out all the steps
                angular.forEach(Object.keys(_this.stepData), function (key, i) {
                    delete _this.stepData[key];
                });
                _this.loadJobData(true);
                //  loadRelatedJobs(executionId);
            };
            this.addJobErrorMessage = function (errMsg) {
                var existingJob = _this.jobData;
                if (existingJob) {
                    existingJob.errorMessage = errMsg;
                }
            };
            this.clearErrorMessage = function () {
                var existingJob = _this.jobData;
                if (existingJob) {
                    existingJob.errorMessage = '';
                }
            };
            this.restartJob = function (event) {
                event.stopPropagation();
                event.preventDefault();
                var executionId = _this.jobData.executionId;
                var xhr = _this.OpsManagerJobService.restartJob(_this.jobData.executionId, { includeSteps: true }, function (response) {
                    _this.updateJob(executionId, response.data);
                    //  loadJobs(true);
                }, function (errMsg) {
                    _this.addJobErrorMessage(executionId + errMsg); // executionId, errMsg
                });
            };
            this.stopJob = function (event) {
                event.stopPropagation();
                event.preventDefault();
                var executionId = _this.jobData.executionId;
                _this.OpsManagerJobService.stopJob(_this.jobData.executionId, { includeSteps: true }, function (response) {
                    _this.updateJob(executionId, response.data);
                    //  loadJobs(true);
                });
            };
            this.abandonJob = function (event) {
                event.stopPropagation();
                event.preventDefault();
                var executionId = _this.jobData.executionId;
                _this.OpsManagerJobService.abandonJob(_this.jobData.executionId, { includeSteps: true }, function (response) {
                    _this.updateJob(executionId, response.data);
                    //  loadJobs(true);
                });
            };
            this.failJob = function (event) {
                event.stopPropagation();
                event.preventDefault();
                var executionId = _this.jobData.executionId;
                _this.OpsManagerJobService.failJob(_this.jobData.executionId, { includeSteps: true }, function (response) {
                    _this.updateJob(executionId, response.data);
                    //  loadJobs(true);
                });
            };
            /**
* Indicates that admin operations are allowed.
* @type {boolean}
*/
            this.allowAdmin = false;
            this.pageName = 'jobs';
            //Page State
            this.refreshing = false;
            this.loading = true;
            this.showProgress = true;
            //Track active requests and be able to cancel them if needed
            this.activeJobRequests = [];
            this.jobData = {};
            /**
             * {step1:data,step2:data}
             * @type {{}}
             */
            this.stepData = {};
            /**
             * [{{title:'',data:{}},{title:'',data:{}}...}]
             * @type {{}}
             */
            this.allSteps = [];
            this.jobTab = { title: 'JOB', content: this.jobData };
            //  this.selectedTab = this.jobTabs[0];
            this.tabMetadata = {
                selectedIndex: 0,
                bottom: false
            };
            var UNKNOWN_JOB_EXECUTION_ID = "UNKNOWN";
            this.next = this.nextTab;
            this.previous = this.previousTab;
            //Refresh Intervals
            this.refreshTimeout = null;
            this.jobData = {};
            this.jobExecutionId = null;
            /**
             * Flag indicating the loading of the passed in JobExecutionId was unable to bring back data
             * @type {boolean}
             */
            this.unableToFindJob = false;
            this.showJobParameters = true;
            this.toggleJobParametersVar = this.toggleJobParameters;
            this.relatedJobs = [];
            this.relatedJob = null;
            this.changeRelatedJobVar = this.changeRelatedJob;
            this.navigateToLogsVar = this.navigateToLogs;
            this.navigateToLogsForStepVar = this.navigateToLogsForStep;
            this.logUiEnabledVar = false;
            this.init();
            this.logUiEnabled();
            $scope.$on('$destroy', function () {
                _this.cancelLoadJobDataTimeout();
            });
            // Fetch allowed permissions
            accessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                _this.allowAdmin = accessControlService.hasAction(AccessControlService_1.default.OPERATIONS_ADMIN, actionSet.actions);
            });
        }
        JobDetailsDirectiveController.prototype.triggerSavepointRetry = function () {
            var _this = this;
            //var self = this;
            if (angular.isDefined(this.jobData.triggerRetryFlowfile)) {
                console.log('TRIGGER SAVE replay for ', this.jobData.triggerRetryFlowfile);
                this.jobData.renderTriggerRetry = false;
                this.$http.post(this.OpsManagerRestUrlService.TRIGGER_SAVEPOINT_RETRY(this.jobExecutionId, this.jobData.triggerRetryFlowfile)).then(function () {
                    _this.$mdToast.show(_this.$mdToast.simple()
                        .textContent('Triggered the retry')
                        .hideDelay(3000));
                    _this.loadJobData(true);
                });
            }
        };
        JobDetailsDirectiveController.prototype.triggerSavepointReleaseFailure = function (callbackFn) {
            var _this = this;
            // var self = this;
            if (angular.isDefined(this.jobData.triggerRetryFlowfile)) {
                this.$http.post(this.OpsManagerRestUrlService.TRIGGER_SAVEPOINT_RELEASE(this.jobExecutionId, this.jobData.triggerRetryFlowfile)).then(function (response) {
                    console.log('TRIGGERD FAILURE ', response);
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
        JobDetailsDirectiveController.prototype.enableTabAnimation = function () {
            if (this.enableTabAnimationTimeout) {
                this.$timeout.cancel(this.enableTabAnimationTimeout);
            }
            this.enableTabAnimationTimeout = this.$timeout(function () {
                angular.element('.job-details-tabs').removeClass('no-animation');
            }, 1000);
        };
        return JobDetailsDirectiveController;
    }());
    exports.JobDetailsDirectiveController = JobDetailsDirectiveController;
    angular.module(module_name_1.moduleName)
        .controller("JobDetailsDirectiveController", ["$scope", "$http", "$state", "$interval", "$timeout", "$q",
        "$mdToast", "OpsManagerRestUrlService",
        "OpsManagerJobService", "IconService", "AccessControlService", "AngularModuleExtensionService",
        "$filter", JobDetailsDirectiveController]);
    angular.module(module_name_1.moduleName).directive("tbaJobDetails", [
        function () {
            return {
                restrict: "EA",
                bindToController: {
                    cardTitle: "@",
                    refreshIntervalTime: "@",
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