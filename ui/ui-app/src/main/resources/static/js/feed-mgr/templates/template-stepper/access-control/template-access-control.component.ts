import * as _ from "underscore";
import { RegisterTemplateServiceFactory } from '../../../services/RegisterTemplateServiceFactory';
import {AccessControlService} from '../../../../services/AccessControlService';
import { EntityAccessControlService } from '../../../shared/entity-access-control/EntityAccessControlService';
import { Component, Input, OnInit } from '@angular/core';

@Component({
    selector: 'thinkbig-template-access-control',
    templateUrl: './template-access-control.html'
})
export class TemplateAccessControlController implements OnInit {

    /**
    * ref back to this controller
    * @type {TemplateAccessControlController}
    */
    // @Input() stepIndex: string;

    templateAccessControlForm: any = {};
    model: any;
    allowEdit: boolean = false;

    ngOnInit() {

        this.model = this.registerTemplateService.model;

        //allow edit if the user has ability to change permissions on the entity if its an existing registered template, or if it is new
        if (this.model.id && this.registerTemplateService.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.TEMPLATE.CHANGE_TEMPLATE_PERMISSIONS)) {
            this.allowEdit = true;
        }
        else {
            this.allowEdit = this.model.id == undefined;
        }
    }

    constructor(private registerTemplateService: RegisterTemplateServiceFactory,
                private accessControlService: AccessControlService,
                private entityAccessControlService: EntityAccessControlService) {}
}

