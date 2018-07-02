import * as angular from "angular";
import {moduleName} from "./module-name";
import 'pascalprecht.translate';
import * as _ from 'underscore';
import OpsManagerJobService from "../../services/OpsManagerJobService";
import IconService from "../../services/IconStatusService";
import OpsManagerRestUrlService from "../../services/OpsManagerRestUrlService";

import {Common} from "../../../common/CommonTypes";


class PageState {
    refreshing: boolean;
    loading: boolean;
    showProgress: boolean;

    constructor() {

    }

    showLoading() {
        this.refreshing = false;
        this.loading = true;
        this.showProgress = true;
    }

    isRefreshing() {
        return this.refreshing;
    }

    isLoading() {
        return this.loading;
    }

    finishedLoading() {
        this.loading = false;
    }

    finished() {
        this.refreshing = false;
        this.showProgress = false;
        this.loading = false;
    }
}

class Step {
    name: string;
    stepName: string;
    nifiEventId: number;
    startTime: any;
    endTime: any;
    runTime: number;
    tabIcon: string;
    tabIconStyle: string;
    running: boolean;
    displayStatus: string;
    exitCode: string;
    cssStatusClass?: string;
    statusIcon: string;
    disabled: boolean;
    executionContext:any;
    exitDescription:string;
    executionId:number;
    jobParameters?:any[];
    constructor() {

    }


}

class Job {
    jobExecutionId: number;
    executionId:number;
    name: string;
    jobName: string;
    exitDescription: string;
    running: boolean;
    stopping: boolean;
    tabIcon: string;
    tabIconStyle: string;
    statusIcon: string;
    cssStatusClass?: string;
    displayStatus:string;
    status:string;
    executedSteps: Step[];
    jobParameters: any[];
    executionContext:any;
    executionContextArray: any[];
    renderTriggerRetry?:boolean
    triggerRetryFlowfile:any;
    errorMessage?:string;
    exitStatus:string;

    constructor(){

    }

}

class StepWithTitle {

    title:string;
    content:Step;

    constructor( title: string,  content: Step) {
        this.title = title;
        this.content = content;
    }
}

class JobWithTitle {
    title:string;
    content:Job;

    constructor( title: string,  content: Job) {
        this.title = title;
        this.content = content;
    }
}

class TabAnimationControl {

    private enableTabAnimationTimeout: angular.IPromise<any>;

    constructor(private $timeout: angular.ITimeoutService) {

    }

    /**
     * Might not be needed
     */
    disableTabAnimation() {
        angular.element('.job-details-tabs').addClass('no-animation');
    }


    enableTabAnimation() {
        if (this.enableTabAnimationTimeout) {
            this.$timeout.cancel(this.enableTabAnimationTimeout);
        }
        this.enableTabAnimationTimeout = this.$timeout(() => {
            angular.element('.job-details-tabs').removeClass('no-animation');
        }, 1000);

    }
}

export class JobDetailsDirectiveController implements ng.IComponentController {

    /**
     * Flag for admin controls
     */
    allowAdmin: boolean = false;

    /**
     * Track loading, progress
     * @type {PageState}
     */
    pageState: PageState = new PageState();

    /**
     * Track active requests and be able to cancel them if needed
     */
    activeJobRequests: angular.IDeferred<any>[] = [];

    /**
     *
     */
    jobData: Job;

    /**
     * Map of the stepName to Step objects
     */
    stepData: Common.Map<Step>;

    /**
     * Array of all the steps
     */
    allSteps: StepWithTitle[] = [];
    /**
     * The Job with content
     */
    jobTab: JobWithTitle;

    tabMetadata: {
        selectedIndex: 0,
        bottom: false
    };

    UNKNOWN_JOB_EXECUTION_ID: string = "UNKNOWN";


    //Refresh Intervals
    refreshTimeout: angular.IPromise<any> = null;
    /**
     * The active Job ID
     */
    jobExecutionId: number;

    executionId:string;

    /**
     * Flag indicating the loading of the passed in JobExecutionId was unable to bring back data
     * @type {boolean}
     */
    unableToFindJob: boolean = false;
    /**
     * Show the Job Params in the Job Details tab
     * @type {boolean}
     */
    showJobParameters: boolean = true;

    /**
     * Should we show the log ui buttons
     * @type {boolean}
     */
    logUiEnabled: boolean = false;

    deferred: angular.IDeferred<any>;

    tabAnimationControl: TabAnimationControl;


    statusCssMap: Common.Map<string>;

    static $inject = ["$scope", "$http", "$state", "$interval", "$timeout", "$q",
        "$mdToast", "OpsManagerRestUrlService",
        "OpsManagerJobService", "IconService", "AccessControlService", "AngularModuleExtensionService",
        "$filter"]

    constructor(private $scope: angular.IScope,
                private $http: angular.IHttpService,
                private $state: any,
                private $interval: angular.IIntervalService,
                private $timeout: angular.ITimeoutService,
                private $q: angular.IQService,
                private $mdToast: angular.material.IToastService,
                private OpsManagerRestUrlService: any,
                private OpsManagerJobService: any,
                private IconService: any,
                private AccessControlService: any,
                private AngularModuleExtensionService: any,
                private $filter: angular.IFilterService) {

        this.pageState.showLoading();


        this.jobData = new Job();

        this.stepData = {};

        this.jobTab = {title: 'JOB', content: this.jobData}


        this.tabAnimationControl = new TabAnimationControl(this.$timeout);

        var cssStatus = {
            'success': ['COMPLETED', 'STARTING', 'STARTED', 'EXECUTING'],
            'error': ['FAILED'],
            'warn': ['STOPPING', 'STOPPED', 'WARNING'],
            'abandoned': ['ABANDONED'],
            'unknown': ['UNKNOWN']
        };
        this.statusCssMap = {};
        _.each(cssStatus, (arr: any, key: any) => {
            _.each(arr, (status: any, i: any) => {
                this.statusCssMap[status] = key;
            });
        });


        $scope.$on("$destroy", this.ngOnDestroy.bind(this));

    }


    $onInit() {
        this.ngOnInit();
    }

    $onDestroy() {
        this.ngOnDestroy();
    }

    ngOnInit() {
        this.jobExecutionId = parseInt(this.executionId)
        //init the log ui flag
        this.logUiEnabled = this.AngularModuleExtensionService.stateExists("log-ui");

        // Fetch allowed permissions
        this.AccessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowAdmin = this.AccessControlService.hasAction(this.AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
            });
        this.loadJobData();

    }

    ngOnDestroy() {
        this.cancelLoadJobDataTimeout();
    }


    abandonJob(event: angular.IAngularEvent) {
        event.stopPropagation();
        event.preventDefault();
        var executionId = this.jobData.executionId;
        this.OpsManagerJobService.abandonJob(this.jobData.executionId, {includeSteps: true}, (response: any) => {
            this.updateJob(executionId, response.data)
        })
    }

    failJob(event: angular.IAngularEvent) {
        event.stopPropagation();
        event.preventDefault();
        var executionId = this.jobData.executionId;

        let _fail = ()=>{
            this.OpsManagerJobService.failJob(this.jobData.executionId, {includeSteps: true}, (response:any) => {
                this.updateJob(executionId, response.data);
            })
        }

        if(this.jobData.renderTriggerRetry){
            this.triggerSavepointReleaseFailure(_fail)
        }
        else {
            _fail()
        }
    };

    restartJob(event:angular.IAngularEvent) {
        event.stopPropagation();
        event.preventDefault();
        var executionId = this.jobData.executionId;
        this.OpsManagerJobService.restartJob(this.jobData.executionId, {includeSteps: true}, (response:any) => {
                this.updateJob(executionId, response.data);
            }, (errMsg:any) => {
                this.addJobErrorMessage(errMsg);
            }
        );
    };

    triggerSavepointRetry() {
        if (angular.isDefined(this.jobData.triggerRetryFlowfile)) {
            this.jobData.renderTriggerRetry = false;
            this.$http.post(this.OpsManagerRestUrlService.TRIGGER_SAVEPOINT_RETRY(this.jobExecutionId, this.jobData.triggerRetryFlowfile),null).then(() => {

                this.$mdToast.show(
                    this.$mdToast.simple()
                        .textContent('Triggered the retry')
                        .hideDelay(3000)
                );
                this.loadJobData(true);
            });
        }
    }

    triggerSavepointReleaseFailure(callbackFn: any) {
        if (angular.isDefined(this.jobData.triggerRetryFlowfile)) {
            this.$http.post(this.OpsManagerRestUrlService.TRIGGER_SAVEPOINT_RELEASE(this.jobExecutionId, this.jobData.triggerRetryFlowfile),null).then((response: any) => {

                this.$mdToast.show(
                    this.$mdToast.simple()
                        .textContent('Triggered the release and failure')
                        .hideDelay(3000)
                );
                if (angular.isDefined(callbackFn)) {
                    callbackFn();
                }
                this.loadJobData(true);

            });
        }
    }


    navigateToLogs(jobStartTime: any, jobEndTime: any) {
        this.$state.go("log-ui", {startTime: jobStartTime, endTime: jobEndTime, showCustom: true});
    }

    navigateToLogsForStep(failedStep: any) {
        var previousStep: any = '';
        for (var title in this.stepData) {
            var step = this.stepData[title];
            if (failedStep.title == title) {
                break;
            }
            previousStep = step;
        }
        this.$state.go("log-ui", {startTime: previousStep.startTime, endTime: failedStep.content.endTime, showCustom: true});
    }

    //Tab Functions
    toggleJobParameters(name: string) {
        if (name == 'JobParameters') {
            this.showJobParameters = true;
        }
        else {
            this.showJobParameters = false;
        }
    }


    private cancelLoadJobDataTimeout() {
        if (this.refreshTimeout != null) {
            this.$timeout.cancel(this.refreshTimeout);
            this.refreshTimeout = null;
        }
    }

    //Load Feeds
    private loadJobData(force?: boolean) {
        this.cancelLoadJobDataTimeout();

        if (force || !this.pageState.refreshing) {
            this.unableToFindJob = false;
            if (force) {
                angular.forEach(this.activeJobRequests, (canceler: any, i: any) => {
                    canceler.resolve();
                });
                this.activeJobRequests = [];
            }

            this.pageState.refreshing = true;
            var sortOptions = '';
            var canceler = this.$q.defer();
            var successFn = (response: any) => {

                if (response.data) {
                    //transform the data for UI
                    this.transformJobData(response.data);
                    if (response.data.running == true || response.data.stopping == true) {
                        this.cancelLoadJobDataTimeout();
                        this.refreshTimeout = this.$timeout(() => {
                            this.loadJobData()
                        }, 1000);
                    }
                    this.pageState.finishedLoading();
                }
                else {
                    this.unableToFindJob = true;
                }

                this.finishedRequest(canceler);

            }
            var errorFn = (err: any) => {
                this.finishedRequest(canceler);
                this.unableToFindJob = true;
                this.addJobErrorMessage(err)
            }
            var finallyFn = () => {

            }
            this.activeJobRequests.push(canceler);
            this.deferred = canceler;
            var params = {'includeSteps': true}

            this.$http.get(this.OpsManagerJobService.LOAD_JOB_URL(this.jobExecutionId), {timeout: canceler.promise, params: params}).then(successFn, errorFn);
        }

        return this.deferred;

    }

    private finishedRequest(canceler: angular.IDeferred<any>){
        var index = _.indexOf(this.activeJobRequests, canceler);
        if (index >= 0) {
            this.activeJobRequests.splice(index, 1);
        }
        this.tabAnimationControl.enableTabAnimation();
        canceler.resolve();
        canceler = null;
        this.pageState.finished();
    }


    private mapToArray(map:any[], obj: any , type:string, fieldName:string, removeKeys ?:string[]) {
        if (removeKeys == undefined) {
            removeKeys = [];
        }
        var arr = [];
        var renderTriggerSavepointRetry = false;
        var jobComplete = false;
        for (var key in map) {
            if (_.indexOf(removeKeys, key) == -1) {
                if (map.hasOwnProperty(key)) {
                    arr.push({key: key, value: map[key]});
                    if (type == 'JOB' && fieldName == 'executionContextArray') {
                        if(key == 'kylo.job.finished') {
                            jobComplete = true;
                        }
                        if(!renderTriggerSavepointRetry) {
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
    }

    private checkTriggerSavepoint(job:Job,key:string,value:any){
        if(key == 'savepoint.trigger.flowfile' && angular.isDefined(value)) {
            {
                job.renderTriggerRetry = true;
                job.triggerRetryFlowfile = value;
                return true;
            }
        }
        return false;
    }


    private assignParameterArray (obj:any, type:string) {
        if (obj) {
            if (obj.jobParameters) {
                this.mapToArray(obj.jobParameters, obj, type,'jobParametersArray')
            }
            else {
                obj['jobParametersArray'] = [];
            }

            if (obj.executionContext) {
                this.mapToArray(obj.executionContext, obj, type,'executionContextArray', ['batch.stepType', 'batch.taskletType'])
            }
            else {
                obj['executionContextArray'] = [];
            }

        }

    }


    private cssClassForDisplayStatus(displayStatus: any){
        return this.statusCssMap[displayStatus];
    }

    private transformJobData(job: Job) {
        this.assignParameterArray(job,'JOB');
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

            angular.forEach(job.executedSteps, (step: any, i: any) => {
                var stepName = "Step " + (i + 1);
                if (this.stepData[stepName] == undefined) {
                    this.stepData[stepName] = new Step();
                    this.allSteps.push({title: stepName, content: this.stepData[stepName]})
                }
                angular.extend(this.stepData[stepName], this.transformStep(step));

            });
        }
    }

   private transformStep (step: Step) {
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

        this.assignParameterArray(step,'STEP');
        return step;
    }

    private updateJob(executionId: number, job: Job) {
        this.clearErrorMessage();
        var existingJob = this.jobData;
        if (existingJob && executionId == job.executionId) {
            this.transformJobData(job);
        }
        else {
            this.tabAnimationControl.disableTabAnimation();
            this.loadJobExecution(job.executionId);

        }
    }

    private loadJobExecution(executionId: number) {
        this.jobExecutionId = executionId;

        //reset steps
        var len = this.allSteps.length;
        while (len > 1) {
            this.allSteps.splice(len - 1, 1);
            len = this.allSteps.length;
        }
        //clear out all the steps
        angular.forEach(Object.keys(this.stepData), (stepName: string, i: number) => {
            delete this.stepData[stepName];
        });

        this.loadJobData(true);
    }

    private addJobErrorMessage(errMsg: string) {
        var existingJob = this.jobData;
        if (existingJob) {
            existingJob.errorMessage = errMsg;
        }
    }

    private clearErrorMessage() {
        var existingJob = this.jobData;
        if (existingJob) {
            existingJob.errorMessage = '';
        }
    }
}

angular.module(moduleName)
    .controller("JobDetailsDirectiveController", JobDetailsDirectiveController);
angular.module(moduleName).directive("tbaJobDetails", [

    () => {
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
