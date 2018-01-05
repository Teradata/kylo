import {Input, OnDestroy, OnInit} from "@angular/core";
import * as angular from "angular";
import {Subscription} from "rxjs/Subscription";

import {DomainType} from "../../services/DomainTypesService";
import {DomainTypeDetailsService} from "../services/details.service";

/**
 * Base class for individual sections of the {@link DomainTypeDetailsComponent}.
 */
export abstract class AbstractSectionComponent implements OnDestroy, OnInit {

    /**
     * Indicates if the user has permission to edit this section.
     */
    @Input()
    public allowEdit: boolean;

    /**
     * Read-only view of the domain type model.
     */
    @Input()
    public model: DomainType;

    /**
     * Domain type model for the edit view.
     */
    editModel: DomainType = {} as DomainType;

    /**
     * Indicates if the edit view is displayed.
     */
    isEditable = false;

    /**
     * Subscription for "failed to save" notifications.
     */
    private failureSubscription: Subscription;

    /**
     * Identifier of the last save request.
     */
    private lastSaveId: number;

    constructor(protected DomainTypeDetailsService: DomainTypeDetailsService) {
        this.failureSubscription = this.DomainTypeDetailsService.failed.subscribe(model => this.onFailure(model));
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
        this.failureSubscription.unsubscribe();
    }

    ngOnInit(): void {
        if (this.isNew) {
            this.onEdit();
            this.isEditable = true;
        }
    }

    /**
     * Cancels edit mode and switches to read-only mode.
     */
    onCancel() {
        this.DomainTypeDetailsService.cancelEdit(this.model);
    }

    /**
     * Deletes the domain type.
     */
    onDelete() {
        this.DomainTypeDetailsService.deleteDomainType(this.model);
    }

    /**
     * Creates a copy of the domain type model for editing.
     */
    onEdit() {
        this.editModel = angular.copy(this.model);
    }

    /**
     * Called when the save operation failed.
     */
    onFailure(model: any) {
        if (model.$$saveId === this.lastSaveId) {
            this.isEditable = true;
        }
    }

    /**
     * Updates the domain type with the specified properties.
     */
    onUpdate(properties: object) {
        const saveModel = angular.extend(angular.copy(this.model), properties);
        this.lastSaveId = saveModel.$$saveId = new Date().getTime();
        this.DomainTypeDetailsService.saveEdit(saveModel);
    }
}
