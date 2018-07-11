import {PreviewDataSet} from "../../../catalog/datasource/preview-schema/model/preview-data-set";
import {TableSchema} from "../../../model/table-schema";
import {FieldPolicy} from "../../../model/field-policy";
import {TableFieldPolicy} from "../../../model/TableFieldPolicy";
import {TableFieldPartition} from "../../../model/TableFieldPartition";
import {FeedDataTransformation} from "../../../model/feed-data-transformation";
import {FEED_DEFINITION_STATE_NAME} from "../define-feed-states";
import {FeedStepValidator} from "./feed-step-validator";
import {PreviewFileDataSet} from "../../../catalog/datasource/preview-schema/model/preview-file-data-set";

export class Step{
    number:number;
    systemName:string;
    name:string;
    description:string;
    complete:boolean;
    valid:boolean;
    sref:string;
    required?:boolean;
    dependsUponSteps?:string[] = [];
    allSteps:Step[];
    disabled:boolean;
    visited:boolean;
    validator:FeedStepValidator

    validate(feed:FeedModel):boolean {
        console.log("validate step ",this.name)
        if(this.validator){
            return this.validator.validate(feed);
        }
        else {
            return true;
        }
    }

    updateStepState(){
        let disabled = !this.complete;
        //update dependent step states
        let dependentSteps = this.findDependentSteps();
        if(dependentSteps){
            dependentSteps.forEach(step => step.disabled = disabled);
        }
    }


    public constructor(init?:Partial<Step>) {
            Object.assign(this, init);
     }

     setComplete(complete:boolean){
        this.complete = complete;
     }

     isPreviousStepComplete(){
        let index = this.number -1;
        if(index >0) {
            let prevStep = this.allSteps[index - 1];
            return prevStep.complete;
        }
        else {
            return true;
        }
     }

     findDependentSteps(){
        return this.allSteps.filter(step => step.dependsUponSteps.find(name=> this.systemName == name) != undefined);
     }

     isDisabled(){
        return this.disabled;
     }


}

export class StepBuilder{
    number:number;
    systemName:string;
    name:string;
    description:string;
    complete:boolean;
    sref:string;
    required?:boolean;
    dependsUponSteps?:string[] = [];
    allSteps:Step[];
    disabled:boolean;
    validator:FeedStepValidator;

    setNumber(num:number): StepBuilder{
        this.number = num;
        return this;
    }
    setSystemName(sysName:string){
        this.systemName = sysName;
        if(this.name == undefined){
            this.name = this.systemName;
        }
        return this;
    }

    setName(name:string){
        this.name = name;
        if(this.systemName == undefined){
            this.systemName = this.name;
        }
        return this;
    }
    setDescription(description:string){
        this.description = description;
        return this;
    }
    setSref(sref:string){
        this.sref = sref;
        return this;
    }
    setRequired(required:boolean){
        this.required = required;
        return this;
    }
    addDependsUpon(systemName:string){
        this.dependsUponSteps.push(systemName);
        return this;
    }
    setDependsUpon(systemNames:string[]){
        this.dependsUponSteps = systemNames;
        return this;
    }
    setAllSteps(steps:Step[]){
        this.allSteps = steps;
        return this;
    }
    setDisabled(disabled:boolean){
        this.disabled = disabled;
        return this;
    }
    setValidator(feedStepValidator:FeedStepValidator){
        this.validator = feedStepValidator;
        return this;
    }

    build(){
        let step = new Step({number:this.number,name:this.name,systemName:this.systemName,description:this.description,sref:FEED_DEFINITION_STATE_NAME+".feed-step."+this.sref,complete:false,dependsUponSteps:this.dependsUponSteps,required:this.required});
        step.allSteps = this.allSteps;
        step.disabled = this.disabled;
        step.validator = this.validator;
        if(step.validator == undefined){
            //add a default one
            step.validator = new FeedStepValidator();
        }
        if(step.validator){
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
tableSchema:string;
}

export interface FeedTableDefinition {

        tableSchema: TableSchema
        sourceTableSchema: SourceTableSchema,
        feedTableSchema:  TableSchema,
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
    concurrentTasks: number ;
}

export interface Category {
id:string;
name:string;
systemName:string;
description:string
    createFeed?:boolean;
icon?:string;
iconColor?:string;
    }


export interface FeedModel {
    /**
     * What options does the template provide for the feed
     */
    templateTableOption:string;
    /**
     * Number of presteps allowed for the feed
     * TODO wire into new feed step framework
     */
    totalPreSteps:number;
    /**
     * Wire in for the feed step framework
     */
    totalSteps:number;
    /**
     * wire into the feed step framework
     */
    renderTemporaryPreStep:boolean;
    /**
     * Steps allowed for the feed
     */
    steps:Step[]
    /**
     * reference to the Template
     * TODO ref to Template type
     */
    template:any;
    isNew():boolean;
    state:string; //NEW, ENABLED, DISABLED
    mode:string; //DRAFT or COMPLETE
    updateDate:Date;
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
    templateName:string;
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
    systemFeedName:string;
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

    /**
     * The selected input processor def
     * TODO replace with concrete Processor type ref
     */
    inputProcessor: any;

    /**
     * The array of all other processors in the feed flow
     */
    nonInputProcessors: any[];
    /**
     * Array of properties
     */
    properties: any[];

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

    registeredTemplate:any;

    validate():boolean;

    readonly:boolean;
}









export class DefaultFeedModel implements FeedModel{

    templateName:string = '';
    templateTableOption:string = null;
    totalPreSteps:number = 0;
    totalSteps:number = null;
    renderTemporaryPreStep:boolean = false;
    steps:Step[] = [];


    id:string = null;
    template:any;
    updateDate:Date;
    state:string;
    mode:string;

    versionName: string = null;
    templateId: string = '';

    feedName: string ='';
    systemName:string = '';
    systemFeedName:string = this.systemName;

    description: string ='';

    inputProcessorType: string ='';
    inputProcessorName: string = null;
    inputProcessor: any = null
    nonInputProcessors: any[] = [];
    properties: any[] = [];
    securityGroups: any[] = [];
    schedule: FeedSchedule = { schedulingPeriod: "0 0 12 1/1 * ? *", schedulingStrategy: 'CRON_DRIVEN', concurrentTasks: 1 };
    defineTable: boolean = false;
    allowPreconditions: boolean = false;
    dataTransformationFeed: boolean = false;
    table: FeedTableDefinition;
    category: Category;
    dataOwner: string = '';
    tags: any[];
    reusableFeed:boolean= false;
    dataTransformation: any;
    userProperties: any[] = [];
    options:any = { skipHeader: false };
    active: boolean = true;
    roleMemberships: any[] = [];
    owner: string = null;
    roleMembershipsUpdated: boolean = false;
    tableOption: any = {};
    cloned: boolean = false;
    usedByFeeds: any[] = [];
    allowIndexing: boolean =true;
    historyReindexingStatus: string = 'NEVER_RUN';
    view:any;
    registeredTemplate:any;
    readonly :boolean;
    public constructor(init?:Partial<FeedModel>) {
        this.initialize();
        Object.assign(this, init);
    }

    initialize() {
        this.readonly = true;
        this.systemName ='';
        this.feedName = '';
        this.mode = "DRAFT";
        this.category = { id: null, name: null ,systemName:null,description:null};
        this.table = {
            tableSchema: {id:null, name: null, schemaName:null, databaseName:null,fields: [], description:null,charset:null,properties:null },
            sourceTableSchema: {id:null, name: null, schemaName:null,tableSchema: null, databaseName:null,fields:[],description:null,charset:null,properties:null },
            feedTableSchema: { id:null,name: null,databaseName:null,schemaName:null, fields: [],description:null,charset:null,properties:null },
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
        this.view =  {
            generalInfo: { disabled: false },
            feedDetails: { disabled: false },
            table: { disabled: false },
            dataPolicies: { disabled: false },
            properties: {
                disabled: false,
                dataOwner: { disabled: false },
                tags: { disabled: false }
            },
            accessControl: { disabled: false },
            schedule: {
                disabled: false,
                schedulingPeriod: { disabled: false },
                schedulingStrategy: { disabled: false },
                active: { disabled: false },
                executionNode: { disabled: false },
                preconditions: { disabled: false }
            }
        };
    }

    isNew(){
        return this.id == undefined;
    }

    validate() : boolean {
        let valid = true;
        let model = this;
        this.steps.forEach((step :Step)=> {
            valid = valid && step.validate(<FeedModel>this);
            step.updateStepState();
        });
        return valid;
    }




}