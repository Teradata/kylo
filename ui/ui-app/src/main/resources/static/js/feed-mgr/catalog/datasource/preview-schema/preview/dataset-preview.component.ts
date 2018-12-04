import {ChangeDetectorRef, Component, Input, OnInit} from "@angular/core";
import {DatasetPreviewDialogComponent, DatasetPreviewDialogData} from "../preview-dialog/dataset-preview-dialog.component";
import {FormGroup} from "@angular/forms";
import {TdDialogService} from "@covalent/core/dialogs";
import {MatDialogConfig, MatTabChangeEvent} from "@angular/material";
import {PreviewSchemaService} from "../service/preview-schema.service";
import {PreviewRawService} from "../service/preview-raw.service";
import {PreviewFileDataSet} from "../model/preview-file-data-set";
import {PreviewDataSet} from "../model/preview-data-set";
import {SchemaParseSettingsDialog} from "../schema-parse-settings-dialog.component";
import {SchemaParser} from "../../../../model/field-policy";
import {PreviewDataSetRequest} from "../model/preview-data-set-request";
import {TdLoadingService} from "@covalent/core/loading";
import {DatasetPreviewService} from "../service/dataset-preview.service";


@Component({
    selector: "dataset-preview",
    styleUrls:["./dataset-preview.component.scss"],
    templateUrl: "./dataset-preview.component.html"
})
export class DatasetPreviewComponent implements OnInit{

    @Input()
    displayTitle?:boolean = true;

    @Input()
    dataset:PreviewDataSet

    @Input()
    formGroup:FormGroup;



    rawReady:boolean = false;

    constructor(private _dialogService: TdDialogService,
                private _loadingService:TdLoadingService,
                private _datasetPreviewService:DatasetPreviewService) {

    }
    ngOnInit(){



    }

    onTabChange($event:MatTabChangeEvent){
        //load Raw data if its not there
        if($event.tab.textLabel.toLowerCase() == "raw"){
            if(this.dataset.hasRaw()){
                this.rawReady = true;
                this.dataset.rawLoading = false;
            }
            if(this.dataset instanceof PreviewFileDataSet) {
                if (!this.dataset.hasRaw() && !this.dataset.hasRawError()) {
                    this._datasetPreviewService.notifyToUpdateView();
                    this.dataset.rawLoading = true;
                    this._datasetPreviewService.previewAsTextOrBinary(<PreviewFileDataSet>this.dataset,false,true).subscribe((ds: PreviewDataSet) => {
                        this.rawReady = true;
                        this.dataset.rawLoading = false;
                        this._datasetPreviewService.notifyToUpdateView();
                    }, (error1: any) => {
                        this.rawReady = true;
                        this.dataset.rawLoading = false;
                        this._datasetPreviewService.notifyToUpdateView();
                    });
                }
            }else {
                //we shouldnt get here since only files have the RAW data... but just in case
                this.dataset.rawLoading = false;
                this.rawReady = true;
            }
        }
        this._datasetPreviewService.notifyToUpdateView();
    }

    openSchemaParseSettingsDialog(dataset:PreviewDataSet): void {
        if(dataset instanceof PreviewFileDataSet) {
            this._datasetPreviewService.openSchemaParseSettingsDialog(<PreviewFileDataSet>dataset).subscribe((ds:PreviewDataSet) => {
                //reapply the final dataset back to the main one
                dataset.applyPreview(ds,false);
                //this.previewDatasetValid.emit(dataset)
            },(error:PreviewFileDataSet) =>{
                dataset.preview = undefined
                let message = error.message || "Preview error";
                dataset.previewError(message)
                //save the schema parser
                dataset.userModifiedSchemaParser = error.schemaParser
              //  this.previewDatasetInvalid.emit(dataset)
            })
        }
    }




    /**
     * Update the dialog and position it in the center and full screen
     *
     */
    fullscreen(){
        if(this.dataset && this.dataset.preview){
            let dialogConfig:MatDialogConfig = DatasetPreviewDialogComponent.DIALOG_CONFIG()
            let dialogData:DatasetPreviewDialogData = new DatasetPreviewDialogData(this.dataset)
            dialogConfig.data = dialogData;
            this._dialogService.open(DatasetPreviewDialogComponent,dialogConfig);
        }
    }



}