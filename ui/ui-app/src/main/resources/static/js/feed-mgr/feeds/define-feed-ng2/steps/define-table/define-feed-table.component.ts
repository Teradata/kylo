import {ChangeDetectorRef, Component, Injector, OnDestroy, OnInit, Pipe, PipeTransform, TemplateRef, ViewChild, ViewContainerRef} from '@angular/core';
import * as angular from 'angular';
import * as _ from "underscore";
import {DomainType, DomainTypesService} from "../../../../services/DomainTypesService";
import {TableColumnDefinition} from "../../../../model/TableColumnDefinition";
import {TableFieldPartition} from "../../../../model/TableFieldPartition";
import {TableFieldPolicy} from "../../../../model/TableFieldPolicy";
import {HttpClient} from "@angular/common/http";
import {DefineFeedService, FeedEditStateChangeEvent} from "../../services/define-feed.service";
import {AbstractFeedStepComponent} from "../AbstractFeedStepComponent";
import {FeedTableColumnDefinitionValidation} from "../../../../model/feed/feed-table-column-definition-validation";
import {FormControl, FormGroup, ValidatorFn, Validators} from "@angular/forms";
import {AbstractControl} from "@angular/forms/src/model";
import {TdDialogService} from "@covalent/core/dialogs";
import {StateService} from "@uirouter/angular";
import {FeedService} from "../../../../services/FeedService";
import {ValidationErrors} from "@angular/forms/src/directives/validators";
import {TdVirtualScrollContainerComponent} from "@covalent/core/virtual-scroll";
import {FeedFieldPolicyRulesDialogService} from "../../../../shared/feed-field-policy-rules/feed-field-policy-rules-dialog.service";
import {SelectedColumn} from "./feed-table-selected-column.model";
import {Feed, SKIP_SOURCE_CATALOG_KEY} from "../../../../model/feed/feed.model";
import {FeedConstants} from "../../../../services/FeedConstants";
import {Observable} from "rxjs/Observable";
import {
    DomainTypeConflictDialogComponent,
    DomainTypeConflictDialogData,
    DomainTypeConflictDialogResponse
} from "../../../../shared/domain-type/domain-type-conflict/domain-type-conflict-dialog.component";
import {ApplyDomainTypeDialogComponent, ApplyDomainTypeDialogData, ApplyDomainTypeDialogDataResponse} from "../../../../shared/domain-type/apply-domain-type/apply-domain-type-dialog.component";
import {CheckAll} from "../../../shared/checkAll";
import {MatDialog} from "@angular/material/dialog";
import {
    ApplyDomainTypesData,
    ApplyDomainTypesDialogComponent,
    ApplyDomainTypesResponse,
    ApplyDomainTypesResponseStatus,
    ApplyDomainTypesRow
} from "../../../../shared/domain-type/apply-domain-types/apply-domain-types-dialog.component";
import {FeedStepConstants} from "../../../../model/feed/feed-step-constants";
import {FeedLoadingService} from "../../services/feed-loading-service";
import {FeedSideNavService} from "../../services/feed-side-nav.service";
import {FeedServiceTypes} from "../../../../services/FeedServiceTypes";
import {PreviewDataSet} from "../../../../catalog/datasource/preview-schema/model/preview-data-set";
import {ShowCatalogCanceledEvent} from "./source-sample/define-feed-step-source-sample.component";
import {PreviewFileDataSet} from "../../../../catalog/datasource/preview-schema/model/preview-file-data-set";
import {DatasetPreviewStepperSavedEvent} from "../../../../catalog-dataset-preview/preview-stepper/dataset-preview-stepper.component";
import {DefineFeedSourceSampleService} from "./source-sample/define-feed-source-sample.service";
import {CatalogService} from "../../../../catalog/api/services/catalog.service";
import {Common} from '../../../../../../lib/common/CommonTypes';
import {SaveFeedResponse} from "../../model/save-feed-response.model";
import {SchemaField} from "../../../../model/schema-field";

const moduleName = require('../../../define-feed/module-name');


class TablePermissions {
    tableLocked: boolean;
    partitionsLocked:boolean;
    dataTypeLocked: boolean;
    canRemoveFields: boolean;
    canAddFields:boolean;
    constructor() {
    this.canRemoveFields = true;
    }
}



@Component({
    selector: "define-feed-step-table",
    styleUrls: ["./define-feed-table.component.css"],
    templateUrl: "./define-feed-table.component.html"
})
export class DefineFeedTableComponent extends AbstractFeedStepComponent implements OnInit,OnDestroy{


    @ViewChild("toolbarActionTemplate")
    private toolbarActionTemplate:TemplateRef<any>;
    /**
     * flag to check if the form is valid or not
     */
    isValid: boolean = false;

    /**
     * Data Types in the drop down
     * @type {any[]}
     */
    availableDefinitionDataTypes: string[] = [];

    /**
     * the selected field
     */
    selectedColumn: SelectedColumn = null;

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

    tablePermissions:TablePermissions =new TablePermissions();

    feedTableColumnDefinitionValidation: FeedTableColumnDefinitionValidation;

    parentForm:FormGroup;

    /**
     * The table form with the fields and virtual repeate
     */
    defineTableForm : FormGroup;

    /**
     * The possible Merge Strategies
     */
    mergeStrategies: FeedServiceTypes.MergeStrategy[];

    /**
     * The selected strategy defined for this feed.
     * This will be used when rendering the read only view
     */
    mergeStrategy:FeedServiceTypes.MergeStrategy;

    /**
     * The possible target options
     */
    targetFormatOptions: Common.LabelValue[];

    /**
     * The compression Options
     */
    compressionOptions: any[];

    /**
     * The partition form
     */
    definePartitionForm : FormGroup;

    private feedService: FeedService;
    private domainTypesService: DomainTypesService;

    private tableFormControls:TableFormControls;

    /**
     * the filter for the partition list
     */
    private filterPartitionFormulaPipe:FilterPartitionFormulaPipe;

    /**
     * Toggle Check All/None on Profile column
     * Default it to true
     * @type {CheckAll}
     */
    profileCheckAll: CheckAll;

    skippedSourceSample:boolean = false;

    /**
     *
     * @type {CheckAll}
     */
    indexCheckAll: CheckAll;

    mergeStrategiesForm : FormGroup;

    targetFormatOptionsForm : FormGroup;

    sourceSampleForm:FormGroup;

    showSourceSampleCatalog:boolean;

    showSourceSample:boolean = true;

    catalogBrowserOpen:boolean = false;

    schemaPanelExpanded:boolean = true;

    targetFields:any[];


    @ViewChild('virtualScroll')
   virtualScroll: TdVirtualScrollContainerComponent

    getStepName() {
        return FeedStepConstants.STEP_FEED_TARGET;
    }


    getToolbarTemplateRef(): TemplateRef<any> {
        return this.toolbarActionTemplate;
    }

    constructor(private http:HttpClient,stateService:StateService, defineFeedService:DefineFeedService,private $$angularInjector: Injector,
                dialogService: TdDialogService,
                private _viewContainerRef: ViewContainerRef,
                public dialog:MatDialog,
                private feedFieldPolicyRulesDialogService:FeedFieldPolicyRulesDialogService, feedLoadingService:FeedLoadingService, feedSideNavService:FeedSideNavService,
                private defineFeedSourceSampleService: DefineFeedSourceSampleService,
                private catalogService: CatalogService,
                private cd:ChangeDetectorRef) {
        super(defineFeedService,stateService, feedLoadingService,dialogService, feedSideNavService);
        this.domainTypesService = $$angularInjector.get("DomainTypesService");
        this.feedService = $$angularInjector.get("FeedService");
        this.mergeStrategies = angular.copy(this.feedService.mergeStrategies);
        this.targetFormatOptions = angular.copy(this.feedService.targetFormatOptions);
        this.compressionOptions = this.feedService.allCompressionOptions();
        this.filterPartitionFormulaPipe = new FilterPartitionFormulaPipe();
        this.profileCheckAll = new CheckAll('profile', true);
        this.indexCheckAll = new CheckAll( 'index', false);
        this.parentForm = new FormGroup({})

        this.defineTableForm = new FormGroup({});
        this.definePartitionForm = new FormGroup({});
        this.mergeStrategiesForm = new FormGroup({});
        this.targetFormatOptionsForm = new FormGroup({});

        this.sourceSampleForm = new FormGroup({})

        this.parentForm.addControl("defineTableForm",this.defineTableForm)
        this.parentForm.addControl("definePartitionForm",this.definePartitionForm)
        this.parentForm.addControl("mergeStrategiesForm",this.mergeStrategiesForm)
        this.parentForm.addControl("targetFormatOptionsForm",this.targetFormatOptionsForm)

        this.mergeStrategiesForm.registerControl("targetMergeStrategy", new FormControl());
        this.defineTableForm.registerControl("indexCheckAll",new FormControl(this.indexCheckAll.isChecked))
        this.defineTableForm.registerControl("profileCheckAll",new FormControl(this.profileCheckAll.isChecked))
    }


    init() {

        this.profileCheckAll.setup(this.feed.table);
        this.indexCheckAll.setup(this.feed.table);


        let locked = this.feed.hasBeenDeployed(); // || this.feed.isDataTransformation();
        this.tablePermissions.canRemoveFields = !locked
        this.tablePermissions.dataTypeLocked = locked
        this.tablePermissions.tableLocked = locked
        this.tablePermissions.canAddFields = !locked && !this.feed.isDataTransformation() && this.feed.table.method != "EXISTING_TABLE";
        this.tablePermissions.partitionsLocked = this.feed.hasBeenDeployed();


        if(this.feed.hasBeenDeployed()){
            this.targetFields = this.feed.table.tableSchema.fields;
        }
        else {
            this.targetFields = this.feed.table.feedDefinitionTableSchema.fields;
        }

        this.feedTableColumnDefinitionValidation = new FeedTableColumnDefinitionValidation(this.definePartitionForm, this.feed);

        this.tableFormControls = new TableFormControls(this.defineTableForm,this.definePartitionForm, this.feedTableColumnDefinitionValidation,this.tablePermissions)

        //fetch the domain types
        this.domainTypesService.findAll().then((domainTypes: DomainType[]) => {
            this.availableDomainTypes = _.sortBy(domainTypes, "title");
            //apply domain types when the schema has changed
            if(this.feed.table.schemaChanged) {
                this.applyDomainTypes();
                this.feed.table.schemaChanged = false;
            }
        });

        this.availableDefinitionDataTypes = FeedConstants.columnDefinitionDataTypes.slice();


        //ensure the table field datatypes exist
        this.ensureTableFields();
        //ensure the partition datatypes exist with proper form controls
        this.ensurePartitionData();



        // Retrieve partition formulas
        this.feedService.getPartitionFunctions()
            .then((functions: any) => {
                this.partitionFormulas = functions;
            });
        if(this.feed.table.targetMergeStrategy) {
            this.mergeStrategy = this.mergeStrategies.find((strategy: FeedServiceTypes.MergeStrategy) => strategy.type == this.feed.table.targetMergeStrategy)
        }

        this.targetFormatOptionsForm.registerControl("targetFormat", new FormControl({value:'',disabled:this.feed.readonly || this.feed.hasBeenDeployed()}));
        this.targetFormatOptionsForm.registerControl("compressionFormat", new FormControl({value:'',disabled:this.feed.readonly || this.feed.hasBeenDeployed()}));

        //listen when the form is valid or invalid
        this.subscribeToFormChanges(this.parentForm);
        this.subscribeToFormDirtyCheck(this.defineTableForm);
        this.subscribeToFormDirtyCheck(this.definePartitionForm);
        this.subscribeToFormDirtyCheck(this.mergeStrategiesForm);
        this.subscribeToFormDirtyCheck(this.targetFormatOptionsForm);

        if(this.feed.isDataTransformation() || (this.feed.hasBeenDeployed() && this.feed.sampleDataSet == undefined)){
            this.showSourceSample = false;
        }
        if (this.feed.sampleDataSet == undefined && !this.skippedSourceSample && this.feed.table.feedDefinitionTableSchema.fields.length ==0) {
            this.showSourceSampleCatalog = true;
        }
    }

    feedStateChange(event: FeedEditStateChangeEvent) {
        super.feedStateChange(event);
        if (this.feed.readonly || this.tablePermissions.tableLocked) {
            this.targetFormatOptionsForm.get("targetFormat").disable();
            this.targetFormatOptionsForm.get("compressionFormat").disable();
        } else {
            this.targetFormatOptionsForm.get("targetFormat").enable();
            this.targetFormatOptionsForm.get("compressionFormat").enable();
        }
    }

    protected feedEdit(feed:Feed){
        this.ensureTableFields();
    }

    /**
     * Helper method called from the html to see if the field control has an error
     * @param {string} prefix
     * @param {TableColumnDefinition} field
     * @param {string} validationKey
     * @return {boolean}
     */
    hasTableFormError(prefix:string,field:TableColumnDefinition, validationKey:string){
        return this.tableFormControls.hasTableFormError(prefix,field,validationKey)
    }

    /**
     * Helper method called from the html to see if the field control has an error
     * @param {string} prefix
     * @param {TableFieldPartition} field
     * @param {string} validationKey
     * @return {boolean}
     */
    hasPartitionFormError(prefix:string,field:TableFieldPartition, validationKey:string){
        return this.tableFormControls.hasPartitionFormError(prefix,field,validationKey);
    }


    /**
     * Adding a new Column to the schema
     * This is called both when the user clicks the "Add Field"
     * If adding from the UI the {@code columnDef} will be null, otherwise it will be the parsed ColumnDef from the sample file
     * @param columnDef
     */
    addColumn(columnDef?: TableColumnDefinition, syncFieldPolicies?: boolean) {
        let newColumn = this.feed.table.addColumn(columnDef, syncFieldPolicies);
        if(this.targetFields.find((col:any) => col._id != undefined && col._id == newColumn._id) == undefined) {
            this.targetFields.push(newColumn);
        }
        this.tableFormControls.addTableFieldFormControl(newColumn)
        this.feedTableColumnDefinitionValidation.validateColumn(newColumn);
        if(this.virtualScroll) {
            this.virtualScroll.refresh();
        }
        this.defineTableForm.markAsDirty();
        if(this.virtualScroll){
            setTimeout(()=>{this.virtualScroll.scrollToEnd()}, 50);
        }
    }

    /**
     * Remove a column from the schema
     * @param index
     */
    removeColumn(index: number) {
        let columnDef = this.feed.table.removeColumn(index)
        this.feed.table.getPartitionsOnColumn(columnDef.name).forEach(partition => {
            let index = this.feed.table.partitions.indexOf(partition);
            this.removePartitionField(index);
        });
        this.tableFormControls.updateFieldState(columnDef);

        //ensure the field names on the columns are unique again as removing a column might fix a "notUnique" error
        this.feedTableColumnDefinitionValidation.validateColumn(columnDef);
        this.feedTableColumnDefinitionValidation.partitionNamesUnique();
        this.isValid = this.feedTableColumnDefinitionValidation.validate();
    }

    undoColumn(index: number) {
        let columnDef =  this.feed.table.undoColumn(index);
        this.feedTableColumnDefinitionValidation.validateColumn(columnDef);
        this.feedTableColumnDefinitionValidation.partitionNamesUnique();
        this.feed.table.syncTableFieldPolicyNames();
        this.isValid = this.feedTableColumnDefinitionValidation.validate();
        this.tableFormControls.updateFieldState(columnDef);
    }

    /**
     * Add a partition to the schema
     * This is called from the UI when the user clicks "Add Partition"
     */
    addPartitionField() {
        var partitionLength = this.feed.table.partitions.length;
        var partition = TableFieldPartition.atPosition(partitionLength);
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

    onIndexCheckAllChange() : boolean {
        this.indexCheckAll.toggleAll();
        let checked = this.indexCheckAll.isChecked;
            //update the form values
            this.feed.table.feedDefinitionFieldPolicies.forEach(fieldPolicy => {
                let ctrl = this.tableFormControls.getFormControl(this.defineTableForm,"index",fieldPolicy.field);
                ctrl.setValue(checked);
                fieldPolicy.index = checked;
            });
        return false;
    }

    onProfileCheckAllChange() : boolean{
        this.profileCheckAll.toggleAll();
        let checked = this.profileCheckAll.isChecked;
        //update the form values
        this.feed.table.feedDefinitionFieldPolicies.forEach(fieldPolicy => {
            let ctrl = this.tableFormControls.getFormControl(this.defineTableForm,"profile",fieldPolicy.field);
            ctrl.setValue(checked);
            fieldPolicy.profile = checked;
        });
        return false;
    }

    onIndexChange(columnDef:TableColumnDefinition){
        this._selectColumn(columnDef);
        this.selectedColumn.fieldPolicy.index = !this.selectedColumn.fieldPolicy.index
        this.indexCheckAll.clicked(!this.selectedColumn.fieldPolicy.index);

    }
    onProfileChange(columnDef:TableColumnDefinition){
        this._selectColumn(columnDef);
        this.selectedColumn.fieldPolicy.profile = !this.selectedColumn.fieldPolicy.profile
        this.profileCheckAll.clicked(!this.selectedColumn.fieldPolicy.profile);
    }

    /**
     *
     * @param {TableColumnDefinition} selectedColumn
     */
    onSelectedColumn(columnDef: TableColumnDefinition) {
       this._selectColumn(columnDef);
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
        this.onFieldChange(columnDef);
        if(!columnDef.isDate()) {
            if(columnDef.createdTracker){
                columnDef.changeColumn()
                columnDef.createdTracker = false;
            }
            if(columnDef.updatedTracker){
                columnDef.changeColumn()
                columnDef.updatedTracker = false;
            }
        }

    }

    onSchemaPanelExpanded() {
        this.schemaPanelExpanded = true;
    }

    onSchemaPanelCollapsed() {
        this.schemaPanelExpanded = false;

        // Set merge strategy state if primary key is available
        let pkSet = _.any(this.feed.table.feedDefinitionTableSchema.fields, (value, index) => {
            return (value.primaryKey);
        });
        let pkMergeStrategy = _.find(this.mergeStrategies, (value,index) => {
            return (value.type == 'PK_MERGE');
        });
        if (pkMergeStrategy != undefined) {
            pkMergeStrategy.disabled = !pkSet;
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
        this.feed.table.syncFieldPolicy(columnDef,index);

        // Check if column data type matches domain data type
        var policy = <TableFieldPolicy>this.feed.table.feedDefinitionFieldPolicies[index];
        var domainType = policy.$currentDomainType;

        if (policy.domainTypeId && domainType.field && columnDef.$allowDomainTypeConflict !== true) {
            var nameChanged = (domainType.field.name && columnDef.name !== domainType.field.name);
            var dataTypeChanged = (domainType.field.derivedDataType && columnDef.derivedDataType !== domainType.field.derivedDataType);
            if (nameChanged || dataTypeChanged) {

                let domainTypeDialogData:DomainTypeConflictDialogData = {columnDef:columnDef,domainType:domainType, propertyName:'', propertyValue:''};

                const dialogRef = this.dialog.open(DomainTypeConflictDialogComponent, {
                    width: '600px',
                    data: domainTypeDialogData
                });

                dialogRef.afterClosed().subscribe((keep:DomainTypeConflictDialogResponse) => {
                    if (keep == DomainTypeConflictDialogResponse.Keep) {
                        columnDef.$allowDomainTypeConflict = true;
                    } else if(keep == DomainTypeConflictDialogResponse.Remove) {
                        delete policy.$currentDomainType;
                        delete policy.domainTypeId;
                    }
                    else if(keep == DomainTypeConflictDialogResponse.Cancel){
                        this.undoColumn(index);
                    }

                });


            }
        }

    }

    /**
     * Open the standardizers and validators
     * @param {SelectedColumn} selectedColumn
     */
    onFieldPoliciesClicked(selectedColumn:SelectedColumn){

        let fieldPolicy: TableFieldPolicy = this.feed.table.feedDefinitionFieldPolicies.find(policy => policy.fieldName == selectedColumn.field.name );
        if(fieldPolicy) {
            this.feedFieldPolicyRulesDialogService.openDialog(this.feed, fieldPolicy).subscribe((result:any) => {
                  this.selectedColumn.update()
            });
        }
    }



    /**
     * When a partition Source field changes it needs to
     *  - auto select the formula if there is only 1 in the drop down (i.e. fields other than dates/timestamps will only have the 'val' formula
     *  - ensure the partition data mapping to this source field is correct
     *  - attempt to prefill in the name with some default name.  if its a val formula it will default the partition name to the source field name and leave it disabled
     * @param partition
     */
    onPartitionSourceFieldChange(partition: TableFieldPartition, feed:Feed) {
        //set the partition data to match the selected sourceField
        partition.syncSource();

        //if there is only 1 option in the formula list then auto select it
        var formulas:string[] = this.filterPartitionFormulaPipe.transform(this.partitionFormulas, partition,feed);
        if (formulas.length == 1) {
            partition.formula = formulas[0];
        }
        partition.updateFieldName();
        this.updatePartitionNameState(partition);

        setTimeout(() => {this.feedTableColumnDefinitionValidation.partitionNamesUnique()}, 50);

    }

    filterFormula(partition: TableFieldPartition, feed: Feed){
        return this.filterPartitionFormulaPipe.transform(this.partitionFormulas, partition,feed);
    }

    /**
     * When a partition formula changes it needs to
     *  - attempt to prefill in the name with some default name.  if its a val formula it will default the partition name to the source field name and leave it disabled
     * @param partition
     */
    onPartitionFormulaChange(partition: TableFieldPartition) {
        partition.updateFieldName();
        this.feedTableColumnDefinitionValidation.partitionNamesUnique();
        this.updatePartitionNameState(partition);
    }

    updatePartitionNameState(partition: TableFieldPartition) : void {
        let nameField = this.definePartitionForm.get('partitionName_'+partition._id);
        if (partition.allowPartitionNameChanges()) {
            nameField.enable({onlySelf:true});
        } else {
            nameField.disable({onlySelf:true});
        }

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
       this._selectColumn(columnDef);
        columnDef.changeColumn();
        this.feedTableColumnDefinitionValidation.validateColumn(columnDef);
    }


    private _selectColumn(columnDef:TableColumnDefinition){
        let fieldPolicy = columnDef.fieldPolicy;
        if(fieldPolicy == undefined) {
            fieldPolicy = this.feed.table.feedDefinitionFieldPolicies.find(policy => policy.fieldName == columnDef.name);
        }
        this.selectedColumn = new SelectedColumn(columnDef, fieldPolicy);
        this.selectedColumn.setDomainType(this.availableDomainTypes);
    }

    /**
     * Ensure that for the partitions the sourceField and sourceDataTypes match the respective schema field data
     */
    private ensurePartitionData() {
        _.each(this.feed.table.partitions, (partition: TableFieldPartition) => {
            if (partition.columnDef == undefined) {
                let columnDef = this.feed.table.getColumnDefinitionByName(partition.sourceField)
                if (columnDef != null) {
                    partition.columnDef = columnDef;
                }
            }
            partition.syncSource();
            this.tableFormControls.addPartitionFieldFormControl(partition);
        });
    }

    /**
     * Detects and applies domain types to all columns.
     */
    private applyDomainTypes() {
        // Detect domain types
        var data: ApplyDomainTypesData = {domainTypes: [], fields: []};

        this.feed.table.feedDefinitionTableSchema.fields.forEach((field: TableColumnDefinition, index: number) => {
            var domainType = this.domainTypesService.detectDomainType(field, this.availableDomainTypes);
            if (domainType !== null) {
                if (this.domainTypesService.matchesField(domainType, field)) {
                    // Domain type can be applied immediately
                    field.applyDomainType(domainType);
                    field.history = [];
                    field.addHistoryItem();
                } else {
                    // Domain type needs user confirmation
                    data.domainTypes.push(domainType);
                    data.fields.push(field);
                }
            }
        });
        if(data.fields.length >0) {
            this.confirmDomainTypes(data);
        }
    }

    private confirmDomainTypes(applyDomainTypesData:ApplyDomainTypesData){

        const dialogRef = this.dialog.open(ApplyDomainTypesDialogComponent, {
            width: '600px',
            data: applyDomainTypesData
        });

        dialogRef.afterClosed().subscribe((response:ApplyDomainTypesResponse) => {
            if(response.status == ApplyDomainTypesResponseStatus.APPLY) {
                response.appliedRows.forEach((selection:ApplyDomainTypesRow) => {
                    var fieldIndex = applyDomainTypesData.fields.findIndex((element: any) => {
                        return element.name === selection.name;
                    });
                    let columnDef = <TableColumnDefinition> applyDomainTypesData.fields[fieldIndex];
                    let domainType = applyDomainTypesData.domainTypes[fieldIndex];
                    columnDef.applyDomainType(domainType);
                    columnDef.history = [];
                    columnDef.addHistoryItem();
                });
            }


        });




    }




    /**
     * Display a confirmation when the domain type of a field is changed and there are existing standardizers and validators.
     *
     * @param {SelectedColumn} the selected column
     */
    onDomainTypeChange(selectedColumn:SelectedColumn) {
        // Check if removing domain type
        if (selectedColumn.fieldPolicy.domainTypeId == undefined) {
            delete selectedColumn.fieldPolicy.$currentDomainType;
            return;
        }

        // Find domain type from id
        let domainType = _.find(this.availableDomainTypes, (domainType: DomainType) => {
            return (domainType.id === selectedColumn.fieldPolicy.domainTypeId);
        });

        if (domainType && selectedColumn.showDomainTypeDialog()) {


            let dialogData: ApplyDomainTypeDialogData = {column: selectedColumn, domainType: domainType};

            const dialogRef = this.dialog.open(ApplyDomainTypeDialogComponent, {
                width: '600px',
                data: dialogData
            });

            dialogRef.afterClosed().subscribe((response: ApplyDomainTypeDialogDataResponse) => {
                if (response == ApplyDomainTypeDialogDataResponse.Apply) {
                    selectedColumn.applyDomainType(domainType);
                }
                else {
                    //revert it
                    selectedColumn.fieldPolicy.domainTypeId = selectedColumn.fieldPolicy.$currentDomainType ? selectedColumn.fieldPolicy.$currentDomainType.id : null;
                }


            });
        } else if(domainType){
            //apply it
            selectedColumn.applyDomainType(domainType);
        }
    }

    /**
     * Called when a user transitions from the Wrangler to this step
     */
    private onDataTransformSchemaLoaded() {
        this.ensureTableFields();

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

    }

    private ensureTableFields(){
        if (this.feed.table.feedDefinitionTableSchema.fields && this.feed.table.feedDefinitionTableSchema.fields.length > 0) {
            //ensure data types
            let targetFormFields  = [];

       this.feed.table.feedDefinitionTableSchema.fields.forEach((columnDef: TableColumnDefinition) => {
                // add exotic data type to available columns if needed
                if ($.inArray(columnDef.derivedDataType, this.availableDefinitionDataTypes) == -1) {
                    this.availableDefinitionDataTypes.push(columnDef.derivedDataType);
                }
                columnDef.replaceNameSpaces();
                columnDef.initFeedColumn()
                //add the form control
                this.tableFormControls.addTableFieldFormControl(columnDef,false);

                if(this.feed.hasBeenDeployed()) {
                    if(!columnDef.deleted)
                    {
                        targetFormFields.push(columnDef);
                    }
                }
                else {
                    targetFormFields.push(columnDef);
                }
            });
            this.targetFields = targetFormFields;

            this.defineTableForm.get("indexCheckAll").setValue(this.indexCheckAll.isChecked)
            this.defineTableForm.get("profileCheckAll").setValue(this.profileCheckAll.isChecked)

        }
        this.calcTableState();
        this.isValid = this.feedTableColumnDefinitionValidation.validate();
    }



    /**
     * Set the table states for locks
     */
    private calcTableState() {
        this.tablePermissions.tableLocked = angular.isDefined(this.tablePermissions.tableLocked) && (this.tablePermissions.tableLocked == true );
        this.tablePermissions.dataTypeLocked = angular.isDefined(this.tablePermissions.dataTypeLocked) && (this.tablePermissions.dataTypeLocked == true );
        this.tablePermissions.canRemoveFields = angular.isUndefined(this.tablePermissions.canRemoveFields) || this.tablePermissions.canRemoveFields === true ;
    }

    /*
    Create columns for tracking changes between original source and the target table schema
    @deprecated
     */
    private syncFeedsColumns() {
        _.each(this.feed.table.feedDefinitionTableSchema.fields, (columnDef: TableColumnDefinition) => {
            columnDef.initFeedColumn()
        });
    }

    tableSchemaTrackByFn(index:number,field:TableColumnDefinition) {
        return field._id;
    }

    onShowCatalogChange($event:boolean){
        this.catalogBrowserOpen = $event;
    }



    onCatalogCanceled($event:ShowCatalogCanceledEvent){
        if($event.skip){
            //mark it in the metadata
            this.step.addProperty(SKIP_SOURCE_CATALOG_KEY,true);
            this.skippedSourceSample = true;
        }
    }

    onSchemaPanelEdit($event:any) {
        //$event.preventDefault();
        //this.schemaPanelExpanded = true;
    }

    onSchemaPanelCancel($event:any) {
        //$event.preventDefault();
        //this.schemaPanelExpanded = false;
    }

    onSchemaPanelSave($event:any) {
        $event.preventDefault();
        this.registerLoading();
        this.defineFeedService.saveFeed(this.feed, false,this.step).subscribe((response: SaveFeedResponse) => {
            this.defineFeedService.openSnackBar("Saved", 1500);
            this.resolveLoading();
            this.step.clearDirty();
            this.schemaPanelExpanded = false;
        }, error1 => {
            this.resolveLoading()
            this.defineFeedService.openSnackBar("Error saving feed ", 3000);
        })
    }

    onSampleSourceSaved(previewEvent: DatasetPreviewStepperSavedEvent) {

        let previews: PreviewDataSet[] = previewEvent.previews;
        if (previews && previews.length) {
            let feedDataSets = this.feed.sourceDataSets;
            //check to see if schema differs
            if (feedDataSets && feedDataSets.length > 0) {
                let feedDatasetKeys = feedDataSets.map(ds => ds.id).sort().toString();
                let newDatasetKeys = previews.map(ds => ds.key).sort().toString();
                if (feedDatasetKeys != "" && feedDatasetKeys != newDatasetKeys) {
                    //WARN different datasets
                    this.dialogService.openConfirm({
                        message: 'The dataset you have selected differs from the one existing on this feed. Switching the source will result in a new target schema.  Are you sure you want to do this?',
                        disableClose: true,
                        title: 'Confirm source dataset change',
                    }).afterClosed().subscribe((accept: boolean) => {
                        if (accept) {
                            this._setSourceAndTarget(previewEvent);
                        } else {
                            // no op
                        }
                    });
                }
                else {
                    this._setSourceAndTarget(previewEvent);
                }
            }
            else {
                this._setSourceAndTarget(previewEvent);
            }
        }
        else {
            this._setSourceAndTarget(previewEvent)
        }


    }

    private _setSourceAndTarget(event: DatasetPreviewStepperSavedEvent) {
        this.feedLoadingService.registerLoading();
        //detach the change detector so we can make the updates without angular
        this.cd.detach()

        let _updateFormControls = () => {
           //apply the updates to this form
            this.ensureTableFields();
            this.ensurePartitionData();
            this.feed.table.syncTableFieldPolicyNames()
            this.showSourceSampleCatalog = false;
            this.catalogBrowserOpen = false;
            if(this.virtualScroll) {
               this.virtualScroll.refresh();
            }
            this.feedLoadingService.resolveLoading();
            this.cd.reattach();
            this.cd.markForCheck();
            this.cd.detectChanges();

        }


        /**
         * Applies the Serde if necessary and then update the form controls
         */
        let applySerdeAndUpdateFormControls = () => {
            if(event.previews[0] instanceof PreviewFileDataSet) {

                this.defineFeedSourceSampleService.parseTableSettings((<PreviewFileDataSet>event.previews[0])).subscribe( (response:any)=> {

                    this.feed.table.feedFormat = response.hiveFormat;
                    this.feed.structuredData(response.structured);
                    this.feed.table.feedTblProperties = response.serdeTableProperties;
                    _updateFormControls();
                }, (error1:any) =>  this.cd.reattach());
            }
            else {
                _updateFormControls();
            }
        }
        // reset the feed fields
        this.selectedColumn = undefined;
        this.tableFormControls.resetFormFields();


        let previews = event.previews;
        let singleSelection = event.singleSelection;
        let firstPreview = previews && previews.length >0 ? previews[0] : undefined;

        if (firstPreview && singleSelection) {
                const sampleDataSet = firstPreview.toSparkDataSet();
                if (sampleDataSet.dataSource && sampleDataSet.dataSource.connector && sampleDataSet.dataSource.connector.pluginId) {
                    this.catalogService.getConnectorPlugin(sampleDataSet.dataSource.connector.pluginId)
                        .subscribe(plugin => {
                            this.feed.setSampleDataSetAndUpdateTarget(sampleDataSet, firstPreview,undefined, plugin)
                            applySerdeAndUpdateFormControls();
                        }, (error1:any) =>  this.cd.reattach());
                } else {
                    this.feed.setSampleDataSetAndUpdateTarget(sampleDataSet, firstPreview);
                    applySerdeAndUpdateFormControls();
                }
        }
        else {
            //set the source and target to empty
            this.feed.setSampleDataSetAndUpdateTarget(null, null);
            applySerdeAndUpdateFormControls();
        }

    }


    protected applyUpdatesToFeed(): Observable<any> | boolean | null {
       if(this.catalogBrowserOpen){
           return  this.dialogService.openConfirm(
               {title:"Pending source sample changes",
                   message:"There are pending changes in the source sample that have not been applied to the target.  Are you sure you want to save without applying these changes?  ",
                   acceptButton: "Abandon changes",
                   cancelButton: "Cancel and review"
               }).afterClosed();
       }
       return this.parentForm.valid;
    }
}
@Pipe({name: 'filterPartitionFormula'})
export class FilterPartitionFormulaPipe implements PipeTransform{
   constructor() {}

    transform(formulas:string[], partition?:TableFieldPartition, feed?:Feed){
       let formulaList = formulas;
       if(partition && partition.sourceField && feed) {
           let columnDef :TableColumnDefinition = feed.table.getColumnDefinitionByName(partition.sourceField)
           if(columnDef != null && columnDef != undefined){

            if (columnDef.derivedDataType !== "date" && columnDef.derivedDataType !== "timestamp") {
               formulaList = _.without(formulas, "to_date", "year", "month", "day", "hour", "minute");
            }

           }
       }
       return formulaList;
    }
}



class TableFormControls {

    public constructor(public defineTableForm:FormGroup,public definePartitionForm:FormGroup,
                       private feedTableColumnDefinitionValidation: FeedTableColumnDefinitionValidation,
                       private tablePermissions:TablePermissions ){

    }



    public static TABLE_COLUMN_DEF_NAME_PREFIX:string = "name";
    public static TABLE_COLUMN_DEF_DATA_TYPE_PREFIX:string = "dataType";
    public static TABLE_COLUMN_DEF_PRECISION_SCALE_PREFIX:string = "precisionScale";
    public static TABLE_COLUMN_DEF_INDEX_PREFIX:string = "index";
    public static TABLE_COLUMN_DEF_PROFILE_PREFIX:string = "profile";


    public static TABLE_COLUMN_DEF_PREFIXES :string[] = [TableFormControls.TABLE_COLUMN_DEF_NAME_PREFIX,
        TableFormControls.TABLE_COLUMN_DEF_DATA_TYPE_PREFIX,
        TableFormControls.TABLE_COLUMN_DEF_PRECISION_SCALE_PREFIX,
        TableFormControls.TABLE_COLUMN_DEF_INDEX_PREFIX,
        TableFormControls.TABLE_COLUMN_DEF_PROFILE_PREFIX];

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
        let nameControl = new FormControl({value:field.name,disabled:field.deleted|| this.tablePermissions.tableLocked },[Validators.required, this.feedNameValidator(this.feedTableColumnDefinitionValidation,field)]);
       // nameControl.valueChanges.subscribe()
        //(change)="onNameFieldChange(row,index)">
        controls[TableFormControls.TABLE_COLUMN_DEF_NAME_PREFIX+"_"+field._id] = nameControl;
        controls[TableFormControls.TABLE_COLUMN_DEF_DATA_TYPE_PREFIX+"_"+field._id] = new FormControl({value:field.derivedDataType,disabled:field.deleted|| this.tablePermissions.dataTypeLocked|| this.tablePermissions.tableLocked },[Validators.required]);
        controls[TableFormControls.TABLE_COLUMN_DEF_PRECISION_SCALE_PREFIX+"_" + field._id] = new FormControl({value:field.precisionScale,disabled:this.tablePermissions.dataTypeLocked || field.deleted},[TableFormControls.precisionScale]);

        let index = field.fieldPolicy ? field.fieldPolicy.index : false;
        let profile = field.fieldPolicy ? field.fieldPolicy.profile: false;
        controls[TableFormControls.TABLE_COLUMN_DEF_INDEX_PREFIX+"_" + field._id] = new FormControl({value:index,disabled:field.isComplex() || field.deleted },[]);
        controls[TableFormControls.TABLE_COLUMN_DEF_PROFILE_PREFIX+"_" + field._id] = new FormControl({value:profile,disabled:field.isComplex() || field.deleted },[]);
        return controls;
    }

    getTableFieldFormControl(prefix:string,field:TableColumnDefinition){
        return this.getFormControl(this.defineTableForm,prefix,field);
    }

    resetFormFields() {
        Object.keys(this.defineTableForm.controls).forEach((key: string) => {
            if(key != "indexCheckAll" && key != "profileCheckAll") {
                this.defineTableForm.removeControl(key)

            }
        });
        Object.keys(this.definePartitionForm.controls).forEach((key: string) => {

         this.definePartitionForm.removeControl(key);

        });
    }


    addTableFieldFormControl(columnDef:TableColumnDefinition,touch:boolean =false){
        let formControls :{ [key: string]: AbstractControl; } = this.buildTableFieldFormControl(columnDef);
        let keys :string[] = Object.keys(formControls)
        keys.forEach(key => {
            const ctrl = formControls[key];
            if(touch) {
                //mark it as touched to force validation
                ctrl.markAsTouched({onlySelf:true});
            }
            this.defineTableForm.addControl(key,ctrl);
        })
    }


    private buildPartitionFieldFormControl(partition: TableFieldPartition ) :Common.Map<FormControl> {
        let controls :Common.Map<FormControl> = {}
        controls["partitionColumnRef_"+partition._id] = new FormControl({value:'',disabled:this.tablePermissions.partitionsLocked},[Validators.required]);
        controls["partitionFormula_"+partition._id] = new FormControl({value:partition.formula,disabled:this.tablePermissions.partitionsLocked},[Validators.required]);
        controls["partitionName_"+partition._id] = new FormControl({value:partition.field,disabled:(!partition.allowPartitionNameChanges() || this.tablePermissions.partitionsLocked)},[Validators.required]);
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
            const ctrl = formControls[key];
            //mark it as touched to force validation
            ctrl.markAsTouched({onlySelf:true});
            this.definePartitionForm.addControl(key,ctrl);
        })
    }

    removePartitionFieldFormControls(partition:TableFieldPartition){
        this.definePartitionForm.removeControl("partitionColumnRef_"+partition._id);
        this.definePartitionForm.removeControl("partitionFormula_"+partition._id);
        this.definePartitionForm.removeControl("partitionName_"+partition._id);
    }

    updateFieldState(field:TableColumnDefinition){
        TableFormControls.TABLE_COLUMN_DEF_PREFIXES.forEach(prefix => {
            let formControl = this.getFormControl(this.defineTableForm,prefix,field);
            if(field.deleted){
                formControl.disable();
            }
            else {
                formControl.enable()
            }
        })
    }

}
