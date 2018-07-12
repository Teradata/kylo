import {Component, Injector, OnDestroy, OnInit, Pipe, PipeTransform, ViewContainerRef} from '@angular/core';
import * as angular from 'angular';
import * as _ from "underscore";
import {Common} from "../../../../../common/CommonTypes";
import {DomainType, DomainTypesService} from "../../../../services/DomainTypesService";
import {TableColumnDefinition} from "../../../../model/TableColumnDefinition";
import {TableFieldPartition} from "../../../../model/TableFieldPartition";
import {TableFieldPolicy} from "../../../../model/TableFieldPolicy";
import {HttpClient} from "@angular/common/http";
import {DefineFeedService} from "../../services/define-feed.service";
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {TableCreateMethod} from "../../../../model/feed-table";
import {FeedTableColumnDefinitionValidation} from "../../../../model/feed-table-column-definition-validation";
import {FormControl, FormGroup, ValidatorFn, Validators} from "@angular/forms";
import {AbstractControl} from "@angular/forms/src/model";
import { interval } from 'rxjs/observable/interval';
import { distinctUntilChanged, map, merge } from 'rxjs/operators';
import {TdDialogService} from "@covalent/core/dialogs";
import {StateRegistry, StateService} from "@uirouter/angular";
import {FeedService} from "../../../../services/FeedService";
import {TableSchema} from "../../../../model/table-schema";
import {SchemaField} from "../../../../model/schema-field";
import {ValidationErrors} from "@angular/forms/src/directives/validators";
import {SaveFeedResponse} from "../../model/SaveFeedResponse";
const moduleName = require('feed-mgr/feeds/define-feed/module-name');


class TablePermissions {
    tableLocked: boolean;
    dataTypeLocked: boolean;
    canRemoveFields: boolean;
constructor() {

}
}

class TableFormControls {

    public constructor(public defineTableForm:FormGroup,public definePartitionForm:FormGroup,private feedTableColumnDefinitionValidation: FeedTableColumnDefinitionValidation, private tablePermissions:TablePermissions ){

    }

    public static precisionScale(control: AbstractControl): ValidationErrors {
        let pattern = new RegExp("[0-9]+(,[0-9]+)");
        return control.value ? (pattern.test(control.value) ? null : {'precisionScale': true}) : null;
    }


    feedNameValidator(form: FeedTableColumnDefinitionValidation, columnDef:TableColumnDefinition): ValidatorFn {
        return (control: AbstractControl): {[key: string]: any} | null => {
            form.validateFeedName(columnDef);
            if(columnDef.validationErrors.name.notUnique){
                return {"notUnique":true};
            }
            else if(columnDef.validationErrors.name.reserved){
                return {"reserved":true};
            }
            else if(columnDef.validationErrors.name.length){
                return {"length":true};
            }
            else {
                return null;
            }
        };
    }



    private buildTableFieldFormControl(field: TableColumnDefinition ) :Common.Map<FormControl> {
        let controls :Common.Map<FormControl> = {}
        controls["name_"+field._id] = new FormControl(field.name,[Validators.required, this.feedNameValidator(this.feedTableColumnDefinitionValidation,field)]);
        controls["dataType_"+field._id] = new FormControl(field.derivedDataType,[Validators.required]);
        controls["precisionScale_" + field._id] = new FormControl({value:field.precisionScale,disabled:this.tablePermissions.dataTypeLocked},[TableFormControls.precisionScale]);

        controls["primaryKey_" + field._id] = new FormControl({value:field.primaryKey,disabled:field.isComplex() || field.deleted},[]);
        controls["created_" + field._id] = new FormControl({value:field.createdTracker,disabled:!(field.derivedDataType =='date' || field.derivedDataType =='timestamp') || field.deleted},[]);
        controls["updated_" + field._id] = new FormControl({value:field.updatedTracker,disabled:!(field.derivedDataType =='date' || field.derivedDataType =='timestamp') || field.deleted},[]);
        return controls;
    }

    getTableFieldFormControl(prefix:string,field:TableColumnDefinition){
        return this.getFormControl(this.defineTableForm,prefix,field);
    }



    addTableFieldFormControl(columnDef:TableColumnDefinition){
        let formControls :{ [key: string]: AbstractControl; } = this.buildTableFieldFormControl(columnDef);
        let keys :string[] = Object.keys(formControls)
        keys.forEach(key => {
            this.defineTableForm.registerControl(key,formControls[key]);
        })
    }


    private buildPartitionFieldFormControl(partition: TableFieldPartition ) :Common.Map<FormControl> {
        let controls :Common.Map<FormControl> = {}
        controls["partitionColumnRef_"+partition._id] = new FormControl('',[Validators.required]);
        controls["partitionFormula_"+partition._id] = new FormControl(partition.formula,[Validators.required]);
        controls["partitionName_"+partition._id] = new FormControl({value:partition.field,disabled:(partition.formula == 'val')},[Validators.required]);
        return controls;
    }

    getFormControl(form:FormGroup,prefix:string,field:TableColumnDefinition | TableFieldPartition){
        return form.get(prefix+"_"+field._id);
    }

    hasTableFormError(prefix:string,field:TableColumnDefinition, validationKey:string){
        let formControl = this.getFormControl(this.defineTableForm,prefix,field);
        return formControl ? formControl.hasError(validationKey) : false;
    }

    hasPartitionFormError(prefix:string,field:TableFieldPartition, validationKey:string){
        let formControl = this.getFormControl(this.definePartitionForm,prefix,field);
        return formControl ? formControl.hasError(validationKey) : false;
    }

    addPartitionFieldFormControl(partition:TableFieldPartition){
        let formControls :{ [key: string]: AbstractControl; } = this.buildPartitionFieldFormControl(partition);
        let keys :string[] = Object.keys(formControls)
        keys.forEach(key => {
            this.definePartitionForm.registerControl(key,formControls[key]);
        })
    }

    removePartitionFieldFormControls(partition:TableFieldPartition){
        this.definePartitionForm.removeControl("partitionColumnRef_"+partition._id);
        this.definePartitionForm.removeControl("partitionFormula_"+partition._id);
        this.definePartitionForm.removeControl("partitionName_"+partition._id);

    }

}


@Component({
    selector: "define-feed-step-table",
    styleUrls: ["js/feed-mgr/feeds/define-feed-ng2/steps/define-table/define-feed-table.component.css"],
    templateUrl: "js/feed-mgr/feeds/define-feed-ng2/steps/define-table/define-feed-table.component.html"
})
export class DefineFeedTableComponent extends AbstractFeedStepComponent implements OnInit,OnDestroy{

    /**
     * flag to check if the form is valid or not
     */
    isValid: boolean = false;

    /**
     * The html File object for the sample
     */
    sampleFile: any = null;

    tableCreateMethods: TableCreateMethod[] = [{type: 'MANUAL', name: 'Manual'}, {type: 'SAMPLE_FILE', name: 'Sample File'}];

    availableDefinitionDataTypes: string[] = [];

    /**
     * The parser selected for the sample file
     */
    schemaParser: any = null;

    /**
     * replace the <space> with underscore in field names
     * @type {boolean}
     */
    useUnderscoreInsteadOfSpaces: boolean = true;

    /**
     * the selected field
     */
    selectedColumn: TableColumnDefinition = null;

    fieldNamesUniqueRetryAmount: number = 0;

    /**
     * Should we show the method panel ?
     * this will be false for Data Transform feeds
     * @type {boolean}
     */
    showMethodPanel: boolean = true;
    /**
     * is the upload button disabled
     * @type {boolean}
     */
    uploadBtnDisabled: boolean = false;

    /**
     * Array of partition formulas
     */
    partitionFormulas: string[] = [];

    /**
     * The feed format
     */
    feedFormat: string;

    /**
     * Metadata for the selected column tag.
     * @type {{searchText: null, selectedItem: null}}
     */
    tagChips: any = {searchText: null, selectedItem: null};

    /**
     * List of available domain types.
     * @type {DomainType[]}
     */
    availableDomainTypes: DomainType[] = [];

    /**
     * the 0 based string index
     */
    stepIndex: string;

    tablePermissions:TablePermissions =new TablePermissions();


    feedTableColumnDefinitionValidation: FeedTableColumnDefinitionValidation;

    defineTableForm : FormGroup;

    definePartitionForm : FormGroup;

    private feedService: FeedService;
    private domainTypesService: DomainTypesService;

    private tableFormControls:TableFormControls;


    getStepName() {
        return "Define Table";
    }


    /**
     * Validation function which does not allow only empty space
     * @param {AbstractControl} control
     * @returns {ValidationErrors}
     */






    constructor(private http:HttpClient,stateService:StateService, defineFeedService:DefineFeedService,private $$angularInjector: Injector,
                private _dialogService: TdDialogService,
                private _viewContainerRef: ViewContainerRef) {
        super(defineFeedService,stateService)
        this.domainTypesService = $$angularInjector.get("DomainTypesService");
        this.feedService = $$angularInjector.get("FeedService");


    }


    init() {
        this.feedTableColumnDefinitionValidation = new FeedTableColumnDefinitionValidation(this.feed);
        this.defineTableForm = new FormGroup({});
        this.definePartitionForm = new FormGroup({});
        this.tableFormControls = new TableFormControls(this.defineTableForm,this.definePartitionForm, this.feedTableColumnDefinitionValidation,this.tablePermissions)

        if(this.feed.table.tableSchema.fields.length >= 0) {
          this.feed.table.tableSchema.fields.forEach((columnDef: TableColumnDefinition) => this.tableFormControls.addTableFieldFormControl(columnDef));
        }

        //Add in any complex data types to the available types
        this.addComplexDataTypes()

        //fetch the domain types
        this.domainTypesService.findAll().then((domainTypes: DomainType[]) => {
            this.availableDomainTypes = domainTypes;
        });

        this.ensurePartitionData();

            this.calcTableState();
            if (this.feed.table.tableSchema.fields && this.feed.table.tableSchema.fields.length > 0) {
                if (this.feed.dataTransformationFeed) {
                    this.addComplexDataTypes();
                }
                this.syncFeedsColumns();
            }else {
                if (!this.feed.dataTransformationFeed) {
               //     this.expansionPanelHelper.expandChooseMethodPanel();
                }
            }


        // Retrieve partition formulas
        this.feedService.getPartitionFunctions()
            .then((functions: any) => {
                this.partitionFormulas = functions;
            });

        this.subscribeToFormChanges(this.defineTableForm);




    }

    hasTableFormError(prefix:string,field:TableColumnDefinition, validationKey:string){
        return this.tableFormControls.hasTableFormError(prefix,field,validationKey)
    }

    hasPartitionFormError(prefix:string,field:TableFieldPartition, validationKey:string){
        return this.tableFormControls.hasPartitionFormError(prefix,field,validationKey);
    }



    /**
     * Adding a new Column to the schema
     * This is called both when the user clicks the "Add Field" button or when the sample file is uploaded
     * If adding from the UI the {@code columnDef} will be null, otherwise it will be the parsed ColumnDef from the sample file
     * @param columnDef
     */
    addColumn(columnDef?: TableColumnDefinition, syncFieldPolicies?: boolean) {
        // console.log("addColumn");
        if (columnDef == null) {
            columnDef = this.feedService.newTableFieldDefinition();
        }
        //redefine index =

        // when adding a new column this is also called to synchronize the field policies array with the columns
        let policy = this.feedService.newTableFieldPolicy(columnDef.name);


        if (columnDef.sampleValues != null && columnDef.sampleValues.length > 0) {
            columnDef.selectedSampleValue = columnDef.sampleValues[0];
        } else {
            columnDef.selectedSampleValue = null;
        }

        if (this.useUnderscoreInsteadOfSpaces) {
            columnDef.name = StringUtils.replaceSpaces(columnDef.name);
        }
        columnDef.initFeedColumn();
        //add the column to both the source and destination tables as well as the fieldPolicies array
        this.feed.table.tableSchema.fields.push(columnDef);
        this.feed.table.fieldPolicies.push(policy);
        this.feed.table.sourceTableSchema.fields.push(this.feedService.newTableFieldDefinition());

        this.tableFormControls.addTableFieldFormControl(columnDef)


        this.feedTableColumnDefinitionValidation.validateColumn(columnDef);
        if (syncFieldPolicies == undefined || syncFieldPolicies == true) {
            this.feedService.syncTableFieldPolicyNames();
        }
    };

    undoColumn(index: number) {
        var columnDef = <TableColumnDefinition> this.feed.table.tableSchema.fields[index];
        columnDef.history.pop();
        let prevValue = columnDef.history[columnDef.history.length - 1];
        columnDef.undo(prevValue);
        this.feedTableColumnDefinitionValidation.validateColumn(columnDef);
        this.feedTableColumnDefinitionValidation.partitionNamesUnique();
        this.feedService.syncTableFieldPolicyNames();
        this.isValid = this.feedTableColumnDefinitionValidation.validate();
    };

    /**
     * Remove a column from the schema
     * @param index
     */
    removeColumn(index: number) {
        var columnDef = <TableColumnDefinition> this.feed.table.tableSchema.fields[index];
        columnDef.deleteColumn();

        //remove any partitions using this field
        this.feed.table.partitions
            .filter((partition: any) => {
                return partition.columnDef.name === columnDef.name;
            })
            .map((partition: any) => {
                return partition._id;
            })
            .forEach((id: any) => {
                var index = this.feed.table.partitions.findIndex((partition: any) => {
                    return partition._id === id;
                });
                if (index > -1) {
                    this.removePartitionField(index);
                }
            });

        //ensure the field names on the columns are unique again as removing a column might fix a "notUnique" error
        this.feedTableColumnDefinitionValidation.validateColumn(columnDef);
        this.feedTableColumnDefinitionValidation.partitionNamesUnique();
        this.isValid = this.feedTableColumnDefinitionValidation.validate();
    };

    /**
     * Removes the column matching the passed in {@code columnDef} with the array of columns
     * @param columnDef
     */
    removeColumnUsingReference(columnDef: TableColumnDefinition) {
        var idx = _.indexOf(this.feed.table.tableSchema.fields, columnDef)
        if (idx >= 0) {
            this.removeColumn(idx);
        }
    };

    /**
     * Add a partition to the schema
     * This is called from the UI when the user clicks "Add Partition"
     */
    addPartitionField() {
        var partitionLength = this.feed.table.partitions.length;
        var partition = new TableFieldPartition(partitionLength);
        this.tableFormControls.addPartitionFieldFormControl(partition)
        this.feed.table.partitions.push(partition);
    };

    /**
     * Remove the partition from the schecma
     * @param index
     */
    removePartitionField(index: number) {
      let partitions =  this.feed.table.partitions.splice(index, 1);
        this.feedTableColumnDefinitionValidation.partitionNamesUnique();
        this.tableFormControls.removePartitionFieldFormControls(partitions[0]);
    };


    /**
     * Not Used now
     * @deprecated
     * @param {TableColumnDefinition} selectedColumn
     */
    onSelectedColumn(selectedColumn: TableColumnDefinition) {
        var firstSelection = this.selectedColumn == null;
        this.selectedColumn = selectedColumn;
        // Show an item in dropdown
        if (this.selectedColumn.selectedSampleValue == null && this.selectedColumn.sampleValues.length > 0) {
            this.selectedColumn.selectedSampleValue = this.selectedColumn.sampleValues[0];
        }
        if (firstSelection) {
            //trigger scroll to stick the selection to the screen
          //  this.utils.waitForDomElementReady('#selectedColumnPanel', () => {
           //     angular.element('#selectedColumnPanel').triggerHandler('stickIt');
           // })
        }

        // Ensure tags is an array
        if (angular.isUndefined(selectedColumn.tags) ) {
            selectedColumn.tags = [];
        }
    };

    onPrecisionChange(columnDef: TableColumnDefinition) {
        this.feedTableColumnDefinitionValidation.validateColumn(columnDef);
        this.onFieldChange(columnDef);
    };

    onDataTypeChange(columnDef :TableColumnDefinition){
        if(columnDef.derivedDataType == "decimal"){
            this.defineTableForm.get("precisionScale_" + columnDef._id).enable()
        }
        else {
            this.defineTableForm.get("precisionScale_" + columnDef._id).disable();
        }
    }

    /**
     * When the schema field changes it needs to
     *  - ensure the names are unique
     *  - update the respective partition names if there is a partition on the field with the 'val' formula
     *  - ensure that partition names are unique since the new field name could clash with an existing partition
     * @param columnDef
     */
    onNameFieldChange(columnDef: TableColumnDefinition, index: number) {
        columnDef.replaceNameSpaces();
        this.onFieldChange(columnDef);

        //update the partitions with "val" on this column so the name matches
        _.each(this.feed.table.partitions, (partition: TableFieldPartition) => {
            if (partition.columnDef == columnDef) {
                partition.syncSource();
                partition.updateFieldName();
            }
        });
        this.feedTableColumnDefinitionValidation.validateColumn(columnDef);
        this.feedTableColumnDefinitionValidation.partitionNamesUnique();
        this.feedService.syncTableFieldPolicyNames();

        // Check if column data type matches domain data type
        var policy = <TableFieldPolicy>this.feed.table.fieldPolicies[index];
        var domainType = policy.$currentDomainType;

        if (policy.domainTypeId && domainType.field && columnDef.$allowDomainTypeConflict !== true) {
            var nameChanged = (domainType.field.name && columnDef.name !== domainType.field.name);
            var dataTypeChanged = (domainType.field.derivedDataType && columnDef.derivedDataType !== domainType.field.derivedDataType);
            if (nameChanged || dataTypeChanged) {
                /*
                this.$mdDialog.show({
                    controller: "DomainTypeConflictDialog",
                    escapeToClose: false,
                    fullscreen: true,
                    parent: angular.element(document.body),
                    templateUrl: "js/feed-mgr/shared/apply-domain-type/domain-type-conflict.component.html",
                    locals: {
                        data: {
                            columnDef: columnDef,
                            domainType: domainType
                        }
                    }
                })
                    .then((keep: any) => {
                        if (keep) {
                            columnDef.$allowDomainTypeConflict = true;
                        } else {
                            delete policy.$currentDomainType;
                            delete policy.domainTypeId;
                        }
                    }, () => {
                        this.undoColumn(index);
                    });
            }
            */
            }
        }
    }



    /**
     * When a partition Source field changes it needs to
     *  - auto select the formula if there is only 1 in the drop down (i.e. fields other than dates/timestamps will only have the 'val' formula
     *  - ensure the partition data mapping to this source field is correct
     *  - attempt to prefill in the name with some default name.  if its a val formula it will default the partition name to the source field name and leave it disabled
     * @param partition
     */
    onPartitionSourceFieldChange(partition: TableFieldPartition) {
        //set the partition data to match the selected sourceField
        partition.syncSource();

        //if there is only 1 option in the formula list then auto select it
        //TODO fix this
        var formulas:string[] = []//this.$filter('filterPartitionFormula')(this.partitionFormulas, partition);
        if (formulas.length == 1) {
            partition.formula = formulas[0];
        }
        partition.updateFieldName();

        this.feedTableColumnDefinitionValidation.partitionNamesUnique();

    }

    /**
     * When a partition formula changes it needs to
     *  - attempt to prefill in the name with some default name.  if its a val formula it will default the partition name to the source field name and leave it disabled
     * @param partition
     */
    onPartitionFormulaChange(partition: TableFieldPartition) {
        partition.updateFieldName();
        this.feedTableColumnDefinitionValidation.partitionNamesUnique();
    }

    /**
     * when the partition name changes it needs to
     *  - ensure the names are unique
     *  - ensure no dups (cannot have more than 1 partitoin on the same col/formula
     * @param partition
     */
    onPartitionNameChange(partition: any) {
        partition.replaceSpaces();
        this.feedTableColumnDefinitionValidation.partitionNamesUnique();
    };


    private onFieldChange(columnDef: TableColumnDefinition) {
        this.selectedColumn = columnDef;
        columnDef.changeColumn();
        this.feedTableColumnDefinitionValidation.validateColumn(columnDef);
    }


    /**
     * Ensure that for the partitions the sourceField and sourceDataTypes match the respective schema field data
     */
    private ensurePartitionData() {
        _.each(this.feed.table.partitions, (partition: TableFieldPartition) => {
            if (partition.columnDef == undefined) {
                var columnDef = this.feedService.getColumnDefinitionByName(partition.sourceField);
                if (columnDef != null) {
                    partition.columnDef = columnDef;
                }
            }
            partition.syncSource();

            this.tableFormControls.addPartitionFieldFormControl(partition);
        });


    }


    private addComplexDataTypes() {
        this.availableDefinitionDataTypes = this.feedService.columnDefinitionDataTypes.slice();
        angular.forEach(this.feed.table.tableSchema.fields, (field) => {
            // add exotic data type to available columns if needed
            if ($.inArray(field.derivedDataType, this.availableDefinitionDataTypes) == -1) {
                this.availableDefinitionDataTypes.push(field.derivedDataType);
            }
        });
    }


    /**
     * Detects and applies domain types to all columns.
     */
    private applyDomainTypes() {
        // Detect domain types
        var data: any = {domainTypes: [], fields: []};

        this.feed.table.tableSchema.fields.forEach((field: TableColumnDefinition, index: number) => {
            var domainType = this.domainTypesService.detectDomainType(field, this.availableDomainTypes);
            if (domainType !== null) {
                if (this.domainTypesService.matchesField(domainType, field)) {
                    // Domain type can be applied immediately
                    this.feedService.setDomainTypeForField(field, this.feed.table.fieldPolicies[index], domainType);
                    field.history = [];
                    field.addHistoryItem();
                } else {
                    // Domain type needs user confirmation
                    data.domainTypes.push(domainType);
                    data.fields.push(field);
                }
            }
        });

        // Get user confirmation for domain type changes to field data types
        /*
        if (data.fields.length > 0) {
            this._dialogService.openConfirm({
                controller: "ApplyTableDomainTypesDialog",
                escapeToClose: false,
                fullscreen: true,
                parent: angular.element(document.body),
                templateUrl: "js/feed-mgr/shared/apply-domain-type/apply-table-domain-types.component.html",
                locals: {
                    data: data
                }
            })
                .then((selected: any) => {
                    selected.forEach((selection: any) => {
                        var fieldIndex = data.fields.findIndex((element: any) => {
                            return element.name === selection.name;
                        });
                        var policyIndex = this.feed.table.tableSchema.fields.findIndex((element: any) => {
                            return element.name === selection.name;
                        });
                        this.feedService.setDomainTypeForField(data.fields[fieldIndex], this.feed.table.fieldPolicies[policyIndex], data.domainTypes[fieldIndex]);
                        data.fields[fieldIndex].history = [];
                        let columnDef = <TableColumnDefinition> data.fields[fieldIndex];
                        columnDef.addHistoryItem();
                    });
                }, () => {
                    // ignore cancel
                });
        }
        */
    }


    /**
     * Called when a user transitions from the Wrangler to this step
     */
    private onDataTransformSchemaLoaded() {
        this.syncFeedsColumns();
        this.isValid = this.feedTableColumnDefinitionValidation.validate();
        if (angular.isDefined(this.feed.schemaChanged) && this.feed.schemaChanged == true) {
            this.isValid = false;
            /*
            this.$mdDialog.show(
                this.$mdDialog.alert()
                    .parent(angular.element(document.body))
                    .clickOutsideToClose(true)
                    .title('Table Schema Changed')
                    .htmlContent('The table schema no longer matches the schema previously defined. <br/><br/> This is invalid.  If you wish to modify the underlying schema <br/> (i.e. change some column names and/or types) please clone<br/> the feed as a new feed instead.')
                    .ariaLabel('Table Schema Changed ')
                    .ok('Got it!')
            );
            */
        }
            this.addComplexDataTypes();
            this.calcTableState();
          //  this.expansionPanelHelper.expandSchemaPanel();

        this.isValid = this.feedTableColumnDefinitionValidation.validate();

    }

    /**
     * Set the table states for locks
     */
    private calcTableState() {
        this.tablePermissions.tableLocked = angular.isDefined(this.tablePermissions.tableLocked) && (this.tablePermissions.tableLocked == true );
        this.tablePermissions.dataTypeLocked = angular.isDefined(this.tablePermissions.dataTypeLocked) && (this.tablePermissions.dataTypeLocked == true );
        this.tablePermissions.canRemoveFields = angular.isUndefined(this.tablePermissions.canRemoveFields) || this.tablePermissions.canRemoveFields === true ;
        this.showMethodPanel = (this.feed.table.method != 'EXISTING_TABLE');
    }

    /*
    Create columns for tracking changes between original source and the target table schema
     */
    private syncFeedsColumns() {
        _.each(this.feed.table.tableSchema.fields, (columnDef: TableColumnDefinition) => {
            columnDef.initFeedColumn()
        });
    }


}
@Pipe({name: 'filterPartitionFormula'})
export class FilterPartitionFormulaPipe implements PipeTransform{
    private feedService: FeedService;
    constructor(private $$angularInjector: Injector){
        this.feedService = $$angularInjector.get("FeedService");
    }
    transform(formulas:string[], partition?:TableFieldPartition){
        // Find column definition
        var columnDef = (partition && partition.sourceField) ? this.feedService.getColumnDefinitionByName(partition.sourceField) : null;
        if (columnDef == null) {
            return formulas;
        }

        // Filter formulas based on column type
        if (columnDef.derivedDataType !== "date" && columnDef.derivedDataType !== "timestamp") {
            return _.without(formulas, "to_date", "year", "month", "day", "hour", "minute");
        } else {
            return formulas;
        }
    }
}
