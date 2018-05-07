import * as angular from "angular";
import {moduleName} from "./module-name";
import "pascalprecht.translate";
import * as _ from 'underscore';
import OpsManagerJobService from "../services/OpsManagerJobService";
import IconService from "../services/IconStatusService";
import TabService from "../services/TabService";

export class JobsCardController implements ng.IComponentController{
    allowAdmin: boolean;
    hideFeedColumn: any;
    pageName: any;
    loading: any;
    showProgress: any;
    jobIdMap: any;
    activeJobRequests: any[];
    timeoutMap: any;
    paginationData: any;
    viewType: any;
    tabs: any;
    tabMetadata: any;
    sortOptions: any;
    abandonAllMenuOption: any;
    additionalMenuOptions: any;
    selectedAdditionalMenuOptionVar: any;
    loaded: any;
    filter: any;
    tab: any;
    refreshing: any;
    onJobAction: any;

    filterHelpOperators: any[];
    filterHelpFields: any[];
    filterHelpExamples: any[];
    feed: any;

    deferred: any;
    feedFilter: any;
    promise: any;

    constructor(private $scope: any,
                private $http: any,
                private $mdDialog: any,
                private $timeout: any,
                private $mdMenu: any,
                private $q: any,
                private $mdToast: any,
                private $mdPanel: any,
                private OpsManagerJobService: any,
                private TableOptionsService: any,
                private PaginationDataService: any,
                private StateService: any,
                private IconService: any,
                private TabService: any,
                private AccessControlService: any,
                private BroadcastService: any,
                private OpsManagerRestUrlService:any){
         /**
         * Indicates that admin operations are allowed.
         * @type {boolean}
         */
        this.allowAdmin = false;
        if (this.hideFeedColumn == undefined) {
            this.hideFeedColumn = false;
        }

        this.pageName = angular.isDefined(this.pageName) ? this.pageName : 'jobs';
        //Page State
        this.loading = true;
        this.showProgress = true;

        //map of jobInstanceId to the Job
        this.jobIdMap = {};

        //Track active requests and be able to cancel them if needed
        this.activeJobRequests = [];

        //Track those Jobs who are refreshing because they are running
        this.timeoutMap = {};

        //Pagination and view Type (list or table)
        this.paginationData = PaginationDataService.paginationData(this.pageName);
        PaginationDataService.setRowsPerPageOptions(this.pageName, ['5', '10', '20', '50', '100']);
        this.viewType = PaginationDataService.viewType(this.pageName);

        //Setup the Tabs
        var tabNames = ['All', 'Running', 'Failed', 'Completed', 'Abandoned'] //, 'Stopped'];
        this.tabs = TabService.registerTabs(this.pageName, tabNames, this.paginationData.activeTab);
        this.tabMetadata = TabService.metadata(this.pageName);

        this.sortOptions = this.loadSortOptions();

        this.abandonAllMenuOption = {};

        this.additionalMenuOptions = this.loadAdditionalMenuOptions();

        this.selectedAdditionalMenuOptionVar = this.selectedAdditionalMenuOption;

        var loaded = false;
        /**
         * The filter supplied in the page
         * @type {string}
         */
        this.filter = angular.isUndefined(this.filter) ? '' : this.filter;

        this.tab = angular.isUndefined(this.tab) ? '' : this.tab;


        BroadcastService.subscribe($scope, 'ABANDONED_ALL_JOBS', this.updateJobs);

       

        $scope.$watch(()=> {
            return this.viewType;
        }, (newVal: any)=> {
            this.onViewTypeChange(newVal);
        });


        $scope.$watch(()=> {
            return this.filter;
        }, (newVal: any, oldVal: any)=> {
            if (newVal != oldVal) {
                console.log('filter changed ',newVal,oldVal)
                return this.loadJobs(true).promise;
            }
        });

        this.filterHelpOperators = [];
        this.filterHelpFields = []
        this.filterHelpExamples = [];
        this.filterHelpOperators.push(this.newHelpItem("Equals", "=="));
        this.filterHelpOperators.push(this.newHelpItem("Like condition", "=~"));
        this.filterHelpOperators.push(this.newHelpItem("In Clause", "Comma separated surrounded with quote    ==\"value1,value2\"   "));
        this.filterHelpOperators.push(this.newHelpItem("Greater than, less than", ">,>=,<,<="));
        this.filterHelpOperators.push(this.newHelpItem("Multiple Filters", "Filers separated by a comma    field1==value,field2==value  "));

        this.filterHelpFields.push(this.newHelpItem("Filter on a feed name", "feed"));
        this.filterHelpFields.push(this.newHelpItem("Filter on a job name", "job"));
        this.filterHelpFields.push(this.newHelpItem("Filter on a job start time", "jobStartTime"));
        this.filterHelpFields.push(this.newHelpItem("Filter on a job end time", "jobEndTime"));
        this.filterHelpFields.push(this.newHelpItem("Filter on a job id", "executionId"));
        this.filterHelpFields.push(this.newHelpItem("Start time date part filters", "startYear,startMonth,startDay"));
        this.filterHelpFields.push(this.newHelpItem("End time date part filters", "endYear,endMonth,endDay"));

        this.filterHelpExamples.push(this.newHelpItem("Find job names that equal 'my.job1' ", "job==my.job1"));
        this.filterHelpExamples.push(this.newHelpItem("Find job names starting with 'my' ", "job=~my"));
        this.filterHelpExamples.push(this.newHelpItem("Find jobs for 'my.job1' or 'my.job2' ", "job==\"my.job1,my.job2\""));
        this.filterHelpExamples.push(this.newHelpItem("Find 'my.job1' starting in 2017 ", "job==my.job1,startYear==2017"));
        this.filterHelpExamples.push(this.newHelpItem("Find jobs that started on February 1st 2017", "startTime>=2017-02-01,startTime<2017-02-02"));

        $scope.$on('$destroy', ()=> {
            this.clearAllTimeouts();
        });

        // Fetch allowed permissions
        AccessControlService.getUserAllowedActions()
                .then((actionSet: any)=> {
                    this.allowAdmin = AccessControlService.hasAction(AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
                });

        if(this.tab != ''){
            var index = _.indexOf(tabNames,this.tab);
            if(index >=0){
                this.tabMetadata.selectedIndex = index;
            }
        }   

    }// end of constructor
        updateJobs=()=> {
            this.loadJobs(true);
        }
         paginationId = (tab: any)=> {
            return this.PaginationDataService.paginationId(this.pageName, tab.title);
        }
        currentPage = (tab: any)=> {
            return this.PaginationDataService.currentPage(this.pageName, tab.title);
        }


        onViewTypeChange = (viewType: any)=> {
            this.PaginationDataService.viewType(this.pageName, this.viewType);
        }

        //Tab Functions

        onTabSelected = (tab: any)=> {
            this.TabService.selectedTab(this.pageName, tab);
            return this.loadJobs(true).promise;
        };

        onOrderChange = (order: any)=> {
            this.PaginationDataService.sort(this.pageName, order);
            this.TableOptionsService.setSortOption(this.pageName, order);
            return this.loadJobs(true).promise;
            //return this.deferred.promise;
        };

        onPaginationChange = (page: any, limit: any)=> {
            var activeTab= this.TabService.getActiveTab(this.pageName);
            //only trigger the reload if the initial page has been loaded.
            //md-data-table will call this function when the page initially loads and we dont want to have it run the query again.\
            //on load the query will be triggered via onTabSelected() method
            if(this.loaded) {
                activeTab.currentPage = page;
                this.PaginationDataService.currentPage(this.pageName, activeTab.title, page);
                return this.loadJobs(true).promise;
            }
        };

        /**
         * Build the possible Sorting Options
         * @returns {*[]}
         */
        loadSortOptions=()=> {
            var options = {'Job Name': 'jobName', 'Start Time': 'startTime', 'Status': 'status'};

            var sortOptions = this.TableOptionsService.newSortOptions(this.pageName, options, 'startTime', 'desc');
            var currentOption = this.TableOptionsService.getCurrentSort(this.pageName);
            if (currentOption) {
                this.TableOptionsService.saveSortOption(this.pageName, currentOption)
            }
            return sortOptions;
        }

        /**
         * Loads the additional menu options that appear in the more_vert options
         * @returns {Array}
         */
        loadAdditionalMenuOptions=()=> {
            var options = [];
            if (this.feed) {
                //only show the abandon all on the feeds page that are unhealthy
                options.push(this.TableOptionsService.newOption("Actions", 'actions_header', true, false))
                options.push(this.TableOptionsService.newOption("Abandon All", 'abandon_all', false, false));
            }
            return options;
        }

        selectedAdditionalMenuOption=(item: any)=> {
            if (item.type == 'abandon_all') {
                this.$mdMenu.hide();
                this.$mdDialog.show({
                    controller:"AbandonAllJobsDialogController",
                    templateUrl: 'js/ops-mgr/jobs/abandon-all-jobs-dialog.html',
                    parent: angular.element(document.body),
                    clickOutsideToClose: false,
                    fullscreen: true,
                    locals: {
                        feedName: this.feedFilter
                    }
                });

                this.OpsManagerJobService.abandonAllJobs(this.feedFilter, ()=> {
                    this.$mdDialog.hide();
                    this.BroadcastService.notify('ABANDONED_ALL_JOBS', {feed: this.feedFilter});
                    this.$mdToast.show(
                        this.$mdToast.simple()
                            .textContent('Abandoned all failed jobs for the feed')
                            .hideDelay(3000)
                    );
                },(err: any)=>{
                    this.$mdDialog.hide();
                    this.$mdToast.show(
                        this.$mdToast.simple()
                            .textContent('Unable to abandonal all jobs for the feed.  A unexpected error occurred.')
                            .hideDelay(3000)
                    );

                })
            }
        }

        /**
         *
         * @param options
         */
        onOptionsMenuOpen = (options: any)=> {
            if (this.feed) {
                var abandonOption = _.find(options.additionalOptions, (option: any)=> {
                    return option.type == 'abandon_all';
                });
                if (abandonOption != null && abandonOption != undefined) {
                    abandonOption.disabled = this.feed.healthText != 'UNHEALTHY';
                }
            }
        }

        /**
         * Called when a user Clicks on a table Option
         * @param option
         */
        selectedTableOption = (option: any)=> {
            var sortString = this.TableOptionsService.toSortString(option);
            this.PaginationDataService.sort(this.pageName, sortString);
            var updatedOption = this.TableOptionsService.toggleSort(this.pageName, option);
            this.TableOptionsService.setSortOption(this.pageName, sortString);
            this.loadJobs(true);
        }

        //Load Jobs

        loadJobs=(force: any) =>{
            if (force || !this.refreshing) {

                if (force) {
                    angular.forEach(this.activeJobRequests, (canceler: any, i: any)=> {
                        canceler.resolve();
                    });
                    this.activeJobRequests = [];
                }
                this.clearAllTimeouts();
                var activeTab = this.TabService.getActiveTab(this.pageName);

                this.showProgress = true;
                var sortOptions = '';
                var tabTitle = activeTab.title;
                var filters = {tabTitle: tabTitle};
                var limit = this.paginationData.rowsPerPage;

                var start = (limit * activeTab.currentPage) - limit; //this.query.page(this.selectedTab));

                var sort = this.PaginationDataService.sort(this.pageName);
                var canceler = this.$q.defer();
                var transformJobs = (response: any)=> {
                    //transform the data for UI
                    this.transformJobData(tabTitle, response.data.data);
                    this.TabService.setTotal(this.pageName, tabTitle, response.data.recordsFiltered)

                    if (this.loading) {
                        this.loading = false;
                    }

                    this.finishedRequest(canceler);

                };
                var successFn = (response: any)=> {
                    if (response.data) {
                        this.fetchFeedNames(response).then(transformJobs);
                    }
                };
                var errorFn = (err: any)=> {
                    this.finishedRequest(canceler);
                };
                this.activeJobRequests.push(canceler);
                this.deferred = canceler;
                this.promise = this.deferred.promise;
                var filter = this.filter;

                var params = {start: start, limit: limit, sort: sort, filter:filter};
                if (this.feedFilter) {
                    if(!params.filter){
                        params.filter = '';
                    }
                    if(params.filter != ''){
                        params.filter +=',';
                    }
                    params.filter += "jobInstance.feed.name=="+this.feedFilter;
                }
                //if the filter doesnt contain an operator, then default it to look for the job name
                if (params.filter != '' && params.filter != null && !this.containsFilterOperator(params.filter)) {
                    params.filter = 'job=~%' + params.filter;
                }


                var query = tabTitle != 'All' ? tabTitle.toLowerCase() : '';

                this.$http.get(this.OpsManagerJobService.JOBS_QUERY_URL + "/" + query, {timeout: canceler.promise, params: params}).then(successFn, errorFn);
            }
            this.showProgress = true;

            return this.deferred;

        }

        fetchFeedNames = (response:any) => {
            var deferred = this.$q.defer();

            this.showProgress = true;
            var jobs = response.data.data;
            if (jobs.length > 0) {
                //_.uniq method to remove duplicates, there may be multiple jobs for the same feed
                var feedNames = _.uniq(_.map(jobs, (job:any) => {
                    return job.feedName;
                }));
                var namesPromise = this.$http.post(this.OpsManagerRestUrlService.FEED_SYSTEM_NAMES_TO_DISPLAY_NAMES_URL, feedNames);
                namesPromise.then((result:any) => {
                    _.each(jobs, (job:any) => {
                        job.displayName = _.find(result.data, (systemNameToDisplayName) => {
                            return systemNameToDisplayName.key === job.feedName;
                        })
                    });
                    this.showProgress = false;
                    deferred.resolve(response);
                }, (err:any) => {
                    console.error('Failed to receive feed names', err);
                    this.showProgress = false;
                    deferred.resolve(response);
                });
            } else {
                deferred.resolve(response);
            }

            return deferred.promise;
        };

        containsFilterOperator=(filterStr: any)=> {
            var contains = false;
            var ops = ['==', '>', '<', '>=', '<=', '=~']
            for (var i = 0; i < ops.length; i++) {
                contains = filterStr.indexOf(ops[i]) >= 0;
                if (contains) {
                    break;
                }
            }
            return contains;
        }

        updateJob=(instanceId: any, newJob: any)=> {
            this.clearErrorMessage(instanceId);
            this.getRunningJobExecutionData(instanceId, newJob.executionId);

        }

        clearErrorMessage=(instanceId: any)=> {
            var existingJob = this.jobIdMap[instanceId];
            if (existingJob) {
                existingJob.errorMessage = '';
            }
        }

        addJobErrorMessage=(instanceId: any, message: any) =>{
            var existingJob = this.jobIdMap[instanceId];
            if (existingJob) {
                existingJob.errorMessage = message;
            }
        }

        finishedRequest=(canceler: any) =>{
            var index = _.indexOf(this.activeJobRequests, canceler);
            if (index >= 0) {
                this.activeJobRequests.splice(index, 1);
            }
            canceler.resolve();
            canceler = null;
            this.refreshing = false;
            this.showProgress = false;
            this.loaded = true;
        }

        transformJobData=(tabTitle: any, jobs: any) =>{
            //first clear out the arrays
            this.jobIdMap = {};
            this.TabService.clearTabs(this.pageName);
            angular.forEach(jobs, (job: any, i: any)=> {
                var transformedJob = this.transformJob(job);
                this.TabService.addContent(this.pageName, tabTitle, transformedJob);
            });
            return jobs;

        }

        clearAllTimeouts=()=> {
            angular.forEach(this.timeoutMap, (timeoutInstance: any, instanceId: any)=> {
                                                this.$timeout.cancel(timeoutInstance);
                                                delete this.timeoutMap[instanceId];
            });
            this.timeoutMap = {};

        }

        clearRefreshTimeout=(instanceId: any)=> {
            var timeoutInstance = this.timeoutMap[instanceId];
            if (timeoutInstance) {
                this.$timeout.cancel(timeoutInstance);
                delete this.timeoutMap[instanceId];
            }
        }

        transformJob=(job: any)=> {
            job.errorMessage = '';
            var executionId = job.executionId;
            var instanceId = job.instanceId;

            job.icon = this.IconService.iconForJobStatus(job.displayStatus);

            if (this.jobIdMap[job.instanceId] == undefined) {
                this.jobIdMap[job.instanceId] = job;
            }
            else {
                angular.extend(this.jobIdMap[job.instanceId], job);
            }

            var shouldRefresh = false;
            if (job.status == 'STARTING' || job.status == 'STARTED' || job.status == 'STOPPING') {
                shouldRefresh = true;
            }

            var wasRefreshing = this.timeoutMap[instanceId];
            if (!shouldRefresh) {
                this.$timeout(()=> {
                    this.triggerJobActionListener("updateEnd", job);
                }, 10);
            }

            this.clearRefreshTimeout(instanceId);

            //Refresh the Job Row if needed
            if (shouldRefresh) {
                this.timeoutMap[instanceId] = this.$timeout(()=> {
                    this.getRunningJobExecutionData(instanceId, executionId)
                }, 1000);
            }

            return job;
        }

        //Util Functions
        capitalize=(string: string)=> {
            return string.charAt(0).toUpperCase() + string.substring(1).toLowerCase();
        }

        jobDetails = (event: any, job: any)=> {
            if(job.stream){
                this.StateService.OpsManager().Feed().navigateToFeedStats(job.jobName);
            }else {
                this.StateService.OpsManager().Job().navigateToJobDetails(job.executionId);
            }
        }

        getRunningJobExecutionData = (instanceId: any, executionId: any)=> {
            var successFn = (response: any)=> {
                this.transformJob(response.data);
                this.triggerJobActionListener("updated", response.data)
            };

            this.$http.get(this.OpsManagerJobService.LOAD_JOB_URL(executionId)).then(successFn);
        }

        triggerJobActionListener=(action: any, job: any)=> {
            if (this.onJobAction && angular.isFunction(this.onJobAction)) {
                this.onJobAction({action: action, job: job});
            }
        }

        restartJob = (event: any, job: any)=> {
            event.stopPropagation();
            event.preventDefault();
            var executionId = job.executionId;
            var instanceId = job.instanceId;
            this.clearRefreshTimeout(instanceId);
            this.triggerJobActionListener('restartJob', job);
            var xhr = this.OpsManagerJobService.restartJob(job.executionId, {}, (response: any)=> {
                this.updateJob(instanceId, response.data)
                //  getRunningJobExecutionData(instanceId,data.executionId);
            }, (errMsg: any)=> {
                this.addJobErrorMessage(executionId, errMsg);
            });
        };

        stopJob = (event: any, job: any)=> {
            event.stopPropagation();
            event.preventDefault();
            var instanceId = job.instanceId;
            this.clearRefreshTimeout(instanceId);
            this.triggerJobActionListener('stopJob', job);
            this.OpsManagerJobService.stopJob(job.executionId, {}, (response: any)=> {
                this.updateJob(instanceId, response.data)
                //  getRunningJobExecutionData(instanceId,data.executionId);
            })
        };

        abandonJob = (event: any, job: any)=> {
            event.stopPropagation();
            event.preventDefault();
            var instanceId = job.instanceId;
            this.clearRefreshTimeout(instanceId);
            this.triggerJobActionListener('abandonJob', job);
            this.OpsManagerJobService.abandonJob(job.executionId, {}, (response: any)=> {
                this.updateJob(instanceId, response.data)
                this.triggerJobActionListener('abandonJob', response.data);
            })
        };

        failJob = (event: any, job: any)=>  {
            event.stopPropagation();
            event.preventDefault();
            var instanceId = job.executionId;
            this.clearRefreshTimeout(instanceId);
            this.triggerJobActionListener('failJob', job);
            this.OpsManagerJobService.failJob(job.executionId, {}, (response: any)=> {
                this.updateJob(instanceId, response.data)
                this.triggerJobActionListener('failJob', response.data);
            })
        };

        newHelpItem = (label: any, description: any)=> {
            return {displayName: label, description: description};
        }

        showFilterHelpPanel = (ev: any)=> {
            var position = this.$mdPanel.newPanelPosition()
                .relativeTo('.filter-help-button')
                .addPanelPosition(this.$mdPanel.xPosition.ALIGN_END, this.$mdPanel.yPosition.BELOW);

            var config = {
                attachTo: angular.element(document.body),
                controller: 'JobFilterHelpPanelMenuCtrl',
                controllerAs: 'ctrl',
                templateUrl: 'js/ops-mgr/jobs/jobs-filter-help-template.html',
                panelClass: 'filter-help',
                position: position,
                locals: {
                    'filterHelpExamples': this.filterHelpExamples,
                    'filterHelpOperators': this.filterHelpOperators,
                    'filterHelpFields': this.filterHelpFields
                },
                openFrom: ev,
                clickOutsideToClose: true,
                escapeToClose: true,
                focusOnOpen: false,
                zIndex: 2
            };

            this.$mdPanel.open(config);
        };

}

export class JobFilterHelpPanelMenuCtrl implements ng.IComponentController{
    _mdPanelRef: any;
    constructor(private mdPanelRef:any){
        this._mdPanelRef = mdPanelRef;
    }
}
/**
     * The Controller used for the abandon all
     */
export class abandonAllDialogController implements ng.IComponentController{
    counter: any;
    index: any;
    messages: any[];
    messageInterval: any;
    constructor(private $scope: any,
                private $mdDialog: any,
                private $interval: any,
                private feedName: any){
        $scope.feedName = feedName;
        $scope.message = "Abandoning the failed jobs for "+feedName;
        this.counter = 0;
        this.index = 0;
        this.messages = [];
        this.messages.push("Still working. Abandoning the failed jobs for "+feedName);
        this.messages.push("Hang tight. Still working.")
        this.messages.push("Just a little while longer.")
        this.messages.push("Should be done soon.")
        this.messages.push("Still working.  Almost done.")
        this.messages.push("It's taking longer than expected.  Should be done soon.")
        this.messages.push("It's taking longer than expected.  Still working...")
        this.messageInterval = $interval(()=> {this.updateMessage();},5000);

        $scope.hide = ()=> {
            this.cancelMessageInterval();
            $mdDialog.hide();

        };

        $scope.cancel = ()=> {
            this.cancelMessageInterval();
            $mdDialog.cancel();
        };
    }
    
        updateMessage=()=>{
            this.counter++;
            var len = this.messages.length;
            if(this.counter %2 == 0 && this.counter >2) {
                this.index = this.index < (len-1) ? this.index+1 : this.index;
            }
            this.$scope.message = this.messages[this.index];
        }
        
        cancelMessageInterval=()=>{
            if(this.messageInterval != null) {
                this.$interval.cancel(this.messageInterval);
            }
        }
}

angular.module(moduleName).controller('AbandonAllJobsDialogController', ["$scope","$mdDialog","$interval","feedName",abandonAllDialogController]);

angular.module(moduleName).controller("JobFilterHelpPanelMenuCtrl", ["mdPanelRef",JobFilterHelpPanelMenuCtrl]);

angular.module(moduleName)
.controller("JobsCardController", 
            ["$scope","$http","$mdDialog","$timeout","$mdMenu","$q","$mdToast",
             "$mdPanel","OpsManagerJobService","TableOptionsService",
             "PaginationDataService","StateService","IconService","TabService",
             "AccessControlService","BroadcastService","OpsManagerRestUrlService",JobsCardController]);
angular.module(moduleName).directive('tbaJobs', [
    ()=>
    {
        return {
            restrict: "EA",
            bindToController: {
                cardTitle: "@",
                refreshIntervalTime: "@",
                pageName: '@',
                feedFilter: '=',
                onJobAction: '&',
                hideFeedColumn: '=?',
                feed: '=?',
                filter:'=?',
                tab:'=?'
            },
            controllerAs: 'vm',
            scope: true,
            templateUrl: 'js/ops-mgr/jobs/jobs-template.html',
            controller: "JobsCardController",
            link: function($scope: any, element: any, attrs: any, controller: any) {

            }
        };
    }
]);
