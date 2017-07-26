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
define(['angular','feed-mgr/feeds/define-feed/module-name'], function (angular,moduleName) {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                canRemoveFields: "@",
                stepIndex: '@',
                tableLocked: "@",
                typeLocked: "@"
            },
            scope: {},
            require: ['thinkbigDefineFeedTable', '^thinkbigStepper'],
            controllerAs: 'vm',
            templateUrl: 'js/feed-mgr/feeds/define-feed/feed-details/define-feed-table.html',
            controller: "DefineFeedTableController",
            link: function ($scope, element, attrs, controllers) {
                var thisController = controllers[0];
                var stepperController = controllers[1];
                thisController.stepperController = stepperController;
                thisController.totalSteps = stepperController.totalSteps;
            }

        };
    };

    var controller = function ($scope, $http, $timeout, $mdToast, $filter, $mdDialog, $mdExpansionPanel, RestUrlService, FeedService, FileUpload, BroadcastService, Utils, FeedTagService,
                               DomainTypesService) {

        this.defineFeedTableForm = {};
        var self = this;
        this.stepNumber = parseInt(this.stepIndex) + 1
        this.stepperController = null;
        this.model = FeedService.createFeedModel;
        this.isValid = false;
        this.sampleFile = null;
        this.tableCreateMethods = [{type: 'MANUAL', name: 'Manual'}, {type: 'SAMPLE_FILE', name: 'Sample File'}];
        this.availableDefinitionDataTypes = FeedService.columnDefinitionDataTypes.slice();
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
         * Provides a list of available tags.
         * @type {FeedTagService}
         */
        self.feedTagService = FeedTagService;

        /**
         * Metadata for the selected column tag.
         * @type {{searchText: null, selectedItem: null}}
         */
        self.tagChips = {searchText: null, selectedItem: null};

        /**
         * List of available domain types.
         * @type {DomainType[]}
         */
        self.availableDomainTypes = [];
        DomainTypesService.findAll().then(function (domainTypes) {
            self.availableDomainTypes = domainTypes;
        });

        $scope.$evalAsync(function() {
            self.calcTableState();
            if (self.model.table.tableSchema.fields && self.model.table.tableSchema.fields.length > 0) {
                self.syncFeedsColumns();
                self.calcTableState();
                self.expandSchemaPanel();
            }
        });

        BroadcastService.subscribe($scope, 'DATA_TRANSFORM_SCHEMA_LOADED', onDataTransformSchemaLoaded);

        function onDataTransformSchemaLoaded() {
            self.syncFeedsColumns();
            validate();
        }

        this.calcTableState = function() {
            self.tableLocked = angular.isDefined(self.tableLocked) && (self.tableLocked === true || self.tableLocked === "true");
            self.dataTypeLocked = angular.isDefined(self.dataTypeLocked) && (self.typeLocked === true || self.typeLocked === "true");
            self.canRemoveFields = angular.isUndefined(self.canRemoveFields) || self.canRemoveFields === true || self.canRemoveFields === "true";
            self.showMethodPanel = (self.model.table.method != 'EXISTING_TABLE');
            self.showTablePanel = (self.model.table.tableSchema.fields.length > 0);
        };
        this.calcTableState();

        /*
        Create columns for tracking changes between original source and the target table schema
         */
        this.syncFeedsColumns = function() {

            _.each(self.model.table.tableSchema.fields, function (columnDef) {
                initFeedColumn(columnDef);
            });
        }

        function initFeedColumn(columnDef){
            if (columnDef.origName == undefined) {
                columnDef.origName = columnDef.name;
                columnDef.origDataType = columnDef.derivedDataType;
                columnDef.deleted = false;
                columnDef.history = [];
                self.addHistoryItem(columnDef);
            }
        }

        /*
        Adds record of changed state
         */
        function createHistoryRecord(columnDef) {
            return { name: columnDef.name, derivedDataType: columnDef.derivedDataType, precisionScale: columnDef.precisionScale, deleted: columnDef.deleted, primaryKey: columnDef.primaryKey, updatedTracker: columnDef.updatedTracker, createdTracker: columnDef.createdTracker }
        }

        /**
         * Called wehn the Method radio option is changed
         */
        this.updateSelectedMethod =function(method){
            if(method == 'MANUAL' ) {
                self.model.allowSkipHeaderOption = true;
            }

        };

        this.addHistoryItem = function(columnDef) {
            var historyItem = createHistoryRecord(columnDef);
            columnDef.history.push(historyItem);
        };

        /**
         * when adding a new column
         * @returns {*|{name, description, dataType, precisionScale, dataTypeDisplay, primaryKey, nullable, sampleValues, selectedSampleValue, isValid, _id}}
         */
        function newColumnDefinition() {
            return FeedService.newTableFieldDefinition();
        }

        /**
         * when adding a new column this is also called to synchronize the field policies array with the columns
         * @returns {*|{name, partition, profile, standardization, validation}}
         */
        function newColumnPolicy() {
            return FeedService.newTableFieldPolicy();
        }

        /**
         * When adding a new Partition
         * @param index
         * @returns {{position: *, field: string, sourceField: string, formula: string, sourceDataType: string, _id: *}}
         */
        function newPartitionField(index) {
            return {position: index, field: '', sourceField: '', formula: '', sourceDataType: '', _id: _.uniqueId()}
        }

        function replaceSpaces(str) {
            if (str != undefined) {
                return str.replace(/ /g, '_');
            }
            else {
                return '';
            }
        }

        function showProgress() {
            if (self.stepperController) {
                self.stepperController.showProgress = true;
            }
        }

        function hideProgress() {
            if (self.stepperController) {
                self.stepperController.showProgress = false;
            }
        }

        function resetColumns() {
            self.model.table.tableSchema.fields = [];
            self.model.table.fieldPolicies = [];
        }

        /**
         * Ensure the field names are unique.
         * If not add the angular $error to invalidate the form
         */
        function fieldNamesUnique(resetFormReadyCheck) {

            //map of names that are not unique
            var notUnique = {}

            if (resetFormReadyCheck == undefined) {
                resetFormReadyCheck = false;
            }
            else if (resetFormReadyCheck == true) {
                self.fieldNamesUniqueRetryAmount = 0;
            }

            //map of names to array of columnDefinitions
            var nameMap = {};
            _.each(self.model.table.tableSchema.fields, function (columnDef) {

                if (nameMap[columnDef.name] == undefined) {
                    nameMap[columnDef.name] = [];
                }
                nameMap[columnDef.name].push(columnDef);
                if (nameMap[columnDef.name].length > 1) {
                    notUnique[columnDef.name] = columnDef.name;
                }

            });

            //invalidate the form if needed
            var formReady = _.every(self.model.table.tableSchema.fields, function(columnDef) {
                // Ensure column is defined
                if (!angular.isDefined(self.defineFeedTableForm["name_" + columnDef._id])) {
                    return false;
                }

                if (columnDef.deleted === true) {
                    self.defineFeedTableForm["name_" + columnDef._id].$setValidity("required", true);
                    self.defineFeedTableForm["datatype_" + columnDef._id].$setValidity("required", true);
                }

                // Check for reserved names
                if (columnDef.name === "processing_dttm") {
                    self.defineFeedTableForm["name_" + columnDef._id].$setValidity("reserved", false);
                } else {
                    self.defineFeedTableForm["name_" + columnDef._id].$setValidity("reserved", true);
                }

                // Check for duplicate column names
                if (angular.isDefined(notUnique[columnDef.name])) {
                    self.defineFeedTableForm["name_" + columnDef._id].$setValidity("notUnique", false);
                } else {
                    self.defineFeedTableForm["name_" + columnDef._id].$setValidity("notUnique", true);
                }

                return true;
            });
            if (formReady) {
                self.fieldNamesUniqueRetryAmount = 0;
            }
            else if (self.fieldNamesUniqueRetryAmount < 10) {
                self.fieldNamesUniqueRetryAmount++;
                $timeout(fieldNamesUnique, 20);
            }
        }

        /**
         * Ensure the Partition Names are unique
         * If Not add a angular error
         */
        function partitionNamesUnique() {

            // Validate the Partition names are unique respective to other partition names
            _.chain(self.model.table.partitions).groupBy(function (partition) {
                return partition.field
            }).each(function (group, name) {
                if (group.length > 1) {
                    _.each(group, function (partition) {
                        //.invalid another partition matches the same name
                        self.defineFeedTableForm['partition_name' + partition._id].$setValidity('notUnique', false);
                    });
                }
                else {
                    _.each(group, function (partition) {
                        //valid this is a unique partition name
                        self.defineFeedTableForm['partition_name' + partition._id].$setValidity('notUnique', true);
                    });
                }
            });

            //Validate the Partition names are unique respective to the other fields

            //an array of column names
            var columnNames = _.map(self.model.table.tableSchema.fields, function (columnDef) {
                return columnDef.name;
            });
            var countPartitionNames = {};
            //add the angular errors
            _.each(self.model.table.partitions, function (partition) {
                if (partition.formula != undefined && partition.formula != 'val' && _.indexOf(columnNames, partition.field) >= 0) {
                    self.defineFeedTableForm['partition_name' + partition._id].$setValidity('notUnique', false);
                }
            });

        }

        /**
         * Adding a new Column to the schema
         * This is called both when the user clicks the "Add Field" button or when the sample file is uploaded
         * If adding from the UI the {@code columnDef} will be null, otherwise it will be the parsed ColumnDef from the sample file
         * @param columnDef
         */
        this.addColumn = function (columnDef, syncFieldPolicies) {

            if (columnDef == null) {
                columnDef = newColumnDefinition();
            }

            // Detect domain type and select sample value
            var policy = newColumnPolicy();
            if (columnDef.sampleValues != null && columnDef.sampleValues.length > 0) {
                columnDef.selectedSampleValue = columnDef.sampleValues[0];
                var domainType = DomainTypesService.detectDomainType(columnDef.sampleValues, self.availableDomainTypes);
                if (domainType !== null) {
                    FeedService.setDomainTypeForField(columnDef, policy, domainType);
                }
            } else {
                columnDef.selectedSampleValue = null;
            }

            if (self.useUnderscoreInsteadOfSpaces) {
                columnDef.name = replaceSpaces(columnDef.name);
            }
            initFeedColumn(columnDef)
            //add the column to both the source and destination tables as well as the fieldPolicies array
            self.model.table.tableSchema.fields.push(columnDef);
            self.model.table.fieldPolicies.push(policy);
            self.model.table.sourceTableSchema.fields.push(newColumnDefinition());
            validate();
            if (syncFieldPolicies == undefined || syncFieldPolicies == true) {
                FeedService.syncTableFieldPolicyNames();
            }
        };

        this.undoColumn = function (index) {
            var columnDef = self.model.table.tableSchema.fields[index];
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

            fieldNamesUnique();
            partitionNamesUnique();
            FeedService.syncTableFieldPolicyNames();
            validate();
        };

        /**
         * Remove a column from the schema
         * @param index
         */
        this.removeColumn = function (index) {
            var columnDef = self.model.table.tableSchema.fields[index];
            columnDef.deleted = true;
            self.addHistoryItem(columnDef);

            //remove any partitions using this field
            var matchingPartitions = _.filter(self.model.table.partitions, function (partition) {
                return partition.columnDef.name == columnDef.name;
            });
            if (matchingPartitions) {
                _.each(matchingPartitions, function (partition) {
                    var idx = _.indexOf(self.model.table.partitions, partition.sourceField)
                    if (idx >= 0) {
                        self.removePartitionField(idx);
                    }
                });
            }

            //ensure the field names on the columns are unique again as removing a column might fix a "notUnique" error
            fieldNamesUnique();
            partitionNamesUnique();
            //FeedService.syncTableFieldPolicyNames();
            validate();
        };

        /**
         * Removes the column matching the passed in {@code columnDef} with the array of columns
         * @param columnDef
         */
        this.removeColumnUsingReference = function (columnDef) {

            var idx = _.indexOf(self.model.table.tableSchema.fields, columnDef)
            if (idx >= 0) {
                self.removeColumn(idx);
            }
        };

        /**
         * Add a partition to the schema
         * This is called from the UI when the user clicks "Add Partition"
         */
        this.addPartitionField = function () {
            var partitionLength = self.model.table.partitions.length;
            var partition = newPartitionField(partitionLength);
            self.model.table.partitions.push(partition);
        };

        /**
         * Remove the partition from the schecma
         * @param index
         */
        this.removePartitionField = function (index) {
            self.model.table.partitions.splice(index, 1);
            partitionNamesUnique();
        };

        this.getSelectedColumn = function () {
            return self.selectedColumn;
        };

        this.onSelectedColumn = function (selectedColumn) {
            var firstSelection = self.selectedColumn == null;
            self.selectedColumn = selectedColumn;
            // Show an item in dropdown
            if (self.selectedColumn.selectedSampleValue == null && self.selectedColumn.sampleValues.length > 0) {
                self.selectedColumn.selectedSampleValue = self.selectedColumn.sampleValues[0];
            }
            if(firstSelection){
                //trigger scroll to stick the selection to the screen
                Utils.waitForDomElementReady('#selectedColumnPanel',function() {
                    angular.element('#selectedColumnPanel').triggerHandler('stickIt');
                })
            }

            // Ensure tags is an array
            if (!angular.isArray(selectedColumn.tags)) {
                selectedColumn.tags = [];
            }
        };

        /**
         * When the schema field changes it needs to
         *  - ensure the names are unique
         *  - update the respective partition names if there is a partition on the field with the 'val' formula
         *  - ensure that partition names are unique since the new field name could clash with an existing partition
         * @param columnDef
         */
        this.onNameFieldChange = function(columnDef) {

            if (self.useUnderscoreInsteadOfSpaces) {
                columnDef.name = replaceSpaces(columnDef.name);
            }

            if(columnDef.derivedDataType != 'decimal'){
                columnDef.precisionScale = null;
            }
            self.onFieldChange(columnDef);

            fieldNamesUnique(true);
            //update the partitions with "val" on this column so the name matches
            _.each(self.model.table.partitions, function (partition) {
                if (partition.columnDef == columnDef) {
                    partition.sourceDataType = columnDef.derivedDataType;
                    partition.sourceField = columnDef.name;
                    self.updatePartitionFieldName(partition);
                }
            });
            partitionNamesUnique();
            FeedService.syncTableFieldPolicyNames();
        };

        this.onFieldChange = function (columnDef) {
            self.selectedColumn = columnDef;
            self.addHistoryItem(columnDef);
        };

        /**
         * When a partition Source field changes it needs to
         *  - auto select the formula if there is only 1 in the drop down (i.e. fields other than dates/timestamps will only have the 'val' formula
         *  - ensure the partition data mapping to this source field is correct
         *  - attempt to prefill in the name with some default name.  if its a val formula it will default the partition name to the source field name and leave it disabled
         * @param partition
         */
        this.onPartitionSourceFieldChange = function (partition) {
            //set the partition data to match the selected sourceField
            if (partition.columnDef != null) {
                partition.sourceField = partition.columnDef.name
                partition.sourceDataType = partition.columnDef.derivedDataType;
            }
            else {
                //  console.error("NO FIELD FOR partition ",partition)
            }
            //if there is only 1 option in the formula list then auto select it
            var formulas = $filter('filterPartitionFormula')(self.partitionFormulas, partition);
            if (formulas.length == 1) {
                partition.formula = formulas[0];
            }
            self.updatePartitionFieldName(partition);
            partitionNamesUnique();

        };
        /**
         * When a partition formula changes it needs to
         *  - attempt to prefill in the name with some default name.  if its a val formula it will default the partition name to the source field name and leave it disabled
         * @param partition
         */
        this.onPartitionFormulaChange = function (partition) {
            self.updatePartitionFieldName(partition);
            partitionNamesUnique();
        };

        /**
         * when the partition name changes it needs to
         *  - ensure the names are unique
         *  - ensure no dups (cannot have more than 1 partitoin on the same col/formula
         * @param partition
         */
        this.onPartitionNameChange = function (partition) {
            if (self.useUnderscoreInsteadOfSpaces) {
                partition.field = replaceSpaces(partition.field);
            }
            partitionNamesUnique();
        };

        function unqiueNameValidation() {
            fieldNamesUnique();
            partitionNamesUnique();
        }

        /**
         * Helper method to look through the table columns (not partitions) and find the first one that matches the incoming {@code fieldName}
         * @param fieldName
         * @returns {*|{}}
         */
        this.getColumnDefinition = function (fieldName) {

            return _.find(self.model.table.fields, function (field) {
                return field.name == fieldName;
            });
        };

        /**
         * Ensure that for the partitions the sourceField and sourceDataTypes match the respective schema field data
         */
        function ensurePartitionData() {
            var nameMap = {};
            _.each(self.model.table.partitions, function (partition) {
                if (partition.columnDef == undefined) {
                    var columnDef = self.getColumnDefinition(partition.sourceField);
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
        this.updatePartitionFieldName = function (partition) {
            if (partition.formula != 'val') {
                if (partition.sourceField != null && (partition.field == null || partition.field == '' || partition.field == partition.sourceField + "_")) {
                    partition.field = partition.sourceField + "_" + partition.formula;
                }
            }
            else {
                partition.field = partition.columnDef ? partition.columnDef.name : partition.sourceField;
            }
        };

        function validate(validForm) {
            if (validForm == undefined) {
                validForm = self.defineFeedTableForm.$valid ;
            }
            var valid = self.model.templateId != null && self.model.table.method != null && self.model.table.tableSchema.name != null && self.model.table.tableSchema.name != ''
                        && self.model.table.tableSchema.fields.length > 0;

            if (valid) {
                //ensure we have at least 1 field (not deleted) assigned to the model)
                var validFields = _.filter(self.model.table.tableSchema.fields,function(field) {
                    return field.deleted == undefined || field.deleted == false;
                });
                valid = validFields.length >0;
                ensurePartitionData();
            }
            self.isValid = valid && validForm;//&& self.model.table.tableSchema.invalidFields.length ==0;

        }

        var tableMethodWatch = $scope.$watch(function() {
            return self.model.table.method;
        }, function(newVal) {
            self.model.table.method = newVal;
            self.calcTableState();
        });

        //Set the Table Name to be the System Feed Name
        var systemFeedNameWatch = $scope.$watch(function () {
            return self.model.systemFeedName;
        }, function (newVal) {
            self.model.table.tableSchema.name = newVal;
        });

        /**
         * Ensure the form is valid
         * @type {*|function()}
         */
        var formValidWatch = $scope.$watch(function () {
            return self.defineFeedTableForm.$valid;
        }, function (newVal) {
            if (newVal == true) {
                validate(newVal);
            }
            else {
                self.isValid = false;
            }

        });


        var sampleFileWatch = $scope.$watch(function () {
            return self.sampleFile;
        }, function (newVal) {
            if (newVal == null) {
                angular.element('#upload-sample-file-btn').removeClass('md-primary');
            }
            else {
                angular.element('#upload-sample-file-btn').addClass('md-primary');
            }
        });

        /*
         Collapse the file picker section
         */
        this.collapseMethodPanel = function () {
            $mdExpansionPanel().waitFor('panelOne').then(function (instance) {
                instance.collapse();
            });
        };

        /*
         Expand the schema panel
         */
        this.expandSchemaPanel = function () {
            $mdExpansionPanel().waitFor('panelTwo').then(function (instance) {
                instance.expand();
            });
        };

        this.expandChooseMethodPanel = function () {
            $mdExpansionPanel().waitFor('panelOne').then(function (instance) {
                instance.expand();
            });
        };

        // choose to expand the choose method initially if no fields have been defined yet
        if(self.model.table.tableSchema.fields.length == 0){
            self.expandChooseMethodPanel();
        }
        else {
            self.expandSchemaPanel();
        }

        this.uploadSampleFile = function () {
            self.uploadBtnDisabled = true;
            showProgress();
            var file = self.sampleFile;
            var params = {};
            if (self.schemaParser) {
                params = {parser: JSON.stringify(self.schemaParser)};
            }
            var uploadUrl = RestUrlService.UPLOAD_SAMPLE_TABLE_FILE;
            var successFn = function (response) {
                var responseData = response.data;
                resetColumns();
                self.availableDefinitionDataTypes = FeedService.columnDefinitionDataTypes.slice();
                angular.forEach(responseData.fields, function (field) {
                    var col = newColumnDefinition();
                    col = angular.extend(col, field)
                    // add exotic data type to available columns if needed
                    if ($.inArray(col.derivedDataType, self.availableDefinitionDataTypes) == -1) {
                        self.availableDefinitionDataTypes.push(col.derivedDataType);
                    }
                    self.addColumn(col, false);
                });
                FeedService.syncTableFieldPolicyNames();
                //set the feedFormat property
                self.model.table.feedFormat = responseData.hiveFormat;
                self.model.table.structured = responseData.structured;
                if (self.schemaParser.allowSkipHeader) {
                    self.model.allowSkipHeaderOption = true;
                    self.model.options.skipHeader = true;
                } else {
                    self.model.allowSkipHeaderOption = false;
                    self.model.options.skipHeader = false;
                }
                fieldNamesUnique();
                hideProgress();
                self.uploadBtnDisabled = false;
                self.syncFeedsColumns();
                self.calcTableState();
                self.collapseMethodPanel();
                self.expandSchemaPanel();

                validate();
                angular.element('#upload-sample-file-btn').removeClass('md-primary');
            };
            var errorFn = function (data) {
                hideProgress();
                self.uploadBtnDisabled = false;
                angular.element('#upload-sample-file-btn').removeClass('md-primary');
            };
            //clear partitions
            while (self.model.table.partitions.length) {
                self.model.table.partitions.pop();
            }
            FileUpload.uploadFileToUrl(file, uploadUrl, successFn, errorFn, params);
        };

        /**
         * Transforms the specified chip into a tag.
         * @param {string} chip the chip
         * @returns {Object} the tag
         */
        self.transformChip = function (chip) {
            return angular.isObject(chip) ? chip : {name: chip};
        };

        // Retrieve partition formulas
        FeedService.getPartitionFunctions()
                .then(function(functions) {
                    self.partitionFormulas = functions;
                });

        validate();

        $scope.$on('$destroy', function () {
            systemFeedNameWatch();
        });
    };

    angular.module(moduleName).controller('DefineFeedTableController', ["$scope","$http","$timeout","$mdToast","$filter","$mdDialog","$mdExpansionPanel","RestUrlService","FeedService","FileUpload","BroadcastService","Utils", "FeedTagService", "DomainTypesService", controller]);

    angular.module(moduleName)
        .directive('thinkbigDefineFeedTable', directive);

    angular.module(moduleName).filter("filterPartitionFormula", ["FeedService", function(FeedService) {
        return function(formulas, partition) {
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
});
