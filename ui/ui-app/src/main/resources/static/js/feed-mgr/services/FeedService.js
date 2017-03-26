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
/**
 *
 */
define(['angular','feed-mgr/module-name'], function (angular,moduleName) {
    angular.module(moduleName).factory('FeedService',["$http","$q","$mdToast","$mdDialog","RestUrlService","VisualQueryService","FeedCreationErrorService","FeedPropertyService",
        function ($http, $q, $mdToast, $mdDialog, RestUrlService, VisualQueryService, FeedCreationErrorService,FeedPropertyService) {

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
                    return "_" + y.toLowerCase()
                }).replace(/^_/, "")
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
                    {name: 'Sync', type: 'SYNC', hint: 'Replace table content', disabled: false},
                    {name: 'Rolling sync', type: 'ROLLING_SYNC', hint: 'Replace content in matching partitions'},
                    {name: 'Merge', type: 'MERGE', hint: 'Insert all rows', disabled: false},
                    {name: 'Dedupe and merge', type: 'DEDUPE_AND_MERGE', hint: 'Insert rows ignoring duplicates', disabled: false},
                    {name: 'Merge using primary key', type: 'PK_MERGE', hint: 'Upsert using primary key'}
                ],

                /**
                 * The available Target Format options
                 */
                targetFormatOptions: [{label: "ORC", value: 'STORED AS ORC'},
                    {label: "PARQUET", value: 'STORED AS PARQUET'},
                    {label: "AVRO", value: 'STORED AS AVRO'},
                    {
                        label: "TEXTFILE",
                        value: 'ROW FORMAT SERDE \'org.apache.hadoop.hive.serde2.OpenCSVSerde\' WITH SERDEPROPERTIES ( \'separatorChar\' = \',\' ,\'escapeChar\' = \'\\\\\' ,\'quoteChar\' = \'"\')'
                               + ' STORED AS'
                               + ' TEXTFILE'
                    },
                    {label: "RCFILE", value: 'ROW FORMAT SERDE "org.apache.hadoop.hive.serde2.columnar.ColumnarSerDe" STORED AS RCFILE'}],
                /**
                 * The available Compression options for a given targetFormat {@see this#targetFormatOptions}
                 */
                compressionOptions: {"ORC": ['NONE', 'SNAPPY', 'ZLIB'], "PARQUET": ['NONE', 'SNAPPY'], "AVRO": ['NONE']},

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
                        description: null,
                        systemFeedName: '',
                        inputProcessorType: '',
                        inputProcessor: null,
                        nonInputProcessors: [],
                        properties: [],
                        securityGroups: [],
                        schedule: {schedulingPeriod: data.DEFAULT_CRON, schedulingStrategy: 'CRON_DRIVEN', concurrentTasks: 1},
                        defineTable: false,
                        allowPreconditions: false,
                        dataTransformationFeed: false,
                        table: {
                            tableSchema: {name: null, fields: []},
                            sourceTableSchema: {name: null, tableSchema: null, fields: []},
                            feedTableSchema: {name: null, fields: []},
                            method: 'SAMPLE_FILE',
                            existingTableName: null,
                            structured: false,
                            targetMergeStrategy: 'DEDUPE_AND_MERGE',
                            feedFormat: 'ROW FORMAT SERDE \'org.apache.hadoop.hive.serde2.OpenCSVSerde\' WITH SERDEPROPERTIES ( \'separatorChar\' = \',\' ,\'escapeChar\' = \'\\\\\' ,\'quoteChar\' = \'"\')'
                                        + ' STORED AS'
                                        + ' TEXTFILE',
                            targetFormat: null,
                            fieldPolicies: [],
                            partitions: [],
                            options: {compress: false, compressionFormat: null, auditLogging: true, encrypt: false, trackHistory: false},
                            sourceTableIncrementalDateField: null
                        },
                        category: {id: null, name: null},
                        dataOwner: '',
                        tags: [],
                        reusableFeed: false,
                        dataTransformation: {chartViewModel: null, dataTransformScript: null, sql: null, states: []},
                        userProperties: [],
                        options: {skipHeader: false},
                        active: true
                    };
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
                buildErrorData: function (name, nifiFeed) {
                    FeedCreationErrorService.buildErrorData(name, nifiFeed);
                },
                /**
                 * Check to see if there are any errors added to the Error Dialog
                 * @returns {*}
                 */
                hasFeedCreationErrors: function () {
                    return FeedCreationErrorService.hasErrors();
                },

                /**
                 * For a Feed find the first property for a given processor name
                 * @param model
                 * @param processorName
                 * @param propertyKey
                 * @returns {*|{}}
                 */
                findPropertyForProcessor: function (model, processorName, propertyKey) {
                    var property = _.find(model.inputProcessor.properties, function (property) {
                        //return property.key = 'Source Database Connection';
                        return property.key == key;
                    });

                    if (property == undefined) {
                        for (processorId in model.nonInputProcessors) {
                            var processor = null;
                            var aProcessor = model[processorId];
                            if (processorName != undefined && processorName != null) {
                                if (aProcessor.processorName == processorName) {
                                    processor = aProcessor;
                                }
                            }
                            else {
                                processor = aProcessor;
                            }
                            if (processor != null) {
                                property = _.find(processor.properties, function (property) {
                                    return property.key == propertyKey;
                                });
                            }
                            if (property != undefined) {
                                break;
                            }
                        }
                    }
                    return property;

                },
                /**
                 * Resets the Create feed ({@code this.createFeedModel}) object
                 */
                resetFeed: function () {
                    angular.extend(this.createFeedModel, this.getNewCreateFeedModel());
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
                        derivedDataType: '',
                        precisionScale: null,
                        dataTypeDisplay: '',
                        primaryKey: false,
                        nullable: false,
                        createdTracker: false,
                        updatedTracker: false,
                        sampleValues: [],
                        selectedSampleValue: '',
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
                    return {name: fieldName || '', partition: null, profile: true, standardization: null, validation: null};
                },
                /**
                 * For a given list of incoming Table schema fields ({@see this#newTableFieldDefinition}) it will create a new FieldPolicy object ({@see this#newTableFieldPolicy} for it
                 * @param fields
                 */
                setTableFields: function (fields) {
                    var self = this;
                    this.createFeedModel.table.tableSchema.fields = [];
                    this.createFeedModel.table.fieldPolicies = [];
                    angular.forEach(fields, function (field) {
                        self.createFeedModel.table.fieldPolicies.push(self.newTableFieldPolicy(field.name))
                    });
                    self.createFeedModel.table.tableSchema.fields = fields;
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
                        return field.primaryKey
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
                        return strategy.type == 'PK_MERGE'
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
                    } else {
                        return true;
                    }
                },

                updateEnabledMergeStrategy: function (feedModel, strategies) {
                    // If data transformation then only support sync
                    if (feedModel.dataTransformationFeed) {
                        feedModel.table.targetMergeStrategy = 'SYNC';
                        angular.forEach(strategies, function (strat) {
                            strat.disabled = true;
                        });
                    } else {
                        this.enableDisablePkMergeStrategy(feedModel, strategies);
                        this.enableDisableRollingSyncMergeStrategy(feedModel, strategies);
                    }

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
                        this.editFeedModel.stateIcon = 'check_circle'
                    }
                    else {
                        this.editFeedModel.stateIcon = 'block'
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
                            FeedPropertyService.initSensitivePropertyForSaving(property)
                            properties.push(property);
                        });
                    }

                    angular.forEach(model.nonInputProcessors, function (processor) {
                        angular.forEach(processor.properties, function (property) {
                            FeedPropertyService.initSensitivePropertyForSaving(property)
                            properties.push(property);
                        });
                    });
                    model.properties = properties;

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

                            // structured files must use the original names
                            if (model.table.structured == true) {
                                feedField.name = columnDef.origName;
                                feedField.derivedDataType = columnDef.origDataType;
                            } else if (model.table.method == 'EXISTING_TABLE') {
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

                            } else {
                                // For files the feed table must contain all the columns from the source even if unused in the target
                                if (model.table.method == 'SAMPLE_FILE') {
                                    feedFields.push(feedField);
                                } else if (model.table.method == 'EXISTING_TABLE' && model.table.sourceTableIncrementalDateField == sourceField.name) {
                                    feedFields.push(feedField);
                                    sourceFields.push(sourceField);
                                }
                            }
                        });
                        model.table.fieldPolicies = newPolicies;
                        model.table.tableSchema.fields = newFields;

                        if (model.table.sourceTableSchema == undefined) {
                            model.table.sourceTableSchema = {name: null, tableSchema: null, fields: []};
                        }
                        //only set the sourceFields if its the first time creating this feed
                        if (model.id == null) {
                            model.table.sourceTableSchema.fields = sourceFields;
                        }
                        if (model.table.feedTableSchema == undefined) {
                            model.table.feedTableSchema = {name: null, fields: []};
                        }
                        model.table.feedTableSchema.fields = feedFields;

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


                            $mdToast.show(
                                $mdToast.simple()
                                    .textContent('Feed successfully saved')
                                    .hideDelay(3000)
                            );
                            deferred.resolve(response);
                        }
                        else {
                            deferred.reject(response);
                        }

                    }
                    var errorFn = function (err) {
                        deferred.reject(err);
                    }
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

                    return $http.get(RestUrlService.GET_SYSTEM_NAME, {params: {name: feedName}});

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
                getFeedNames: function () {

                    var successFn = function (response) {
                        return response.data;
                    }
                    var errorFn = function (err) {

                    }
                    var promise = $http.get(RestUrlService.GET_FEED_NAMES_URL);
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
                    }
                    var errorFn = function (err) {
                        console.log('ERROR ', err)
                    }
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
                            userPropertyList.push({key: key, value: value});
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
                    return $http.get(RestUrlService.LIST_SERVICES_URL("root"), {params: {type: type}})
                        .then(function (response) {
                            return response.data;
                        });
                },
                /**
                 * Finds the allowed controller services for the specified property and sets the allowable values.
                 *
                 * @param {Object} property the property to be updated
                 */
                findControllerServicesForProperty: function(property) {
                    // Show progress indicator
                    property.isLoading = true;

                    // Fetch the list of controller services
                    data.getAvailableControllerServices(property.propertyDescriptor.identifiesControllerService)
                        .then(function(services) {
                            // Update the allowable values
                            property.isLoading = false;
                            property.propertyDescriptor.allowableValues = _.map(services, function(service) {
                                return {displayName: service.name, value: service.id}
                            });
                        }, function() {
                            // Hide progress indicator
                            property.isLoading = false;
                        });
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
                }
            };
            data.init();
            return data;

        }]);

    /**
     * The Controller used for the Feed Saving Dialog
     */
        var controller = function ($scope, $mdDialog, message, feedName) {
            var self = this;

            $scope.feedName = feedName;
            $scope.message = message;

            $scope.hide = function () {
                $mdDialog.hide();
            };

            $scope.cancel = function () {
                $mdDialog.cancel();
            };

        };

        angular.module(moduleName).controller('FeedSavingDialogController', ["$scope","$mdDialog","message","feedName",controller]);

});