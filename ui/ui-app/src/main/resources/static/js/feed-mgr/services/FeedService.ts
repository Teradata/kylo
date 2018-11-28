import * as angular from "angular";
import * as _ from "underscore";
import {DomainType} from "./DomainTypesService";
import {AccessControlService} from "../../services/AccessControlService";
import {TableColumnDefinition} from "../model/TableColumnDefinition";
import {TableFieldPolicy} from "../model/TableFieldPolicy";
import {EntityAccessControlService} from "../shared/entity-access-control/EntityAccessControlService";
import {FeedConstants} from "./FeedConstants";
import {Common} from '../../../lib/common/CommonTypes';

/**
 * A cache of the controllerservice Id to its display name.
 * This is used when a user views a feed that has a controller service as a property so it shows the Name (i.e. MySQL)
 * and not the UUID of the service.
 *
 * @type {{}}
 */
let controllerServiceDisplayCache :Common.Map<string> = {};
let controllerServiceDisplayCachePromiseTracker: any = {};

export class FeedService {
   //data: any= {
        /**
         * The Feed model in the Create Feed Stepper
         */
        createFeedModel:any= {};
        /**
         * The Feed Model that is being Edited when a user clicks on a Feed Details
         */
        editFeedModel:any= {};
        /**
         * Feed model for comparison with editFeedModel in Versions tab
         */
        versionFeedModel:any= {};
        /**
         * Difference between editFeedModel and versionFeedModel
         */
        versionFeedModelDiff:any= [];
        /**
         * The initial CRON expression used when a user selects Cron for the Schedule option
         */
        DEFAULT_CRON:string= FeedConstants.DEFAULT_CRON;

        /**
         * In the Data Processing section these are the available Strategies a user can choose when defining the feed
         */
        mergeStrategies:any= FeedConstants.mergeStrategies;

        /**
         * The available Target Format options
         */
        targetFormatOptions:any= FeedConstants.targetFormatOptions;
        /**
         * The available Compression options for a given targetFormat {@see this#targetFormatOptions}
         */
        compressionOptions:any= FeedConstants.compressionOptions;

        /**
         * Standard data types for column definitions
         */
        columnDefinitionDataTypes:any= FeedConstants.columnDefinitionDataTypes;
        /**
         * Returns an array of all the compression options regardless of the {@code targetFormat}
         * (i.e. ['NONE','SNAPPY','ZLIB']
         * @returns {Array}
         */
        allCompressionOptions= FeedConstants.allCompressionOptions;
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
            getNewCreateFeedModel= () => {
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
                    schedule: { schedulingPeriod: this.DEFAULT_CRON, schedulingStrategy: 'CRON_DRIVEN', concurrentTasks: 1 },
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
                    targetFormat: 'STORED AS ORC',
                    feedTblProperties: '',
                        fieldPolicies: [],
                        partitions: [],
                        options: {compress: false, compressionFormat: 'NONE', auditLogging: true, encrypt: false, trackHistory: false},
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
                } as any;
            };
            cloneFeed = () => {
                //copy the feed
                this.createFeedModel = angular.copy(this.editFeedModel);
                this.createFeedModel.id = null;
                this.createFeedModel.cloned = true;
                this.createFeedModel.clonedFrom = this.createFeedModel.feedName;
                this.createFeedModel.feedName += "_copy";
                this.createFeedModel.systemFeedName += "_copy";
                this.createFeedModel.owner = undefined;
                _.each(this.createFeedModel.table.tableSchema.fields, (field: any) =>{
                    field._id = _.uniqueId();
                });
                _.each(this.createFeedModel.table.partitions, (partition: any) =>{
                    partition._id = _.uniqueId();
                });
                return this.createFeedModel;
            }
            /**
             * Called when starting a new feed.
             * This will return the default model and also reset the Query Builder and Error service
             */
            newCreateFeed= () =>{
                this.createFeedModel = this.getNewCreateFeedModel();
                this.visualQueryService.resetModel();
                this.FeedCreationErrorService.reset();
            }
            /**
             * Updates a Feed with another model.
             * The model that is passed in will update the currently model being edited ({@code this.editFeedModel})
             * @param feedModel
             */
            updateFeed= (feedModel: any) =>{
                this.editFeedModel.totalPreSteps = 0;
                this.editFeedModel.inputProcessorName = null;
                this.editFeedModel.usedByFeeds = [];
                this.editFeedModel.description = '';
                this.editFeedModel.inputProcessor = null;
                angular.extend(this.editFeedModel, feedModel);

                //set the field name to the policy name attribute
                if (this.editFeedModel.table != null && this.editFeedModel.table.fieldPolicies != null) {
                    angular.forEach(this.editFeedModel.table.fieldPolicies, (policy: any, i: any) =>{
                        var field = this.editFeedModel.table.tableSchema.fields[i];
                        if (field != null && field != undefined) {
                            policy.name = field.name;
                            policy.derivedDataType = field.derivedDataType;
                            policy.nullable = field.nullable;
                            policy.primaryKey = field.primaryKey;
                        }
                    });
                }

                //add in the view states
                var defaultView = this.getNewCreateFeedModel().view;
                this.editFeedModel.view = defaultView;

            }
            /**
             * Shows the Feed Error Dialog
             * @returns {*}
             */
            showFeedErrorsDialog= () =>{
                return this.FeedCreationErrorService.showErrorDialog();
            }
            /**
             * Adds a Nifi Exception error to the Feed Error dialog
             * @param name
             * @param nifiFeed
             */
            buildErrorData= function (name: any, response: any) {
                this.FeedCreationErrorService.buildErrorData(name, response);
            }
            /**
             * Check to see if there are any errors added to the Error Dialog
             * @returns {*}
             */
            hasFeedCreationErrors= function () {
                return this.FeedCreationErrorService.hasErrors();
            }

            /**
             * Resets the Create feed ({@code this.createFeedModel}) object
             */
            resetFeed= () =>{
                //get the new model and its keys
                var newFeedObj = this.getNewCreateFeedModel();
                var keys = _.keys(newFeedObj)
                var createFeedModel = angular.extend(this.createFeedModel, newFeedObj);

                //get the create model and its keys
                var modelKeys = _.keys(this.createFeedModel);

                //find those that have been added and delete them
                var extraKeys = _.difference(modelKeys, keys);
                _.each(extraKeys, function (key: any) {
                    delete createFeedModel[key];
                })

                this.visualQueryService.resetModel();
                this.FeedCreationErrorService.reset();
            }

            getDataTypeDisplay(columnDef: TableColumnDefinition) {
                return columnDef.getDataTypeDisplay();
            }

            /**
             * returns the Object used for creating the destination schema for each Field
             * This is used in the Table Step to define the schema
             *
             * @returns {{name: string, description: string, dataType: string, precisionScale: null, dataTypeDisplay: Function, primaryKey: boolean, nullable: boolean, createdTracker: boolean,
             *     updatedTracker: boolean, sampleValues: Array, selectedSampleValue: string, isValid: Function, _id: *}}
             */
            newTableFieldDefinition(): TableColumnDefinition {
                return new TableColumnDefinition();
            }
            /**
             * Returns the object used for creating Data Processing policies on a given field
             * This is used in the Data Processing step
             *
             * @param fieldName
             * @returns {{name: (*|string), partition: null, profile: boolean, standardization: null, validation: null}}
             */
            newTableFieldPolicy(fieldName: string): TableFieldPolicy {
                return TableFieldPolicy.forName(fieldName);
                // return {name: fieldName || '', partition: null, profile: true, standardization: null, validation: null};
            }
            /**
             * For a given list of incoming Table schema fields ({@see this#newTableFieldDefinition}) it will create a new FieldPolicy object ({@see this#newTableFieldPolicy} for it
             */
            setTableFields(fields: any[], policies: any[] = null) {
                //ensure the fields are of type TableColumnDefinition
                let newFields =  _.map(fields,(field) => {
                    if(!field['objectType'] || field['objectType'] != 'TableColumnDefinition' ){
                        let columnDef = new TableColumnDefinition();
                        angular.extend(columnDef,field);
                        return columnDef;
                    }
                    else {
                        return field;
                    }
                })
                this.createFeedModel.table.tableSchema.fields = newFields;
                this.createFeedModel.table.fieldPolicies = (policies != null && policies.length > 0) ? policies : newFields.map(field => this.newTableFieldPolicy(field.name));

                this.createFeedModel.schemaChanged = !this.validateSchemaDidNotChange(this.createFeedModel);
            }
            /**
             * Ensure that the Table Schema has a Field Policy for each of the fields and that their indices are matching.
             */
            syncTableFieldPolicyNames= () => {
                var self = this;
                angular.forEach(self.createFeedModel.table.tableSchema.fields, (columnDef: any, index: any) => {
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
            }
            /**
             * return true/false if there is a PK defined for the incoming set of {@code feedModel.table.tableSchema.fields
                 * @param fields
                 * @returns {boolean}
             */
            hasPrimaryKeyDefined= (feedModel: any) => {
                var firstPk = _.find(feedModel.table.tableSchema.fields, (field: any) => {
                    return field.primaryKey
                });
                return firstPk != null && firstPk != undefined;
            }

            /**
             * enable/disable the PK Merge strategy enforcing a PK column set.
             * returns if the strategy is valid or not
             *
             * @param feedModel
             * @param strategies
             * @returns {boolean}
             */
            enableDisablePkMergeStrategy= (feedModel: any, strategies: any) => {
                var pkStrategy = _.find(strategies, (strategy: any) => {
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

            }

            /**
             * return true/false if there is a
             */
            enableDisableRollingSyncMergeStrategy= (feedModel: any, strategies: any) => {
                var rollingSyncStrategy = _.find(strategies, (strategy: any) => {
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
            }

            updateEnabledMergeStrategy= (feedModel: any, strategies: any) => {
                this.enableDisablePkMergeStrategy(feedModel, strategies);
                this.enableDisableRollingSyncMergeStrategy(feedModel, strategies);
            }

            hasPartitions= (feedModel: any) => {
                return feedModel.table.partitions != null
                    && feedModel.table.partitions != undefined
                    && feedModel.table.partitions.length > 0;
            }

            /**
             * This will clear the Table Schema resetting the method, fields, and policies
             */
            clearTableData= ()  => {

                //this.createFeedModel.table.method = 'MANUAL';
                this.createFeedModel.table.tableSchema.fields = [];
                this.createFeedModel.table.fieldPolicies = [];
                this.createFeedModel.table.existingTableName = null;
            }
            /**
             * In the stepper when a feeds step is complete and validated it will change the Step # icon to a Check circle
             */
            updateEditModelStateIcon= () => {
                if (this.editFeedModel.state == 'ENABLED') {
                    this.editFeedModel.stateIcon = 'check_circle'
                }
                else {
                    this.editFeedModel.stateIcon = 'block'
                }
            }
            /**
             * Initialize this object by creating a new empty {@see this#createFeedModel} object
             */
            init= () => {
                this.newCreateFeed();
            }
            /**
             * Before the model is saved to the server this will be called to make any changes
             * @see this#saveFeedModel
             * @param model
             */
            prepareModelForSave= (model: any) => {
                var properties: any[] = [];
                var copy = angular.copy(model);

            if (copy.inputProcessor != null) {
                angular.forEach(copy.inputProcessor.properties, (property: any) => {
                        this.FeedPropertyService.initSensitivePropertyForSaving(property)
                        properties.push(property);
                    });
                }

            angular.forEach(copy.nonInputProcessors, (processor: any) => {
                    angular.forEach(processor.properties, (property: any) => {
                        this.FeedPropertyService.initSensitivePropertyForSaving(property)
                        properties.push(property);
                    });
                });
            if(copy.inputProcessor) {
                copy.inputProcessorName = copy.inputProcessor.name;
                }
            copy.properties = properties;

            //Force this feed to always be a final feed
            //Any save through this service is not a DRAFT feed
            copy.mode="COMPLETE";

            //clear the extra UI only properties
            copy.inputProcessor = null
            copy.nonInputProcessors = null

                //prepare access control changes if any
                this.entityAccessControlService.updateRoleMembershipsForSave(copy.roleMemberships);

            if(copy.cloned){
                copy.state = null;
                }
            //remove the self.model.originalTableSchema if its there
            delete copy.originalTableSchema;

            //delete the fileParser options
            delete copy.schemaParser;

            if (copy.table && copy.table.fieldPolicies && copy.table.tableSchema && copy.table.tableSchema.fields) {
                // Set feed

                    var newFields: any[] = [];
                    var newPolicies: any[] = [];
                    var feedFields: any[] = [];
                    var sourceFields: any[] = [];
                angular.forEach(copy.table.tableSchema.fields, (columnDef, idx) => {
                    var policy = copy.table.fieldPolicies[idx];
                        var policy = model.table.fieldPolicies[idx];
                        var sourceField = angular.copy(columnDef);
                        var feedField = angular.copy(columnDef);

                        sourceField.name = columnDef.origName;
                        sourceField.derivedDataType = columnDef.origDataType;
                        // structured files must use the original names
                        if (copy.table.structured == true) {
                            feedField.name = columnDef.origName;
                            feedField.derivedDataType = columnDef.origDataType;
                        } else if (copy.table.method == 'EXISTING_TABLE') {
                            sourceField.name = columnDef.origName;
                        }
                        if (angular.isDefined(policy)) {
                            policy.feedFieldName = feedField.name;
                            policy.name = columnDef.name;
                        }

                        if (!columnDef.deleted) {
                        //remove sample values
                        columnDef.sampleValues = null;
                        columnDef.history = null;
                            newFields.push(columnDef);
                            if (angular.isDefined(policy)) {
                                newPolicies.push(policy);
                            }
                        sourceField.sampleValues = null;
                        sourceField.history = null;
                        feedField.sampleValues = null;
                        feedField.history = null;
                            sourceFields.push(sourceField);
                            feedFields.push(feedField);

                        } else {
                            // For files the feed table must contain all the columns from the source even if unused in the target
                            if (copy.table.method == 'SAMPLE_FILE') {
                                feedFields.push(feedField);
                            } else if (copy.table.method == 'EXISTING_TABLE' && copy.table.sourceTableIncrementalDateField == sourceField.name) {
                                feedFields.push(feedField);
                                sourceFields.push(sourceField);
                            }
                        }
                    });
                copy.table.fieldPolicies = newPolicies;
                copy.table.tableSchema.fields = newFields;

                if (copy.table.sourceTableSchema == undefined) {
                    copy.table.sourceTableSchema = {name: null, tableSchema: null, fields: []};
                    }
                    //only set the sourceFields if its the first time creating this feed
                if (copy.id == null) {
                    copy.table.sourceTableSchema.fields = sourceFields;
                    copy.table.feedTableSchema.fields = feedFields;
                    }
                if (copy.table.feedTableSchema == undefined) {
                    copy.table.feedTableSchema = {name: null, fields: []};
                    }


                    //remove any extra columns in the policies
                    /*
                     while(model.table.fieldPolicies.length > model.table.tableSchema.fields.length) {
                     model.table.fieldPolicies.splice(model.table.tableSchema.fields.length, 1);
                     }
                     */
                }
            if (copy.registeredTemplate) {
                copy.registeredTemplate = undefined;
            }

            return copy;


            }
            /**
             * Show a dialog indicating that the feed is saving
             * @param ev
             * @param message
             * @param feedName
             */
            showFeedSavingDialog= (ev: any, message: any, feedName: any) => {
                this.$mdDialog.show({
                    controller: 'FeedSavingDialogController',
                    templateUrl: '../feeds/edit-feed/details/feed-saving-dialog.html',
                    parent: angular.element(document.body),
                    targetEvent: ev,
                    clickOutsideToClose: false,
                    fullscreen: true,
                    locals: {
                        message: message,
                        feedName: feedName
                    }
                })
                    .then((answer: any) => {
                        //do something with result
                    }, function () {
                        //cancelled the dialog
                    });
            }
            /**
             * Hide the Feed Saving Dialog
             */
            hideFeedSavingDialog= () => {
                this.$mdDialog.hide();
            }

        validateSchemaDidNotChange(model:any) {
            let valid = true;
            //if we are editing we need to make sure we dont modify the originalTableSchema
            if(model.id && model.originalTableSchema && model.table && model.table.tableSchema) {
                //if model.originalTableSchema != model.table.tableSchema  ... ERROR
                //mark as invalid if they dont match
                const origFields = _.chain(model.originalTableSchema.fields).sortBy('name').map(_.property("derivedDataType")).value().join(",");
                const updatedFields = _.chain(model.table.tableSchema.fields).sortBy('name').map(_.property("derivedDataType")).value().join(",");
                valid = origFields == updatedFields;
            }
            return valid;
        }
            /**
             * Save the model Posting the data to the server
             * @param model
             * @returns {*}
             */
            saveFeedModel= (model: any) => {
                var self = this;
            let copy = self.prepareModelForSave(model);
            //reset the sensitive properties
            this.FeedPropertyService.initSensitivePropertiesForEditing(model.properties);

                var deferred = this.$q.defer();
                var successFn = (response: any) => {
                    var invalidCount = 0;
                    if (response.data && response.data.success) {
                        //update the feed versionId and internal id upon save
                        model.id = response.data.feedMetadata.id;
                        model.versionName = response.data.feedMetadata.versionName;
                        this.$mdToast.show(
                            this.$mdToast.simple()
                                .textContent('Feed successfully saved')
                                .hideDelay(3000)
                        );
                        deferred.resolve(response);
                    }
                    else {
                        deferred.reject(response);
                    }

                }
                var errorFn = (err: any) => {
                    deferred.reject(err);
                }

                //post to the server to save with a custom timeout of 7 minutes.
                var promise = this.$http({
                    url: this.RestUrlService.CREATE_FEED_FROM_TEMPLATE_URL,
                    method: "POST",
                    data: angular.toJson(copy),
                    headers: {
                        'Content-Type': 'application/json; charset=UTF-8'
                    },
                    timeout: 7*60 * 1000
                }).then(successFn, errorFn);

                return deferred.promise;
            }
            /**
             * Call out to the server to get the System Name for a passed in name
             * @param feedName
             * @returns {HttpPromise}
             */
            getSystemName= (feedName: any) => {

                return this.$http.get(this.RestUrlService.GET_SYSTEM_NAME, { params: { name: feedName } });

            }
            /**
             * Call out to the server to get info on whether feed history data reindexing is configured in Kylo
             * @returns {HttpPromise}
             */
            isKyloConfiguredForFeedHistoryDataReindexing= () => {
                return this.$http.get(this.RestUrlService.FEED_HISTORY_CONFIGURED);
            }
            /**
             * When creating a Feed find the First Column/Field that matches the given name
             * @param name
             * @returns {*|{}}
             */
            getColumnDefinitionByName(name: string): TableColumnDefinition {
                return _.find(this.createFeedModel.table.tableSchema.fields, (columnDef: any) => {
                    return columnDef.name == name;
                });
            }
            /**
             * Call the server to return a list of Feed Names
             * @returns {HttpPromise}
             */
            getFeedSummary= () => {

                var successFn = (response: any) => {
                    return response.data;
                }
                var errorFn = (err: any) => {

                }
                var promise = this.$http.get(this.RestUrlService.GET_FEED_NAMES_URL);
                promise.then(successFn, errorFn);
                return promise;
            }
            /**
             * Call the server to return a list of Feed Names
             * @returns {HttpPromise}
             */
            getFeedNames= () => {

                var successFn = (response: any) => {
                    return response.data;
                }
                var errorFn = (err: any) => {

                }
                var promise =this.$http.get(this.RestUrlService.OPS_MANAGER_FEED_NAMES);
                promise.then(successFn, errorFn);
                return promise;
            }

            /**
             * Call server to return a list of Feed Names (from JCR)
             * @returns {angular.IHttpPromise<any>}
             */
            getFeedNamesFromJcr() {
                let successFn = (response: any) => {
                    return response.data;
                };
                let errorFn = (err: any) => {
                    console.log("Error getting list of feed names from JCR " + err);
                };
                let promise = this.$http.get(this.RestUrlService.GET_FEED_NAMES_URL);
                promise.then(successFn, errorFn);
                return promise;
            }

            /**
             * Call the server to get a list of all the available Preconditions that can be used when saving/scheduling the feed
             * @returns {HttpPromise}
             */
            getPossibleFeedPreconditions=  () => {

                var successFn =  (response: any) => {
                    return response.data;
                }
                var errorFn = (err: any) => {
                    console.log('ERROR ', err)
                }
                var promise = this.$http.get(this.RestUrlService.GET_POSSIBLE_FEED_PRECONDITIONS_URL);
                promise.then(successFn, errorFn);
                return promise;
            }

            /**
             * Gets the list of user properties for the specified feed.
             *
             * @param {Object} model the feed model
             * @return {Array.<{key: string, value: string}>} the list of user properties
             */
            getUserPropertyList= (model: any): { key: string, value: string }[] => {
                var userPropertyList: any[] = [];
                angular.forEach(model.userProperties, (value: any, key: string) => {
                    if (!key.startsWith("jcr:")) {
                        userPropertyList.push({ key: key, value: value });
                    }
                });
                return userPropertyList;
            }

            /**
             * Gets the user fields for a new feed.
             *
             * @param {string} categoryId the category id
             * @returns {Promise} for the user fields
             */
            getUserFields= (categoryId: string): angular.IPromise<any> => {
                return this.$http.get(this.RestUrlService.GET_FEED_USER_FIELDS_URL(categoryId))
                    .then((response: any) =>{
                        return response.data;
                    });
            }

            /**
             * Gets the controller services of the specified type.
             *
             * @param {string} type a type class
             * @returns {Array}
             */
            getAvailableControllerServices= (type: string): angular.IPromise<any> => {
                return this.$http.get(this.RestUrlService.LIST_SERVICES_URL("root"), { params: { type: type } })
                    .then( (response: any) => {
                        return response.data;
                    });
            }
        setControllerServicePropertyDisplayName=(property: any) => {

            let setDisplayValue = (property :any) : boolean => {
                let cacheEntry:string = controllerServiceDisplayCache[property.value];
                if(cacheEntry != null) {
                    property.displayValue =cacheEntry;
                    return true;
                }
                return false;
            }

            if(angular.isObject(property.propertyDescriptor) && angular.isString(property.propertyDescriptor.identifiesControllerService)) {
                if (!setDisplayValue(property)) {

                    let entry: any = controllerServiceDisplayCachePromiseTracker[property.propertyDescriptor.identifiesControllerService];
                    if (entry == undefined) {
                        let promise = this.getAvailableControllerServices(property.propertyDescriptor.identifiesControllerService);
                        entry = {request: promise, waitingProperties: []};
                        entry.waitingProperties.push(property);
                        controllerServiceDisplayCachePromiseTracker[property.propertyDescriptor.identifiesControllerService] = entry;
                        promise.then((services: any) => {
                            _.each(services, (service: any) => {
                                controllerServiceDisplayCache[service.id] = service.name;
                            });
                            _.each(entry.waitingProperties, (property: any) => {
                                setDisplayValue(property);
                            });
                            delete controllerServiceDisplayCachePromiseTracker[property.propertyDescriptor.identifiesControllerService];
                        })
                    }
                    else {
                        entry.waitingProperties.push(property);
                    }
                }
            }
        }
            /**
             * Finds the allowed controller services for the specified property and sets the allowable values.
             *
             * @param {Object} property the property to be updated
             */
            findControllerServicesForProperty= (property: any) => {
                // Show progress indicator
                property.isLoading = true;

                // Fetch the list of controller services
                this.getAvailableControllerServices(property.propertyDescriptor.identifiesControllerService)
                    .then( (services: any) => {
                        // Update the allowable values
                        property.isLoading = false;
                        property.propertyDescriptor.allowableValues = _.map(services, (service: any) => {
                            return { displayName: service.name, value: service.id }
                        });
                    }, () => {
                        // Hide progress indicator
                        property.isLoading = false;
                    });
            }

            getFeedByName= (feedName: string) => {
                var deferred = this.$q.defer();
                this.$http.get(this.RestUrlService.FEED_DETAILS_BY_NAME_URL(feedName))
                    .then( (response: any) => {
                        var feedResponse = response.data;
                        return deferred.resolve(feedResponse);
                    });
                return deferred.promise;
            }

            /**
             * Gets the list of available Hive partition functions.
             *
             * @returns {Array.<string>} list of function names
             */
            getPartitionFunctions= () => {
                return this.$http.get(this.RestUrlService.PARTITION_FUNCTIONS_URL)
                    .then((response: any) => {
                        return response.data;
                    });
            }

            getFeedVersions= (feedId: string) => {
                var successFn = (response: any) => {
                    return response.data;
                }
                var errorFn = (err: any) => {
                    console.log('ERROR ', err)
                }
                return this.$http.get(this.RestUrlService.FEED_VERSIONS_URL(feedId)).then(successFn, errorFn);
            }

            getFeedVersion= (feedId: string, versionId: string) => {
                var successFn = (response: any) => {
                    return response.data;
                }
                var errorFn = (err: any) => {
                    console.log('ERROR ', err)
                }
                return this.$http.get(this.RestUrlService.FEED_VERSION_ID_URL(feedId, versionId)).then(successFn, errorFn);
            }

            diffFeedVersions= (feedId: string, versionId1: string, versionId2: string) => {
                var successFn = (response: any) => {
                    return response.data;

                }
                var errorFn = (err: any) => {
                    console.log('ERROR ', err)
                }
                return this.$http.get(this.RestUrlService.FEED_VERSIONS_DIFF_URL(feedId, versionId1, versionId2)).then(successFn, errorFn);
            }

            /**
             * check if the user has access on an entity
             * @param permissionsToCheck an Array or a single string of a permission/action to check against this entity and current user
             * @param entity the entity to check. if its undefined it will use the current feed in the model
             * @returns {*} a promise, or a true/false.  be sure to wrap this with a $q().when()
             */
            hasEntityAccess= (permissionsToCheck: any, entity: any) => {
                if (entity == undefined) {
                    // entity = this.model; @Greg model is not defined anywhere inside this service what is it?
                }
                return this.accessControlService.hasEntityAccess(permissionsToCheck, entity, EntityAccessControlService.entityRoleTypes.FEED);
            }

            /**
             * Applies the specified domain type to the specified field.
             *
             * @param {Field} field the field to be updated
             * @param {FieldPolicy} policy the field policy to be updated
             * @param {DomainType} domainType the domain type be be applies
             */
            setDomainTypeForField(field: TableColumnDefinition, policy: TableFieldPolicy, domainType: DomainType) {
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
                        field.dataTypeDisplay = this.getDataTypeDisplay(field);
                    }
                }

                if (angular.isObject(domainType.fieldPolicy)) {
                    policy.standardization = angular.copy(domainType.fieldPolicy.standardization);
                    policy.validation = angular.copy(domainType.fieldPolicy.validation);
                }
            }
            /**
             * Returns operation of the difference at given path for versioned feed
             * @param path current diff model
             * @returns {string} operation type, e.g. add, remove, update, no-change
             */
            diffOperation= (path: any) => {
                return this.versionFeedModelDiff && this.versionFeedModelDiff[path] ? this.versionFeedModelDiff[path].op : 'no-change';
            }

            diffCollectionOperation= (path: any) => {
                const self = this;
                if (this.versionFeedModelDiff) {
                    if (this.versionFeedModelDiff[path]) {
                        return this.versionFeedModelDiff[path].op;
                    } else {
                        const patch = { op: 'no-change' };
                        _.each(_.values(this.versionFeedModelDiff), (p) => {
                            if (p.path.startsWith(path + "/")) {
                                patch.op = self.joinVersionOperations(patch.op, p.op);
                            }
                        });
                        return patch.op;
                    }
                }
                return 'no-change';
            }

            joinVersionOperations= (op1: any, op2: any) => {
                const opLevels = { 'no-change': 0, 'add': 1, 'remove': 1, 'replace': 2 };
                if (opLevels[op1] === opLevels[op2] && op1 !== 'no-change') {
                    return 'replace';
                }
                return opLevels[op1] > opLevels[op2] ? op1 : op2;
            }

            resetVersionFeedModel= () => {
                this.versionFeedModel = {};
                this.versionFeedModelDiff = {};
            }
        //}

static $inject = ["$http", "$q", "$mdToast", "$mdDialog", "RestUrlService", "VisualQueryService", "FeedCreationErrorService", "FeedPropertyService", "AccessControlService",
        "EntityAccessControlService", "StateService"];
    constructor(private $http: angular.IHttpService,
        private $q: angular.IQService,
        private $mdToast: angular.material.IToastService,
        private $mdDialog: angular.material.IDialogService,
        private RestUrlService: any,
        private visualQueryService: any,
        private FeedCreationErrorService: any,
        private FeedPropertyService: any,
        private accessControlService: AccessControlService,
        private entityAccessControlService: EntityAccessControlService,
        private StateService: any) {
        this.init();
        //return this.data;
    }// constructor terminating here

    trim(str: string) {
        return str.replace(/^\s+|\s+$/g, "");
    }

    toCamel(str: string) {
        return str.replace(/(\-[a-z])/g, function ($1) {
            return $1.toUpperCase().replace('-', '');
        });
    }

    toDash(str: string) {
        return str.replace(/([A-Z])/g, function ($1) {
            return "-" + $1.toLowerCase();
        });
    }

    spacesToUnderscore(str: string) {
        return str.replace(/\s+/g, '_');
    }

    toUnderscore(str: string) {
        return str.replace(/(?:^|\.?)([A-Z])/g, function (x, y) {
            return "_" + y.toLowerCase()
        }).replace(/^_/, "")
        //return str.replace(/([A-Z])/g, "_$1").replace(/^_/,'').toLowerCase();
    }
}

/**
 * The Controller used for the Feed Saving Dialog
 */
export class FeedSavingDialogController {
    static readonly $inject = ["$scope", "$mdDialog", "message", "feedName"];
    constructor(private $scope: any,
        private $mdDialog: angular.material.IDialogService,
        private message: string,
        private feedName: string) {
        $scope.feedName = feedName;
        $scope.message = message;

        this.$scope.hide = () => {
            this.$mdDialog.hide();
        };

        this.$scope.cancel = () => {
            this.$mdDialog.cancel();
        };
    }
}
