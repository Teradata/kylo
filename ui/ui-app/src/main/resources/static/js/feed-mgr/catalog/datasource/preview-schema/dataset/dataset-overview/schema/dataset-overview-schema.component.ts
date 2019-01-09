import {Component, Input} from "@angular/core";
import {DatasetSaveResponse} from '../../dataset-info/dataset-save-response';
import {ItemSaveResponse} from '../../../../../../shared/info-item/item-save-response';
import {DatasetLoadingService} from '../../dataset-loading-service';
import * as angular from 'angular';
import {Observable} from 'rxjs/Observable';
import {DatasetService} from '../../dataset-service';
import {TableColumn} from '../../../../../datasource/preview-schema/model/table-view-model';
import {MatDialog} from '@angular/material/dialog';
import {PromptDialogComponent, PromptDialogData, PromptDialogResult} from '../../../../../../shared/prompt-dialog/prompt-dialog.component';
import {SparkDataSet} from '../../../../../../model/spark-data-set.model';

/**
 * Displays dataset overview
 */
@Component({
    selector: "dataset-overview-schema",
    templateUrl: "./dataset-overview-schema.component.html"
})
export class DatasetOverviewSchemaComponent {

    @Input()
    public dataset: SparkDataSet;
    public schemaColumns: TableColumn[];

    constructor(private dialog: MatDialog,
                private datasetService: DatasetService,
                private datasetLoadingService: DatasetLoadingService) {
        let schemaColumns :TableColumn[] = [];
        schemaColumns.push({"name":"name","label":"Column Name","dataType":"string","sortable":true});
        schemaColumns.push({"name":"dataType","label":"Data Type","dataType":"string","sortable":true});
        schemaColumns.push({"name":"description","label":"Description","dataType":"string","sortable":true});
        this.schemaColumns = schemaColumns;
    }

    ngOnInit(): void {
    }

    openPrompt(row: any, columnName: string): void {
        const data = new PromptDialogData();
        data.title = "Add Description?";
        data.hint = "Add description to '" + row['name'] + "' column";
        data.value = row[columnName];
        data.placeholder = "Description";
        const dialogRef = this.dialog.open(PromptDialogComponent, {
            minWidth: 600,
            data: data
        });

        dialogRef.afterClosed().subscribe((result: PromptDialogResult) => {
            if (result && result.isValueUpdated) {
                const beforeSave = angular.copy(this.dataset);
                row[columnName] = result.value;
                this.saveDataset(this.dataset, beforeSave);
            }
        });
    }

    saveDataset(dataset: SparkDataSet, datasetBeforeSave: SparkDataSet): Observable<ItemSaveResponse> {
        this.datasetLoadingService.registerLoading();
        let observable = this.datasetService.saveDataset(dataset);
        observable.subscribe(
            (response: DatasetSaveResponse) => {
                if (response.success) {
                    this.dataset = response.dataset;
                } else {
                    this.dataset = datasetBeforeSave;
                    this.datasetLoadingService.resolveLoading();
                }
                this.datasetLoadingService.resolveLoading();
            },
            error => {
                console.error(error);
                this.dataset = datasetBeforeSave;
                this.datasetLoadingService.resolveLoading();
            }
        );
        return observable;
    }

}
