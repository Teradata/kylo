import {SimpleDynamicFormDialogComponent} from "../../../../shared/dynamic-form/simple-dynamic-form/simple-dynamic-form-dialog.component";
import {Component, Inject, OnDestroy, OnInit} from "@angular/core";
import {DynamicFormDialogData} from "../../../../shared/dynamic-form/simple-dynamic-form/dynamic-form-dialog-data";
import {MatDialogRef,MAT_DIALOG_DATA} from "@angular/material/dialog";
import {DatasetChangeEvent, PreviewDatasetCollectionService} from "../../../../catalog/api/services/preview-dataset-collection.service";
import {FileMetadataTransformService} from "../../../../catalog/datasource/preview-schema/service/file-metadata-transform.service";
import {PreviewSchemaService} from "../../../../catalog/datasource/preview-schema/service/preview-schema.service";
import {ISubscription} from "rxjs/Subscription";
import {PreviewDataSet} from "../../../../catalog/datasource/preview-schema/model/preview-data-set";
import {DialogPosition} from "@angular/material";
import {DatasetCollectionPreviewCartComponent} from "./dataset-collection-preview-cart.component";
import {CloneUtil} from "../../../../../common/utils/clone-util";

export enum DataSetPreviewMode{
    PREVIEW="preview",CART="cart", PREVIEW_CART_MODE ="preview_cart"
}
export class DatasetCollectionPreviewDialogData {

    dialogPosition:DialogPosition;
constructor(public mode:DataSetPreviewMode, public dataset?:PreviewDataSet)  {  }


}
@Component({
    selector: "dataset-collection-preview-dialog",
    styleUrls:["js/feed-mgr/feeds/define-feed-ng2/steps/source-sample/dataset-collection-preview-dialog.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/source-sample/dataset-collection-preview-dialog.component.html"
})
export class DatasetCollectionPreviewDialogComponent  implements OnInit, OnDestroy{
    static DIALOG_CONFIG() {
        return { panelClass: "full-screen-dialog"}
    };

    datasetCollectionChangedSubscription:ISubscription;

    datasetCount = 0;

    datasets:PreviewDataSet[];

    dataset:PreviewDataSet = null;


    step = 0;



    constructor(private dialog: MatDialogRef<DatasetCollectionPreviewDialogComponent>,
                private _fileMetadataTransformService: FileMetadataTransformService,
                private previewDatasetCollectionService : PreviewDatasetCollectionService,
                private previewService:PreviewSchemaService,
                @Inject(MAT_DIALOG_DATA) public data: DatasetCollectionPreviewDialogData) {
        this.datasetCollectionChangedSubscription = this.previewDatasetCollectionService.subscribeToDatasetChanges(this.onDataSetCollectionChanged.bind(this))
        if(this.data.mode == DataSetPreviewMode.PREVIEW){
            this.dataset = this.data.dataset;
        }
    }

    /**
     * when initialized set the dataset vars
     */
    ngOnInit(){
        this.datasetCount = this.previewDatasetCollectionService.datasetCount();
        //copy to a new array
        this.datasets = this.previewDatasetCollectionService.datasets.slice();
    }
    ngOnDestroy(){
        this.datasetCollectionChangedSubscription.unsubscribe();
    }


    /**
     * Callback when datasets are collected
     * @param {DatasetChangeEvent} event
     */
    onDataSetCollectionChanged(event:DatasetChangeEvent){
        this.datasetCount = event.totalDatasets;
        //reorder to push the latest dataset to the top if its newly collected
        let datasets =  event.allDataSets.slice();
        if(event.dataset && event.dataset.isCollected()){
            datasets.sort((x:PreviewDataSet,y:PreviewDataSet) =>{
                return x == event.dataset ? -1 : y == event.dataset ? 1 : 0;
            });
        }
        this.datasets = datasets;

    }



    /**
     * Update the dialog and position it in the center and full screen
     * @param {PreviewDataSet} dataset
     */
    fullscreen(dataset:PreviewDataSet){
        if(dataset){
            this.dataset = dataset;
            this.data.mode = DataSetPreviewMode.PREVIEW_CART_MODE;
            //get the full screen width and height
            this.dialog.updateSize()
            //update position
            this.dialog.updatePosition( )
        }
    }

    /**
     * Remove the dataset from the collection
     * @param {PreviewDataSet} dataset
     */
    removeDataSet(dataset:PreviewDataSet){
        if(dataset){
            this.previewDatasetCollectionService.remove(dataset);
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
     * Cancel this dialog.
     * if previewing and in CART mode then return back to the cart view, otherwise close
     */
    cancel() {
        this.dataset = null
        if(this.data.mode == DataSetPreviewMode.PREVIEW_CART_MODE) {
            console.log('UPDATING POSITION TO BE ',this.data.dialogPosition)
            this.dialog.updatePosition( this.data.dialogPosition)
            this.dialog.updateSize(DatasetCollectionPreviewCartComponent.CART_WIDTH,'100%');
            this.data.mode = DataSetPreviewMode.CART;
        }
        else {
            this.dialog.close();
        }

    }


}