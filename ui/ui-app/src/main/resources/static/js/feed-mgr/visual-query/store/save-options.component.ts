import {Component} from "@angular/core";
import {MatDialogRef} from "@angular/material/dialog";

@Component({
    templateUrl: "js/feed-mgr/visual-query/store/save-options.component.html"
})
export class SaveOptionsComponent {

    constructor(private $mdDialog: MatDialogRef<SaveOptionsComponent>) {
    }

    /**
     * Hides this dialog.
     */
    hide() {
        this.$mdDialog.close();
    }
}
