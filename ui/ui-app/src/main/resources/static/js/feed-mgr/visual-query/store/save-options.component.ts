import {Component} from "@angular/core";
import {MatDialogRef} from "@angular/material/dialog";

@Component({
    styleUrls: [ "./store-options.scss"],
    templateUrl: "./save-options.component.html"
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
