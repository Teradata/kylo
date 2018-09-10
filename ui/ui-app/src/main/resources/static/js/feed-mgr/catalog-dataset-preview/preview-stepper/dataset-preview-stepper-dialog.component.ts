import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Inject, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DatasetPreviewStepperComponent, DatasetPreviewStepperSavedEvent} from "./dataset-preview-stepper.component";

export class DatasetPreviewStepperDialogData {

constructor(public saveLabel:string = "Save",public title:string="Browse for data")  {  }


}
@Component({
    selector: "dataset-preview-stepper-dialog",
    styleUrls:["js/feed-mgr/catalog-dataset-preview/preview-stepper/dataset-preview-stepper-dialog.component.scss"],
    templateUrl: "js/feed-mgr/catalog-dataset-preview/preview-stepper/dataset-preview-stepper-dialog.component.html",
    changeDetection:ChangeDetectionStrategy.OnPush
})
export class DatasetPreviewStepperDialogComponent  implements OnInit, OnDestroy{
    static DIALOG_CONFIG() {
        return { panelClass: "full-screen-dialog-max-width"}
    };

    @ViewChild("datasetStepper")
    datasetStepper:DatasetPreviewStepperComponent;



    constructor(private dialog: MatDialogRef<DatasetPreviewStepperDialogComponent>,
                @Inject(MAT_DIALOG_DATA) public data: DatasetPreviewStepperDialogData,
                private cd:ChangeDetectorRef) {
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
    onNext(){
        this.datasetStepper.next()
    }
    onBack(){
        this.datasetStepper.previous();
    }

    showBack(){
        return this.datasetStepper.showBack();
    }
    showNext(){
       return this.datasetStepper.showNext();
    }

    nextDisabled(){
        return  this.datasetStepper.nextDisabled();
    }

    showSave(){
        return this.datasetStepper.showSave();
    }
    saveDisabled(){
        return this.datasetStepper.saveDisabled();
    }
    onSave(){
        this.datasetStepper.onSave();
    }
}