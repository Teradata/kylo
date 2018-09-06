import * as _ from 'underscore';
import OpsManagerRestUrlService from "../services/OpsManagerRestUrlService";
import IconService from "../services/IconStatusService";
import TabService from "../services/TabService";
import AccessControlService from "../../services/AccessControlService";
import { DefaultTableOptionsService } from "../../services/TableOptionsService";
import BroadcastService from "../../services/broadcast-service";
import { DefaultPaginationDataService } from "../../services/PaginationDataService";
import { HttpClient, HttpParams } from "@angular/common/http";
import StateService from "../../services/StateService";
import { Component, Input } from "@angular/core";
import { ObjectUtils } from '../../common/utils/object-utils';
import { Subscription } from 'rxjs/Subscription';
import { BaseFilteredPaginatedTableView } from '../../common/filtered-paginated-table-view/BaseFilteredPaginatedTableView';
import { ITdDataTableColumn, TdDataTableService, ITdDataTableSortChangeEvent, TdDataTableSortingOrder } from '@covalent/core/data-table';
import { IPageChangeEvent } from '@covalent/core';

@Component({
    selector: 'kylo-service-level-assessments',
    templateUrl: 'js/ops-mgr/sla/service-level-assessments-template.html'
})
export class kyloServiceLevelAssessments extends BaseFilteredPaginatedTableView {
    
    @Input() pageName: string = ObjectUtils.isDefined(this.pageName) ? this.pageName : 'service-level-assessments';
    @Input() cardTitle: string
    loaded: boolean = false;
    refreshing: boolean;
    showProgress: boolean = true;
    /**
     * The filter supplied in the page
     * @type {string}
     */
    @Input() filterSLA: string;
    //Track active requests and be able to cancel them if needed
    activeRequests: Subscription[] = [];
    paginationData: any;
    tabs: any;
    tabMetadata: any;
    tabNames : string[] = ['All', 'Failure', 'Warning','Success'];
    allowAdmin: boolean = false;
    sortSLA: any;
    page: number = 1;

    columns: ITdDataTableColumn[] = [
        { name: 'name',  label: 'SLA Name', sortable: true, filter: true },
        { name: 'result', label: 'Result', sortable: true, filter: true },
        { name: 'createdTime', label: 'Created', sortable: true, filter: true},
        { name: 'message', label: 'Message', sortable: true, filter: true},
      ];

    ngOnInit() {

        this.sortSLA = "createdTime";
        this.filterSLA = ObjectUtils.isUndefined(this.filterSLA) || this.filterSLA == null ? '' : this.filterSLA;

        //Pagination
        this.paginationData = this.paginationDataService.paginationData(this.pageName);
        
        //Setup the Tabs
        this.tabs = this.tabService.registerTabs(this.pageName, this.tabNames, this.paginationData.activeTab);
        this.tabMetadata= this.tabService.metadata(this.pageName);

        // Fetch allowed permissions
        this.accessControlService.getUserAllowedActions()
                .then((actionSet: any)=>{
                    this.allowAdmin = this.accessControlService.hasAction(AccessControlService.OPERATIONS_ADMIN, actionSet.actions);
                });

        this.loadAssessments(true);
    }

    constructor(private http: HttpClient,
                private opsManagerRestUrlService: OpsManagerRestUrlService,
                private tableOptionsService: DefaultTableOptionsService,
                private paginationDataService: DefaultPaginationDataService,
                private StateService: StateService,
                private iconService: IconService,
                private tabService: TabService,
                private accessControlService: AccessControlService,
                private broadcastService: BroadcastService,
                public _dataTableService: TdDataTableService){
                    super(_dataTableService);
    } // end of constructor
    
    //Tab Functions
    onTabSelected=(tab: any)=>{
        this.loaded = false;
        this.tabService.selectedTab(this.pageName, this.tabs[tab]);
        return this.loadAssessments(true);
    };

    assessmentDetails= (event: any)=> {
            this.StateService.OpsManager().Sla().navigateToServiceLevelAssessment(event.row.id);
    }

    //Load Jobs
    loadAssessments= (force: any) =>{
        if (force || !this.refreshing) {

            if (force) {
                this.activeRequests.forEach((subscription: Subscription)=> {
                    subscription.unsubscribe();
                });
                this.activeRequests = [];
            }
            var activeTab = this.tabService.getActiveTab(this.pageName);

            this.refreshing = true;
            var tabTitle = activeTab.title;
            var filters = {tabTitle: tabTitle};
            var limit = this.paginationData.rowsPerPage;

            var start = (limit * activeTab.currentPage) - limit;

            var successFn=(response: any, loadAssessmentsSubscription : Subscription)=>{
                if (response) {
                    this.transformAssessments(tabTitle,response.data)
                    this.tabService.setTotal(this.pageName, tabTitle, response.recordsFiltered)
                }

                this.finishedRequest(loadAssessmentsSubscription);

            }
            var errorFn=(err: any, loadAssessmentsSubscription : Subscription)=>{
                this.finishedRequest(loadAssessmentsSubscription);
            }
            var finallyFn= ()=>{
            }
            var filter = this.filterSLA;
            if(tabTitle.toUpperCase() != 'ALL'){
                if(filter != null && ObjectUtils.isDefined(filter) && filter != '') {
                    filter +=','
                }
                filter += 'result=='+tabTitle.toUpperCase();
            }
            let params = new HttpParams();
            params = params.append('start', start.toString());
            params = params.append('limit', limit);
            params = params.append('sort', this.sortSLA);
            params = params.append('filter', filter);

            var loadAssessmentsObservable = this.http.get(this.opsManagerRestUrlService.LIST_SLA_ASSESSMENTS_URL,{params: params});
            var loadAssessmentsSubscription = loadAssessmentsObservable.subscribe(
                (response : any) => {successFn(response,loadAssessmentsSubscription)},
                (error: any)=>{ errorFn(error,loadAssessmentsSubscription)
            });
            this.activeRequests.push(loadAssessmentsSubscription);
        }
        this.showProgress = true;
    }

    transformAssessments (tabTitle: any, assessments: any){
        //first clear out the arrays
        this.tabService.clearTabs(this.pageName);
        assessments.forEach((assessment: any, i: number) =>{
            this.tabService.addContent(this.pageName, tabTitle, assessment);
        });

        super.setSortBy('createdTime');
        super.setDataAndColumnSchema(this.tabService.getActiveTab(this.pageName).data.content,this.columns);
        super.filter();

        return assessments;

    }

    finishedRequest(subscription : Subscription){
        var index = _.indexOf(this.activeRequests, subscription);
        if (index >= 0) {
            this.activeRequests.splice(index, 1);
        }
        subscription.unsubscribe();
        this.refreshing = false;
        this.showProgress = false;
        this.loaded = true;
    }

    onChangeLinks = (page: any) => {
        this.loaded = false;
        this.tabService.getActiveTab(this.pageName).currentPage = page;
        this.loadAssessments(true);
    }

    onPaginationChange = (pagingEvent: IPageChangeEvent) => {
        if(this.page != pagingEvent.page) {
            this.page = pagingEvent.page;
            this.onChangeLinks(pagingEvent.page);
        }
        else {
            this.paginationData.rowsPerPage = pagingEvent.pageSize;
            this.loadAssessments(true);
            this.onPageSizeChange(pagingEvent);
        }
    }

    onSearchTable = (searchTerm: string) => {
        this.filterSLA = searchTerm;
        this.loadAssessments(true);
    }

    onSortChange = (sortEvent: ITdDataTableSortChangeEvent) => {
        this.loaded = false;
        this.sortSLA = sortEvent.name;
        if(sortEvent.order == TdDataTableSortingOrder.Descending) {
            this.sortSLA = "-" + this.sortSLA;
        }
        this.loadAssessments(true);
    }

}

