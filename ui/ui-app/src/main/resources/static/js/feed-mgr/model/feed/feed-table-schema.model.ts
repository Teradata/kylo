import {DefaultTableSchema} from "../default-table-schema.model";
import {TableColumnDefinition} from "../TableColumnDefinition";
import {FeedTableDefinition} from "./feed-table-definition.model";

export class FeedTableSchema extends DefaultTableSchema {

    public static OBJECT_TYPE:string = 'FeedTableSchema'

    public objectType:string = FeedTableSchema.OBJECT_TYPE;

    constructor() {
super();
    }



}