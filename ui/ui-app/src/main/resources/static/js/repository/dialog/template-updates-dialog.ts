import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";

@Component({
    selector: 'template-publish-dialog',
    templateUrl: './template-updates-dialog.html',
})
export class TemplateUpdatesDialog {

    loading:boolean = true;

    constructor(private dialog: MatDialog,
                private dialogRef: MatDialogRef<TemplateUpdatesDialog>,
                @Inject(MAT_DIALOG_DATA) public data: any) {
    }

    close(): void {
        this.dialogRef.close();
    }

}
