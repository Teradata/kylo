import * as _ from "underscore";
import {TableSchema} from "../table-schema";
import {TableFieldPartition} from "../TableFieldPartition";
import {TableFieldPolicy} from "../TableFieldPolicy";
import {DefaultTableSchema} from "../default-table-schema.model";
import {TableColumnDefinition} from "../TableColumnDefinition";
import {ObjectUtils} from "../../../../lib/common/utils/object-utils";
import {SourceTableSchema} from "./feed-source-table-schema.model";
import {TableOptions} from "./feed.model";
import {FeedTableSchema} from "./feed-table-schema.model";
import {KyloObject} from "../../../../lib/common/common.model";
import {SchemaField} from "../schema-field";
import {CloneUtil} from "../../../common/utils/clone-util";


   export class FeedTableDefinition  implements KyloObject{

        public static OBJECT_TYPE:string = 'FeedTableDefinition'

        public objectType:string = FeedTableDefinition.OBJECT_TYPE;


        tableSchema: FeedTableSchema
        sourceTableSchema: SourceTableSchema
        feedTableSchema: FeedTableSchema;
        feedDefinitionTableSchema:FeedTableSchema;
        method: string;
        existingTableName: string;
        structured: boolean;
        targetMergeStrategy: string;
        feedFormat: string;
        targetFormat: string;
        feedTblProperties: string;
        fieldPolicies: TableFieldPolicy[]
        feedDefinitionFieldPolicies:TableFieldPolicy[];
        partitions: TableFieldPartition[]
        options: TableOptions
        sourceTableIncrementalDateField: string
        schemaChanged:boolean;


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
                this.feedDefinitionTableSchema = new FeedTableSchema(),
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
                this.feedDefinitionFieldPolicies = [],
                this.partitions= [],
                this.options= {compress: false, compressionFormat: 'NONE', auditLogging: true, encrypt: false, trackHistory: false},
            this.sourceTableIncrementalDateField= null
        }

        update(oldFields:TableColumnDefinition[], oldPartitions:TableFieldPartition[]){

            let tableFieldMap: { [key: string]: TableColumnDefinition; } = {};
            this.feedDefinitionTableSchema.fields.forEach((field: TableColumnDefinition, index: number) => {
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

           // this.tableSchema = ObjectUtils.getAs(this.tableSchema,FeedTableSchema);
           // this.sourceTableSchema = ObjectUtils.getAs(this.sourceTableSchema,SourceTableSchema);
           // this.feedTableSchema = ObjectUtils.getAs(this.feedTableSchema,FeedTableSchema);
        }


        public ensureObjectTypes() {

            this.tableSchema = ObjectUtils.getAs(this.tableSchema,FeedTableSchema);
            this.feedDefinitionTableSchema =  (this.feedDefinitionTableSchema && this.feedDefinitionTableSchema.fields.length >0)  ? ObjectUtils.getAs(this.feedDefinitionTableSchema,FeedTableSchema) : CloneUtil.deepCopy(this.tableSchema);
            //ensure the table fields are correct objects
            let tableFieldMap: { [key: string]: TableColumnDefinition; } = {};
            this.feedDefinitionTableSchema.fields.forEach((field: TableColumnDefinition) => {
                tableFieldMap[field.name] = field;
            });

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

            this.sourceTableSchema = ObjectUtils.getAs(this.sourceTableSchema,SourceTableSchema);
            this.feedTableSchema = ObjectUtils.getAs(this.feedTableSchema,FeedTableSchema);

            this.ensureTableFieldPolicyTypes();

        }

        private fieldPolicyAsObject(policy:any,isTarget:boolean) {
            let policyObj =  ObjectUtils.getAs(policy,TableFieldPolicy, TableFieldPolicy.OBJECT_TYPE);
            let columnDef = isTarget ? this.getTargetColumnFieldByName(policyObj.fieldName) : this.getColumnDefinitionByName(policyObj.fieldName);
            policyObj.field = columnDef;
            columnDef.fieldPolicy = policyObj;
            return policyObj;
        }

        ensureTableFieldPolicyTypes(){

            this.feedDefinitionFieldPolicies = this.feedDefinitionFieldPolicies && this.feedDefinitionFieldPolicies.length >0 ?this.feedDefinitionFieldPolicies : this.fieldPolicies;
            this.feedDefinitionFieldPolicies = this.feedDefinitionFieldPolicies.map((policy: any) => this.fieldPolicyAsObject(policy,false));
            this.fieldPolicies = this.fieldPolicies.map((policy: any) => this.fieldPolicyAsObject(policy,true));

        }

        syncFieldPolicy(columnDef: TableColumnDefinition, index: number){
            var name = columnDef.name;
            if (name != undefined) {
                this.feedDefinitionFieldPolicies[index].name = name;
                this.feedDefinitionFieldPolicies[index].fieldName = name;
                this.feedDefinitionFieldPolicies[index].field = columnDef;
                columnDef.fieldPolicy = this.feedDefinitionFieldPolicies[index];
            }
            else {
                if (this.feedDefinitionFieldPolicies[index].field) {
                    this.feedDefinitionFieldPolicies[index].field == null;
                    columnDef.fieldPolicy = undefined;
                }
            }
        }
        

        syncTableFieldPolicyNames():void{
            this.feedDefinitionTableSchema.fields.forEach((columnDef: TableColumnDefinition, index: number) => {
              this.syncFieldPolicy(columnDef, index);
            });
            //remove any extra columns in the policies
            while (this.feedDefinitionFieldPolicies.length > this.feedDefinitionTableSchema.fields.length) {
                this.feedDefinitionFieldPolicies.splice(this.feedDefinitionTableSchema.fields.length, 1);
            }
        }


       /**
        * For a given list of incoming Table schema fields ({@see this#newTableFieldDefinition}) it will create a new FieldPolicy object ({@see this#newTableFieldPolicy} for it
        */
       setTableFields(fields: TableColumnDefinition[] | SchemaField[], policies: TableFieldPolicy[] = null) {
           //ensure the fields are of type TableColumnDefinition
           let newFields =  _.map(fields,(field) => {
               if(!field['objectType'] || field['objectType'] != 'TableColumnDefinition' ){
                   return new TableColumnDefinition(field);
               }
               else {
                   return field;
               }
           })
           this.feedDefinitionTableSchema.fields = newFields;
           this.feedDefinitionFieldPolicies = (policies != null && policies.length > 0) ? policies : newFields.map(field => TableFieldPolicy.forName(field.name));

       }





       /**
        * Adding a new Column to the schema
        * This is called both when the user clicks the "Add Field" button or when the sample file is uploaded
        * If adding from the UI the {@code columnDef} will be null, otherwise it will be the parsed ColumnDef from the sample file
        * @param columnDef
        */
       addColumn(columnDef?: TableColumnDefinition, syncFieldPolicies?: boolean) :TableColumnDefinition{

           //add to the fields
           let newColumn = this.feedDefinitionTableSchema.addColumn(columnDef);

           // when adding a new column this is also called to synchronize the field policies array with the columns
           let policy = TableFieldPolicy.forName(newColumn.name);
           this.feedDefinitionFieldPolicies.push(policy);
           newColumn.fieldPolicy = policy;
           policy.field = columnDef;
           this.sourceTableSchema.fields.push(newColumn.copy());
           if (syncFieldPolicies == undefined || syncFieldPolicies == true) {
               this.syncTableFieldPolicyNames();
           }
           return newColumn;
       }

       undoColumn(index: number):TableColumnDefinition {
           var columnDef = <TableColumnDefinition> this.feedDefinitionTableSchema.fields[index];
           columnDef.history.pop();
           let prevValue = columnDef.history[columnDef.history.length - 1];
           columnDef.undo(prevValue);
           return columnDef;
       };

       getColumnDefinitionByName(name:string) :TableColumnDefinition{
           return <TableColumnDefinition> this.feedDefinitionTableSchema.fields.find(columnDef =>  columnDef.name == name);
       }

       getTargetColumnFieldByName(name:string) :TableColumnDefinition {
           return <TableColumnDefinition> this.tableSchema.fields.find(columnDef =>  columnDef.name == name);
       }

       /**
        * get the partition objects for a given column
        * @param {string} columnName
        * @return {TableFieldPartition[]}
        */
       getPartitionsOnColumn(columnName:string){
           let partitions = this.partitions
               .filter((partition: any) => {
                   return partition.columnDef.name === columnName;
               });
           return partitions;
       }

       /**
        * Remove a column from the schema
        * @param index
        */
       removeColumn(index: number) :TableColumnDefinition{
           var columnDef = <TableColumnDefinition> this.feedDefinitionTableSchema.fields[index];
           columnDef.deleteColumn();
           return columnDef;

       }


    }
