import * as _ from 'underscore';
import { Component, Input, SimpleChanges, OnInit } from '@angular/core';
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
export class PropertiesAdminController implements OnInit{

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

    /**
     * Indicates if all fields are valid.
     * @type {boolean} {@code true} if all fields are valid, or {@code false} otherwise
     */
    @Input() isValid: boolean = true;

    constructor(){
        
    }

    public ngOnInit(){
        this.onModelChange();
    }

    public ngOnChanges(changes: SimpleChanges): void {
        if (changes.model && changes.model.currentValue && !changes.model.firstChange) {
            this.model = changes.model.currentValue;
            this.onModelChange();
        }
    }

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
        }
    };
}