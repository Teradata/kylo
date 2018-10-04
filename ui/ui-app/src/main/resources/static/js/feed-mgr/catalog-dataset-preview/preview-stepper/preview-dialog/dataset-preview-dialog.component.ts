import {Component, Inject, OnDestroy, OnInit} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {FileMetadataTransformService} from "../../../catalog/datasource/preview-schema/service/file-metadata-transform.service";
import {PreviewDatasetCollectionService} from "../../../catalog/api/services/preview-dataset-collection.service";
import {PreviewSchemaService} from "../../../catalog/datasource/preview-schema/service/preview-schema.service";
import {PreviewDataSet} from "../../../catalog/datasource/preview-schema/model/preview-data-set";
import {TdLoadingService} from "@covalent/core/loading";
import {DatasetPreviewStepperService, PreviewDataSetResultEvent} from "../dataset-preview-stepper.service";
import {DataSource} from "../../../catalog/api/models/datasource";
import {BrowserObject} from "../../../catalog/api/models/browser-object";

export class DatasetPreviewDialogData {

public file?: BrowserObject;
public datasource?:DataSource;
constructor(public dataset?:PreviewDataSet)  {  }


}
@Component({
    selector: "dataset-preview-dialog",
    styleUrls:["js/feed-mgr/catalog-dataset-preview/preview-stepper/preview-dialog/dataset-preview-dialog.component.scss"],
    templateUrl: "js/feed-mgr/catalog-dataset-preview/preview-stepper/preview-dialog/dataset-preview-dialog.component.html"
})
export class DatasetPreviewDialogComponent  implements OnInit, OnDestroy{
    static DIALOG_CONFIG() {
        return { panelClass: "full-screen-dialog"}
    };
    static LOADER = "DatasetPreviewDialogComponent.LOADING"

    dataset:PreviewDataSet = null;

    error:boolean = false;
    errorTitle:string;
    errorMessage:string;


    constructor(private dialog: MatDialogRef<DatasetPreviewDialogComponent>,
                private _fileMetadataTransformService: FileMetadataTransformService,
                private previewDatasetCollectionService : PreviewDatasetCollectionService,
                private _datasetPreviewStepperService:DatasetPreviewStepperService,
                private _tdLoadingService:TdLoadingService,
                private previewService:PreviewSchemaService,
                @Inject(MAT_DIALOG_DATA) public data: DatasetPreviewDialogData) {
            this.dataset = this.data.dataset;

    }

    /**
     * when initialized set the dataset vars
     */
    ngOnInit(){
        if(!this.dataset && this.data.file && this.data.datasource) {
            this._tdLoadingService.register(DatasetPreviewDialogComponent.LOADER)
            this._datasetPreviewStepperService.prepareAndPopulatePreviewDataSet(this.data.file, this.data.datasource).subscribe((ev: PreviewDataSetResultEvent) => {
                if (!ev.isEmpty()) {
                    this.dataset = ev.dataSets[0]
                    this._tdLoadingService.resolve(DatasetPreviewDialogComponent.LOADER);
                }
                else {
                    //SHOW ERROR
                    this.errorTitle="Unable to preview the data";
                    this.errorMessage="There was an error previewing your data ";
                    this.error = true;
                    this._tdLoadingService.resolve(DatasetPreviewDialogComponent.LOADER);

                }
            });
        }
        else {
            //TODO ERROR!!!!
            this.errorTitle="Unable to preview the data";
            this.errorMessage="No data has been selected to preview";
            this.error = true;

        }
    }
    ngOnDestroy(){
        this._tdLoadingService.resolve(DatasetPreviewDialogComponent.LOADER);
    }

    /**
     * Cancel this dialog.
     * if previewing and in CART mode then return back to the cart view, otherwise close
     */
    cancel() {
        this.dataset = null
            this.dialog.close();
    }


}