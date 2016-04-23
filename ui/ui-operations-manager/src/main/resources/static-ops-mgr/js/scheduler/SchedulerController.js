(function () {

    var controller = function ($scope, $interval, $timeout, $http, $location, HttpService, Utils) {
        var self = this;
        this.refreshIntervalTime = 5000;
        this.refreshedDate;
        this.jobs = [];
        this.jobMap = {};
        this.allMetadata = {};
        this.schedulerDetails = {};
        var API_URL_BASE = '/api/v1/scheduler';

        this.fetchingMetadata = false;
        this.fetchingJobs = false;

        this.fetchJobsTimeout = null;

        var firedJobs = {};
        var runningDisplayInterval = 3000;
        this.destroyed = false;

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
        this.refreshAll = function() {
            //force the refresh
            self.editing = false;
            self.editingTriggers = {};
            self.refresh();
        }

        this.clearSchedulerDetails = function() {
             this.schedulerDetails = {"startTime":'','jobsExecuted':0,"status":"RUNNING", icon:'check_circle'};
        }

        this.fetchSchedulerDetails = function() {
         //   if(!this.fetchingMetadata) {
                this.fetchingMetadata = true;
                $http.get(API_URL_BASE + "/metadata").success(function (data) {
                    self.clearSchedulerDetails();
                    if (angular.isObject(data)) {
                        self.populateSchedulerDetails(data);
                    }
                    this.fetchingMetadata = false;

                }).error(function () {
                    this.fetchingMetadata = false;
                    // console.log("failed to retrieve the jobs ")
                });
          //  }
        }


        this.pauseJob = function(job){
            $http.post(API_URL_BASE+"/jobs/pause",job.jobIdentifier).success(function (data) {
                self.fetchJobs();
            }).error(function (xhr,status,err) {
                console.log("failed to update the trigger  ",xhr,status,err)
            });
        }

        this.resumeJob = function(job){
            $http.post(API_URL_BASE+"/jobs/resume",job.jobIdentifier).success(function (data) {
                self.fetchJobs();
            }).error(function (xhr,status,err) {
                console.log("failed to update the trigger  ",xhr,status,err)
            });
        }



function justFiredJob(job){
    firedJobs[job.jobName]= new Date();
    var jobName = job.jobName;
    $timeout(function() {delete firedJobs[jobName];}, runningDisplayInterval);
}

        this.triggerJob = function(job){
          justFiredJob(job);

            $http.post(API_URL_BASE+"/jobs/trigger",job.jobIdentifier).success(function (data) {
                self.fetchJobs();
            }).error(function (xhr,status,err) {
                console.log("failed to update the trigger  ",xhr,status,err)
            });
        }


        this.pauseScheduler = function(){
          //  var proceed = confirm("Are you sure you want to pause the scheduler?");
          //  if(proceed) {
                $http.post(API_URL_BASE+"/pause").success(function (data) {
                    self.fetchSchedulerDetails();
                }).error(function (xhr, status, err) {
                    console.log("failed to standby the scheduler  ", xhr, status, err)
                });
          //  }
        }

        this.resumeScheduler = function(){
            $http.post(API_URL_BASE+"/resume").success(function (data) {
                self.fetchSchedulerDetails();
            }).error(function (xhr,status,err) {
                console.log("failed to shutdown the scheduler  ",xhr,status,err)
            });
        }

        this.completedRefresh = function(){
            this.refreshedDate = new Date();
        }

        this.assignFetchTimeout = function() {
            $timeout.cancel(self.fetchJobsTimeout);
            self.fetchJobsTimeout = $timeout(function(){self.refresh() },1000);
        }

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

        function jobKey(job){
            var key = job.jobName+'-'+job.jobGroup;
            return key;
        }

        function setNextFireTimeString(job) {
            if( job.state == 'PAUSED') {
                job.nextFireTimeString = ' - ';
            }
            else {
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
        }

        this.fetchJobs = function () {

            $http.get(API_URL_BASE+"/jobs").success(function (data) {
                if(data){
                    var jobArray = [];
                    angular.forEach(data,function(job,i){
                        var key = jobKey(job);
                        var theJob = self.jobMap[key];
                        if(theJob == undefined) {
                            theJob = job;
                            self.jobMap[key] = job;
                        }
                        if(theJob.nextFireTime != job.nextFireTime && self.schedulerDetails.status != 'PAUSED' && theJob.state != 'PAUSED') {
                            //the job just got fired.... simulate teh running condition
                            justFiredJob(theJob);
                        }
                        angular.extend(theJob,job);
                        var jobName = theJob.jobName;
                        //If a Job was just fired keep it in the psuedo running state.
                        //this will be cleaned up in the $timeout below
                        if(firedJobs[jobName] != undefined){
                            theJob.state ='RUNNING'
                        }
                        if(theJob.state != 'RUNNING' && self.schedulerDetails.status == 'PAUSED'){
                            theJob.state = 'PAUSED';
                        }
                        //add the moment date
                        setNextFireTimeString(theJob);
                       // theJob.nextFireTimeString = moment(job.nextFireTime).fromNow();
                        applyIcon(theJob);
                        jobArray.push(theJob);
                    });
                }
                self.jobs = jobArray;
                self.completedRefresh();
                if(!self.destroyed) {
                    self.assignFetchTimeout();
                }
            }).error(function () {
                console.log("failed to retrieve the jobs ");
                if(!self.destroyed) {
                    self.assignFetchTimeout();
                }
            });
        };




        this.init = function () {
            this.clearSchedulerDetails();
            this.fetchJobs();
            this.fetchSchedulerDetails();
           // this.setRefreshInterval();
        }

        this.refresh = function(){
            this.fetchSchedulerDetails();
            this.fetchJobs();
        }
        /**

        this.clearRefreshInterval = function () {
            if (self.refreshInterval != null) {
                $interval.cancel(self.refreshInterval);
                self.refreshInterval = null;
                self.refreshIntervalSet = false;
            }
        }


        this.setRefreshInterval = function () {
            this.clearRefreshInterval();
            if (self.refreshIntervalTime ) {
                self.refreshInterval = $interval(function() {self.refresh();}, self.refreshIntervalTime);
                self.refreshIntervalSet = true;

            }
        }
*/


        this.init();


        $scope.$on('$destroy', function () {
           // self.clearRefreshInterval();
            if(self.fetchJobsTimeout) {
                $timeout.cancel(self.fetchJobsTimeout);
            }
            self.fetchJobsTimeout = null;
            self.destroyed = true;
        });
    };

    angular.module(MODULE_OPERATIONS).controller('SchedulerController', controller);


}());