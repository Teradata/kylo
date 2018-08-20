import * as angular from "angular";
import * as _ from "underscore";
import {Injectable, Injector} from "@angular/core";
import {Feed} from "../../../model/feed/feed.model";
import {Step, StepBuilder} from "../../../model/feed/feed-step.model";
import {Common} from "../../../../common/CommonTypes"
import { Templates } from "../../../services/TemplateTypes";
import {Observable} from "rxjs/Observable";
import {Subject} from "rxjs/Subject";
import {PreviewDataSet} from "../../../catalog/datasource/preview-schema/model/preview-data-set";
import {HttpClient} from "@angular/common/http";
import {SaveFeedResponse} from "../model/save-feed-response.model";
import {DefineFeedStepGeneralInfoValidator} from "../steps/general-info/define-feed-step-general-info-validator";
import {DefineFeedStepSourceSampleValidator} from "../steps/source-sample/define-feed-step-source-sample-validator";
import "rxjs/add/observable/empty";
import "rxjs/add/observable/of";
import 'rxjs/add/observable/forkJoin'
import {PreviewDatasetCollectionService} from "../../../catalog/api/services/preview-dataset-collection.service";
import {TableColumnDefinition} from "../../../model/TableColumnDefinition";
import {TableColumn} from "../../../catalog/datasource/preview-schema/model/table-view-model";
import {DefineFeedTableValidator} from "../steps/define-table/define-feed-table-validator";
import {EntityAccessControlService} from "../../../shared/entity-access-control/EntityAccessControlService";
import AccessControlService from "../../../../services/AccessControlService";
import {RestUrlConstants} from "../../../services/RestUrlConstants";
import {RegisterTemplatePropertyService} from "../../../services/RegisterTemplatePropertyService";
import {UiComponentsService} from "../../../services/UiComponentsService";
import {FeedService} from "../../../services/FeedService";
import {TableFieldPolicy} from "../../../model/TableFieldPolicy";
import {FeedConstants} from "../../../services/FeedConstants";
import {FeedStepConstants} from "../../../model/feed/feed-step-constants";
import {TranslateService} from "@ngx-translate/core";
import {TdDialogService} from "@covalent/core/dialogs";


export enum TableSchemaUpdateMode {
    UPDATE_SOURCE=1, UPDATE_TARGET=2, UPDATE_SOURCE_AND_TARGET=3
}

@Injectable()
export class DefineFeedService {

    feed:Feed;

    lastSavedFeed:Feed;

    stepSrefMap:Common.Map<Step> = {}

    currentStep:Step

    /**
     * Allow other components to listen for changes to the currentStep
     *
     */
    public currentStep$: Observable<Step>;

    /**
     * The datasets subject for listening
     */
    private currentStepSubject: Subject<Step>;


    /**
     * Allow other components to listen for changes to the currentStep
     *
     */
    public savedFeed$: Observable<SaveFeedResponse>;

    /**
     * The datasets subject for listening
     */
    private savedFeedSubject: Subject<SaveFeedResponse>;



    /**
     * Allow other components to listen for changes to the currentStep
     *
     */
    public beforeSave$: Observable<Feed>;

    /**
     * The datasets subject for listening
     */
    private beforeSaveSubject: Subject<Feed>;

    /**
     * Allow other components to listen for changes to the currentStep
     *
     */
    public cancelFeedEdit$: Observable<Feed>;

    /**
     * The datasets subject for listening
     */
    private cancelFeedEditSubject: Subject<Feed>;

    /**
     * Allow other components to listen for changes to the currentStep
     *
     */
    public feedEdit$: Observable<Feed>;

    /**
     * The datasets subject for listening
     */
    private feedEditSubject: Subject<Feed>;


    /**
     * Allow other components to listen for changes to the currentStep
     *
     */
    public feedStateChange$: Observable<Feed>;

    /**
     * The datasets subject for listening
     */
    private feedStateChangeSubject: Subject<Feed>;

    private uiComponentsService :UiComponentsService;

    private registerTemplatePropertyService :RegisterTemplatePropertyService;

    private feedService :FeedService;


    /**
     * Listen for when a user chooses a new source
     */
    private previewDatasetCollectionService:PreviewDatasetCollectionService;

    constructor(private http:HttpClient,    private _translateService: TranslateService,private $$angularInjector: Injector,
                private _dialogService: TdDialogService){
        this.currentStepSubject = new Subject<Step>();
        this.currentStep$ = this.currentStepSubject.asObservable();

        this.savedFeedSubject = new Subject<SaveFeedResponse>();
        this.savedFeed$ = this.savedFeedSubject.asObservable();

        this.beforeSaveSubject = new Subject<Feed>();
        this.beforeSave$ = this.beforeSaveSubject.asObservable();


        this.feedStateChangeSubject = new Subject<Feed>();
        this.feedStateChange$ = this.feedStateChangeSubject.asObservable();

        this.cancelFeedEditSubject = new Subject<Feed>();
        this.cancelFeedEdit$ = this.cancelFeedEditSubject.asObservable();

        this.feedEditSubject = new Subject<Feed>();
        this.feedEdit$ = this.feedEditSubject.asObservable();

        this.previewDatasetCollectionService = $$angularInjector.get("PreviewDatasetCollectionService");
        this.previewDatasetCollectionService.datasets$.subscribe(this.onDataSetCollectionChanged.bind(this))

        this.uiComponentsService = $$angularInjector.get("UiComponentsService");

        this.registerTemplatePropertyService = $$angularInjector.get("RegisterTemplatePropertyService");

        this.feedService = $$angularInjector.get("FeedService");


    }

    /**
     * Load a feed based upon its UUID
     *
     * @param {string} id
     * @return {Observable<Feed>}
     */
    loadFeed(id:string, force?:boolean) :Observable<Feed> {

        let feed = this.getFeed();
        if((feed && feed.id != id) || (feed == undefined && id != undefined) || force == true) {

            let observable = <Observable<Feed>> this.http.get("/proxy/v1/feedmgr/feeds/" + id)
            observable.subscribe((feed) => {
                console.log("LOADED ", feed)
                //reset the collection service
                this.previewDatasetCollectionService.reset();
                //convert it to our needed class
                let feedModel = new Feed(feed)
                //reset the propertiesInitalized flag
                feedModel.propertiesInitialized = false;

                this.initializeFeedSteps(feedModel);
                feedModel.validate(true);
                this.setFeed(feedModel)
                this.lastSavedFeed = feedModel.copy();
                //reset the dataset collection service
                this.previewDatasetCollectionService.reset();
            });
            return observable;
        }
        else if(feed){
            return Observable.of(this.getFeed())
        }
        else {
            return Observable.empty();
        }
    }

    ensureSparkShell(){
        this.http.post(RestUrlConstants.SPARK_SHELL_SERVICE_URL+ "/start", null).subscribe((response)=> {
            console.log("SPARK SHELL STARTED!");
        },(error1 => {
            console.error("ERROR STARTING spark shell ",error1)
        }));
    }

    onFeedEdit(){
        this.feed.readonly = false;
        this.feedEditSubject.next(this.feed);
        this.feedStateChangeSubject.next(this.feed)
    }

    /**
     * Restore the last saved feed effectively removing all edits done on the current feed
     */
    restoreLastSavedFeed() : Feed{
        if(this.lastSavedFeed) {
            this.feed.update(this.lastSavedFeed.copy());
            this.cancelFeedEditSubject.next(this.feed);
            this.feedStateChangeSubject.next(this.feed)
            return this.getFeed();
        }
        else {
            this.cancelFeedEditSubject.next(this.feed);
            this.feedStateChangeSubject.next(this.feed)
        }
    }



    sortAndSetupFeedProperties(feed:Feed){
        if((feed.inputProcessors == undefined || feed.inputProcessors.length == 0) && feed.registeredTemplate){
            feed.inputProcessors = feed.registeredTemplate.inputProcessors;
        }

        feed.inputProcessors= _.sortBy(feed.inputProcessors, 'name')
        // Find controller services
        _.chain(feed.inputProcessors.concat(feed.nonInputProcessors))
            .pluck("properties")
            .flatten(true)
            .filter((property) => {
                return property != undefined && property.propertyDescriptor && property.propertyDescriptor.identifiesControllerService && (typeof property.propertyDescriptor.identifiesControllerService == 'string' );
            })
            .each((property:any) => this.feedService.findControllerServicesForProperty(property));

        //find the input processor associated to this feed
        feed.inputProcessor = feed.inputProcessors.find((processor: Templates.Processor) => {
            if (feed.inputProcessorName) {
                return   feed.inputProcessorType == processor.type && feed.inputProcessorName.toLowerCase() == processor.name.toLowerCase()
            }
            else {
                return    feed.inputProcessorType == processor.type;
            }
        });
        if(feed.inputProcessor == undefined && feed.inputProcessors && feed.inputProcessors.length >0){
            feed.inputProcessor = feed.inputProcessors[0];
        }
    }

    setupFeedProperties(feed:Feed,template:any, mode:string) {
        if(feed.isNew()){
            this.registerTemplatePropertyService.initializeProperties(template, 'create', feed.properties);
        }
        else {
            this.registerTemplatePropertyService.initializeProperties(template, "edit", []);
        }

      this.sortAndSetupFeedProperties(feed);


        // this.inputProcessors = template.inputProcessors;
        feed.allowPreconditions = template.allowPreconditions;

        //merge the non input processors
        feed.nonInputProcessors = this.registerTemplatePropertyService.removeNonUserEditableProperties(template.nonInputProcessors, false);

    }

    /**
     * Sets the feed
     * @param {Feed} feed
     */
    setFeed(feed:Feed) : void{
        this.feed = feed;
        this.feed.steps.forEach(step => {
            this.stepSrefMap[step.sref] = step;
        })
    }

    beforeSave() {
        this.beforeSaveSubject.next(this.feed);
    }

    /**
     * Save the Feed
     * Users can subscribe to this event via the savedFeedSubject
     * @return {Observable<Feed>}
     */
    saveFeed() : Observable<Feed>{

        let valid = this.feed.validate(false);
        if(valid) {
            return this._saveFeed();
        }
        else {
            //errors exist before we try to save
            //notify watchers that this step was saved
            let response = new SaveFeedResponse(this.feed,false,"Error saving feed "+this.feed.feedName+". You have validation errors");
            this.savedFeedSubject.next(response);
            return null;
        }
    }

    deleteFeed():Observable<any> {
        if(this.feed && this.feed.id) {
            return this.http.delete("/proxy/v1/feedmgr/feeds" + "/" + this.feed.id);
        }
        else {
            return Observable.empty();
        }
    }


    /**
     * Set the current step reference and notify any subscribers
     * @param {Step} step
     */
    setCurrentStep(step:Step){
        this.currentStep = step;
        //notify the observers of the change
        this.currentStepSubject.next(this.currentStep)
    }

    /**
     * Make a copy of this feed
     * @return {Feed}
     */
    copyFeed() :Feed{
        let feed =this.getFeed();
        let feedCopy :Feed = undefined;
        if(feed != undefined) {
            //copy the feed data for this step
            feedCopy = angular.copy(feed);
            //set the steps back to the core steps
            feedCopy.steps = feed.steps;
        }
        return feedCopy;
    }


    getCurrentStep(){
        return this.currentStep;
    }

    /**
     * Gets the current feed
     * @return {Feed}
     */
    getFeed(): Feed{
        return this.feed;
    }

    /**
     * gets the step from the sRef
     * @param {string} sRef
     * @return {Step}
     */
    getStep(sRef:string): Step{
        return this.stepSrefMap[sRef];
    }

    getStepByIndex(index:number){
        return this.feed.steps[index];
    }

    /**
     * Initialize the Feed Steps based upon the feed template type
     * @param {Feed} feed
     */
    initializeFeedSteps(feed:Feed){
        let templateTableOption = feed.templateTableOption ? feed.templateTableOption : (feed.registeredTemplate? feed.registeredTemplate.templateTableOption : '')
        if(templateTableOption == "DEFINE_TABLE"){
            feed.steps = this.newDefineTableFeedSteps();
        }
        else if(templateTableOption == "DATA_TRANSFORMATION"){
          feed.steps = this.newDataTransformationSteps();
         }
        else {
            feed.steps = this.newSimpleFeedSteps();
        }
    }


    generalInfoStep(steps:Step[]):Step{
        let name = this._translateService.instant("views.define-feed-stepper.GeneralInfo")
        return new StepBuilder().setNumber(1).setName(name).setSystemName(FeedStepConstants.STEP_GENERAL_INFO).setDescription("Feed name and desc").setSref("general-info").setAllSteps(steps).setDisabled(false).setRequired(true).setValidator(new DefineFeedStepGeneralInfoValidator()).build();
    }

    feedDetailsStep(steps:Step[], stepNumber:number):Step {
        let name = this._translateService.instant("views.define-feed-stepper.FeedDetails")
        return new StepBuilder().setNumber(stepNumber).setName(name).setSystemName(FeedStepConstants.STEP_FEED_DETAILS).setDescription("Update NiFi processor settings").addDependsUpon(FeedStepConstants.STEP_GENERAL_INFO).setAllSteps(steps).setSref("feed-details").setDisabled(true).setRequired(true).build();
    }

    private newDefineTableFeedSteps() :Step[] {
        let steps :Step[] = []
        let generalInfoStep = this.generalInfoStep(steps);
        let sourceSampleStep = new StepBuilder().setNumber(2).setSystemName(FeedStepConstants.STEP_SOURCE_SAMPLE).setDescription("Browse catalog for sample").addDependsUpon(FeedStepConstants.STEP_GENERAL_INFO).setAllSteps(steps).setSref("datasources").setDisabled(true).setRequired(true).setValidator(new DefineFeedStepSourceSampleValidator()).build();
        let table = new StepBuilder().setNumber(3).setSystemName(FeedStepConstants.STEP_FEED_TARGET).setDescription("Define target table").addDependsUpon(FeedStepConstants.STEP_SOURCE_SAMPLE).setAllSteps(steps).setSref("feed-table").setDisabled(true).setRequired(true).setValidator(new DefineFeedTableValidator()).build();
        let feedDetails = this.feedDetailsStep(steps,4);
        steps.push(generalInfoStep);
        steps.push(sourceSampleStep);
        steps.push(table)
        steps.push(feedDetails);
        return steps;
    }

    private newDataTransformationSteps() :Step[] {
        let steps :Step[] = []
        let generalInfoStep = this.generalInfoStep(steps);
        let sourceSampleStep = new StepBuilder().setNumber(2).setSystemName(FeedStepConstants.STEP_SOURCE_SAMPLE).setDescription("Browse catalog for sample").addDependsUpon(FeedStepConstants.STEP_GENERAL_INFO).setAllSteps(steps).setSref("datasources").setDisabled(true).setRequired(true).setValidator(new DefineFeedStepSourceSampleValidator()).build();
        let wranglerStep =  new StepBuilder().setNumber(3).setSystemName(FeedStepConstants.STEP_WRANGLER).setDescription("Data Wrangler").addDependsUpon(FeedStepConstants.STEP_GENERAL_INFO).addDependsUpon(FeedStepConstants.STEP_SOURCE_SAMPLE).setAllSteps(steps).setSref("wrangler").setDisabled(true).setRequired(true).build();
        let table = new StepBuilder().setNumber(4).setSystemName(FeedStepConstants.STEP_FEED_TARGET).setDescription("Define target table").addDependsUpon(FeedStepConstants.STEP_SOURCE_SAMPLE).setAllSteps(steps).setSref("feed-table").setDisabled(true).setRequired(true).setValidator(new DefineFeedTableValidator()).build();
        let feedDetails = this.feedDetailsStep(steps,5);
        steps.push(generalInfoStep);
        steps.push(sourceSampleStep);
        steps.push(wranglerStep);
        steps.push(table)
        steps.push(feedDetails);
        return steps;
    }


    private newSimpleFeedSteps() :Step[] {
        let steps :Step[] = []
        let generalInfoStep = this.generalInfoStep(steps);
        let feedDetails = this.feedDetailsStep(steps,2);
        steps.push(generalInfoStep);
        steps.push(feedDetails);
        return steps;
    }



    /**
     * Call kylo-services and save the feed
     * @return {Observable<Feed>}
     * @private
     */
    private _saveFeed() : Observable<Feed>{
        let body = this.feed.copyModelForSave();


        let newFeed = body.id == undefined;
        //remove circular steps
        delete body.steps;
        let observable : Observable<Feed> = <Observable<Feed>> this.http.post("/proxy/v1/feedmgr/feeds",body,{ headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            }});
        observable.subscribe((response: any)=> {
            let steps = this.feed.steps;
            let updatedFeed = response.feedMetadata;
            //turn the response back into our Feed object
            let savedFeed = new Feed(updatedFeed);

            //if the properties are already initialized we should keep those values
            if(this.feed.propertiesInitialized){
                savedFeed.inputProcessor =  this.feed.inputProcessor;
                savedFeed.inputProcessors = this.feed.inputProcessors;
                savedFeed.nonInputProcessors = this.feed.nonInputProcessors;
            }
            this.feed.update(savedFeed);

            //reset it to be editable
            this.feed.readonly = false;
            //set the steps
            this.feed.steps = steps;
            this.feed.updateDate = new Date(updatedFeed.updateDate);
            this.feed.validate(true);
            //mark the last saved feed
            this.lastSavedFeed = this.feed.copy();
            //notify watchers that this step was saved
            let saveFeedResponse = new SaveFeedResponse(this.feed,true,"Successfully saved "+this.feed.feedName);
            saveFeedResponse.newFeed = newFeed;
            this.savedFeedSubject.next(saveFeedResponse);
        },(error: any) => {
            console.error("Error",error);
            let response = new SaveFeedResponse(this.feed,false,"Error saving feed "+this.feed.feedName+". You have validation errors");
            this.savedFeedSubject.next(response);
        });
        return observable;
    }

    /**
     * Update the feed table schemas for a given dataset previewed
     * @param {PreviewDataSet} dataSet
     * @param {TableSchemaUpdateMode} mode
     * @private
     */
    private _updateTableSchemas(dataSet:PreviewDataSet,mode:TableSchemaUpdateMode){
        if(dataSet){
            let sourceColumns: TableColumnDefinition[] = [];
            let targetColumns: TableColumnDefinition[] = [];
            let feedColumns: TableColumnDefinition[] = [];

            let columns: TableColumn[] = dataSet.schema
            if(columns) {
                columns.forEach(col => {
                    let def = angular.extend({}, col);
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
                this.feed.sourceDataSets = [dataSet.toSparkDataSet()];
                this.feed.table.sourceTableSchema.fields = sourceColumns;
            }
            if(TableSchemaUpdateMode.UPDATE_TARGET == mode || TableSchemaUpdateMode.UPDATE_SOURCE_AND_TARGET == mode) {
                this.feed.table.feedTableSchema.fields = feedColumns;
                this.feed.table.tableSchema.fields = targetColumns;
                this.feed.table.fieldPolicies = targetColumns.map(field => {
                    let policy = TableFieldPolicy.forName(field.name);
                    policy.field = field;
                    field.fieldPolicy = policy;
                    return policy;
                });
                //flip the changed flag
                this.feed.table.schemaChanged = true;
            }


        }
        else {
            if(TableSchemaUpdateMode.UPDATE_SOURCE == mode || TableSchemaUpdateMode.UPDATE_SOURCE_AND_TARGET == mode){
                this.feed.sourceDataSets = [];
                this.feed.table.sourceTableSchema.fields = [];
            }
            if(TableSchemaUpdateMode.UPDATE_TARGET == mode || TableSchemaUpdateMode.UPDATE_SOURCE_AND_TARGET == mode) {
                this.feed.table.feedTableSchema.fields = [];
                this.feed.table.tableSchema.fields = [];
                this.feed.table.fieldPolicies = [];
            }
        }
    }


    /**
     * Listener for changes from the collection service
     * @param {PreviewDataSet[]} dataSets
     */
    onDataSetCollectionChanged(dataSets:PreviewDataSet[]){
        let dataSet :PreviewDataSet = dataSets[0];
        if(dataSet) {
            //compare existing source against this source
            //    let existingSourceColumns = this.feed.table.sourceTableSchema.fields.map(col => col.name+" "+col.derivedDataType).toString();
            //   let dataSetColumns = dataSet.schema.map(col => col.name+" "+col.dataType).toString();

            // check if the dataset source and item name match the feed
            let previewSparkDataSet = dataSet.toSparkDataSet();
            let key = previewSparkDataSet.id
            let dataSourceId = previewSparkDataSet.dataSource.id;
            let matchingFeedDataSet = this.feed.sourceDataSets.find(sparkDataset => sparkDataset.id == key && sparkDataset.dataSource.id == dataSourceId);

            if (matchingFeedDataSet == undefined) {
                //the source differs from the feed source and if we have a target defined.... confirm change
                if (this.feed.table.tableSchema.fields.length > 0) {
                    this._dialogService.openConfirm({
                        message: 'The source schema has changed.  A target schema already exists for this feed.  Do you wish to reset the target schema to match the new source schema? ',
                        disableClose: true,
                        title: 'Source dataset already defined', //OPTIONAL, hides if not provided
                        cancelButton: 'Cancel', //OPTIONAL, defaults to 'CANCEL'
                        acceptButton: 'Accept', //OPTIONAL, defaults to 'ACCEPT'
                        width: '500px', //OPTIONAL, defaults to 400px
                    }).afterClosed().subscribe((accept: boolean) => {

                        if (accept) {
                            this._updateTableSchemas(dataSet, TableSchemaUpdateMode.UPDATE_SOURCE_AND_TARGET)
                        } else {
                            // no op
                            this._updateTableSchemas(dataSet, TableSchemaUpdateMode.UPDATE_SOURCE);
                        }
                    });
                }
                else {
                    //this will be if its the first time a source is selected for a feed
                    this._updateTableSchemas(dataSet, TableSchemaUpdateMode.UPDATE_SOURCE_AND_TARGET);
                }

            }
            else {
                console.log("No change to source schema found.  ");
            }
        }

    }




}