import {Component, Inject, Input, OnInit} from "@angular/core";
import {AccessControlService} from "../../services/AccessControlService";
import AccessConstants from "../../constants/AccessConstants";

@Component({
    templateUrl:"./access-denied.component.html"
})
export class AccessDeniedComponent implements OnInit{


    @Input()
    stateParams:any;

    permissions:string[] = []

    missingPermissions:string[] = [];

    constructor(@Inject("AccessControlService") private accessControlService:AccessControlService) {}


    ngOnInit(){
        if(this.stateParams && this.stateParams.attemptedState) {
            if(this.stateParams.attemptedState.data.permissions) {
                this.permissions = this.stateParams.attemptedState.data.permissions;
                this.missingPermissions = this.accessControlService.findMissingPermissions(this.stateParams.attemptedState.data.permissions);
            }
            else  if(this.stateParams.attemptedState.data.permissionsKey) {
                this.permissions = AccessConstants.getStatePermissions(this.stateParams.attemptedState.data.permissionsKey);
                this.missingPermissions = this.accessControlService.findMissingPermissions(this.permissions);
            }

        }
    }
}