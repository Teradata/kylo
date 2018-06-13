import * as angular from "angular";
import * as _ from "underscore";
import {DomainType, DomainTypesService} from "../services/DomainTypesService";
import {Common} from "../../common/CommonTypes";
import {SchemaField} from "./schema-field"


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


export class TableColumnDefinition extends SchemaField {

    private static NAME_PATTERN: RegExp = /^[a-zA-Z0-9_\s\)\(-]*$/;
    private static PRECISION_SCALE_PATTERN: RegExp = /^\d+,\d+$/;
    private static MAX_COLUMN_LENGTH: number = 767;

    classType:string = 'TableColumnDefinition'

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
    history: ColumnDefinitionHistoryRecord[];

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

    constructor() {
        super();
        this.name = '';
        this._id = _.uniqueId();
        this.validationErrors = new ColumnDefinitionValidationError();
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


    initFeedColumn() {
        if (this.origName == undefined) {
            this.origName = this.name;
            this.origDataType = this.derivedDataType;
            this.deleted = false;
            this.history = [];
            this.dataTypeDisplay = this.getDataTypeDisplay();
            this.addHistoryItem();
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
        if (angular.isUndefined(this.validationErrors)) {
            this.validationErrors = new ColumnDefinitionValidationError();
        }
    }

    /**
     * Updates the validation state for this column
     */
    updateValidationErrors() {
        this.initializeValidationErrors();

        if (!this.isDeleted()) {
            this.validationErrors.name.reserved = this.name === "processing_dttm";
            this.validationErrors.name.required = _.isUndefined(this.name) || this.name.trim() === "";
            this.validationErrors.name.length = !_.isUndefined(this.name) && this.name.length > TableColumnDefinition.MAX_COLUMN_LENGTH;
            this.validationErrors.name.pattern = !_.isUndefined(this.name) && !TableColumnDefinition.NAME_PATTERN.test(this.name);
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

}