/*-
 * #%L
 * thinkbig-ui-feed-manager
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import * as angular from 'angular';
import * as _ from "underscore";
const moduleName = require('feed-mgr/feeds/define-feed/module-name');


export class DefineFeedTableController {


    messages:any;

    defineFeedTableForm:any = {};
    stepNumber:any;
    stepperController:any=null;
    model:any;
    isValid:boolean=false;
    sampleFile:any=null;
    tableCreateMethods:any;
    availableDefinitionDataTypes:any;
    schemaParser:any={};
    useUnderscoreInsteadOfSpaces:boolean=true;
    selectedColumn:any=null;
    fieldNamesUniqueRetryAmount:number=0;

    showMethodPanel:boolean=true;
    showTablePanel:boolean=true;
    uploadBtnDisabled:boolean=false;
    partitionFormulas:any=[];

    feedFormat:string='';

    /**
     * Provides a list of available tags.
     * @type {FeedTagService}
     */
    feedTagService:any;

    /**
     * Metadata for the selected column tag.
     * @type {{searchText: null, selectedItem: null}}
     */
    tagChips:any;

    /**
     * List of available domain types.
     * @type {DomainType[]}
     */
    availableDomainTypes:any=[];

    stepIndex:any;

    tableLocked:any;
    dataTypeLocked:any;
    canRemoveFields:any;
    typeLocked:any;
    expanded:any;
    totalSteps:number;
    systemFeedNameWatch:any;
    invalidColumnsWatch:any;
    formValidWatch:any;
    tableMethodWatch:any;
    sampleFileWatch:any;


    NAME_PATTERN:any = /^[a-zA-Z0-9_\s\)\(-]*$/;
    PRECISION_SCALE_PATTERN:any = /^\d+,\d+$/;
    MAX_COLUMN_LENGTH:number = 767;

    $onInit() {
        this.totalSteps = this.stepperController.totalSteps;
        this.stepNumber = parseInt(this.stepIndex) + 1;
        // choose to expand the choose method initially if no fields have been defined yet
        if(this.model.table.tableSchema.fields.length == 0){
            this.expandChooseMethodPanel();
        }
        else {
            this.expandSchemaPanel();
        }
    }
    $onDestroy(){
            this.systemFeedNameWatch();
            this.invalidColumnsWatch();
            this.formValidWatch();
            this.tableMethodWatch();
            this.sampleFileWatch();
    }

    static readonly $inject = ["$rootScope","$scope","$http","$timeout","$mdToast","$filter","$mdDialog",
            "$mdExpansionPanel","RestUrlService","FeedService","FileUpload","BroadcastService",
            "Utils", "FeedTagService", "DomainTypesService"];

    constructor(private $rootScope:any, private $scope:any, private $http:any, private $timeout:any, private $mdToast:any, private $filter:any, private $mdDialog:any
        ,private $mdExpansionPanel:any, private RestUrlService:any, private FeedService:any, private FileUpload:any, private BroadcastService:any, private Utils:any, private FeedTagService:any,
        private DomainTypesService:any) {

            this.messages = {
            name: {
                required: "Name is required",
                pattern: "Name cannot contain special characters",
                notUnique: "Name must be unique",
                reserved: "Name reserved by Kylo",
                length: "Name cannot be longer than " + this.MAX_COLUMN_LENGTH + " characters"
            },
            precision: {
                pattern: "Invalid, e.g. 10,0"
            }
        };
        
        this.tableCreateMethods = [{type: 'MANUAL', name: 'Manual'}, {type: 'SAMPLE_FILE', name: 'Sample File'}];
        this.model = FeedService.createFeedModel;
        this.availableDefinitionDataTypes = FeedService.columnDefinitionDataTypes.slice();
        /**
         * Provides a list of available tags.
         * @type {FeedTagService}
         */
        this.feedTagService = FeedTagService;
        this.tagChips = {searchText: null, selectedItem: null};

        DomainTypesService.findAll().then((domainTypes:any) => {
            this.availableDomainTypes = domainTypes;
        });
        $scope.$evalAsync(() => {
            // console.log("$scope.$evalAsync");
            this.calcTableState();
            if (this.model.table.tableSchema.fields && this.model.table.tableSchema.fields.length > 0) {
                this.syncFeedsColumns();
                this.calcTableState();
                this.expandSchemaPanel();
            }
        });
        BroadcastService.subscribe($scope, 'DATA_TRANSFORM_SCHEMA_LOADED', () =>{
            this.syncFeedsColumns();
            this.validate(undefined);
        });

        this.invalidColumnsWatch = $scope.$watch(() => {
            return this.defineFeedTableForm.invalidColumns
        }, (newVal:any) =>{
            // console.log("watching this.defineFeedTableForm.invalidColumns");
            this.validate(undefined);
        }, true);

        this.tableMethodWatch = $scope.$watch(() => {
            return this.model.table.method;
        }, (newVal:any) => {
            // console.log("watching model.table.method");
            this.model.table.method = newVal;
            this.calcTableState();
        });

        //Set the Table Name to be the System Feed Name
        this.systemFeedNameWatch = $scope.$watch(() => {
            return this.model.systemFeedName;
        }, (newVal:any) => {
            this.model.table.tableSchema.name = newVal;
        });

        /**
         * Ensure the form is valid
         * @type {*|function()}
         */
        this.formValidWatch = $scope.$watch(() => {
            return this.defineFeedTableForm.$valid;
        }, (newVal:any) => {
            // console.log("watching this.defineFeedTableForm.$valid, newVal = " + newVal);
            if (newVal === true) {
                this.validate(newVal);
            }
            else {
                this.isValid = false;
            }

        });

        this.sampleFileWatch = $scope.$watch(() => {
            return this.sampleFile;
        }, (newVal:any) =>{
            if (newVal == null) {
                angular.element('#upload-sample-file-btn').removeClass('md-primary');
            }
            else {
                angular.element('#upload-sample-file-btn').addClass('md-primary');
            }
        });
        // Retrieve partition formulas
        this.FeedService.getPartitionFunctions()
                .then((functions:any) =>{
                    this.partitionFormulas = functions;
                });

        this.validate(undefined);
        this.expanded = false;

        this.calcTableState();
    }
        isSchemaPanelExpanded(){
            return this.expanded;
        }
        /*
         Collapse the file picker section
         */
        collapseMethodPanel(){
            this.$mdExpansionPanel().waitFor('panelOne').then((instance:any) =>{
                instance.collapse();
            });
        };
        /*
         Expand the schema panel
         */
        expandSchemaPanel(){
            // console.log("expandSchemaPanel");
            this.$mdExpansionPanel().waitFor('panelTwo').then((instance:any) => {
                // console.log("expanding schema panel");
                instance.expand();
                this.expanded = true;
            });
        };
        expandChooseMethodPanel(){
            this.$mdExpansionPanel().waitFor('panelOne').then((instance:any) =>{
                instance.expand();
            });
        };
        uploadSampleFile(){
            this.uploadBtnDisabled = true;
            this.showProgress();
            var file = this.sampleFile;
            var params = {};
            if (this.schemaParser) {
                params = {parser: JSON.stringify(this.schemaParser)};
            }
            var uploadUrl = this.RestUrlService.UPLOAD_SAMPLE_TABLE_FILE;
            var successFn = (response:any) => {
                // console.log("loaded schema");
                var responseData = response.data;
                this.resetColumns();
                this.availableDefinitionDataTypes = this.FeedService.columnDefinitionDataTypes.slice();
                angular.forEach(responseData.fields, (field) => {
                    var col = this.newColumnDefinition();
                    col = angular.extend(col, field)
                    // add exotic data type to available columns if needed
                    if ($.inArray(col.derivedDataType, this.availableDefinitionDataTypes) == -1) {
                        this.availableDefinitionDataTypes.push(col.derivedDataType);
                    }
                    this.addColumn(col, false);
                });
                this.FeedService.syncTableFieldPolicyNames();
                this.applyDomainTypes();
                //set the feedFormat property
                this.model.table.feedFormat = responseData.hiveFormat;
                this.model.table.structured = responseData.structured;
                if (this.schemaParser.allowSkipHeader) {
                    this.model.allowSkipHeaderOption = true;
                    this.model.options.skipHeader = true;
                } else {
                    this.model.allowSkipHeaderOption = false;
                    this.model.options.skipHeader = false;
                }
                this.hideProgress();
                this.uploadBtnDisabled = false;
                this.syncFeedsColumns();
                this.calcTableState();
                this.collapseMethodPanel();
                this.expandSchemaPanel();

                this.validate(undefined);
                angular.element('#upload-sample-file-btn').removeClass('md-primary');

                this.$timeout(this.touchErrorFields, 2000);
            };
            var errorFn = (data:any) => {
                this.hideProgress();
                this.uploadBtnDisabled = false;
                angular.element('#upload-sample-file-btn').removeClass('md-primary');
                angular.element('#uploadButton').addClass('md-primary');
            };
            //clear partitions
            while (this.model.table.partitions.length) {
                this.model.table.partitions.pop();
            }
            this.FileUpload.uploadFileToUrl(file, uploadUrl, successFn, errorFn, params);
        }

        /**
         * Detects and applies domain types to all columns.
         */
        applyDomainTypes() {
            // Detect domain types
            var data:any = {domainTypes: [], fields: []};

            this.model.table.tableSchema.fields.forEach((field:any, index:any) => {
                var domainType = this.DomainTypesService.detectDomainType(field, this.availableDomainTypes);
                if (domainType !== null) {
                    if (this.DomainTypesService.matchesField(domainType, field)) {
                        // Domain type can be applied immediately
                        this.FeedService.setDomainTypeForField(field, this.model.table.fieldPolicies[index], domainType);
                        field.history = [];
                        this.addHistoryItem(field);
                    } else {
                        // Domain type needs user confirmation
                        data.domainTypes.push(domainType);
                        data.fields.push(field);
                    }
                }
            });

            // Get user confirmation for domain type changes to field data types
            if (data.fields.length > 0) {
                this.$mdDialog.show({
                    controller: "ApplyTableDomainTypesDialog",
                    escapeToClose: false,
                    fullscreen: true,
                    parent: angular.element(document.body),
                    templateUrl: "js/feed-mgr/shared/apply-domain-type/apply-table-domain-types.component.html",
                    locals: {
                        data: data
                    }
                })
                    .then((selected:any) => {
                        selected.forEach((selection:any) => {
                            var fieldIndex = data.fields.findIndex((element:any) => {
                                return element.name === selection.name;
                            });
                            var policyIndex = this.model.table.tableSchema.fields.findIndex((element:any) =>{
                                return element.name === selection.name;
                            });
                            this.FeedService.setDomainTypeForField(data.fields[fieldIndex], this.model.table.fieldPolicies[policyIndex], data.domainTypes[fieldIndex]);
                            data.fields[fieldIndex].history = [];
                            this.addHistoryItem(data.fields[fieldIndex]);
                        });
                    }, () =>{
                        // ignore cancel
                    });
            }
        }

    touchErrorFields() {
        var errors = this.defineFeedTableForm.$error;
        for (var key in errors) {
            if (errors.hasOwnProperty(key)) {
                var errorFields = errors[key];
                angular.forEach(errorFields, (errorField) =>{
                    errorField.$setTouched();
                });
            }
        }
    }
    /**
    * Transforms the specified chip into a tag.
    * @param {string} chip the chip
    * @returns {Object} the tag
    */
    transformChip(chip:any) {
        return angular.isObject(chip) ? chip : {name: chip};
    }

    calcTableState(){
        // console.log("calcTableState");
        this.tableLocked = angular.isDefined(this.tableLocked) && (this.tableLocked === true || this.tableLocked === "true");
        this.dataTypeLocked = angular.isDefined(this.dataTypeLocked) && (this.typeLocked === true || this.typeLocked === "true");
        this.canRemoveFields = angular.isUndefined(this.canRemoveFields) || this.canRemoveFields === true || this.canRemoveFields === "true";
        this.showMethodPanel = (this.model.table.method != 'EXISTING_TABLE');
        this.showTablePanel = (this.model.table.tableSchema.fields.length > 0);
    };
    

    /*
    Create columns for tracking changes between original source and the target table schema
     */
    syncFeedsColumns (){
        // console.log("syncFeedsColumns");
        _.each(this.model.table.tableSchema.fields, function (columnDef) {
            this.initFeedColumn(columnDef);
        });
    }

    initFeedColumn(columnDef:any){
        // console.log('init feed column');
        if (columnDef.origName == undefined) {
            columnDef.origName = columnDef.name;
            columnDef.origDataType = columnDef.derivedDataType;
            columnDef.deleted = false;
            columnDef._id = _.uniqueId();
            columnDef.history = [];
            this.addHistoryItem(columnDef);
        }
    }

    /*
    Adds record of changed state
     */
    createHistoryRecord(columnDef:any) {
        return { name: columnDef.name, derivedDataType: columnDef.derivedDataType, precisionScale: columnDef.precisionScale, deleted: columnDef.deleted, primaryKey: columnDef.primaryKey, updatedTracker: columnDef.updatedTracker, createdTracker: columnDef.createdTracker }
    }

    /**
     * Called wehn the Method radio option is changed
     */
    updateSelectedMethod(method:any){
        if(method == 'MANUAL' ) {
            this.model.allowSkipHeaderOption = true;
        }

    };

    addHistoryItem(columnDef:any){
        var historyItem = this.createHistoryRecord(columnDef);
        columnDef.history.push(historyItem);
    };

    /**
     * when adding a new column
     * @returns {*|{name, description, dataType, precisionScale, dataTypeDisplay, primaryKey, nullable, sampleValues, selectedSampleValue, isValid, _id}}
     */
    newColumnDefinition() {
        // console.log('new column def');
        return this.FeedService.newTableFieldDefinition();
    }

    /**
     * when adding a new column this is also called to synchronize the field policies array with the columns
     * @returns {*|{name, partition, profile, standardization, validation}}
     */
    newColumnPolicy() {
        return this.FeedService.newTableFieldPolicy();
    }

    /**
     * When adding a new Partition
     * @param index
     * @returns {{position: *, field: string, sourceField: string, formula: string, sourceDataType: string, _id: *}}
     */
    newPartitionField(index:any) {
        return {position: index, field: '', sourceField: '', formula: '', sourceDataType: '', _id: _.uniqueId()}
    }

    replaceSpaces(str:any) {
        if (str != undefined) {
            return str.replace(/ /g, '_');
        }
        else {
            return '';
        }
    }

    showProgress() {
        if (this.stepperController) {
            this.stepperController.showProgress = true;
        }
    }

    hideProgress() {
        if (this.stepperController) {
            this.stepperController.showProgress = false;
        }
    }

    resetColumns() {
        // console.log("resetColumns");
        this.model.table.tableSchema.fields = [];
        this.model.table.fieldPolicies = [];
        this.defineFeedTableForm.invalidColumns = [];
    }

    /**
     * Ensure the Partition Names are unique
     * If Not add a angular error
     */
    partitionNamesUnique() {
        // console.log("partitionNamesUnique");

        // Validate the Partition names are unique respective to other partition names
        _.chain(this.model.table.partitions).groupBy( (partition) => {
            return partition.field
        }).each((group, name) => {
            if (group.length > 1) {
                _.each(group, (partition) =>{
                    //.invalid another partition matches the same name
                    this.defineFeedTableForm['partition_name' + partition._id].$setValidity('notUnique', false);
                });
            }
            else {
                _.each(group, (partition) =>{
                    //valid this is a unique partition name
                    this.defineFeedTableForm['partition_name' + partition._id].$setValidity('notUnique', true);
                });
            }
        });

        //Validate the Partition names are unique respective to the other fields

        //an array of column names
        var columnNames = _.map(this.model.table.tableSchema.fields, (columnDef:any) =>{
            return columnDef.name;
        });
        var countPartitionNames = {};
        //add the angular errors
        _.each(this.model.table.partitions, (partition:any) =>{
            if (partition.formula != undefined && partition.formula != 'val' && _.indexOf(columnNames, partition.field) >= 0) {
                this.defineFeedTableForm['partition_name' + partition._id].$setValidity('notUnique', false);
            }
        });

    }

    /**
     * Adding a new Column to the schema
     * This is called both when the user clicks the "Add Field" button or when the sample file is uploaded
     * If adding from the UI the {@code columnDef} will be null, otherwise it will be the parsed ColumnDef from the sample file
     * @param columnDef
     */
    addColumn(columnDef:any, syncFieldPolicies:any){
        // console.log("addColumn");
        if (columnDef == null) {
            columnDef = this.newColumnDefinition();
        }

        // Detect domain type and select sample value
        var policy = this.newColumnPolicy();
        if (columnDef.sampleValues != null && columnDef.sampleValues.length > 0) {
            columnDef.selectedSampleValue = columnDef.sampleValues[0];
        } else {
            columnDef.selectedSampleValue = null;
        }

        if (this.useUnderscoreInsteadOfSpaces) {
            columnDef.name = this.replaceSpaces(columnDef.name);
        }
        this.initFeedColumn(columnDef);
        //add the column to both the source and destination tables as well as the fieldPolicies array
        this.model.table.tableSchema.fields.push(columnDef);
        this.model.table.fieldPolicies.push(policy);
        this.model.table.sourceTableSchema.fields.push(this.newColumnDefinition());
        this.validateColumn(columnDef);
        if (syncFieldPolicies == undefined || syncFieldPolicies == true) {
            this.FeedService.syncTableFieldPolicyNames();
        }
    }

    undoColumn(index:any){
        // console.log("undoColumn");
        var columnDef = this.model.table.tableSchema.fields[index];
        columnDef.history.pop();
        var prevValue = columnDef.history[columnDef.history.length-1];

        // Set to previous history value
        columnDef.name = prevValue.name;
        columnDef.derivedDataType = prevValue.derivedDataType;
        columnDef.precisionScale = prevValue.precisionScale;
        columnDef.deleted = prevValue.deleted;
        columnDef.primaryKey = prevValue.primaryKey;
        columnDef.createdTracker = prevValue.createdTracker;
        columnDef.updatedTracker = prevValue.updatedTracker;

        this.validateColumn(columnDef);
        this.partitionNamesUnique();
        this.FeedService.syncTableFieldPolicyNames();
        this.validate(undefined);
    }

    /**
     * Remove a column from the schema
     * @param index
     */
    removeColumn(index:any){
        // console.log("removeColumn");
        var columnDef = this.model.table.tableSchema.fields[index];
        columnDef.deleted = true;
        this.addHistoryItem(columnDef);

        //remove any partitions using this field
        this.model.table.partitions
            .filter(function (partition:any) {
                return partition.columnDef.name === columnDef.name;
            })
            .map(function (partition:any) {
                return partition._id;
            })
            .forEach(function (id:any) {
                var index = this.model.table.partitions.findIndex(function (partition:any) {
                    return partition._id === id;
                });
                if (index > -1) {
                    this.removePartitionField(index);
                }
            });

        //ensure the field names on the columns are unique again as removing a column might fix a "notUnique" error
        this.validateColumn(columnDef);
        this.partitionNamesUnique();
        this.validate(undefined);
    }

    /**
     * Removes the column matching the passed in {@code columnDef} with the array of columns
     * @param columnDef
     */
    removeColumnUsingReference(columnDef:any){

        var idx = _.indexOf(this.model.table.tableSchema.fields, columnDef)
        if (idx >= 0) {
            this.removeColumn(idx);
        }
    }

    /**
     * Add a partition to the schema
     * This is called from the UI when the user clicks "Add Partition"
     */
    addPartitionField(){
        var partitionLength = this.model.table.partitions.length;
        var partition = this.newPartitionField(partitionLength);
        this.model.table.partitions.push(partition);
    }

    /**
     * Remove the partition from the schecma
     * @param index
     */
    removePartitionField(index:any){
        this.model.table.partitions.splice(index, 1);
        this.partitionNamesUnique();
    }

    getSelectedColumn(){
        return this.selectedColumn;
    }

    onSelectedColumn(selectedColumn:any){
        var firstSelection = this.selectedColumn == null;
        this.selectedColumn = selectedColumn;
        // Show an item in dropdown
        if (this.selectedColumn.selectedSampleValue == null && this.selectedColumn.sampleValues.length > 0) {
            this.selectedColumn.selectedSampleValue = this.selectedColumn.sampleValues[0];
        }
        if(firstSelection){
            //trigger scroll to stick the selection to the screen
            this.Utils.waitForDomElementReady('#selectedColumnPanel',()=> {
                angular.element('#selectedColumnPanel').triggerHandler('stickIt');
            })
        }

        // Ensure tags is an array
        if (!angular.isArray(selectedColumn.tags)) {
            selectedColumn.tags = [];
        }
    }

    onPrecisionChange(columnDef:any){
        this.validateColumn(columnDef);
        this.onFieldChange(columnDef);
    }

    /**
     * When the schema field changes it needs to
     *  - ensure the names are unique
     *  - update the respective partition names if there is a partition on the field with the 'val' formula
     *  - ensure that partition names are unique since the new field name could clash with an existing partition
     * @param columnDef
     */
    onNameFieldChange(columnDef:any, index:any){
        // console.log("onNameFieldChange, columnDef", columnDef);

        if (this.useUnderscoreInsteadOfSpaces) {
            columnDef.name = this.replaceSpaces(columnDef.name);
        }

        if(columnDef.derivedDataType !== 'decimal'){
            columnDef.precisionScale = null;
        }
        this.onFieldChange(columnDef);

        //update the partitions with "val" on this column so the name matches
        _.each(this.model.table.partitions, (partition:any) =>{
            if (partition.columnDef == columnDef) {
                partition.sourceDataType = columnDef.derivedDataType;
                partition.sourceField = columnDef.name;
                this.updatePartitionFieldName(partition);
            }
        });
        this.validateColumn(columnDef);
        this.partitionNamesUnique();
        this.FeedService.syncTableFieldPolicyNames();

        // Check if column data type matches domain data type
        var policy = this.model.table.fieldPolicies[index];
        var domainType = policy.$currentDomainType;

        if (policy.domainTypeId && domainType.field && columnDef.$allowDomainTypeConflict !== true) {
            var nameChanged = (domainType.field.name && columnDef.name !== domainType.field.name);
            var dataTypeChanged = (domainType.field.derivedDataType && columnDef.derivedDataType !== domainType.field.derivedDataType);
            if (nameChanged || dataTypeChanged) {
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
                    .then((keep:any) =>{
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
        }
    }

    isDeleted(columnDef:any) {
        return columnDef.deleted === true;
    }

    isInvalid(columnDef:any) {
        var errorCount = 0;
        var columnDefFields = _.values(columnDef.validationErrors);
        _.each(columnDefFields, (columnDefField) => {
            errorCount += this.fieldErrorCount(columnDefField);
        });
        return errorCount > 0;
    }

    fieldErrorCount(columnDefField:any) {
        var errorTypes = _.values(columnDefField);
        var errors = _.filter(errorTypes, (errorType) =>{
            return errorType === true;
        });
        return errors === undefined ? 0 : errors.length;
    }

    updateFormValidation(columnDef:any){
        if (this.isInvalid(columnDef)) {
            this.add(this.defineFeedTableForm.invalidColumns, columnDef);
        } else {
            this.remove(this.defineFeedTableForm.invalidColumns, columnDef);
        }
    }

    initValidationErrors(columnDef:any){
        columnDef.validationErrors = {
            name: {},
            precision: {}
        };
    }

    validateColumn(columnDef:any) {
        // console.log("validateColumn");
        if (columnDef.validationErrors === undefined) {
            this.initValidationErrors(columnDef);
        }

        if (!this.isDeleted(columnDef)) {
            columnDef.validationErrors.name.reserved = columnDef.name === "processing_dttm";
            columnDef.validationErrors.name.required = _.isUndefined(columnDef.name) || columnDef.name.trim() === "";
            columnDef.validationErrors.name.length = !_.isUndefined(columnDef.name) && columnDef.name.length > this.MAX_COLUMN_LENGTH;
            columnDef.validationErrors.name.pattern = !_.isUndefined(columnDef.name) && !this.NAME_PATTERN.test(columnDef.name);
            columnDef.validationErrors.precision.pattern = columnDef.derivedDataType === 'decimal' && (_.isUndefined(columnDef.precisionScale) || !this.PRECISION_SCALE_PATTERN.test(columnDef.precisionScale));
        }

        //update all columns at all times, because column removal may fix not unique name error on other columns
        var columnsByName = _.groupBy(this.model.table.tableSchema.fields, (column:any) => {
            //we'll disregard "not unique" name for all empty names and all deleted columns, i.e. put them into single group
            if (this.isDeleted(column)) {
                return "";
            }
            return column.name ? column.name.trim() : "";
        });
        _.each(_.keys(columnsByName), (columnName) => {
            var group = columnsByName[columnName];
            _.each(group, (column) => {
                if (column.validationErrors === undefined) {
                    this.initValidationErrors(column);
                }
                if (columnName !== "") {
                    column.validationErrors.name.notUnique = group.length > 1;
                } else {
                    //group with empty column name which also includes "deleted" columns
                    column.validationErrors.name.notUnique = false;
                }
                this.updateFormValidation(column);
            });
        });

        //reset partition formula if data type has changed from date/timestamp to another type
        _.each(this.model.table.partitions, (partition:any) => {
            if (partition.columnDef === columnDef) {
                if (columnDef.derivedDataType !== "date" || columnDef.derivedDataType !== "timestamp") {
                     _.forEach(["to_date", "year", "month", "day", "hour", "minute"], (formula) => {
                         if (partition.formula === formula) {
                             partition.formula = "val"
                         }
                     });
                }
            }
        });

    }

    errorMessage(columnDef:any){
        if (columnDef.validationErrors === undefined) {
            return;
        }

        if (columnDef.validationErrors.name.required) {
            return this.messages.name.required;
        }
        if (columnDef.validationErrors.name.pattern) {
            return this.messages.name.pattern;
        }
        if (columnDef.validationErrors.name.notUnique) {
            return this.messages.name.notUnique;
        }
        if (columnDef.validationErrors.name.reserved) {
            return this.messages.name.reserved;
        }
        if (columnDef.validationErrors.name.length) {
            return this.messages.name.length;
        }
    }

    remove(array:any, element:any) {
        for (var i = 0; i < array.length; i++) {
            if (array[i]._id === element._id) {
                array.splice(i, 1);
                break;
            }
        }
    }
    add(array:any, element:any){
        this.remove(array, element);
        array.push(element);
    }

    onFieldChange(columnDef:any){
        // console.log("onFieldChange");
        this.selectedColumn = columnDef;
        this.addHistoryItem(columnDef);
    }

    /**
     * When a partition Source field changes it needs to
     *  - auto select the formula if there is only 1 in the drop down (i.e. fields other than dates/timestamps will only have the 'val' formula
     *  - ensure the partition data mapping to this source field is correct
     *  - attempt to prefill in the name with some default name.  if its a val formula it will default the partition name to the source field name and leave it disabled
     * @param partition
     */
    onPartitionSourceFieldChange(partition:any){
        // console.log("onPartitionSourceFieldChange");
        //set the partition data to match the selected sourceField
        if (partition.columnDef != null) {
            partition.sourceField = partition.columnDef.name
            partition.sourceDataType = partition.columnDef.derivedDataType;
        }
        else {
            //  console.error("NO FIELD FOR partition ",partition)
        }
        //if there is only 1 option in the formula list then auto select it
        var formulas = this.$filter('filterPartitionFormula')(this.partitionFormulas, partition);
        if (formulas.length == 1) {
            partition.formula = formulas[0];
        }
        this.updatePartitionFieldName(partition);
        this.partitionNamesUnique();

    }
    /**
     * When a partition formula changes it needs to
     *  - attempt to prefill in the name with some default name.  if its a val formula it will default the partition name to the source field name and leave it disabled
     * @param partition
     */
    onPartitionFormulaChange(partition:any){
        // console.log("onPartitionFormulaChange");
        this.updatePartitionFieldName(partition);
        this.partitionNamesUnique();
    }

    /**
     * when the partition name changes it needs to
     *  - ensure the names are unique
     *  - ensure no dups (cannot have more than 1 partitoin on the same col/formula
     * @param partition
     */
    onPartitionNameChange(partition:any){
        // console.log("onPartitionNameChange");
        if (this.useUnderscoreInsteadOfSpaces) {
            partition.field = this.replaceSpaces(partition.field);
        }
        this.partitionNamesUnique();
    }

    /**
     * Helper method to look through the table columns (not partitions) and find the first one that matches the incoming {@code fieldName}
     * @param fieldName
     * @returns {*|{}}
     */
    getColumnDefinition(fieldName:any){

        return _.find(this.model.table.tableSchema.fields, (field:any) => {
            return field.name == fieldName;
        });
    }

    /**
     * Ensure that for the partitions the sourceField and sourceDataTypes match the respective schema field data
     */
    ensurePartitionData() {
        // console.log("ensurePartitionData");
        var nameMap = {};
        _.each(this.model.table.partitions, (partition:any) => {
            if (partition.columnDef == undefined) {
                var columnDef = this.getColumnDefinition(partition.sourceField);
                if (columnDef != null) {
                    partition.columnDef = columnDef;
                }
                else {
                    //ERROR!!
                    //  console.error("unable to find columnDef for partition ",partition)
                }
            }

            if (partition.columnDef) {
                partition.sourceDataType = partition.columnDef.derivedDataType;
                partition.sourceField = partition.columnDef.name;
            }
        });

    }

    /**
     * if the formula == val assign the field to be the same as the source field, otherwise attempt to prefill the name with the source_formula
     * @param partition
     */
    updatePartitionFieldName(partition:any){
        // console.log("updatePartitionFieldName");
        if (partition.formula != 'val') {
            if (partition.sourceField != null && (partition.field == null || partition.field == '' || partition.field == partition.sourceField + "_")) {
                partition.field = partition.sourceField + "_" + partition.formula;
            }
        }
        else {
            partition.field = partition.columnDef ? partition.columnDef.name : partition.sourceField;
        }
    }

    validate(validForm:any) {
        // console.log("validate valid ? " + validForm);
        if (_.isUndefined(this.defineFeedTableForm.invalidColumns)) {
            this.defineFeedTableForm.invalidColumns = [];
        }
        if (validForm == undefined) {
            validForm = this.defineFeedTableForm.$valid ;
        }
        var valid = this.model.templateId != null && this.model.table.method != null && this.model.table.tableSchema.name != null && this.model.table.tableSchema.name != ''
                    && this.model.table.tableSchema.fields.length > 0;

        if (valid) {
            //ensure we have at least 1 field (not deleted) assigned to the model)
            var validFields = _.filter(this.model.table.tableSchema.fields,(field:any) => {
                return field.deleted == undefined || field.deleted == false;
            });
            valid = validFields.length >0;
            this.ensurePartitionData();
        }
        this.isValid = valid && validForm && this.defineFeedTableForm.invalidColumns.length === 0;
    }

}

// angular.module(moduleName).controller('DefineFeedTableController', ["$rootScope","$scope","$http","$timeout","$mdToast","$filter","$mdDialog","$mdExpansionPanel","RestUrlService","FeedService","FileUpload","BroadcastService","Utils", "FeedTagService", "DomainTypesService", DefineFeedTableController]);

// angular.module(moduleName).directive('thinkbigDefineFeedTable', directive);

angular.module(moduleName).filter("filterPartitionFormula", ["FeedService", (FeedService:any) => {
    return (formulas:any, partition:any) => {
        // Find column definition
        var columnDef = (partition && partition.sourceField) ? FeedService.getColumnDefinitionByName(partition.sourceField) : null;
        if (columnDef == null) {
            return formulas;
        }

        // Filter formulas based on column type
        if (columnDef.derivedDataType !== "date" && columnDef.derivedDataType !== "timestamp") {
            return _.without(formulas, "to_date", "year", "month", "day", "hour", "minute");
        } else {
           return formulas;
        }
    };
}]);
angular.module(moduleName).
    component("thinkbigDefineFeedTable", {
        bindings: {
            canRemoveFields: "@",
            stepIndex: '@',
            tableLocked: "@",
            typeLocked: "@"
        },
        require: {
            stepperController: "^thinkbigStepper"
        },
        controllerAs: 'vm',
        controller: DefineFeedTableController,
        templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/define-feed-table.html',
    });