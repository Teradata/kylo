define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    function FeedService($http, $q, $mdToast, $mdDialog, RestUrlService, VisualQueryService, FeedCreationErrorService, FeedPropertyService, AccessControlService, EntityAccessControlService, StateService) {
        function trim(str) {
            return str.replace(/^\s+|\s+$/g, "");
        }
        function toCamel(str) {
            return str.replace(/(\-[a-z])/g, function ($1) {
                return $1.toUpperCase().replace('-', '');
            });
        }
        function toDash(str) {
            return str.replace(/([A-Z])/g, function ($1) {
                return "-" + $1.toLowerCase();
            });
        }
        function spacesToUnderscore(str) {
            return str.replace(/\s+/g, '_');
        }
        function toUnderscore(str) {
            return str.replace(/(?:^|\.?)([A-Z])/g, function (x, y) {
                return "_" + y.toLowerCase();
            }).replace(/^_/, "");
            //return str.replace(/([A-Z])/g, "_$1").replace(/^_/,'').toLowerCase();
        }
        var data = {
            /**
             * The Feed model in the Create Feed Stepper
             */
            createFeedModel: {},
            /**
             * The Feed Model that is being Edited when a user clicks on a Feed Details
             */
            editFeedModel: {},
            /**
             * The initial CRON expression used when a user selects Cron for the Schedule option
             */
            DEFAULT_CRON: "0 0 12 1/1 * ? *",
            /**
             * In the Data Processing section these are the available Strategies a user can choose when defining the feed
             */
            mergeStrategies: [
                { name: 'Sync', type: 'SYNC', hint: 'Replace table content', disabled: false },
                { name: 'Rolling sync', type: 'ROLLING_SYNC', hint: 'Replace content in matching partitions' },
                { name: 'Merge', type: 'MERGE', hint: 'Insert all rows', disabled: false },
                { name: 'Dedupe and merge', type: 'DEDUPE_AND_MERGE', hint: 'Insert rows ignoring duplicates', disabled: false },
                { name: 'Merge using primary key', type: 'PK_MERGE', hint: 'Upsert using primary key' }
            ],
            /**
             * The available Target Format options
             */
            targetFormatOptions: [{ label: "ORC", value: 'STORED AS ORC' },
                { label: "PARQUET", value: 'STORED AS PARQUET' },
                { label: "AVRO", value: 'STORED AS AVRO' },
                {
                    label: "TEXTFILE",
                    value: 'ROW FORMAT SERDE \'org.apache.hadoop.hive.serde2.OpenCSVSerde\' WITH SERDEPROPERTIES ( \'separatorChar\' = \',\' ,\'escapeChar\' = \'\\\\\' ,\'quoteChar\' = \'"\')'
                        + ' STORED AS'
                        + ' TEXTFILE'
                },
                { label: "RCFILE", value: 'ROW FORMAT SERDE "org.apache.hadoop.hive.serde2.columnar.ColumnarSerDe" STORED AS RCFILE' }],
            /**
             * The available Compression options for a given targetFormat {@see this#targetFormatOptions}
             */
            compressionOptions: { "ORC": ['NONE', 'SNAPPY', 'ZLIB'], "PARQUET": ['NONE', 'SNAPPY'], "AVRO": ['NONE'] },
            /**
             * Standard data types for column definitions
             */
            columnDefinitionDataTypes: ['string', 'int', 'bigint', 'tinyint', 'decimal', 'double', 'float', 'date', 'timestamp', 'boolean', 'binary'],
            /**
             * Returns an array of all the compression options regardless of the {@code targetFormat}
             * (i.e. ['NONE','SNAPPY','ZLIB']
             * @returns {Array}
             */
            allCompressionOptions: function () {
                var arr = [];
                _.each(this.compressionOptions, function (options) {
                    arr = _.union(arr, options);
                });
                return arr;
            },
            /**
             * Returns the feed object model for creating a new feed
             *
             * @returns {{id: null, versionName: null, templateId: string, feedName: string, description: null, systemFeedName: string, inputProcessorType: string, inputProcessor: null,
             *     nonInputProcessors: Array, properties: Array, securityGroups: Array, schedule: {schedulingPeriod: string, schedulingStrategy: string, concurrentTasks: number}, defineTable: boolean,
             *     allowPreconditions: boolean, dataTransformationFeed: boolean, table: {tableSchema: {name: null, fields: Array}, sourceTableSchema: {name: null, fields: Array}, method: string,
             *     existingTableName: null, targetMergeStrategy: string, feedFormat: string, targetFormat: null, fieldPolicies: Array, partitions: Array, options: {compress: boolean, compressionFormat:
             *     null, auditLogging: boolean, encrypt: boolean, trackHistory: boolean}, sourceTableIncrementalDateField: null}, category: {id: null, name: null}, dataOwner: string, tags: Array,
             *     reusableFeed: boolean, dataTransformation: {chartViewModel: null, dataTransformScript: null, sql: null, states: Array}, userProperties: Array}}
             */
            getNewCreateFeedModel: function () {
                return {
                    id: null,
                    versionName: null,
                    templateId: '',
                    feedName: '',
                    description: '',
                    systemFeedName: '',
                    inputProcessorType: '',
                    inputProcessorName: null,
                    inputProcessor: null,
                    nonInputProcessors: [],
                    properties: [],
                    securityGroups: [],
                    schedule: { schedulingPeriod: data.DEFAULT_CRON, schedulingStrategy: 'CRON_DRIVEN', concurrentTasks: 1 },
                    defineTable: false,
                    allowPreconditions: false,
                    dataTransformationFeed: false,
                    table: {
                        tableSchema: { name: null, fields: [] },
                        sourceTableSchema: { name: null, tableSchema: null, fields: [] },
                        feedTableSchema: { name: null, fields: [] },
                        method: 'SAMPLE_FILE',
                        existingTableName: null,
                        structured: false,
                        targetMergeStrategy: 'DEDUPE_AND_MERGE',
                        feedFormat: 'ROW FORMAT SERDE \'org.apache.hadoop.hive.serde2.OpenCSVSerde\''
                            + ' WITH SERDEPROPERTIES ( \'separatorChar\' = \',\' ,\'escapeChar\' = \'\\\\\' ,\'quoteChar\' = \'"\')'
                            + ' STORED AS TEXTFILE',
                        targetFormat: null,
                        fieldPolicies: [],
                        partitions: [],
                        options: { compress: false, compressionFormat: null, auditLogging: true, encrypt: false, trackHistory: false },
                        sourceTableIncrementalDateField: null
                    },
                    category: { id: null, name: null },
                    dataOwner: '',
                    tags: [],
                    reusableFeed: false,
                    dataTransformation: {
                        chartViewModel: null,
                        datasourceIds: null,
                        datasources: null,
                        dataTransformScript: null,
                        sql: null,
                        states: []
                    },
                    userProperties: [],
                    options: { skipHeader: false },
                    active: true,
                    roleMemberships: [],
                    owner: null,
                    roleMembershipsUpdated: false,
                    tableOption: {},
                    cloned: false,
                    usedByFeeds: [],
                    view: {
                        generalInfo: { disabled: false },
                        feedDetails: { disabled: false },
                        table: { disabled: false },
                        dataPolicies: { disabled: false },
                        properties: {
                            disabled: false,
                            dataOwner: { disabled: false },
                            tags: { disabled: false }
                        },
                        accessControl: { disabled: false },
                        schedule: {
                            disabled: false,
                            schedulingPeriod: { disabled: false },
                            schedulingStrategy: { disabled: false },
                            active: { disabled: false },
                            executionNode: { disabled: false },
                            preconditions: { disabled: false }
                        }
                    }
                };
            },
            cloneFeed: function () {
                //copy the feed
                data.createFeedModel = angular.copy(data.editFeedModel);
                data.createFeedModel.id = null;
                data.createFeedModel.cloned = true;
                data.createFeedModel.clonedFrom = data.createFeedModel.feedName;
                data.createFeedModel.feedName += "_copy";
                data.createFeedModel.systemFeedName += "_copy";
                data.createFeedModel.owner = undefined;
                return data.createFeedModel;
            },
            /**
             * Called when starting a new feed.
             * This will return the default model and also reset the Query Builder and Error service
             */
            newCreateFeed: function () {
                this.createFeedModel = this.getNewCreateFeedModel();
                VisualQueryService.resetModel();
                FeedCreationErrorService.reset();
            },
            /**
             * Updates a Feed with another model.
             * The model that is passed in will update the currently model being edited ({@code this.editFeedModel})
             * @param feedModel
             */
            updateFeed: function (feedModel) {
                var self = this;
                this.editFeedModel.totalPreSteps = 0;
                this.editFeedModel.inputProcessorName = null;
                this.editFeedModel.usedByFeeds = [];
                this.editFeedModel.description = '';
                this.editFeedModel.inputProcessor = null;
                angular.extend(this.editFeedModel, feedModel);
                //set the field name to the policy name attribute
                if (this.editFeedModel.table != null && this.editFeedModel.table.fieldPolicies != null) {
                    angular.forEach(this.editFeedModel.table.fieldPolicies, function (policy, i) {
                        var field = self.editFeedModel.table.tableSchema.fields[i];
                        if (field != null && field != undefined) {
                            policy.name = field.name;
                            policy.derivedDataType = field.derivedDataType;
                            policy.nullable = field.nullable;
                            policy.primaryKey = field.primaryKey;
                        }
                    });
                }
                //add in the view states
                var defaultView = self.getNewCreateFeedModel().view;
                this.editFeedModel.view = defaultView;
            },
            /**
             * Shows the Feed Error Dialog
             * @returns {*}
             */
            showFeedErrorsDialog: function () {
                return FeedCreationErrorService.showErrorDialog();
            },
            /**
             * Adds a Nifi Exception error to the Feed Error dialog
             * @param name
             * @param nifiFeed
             */
            buildErrorData: function (name, response) {
                FeedCreationErrorService.buildErrorData(name, response);
            },
            /**
             * Check to see if there are any errors added to the Error Dialog
             * @returns {*}
             */
            hasFeedCreationErrors: function () {
                return FeedCreationErrorService.hasErrors();
            },
            /**
             * Resets the Create feed ({@code this.createFeedModel}) object
             */
            resetFeed: function () {
                //get the new model and its keys
                var newFeedObj = this.getNewCreateFeedModel();
                var keys = _.keys(newFeedObj);
                var createFeedModel = angular.extend(this.createFeedModel, newFeedObj);
                //get the create model and its keys
                var modelKeys = _.keys(this.createFeedModel);
                //find those that have been added and delete them
                var extraKeys = _.difference(modelKeys, keys);
                _.each(extraKeys, function (key) {
                    delete createFeedModel[key];
                });
                VisualQueryService.resetModel();
                FeedCreationErrorService.reset();
            },
            getDataTypeDisplay: function (columnDef) {
                return columnDef.precisionScale != null ? columnDef.derivedDataType + "(" + columnDef.precisionScale + ")" : columnDef.derivedDataType;
            },
            /**
             * returns the Object used for creating the destination schema for each Field
             * This is used in the Table Step to define the schema
             *
             * @returns {{name: string, description: string, dataType: string, precisionScale: null, dataTypeDisplay: Function, primaryKey: boolean, nullable: boolean, createdTracker: boolean,
             *     updatedTracker: boolean, sampleValues: Array, selectedSampleValue: string, isValid: Function, _id: *}}
             */
            newTableFieldDefinition: function () {
                var newField = {
                    name: '',
                    description: '',
                    derivedDataType: 'string',
                    precisionScale: null,
                    dataTypeDisplay: '',
                    primaryKey: false,
                    nullable: false,
                    createdTracker: false,
                    updatedTracker: false,
                    sampleValues: [],
                    selectedSampleValue: '',
                    tags: [],
                    validationErrors: {
                        name: {},
                        precision: {}
                    },
                    isValid: function () {
                        return this.name != '' && this.derivedDataType != '';
                    },
                    _id: _.uniqueId()
                };
                return newField;
            },
            /**
             * Returns the object used for creating Data Processing policies on a given field
             * This is used in the Data Processing step
             *
             * @param fieldName
             * @returns {{name: (*|string), partition: null, profile: boolean, standardization: null, validation: null}}
             */
            newTableFieldPolicy: function (fieldName) {
                return { name: fieldName || '', partition: null, profile: true, standardization: null, validation: null };
            },
            /**
             * For a given list of incoming Table schema fields ({@see this#newTableFieldDefinition}) it will create a new FieldPolicy object ({@see this#newTableFieldPolicy} for it
             */
            setTableFields: function (fields, policies) {
                var _this = this;
                if (policies === void 0) { policies = null; }
                this.createFeedModel.table.tableSchema.fields = fields;
                this.createFeedModel.table.fieldPolicies = (policies != null && policies.length > 0) ? policies : fields.map(function (field) { return _this.newTableFieldPolicy(field.name); });
            },
            /**
             * Ensure that the Table Schema has a Field Policy for each of the fields and that their indices are matching.
             */
            syncTableFieldPolicyNames: function () {
                var self = this;
                angular.forEach(self.createFeedModel.table.tableSchema.fields, function (columnDef, index) {
                    //update the the policy
                    var inArray = index < self.createFeedModel.table.tableSchema.fields.length && index >= 0;
                    if (inArray) {
                        var name = self.createFeedModel.table.tableSchema.fields[index].name;
                        if (name != undefined) {
                            self.createFeedModel.table.fieldPolicies[index].name = name;
                            //assign pointer to the field?
                            self.createFeedModel.table.fieldPolicies[index].field = columnDef;
                        }
                        else {
                            if (self.createFeedModel.table.fieldPolicies[index].field) {
                                self.createFeedModel.table.fieldPolicies[index].field == null;
                            }
                        }
                    }
                });
                //remove any extra columns in the policies
                while (self.createFeedModel.table.fieldPolicies.length > self.createFeedModel.table.tableSchema.fields.length) {
                    self.createFeedModel.table.fieldPolicies.splice(self.createFeedModel.table.tableSchema.fields.length, 1);
                }
            },
            /**
             * return true/false if there is a PK defined for the incoming set of {@code feedModel.table.tableSchema.fields
                 * @param fields
                 * @returns {boolean}
             */
            hasPrimaryKeyDefined: function (feedModel) {
                var firstPk = _.find(feedModel.table.tableSchema.fields, function (field) {
                    return field.primaryKey;
                });
                return firstPk != null && firstPk != undefined;
            },
            /**
             * enable/disable the PK Merge strategy enforcing a PK column set.
             * returns if the strategy is valid or not
             *
             * @param feedModel
             * @param strategies
             * @returns {boolean}
             */
            enableDisablePkMergeStrategy: function (feedModel, strategies) {
                var pkStrategy = _.find(strategies, function (strategy) {
                    return strategy.type == 'PK_MERGE';
                });
                var selectedStrategy = feedModel.table.targetMergeStrategy;
                if (pkStrategy) {
                    if (!this.hasPrimaryKeyDefined(feedModel)) {
                        pkStrategy.disabled = true;
                    }
                    else {
                        pkStrategy.disabled = false;
                    }
                }
                if (pkStrategy && selectedStrategy == pkStrategy.type) {
                    return !pkStrategy.disabled;
                }
                else {
                    return true;
                }
            },
            /**
             * return true/false if there is a
             */
            enableDisableRollingSyncMergeStrategy: function (feedModel, strategies) {
                var rollingSyncStrategy = _.find(strategies, function (strategy) {
                    return strategy.type == 'ROLLING_SYNC';
                });
                var selectedStrategy = feedModel.table.targetMergeStrategy;
                if (rollingSyncStrategy) {
                    rollingSyncStrategy.disabled = !this.hasPartitions(feedModel);
                }
                if (rollingSyncStrategy && selectedStrategy == rollingSyncStrategy.type) {
                    return !rollingSyncStrategy.disabled;
                }
                else {
                    return true;
                }
            },
            updateEnabledMergeStrategy: function (feedModel, strategies) {
                this.enableDisablePkMergeStrategy(feedModel, strategies);
                this.enableDisableRollingSyncMergeStrategy(feedModel, strategies);
            },
            hasPartitions: function (feedModel) {
                return feedModel.table.partitions != null
                    && feedModel.table.partitions != undefined
                    && feedModel.table.partitions.length > 0;
            },
            /**
             * This will clear the Table Schema resetting the method, fields, and policies
             */
            clearTableData: function () {
                //this.createFeedModel.table.method = 'MANUAL';
                this.createFeedModel.table.tableSchema.fields = [];
                this.createFeedModel.table.fieldPolicies = [];
                this.createFeedModel.table.existingTableName = null;
            },
            /**
             * In the stepper when a feeds step is complete and validated it will change the Step # icon to a Check circle
             */
            updateEditModelStateIcon: function () {
                if (this.editFeedModel.state == 'ENABLED') {
                    this.editFeedModel.stateIcon = 'check_circle';
                }
                else {
                    this.editFeedModel.stateIcon = 'block';
                }
            },
            /**
             * Initialize this object by creating a new empty {@see this#createFeedModel} object
             */
            init: function () {
                this.newCreateFeed();
            },
            /**
             * Before the model is saved to the server this will be called to make any changes
             * @see this#saveFeedModel
             * @param model
             */
            prepareModelForSave: function (model) {
                var properties = [];
                if (model.inputProcessor != null) {
                    angular.forEach(model.inputProcessor.properties, function (property) {
                        FeedPropertyService.initSensitivePropertyForSaving(property);
                        properties.push(property);
                    });
                }
                angular.forEach(model.nonInputProcessors, function (processor) {
                    angular.forEach(processor.properties, function (property) {
                        FeedPropertyService.initSensitivePropertyForSaving(property);
                        properties.push(property);
                    });
                });
                if (model.inputProcessor) {
                    model.inputProcessorName = model.inputProcessor.name;
                }
                model.properties = properties;
                //prepare access control changes if any
                EntityAccessControlService.updateRoleMembershipsForSave(model.roleMemberships);
                if (model.table && model.table.fieldPolicies && model.table.tableSchema && model.table.tableSchema.fields) {
                    // Set feed
                    var newFields = [];
                    var newPolicies = [];
                    var feedFields = [];
                    var sourceFields = [];
                    angular.forEach(model.table.tableSchema.fields, function (columnDef, idx) {
                        var policy = model.table.fieldPolicies[idx];
                        var sourceField = angular.copy(columnDef);
                        var feedField = angular.copy(columnDef);
                        sourceField.name = columnDef.origName;
                        sourceField.derivedDataType = columnDef.origDataType;
                        // structured files must use the original names
                        if (model.table.structured == true) {
                            feedField.name = columnDef.origName;
                            feedField.derivedDataType = columnDef.origDataType;
                        }
                        else if (model.table.method == 'EXISTING_TABLE') {
                            sourceField.name = columnDef.origName;
                        }
                        if (angular.isDefined(policy)) {
                            policy.feedFieldName = feedField.name;
                            policy.name = columnDef.name;
                        }
                        if (!columnDef.deleted) {
                            newFields.push(columnDef);
                            if (angular.isDefined(policy)) {
                                newPolicies.push(policy);
                            }
                            sourceFields.push(sourceField);
                            feedFields.push(feedField);
                        }
                        else {
                            // For files the feed table must contain all the columns from the source even if unused in the target
                            if (model.table.method == 'SAMPLE_FILE') {
                                feedFields.push(feedField);
                            }
                            else if (model.table.method == 'EXISTING_TABLE' && model.table.sourceTableIncrementalDateField == sourceField.name) {
                                feedFields.push(feedField);
                                sourceFields.push(sourceField);
                            }
                        }
                    });
                    model.table.fieldPolicies = newPolicies;
                    model.table.tableSchema.fields = newFields;
                    if (model.table.sourceTableSchema == undefined) {
                        model.table.sourceTableSchema = { name: null, tableSchema: null, fields: [] };
                    }
                    //only set the sourceFields if its the first time creating this feed
                    if (model.id == null) {
                        model.table.sourceTableSchema.fields = sourceFields;
                        model.table.feedTableSchema.fields = feedFields;
                    }
                    if (model.table.feedTableSchema == undefined) {
                        model.table.feedTableSchema = { name: null, fields: [] };
                    }
                    //remove any extra columns in the policies
                    /*
                     while(model.table.fieldPolicies.length > model.table.tableSchema.fields.length) {
                     model.table.fieldPolicies.splice(model.table.tableSchema.fields.length, 1);
                     }
                     */
                }
            },
            /**
             * Show a dialog indicating that the feed is saving
             * @param ev
             * @param message
             * @param feedName
             */
            showFeedSavingDialog: function (ev, message, feedName) {
                $mdDialog.show({
                    controller: 'FeedSavingDialogController',
                    templateUrl: 'js/feed-mgr/feeds/edit-feed/details/feed-saving-dialog.html',
                    parent: angular.element(document.body),
                    targetEvent: ev,
                    clickOutsideToClose: false,
                    fullscreen: true,
                    locals: {
                        message: message,
                        feedName: feedName
                    }
                })
                    .then(function (answer) {
                    //do something with result
                }, function () {
                    //cancelled the dialog
                });
            },
            /**
             * Hide the Feed Saving Dialog
             */
            hideFeedSavingDialog: function () {
                $mdDialog.hide();
            },
            /**
             * Save the model Posting the data to the server
             * @param model
             * @returns {*}
             */
            saveFeedModel: function (model) {
                var self = this;
                self.prepareModelForSave(model);
                var deferred = $q.defer();
                var successFn = function (response) {
                    var invalidCount = 0;
                    if (response.data && response.data.success) {
                        //update the feed versionId and internal id upon save
                        model.id = response.data.feedMetadata.id;
                        model.versionName = response.data.feedMetadata.versionName;
                        $mdToast.show($mdToast.simple()
                            .textContent('Feed successfully saved')
                            .hideDelay(3000));
                        deferred.resolve(response);
                    }
                    else {
                        deferred.reject(response);
                    }
                };
                var errorFn = function (err) {
                    deferred.reject(err);
                };
                var copy = angular.copy(model);
                if (copy.registeredTemplate) {
                    copy.registeredTemplate = undefined;
                }
                //reset the sensitive properties
                FeedPropertyService.initSensitivePropertiesForEditing(model.properties);
                var promise = $http({
                    url: RestUrlService.CREATE_FEED_FROM_TEMPLATE_URL,
                    method: "POST",
                    data: angular.toJson(copy),
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    }
                }).then(successFn, errorFn);
                return deferred.promise;
            },
            /**
             * Call out to the server to get the System Name for a passed in name
             * @param feedName
             * @returns {HttpPromise}
             */
            getSystemName: function (feedName) {
                return $http.get(RestUrlService.GET_SYSTEM_NAME, { params: { name: feedName } });
            },
            /**
             * When creating a Feed find the First Column/Field that matches the given name
             * @param name
             * @returns {*|{}}
             */
            getColumnDefinitionByName: function (name) {
                return _.find(this.createFeedModel.table.tableSchema.fields, function (columnDef) {
                    return columnDef.name == name;
                });
            },
            /**
             * Call the server to return a list of Feed Names
             * @returns {HttpPromise}
             */
            getFeedSummary: function () {
                var successFn = function (response) {
                    return response.data;
                };
                var errorFn = function (err) {
                };
                var promise = $http.get(RestUrlService.GET_FEED_NAMES_URL);
                promise.then(successFn, errorFn);
                return promise;
            },
            /**
             * Call the server to return a list of Feed Names
             * @returns {HttpPromise}
             */
            getFeedNames: function () {
                var successFn = function (response) {
                    return response.data;
                };
                var errorFn = function (err) {
                };
                var promise = $http.get(RestUrlService.OPS_MANAGER_FEED_NAMES);
                promise.then(successFn, errorFn);
                return promise;
            },
            /**
             * Call the server to get a list of all the available Preconditions that can be used when saving/scheduling the feed
             * @returns {HttpPromise}
             */
            getPossibleFeedPreconditions: function () {
                var successFn = function (response) {
                    return response.data;
                };
                var errorFn = function (err) {
                    console.log('ERROR ', err);
                };
                var promise = $http.get(RestUrlService.GET_POSSIBLE_FEED_PRECONDITIONS_URL);
                promise.then(successFn, errorFn);
                return promise;
            },
            /**
             * Gets the list of user properties for the specified feed.
             *
             * @param {Object} model the feed model
             * @return {Array.<{key: string, value: string}>} the list of user properties
             */
            getUserPropertyList: function (model) {
                var userPropertyList = [];
                angular.forEach(model.userProperties, function (value, key) {
                    if (!key.startsWith("jcr:")) {
                        userPropertyList.push({ key: key, value: value });
                    }
                });
                return userPropertyList;
            },
            /**
             * Gets the user fields for a new feed.
             *
             * @param {string} categoryId the category id
             * @returns {Promise} for the user fields
             */
            getUserFields: function (categoryId) {
                return $http.get(RestUrlService.GET_FEED_USER_FIELDS_URL(categoryId))
                    .then(function (response) {
                    return response.data;
                });
            },
            /**
             * Gets the controller services of the specified type.
             *
             * @param {string} type a type class
             * @returns {Array}
             */
            getAvailableControllerServices: function (type) {
                return $http.get(RestUrlService.LIST_SERVICES_URL("root"), { params: { type: type } })
                    .then(function (response) {
                    return response.data;
                });
            },
            /**
             * Finds the allowed controller services for the specified property and sets the allowable values.
             *
             * @param {Object} property the property to be updated
             */
            findControllerServicesForProperty: function (property) {
                // Show progress indicator
                property.isLoading = true;
                // Fetch the list of controller services
                data.getAvailableControllerServices(property.propertyDescriptor.identifiesControllerService)
                    .then(function (services) {
                    // Update the allowable values
                    property.isLoading = false;
                    property.propertyDescriptor.allowableValues = _.map(services, function (service) {
                        return { displayName: service.name, value: service.id };
                    });
                }, function () {
                    // Hide progress indicator
                    property.isLoading = false;
                });
            },
            getFeedByName: function (feedName) {
                var deferred = $q.defer();
                $http.get(RestUrlService.FEED_DETAILS_BY_NAME_URL(feedName))
                    .then(function (response) {
                    var feedResponse = response.data;
                    return deferred.resolve(feedResponse);
                });
                return deferred.promise;
            },
            /**
             * Gets the list of available Hive partition functions.
             *
             * @returns {Array.<string>} list of function names
             */
            getPartitionFunctions: function () {
                return $http.get(RestUrlService.PARTITION_FUNCTIONS_URL)
                    .then(function (response) {
                    return response.data;
                });
            },
            /**
             * check if the user has access on an entity
             * @param permissionsToCheck an Array or a single string of a permission/action to check against this entity and current user
             * @param entity the entity to check. if its undefined it will use the current feed in the model
             * @returns {*} a promise, or a true/false.  be sure to wrap this with a $q().when()
             */
            hasEntityAccess: function (permissionsToCheck, entity) {
                if (entity == undefined) {
                    entity = data.model;
                }
                return AccessControlService.hasEntityAccess(permissionsToCheck, entity, EntityAccessControlService.entityRoleTypes.FEED);
            },
            /**
             * Applies the specified domain type to the specified field.
             *
             * @param {Field} field the field to be updated
             * @param {FieldPolicy} policy the field policy to be updated
             * @param {DomainType} domainType the domain type be be applies
             */
            setDomainTypeForField: function (field, policy, domainType) {
                policy.$currentDomainType = domainType;
                policy.domainTypeId = domainType.id;
                if (angular.isObject(domainType.field)) {
                    field.tags = angular.copy(domainType.field.tags);
                    if (angular.isString(domainType.field.derivedDataType) && domainType.field.derivedDataType.length > 0) {
                        field.derivedDataType = domainType.field.derivedDataType;
                        field.precisionScale = domainType.field.precisionScale;
                        field.dataTypeDisplay = data.getDataTypeDisplay(field);
                    }
                }
                if (angular.isObject(domainType.fieldPolicy)) {
                    policy.standardization = angular.copy(domainType.fieldPolicy.standardization);
                    policy.validation = angular.copy(domainType.fieldPolicy.validation);
                }
            }
        };
        data.init();
        return data;
    }
    /**
     * The Controller used for the Feed Saving Dialog
     */
    function FeedSavingDialogController($scope, $mdDialog, message, feedName) {
        $scope.feedName = feedName;
        $scope.message = message;
        $scope.hide = function () {
            $mdDialog.hide();
        };
        $scope.cancel = function () {
            $mdDialog.cancel();
        };
    }
    exports.FeedSavingDialogController = FeedSavingDialogController;
    angular.module(require("feed-mgr/module-name"))
        .factory('FeedService', ["$http", "$q", "$mdToast", "$mdDialog", "RestUrlService", "VisualQueryService", "FeedCreationErrorService", "FeedPropertyService", "AccessControlService",
        "EntityAccessControlService", "StateService", FeedService])
        .controller('FeedSavingDialogController', ["$scope", "$mdDialog", "message", "feedName", FeedSavingDialogController]);
});
//# sourceMappingURL=FeedService.js.map