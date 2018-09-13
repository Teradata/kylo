import {Component, Input, OnInit, ViewContainerRef} from "@angular/core";
import {PreviewDataSet} from "../../../catalog/datasource/preview-schema/model/preview-data-set";
import {TdDialogService} from "@covalent/core/dialogs";
import {PreviewFileDataSet} from "../../../catalog/datasource/preview-schema/model/preview-file-data-set";
import {DatasetPreviewStepperService} from "../dataset-preview-stepper.service";

@Component({
    selector: "datasets-preview-container",
    styleUrls: ["js/feed-mgr/catalog-dataset-preview/preview-stepper/preview/dataset-preview-container.component.css"],
    templateUrl: "js/feed-mgr/catalog-dataset-preview/preview-stepper/preview/dataset-preview-container.component.html"
})
export class DatasetPreviewContainerComponent implements OnInit{

    @Input()
    previews:PreviewDataSet[] = [];

    selectedDataSet:PreviewDataSet;


    hasPreviews:boolean;


    constructor(private _tdDialogService:TdDialogService, private viewContainerRef:ViewContainerRef,    private datasetPreviewStepperService:DatasetPreviewStepperService ){

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

      openSchemaParseSettingsDialog(dataset:PreviewDataSet): void {
        if(dataset instanceof PreviewFileDataSet) {
            this.datasetPreviewStepperService.openSchemaParseSettingsDialog(<PreviewFileDataSet>dataset)
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
                    }
                    this.selectedDataSet = undefined
                } else {

                }
            });
        }
    }

}