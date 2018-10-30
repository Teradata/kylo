import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {Component, Inject, OnInit, SecurityContext} from "@angular/core";
import {StateService} from "@uirouter/angular";
import {TemplateService} from "../services/template.service";
import {HttpClient} from "@angular/common/http";
import {DomSanitizer, SafeHtml} from "@angular/platform-browser";
import {TemplateRepository} from "../services/model";

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