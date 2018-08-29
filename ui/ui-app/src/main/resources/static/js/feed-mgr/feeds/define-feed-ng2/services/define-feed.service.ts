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
import {SelectionService} from "../../../catalog/api/services/selection.service";
import {FeedSourceSampleChange} from "./feed-source-sample-change-listener";
import {MatSnackBar} from "@angular/material/snack-bar";
import {PartialObserver} from "rxjs/Observer";
import {ISubscription} from "rxjs/Subscription";


export enum TableSchemaUpdateMode {
    UPDATE_SOURCE=1, UPDATE_TARGET=2, UPDATE_SOURCE_AND_TARGET=3
}

@Injectable()
export class DefineFeedService {

    /**
     *
     * The feed in the same state as the server
     * All Steps will get a copy of the feed that they manipulate and save back to this service.
     * This object will get updated after each explicit Load or after a Save
     */
    feed:Feed;


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




    private uiComponentsService :UiComponentsService;

    private registerTemplatePropertyService :RegisterTemplatePropertyService;

    private feedService :FeedService;




    /**
     * Listen for when a user chooses a new source
     */
    private previewDatasetCollectionService:PreviewDatasetCollectionService;

    constructor(private http:HttpClient,    private _translateService: TranslateService,private $$angularInjector: Injector,
                private _dialogService: TdDialogService,
                private selectionService: SelectionService,
                private feedSourceSampleChange:FeedSourceSampleChange,
                private snackBar: MatSnackBar){

        this.previewDatasetCollectionService = $$angularInjector.get("PreviewDatasetCollectionService");
        this.previewDatasetCollectionService.datasets$.subscribe(this.onDataSetCollectionChanged.bind(this))

        this.currentStepSubject = new Subject<Step>();
        this.currentStep$ = this.currentStepSubject.asObservable();

        this.uiComponentsService = $$angularInjector.get("UiComponentsService");

        this.registerTemplatePropertyService = $$angularInjector.get("RegisterTemplatePropertyService");

        this.feedService = $$angularInjector.get("FeedService");


    }

    /**
     * Load a feed based upon its UUID and return a copy to the subscribers
     *
     * @param {string} id
     * @return {Observable<Feed>}
     */
    loadFeed(id:string, force?:boolean) :Observable<Feed> {

        let feed = this.getFeed();
        if((feed && feed.id != id) || (feed == undefined && id != undefined) || force == true) {
            let loadFeedSubject = new Subject<Feed>();
            let loadFeedObservable$ :Observable<Feed> = loadFeedSubject.asObservable();

            let observable = <Observable<Feed>> this.http.get("/proxy/v1/feedmgr/feeds/" + id)
            observable.subscribe((feed) => {
                //reset the collection service
                this.previewDatasetCollectionService.reset();
                this.selectionService.reset()
                //convert it to our needed class
                let feedModel = new Feed(feed)
                //reset the propertiesInitalized flag
                feedModel.propertiesInitialized = false;

                this.initializeFeedSteps(feedModel);
                feedModel.validate(true);
                this.feed = feedModel;
                //reset the dataset collection service
                this.previewDatasetCollectionService.reset();
                //notify subscribers of a copy
                loadFeedSubject.next(feedModel.copy());

            });
            return loadFeedObservable$;
        }
        else if(feed){
            return Observable.of(this.feed.copy())
        }
        else {
            return Observable.empty();
        }
    }

    ensureSparkShell(){
        this.http.post(RestUrlConstants.SPARK_SHELL_SERVICE_URL+ "/start", null).subscribe((response)=> {
        },(error1 => {
            console.error("ERROR STARTING spark shell ",error1)
        }));
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
     * Save the Feed
     * Users can subscribe to this event via the savedFeedSubject
     * @return {Observable<Feed>}
     */
    saveFeed(feed:Feed) : Observable<SaveFeedResponse>{

        let valid = feed.validate(false);
        if(feed.isDraft() || (!feed.isDraft() && valid)) {
            return this._saveFeed(feed);
        }
        else {
            //errors exist before we try to save
            //notify watchers that this step was saved
            let response = new SaveFeedResponse(this.feed,false,"Error saving feed "+this.feed.feedName+". You have validation errors");
            /**
             * Allow other components to listen for changes to the currentStep
             *
             */
            let  savedFeedSubject: Subject<SaveFeedResponse> = new Subject<SaveFeedResponse>();
            let  savedFeed$: Observable<SaveFeedResponse> = savedFeedSubject.asObservable();
            savedFeedSubject.next(response);
            return savedFeed$;
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
        if(this.currentStep == undefined || this.currentStep != step){
            //notify of change
            this.currentStepSubject.next(step);
        }
        this.currentStep = step;
    }

    getCurrentStep(){
        return this.currentStep;
    }

    subscribeToStepChanges(observer:PartialObserver<Step>) :ISubscription{
      return  this.currentStepSubject.subscribe(observer);
    }

    /**
     * Gets the current feed
     * This is not the copy!!!
     * @return {Feed}
     */
    getFeed(): Feed{
        return this.feed != undefined && this.feed.copy() || undefined;
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
        let templateTableOption = feed.getTemplateType()
        if(feed.isDefineTable()){
            feed.steps = this.newDefineTableFeedSteps();
        }
        else if(feed.isDataTransformation()){
          feed.steps = this.newDataTransformationSteps();
         }
        else {
            feed.steps = this.newSimpleFeedSteps();
        }
    }


    generalInfoStep(steps:Step[]):Step{
        let name = this._translateService.instant("views.define-feed-stepper.GeneralInfo")
        return new StepBuilder().setNumber(1).setName(name).setSystemName(FeedStepConstants.STEP_GENERAL_INFO).setDescription("Describe the feed, assign properties and schedule").setSref("general-info").setAllSteps(steps).setDisabled(false).setRequired(true).setValidator(new DefineFeedStepGeneralInfoValidator()).build();
    }

    feedDetailsStep(steps:Step[], stepNumber:number):Step {
        let name = this._translateService.instant("views.define-feed-stepper.FeedDetails")
        return new StepBuilder().setNumber(stepNumber).setName(name).setSystemName(FeedStepConstants.STEP_FEED_DETAILS).setDescription("Update NiFi processor settings").addDependsUpon(FeedStepConstants.STEP_GENERAL_INFO).setAllSteps(steps).setSref("feed-details").setDisabled(true).setRequired(true).build();
    }

    private newDefineTableFeedSteps() :Step[] {
        let steps :Step[] = []
        let generalInfoStep = this.generalInfoStep(steps);
        let sourceSampleStep = new StepBuilder().setNumber(2).setSystemName(FeedStepConstants.STEP_SOURCE_SAMPLE).setDescription("Browse catalog for sample").addDependsUpon(FeedStepConstants.STEP_GENERAL_INFO).setAllSteps(steps).setSref("datasources").setDisabled(true).setRequired(true).setValidator(new DefineFeedStepSourceSampleValidator(this.previewDatasetCollectionService)).build();
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
        let sourceSampleStep = new StepBuilder().setNumber(2).setSystemName(FeedStepConstants.STEP_SOURCE_SAMPLE).setDescription("Browse catalog for sample").addDependsUpon(FeedStepConstants.STEP_GENERAL_INFO).setAllSteps(steps).setSref("datasources").setDisabled(true).setRequired(true).setValidator(new DefineFeedStepSourceSampleValidator(this.previewDatasetCollectionService)).build();
        let wranglerStep =  new StepBuilder().setNumber(3).setSystemName(FeedStepConstants.STEP_WRANGLER).setDescription("Data Wrangler").addDependsUpon(FeedStepConstants.STEP_GENERAL_INFO).addDependsUpon(FeedStepConstants.STEP_SOURCE_SAMPLE).setAllSteps(steps).setSref("wrangler").setDisabled(true).setRequired(true).build();
        let table = new StepBuilder().setNumber(4).setSystemName(FeedStepConstants.STEP_FEED_TARGET).setDescription("Define target table").addDependsUpon(FeedStepConstants.STEP_WRANGLER).setAllSteps(steps).setSref("feed-table").setDisabled(true).setRequired(true).setValidator(new DefineFeedTableValidator()).build();
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
    private _saveFeed(feed:Feed) : Observable<SaveFeedResponse>{
        let body = feed.copyModelForSave();

        let savedFeedSubject = new Subject<SaveFeedResponse>();
        let savedFeedObservable$ = savedFeedSubject.asObservable();


        let newFeed = body.id == undefined;
        //remove circular steps
        delete body.steps;
        let observable : Observable<Feed> = <Observable<Feed>> this.http.post("/proxy/v1/feedmgr/feeds",body,{ headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            }});
        observable.subscribe((response: any)=> {

            let updatedFeed = response.feedMetadata;
            //turn the response back into our Feed object
            let savedFeed = new Feed(updatedFeed);
            savedFeed.steps = feed.steps;

            //if the properties are already initialized we should keep those values
            if(feed.propertiesInitialized){
                savedFeed.inputProcessor =  feed.inputProcessor;
                savedFeed.inputProcessors = feed.inputProcessors;
                savedFeed.nonInputProcessors = feed.nonInputProcessors;
            }
           // feedCopy.update(savedFeed);

            //reset it to be editable
            savedFeed.readonly = false;

            savedFeed.updateDate = new Date(updatedFeed.updateDate);
            let valid = savedFeed.validate(true);

            //notify watchers that this step was saved
            let message = "Successfully saved "+feed.feedName;
            if(!valid){
                message = "Saved the feed, but you have validation errors.  Please review."
            }
            this.feed = savedFeed;
            let saveFeedResponse = new SaveFeedResponse(savedFeed,true,message);
            saveFeedResponse.newFeed = newFeed;
            savedFeedSubject.next(saveFeedResponse);
        },(error: any) => {
            console.error("Error",error);
            let response = new SaveFeedResponse(feed,false,"Error saving feed "+feed.feedName+". You have validation errors");
            savedFeedSubject.next(response);
        });
        return savedFeedObservable$
    }




    /**
     * Listener for changes from the collection service
     * @param {PreviewDataSet[]} dataSets
     */
    onDataSetCollectionChanged(dataSets:PreviewDataSet[]){
        this.feedSourceSampleChange.dataSetCollectionChanged(dataSets,this.feed)
    }


    public openSnackBar(message:string, duration?:number){
        if(duration == undefined){
            duration = 3000;
        }
        this.snackBar.open(message, null, {
            duration: duration,
        });
    }



}