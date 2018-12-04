import {Input, OnDestroy, OnInit, Component} from "@angular/core";
import {Subscription} from "rxjs/Subscription";

import {DomainType} from "../../services/DomainTypesService";
import {DomainTypeDetailsService} from "../services/details.service";
import {AccessControlService} from "../../../services/AccessControlService";
import {StateService as StateServices} from "../../../services/StateService";
import { DomainTypesService } from "../../services/DomainTypesService";
import { TdDialogService } from "@covalent/core/dialogs";
import { MatSnackBar } from "@angular/material/snack-bar";
import { ObjectUtils } from "../../../../lib/common/utils/object-utils";
import { StateService } from "@uirouter/core";

/**
 * Adds or updates domain types.
 */
@Component({
    templateUrl: './details.component.html'
})
export class DomainTypeDetailsComponent implements OnDestroy, OnInit {

    /**
     * Domain type model for the read-only view.
     */
    public model: DomainType;

    /**
     * Indicates if the edit buttons are visible.
     */
    allowEdit = false;

    /**
     * Subscription to cancel button notifications.
     */
    private cancelSubscription: Subscription;

    /**
     * Subscription to delete button notifications.
     */
    private deleteSubscription: Subscription;

    /**
     * Subscription to save button notifications.
     */
    private saveSubscription: Subscription;
    loaded: boolean = false;

    constructor(private accessControlService: AccessControlService,
                private DomainTypeDetailService: DomainTypeDetailsService,
                private DomainTypesService: DomainTypesService,
                private StateService: StateServices,
                private dialog: TdDialogService,
                private snackBar: MatSnackBar,
                private transition: StateService) {

        this.cancelSubscription = DomainTypeDetailService.cancelled.subscribe(() => this.onCancel());
        this.deleteSubscription = DomainTypeDetailService.deleted.subscribe(() => this.onDelete());
        this.saveSubscription = DomainTypeDetailService.saved.subscribe(model => this.onSave(model));
    }

    /**
     * Indicates if this domain type is newly created.
     */
    get isNew() {
        return (!ObjectUtils.isString(this.model.id) || this.model.id.length === 0);
    }

    ngOnDestroy(): void {
        this.cancelSubscription.unsubscribe();
        this.deleteSubscription.unsubscribe();
        this.saveSubscription.unsubscribe();
    }

    ngOnInit(): void {

        // Check for edit access
        this.accessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowEdit = this.accessControlService.hasAction(AccessControlService.FEEDS_ADMIN, actionSet.actions);
                if (ObjectUtils.isString(this.transition.params.domainTypeId)) {
                    this.DomainTypesService.findById(this.transition.params.domainTypeId).then((response) => {
                        this.model = response;
                        this.loaded = true;
                    });
                } else {
                    this.model = this.DomainTypesService.newDomainType();
                    this.loaded = true;
                }
            });
    }

    /**
     * Cancels the current edit operation. If a new domain type is being created then redirects to the domain types page.
     */
    onCancel() {
        if (this.isNew) {
            this.StateService.FeedManager().DomainType().navigateToDomainTypes();
        }
    };

    /**
     * Deletes the current domain type.
     */
    onDelete() {

        this.dialog.openConfirm({
            message : "Are you sure you want to delete this domain type? Columns using this domain type will appear to have no domain type.",
            title : "Delete Domain Type",
            ariaLabel : "Delete Domain Type",
            acceptButton : "Please do it!",
            cancelButton: "Nope",
            disableClose : false
        }).afterClosed().subscribe((accept: boolean) => {
            if (accept) {
                this.DomainTypesService.deleteById(this.model.id).toPromise()
                .then(() => {
                    this.snackBar.open('Successfully deleted the domain type' + this.model.title,'OK',{duration : 3000});
                    this.StateService.FeedManager().DomainType().navigateToDomainTypes();
                }, (err: angular.IHttpResponse<any>) => {
                    this.dialog.openAlert({
                        message : "The domain type " + this.model.title + " could not be deleted. " + err.data.message,
                        title : "Delete Failed",
                        ariaLabel : "Failed to delete domain type",
                        closeButton : "Got it!",
                        disableClose : false
                    });
                });
            }
        });
    };

    /**
     * Saves the current domain type.
     */
    onSave(saveModel: DomainType) {
        this.DomainTypesService.save(saveModel)
            .then((newModel: DomainType) => this.model = newModel,
                (err: angular.IHttpResponse<any>) => {
                    this.DomainTypeDetailService.fail(err.data);
                    this.dialog.openAlert({
                        message : "The domain type " + this.model.title + " could not be saved. " + err.data.message,
                        title : "Save Failed",
                        ariaLabel : "Failed to save domain type",
                        closeButton : "Got it!",
                        disableClose : false
                    });
                });
    };
}

