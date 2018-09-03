import * as _ from "underscore";
import {Injectable, Injector} from "@angular/core";
import {Feed, FeedTemplateType} from "../../../model/feed/feed.model";
import {Step} from "../../../model/feed/feed-step.model";
import {Common} from "../../../../common/CommonTypes"
import { Templates } from "../../../services/TemplateTypes";
import {Observable} from "rxjs/Observable";
import {StateRegistry, StateService, Transition} from "@uirouter/angular";
import {Subject} from "rxjs/Subject";
import {PreviewDataSet} from "../../../catalog/datasource/preview-schema/model/preview-data-set";
import {HttpClient} from "@angular/common/http";
import {SaveFeedResponse} from "../model/save-feed-response.model";
import {DefineFeedStepGeneralInfoValidator} from "../steps/general-info/define-feed-step-general-info-validator";
import {DefineFeedStepSourceSampleValidator} from "../steps/source-sample/define-feed-step-source-sample-validator";
import "rxjs/add/observable/empty";
import "rxjs/add/observable/of";
import 'rxjs/add/observable/forkJoin'
import {DatasetChangeEvent, PreviewDatasetCollectionService} from "../../../catalog/api/services/preview-dataset-collection.service";
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
import {SparkDataSet} from "../../../model/spark-data-set.model";
import {FeedStepBuilderUtil} from "./feed-step-builder-util";
import {CloneUtil} from "../../../../common/utils/clone-util";



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

    /**
     * The datasets subject for listening
     */
    private savedFeedSubject: Subject<SaveFeedResponse>;


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

        this.savedFeedSubject = new Subject<SaveFeedResponse>();

        this.uiComponentsService = $$angularInjector.get("UiComponentsService");

        this.registerTemplatePropertyService = $$angularInjector.get("RegisterTemplatePropertyService");

        this.feedService = $$angularInjector.get("FeedService");


    }

    toPreviewDataSet(dataset:SparkDataSet):PreviewDataSet{
        let preview = new PreviewDataSet();
        preview.displayKey =dataset.id;
        preview.dataSource = dataset.dataSource;
        preview.schema = dataset.schema;
        preview.preview = dataset.preview;
        preview.items = [dataset.previewPath];
        return preview;

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
                //set the collectionService
                if(feed.sourceDataSets){
                    feed.sourceDataSets.map(ds => this.toPreviewDataSet(ds)).forEach(previewDataSet => this.previewDatasetCollectionService.addDataSet(previewDataSet));
                }

                this.selectionService.reset()
                //convert it to our needed class
                let uiState = feed.uiState;

                let feedModel = new Feed(feed)
                //reset the propertiesInitalized flag
                feedModel.propertiesInitialized = false;

                //get the steps back from the model
                let defaultSteps = this.getStepsForTemplate(feedModel.getTemplateType());
                //merge in the saved steps
                let savedStepJSON = feedModel.uiState[Feed.UI_STATE_STEPS_KEY] || "[]";
                let steps :Step[] = JSON.parse(savedStepJSON);
                //group by stepName
                let savedStepMap = _.indexBy(steps,'systemName')
                let mergedSteps:Step[] = defaultSteps.map(step => {
                    if(savedStepMap[step.systemName]){
                       step.update(savedStepMap[step.systemName]);
                    }
                    return step;
                });
                feedModel.steps = mergedSteps;

                feedModel.validate(true);
                this.feed = feedModel;
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

    subscribeToFeedSaveEvent(observer:PartialObserver<SaveFeedResponse>) :ISubscription{
        return  this.savedFeedSubject.subscribe(observer);
    }


    markFeedAsEditable(){
        if(this.feed){
            this.feed.readonly = false;
        }
    }

    markFeedAsReadonly(){
        if(this.feed){
            this.feed.readonly = true;
        }
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
        let templateType = feed.getTemplateType()
        feed.steps = this.getStepsForTemplate(templateType)
    }

    /**
     * Initialize the Feed Steps based upon the feed template type
     * @param {Feed} feed
     */
    getStepsForTemplate(templateType:string){

        let stepUtil = new FeedStepBuilderUtil(this._translateService);
        if(templateType){
            templateType == FeedTemplateType.SIMPLE_FEED;
        }
        if(FeedTemplateType.DEFINE_TABLE == templateType){
            return stepUtil.defineTableFeedSteps();
        }
        else if(FeedTemplateType.DATA_TRANSFORMATION == templateType){
            return stepUtil.dataTransformationSteps()
        }
        else {
            return  stepUtil.simpleFeedSteps();
        }
    }




    /**
     * is the user allowed to leave this component and transition to a new state?
     * @return {boolean}
     */
    uiCanExit(step:Step,newTransition: Transition) :(Promise<any> | boolean) {
        if(step.isDirty()){

            let observable =  this._dialogService.openConfirm({
                message: 'You have unsaved changes.  Are you sure you want to exit? ',
                disableClose: true,
                title: 'Unsaved Changes', //OPTIONAL, hides if not provided
                cancelButton: 'Cancel', //OPTIONAL, defaults to 'CANCEL'
                acceptButton: 'Accept', //OPTIONAL, defaults to 'ACCEPT'
                width: '500px', //OPTIONAL, defaults to 400px
            }).afterClosed();
            observable.subscribe((accept: boolean) => {
                if (accept) {
                    return true;
                } else {
                    // no op
                    return false;
                }
            });
            return observable.toPromise();
        }
        else {
            return true;
        }
    }

    /**
     * Call kylo-services and save the feed
     * @return {Observable<Feed>}
     * @private
     */
    private _saveFeed(feed:Feed) : Observable<SaveFeedResponse>{
        let body = feed.copyModelForSave();

        let subject = new Subject<SaveFeedResponse>();
        let savedFeedObservable$ = subject.asObservable();


        let newFeed = body.id == undefined;
        //remove circular steps
        let steps :Step[] = CloneUtil.deepCopy(body.steps);
        steps.forEach(step => {
            delete step.allSteps;
            delete step.validator;
        })
        //push the steps into the new uiState object on the feed to persist the step status
        body.uiState[Feed.UI_STATE_STEPS_KEY] = JSON.stringify(steps);
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
            //notify any subscribers to this single function
            subject.next(saveFeedResponse)
            //notify any global subscribers
            this.savedFeedSubject.next(saveFeedResponse);
        },(error: any) => {
            console.error("Error",error);
            let response = new SaveFeedResponse(feed,false,"Error saving feed "+feed.feedName+". You have validation errors");
            //notify any subscribers to this single function
            subject.next(response)
            //notify any global subscribers
            this.savedFeedSubject.next(response);
        });
        return savedFeedObservable$
    }




    /**
     * Listener for changes from the collection service
     * @param {PreviewDataSet[]} dataSets
     */
    onDataSetCollectionChanged(event:DatasetChangeEvent){
     //   this.feedSourceSampleChange.dataSetCollectionChanged(event.allDataSets,this.feed)
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