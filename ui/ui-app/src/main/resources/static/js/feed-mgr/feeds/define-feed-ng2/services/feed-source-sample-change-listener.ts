import {PreviewDataSet} from "../../../catalog/datasource/preview-schema/model/preview-data-set";
import {TableSchemaUpdateMode} from "./define-feed.service";
import {TdDialogService} from "@covalent/core/dialogs";
import {TableColumnDefinition} from "../../../model/TableColumnDefinition";
import {TableColumn} from "../../../catalog/datasource/preview-schema/model/table-view-model";
import {TableFieldPolicy} from "../../../model/TableFieldPolicy";
import {Feed} from "../../../model/feed/feed.model";
import {Injectable} from "@angular/core";
import * as _ from "underscore";
import {FeedConstants} from "../../../services/FeedConstants";
import {FeedStepConstants} from "../../../model/feed/feed-step-constants";

@Injectable()
export class FeedSourceSampleChange {

    dataTransformationListener:DataTransformationSourceChangeStrategy;

    defineTableListener:DefineTableSourceChangeStrategy;

    constructor(private _dialogService: TdDialogService){
        this.dataTransformationListener = new DataTransformationSourceChangeStrategy(this._dialogService);
        this.defineTableListener = new DefineTableSourceChangeStrategy(this._dialogService);
    }

     dataSetCollectionChanged(dataSets:PreviewDataSet[], feed:Feed) :void{
        if(dataSets.length >0){
            feed.getStepBySystemName(FeedStepConstants.STEP_SOURCE_SAMPLE).setComplete(true)
            feed.getStepBySystemName(FeedStepConstants.STEP_SOURCE_SAMPLE).valid = true;
            feed.getStepBySystemName(FeedStepConstants.STEP_SOURCE_SAMPLE).updateStepState()

        }
        if(feed.isDataTransformation()){
            this.dataTransformationListener.dataSetCollectionChanged(dataSets,feed)
        }
        else  if(feed.isDefineTable()){
            this.defineTableListener.dataSetCollectionChanged(dataSets,feed)
        }
    }
}

export interface FeedSourceSampleChangeListener{
    dataSetCollectionChanged(dataSets:PreviewDataSet[], feed:Feed):void;
}

export class DataTransformationSourceChangeStrategy implements FeedSourceSampleChangeListener{


    constructor(private _dialogService: TdDialogService){

    }
    dataSetCollectionChanged(dataSets:PreviewDataSet[], feed:Feed):void{

        console.log("DATASET COLLECTION CHANGED WRANGLER!!!!")
        //invalidate Wrangler step
        feed.dataTransformation.reset();
        feed.getStepBySystemName(FeedStepConstants.STEP_WRANGLER).setComplete(false);


        //invalidate target step
        feed.getStepBySystemName(FeedStepConstants.STEP_FEED_TARGET).setComplete(false);
        feed.getStepBySystemName(FeedStepConstants.STEP_WRANGLER).updateStepState();
        feed.getStepBySystemName(FeedStepConstants.STEP_FEED_TARGET).updateStepState();
    }



}



export class DefineTableSourceChangeStrategy implements FeedSourceSampleChangeListener{

    constructor(private _dialogService: TdDialogService){

    }

    dataSetCollectionChanged(dataSets:PreviewDataSet[], feed:Feed):void{


        let dataSet :PreviewDataSet = dataSets[0];
        if(dataSet) {
            //compare existing source against this source
            //    let existingSourceColumns = this.feed.table.sourceTableSchema.fields.map(col => col.name+" "+col.derivedDataType).toString();
            //   let dataSetColumns = dataSet.schema.map(col => col.name+" "+col.dataType).toString();

            // check if the dataset source and item name match the feed
            let previewSparkDataSet = dataSet.toSparkDataSet();
            let key = previewSparkDataSet.id
            let dataSourceId = previewSparkDataSet.dataSource.id;
            let matchingFeedDataSet = feed.sourceDataSets.find(sparkDataset => sparkDataset.id == key && sparkDataset.dataSource.id == dataSourceId);

            if (matchingFeedDataSet == undefined || feed.table.tableSchema.fields.length == 0) {
                //the source differs from the feed source and if we have a target defined.... confirm change
                if (feed.table.tableSchema.fields.length > 0) {
                    this._dialogService.openConfirm({
                        message: 'The source schema has changed.  A target schema already exists for this feed.  Do you wish to reset the target schema to match the new source schema? ',
                        disableClose: true,
                        title: 'Source dataset already defined', //OPTIONAL, hides if not provided
                        cancelButton: 'Cancel', //OPTIONAL, defaults to 'CANCEL'
                        acceptButton: 'Accept', //OPTIONAL, defaults to 'ACCEPT'
                        width: '500px', //OPTIONAL, defaults to 400px
                    }).afterClosed().subscribe((accept: boolean) => {

                        if (accept) {
                            this._updateTableSchemas(dataSet, TableSchemaUpdateMode.UPDATE_SOURCE_AND_TARGET,feed)
                        } else {
                            // no op
                            this._updateTableSchemas(dataSet, TableSchemaUpdateMode.UPDATE_SOURCE,feed);
                        }
                    });
                }
                else {
                    //this will be if its the first time a source is selected for a feed
                    this._updateTableSchemas(dataSet, TableSchemaUpdateMode.UPDATE_SOURCE_AND_TARGET,feed);
                }

            }
            else {
            //    console.log("No change to source schema found.  ");
            }
        }


    }



    /**
     * Update the feed table schemas for a given dataset previewed
     * @param {PreviewDataSet} dataSet
     * @param {TableSchemaUpdateMode} mode
     * @private
     */
    private _updateTableSchemas(dataSet:PreviewDataSet,mode:TableSchemaUpdateMode, feed:Feed){
        if(dataSet){
            let sourceColumns: TableColumnDefinition[] = [];
            let targetColumns: TableColumnDefinition[] = [];
            let feedColumns: TableColumnDefinition[] = [];

            let columns: TableColumn[] = dataSet.schema
            if(columns) {
                columns.forEach(col => {
                    let def = _.extend({}, col);
                    def.derivedDataType = def.dataType;
                    //sample data
                    if (dataSet.preview) {
                        let sampleValues: string[] = dataSet.preview.columnData(def.name)
                        def.sampleValues = sampleValues
                    }
                    sourceColumns.push(new TableColumnDefinition((def)));
                    targetColumns.push(new TableColumnDefinition((def)));
                    feedColumns.push(new TableColumnDefinition((def)));
                });
            }
            else {
                //WARN Columns are empty.
                console.log("EMPTY columns for ",dataSet);
            }
            if(TableSchemaUpdateMode.UPDATE_SOURCE == mode || TableSchemaUpdateMode.UPDATE_SOURCE_AND_TARGET == mode){
                feed.sourceDataSets = [dataSet.toSparkDataSet()];
                feed.table.sourceTableSchema.fields = sourceColumns;
            }
            if(TableSchemaUpdateMode.UPDATE_TARGET == mode || TableSchemaUpdateMode.UPDATE_SOURCE_AND_TARGET == mode) {
                feed.table.feedTableSchema.fields = feedColumns;
                feed.table.tableSchema.fields = targetColumns;
                feed.table.fieldPolicies = targetColumns.map(field => {
                    let policy = TableFieldPolicy.forName(field.name);
                    policy.field = field;
                    field.fieldPolicy = policy;
                    return policy;
                });
                //flip the changed flag
                feed.table.schemaChanged = true;
            }


        }
        else {
            if(TableSchemaUpdateMode.UPDATE_SOURCE == mode || TableSchemaUpdateMode.UPDATE_SOURCE_AND_TARGET == mode){
                feed.sourceDataSets = [];
                feed.table.sourceTableSchema.fields = [];
            }
            if(TableSchemaUpdateMode.UPDATE_TARGET == mode || TableSchemaUpdateMode.UPDATE_SOURCE_AND_TARGET == mode) {
                feed.table.feedTableSchema.fields = [];
                feed.table.tableSchema.fields = [];
                feed.table.fieldPolicies = [];
            }
        }
    }

}