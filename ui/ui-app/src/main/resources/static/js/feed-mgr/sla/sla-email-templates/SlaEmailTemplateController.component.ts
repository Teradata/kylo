import * as angular from 'angular';
import * as _ from 'underscore';
import SlaEmailTemplateService from "./SlaEmailTemplateService";
import AccessControlService from '../../../services/AccessControlService';
import {Transition, StateService} from "@uirouter/core";
import { Component, Inject } from '@angular/core';
import {MatDialog, MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
    selector: 'sla-email-template-controller',
    templateUrl: 'js/feed-mgr/sla/sla-email-templates/sla-email-template.html'
})
export class SlaEmailTemplateController {

    /**
     * The id of the template
     */
    templateId: string;
    /**
     * allow editing
     * @type {boolean}
     */
    allowEdit: boolean = false;
    /**
     * The current template we are editing
     * @type {null}
     */
    template: any;
    /**
     * Email Address to test with
     * @type {string}
     */
    emailAddress: string = '';
    /**
     * the template that been queried
     */
    queriedTemplate: any;
    /**
     * is this the default template
     * @type {boolean}
     */
    isDefault: boolean = false;
    /**
     * the list of available sla actions the template(s) can be assigned to
     * @type {Array}
     */
    availableSlaActions: any[] = [];

    /**
     * Variables allowed for the template
     * @type {{item: string; desc: string}[]}
     */
    templateVariables: any;

    /**
     * Any slas using this template
     * @type {any[]}
     */
    relatedSlas: any[] = [];

    constructor(private slaEmailTemplateService: SlaEmailTemplateService,
                private stateService: StateService,
                private accessControlService: AccessControlService,
                private dialog: MatDialog,
                @Inject("$injector") private $injector: any) {}

    ngOnInit() {
        this.templateId = this.stateService.params.emailTemplateId;
        this.template = this.slaEmailTemplateService.template;
        this.templateVariables = this.slaEmailTemplateService.getTemplateVariables();
        this.loadTemplate();
        this.getAvailableActionItems();
        this.getRelatedSlas();
        this.applyAccessControls();
    }


    /**
     * Validate and preview
     */
    validate() {
        this.slaEmailTemplateService.validateTemplate(this.template.subject, this.template.template).then((response: angular.IHttpResponse<any>) => {
            response.data.sendTest = false;
            this.showTestDialog(response.data);
        });
    }

    /**
     * Test the email
     */
    sendTestEmail() {
        this.slaEmailTemplateService.sendTestEmail(this.emailAddress, this.template.subject, this.template.template).then((response: angular.IHttpResponse<any>) => {
            response.data.sendTest = true;
            if (response.data.success) {
                this.$injector.get("$mdToast").show(
                    this.$injector.get("$mdToast").simple()
                        .textContent('Successfully sent the template')
                        .hideDelay(3000)
                );
            }
            else {
                this.$injector.get("$mdToast").show(
                    this.$injector.get("$mdToast").simple()
                        .textContent('Error sending the template ')
                        .hideDelay(3000)
                );
                this.showTestDialog(response.data);
            }
        })
    }

    /**
     * Save the template
     */
    saveTemplate() {
        this.showDialog("Saving", "Saving template. Please wait...");

        var successFn = (response: any) => {
            this.hideDialog();
            if (response.data) {
                this.$injector.get("$mdToast").show(
                    this.$injector.get("$mdToast").simple()
                        .textContent('Successfully saved the template')
                        .hideDelay(3000)
                );
            }
        }
        var errorFn = (err: any) => {
            this.hideDialog();
            this.$injector.get("$mdToast").show(
                this.$injector.get("$mdToast").simple()
                    .textContent('Error saving template ')
                    .hideDelay(3000)
            );
        }

        this.slaEmailTemplateService.save(this.template).then(successFn, errorFn);
    }

    /**
     * Go to the SLA agreement
     * @param {string} slaId
     */
    navigateToSla(slaId: string) {
        this.stateService.go('service-level-agreements');
    }

    /**
     * Load the example template
     */
    exampleTemplate() {
        this.template.subject = 'SLA Violation for $sla.name';
        this.template.template = '<html>\n<body> \n' +
            '\t<table>\n' +
            '\t\t<tr>\n' +
            '\t\t\t<td align="center" style="background-color:rgb(43,108,154);">\n' +
            '\t\t\t\t<img src="https://kylo.io/assets/Kylo-Logo-REV.png" height="50%" width="50%">\n' +
            '\t\t\t</td>\n' +
            '\t\t</tr>\n' +
            '\t\t<tr>\n' +
            '\t\t\t<td>\n' +
            '\t\t\t\t<table>\n' +
            '\t\t\t\t\t<tr>\n' +
            '\t\t\t\t\t\t<td>$sla.name</td>\n' +
            '\t\t\t\t\t </tr>\n' +
            '\t\t\t\t\t<tr>\n' +
            '\t\t\t\t\t\t<td>$sla.description</td>\n' +
            '\t\t\t\t\t</tr>\n' +
            '\t\t\t\t\t<tr>\n' +
            '\t\t\t\t\t\t<td colspan="2">\n' +
            '\t\t\t\t\t\t\t<h3>Assessment Description</h3>\n' +
            '\t\t\t\t\t\t</td>\n' +
            '\t\t\t\t\t</tr>\n' +
            '\t\t\t\t\t<tr>\n' +
            '\t\t\t\t\t\t<td colspan="2" style="white-space:pre-wrap;">$assessmentDescription</td>\n' +
            '\t\t\t\t\t</tr>\n' +
            '\t\t\t\t</table>\n' +
            '\t\t\t</td>\n' +
            '\t\t</tr>\n' +
            '\t</table>\n' +
            '</body>\n</html>';
        '</html>';
    }

    private loadTemplate() {
        if (angular.isDefined(this.templateId) && this.templateId != null && (this.template == null || angular.isUndefined(this.template))) {
            this.queriedTemplate = null;
            this.slaEmailTemplateService.getExistingTemplates().then(() => {
                this.template = this.slaEmailTemplateService.getTemplate(this.templateId);
                if (angular.isUndefined(this.template)) {
                    ///WARN UNABLE TO FNID TEMPLATE
                    this.showDialog("Unable to find template", "Unable to find the template for " + this.templateId);
                }
                else {
                    this.queriedTemplate = angular.copy(this.template);
                    this.isDefault = this.queriedTemplate.default;
                    this.getRelatedSlas();
                }
            })
        }
        else if ((this.template != null && angular.isDefined(this.template))) {
            this.queriedTemplate = angular.copy(this.template);
            this.isDefault = this.queriedTemplate.default;
        }
        else {
            //redirect back to email template list page
            this.stateService.go('sla-email-templates');
        }
    }


    private applyAccessControls() {
        // Fetch the allowed actions
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowEdit = this.accessControlService.hasAction(AccessControlService.EDIT_SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATE, actionSet.actions);
            });
    }


    private getAvailableActionItems() {
        this.slaEmailTemplateService.getAvailableActionItems().then((response: any) => {
            this.availableSlaActions = response;
        });
    }


    private getRelatedSlas() {
        this.relatedSlas = [];
        if (this.template != null && angular.isDefined(this.template) && angular.isDefined(this.template.id)) {
            this.slaEmailTemplateService.getRelatedSlas(this.template.id).then((response: any) => {
                _.each(response.data, (sla: any) => {
                    this.relatedSlas.push(sla)
                    this.template.enabled = true;
                })
            })
        }
    }

    private showTestDialog(resolvedTemplate: any) {

        let dialogRef = this.dialog.open(testDialogController, {
            data: { resolvedTemplate: resolvedTemplate,
                    emailAddress: this.emailAddress
            },
            panelClass: "full-screen-dialog"
          });
    }

    private showDialog(title: string, message: string) {
        this.$injector.get("$mdDialog").show(
            this.$injector.get("$mdDialog").alert()
                .parent(angular.element(document.body))
                .clickOutsideToClose(false)
                .title(title)
                .textContent(message)
                .ariaLabel(title)
        );
    }

    private hideDialog() {
        this.$injector.get("$mdDialog").hide();
    }

}

@Component({
    selector: "test-dialog-controller",
    templateUrl: "js/feed-mgr/sla/sla-email-templates/test-velocity-dialog.html"
})
export class testDialogController {

    resolvedTemplateSubject: any;
    resolvedTemplateBody: any;
    resolvedTemplate: any;
    emailAddress: any;

    ngOnInit() {
        this.resolvedTemplateSubject = this.$injector.get("$sce").trustAsHtml(this.data.resolvedTemplate.subject);
        this.resolvedTemplateBody = this.$injector.get("$sce").trustAsHtml(this.data.resolvedTemplate.body);
        this.resolvedTemplate = this.data.resolvedTemplate;
        this.emailAddress = this.data.emailAddress;
    }

    constructor(@Inject("$injector") private $injector: any,
                private dialogRef: MatDialogRef<testDialogController>,
                @Inject(MAT_DIALOG_DATA) private data: any) {}

    hide = () => {
        this.dialogRef.close();
    };

    cancel = () => {
        this.dialogRef.close();
    };

    trustAsHtml = (string: any) => {
        return this.$injector.get("$sce").trustAsHtml(string);
    };

}