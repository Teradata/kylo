import {AccessControlService} from "../../../services/AccessControlService";
import {SlaEmailTemplate} from "./sla-email-template.model";
import {Component, Inject, Input} from "@angular/core";
import {TdDialogService} from "@covalent/core/dialogs";
import {MatSnackBar} from "@angular/material/snack-bar";
import {CloneUtil} from "../../../common/utils/clone-util";
import {SlaEmailTemplateService} from "./sla-email-template.service";
import * as _ from "underscore"
import {VelocityTemplateDialogComponent, VelocityTemplateDialogData} from "./velocity-template-dialog.component";
import {FeedUploadFileDialogComponentData} from "../../feeds/define-feed-ng2/summary/feed-activity-summary/feed-upload-file-dialog/feed-upload-file-dialog.component";
import {StateService} from "@uirouter/core";
import {VelocityEmailTemplate} from "./velocity-email-template.model";
import {MatDialogRef} from "@angular/material";
import {TdAlertDialogComponent} from "@covalent/core";




@Component({
    selector: 'sla-email-template',
    styleUrls: ['./sla-email-template.component.scss'],
    templateUrl: './sla-email-template.component.html'
})
export class SlaEmailTemplateComponent {



    stateParams:any;

    /**
     * The id of the template
     */
    @Input()
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
    template: SlaEmailTemplate = null;
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

    openDialogRef:MatDialogRef<any> = null;

    codemirrorOptions :any = {
        lineWrapping : true,
        indentWithTabs: true,
        smartIndent: true,
        lineNumbers: true,
        matchBrackets : true,
        autofocus: true,
        mode: 'text/velocity'
    }



    constructor(private slaEmailTemplateService: SlaEmailTemplateService,
                private state: StateService,
                private _dialogService:TdDialogService,
                private snackBar:MatSnackBar,
                @Inject("AccessControlService") private accessControlService: AccessControlService) {


    }


    ngOnInit() {
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
        this.slaEmailTemplateService.validateTemplate(this.template.subject, this.template.template).subscribe((response: VelocityEmailTemplate) => {
            response.sendTest = false;
            this.showTestDialog(response);
        });
    }

    /**
     * Test the email
     */
    sendTestEmail() {
        this.slaEmailTemplateService.sendTestEmail(this.emailAddress, this.template.subject, this.template.template).subscribe((response: VelocityEmailTemplate) => {
            response.sendTest = true;
            if (response.success) {
                this.snackBar.open( 'Successfully sent the template',null,{duration:3000});
            }
            else {
                this.snackBar.open( 'Error sending the template ',null,{duration:3000});
                this.showTestDialog(response);
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
            if (response) {
                this.snackBar.open('Successfully saved the template',null,{duration:3000});
                this.state.go("^.list");
            }
        }
        var errorFn = (err: any) => {
            this.hideDialog();
            this.snackBar.open( 'Error saving the template ',null,{duration:3000});
        }

        this.slaEmailTemplateService.save(this.template).subscribe(successFn, errorFn);
    }

    cancel(){
        this.state.go("^.list");
    }

    /**
     * Go to the SLA agreement
     * @param {string} slaId
     */
    navigateToSla(slaId: string) {
        this.state.go("sla.edit",{slaId:slaId});
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

    private isDefined(obj:any){
        return !_.isUndefined(obj);
    }

    private loadTemplate() {
        if (this.isDefined(this.templateId) && this.templateId != null || (this.template == null || _.isUndefined(this.template))) {
            this.queriedTemplate = null;
            this.slaEmailTemplateService.getExistingTemplates().subscribe((templates:SlaEmailTemplate[]) => {
                if(this.templateId && this.templateId != null) {
                    this.queriedTemplate = this.slaEmailTemplateService.getTemplate(this.templateId);
                    if (_.isUndefined(this.queriedTemplate)) {
                        ///WARN UNABLE TO FNID TEMPLATE
                        this.showDialog("Unable to find template", "Unable to find the template for " + this.templateId);
                    }
                    else {
                        this.template = CloneUtil.deepCopy(this.queriedTemplate);
                        this.isDefault = this.template.default;
                        this.getRelatedSlas();
                    }
                }
                else {
                  this.template = this.slaEmailTemplateService.newTemplate();

                }
            })
        }
        else if ((this.template != null && this.isDefined(this.template))) {
            this.template = CloneUtil.deepCopy(this.template);
            this.isDefault = this.template.default;
        }
        else {
            //redirect back to email template list page
            this.state.go("^.list");
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
        this.slaEmailTemplateService.getAvailableActionItems().subscribe((response: any) => {
            this.availableSlaActions = response;
        });
    }


    private getRelatedSlas() {
        this.relatedSlas = [];
        if (this.template != null && this.isDefined(this.template) && this.isDefined(this.template.id)) {
            this.slaEmailTemplateService.getRelatedSlas(this.template.id).subscribe((response: any) => {
                _.each(response, (sla: any) => {
                    this.relatedSlas.push(sla)
                    this.template.enabled = true;
                })
            })
        }
    }

    private showTestDialog(resolvedTemplate: VelocityEmailTemplate) {
        let config = {data: new VelocityTemplateDialogData(resolvedTemplate,this.emailAddress), width: "500px"};
        this._dialogService.open(VelocityTemplateDialogComponent,config)

    }

    private showDialog(title: string, message: string) {
     this.openDialogRef =   this._dialogService.openAlert({title:title,message:message});
    }

    private hideDialog() {
    if(this.openDialogRef != null){
        this.openDialogRef.close();
        this.openDialogRef = null;
    }
    }
}