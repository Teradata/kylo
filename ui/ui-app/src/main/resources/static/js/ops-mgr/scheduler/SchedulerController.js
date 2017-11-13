define(['angular','ops-mgr/scheduler/module-name'], function (angular,moduleName) {

    var controller = function ($scope, $interval, $timeout, $http, $location, HttpService, Utils,AccessControlService) {
        var self = this;

        /**
         * Time to query for the jobs
         * @type {number}
         */
        var refreshInterval = 3000;

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
        this.jobsByGroupMap = {}

        /**
         * Scheduler status indicating if its up/down/paused
         * @type {{}}
         */
        this.schedulerDetails = {};

        var API_URL_BASE = '/proxy/v1/scheduler';

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
        var firedJobs = {};
        /**
         * Time frame that simulated "RUNNING" status should be displayed for before returning back to "Scheduled" status
         * @type {number}
         */
        var runningDisplayInterval = 3000;

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

        /**
         * Fetch the metadata about the scheduler and populate the self.schedulerDetails object
         * @param metadata
         */
        this.populateSchedulerDetails = function(metadata){

            if(metadata.runningSince) {
                this.schedulerDetails['startTime'] = moment(metadata.runningSince).format('MM/DD/YYYY hh:mm:ss a');
                this.schedulerDetails["upTime"] =  Utils.dateDifference(metadata.runningSince,new Date().getTime());
            }
            else {
                this.schedulerDetails['startTime'] = "N/A";
                this.schedulerDetails['upTime'] = "N/A";
            }
            this.schedulerDetails["jobsExecuted"] = metadata.numberOfJobsExecuted;
            var status = 'UNKNOWN';
            var icon = '';
            if(metadata.shutdown){
                status = 'STOPPED';
                icon = 'stop';
            }
            else if(metadata.inStandbyMode){
                status = 'PAUSED';
                icon = 'pause_circle_outline';
            }
            else if(metadata.started){
                status = 'RUNNING';
                icon = 'check_circle';
            }
            this.schedulerDetails["status"] = status;
            this.schedulerDetails['statusIcon'] = icon;
        }

        /**
         * Refresh the page
         */
        this.refreshAll = function() {
            //force the refresh
            self.editing = false;
            self.editingTriggers = {};
            self.refresh();
        }

        /**
         * Clear the scheduler details
         */
        this.clearSchedulerDetails = function() {
             this.schedulerDetails = {"startTime":'','jobsExecuted':0,"status":"RUNNING", icon:'check_circle'};
        }

        /**
         * Query for the scheduler details
         */
        this.fetchSchedulerDetails = function() {
                this.fetchingMetadata = true;
                $http.get(API_URL_BASE + "/metadata").then(function (response) {
                    var data = response.data;
                    self.clearSchedulerDetails();
                    if (angular.isObject(data)) {
                        self.populateSchedulerDetails(data);
                    }
                    this.fetchingMetadata = false;

                },function () {
                    this.fetchingMetadata = false;
                });
        }

        /**
         * Pause a given job
         * @param job
         */
        this.pauseJob = function(job){
            $http.post(API_URL_BASE+"/jobs/pause",job.jobIdentifier).then(function (response) {
                self.fetchJobs();
            },function (xhr,status,err) {
                console.log("failed to update the trigger  ",xhr,status,err)
            });
        }

        /**
         * Resume a given job
         * @param job
         */
        this.resumeJob = function(job){
            $http.post(API_URL_BASE+"/jobs/resume",job.jobIdentifier).then(function (response) {
                self.fetchJobs();
            },function (xhr,status,err) {
                console.log("failed to update the trigger  ",xhr,status,err)
            });
        }

        /**
         * Trigger the job
         * @param job
         */
        this.triggerJob = function(job){
            justFiredJob(job);

            $http.post(API_URL_BASE+"/jobs/trigger",job.jobIdentifier).then(function (response) {
                self.fetchJobs();
            },function (xhr,status,err) {
                console.log("failed to update the trigger  ",xhr,status,err)
            });
        }

        /**
         * Pause the entire scheduler
         */
        this.pauseScheduler = function(){
            $http.post(API_URL_BASE+"/pause").then(function (response) {
                self.fetchSchedulerDetails();
            },function (xhr, status, err) {
                console.log("failed to standby the scheduler  ", xhr, status, err)
            });
        }

        /**
         * Resume the entire scheduler
         */
        this.resumeScheduler = function(){
            $http.post(API_URL_BASE+"/resume").then(function (response) {
                self.fetchSchedulerDetails();
            },function (xhr,status,err) {
                console.log("failed to shutdown the scheduler  ",xhr,status,err)
            });
        }

        /**
         * Store data that a job just got fired (i.e. user manually triggered the job)
         * this will keep the job in a "RUNNING" state for the 'runningDisplayInterval'
         * @param job
         */
        function justFiredJob(job){
            firedJobs[job.jobName]= new Date();
            var jobName = job.jobName;
            $timeout(function() {
                delete firedJobs[jobName];
                var currentJob = self.jobMap[jobName];
                if(currentJob != undefined) {
                    //If a Job was just fired keep it in the psuedo running state.
                    //this will be cleaned up in the $timeout below
                    if (firedJobs[jobName] != undefined) {
                        currentJob.state = 'RUNNING'
                    }
                    if (currentJob.state != 'RUNNING' && self.schedulerDetails.status == 'PAUSED') {
                        currentJob.state = 'PAUSED';
                    }
                    //add the moment date
                    setNextFireTimeString(currentJob);
                    applyIcon(currentJob);
                }
            }, runningDisplayInterval);
        }

        /**
         * Reset the timeout to query for the jobs again
         */
        this.assignFetchTimeout = function() {
            $timeout.cancel(self.fetchJobsTimeout);
            self.fetchJobsTimeout = $timeout(function(){self.refresh() },refreshInterval);
        }

        /**
         * Depending upon the state of the job, assign an icon
         * @param job
         */
        function applyIcon(job){
            if(job.state =='RUNNING') {
                job.stateIcon = 'directions_run';
            }
            else if(job.state =='SCHEDULED') {
                job.stateIcon = 'timer';
            }
            else if(job.state =='PAUSED') {
                job.stateIcon = 'pause_circle_outline';
            }
            else if(job.state =='UNKNOWN') {
                job.stateIcon = 'error';
            }
        }

        /**
         * Return a unique key for the job
         * @param job
         * @return {string}
         */
        function jobKey(job){
            var key = job.jobName+'-'+job.jobGroup;
            return key;
        }

        function setNextFireTimeString(job) {
            if( job.state == 'PAUSED') {
                job.nextFireTimeString = ' - ';
            }
            else {
                if (job.nextFireTime != null && job.nextFireTime != undefined) {

                var timeFromNow = Utils.dateDifferenceMs(new Date().getTime(), job.nextFireTime);
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
                    job.nextFireTimeString = ' Unable to identify'
                }
            }
        }

        /**
         * Query for the jobs
         */
        this.fetchJobs = function () {

            $http.get(API_URL_BASE+"/jobs").then(function (response) {

                //store a record of the jobs that were processed
                var processedJobGroups = {};

                if(response && response.data){

                    var processedJobs = []
                    angular.forEach(response.data,function(job,i){
                        var key = jobKey(job);
                        var theJob = self.jobMap[key];

                        if(theJob == undefined) {
                            theJob = job;
                            self.jobMap[key] = theJob;
                        }
                        processedJobs.push(key);


                        if(theJob.nextFireTime != job.nextFireTime && self.schedulerDetails.status != 'PAUSED' && theJob.state != 'PAUSED') {
                            //the job just got fired.... simulate the running condition
                            justFiredJob(theJob);
                        }
                        var jobName = theJob.jobName;
                        //If a Job was just fired keep it in the psuedo running state.
                        //this will be cleaned up in the $timeout for the firedJob
                        if(firedJobs[jobName] != undefined){
                            job.state ='RUNNING'
                        }
                        if(job.state != 'RUNNING' && self.schedulerDetails.status == 'PAUSED'){
                            job.state = 'PAUSED';
                        }
                        //add the moment date
                        setNextFireTimeString(job);
                        applyIcon(job);
                        //write it back to the theJob
                        angular.extend(theJob,job);


                        if(self.jobsByGroupMap[theJob.jobGroup] == undefined) {
                            //add the group if its new
                            var group = {name:theJob.jobGroup,jobs:[], jobMap:{}}
                            self.jobsByGroupMap[theJob.jobGroup] = group;
                            self.jobGroups.push(group);
                        }
                        var jobMap = self.jobsByGroupMap[theJob.jobGroup].jobMap;
                        if(jobMap[key] == undefined) {
                            //add the job if its new
                            self.jobsByGroupMap[theJob.jobGroup].jobs.push(theJob);
                            self.jobsByGroupMap[theJob.jobGroup].jobMap[key] = theJob;
                        }
                    });
                }

                //reconcile the data back to the ui bound object
                _.each(self.jobMap,function(job,jobKey){
                    if(_.indexOf(processedJobs,jobKey) == -1){
                        //this job has been removed
                        var group = job.jobGroup;
                        if(self.jobsByGroupMap[group] != undefined){
                            var groupJobsArray = self.jobsByGroupMap[group].jobs;
                            var groupJobMap = self.jobsByGroupMap[group].jobMap;
                            var idx = _.indexOf(groupJobsArray,job);
                            if(idx > -1){
                                groupJobsArray.splice(idx,1);
                            }
                            delete groupJobMap[jobKey];
                        }
                        delete self.jobMap[jobKey];
                    }
                });

                if(!self.destroyed) {
                    self.assignFetchTimeout();
                }
            },function () {
                console.log("failed to retrieve the jobs ");
                if(!self.destroyed) {
                    self.assignFetchTimeout();
                }
            });
        };




        this.init = function () {

            // Fetch the allowed actions
            AccessControlService.getUserAllowedActions()
                .then(function (actionSet) {
                    self.allowAdmin = AccessControlService.hasAction(AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
                });


            this.clearSchedulerDetails();
            this.fetchJobs();
            this.fetchSchedulerDetails();
        }

        this.refresh = function(){
            this.fetchSchedulerDetails();
            this.fetchJobs();
        }



        this.init();


        $scope.$on('$destroy', function () {
            if(self.fetchJobsTimeout) {
                $timeout.cancel(self.fetchJobsTimeout);
            }
            self.fetchJobsTimeout = null;
            self.destroyed = true;
        });
    };

    angular.module(moduleName).controller('SchedulerController', ["$scope","$interval","$timeout","$http","$location","HttpService","Utils","AccessControlService",controller]);


});
