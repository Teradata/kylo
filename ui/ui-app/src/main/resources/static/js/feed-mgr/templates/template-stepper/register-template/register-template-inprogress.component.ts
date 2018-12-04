import { Component, Input, Inject } from "@angular/core";
import {MatDialog, MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
    selector: 'register-template-inprogress-dialog',
    templateUrl: './register-template-inprogress-dialog.html'
})
export class RegisterTemplateInprogressDialog {

    @Input() templateName: string;

    ngOnInit() {
        this.templateName = this.data.templateName;
    }

    constructor(@Inject(MAT_DIALOG_DATA) private data: any,
                private dialogRef: MatDialogRef<RegisterTemplateInprogressDialog>) {}

    hide () {
        this.dialogRef.close();
    };
}
