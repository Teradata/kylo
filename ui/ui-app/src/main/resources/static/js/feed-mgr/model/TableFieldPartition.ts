import * as angular from "angular";
import * as _ from "underscore";
import {DomainType} from "../services/DomainTypesService";
import {Common} from "../../common/CommonTypes";
import {TableColumnDefinition} from "./TableColumnDefinition";

export class TableFieldPartition {
    /**
     * the 1 based index of the partition entry
     */
    position: number;

    /**
     * the partition field name
     * @type {string}
     */
    field: string = '';

    /**
     * the name of the source field used for this partition
     * @type {string}
     */
    sourceField: string = '';


    /**
     * the datatype for this source field
     * @type {string}
     */
    sourceDataType: string = '';

    /**
     * the partition formula (i.e. val, year, month,...)
     */
    formula: string;

    /**
     * unique identifer for this  field;
     * @type {string}
     * @private
     */
    _id: string = '';

    columnDef: TableColumnDefinition;

    constructor(index: number) {
        this.position = index;
        this._id = _.uniqueId();
    }

    /**
     * Sync the sourceField and sourceDataType with this assigned column
     */
    syncSource() {
        if (angular.isDefined(this.columnDef)) {
            this.sourceDataType = this.columnDef.derivedDataType;
            this.sourceField = this.columnDef.name;
        }
    }

    updateFieldName() {
        if(angular.isUndefined(this.formula)){
            this.formula = 'val';
        }
        if (this.formula != 'val') {
            if (this.sourceField != null && (this.field == null || this.field == '' || this.field == this.sourceField || this.field == this.sourceField + "_")) {
                this.field = this.sourceField + "_" + this.formula;
            }
        }
        else {
            this.field = this.columnDef ? this.columnDef.name : this.sourceField;
        }
    }

    replaceSpaces() {
        this.field = StringUtils.replaceSpaces(this.field,'_');
    }
    isDate(){
        return this.columnDef.derivedDataType === "date" || this.columnDef.derivedDataType === "timestamp"
    }

    updateFormula() {
        if (!this.isDate()) {
            _.forEach(["to_date", "year", "month", "day", "hour", "minute"],  (formula) =>{
                if (this.formula === formula) {
                    this.formula = "val"
                }
            });
        }
    }


}