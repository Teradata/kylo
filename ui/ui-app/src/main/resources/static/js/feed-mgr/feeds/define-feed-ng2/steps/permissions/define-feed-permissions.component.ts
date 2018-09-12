import {DefineFeedService} from "../../services/define-feed.service";
import {TdDialogService} from "@covalent/core/dialogs";
import {FeedSideNavService} from "../../shared/feed-side-nav.service";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FormControl, FormGroup} from "@angular/forms";
import {FeedStepConstants} from "../../../../model/feed/feed-step-constants";
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {StateRegistry, StateService} from "@uirouter/angular";
import {Component, ViewChild} from "@angular/core";
import {EntityAccessControlComponent} from "../../../../shared/entity-access-control/entity-access-control.component";

@Component({
    selector: "define-feed-permissions",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/permissions/define-feed-permissions.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/permissions/define-feed-permissions.component.html"
})
export class DefineFeedPermissionsComponent extends AbstractFeedStepComponent {

    /**
     * The form for this step
     */
    formGroup:FormGroup;

    @ViewChild("entityAccessControl")
    private entityAccessControl: EntityAccessControlComponent;

    constructor(defineFeedService: DefineFeedService,
                stateService: StateService,
                feedLoadingService:FeedLoadingService,
                dialogService: TdDialogService,
                feedSideNavService:FeedSideNavService){
        super(defineFeedService,stateService, feedLoadingService,dialogService, feedSideNavService);
        this.formGroup = new FormGroup({})
        this.subscribeToFormChanges(this.formGroup);
    }

    init() {
        super.init();
        this.registerFormControls();
        this.subscribeToFormDirtyCheck(this.formGroup);
    }
    destroy(){

    }

    getStepName() {
        return FeedStepConstants.STEP_PERMISSIONS;
    }


    /**
     * register form controls for the feed
     */
    private registerFormControls(){
        //this.formGroup.registerControl("description", new FormControl(this.feed.description));
    }

    onSave() {
        this.feedLoadingService.registerLoading();

        //overriding
        this.entityAccessControl.onSave()
            .subscribe((updatedRoleMemberships) => {
                this.defineFeedService.updateFeedRoleMemberships(updatedRoleMemberships);
                this.feedLoadingService.resolveLoading();
            }, error1 => {
            this.feedLoadingService.resolveLoading();
        });
    }
}