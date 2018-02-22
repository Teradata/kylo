import * as angular from "angular";
import {moduleName} from "./module-name";
import 'pascalprecht.translate';
import * as _ from 'underscore';
import OpsManagerJobService from "../../services/OpsManagerJobService";
import IconService from "../../services/IconStatusService";

export class JobDetailsDirectiveController implements ng.IComponentController{
        allowAdmin: boolean;
        pageName: string;
        //Page State
        refreshing: boolean;
        loading: boolean;
        showProgress: boolean;

        //Track active requests and be able to cancel them if needed
        activeJobRequests: any[]
        jobData: any;
        stepData: any;
        allSteps: any[];
        jobTab: any;
        tabMetadata: any;
        UNKNOWN_JOB_EXECUTION_ID: any;
        next: any;
        previous: any;
        
        //Refresh Intervals
        refreshTimeout: any;
        jobExecutionId: any;
        executionId: any;

        unableToFindJob: boolean;
        showJobParameters: boolean;
        toggleJobParametersVar: any;
        relatedJobs: any[];
        relatedJob: any;
        changeRelatedJobVar: any;
        navigateToLogsVar: any;
        navigateToLogsForStepVar: any;
        logUiEnabledVar: boolean;
        refreshInterval: any;
        refreshIntervalTime: any;
        deferred: any;
        relatedJobIndex: any;
        loadJobs: any;

constructor(private $scope: any,
            private $http: any,
            private $state: any,
            private $interval: any,
            private $timeout: any,
            private $q: any,
            private OpsManagerJobService: any,
            private IconService: any,
            private AccessControlService: any,
            private AngularModuleExtensionService: any,
            private $filter: any){
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

        this.jobTab = {title: 'JOB', content: this.jobData}

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

         $scope.$on('$destroy', ()=>{
            this.cancelLoadJobDataTimeout();
        })

        // Fetch allowed permissions
        AccessControlService.getUserAllowedActions()
                .then((actionSet: any)=> {
                    this.allowAdmin = AccessControlService.hasAction(AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
        });
    }

        init = ()=> {
            var executionId = this.executionId;
            this.jobExecutionId = executionId;
            this.relatedJob = this.jobExecutionId;
            this.loadJobData();
            //   loadRelatedJobs();
        }
        nextTab=()=> {
            this.tabMetadata.selectedIndex = Math.min(this.tabMetadata.selectedIndex + 1, 2);
        };
        previousTab=()=> {
            this.tabMetadata.selectedIndex = Math.max(this.tabMetadata.selectedIndex - 1, 0);
        };

        logUiEnabled=() =>{
            this.logUiEnabledVar = this.AngularModuleExtensionService.stateExists("log-ui");
        }
        navigateToLogs=(jobStartTime: any, jobEndTime: any)=>{
            this.$state.go("log-ui", {startTime:jobStartTime, endTime:jobEndTime, showCustom:true});
        }

        navigateToLogsForStep=(failedStep: any)=>{
            var previousStep: any = '';
            for (var title in this.stepData) {
                var step = this.stepData[title];
                if(failedStep.title == title) {
                    break;
                }
                previousStep = step;
            }
            this.$state.go("log-ui", {startTime:previousStep.startTime, endTime:failedStep.content.endTime, showCustom:true});
        }

        //Tab Functions
        toggleJobParameters=(name: any)=> {
            if (name == 'JobParameters') {
                this.showJobParameters = true;
            }
            else {
                this.showJobParameters = false;
            }
        }

        selectFirstTab=() =>{
            this.tabMetadata.selectedIndex = 0;
            // this.selectedTab = this.jobTabs[0];
        }

        cancelLoadJobDataTimeout=()=> {
            if (this.refreshTimeout != null) {
                this.$timeout.cancel(this.refreshTimeout);
                this.refreshTimeout = null;
            }
        }

        //Load Feeds
        loadJobData=(force?: any)=> {
            this.cancelLoadJobDataTimeout();

            if (force || !this.refreshing) {
                this.unableToFindJob = false;
                if (force) {
                    angular.forEach(this.activeJobRequests, (canceler: any, i: any)=> {
                        canceler.resolve();
                    });
                    this.activeJobRequests = [];
                }

                this.refreshing = true;
                var sortOptions = '';
                var canceler = this.$q.defer();
                var successFn = (response: any)=> {

                    if (response.data) {
                        //transform the data for UI
                        this.transformJobData(response.data);
                        if (response.data.running == true || response.data.stopping == true) {
                            this.cancelLoadJobDataTimeout();
                            this.refreshTimeout = this.$timeout(()=> {
                                this.loadJobData()
                            }, 1000);
                        }

                        if (this.loading) {
                            this.loading = false;
                        }
                    }
                    else {
                        this.unableToFindJob = true;
                    }

                    this.finishedRequest(canceler);

                }
                var errorFn = (err: any)=> {
                    this.finishedRequest(canceler);
                    this.unableToFindJob = true;
                    this.addJobErrorMessage(err)
                }
                var finallyFn = ()=> {

                }
                this.activeJobRequests.push(canceler);
                this.deferred = canceler;
                var params = {'includeSteps': true}

                this.$http.get(this.OpsManagerJobService.LOAD_JOB_URL(this.jobExecutionId), {timeout: canceler.promise, params: params}).then(successFn, errorFn);
            }

            return this.deferred;

        }

        finishedRequest=(canceler: any)=> {
            var index = _.indexOf(this.activeJobRequests, canceler);
            if (index >= 0) {
                this.activeJobRequests.splice(index, 1);
            }
            this.enableTabAnimation();
            canceler.resolve();
            canceler = null;
            this.refreshing = false;
            this.showProgress = false;
        }


          loadRelatedJobs=(setExecutionId: any)=> {
            var successFn = (response: any)=> {
                if (response.data) {
                    this.relatedJobs = response.data;
                    if (setExecutionId) {
                        this.relatedJob = setExecutionId;
                    }
                    this.updateRelatedJobIndex();
                }
            }
            var errorFn = (err: any)=> {
            }
            //todo uncomment once related job are linked and working
            // $http.get(OpsManagerJobService.RELATED_JOBS_URL(this.jobExecutionId)).then(successFn, errorFn);
        }
        
        updateRelatedJobIndex=()=> {
            if (this.relatedJob) {
                angular.forEach(this.relatedJobs, (job: any, i: any)=> {
                    if (job.jobExecutionId == this.relatedJob) {
                        this.relatedJobIndex = i;
                    }
                })
            }
        }

        disableTabAnimation=()=> {
            angular.element('.job-details-tabs').addClass('no-animation');
        }

        enableTabAnimationTimeout: any;
        enableTabAnimation() {
            if (this.enableTabAnimationTimeout) {
                this.$timeout.cancel(this.enableTabAnimationTimeout);
            }
            this.enableTabAnimationTimeout = this.$timeout(()=> {
                angular.element('.job-details-tabs').removeClass('no-animation');
            }, 1000);

        }

        changeRelatedJob=(relatedJob: any)=> {
            //remove animation for load
            this.disableTabAnimation();
            this.loadJobExecution(relatedJob)
        }

        mapToArray=(map: any, obj: any, fieldName: any, removeKeys?: any)=> {
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

        assignParameterArray=(obj: any)=> {
            if (obj) {
                if (obj.jobParameters) {
                    this.mapToArray(obj.jobParameters, obj, 'jobParametersArray')
                }
                else {
                    obj['jobParametersArray'] = [];
                }

                if (obj.executionContext) {
                    this.mapToArray(obj.executionContext, obj, 'executionContextArray', ['batch.stepType', 'batch.taskletType'])
                }
                else {
                    obj['executionContextArray'] = [];
                }

            }

        }

        statusCssMap: any;
        cssClassForDisplayStatus=(displayStatus: any)=> {
            var cssStatus = {
                'success': ['COMPLETED', 'STARTING', 'STARTED', 'EXECUTING'],
                'error': ['FAILED'],
                'warn': ['STOPPING', 'STOPPED', 'WARNING'],
                'abandoned': ['ABANDONED'],
                'unknown': ['UNKNOWN']
            };
            this.statusCssMap = {};
            _.each(cssStatus, (arr: any, key: any)=> {
                _.each(arr,  (status: any, i: any)=> {
                    this.statusCssMap[status] = key;
                });
            });
            return this.statusCssMap[displayStatus];

        }

        transformJobData=(job: any)=> {
            this.assignParameterArray(job);
            job.name = job.jobName;
            job.running = false;
            job.stopping = false;
            job.exitDescription = job.exitStatus;
            if (job.exitDescription == undefined || job.exitDescription == '') {
                job.exitDescription = this.$filter('translate')('views.JobDetailsDirective.Nda')
            }
            job.tabIcon = undefined;

            var iconStyle = this.IconService.iconStyleForJobStatus(job.displayStatus);
            var icon = this.IconService.iconForJobStatus(job.displayStatus);
            job.cssStatusClass =this.cssClassForDisplayStatus(job.displayStatus);

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
                job.executedSteps = _.chain(job.executedSteps).sortBy('startTime').sortBy('nifiEventId').value();

                angular.forEach(job.executedSteps, (step: any, i: any)=> {
                    var stepName = "Step " + (i + 1);
                    if (this.stepData[stepName] == undefined) {
                        this.stepData[stepName] = {};
                        this.allSteps.push({title: stepName, content: this.stepData[stepName]})
                    }
                    angular.extend(this.stepData[stepName], this.transformStep(step));

                });
            }
        }

        transformStep=(step: any)=> {
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
                step.exitDescription = this.$filter('translate')('views.JobDetailsDirective.Nda')
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

            this.assignParameterArray(step);
            return step;
        }

        clearRefreshInterval=()=> {
            if (this.refreshInterval != null) {
                this.$interval.cancel(this.refreshInterval);
                this.refreshInterval = null;
            }
        }

        setRefreshInterval=() =>{
            this.clearRefreshInterval();
            if (this.refreshIntervalTime) {
                this.refreshInterval = this.$interval(this.loadJobs, this.refreshIntervalTime);

            }
        }

        //Util Functions
        capitalize=(string: string)=> {
            return string.charAt(0).toUpperCase() + string.substring(1).toLowerCase();
        }

        updateJob=(executionId: any, job: any)=> {
            this.clearErrorMessage();
            var existingJob = this.jobData;
            if (existingJob && executionId == job.executionId) {
                this.transformJobData(job);
            }
            else {
                this.disableTabAnimation();
                this.loadJobExecution(job.executionId);

            }
        }

        loadJobExecution=(executionId: any)=> {
            this.jobExecutionId = executionId;

            //reset steps
            var len = this.allSteps.length;
            while (len > 1) {
                this.allSteps.splice(len - 1, 1);
                len = this.allSteps.length;
            }
            //clear out all the steps
            angular.forEach(Object.keys(this.stepData), (key: any, i: any)=> {
                delete this.stepData[key];
            });

            this.loadJobData(true);
            //  loadRelatedJobs(executionId);
        }

        addJobErrorMessage=(errMsg: any) =>{
            var existingJob = this.jobData;
            if (existingJob) {
                existingJob.errorMessage = errMsg;
            }
        }

        clearErrorMessage=()=> {
            var existingJob = this.jobData;
            if (existingJob) {
                existingJob.errorMessage = '';
            }
        }

        restartJob = (event: any)=> {
            event.stopPropagation();
            event.preventDefault();
            var executionId = this.jobData.executionId;
            var xhr = this.OpsManagerJobService.restartJob(this.jobData.executionId, {includeSteps: true}, (response: any)=> {
                        this.updateJob(executionId, response.data);
                        //  loadJobs(true);
                    }, (errMsg: any)=> {
                        this.addJobErrorMessage(executionId+errMsg); // executionId, errMsg
                    }
            );
        };

        stopJob = (event: any)=> {
            event.stopPropagation();
            event.preventDefault();
            var executionId = this.jobData.executionId;
            this.OpsManagerJobService.stopJob(this.jobData.executionId, {includeSteps: true}, (response: any)=> {
                this.updateJob(executionId, response.data)
                //  loadJobs(true);
            })
        };

        abandonJob = (event: any)=> {
            event.stopPropagation();
            event.preventDefault();
            var executionId = this.jobData.executionId;
            this.OpsManagerJobService.abandonJob(this.jobData.executionId, {includeSteps: true}, (response: any)=> {
                this.updateJob(executionId, response.data)
                //  loadJobs(true);
            })
        };

        failJob = (event: any)=> {
            event.stopPropagation();
            event.preventDefault();
            var executionId = this.jobData.executionId;
            this.OpsManagerJobService.failJob(this.jobData.executionId, {includeSteps: true}, (response: any)=>{
                this.updateJob(executionId, response.data)
                //  loadJobs(true);
            })
        };
}
//$scope,$http, $state, $interval, $timeout, $q, OpsManagerJobService, IconService, AccessControlService, AngularModuleExtensionService, $filter
angular.module(moduleName)
.service('OpsManagerJobService',['$q', '$http', '$log', 'HttpService', 'NotificationService', 'OpsManagerRestUrlService',OpsManagerJobService])
.service('IconService',[IconService])
.controller("JobDetailsDirectiveController", ["$scope","$http", "$state", "$interval","$timeout","$q",
            "OpsManagerJobService","IconService","AccessControlService", "AngularModuleExtensionService",
            "$filter",JobDetailsDirectiveController]);
angular.module(moduleName).directive("tbaJobDetails", [

    ()=>
    {
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
]);