import {Component, Input, OnInit, ViewChild} from "@angular/core";
import {Connector} from "../api/models/connector";
import {EntityAccessControlService} from "../../shared/entity-access-control/EntityAccessControlService";
import {AccessControlService} from "../../../services/AccessControlService";
import {TranslateService} from "@ngx-translate/core";
import {StateService} from "@uirouter/core";
import {EntityAccessControlComponent} from "../../shared/entity-access-control/entity-access-control.component";
import {TdLoadingService} from "@covalent/core/loading";
import {MatSnackBar} from "@angular/material/snack-bar";
@Component({
    selector:"admin-connector",
    templateUrl:"./admin-connector.component.html"
})
export class AdminConnectorComponent implements OnInit {

    static LOADER = "AdminConnector.LOADER"

    allowAdmin:boolean = false;

    @ViewChild("entityAccessControl")
    private entityAccessControl: EntityAccessControlComponent;

    @Input("connector")
    public connector: Connector;

    saving:boolean = false;

    constructor(private translateService: TranslateService,
                private entityAccessControlService: EntityAccessControlService,
                private accessControlService: AccessControlService,
                private state: StateService,
                private loadingService:TdLoadingService,
                private snackBar:MatSnackBar) {


    }

    ngOnInit() {
        this.loadingService.register(AdminConnectorComponent.LOADER);
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
               this.setPermissions(actionSet)
                this.loadingService.resolve(AdminConnectorComponent.LOADER);
            });
    }

    private setPermissions(actionSet:any){
        let adminAccess = this.accessControlService.hasAction(AccessControlService.ADMIN_CONNECTORS, actionSet.actions);
        let entityAccess = this.accessControlService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.CONNECTOR.CHANGE_CONNECTOR_PERMISSIONS, this.connector,
            EntityAccessControlService.entityRoleTypes.CONNECTOR);
        this.allowAdmin = adminAccess && entityAccess;
    }
    goBackToList(){
        this.state.go("catalog.admin-connectors");
    }

    saveConnector(){
        this.saving = true;
        this.loadingService.register(AdminConnectorComponent.LOADER);
        this.entityAccessControl.onSave()
            .subscribe((updatedRoleMemberships) => {
                this.saving = false;
                this.loadingService.resolve(AdminConnectorComponent.LOADER);
                this.snackBar.open("Saved the connector",null,{duration:3000});
                this.state.go("catalog.admin-connectors");
            }, error => {
                this.saving = false;
                this.loadingService.resolve(AdminConnectorComponent.LOADER);
                this.snackBar.open("Error saving the connector",null,{duration:3000});
            });
    }
    cancel(){
        this.state.go("catalog.admin-connectors");
    }
}