import { Component, Input, Inject } from "@angular/core";
import {MatDialog, MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
    selector: 'register-template-error-dialog',
    templateUrl: 'js/feed-mgr/templates/template-stepper/register-template/register-template-error-dialog.html'
})
export class RegisterTemplateErrorDialog {

    @Input() nifiTemplateId: string;
    @Input() templateName: string;
    @Input() message : string;

    ngOnInit() {
        this.templateName = this.data.templateName;
        this.nifiTemplateId = this.data.nifiTemplateId;
        this.message = this.data.message;
    }

    constructor(@Inject(MAT_DIALOG_DATA) private data: any,
                private dialogRef: MatDialogRef<RegisterTemplateErrorDialog>) {}

    hide = () => {
        this.dialogRef.close();
    };
}