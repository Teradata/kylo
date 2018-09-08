import {Component, Inject, OnDestroy, OnInit} from "@angular/core";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {FileMetadataTransformService} from "../../../catalog/datasource/preview-schema/service/file-metadata-transform.service";
import {PreviewDatasetCollectionService} from "../../../catalog/api/services/preview-dataset-collection.service";
import {PreviewSchemaService} from "../../../catalog/datasource/preview-schema/service/preview-schema.service";
import {PreviewDataSet} from "../../../catalog/datasource/preview-schema/model/preview-data-set";

export class DatasetPreviewDialogData {

constructor(public dataset:PreviewDataSet)  {  }


}
@Component({
    selector: "dataset-preview-dialog",
    styleUrls:["js/feed-mgr/catalog-dataset-preview/preview-stepper/preview-dialog/dataset-preview-dialog.component.css"],
    templateUrl: "js/feed-mgr/catalog-dataset-preview/preview-stepper/preview-dialog/dataset-preview-dialog.component.html"
})
export class DatasetPreviewDialogComponent  implements OnInit, OnDestroy{
    static DIALOG_CONFIG() {
        return { panelClass: "full-screen-dialog"}
    };

    dataset:PreviewDataSet = null;


    constructor(private dialog: MatDialogRef<DatasetPreviewDialogComponent>,
                private _fileMetadataTransformService: FileMetadataTransformService,
                private previewDatasetCollectionService : PreviewDatasetCollectionService,
                private previewService:PreviewSchemaService,
                @Inject(MAT_DIALOG_DATA) public data: DatasetPreviewDialogData) {
            this.dataset = this.data.dataset;
    }

    /**
     * when initialized set the dataset vars
     */
    ngOnInit(){

    }
    ngOnDestroy(){

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