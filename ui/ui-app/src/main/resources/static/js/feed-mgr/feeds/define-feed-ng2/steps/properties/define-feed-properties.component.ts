import {DefineFeedService} from "../../services/define-feed.service";
import {TdDialogService} from "@covalent/core/dialogs";
import {FeedSideNavService} from "../../shared/feed-side-nav.service";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FormGroup} from "@angular/forms";
import {FeedStepConstants} from "../../../../model/feed/feed-step-constants";
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {StateRegistry, StateService} from "@uirouter/angular";
import {Component, ViewChild} from "@angular/core";
import {PropertyListComponent} from "../../../../shared/property-list/property-list.component";

@Component({
    selector: "define-feed-properties",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/properties/define-feed-properties.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/properties/define-feed-properties.component.html"
})
export class DefineFeedPropertiesComponent extends AbstractFeedStepComponent {

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
        this.subscribeToFormDirtyCheck(this.formGroup);
    }
    destroy(){

    }

    /**
     * Return the name of this step
     * @return {string}
     */
    getStepName() {
        return FeedStepConstants.STEP_PROPERTIES;
    }


    cancelFeedEdit(){
        this.propertyList.reset(this.feed.userProperties);
    }

    /**
     * Update the feed model with the form values
     */
    protected  applyUpdatesToFeed() {
        //update the model
        let formModel = this.formGroup.value;

        if(this.propertyList) {
            this.propertyList.updateModel();
        }
        //this.feed. .... = formModel. ...
    }


}