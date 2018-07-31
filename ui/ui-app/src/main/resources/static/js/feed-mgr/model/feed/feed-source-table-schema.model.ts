import {SchemaField} from "../schema-field";
import {DefaultTableSchema} from "../default-table-schema.model";
import {KyloObject} from "../../../common/common.model";
import {TableColumnDefinition} from "../TableColumnDefinition";
import {ObjectUtils} from "../../../common/utils/object-utils";
import {FeedTableSchema} from "./feed-table-schema.model";

export class SourceTableSchema extends DefaultTableSchema implements KyloObject{
    public static OBJECT_TYPE:string = 'SourceTableSchema'

    public objectType:string = SourceTableSchema.OBJECT_TYPE;


    tableSchema: string;
    public constructor(init?:Partial<SourceTableSchema>) {
        super();
        Object.assign(this, init);
        this.ensureObjectTypes();

    }

}
