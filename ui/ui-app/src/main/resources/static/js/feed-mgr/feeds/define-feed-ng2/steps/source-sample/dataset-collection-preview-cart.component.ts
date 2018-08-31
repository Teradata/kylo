import {DatasetChangeEvent, PreviewDatasetCollectionService} from "../../../../catalog/api/services/preview-dataset-collection.service";
import {FileMetadataTransformService} from "../../../../catalog/datasource/preview-schema/service/file-metadata-transform.service";
import {PreviewDataSet} from "../../../../catalog/datasource/preview-schema/model/preview-data-set";
import {ISubscription} from "rxjs/Subscription";
import {Component, ElementRef, OnDestroy, OnInit} from "@angular/core";
import {PreviewSchemaService} from "../../../../catalog/datasource/preview-schema/service/preview-schema.service";
import {DatasetCollectionPreviewDialogComponent, DatasetCollectionPreviewDialogData, DataSetPreviewMode} from "./dataset-collection-preview-dialog.component";
import {TdDialogService} from "@covalent/core/dialogs";
import {DialogPosition, MatDialogConfig} from "@angular/material/dialog";

@Component({
    selector: "dataset-collection-preview-cart",
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/source-sample/dataset-collection-preview-cart.component.html"
})
export class DatasetCollectionPreviewCartComponent implements OnInit, OnDestroy{

    static CART_WIDTH = '400px';

    static DIALOG_CONFIG():MatDialogConfig {
        return <MatDialogConfig> { panelClass: "full-screen-dialog",width:DatasetCollectionPreviewCartComponent.CART_WIDTH,position:{top:"0",right:'0'}};
    }


    datasetCollectionChangedSubscription:ISubscription;

    datasetCount = 0;

    elementPosition:DialogPosition;

    constructor(private ele:ElementRef,
                  private _fileMetadataTransformService: FileMetadataTransformService,
                  private previewSchemaService:PreviewSchemaService,
                  private previewDatasetCollectionService : PreviewDatasetCollectionService,
                  private _dialogService: TdDialogService) {
        this.datasetCollectionChangedSubscription = this.previewDatasetCollectionService.subscribeToDatasetChanges(this.onDataSetCollectionChanged.bind(this))
    }

    getElementPosition() {
        if(!this.elementPosition) {
            let ele1 = this.ele.nativeElement
            let top = ele1.getBoundingClientRect().top;
            let left = ele1.offsetLeft
            let pos = {left:left+'px',top:top+'px'};
            this.elementPosition = pos;
        }
        return this.elementPosition;
    }

    applyCartPositionSettings(dialogPosition:DialogPosition){
       // let elePosition = this.getElementPosition();
       // dialogPosition.top = elePosition.top;
       // dialogPosition.left = elePosition.left;

        dialogPosition.right='0px'
        dialogPosition.top='0px';
    }

    openCart(){

        //open side dialog
        let dialogData:DatasetCollectionPreviewDialogData = new DatasetCollectionPreviewDialogData(DataSetPreviewMode.CART)
        let config = DatasetCollectionPreviewCartComponent.DIALOG_CONFIG();
        this.applyCartPositionSettings(config.position);
        dialogData.dialogPosition = config.position;
        config.data = dialogData;
        console.log('OPEN DIALOG WITH CONFIG ',config);
        this._dialogService.open(DatasetCollectionPreviewDialogComponent,config);
    }

    /**
     * Callback when datasets are collected
     * @param {DatasetChangeEvent} event
     */
    onDataSetCollectionChanged(event:DatasetChangeEvent){
        this.datasetCount = event.totalDatasets;
    }


    ngOnInit(){
        this.datasetCount = this.previewDatasetCollectionService.datasetCount();
    }
    ngOnDestroy(){
        this.datasetCollectionChangedSubscription.unsubscribe();
    }







}