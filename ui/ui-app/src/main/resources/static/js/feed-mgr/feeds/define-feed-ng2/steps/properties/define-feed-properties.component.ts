import {DefineFeedService, FeedEditStateChangeEvent} from "../../services/define-feed.service";
import {TdDialogService} from "@covalent/core/dialogs";
import {FeedSideNavService} from "../../services/feed-side-nav.service";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FormGroup} from "@angular/forms";
import {FeedStepConstants} from "../../../../model/feed/feed-step-constants";
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {StateRegistry, StateService} from "@uirouter/angular";
import {Component, OnChanges, SimpleChanges, ViewChild} from "@angular/core";
import {PropertyListComponent} from "../../../../shared/property-list/property-list.component";
import {Observable} from "rxjs/Observable";
import {UserProperty} from "../../../../model/user-property.model";
import {FormGroupUtil} from "../../../../../services/form-group-util";

@Component({
    selector: "define-feed-properties",
    styleUrls: ["./define-feed-properties.component.scss"],
    templateUrl: "./define-feed-properties.component.html"
})
export class DefineFeedPropertiesComponent extends AbstractFeedStepComponent{

    displayEditActions:boolean

    /**
     * The form for this step
     */
    formGroup:FormGroup;

    @ViewChild("propertyList")
    propertyList: PropertyListComponent;

    constructor(defineFeedService: DefineFeedService,
                stateService: StateService,
                feedLoadingService:FeedLoadingService,
                dialogService: TdDialogService,
                feedSideNavService:FeedSideNavService){
        super(defineFeedService,stateService, feedLoadingService,dialogService, feedSideNavService);
        this.formGroup = new FormGroup({})
    }

    init() {
        super.init();
        this.initEditActions();
    }
    destroy(){

    }

      onPropertiesChanged($event:UserProperty[]){
            this.displayEditActions = true;
      }

    onAllowIndexingChange($event: boolean) {
        //console.log("allowIndexing set to: " + $event);
    }

    private initEditActions(){
        if(!this.feed.readonly ){
            this.displayEditActions = true;
        }
        else {
            this.displayEditActions = false;
        }
    }

    ngAfterViewInit(){
        this.subscribeToFormDirtyCheck(this.formGroup);
        this.subscribeToFormChanges(this.formGroup);

       let formGroup =  this.formGroup.get('userPropertyForm');
       if(formGroup) {
           formGroup.valueChanges.subscribe(changes => {
               if(!this.feed.readonly){
                   this.displayEditActions = true;
               }
           });
       }
    }

    public feedStateChange(event:FeedEditStateChangeEvent){
        this.initEditActions();
    }

    /**
     * Return the name of this step
     * @return {string}
     */
    getStepName() {
        return FeedStepConstants.STEP_PROPERTIES;
    }


    cancelFeedEdit(){
        super.cancelFeedEdit();
        //let oldFeed = this.defineFeedService.getFeed();
        //this.propertyList.reset(oldFeed.userProperties);
        //this.initEditActions();
    }

    /**
     * Update the feed model with the form values
     */
    protected  applyUpdatesToFeed() :(Observable<any>| boolean|null){
        if(this.formGroup.invalid){
            this.dialogService.openAlert({
                title:"Validation error",
                message:"Unable to save the feed properties.  Please fix all form validation errors and try again."
            });

            FormGroupUtil.touchFormControls(this.formGroup);
            return false;
        }
        else {

            //update the model
            let formModel = this.formGroup.value;

            if (this.propertyList) {
                this.propertyList.updateModel();
            }
            //this.feed. .... = formModel. ...
            return null;
        }
    }
}