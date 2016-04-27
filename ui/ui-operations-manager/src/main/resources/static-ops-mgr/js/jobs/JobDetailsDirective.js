/*
 * Copyright (c) 2015.
 */

/**
 * This Directive is wired in to the FeedStatusIndicatorDirective.
 * It uses the OverviewService to watch for changes and update after the Indicator updates
 */


(function () {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                cardTitle: "@",
                refreshIntervalTime:"@"
            },
            controllerAs: 'vm',
            scope: true,
            templateUrl: 'js/jobs/job-details-template.html',
            controller: "JobDetailsDirectiveController",
            link: function ($scope, element, attrs, controller) {

            }
        };
    }

    var controller = function ($scope,$stateParams,$http,$interval,$timeout, $q,$mdDialog,JobData,AlertsService, StateService, IconService) {
        var self = this;
        this.pageName = 'jobs';
        //Page State
        this.refreshing = false;
        this.loading = true;
        this.showProgress = true;

        //Track active requests and be able to cancel them if needed
        this.activeJobRequests = []

        //Setup the Tabs
        this.jobTabData = {'JOB':{}};
        this.jobTabs = [{title:'JOB',content:self.jobTabData['JOB']}];
        this.selectedTab = this.jobTabs[0];
        this.tabMetadata = {
            selectedIndex: 0,
            bottom:        false
        };

        this.next = nextTab;
        this.previous = previousTab;


        //Refresh Intervals
        this.refreshTimeout = null;
        this.jobData = {};
        this.jobExecutionId = null;




        this.showJobParameters = true;
        this.toggleJobParameters = toggleJobParameters;
        this.relatedJobs = [];
        this.relatedJob = null;
        this.changeRelatedJob = changeRelatedJob;


        this.init = function(){
            var executionId = $stateParams.executionId;
            self.jobExecutionId = executionId;
            this.relatedJob = self.jobExecutionId;
            loadJobData();
            loadRelatedJobs();
        }



        this.init();



        function nextTab() {
            self.tabMetadata.selectedIndex = Math.min(self.tabMetadata.selectedIndex + 1, 2) ;
        };
        function previousTab() {
            self.tabMetadata.selectedIndex = Math.max(self.tabMetadata.selectedIndex - 1, 0);
        };

        //Tab Functions

        /**
         * Called when a Tab is clicked
         * @param tab
         */
        this.tabSelected = function(tab){
            self.selectedTab = tab;
        }

        function toggleJobParameters(name){
            if(name == 'JobParameters'){
                self.showJobParameters= true;
            }
            else {
                self.showJobParameters = false;
            }
            console.log('TOGGLE ',name, self.showJobParameters)
        }

        function selectFirstTab(){
            self.tabMetadata.selectedIndex = 0;
            self.selectedTab = self.jobTabs[0];
        }


function cancelLoadJobDataTimeout(){
    if(self.refreshTimeout != null){
        $timeout.cancel(self.refreshTimeout);
        self.refreshTimeout = null;
    }
}
        //Load Feeds

        function loadJobData(force) {
            cancelLoadJobDataTimeout();

            if(force || !self.refreshing) {

                if(force ){
                    angular.forEach(self.activeJobRequests,function(canceler,i){
                        canceler.resolve();
                    });
                    self.activeJobRequests = [];
                }

                self.refreshing = true;
                var sortOptions = '';
                var canceler = $q.defer();
                var successFn = function (response) {

                    if (response.data) {
                        //transform the data for UI
                        transformJobData(response.data);
                        if(response.data.running == true || response.data.stopping == true) {
                            cancelLoadJobDataTimeout();
                            self.refreshTimeout = $timeout(function(){loadJobData() },1000);
                        }

                        if (self.loading) {
                            self.loading = false;
                        }
                    }


                    finishedRequest(canceler);

                }
                var errorFn = function (err) {
                    finishedRequest(canceler);
                }
                var finallyFn = function () {

                }
                self.activeJobRequests.push(canceler);
                self.deferred = canceler;
                var params = {'includeSteps':true}

                $http.get(JobData.LOAD_JOB_URL(self.jobExecutionId),{timeout: canceler.promise,params:params}).then( successFn, errorFn);
            }
           // self.showProgress = true;

            return self.deferred;

        }


        function finishedRequest(canceler) {
            var index = _.indexOf(self.activeJobRequests,canceler);
            if(index >=0){
                self.activeJobRequests.splice(index,1);
            }
            enableTabAnimation();
            canceler.resolve();
            canceler = null;
            self.refreshing = false;
            self.showProgress = false;
        }


        function loadRelatedJobs(setExecutionId) {
            var successFn = function (response) {
                if (response.data) {
                    self.relatedJobs = response.data;
                    if(setExecutionId){
                        self.relatedJob = setExecutionId;
                    }
                    updateRelatedJobIndex();
                }
            }
            var errorFn = function (err) {
            }

            $http.get(JobData.RELATED_JOBS_URL(self.jobExecutionId)).then( successFn, errorFn);
        }

        function updateRelatedJobIndex() {
            if(self.relatedJob){
                angular.forEach(self.relatedJobs,function(job,i) {
                    if(job.jobExecutionId == self.relatedJob){
                        self.relatedJobIndex = i;
                    }
                })
            }
        }

        function disableTabAnimation(){
            angular.element('.job-details-tabs').addClass('no-animation');
        }

        function enableTabAnimation(){
            if(self.enableTabAnimationTimeout){
                $timeout.cancel(self.enableTabAnimationTimeout);
            }
            self.enableTabAnimationTimeout = $timeout(function() {
                angular.element('.job-details-tabs').removeClass('no-animation');
            },1000);

        }


        function changeRelatedJob(relatedJob){
            console.log('CHANGING RELATED JOB ',relatedJob, self.relatedJob, self.jobExecutionId)
          //remove animation for load
            disableTabAnimation();
            loadJobExecution(relatedJob)
        }

        function mapToArray(map,obj,fieldName, removeKeys){
            if(removeKeys == undefined){
                removeKeys = [];
            }
            var arr = [];
            for(var key in map) {
                if(_.indexOf(removeKeys,key) == -1) {
                    if (map.hasOwnProperty(key)) {
                        arr.push({key: key, value: map[key]});
                    }
                }
            }
            obj[fieldName] = arr;
        }

        function assignParameterArray(obj){
                if(obj) {
                    if (obj.jobParameters) {

                        mapToArray(obj.jobParameters, obj, 'jobParametersArray')
                    }
                    else {
                        obj['jobParametersArray'] = [];
                    }

                    if(obj.executionContext){
                        mapToArray(obj.executionContext, obj, 'executionContextArray',['batch.stepType','batch.taskletType'])
                    }
                    else {
                        obj['executionContextArray'] = [];
                    }

                }




        }

        function transformJobData(job){
            assignParameterArray(job);
            job.name = job.jobName;
            job.running = false;
            job.stopping = false;
            job.exitDescription = job.exitStatus;
            if(job.exitDescription == undefined || job.exitDescription == ''){
                job.exitDescription = 'No description available.'
            }
            job.tabIcon = undefined;

            var iconStyle = IconService.iconStyleForJobStatus(job.displayStatus);
            var icon  = IconService.iconForJobStatus(job.displayStatus);

            if(job.status == "STARTED"){
                job.running = true;
            }
            if(job.status =='STOPPING') {
                job.stopping = true;
            }
            job.statusIcon = icon;


            angular.extend(self.jobTabData['JOB'],job);
            self.jobData = self.jobTabData['JOB'];

            if(job.executedSteps){
                angular.forEach(job.executedSteps,function(step,i) {

                        var tabName = "STEP " + (i + 1);
                        if (self.jobTabData[tabName] == undefined) {
                            var data = {};
                            self.jobTabData[tabName] = data;
                            self.jobTabs.push({title: tabName, content:  self.jobTabData[tabName]})
                             }
                        angular.extend(self.jobTabData[tabName], transformStep(step));


                });
            }
       }

            function transformStep(step){
                step.name = step.stepName;
                step.running = false;
                step.tabIcon = undefined;
                if(step.runTime == undefined && step.endTime && step.startTime) {
                    step.runTime = step.endTime - step.startTime;
                }
                if(step.endTime == undefined && step.startTime) {
                    step.running = true;
                    if(step.runTime == undefined) {
                        step.runTime = new Date().getTime() - step.startTime;
                    }
                }
                step.displayStatus = step.exitCode;

                if(step.exitDescription == undefined || step.exitDescription == ''){
                    step.exitDescription = 'No description available.'
                }

                var style = IconService.iconStyleForJobStatus(step.displayStatus);
                var icon =  IconService.iconForJobStatus(step.displayStatus);
                step.statusIcon = icon;
                if(step.displayStatus == 'FAILED' || step.displayStatus == 'EXECUTING' ) {
                    step.tabIconStyle = style;
                    step.tabIcon = icon;
                }

                if(step.startTime == null || step.startTime == undefined){
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

        function updateJob(executionId,job){
            clearErrorMessage(executionId);
            var existingJob = self.jobTabData['JOB'];
            if(existingJob && executionId == job.executionId) {
                transformJobData(job);
            }
           else {
                disableTabAnimation();
               loadJobExecution( job.executionId);

            }
        }

        function loadJobExecution(executionId){
            self.jobExecutionId =executionId;

            //reset Job Tabs
            var len = self.jobTabs.length;
            while(len >1) {
                self.jobTabs.splice(len - 1, 1);
                len = self.jobTabs.length;
            }
            angular.forEach(Object.keys(self.jobTabData),function(key,i){
                if(key == 'JOB'){
                    //    self.jobTabData[key] = {};
                }
                else {
                    delete self.jobTabData[key];
                }
            })

            loadJobData(true);
            loadRelatedJobs(executionId);
            //selectFirstTab();
        }

        function addJobErrorMessage(executionId, errMsg) {
            var existingJob = self.jobTabData['JOB'];
            if(existingJob) {
                errMsg= errMsg.split('<br/>').join('\n');
                existingJob.errorMessage = errMsg;
            }
        }
        function clearErrorMessage(executionId) {
            var existingJob = self.jobTabData['JOB'];
            if(existingJob) {
                existingJob.errorMessage = '';
            }
        }


        this.restartJob = function (event) {
            event.stopPropagation();
            event.preventDefault();
            var executionId = self.jobData.executionId;
             var xhr = JobData.restartJob(self.jobData.executionId,{includeSteps:true}, function (data) {
                updateJob(executionId, data);
              //  loadJobs(true);
            }, function(errMsg){
                    addJobErrorMessage(executionId,errMsg);
            }
            );
        };

        this.stopJob = function (event) {
            event.stopPropagation();
            event.preventDefault();
            var executionId = self.jobData.executionId;
            JobData.stopJob(self.jobData.executionId, {includeSteps:true},function (data) {
                updateJob(executionId, data)
                //  loadJobs(true);
            })
        };

        this.abandonJob = function (event) {
            event.stopPropagation();
            event.preventDefault();
            var executionId = self.jobData.executionId;
            JobData.abandonJob(self.jobData.executionId,{includeSteps:true}, function (data) {
                updateJob(executionId, data)
                //  loadJobs(true);
            })
        };

        this.failJob = function (event) {
            event.stopPropagation();
            event.preventDefault();
            var executionId = self.jobData.executionId;
            JobData.failJob(self.jobData.executionId,{includeSteps:true}, function (data) {
                updateJob(executionId, data)
                //  loadJobs(true);
            })
        };

        this.showJobDialog = function(ev) {

            $mdDialog.show({
                controller: DuplicateJobDialogController,
                templateUrl: 'js/jobs/duplicate-job-template.html',
                parent: angular.element(document.body),
                targetEvent: ev,
                clickOutsideToClose:true,
                fullscreen: true,
                locals : {
                    jobExecutionId : self.jobExecutionId,
                    jobName: self.jobTabData['JOB'].jobName
                }
            })
                .then(function(job) {

                }, function() {

                });
        };

    };


    angular.module(MODULE_OPERATIONS).controller('JobDetailsDirectiveController', controller);

    angular.module(MODULE_OPERATIONS)
        .directive('tbaJobDetails', directive);

})();

function DuplicateJobDialogController($scope, $mdDialog, $mdToast, $http, JobData, StateService, jobName,jobExecutionId ){
        var self = this;

    $scope.jobParameters = [];
    $scope.newJobParameter = {type: 'STRING', lastValue:'Value', inputType:'text'};

    $scope.jobName = jobName;
    $scope.jobExecutionId =jobExecutionId;

    $scope.jobParameters = [];
    $scope.parameterTypes = ['STRING','NUMBER','DATE','UUID'];

    $scope.selectedParameterType = selectedParameterType;
    $scope.createJob = createJob;

    populateJobParameters();

    function populateJobParameters() {
            $http.get(JobData.JOB_PARAMETERS_URL($scope.jobExecutionId)).success(function(data) {
                $scope.jobParameters = [];
                if(data){
                        angular.forEach(data,function(jobParameter,i){
                            var value = '';
                            var keyName = jobParameter.name;
                            var typeCode = jobParameter.type;
                            var lastValue = jobParameter.value;
                            var inputType = 'text';
                            var modified = false;
                            var hide = false;
                            if(keyName.toUpperCase() == 'STARTTIME'){
                                value = moment().format('YYYYMMDDHHmm');
                                modified = true;
                                hide = true;
                            }
                            else if(keyName.toUpperCase().indexOf('UUID')>=0){
                                typeCode = 'UUID';
                                value = generateUUID();
                                modified = true;
                                hide = true;
                            }
                            else if(keyName.toUpperCase().indexOf('JOBRUNDATE')>=0){
                                typeCode = 'NUMBER';
                                value = new Date().getTime();
                                modified = true;
                                hide = true;
                            }
                            else if(typeCode.toUpperCase() == 'DATE'){
                                value = new Date().toISOString();
                                modified = true;
                            }
                            else {
                                value = lastValue;
                            }
                            if(typeCode.toUpperCase == 'NUMBER') {
                                inputType = 'number';
                            }

                            var jobParameter = {type:typeCode, name:keyName, lastValue:value,value:value, placeholder:lastValue, modified:modified, inputType:inputType,hide:hide};
                            if(keyName.toUpperCase() != "WEB JOB") {
                                $scope.jobParameters.push(jobParameter);
                            }
                        });
                }
            }).error(function() {
                console.log("Failed to retrieve the job parameter names.")
            })

    };


    function selectedParameterType(parameterType, param){
        if(parameterType == 'UUID'){
            param.value=generateUUID();
        }
        if(parameterType =='NUMBER'){
            param.inputType = 'number';
        }
        else {
            param.inputType = 'text';
        }
    }

    function generateUUID() {
        var d = new Date().getTime();
        var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,
            function(c) {
                var r = (d + Math.random()*16)%16 | 0;
                d = Math.floor(d/16);
                return (c=='x' ? r : (r&0x3|0x8)).toString(16);
            });
        return uuid;
    };


    function createJob() {
            var postData = {};
            postData.jobName = $scope.jobName;
            postData.jobParameters =  $scope.jobParameters;
        postData.jobParameters.push({type: 'STRING', name:'Web Job',value:'Created from Web @ '+new Date()});
        $http.post("/proxy/v1/jobs/", postData).success(function(data){
                $scope.jobParameters = [];
                $scope.selectedJobName = '';
/*
               var toast =     $mdToast.simple()
                        .textContent('Successfully created and launched the Job!')
                        .highlightAction(false)
                        .position('top right')
                        .hideDelay(5000);
                $mdToast.show(toast);
                */
                StateService.navigateToJobDetails(data.executionId);
                $mdDialog.hide();
            }).error(function(data) {
                $mdToast.show(
                    $mdToast.simple()
                        .textContent('Unable to create the Job!')
                        .position('top right')
                        .hideDelay(5000)
                );
            });
    }







           $scope.hide = function() {
                $mdDialog.hide();
            };

            $scope.cancel = function() {
                $mdDialog.cancel();
            };


    };
