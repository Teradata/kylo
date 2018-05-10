define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * A cache of the controllerservice Id to its display name.
     * This is used when a user views a feed that has a controller service as a property so it shows the Name (i.e. MySQL)
     * and not the UUID of the service.
     *
     * @type {{}}
     */
    var controllerServiceDisplayCache = {};
    var controllerServiceDisplayCachePromiseTracker = {};
    var FeedService = /** @class */ (function () {
        function FeedService($http, $q, $mdToast, $mdDialog, RestUrlService, VisualQueryService, FeedCreationErrorService, FeedPropertyService, accessControlService, EntityAccessControlService, StateService) {
            var _this = this;
            this.$http = $http;
            this.$q = $q;
            this.$mdToast = $mdToast;
            this.$mdDialog = $mdDialog;
            this.RestUrlService = RestUrlService;
            this.VisualQueryService = VisualQueryService;
            this.FeedCreationErrorService = FeedCreationErrorService;
            this.FeedPropertyService = FeedPropertyService;
            this.accessControlService = accessControlService;
            this.EntityAccessControlService = EntityAccessControlService;
            this.StateService = StateService;
            //data: any= {
            /**
             * The Feed model in the Create Feed Stepper
             */
            this.createFeedModel = {};
            /**
             * The Feed Model that is being Edited when a user clicks on a Feed Details
             */
            this.editFeedModel = {};
            /**
             * Feed model for comparison with editFeedModel in Versions tab
             */
            this.versionFeedModel = {};
            /**
             * Difference between editFeedModel and versionFeedModel
             */
            this.versionFeedModelDiff = [];
            /**
             * The initial CRON expression used when a user selects Cron for the Schedule option
             */
            this.DEFAULT_CRON = "0 0 12 1/1 * ? *";
            /**
             * In the Data Processing section these are the available Strategies a user can choose when defining the feed
             */
            this.mergeStrategies = [
                { name: 'Sync', type: 'SYNC', hint: 'Replace table content', disabled: false },
                { name: 'Rolling sync', type: 'ROLLING_SYNC', hint: 'Replace content in matching partitions' },
                { name: 'Merge', type: 'MERGE', hint: 'Insert all rows', disabled: false },
                { name: 'Dedupe and merge', type: 'DEDUPE_AND_MERGE', hint: 'Insert rows ignoring duplicates', disabled: false },
                { name: 'Merge using primary key', type: 'PK_MERGE', hint: 'Upsert using primary key' }
            ];
            /**
             * The available Target Format options
             */
            this.targetFormatOptions = [{ label: "ORC", value: 'STORED AS ORC' },
                { label: "PARQUET", value: 'STORED AS PARQUET' },
                { label: "AVRO", value: 'STORED AS AVRO' },
                {
                    label: "TEXTFILE",
                    value: 'ROW FORMAT SERDE \'org.apache.hadoop.hive.serde2.OpenCSVSerde\' WITH SERDEPROPERTIES ( \'separatorChar\' = \',\' ,\'escapeChar\' = \'\\\\\' ,\'quoteChar\' = \'"\')'
                        + ' STORED AS'
                        + ' TEXTFILE'
                },
                { label: "RCFILE", value: 'ROW FORMAT SERDE "org.apache.hadoop.hive.serde2.columnar.ColumnarSerDe" STORED AS RCFILE' }];
            /**
             * The available Compression options for a given targetFormat {@see this#targetFormatOptions}
             */
            this.compressionOptions = { "ORC": ['NONE', 'SNAPPY', 'ZLIB'], "PARQUET": ['NONE', 'SNAPPY'], "AVRO": ['NONE'] };
            /**
             * Standard data types for column definitions
             */
            this.columnDefinitionDataTypes = ['string', 'int', 'bigint', 'tinyint', 'decimal', 'double', 'float', 'date', 'timestamp', 'boolean', 'binary'];
            /**
             * Returns an array of all the compression options regardless of the {@code targetFormat}
             * (i.e. ['NONE','SNAPPY','ZLIB']
             * @returns {Array}
             */
            this.allCompressionOptions = function () {
                var arr = [];
                _.each(_this.compressionOptions, function (options) {
                    arr = _.union(arr, options);
                });
                return arr;
            };
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
            this.getNewCreateFeedModel = function () {
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
                    schedule: { schedulingPeriod: _this.DEFAULT_CRON, schedulingStrategy: 'CRON_DRIVEN', concurrentTasks: 1 },
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
                    allowIndexing: true,
                    historyReindexingStatus: 'NEVER_RUN',
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
            };
            this.cloneFeed = function () {
                //copy the feed
                _this.createFeedModel = angular.copy(_this.editFeedModel);
                _this.createFeedModel.id = null;
                _this.createFeedModel.cloned = true;
                _this.createFeedModel.clonedFrom = _this.createFeedModel.feedName;
                _this.createFeedModel.feedName += "_copy";
                _this.createFeedModel.systemFeedName += "_copy";
                _this.createFeedModel.owner = undefined;
                _.each(_this.createFeedModel.table.tableSchema.fields, function (field) {
                    field._id = _.uniqueId();
                });
                _.each(_this.createFeedModel.table.partitions, function (partition) {
                    partition._id = _.uniqueId();
                });
                return _this.createFeedModel;
            };
            /**
             * Called when starting a new feed.
             * This will return the default model and also reset the Query Builder and Error service
             */
            this.newCreateFeed = function () {
                _this.createFeedModel = _this.getNewCreateFeedModel();
                _this.VisualQueryService.resetModel();
                _this.FeedCreationErrorService.reset();
            };
            /**
             * Updates a Feed with another model.
             * The model that is passed in will update the currently model being edited ({@code this.editFeedModel})
             * @param feedModel
             */
            this.updateFeed = function (feedModel) {
                _this.editFeedModel.totalPreSteps = 0;
                _this.editFeedModel.inputProcessorName = null;
                _this.editFeedModel.usedByFeeds = [];
                _this.editFeedModel.description = '';
                _this.editFeedModel.inputProcessor = null;
                angular.extend(_this.editFeedModel, feedModel);
                //set the field name to the policy name attribute
                if (_this.editFeedModel.table != null && _this.editFeedModel.table.fieldPolicies != null) {
                    angular.forEach(_this.editFeedModel.table.fieldPolicies, function (policy, i) {
                        var field = _this.editFeedModel.table.tableSchema.fields[i];
                        if (field != null && field != undefined) {
                            policy.name = field.name;
                            policy.derivedDataType = field.derivedDataType;
                            policy.nullable = field.nullable;
                            policy.primaryKey = field.primaryKey;
                        }
                    });
                }
                //add in the view states
                var defaultView = _this.getNewCreateFeedModel().view;
                _this.editFeedModel.view = defaultView;
            };
            /**
             * Shows the Feed Error Dialog
             * @returns {*}
             */
            this.showFeedErrorsDialog = function () {
                return _this.FeedCreationErrorService.showErrorDialog();
            };
            /**
             * Adds a Nifi Exception error to the Feed Error dialog
             * @param name
             * @param nifiFeed
             */
            this.buildErrorData = function (name, response) {
                this.FeedCreationErrorService.buildErrorData(name, response);
            };
            /**
             * Check to see if there are any errors added to the Error Dialog
             * @returns {*}
             */
            this.hasFeedCreationErrors = function () {
                return this.FeedCreationErrorService.hasErrors();
            };
            /**
             * Resets the Create feed ({@code this.createFeedModel}) object
             */
            this.resetFeed = function () {
                //get the new model and its keys
                var newFeedObj = _this.getNewCreateFeedModel();
                var keys = _.keys(newFeedObj);
                var createFeedModel = angular.extend(_this.createFeedModel, newFeedObj);
                //get the create model and its keys
                var modelKeys = _.keys(_this.createFeedModel);
                //find those that have been added and delete them
                var extraKeys = _.difference(modelKeys, keys);
                _.each(extraKeys, function (key) {
                    delete createFeedModel[key];
                });
                _this.VisualQueryService.resetModel();
                _this.FeedCreationErrorService.reset();
            };
            this.getDataTypeDisplay = function (columnDef) {
                return columnDef.precisionScale != null ? columnDef.derivedDataType + "(" + columnDef.precisionScale + ")" : columnDef.derivedDataType;
            };
            /**
             * returns the Object used for creating the destination schema for each Field
             * This is used in the Table Step to define the schema
             *
             * @returns {{name: string, description: string, dataType: string, precisionScale: null, dataTypeDisplay: Function, primaryKey: boolean, nullable: boolean, createdTracker: boolean,
             *     updatedTracker: boolean, sampleValues: Array, selectedSampleValue: string, isValid: Function, _id: *}}
             */
            this.newTableFieldDefinition = function () {
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
            };
            /**
             * Returns the object used for creating Data Processing policies on a given field
             * This is used in the Data Processing step
             *
             * @param fieldName
             * @returns {{name: (*|string), partition: null, profile: boolean, standardization: null, validation: null}}
             */
            this.newTableFieldPolicy = function (fieldName) {
                return { name: fieldName || '', partition: null, profile: true, standardization: null, validation: null };
            };
            /**
             * For a given list of incoming Table schema fields ({@see this#newTableFieldDefinition}) it will create a new FieldPolicy object ({@see this#newTableFieldPolicy} for it
             */
            this.setTableFields = function (fields, policies) {
                if (policies === void 0) { policies = null; }
                _this.createFeedModel.table.tableSchema.fields = fields;
                _this.createFeedModel.table.fieldPolicies = (policies != null && policies.length > 0) ? policies : fields.map(function (field) { return _this.newTableFieldPolicy(field.name); });
            };
            /**
             * Ensure that the Table Schema has a Field Policy for each of the fields and that their indices are matching.
             */
            this.syncTableFieldPolicyNames = function () {
                var self = _this;
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
            };
            /**
             * return true/false if there is a PK defined for the incoming set of {@code feedModel.table.tableSchema.fields
                 * @param fields
                 * @returns {boolean}
             */
            this.hasPrimaryKeyDefined = function (feedModel) {
                var firstPk = _.find(feedModel.table.tableSchema.fields, function (field) {
                    return field.primaryKey;
                });
                return firstPk != null && firstPk != undefined;
            };
            /**
             * enable/disable the PK Merge strategy enforcing a PK column set.
             * returns if the strategy is valid or not
             *
             * @param feedModel
             * @param strategies
             * @returns {boolean}
             */
            this.enableDisablePkMergeStrategy = function (feedModel, strategies) {
                var pkStrategy = _.find(strategies, function (strategy) {
                    return strategy.type == 'PK_MERGE';
                });
                var selectedStrategy = feedModel.table.targetMergeStrategy;
                if (pkStrategy) {
                    if (!_this.hasPrimaryKeyDefined(feedModel)) {
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
            };
            /**
             * return true/false if there is a
             */
            this.enableDisableRollingSyncMergeStrategy = function (feedModel, strategies) {
                var rollingSyncStrategy = _.find(strategies, function (strategy) {
                    return strategy.type == 'ROLLING_SYNC';
                });
                var selectedStrategy = feedModel.table.targetMergeStrategy;
                if (rollingSyncStrategy) {
                    rollingSyncStrategy.disabled = !_this.hasPartitions(feedModel);
                }
                if (rollingSyncStrategy && selectedStrategy == rollingSyncStrategy.type) {
                    return !rollingSyncStrategy.disabled;
                }
                else {
                    return true;
                }
            };
            this.updateEnabledMergeStrategy = function (feedModel, strategies) {
                _this.enableDisablePkMergeStrategy(feedModel, strategies);
                _this.enableDisableRollingSyncMergeStrategy(feedModel, strategies);
            };
            this.hasPartitions = function (feedModel) {
                return feedModel.table.partitions != null
                    && feedModel.table.partitions != undefined
                    && feedModel.table.partitions.length > 0;
            };
            /**
             * This will clear the Table Schema resetting the method, fields, and policies
             */
            this.clearTableData = function () {
                //this.createFeedModel.table.method = 'MANUAL';
                _this.createFeedModel.table.tableSchema.fields = [];
                _this.createFeedModel.table.fieldPolicies = [];
                _this.createFeedModel.table.existingTableName = null;
            };
            /**
             * In the stepper when a feeds step is complete and validated it will change the Step # icon to a Check circle
             */
            this.updateEditModelStateIcon = function () {
                if (_this.editFeedModel.state == 'ENABLED') {
                    _this.editFeedModel.stateIcon = 'check_circle';
                }
                else {
                    _this.editFeedModel.stateIcon = 'block';
                }
            };
            /**
             * Initialize this object by creating a new empty {@see this#createFeedModel} object
             */
            this.init = function () {
                _this.newCreateFeed();
            };
            /**
             * Before the model is saved to the server this will be called to make any changes
             * @see this#saveFeedModel
             * @param model
             */
            this.prepareModelForSave = function (model) {
                var properties = [];
                if (model.inputProcessor != null) {
                    angular.forEach(model.inputProcessor.properties, function (property) {
                        _this.FeedPropertyService.initSensitivePropertyForSaving(property);
                        properties.push(property);
                    });
                }
                angular.forEach(model.nonInputProcessors, function (processor) {
                    angular.forEach(processor.properties, function (property) {
                        _this.FeedPropertyService.initSensitivePropertyForSaving(property);
                        properties.push(property);
                    });
                });
                if (model.inputProcessor) {
                    model.inputProcessorName = model.inputProcessor.name;
                }
                model.properties = properties;
                //prepare access control changes if any
                _this.EntityAccessControlService.updateRoleMembershipsForSave(model.roleMemberships);
                if (model.cloned) {
                    model.state = null;
                }
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
            };
            /**
             * Show a dialog indicating that the feed is saving
             * @param ev
             * @param message
             * @param feedName
             */
            this.showFeedSavingDialog = function (ev, message, feedName) {
                _this.$mdDialog.show({
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
            };
            /**
             * Hide the Feed Saving Dialog
             */
            this.hideFeedSavingDialog = function () {
                _this.$mdDialog.hide();
            };
            /**
             * Save the model Posting the data to the server
             * @param model
             * @returns {*}
             */
            this.saveFeedModel = function (model) {
                var self = _this;
                self.prepareModelForSave(model);
                var deferred = _this.$q.defer();
                var successFn = function (response) {
                    var invalidCount = 0;
                    if (response.data && response.data.success) {
                        //update the feed versionId and internal id upon save
                        model.id = response.data.feedMetadata.id;
                        model.versionName = response.data.feedMetadata.versionName;
                        this.$mdToast.show(this.$mdToast.simple()
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
                _this.FeedPropertyService.initSensitivePropertiesForEditing(model.properties);
                var promise = _this.$http({
                    url: _this.RestUrlService.CREATE_FEED_FROM_TEMPLATE_URL,
                    method: "POST",
                    data: angular.toJson(copy),
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    }
                }).then(successFn, errorFn);
                return deferred.promise;
            };
            /**
             * Call out to the server to get the System Name for a passed in name
             * @param feedName
             * @returns {HttpPromise}
             */
            this.getSystemName = function (feedName) {
                return _this.$http.get(_this.RestUrlService.GET_SYSTEM_NAME, { params: { name: feedName } });
            };
            /**
             * Call out to the server to get info on whether feed history data reindexing is configured in Kylo
             * @returns {HttpPromise}
             */
            this.isKyloConfiguredForFeedHistoryDataReindexing = function () {
                return _this.$http.get(_this.RestUrlService.FEED_HISTORY_CONFIGURED);
            };
            /**
             * When creating a Feed find the First Column/Field that matches the given name
             * @param name
             * @returns {*|{}}
             */
            this.getColumnDefinitionByName = function (name) {
                return _.find(_this.createFeedModel.table.tableSchema.fields, function (columnDef) {
                    return columnDef.name == name;
                });
            };
            /**
             * Call the server to return a list of Feed Names
             * @returns {HttpPromise}
             */
            this.getFeedSummary = function () {
                var successFn = function (response) {
                    return response.data;
                };
                var errorFn = function (err) {
                };
                var promise = _this.$http.get(_this.RestUrlService.GET_FEED_NAMES_URL);
                promise.then(successFn, errorFn);
                return promise;
            };
            /**
             * Call the server to return a list of Feed Names
             * @returns {HttpPromise}
             */
            this.getFeedNames = function () {
                var successFn = function (response) {
                    return response.data;
                };
                var errorFn = function (err) {
                };
                var promise = _this.$http.get(_this.RestUrlService.OPS_MANAGER_FEED_NAMES);
                promise.then(successFn, errorFn);
                return promise;
            };
            /**
             * Call the server to get a list of all the available Preconditions that can be used when saving/scheduling the feed
             * @returns {HttpPromise}
             */
            this.getPossibleFeedPreconditions = function () {
                var successFn = function (response) {
                    return response.data;
                };
                var errorFn = function (err) {
                    console.log('ERROR ', err);
                };
                var promise = _this.$http.get(_this.RestUrlService.GET_POSSIBLE_FEED_PRECONDITIONS_URL);
                promise.then(successFn, errorFn);
                return promise;
            };
            /**
             * Gets the list of user properties for the specified feed.
             *
             * @param {Object} model the feed model
             * @return {Array.<{key: string, value: string}>} the list of user properties
             */
            this.getUserPropertyList = function (model) {
                var userPropertyList = [];
                angular.forEach(model.userProperties, function (value, key) {
                    if (!key.startsWith("jcr:")) {
                        userPropertyList.push({ key: key, value: value });
                    }
                });
                return userPropertyList;
            };
            /**
             * Gets the user fields for a new feed.
             *
             * @param {string} categoryId the category id
             * @returns {Promise} for the user fields
             */
            this.getUserFields = function (categoryId) {
                return _this.$http.get(_this.RestUrlService.GET_FEED_USER_FIELDS_URL(categoryId))
                    .then(function (response) {
                    return response.data;
                });
            };
            /**
             * Gets the controller services of the specified type.
             *
             * @param {string} type a type class
             * @returns {Array}
             */
            this.getAvailableControllerServices = function (type) {
                return _this.$http.get(_this.RestUrlService.LIST_SERVICES_URL("root"), { params: { type: type } })
                    .then(function (response) {
                    return response.data;
                });
            };
            this.setControllerServicePropertyDisplayName = function (property) {
                var setDisplayValue = function (property) {
                    var cacheEntry = controllerServiceDisplayCache[property.value];
                    if (cacheEntry != null) {
                        property.displayValue = cacheEntry;
                        return true;
                    }
                    return false;
                };
                if (angular.isObject(property.propertyDescriptor) && angular.isString(property.propertyDescriptor.identifiesControllerService)) {
                    if (!setDisplayValue(property)) {
                        var entry_1 = controllerServiceDisplayCachePromiseTracker[property.propertyDescriptor.identifiesControllerService];
                        if (entry_1 == undefined) {
                            var promise = _this.getAvailableControllerServices(property.propertyDescriptor.identifiesControllerService);
                            entry_1 = { request: promise, waitingProperties: [] };
                            entry_1.waitingProperties.push(property);
                            controllerServiceDisplayCachePromiseTracker[property.propertyDescriptor.identifiesControllerService] = entry_1;
                            promise.then(function (services) {
                                _.each(services, function (service) {
                                    controllerServiceDisplayCache[service.id] = service.name;
                                });
                                _.each(entry_1.waitingProperties, function (property) {
                                    setDisplayValue(property);
                                });
                                delete controllerServiceDisplayCachePromiseTracker[property.propertyDescriptor.identifiesControllerService];
                            });
                        }
                        else {
                            entry_1.waitingProperties.push(property);
                        }
                    }
                }
            };
            /**
             * Finds the allowed controller services for the specified property and sets the allowable values.
             *
             * @param {Object} property the property to be updated
             */
            this.findControllerServicesForProperty = function (property) {
                // Show progress indicator
                property.isLoading = true;
                // Fetch the list of controller services
                _this.getAvailableControllerServices(property.propertyDescriptor.identifiesControllerService)
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
            };
            this.getFeedByName = function (feedName) {
                var deferred = _this.$q.defer();
                _this.$http.get(_this.RestUrlService.FEED_DETAILS_BY_NAME_URL(feedName))
                    .then(function (response) {
                    var feedResponse = response.data;
                    return deferred.resolve(feedResponse);
                });
                return deferred.promise;
            };
            /**
             * Gets the list of available Hive partition functions.
             *
             * @returns {Array.<string>} list of function names
             */
            this.getPartitionFunctions = function () {
                return _this.$http.get(_this.RestUrlService.PARTITION_FUNCTIONS_URL)
                    .then(function (response) {
                    return response.data;
                });
            };
            this.getFeedVersions = function (feedId) {
                var successFn = function (response) {
                    return response.data;
                };
                var errorFn = function (err) {
                    console.log('ERROR ', err);
                };
                return _this.$http.get(_this.RestUrlService.FEED_VERSIONS_URL(feedId)).then(successFn, errorFn);
            };
            this.getFeedVersion = function (feedId, versionId) {
                var successFn = function (response) {
                    return response.data;
                };
                var errorFn = function (err) {
                    console.log('ERROR ', err);
                };
                return _this.$http.get(_this.RestUrlService.FEED_VERSION_ID_URL(feedId, versionId)).then(successFn, errorFn);
            };
            this.diffFeedVersions = function (feedId, versionId1, versionId2) {
                var successFn = function (response) {
                    return response.data;
                };
                var errorFn = function (err) {
                    console.log('ERROR ', err);
                };
                return _this.$http.get(_this.RestUrlService.FEED_VERSIONS_DIFF_URL(feedId, versionId1, versionId2)).then(successFn, errorFn);
            };
            /**
             * check if the user has access on an entity
             * @param permissionsToCheck an Array or a single string of a permission/action to check against this entity and current user
             * @param entity the entity to check. if its undefined it will use the current feed in the model
             * @returns {*} a promise, or a true/false.  be sure to wrap this with a $q().when()
             */
            this.hasEntityAccess = function (permissionsToCheck, entity) {
                if (entity == undefined) {
                    // entity = this.model; @Greg model is not defined anywhere inside this service what is it?
                }
                return _this.accessControlService.hasEntityAccess(permissionsToCheck, entity, _this.EntityAccessControlService.entityRoleTypes.FEED);
            };
            /**
             * Applies the specified domain type to the specified field.
             *
             * @param {Field} field the field to be updated
             * @param {FieldPolicy} policy the field policy to be updated
             * @param {DomainType} domainType the domain type be be applies
             */
            this.setDomainTypeForField = function (field, policy, domainType) {
                policy.$currentDomainType = domainType;
                policy.domainTypeId = domainType.id;
                if (angular.isObject(domainType.field)) {
                    field.tags = angular.copy(domainType.field.tags);
                    if (angular.isString(domainType.field.name) && domainType.field.name.length > 0) {
                        field.name = domainType.field.name;
                    }
                    if (angular.isString(domainType.field.derivedDataType) && domainType.field.derivedDataType.length > 0) {
                        field.derivedDataType = domainType.field.derivedDataType;
                        field.precisionScale = domainType.field.precisionScale;
                        field.dataTypeDisplay = _this.getDataTypeDisplay(field);
                    }
                }
                if (angular.isObject(domainType.fieldPolicy)) {
                    policy.standardization = angular.copy(domainType.fieldPolicy.standardization);
                    policy.validation = angular.copy(domainType.fieldPolicy.validation);
                }
            };
            /**
             * Returns operation of the difference at given path for versioned feed
             * @param path current diff model
             * @returns {string} operation type, e.g. add, remove, update, no-change
             */
            this.diffOperation = function (path) {
                return _this.versionFeedModelDiff && _this.versionFeedModelDiff[path] ? _this.versionFeedModelDiff[path].op : 'no-change';
            };
            this.diffCollectionOperation = function (path) {
                var self = _this;
                if (_this.versionFeedModelDiff) {
                    if (_this.versionFeedModelDiff[path]) {
                        return _this.versionFeedModelDiff[path].op;
                    }
                    else {
                        var patch_1 = { op: 'no-change' };
                        _.each(_.values(_this.versionFeedModelDiff), function (p) {
                            if (p.path.startsWith(path + "/")) {
                                patch_1.op = self.joinVersionOperations(patch_1.op, p.op);
                            }
                        });
                        return patch_1.op;
                    }
                }
                return 'no-change';
            };
            this.joinVersionOperations = function (op1, op2) {
                var opLevels = { 'no-change': 0, 'add': 1, 'remove': 1, 'replace': 2 };
                if (opLevels[op1] === opLevels[op2] && op1 !== 'no-change') {
                    return 'replace';
                }
                return opLevels[op1] > opLevels[op2] ? op1 : op2;
            };
            this.resetVersionFeedModel = function () {
                _this.versionFeedModel = {};
                _this.versionFeedModelDiff = {};
            };
            this.init();
            //return this.data;
        } // constructor terminating here
        FeedService.prototype.trim = function (str) {
            return str.replace(/^\s+|\s+$/g, "");
        };
        FeedService.prototype.toCamel = function (str) {
            return str.replace(/(\-[a-z])/g, function ($1) {
                return $1.toUpperCase().replace('-', '');
            });
        };
        FeedService.prototype.toDash = function (str) {
            return str.replace(/([A-Z])/g, function ($1) {
                return "-" + $1.toLowerCase();
            });
        };
        FeedService.prototype.spacesToUnderscore = function (str) {
            return str.replace(/\s+/g, '_');
        };
        FeedService.prototype.toUnderscore = function (str) {
            return str.replace(/(?:^|\.?)([A-Z])/g, function (x, y) {
                return "_" + y.toLowerCase();
            }).replace(/^_/, "");
            //return str.replace(/([A-Z])/g, "_$1").replace(/^_/,'').toLowerCase();
        };
        //}
        FeedService.$inject = ["$http", "$q", "$mdToast", "$mdDialog", "RestUrlService", "VisualQueryService", "FeedCreationErrorService", "FeedPropertyService", "AccessControlService",
            "EntityAccessControlService", "StateService"];
        return FeedService;
    }());
    exports.FeedService = FeedService;
    /**
     * The Controller used for the Feed Saving Dialog
     */
    var FeedSavingDialogController = /** @class */ (function () {
        function FeedSavingDialogController($scope, $mdDialog, message, feedName) {
            var _this = this;
            this.$scope = $scope;
            this.$mdDialog = $mdDialog;
            this.message = message;
            this.feedName = feedName;
            $scope.feedName = feedName;
            $scope.message = message;
            this.$scope.hide = function () {
                _this.$mdDialog.hide();
            };
            this.$scope.cancel = function () {
                _this.$mdDialog.cancel();
            };
        }
        FeedSavingDialogController.$inject = ["$scope", "$mdDialog", "message", "feedName"];
        return FeedSavingDialogController;
    }());
    exports.FeedSavingDialogController = FeedSavingDialogController;
    angular.module(require("feed-mgr/module-name"))
        .service('FeedService', FeedService)
        .controller('FeedSavingDialogController', FeedSavingDialogController);
});
//# sourceMappingURL=FeedService.js.map