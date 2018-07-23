import {TableSchema} from "../table-schema";
import {TableFieldPartition} from "../TableFieldPartition";
import {TableFieldPolicy} from "../TableFieldPolicy";
import {DefaultTableSchema} from "../default-table-schema.model";
import {TableColumnDefinition} from "../TableColumnDefinition";
import {ObjectUtils} from "../../../common/utils/object-utils";
import {SourceTableSchema} from "./feed-source-table-schema.model";
import {TableOptions} from "./feed.model";
import {FeedTableSchema} from "./feed-table-schema.model";
import {KyloObject} from "../../../common/common.model";


   export class FeedTableDefinition  implements KyloObject{

        public static OBJECT_TYPE:string = 'FeedTableDefinition'

        public objectType:string = FeedTableDefinition.OBJECT_TYPE;


        tableSchema: FeedTableSchema
        sourceTableSchema: SourceTableSchema
        feedTableSchema: FeedTableSchema
        method: string;
        existingTableName: string;
        structured: boolean;
        targetMergeStrategy: string;
        feedFormat: string;
        targetFormat: string;
        feedTblProperties: string;
        fieldPolicies: TableFieldPolicy[]
        partitions: TableFieldPartition[]
        options: TableOptions
        sourceTableIncrementalDateField: string


        public constructor(init?:Partial<FeedTableDefinition>) {
            //set the defaults
            this.initialize();
            //apply the new object
            Object.assign(this, init);
            //ensure object types
            this.ensureObjectTypes();
        }
        
        initialize(){
                this.tableSchema= new FeedTableSchema(),
                this.sourceTableSchema= new SourceTableSchema(),
                this.feedTableSchema = new FeedTableSchema(),
                this.method= 'SAMPLE_FILE',
                this.existingTableName= null,
                this.structured= false,
                this.targetMergeStrategy= 'DEDUPE_AND_MERGE',
                this.feedFormat= 'ROW FORMAT SERDE \'org.apache.hadoop.hive.serde2.OpenCSVSerde\''
            + ' WITH SERDEPROPERTIES ( \'separatorChar\' = \',\' ,\'escapeChar\' = \'\\\\\' ,\'quoteChar\' = \'"\')'
            + ' STORED AS TEXTFILE',
                this.targetFormat= 'STORED AS ORC',
                this.feedTblProperties= '',
                this.fieldPolicies= [],
                this.partitions= [],
                this.options= {compress: false, compressionFormat: 'NONE', auditLogging: true, encrypt: false, trackHistory: false},
            this.sourceTableIncrementalDateField= null
        }

        update(oldFields:TableColumnDefinition[], oldPartitions:TableFieldPartition[]){

            let tableFieldMap: { [key: string]: TableColumnDefinition; } = {};
            this.tableSchema.fields.forEach((field: TableColumnDefinition, index: number) => {
                field._id = (<TableColumnDefinition> oldFields[index])._id;
                tableFieldMap[field.name] = field;
            });
            this.partitions.forEach((partition: TableFieldPartition, index: number) => {
                let oldPartition = (<TableFieldPartition> oldPartitions[index]);
                partition._id = (<TableFieldPartition> oldPartitions[index])._id;
                //update the columnDef ref
                if (oldPartition.sourceField && tableFieldMap[oldPartition.sourceField] != undefined) {
                    //find the matching column field
                    partition.columnDef = tableFieldMap[oldPartition.sourceField];

                }
            });
            this.ensureTableFieldPolicyTypes();
        }


        public ensureObjectTypes() {
            //ensure the table fields are correct objects
            let tableFieldMap: { [key: string]: TableColumnDefinition; } = {};
            let fields = this.tableSchema.fields.map((field: any) => {

                let tableColumnDef = ObjectUtils.getAs(field,TableColumnDefinition, TableColumnDefinition.OBJECT_TYPE);
                tableFieldMap[tableColumnDef.name] = tableColumnDef;
                return tableColumnDef;
            });
            this.tableSchema.fields = fields;

            //ensure the table partitions are correct objects
            let partitions = this.partitions.map((partition: any) => {
                let partitionObject = ObjectUtils.getAs<TableFieldPartition>(partition,TableFieldPartition,TableFieldPartition.OBJECT_TYPE);
                //ensure it has the correct columnDef ref
                if (partitionObject.sourceField && tableFieldMap[partitionObject.sourceField] != undefined) {
                    //find the matching column field
                    partitionObject.columnDef = tableFieldMap[partitionObject.sourceField];
                }
                return partitionObject;
            });
            this.partitions = partitions;
            this.ensureTableFieldPolicyTypes();

        }

        ensureTableFieldPolicyTypes(){
            let fieldPolicies = this.fieldPolicies.map((policy: any) => {
                return  ObjectUtils.getAs(policy,TableFieldPolicy, TableFieldPolicy.OBJECT_TYPE);
            });
            this.fieldPolicies = fieldPolicies;

        }
        

        syncTableFieldPolicyNames():void{
            this.tableSchema.fields.forEach((columnDef: TableColumnDefinition, index: number) => {
               var name = columnDef.name;
                    if (name != undefined) {
                        this.fieldPolicies[index].name = name;
                        this.fieldPolicies[index].field = columnDef;
                    }
                    else {
                        if (this.fieldPolicies[index].field) {
                            this.fieldPolicies[index].field == null;
                        }
                    }                
            });
            //remove any extra columns in the policies
            while (this.fieldPolicies.length > this.tableSchema.fields.length) {
                this.fieldPolicies.splice(this.tableSchema.fields.length, 1);
            }
        }




    }
