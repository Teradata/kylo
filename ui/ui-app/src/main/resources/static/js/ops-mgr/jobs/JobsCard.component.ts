import "pascalprecht.translate";
import * as _ from 'underscore';
import {TabService} from "../../services/tab.service";
import {IconService} from "../services/IconStatusService";
import {AccessControlService} from "../../services/AccessControlService";
import { Component, Output, Input, OnChanges, SimpleChanges, Inject, EventEmitter } from "@angular/core";
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import {BroadcastService} from "../../services/broadcast-service";
import {OpsManagerJobService} from "../services/ops-manager-jobs.service";
import {OpsManagerRestUrlService} from "../services/OpsManagerRestUrlService";
import {StateService} from "../../services/StateService";
import { DefaultPaginationDataService } from "../../services/PaginationDataService";
import { DefaultTableOptionsService } from "../../services/TableOptionsService";
import { TdDataTableSortingOrder, ITdDataTableColumn, TdDataTableService, ITdDataTableSortChangeEvent } from "@covalent/core/data-table";
import { IPageChangeEvent } from "@covalent/core/paging";
import { Subject } from "rxjs/Subject";
import { MatSnackBar } from "@angular/material/snack-bar";
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import 'rxjs/add/operator/timeout';
import { BaseFilteredPaginatedTableView } from "../../common/filtered-paginated-table-view/BaseFilteredPaginatedTableView";
import { ObjectUtils } from "../../../lib/common/utils/object-utils";
import { Subscription } from "rxjs";

@Component({
    selector: 'tba-jobs',
    templateUrl: './jobs-template.html'
})
export class JobsCardComponent extends BaseFilteredPaginatedTableView {

    allowAdmin: boolean = false;
    loading: boolean = true;
    showProgress: boolean = true;
    jobIdMap: any;
    timeoutMap: any;
    paginationData: any;
    tabs: any;
    tabMetadata: any;
    sortOptions: any;
    abandonAllMenuOption: any;
    additionalMenuOptions: any;
    selectedAdditionalMenuOptionVar: any;
    loaded: boolean =false;
    total: number = 0;
    page: number = 1;
    refreshing: boolean;

    activeJobRequests: Subscription[] = [];

    filterHelpOperators: any[];
    filterHelpFields: any[];
    filterHelpExamples: any[];

    @Input() filterJob: any;

    @Input() tab: any;
    @Input() cardTitle: any;
    @Input() refreshIntervalTime: any;
    @Input() feed: any;
    @Input() feedFilter: any;
    @Input() hideFeedColumn: any = false;
    @Input() pageName: any;

    @Output() onJobAction: EventEmitter<any> = new EventEmitter<any>();

    constructor(private http: HttpClient,
        private dialog: MatDialog,
        private snackBar: MatSnackBar,
        private OpsManagerJobService: OpsManagerJobService,
        private TableOptionsService: DefaultTableOptionsService,
        private PaginationDataService: DefaultPaginationDataService,
        private StateService: StateService,
        private IconService: IconService,
        private TabService: TabService,
        private accessControlService: AccessControlService,
        private BroadcastService: BroadcastService,
        private OpsManagerRestUrlService: OpsManagerRestUrlService,
        public _dataTableService: TdDataTableService) {
            super(_dataTableService);
    }

    ngOnInit() {

        this.pageName = ObjectUtils.isDefined(this.pageName) ? this.pageName : 'jobs';
        //map of jobInstanceId to the Job
        this.jobIdMap = {};
        //Track those Jobs who are refreshing because they are running
        this.timeoutMap = {};
        //Pagination and view Type (list or table)
        this.paginationData = this.PaginationDataService.paginationData(this.pageName);
        //Setup the Tabs
        var tabNames = ['All', 'Running', 'Failed', 'Completed', 'Abandoned'] //, 'Stopped'];
        this.tabs = this.TabService.registerTabs(this.pageName, tabNames, this.paginationData.activeTab);

        this.tabMetadata = this.TabService.metadata(this.pageName);
        
        this.abandonAllMenuOption = {};

        this.additionalMenuOptions = this.loadAdditionalMenuOptions();

        this.selectedAdditionalMenuOptionVar = this.selectedAdditionalMenuOption;

        /**
         * The filter supplied in the page
         * @type {string}
         */
        this.filterJob = ObjectUtils.isUndefined(this.filterJob) ? '' : this.filterJob;

        this.tab = ObjectUtils.isUndefined(this.tab) ? '' : this.tab;

        this.BroadcastService.subscribe(null, 'ABANDONED_ALL_JOBS', this.updateJobs);
        if (this.hideFeedColumn) {
            this.columns.splice(2, 1);
        }
        this.loadJobs(true);

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


        // Fetch allowed permissions
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowAdmin = this.accessControlService.hasAction(AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
            });

        if (this.tab != '') {
            var index = _.indexOf(tabNames, this.tab);
            if (index >= 0) {
                this.tabMetadata.selectedIndex = index;
            }
            let tabIndex = tabNames.indexOf(this.tab);
            if (tabIndex > -1) {
                this.onTabSelected(tabIndex);
            }
        }
    }

    ngOnDestroy(): void {
        this.clearAllTimeouts();
    }

    columns: ITdDataTableColumn[] = [
        { name: 'jobName', label: 'Job Name', sortable: true, filter: true },
        { name: 'state', label: 'State', sortable: false, filter: false },
        { name: 'feedName', label: 'Feed', sortable: true, filter: true },
        { name: 'startTime', label: 'Start Time', sortable: true, filter: true },
        { name: 'runTime', label: 'Run Time', sortable: false, filter: false },
        { name: 'status', label: 'Status', sortable: true, filter: true },
        { name: 'action', label: 'Action', sortable: false, filter: false }
    ];

    updateJobs() {
        this.loadJobs(true);
    }

    //Tab Functions
    onTabSelected(tab: any) {
        this.TabService.selectedTab(this.pageName, this.tabs[tab]);
        return this.loadJobs(true);
    };

    /**
     * Loads the additional menu options that appear in the more_vert options
     * @returns {Array}
     */
    loadAdditionalMenuOptions() {
        var options = [];
        if (this.feed) {
            //only show the abandon all on the feeds page that are unhealthy
            options.push(this.TableOptionsService.newOption("Actions", 'actions_header', true, false))
            options.push(this.TableOptionsService.newOption("Abandon All", 'abandon_all', false, false));
        }
        return options;
    }

    selectedAdditionalMenuOption(item: any) {
        if (item.type == 'abandon_all') {

            // this.$injector.get("$mdMenu").hide();

            let dialogRef = this.dialog.open(abandonAllDialogController, {
                data: { feedName: this.feedFilter },
                panelClass: "full-screen-dialog"
            });

            this.OpsManagerJobService.abandonAllJobs(this.feedFilter, () => {
                this.dialog.closeAll();
                this.BroadcastService.notify('ABANDONED_ALL_JOBS', { feed: this.feedFilter });
                this.snackBar.open("Abandoned all failed jobs for the feed", "OK", {
                    duration: 3000
                });
            }, (err: any) => {
                this.dialog.closeAll();
                this.snackBar.open("Unable to abandonal all jobs for the feed.  A unexpected error occurred.", "OK", {
                    duration: 3000
                });
            })
        }
    }

    /**
     *
     * @param options
     */
    onOptionsMenuOpen(options: any) {
        if (this.feed) {
            var abandonOption = _.find(options.additionalOptions, (option: any) => {
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
    selectedTableOption(option: any) {
        var sortString = this.TableOptionsService.toSortString(option);
        this.PaginationDataService.sort(this.pageName, sortString);
        var updatedOption = this.TableOptionsService.toggleSort(this.pageName, option);
        this.TableOptionsService.setSortOption(this.pageName, sortString);
        this.loadJobs(true);
    }

    //Load Jobs

    loadJobs(force: any) {
        if (force || !this.refreshing) {
            if (force) {
                this.activeJobRequests.forEach((canceler: Subscription) => {
                    canceler.unsubscribe();
                });
                this.activeJobRequests = [];
            }
            this.clearAllTimeouts();
            var activeTab = this.TabService.getActiveTab(this.pageName);

            this.showProgress = true;
            var sortOptions = '';
            var tabTitle = activeTab.title;
            var filters = { tabTitle: tabTitle };
            var limit = this.paginationData.rowsPerPage;

            var start = (limit * activeTab.currentPage) - limit; //this.query.page(this.selectedTab));

            var sort = this.PaginationDataService.sort(this.pageName);
            var transformJobs = (response: any,canceler: Subscription) => {
                //transform the data for UI
                this.transformJobData(tabTitle, response);
                this.TabService.setTotal(this.pageName, tabTitle, response.recordsFiltered)

                if (this.loading) {
                    this.loading = false;
                }

                this.finishedRequest(canceler);

            };
            var successFn = (response: any, canceler : Subscription) => {
                if (response) {
                    this.total = response.recordsTotal;
                    this.fetchFeedNames(response.data).then((feeds:any) => transformJobs(feeds,canceler));
                }
            };
            var errorFn = (err: any, canceler : Subscription) => {
                this.finishedRequest(canceler);
            };
            var filter = this.feedFilter ? "jobInstance.feed.name==" + this.feedFilter : this.filterJob;
            if(!this.containsFilterOperator(filter)) {
                filter = 'job=~%' + filter;
            }

            let params = new HttpParams();
            params = params.append('start', start.toString());
            params = params.append('limit', limit);
            params = params.append('sort', sort);
            params = params.append('filter', filter);

            var query = tabTitle != 'All' ? tabTitle.toLowerCase() : '';

            // {timeout: canceler.promise}
            var jobsSubscription = this.http.get(this.OpsManagerJobService.JOBS_QUERY_URL + "/" + query, { params: params })
                .subscribe(
                    (response)=> {successFn(response,jobsSubscription)},
                    (err : any) => {errorFn(err,jobsSubscription)}
                );
            this.activeJobRequests.push(jobsSubscription);
            this.showProgress = true;
        }
    }

    fetchFeedNames(response: any): Promise<any> {
        this.showProgress = true;
        var jobs = response;
        return new Promise((resolve, reject) => {
            if (jobs.length > 0) {
                //_.uniq method to remove duplicates, there may be multiple jobs for the same feed
                var feedNames = _.uniq(_.map(jobs, (job: any) => {
                    return job.feedName;
                }));
                this.http.post(this.OpsManagerRestUrlService.FEED_SYSTEM_NAMES_TO_DISPLAY_NAMES_URL, feedNames,
                    { headers: new HttpHeaders({ 'Content-Type': 'application/json; charset=utf-8' }) })
                    .toPromise().then((result: any) => {
                        _.each(jobs, (job: any) => {
                            job.displayName = _.find(result.data, (systemNameToDisplayName) => {
                                return systemNameToDisplayName.key === job.feedName;
                            })
                        });
                        this.showProgress = false;
                        resolve(response);
                    }, (err: any) => {
                        console.error('Failed to receive feed names', err);
                        this.showProgress = false;
                        resolve(response);
                    });
            } else {
                resolve(response);
            }
        })
    };

    containsFilterOperator(filterStr: any) {
        var contains = false;
        var ops = ['==', '>', '<', '>=', '<=', '=~']
        if(filterStr != null) {
            for (var i = 0; i < ops.length; i++) {
                contains = filterStr.indexOf(ops[i]) >= 0;
                if (contains) {
                    break;
                }
            }
        }
        return contains;
    }

    updateJob(instanceId: any, newJob: any) {
        this.clearErrorMessage(instanceId);
        this.getRunningJobExecutionData(instanceId, newJob.executionId);

    }

    clearErrorMessage(instanceId: any) {
        var existingJob = this.jobIdMap[instanceId];
        if (existingJob) {
            existingJob.errorMessage = '';
        }
    }

    addJobErrorMessage(instanceId: any, message: any) {
        var existingJob = this.jobIdMap[instanceId];
        if (existingJob) {
            existingJob.errorMessage = message;
        }
    }

    finishedRequest(canceler : Subscription) {
        var index = _.indexOf(this.activeJobRequests, canceler);
        if (index >= 0) {
            canceler.unsubscribe();
            this.activeJobRequests.splice(index, 1);
        }
        this.refreshing = false;
        this.showProgress = false;
        this.loaded = true;
    }

    transformJobData(tabTitle: any, jobs: any) {
        //first clear out the arrays
        this.jobIdMap = {};
        this.TabService.clearTabs(this.pageName);
        _.each(jobs, (job: any, i: any) => {
            var transformedJob = this.transformJob(job);
            this.TabService.addContent(this.pageName, tabTitle, transformedJob);
        });

        super.setSortBy('jobName');
        super.setDataAndColumnSchema(this.TabService.getActiveTab(this.pageName).data.content, this.columns);
        super.filter();

        return jobs;

    }

    onChangeLinks (page: any) {
        this.loaded = false;
        this.TabService.getActiveTab(this.pageName).currentPage = page;
        this.loadJobs(true);
    }

    onPaginationChange (pagingEvent: IPageChangeEvent) {

        if(this.page != pagingEvent.page) {
            this.page = pagingEvent.page;
            this.onChangeLinks(pagingEvent.page);
        }
        else {
            this.paginationData.rowsPerPage = pagingEvent.pageSize;
            this.loadJobs(true);
            this.onPageSizeChange(pagingEvent);
        }

    }

    onSearchTable (searchTerm: string) {
        this.filterJob = searchTerm;
        this.loadJobs(true);
    }

    clearAllTimeouts() {
        if (this.timeoutMap) {
            Object.keys(this.timeoutMap).forEach((instanceId: any) => {
                clearTimeout(this.timeoutMap[instanceId]);
                delete this.timeoutMap[instanceId];
            });
        }
        this.timeoutMap = {};

    }

    clearRefreshTimeout(instanceId: any) {
        var timeoutInstance = this.timeoutMap[instanceId];
        if (timeoutInstance) {
            clearTimeout(timeoutInstance);
            delete this.timeoutMap[instanceId];
        }
    }

    transformJob(job: any) {
        job.errorMessage = '';
        var executionId = job.executionId;
        var instanceId = job.instanceId;

        job.icon = this.IconService.iconForJobStatus(job.displayStatus);

        if (this.jobIdMap[job.instanceId] == undefined) {
            this.jobIdMap[job.instanceId] = job;
        }
        else {
            this.jobIdMap[job.instanceId] = _.extend(this.jobIdMap[job.instanceId], job);
        }

        var shouldRefresh = false;
        if (job.status == 'STARTING' || job.status == 'STARTED' || job.status == 'STOPPING') {
            shouldRefresh = true;
        }

        var wasRefreshing = this.timeoutMap[instanceId];
        if (!shouldRefresh) {
            setTimeout(() => { this.triggerJobActionListener("updateEnd", job); }, 10);
        }

        this.clearRefreshTimeout(instanceId);

        //Refresh the Job Row if needed
        if (shouldRefresh) {
            this.timeoutMap[instanceId] = setTimeout(() => {
                this.getRunningJobExecutionData(instanceId, executionId)
            }, 1000);
        }

        return job;
    }

    //Util Functions
    capitalize(string: string) {
        return string.charAt(0).toUpperCase() + string.substring(1).toLowerCase();
    }

    jobDetails(event: any) {
        if (event.row.stream) {
            this.StateService.OpsManager().Feed().navigateToFeedStats(event.row.jobName);
        } else {
            this.StateService.OpsManager().Job().navigateToJobDetails(event.row.executionId);
        }
    }

    getRunningJobExecutionData(instanceId: any, executionId: any) {
        var successFn = (response: any) => {
            this.transformJob(response);
            this.triggerJobActionListener("updated", response)
        };

        this.http.get(this.OpsManagerJobService.LOAD_JOB_URL(executionId)).toPromise().then(successFn);
    }

    triggerJobActionListener(action: any, job: any) {
        this.onJobAction.emit({ action: action, job: job });
    }

    restartJob(event: any, job: any) {
        event.stopPropagation();
        event.preventDefault();
        var executionId = job.executionId;
        var instanceId = job.instanceId;
        this.clearRefreshTimeout(instanceId);
        this.triggerJobActionListener('restartJob', job);
        var xhr = this.OpsManagerJobService.restartJob(job.executionId, {}, (response: any) => {
            this.updateJob(instanceId, response)
            //  getRunningJobExecutionData(instanceId,data.executionId);
        }, (errMsg: any) => {
            this.addJobErrorMessage(executionId, errMsg);
        });
    };

    stopJob(event: any, job: any) {
        event.stopPropagation();
        event.preventDefault();
        var instanceId = job.instanceId;
        this.clearRefreshTimeout(instanceId);
        this.triggerJobActionListener('stopJob', job);
        this.OpsManagerJobService.stopJob(job.executionId, {}, (response: any) => {
            this.updateJob(instanceId, response)
            //  getRunningJobExecutionData(instanceId,data.executionId);
        })
    };

    abandonJob(event: any, job: any) {
        event.stopPropagation();
        event.preventDefault();
        var instanceId = job.instanceId;
        this.clearRefreshTimeout(instanceId);
        this.triggerJobActionListener('abandonJob', job);
        this.OpsManagerJobService.abandonJob(job.executionId, {}, (response: any) => {
            this.updateJob(instanceId, response)
            this.triggerJobActionListener('abandonJob', response);
        })
    };

    failJob(event: any, job: any) {
        event.stopPropagation();
        event.preventDefault();
        var instanceId = job.executionId;
        this.clearRefreshTimeout(instanceId);
        this.triggerJobActionListener('failJob', job);
        this.OpsManagerJobService.failJob(job.executionId, {}, (response: any) => {
            this.updateJob(instanceId, response)
            this.triggerJobActionListener('failJob', response);
        })
    };

    newHelpItem(label: any, description: any) {
        return { displayName: label, description: description };
    }

    showFilterHelpPanel(ev: any) {

        var position = $(".filter-help-button").position();

        let dialogRef = this.dialog.open(JobFilterHelpPanelMenuCtrl, {
            data: {
                filterHelpExamples: this.filterHelpExamples,
                filterHelpOperators: this.filterHelpOperators,
                filterHelpFields: this.filterHelpFields
            },
            panelClass: "filter-help",
            position: {
                'top': (position.top + 120).toString() + 'px',
                'left': (position.left - 80).toString() + 'px'
            },
            closeOnNavigation: true,
            autoFocus: true,
            backdropClass: "filter-help-backdrop"
        });

    };
}

@Component({
    selector: 'job-filter-help-panel-menu-ctrl',
    templateUrl: './jobs-filter-help-template.html',
    styles: [`
        .filter-help-backdrop {
            background: transparent;
        }
    `]
})
export class JobFilterHelpPanelMenuCtrl implements ng.IComponentController {

    filterHelpExamples: any[];
    filterHelpOperators: any[];
    filterHelpFields: any[];

    ngOnInit() {
        this.filterHelpExamples = this.data.filterHelpExamples,
            this.filterHelpOperators = this.data.filterHelpOperators,
            this.filterHelpFields = this.data.filterHelpFields
    }
    constructor(private dialogRef: MatDialogRef<abandonAllDialogController>,
        @Inject(MAT_DIALOG_DATA) private data: any) { }
}
/**
 * The Controller used for the abandon all
 */
@Component({
    templateUrl: './abandon-all-jobs-dialog.html'
})
export class abandonAllDialogController implements ng.IComponentController {
    counter: number;
    index: number;
    messages: string[];
    messageInterval: number;

    feedName: string;
    message: string;

    ngOnInit() {
        this.feedName = this.data.feedName;
        this.message = "Abandoning the failed jobs for " + this.feedName;
        this.counter = 0;
        this.index = 0;
        this.messages = [];
        this.messages.push("Still working. Abandoning the failed jobs for " + this.feedName);
        this.messages.push("Hang tight. Still working.");
        this.messages.push("Just a little while longer.");
        this.messages.push("Should be done soon.");
        this.messages.push("Still working.  Almost done.");
        this.messages.push("It's taking longer than expected.  Should be done soon.");
        this.messages.push("It's taking longer than expected.  Still working...");
        this.messageInterval = setTimeout(() => { this.updateMessage(); }, 5000);
    }

    constructor(private dialogRef: MatDialogRef<abandonAllDialogController>,
        @Inject(MAT_DIALOG_DATA) private data: any) { }

    hide() {
        this.cancelMessageInterval();
        this.dialogRef.close();
    };

    cancel() {
        this.cancelMessageInterval();
        this.dialogRef.close();
    };

    updateMessage() {
        this.counter++;
        var len = this.messages.length;
        if (this.counter % 2 == 0 && this.counter > 2) {
            this.index = this.index < (len - 1) ? this.index + 1 : this.index;
        }
        this.message = this.messages[this.index];
    }

    cancelMessageInterval() {
        if (this.messageInterval != null) {
            clearInterval(this.messageInterval);
        }
    }
}
