import { DefaultTableOptionsService } from "../../services/TableOptionsService";
import { DefaultPaginationDataService } from "../../services/PaginationDataService";
import {StateService} from "../../services/StateService";
import { Component, Input, OnInit, OnDestroy, ViewEncapsulation } from "@angular/core";
import {ServicesStatusData} from "../services/ServicesStatusService";
import { BaseFilteredPaginatedTableView } from "../../common/filtered-paginated-table-view/BaseFilteredPaginatedTableView";
import { TdDataTableService, ITdDataTableColumn } from "@covalent/core/data-table";
@Component({
    selector: "tba-Service-Health",
    templateUrl: "./service-health-template.html",
    encapsulation: ViewEncapsulation.None,
    styles: [`
        tba-service-health .mat-card {
            box-shadow: 0 3px 1px -2px rgba(0,0,0,.2), 0 2px 2px 0 rgba(0,0,0,.14), 0 1px 5px 0 rgba(0,0,0,.12) !important;
        }
    `]
})
export class ServiceHealthComponent extends BaseFilteredPaginatedTableView implements OnInit, OnDestroy {
    pageName: string;
    //Page State
    loading: boolean;
    services: any[] = [];
    serviceName: string;
    //Pagination and view Type (list or table)
    refreshInterval: number;
    cardTitle: string = "Service Health";
    refreshIntervalTime: number = 5000;

    RefreshIntervalSet: any = this.setRefreshInterval();
    RefreshIntervalClear: any = this.clearRefreshInterval();

    public columns: ITdDataTableColumn[] = [
        { name: 'name', label: 'Name', sortable: true, filter: true },
        { name: 'status', label: 'Status', sortable: true, filter: true },
        { name: 'components', label: 'Components', sortable: true, filter: true },
        { name: 'alerts', label: 'Alerts', sortable: true, filter: true },
        { name: 'checkDate', label: 'Last Checked', sortable: true, filter: true },
    ];

    ngOnInit() {

        this.pageName = 'service-health';
        //Page State
        this.loading = true;
        //Load the data
        this.loadData();
        //Refresh Intervals
        this.setRefreshInterval();
    }
    ngOnDestroy() {
        this.clearRefreshInterval();
    }

    constructor(
        private servicesStatusData: ServicesStatusData,
        private stateService: StateService,
        private _tdDataTableService : TdDataTableService) {
            super(_tdDataTableService);
    }// end of constructor


    //Load Jobs
    loadData() {
        var successFn = (data: any) => {
            this.services = data;
            super.setSortBy('name');
            var servcesList = this.services.map((service : any) => {
                let serv :any = {};
                serv.name = service.serviceName;
                serv.components = service.componentsCount;
                serv.checkDate = service.checkDate;
                serv.alerts = service.alertsCount;
                serv.status = {icon : service.icon, healthText : service.healthText};
                return serv;
            });
            super.setDataAndColumnSchema(servcesList, this.columns);
            this.loading == false;
        }
        var errorFn = (err: any) => {
            console.log('error', err);
        }
        var finallyFn = () => {

        }
        //Only Refresh if the modal dialog does not have any open alerts
        this.servicesStatusData.fetchServiceStatus(successFn, errorFn);

    }

    serviceDetails(event: any) {
        this.stateService.OpsManager().ServiceStatus().navigateToServiceDetails(event.row.name);
    }

    clearRefreshInterval() {
        if (this.refreshInterval != null) {
            clearInterval(this.refreshInterval);
            this.refreshInterval = null;
        }
    }

    setRefreshInterval() {
        this.clearRefreshInterval();
        if (this.refreshIntervalTime) {
            this.refreshInterval = setInterval(() => {this.loadData()}, this.refreshIntervalTime);
        }
    }
}
