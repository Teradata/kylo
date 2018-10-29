import * as _ from "underscore";
import OpsManagerRestUrlService from "../services/OpsManagerRestUrlService";
import {DefaultPaginationDataService} from "../../services/PaginationDataService";
import {DefaultTableOptionsService}  from "../../services/TableOptionsService";
import TabService from "../services/TabService";
import {Transition, StateService} from "@uirouter/core";
import { Component, Input, Inject, SimpleChanges } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import StateServices from "../../services/StateService";
import { ITdDataTableColumn, TdDataTableService } from "@covalent/core/data-table";
import { BaseFilteredPaginatedTableView } from "../../common/filtered-paginated-table-view/BaseFilteredPaginatedTableView";
import { Subject } from "rxjs/Subject";
import { ObjectUtils } from "../../common/utils/object-utils";
import { Subscription } from "rxjs/Subscription";
import { IPageChangeEvent } from "@covalent/core/paging";

@Component({
    selector: 'tba-alerts-table',
    templateUrl: 'js/ops-mgr/alerts/alerts-table-template.html',
    styles:[`
        .mat-form-field-underline {
            background-color: transparent !important;
            display: none;
        }
        .optionsDialog {
            width: -webkit-fill-available;
            display: inline-block;
            position: relative;
            line-height: 36px;
            vertical-align: middle;
            text-align: center;
            cursor: pointer;
            padding: 0 6px;
        }
        .optionsDialog:hover {
            background-color: rgba(158,158,158,0.2) !important;
        }
    `]
})
export class AlertsTableComponent extends BaseFilteredPaginatedTableView {
    @Input() pageName: any;
    loading: boolean;
    showProgress: boolean;
    paginationData: any;
    viewType: any;
    tabNames: any;
    tabs: any;
    tabMetadata: any;
    sortOptions: any;
    filterAlertType: any;
    alertTypes: any;
    filterAlertState: any;
    alertStates: any;
    showCleared: boolean = true;
    menuLabel: string = "Show Cleared";
    @Input() query: any;
    @Input() cardTitle: string;
    /**
     * The filter supplied in the page
     * @type {string}
     */
    filterTable: any =  '';
    /**
     * Array holding onto the active alert promises
     * @type {Array}
     */
    activeAlertRequests: Subscription[] = [];
    /**
     * The time of the newest alert from the last server response.
     * @type {number|null}
     */
    newestTime: any = null;
    /**
     * The time of the oldest alert from the last server response.
     * @type {number|null}
     */
    oldestTime: any = null;
    ALL_ALERT_TYPES_FILTER: any;
    direction: any;
    PAGE_DIRECTION: any;
    refreshing: any;
    deferred: any;
    promise: any;
    total: number = 0;
    page: number = 1;

    columns: ITdDataTableColumn[] = [
        { name: 'level',  label: 'Level', sortable: true, filter: true },
        { name: 'state', label: 'State', sortable: true, filter: true },
        { name: 'typeDisplay', label: 'Type', sortable: true, filter: true},
        { name: 'startTime', label: 'Start Time', sortable: true, filter: true},
        { name: 'description', label: 'Description', sortable: false, filter: true}
      ];

    ngOnInit(){
        this.filterTable =  ObjectUtils.isDefined(this.query) ? this.query : '';
        this.pageName = ObjectUtils.isDefined(this.pageName) ? this.pageName : 'alerts';
        //Page State
        this.loading = true;
        this.showProgress = true;
        //Pagination and view Type (list or table)
        this.paginationData = this.PaginationDataService.paginationData(this.pageName);
        //Setup the Tabs
        this.tabNames = ['All', 'INFO', 'WARNING', 'MINOR', 'MAJOR', 'CRITICAL', 'FATAL'];
        this.tabs = this.TabService.registerTabs(this.pageName, this.tabNames, this.paginationData.activeTab);
        this.tabMetadata = this.TabService.metadata(this.pageName);
        this.PAGE_DIRECTION = {forward: 'f', backward: 'b', none: 'n'};

        this.ALL_ALERT_TYPES_FILTER = {label:"ALL",type:""};
        this.filterAlertType = this.ALL_ALERT_TYPES_FILTER;

        this.alertTypes = [this.ALL_ALERT_TYPES_FILTER];

        var UNHANDLED_FILTER = {label:'UNHANDLED'};
        this.filterAlertState = UNHANDLED_FILTER;

        this.alertStates = [{label:'ALL'},{label:'HANDLED'},UNHANDLED_FILTER]
        this.initAlertTypes();

        this.loadAlerts();
    }

    constructor(private http: HttpClient,
            private TableOptionsService: DefaultTableOptionsService,
            private PaginationDataService: DefaultPaginationDataService, 
            private StateService: StateServices,
            private TabService: TabService,
            private OpsManagerRestUrlService: OpsManagerRestUrlService,
            public _dataTableService: TdDataTableService){
                super(_dataTableService);

    } // end of constructor

    initAlertTypes() {
        this.http.get(this.OpsManagerRestUrlService.ALERT_TYPES).toPromise().then((response: any)=> {
            this.alertTypes = this.alertTypes.concat(response);
        });
    }

    onFilterAlertTypeChange(alertType: any){
        this.loadAlerts(true);
    }

    onFilterAlertStateChange(alertState: any){
            this.loadAlerts(true);
    }       

    //Tab Functions
    onTabSelected(tab: any){
        this.newestTime = null;
        this.oldestTime = null;
        this.TabService.selectedTab(this.pageName, this.tabs[tab]);
        return this.loadAlerts();
    };

    showFilterHelpPanel() {
        this.selectedAdditionalMenuOption();
    }
    
    selectedAdditionalMenuOption() {
        if(this.showCleared) {
            this.menuLabel = "Hide Cleared";
            this.columns.splice(3, 0, { name: 'cleared', label: 'Cleared', sortable: false, filter: false});
        }
        else {
            this.menuLabel = "Show Cleared";
            this.columns.splice(3, 1);
        }
        this.showCleared = !this.showCleared;
        this.loadAlerts();
    }

    //Load Alerts
    loadAlerts(direction?: any) {
        if (direction == undefined) {
            direction = this.PAGE_DIRECTION.none;
        }

        if (!this.refreshing) {
                //cancel any active requests
                this.activeAlertRequests.forEach((subscription: Subscription)=> {
                    subscription.unsubscribe();
                });
                this.activeAlertRequests = [];

            var activeTab = this.TabService.getActiveTab(this.pageName);

            this.refreshing = true;
            var sortOptions = '';
            var tabTitle = activeTab.title;
            var filters = {tabTitle: tabTitle};
            var limit = this.paginationData.rowsPerPage;

            var successFn = (response: any, loadJobsSubscription : Subscription)=> {
                if (response) {
                    var alertRange = response;

                    if (ObjectUtils.isDefined(alertRange.size)) {
                        if (direction === this.PAGE_DIRECTION.forward || direction === this.PAGE_DIRECTION.none) {
                            this.total = (this.PaginationDataService.currentPage(this.pageName, activeTab.title) - 1) * this.PaginationDataService.rowsPerPage(this.pageName) + alertRange.size + 1;
                        } else {
                            this.total = this.PaginationDataService.currentPage(this.pageName, activeTab.title) * this.PaginationDataService.rowsPerPage(this.pageName) + 1;
                        }
                    } else {
                        this.total = (this.PaginationDataService.currentPage(this.pageName, activeTab.title) - 1) * this.PaginationDataService.rowsPerPage(this.pageName) + 1;
                    }

                    this.newestTime = ObjectUtils.isDefined(alertRange.newestTime) ? alertRange.newestTime : 0;
                    this.oldestTime = ObjectUtils.isDefined(alertRange.oldestTime) ? alertRange.oldestTime : 0;

                    //transform the data for UI
                    this.transformAlertData(tabTitle, ObjectUtils.isDefined(alertRange.alerts) ? alertRange.alerts : []);
                    this.TabService.setTotal(this.pageName, tabTitle, this.total);

                    if (this.loading) {
                        this.loading = false;
                    }
                }
                this.finishedRequest(loadJobsSubscription);
            };
            var errorFn =  (err: any, loadJobsSubscription : Subscription)=> {
                this.finishedRequest(loadJobsSubscription);
            };

            var filter = this.filterTable;
            var params: any = {limit: limit};

            // Get the next oldest or next newest alerts depending on paging direction.
            if (direction == this.PAGE_DIRECTION.forward) {
                if (this.oldestTime !== null) {
                    // Filter alerts to those created before the oldest alert of the previous results
                    params.before = this.oldestTime;
                }
            } else if (direction == this.PAGE_DIRECTION.backward) {
                if (this.newestTime !== null) {
                    // Filter alerts to those created after the newest alert of the previous results
                    params.after = this.newestTime;
                }
            } else {
                if (this.newestTime !== null && this.newestTime !== 0) {
                    // Filter alerts to the current results
                    params.before = this.newestTime + 1;
                }
            }
            params.cleared = this.showCleared;
            if (tabTitle != 'All') {
                params.level=tabTitle;
            }
            if(this.filterTable != '') {
                params.filter = filter;
            }
            if(this.filterAlertType.label != 'ALL'){
                if(params.filter == undefined){
                    params.filter = this.filterAlertType.type;
                }
                else {
                    params.filter+=','+this.filterAlertType.type;
                }
            }
            if(this.filterAlertState.label != 'ALL'){
                params.state = this.filterAlertState.label;
            }
            var loadJobsObservable = this.http.get(this.OpsManagerRestUrlService.ALERTS_URL,{params: params});
            var loadJobsSubscription = loadJobsObservable.subscribe(
                (response : any) => {successFn(response,loadJobsSubscription)},
                (error: any)=>{ errorFn(error,loadJobsSubscription)
            });
            this.activeAlertRequests.push(loadJobsSubscription);
        }

        this.showProgress = true;
    }

    /**
     * Called when the Server finishes.
     * @param canceler
     */
    finishedRequest(subscription : Subscription) {
        var index = _.indexOf(this.activeAlertRequests, subscription);
        if (index >= 0) {
            this.activeAlertRequests.splice(index, 1);
        }
        subscription.unsubscribe();
        this.refreshing = false;
        this.showProgress = false;
    }

    /**
     * Transform the array of alerts for the selected Tab coming from the server to the UI model
     * @param tabTitle
     * @param alerts
     */
    transformAlertData(tabTitle: any, alerts: any) {
        //first clear out the arrays

        this.TabService.clearTabs(this.pageName);
        alerts.forEach((alert: any, i: any)=>{
            var transformedAlert = this.transformAlert(alert);
            this.TabService.addContent(this.pageName, tabTitle, transformedAlert);
        });

        super.setSortBy('level');
        super.setDataAndColumnSchema(this.TabService.getActiveTab(this.pageName).data.content,this.columns);
        super.filter();
    }

    /**
     * Transform the alert coming from the server to a UI model
     * @param alert
     * @returns {*}
     */
    transformAlert(alert: any) {
        alert.typeDisplay = alert.type;
        if(alert.typeDisplay.indexOf("http://kylo.io/alert/alert/") == 0){
            alert.typeDisplay = alert.typeDisplay.substring("http://kylo.io/alert/alert/".length);
            alert.typeDisplay= alert.typeDisplay.split("/").join(" ");
        }
        else if(alert.typeDisplay.indexOf("http://kylo.io/alert/") == 0){
            alert.typeDisplay = alert.typeDisplay.substring("http://kylo.io/alert/".length);
            alert.typeDisplay = alert.typeDisplay.split("/").join(" ");
        }
        return alert;
    }

    /**
     * Navigate to the alert details page
     * @param event
     * @param alert
     */
    alertDetails (event: any) {
        this.StateService.OpsManager().Alert().navigateToAlertDetails(event.row.id);
    };

    onChangeLinks (page: any) {
        var activeTab = this.TabService.getActiveTab(this.pageName);
        var prevPage = this.PaginationDataService.currentPage(this.pageName, activeTab.title);
        
        // Current page number is only used for comparison in determining the direction, i.e. the value is not relevant.
        if (prevPage > page) {
            this.direction = this.PAGE_DIRECTION.backward;
        } else if (prevPage < page) {
            this.direction = this.PAGE_DIRECTION.forward;
        } else {
            this.direction = this.PAGE_DIRECTION.none;
        }

        this.PaginationDataService.currentPage(this.pageName, activeTab.title, page);
        return this.loadAlerts(this.direction);
    }

    onPaginationChange (pagingEvent: IPageChangeEvent) {

        if(this.page != pagingEvent.page) {
            this.page = pagingEvent.page;
            this.onChangeLinks(pagingEvent.page);
        }
        else {
            this.paginationData.rowsPerPage = pagingEvent.pageSize;
            this.loadAlerts(true);
            this.onPageSizeChange(pagingEvent);
        }

    }

    onSearchTable (searchTerm: string) {
        this.filterTable = searchTerm;
        this.loadAlerts(true);
    }
}

@Component({
    selector: 'alerts-controller',
    templateUrl: 'js/ops-mgr/alerts/alerts-table.html'
})
export class AlertsComponent {
    query: any;

    constructor(private stateService: StateService){
        this.query = this.stateService.params.query;
    }
}