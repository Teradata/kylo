/**
 * @fileoverview added by tsickle
 * @suppress {checkTypes} checked by tsc
 */
import { FieldConfig } from "./FieldConfig";
import * as _ from "underscore";
export class Chip extends FieldConfig {
    /**
     * @param {?=} options
     */
    constructor(options = {}) {
        super(options);
        this.controlType = Chip.CONTROL_TYPE;
        this.items = [];
        this.stacked = false;
        this.disabled = false;
        this.chipAddition = true;
        this.chipRemoval = true;
        this.selectedItems = [];
        this.items = options['items'];
        if (options['values'] !== undefined && _.isArray(options['values'])) {
            this.selectedItems = options['values'].map((item) => {
                return item.label;
            });
            this.filteredItems = this.selectedItems;
        }
        this.stacked = options['stacked'] || false;
    }
    /**
     * @return {?}
     */
    ngOnInit() {
        this.filterItems('');
    }
    /**
     * @param {?} value
     * @return {?}
     */
    filterItems(value) {
        this.filteredItems = this.items.filter((item) => {
            if (value) {
                return item.value && (/** @type {?} */ (item.value)).toLowerCase().indexOf(value.toLowerCase()) > -1;
            }
            else {
                return false;
            }
        }).filter((filteredItem) => {
            return this.selectedItems ? this.selectedItems.indexOf(filteredItem.label) < 0 : true;
        }).map((item) => {
            return item.label;
        });
    }
    /**
     * @param {?} value
     * @return {?}
     */
    updateModel(value) {
        this.model['values'] = this.items.filter((item) => {
            return this.selectedItems ? this.selectedItems.indexOf(item.label) > -1 : false;
        });
    }
}
Chip.CONTROL_TYPE = "chips";
function Chip_tsickle_Closure_declarations() {
    /** @type {?} */
    Chip.CONTROL_TYPE;
    /** @type {?} */
    Chip.prototype.controlType;
    /** @type {?} */
    Chip.prototype.items;
    /** @type {?} */
    Chip.prototype.stacked;
    /** @type {?} */
    Chip.prototype.disabled;
    /** @type {?} */
    Chip.prototype.chipAddition;
    /** @type {?} */
    Chip.prototype.chipRemoval;
    /** @type {?} */
    Chip.prototype.filteredItems;
    /** @type {?} */
    Chip.prototype.selectedItems;
}
//# sourceMappingURL=Chip.js.map