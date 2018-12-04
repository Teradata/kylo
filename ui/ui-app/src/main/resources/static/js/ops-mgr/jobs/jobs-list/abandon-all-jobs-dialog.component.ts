import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Component, Inject} from "@angular/core";

/**
 * The Controller used for the abandon all
 */
@Component({
    templateUrl: './abandon-all-jobs-dialog.component.html'
})
export class AbandonAllJobsDialogComponent implements ng.IComponentController {
    counter: number;
    index: number;
    messages: string[];
    messageInterval: number;

    feedName: string;
    message: string;

    ngOnInit() {
        this.feedName = this.data.feedName;
        this.message = "Abandoning the failed jobs for " + this.feedName;
        this.counter = 0;
        this.index = 0;
        this.messages = [];
        this.messages.push("Still working. Abandoning the failed jobs for " + this.feedName);
        this.messages.push("Hang tight. Still working.");
        this.messages.push("Just a little while longer.");
        this.messages.push("Should be done soon.");
        this.messages.push("Still working.  Almost done.");
        this.messages.push("It's taking longer than expected.  Should be done soon.");
        this.messages.push("It's taking longer than expected.  Still working...");
        this.messageInterval = setTimeout(() => { this.updateMessage(); }, 5000);
    }

    constructor(private dialogRef: MatDialogRef<AbandonAllJobsDialogComponent>,
                @Inject(MAT_DIALOG_DATA) private data: any) { }

    hide() {
        this.cancelMessageInterval();
        this.dialogRef.close();
    };

    cancel() {
        this.cancelMessageInterval();
        this.dialogRef.close();
    };

    updateMessage() {
        this.counter++;
        var len = this.messages.length;
        if (this.counter % 2 == 0 && this.counter > 2) {
            this.index = this.index < (len - 1) ? this.index + 1 : this.index;
        }
        this.message = this.messages[this.index];
    }

    cancelMessageInterval() {
        if (this.messageInterval != null) {
            clearInterval(this.messageInterval);
        }
    }
}