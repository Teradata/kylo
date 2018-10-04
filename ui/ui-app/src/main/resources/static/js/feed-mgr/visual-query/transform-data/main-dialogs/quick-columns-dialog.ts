import {Component, Inject} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ColumnProfile} from "../../wrangler/api/column-profile";

export class QuickColumnsDialogData {

    constructor(public items: ColumnProfile[]) {

    }
}


@Component({
    templateUrl: 'js/feed-mgr/visual-query/transform-data/main-dialogs/quick-columns-dialog.html',
    styleUrls: ["js/feed-mgr/visual-query/transform-data/main-dialogs/column-analysis.css"]
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
