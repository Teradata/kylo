import * as _ from 'underscore';
import { Component, Input, SimpleChanges, OnChanges, Output, EventEmitter } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Subject } from 'rxjs/Subject';
import { ObjectUtils } from '../../../common/utils/object-utils';
import { CloneUtil } from '../../../common/utils/clone-util';
/**
 * A user-defined property field (or business metadata) for categories or feeds.
 *
 * @typedef {Object} UserField
 * @property {string|null} description a human-readable specification
 * @property {string|null} displayName a human-readable title
 * @property {number} order index for the display order from 0 and up
 * @property {boolean} required indicates that the value cannot be empty
 * @property {string} systemName an internal identifier
 * @property {Object.<string, boolean>} [$error] used for validation
 */

@Component({
    selector: 'thinkbig-properties-admin',
    templateUrl: 'js/feed-mgr/shared/properties-admin/properties-admin.html'
})
export class PropertiesAdminController {

    /**
     * Copy of model that mirrors the field list.
     * @type {Array.<UserField>}
     */
    lastModel: any = {};

    /**
     * List of fields in the model.
     * @type {Array.<UserField>}
     */
    fieldList: any[] = [];

    @Input() model: any;
    @Output() modelChange: EventEmitter<any> = new EventEmitter<any>();

    private fieldListObserver = new Subject<any>();

    /**
     * Indicates if all fields are valid.
     * @type {boolean} {@code true} if all fields are valid, or {@code false} otherwise
     */
    @Input() isValid: boolean = true;

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.model && changes.model.currentValue && !changes.model.firstChange) {
            this.model = changes.model.currentValue;
            this.onModelChange();
        }
    }

    /**
    * Adds a new user-defined field.
    */
    addField = () => {
        this.fieldList.push({ description: null, displayName: "", order: this.fieldList.length, required: false, systemName: "", $error: {}, $isNew: true });
        this.onFieldChange();
        this.modelChange.emit(this.fieldList);
    };

    /**
     * Moves the specified field down in the list.
     *
     * @param index the index of the field
     */
    moveDown = (index: any) => {
        this.fieldList.splice(index, 2, this.fieldList[index + 1], this.fieldList[index]);
        this.onFieldChange();
        this.modelChange.emit(this.fieldList);
    };

    /**
     * Moves the specified field up in the list.
     *
     * @param index the index of the field
     */
    moveUp = (index: any) => {
        this.fieldList.splice(index - 1, 2, this.fieldList[index], this.fieldList[index - 1]);
        this.onFieldChange();
        this.modelChange.emit(this.fieldList);
    };

    /**
     * Updates the model with changes to the field list.
     */
    onFieldChange = () => {
        // Convert fields to model
        var hasError: any = false;
        var keys: any = {};
        var model: any = [];
        var order: any = 0;

        this.fieldList.forEach((field: any) => {
            let dn: any = (field.displayName.length === 0);
            // Validate field
            let _: any = (field.$error.duplicate = ObjectUtils.isDefined(keys[field.systemName]));
            hasError |= _;
            hasError |= (field.$error.missingName = dn);

            // Add to user fields object
            if (field.systemName.length > 0) {
                field.order = order++;
                keys[field.systemName] = true;
                model.push(CloneUtil.deepCopy(field));
            }
        });

        // Update model
        this.isValid = !hasError;
        if (!hasError) {
            this.model = model;
            this.lastModel = CloneUtil.deepCopy(this.model);
        }

    };

    /**
     * Updates the field list with changes to the model.
     */
    onModelChange = () => {
        if (!_.isEqual(this.model, this.lastModel)) {
            // Convert model to fields
            this.fieldList = [];
            this.model.forEach((element: any) => {
                var field = CloneUtil.deepCopy(element);
                field.$error = {};
                this.fieldList.push(field);
            });

            // Sort fields
            this.fieldList.sort((a: any, b: any) => {
                return a.order - b.order;
            });

            // Save a copy for update detection
            this.lastModel = CloneUtil.deepCopy(this.model);
            this.onFieldChange();
        }
    };

    /**
     * Deletes the item at the specified index from the user-defined fields list.
     *
     * @param {number} index the index of the field to delete
     */
    removeField = (index: any) => {
        this.fieldList.splice(index, 1);
        this.onFieldChange();
        this.modelChange.emit(this.fieldList);
    };

    /**
     * Updates the system name property of the specified field.
     *
     * @param field the user-defined field
     */
    updateField = (field: any) => {
        if (field.$isNew) {
            field.systemName = field.displayName
                .replace(/[^a-zA-Z0-9]+([a-zA-Z0-9]?)/g, (match: any, p1: any) => { return p1.toUpperCase(); })
                .replace(/^[A-Z]/, (match: any) => { return match.toLowerCase(); });
        }
        this.onFieldChange();
        this.modelChange.emit(this.fieldList);
    }

}