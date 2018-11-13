import {Component, Inject, Input, OnInit} from "@angular/core";
import {AccessControlService} from "../../services/AccessControlService";

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
            this.missingPermissions = this.accessControlService.findMissingPermissions(this.stateParams.attemptedState.data.permissions);
            this.permissions = this.stateParams.attemptedState.data.permissions;
        }
    }
}