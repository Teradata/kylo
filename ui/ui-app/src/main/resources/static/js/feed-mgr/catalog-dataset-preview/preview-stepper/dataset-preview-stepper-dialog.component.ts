import {Component, Inject, OnDestroy, OnInit} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DatasetPreviewStepperSavedEvent} from "./dataset-preview-stepper.component";

export class DatasetPreviewStepperDialogData {

constructor(public saveLabel:string = "Save",public title:string="Browse for data")  {  }


}
@Component({
    selector: "dataset-preview-stepper-dialog",
    templateUrl: "js/feed-mgr/catalog-dataset-preview/preview-stepper/dataset-preview-stepper-dialog.component.html"
})
export class DatasetPreviewStepperDialogComponent  implements OnInit, OnDestroy{
    static DIALOG_CONFIG() {
        return { panelClass: "full-screen-dialog-max-width"}
    };


    constructor(private dialog: MatDialogRef<DatasetPreviewStepperDialogComponent>,
                @Inject(MAT_DIALOG_DATA) public data: DatasetPreviewStepperDialogData) {
    }


    ngOnInit(){

    }
    ngOnDestroy(){

    }

    /**
     * Cancel this dialog.
     */
    cancel() {
            this.dialog.close();
    }

    onSaved(saveEvent:DatasetPreviewStepperSavedEvent) {
        this.dialog.close(saveEvent);
    }


}