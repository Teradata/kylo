import {SparkDataSet} from "../../../catalog/datasource/preview-schema/model/preview-data-set";
import {TableSchema} from "../../../model/table-schema";
import {SchemaParser} from "../../../model/field-policy";
import {TableFieldPolicy} from "../../../model/TableFieldPolicy";
import {TableFieldPartition} from "../../../model/TableFieldPartition";
import {FeedDataTransformation} from "../../../model/feed-data-transformation";
import {FEED_DEFINITION_STATE_NAME} from "../define-feed-states";
import {FeedStepValidator} from "./feed-step-validator";
import {TableColumnDefinition} from "../../../model/TableColumnDefinition";
import {Templates} from "../../../services/TemplateTypes";
import {CloneUtil} from "../../../../common/utils/CloneUtil";
import {DefaultSourceTableSchema, FeedTableSchema} from "./default-table.schema";

export class Step {
    number: number;
    systemName: string;
    name: string;
    description: string;
    complete: boolean;
    valid: boolean;
    sref: string;
    required?: boolean;
    dependsUponSteps?: string[] = [];
    allSteps: Step[];
    disabled: boolean;
    visited: boolean;
    validator: FeedStepValidator

    validate(feed: FeedModel): boolean {
        if (this.disabled) {
            return true;
        }
        else {
            return this.validator ? this.validator.validate(feed) : true;
        }

    }

    updateStepState() {
        let disabled = !this.complete;
        //update dependent step states
        let dependentSteps = this.findDependentSteps();
        if (dependentSteps) {
            dependentSteps.forEach(step => step.disabled = disabled);
        }
    }


    public constructor(init?: Partial<Step>) {
        Object.assign(this, init);
    }

    setComplete(complete: boolean) {
        this.complete = complete;
    }

    isPreviousStepComplete() {
        let index = this.number - 1;
        if (index > 0) {
            let prevStep = this.allSteps[index - 1];
            return prevStep.complete;
        }
        else {
            return true;
        }
    }

    findDependentSteps() {
        return this.allSteps.filter(step => step.dependsUponSteps.find(name => this.systemName == name) != undefined);
    }

    isDisabled() {
        return this.disabled;
    }

    shallowCopy():Step{
        return Object.assign(Object.create(this),this)
    }


}

export class StepBuilder {
    number: number;
    systemName: string;
    name: string;
    description: string;
    complete: boolean;
    sref: string;
    required?: boolean;
    dependsUponSteps?: string[] = [];
    allSteps: Step[];
    disabled: boolean;
    validator: FeedStepValidator;

    setNumber(num: number): StepBuilder {
        this.number = num;
        return this;
    }

    setSystemName(sysName: string) {
        this.systemName = sysName;
        if (this.name == undefined) {
            this.name = this.systemName;
        }
        return this;
    }

    setName(name: string) {
        this.name = name;
        if (this.systemName == undefined) {
            this.systemName = this.name;
        }
        return this;
    }

    setDescription(description: string) {
        this.description = description;
        return this;
    }

    setSref(sref: string) {
        this.sref = sref;
        return this;
    }

    setRequired(required: boolean) {
        this.required = required;
        return this;
    }

    addDependsUpon(systemName: string) {
        this.dependsUponSteps.push(systemName);
        return this;
    }

    setDependsUpon(systemNames: string[]) {
        this.dependsUponSteps = systemNames;
        return this;
    }

    setAllSteps(steps: Step[]) {
        this.allSteps = steps;
        return this;
    }

    setDisabled(disabled: boolean) {
        this.disabled = disabled;
        return this;
    }

    setValidator(feedStepValidator: FeedStepValidator) {
        this.validator = feedStepValidator;
        return this;
    }

    build() {
        let step = new Step({
            number: this.number,
            name: this.name,
            systemName: this.systemName,
            description: this.description,
            sref: FEED_DEFINITION_STATE_NAME + ".feed-step." + this.sref,
            complete: false,
            dependsUponSteps: this.dependsUponSteps,
            required: this.required
        });
        step.allSteps = this.allSteps;
        step.disabled = this.disabled;
        step.validator = this.validator;
        if (step.validator == undefined) {
            //add a default one
            step.validator = new FeedStepValidator();
        }
        if (step.validator) {
            step.validator.setStep(step)
        }
        return step;
    }


}

export interface TableOptions {
    compress: boolean;
    compressionFormat: string;
    auditLogging: boolean;
    encrypt: boolean;
    trackHistory: boolean;
}

export interface SourceTableSchema extends TableSchema {
    tableSchema: string;
}

export interface FeedTableDefinition {
    // id:string;
    //  schemaName:string;
    // name:string;
    //  databaseName:string;
    tableSchema: TableSchema
    sourceTableSchema: SourceTableSchema,
    feedTableSchema: TableSchema,
    method: string;
    existingTableName: string;
    structured: boolean;
    targetMergeStrategy: string;
    feedFormat: string;
    targetFormat: string;
    feedTblProperties: string;
    fieldPolicies: TableFieldPolicy[]
    partitions: TableFieldPartition[],
    options: TableOptions,
    sourceTableIncrementalDateField: string


}

export interface FeedSchedule {
    schedulingPeriod: string;
    schedulingStrategy: string;
    concurrentTasks: number;
}

export interface Category {
    id: string;
    name: string;
    systemName: string;
    description: string
    createFeed?: boolean;
    icon?: string;
    iconColor?: string;
}


export interface FeedModel {
    /**
     * What options does the template provide for the feed
     */
    templateTableOption: string;
    /**
     * Number of presteps allowed for the feed
     * TODO wire into new feed step framework
     */
    totalPreSteps: number;
    /**
     * Wire in for the feed step framework
     */
    totalSteps: number;
    /**
     * wire into the feed step framework
     */
    renderTemporaryPreStep: boolean;
    /**
     * Steps allowed for the feed
     */
    steps: Step[]
    /**
     * reference to the Template
     * TODO ref to Template type
     * @TODO  IS THIS NEEDED!!!!!
     */
    template: any;

    isNew(): boolean;

    state: string; //NEW, ENABLED, DISABLED
    mode: string; //DRAFT or COMPLETE
    updateDate: Date;
    /**
     * The Feed ID
     */
    id: string;
    /**
     * The feed version name (1.1, 1.2..)
     */
    versionName: string;
    /**
     * The Feed RegisteredTemplateId
     */
    templateId: string;
    /**
     * the template name
     */
    templateName: string;
    /**
     * the name of the feed
     */
    feedName: string;
    /**
     * internal system name
     */
    systemName: string;

    /**
     * the system name
     */
    systemFeedName: string;
    /**
     * Feed Description
     */
    description: string;
    /**
     * The class name for the selected input processor (i.e. org.nifi.GetFile)
     */
    inputProcessorType: string;
    /**
     * The name of the input processor (i.e. FileSystem)
     */
    inputProcessorName: string;


    inputProcessor: Templates.Processor;

    inputProcessors: Templates.Processor[];

    /**
     * The array of all other processors in the feed flow
     */
    nonInputProcessors: Templates.Processor[];
    /**
     * Array of properties
     */
    properties: Templates.Property[];

    securityGroups: any[];

    /**
     * The feed schedule
     */
    schedule: FeedSchedule;
    /**
     * does this feed use a template that requires target table definition
     */
    defineTable: boolean;
    /**
     * Does this feed have a template with a TriggerFeed that requires Precondition rendering
     */
    allowPreconditions: boolean;

    /**
     * Does this feed use a Data Transformation template
     */
    dataTransformationFeed: boolean;

    /**
     * The table definitions for the feed
     */
    table: FeedTableDefinition;
    /**
     * Category ref of the feed
     */
    category: Category;
    /**
     * the feed owner
     */
    dataOwner: string;
    /**
     * the feed tags
     */
    tags: any[];
    /**
     * is this a reusable feed?
     * TODO REMOVE
     */
    reusableFeed: boolean;
    /**
     * if this is a DataTransformation feed, the metadata needed for the transform
     */
    dataTransformation: FeedDataTransformation;
    /**
     * Additional properties supplied by the user
     */
    userProperties: any[];
    /**
     * Additional options (i.e. skipHeader
     */
    options: any;
    /**
     * Is the feed active (enabled)
     */
    active: boolean;
    /**
     * Entity Access controls
     */
    roleMemberships: any[];
    /**
     * Entity Access Control owner (different than DataOwnewr property)
     */
    owner: string;
    /**
     * flag to indicate the roleMemberships are stale
     */
    roleMembershipsUpdated: boolean;
    /**
     * TODO fill in with desc
     */
    tableOption: any;
    /**
     * is this a cloned feed
     */
    cloned: boolean;
    /**
     * What other feeds use this feed
     * array of feed system names
     */
    usedByFeeds: any[];
    /**
     * Does this feed allow Reindexing of the data in Global Search
     */
    allowIndexing: boolean;
    /**
     * the status of the reindex job if appliciable
     */
    historyReindexingStatus: string;
    /**
     * metadata around the view states for this feed.
     * Used for feed rendering and plugins
     */
    view: any;

    registeredTemplate: any;

    validate(updateStepState?: boolean): boolean;

    readonly: boolean;

    /**
     * The name of the sample file used to parse for table schema
     * Optional
     */
    sampleFile?: string;

    /**
     * the name of the schema parser used to build the schema
     */
    schemaParser?: SchemaParser;

    /**
     * Should this feed show the "Skip Header" option
     */
    allowSkipHeaderOption: boolean;

    /**
     * Has the schema changed since creation
     */
    schemaChanged?: boolean;

    sourceDataSets?: SparkDataSet[];

    update(model: Partial<FeedModel>): void;

    copy(): FeedModel;

    copyModelForSave() :FeedModel

    isStream: boolean;

    /**
     * Have the properties been merged and initialized with the template
     */
    propertiesInitialized?: boolean;
}


export class DefaultFeedModel implements FeedModel {

    templateName: string = '';
    templateTableOption: string = null;
    totalPreSteps: number = 0;
    totalSteps: number = null;
    renderTemporaryPreStep: boolean = false;
    steps: Step[] = [];


    id: string = null;
    template: any;
    updateDate: Date;
    state: string;
    mode: string;

    versionName: string = null;
    templateId: string = '';

    feedName: string = '';
    systemName: string = '';
    systemFeedName: string = this.systemName;

    description: string = '';

    inputProcessorType: string = '';
    inputProcessorName: string = null;
    inputProcessor: Templates.Processor = null;
    inputProcessors: Templates.Processor[] = [];
    nonInputProcessors: Templates.Processor[] = [];
    properties: Templates.Property[] = [];
    securityGroups: any[] = [];
    schedule: FeedSchedule = {schedulingPeriod: "0 0 12 1/1 * ? *", schedulingStrategy: 'CRON_DRIVEN', concurrentTasks: 1};
    defineTable: boolean = false;
    allowPreconditions: boolean = false;
    dataTransformationFeed: boolean = false;
    table: FeedTableDefinition;
    category: Category;
    dataOwner: string = '';
    tags: any[];
    reusableFeed: boolean = false;
    dataTransformation: any;
    userProperties: any[] = [];
    options: any = {skipHeader: false};
    active: boolean = true;
    roleMemberships: any[] = [];
    owner: string = null;
    roleMembershipsUpdated: boolean = false;
    tableOption: any = {};
    cloned: boolean = false;
    usedByFeeds: any[] = [];
    allowIndexing: boolean = true;
    historyReindexingStatus: string = 'NEVER_RUN';
    view: any;
    registeredTemplate: any;
    readonly: boolean;
    sampleFile?: string;
    schemaParser?: SchemaParser;
    schemaChanged?: boolean;
    sourceDataSets?: SparkDataSet[] = [];
    isStream: boolean;
    /**
     * Have the properties been merged and initialized with the template
     */
    propertiesInitialized?: boolean;
    /**
     * Should this feed show the "Skip Header" option
     */
    allowSkipHeaderOption: boolean;

    public constructor(init?: Partial<FeedModel>) {
        this.initialize();
        Object.assign(this, init);
        if (this.sourceDataSets) {
            //ensure they are of the right class objects
            this.sourceDataSets = this.sourceDataSets.map(ds => {
                return new SparkDataSet(ds);
            });
        }

        //ensure the table fields are correct objects
        let tableFieldMap: { [key: string]: TableColumnDefinition; } = {};
        let fields = this.table.tableSchema.fields.map((field: any) => {
            let tableColumnDef: TableColumnDefinition;
            if (field.objectType && field.objectType == TableColumnDefinition.OBJECT_TYPE) {
                tableColumnDef = field;
            }
            else {
                tableColumnDef = new TableColumnDefinition(field);
            }
            tableFieldMap[tableColumnDef.name] = tableColumnDef;
            return tableColumnDef;
        });
        this.table.tableSchema.fields = fields;

        //ensure the table partitions are correct objects
        let partitions = this.table.partitions.map((partition: any) => {
            let partitionObject: TableFieldPartition;
            if (partition.objectType && partition.objectType == TableFieldPartition.OBJECT_TYPE) {
                partitionObject = partition;
            }
            else {
                partitionObject = new TableFieldPartition(partition.position, partition);
            }
            //ensure it has the correct columnDef ref
            if (partitionObject.sourceField && tableFieldMap[partitionObject.sourceField] != undefined) {
                //find the matching column field
                partitionObject.columnDef = tableFieldMap[partitionObject.sourceField];
            }
            return partitionObject;
        });
        this.table.partitions = partitions;
        this.ensureTableFieldPolicyTypes();


    }

    updateNonNullFields(model: any) {
        let keys = Object.keys(model);
        let obj = {}
        keys.forEach((key: string) => {
            if (model[key] && model[key] != null) {
                obj[key] = model[key];
            }
        });
        Object.assign(this, obj);
    }

    update(model: Partial<FeedModel>): void {
        //keep the internal ids saved for the table.tableSchema.fields and table.partitions
        let oldFields = this.table.tableSchema.fields;
        let oldPartitions = this.table.partitions;
        this.updateNonNullFields(model);
        let tableFieldMap: { [key: string]: TableColumnDefinition; } = {};
        this.table.tableSchema.fields.forEach((field: TableColumnDefinition, index: number) => {
            field._id = (<TableColumnDefinition> oldFields[index])._id;
            tableFieldMap[field.name] = field;
        });
        this.table.partitions.forEach((partition: TableFieldPartition, index: number) => {
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

    private ensureTableFieldPolicyTypes() {

        let fieldPolicies = this.table.fieldPolicies.map((policy: any) => {
            let policyObj: TableFieldPolicy;
            if (policy.objectType && policy.objectType == TableFieldPolicy.OBJECT_TYPE) {
                policyObj = policy;
            }
            else {
                policyObj = new TableFieldPolicy(policy);
            }
            return policyObj;
        });
        this.table.fieldPolicies = fieldPolicies;
    }


    /**
     * {id: null, name: null, schemaName: null, databaseName: null, fields: [], description: null, charset: null, properties: null},
     sourceTableSchema: {id: null, name: null, schemaName: null, tableSchema: null, databaseName: null, fields: [], description: null, charset: null, properties: null},
     feedTableSchema: {id: null, name: null, databaseName: null, schemaName: null, fields: [], description: null, charset: null, properties: null},
     */

    initialize() {
        this.readonly = true;
        this.systemName = '';
        this.feedName = '';
        this.mode = "DRAFT";
        this.category = {id: null, name: null, systemName: null, description: null};
        this.table = {
            tableSchema: new FeedTableSchema(),
            sourceTableSchema: new DefaultSourceTableSchema(),
            feedTableSchema : new FeedTableSchema(),
            method: 'SAMPLE_FILE',
            existingTableName: null,
            structured: false,
            targetMergeStrategy: 'DEDUPE_AND_MERGE',
            feedFormat: 'ROW FORMAT SERDE \'org.apache.hadoop.hive.serde2.OpenCSVSerde\''
            + ' WITH SERDEPROPERTIES ( \'separatorChar\' = \',\' ,\'escapeChar\' = \'\\\\\' ,\'quoteChar\' = \'"\')'
            + ' STORED AS TEXTFILE',
            targetFormat: 'STORED AS ORC',
            feedTblProperties: '',
            fieldPolicies: [],
            partitions: [],
            options: {compress: false, compressionFormat: 'NONE', auditLogging: true, encrypt: false, trackHistory: false},
            sourceTableIncrementalDateField: null
        };
        this.dataTransformation = {
            chartViewModel: null,
            datasourceIds: null,
            datasources: null,
            dataTransformScript: null,
            sql: null,
            states: []
        };
        this.view = {
            generalInfo: {disabled: false},
            feedDetails: {disabled: false},
            table: {disabled: false},
            dataPolicies: {disabled: false},
            properties: {
                disabled: false,
                dataOwner: {disabled: false},
                tags: {disabled: false}
            },
            accessControl: {disabled: false},
            schedule: {
                disabled: false,
                schedulingPeriod: {disabled: false},
                schedulingStrategy: {disabled: false},
                active: {disabled: false},
                executionNode: {disabled: false},
                preconditions: {disabled: false}
            }
        };
    }

    isNew() {
        return this.id == undefined;
    }

    validate(updateStepState?: boolean): boolean {
        let valid = true;
        let model = this;
        this.steps.forEach((step: Step) => {
            valid = valid && step.validate(<FeedModel>this);
            if (updateStepState) {
                step.updateStepState();
            }
        });
        return valid;
    }

    /**
     * Are all the steps for this feed complete
     * @return {boolean}
     */
    isComplete() {
        let complete = true;
        this.steps.forEach(step => complete = complete && step.complete);
        return complete;
    }


    /**
     * Deep copy of this object
     * @return {FeedModel}
     */
    copy(): FeedModel {
        //steps have self references back to themselves.
        let allSteps: Step[] = [];
        this.steps.forEach(step => {let copy = step.shallowCopy();
        allSteps.push(copy);
        copy.allSteps = allSteps;
        });
        let newFeed :FeedModel =  Object.create(this);
        Object.assign(newFeed,this)
        newFeed.steps = null;
        let copy :FeedModel = CloneUtil.deepCopy(newFeed);
        copy.steps = allSteps;
        return copy;
    }

    /**
     * Return a copy of this model that is stripped of un necessary properties ready to be saved
     * @param {FeedModel} copy
     * @return {FeedModel}
     */
     copyModelForSave() :FeedModel{
         //create a deep copy
        let copy = this.copy();

        if (copy.table && copy.table.fieldPolicies && copy.table.tableSchema && copy.table.tableSchema.fields) {
            // Set feed

            //if the sourceSchema is not defined then set it to match the target
            let addSourceSchemaFields:boolean = copy.table.sourceTableSchema.fields.length == 0;
            let addFeedSchemaFields = copy.isNew() && copy.table.feedTableSchema.fields.length ==0;


            /**
             * Undeleted fields
             * @type {any[]}
             */
            let tableFields: TableColumnDefinition[] = [];
            /**
             * Field Policies ensuring the names match the column fields
             * @type {any[]}
             */
            var newPolicies: TableFieldPolicy[] = [];
            /**
             * Feed Schema
             * @type {any[]}
             */
            var feedFields: TableColumnDefinition[] = [];
            /**
             * Source Schema
             * @type {any[]}
             */
            var sourceFields: TableColumnDefinition[] = [];

            copy.table.tableSchema.fields.forEach((columnDef: TableColumnDefinition, idx: number) => {

                if(addSourceSchemaFields) {
                    let sourceField :TableColumnDefinition = columnDef.copy();
                    //set the original names as the source field names
                    sourceField.name = columnDef.origName;
                    sourceField.derivedDataType = columnDef.origDataType;
                    //remove sample
                    sourceField.prepareForSave();
                    sourceFields.push(sourceField);
                }

                if(addFeedSchemaFields) {
                    let feedField :TableColumnDefinition= columnDef.copy();
                    feedField.prepareForSave();
                    feedFields.push(feedField);
                    //TODO
                    // structured files must use the original names
                    // if (copy.table.structured == true) {
                    //     feedField.name = columnDef.origName;
                    //     feedField.derivedDataType = columnDef.origDataType;
                    //  }
                }
                if (!columnDef.deleted) {
                    columnDef.prepareForSave();
                    tableFields.push(columnDef);

                    let policy = copy.table.fieldPolicies[idx];
                    if (policy) {
                        policy.feedFieldName = columnDef.name;
                        policy.name = columnDef.name;
                        newPolicies.push(policy);
                    }
                }

               /*
                    // For files the feed table must contain all the columns from the source even if unused in the target
                    if (copy.table.method == 'SAMPLE_FILE') {
                        feedFields.push(feedField);
                    } else if (copy.table.method == 'EXISTING_TABLE' && copy.table.sourceTableIncrementalDateField == sourceField.name) {
                        feedFields.push(feedField);
                        sourceFields.push(sourceField);
                    }
                    */
            });

            //update the table schema and policies
            copy.table.fieldPolicies = newPolicies;
            copy.table.tableSchema.fields = tableFields;

            //TODO move init logic for undefind schema to a FeedTable class constuctor so its handled prior to this step
            if (copy.table.sourceTableSchema == undefined) {
                copy.table.sourceTableSchema = new DefaultSourceTableSchema();
            }
            //only set the sourceFields if its the first time creating this feed
            if (copy.id == null) {
                copy.table.sourceTableSchema.fields = sourceFields;
                copy.table.feedTableSchema.fields = feedFields;
            }
            if (copy.table.feedTableSchema == undefined) {
                copy.table.feedTableSchema = new FeedTableSchema();
            }

            if (copy.registeredTemplate) {
                copy.registeredTemplate = undefined;
            }

        }
        return copy;
    }



}