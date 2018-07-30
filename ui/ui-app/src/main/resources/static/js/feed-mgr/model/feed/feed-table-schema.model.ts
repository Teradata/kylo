import {DefaultTableSchema} from "../default-table-schema.model";
import {TableColumnDefinition} from "../TableColumnDefinition";
import {FeedTableDefinition} from "./feed-table-definition.model";
import {TableFieldPolicy} from "../TableFieldPolicy";

export class FeedTableSchema extends DefaultTableSchema {

    useUnderscoreInsteadOfSpaces:boolean = true;

    public static OBJECT_TYPE:string = 'FeedTableSchema'

    public objectType:string = FeedTableSchema.OBJECT_TYPE;

    constructor() {
     super();
    }


    addColumn(columnDef?: TableColumnDefinition) :TableColumnDefinition{

        // console.log("addColumn");
        if (columnDef == null) {
            columnDef = new TableColumnDefinition();
        }
        if(!columnDef.name){
            columnDef.name = "col_"+this.fields.length+1;
        }
        if(!columnDef.derivedDataType){
            columnDef.derivedDataType = 'string'
            columnDef.dataTypeDisplay = 'string';
        }




        if (columnDef.sampleValues != null && columnDef.sampleValues.length > 0) {
            columnDef.selectedSampleValue = columnDef.sampleValues[0];
        } else {
            columnDef.selectedSampleValue = null;
        }

        if (this.useUnderscoreInsteadOfSpaces) {
            columnDef.name = StringUtils.replaceSpaces(columnDef.name);
        }

        columnDef.initFeedColumn();
        //add the column to both the source and destination tables as well as the fieldPolicies array
        this.fields.push(columnDef);
return columnDef;

    };


}