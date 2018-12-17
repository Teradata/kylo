import {ChangeDetectionStrategy, ChangeDetectorRef, Component, Inject, Input, OnDestroy, OnInit, ViewChild} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {DatasetPreviewStepperComponent, DatasetPreviewStepperSavedEvent} from "./dataset-preview-stepper.component";
import {PreviewStepperStep} from "./preview-stepper-step";

export class DatasetPreviewStepperDialogData {

  additionalSteps:PreviewStepperStep<any>[] = []

constructor(public allowMultiSelection:boolean=false,public saveLabel:string = "Save",public title:string="Browse for data")  {  }



}
@Component({
    selector: "dataset-preview-stepper-dialog",
    styleUrls:["./dataset-preview-stepper-dialog.component.scss"],
    templateUrl: "./dataset-preview-stepper-dialog.component.html",
    changeDetection:ChangeDetectionStrategy.OnPush
})
export class DatasetPreviewStepperDialogComponent  implements OnInit, OnDestroy{
    static DIALOG_CONFIG() {
        return { panelClass: "full-screen-dialog-max-width",disableClose:true}
    };

    @ViewChild("datasetStepper")
    datasetStepper:DatasetPreviewStepperComponent;



    constructor(private dialog: MatDialogRef<DatasetPreviewStepperDialogComponent>,
                @Inject(MAT_DIALOG_DATA) public data: DatasetPreviewStepperDialogData,
                private cd:ChangeDetectorRef) {
    }


    ngOnInit(){
        if(this.data && this.data.additionalSteps){
            this.data.additionalSteps.forEach((step:PreviewStepperStep<any>) => {
                step.init()
            })
        }

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
        return this.datasetStepper.showSave() && this.getDataSetsLength() >0;
    }
    saveDisabled(){
        return this.datasetStepper.saveDisabled();
    }
    onSave(){
        let saveEvent = this.datasetStepper.getPreviewSaveEvent();
        if(this.data.additionalSteps){
            //capture additional info
            let lastStep = this.datasetStepper.getLastAdditionalStep();
            if(lastStep != null){
                let savedData = lastStep.onSave();
                saveEvent.data = savedData;
            }
        }
        this.datasetStepper.saved(saveEvent);
    }

    getDatasets(){
        return this.datasetStepper.getDataSets();
    }
    getDataSetsLength(){
        return this.datasetStepper.getDataSetsLength();
    }
}