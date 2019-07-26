import {HttpClient, HttpErrorResponse, HttpHeaders, HttpParams} from "@angular/common/http";
import {Injectable, Injector, ViewContainerRef} from "@angular/core";
import {MatSnackBar} from "@angular/material/snack-bar";
import {TdDialogService} from "@covalent/core/dialogs";
import {TdLoadingService} from "@covalent/core/loading";
import {TranslateService} from "@ngx-translate/core";
import {StateService, Transition} from "@uirouter/angular";
import "rxjs/add/observable/empty";
import 'rxjs/add/observable/forkJoin'
import "rxjs/add/observable/of";
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import {Observable} from "rxjs/Observable";
import {PartialObserver} from "rxjs/Observer";
import {catchError} from "rxjs/operators/catchError";
import {concatMap} from "rxjs/operators/concatMap";
import {filter} from "rxjs/operators/filter";
import {finalize} from "rxjs/operators/finalize";
import {tap} from "rxjs/operators/tap";
import {ReplaySubject} from "rxjs/ReplaySubject";
import {Subject} from "rxjs/Subject";
import {ISubscription} from "rxjs/Subscription";
import * as _ from "underscore";

import {ObjectChanged} from "../../../../../lib/common/common.model";
import {CloneUtil} from "../../../../common/utils/clone-util";
import {AccessControlService} from "../../../../services/AccessControlService";
import {SelectionService} from "../../../catalog/api/services/selection.service";
import {PreviewDataSet} from "../../../catalog/datasource/preview-schema/model/preview-data-set";
import {Category} from "../../../model/category/category.model";
import {EntityVersion} from "../../../model/entity-version.model";
import {FEED_DEFINITION_SECTION_STATE_NAME} from "../../../model/feed/feed-constants";
import {Step} from "../../../model/feed/feed-step.model";
import {Feed, FeedAccessControl, FeedMode, FeedTemplateType, LoadMode, StepStateChangeEvent} from "../../../model/feed/feed.model";
import {SparkDataSet} from "../../../model/spark-data-set.model";
import {FeedService} from "../../../services/FeedService";
import {RestUrlConstants} from "../../../services/RestUrlConstants";
import {UiComponentsService} from "../../../services/UiComponentsService";
import {SaveFeedResponse} from "../model/save-feed-response.model";
import {NewFeedDialogComponent, NewFeedDialogData, NewFeedDialogResponse} from "../new-feed-dialog/new-feed-dialog.component";
import {FeedAccessControlService} from "../services/feed-access-control.service";
import {FeedStepBuilderUtil} from "./feed-step-builder-util";
import {FeedStepConstants} from "../../../model/feed/feed-step-constants";
import {DeployEntityVersionResponse} from "../../../model/deploy-entity-response.model";
import {FeedNifiErrorUtil} from "../../../services/feed-nifi-error-util";
import {Common} from '../../../../../lib/common/CommonTypes';


export class FeedEditStateChangeEvent{

    public readonly: boolean;
    public accessControl:FeedAccessControl;

constructor(readonly:boolean, accessControl:FeedAccessControl){
    this.readonly = readonly;
    this.accessControl = accessControl;
}

}

export enum TableSchemaUpdateMode {
    UPDATE_SOURCE=1, UPDATE_TARGET=2, UPDATE_SOURCE_AND_TARGET=3
}

export interface DefineFeedContainerSideNavEvent {
    opened:boolean;
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

    /**
     * the load mode used for this feed
     */
    feedLoadMode:LoadMode;


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

    private feedEditStateChangeSubject: Subject<FeedEditStateChangeEvent>;

    private feedLoadedSubject: Subject<Feed>;

    private defineFeedContainerSideNavState: Subject<DefineFeedContainerSideNavEvent>;

    private uiComponentsService :UiComponentsService;


    private feedService :FeedService;

    private accessControlService: AccessControlService;


    private loadingFeedCache :Common.Map<Observable<EntityVersion>> = {}

    private loadingFeedErrors: Common.Map<string> = {};



    constructor(private http:HttpClient,    private _translateService: TranslateService,private $$angularInjector: Injector,
                private _dialogService: TdDialogService,
                private _loadingService:TdLoadingService,
                private selectionService: SelectionService,
                private snackBar: MatSnackBar,
                private stateService:StateService,
                private feedAccessControlService:FeedAccessControlService){


        this.currentStepSubject = new Subject<Step>();
        this.currentStep$ = this.currentStepSubject.asObservable();

        this.savedFeedSubject = new Subject<SaveFeedResponse>();

        this.feedLoadedSubject = new Subject<Feed>();

        this.defineFeedContainerSideNavState = new Subject<DefineFeedContainerSideNavEvent>();

        this.uiComponentsService = $$angularInjector.get("UiComponentsService");


        this.feedService = $$angularInjector.get("FeedService");

        this.feedEditStateChangeSubject = new Subject<FeedEditStateChangeEvent>();


    }


    toPreviewDataSet(dataset:SparkDataSet):PreviewDataSet{
        let existingPreview = dataset.preview || {}
        let preview = new PreviewDataSet(existingPreview);
        preview.displayKey =dataset.id;
        preview.dataSource = dataset.dataSource;
        preview.schema = dataset.schema;
        preview.items = [dataset.previewPath ? dataset.previewPath : dataset.resolvePath()];
        preview.sparkOptions = dataset.options;
        if(preview.sparkOptions) {
            preview.sparkOptions['format'] = dataset.format;
        }
        return preview;

    }



    updateFeedRoleMemberships(roleMemberships: any) {
        this.feed.roleMemberships = roleMemberships;
        this.feed.roleMembershipsUpdated = true;

        //notify subscribers of updated feed role memberships
        let savedFeed = new SaveFeedResponse(this.feed, true, "Updated role memberships");
        this.savedFeedSubject.next(savedFeed);
    }

    cloneFeed(feed?:Feed){
        let feedToClone = feed != undefined ? this.getFeed() : feed;

            let template = feedToClone.registeredTemplate;
            let config:NewFeedDialogData = new NewFeedDialogData(template," Clone "+feedToClone.getFullName());
            this._dialogService.open(NewFeedDialogComponent, {data: config, width:'800px'})
                .afterClosed()
                .filter(value => typeof value !== "undefined").subscribe((newFeedData:NewFeedDialogResponse) => {
                    this._loadingService.register("processingFeed")
                let template = newFeedData.template;
                let feedName = newFeedData.feedName;
                let systemFeedName = newFeedData.systemFeedName;
                let category:Category = newFeedData.category;
                let copy = this.feed.copyModelForSave();
                copy.id = undefined;
                copy.deployedVersion = undefined;
                copy.feedName = feedName;
                copy.systemFeedName = systemFeedName
                copy.category = newFeedData.category;
                copy.versionId = undefined;
                copy.versionName = undefined;
                this.saveFeed(copy).subscribe((response:SaveFeedResponse) => {
                    if(response.success){
                        let feed = response.feed;
                        this.openSnackBar("Successfully cloned the feed ",5000)
                        this._loadingService.resolve("processingFeed")
                        this.stateService.go(FEED_DEFINITION_SECTION_STATE_NAME+".setup-guide",{feedId:feed.id},{"location":"replace"})
                    }
                    else {
                        //ERROR
                        this._loadingService.resolve("processingFeed")
                        this.openSnackBar("Error cloning feed ",5000)
                    }
                }, error1 => {
                    //ERROR!!!!!
                    this._loadingService.resolve("processingFeed")
                    this.openSnackBar("Error cloning feed ",5000)
                })
            });

    }



    /**
     * Load a feed based upon its UUID and return a copy to the subscribers
     *
     * @param {string} id
     * @return {Observable<Feed>}
     */
    loadFeed(id:string, loadMode:LoadMode = LoadMode.LATEST, force?:boolean) :Observable<Feed> {

        let feed = this.getFeed();
        if((feed && (feed.id != id || this.feedLoadMode != loadMode)) || (feed == undefined && id != undefined) || force == true) {

            //if asking for a deployed feed and the feed matches the id to load and the feed is deployed
            if(feed && feed.id == id && loadMode == LoadMode.DEPLOYED && feed.isDeployed()){
                return Observable.of(this.feed.copy())
            }
            else  if(feed && feed.id == id && loadMode == LoadMode.DRAFT && feed.isDraft()){
                return Observable.of(this.feed.copy())
            }


            if (LoadMode.LATEST == loadMode) {
               return this.loadLatestFeed(id, true,force);

            }
            else if (LoadMode.DEPLOYED == loadMode) {
                return this.loadDeployedFeed(id, false,force).pipe(
                    catchError((error1: any) => {
                        console.log("unable to load deployed feed... retry against latest", id, error1);
                        return this.loadLatestFeed(id, true, false)
                    }));

            }
            else if (LoadMode.DRAFT == loadMode) {
                return this.loadDraftFeed(id,true,force)
            }
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
    private loadingCacheKey(feedId:string, loadMode:LoadMode){
        return feedId+"-"+loadMode;
    }




    loadLatestFeed(feedId:string, alertOnError:boolean = true, force:boolean = false):Observable<Feed>{
         let url = "/proxy/v1/feedmgr/feeds/"+feedId+"/versions/latest";
       return  this._loadFeedVersion(feedId, url,LoadMode.LATEST, true, true, force)
    }

    loadDeployedFeed(feedId:string, alertOnError:boolean = true, force:boolean = false):Observable<Feed>{
        let url = "/proxy/v1/feedmgr/feeds/"+feedId+"/versions/deployed";
       return  this._loadFeedVersion(feedId, url,LoadMode.DEPLOYED, true, true, force)
    }

    loadDraftFeed(feedId:string,alertOnError:boolean = true, force:boolean = false):Observable<Feed>{
        let url = "/proxy/v1/feedmgr/feeds/"+feedId+"/versions/draft";
        return  this._loadFeedVersion(feedId, url,LoadMode.DRAFT, true, true, force)
    }

    getDraftFeed(feedId:string):Observable<Feed>{

        let url = "/proxy/v1/feedmgr/feeds/"+feedId+"/versions/draft";
        return  this._loadFeedVersion(feedId, url,LoadMode.DRAFT, false, true, true)
    }

    deployedVersionExists(feedId:string):Observable<string> {
        let url = "/proxy/v1/feedmgr/feeds/"+feedId+"/versions/deployed/exists";
        return <Observable<string>>this.http.get(url,{responseType:'text'});
    }

    draftVersionExists(feedId:string):Observable<string> {
        const headers = new HttpHeaders({'Content-Type':'text/plain; charset=utf-8'});
        let url = "/proxy/v1/feedmgr/feeds/"+feedId+"/versions/draft/exists";
        return <Observable<string>>this.http.get(url,{headers:headers,responseType:'text'});
    }

    /**
     * Updates the main step state for the main feed in the service which is shared.
     * @param {Feed} feed
     * @param {Step} step
     */
    updateStepState(feed:Feed,step:Step) {
        //make sure the feeds match
        if(this.feed.id == feed.id){
            const feedStep = this.feed.getStepBySystemName(step.systemName);
            if(feedStep){
                feedStep.update(step);
            }
        }
    }

    /**
     * Save the Feed
     * Users can subscribe to this event via the savedFeedSubject
     * @param {Feed} feed
     * @param {boolean} validateFeed  should we enforce validation of the entire feed (should be called on deploy)
     * @param {Step} step  pass in the current step to validate the step before saving
     * @return {Observable<SaveFeedResponse>}
     */
    saveFeed(feed:Feed,validateFeed:boolean = false,step?:Step) : Observable<SaveFeedResponse>{

        let valid = validateFeed ? feed.validate(false) : step ? step.validate(feed) : true;
         if (!valid) {
             if(step){
                 step.saved=false;
             }
                this._dialogService.openAlert({
                    title: "Validation Error",
                    message: "Error saving feed " + feed.feedName + ". You have validation errors"
                })
                let response = new SaveFeedResponse(feed, false, "Error saving feed " + feed.feedName + ". You have validation errors");
                // Allow other components to listen for changes to the currentStep
                return Observable.throw(response);
            }
            else {
             if(step){
                 step.saved=true;
             }
                return this._saveFeed(feed);
            }


       // if(feed.isDraft() || (!feed.isDraft() && valid)) {
        //    return this._saveFeed(feed);
       // }
      /**  else {
            //errors exist before we try to save
            //notify watchers that this step was saved
            let response = new SaveFeedResponse(this.feed,false,"Error saving feed "+this.feed.feedName+". You have validation errors");
            // Allow other components to listen for changes to the currentStep
            let  savedFeedSubject: Subject<SaveFeedResponse> = new Subject<SaveFeedResponse>();
            let  savedFeed$: Observable<SaveFeedResponse> = savedFeedSubject.asObservable();
            savedFeedSubject.next(response);
            return savedFeed$;
        }
       */
    }

    public deleteFeed(viewContainerRef?:ViewContainerRef){
        this._dialogService.openConfirm({
            message: 'Delete '+this.feed.feedName+' and all associated data?',
            disableClose: true,
            viewContainerRef: viewContainerRef, //OPTIONAL
            title: 'Confirm Delete', //OPTIONAL, hides if not provided
            cancelButton: 'No', //OPTIONAL, defaults to 'CANCEL'
            acceptButton: 'Yes', //OPTIONAL, defaults to 'ACCEPT'
            width: '500px', //OPTIONAL, defaults to 400px
        }).afterClosed().subscribe((accept: boolean) => {
            if (accept) {
                this._loadingService.register("processingFeed");
                this._deleteFeed().subscribe(() => {
                    this.openSnackBar("Deleted the feed")
                    this._loadingService.resolve("processingFeed");
                    this.stateService.go('feeds')
                },(error:any) => {
                    this.openSnackBar("Error deleting the feed ")
                    this._loadingService.resolve("processingFeed");
                })
            } else {
                // DO SOMETHING ELSE
            }
        });
    }


    private _deleteFeed():Observable<any> {
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

    subscribeToSideNavChanges(observer:PartialObserver<DefineFeedContainerSideNavEvent>) :ISubscription {
        return this.defineFeedContainerSideNavState.subscribe(observer);
    }

    subscribeToStepChanges(observer:PartialObserver<Step>) :ISubscription{
      return  this.currentStepSubject.subscribe(observer);
    }

    subscribeToFeedSaveEvent(observer: PartialObserver<SaveFeedResponse>): ISubscription {
        if (this.savedFeedSubject.isStopped) {
            this.savedFeedSubject = new Subject<SaveFeedResponse>();
        }
        return this.savedFeedSubject.subscribe(observer);
    }

    subscribeToFeedEditStateChangeEvent(observer:PartialObserver<FeedEditStateChangeEvent>){
        return this.feedEditStateChangeSubject.subscribe(observer);
    }


    subscribeToFeedLoadedEvent(observer:PartialObserver<Feed>){
        return this.feedLoadedSubject.subscribe(observer);
    }

    sideNavStateChanged(event:DefineFeedContainerSideNavEvent){
        this.defineFeedContainerSideNavState.next(event);
    }

    onStepStateChangeEvent(stepChanges:StepStateChangeEvent){
        if(this.feed && stepChanges.feed.id == this.feed.id){
            //update this feed with the new step changes
            stepChanges.changes.forEach((change:ObjectChanged<Step>) => {

                let newStep = change.newValue;
                let currStep = this.feed.getStepBySystemName(newStep.systemName);
                if(currStep){
                    currStep.update(newStep);
                }
            })
        }
    }



    markFeedAsEditable(){

        if(this.feed && this.feed.loadMode != LoadMode.DEPLOYED){
            this.feed.readonly = false;
            this.feedEditStateChangeSubject.next(new FeedEditStateChangeEvent(this.feed.readonly,this.feed.accessControl))
        }
        else if(this.feed && this.feed.loadMode == LoadMode.DEPLOYED){
            console.log("Unable to Edit a deployed feed ",this.feed)
        }
    }

    markFeedAsReadonly(){
        if(this.feed){
            this.feed.readonly = true;
            this.feedEditStateChangeSubject.next(new FeedEditStateChangeEvent(this.feed.readonly,this.feed.accessControl))
        }
    }

    /**
     * Gets a copy of the current feed
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
        //feed.steps = this.getStepsForTemplate(templateType)
        feed.steps = this.getStepsForFeed(feed)
    }

    /**
     * Initialize the Feed Steps based upon the feed template type
     * @param {Feed} feed
     */
    getStepsForTemplate(templateType:string, registeredTemplate?:any){

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
     * Initialize the Feed Steps based upon the feed template type
     * @param {Feed} feed
     */
    getStepsForFeed(feed:Feed){
        let steps:Step[] = [];
        let templateType = feed.getTemplateType()
        let stepUtil = new FeedStepBuilderUtil(this._translateService);
        if(templateType){
            templateType == FeedTemplateType.SIMPLE_FEED;
        }
        if(FeedTemplateType.DEFINE_TABLE == templateType){
            steps = stepUtil.defineTableFeedSteps();
        }
        else if(FeedTemplateType.DATA_TRANSFORMATION == templateType){
            steps = stepUtil.dataTransformationSteps()
            steps.filter(step => step.systemName == FeedStepConstants.STEP_FEED_SOURCE).forEach(step => {
                step.hidden = !feed.renderSourceStep();
            });
        }
        else {
             steps =  stepUtil.simpleFeedSteps();
        }
        return steps;
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
        });

        // Ensure data sets are uploaded with no title. Titles must be unique if set.
        if (body.sourceDataSets) {
            body.sourceDataSets.filter(ds=> ds.isUpload == true).forEach(dataSet => dataSet.title = null);
        }

        //push the steps into the new uiState object on the feed to persist the step status
        body.uiState[Feed.UI_STATE_STEPS_KEY] = JSON.stringify(steps);
        delete body.steps;

        let url = "/proxy/v1/feedmgr/feeds/draft/entity";

        if(!feed.isNew()){
            url = "/proxy/v1/feedmgr/feeds/"+feed.id+"/versions/draft/entity";
        }

        let observable : Observable<Feed> = <Observable<Feed>> this.http.post(url,body,{ headers: {
                'Content-Type': 'application/json; charset=UTF-8'
            }});

        let checkForDraft = !feed.isDraft() ;

        let accessControl = feed.accessControl;
        observable.subscribe((response: any)=> {
            this.clearLoadingFeedCache(feed.id)

            let updatedFeed = response;
            //when a feed is initially created it will have the data in the "feedMetadata" property
            if(response.feedMetadata){
                updatedFeed = response.feedMetadata
            }
            //turn the response back into our Feed object
            let savedFeed = new Feed(updatedFeed);
            savedFeed.steps = feed.steps;

            //if the incoming feed has a templateTableOption set, and the saved feed doesnt... update it
            if(feed.templateTableOption && !savedFeed.templateTableOption) {
                savedFeed.templateTableOption = feed.templateTableOption;
            }

            //if the properties are already initialized we should keep those values
            if(feed.propertiesInitialized){
                savedFeed.inputProcessor =  feed.inputProcessor;
                savedFeed.inputProcessors = feed.inputProcessors;
                savedFeed.nonInputProcessors = feed.nonInputProcessors;
            }
           // feedCopy.update(savedFeed);

            //reset it to be editable
            savedFeed.readonly = false;
            //reassign accessControl
            savedFeed.accessControl = accessControl;
            if(newFeed && savedFeed.accessControl == FeedAccessControl.NO_ACCESS){
                // give it edit permisisons
                savedFeed.accessControl = FeedAccessControl.adminAccess();
            }

            savedFeed.updateDate = new Date(updatedFeed.updateDate);
            let valid = savedFeed.validate(true);

            //notify watchers that this step was saved
            let message = "Successfully saved "+feed.feedName;
            if(!valid){
                message = "Saved the feed, but you have validation errors.  Please review."
            }
            if(checkForDraft){
                //fill in this feeds deployedVersion
                savedFeed.deployedVersion = {id:feed.versionId,name:feed.versionName,createdDate:(feed.createdDate ? feed.createdDate.getDate() : undefined),entityId:feed.id}
            }else if(feed.deployedVersion){
                savedFeed.deployedVersion = feed.deployedVersion;
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
            subject.error(response)
            //notify any global subscribers
            this.savedFeedSubject.error(response);
        });
        return savedFeedObservable$
    }

    deployFeed(feed:Feed) :Observable<DeployEntityVersionResponse|any> {
        feed.validate(false)
        if(feed.isDraft() && feed.isValid && feed.isComplete()){
            let url = "/proxy/v1/feedmgr/feeds/"+feed.id+"/versions/draft";
            let params :HttpParams = new HttpParams();
            params =params.append("action","VERSION,DEPLOY")

            let headers :HttpHeaders = new HttpHeaders();
            headers =headers.append('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8')

           // this._loadingService.register("processingFeed")

           return this.http.post(url,null,{ params:params,headers:headers})
               .map((version:DeployEntityVersionResponse) => {
                this.clearLoadingFeedCache(feed.id)
                this.openSnackBar("Deployed feed v."+version.name,5000)
                   return version;
            }).catch((error1:HttpErrorResponse,caught:Observable<any>) => {
                if(error1.error){
                    this.clearLoadingFeedCache(feed.id)
                    let content = error1.error as DeployEntityVersionResponse;
                    content.httpStatus = error1.status;
                    content.httpStatusText = error1.statusText;
                    //fill the entity.errors object
                    FeedNifiErrorUtil.parseDeployNiFiFeedErrors(content);

                }
                console.log(error1, caught);
             //   this._loadingService.resolve("processingFeed")
                  return Observable.throw(error1);
            }).share();
        }
        else {
            this.openSnackBar("Unable to deploy this feed",5000)
            return null;
        }
    }

    revertDraft(feed: Feed): void {
        feed.validate(false);
        if (feed.isDraft()) {
            this._translateService.get("FeedDefinition.Dialogs.RemoveDraft").pipe(
                concatMap(text => {
                    return this._dialogService.openConfirm({
                        message: text.Message,
                        disableClose: true,
                        title: text.Title,
                        cancelButton: text.Cancel,
                        acceptButton: text.Accept,
                        width: "500px",
                    }).afterClosed()
                }),
                filter((accept: boolean) => accept),
                tap(() => this._loadingService.register("processingFeed")),
                concatMap(() => {
                    let url = "/proxy/v1/feedmgr/feeds/"+feed.id+"/versions/draft";
                    let params: HttpParams = new HttpParams();
                    params = params.append("action","REMOVE");
                    let headers: HttpHeaders = new HttpHeaders();
                    headers = headers.append("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    return this.http.post<EntityVersion>(url, null, {params: params, headers: headers});
                }),
                finalize(() => this._loadingService.resolve("processingFeed"))
            ).subscribe(
                (version: EntityVersion) => {
                    this.openSnackBar("Removed draft "+version.id, 5000);
                    this.clearLoadingFeedCache(feed.id)
                    this.stateService.go("feed-definition.summary.setup-guide",{feedId:feed.id, refresh:true}, {location:'replace'})
                },
                error => {
                    console.log(error);
                    this.clearLoadingFeedCache(feed.id)
                    this.openSnackBar("Failed to remove draft.", 5000);
                }
            );
        }
    }



    public openSnackBar(message:string, duration?:number){
        if(duration == undefined){
            duration = 3000;
        }
        this.snackBar.open(message, null, {
            duration: duration,
        });
    }

    private clearLoadingFeedCache(feedId:string) {
        Object.keys(this.loadingFeedCache).filter((cacheKey:string) => cacheKey.indexOf(feedId) >=0).forEach((key:string) => {
            delete this.loadingFeedCache[key]
        });
        //remove the error map
        delete this.loadingFeedErrors[feedId];
    }



    private _loadFeedVersion(id:string,url:string, loadMode:LoadMode,load:boolean = true, alertOnError:boolean = true, force:boolean = false ){
        let loadFeedSubject = new ReplaySubject<Feed>(1);
        let loadFeedObservable$ :Observable<Feed> = loadFeedSubject.asObservable();

        /**
         * subject for the http request.
         * Any subsequent request asking for this same entity/loadMode will return this as a cached observable rather than hitting the http client
         * @type {}
         */
        let entitySubject: ReplaySubject<EntityVersion> = new ReplaySubject<EntityVersion>(1);

        let feed = this.getFeed();

        //clear the cache if we are switching feeds
        if(feed && (feed.id != id )){
          this.clearLoadingFeedCache(feed.id)
        }

        let key = this.loadingCacheKey(id,loadMode);

        //setup the observable we will return
        let observable: Observable<EntityVersion> = null;

        if(!force && this.loadingFeedCache[key]){
            observable = this.loadingFeedCache[key]
        } else {
            //make the http request and then assign the result to the 'entitySubject' for others to subscribe to
             this.http.get(url).subscribe((entityVersion:EntityVersion) => {
                entitySubject.next(entityVersion);
            }, error1 => {
                if(load && alertOnError && this.loadingFeedErrors[id] == undefined) {
                    this.loadingFeedErrors[id] = id;
                    let message = "There was an error attempting to load the feed";
                    if (error1.status == 404) {
                        message = "Feed not found";
                    }
                    this._dialogService.openAlert({
                        title:"Error loading feed",
                        message: message
                    });
                }
                loadFeedSubject.error(error1)
            });
             //set the observable and cache
            observable = entitySubject.asObservable()
            this.loadingFeedCache[key] = observable;
        }

        observable.subscribe((entityVersion:EntityVersion) => {

            let feed:Feed = <Feed>entityVersion.entity;
            //remove the error map
            delete this.loadingFeedErrors[feed.id];
            feed.mode = entityVersion.name == "draft" ? FeedMode.DRAFT : FeedMode.DEPLOYED;
            feed.versionId = entityVersion.id;
            feed.versionName = entityVersion.name;
            if(entityVersion.deployedVersion == undefined && feed.mode ==  FeedMode.DEPLOYED) {
                //create a copy of this entityVersion without the entity as the deployed version
                entityVersion.deployedVersion = {id:entityVersion.id,
                    createdDate:entityVersion.createdDate,
                    name:entityVersion.name,
                    entityId:entityVersion.entityId,
                    draft:entityVersion.draft }
            }
            feed.deployedVersion = entityVersion.deployedVersion;

            feed.createdDate = entityVersion.createdDate != null ? new Date(entityVersion.createdDate) : undefined;

            const hasBeenDeployed = feed.mode != FeedMode.DRAFT
            this.selectionService.reset()
            //convert it to our needed class
            let uiState = feed.uiState;

            let feedModel = new Feed(feed)
            //reset the propertiesInitalized flag
            feedModel.propertiesInitialized = false;


            //get the steps back from the model
            //let defaultSteps = this.getStepsForTemplate(feedModel.getTemplateType());
            let defaultSteps = this.getStepsForFeed(feedModel);
            //merge in the saved steps
            let savedStepJSON = feedModel.uiState[Feed.UI_STATE_STEPS_KEY] || "[]";
            let steps :Step[] = JSON.parse(savedStepJSON);
            //group by stepName
            let savedStepMap = _.indexBy(steps,'systemName')
            let mergedSteps:Step[] = defaultSteps.map(step => {
                if(savedStepMap[step.systemName]){
                    step.update(savedStepMap[step.systemName]);
                }
                if(hasBeenDeployed){
                    step.visited = true;
                    step.saved = true;
                    step.complete = true;
                }
                return step;
            });
            feedModel.steps = mergedSteps;

            feedModel.validate(true);
            feedModel.loadMode = loadMode;
            if(load) {

                this.feed = feedModel;
                this.feedLoadMode = loadMode;
            }

            this.feedAccessControlService.accessControlCheck(feedModel).subscribe((accessControl:FeedAccessControl) => {
                feedModel.accessControl = accessControl;
                if(load) {
                    this.feedEditStateChangeSubject.next(new FeedEditStateChangeEvent(feedModel.readonly, feedModel.accessControl))
                }
            })


            if(load) {
                //notify subscribers of a copy
                this.feedLoadedSubject.next(feedModel.copy());
            }
            loadFeedSubject.next(feedModel.copy());
        })
        return loadFeedObservable$;

    }





}
