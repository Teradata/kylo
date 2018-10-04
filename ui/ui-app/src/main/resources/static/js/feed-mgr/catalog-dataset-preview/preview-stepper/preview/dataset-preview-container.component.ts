import {Component, Input, OnDestroy, OnInit, ViewContainerRef} from "@angular/core";
import {PreviewDataSet} from "../../../catalog/datasource/preview-schema/model/preview-data-set";
import {TdDialogService} from "@covalent/core/dialogs";
import {PreviewFileDataSet} from "../../../catalog/datasource/preview-schema/model/preview-file-data-set";
import {DatasetPreviewStepperService} from "../dataset-preview-stepper.service";
import {FormGroup} from "@angular/forms";
import {ISubscription} from "rxjs/Subscription";

@Component({
    selector: "datasets-preview-container",
    styleUrls: ["js/feed-mgr/catalog-dataset-preview/preview-stepper/preview/dataset-preview-container.component.scss"],
    templateUrl: "js/feed-mgr/catalog-dataset-preview/preview-stepper/preview/dataset-preview-container.component.html"
})
export class DatasetPreviewContainerComponent implements OnInit, OnDestroy{

    @Input()
    previews:PreviewDataSet[] = [];

    selectedDataSet:PreviewDataSet;


    hasPreviews:boolean;

    @Input()
    formGroup:FormGroup

    stepChangedSubscription:ISubscription

    constructor(private _tdDialogService:TdDialogService, private viewContainerRef:ViewContainerRef,    private datasetPreviewStepperService:DatasetPreviewStepperService ){
this.stepChangedSubscription = this.datasetPreviewStepperService.subscribeToStepChanges(this.onStepChanged.bind(this))
    }
    ngOnInit(){

        if(this.previews == undefined || this.previews.length ==0) {
            if(this.previews == undefined){
                this.previews = [];
            }
        }

        if(this.previews != undefined && this.previews.length > 0) {
            this.hasPreviews = true;
        }
        else {
            this.hasPreviews = false;
        }

    }

    ngOnDestroy(){
        this.stepChangedSubscription.unsubscribe();
    }

    private onStepChanged(idx:number){
        if(idx ==2) {
         this.selectedDataSet = undefined;
        }
    }

      openSchemaParseSettingsDialog(dataset:PreviewDataSet): void {
        if(dataset instanceof PreviewFileDataSet) {
            this.datasetPreviewStepperService.openSchemaParseSettingsDialog(<PreviewFileDataSet>dataset).subscribe((ds:PreviewDataSet) => {
                console.log('DONE!',ds)
                //reapply the final dataset back to the main one
                dataset.applyPreview(ds,false);
                console.log('DONE!',dataset,ds)
                this.datasetPreviewStepperService.markFormAsValid(this.formGroup)
            },(error:PreviewFileDataSet) =>{
                console.log("ERROR ",error)
                dataset.preview = undefined
                let message = error.message || "Preview error";
                dataset.previewError(message)

                //save the schema parser
                dataset.userModifiedSchemaParser = error.schemaParser

                this.datasetPreviewStepperService.markFormAsInvalid(this.formGroup)
            })
        }
    }


    /**
     * Remove the dataset from the collection
     * @param {PreviewDataSet} dataset
     */
    removeDataSet(dataset:PreviewDataSet){
        if(dataset) {
            this._tdDialogService.openConfirm({
                message: "Are you sure you want to remove the dataset " + dataset.displayKey + "?",
                disableClose: true,
                viewContainerRef: this.viewContainerRef, //OPTIONAL
                title: 'Remove dataset', //OPTIONAL, hides if not provided
                cancelButton: 'Cancel', //OPTIONAL, defaults to 'CANCEL'
                acceptButton: 'Remove', //OPTIONAL, defaults to 'ACCEPT'
                width: '500px', //OPTIONAL, defaults to 400px
            }).afterClosed().subscribe((accept: boolean) => {
                if (accept) {
                    let previewIndex = this.previews.indexOf(dataset);
                    this.previews.splice(previewIndex,1);
                    if(this.previews.length == 0){
                        this.hasPreviews = false;
                        this.datasetPreviewStepperService.markFormAsInvalid(this.formGroup)
                    }else {
                        let previewError = this.previews.find(ds => ds.hasPreviewError())
                        if(previewError != undefined){
                            this.datasetPreviewStepperService.markFormAsInvalid(this.formGroup)
                        }
                    }
                    this.selectedDataSet = undefined
                } else {

                }
            });
        }
    }

}