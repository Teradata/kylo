import {TableColumnDefinition} from "../TableColumnDefinition";
import {TableFieldPartition} from "../TableFieldPartition";
import {ArrayUtils} from "../utils";
import {Subject} from "rxjs/Subject";
import {Observable} from "rxjs/Observable";
import {Feed} from "./feed.model";
import * as angular from 'angular';
import * as _ from "underscore";

export interface TableCreateMethod {
    name: string;
    type: string;
}

export class TableForm {

    defineFeedTableForm: any = {};

    arrayUtils: ArrayUtils = new ArrayUtils();


    /**
     * Allow other components to listen for changes to the currentStep
     *
     */
    public beforeSave$: Observable<Feed>;

    /**
     * The datasets subject for listening
     */
    private beforeSaveSubject: Subject<Feed>;

    constructor(private model: any) {

        this.touchErrorFields.bind(this);
        if (_.isUndefined(this.defineFeedTableForm.invalidColumns)) {
            this.defineFeedTableForm.invalidColumns = [];
        }
    }

    validate(validForm?: boolean) {
        // console.log("validate valid ? " + validForm);
        if (_.isUndefined(this.defineFeedTableForm.invalidColumns)) {
            this.defineFeedTableForm.invalidColumns = [];
        }
        if (validForm == undefined) {
            validForm = this.defineFeedTableForm.$valid;
        }
        let valid = this.model.templateId != null && this.model.table.method != null && this.model.table.tableSchema.name != null && this.model.table.tableSchema.name != ''
            && this.model.table.feedDefinitionTableSchema.fields.length > 0;

        if (valid) {
            //ensure we have at least 1 field (not deleted) assigned to the model)
            var validFields = _.filter(this.model.table.feedDefinitionTableSchema.fields, (field: any) => {
                return field.deleted == undefined || field.deleted == false;
            });
            valid = validFields.length > 0;
        }

        return valid && validForm && this.defineFeedTableForm.invalidColumns.length === 0;
    }

    validateFeedName(columnDef:TableColumnDefinition){
        columnDef.validateName();
        this.validateUniqueFeedName(columnDef);

    }
    validateUniqueFeedName(columnDef:TableColumnDefinition){
        //update all columns at all times, because column removal may fix not unique name error on other columns
        var columnsByName = _.groupBy(this.model.table.feedDefinitionTableSchema.fields, (column: TableColumnDefinition) => {
            //we'll disregard "not unique" name for all empty names and all deleted columns, i.e. put them into single group
            if (column.isDeleted()) {
                return "";
            }
            return column.name ? column.name.trim() : "";
        });
        _.each(_.keys(columnsByName), (columnName) => {
            var group = columnsByName[columnName];
            _.each(group, (column) => {
                column.initializeValidationErrors();
                if (columnName !== "") {
                    column.validationErrors.name.notUnique = group.length > 1;
                } else {
                    //group with empty column name which also includes "deleted" columns
                    column.validationErrors.name.notUnique = false;
                }
                this.updateFormValidation(column);
            });
        });
    }

    validateColumn(columnDef: TableColumnDefinition) {

        columnDef.updateValidationErrors();
        this.validateUniqueFeedName(columnDef);


        //reset partition formula if data type has changed from date/timestamp to another type
        _.each(this.model.table.partitions, (partition: TableFieldPartition) => {
            if (partition.columnDef === columnDef) {
                partition.updateFormula();
            }
        });

    }

    /**
     * Ensure the Partition Names are unique
     * If Not add a angular error
     */
    partitionNamesUnique() {
        // console.log("partitionNamesUnique");

        // Validate the Partition names are unique respective to other partition names
        _.chain(this.model.table.partitions).groupBy((partition) => {
            return partition.field
        }).each((group, name) => {
            if (group.length > 1) {
                _.each(group, (partition) => {
                    //.invalid another partition matches the same name
                    this.defineFeedTableForm['partition_name' + partition._id].$setValidity('notUnique', false);
                });
            }
            else {
                _.each(group, (partition) => {
                    //valid this is a unique partition name
                    this.defineFeedTableForm['partition_name' + partition._id].$setValidity('notUnique', true);
                });
            }
        });

        //Validate the Partition names are unique respective to the other fields

        //an array of column names
        var columnNames = _.map(this.model.table.feedDefinitionTableSchema.fields, (columnDef: TableColumnDefinition) => {
            return columnDef.name;
        });
        var countPartitionNames = {};
        //add the angular errors
        _.each(this.model.table.partitions, (partition: any) => {
            if (partition.formula != undefined && partition.formula != 'val' && _.indexOf(columnNames, partition.field) >= 0) {
                this.defineFeedTableForm['partition_name' + partition._id].$setValidity('notUnique', false);
            }
        });

    }

    touchErrorFields() {
        var errors = this.defineFeedTableForm.$error;
        for (var key in errors) {
            if (errors.hasOwnProperty(key)) {
                var errorFields = errors[key];
                angular.forEach(errorFields, (errorField: any) => {
                    errorField.$setTouched();
                });
            }
        }
    }

    updateFormValidation(columnDef: TableColumnDefinition) {
        if (columnDef.isInvalid()) {
            this.arrayUtils.add(this.defineFeedTableForm.invalidColumns, columnDef);
        } else {
            this.arrayUtils.remove(this.defineFeedTableForm.invalidColumns, columnDef);
        }
    }

}