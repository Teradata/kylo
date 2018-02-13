import * as angular from 'angular';
import {moduleName} from "./module-name";
import * as _ from 'underscore';
import * as moment from "moment";

export class controller implements ng.IComponentController{
       /**
         * Time to query for the jobs
         * @type {number}
         */
        refreshInterval: any = 3000;

        /**
         * A map of the jobKey to job
         * @type {{}}
         */
        jobMap: any = {};

        /**
         * An arry of the Groups along with their respective jobs
         * @type {Array}
         */
        jobGroups: any[] = [];

        /**
         * Map of group name to group objects
         * @type {{}}
         */
        jobsByGroupMap: any = {}

        /**
         * Scheduler status indicating if its up/down/paused
         * @type {{}}
         */
        schedulerDetails: any = {};

        API_URL_BASE: string = '/proxy/v1/scheduler';

        /**
         * Flag to indicate we are fetching the scheduler metadata/details
         * @type {boolean}
         */
        fetchingMetadata: boolean = false;

        /**
         * timeout promise when fetching the jobs
         * @type {null}
         */
        fetchJobsTimeout: any = null;

        /**
         * A map of jobs that are currently running (either from the fetch status or manually triggered.
         * This is used to ensure the icon stays running/scheduled when refreshing job status
         * @type {{}}
         */
        firedJobs: any = {};
        /**
         * Time frame that simulated "RUNNING" status should be displayed for before returning back to "Scheduled" status
         * @type {number}
         */
        runningDisplayInterval: number = 3000;

        /**
         * Flag to indicate this view is being destroyed (i.e. the user navigated away)
         * @type {boolean}
         */
        destroyed: boolean = false;

        /**
         * flag to allow access to the scheduler controls
         * @type {boolean}
         */
        allowAdmin: boolean = false;

      constructor(private $scope: any,
                private $interval: any,
                private $timeout: any,
                private $http: any,
                private $location: any,
                private HttpService: any,
                private Utils: any,
                private AccessControlService: any){
                    this.init();

                    $scope.$on('$destroy', ()=> {
                        if(this.fetchJobsTimeout) {
                            $timeout.cancel(this.fetchJobsTimeout);
                        }
                        this.fetchJobsTimeout = null;
                        this.destroyed = true;
                    });
                }


           /**
         * Fetch the metadata about the scheduler and populate the this.schedulerDetails object
         * @param metadata
         */
        populateSchedulerDetails = function(metadata: any){

            if(metadata.runningSince) {
                this.schedulerDetails['startTime'] = moment(metadata.runningSince).format('MM/DD/YYYY hh:mm:ss a');
                this.schedulerDetails["upTime"] =  this.Utils.dateDifference(metadata.runningSince,new Date().getTime());
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
        refreshAll = function() {
            //force the refresh
            this.editing = false;
            this.editingTriggers = {};
            this.refresh();
        }

        /**
         * Clear the scheduler details
         */
        clearSchedulerDetails = function() {
             this.schedulerDetails = {"startTime":'','jobsExecuted':0,"status":"RUNNING", icon:'check_circle'};
        }

        /**
         * Query for the scheduler details
         */
        fetchSchedulerDetails = function() {
                this.fetchingMetadata = true;
                this.$http.get(this.API_URL_BASE + "/metadata").then( (response: any)=>{
                    var data = response.data;
                    this.clearSchedulerDetails();
                    if (angular.isObject(data)) {
                        this.populateSchedulerDetails(data);
                    }
                    this.fetchingMetadata = false;

                }, ()=> {
                    this.fetchingMetadata = false;
                });
        }

        /**
         * Pause a given job
         * @param job
         */
        pauseJob = function(job: any){
            this.$http.post(this.API_URL_BASE+"/jobs/pause",job.jobIdentifier).then(function (response: any) {
                this.fetchJobs();
            },function (xhr: any,status: any,err: any) {
                console.log("failed to update the trigger  ",xhr,status,err)
            });
        }

        /**
         * Resume a given job
         * @param job
         */
        resumeJob = function(job: any){
            this.$http.post(this.API_URL_BASE+"/jobs/resume",job.jobIdentifier).then(function (response: any) {
                this.fetchJobs();
            },function (xhr: any,status: any,err: any) {
                console.log("failed to update the trigger  ",xhr,status,err)
            });
        }

        /**
         * Trigger the job
         * @param job
         */
        triggerJob = function(job: any){
            this.justFiredJob(job);

            this.$http.post(this.API_URL_BASE+"/jobs/trigger",job.jobIdentifier).then(function (response: any) {
                this.fetchJobs();
            },function (xhr: any,status: any,err: any) {
                console.log("failed to update the trigger  ",xhr,status,err)
            });
        }

        /**
         * Pause the entire scheduler
         */
        pauseScheduler = function(){
            this.$http.post(this.API_URL_BASE+"/pause").then(function (response: any) {
                this.fetchSchedulerDetails();
            },function (xhr: any, status: any, err: any) {
                console.log("failed to standby the scheduler  ", xhr, status, err)
            });
        }

        /**
         * Resume the entire scheduler
         */
        resumeScheduler = function(){
            this.$http.post(this.API_URL_BASE+"/resume").then(function (response: any) {
                this.fetchSchedulerDetails();
            },function (xhr: any,status: any,err: any) {
                console.log("failed to shutdown the scheduler  ",xhr,status,err)
            });
        }

        /**
         * Store data that a job just got fired (i.e. user manually triggered the job)
         * this will keep the job in a "RUNNING" state for the 'runningDisplayInterval'
         * @param job
         */
        justFiredJob= function(job: any){
            this.firedJobs[job.jobName]= new Date();
            var jobName = job.jobName;
            this.$timeout(function() {
                delete this.firedJobs[jobName];
                var currentJob = this.jobMap[jobName];
                if(currentJob != undefined) {
                    //If a Job was just fired keep it in the psuedo running state.
                    //this will be cleaned up in the $timeout below
                    if (this.firedJobs[jobName] != undefined) {
                        currentJob.state = 'RUNNING'
                    }
                    if (currentJob.state != 'RUNNING' && this.schedulerDetails.status == 'PAUSED') {
                        currentJob.state = 'PAUSED';
                    }
                    //add the moment date
                    this.setNextFireTimeString(currentJob);
                    this.applyIcon(currentJob);
                }
            }, this.runningDisplayInterval);
        }

        /**
         * Reset the timeout to query for the jobs again
         */
        assignFetchTimeout = () =>{
            this.$timeout.cancel(this.fetchJobsTimeout);
            this.fetchJobsTimeout = this.$timeout(()=>{this.refresh() },this.refreshInterval);
        }

        /**
         * Depending upon the state of the job, assign an icon
         * @param job
         */
         applyIcon= function(job: any){
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
        jobKey=function(job: any){
            var key = job.jobName+'-'+job.jobGroup;
            return key;
        }

        setNextFireTimeString=function(job: any) {
            if( job.state == 'PAUSED') {
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
                    job.nextFireTimeString = ' Unable to identify'
                }
            }
        }

        /**
         * Query for the jobs
         */
        fetchJobs= function() {

            this.$http.get(this.API_URL_BASE+"/jobs").then((response: any)=> {

                //store a record of the jobs that were processed
                var processedJobGroups = {};

                if(response && response.data){

                    var processedJobs: any[] = []
                    angular.forEach(response.data,(job: any,i: any)=>{
                        var key = this.jobKey(job);
                        var theJob = this.jobMap[key];

                        if(theJob == undefined) {
                            theJob = job;
                            this.jobMap[key] = theJob;
                        }
                        processedJobs.push(key);


                        if(theJob.nextFireTime != job.nextFireTime && this.schedulerDetails.status != 'PAUSED' && theJob.state != 'PAUSED') {
                            //the job just got fired.... simulate the running condition
                            this.justFiredJob(theJob);
                        }
                        var jobName = theJob.jobName;
                        //If a Job was just fired keep it in the psuedo running state.
                        //this will be cleaned up in the $timeout for the firedJob
                        if(this.firedJobs[jobName] != undefined){
                            job.state ='RUNNING'
                        }
                        if(job.state != 'RUNNING' && this.schedulerDetails.status == 'PAUSED'){
                            job.state = 'PAUSED';
                        }
                        //add the moment date
                        this.setNextFireTimeString(job);
                        this.applyIcon(job);
                        //write it back to the theJob
                        angular.extend(theJob,job);

var jobs: any[]=[];
var jobMap: any = {};
                        if(this.jobsByGroupMap[theJob.jobGroup] == undefined) {
                            //add the group if its new
                            var group = {name:theJob.jobGroup,jobs:jobs, jobMap:jobMap}
                            this.jobsByGroupMap[theJob.jobGroup] = group;
                            this.jobGroups.push(group);
                        }
                        var jobMap = this.jobsByGroupMap[theJob.jobGroup].jobMap;
                        if(jobMap[key] == undefined) {
                            //add the job if its new
                            this.jobsByGroupMap[theJob.jobGroup].jobs.push(theJob);
                            this.jobsByGroupMap[theJob.jobGroup].jobMap[key] = theJob;
                        }
                    });
                }

                //reconcile the data back to the ui bound object
                _.each(this.jobMap,(job: any,jobKey: any)=>{
                    if(_.indexOf(processedJobs,jobKey) == -1){
                        //this job has been removed
                        var group = job.jobGroup;
                        if(this.jobsByGroupMap[group] != undefined){
                            var groupJobsArray = this.jobsByGroupMap[group].jobs;
                            var groupJobMap = this.jobsByGroupMap[group].jobMap;
                            var idx = _.indexOf(groupJobsArray,job);
                            if(idx > -1){
                                groupJobsArray.splice(idx,1);
                            }
                            delete groupJobMap[jobKey];
                        }
                        delete this.jobMap[jobKey];
                    }
                });

                if(!this.destroyed) {
                    this.assignFetchTimeout();
                }
            },function () {
                console.log("failed to retrieve the jobs ");
                if(!this.destroyed) {
                    this.assignFetchTimeout();
                }
            });
        };

        init=()=>{

            // Fetch the allowed actions
            this.AccessControlService.getUserAllowedActions()
                .then((actionSet: any)=>{
                    this.allowAdmin = this.AccessControlService.hasAction(this.AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
                });


            this.clearSchedulerDetails();
            this.fetchJobs();
            this.fetchSchedulerDetails();
        }

        refresh=()=>{
            this.fetchSchedulerDetails();
            this.fetchJobs();
        }

    
}



 angular.module(moduleName).controller('SchedulerController', 
                                        ["$scope","$interval","$timeout","$http","$location",
                                        "HttpService","Utils","AccessControlService",controller]);
