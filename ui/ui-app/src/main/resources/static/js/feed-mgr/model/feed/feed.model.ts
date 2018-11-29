import * as _ from "underscore";

import {KyloObject, ObjectChanged} from "../../../../lib/common/common.model";
import {CloneUtil} from "../../../common/utils/clone-util";
import {ObjectUtils} from "../../../../lib/common/utils/object-utils";
import {ConnectorPlugin} from "../../catalog/api/models/connector-plugin";
import {ConnectorPluginNifiProperties} from "../../catalog/api/models/connector-plugin-nifi-properties";
import {TableColumn} from "../../catalog/datasource/preview-schema/model/table-view-model";
import {TableSchemaUpdateMode} from "../../feeds/define-feed-ng2/services/define-feed.service";
import {FeedDetailsProcessorRenderingHelper} from "../../services/FeedDetailsProcessorRenderingHelper";
import {Templates} from "../../../../lib/feed-mgr/services/TemplateTypes";
import {Category} from "../category/category.model";
import {DefaultFeedDataTransformation, FeedDataTransformation} from "../feed-data-transformation";
import {SchemaParser} from "../field-policy";
import {SparkDataSet} from "../spark-data-set.model"
import {TableColumnDefinition} from "../TableColumnDefinition";
import {TableFieldPolicy} from "../TableFieldPolicy";
import {UserProperty, UserPropertyUtil} from "../user-property.model";
import {SourceTableSchema} from "./feed-source-table-schema.model";
import {Step} from "./feed-step.model";
import {FeedTableDefinition} from "./feed-table-definition.model";
import {FeedTableSchema} from "./feed-table-schema.model";
import {EntityVersion} from "../entity-version.model";
import {PartialObserver} from "rxjs/Observer";
import {Observable} from "rxjs/Observable";
import {Subject} from "rxjs/Subject";
import {SchemaField} from "../schema-field";
import {PreviewDataSet} from "../../catalog/datasource/preview-schema/model/preview-data-set";
import {PreviewFileDataSet} from "../../catalog/datasource/preview-schema/model/preview-file-data-set";
import {PreviewHiveDataSet} from "../../catalog/datasource/preview-schema/model/preview-hive-data-set";
import {PreviewJdbcDataSet} from "../../catalog/datasource/preview-schema/model/preview-jdbc-data-set";
import {FeedStepConstants} from "./feed-step-constants";
import {NifiFeedPropertyUtil} from "../../services/nifi-feed-property-util";
import {KyloFeed} from '../../../../lib/feed-mgr/model/feed/kylo-feed';


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
    preconditions: any[],
    executionNode: any;
}

export enum FeedMode {
    DRAFT="DRAFT", DEPLOYED="DEPLOYED", DEPLDYED_WITH_ACTIVE_DRAFT="DEPLOYED_ACTIVE_DRAFT"
}

export enum FeedState {
    NEW = "NEW", ENABLED = "ENABLED", DISABLED = "DISABLED"
}

export enum FeedOperationsState {
    RUNNING = "RUNNING", WAITING = "WAITING"
}


export enum FeedTemplateType {
    DEFINE_TABLE = "DEFINE_TABLE", DATA_TRANSFORMATION = "DATA_TRANSFORMATION", SIMPLE_FEED = "SIMPLE_FEED"
}

export enum LoadMode {
    LATEST="LATEST",DEPLOYED="DEPLOYED",DRAFT="DRAFT"
}

export enum TableMethod {
    SAMPLE_FILE="SAMPLE_FILE",EXISTING_TABLE="EXISTING_TABLE",MANUAL="MANUAL"
}

export class FeedAccessControl {
    allowEdit:boolean;
    allowChangePermissions:boolean;
    allowAdmin:boolean;
    allowSlaAccess:boolean;
    allowExport:boolean;
    allowStart:boolean;
    /**
     * Access to the feed.sourceDataSet
     */
    datasourceAccess:boolean;
    accessMessage:string;
    public constructor(init?: Partial<FeedAccessControl>) {
        Object.assign(this, init);
    }

    static NO_ACCESS = new FeedAccessControl();

    allAccess(){
        this.accessMessage = '';
        this.datasourceAccess = true;
        this.allowEdit = true;
        this.allowChangePermissions = true;
        this.allowAdmin = true;
        this.allowSlaAccess = true;
        this.allowExport = true;
        this.allowStart=true;
        return this;
    }

    static adminAccess = () => new FeedAccessControl().allAccess();
}



export class StepStateChangeEvent {
    public changes:ObjectChanged<Step>[] = [];
    public constructor(public feed:Feed){}

    addChange(oldStep:Step,newStep:Step){
        this.changes.push({oldValue:oldStep,newValue:newStep});
    }

    hasChanges():boolean {
        return this.changes.length >0;
    }
}

/**
 * Key set on the step metadata when a user acknowledges that they want to proceed without selecting a source
 * @type {string}
 */
export const SKIP_SOURCE_CATALOG_KEY = "SKIP_SOURCE_CATALOG"

export class Feed implements KyloObject, KyloFeed {


    public static OBJECT_TYPE: string = 'Feed'

    public objectType: string = Feed.OBJECT_TYPE;

    static UI_STATE_STEPS_KEY = "STEPS";

    /**
     * internal id to track feed instances
     */
    userInterfaceId: string;

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

    state: FeedState; //NEW, ENABLED, DISABLED
    mode: FeedMode; //DRAFT or DEPLOYED

    updateDate: Date;

    createdDate:Date;
    /**
     * The Feed ID
     */
    id: string = null;
    /**
     * The feed version name (1.1, 1.2..)
     */
    versionName: string = null;
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
    schedule: FeedSchedule = {schedulingPeriod: "0 0 12 1/1 * ? *", schedulingStrategy: 'CRON_DRIVEN', concurrentTasks: 1, preconditions: null, executionNode: null};
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
     * A copy of the orig schema if it exists
     */
    originalTableSchema:FeedTableSchema;
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
    userProperties: UserProperty[] = [];
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

    accessControl:FeedAccessControl;


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
     * sample Datasets selected for this feed
     * @type {any[]}
     */
    sampleDataSet?: SparkDataSet;


    /**
     * is this a streaming feed
     */
    isStream: boolean;

    /**
     * Have the properties been merged and initialized with the template
     */
    propertiesInitialized?: boolean;

    isValid: boolean;

    /**
     * map of userinterface state objects persisted to the feed
     */
    uiState: { [key: string]: any; }

    /**
     * The versionId for the feed
     */
    versionId: string;

    /**
     * The version that is deployed for this feed
     * This will be null if this.mode != DRAFT
     */
    deployedVersion:EntityVersion

    /**
     * if we are viewing a deployed versoin and it has a draft
     */
    loadMode:LoadMode;

    stepChangesSubject = new Subject<StepStateChangeEvent>();

    subscribeToStepStateChanges(o:PartialObserver<StepStateChangeEvent>) {
       return this.stepChangesSubject.subscribe(o);
    }

    public constructor(init?: Partial<Feed>) {
        this.initialize();
        Object.assign(this, init);
        if (this.sourceDataSets) {
            //ensure they are of the right class objects
            if (this.sourceDataSets) {
                this.sourceDataSets = this.sourceDataSets.map(ds => new SparkDataSet(ds));
            }
        }
        if (this.sampleDataSet) {
            //ensure they are of the right class objects
            if (this.sampleDataSet) {
                this.sampleDataSet = new SparkDataSet(this.sampleDataSet);
            }
        }
        if (this.uiState == undefined) {
            this.uiState = {}
        }


        //ensure the tableDef model
        this.table = ObjectUtils.getAs(this.table, FeedTableDefinition, FeedTableDefinition.OBJECT_TYPE);
        this.table.ensureObjectTypes();
        this.originalTableSchema = CloneUtil.deepCopy(this.table.tableSchema)

        if (this.isDataTransformation()) {
            //ensure types
            this.dataTransformation = ObjectUtils.getAs(this.dataTransformation, DefaultFeedDataTransformation, DefaultFeedDataTransformation.OBJECT_TYPE);
        }
        this.userInterfaceId = _.uniqueId("feed-");

        if(this.registeredTemplate){
            this.isStream = this.registeredTemplate.isStream
        }
    }

    getCountOfTemplateInputProcessors() :number{
        return this.registeredTemplate && this.registeredTemplate.inputProcessors ? this.registeredTemplate.inputProcessors.length : 0;
    }

    renderSourceStep()
    {
        let sourceStep = this.getStepBySystemName(FeedStepConstants.STEP_FEED_SOURCE);
        let render = !this.isDataTransformation() || (this.isDataTransformation() && this.getCountOfTemplateInputProcessors() >1 );
        return render;
    }

    getFullName(){
        return this.category.systemName+"."+this.systemFeedName;
    }

    getFeedNameAndVersion(){
        if(this.isDraft()){
            return this.feedName+" - DRAFT";
        }
        else {
            return this.feedName + "- v"+this.versionName;
        }
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
        let oldFields = <TableColumnDefinition[]>this.table.feedDefinitionTableSchema.fields;
        let oldPartitions = this.table.partitions;
        let oldUserProperties = this.userProperties;
        this.updateNonNullFields(model);
        this.table.update(oldFields, oldPartitions);
        UserPropertyUtil.updatePropertyIds(oldUserProperties, this.userProperties);
    }


    initialize() {
        this.readonly = true;
        this.accessControl = FeedAccessControl.NO_ACCESS;
        this.systemFeedName = '';
        this.feedName = '';
        this.mode = FeedMode.DRAFT;
        this.category = {id: null, name: null, systemName: null, description: null};
        this.table = new FeedTableDefinition()
        this.dataTransformation = new DefaultFeedDataTransformation();
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
            },
            uiState: {}
        };
        this.loadMode = LoadMode.LATEST;
    }

    isNew() {
        return this.id == undefined;
    }


    /**
     * Does this draft feed have a deployed version
     * @return {boolean}
     */
    hasBeenDeployed(){
        return (!this.isDraft()) ||
            (this.isDraft() && !_.isUndefined(this.deployedVersion) && !_.isUndefined(this.deployedVersion.id));
    }

    isDraft(){
        return this.mode == FeedMode.DRAFT
    }


    isDeployed(){
        return this.mode == FeedMode.DEPLOYED || this.mode == FeedMode.DEPLDYED_WITH_ACTIVE_DRAFT;
    }

    isDeployedWithActiveDraft() {
        return this.mode == FeedMode.DEPLDYED_WITH_ACTIVE_DRAFT;
    }

    getStepBySystemName(stepName: string) {
        return this.steps.find(step => step.systemName == stepName);
    }

    validate(updateStepState?: boolean): boolean {
        let valid = true;
        let model = this;
        let disabledSteps: Step[] = [];

        //copy off the old steps
        let copySteps = this._copySteps();
        let stepMap = _.indexBy(copySteps,'systemName');

        let changes = new StepStateChangeEvent(this);

        this.steps.forEach((step: Step) => {
            if(!step.hidden) {
                valid = valid && step.validate(<Feed>this);
                if (updateStepState) {
                    step.updateStepState().forEach(step => disabledSteps.push(step));
                }
            }
        });
        let enabledSteps = this.steps.filter(step => disabledSteps.indexOf(step) == -1);
        enabledSteps.forEach(step => step.disabled = false);
        //detect step changes
        this.steps.forEach((step:Step) => {
            let oldStep = stepMap[step.systemName];
            if(oldStep && step.isStepStateChange(oldStep)){
                changes.addChange(oldStep,step);
            }
        });
        this.isValid = valid;
        if(changes.hasChanges()){
            this.stepChangesSubject.next(changes);
        }

        return valid;
    }

    getTemplateType(): string {
        return this.templateTableOption ? this.templateTableOption : (this.registeredTemplate ? this.registeredTemplate.templateTableOption : '')
    }

    /**
     * is this a feed to define a target table
     * @return {boolean}
     */
    isDefineTable() {
        return FeedTemplateType.DEFINE_TABLE == this.getTemplateType();
    }

    /**
     * Is this a data transformation feed
     * @return {boolean}
     */
    isDataTransformation() {
        return FeedTemplateType.DATA_TRANSFORMATION == this.getTemplateType();
    }

    /**
     * Does this feed have any data transformation sets defined
     * @return {boolean}
     */
    hasDataTransformationDataSets() {
        return this.isDataTransformation() && this.dataTransformation && this.dataTransformation.datasets && this.dataTransformation.datasets.length > 0;
    }

    /**
     * Are all the steps for this feed complete
     * @return {boolean}
     */
    isComplete() {
        let complete = true;
        this.steps.filter(step => !step.hidden).forEach(step => complete = complete && step.complete);
        return complete;
    }

    /**
     * Is the feed editable
     * Does the user have access to edit and make sure its not a 'deployed' feed
     * @return {boolean}
     */
    canEdit(){
        return this.accessControl.allowEdit && this.loadMode != LoadMode.DEPLOYED;
    }

    /**
     * Sets the table method as to where the target table was build from (i.e. database, sample file, manual)
     * @param {TableMethod | string} method
     */
    setTableMethod(method:(TableMethod | string)){
        this.table.method = method;
        if(method == TableMethod.EXISTING_TABLE){
            this.options.skipHeader = true;
        }
    }

    /**
     * mark the data as structured (i.e. JSON, or not (CSV) )
     * @param {boolean} structured
     */
    structuredData(structured:boolean){
        this.table.structured = structured;
        if(structured){
            this.options.skipHeader = false;
        }
        else {
            this.options.skipHeader = true;
        }
    }
    /**
     * returns the array of paths used for the source sample preview
     * will return an empty array of no paths are used
     * @return {string[]}
     */
    getSourcePaths(): string[] {
        let paths: string[] = [];
        if (this.sourceDataSets && this.sourceDataSets.length > 0) {
            //get the array of paths to use
            paths = this.sourceDataSets.map((dataset: SparkDataSet) => {
                return dataset.resolvePaths();
            }).reduce((a: string[], b: string[]) => {
                return a.concat(b);
            }, []);
        }
        return paths;
    }

    private _copySteps():Step[]{
        //steps have self references back to themselves.
        let allSteps: Step[] = [];
        this.steps.forEach(step => {
            let copy = step.shallowCopy();
            allSteps.push(copy);
            copy.allSteps = allSteps;
        });
        return allSteps;
    }


    /**
     * Deep copy of this object
     * @return {Feed}
     */
    copy(keepCircularReference: boolean = true): Feed {
        let allSteps = this._copySteps();
        let newFeed: Feed = new Feed(this)
        //  Object.assign(newFeed,this)
        newFeed.steps = null;
        let copy: Feed;
        if (keepCircularReference) {
            copy = CloneUtil.deepCopy(newFeed);
        }
        else {
            copy = CloneUtil.deepCopyWithoutCircularReferences(newFeed);
        }
        copy.steps = allSteps;
        return copy;
    }

    /**
     * Return a copy of this model that is stripped of un necessary properties ready to be saved
     * @param {Feed} copy
     * @return {Feed}
     */
    copyModelForSave(): Feed {
        //create a deep copy
        let copy = this.copy();
        copy.stepChangesSubject = undefined;

        copy.originalTableSchema = undefined;

        //only do this if the schema could have changed (i.e. never been deployed)
        if (copy.table && copy.table.feedDefinitionFieldPolicies && copy.table.feedDefinitionTableSchema && copy.table.feedDefinitionTableSchema.fields) {
            //if the sourceSchema is not defined then set it to match the target
            let addSourceSchemaFields: boolean = copy.table.sourceTableSchema.fields.length == 0;
            let addFeedSchemaFields =  copy.table.feedTableSchema.fields.length == 0;


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

            copy.table.feedDefinitionTableSchema.fields.forEach((columnDef: TableColumnDefinition, idx: number) => {

                let sourceField: TableColumnDefinition = columnDef.copy();
                let feedField: TableColumnDefinition = columnDef.copy();


                    //set the original names as the source field names
                    sourceField.name = columnDef.origName;
                    sourceField.derivedDataType = columnDef.origDataType;
                    //remove sample
                    sourceField.prepareForSave();



                    feedField.prepareForSave();
                    // structured files must use the original names
                     if (copy.table.structured == true) {
                         feedField.name = columnDef.origName;
                         feedField.derivedDataType = columnDef.origDataType;
                      }

                if (!columnDef.deleted) {
                    //remove sample
                    columnDef.prepareForSave();
                    tableFields.push(columnDef);

                    let policy = copy.table.feedDefinitionFieldPolicies[idx];
                    if (policy) {
                        policy.feedFieldName = feedField.name;
                        policy.name = columnDef.name;
                        policy.field = null;
                        newPolicies.push(policy);
                    }
                    sourceFields.push(sourceField)
                    feedFields.push(feedField)
                }
                else {
                    // For files the feed table must contain all the columns from the source even if unused in the target
                    if (copy.table.method == 'SAMPLE_FILE' || copy.isDataTransformation()) {
                        feedFields.push(feedField);
                    }
                    //if we somehow deleted the source incremental field from the target we need to push it back into the source map
                    if( sourceField.name == copy.table.sourceTableIncrementalDateField) {
                        feedFields.push(feedField);
                        sourceFields.push(sourceField);
                    }  else if(copy.table.method == "EXISTING_TABLE"){
                        sourceFields.push(sourceField);
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

            if (copy.table.sourceTableSchema == undefined) {
                copy.table.sourceTableSchema = new SourceTableSchema();
            }

            if (copy.table.feedTableSchema == undefined) {
                copy.table.feedTableSchema = new FeedTableSchema();
            }

            //ensure the source and feed tables match those defined by this feed
            if (!this.hasBeenDeployed() && sourceFields.length != 0 ) {
                copy.table.sourceTableSchema.fields = sourceFields;
            }

            if (!this.hasBeenDeployed() && feedFields.length != 0 ) {
                copy.table.feedTableSchema.fields = feedFields;
            }

            if (copy.registeredTemplate) {
                copy.registeredTemplate = undefined;
            }

        }


        if (copy.inputProcessor != null) {
            _.each(copy.inputProcessor.properties, (property: any) => {
                NifiFeedPropertyUtil.initSensitivePropertyForSaving(property)
             //   properties.push(property);
            });
        }

        _.each(copy.nonInputProcessors, (processor: any) => {
            _.each(processor.properties, (property: any) => {
                NifiFeedPropertyUtil.initSensitivePropertyForSaving(property)
              //  properties.push(property);
            });
        });


        return copy;
    }

    /**
     * updates the target and or source with the schem provided in the sourceDataSet
     */
    setSourceDataSetAndUpdateTarget(sourceDataSet: SparkDataSet, mode: TableSchemaUpdateMode = TableSchemaUpdateMode.UPDATE_SOURCE_AND_TARGET, connectorPlugin?: ConnectorPlugin) {

        if (sourceDataSet && sourceDataSet != null) {
            this.sourceDataSets = [sourceDataSet];
        }
        this.updateTarget(sourceDataSet)
    }

    /**
     * updates the target and or source with the schem provided in the sourceDataSet
     */
    setSampleDataSetAndUpdateTarget(sampleDataSet: SparkDataSet, previewDataSet: PreviewDataSet,mode: TableSchemaUpdateMode = TableSchemaUpdateMode.UPDATE_SOURCE_AND_TARGET, connectorPlugin?: ConnectorPlugin) {
        this.sampleDataSet = sampleDataSet;
        this.updateTarget(sampleDataSet)
        if(previewDataSet){
            let sampleFile = previewDataSet instanceof PreviewFileDataSet;
            let table = previewDataSet instanceof  PreviewHiveDataSet || previewDataSet instanceof PreviewJdbcDataSet;
            if(sampleFile) {
                this.table.method = "SAMPLE_FILE";
                if(!(previewDataSet as PreviewFileDataSet).schemaParser.allowSkipHeader) {
                    this.options.skipHeader = false
                }
            }
            else  if(table) {
                this.table.method = "EXISTING_TABLE";
            }

        }
    }

    /**
     * updates the target and or source with the schem provided in the sourceDataSet
     */
    updateTarget(sourceDataSet: SparkDataSet, mode: TableSchemaUpdateMode = TableSchemaUpdateMode.UPDATE_SOURCE_AND_TARGET, connectorPlugin?: ConnectorPlugin) {

        if (sourceDataSet && sourceDataSet != null) {
            let dataSet = sourceDataSet
            let sourceColumns: TableColumnDefinition[] = [];
            let targetColumns: TableColumnDefinition[] = [];
            let feedColumns: TableColumnDefinition[] = [];

            let columns: TableColumn[] = dataSet.schema
            if (columns) {
                columns.forEach(col => {
                    let def = _.extend({}, col);
                    def.derivedDataType = def.dataType;
                    //sample data
                    if (dataSet.preview && dataSet.preview.preview) {
                        let sampleValues: string[] = dataSet.preview.preview.columnData(def.name)
                        def.sampleValues = sampleValues
                    }
                    sourceColumns.push(new TableColumnDefinition((def)));
                    targetColumns.push(new TableColumnDefinition((def)));
                    feedColumns.push(new TableColumnDefinition((def)));
                });
            }
            else {
                //WARN Columns are empty.
               //console.log("EMPTY columns for ", dataSet);
            }
            if (TableSchemaUpdateMode.UPDATE_SOURCE == mode || TableSchemaUpdateMode.UPDATE_SOURCE_AND_TARGET == mode) {
                this.table.sourceTableSchema.fields = sourceColumns;
            }
            if (TableSchemaUpdateMode.UPDATE_TARGET == mode || TableSchemaUpdateMode.UPDATE_SOURCE_AND_TARGET == mode) {
                this.table.feedTableSchema.fields = feedColumns;
                this.table.tableSchema.fields = targetColumns;
                this.table.feedDefinitionTableSchema.fields = targetColumns;
                this.table.feedDefinitionFieldPolicies = targetColumns.map(field => {
                    let policy = TableFieldPolicy.forName(field.name);
                    policy.field = field;
                    field.fieldPolicy = policy;
                    return policy;
                });
                //flip the changed flag
                this.table.schemaChanged = true;
            }

            // Update NiFi properties based on Spark data set
            if (connectorPlugin != null && connectorPlugin.nifiProperties != null) {
                const renderingHelper = new FeedDetailsProcessorRenderingHelper();

                // Find matching NiFi input processor
                let connectorProperties: ConnectorPluginNifiProperties = null;
                let inputProcessor = this.inputProcessors.find(processor => {
                    connectorProperties = connectorPlugin.nifiProperties.find(properties =>
                        properties.processorTypes.indexOf(processor.type) > -1
                        || (
                            renderingHelper.isWatermarkProcessor(processor)
                            && (
                                properties.processorTypes.indexOf(renderingHelper.GET_TABLE_DATA_PROCESSOR_TYPE) > -1
                                || properties.processorTypes.indexOf(renderingHelper.GET_TABLE_DATA_PROCESSOR_TYPE2) > -1
                                || properties.processorTypes.indexOf(renderingHelper.SQOOP_PROCESSOR) > -1
                            )
                        )
                    );
                    return typeof connectorProperties !== "undefined";
                });

                // Update feed properties
                if (typeof inputProcessor !== "undefined") {
                    this.inputProcessor = inputProcessor;

                    let propertiesProcessor = inputProcessor;
                    if (renderingHelper.isWatermarkProcessor(inputProcessor)) {
                        propertiesProcessor = this.nonInputProcessors.find(processor => renderingHelper.isGetTableDataProcessor(processor) || renderingHelper.isSqoopProcessor(processor));
                    }

                    if (typeof propertiesProcessor !== "undefined") {
                        const context = {...sourceDataSet.mergeTemplates(), dataSource: sourceDataSet.dataSource};
                        propertiesProcessor.properties.filter(property => property.userEditable).forEach(inputProperty => {
                            if (typeof connectorProperties.properties[inputProperty.key] !== "undefined") {
                                const valueFn = _.template(connectorProperties.properties[inputProperty.key], {escape: null, interpolate: /\{\{(.+?)\}\}/g, evaluate: null});
                                inputProperty.value = valueFn(context);
                            }
                        });
                    }
                }
            }
        }
        else {
            if (TableSchemaUpdateMode.UPDATE_SOURCE == mode || TableSchemaUpdateMode.UPDATE_SOURCE_AND_TARGET == mode) {
                this.sourceDataSets = [];
                this.table.sourceTableSchema.fields = [];
            }
            if (TableSchemaUpdateMode.UPDATE_TARGET == mode || TableSchemaUpdateMode.UPDATE_SOURCE_AND_TARGET == mode) {
                this.table.feedTableSchema.fields = [];
                this.table.tableSchema.fields = [];
                this.table.feedDefinitionTableSchema.fields = [];
                this.table.feedDefinitionFieldPolicies = [];
            }
        }
    }



    validateSchemaDidNotChange(fields?:TableColumnDefinition[]| SchemaField[]){
        var valid = true;
        if(fields == undefined && this.table){
            fields = this.table.feedDefinitionTableSchema.fields
        }
        //if we are editing we need to make sure we dont modify the originalTableSchema
        if(this.hasBeenDeployed() && this.originalTableSchema && fields) {
            //if model.originalTableSchema != model.table.tableSchema  ... ERROR
            //mark as invalid if they dont match
            var origFields = _.chain(this.originalTableSchema.fields).sortBy('name').map(function (i) {
                return i.name + " " + i.derivedDataType;
            }).value().join()
            var updatedFields = _.chain(fields).sortBy('name').map(function (i) {
                return i.name + " " + i.derivedDataType;
            }).value().join()
            valid = origFields == updatedFields;
        }
        this.table.schemaChanged = !valid;
        return valid
    }
}
