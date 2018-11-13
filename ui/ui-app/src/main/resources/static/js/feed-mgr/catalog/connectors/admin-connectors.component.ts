import {Component, Input} from "@angular/core";
import {Connector} from "../api/models/connector";
import {ConnectorsComponent} from "./connectors.component";
import {EntityAccessControlService} from "../../shared/entity-access-control/EntityAccessControlService";
import {AccessControlService} from "../../../services/AccessControlService";
import {CatalogService} from "../api/services/catalog.service";
import {TdLoadingService} from "@covalent/core/loading";
import {TdDialogService} from "@covalent/core/dialogs";
import {TdDataTableService} from "@covalent/core/data-table";
import {StateService} from "@uirouter/angular";

@Component({
    selector:"admin-connectors",
    templateUrl:"./connectors.component.html"
})
export class AdminConnectorsComponent extends ConnectorsComponent {


    constructor(catalog: CatalogService, dataTable: TdDataTableService, dialog: TdDialogService, loading: TdLoadingService, state: StateService,
                private accessControlService: AccessControlService ) {
        super(catalog,dataTable,dialog,loading,state);
    }

    allowAccess:boolean = false;

    public ngOnInit() {
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowAccess = this.accessControlService.hasAction(AccessControlService.ADMIN_CONNECTORS, actionSet.actions);
                if(this.allowAccess) {
                    this.filter();
                }
                else {
                    //not allowed ... set the list to empty
                    this.availableConnectors = [];
                }
            });
    }

    /**
     * Creates a new data set from the specified connector.
     */
    selectConnector(connector: Connector) {
        this.state.go("catalog.admin-connector", {connectorId: connector.id});
    }

}