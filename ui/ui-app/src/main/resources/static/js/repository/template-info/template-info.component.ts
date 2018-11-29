import {Component, Inject, OnInit} from "@angular/core";
import {MatDialog} from "@angular/material/dialog";
import {StateService} from "@uirouter/angular";

import {AccessControlService} from "../../services/AccessControlService";
import {StateService as KyloStateService} from "../../services/StateService";
import {TemplatePublishDialog} from "../dialog/template-publish-dialog";

@Component({
    selector: "template-info",
    styleUrls: ["./template-info.component.css"],
    templateUrl: "./template-info.component.html"
})
export class TemplateInfoComponent implements OnInit {
    template: any;
    templateId: string;
    nifiTemplateId: string;
    loading: boolean = true;
    allowEdit: boolean = false;
    allowExport: boolean = false;
    enabling: boolean = false;
    disabling: boolean = false;

    constructor(@Inject("RegisterTemplateService") private registerTemplateService: any,
                @Inject("AccessControlService") private accessControlService: AccessControlService,
                @Inject("StateService") private kyloStateService: KyloStateService,
                private state: StateService,
                private dialog: MatDialog) {
        accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowEdit = accessControlService.hasAction(AccessControlService.TEMPLATES_EDIT, actionSet.actions);
                this.allowExport = accessControlService.hasAction(AccessControlService.TEMPLATES_EXPORT, actionSet.actions);
            });
        this.templateId = this.state.params.registeredTemplateId;
        this.nifiTemplateId = this.state.params.nifiTemplateId;
    }

    ngOnInit(): void {
        this.templateId = this.state.params.registeredTemplateId;
        if (this.templateId == null) {
            console.error("Error retrieving template: " + this.templateId);
            this.kyloStateService.TemplateStates().navigateToRegisteredTemplates();
        } else {
            this.registerTemplateService.loadTemplateWithProperties(this.templateId, this.nifiTemplateId).then((response: any) => {
                this.template = response.data;
                this.loading = false;
            }, (err: any) => {
                console.log("Error retrieving template", err);
            });
        }
    }

    disableTemplate(): void {
        this.disabling = true;
        if (this.template.id) {
            this.registerTemplateService.disableTemplate(this.template.id).then((response: any) => {

                this.template = response.data;
                this.disabling = false;
            }, (error: any) => {

                this.disabling = false;
            });
        }
    }

    enableTemplate(): void {
        this.enabling = true;
        if (this.template.id) {
            this.registerTemplateService.enableTemplate(this.template.id).then((response: any) => {
                // console.log(response);
                this.template = response.data;
                this.enabling = false;
            }, (error: any) => {
                console.log(error);
                this.enabling = false;
            });
        }
    }

    cancel(): void {
        this.state.go('registered-templates');
    }

    editTemplate(): void {
        if (this.allowEdit)
            this.state.go('register-template', {registeredTemplateId: this.templateId, nifiTemplateId: this.nifiTemplateId});
    }

    confirmPublish(): void {
        this.dialog.open(TemplatePublishDialog, {
            data: {templateName: this.template.templateName, templateId: this.template.id},
            width: '500px'
        });
    }
}
