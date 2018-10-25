import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ColumnProfile} from "../../wrangler/api/column-profile";

export class QuickColumnsDialogData {

    constructor(public items: ColumnProfile[]) {

    }
}


@Component({
    templateUrl: './quick-columns-dialog.html',
    styleUrls: ["./column-analysis.scss"]
})
export class QuickColumnsDialog {


    // @ts-ignore
    constructor(private dialog: MatDialogRef<QuickColumnsDialog>, @Inject(MAT_DIALOG_DATA) public data: QuickColumnsDialogData) {

    }

    /**
     * Hides this dialog.
     */
    hide() {
        this.dialog.close();
    }
}
