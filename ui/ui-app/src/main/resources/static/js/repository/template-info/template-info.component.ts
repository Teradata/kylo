import {Component, Injector, OnInit} from "@angular/core";
import {StateService} from "@uirouter/angular";
import AccessControlService from "../../services/AccessControlService";
import {TemplatePublishDialog} from "../dialog/template-publish-dialog";
import {MatDialog} from "@angular/material/dialog";

@Component({
    selector: "template-info",
    templateUrl: "js/repository/template-info/template-info.component.html"
})
export class TemplateInfoComponent implements OnInit {
    template: any;
    templateId: string;
    nifiTemplateId: string;
    loading: boolean = true;
    allowEdit: boolean = false;
    allowExport: boolean = false;
    registerTemplateService: any;
    enabling: boolean = false;
    disabling: boolean = false;

    constructor(private $$angularInjector: Injector, private state: StateService, private dialog: MatDialog) {
        this.registerTemplateService = $$angularInjector.get("RegisterTemplateService");
        let accessControlService = $$angularInjector.get("AccessControlService");
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
        this.registerTemplateService.loadTemplateWithProperties(this.templateId, this.nifiTemplateId).then((response: any) => {
            this.template = this.registerTemplateService.model;
            this.loading = false;
            // console.log(this.template);
        }, (err: any) => {
            console.log("Error retrieving template");
        });
    }

    disableTemplate(): void {
        this.disabling = true;
        if (this.template.id) {
            this.registerTemplateService.disableTemplate(this.template.id).then((response: any) => {
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
                this.enabling = false;
            }, (error: any) => {
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
        let dialogRef = this.dialog.open(TemplatePublishDialog, {
            data: {templateName: this.template.templateName, templateId: this.template.id}
        });
    }
}
