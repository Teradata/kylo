import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA,MatDialogRef} from '@angular/material/dialog';
import {SchemaParser} from "../../../model/field-policy";

@Component({
    selector: 'schema-parse-settings-dialog',
    templateUrl: "js/feed-mgr/catalog/datasource/preview-schema/schema-parse-settings-dialog.component.html",
})
export class SchemaParseSettingsDialog {
    private selectedParser : SchemaParser;
    constructor(public dialogRef: MatDialogRef<SchemaParseSettingsDialog>,
                @Inject(MAT_DIALOG_DATA) public data: any) {
    this.selectedParser = data.schemaParser;
    }

    apply(){
        console.log("APPLY ",this.selectedParser)
            this.dialogRef.close(this.selectedParser)
    }

     cancel(): void {
            this.dialogRef.close();
    }


}