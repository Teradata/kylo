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


@Component({
    selector: "dataset-preview",
    templateUrl: "js/feed-mgr/catalog-dataset-preview/preview-stepper/preview/dataset-preview.component.html"
})
export class DatasetPreviewComponent implements OnInit{

    @Input()
    displayTitle?:boolean = true;

    @Input()
    dataset:PreviewDataSet

    rawReady:boolean;

    constructor(private _dialogService: TdDialogService,
                private previewSchemaService: PreviewSchemaService,
                private previewRawService:PreviewRawService){

    }
    ngOnInit(){



    }

    onTabChange($event:MatTabChangeEvent){
        if($event.tab.textLabel.toLowerCase() == "raw"){
            if(this.dataset.hasRaw()){
                this.rawReady = true;
            }
            if(this.dataset instanceof PreviewFileDataSet) {

                if (!this.dataset.hasRaw() && !this.dataset.hasRawError()) {

                    this.previewRawService.preview(<PreviewFileDataSet>this.dataset).subscribe((ds: PreviewDataSet) => {
                        this.rawReady = true;
                    }, (error1: any) => {
                        this.rawReady = true;
                    });
                }
            }
        }
    }


    openSchemaParseSettingsDialog(): void {
        let dialogRef = this._dialogService.open(SchemaParseSettingsDialog, {
            width: '500px',
            data: { schemaParser: (<PreviewFileDataSet>this.dataset).schemaParser,
                sparkScript: (<PreviewFileDataSet>this.dataset).sparkScript
            }
        });

        dialogRef.afterClosed().subscribe(result => {

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