import {Component, Input, OnInit} from "@angular/core";
import {PreviewDataSet} from "../../../catalog/datasource/preview-schema/model/preview-data-set";

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


    constructor( ){

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



    /**
     * Remove the dataset from the collection
     * @param {PreviewDataSet} dataset
     */
    removeDataSet(dataset:PreviewDataSet){
        if(dataset){
            let previewIndex = this.previews.indexOf(dataset);
            this.previews.splice(previewIndex,1);
        }
    }

}