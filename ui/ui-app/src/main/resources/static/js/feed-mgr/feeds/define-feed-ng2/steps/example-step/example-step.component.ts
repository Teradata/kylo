import {DefineFeedService} from "../../services/define-feed.service";
import {TdDialogService} from "@covalent/core/dialogs";
import {FeedSideNavService} from "../../services/feed-side-nav.service";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FormGroup} from "@angular/forms";
import {FeedStepConstants} from "../../../../model/feed/feed-step-constants";
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {StateRegistry, StateService} from "@uirouter/angular";
import {Component} from "@angular/core";
import {Observable} from "rxjs/Observable";

@Component({
    selector: "define-feed-example-step",
    styleUrls: ["./example-step.component.scss"],
    templateUrl: "./example-step.component.html"
})
export class ExampleStepComponent extends AbstractFeedStepComponent {

    /**
     * The form for this step
     */
    formGroup:FormGroup;

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
        this.registerFormControls();
        this.subscribeToFormDirtyCheck(this.formGroup);
    }
    destroy(){

    }

    /**
     * Return the name of this step
     * @return {string}
     */
    getStepName() {
        return "EXAMPLE" //FeedStepConstants.STEP_GENERAL_INFO;
    }


    /**
     * register form controls for the feed
     */
    private registerFormControls(){
        //this.formGroup.registerControl("description", new FormControl(this.feed.description));
    }

    /**
     * Update the feed model with the form values
     */
    protected  applyUpdatesToFeed() :(Observable<any>| null){
        //update the model
        let formModel = this.formGroup.value;
        //this.feed. .... = formModel. ...
        return null;
    }


}