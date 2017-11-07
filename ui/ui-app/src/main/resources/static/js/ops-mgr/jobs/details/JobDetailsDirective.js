define(['angular','ops-mgr/jobs/details/module-name'], function (angular,moduleName) {
    var directive = function() {
        return {
            restrict: "EA",
            bindToController: {
                cardTitle: "@",
                refreshIntervalTime: "@",
                executionId:'='
            },
            controllerAs: 'vm',
            scope: {},
            templateUrl: 'js/ops-mgr/jobs/details/job-details-template.html',
            controller: "JobDetailsDirectiveController",
            link: function($scope, element, attrs, controller) {

            }
        };
    }



    function JobDetailsDirectiveController($scope,$http, $state, $interval, $timeout, $q, OpsManagerJobService, IconService, AccessControlService, AngularModuleExtensionService) {
        var self = this;

        /**
         * Indicates that admin operations are allowed.
         * @type {boolean}
         */
        self.allowAdmin = false;

        this.pageName = 'jobs';
        //Page State
        this.refreshing = false;
        this.loading = true;
        this.showProgress = true;

        //Track active requests and be able to cancel them if needed
        this.activeJobRequests = []

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

        this.jobTab = {title: 'JOB', content: self.jobData}

        //  this.selectedTab = this.jobTabs[0];
        this.tabMetadata = {
            selectedIndex: 0,
            bottom: false
        };

        var UNKNOWN_JOB_EXECUTION_ID = "UNKNOWN";

        this.next = nextTab;
        this.previous = previousTab;

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
        this.toggleJobParameters = toggleJobParameters;
        this.relatedJobs = [];
        this.relatedJob = null;
        this.changeRelatedJob = changeRelatedJob;
        this.navigateToLogs = navigateToLogs;
        this.navigateToLogsForStep = navigateToLogsForStep;
        this.logUiEnabled = false;

        this.init = function() {
            var executionId = self.executionId;
            self.jobExecutionId = executionId;
            this.relatedJob = self.jobExecutionId;
            loadJobData();
            //   loadRelatedJobs();
        }

        this.init();

        function nextTab() {
            self.tabMetadata.selectedIndex = Math.min(self.tabMetadata.selectedIndex + 1, 2);
        };
        function previousTab() {
            self.tabMetadata.selectedIndex = Math.max(self.tabMetadata.selectedIndex - 1, 0);
        };

        function logUiEnabled() {
            self.logUiEnabled = AngularModuleExtensionService.stateExists("log-ui");
        }

        logUiEnabled();

        function navigateToLogs(jobStartTime, jobEndTime){
            $state.go("log-ui", {startTime:jobStartTime, endTime:jobEndTime, showCustom:true});
        }

        function navigateToLogsForStep(failedStep){
            var previousStep = '';
            for (var title in self.stepData) {
                var step = self.stepData[title];
                if(failedStep.title == title) {
                    break;
                }
                previousStep = step;
            }
            $state.go("log-ui", {startTime:previousStep.startTime, endTime:failedStep.content.endTime, showCustom:true});
        }

        //Tab Functions


        function toggleJobParameters(name) {
            if (name == 'JobParameters') {
                self.showJobParameters = true;
            }
            else {
                self.showJobParameters = false;
            }
        }

        function selectFirstTab() {
            self.tabMetadata.selectedIndex = 0;
            // self.selectedTab = self.jobTabs[0];
        }

        function cancelLoadJobDataTimeout() {
            if (self.refreshTimeout != null) {
                $timeout.cancel(self.refreshTimeout);
                self.refreshTimeout = null;
            }
        }

        //Load Feeds

        function loadJobData(force) {
            cancelLoadJobDataTimeout();

            if (force || !self.refreshing) {
                self.unableToFindJob = false;
                if (force) {
                    angular.forEach(self.activeJobRequests, function(canceler, i) {
                        canceler.resolve();
                    });
                    self.activeJobRequests = [];
                }

                self.refreshing = true;
                var sortOptions = '';
                var canceler = $q.defer();
                var successFn = function(response) {

                    if (response.data) {
                        //transform the data for UI
                        transformJobData(response.data);
                        if (response.data.running == true || response.data.stopping == true) {
                            cancelLoadJobDataTimeout();
                            self.refreshTimeout = $timeout(function() {
                                loadJobData()
                            }, 1000);
                        }

                        if (self.loading) {
                            self.loading = false;
                        }
                    }
                    else {
                        self.unableToFindJob = true;
                    }

                    finishedRequest(canceler);

                }
                var errorFn = function(err) {
                    finishedRequest(canceler);
                    self.unableToFindJob = true;
                    addJobErrorMessage(err)
                }
                var finallyFn = function() {

                }
                self.activeJobRequests.push(canceler);
                self.deferred = canceler;
                var params = {'includeSteps': true}

                $http.get(OpsManagerJobService.LOAD_JOB_URL(self.jobExecutionId), {timeout: canceler.promise, params: params}).then(successFn, errorFn);
            }

            return self.deferred;

        }

        function finishedRequest(canceler) {
            var index = _.indexOf(self.activeJobRequests, canceler);
            if (index >= 0) {
                self.activeJobRequests.splice(index, 1);
            }
            enableTabAnimation();
            canceler.resolve();
            canceler = null;
            self.refreshing = false;
            self.showProgress = false;
        }

        function loadRelatedJobs(setExecutionId) {
            var successFn = function(response) {
                if (response.data) {
                    self.relatedJobs = response.data;
                    if (setExecutionId) {
                        self.relatedJob = setExecutionId;
                    }
                    updateRelatedJobIndex();
                }
            }
            var errorFn = function(err) {
            }

            //todo uncomment once related job are linked and working
            // $http.get(OpsManagerJobService.RELATED_JOBS_URL(self.jobExecutionId)).then(successFn, errorFn);
        }

        function updateRelatedJobIndex() {
            if (self.relatedJob) {
                angular.forEach(self.relatedJobs, function(job, i) {
                    if (job.jobExecutionId == self.relatedJob) {
                        self.relatedJobIndex = i;
                    }
                })
            }
        }

        function disableTabAnimation() {
            angular.element('.job-details-tabs').addClass('no-animation');
        }

        function enableTabAnimation() {
            if (self.enableTabAnimationTimeout) {
                $timeout.cancel(self.enableTabAnimationTimeout);
            }
            self.enableTabAnimationTimeout = $timeout(function() {
                angular.element('.job-details-tabs').removeClass('no-animation');
            }, 1000);

        }

        function changeRelatedJob(relatedJob) {
            //remove animation for load
            disableTabAnimation();
            loadJobExecution(relatedJob)
        }

        function mapToArray(map, obj, fieldName, removeKeys) {
            if (removeKeys == undefined) {
                removeKeys = [];
            }
            var arr = [];
            for (var key in map) {
                if (_.indexOf(removeKeys, key) == -1) {
                    if (map.hasOwnProperty(key)) {
                        arr.push({key: key, value: map[key]});
                    }
                }
            }
            obj[fieldName] = arr;
        }

        function assignParameterArray(obj) {
            if (obj) {
                if (obj.jobParameters) {

                    mapToArray(obj.jobParameters, obj, 'jobParametersArray')
                }
                else {
                    obj['jobParametersArray'] = [];
                }

                if (obj.executionContext) {
                    mapToArray(obj.executionContext, obj, 'executionContextArray', ['batch.stepType', 'batch.taskletType'])
                }
                else {
                    obj['executionContextArray'] = [];
                }

            }

        }

        function cssClassForDisplayStatus(displayStatus) {
            var cssStatus = {
                'success': ['COMPLETED', 'STARTING', 'STARTED', 'EXECUTING'],
                'error': ['FAILED'],
                'warn': ['STOPPING', 'STOPPED', 'WARNING'],
                'abandoned': ['ABANDONED'],
                'unknown': ['UNKNOWN']
            };
            var statusCssMap = {};
            _.each(cssStatus, function (arr, key) {
                _.each(arr, function (status, i) {
                    statusCssMap[status] = key;
                });
            });
            return statusCssMap[displayStatus];

        }

        function transformJobData(job) {
            assignParameterArray(job);
            job.name = job.jobName;
            job.running = false;
            job.stopping = false;
            job.exitDescription = job.exitStatus;
            if (job.exitDescription == undefined || job.exitDescription == '') {
                job.exitDescription = 'No description available.'
            }
            job.tabIcon = undefined;

            var iconStyle = IconService.iconStyleForJobStatus(job.displayStatus);
            var icon = IconService.iconForJobStatus(job.displayStatus);
            job.cssStatusClass = cssClassForDisplayStatus(job.displayStatus);

            if (job.status == "STARTED") {
                job.running = true;
            }
            if (job.status == 'STOPPING') {
                job.stopping = true;
            }
            job.statusIcon = icon;
            job.tabIconStyle = iconStyle;

            angular.extend(self.jobData, job);


            if (job.executedSteps) {
                job.executedSteps = _.chain(job.executedSteps).sortBy('startTime').sortBy('nifiEventId').value();

                angular.forEach(job.executedSteps, function(step, i) {
                    var stepName = "Step " + (i + 1);
                    if (self.stepData[stepName] == undefined) {
                        self.stepData[stepName] = {};
                        self.allSteps.push({title: stepName, content: self.stepData[stepName]})
                    }
                    angular.extend(self.stepData[stepName], transformStep(step));

                });
            }
        }

        function transformStep(step) {
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
                step.exitDescription = 'No description available.'
            }

            var style = IconService.iconStyleForJobStatus(step.displayStatus);
            var icon = IconService.iconForJobStatus(step.displayStatus);
            step.cssStatusClass = cssClassForDisplayStatus(step.displayStatus);
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

            assignParameterArray(step);
            return step;
        }

        function clearRefreshInterval() {
            if (self.refreshInterval != null) {
                $interval.cancel(self.refreshInterval);
                self.refreshInterval = null;
            }
        }

        function setRefreshInterval() {
            self.clearRefreshInterval();
            if (self.refreshIntervalTime) {
                self.refreshInterval = $interval(loadJobs, self.refreshIntervalTime);

            }
        }

        //Util Functions
        function capitalize(string) {
            return string.charAt(0).toUpperCase() + string.substring(1).toLowerCase();
        }

        function updateJob(executionId, job) {
            clearErrorMessage();
            var existingJob = self.jobData;
            if (existingJob && executionId == job.executionId) {
                transformJobData(job);
            }
            else {
                disableTabAnimation();
                loadJobExecution(job.executionId);

            }
        }

        function loadJobExecution(executionId) {
            self.jobExecutionId = executionId;

            //reset steps
            var len = self.allSteps.length;
            while (len > 1) {
                self.allSteps.splice(len - 1, 1);
                len = self.allSteps.length;
            }
            //clear out all the steps
            angular.forEach(Object.keys(self.stepData), function (key, i) {
                delete self.stepData[key];
            });

            loadJobData(true);
            //  loadRelatedJobs(executionId);
        }

        function addJobErrorMessage(errMsg) {
            var existingJob = self.jobData;
            if (existingJob) {
                existingJob.errorMessage = errMsg;
            }
        }

        function clearErrorMessage() {
            var existingJob = self.jobData;
            if (existingJob) {
                existingJob.errorMessage = '';
            }
        }

        this.restartJob = function(event) {
            event.stopPropagation();
            event.preventDefault();
            var executionId = self.jobData.executionId;
            var xhr = OpsManagerJobService.restartJob(self.jobData.executionId, {includeSteps: true}, function(response) {
                        updateJob(executionId, response.data);
                        //  loadJobs(true);
                    }, function(errMsg) {
                        addJobErrorMessage(executionId, errMsg);
                    }
            );
        };

        this.stopJob = function(event) {
            event.stopPropagation();
            event.preventDefault();
            var executionId = self.jobData.executionId;
            OpsManagerJobService.stopJob(self.jobData.executionId, {includeSteps: true}, function(response) {
                updateJob(executionId, response.data)
                //  loadJobs(true);
            })
        };

        this.abandonJob = function(event) {
            event.stopPropagation();
            event.preventDefault();
            var executionId = self.jobData.executionId;
            OpsManagerJobService.abandonJob(self.jobData.executionId, {includeSteps: true}, function(response) {
                updateJob(executionId, response.data)
                //  loadJobs(true);
            })
        };

        this.failJob = function(event) {
            event.stopPropagation();
            event.preventDefault();
            var executionId = self.jobData.executionId;
            OpsManagerJobService.failJob(self.jobData.executionId, {includeSteps: true}, function(response) {
                updateJob(executionId, response.data)
                //  loadJobs(true);
            })
        };

        $scope.$on('$destroy', function(){
            cancelLoadJobDataTimeout();
        })



        // Fetch allowed permissions
        AccessControlService.getUserAllowedActions()
                .then(function(actionSet) {
                    self.allowAdmin = AccessControlService.hasAction(AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
                });
    }

    angular.module(moduleName).controller("JobDetailsDirectiveController", ["$scope","$http", "$state", "$interval","$timeout","$q","OpsManagerJobService","IconService","AccessControlService", "AngularModuleExtensionService",JobDetailsDirectiveController]);
    angular.module(moduleName).directive("tbaJobDetails", directive);
});
