import {Feed, StepStateChangeEvent} from "../../../model/feed/feed.model";
import {DefineFeedService, FeedEditStateChangeEvent} from "../services/define-feed.service";
import {StateService, Transition} from "@uirouter/angular";
import {OnDestroy, OnInit, TemplateRef} from "@angular/core";
import {SaveFeedResponse} from "../model/save-feed-response.model";
import {AbstractControl, FormGroup} from "@angular/forms";
import {Step} from "../../../model/feed/feed-step.model";
import {FEED_DEFINITION_STATE_NAME, FEED_DEFINITION_SUMMARY_STATE_NAME, FEED_SETUP_GUIDE_STATE_NAME} from "../../../model/feed/feed-constants";
import {FeedLoadingService} from "../services/feed-loading-service";
import {TdDialogService} from "@covalent/core/dialogs";
import {FeedSideNavService} from "../services/feed-side-nav.service";
import {Observable} from "rxjs/Observable";
import {ISubscription} from "rxjs/Subscription";
import 'rxjs/add/operator/distinctUntilChanged';

export abstract class AbstractFeedStepComponent implements OnInit, OnDestroy {


    /**
     * The feed
     */
    public feed: Feed;

    /**
     * The step
     */
    public step: Step;

    /**
     * The name of this step
     * @return {string}
     */
    public abstract getStepName(): string;

    /**
     * flag indicate that the form is valid for this step
     */
    public formValid: boolean;

    /**
     * indicate that this step is subscribing to form changes
     */
    private subscribingToFormChanges: boolean;

    private feedEditStateChangeEvent: ISubscription;

    private feedStepStateChangeSubscription:ISubscription;

    protected redirectAfterSave:boolean = true;


    protected constructor(protected  defineFeedService: DefineFeedService, protected stateService: StateService,
                          protected feedLoadingService: FeedLoadingService, protected dialogService: TdDialogService,
                          protected feedSideNavService: FeedSideNavService) {
        this.feedEditStateChangeEvent = this.defineFeedService.subscribeToFeedEditStateChangeEvent(this._onFeedEditStateChange.bind(this))
    }

    /**
     * component initialized
     * run thing base logic then fire public init method
     */
    ngOnInit() {
        this.initData();
        this.init();
    }

    /**
     *  Component is getting destroyed.
     *  Call any callbacks
     */
    ngOnDestroy() {
        try {
            if (this.feedEditStateChangeEvent) {
                this.feedEditStateChangeEvent.unsubscribe();
            }
            if(this.feedStepStateChangeSubscription){
                this.feedStepStateChangeSubscription.unsubscribe();
            }
            this.destroy();
        } catch (err) {
            console.error("error in destroy", err);
        }
    }

    private _onFeedEditStateChange(event: FeedEditStateChangeEvent) {
        console.log("FEED STATE CHANGED!!!!", event)
        this.feed.readonly = event.readonly;
        this.feed.accessControl = event.accessControl;
        this.feedStateChange(event);
    }

    orignalVal: string;

    /**
     * Allow users to subscribe to their form and mark for changes
     * @param {FormGroup} formGroup
     * @param {number} debounceTime
     */
    subscribeToFormChanges(formGroup: AbstractControl, debounceTime: number = 500) {
        this.subscribingToFormChanges = true;
        // initialize stream
        const formValueChanges$ = formGroup.statusChanges;

        // subscribe to the stream
        formValueChanges$.debounceTime(debounceTime).subscribe(changes => {

            this.formValid = changes == "VALID" //&&  this.tableForm.validate(undefined);
            this.step.valid = this.formValid;
            this.step.validator.hasFormErrors = !this.formValid;
            //callback
            this.onFormStatusChanged(this.formValid);
        });

    }

    subscribeToFormDirtyCheck(formGroup: AbstractControl, debounceTime: number = 500) {
        //watch for form changes and mark dirty
        //start watching for form changes after init time
        formGroup.valueChanges.debounceTime(debounceTime).subscribe(change => {
            if(!formGroup.dirty)
                this.orignalVal = JSON.stringify(change);
                
            if(formGroup.dirty && this.orignalVal !== JSON.stringify(change)){
                this.onFormChanged(change);
            }
        });

    }

    public goToSetupGuideSummary(){
        let redirectState = FEED_SETUP_GUIDE_STATE_NAME;
        this.stateService.go(redirectState,{feedId:this.feed.id, refresh:false}, {location:'replace'})
    }


    /**
     * When a feed changes from read only to edit
     * @param {FeedEditStateChangeEvent} event
     */
    public feedStateChange(event: FeedEditStateChangeEvent) {

    }

    public onStepStateChangeEvent(event:StepStateChangeEvent){
        console.log('STEP Component, step CHANGED!!!',this,event)
        this.defineFeedService.onStepStateChangeEvent(event)
    }


    /**
     * Initialize the component
     */
    public init() {

    }

    /**
     * called when the user moves away from this step
     */
    public destroy() {

    }

    /**
     * Callback when a form changes state
     */
    public onFormChanged(change: any) {
        console.log('FORM CHANGED ', change)
        if (!this.feed.readonly) {
            this.step.markDirty();
        }
    }

    /**
     * Callback when a form changes state
     */
    public onFormStatusChanged(valid: boolean) {
        this.step.setComplete(valid);
    }

    /**
     * Override and return a template ref that will be displayed and used in the toolbar
     * @return {TemplateRef<any>}
     */
    getToolbarTemplateRef(): TemplateRef<any> {
        return undefined;
    }

    /**
     * Called before save to apply updates to the feed model
     */
    protected applyUpdatesToFeed(): (Observable<any> | boolean | null) {
        return null;
    }


    /**
     * When a feed edit is cancelled, reset the forms
     * @param {Feed} feed
     */
    protected cancelFeedEdit() {
        //get the old feed
        this.defineFeedService.markFeedAsReadonly();
        this.feed = this.defineFeedService.getFeed();
    }


    registerLoading(): void {
        this.feedLoadingService.registerLoading();
    }

    resolveLoading(): void {
        this.feedLoadingService.resolveLoading();
    }


    onSave() {
        this.registerLoading();

        let saveCall = () => {
            //notify the subscribers on the actual save call so they can listen when the save finishes
            this.defineFeedService.saveFeed(this.feed).subscribe((response: SaveFeedResponse) => {
                this.defineFeedService.openSnackBar("Saved the feed ", 3000);
                this.resolveLoading();
                this.step.clearDirty();
                if(this.redirectAfterSave) {
                    this.goToSetupGuideSummary();
                }
            }, error1 => {
                this.resolveLoading()
                this.defineFeedService.openSnackBar("Error saving the feed ", 3000);
            })
        }

        let updates = this.applyUpdatesToFeed();
        if (updates == false) {
            //no op.  errors applying update
            this.resolveLoading();
        }
        else if (updates && updates instanceof Observable) {
            updates.subscribe((response: any) => {
                saveCall();
            })
        }
        else {
            saveCall();
        }

    }


    /**
     * public method called from the step-card.component
     */
    onCancelEdit() {
        //warn if there are pending changes
        if ((this.subscribingToFormChanges && this.step.isDirty()) || !this.subscribingToFormChanges) {
            this.dialogService.openConfirm({
                message: 'Are you sure you want to canel editing  ' + this.feed.feedName + '?  All pending edits will be lost.',
                disableClose: true,
                title: 'Confirm Cancel Edit', //OPTIONAL, hides if not provided
                cancelButton: 'No', //OPTIONAL, defaults to 'CANCEL'
                acceptButton: 'Yes', //OPTIONAL, defaults to 'ACCEPT'
                width: '500px', //OPTIONAL, defaults to 400px
            }).afterClosed().subscribe((accept: boolean) => {
                if (accept) {
                    this.cancelFeedEdit();
                } else {
                    // DO SOMETHING ELSE
                }
            });
        }
        else {
            this.cancelFeedEdit();
        }
    }

    /**
     * is the user allowed to leave this component and transition to a new state?
     * @return {boolean}
     */
    uiCanExit(newTransition: Transition): (Promise<any> | boolean) {
        return this.defineFeedService.uiCanExit(this.step, newTransition)
    }


    protected initData() {


        if (this.feed == undefined) {
            this.feed = this.defineFeedService.getFeed();
            if (this.feed == undefined) {
                this.stateService.go(FEED_DEFINITION_STATE_NAME + ".select-template")
            }
        }
        this.feedStepStateChangeSubscription = this.feed.subscribeToStepStateChanges(this.onStepStateChangeEvent.bind(this))

        this.step = this.feed.steps.find(step => step.systemName == this.getStepName());
        if (this.step) {
            this.step.dirty = false;
            this.step.visited = true;
            //register any custom toolbar actions
            let toolbarActionTemplate = this.getToolbarTemplateRef();
            if (toolbarActionTemplate) {
                this.feedSideNavService.registerStepToolbarActionTemplate(this.step.name, toolbarActionTemplate)
            }
            this.defineFeedService.setCurrentStep(this.step)
            let valid = this.feed.validate(true);
        }
        else {
            //ERROR OUT
        }

    }


}
