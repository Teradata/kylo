import {Component} from "@angular/core";
import {MatDialogRef} from "@angular/material/dialog";

@Component({
    templateUrl: "js/plugin/spark-ingest/browse.component.html"
})
export class BrowseDialog {

    constructor(private dialog: MatDialogRef<BrowseDialog>) {
    }

    cancel(): void {
        this.dialog.close();
    }

    open(path: string): void {
        this.dialog.close(path);
    }
}
