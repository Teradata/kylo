import {Component, Inject} from "@angular/core";
import {MatDialogRef} from "@angular/material/dialog";
import {MAT_DIALOG_DATA} from "@angular/material/dialog";

@Component({
    templateUrl: "./describe-table-dialog.component.html"
})
export class DescribeTableDialogComponent {

    tableName: string;

    constructor(private dialog: MatDialogRef<DescribeTableDialogComponent>, @Inject(MAT_DIALOG_DATA) table: any) {
        if (typeof table !== "undefined" && table !== null && typeof table.schema !== "undefined" && table.schema !== null && typeof table.tableName !== "undefined" && table.tableName !== null) {
            this.tableName = table.schema + "." + table.tableName;
        } else {
            this.tableName = "table";
        }
    }

    hide() {
        this.dialog.close();
    }
}
