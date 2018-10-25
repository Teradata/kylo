import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ColumnProfile} from "../../wrangler/api/column-profile";


@Component({
    templateUrl: './analyze-column-dialog.html'
})
export class AnalyzeColumnDialog {
    /**
     * Additional details about the error.
     */
    profile: ColumnProfile;
    fieldName: any;

    // @ts-ignore
    constructor(private dialog: MatDialogRef<AnalyzeColumnDialog>, @Inject(MAT_DIALOG_DATA) private data: any) {
        this.profile = data.profileStats;
        this.fieldName = data.fieldName;
    }

    /**
     * Hides this dialog.
     */
    hide() {
        this.dialog.close();
    }
}
