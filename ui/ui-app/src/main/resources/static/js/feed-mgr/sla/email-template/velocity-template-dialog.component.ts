import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {SlaEmailTemplate} from "./sla-email-template.model";
import {VelocityEmailTemplate} from "./velocity-email-template.model";
import {DomSanitizer} from "@angular/platform-browser";

export class VelocityTemplateDialogData {

    constructor( public template:VelocityEmailTemplate, public emailAddress:string){

    }
}

@Component({
    selector:"velocity-template-dialog",
    templateUrl: "./velocity-template-dialog.component.html"
})
export class VelocityTemplateDialogComponent {

    resolvedTemplateSubject:string;
    resolvedTemplateBody:string;
    resolvedTemplate:VelocityEmailTemplate;
    emailAddress:string;
    constructor(private dialog: MatDialogRef<VelocityTemplateDialogComponent>, @Inject(MAT_DIALOG_DATA) public data: VelocityTemplateDialogData, private sanitized:DomSanitizer) {

    }

    ngOnInit() {
        this.resolvedTemplateSubject =this.data.template.subject;
        this.resolvedTemplateBody = this.data.template.body;
        this.resolvedTemplate = this.data.template;
        this.emailAddress = this.data.emailAddress;
    }
    ngOnDestroy(){

    }
    close() {
        this.dialog.close();
    }

}