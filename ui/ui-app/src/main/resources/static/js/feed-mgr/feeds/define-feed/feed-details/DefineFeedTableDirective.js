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
define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/feeds/define-feed/module-name');
    var DefineFeedTableController = /** @class */ (function () {
        function DefineFeedTableController($rootScope, $scope, $http, $timeout, $mdToast, $filter, $mdDialog, $mdExpansionPanel, RestUrlService, FeedService, FileUpload, BroadcastService, Utils, FeedTagService, DomainTypesService) {
            var _this = this;
            this.$rootScope = $rootScope;
            this.$scope = $scope;
            this.$http = $http;
            this.$timeout = $timeout;
            this.$mdToast = $mdToast;
            this.$filter = $filter;
            this.$mdDialog = $mdDialog;
            this.$mdExpansionPanel = $mdExpansionPanel;
            this.RestUrlService = RestUrlService;
            this.FeedService = FeedService;
            this.FileUpload = FileUpload;
            this.BroadcastService = BroadcastService;
            this.Utils = Utils;
            this.FeedTagService = FeedTagService;
            this.DomainTypesService = DomainTypesService;
            this.defineFeedTableForm = {};
            this.stepperController = null;
            this.isValid = false;
            this.sampleFile = null;
            this.schemaParser = {};
            this.useUnderscoreInsteadOfSpaces = true;
            this.selectedColumn = null;
            this.fieldNamesUniqueRetryAmount = 0;
            this.showMethodPanel = true;
            this.showTablePanel = true;
            this.uploadBtnDisabled = false;
            this.partitionFormulas = [];
            this.feedFormat = '';
            /**
             * List of available domain types.
             * @type {DomainType[]}
             */
            this.availableDomainTypes = [];
            this.NAME_PATTERN = /^[a-zA-Z0-9_\s\)\(-]*$/;
            this.PRECISION_SCALE_PATTERN = /^\d+,\d+$/;
            this.MAX_COLUMN_LENGTH = 767;
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
            this.tableCreateMethods = [{ type: 'MANUAL', name: 'Manual' }, { type: 'SAMPLE_FILE', name: 'Sample File' }];
            this.model = FeedService.createFeedModel;
            this.availableDefinitionDataTypes = FeedService.columnDefinitionDataTypes.slice();
            /**
             * Provides a list of available tags.
             * @type {FeedTagService}
             */
            this.feedTagService = FeedTagService;
            this.tagChips = { searchText: null, selectedItem: null };
            DomainTypesService.findAll().then(function (domainTypes) {
                _this.availableDomainTypes = domainTypes;
            });
            $scope.$evalAsync(function () {
                // console.log("$scope.$evalAsync");
                _this.calcTableState();
                if (_this.model.table.tableSchema.fields && _this.model.table.tableSchema.fields.length > 0) {
                    _this.syncFeedsColumns();
                    _this.calcTableState();
                    _this.expandSchemaPanel();
                }
            });
            BroadcastService.subscribe($scope, 'DATA_TRANSFORM_SCHEMA_LOADED', function () {
                _this.syncFeedsColumns();
                _this.validate(undefined);
            });
            this.invalidColumnsWatch = $scope.$watch(function () {
                return _this.defineFeedTableForm.invalidColumns;
            }, function (newVal) {
                // console.log("watching this.defineFeedTableForm.invalidColumns");
                _this.validate(undefined);
            }, true);
            this.tableMethodWatch = $scope.$watch(function () {
                return _this.model.table.method;
            }, function (newVal) {
                // console.log("watching model.table.method");
                _this.model.table.method = newVal;
                _this.calcTableState();
            });
            //Set the Table Name to be the System Feed Name
            this.systemFeedNameWatch = $scope.$watch(function () {
                return _this.model.systemFeedName;
            }, function (newVal) {
                _this.model.table.tableSchema.name = newVal;
            });
            /**
             * Ensure the form is valid
             * @type {*|function()}
             */
            this.formValidWatch = $scope.$watch(function () {
                return _this.defineFeedTableForm.$valid;
            }, function (newVal) {
                // console.log("watching this.defineFeedTableForm.$valid, newVal = " + newVal);
                if (newVal === true) {
                    _this.validate(newVal);
                }
                else {
                    _this.isValid = false;
                }
            });
            this.sampleFileWatch = $scope.$watch(function () {
                return _this.sampleFile;
            }, function (newVal) {
                if (newVal == null) {
                    angular.element('#upload-sample-file-btn').removeClass('md-primary');
                }
                else {
                    angular.element('#upload-sample-file-btn').addClass('md-primary');
                }
            });
            // Retrieve partition formulas
            this.FeedService.getPartitionFunctions()
                .then(function (functions) {
                _this.partitionFormulas = functions;
            });
            this.validate(undefined);
            this.expanded = false;
            this.calcTableState();
        }
        DefineFeedTableController.prototype.$onInit = function () {
            this.ngOnInit();
        };
        DefineFeedTableController.prototype.$onDestroy = function () {
            this.ngOnDestroy();
        };
        DefineFeedTableController.prototype.ngOnInit = function () {
            this.totalSteps = this.stepperController.totalSteps;
            this.stepNumber = parseInt(this.stepIndex) + 1;
            // choose to expand the choose method initially if no fields have been defined yet
            if (this.model.table.tableSchema.fields.length == 0) {
                this.expandChooseMethodPanel();
            }
            else {
                this.expandSchemaPanel();
            }
        };
        DefineFeedTableController.prototype.ngOnDestroy = function () {
            this.systemFeedNameWatch();
            this.invalidColumnsWatch();
            this.formValidWatch();
            this.tableMethodWatch();
            this.sampleFileWatch();
        };
        DefineFeedTableController.prototype.isSchemaPanelExpanded = function () {
            return this.expanded;
        };
        /*
         Collapse the file picker section
         */
        DefineFeedTableController.prototype.collapseMethodPanel = function () {
            this.$mdExpansionPanel().waitFor('panelOne').then(function (instance) {
                instance.collapse();
            });
        };
        ;
        /*
         Expand the schema panel
         */
        DefineFeedTableController.prototype.expandSchemaPanel = function () {
            var _this = this;
            // console.log("expandSchemaPanel");
            this.$mdExpansionPanel().waitFor('panelTwo').then(function (instance) {
                // console.log("expanding schema panel");
                instance.expand();
                _this.expanded = true;
            });
        };
        ;
        DefineFeedTableController.prototype.expandChooseMethodPanel = function () {
            this.$mdExpansionPanel().waitFor('panelOne').then(function (instance) {
                instance.expand();
            });
        };
        ;
        DefineFeedTableController.prototype.uploadSampleFile = function () {
            var _this = this;
            this.uploadBtnDisabled = true;
            this.showProgress();
            var file = this.sampleFile;
            var params = {};
            if (this.schemaParser) {
                params = { parser: JSON.stringify(this.schemaParser) };
            }
            var uploadUrl = this.RestUrlService.UPLOAD_SAMPLE_TABLE_FILE;
            var successFn = function (response) {
                // console.log("loaded schema");
                var responseData = response.data;
                _this.resetColumns();
                _this.availableDefinitionDataTypes = _this.FeedService.columnDefinitionDataTypes.slice();
                angular.forEach(responseData.fields, function (field) {
                    var col = _this.newColumnDefinition();
                    col = angular.extend(col, field);
                    // add exotic data type to available columns if needed
                    if ($.inArray(col.derivedDataType, _this.availableDefinitionDataTypes) == -1) {
                        _this.availableDefinitionDataTypes.push(col.derivedDataType);
                    }
                    _this.addColumn(col, false);
                });
                _this.FeedService.syncTableFieldPolicyNames();
                _this.applyDomainTypes();
                //set the feedFormat property
                _this.model.table.feedFormat = responseData.hiveFormat;
                _this.model.table.structured = responseData.structured;
                if (_this.schemaParser.allowSkipHeader) {
                    _this.model.allowSkipHeaderOption = true;
                    _this.model.options.skipHeader = true;
                }
                else {
                    _this.model.allowSkipHeaderOption = false;
                    _this.model.options.skipHeader = false;
                }
                _this.hideProgress();
                _this.uploadBtnDisabled = false;
                _this.syncFeedsColumns();
                _this.calcTableState();
                _this.collapseMethodPanel();
                _this.expandSchemaPanel();
                _this.validate(undefined);
                angular.element('#upload-sample-file-btn').removeClass('md-primary');
                _this.$timeout(_this.touchErrorFields, 2000);
            };
            var errorFn = function (data) {
                _this.hideProgress();
                _this.uploadBtnDisabled = false;
                angular.element('#upload-sample-file-btn').removeClass('md-primary');
                angular.element('#uploadButton').addClass('md-primary');
            };
            //clear partitions
            while (this.model.table.partitions.length) {
                this.model.table.partitions.pop();
            }
            this.FileUpload.uploadFileToUrl(file, uploadUrl, successFn, errorFn, params);
        };
        /**
         * Detects and applies domain types to all columns.
         */
        DefineFeedTableController.prototype.applyDomainTypes = function () {
            var _this = this;
            // Detect domain types
            var data = { domainTypes: [], fields: [] };
            this.model.table.tableSchema.fields.forEach(function (field, index) {
                var domainType = _this.DomainTypesService.detectDomainType(field, _this.availableDomainTypes);
                if (domainType !== null) {
                    if (_this.DomainTypesService.matchesField(domainType, field)) {
                        // Domain type can be applied immediately
                        _this.FeedService.setDomainTypeForField(field, _this.model.table.fieldPolicies[index], domainType);
                        field.history = [];
                        _this.addHistoryItem(field);
                    }
                    else {
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
                    .then(function (selected) {
                    selected.forEach(function (selection) {
                        var fieldIndex = data.fields.findIndex(function (element) {
                            return element.name === selection.name;
                        });
                        var policyIndex = _this.model.table.tableSchema.fields.findIndex(function (element) {
                            return element.name === selection.name;
                        });
                        _this.FeedService.setDomainTypeForField(data.fields[fieldIndex], _this.model.table.fieldPolicies[policyIndex], data.domainTypes[fieldIndex]);
                        data.fields[fieldIndex].history = [];
                        _this.addHistoryItem(data.fields[fieldIndex]);
                    });
                }, function () {
                    // ignore cancel
                });
            }
        };
        DefineFeedTableController.prototype.touchErrorFields = function () {
            var errors = this.defineFeedTableForm.$error;
            for (var key in errors) {
                if (errors.hasOwnProperty(key)) {
                    var errorFields = errors[key];
                    angular.forEach(errorFields, function (errorField) {
                        errorField.$setTouched();
                    });
                }
            }
        };
        /**
        * Transforms the specified chip into a tag.
        * @param {string} chip the chip
        * @returns {Object} the tag
        */
        DefineFeedTableController.prototype.transformChip = function (chip) {
            return angular.isObject(chip) ? chip : { name: chip };
        };
        DefineFeedTableController.prototype.calcTableState = function () {
            // console.log("calcTableState");
            this.tableLocked = angular.isDefined(this.tableLocked) && (this.tableLocked === true || this.tableLocked === "true");
            this.dataTypeLocked = angular.isDefined(this.dataTypeLocked) && (this.typeLocked === true || this.typeLocked === "true");
            this.canRemoveFields = angular.isUndefined(this.canRemoveFields) || this.canRemoveFields === true || this.canRemoveFields === "true";
            this.showMethodPanel = (this.model.table.method != 'EXISTING_TABLE');
            this.showTablePanel = (this.model.table.tableSchema.fields.length > 0);
        };
        ;
        /*
        Create columns for tracking changes between original source and the target table schema
         */
        DefineFeedTableController.prototype.syncFeedsColumns = function () {
            var _this = this;
            // console.log("syncFeedsColumns");
            _.each(this.model.table.tableSchema.fields, function (columnDef) {
                _this.initFeedColumn(columnDef);
            });
        };
        DefineFeedTableController.prototype.initFeedColumn = function (columnDef) {
            // console.log('init feed column');
            if (columnDef.origName == undefined) {
                columnDef.origName = columnDef.name;
                columnDef.origDataType = columnDef.derivedDataType;
                columnDef.deleted = false;
                columnDef._id = _.uniqueId();
                columnDef.history = [];
                this.addHistoryItem(columnDef);
            }
        };
        /*
        Adds record of changed state
         */
        DefineFeedTableController.prototype.createHistoryRecord = function (columnDef) {
            return { name: columnDef.name, derivedDataType: columnDef.derivedDataType, precisionScale: columnDef.precisionScale, deleted: columnDef.deleted, primaryKey: columnDef.primaryKey, updatedTracker: columnDef.updatedTracker, createdTracker: columnDef.createdTracker };
        };
        /**
         * Called wehn the Method radio option is changed
         */
        DefineFeedTableController.prototype.updateSelectedMethod = function (method) {
            if (method == 'MANUAL') {
                this.model.allowSkipHeaderOption = true;
            }
        };
        ;
        DefineFeedTableController.prototype.addHistoryItem = function (columnDef) {
            var historyItem = this.createHistoryRecord(columnDef);
            columnDef.history.push(historyItem);
        };
        ;
        /**
         * when adding a new column
         * @returns {*|{name, description, dataType, precisionScale, dataTypeDisplay, primaryKey, nullable, sampleValues, selectedSampleValue, isValid, _id}}
         */
        DefineFeedTableController.prototype.newColumnDefinition = function () {
            // console.log('new column def');
            return this.FeedService.newTableFieldDefinition();
        };
        /**
         * when adding a new column this is also called to synchronize the field policies array with the columns
         * @returns {*|{name, partition, profile, standardization, validation}}
         */
        DefineFeedTableController.prototype.newColumnPolicy = function () {
            return this.FeedService.newTableFieldPolicy();
        };
        /**
         * When adding a new Partition
         * @param index
         * @returns {{position: *, field: string, sourceField: string, formula: string, sourceDataType: string, _id: *}}
         */
        DefineFeedTableController.prototype.newPartitionField = function (index) {
            return { position: index, field: '', sourceField: '', formula: '', sourceDataType: '', _id: _.uniqueId() };
        };
        DefineFeedTableController.prototype.replaceSpaces = function (str) {
            if (str != undefined) {
                return str.replace(/ /g, '_');
            }
            else {
                return '';
            }
        };
        DefineFeedTableController.prototype.showProgress = function () {
            if (this.stepperController) {
                this.stepperController.showProgress = true;
            }
        };
        DefineFeedTableController.prototype.hideProgress = function () {
            if (this.stepperController) {
                this.stepperController.showProgress = false;
            }
        };
        DefineFeedTableController.prototype.resetColumns = function () {
            // console.log("resetColumns");
            this.model.table.tableSchema.fields = [];
            this.model.table.fieldPolicies = [];
            this.defineFeedTableForm.invalidColumns = [];
        };
        /**
         * Ensure the Partition Names are unique
         * If Not add a angular error
         */
        DefineFeedTableController.prototype.partitionNamesUnique = function () {
            // console.log("partitionNamesUnique");
            var _this = this;
            // Validate the Partition names are unique respective to other partition names
            _.chain(this.model.table.partitions).groupBy(function (partition) {
                return partition.field;
            }).each(function (group, name) {
                if (group.length > 1) {
                    _.each(group, function (partition) {
                        //.invalid another partition matches the same name
                        _this.defineFeedTableForm['partition_name' + partition._id].$setValidity('notUnique', false);
                    });
                }
                else {
                    _.each(group, function (partition) {
                        //valid this is a unique partition name
                        _this.defineFeedTableForm['partition_name' + partition._id].$setValidity('notUnique', true);
                    });
                }
            });
            //Validate the Partition names are unique respective to the other fields
            //an array of column names
            var columnNames = _.map(this.model.table.tableSchema.fields, function (columnDef) {
                return columnDef.name;
            });
            var countPartitionNames = {};
            //add the angular errors
            _.each(this.model.table.partitions, function (partition) {
                if (partition.formula != undefined && partition.formula != 'val' && _.indexOf(columnNames, partition.field) >= 0) {
                    _this.defineFeedTableForm['partition_name' + partition._id].$setValidity('notUnique', false);
                }
            });
        };
        /**
         * Adding a new Column to the schema
         * This is called both when the user clicks the "Add Field" button or when the sample file is uploaded
         * If adding from the UI the {@code columnDef} will be null, otherwise it will be the parsed ColumnDef from the sample file
         * @param columnDef
         */
        DefineFeedTableController.prototype.addColumn = function (columnDef, syncFieldPolicies) {
            // console.log("addColumn");
            if (columnDef == null) {
                columnDef = this.newColumnDefinition();
            }
            // Detect domain type and select sample value
            var policy = this.newColumnPolicy();
            if (columnDef.sampleValues != null && columnDef.sampleValues.length > 0) {
                columnDef.selectedSampleValue = columnDef.sampleValues[0];
            }
            else {
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
        };
        DefineFeedTableController.prototype.undoColumn = function (index) {
            // console.log("undoColumn");
            var columnDef = this.model.table.tableSchema.fields[index];
            columnDef.history.pop();
            var prevValue = columnDef.history[columnDef.history.length - 1];
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
        };
        /**
         * Remove a column from the schema
         * @param index
         */
        DefineFeedTableController.prototype.removeColumn = function (index) {
            var _this = this;
            // console.log("removeColumn");
            var columnDef = this.model.table.tableSchema.fields[index];
            columnDef.deleted = true;
            this.addHistoryItem(columnDef);
            //remove any partitions using this field
            this.model.table.partitions
                .filter(function (partition) {
                return partition.columnDef.name === columnDef.name;
            })
                .map(function (partition) {
                return partition._id;
            })
                .forEach(function (id) {
                var index = _this.model.table.partitions.findIndex(function (partition) {
                    return partition._id === id;
                });
                if (index > -1) {
                    _this.removePartitionField(index);
                }
            });
            //ensure the field names on the columns are unique again as removing a column might fix a "notUnique" error
            this.validateColumn(columnDef);
            this.partitionNamesUnique();
            this.validate(undefined);
        };
        /**
         * Removes the column matching the passed in {@code columnDef} with the array of columns
         * @param columnDef
         */
        DefineFeedTableController.prototype.removeColumnUsingReference = function (columnDef) {
            var idx = _.indexOf(this.model.table.tableSchema.fields, columnDef);
            if (idx >= 0) {
                this.removeColumn(idx);
            }
        };
        /**
         * Add a partition to the schema
         * This is called from the UI when the user clicks "Add Partition"
         */
        DefineFeedTableController.prototype.addPartitionField = function () {
            var partitionLength = this.model.table.partitions.length;
            var partition = this.newPartitionField(partitionLength);
            this.model.table.partitions.push(partition);
        };
        /**
         * Remove the partition from the schecma
         * @param index
         */
        DefineFeedTableController.prototype.removePartitionField = function (index) {
            this.model.table.partitions.splice(index, 1);
            this.partitionNamesUnique();
        };
        DefineFeedTableController.prototype.getSelectedColumn = function () {
            return this.selectedColumn;
        };
        DefineFeedTableController.prototype.onSelectedColumn = function (selectedColumn) {
            var firstSelection = this.selectedColumn == null;
            this.selectedColumn = selectedColumn;
            // Show an item in dropdown
            if (this.selectedColumn.selectedSampleValue == null && this.selectedColumn.sampleValues.length > 0) {
                this.selectedColumn.selectedSampleValue = this.selectedColumn.sampleValues[0];
            }
            if (firstSelection) {
                //trigger scroll to stick the selection to the screen
                this.Utils.waitForDomElementReady('#selectedColumnPanel', function () {
                    angular.element('#selectedColumnPanel').triggerHandler('stickIt');
                });
            }
            // Ensure tags is an array
            if (!angular.isArray(selectedColumn.tags)) {
                selectedColumn.tags = [];
            }
        };
        DefineFeedTableController.prototype.onPrecisionChange = function (columnDef) {
            this.validateColumn(columnDef);
            this.onFieldChange(columnDef);
        };
        /**
         * When the schema field changes it needs to
         *  - ensure the names are unique
         *  - update the respective partition names if there is a partition on the field with the 'val' formula
         *  - ensure that partition names are unique since the new field name could clash with an existing partition
         * @param columnDef
         */
        DefineFeedTableController.prototype.onNameFieldChange = function (columnDef, index) {
            // console.log("onNameFieldChange, columnDef", columnDef);
            var _this = this;
            if (this.useUnderscoreInsteadOfSpaces) {
                columnDef.name = this.replaceSpaces(columnDef.name);
            }
            if (columnDef.derivedDataType !== 'decimal') {
                columnDef.precisionScale = null;
            }
            this.onFieldChange(columnDef);
            //update the partitions with "val" on this column so the name matches
            _.each(this.model.table.partitions, function (partition) {
                if (partition.columnDef == columnDef) {
                    partition.sourceDataType = columnDef.derivedDataType;
                    partition.sourceField = columnDef.name;
                    _this.updatePartitionFieldName(partition);
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
                        .then(function (keep) {
                        if (keep) {
                            columnDef.$allowDomainTypeConflict = true;
                        }
                        else {
                            delete policy.$currentDomainType;
                            delete policy.domainTypeId;
                        }
                    }, function () {
                        _this.undoColumn(index);
                    });
                }
            }
        };
        DefineFeedTableController.prototype.isDeleted = function (columnDef) {
            return columnDef.deleted === true;
        };
        DefineFeedTableController.prototype.isInvalid = function (columnDef) {
            var _this = this;
            var errorCount = 0;
            var columnDefFields = _.values(columnDef.validationErrors);
            _.each(columnDefFields, function (columnDefField) {
                errorCount += _this.fieldErrorCount(columnDefField);
            });
            return errorCount > 0;
        };
        DefineFeedTableController.prototype.fieldErrorCount = function (columnDefField) {
            var errorTypes = _.values(columnDefField);
            var errors = _.filter(errorTypes, function (errorType) {
                return errorType === true;
            });
            return errors === undefined ? 0 : errors.length;
        };
        DefineFeedTableController.prototype.updateFormValidation = function (columnDef) {
            if (this.isInvalid(columnDef)) {
                this.add(this.defineFeedTableForm.invalidColumns, columnDef);
            }
            else {
                this.remove(this.defineFeedTableForm.invalidColumns, columnDef);
            }
        };
        DefineFeedTableController.prototype.initValidationErrors = function (columnDef) {
            columnDef.validationErrors = {
                name: {},
                precision: {}
            };
        };
        DefineFeedTableController.prototype.validateColumn = function (columnDef) {
            var _this = this;
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
            var columnsByName = _.groupBy(this.model.table.tableSchema.fields, function (column) {
                //we'll disregard "not unique" name for all empty names and all deleted columns, i.e. put them into single group
                if (_this.isDeleted(column)) {
                    return "";
                }
                return column.name ? column.name.trim() : "";
            });
            _.each(_.keys(columnsByName), function (columnName) {
                var group = columnsByName[columnName];
                _.each(group, function (column) {
                    if (column.validationErrors === undefined) {
                        _this.initValidationErrors(column);
                    }
                    if (columnName !== "") {
                        column.validationErrors.name.notUnique = group.length > 1;
                    }
                    else {
                        //group with empty column name which also includes "deleted" columns
                        column.validationErrors.name.notUnique = false;
                    }
                    _this.updateFormValidation(column);
                });
            });
            //reset partition formula if data type has changed from date/timestamp to another type
            _.each(this.model.table.partitions, function (partition) {
                if (partition.columnDef === columnDef) {
                    if (columnDef.derivedDataType !== "date" || columnDef.derivedDataType !== "timestamp") {
                        _.forEach(["to_date", "year", "month", "day", "hour", "minute"], function (formula) {
                            if (partition.formula === formula) {
                                partition.formula = "val";
                            }
                        });
                    }
                }
            });
        };
        DefineFeedTableController.prototype.errorMessage = function (columnDef) {
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
        };
        DefineFeedTableController.prototype.remove = function (array, element) {
            for (var i = 0; i < array.length; i++) {
                if (array[i]._id === element._id) {
                    array.splice(i, 1);
                    break;
                }
            }
        };
        DefineFeedTableController.prototype.add = function (array, element) {
            this.remove(array, element);
            array.push(element);
        };
        DefineFeedTableController.prototype.onFieldChange = function (columnDef) {
            // console.log("onFieldChange");
            this.selectedColumn = columnDef;
            this.addHistoryItem(columnDef);
        };
        /**
         * When a partition Source field changes it needs to
         *  - auto select the formula if there is only 1 in the drop down (i.e. fields other than dates/timestamps will only have the 'val' formula
         *  - ensure the partition data mapping to this source field is correct
         *  - attempt to prefill in the name with some default name.  if its a val formula it will default the partition name to the source field name and leave it disabled
         * @param partition
         */
        DefineFeedTableController.prototype.onPartitionSourceFieldChange = function (partition) {
            // console.log("onPartitionSourceFieldChange");
            //set the partition data to match the selected sourceField
            if (partition.columnDef != null) {
                partition.sourceField = partition.columnDef.name;
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
        };
        /**
         * When a partition formula changes it needs to
         *  - attempt to prefill in the name with some default name.  if its a val formula it will default the partition name to the source field name and leave it disabled
         * @param partition
         */
        DefineFeedTableController.prototype.onPartitionFormulaChange = function (partition) {
            // console.log("onPartitionFormulaChange");
            this.updatePartitionFieldName(partition);
            this.partitionNamesUnique();
        };
        /**
         * when the partition name changes it needs to
         *  - ensure the names are unique
         *  - ensure no dups (cannot have more than 1 partitoin on the same col/formula
         * @param partition
         */
        DefineFeedTableController.prototype.onPartitionNameChange = function (partition) {
            // console.log("onPartitionNameChange");
            if (this.useUnderscoreInsteadOfSpaces) {
                partition.field = this.replaceSpaces(partition.field);
            }
            this.partitionNamesUnique();
        };
        /**
         * Helper method to look through the table columns (not partitions) and find the first one that matches the incoming {@code fieldName}
         * @param fieldName
         * @returns {*|{}}
         */
        DefineFeedTableController.prototype.getColumnDefinition = function (fieldName) {
            return _.find(this.model.table.tableSchema.fields, function (field) {
                return field.name == fieldName;
            });
        };
        /**
         * Ensure that for the partitions the sourceField and sourceDataTypes match the respective schema field data
         */
        DefineFeedTableController.prototype.ensurePartitionData = function () {
            var _this = this;
            // console.log("ensurePartitionData");
            var nameMap = {};
            _.each(this.model.table.partitions, function (partition) {
                if (partition.columnDef == undefined) {
                    var columnDef = _this.getColumnDefinition(partition.sourceField);
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
        };
        /**
         * if the formula == val assign the field to be the same as the source field, otherwise attempt to prefill the name with the source_formula
         * @param partition
         */
        DefineFeedTableController.prototype.updatePartitionFieldName = function (partition) {
            // console.log("updatePartitionFieldName");
            if (partition.formula != 'val') {
                if (partition.sourceField != null && (partition.field == null || partition.field == '' || partition.field == partition.sourceField + "_")) {
                    partition.field = partition.sourceField + "_" + partition.formula;
                }
            }
            else {
                partition.field = partition.columnDef ? partition.columnDef.name : partition.sourceField;
            }
        };
        DefineFeedTableController.prototype.validate = function (validForm) {
            // console.log("validate valid ? " + validForm);
            if (_.isUndefined(this.defineFeedTableForm.invalidColumns)) {
                this.defineFeedTableForm.invalidColumns = [];
            }
            if (validForm == undefined) {
                validForm = this.defineFeedTableForm.$valid;
            }
            var valid = this.model.templateId != null && this.model.table.method != null && this.model.table.tableSchema.name != null && this.model.table.tableSchema.name != ''
                && this.model.table.tableSchema.fields.length > 0;
            if (valid) {
                //ensure we have at least 1 field (not deleted) assigned to the model)
                var validFields = _.filter(this.model.table.tableSchema.fields, function (field) {
                    return field.deleted == undefined || field.deleted == false;
                });
                valid = validFields.length > 0;
                this.ensurePartitionData();
            }
            this.isValid = valid && validForm && this.defineFeedTableForm.invalidColumns.length === 0;
        };
        DefineFeedTableController.$inject = ["$rootScope", "$scope", "$http", "$timeout", "$mdToast", "$filter", "$mdDialog",
            "$mdExpansionPanel", "RestUrlService", "FeedService", "FileUpload", "BroadcastService",
            "Utils", "FeedTagService", "DomainTypesService"];
        return DefineFeedTableController;
    }());
    exports.DefineFeedTableController = DefineFeedTableController;
    var FilterPartitionFormulaPipe = /** @class */ (function () {
        function FilterPartitionFormulaPipe(FeedService) {
            this.FeedService = FeedService;
        }
        FilterPartitionFormulaPipe.prototype.transform = function (formulas, partition) {
            // Find column definition
            var columnDef = (partition && partition.sourceField) ? this.FeedService.getColumnDefinitionByName(partition.sourceField) : null;
            if (columnDef == null) {
                return formulas;
            }
            // Filter formulas based on column type
            if (columnDef.derivedDataType !== "date" && columnDef.derivedDataType !== "timestamp") {
                return _.without(formulas, "to_date", "year", "month", "day", "hour", "minute");
            }
            else {
                return formulas;
            }
        };
        return FilterPartitionFormulaPipe;
    }());
    angular.module(moduleName).filter("filterPartitionFormula", ["FeedService", function (FeedService) {
            var pipe = new FilterPartitionFormulaPipe(FeedService);
            return pipe.transform.bind(pipe);
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
});
//# sourceMappingURL=DefineFeedTableDirective.js.map