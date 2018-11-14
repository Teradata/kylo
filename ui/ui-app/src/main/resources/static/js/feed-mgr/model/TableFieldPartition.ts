import * as angular from "angular";
import * as _ from "underscore";
import {TableColumnDefinition} from "./TableColumnDefinition";
import {KyloObject} from "../../../lib/common/common.model";
import {StringUtils} from "../../common/utils/StringUtils";

export class TableFieldPartition  implements KyloObject {

    public static OBJECT_TYPE:string = 'TableFieldPartition'

    public objectType:string = TableFieldPartition.OBJECT_TYPE;

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

    initialize() {
        this._id = _.uniqueId();
    }

    public constructor(init?:Partial<TableFieldPartition>) {
        Object.assign(this, init);

        if(this.columnDef && this.columnDef.objectType && this.columnDef.objectType == TableColumnDefinition.OBJECT_TYPE){
           //ok
        }
        else {
            this.columnDef = new TableColumnDefinition(this.columnDef);
        }

        this.initialize();
    }

    static atPosition(index:number) :TableFieldPartition {
        return new this({position:index});
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

    public allowPartitionNameChanges() : boolean {
        return (this.formula && this.formula != 'val');
    }

    updateFieldName() {
        if(angular.isUndefined(this.formula)) return;
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
