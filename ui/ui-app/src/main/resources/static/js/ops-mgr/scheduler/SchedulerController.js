define(["require", "exports", "angular", "./module-name", "underscore", "moment"], function (require, exports, angular, module_name_1, _, moment) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var controller = /** @class */ (function () {
        function controller($scope, $interval, $timeout, $http, $location, HttpService, Utils, AccessControlService) {
            this.$scope = $scope;
            this.$interval = $interval;
            this.$timeout = $timeout;
            this.$http = $http;
            this.$location = $location;
            this.HttpService = HttpService;
            this.Utils = Utils;
            this.AccessControlService = AccessControlService;
            /**
             * Time to query for the jobs
             * @type {number}
             */
            this.refreshInterval = 3000;
            /**
             * A map of the jobKey to job
             * @type {{}}
             */
            this.jobMap = {};
            /**
             * An arry of the Groups along with their respective jobs
             * @type {Array}
             */
            this.jobGroups = [];
            /**
             * Map of group name to group objects
             * @type {{}}
             */
            this.jobsByGroupMap = {};
            /**
             * Scheduler status indicating if its up/down/paused
             * @type {{}}
             */
            this.schedulerDetails = {};
            this.API_URL_BASE = '/proxy/v1/scheduler';
            /**
             * Flag to indicate we are fetching the scheduler metadata/details
             * @type {boolean}
             */
            this.fetchingMetadata = false;
            /**
             * timeout promise when fetching the jobs
             * @type {null}
             */
            this.fetchJobsTimeout = null;
            /**
             * A map of jobs that are currently running (either from the fetch status or manually triggered.
             * This is used to ensure the icon stays running/scheduled when refreshing job status
             * @type {{}}
             */
            this.firedJobs = {};
            /**
             * Time frame that simulated "RUNNING" status should be displayed for before returning back to "Scheduled" status
             * @type {number}
             */
            this.runningDisplayInterval = 3000;
            /**
             * Flag to indicate this view is being destroyed (i.e. the user navigated away)
             * @type {boolean}
             */
            this.destroyed = false;
            /**
             * flag to allow access to the scheduler controls
             * @type {boolean}
             */
            this.allowAdmin = false;
        }
        controller.prototype.$onInit = function () {
            this.ngOnInit();
        };
        controller.prototype.$onDestroy = function () {
            this.ngOnDestroy();
        };
        controller.prototype.ngOnDestroy = function () {
            if (this.fetchJobsTimeout) {
                this.$timeout.cancel(this.fetchJobsTimeout);
            }
            this.fetchJobsTimeout = null;
            this.destroyed = true;
        };
        controller.prototype.ngOnInit = function () {
            var _this = this;
            // Fetch the allowed actions
            this.AccessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                _this.allowAdmin = _this.AccessControlService.hasAction(_this.AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
            });
            this.clearSchedulerDetails();
            this.fetchJobs();
            this.fetchSchedulerDetails();
        };
        /**
         * Pause a given job
         * @param job
         */
        controller.prototype.pauseJob = function (job) {
            var _this = this;
            this.$http.post(this.API_URL_BASE + "/jobs/pause", job.jobIdentifier).then(function (response) {
                _this.fetchJobs();
            }, function (reason) {
                console.log("failed to update the trigger  ", reason);
            });
        };
        /**
         * Resume a given job
         * @param job
         */
        controller.prototype.resumeJob = function (job) {
            var _this = this;
            this.$http.post(this.API_URL_BASE + "/jobs/resume", job.jobIdentifier).then(function (response) {
                _this.fetchJobs();
            }, function (reason) {
                console.log("failed to update the trigger  ", reason);
            });
        };
        /**
         * Trigger the job
         * @param job
         */
        controller.prototype.triggerJob = function (job) {
            var _this = this;
            this.justFiredJob(job);
            this.$http.post(this.API_URL_BASE + "/jobs/trigger", job.jobIdentifier).then(function (response) {
                _this.fetchJobs();
            }, function (reason) {
                console.log("failed to update the trigger  ", reason);
            });
        };
        /**
         * Pause the entire scheduler
         */
        controller.prototype.pauseScheduler = function () {
            var _this = this;
            this.$http.post(this.API_URL_BASE + "/pause").then(function (response) {
                _this.fetchSchedulerDetails();
            }, function (reason) {
                console.log("failed to standby the scheduler  ", reason);
            });
        };
        /**
         * Resume the entire scheduler
         */
        controller.prototype.resumeScheduler = function () {
            var _this = this;
            this.$http.post(this.API_URL_BASE + "/resume").then(function (response) {
                _this.fetchSchedulerDetails();
            }, function (reason) {
                console.log("failed to shutdown the scheduler  ", reason);
            });
        };
        /**
         * Fetch the metadata about the scheduler and populate the this.schedulerDetails object
         * @param metadata
         */
        controller.prototype.populateSchedulerDetails = function (metadata) {
            if (metadata.runningSince) {
                this.schedulerDetails['startTime'] = moment(metadata.runningSince).format('MM/DD/YYYY hh:mm:ss a');
                this.schedulerDetails["upTime"] = this.Utils.dateDifference(metadata.runningSince, new Date().getTime());
            }
            else {
                this.schedulerDetails['startTime'] = "N/A";
                this.schedulerDetails['upTime'] = "N/A";
            }
            this.schedulerDetails["jobsExecuted"] = metadata.numberOfJobsExecuted;
            var status = 'UNKNOWN';
            var icon = '';
            if (metadata.shutdown) {
                status = 'STOPPED';
                icon = 'stop';
            }
            else if (metadata.inStandbyMode) {
                status = 'PAUSED';
                icon = 'pause_circle_outline';
            }
            else if (metadata.started) {
                status = 'RUNNING';
                icon = 'check_circle';
            }
            this.schedulerDetails["status"] = status;
            this.schedulerDetails['statusIcon'] = icon;
        };
        /**
         * Clear the scheduler details
         */
        controller.prototype.clearSchedulerDetails = function () {
            this.schedulerDetails = { "startTime": '', 'jobsExecuted': 0, "status": "RUNNING", icon: 'check_circle' };
        };
        /**
         * Query for the scheduler details
         */
        controller.prototype.fetchSchedulerDetails = function () {
            var _this = this;
            this.fetchingMetadata = true;
            this.$http.get(this.API_URL_BASE + "/metadata").then(function (response) {
                var data = response.data;
                _this.clearSchedulerDetails();
                if (angular.isObject(data)) {
                    _this.populateSchedulerDetails(data);
                }
                _this.fetchingMetadata = false;
            }, function () {
                _this.fetchingMetadata = false;
            });
        };
        /**
         * Store data that a job just got fired (i.e. user manually triggered the job)
         * this will keep the job in a "RUNNING" state for the 'runningDisplayInterval'
         * @param job
         */
        controller.prototype.justFiredJob = function (job) {
            var _this = this;
            this.firedJobs[job.jobName] = new Date();
            var jobName = job.jobName;
            this.$timeout(function () {
                delete _this.firedJobs[jobName];
                var currentJob = _this.jobMap[jobName];
                if (currentJob != undefined) {
                    //If a Job was just fired keep it in the psuedo running state.
                    //this will be cleaned up in the $timeout below
                    if (_this.firedJobs[jobName] != undefined) {
                        currentJob.state = 'RUNNING';
                    }
                    if (currentJob.state != 'RUNNING' && _this.schedulerDetails.status == 'PAUSED') {
                        currentJob.state = 'PAUSED';
                    }
                    //add the moment date
                    _this.setNextFireTimeString(currentJob);
                    _this.applyIcon(currentJob);
                }
            }, this.runningDisplayInterval);
        };
        /**
         * Reset the timeout to query for the jobs again
         */
        controller.prototype.assignFetchTimeout = function () {
            var _this = this;
            this.$timeout.cancel(this.fetchJobsTimeout);
            this.fetchJobsTimeout = this.$timeout(function () {
                _this.refresh();
            }, this.refreshInterval);
        };
        /**
         * Depending upon the state of the job, assign an icon
         * @param job
         */
        controller.prototype.applyIcon = function (job) {
            if (job.state == 'RUNNING') {
                job.stateIcon = 'directions_run';
            }
            else if (job.state == 'SCHEDULED') {
                job.stateIcon = 'timer';
            }
            else if (job.state == 'PAUSED') {
                job.stateIcon = 'pause_circle_outline';
            }
            else if (job.state == 'UNKNOWN') {
                job.stateIcon = 'error';
            }
        };
        /**
         * Return a unique key for the job
         * @param job
         * @return {string}
         */
        controller.prototype.jobKey = function (job) {
            var key = job.jobName + '-' + job.jobGroup;
            return key;
        };
        controller.prototype.setNextFireTimeString = function (job) {
            if (job.state == 'PAUSED') {
                job.nextFireTimeString = ' - ';
            }
            else {
                if (job.nextFireTime != null && job.nextFireTime != undefined) {
                    var timeFromNow = this.Utils.dateDifferenceMs(new Date().getTime(), job.nextFireTime);
                    if (timeFromNow < 45000) {
                        if (timeFromNow < 15000) {
                            job.nextFireTimeString = "in a few seconds";
                        }
                        else if (timeFromNow < 30000) {
                            job.nextFireTimeString = "in 30 seconds";
                        }
                        else if (timeFromNow < 45000) {
                            job.nextFireTimeString = "in 45 seconds";
                        }
                    }
                    else {
                        job.nextFireTimeString = moment(job.nextFireTime).fromNow();
                    }
                }
                else {
                    job.nextFireTimeString = ' Unable to identify';
                }
            }
        };
        /**
         * Query for the jobs
         */
        controller.prototype.fetchJobs = function () {
            var _this = this;
            this.$http.get(this.API_URL_BASE + "/jobs").then(function (response) {
                //store a record of the jobs that were processed
                var processedJobGroups = {};
                if (response && response.data) {
                    var processedJobs = [];
                    angular.forEach(response.data, function (job, i) {
                        var key = _this.jobKey(job);
                        var theJob = _this.jobMap[key];
                        if (theJob == undefined) {
                            theJob = job;
                            _this.jobMap[key] = theJob;
                        }
                        processedJobs.push(key);
                        if (theJob.nextFireTime != job.nextFireTime && _this.schedulerDetails.status != 'PAUSED' && theJob.state != 'PAUSED') {
                            //the job just got fired.... simulate the running condition
                            _this.justFiredJob(theJob);
                        }
                        var jobName = theJob.jobName;
                        //If a Job was just fired keep it in the psuedo running state.
                        //this will be cleaned up in the $timeout for the firedJob
                        if (_this.firedJobs[jobName] != undefined) {
                            job.state = 'RUNNING';
                        }
                        if (job.state != 'RUNNING' && _this.schedulerDetails.status == 'PAUSED') {
                            job.state = 'PAUSED';
                        }
                        //add the moment date
                        _this.setNextFireTimeString(job);
                        _this.applyIcon(job);
                        //write it back to the theJob
                        angular.extend(theJob, job);
                        var jobs = [];
                        var jobMap = {};
                        if (_this.jobsByGroupMap[theJob.jobGroup] == undefined) {
                            //add the group if its new
                            var group = { name: theJob.jobGroup, jobs: jobs, jobMap: jobMap };
                            _this.jobsByGroupMap[theJob.jobGroup] = group;
                            _this.jobGroups.push(group);
                        }
                        var jobMap = _this.jobsByGroupMap[theJob.jobGroup].jobMap;
                        if (jobMap[key] == undefined) {
                            //add the job if its new
                            _this.jobsByGroupMap[theJob.jobGroup].jobs.push(theJob);
                            _this.jobsByGroupMap[theJob.jobGroup].jobMap[key] = theJob;
                        }
                    });
                }
                //reconcile the data back to the ui bound object
                _.each(_this.jobMap, function (job, jobKey) {
                    if (_.indexOf(processedJobs, jobKey) == -1) {
                        //this job has been removed
                        var group = job.jobGroup;
                        if (_this.jobsByGroupMap[group] != undefined) {
                            var groupJobsArray = _this.jobsByGroupMap[group].jobs;
                            var groupJobMap = _this.jobsByGroupMap[group].jobMap;
                            var idx = _.indexOf(groupJobsArray, job);
                            if (idx > -1) {
                                groupJobsArray.splice(idx, 1);
                            }
                            delete groupJobMap[jobKey];
                        }
                        delete _this.jobMap[jobKey];
                    }
                });
                if (!_this.destroyed) {
                    _this.assignFetchTimeout();
                }
            }, function () {
                console.log("failed to retrieve the jobs ");
                if (!_this.destroyed) {
                    _this.assignFetchTimeout();
                }
            });
        };
        ;
        controller.prototype.refresh = function () {
            this.fetchSchedulerDetails();
            this.fetchJobs();
        };
        return controller;
    }());
    exports.controller = controller;
    angular.module(module_name_1.moduleName).controller('SchedulerController', ["$scope", "$interval", "$timeout", "$http", "$location",
        "HttpService", "Utils", "AccessControlService", controller]);
});
//# sourceMappingURL=SchedulerController.js.map