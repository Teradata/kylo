import * as _ from 'underscore';
import {OpsManagerJobService} from "../../services/ops-manager-jobs.service";
import {TabService} from "../../../services/tab.service";
import {AccessControlService} from "../../../services/AccessControlService";
import {Component, EventEmitter, Inject, Input, Output} from "@angular/core";
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {BroadcastService} from "../../../services/broadcast-service";
import {StateService as  KyoStateService} from "../../../services/StateService";
import {DefaultPaginationDataService} from "../../../services/PaginationDataService";
import {ITdDataTableColumn, TdDataTableService} from "@covalent/core/data-table";
import {IPageChangeEvent} from "@covalent/core/paging";
import {MatSnackBar} from "@angular/material/snack-bar";
import 'rxjs/add/operator/timeout';
import {BaseFilteredPaginatedTableView} from "../../../common/filtered-paginated-table-view/BaseFilteredPaginatedTableView";
import {Subscription} from "rxjs";
import {OperationsRestUrlConstants} from "../../../services/operations-rest-url-constants";
import IconUtil from "../../../services/icon-util";
import {DateTimeService} from "../../../common/utils/date-time.service";
import {KyloIcons} from "../../../kylo-utils/kylo-icons";
import {OpsManagerFeedService} from "../../services/ops-manager-feed.service";
import {TdDialogService} from "@covalent/core/dialogs";
import {AbandonAllJobsDialogComponent} from "./abandon-all-jobs-dialog.component";
import {JobsFilterHelpPanelDialogComponent} from "./jobs-filter-help-panel-dialog.component";
import {FeedOperationsSummary} from "../../../feed-mgr/model/feed/feed-operations-summary.model";



@Component({
    selector: 'jobs-list',
    styleUrls:['./jobs-list.component.scss'],
    templateUrl: './jobs-list.component.html'
})
export class JobsListComponent extends BaseFilteredPaginatedTableView {

    allowAdmin: boolean = false;
    loading: boolean = true;
    showProgress: boolean = true;
    jobIdMap: any;
    timeoutMap: any;
    paginationData: any;
    tabs: any;
    tabMetadata: any;
    sortOptions: any;
    loaded: boolean =false;
    total: number = 0;
    page: number = 1;
    refreshing: boolean;

    activeJobRequests: Subscription[] = [];


    abandonAllDisabled:boolean = false;

    updateFeedHealth:any;

    cancelTimeouts:boolean = false;



    @Input() filterJob: any;

    @Input() tab: any;
    @Input() cardTitle: any;
    @Input() refreshIntervalTime: any;
    @Input() feed: FeedOperationsSummary;
    @Input() feedFilter: string;
    @Input() pageName: any;

    @Output() onJobAction: EventEmitter<any> = new EventEmitter<any>();


    kyloIcons = KyloIcons;
    columns: ITdDataTableColumn[] = [
        { name: 'startTime', label: 'Start Time', sortable: true, filter: true },
        { name: 'jobName', label: 'Job Name', sortable: true, filter: true, hidden: true  },
        { name: 'state', label: 'State', sortable: false, filter: false },
        { name: 'feedName', label: 'Feed', sortable: true, filter: true, hidden:true },

        { name: 'runTimeStr', label: 'Run Time', sortable: false, filter: false },
        // { name: 'status', label: 'Status', sortable: true, filter: true },
        { name: 'action', label: 'Action', sortable: false, filter: false }
    ];

    constructor(private http: HttpClient,
                private _dialogService: TdDialogService,
                private snackBar: MatSnackBar,
                private opsManagerJobService: OpsManagerJobService,
               private opsManagerFeedService:OpsManagerFeedService,
                private paginationDataService: DefaultPaginationDataService,
               private _dateTimeService:DateTimeService,
                  @Inject("StateService") private kyloStateService: KyoStateService,
              //  private IconService: IconService,
                private tabService: TabService,
                @Inject("AccessControlService") private accessControlService: AccessControlService,
                @Inject("BroadcastService") private broadcastService: BroadcastService,
                public _dataTableService: TdDataTableService) {
        super(_dataTableService);
    }

    fetchFeedHealth(){
        if(this.feedFilter) {
            this.opsManagerFeedService.getFeedHealth(this.feedFilter).subscribe((response: FeedOperationsSummary) => {
                if (response) {
                    this.feed = response;
                    this.abandonAllDisabled = this.feed.healthText != "UNHEALTHY"
                }
            });
            if(this.updateFeedHealth){
                clearTimeout(this.updateFeedHealth);
                this.updateFeedHealth = undefined;
            }
        }
    }

    ngOnInit() {
        if(this.feedFilter){
            //remove the feedName column
            let feedName = this.columns.find((col:ITdDataTableColumn) => col.name == "feedName" );
            if(feedName){
                feedName.hidden = true;
            }
        }
        if(this.feedFilter && !this.feed) {
           this.fetchFeedHealth();
        }


        this.pageName = !_.isUndefined(this.pageName) ? this.pageName : 'jobs';
        //map of jobInstanceId to the Job
        this.jobIdMap = {};
        //Track those Jobs who are refreshing because they are running
        this.timeoutMap = {};
        //Pagination and view Type (list or table)
        this.paginationData = this.paginationDataService.paginationData(this.pageName);

        this.paginationDataService.sort(this.pageName,"-startTime")
        //Setup the Tabs
        var tabNames = ['All', 'Running', 'Failed', 'Completed', 'Abandoned'] //, 'Stopped'];
        this.tabs = this.tabService.registerTabs(this.pageName, tabNames, this.paginationData.activeTab);
        this.tabMetadata = this.tabService.metadata(this.pageName);

        Object.keys(this.tabs).forEach((key: any) => {
            if(this.tabs[key].active)
                this.tabMetadata.selectedIndex = key;
        });

        /**
         * The filter supplied in the page
         * @type {string}
         */
        this.filterJob = _.isUndefined(this.filterJob) ? '' : this.filterJob;

        this.tab = _.isUndefined(this.tab) ? '' : this.tab;

        this.broadcastService.subscribe(null, 'ABANDONED_ALL_JOBS', this.updateJobs);

        this.loadJobs(true);

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
        this.cancelTimeouts = true;
        this.clearAllTimeouts();
    }



    updateJobs() {
        this.loadJobs(true);
    }

    //Tab Functions
    onTabSelected(tab: any) {
        this.tabService.selectedTab(this.pageName, this.tabs[tab]);
        return this.loadJobs(true);
    };



    abandonAllJobs() {

          let dialog =  this._dialogService.open(AbandonAllJobsDialogComponent, {
                data: { feedName: this.feedFilter },
                height:"500px",
              width:"500px"

            });
        this.opsManagerJobService.abandonAllJobs(this.feedFilter, () => {
            dialog.close();
            this.broadcastService.notify('ABANDONED_ALL_JOBS', { feed: this.feedFilter });
            this.snackBar.open("Abandoned all failed jobs for the feed", "OK", {
                duration: 3000
            });
            this.fetchFeedHealth();
            this.loadJobs();
        }, (err: any) => {
            dialog.close();
            this.snackBar.open("Unable to abandon all jobs for the feed.  A unexpected error occurred.", "OK", {
                duration: 3000
            });
        })


    }


    //Load Jobs

    loadJobs(force?: boolean) {

        if (force || !this.refreshing) {
            if (force) {
                this.activeJobRequests.forEach((canceler: Subscription) => {
                    canceler.unsubscribe();
                });
                this.activeJobRequests = [];
            }
            this.clearAllTimeouts();
            var activeTab = this.tabService.getActiveTab(this.pageName);
            this.showProgress = true;
            var sortOptions = '';
            var tabTitle = activeTab.title;
            var filters = { tabTitle: tabTitle };
            var limit = this.paginationData.rowsPerPage;

            var start = (limit * activeTab.currentPage) - limit; //this.query.page(this.selectedTab));

            var sort = this.paginationDataService.sort(this.pageName);
            var transformJobs = (response: any,canceler: Subscription) => {
                //transform the data for UI
                this.transformJobData(tabTitle, response);
                this.tabService.setTotal(this.pageName, tabTitle, response.recordsFiltered)

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
            var filter = this.filterJob;

            let params = new HttpParams();
            params = params.append('start', start.toString());
            params = params.append('limit', limit);
            params = params.append('sort', sort);
            params = params.append('filter', filter);

            if (this.feedFilter) {
                if (!params.has('filter')) {
                    params.append('filter', '');
                }
                if (params.get('filter') != '') {
                    params = params.set('filter', params.get('filter') + '');
                }
                //params = params.set('filter', params.get('filter') + "jobInstance.feed.name==" + this.feedFilter);
                params = params.set('filter', params.get('filter') + "jobName==" + this.feedFilter);
            }
            //if the filter doesnt contain an operator, then default it to look for the job name
            if (params.get('filter') != '' && params.get('filter') != null && !this.containsFilterOperator(params.get('filter'))) {
                params =  params.set('filter', 'job=~%' + params.get('filter'));
            }


            var query = tabTitle != 'All' ? tabTitle.toLowerCase() : '';

            // {timeout: canceler.promise}
            var jobsSubscription = this.http.get(this.opsManagerJobService.JOBS_QUERY_URL + "/" + query, { params: params })
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
                this.http.post(OperationsRestUrlConstants.FEED_SYSTEM_NAMES_TO_DISPLAY_NAMES_URL, feedNames,
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
        for (var i = 0; i < ops.length; i++) {
            contains = filterStr.indexOf(ops[i]) >= 0;
            if (contains) {
                break;
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
        this.tabService.clearTabs(this.pageName);
        _.each(jobs, (job: any, i: any) => {

            var transformedJob = this.transformJob(job);
            this.tabService.addContent(this.pageName, tabTitle, transformedJob);
        });

        super.setSortBy('jobName');
        super.setDataAndColumnSchema(this.tabService.getActiveTab(this.pageName).data.content, this.columns);
        super.filter();

        return jobs;

    }

    onChangeLinks = (page: any) => {
        this.loaded = false;
        this.tabService.getActiveTab(this.pageName).currentPage = page;
        this.loadJobs(true);
    }

    onPaginationChange = (pagingEvent: IPageChangeEvent) => {

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

    onSearchTable = (searchTerm: string) => {
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

        job.icon = IconUtil.iconForJobStatus(job.displayStatus);

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

        // Unnecessary update
        // if (!shouldRefresh) {
            //setTimeout(() => { this.triggerJobActionListener("updateEnd", job); }, 10);
        //}

        this.clearRefreshTimeout(instanceId);

        //Refresh the Job Row if needed
        if (shouldRefresh && !this.cancelTimeouts) {
            this.timeoutMap[instanceId] = setTimeout(() => {
                this.getRunningJobExecutionData(instanceId, executionId)
            }, 1000);
        }
        job.runTimeStr = job.runTime ? this._dateTimeService.formatMillisAsText(job.runTime,true,false) : "--";

        return job;
    }

    //Util Functions
    capitalize(string: string) {
        return string.charAt(0).toUpperCase() + string.substring(1).toLowerCase();
    }

    jobDetails(event: any) {
        if (event.row.stream) {
            this.kyloStateService.OpsManager().Feed().navigateToFeedStats(event.row.jobName);
        } else {DateTimeService
            this.kyloStateService.OpsManager().Job().navigateToJobDetails(event.row.executionId);
        }

    }

    getRunningJobExecutionData(instanceId: any, executionId: any) {
        var successFn = (response: any) => {
            this.transformJob(response);
            this.triggerJobActionListener("updated", response)
        };

        this.http.get(this.opsManagerJobService.LOAD_JOB_URL(executionId)).toPromise().then(successFn);
    }

    triggerJobActionListener(action: any, job: any) {
        this.onJobAction.emit({ action: action, job: job });

        if(this.feedFilter) {
            //queue up the refresh
            if (this.updateFeedHealth) {
                clearTimeout(this.updateFeedHealth)
                this.updateFeedHealth = undefined;
            }
            if(!this.cancelTimeouts) {
                setTimeout(this.fetchFeedHealth.bind(this), 3000)
            }
        }
    }

    restartJob(event: any, job: any) {
        event.stopPropagation();
        event.preventDefault();
        var executionId = job.executionId;
        var instanceId = job.instanceId;
        this.clearRefreshTimeout(instanceId);
        this.triggerJobActionListener('restartJob', job);
        var xhr = this.opsManagerJobService.restartJob(job.executionId, {}, (response: any) => {
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
        this.opsManagerJobService.stopJob(job.executionId, {}, (response: any) => {
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
        this.opsManagerJobService.abandonJob(job.executionId, {}, (response: any) => {
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
        this.opsManagerJobService.failJob(job.executionId, {}, (response: any) => {
            this.updateJob(instanceId, response)
            this.triggerJobActionListener('failJob', response);
        })
    };



    showFilterHelpPanel() {

        var position = $("#jobs_more_vert").position();

        let dialogRef = this._dialogService.open(JobsFilterHelpPanelDialogComponent, {
            data: {

            },
            panelClass: "filter-help",
            width:"600px",
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

