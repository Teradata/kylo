import {Component, EventEmitter, Input, OnDestroy, OnInit, Output, ViewContainerRef} from "@angular/core";
import {PreviewDataSet} from "../model/preview-data-set";
import {TdDialogService} from "@covalent/core/dialogs";
import {PreviewFileDataSet} from "../model/preview-file-data-set";
import {DatasetPreviewService} from "../service/dataset-preview.service";
import {FormGroup} from "@angular/forms";
import {ISubscription} from "rxjs/Subscription";

@Component({
    selector: "datasets-preview-container",
    styleUrls: ["./dataset-preview-container.component.scss"],
    templateUrl: "./dataset-preview-container.component.html"
})
export class DatasetPreviewContainerComponent implements OnInit{

    @Input()
    previews:PreviewDataSet[] = [];

    /**
     * If there is only 1 dataset to preview, should it jump right to the preview
     * if set to true and the previews collection == 1 then it will auto set the first item as the "selectedDataSet" and wont show the nav list
     *
     * @type {boolean}
     */
    @Input()
    autoSelectSingleDataSet:boolean = false;

    /**
     * The selected dataset to preview
     */
    selectedDataSet:PreviewDataSet;

    /**
     * Flag to tell the UI that previews exist
     */
    hasPreviews:boolean;

    @Input()
    formGroup:FormGroup

    @Output()
    previewDatasetValid = new EventEmitter<PreviewDataSet>()

    @Output()
    previewDatasetInvalid = new EventEmitter<PreviewDataSet>()

    @Output()
    previewSelectionChange = new EventEmitter<PreviewDataSet>();

    @Input()
    renderBackButton:boolean = true;


    constructor(private _tdDialogService:TdDialogService, private viewContainerRef:ViewContainerRef,    private _datasetPreviewService:DatasetPreviewService ){

    }
    ngOnInit(){

        if(this.previews == undefined || this.previews.length ==0) {
            if(this.previews == undefined){
                this.previews = [];
            }
        }

        if(this.previews != undefined && this.previews.length > 0) {
            this.hasPreviews = true;
            if(this.previews.length == 1 && this.autoSelectSingleDataSet) {
                this.selectDataSet(this.previews[0])
            }
        }
        else {
            this.hasPreviews = false;
        }

    }

    selectDataSet(dataSet:PreviewDataSet){
        if(dataSet && dataSet != null) {
            this.selectedDataSet = dataSet;
        }
        else {
            this.selectedDataSet = undefined;
        }
        this.previewSelectionChange.emit(this.selectedDataSet)
    }

      openSchemaParseSettingsDialog(dataset:PreviewDataSet): void {
        if(dataset instanceof PreviewFileDataSet) {
            this._datasetPreviewService.openSchemaParseSettingsDialog(<PreviewFileDataSet>dataset).subscribe((ds:PreviewDataSet) => {
                console.log('DONE!',ds)
                //reapply the final dataset back to the main one
                dataset.applyPreview(ds,false);
                console.log('DONE!',dataset,ds)
                this.previewDatasetValid.emit(dataset)
               // this._datasetPreviewService.markFormAsValid(this.formGroup)
            },(error:PreviewFileDataSet) =>{
                console.log("ERROR ",error)
                dataset.preview = undefined
                let message = error.message || "Preview error";
                dataset.previewError(message)

                //save the schema parser
                dataset.userModifiedSchemaParser = error.schemaParser
                this.previewDatasetInvalid.emit(dataset)
             //   this._datasetPreviewService.markFormAsInvalid(this.formGroup)
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
                        this.previewDatasetInvalid.emit(dataset)
                        //this._datasetPreviewService.markFormAsInvalid(this.formGroup)
                    }else {
                        let previewError = this.previews.find(ds => ds.hasPreviewError())
                        if(previewError != undefined){
                            this.previewDatasetInvalid.emit(dataset)
                            //this._datasetPreviewService.markFormAsInvalid(this.formGroup)
                        }
                    }
                    this.selectedDataSet = undefined
                } else {

                }
            });
        }
    }

}