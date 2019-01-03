import { StateService } from "@uirouter/core";
import { DefaultTableOptionsService } from "../../services/TableOptionsService";
import { DefaultPaginationDataService } from "../../services/PaginationDataService";
import { Component, OnInit, ViewContainerRef, ViewEncapsulation } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import {ServicesStatusData} from "../services/ServicesStatusService";
import { BaseFilteredPaginatedTableView } from "../../common/filtered-paginated-table-view/BaseFilteredPaginatedTableView";
import { TdDataTableService, ITdDataTableColumn } from "@covalent/core/data-table";

@Component({
    selector: "service-Component-Health-Details-Controller",
    templateUrl: "./service-component-detail.html",
    encapsulation: ViewEncapsulation.None,
    styles: [`
        .ListItemContainer{
            padding: 0px !important;
            line-height: inherit;
            -webkit-box-pack: start;
            justify-content: flex-start;
            -webkit-box-align: center;
            align-items: center;
            min-height: 48px;
            height: auto;
            flex: 1 1 auto;
        }
        service-component-health-details-controller .mat-card {
            box-shadow: 0 3px 1px -2px rgba(0,0,0,.2), 0 2px 2px 0 rgba(0,0,0,.14), 0 1px 5px 0 rgba(0,0,0,.12) !important;
        }
    `]
})
export class ServiceComponentHealthDetailsComponent extends BaseFilteredPaginatedTableView implements OnInit {

    pageName: string;
    cardTitle: string;
    //Page State
    showProgress: boolean = true;
    component: any = { alerts: [] };
    componentName: any;
    serviceName: any;
    public columns: ITdDataTableColumn[] = [
        { name: 'name', label: 'Name', sortable: true, filter: true },
        { name: 'status', label: 'Status', sortable: true, filter: true },
        { name: 'message', label: 'message', sortable: true, filter: true },
        { name: 'latestTimestamp', label: 'Time', sortable: true, filter: true },
    ];
    service: any;

    ngOnInit() {

        this.pageName = 'service-component-details';
        this.cardTitle = 'Service Component Alerts';
        this.componentName = this.stateService.params.componentName;
        this.serviceName = this.stateService.params.serviceName;
        this.service = this.servicesStatusData.services[this.serviceName];

        if (this.service) {
            this.component = this.service.componentMap[this.componentName];
        };
        this.setComponentAlerts();

    }

    constructor(
        private servicesStatusData: ServicesStatusData,
        private stateService: StateService,
        private _tdDataTableService: TdDataTableService) {
        super(_tdDataTableService);
        this.servicesStatusData.servicesSubject.subscribe((services: any) => {
            this.service = services[this.serviceName];
            var updatedComponent = this.service.componentMap[this.componentName];
            if (updatedComponent != this.component) {
                this.component = updatedComponent;
                this.setComponentAlerts();
            }
        });
    }// end of constructor

    setComponentAlerts(){
        if(this.component && this.component.alerts){
            var alerts = this.component.alerts.map((alert : any) => {
                let com:any = {};
                com.name = alert.name;
                com.status = {iconstyle : alert.iconstyle, icon: alert.icon, healthText : alert.healthText};
                com.message = alert.message;
                com.checkDate = alert.latestTimestamp;
                return com;
            });
            super.setDataAndColumnSchema(alerts, this.columns);
            super.filter();
        }else{
            super.setDataAndColumnSchema(null, this.columns);
        }
    }

}
