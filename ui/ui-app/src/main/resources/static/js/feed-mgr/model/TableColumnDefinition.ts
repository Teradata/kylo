import * as _ from "underscore";
import {DomainType, DomainTypesService} from "../services/DomainTypesService";
import {SchemaField} from "./schema-field"
import {CloneUtil} from "../../common/utils/clone-util";
import {StringUtils} from "../../common/utils/StringUtils";
import {KyloObject} from "../../../lib/common/common.model";
import {TableFieldPolicy} from "./TableFieldPolicy";
import {Common} from '../../../lib/common/CommonTypes';


export class ColumnDefinitionHistoryRecord {
    name: string;
    updatedTracker: boolean;
    createdTracker: boolean;
    primaryKey: boolean;
    deleted: boolean;
    derivedDataType: string;
    precisionScale: string;
    origName:string

    constructor(columnDefinition: TableColumnDefinition) {
        this.name = columnDefinition.name;
        this.derivedDataType = columnDefinition.derivedDataType;
        this.precisionScale = columnDefinition.precisionScale;
        this.deleted = columnDefinition.deleted;
        this.primaryKey = columnDefinition.primaryKey;
        this.updatedTracker = columnDefinition.updatedTracker;
        this.createdTracker = columnDefinition.createdTracker;
    }
}

export class ColumnDefinitionValidationError {
    name: Common.Map<boolean> = {};
    precision: Common.Map<boolean> = {};

    constructor() {

    }
}

export interface ValidationNameMessages {
    required: string;
    pattern: string;
    notUnique: string;
    reserved: string;
    length: string;
}

export interface ValidationPrecisionMessages {
    pattern: string;
}

export interface ValidationMessages {
    name: ValidationNameMessages;
    precision: ValidationPrecisionMessages;
}


export class TableColumnDefinition extends SchemaField implements KyloObject{

    private static NAME_PATTERN: RegExp = /^[a-zA-Z0-9_\s\)\(-]*$/;
    private static PRECISION_SCALE_PATTERN: RegExp = /^\d+,\d+$/;
    private static MAX_COLUMN_LENGTH: number = 767;

    public static OBJECT_TYPE:string = 'TableColumnDefinition'

    public objectType:string = TableColumnDefinition.OBJECT_TYPE;

    /**
     * Vaidate message types
     *
     */
    private static validationMessages: ValidationMessages = {
        name: {
            required: "Name is required",
            pattern: "Name cannot contain special characters",
            notUnique: "Name must be unique",
            reserved: "Name reserved by Kylo",
            length: "Name cannot be longer than " + TableColumnDefinition.MAX_COLUMN_LENGTH + " characters"
        },
        precision: {
            pattern: "Invalid, e.g. 10,0"
        }
    };

    /**
     * the original name of the field.
     */
    origName: string;
    /**
     * the original datatype of the field
     */
    origDataType: string;


    /**
     * if the field has been deleted
     * @type {boolean}
     */
    deleted: boolean = false;

    dataTypeDisplay: string;
    /**
     * internal unique identifier
     */
    _id: string;
    /**
     * History of changes for the column
     */
    history: ColumnDefinitionHistoryRecord[] = [];

    /**
     * listing of errors for this column
     */
    validationErrors: ColumnDefinitionValidationError;

    /**
     * the selected sample value
     */
    selectedSampleValue:string = '';


    /**
     * do we allow domain type name conflicts
     */
    $allowDomainTypeConflict:boolean;

    fieldPolicy?:TableFieldPolicy;



    public constructor(init?:Partial<TableColumnDefinition>) {
        super();
        this.initialize();
        Object.assign(this, init);
        if(this.tags == null || this.tags == undefined){
            this.tags = []
        }
        if(this.sampleValues == null || this.sampleValues == undefined){
            this.sampleValues = []
        }

    }
    initialize() {
        this.name = '';
        this._id = _.uniqueId();
        this.validationErrors = new ColumnDefinitionValidationError();
        this.dataTypeDisplay = this.getDataTypeDisplay();
    }

    initSampleValue(){
        if (this.selectedSampleValue == null && this.sampleValues.length > 0) {
            this.selectedSampleValue = this.sampleValues[0];
        }
    }

    replaceNameSpaces(){
        this.name = StringUtils.replaceSpaces(this.name, '_');
    }

    isValid(): boolean {
        return this.name != '' && this.derivedDataType != '';
    }


    isDeleted() {
        return this.deleted == true;
    }

    /**
     * returns the data type along with the precision (if needed)
     * @return {string}
     */
    getDataTypeDisplay() {
        return this.precisionScale != null ? this.derivedDataType + "(" + this.precisionScale + ")" : this.derivedDataType;
    }


    isDate():boolean{
      return this.derivedDataType =='date' || this.derivedDataType =='timestamp';
    }

    initFeedColumn() {
        if (this.origName == undefined) {
            this.origName = this.name;
            this.origDataType = this.derivedDataType;
            this.deleted = false;
            this.history = [];
            this.dataTypeDisplay = this.getDataTypeDisplay();
        }
        if(this.history.length == 0){
            if(this.deleted){
                let undoDeleted = new ColumnDefinitionHistoryRecord(this);
                undoDeleted.deleted = false;
                this.history.push(undoDeleted);
            }
            this.addHistoryItem();
        }
        if(this.dataTypeDisplay == undefined) {
         this.dataTypeDisplay  = this.getDataTypeDisplay();
        }
    }

    deleteColumn() {
        this.deleted = true;
        this.addHistoryItem();
    }

    /**
     * Adds an item to this columns history array
     */
    addHistoryItem() {
        this.history.push(new ColumnDefinitionHistoryRecord(this));
    }

    changeColumn() {

        if (this.derivedDataType !== 'decimal') {
            this.precisionScale = null;
        }
        this.dataTypeDisplay = this.getDataTypeDisplay();
        this.addHistoryItem();
    }

    /**
     * Resets this columns data to the incoming history record data
     * @param {FeedServiceTypes.ColumnDefinitionHistoryRecord} historyRecord
     */
    undo(historyRecord: ColumnDefinitionHistoryRecord) {
        this.name = historyRecord.name;
        this.derivedDataType = historyRecord.derivedDataType;
        this.precisionScale = historyRecord.precisionScale;
        this.deleted = historyRecord.deleted;
        this.primaryKey = historyRecord.primaryKey;
        this.createdTracker = historyRecord.createdTracker;
        this.updatedTracker = historyRecord.updatedTracker;
    }


    applyDomainType(domainType:DomainType){
        this.fieldPolicy.$currentDomainType = domainType;
        this.fieldPolicy.domainTypeId = domainType.id;

        if (_.isObject(domainType.field)) {
            this.tags = CloneUtil.deepCopy(domainType.field.tags);
            if (_.isString(domainType.field.name) && domainType.field.name.length > 0) {
                this.name = domainType.field.name;
            }
            if (_.isString(domainType.field.derivedDataType) && domainType.field.derivedDataType.length > 0) {
                this.derivedDataType = domainType.field.derivedDataType;
                this.precisionScale = domainType.field.precisionScale;
                this.dataTypeDisplay = this.getDataTypeDisplay();
            }
        }

        if (_.isObject(domainType.fieldPolicy)) {
            this.fieldPolicy.standardization = CloneUtil.deepCopy(domainType.fieldPolicy.standardization);
            this.fieldPolicy.validation = CloneUtil.deepCopy(domainType.fieldPolicy.validation);
        }



        // Update field properties
        delete this.$allowDomainTypeConflict;
        this.dataTypeDisplay = this.getDataTypeDisplay();
        this.fieldPolicy.name = this.name;
    }



    /**
     * is the column definition invalid
     * @return {boolean}
     */
    isInvalid(): boolean {
        var errorCount = 0;
        //return the validation errors into an 2 item array of just the values
        let validationErrorValues = <Common.Map<boolean>[]> _.values(this.validationErrors);
        _.each(validationErrorValues, (errorMessageObj: Common.Map<boolean>) => {
            errorCount += this.fieldErrorCount(errorMessageObj);
        });
        return errorCount > 0;
    }



    initializeValidationErrors() {
        if (_.isUndefined(this.validationErrors)) {
            this.validationErrors = new ColumnDefinitionValidationError();
        }
    }

    validateName(){
        this.initializeValidationErrors();
        this.validationErrors.name.reserved = this.name === "processing_dttm";
        this.validationErrors.name.required = _.isUndefined(this.name) || this.name.trim() === "";
        this.validationErrors.name.length = !_.isUndefined(this.name) && this.name.length > TableColumnDefinition.MAX_COLUMN_LENGTH;
        this.validationErrors.name.pattern = !_.isUndefined(this.name) && !TableColumnDefinition.NAME_PATTERN.test(this.name);

    }
    /**
     * Updates the validation state for this column
     */
    updateValidationErrors() {
        this.initializeValidationErrors();

        this.validateName();
        if (!this.isDeleted()) {
        this.validationErrors.precision.pattern = this.derivedDataType === 'decimal' && (_.isUndefined(this.precisionScale) || !TableColumnDefinition.PRECISION_SCALE_PATTERN.test(this.precisionScale));
        }
    }

    validate(): string | undefined {
        if (this.validationErrors === undefined) {
            return;
        }

        if (this.validationErrors.name.required) {
            return TableColumnDefinition.validationMessages.name.required;
        }
        if (this.validationErrors.name.pattern) {
            return TableColumnDefinition.validationMessages.name.pattern;
        }
        if (this.validationErrors.name.notUnique) {
            return TableColumnDefinition.validationMessages.name.notUnique;
        }
        if (this.validationErrors.name.reserved) {
            return TableColumnDefinition.validationMessages.name.reserved;
        }
        if (this.validationErrors.name.length) {
            return TableColumnDefinition.validationMessages.name.length;
        }
    }

    /**
     * determine if the map has any invalid items in it
     * @param {Common.Map<boolean>} errorMessageObj
     * @return {number}
     */
    private fieldErrorCount(errorMessageObj: Common.Map<boolean>) {
        var errorTypes = <boolean[]>_.values(errorMessageObj);
        var errors = _.filter(errorTypes, (errorType:boolean) => {
            return errorType == true;
        });
        return errors === undefined ? 0 : errors.length;
    }

    isComplex(){
        return this.dataTypeDescriptor && !_.isUndefined(this.dataTypeDescriptor.complex) ? this.dataTypeDescriptor.complex : false;
    }

    copy() :TableColumnDefinition{
        let policy = this.fieldPolicy;
        this.fieldPolicy = null;
        let copy :TableColumnDefinition = CloneUtil.deepCopyWithoutCircularReferences(this);
        let policyCopy = policy.copy();
        copy.fieldPolicy= policyCopy;
        this.fieldPolicy = policy;
        return copy;
    }

    prepareForSave(){
        this.sampleValues = null;
        this.history = null;
        if(this.fieldPolicy) {
            this.fieldPolicy.field = null;
            this.fieldPolicy = null;
        }
    }

}
