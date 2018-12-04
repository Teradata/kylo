import {Component, Input, OnInit} from "@angular/core";
import {PreviewDataSet} from "../model/preview-data-set";

@Component({
    selector: "datasets-preview-accordion-container",
    templateUrl: "./dataset-preview-container-accordion.component.html"
})
export class DatasetPreviewContainerAccordionComponent implements OnInit{

    @Input()
    previews:PreviewDataSet[] = [];

    @Input()
    expandToFirst?:boolean = false;

    @Input()
    accordionShowFullScreen?:boolean = false;

    step = 0;

    firstDataSet:PreviewDataSet;

    singleDataSet:boolean;

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
            if(this.expandToFirst){
                this.step = 0;
            }
            else {
                this.step = -1;
            }
            this.singleDataSet = this.previews.length == 1 ? true : false;
            this.firstDataSet = this.previews[0];
            this.hasPreviews = true;
        }
        else {
            this.hasPreviews = false;
        }

    }





    /**
     * set the current cart item
     * @param {number} index
     */
    setStep(index: number) {
        this.step = index;
    }

    /**
     * navigate to the next cart item
     */
    nextStep() {
        this.step++;
    }

    /**
     * navigate to the prev cart item
     */
    prevStep() {
        this.step--;
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