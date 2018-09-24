import * as _ from 'underscore';
import { TransitionService, StateService } from "@uirouter/core";
import { DefaultTableOptionsService } from "../../services/TableOptionsService";
import { DefaultPaginationDataService } from "../../services/PaginationDataService";
import ServicesStatusData from "../services/ServicesStatusService";
import { Component, OnInit } from "@angular/core";
import { BaseFilteredPaginatedTableView } from '../../common/filtered-paginated-table-view/BaseFilteredPaginatedTableView';
import { ITdDataTableColumn, TdDataTableService } from '@covalent/core/data-table';
@Component({
    selector: "service-Health-Details-Controller",
    templateUrl: "js/ops-mgr/service-health/service-detail.html",
    styles: [`.ListItemContainer{
        padding: 0px !important;
        line-height: inherit;
        -webkit-box-pack: start;
        justify-content: flex-start;
        -webkit-box-align: center;
        align-items: center;
        min-height: 48px;
        height: auto;
        flex: 1 1 auto;
    }`]
})
export class ServiceHealthDetailsComponent extends BaseFilteredPaginatedTableView implements OnInit {
    cardTitle: string = 'Service Components';
    loading: boolean = true;
    showProgress: boolean = true;
    serviceName: any;

    service: any = { components: [] };
    public columns: ITdDataTableColumn[] = [
        { name: 'name', label: 'Name', sortable: true, filter: true },
        { name: 'status', label: 'Status', sortable: true, filter: true },
        { name: 'message', label: 'Message', sortable: true, filter: true },
        { name: 'alerts', label: 'Alerts', sortable: true, filter: true },
        { name: 'checkDate', label: 'Last Checked', sortable: true, filter: true },
    ];

    ngOnInit() {
        this.serviceName = this.stateService.params.serviceName;
        this.service = this.servicesStatusData.services[this.serviceName];

        if (_.isEmpty(this.servicesStatusData.services)) {
            this.servicesStatusData.fetchServiceStatus((data: any) => {
                super.setSortBy('name');
                this.setServiceComponents();
                this.loading = false;
            }, () => {
                this.loading = false;
            });
        }else{
            this.setServiceComponents();
            this.loading = false;
        }
    }
    constructor(
        private servicesStatusData: ServicesStatusData,
        private stateService: StateService,
        private tdDataTableService: TdDataTableService) {
        super(tdDataTableService);
        this.servicesStatusData.servicesSubject.subscribe((services :any) => {
            this.service = services[this.serviceName];
            this.setServiceComponents();
        });
    }// end of constructor

    setServiceComponents(){
        if(this.service && this.service.components){
            var components = this.service.components.map((component : any) => {
                let com:any = {};
                com.name = component.name;
                com.status = {iconstyle : component.iconstyle, icon: component.icon, healthText : component.healthText};
                com.message = component.message;
                com.alerts = component.alerts.length;
                com.checkDate = component.checkDate;
                return com;
            });
            super.setDataAndColumnSchema(components, this.columns);
            super.filter();
        }else{
            super.setDataAndColumnSchema(null, this.columns);
        }
    }


    serviceComponentDetails(event: any) {
        this.stateService.go('service-component-details', { serviceName: this.serviceName, componentName: event.row.name });
    }
}

