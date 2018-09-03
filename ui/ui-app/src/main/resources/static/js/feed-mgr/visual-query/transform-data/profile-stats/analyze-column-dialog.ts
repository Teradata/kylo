import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ProfileHelper} from "../../wrangler/api/profile-helper";

@Component({
    templateUrl: 'js/feed-mgr/visual-query/transform-data/profile-stats/analyze-column-dialog.html'
})
export class AnalyzeColumnDialog {
    /**
     * Additional details about the error.
     */
    profile: ProfileHelper;
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
