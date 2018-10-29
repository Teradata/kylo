import { Component, Input, Inject, EventEmitter, Output } from "@angular/core";
import {MatDialog, MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';

@Component({
    selector: 'template-delete-dialog',
    templateUrl: 'js/feed-mgr/templates/template-stepper/select-template/template-delete-dialog.html'
})
export class TemplateDeleteDialog {

    @Input() model: any;
    @Output() onDeleteTemplate: EventEmitter<any> = new EventEmitter<any>();

    ngOnInit() {
        this.model = this.data.model;
    }

    constructor(@Inject(MAT_DIALOG_DATA) private data: any,
                private dialogRef: MatDialogRef<TemplateDeleteDialog>) {}

    hide () {
        this.dialogRef.close();
    };

    deleteTemplate() {
        this.onDeleteTemplate.emit();
    }
}