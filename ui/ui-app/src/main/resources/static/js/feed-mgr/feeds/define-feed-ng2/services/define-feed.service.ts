import * as angular from "angular";
import * as _ from "underscore";
import {Injectable, Injector} from "@angular/core";
import {DefaultFeedModel, FeedModel, Step, StepBuilder} from "../model/feed.model";
import {Common} from "../../../../common/CommonTypes"
import { Templates } from "../../../services/TemplateTypes";
import {Observable} from "rxjs/Observable";
import {Subject} from "rxjs/Subject";
import {PreviewDataSet} from "../../../catalog/datasource/preview-schema/model/preview-data-set";
import {HttpClient} from "@angular/common/http";
import {SaveFeedResponse} from "../model/SaveFeedResponse";
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


@Injectable()
export class DefineFeedService {

    feed:FeedModel;

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
    public beforeSave$: Observable<FeedModel>;

    /**
     * The datasets subject for listening
     */
    private beforeSaveSubject: Subject<FeedModel>;


    /**
     * Allow other components to listen for changes to the currentStep
     *
     */
    public feedStateChange$: Observable<FeedModel>;

    /**
     * The datasets subject for listening
     */
    private feedStateChangeSubject: Subject<FeedModel>;

    private uiComponentsService :UiComponentsService;

    private registerTemplatePropertyService :RegisterTemplatePropertyService;

    private feedService :FeedService;

    /**
     * Listen for when a user chooses a new source
     */
    private previewDatasetCollectionService:PreviewDatasetCollectionService;

    constructor(private http:HttpClient,private $$angularInjector: Injector){
        this.currentStepSubject = new Subject<Step>();
        this.currentStep$ = this.currentStepSubject.asObservable();

        this.savedFeedSubject = new Subject<SaveFeedResponse>();
        this.savedFeed$ = this.savedFeedSubject.asObservable();

        this.beforeSaveSubject = new Subject<FeedModel>();
        this.beforeSave$ = this.beforeSaveSubject.asObservable();


        this.feedStateChangeSubject = new Subject<FeedModel>();
        this.feedStateChange$ = this.feedStateChangeSubject.asObservable();

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
     * @return {Observable<FeedModel>}
     */
    loadFeed(id:string, force?:boolean) :Observable<FeedModel> {

        let feed = this.getFeed();
        if((feed && feed.id != id) || (feed == undefined && id != undefined)) {

            let observable = <Observable<FeedModel>> this.http.get("/proxy/v1/feedmgr/feeds/" + id)
            observable.subscribe((feed) => {
                console.log("LOADED ", feed)
                //convert it to our needed class
                let feedModel = new DefaultFeedModel(feed)
                this.initializeFeedSteps(feedModel);
                feedModel.validate(true);
                this.setFeed(feedModel)
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



    sortAndSetupFeedProperties(feed:FeedModel){
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

    setupFeedProperties(feed:FeedModel,template:any, mode:string) {
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
     * @param {FeedModel} feed
     */
    setFeed(feed:FeedModel) : void{
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
     * @return {Observable<FeedModel>}
     */
    saveFeed() : Observable<FeedModel>{

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
     * @return {FeedModel}
     */
    copyFeed() :FeedModel{
        let feed =this.getFeed();
        let feedCopy :FeedModel = undefined;
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
     * @return {FeedModel}
     */
    getFeed(): FeedModel{
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
     * @param {FeedModel} feed
     */
    initializeFeedSteps(feed:FeedModel){
        let templateTableOption = feed.templateTableOption ? feed.templateTableOption : (feed.registeredTemplate? feed.registeredTemplate.templateTableOption : '')
        if(templateTableOption == "DEFINE_TABLE"){
            feed.steps = this.newDefineTableFeedSteps();
        }
        //else if(feed.templateTableOption == "DATA_TRANSFORMATION"){
        //
        // }
        else {
            feed.steps = this.newSimpleFeedSteps();
        }
    }


    generalInfoStep(steps:Step[]):Step{
        return new StepBuilder().setNumber(1).setSystemName("General Info").setDescription("Feed name and desc").setSref("general-info").setAllSteps(steps).setDisabled(false).setRequired(true).setValidator(new DefineFeedStepGeneralInfoValidator()).build();
    }

    feedDetailsStep(steps:Step[]):Step {
        return new StepBuilder().setNumber(3).setSystemName("Feed Details").setDescription("Update NiFi processor settings").addDependsUpon("General Info").setAllSteps(steps).setSref("feed-details").setDisabled(true).setRequired(true).build();
    }

    private newDefineTableFeedSteps() :Step[] {
        let steps :Step[] = []
        let generalInfoStep = this.generalInfoStep(steps);
        let sourceSampleStep = new StepBuilder().setNumber(2).setSystemName("Source Sample").setDescription("Browse Catalog for sample").addDependsUpon("General Info").setAllSteps(steps).setSref("datasources").setDisabled(true).setRequired(true).setValidator(new DefineFeedStepSourceSampleValidator()).build();
        let feedDetails = this.feedDetailsStep(steps);
        let feedTarget = new StepBuilder().setNumber(4).setSystemName("Feed Target").setDescription("Define Target").addDependsUpon("Source Sample").setAllSteps(steps).setSref("feed-target").setDisabled(true).setRequired(true).build();
        let table = new StepBuilder().setNumber(5).setSystemName("Define Table").setDescription("Table").addDependsUpon("Source Sample").setAllSteps(steps).setSref("feed-table").setDisabled(true).setRequired(true).setValidator(new DefineFeedTableValidator()).build();
        steps.push(generalInfoStep);
        steps.push(sourceSampleStep);
        steps.push(feedDetails);
        steps.push(feedTarget);
        steps.push(table)
        return steps;
    }


    private newSimpleFeedSteps() :Step[] {
        let steps :Step[] = []
        let generalInfoStep = this.generalInfoStep(steps);
        let feedDetails = this.feedDetailsStep(steps);
        steps.push(generalInfoStep);
        steps.push(feedDetails);
        return steps;
    }

    /**
     * Call kylo-services and save the feed
     * @return {Observable<FeedModel>}
     * @private
     */
    private _saveFeed() : Observable<FeedModel>{
        this.feed.systemFeedName = this.feed.systemName;
        let body = angular.copy(this.feed);

        let newFeed = body.id == undefined;
        //remove circular steps
        delete body.steps;
        let observable : Observable<FeedModel> = <Observable<FeedModel>> this.http.post("/proxy/v1/feedmgr/feeds",body,{ headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            }});
        observable.subscribe((response: any)=> {
            let steps = this.feed.steps;
            let updatedFeed = response.feedMetadata;
            //turn the response back into our FeedModel object
            let savedFeed = new DefaultFeedModel(updatedFeed);
            this.feed.update(savedFeed);
            //reset it to be editable
            this.feed.readonly = false;
            //set the steps
            this.feed.steps = steps;
            this.feed.updateDate = new Date(updatedFeed.updateDate);
            this.feed.validate(true);
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
     * Listener for changes from the collection service
     * @param {PreviewDataSet[]} dataSets
     */
    onDataSetCollectionChanged(dataSets:PreviewDataSet[]){
        if(dataSets.length == 0){
            this.feed.table.sourceTableSchema.fields =[];
            this.feed.table.tableSchema.fields = [];
        }
        else {
            let dataSet :PreviewDataSet = dataSets[0];
            let columns: TableColumn[] = dataSet.schema
            //convert to TableColumnDefintion objects
            //set the source and target to the same
            let sourceColumns: TableColumnDefinition[] = [];
            let targetColumns: TableColumnDefinition[] = [];
            columns.forEach(col => {
                let def = angular.extend({}, col);
                def.derivedDataType = def.dataType;
                sourceColumns.push(new TableColumnDefinition((def)));
                targetColumns.push(new TableColumnDefinition((def)));
            });
            this.feed.sourceDataSets = [dataSet.toSparkDataSet()];
            this.feed.table.sourceTableSchema.fields = sourceColumns;
            this.feed.table.tableSchema.fields = targetColumns;
        }
    }




}