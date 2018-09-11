import {Component, Input, OnInit} from "@angular/core";
import {DatasetPreviewDialogComponent, DatasetPreviewDialogData} from "../preview-dialog/dataset-preview-dialog.component";
import {FormGroup} from "@angular/forms";
import {TdDialogService} from "@covalent/core/dialogs";
import {MatDialogConfig, MatTabChangeEvent} from "@angular/material";
import {PreviewSchemaService} from "../../../catalog/datasource/preview-schema/service/preview-schema.service";
import {PreviewRawService} from "../../../catalog/datasource/preview-schema/service/preview-raw.service";
import {PreviewFileDataSet} from "../../../catalog/datasource/preview-schema/model/preview-file-data-set";
import {PreviewDataSet} from "../../../catalog/datasource/preview-schema/model/preview-data-set";
import {SchemaParseSettingsDialog} from "../../../catalog/datasource/preview-schema/schema-parse-settings-dialog.component";
import {SchemaParser} from "../../../model/field-policy";
import {PreviewDataSetRequest} from "../../../catalog/datasource/preview-schema/model/preview-data-set-request";
import {TdLoadingService} from "@covalent/core/loading";
import {DatasetPreviewStepperService} from "../dataset-preview-stepper.service";


@Component({
    selector: "dataset-preview",
    styleUrls:["js/feed-mgr/catalog-dataset-preview/preview-stepper/preview/dataset-preview.component.scss"],
    templateUrl: "js/feed-mgr/catalog-dataset-preview/preview-stepper/preview/dataset-preview.component.html"
})
export class DatasetPreviewComponent implements OnInit{

    @Input()
    displayTitle?:boolean = true;

    @Input()
    dataset:PreviewDataSet

    static PREVIEW_LOADING = "DatasetPreviewComponent.previewLoading"

    static RAW_LOADING = "DatasetPreviewComponent.rawLoading"

    rawReady:boolean;

    constructor(private _dialogService: TdDialogService,
                private _loadingService:TdLoadingService,
                private previewSchemaService: PreviewSchemaService,
                private datasetPreviewStepperService:DatasetPreviewStepperService,
                private previewRawService:PreviewRawService){

    }
    ngOnInit(){



    }

    onTabChange($event:MatTabChangeEvent){
        //load Raw data if its not there
        if($event.tab.textLabel.toLowerCase() == "raw"){
            if(this.dataset.hasRaw()){
                this.rawReady = true;
            }
            if(this.dataset instanceof PreviewFileDataSet) {

                if (!this.dataset.hasRaw() && !this.dataset.hasRawError()) {
                    this.datasetPreviewStepperService.notifyToUpdateView();
                    this._loadingService.register(DatasetPreviewComponent.RAW_LOADING)
                    this.previewRawService.preview(<PreviewFileDataSet>this.dataset).subscribe((ds: PreviewDataSet) => {
                        this._loadingService.resolve(DatasetPreviewComponent.RAW_LOADING)
                        this.rawReady = true;
                        this.datasetPreviewStepperService.notifyToUpdateView();
                    }, (error1: any) => {
                        this.rawReady = true;
                        this._loadingService.resolve(DatasetPreviewComponent.RAW_LOADING)
                        this.datasetPreviewStepperService.notifyToUpdateView();
                    });
                }
            }
        }
        this.datasetPreviewStepperService.notifyToUpdateView();
    }


    openSchemaParseSettingsDialog(): void {
        let dialogRef = this._dialogService.open(SchemaParseSettingsDialog, {
            width: '500px',
            data: { schemaParser: (<PreviewFileDataSet>this.dataset).schemaParser,
                sparkScript: (<PreviewFileDataSet>this.dataset).sparkScript
            }
        });

        dialogRef.afterClosed().filter(result => result != undefined).subscribe((result:SchemaParser) => {
            (<PreviewFileDataSet>this.dataset).schemaParser = result

            let previewRequest = new PreviewDataSetRequest();
            previewRequest.dataSource = this.dataset.dataSource;
            //reset the preview
            this.dataset.preview = undefined;
            this._loadingService.register(DatasetPreviewComponent.PREVIEW_LOADING)
            this.datasetPreviewStepperService.notifyToUpdateView();
            this.previewSchemaService.preview(this.dataset,previewRequest).subscribe((result:PreviewDataSet) => {
                console.log('FINISHED PREVIEW!!!',result)
                this._loadingService.resolve(DatasetPreviewComponent.PREVIEW_LOADING)
                this.datasetPreviewStepperService.notifyToUpdateView();
            },error1 => {
                this._loadingService.resolve(DatasetPreviewComponent.PREVIEW_LOADING)
                this.datasetPreviewStepperService.notifyToUpdateView();
            })
            ///update it
        });
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