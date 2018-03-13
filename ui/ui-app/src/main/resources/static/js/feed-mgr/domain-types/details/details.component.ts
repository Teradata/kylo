import {Input, OnDestroy, OnInit} from "@angular/core";
import * as angular from "angular";
import {Subscription} from "rxjs/Subscription";

import {moduleName} from "../module-name";
import {DomainType} from "../../services/DomainTypesService";
import {DomainTypeDetailsService} from "../services/details.service";

/**
 * Adds or updates domain types.
 */
export class DomainTypeDetailsComponent implements OnDestroy, OnInit {

    /**
     * Domain type model for the read-only view.
     */
    @Input()
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

    static readonly $inject: string[] = ["$mdDialog", "$mdToast", "AccessControlService", "DomainTypeDetailsService", "DomainTypesService", "StateService"];

    constructor(private $mdDialog: angular.material.IDialogService, private $mdToast: angular.material.IToastService, private AccessControlService: any,
                private DomainTypeDetailService: DomainTypeDetailsService, private DomainTypesService: any, private StateService: any) {
        this.cancelSubscription = DomainTypeDetailService.cancelled.subscribe(() => this.onCancel());
        this.deleteSubscription = DomainTypeDetailService.deleted.subscribe(() => this.onDelete());
        this.saveSubscription = DomainTypeDetailService.saved.subscribe(model => this.onSave(model));
    }

    /**
     * Indicates if this domain type is newly created.
     */
    get isNew() {
        return (!angular.isString(this.model.id) || this.model.id.length === 0);
    }

    $onDestroy() {
        this.ngOnDestroy();
    }

    $onInit() {
        this.ngOnInit();
    }

    ngOnDestroy(): void {
        this.cancelSubscription.unsubscribe();
        this.deleteSubscription.unsubscribe();
        this.saveSubscription.unsubscribe();
    }

    ngOnInit(): void {
        // Check for edit access
        this.AccessControlService.getUserAllowedActions()
            .then((actionSet: any) => {
                this.allowEdit = this.AccessControlService.hasAction(this.AccessControlService.FEEDS_ADMIN, actionSet.actions);
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
        const confirm = this.$mdDialog.confirm()
            .title("Delete Domain Type")
            .textContent("Are you sure you want to delete this domain type? Columns using this domain type will appear to have no domain type.")
            .ariaLabel("Delete Domain Type")
            .ok("Please do it!")
            .cancel("Nope");
        this.$mdDialog.show(confirm).then(() => {
            this.DomainTypesService.deleteById(this.model.id)
                .then(() => {
                    this.$mdToast.show(
                        this.$mdToast.simple()
                            .textContent("Successfully deleted the domain type " + this.model.title)
                            .hideDelay(3000)
                    );
                    this.StateService.FeedManager().DomainType().navigateToDomainTypes();
                }, (err: angular.IHttpResponse<any>) => {
                    this.$mdDialog.show(
                        this.$mdDialog.alert()
                            .clickOutsideToClose(true)
                            .title("Delete Failed")
                            .textContent("The domain type " + this.model.title + " could not be deleted. " + err.data.message)
                            .ariaLabel("Failed to delete domain type")
                            .ok("Got it!")
                    );
                });
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
                    this.$mdDialog.show(
                        this.$mdDialog.alert()
                            .clickOutsideToClose(true)
                            .title("Save Failed")
                            .textContent("The domain type " + this.model.title + " could not be saved. " + err.data.message)
                            .ariaLabel("Failed to save domain type")
                            .ok("Got it!")
                    );
                });
    };
}

angular.module(moduleName)
    .component("domainTypeDetailsComponent", {
        bindings: {
            model: "<"
        },
        controller: DomainTypeDetailsComponent,
        templateUrl: "js/feed-mgr/domain-types/details/details.component.html"
    });
