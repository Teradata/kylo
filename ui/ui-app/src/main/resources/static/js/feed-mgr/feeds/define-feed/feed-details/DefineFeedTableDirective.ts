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

import {PipeTransform} from '@angular/core';
import * as angular from 'angular';
import * as _ from "underscore";
import {DomainType, DomainTypesService} from "../../../services/DomainTypesService";
import {TableColumnDefinition} from "../../../model/TableColumnDefinition";
import {TableFieldPartition} from "../../../model/TableFieldPartition";
import {TableFieldPolicy} from "../../../model/TableFieldPolicy";
import {TableCreateMethod, TableForm} from "../../../model/feed/feed-table";
import {ObjectUtils} from "../../../../../lib/common/utils/object-utils";
import {StringUtils} from "../../../../common/utils/StringUtils";


const moduleName = require('../module-name');


export class ExpansionPanelHelper {


    constructor(private $mdExpansionPanel: any) {

    }

    /*
     Collapse the file picker section
     */
    collapseMethodPanel() {
        this.$mdExpansionPanel().waitFor('panelOne').then((instance: any) => {
            instance.collapse();
        });
    };

    /*
     Expand the schema panel
     */
    expandSchemaPanel() {
        this.$mdExpansionPanel().waitFor('panelTwo').then((instance: any) => {
            instance.expand();
        });
    };

    collapseMethodAndExpandSchemaPanel() {
        this.collapseMethodPanel();
        this.expandSchemaPanel();
    }

    expandChooseMethodPanel() {
        this.$mdExpansionPanel().waitFor('panelOne').then((instance: any) => {
            instance.expand();
        });
    };

}


export class DefineFeedTableController {

    /**
     * The 1 based index step number
     */
    stepNumber: number;
    /**
     * The feed stepper controller
     */
    stepperController: any;

    /**
     * The FeedMetadata model boject
     */
    model: any;
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

    expansionPanelHelper: ExpansionPanelHelper;

    tableLocked: boolean;
    dataTypeLocked: boolean;
    canRemoveFields: boolean;


    tableForm: TableForm;

    static readonly $inject = ["$rootScope", "$scope", "$http", "$timeout", "$mdToast", "$filter", "$mdDialog", "$mdExpansionPanel", "RestUrlService", "FeedService", "FileUpload", "BroadcastService", "Utils", "FeedTagService", "DomainTypesService"]

    constructor(private $rootScope: any, private $scope: any, private $http: any, private $timeout: any, private $mdToast: any, private $filter: any, private $mdDialog: any
        , private $mdExpansionPanel: any, private restUrlService: any, private feedService: any, private fileUpload: any, private broadcastService: any, private utils: any, private feedTagService: any,
                private domainTypesService: any) {

        this.expansionPanelHelper = new ExpansionPanelHelper((this.$mdExpansionPanel));

        this.model = feedService.createFeedModel;
        this.addComplexDataTypes()
        this.tableForm = new TableForm(this.model);

        domainTypesService.findAll().then((domainTypes: DomainType[]) => {
            this.availableDomainTypes = domainTypes;
        });
        this.ensurePartitionData();
        broadcastService.subscribe($scope, 'DATA_TRANSFORM_SCHEMA_LOADED', this.onDataTransformSchemaLoaded.bind(this));



        var invalidColumnsWatch = $scope.$watch(() => {
            return this.tableForm.defineFeedTableForm.invalidColumns
        }, (newVal: any) => {
            // console.log("watching this.defineFeedTableForm.invalidColumns");
            this.isValid = this.tableForm.validate(undefined);
        }, true);

        var tableMethodWatch = $scope.$watch(() => {
            return this.model.table.method;
        }, (newVal: any) => {
            // console.log("watching model.table.method");
            this.model.table.method = newVal;
            this.calcTableState();
        });

        //Set the Table Name to be the System Feed Name
        var systemFeedNameWatch = $scope.$watch(() => {
            return this.model.systemFeedName;
        }, (newVal: any) => {
            this.model.table.tableSchema.name = newVal;
        });

        /**
         * Ensure the form is valid
         * @type {*|function()}
         */
        var formValidWatch = $scope.$watch(() => {
            return this.tableForm.defineFeedTableForm.$valid;
        }, (newVal: any) => {
            if (newVal === true) {
                this.isValid = this.tableForm.validate(newVal);
            }
            else {
                this.isValid = false;
            }

        });

        var sampleFileWatch = $scope.$watch(() => {
            return this.sampleFile;
        }, (newVal: any) => {
            if (newVal == null) {
                angular.element('#upload-sample-file-btn').removeClass('md-primary');
            }
            else {
                angular.element('#upload-sample-file-btn').addClass('md-primary');
            }
        });

        $scope.$on('$destroy', () => {
            systemFeedNameWatch();
            invalidColumnsWatch();
            formValidWatch();
            tableMethodWatch();
            sampleFileWatch();
        });
    }

    $onInit() {
        this.ngOnInit();
    }

    ngOnInit() {
        this.stepNumber = parseInt(this.stepIndex) + 1;
        if (this.model.sampleFile) {
            this.sampleFile = this.model.sampleFile;
        }
        //attach the schema parser options if theey were saved on the model
        if (this.model.schemaParser) {
            this.schemaParser = this.model.schemaParser;
        }


       // this.$scope.$evalAsync(() => {
            this.calcTableState();
            if (this.model.table.tableSchema.fields && this.model.table.tableSchema.fields.length > 0) {
                if (this.model.dataTransformationFeed) {
                    this.addComplexDataTypes();
                }
                this.syncFeedsColumns();
                this.expansionPanelHelper.expandSchemaPanel();
            }else {
                if (!this.model.dataTransformationFeed) {
                    this.expansionPanelHelper.expandChooseMethodPanel();
                }
            }


        // Retrieve partition formulas
        this.feedService.getPartitionFunctions()
            .then((functions: any) => {
                this.partitionFormulas = functions;
            });


        this.isValid = this.tableForm.validate(undefined);


    }


    /**
     * Called when the Method radio option is changed
     */
    updateSelectedMethod(method: string) {
        if (method == 'MANUAL') {
            this.model.allowSkipHeaderOption = true;
        }
    }


    /**
     * Adding a new Column to the schema
     * This is called both when the user clicks the "Add Field" button or when the sample file is uploaded
     * If adding from the UI the {@code columnDef} will be null, otherwise it will be the parsed ColumnDef from the sample file
     * @param columnDef
     */
    addColumn(columnDef: TableColumnDefinition, syncFieldPolicies?: boolean) {
        // console.log("addColumn");
        if (columnDef == null) {
            columnDef = this.feedService.newTableFieldDefinition();
        }

        // when adding a new column this is also called to synchronize the field policies array with the columns
        let policy = this.feedService.newTableFieldPolicy();


        if (columnDef.sampleValues != null && columnDef.sampleValues.length > 0) {
            columnDef.selectedSampleValue = columnDef.sampleValues[0];
        } else {
            columnDef.selectedSampleValue = null;
        }

        if (this.useUnderscoreInsteadOfSpaces) {
            columnDef.name = StringUtils.replaceSpaces(columnDef.name, '_');
        }
        columnDef.initFeedColumn();
        //add the column to both the source and destination tables as well as the fieldPolicies array
        this.model.table.tableSchema.fields.push(columnDef);
        this.model.table.fieldPolicies.push(policy);
        this.model.table.sourceTableSchema.fields.push(this.feedService.newTableFieldDefinition());
        this.tableForm.validateColumn(columnDef);
        if (syncFieldPolicies == undefined || syncFieldPolicies == true) {
            this.feedService.syncTableFieldPolicyNames();
        }
    };

    undoColumn(index: number) {
        var columnDef = <TableColumnDefinition> this.model.table.tableSchema.fields[index];
        columnDef.history.pop();
        let prevValue = columnDef.history[columnDef.history.length - 1];
        columnDef.undo(prevValue);
        this.tableForm.validateColumn(columnDef);
        this.tableForm.partitionNamesUnique();
        this.feedService.syncTableFieldPolicyNames();
        this.isValid = this.tableForm.validate(undefined);
    };

    /**
     * Remove a column from the schema
     * @param index
     */
    removeColumn(index: number) {
        var columnDef = <TableColumnDefinition> this.model.table.tableSchema.fields[index];
        columnDef.deleteColumn();

        //remove any partitions using this field
        this.model.table.partitions
            .filter((partition: any) => {
                return partition.columnDef.name === columnDef.name;
            })
            .map((partition: any) => {
                return partition._id;
            })
            .forEach((id: any) => {
                var index = this.model.table.partitions.findIndex((partition: any) => {
                    return partition._id === id;
                });
                if (index > -1) {
                    this.removePartitionField(index);
                }
            });

        //ensure the field names on the columns are unique again as removing a column might fix a "notUnique" error
        this.tableForm.validateColumn(columnDef);
        this.tableForm.partitionNamesUnique();
        this.isValid = this.tableForm.validate(undefined);
    };

    /**
     * Removes the column matching the passed in {@code columnDef} with the array of columns
     * @param columnDef
     */
    removeColumnUsingReference(columnDef: TableColumnDefinition) {
        var idx = _.indexOf(this.model.table.tableSchema.fields, columnDef)
        if (idx >= 0) {
            this.removeColumn(idx);
        }
    };

    /**
     * Add a partition to the schema
     * This is called from the UI when the user clicks "Add Partition"
     */
    addPartitionField() {
        var partitionLength = this.model.table.partitions.length;
        var partition = TableFieldPartition.atPosition(partitionLength);
        this.model.table.partitions.push(partition);
    };

    /**
     * Remove the partition from the schecma
     * @param index
     */
    removePartitionField(index: number) {
        this.model.table.partitions.splice(index, 1);
        this.tableForm.partitionNamesUnique();
    };


    onSelectedColumn(selectedColumn: TableColumnDefinition) {
        var firstSelection = this.selectedColumn == null;
        this.selectedColumn = selectedColumn;
        // Show an item in dropdown
        if (this.selectedColumn.selectedSampleValue == null && this.selectedColumn.sampleValues.length > 0) {
            this.selectedColumn.selectedSampleValue = this.selectedColumn.sampleValues[0];
        }
        if (firstSelection) {
            //trigger scroll to stick the selection to the screen
            this.utils.waitForDomElementReady('#selectedColumnPanel', () => {
                angular.element('#selectedColumnPanel').triggerHandler('stickIt');
            })
        }

        // Ensure tags is an array
        if (angular.isUndefined(selectedColumn.tags) ) {
            selectedColumn.tags = [];
        }
    };

    onPrecisionChange(columnDef: TableColumnDefinition) {
        this.tableForm.validateColumn(columnDef);
        this.onFieldChange(columnDef);
    };

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
        _.each(this.model.table.partitions, (partition: TableFieldPartition) => {
            if (partition.columnDef == columnDef) {
                partition.syncSource();
                partition.updateFieldName();
            }
        });
        this.tableForm.validateColumn(columnDef);
        this.tableForm.partitionNamesUnique();
        this.feedService.syncTableFieldPolicyNames();

        // Check if column data type matches domain data type
        var policy = <TableFieldPolicy>this.model.table.fieldPolicies[index];
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
                    templateUrl: "../../../shared/apply-domain-type/domain-type-conflict.component.html",
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
        var formulas = this.$filter('filterPartitionFormula')(this.partitionFormulas, partition);
        if (formulas.length == 1) {
            partition.formula = formulas[0];
        }
        partition.updateFieldName();

        this.tableForm.partitionNamesUnique();

    }

    /**
     * When a partition formula changes it needs to
     *  - attempt to prefill in the name with some default name.  if its a val formula it will default the partition name to the source field name and leave it disabled
     * @param partition
     */
    onPartitionFormulaChange(partition: TableFieldPartition) {
        partition.updateFieldName();
        this.tableForm.partitionNamesUnique();
    }

    /**
     * when the partition name changes it needs to
     *  - ensure the names are unique
     *  - ensure no dups (cannot have more than 1 partitoin on the same col/formula
     * @param partition
     */
    onPartitionNameChange(partition: any) {
        partition.replaceSpaces();
        this.tableForm.partitionNamesUnique();
    };

    /**
     * User uploads a sample file.
     * resets the columns, applys fields to the table
     */
    uploadSampleFile() {
        this.uploadBtnDisabled = true;
        this.showProgress();
        var file = this.sampleFile;
        var params = {};
        if (this.schemaParser) {
            params = {parser: JSON.stringify(this.schemaParser)};
        }
        //Store the Schema Parser option on the model so they can be loaded when returning back to this step
        this.model.schemaParser = this.schemaParser;
        var uploadUrl = this.restUrlService.UPLOAD_SAMPLE_TABLE_FILE;
        var successFn = (response: any) => {

            var responseData = response.data;
            this.resetColumns();
            this.availableDefinitionDataTypes = this.feedService.columnDefinitionDataTypes.slice();
            angular.forEach(responseData.fields, (field) => {
                var col = this.feedService.newTableFieldDefinition();
                col = angular.extend(col, field)
                // add exotic data type to available columns if needed
                if ($.inArray(col.derivedDataType, this.availableDefinitionDataTypes) == -1) {
                    this.availableDefinitionDataTypes.push(col.derivedDataType);
                }
                this.addColumn(col, false);
            });
            this.feedService.syncTableFieldPolicyNames();
            this.applyDomainTypes();
            //set the feedFormat property
            this.model.table.feedFormat = responseData.hiveFormat;
            this.model.table.structured = responseData.structured;
            this.model.table.feedTblProperties = responseData.serdeTableProperties;
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
            this.expansionPanelHelper.collapseMethodAndExpandSchemaPanel();

            this.isValid = this.tableForm.validate(undefined);
            angular.element('#upload-sample-file-btn').removeClass('md-primary');

            this.$timeout(() => this.tableForm.touchErrorFields(), 2000);
        };
        var errorFn = (data: any) => {
            //clear the schemaParser options
            this.model.schemaParser = undefined;
            this.hideProgress();
            this.uploadBtnDisabled = false;
            angular.element('#upload-sample-file-btn').removeClass('md-primary');
            angular.element('#uploadButton').addClass('md-primary');
        };
        //clear partitions
        while (this.model.table.partitions.length) {
            this.model.table.partitions.pop();
        }
        this.fileUpload.uploadFileToUrl(file, uploadUrl, successFn, errorFn, params);
    }


    private onFieldChange(columnDef: TableColumnDefinition) {
        this.selectedColumn = columnDef;
        columnDef.changeColumn();
    }


    /**
     * Transforms the specified chip into a tag.
     * @param {string} chip the chip
     * @returns {Object} the tag
     */
    transformTagChip(chip: any) {
        return angular.isObject(chip) ? chip : {name: chip};
    };

    private showProgress() {
        if (this.stepperController) {
            this.stepperController.showProgress = true;
        }
    }

    private hideProgress() {
        if (this.stepperController) {
            this.stepperController.showProgress = false;
        }
    }

    private resetColumns() {
        this.model.table.tableSchema.fields = [];
        this.model.table.fieldPolicies = [];
        this.tableForm.defineFeedTableForm.invalidColumns = [];
    }


    /**
     * Ensure that for the partitions the sourceField and sourceDataTypes match the respective schema field data
     */
    private ensurePartitionData() {
        _.each(this.model.table.partitions, (partition: TableFieldPartition) => {
            if (partition.columnDef == undefined) {
                var columnDef = this.feedService.getColumnDefinitionByName(partition.sourceField);
                if (columnDef != null) {
                    partition.columnDef = columnDef;
                }
            }
            partition.syncSource();

        });

    }


    private addComplexDataTypes() {
        this.availableDefinitionDataTypes = this.feedService.columnDefinitionDataTypes.slice();
        angular.forEach(this.model.table.tableSchema.fields, (field) => {
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

        this.model.table.tableSchema.fields.forEach((field: TableColumnDefinition, index: number) => {
            var domainType = this.domainTypesService.detectDomainType(field, this.availableDomainTypes);
            if (domainType !== null) {
                if (this.domainTypesService.matchesField(domainType, field)) {
                    // Domain type can be applied immediately
                    this.feedService.setDomainTypeForField(field, this.model.table.fieldPolicies[index], domainType);
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
        if (data.fields.length > 0) {
            this.$mdDialog.show({
                controller: "ApplyTableDomainTypesDialog",
                escapeToClose: false,
                fullscreen: true,
                parent: angular.element(document.body),
                templateUrl: "../../../shared/apply-domain-type/apply-table-domain-types.component.html",
                locals: {
                    data: data
                }
            })
                .then((selected: any) => {
                    selected.forEach((selection: any) => {
                        var fieldIndex = data.fields.findIndex((element: any) => {
                            return element.name === selection.name;
                        });
                        var policyIndex = this.model.table.tableSchema.fields.findIndex((element: any) => {
                            return element.name === selection.name;
                        });
                        this.feedService.setDomainTypeForField(data.fields[fieldIndex], this.model.table.fieldPolicies[policyIndex], data.domainTypes[fieldIndex]);
                        data.fields[fieldIndex].history = [];
                        let columnDef = <TableColumnDefinition> data.fields[fieldIndex];
                        columnDef.addHistoryItem();
                    });
                }, () => {
                    // ignore cancel
                });
        }
    }


    /**
     * Called when a user transitions from the Wrangler to this step
     */
    private onDataTransformSchemaLoaded() {
        this.syncFeedsColumns();
        this.isValid = this.tableForm.validate(undefined);
        if (angular.isDefined(this.model.schemaChanged) && this.model.schemaChanged == true) {
            this.isValid = false;
            this.$mdDialog.show(
                this.$mdDialog.alert()
                    .parent(angular.element(document.body))
                    .clickOutsideToClose(true)
                    .title('Table Schema Changed')
                    .htmlContent('The table schema no longer matches the schema previously defined. <br/><br/> This is invalid.  If you wish to modify the underlying schema <br/> (i.e. change some column names and/or types) please clone<br/> the feed as a new feed instead.')
                    .ariaLabel('Table Schema Changed ')
                    .ok('Got it!')
            );
        }
            this.addComplexDataTypes();
            this.calcTableState();
            this.expansionPanelHelper.expandSchemaPanel();

        this.isValid = this.tableForm.validate(undefined);

    }

    /**
     * Set the table states for locks
     */
    private calcTableState() {
        this.tableLocked = angular.isDefined(this.tableLocked) && (this.tableLocked == true );
        this.dataTypeLocked = angular.isDefined(this.dataTypeLocked) && (this.dataTypeLocked == true );
        this.canRemoveFields = angular.isUndefined(this.canRemoveFields) || this.canRemoveFields === true ;
        this.showMethodPanel = (this.model.table.method != 'EXISTING_TABLE');
    }

    /*
    Create columns for tracking changes between original source and the target table schema
     */
    private syncFeedsColumns() {
        let convertFieldsToObjects = false;
        if(this.model.table.tableSchema.fields.length >0){
          convertFieldsToObjects = !ObjectUtils.isType(this.model.table.tableSchema.fields[0],TableColumnDefinition.OBJECT_TYPE);
        }
        if(convertFieldsToObjects){
          let convertedFields:TableColumnDefinition[] = this.model.table.tableSchema.fields.map((columnDef:any) => ObjectUtils.getAs(columnDef,TableColumnDefinition));
            this.model.table.tableSchema.fields = convertedFields;
        }

        _.each(this.model.table.tableSchema.fields, (columnDef: TableColumnDefinition) => {
            columnDef.initFeedColumn()
        });
    }


}

class FilterPartitionFormulaPipe implements PipeTransform{
    constructor(private FeedService:any){

    }
    transform(formulas:any, partition:any){
        // Find column definition
        var columnDef = (partition && partition.sourceField) ? this.FeedService.getColumnDefinitionByName(partition.sourceField) : null;
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

angular.module(moduleName).filter("filterPartitionFormula", ["FeedService", (FeedService:any) => {
    const pipe = new FilterPartitionFormulaPipe(FeedService);
    return pipe.transform.bind(pipe);
}]);

angular.module(moduleName).
    component("thinkbigDefineFeedTable", {
        bindings: {
            canRemoveFields: "<?",
            stepIndex: '@',
            tableLocked: "<?",
            dataTypeLocked: "<?typeLocked"
        },
        require: {
            stepperController: "^thinkbigStepper"
        },
        controllerAs: 'vm',
        controller: DefineFeedTableController,
        templateUrl: './define-feed-table.html',
    });
