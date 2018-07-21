import {SparkDataSet} from "../spark-data-set.model"
import {TableSchema} from "../table-schema";
import {SchemaParser} from "../field-policy";
import {TableFieldPolicy} from "../TableFieldPolicy";
import {TableFieldPartition} from "../TableFieldPartition";
import {FeedDataTransformation} from "../feed-data-transformation";
import {FeedStepValidator} from "./feed-step-validator";
import {TableColumnDefinition} from "../TableColumnDefinition";
import {Templates} from "../../services/TemplateTypes";
import {CloneUtil} from "../../../common/utils/clone-util";
import {FeedTableDefinition} from "./feed-table-definition.model";
import {ObjectUtils} from "../../../common/utils/object-utils";
import {Category} from "../category/category.model";
import {FeedTableSchema} from "./feed-table-schema.model";
import {Step} from "./feed-step.model";
import {KyloObject} from "../../../common/common.model";
import {SourceTableSchema} from "./feed-source-table-schema.model";


export interface TableOptions {
    compress: boolean;
    compressionFormat: string;
    auditLogging: boolean;
    encrypt: boolean;
    trackHistory: boolean;
}



export interface FeedSchedule {
    schedulingPeriod: string;
    schedulingStrategy: string;
    concurrentTasks: number;
}




export class Feed  implements KyloObject{


    public static OBJECT_TYPE:string = 'Feed'

    public objectType:string = Feed.OBJECT_TYPE;

    /**
     * What options does the template provide for the feed
     */
    templateTableOption: string = null;
    /**
     * Number of presteps allowed for the feed
     * TODO wire into new feed step framework
     */
    totalPreSteps: number = 0;
    /**
     * Wire in for the feed step framework
     */
    totalSteps: number = null;
    /**
     * wire into the feed step framework
     */
    renderTemporaryPreStep: boolean = false;
    /**
     * Steps allowed for the feed
     */
    steps: Step[] = [];
    /**
     * reference to the Template
     * TODO ref to Template type
     * @TODO  IS THIS NEEDED!!!!!
     */
    template: any = null;

    state: string; //NEW, ENABLED, DISABLED
    mode: string; //DRAFT or COMPLETE

    updateDate: Date;
    /**
     * The Feed ID
     */
    id: string = null;
    /**
     * The feed version name (1.1, 1.2..)
     */
    versionName: string =null;
    /**
     * The Feed RegisteredTemplateId
     */
    templateId: string = '';
    /**
     * the template name
     */
    templateName: string = '';
    /**
     * the name of the feed
     */
    feedName: string = '';
    /**
     * internal system name
     */
    systemName: string = '';

    /**
     * the system name
     * TODO consolidate with systemName?
     */
    systemFeedName: string = '';
    /**
     * Feed Description
     */
    description: string = '';
    /**
     * The class name for the selected input processor (i.e. org.nifi.GetFile)
     */
    inputProcessorType: string = '';
    /**
     * The name of the input processor (i.e. FileSystem)
     */
    inputProcessorName: string = null;


    inputProcessor: Templates.Processor = null;

    inputProcessors: Templates.Processor[] = [];

    /**
     * The array of all other processors in the feed flow
     */
    nonInputProcessors: Templates.Processor[] = [];
    /**
     * Array of properties
     */
    properties: Templates.Property[] = [];

    securityGroups: any[] = [];

    /**
     * The feed schedule
     */
    schedule: FeedSchedule = {schedulingPeriod: "0 0 12 1/1 * ? *", schedulingStrategy: 'CRON_DRIVEN', concurrentTasks: 1};
    /**
     * does this feed use a template that requires target table definition
     */
    defineTable: boolean = false;
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
     * @deprecated
     */
    reusableFeed: boolean;
    /**
     * if this is a DataTransformation feed, the metadata needed for the transform
     */
    dataTransformation: FeedDataTransformation;
    /**
     * Additional properties supplied by the user
     */
    userProperties: any[] = [];
    /**
     * Additional options (i.e. skipHeader
     */
    options: any = {skipHeader: false};
    /**
     * Is the feed active (enabled)
     */
    active: boolean = true;
    /**
     * Entity Access controls
     */
    roleMemberships: any[] = [];
    /**
     * Entity Access Control owner (different than DataOwnewr property)
     */
    owner: string = null;
    /**
     * flag to indicate the roleMemberships are stale
     */
    roleMembershipsUpdated: boolean;
    /**
     * TODO fill in with desc
     */
    tableOption: any = {};
    /**
     * is this a cloned feed
     */
    cloned: boolean;
    /**
     * What other feeds use this feed
     * array of feed system names
     */
    usedByFeeds: any[] = [];
    /**
     * Does this feed allow Reindexing of the data in Global Search
     */
    allowIndexing: boolean = true;
    /**
     * the status of the reindex job if appliciable
     */
    historyReindexingStatus: string = 'NEVER_RUN';
    /**
     * metadata around the view states for this feed.
     * Used for feed rendering and plugins
     */
    view: any;

    registeredTemplate: any;


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

    /**
     * Source Datasets selected for this feed
     * @type {any[]}
     */
    sourceDataSets?: SparkDataSet[] = [];

    /**
     * is this a streaming feed
     */
    isStream: boolean;

    /**
     * Have the properties been merged and initialized with the template
     */
    propertiesInitialized?: boolean;


    public constructor(init?: Partial<Feed>) {
        this.initialize();
        Object.assign(this, init);
        if (this.sourceDataSets) {
            //ensure they are of the right class objects
            this.sourceDataSets = this.sourceDataSets.map(ds => {
                return new SparkDataSet(ds);
            });
        }

        //ensure the tableDef model
        this.table = ObjectUtils.getAs(this.table,FeedTableDefinition, FeedTableDefinition.OBJECT_TYPE);
        this.table.ensureObjectTypes();
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

    update(model: Partial<Feed>): void {
        //keep the internal ids saved for the table.tableSchema.fields and table.partitions
        let oldFields = <TableColumnDefinition[]>this.table.tableSchema.fields;
        let oldPartitions = this.table.partitions;
        this.updateNonNullFields(model);
        this.table.update(oldFields,oldPartitions);
    }



    initialize() {
        this.readonly = true;
        this.systemName = '';
        this.feedName = '';
        this.mode = "DRAFT";
        this.category = {id: null, name: null, systemName: null, description: null};
        this.table = new FeedTableDefinition()
        this.dataTransformation = {
            chartViewModel: null,
            datasourceIds: null,
            $datasources: null,
            $selectedColumnsAndTables:null,
            $selectedDatasourceId:null,
            dataTransformScript: null,
            datasets:null,
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
            valid = valid && step.validate(<Feed>this);
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
     * @return {Feed}
     */
    copy(): Feed {
        //steps have self references back to themselves.
        let allSteps: Step[] = [];
        this.steps.forEach(step => {let copy = step.shallowCopy();
        allSteps.push(copy);
        copy.allSteps = allSteps;
        });
        let newFeed :Feed =  Object.create(this);
        Object.assign(newFeed,this)
        newFeed.steps = null;
        let copy :Feed = CloneUtil.deepCopy(newFeed);
        copy.steps = allSteps;
        return copy;
    }

    /**
     * Return a copy of this model that is stripped of un necessary properties ready to be saved
     * @param {Feed} copy
     * @return {Feed}
     */
     copyModelForSave() :Feed{
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
                copy.table.sourceTableSchema = new SourceTableSchema();
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