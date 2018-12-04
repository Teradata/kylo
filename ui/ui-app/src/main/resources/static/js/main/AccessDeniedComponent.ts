import { Component } from '@angular/core';
import {AccessControlService} from "../services/AccessControlService";

@Component({
    selector: 'access-denied-controller',
    templateUrl:'./access-denied.html'
})
export class AccessDeniedComponent {

    constructor(private AccessControlService:AccessControlService) {}
}

