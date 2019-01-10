import {DefineFeedService} from "../../services/define-feed.service";
import {TdDialogService} from "@covalent/core/dialogs";
import {FeedSideNavService} from "../../services/feed-side-nav.service";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FormControl, FormGroup} from "@angular/forms";
import {FeedStepConstants} from "../../../../model/feed/feed-step-constants";
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {StateRegistry, StateService} from "@uirouter/angular";
import {Component, Inject, ViewChild} from "@angular/core";
import {EntityAccessControlComponent} from "../../../../shared/entity-access-control/entity-access-control.component";
import {AccessControlService} from "../../../../../services/AccessControlService";
import {of} from "rxjs/observable/of";
import {Observable} from "rxjs/Observable";

@Component({
    selector: "define-feed-permissions",
    styleUrls: ["./define-feed-permissions.component.scss"],
    templateUrl: "./define-feed-permissions.component.html"
})
export class DefineFeedPermissionsComponent extends AbstractFeedStepComponent {

    displayEditActions:boolean;

    /**
     * The form for this step
     */
    formGroup:FormGroup;

    accessControlService: AccessControlService;

    @ViewChild("entityAccessControl")
    private entityAccessControl: EntityAccessControlComponent;

    constructor(defineFeedService: DefineFeedService,
                stateService: StateService,
                feedLoadingService:FeedLoadingService,
                dialogService: TdDialogService,
                feedSideNavService:FeedSideNavService,
                @Inject("AccessControlService") accessControlService: AccessControlService){
        super(defineFeedService,stateService, feedLoadingService,dialogService, feedSideNavService);
        this.accessControlService = accessControlService;
        this.formGroup = new FormGroup({})
        this.subscribeToFormChanges(this.formGroup);


    }

    checkEntityAccess() {
        let entityAccessControlCheck:Observable<boolean> = of(this.accessControlService.checkEntityAccessControlled());
        entityAccessControlCheck.subscribe((result: any) => {
                this.displayEditActions = this.accessControlService.isEntityAccessControlled() && this.feed.accessControl.allowChangePermissions;
            }, (err: any) => {
                console.log("Error checking if entity access control is enabled");
            });
    }

    init() {
        super.init();
        this.checkEntityAccess();
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
                this.goToSetupGuideSummary();
            }, error1 => {
            this.feedLoadingService.resolveLoading();
        });
    }
}