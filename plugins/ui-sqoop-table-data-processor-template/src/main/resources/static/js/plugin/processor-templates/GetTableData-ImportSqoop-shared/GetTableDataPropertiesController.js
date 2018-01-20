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

define(['angular'], function (angular) {

    var directive = function () {
        return {
            restrict: "EA",
            bindToController: {
                mode: '@',
                processor: '=',
                theForm: '=',
                sourceTableKey: '@?',
                sourceFieldsKey: '@?',
                connectionServiceKey: '@?',
                loadStrategyKey: '@?',
                incrementalPropertyKey: '@?',
                renderLoadStrategyOptions: '=',
                loadStrategyOptions: '=?',
                defaultLoadStrategyValue: '@?',
                useTableNameOnly: '=?'
            },
            controllerAs: 'ctl',
            scope: {},
            templateUrl: 'js/plugin/processor-templates/GetTableData-ImportSqoop-shared/get-table-data-properties.html',
            controller: "GetTableDataPropertiesController",
            link: function ($scope, element, attrs, controllers) {
            }

        };
    }

    var controller =  function($scope, $q,$http,$mdToast,RestUrlService, FeedService,EditFeedNifiPropertiesService,DBCPTableSchemaService) {

        var self = this;
        /**
         * The object storing the table selected via the autocomplete
         * @type {null}
         */
        this.selectedTable = null;

        /**
         * The table Schema, parsed from the table autocomplete
         * @type {null}
         */
        this.tableSchema = null;

        /**
         * boolean flag to indicate we are initializing the controller
         * @type {boolean}
         */
        this.initializing = true;

        /**
         * If we are creating a new feed then use the Create Model, otherwise use the Edit Model
         * If the processor is undefined, attempt to get it via the inputProcessor of the model
         */
        if(this.mode == 'create') {
            if (this.processor == undefined) {
                this.processor = FeedService.createFeedModel.inputProcessor;
            }

            this.model = FeedService.createFeedModel;
        }
        else {
            if (this.processor == undefined) {
                this.processor = EditFeedNifiPropertiesService.editFeedModel.inputProcessor;
            }
            this.model = EditFeedNifiPropertiesService.editFeedModel;

        }

        /**
         * Cache of the Table objects returned from the initial autocomplete call out
         * @type {{}}
         */
        this.allTables = {};

        /**
         * Flag to indicate if the controller service has connection errors.
         * if there are erros the UI will display input boxes for the user to defin the correct table name
         * @type {boolean}
         */
        this.databaseConnectionError = false;

        /**
         *  load strategies either passed in via the directive, or defaulted
         */
        this.loadStrategyOptions =
            angular.isDefined(this.loadStrategyOptions) ? this.loadStrategyOptions : [{name: 'Full Load', type: 'SNAPSHOT', strategy: 'FULL_LOAD', hint: 'Replace entire table'}, {
                name: 'Incremental', type: 'DELTA', strategy: 'INCREMENTAL', hint: 'Incremental load'
                                                                                   + ' based on a '
                                                                                   + ' high watermark', incremental: true, restrictDates: true
            }];

        /**
         * Default the property keys that are used to look up the
         *
         */
        this.SOURCE_TABLE_PROPERTY_KEY = angular.isDefined(this.sourceTableKey) ? this.sourceTableKey : 'Source Table';
        this.SOURCE_FIELDS_PROPERTY_KEY = angular.isDefined(this.sourceFieldsKey) ? this.sourceFieldsKey : 'Source Fields';
        this.DB_CONNECTION_SERVICE_PROPERTY_KEY = angular.isDefined(this.connectionServiceKey) ? this.connectionServiceKey : 'Source Database Connection'; //''
        this.LOAD_STRATEGY_PROPERTY_KEY = angular.isDefined(this.loadStrategyKey) ? this.loadStrategyKey : 'Load Strategy';
        this.INCREMENTALPROPERTY_KEY = angular.isDefined(this.incrementalPropertyKey) ? this.incrementalPropertyKey : 'Date Field';
        this.defaultLoadStrategyValue = angular.isDefined(this.defaultLoadStrategyValue) ? this.defaultLoadStrategyValue : 'INCREMENTAL';
        this.dbConnectionProperty = findProperty(self.DB_CONNECTION_SERVICE_PROPERTY_KEY, false)
        this.useTableNameOnly = angular.isDefined(this.useTableNameOnly)? this.useTableNameOnly : false;
        /**
         * The 2 properties are not used right now
         */
        this.RETENTION_PERIOD_PROPERTY_KEY = 'Backoff Period';
        this.ARCHIVE_UNIT_PROPERTY_KEY = 'Minimum Time Unit';

        /**
         * Cache of the fields related to the table selected
         * This is used the the dropdown when showing the fields for the incremtal options
         * @type {Array}
         */
        this.tableFields = [];

        /**
         * property that stores the selected table fields and maps it to the hive table schema
         * @type {Array}
         */
        this.originalTableFields = [];

        /**
         * Array of the Nifi property keys that are marked as custom.  any keys here will not be loaded by the default nifi-property rendering  mechanism and be required to be implemented by this
         * template.
         * @type {*[]}
         */
        var customPropertyKeys = [self.DB_CONNECTION_SERVICE_PROPERTY_KEY, self.SOURCE_TABLE_PROPERTY_KEY, self.SOURCE_FIELDS_PROPERTY_KEY, self.LOAD_STRATEGY_PROPERTY_KEY,
                                  self.INCREMENTALPROPERTY_KEY, self.RETENTION_PERIOD_PROPERTY_KEY, self.ARCHIVE_UNIT_PROPERTY_KEY];



        /**
         * Autocomplete objected used in the UI html page
         * @type {{clear: Function, searchText: string, selectedTable: null, searchTextChange: Function, selectedItemChange: Function, querySearch: Function}}
         */
        this.tablesAutocomplete = {
            clear:function(){
                this.searchText = '';
                this.selectedTable = null;
            },
            searchText:'',
            selectedTable:null,
            searchTextChange:function(text){
                validate();

            },
            selectedItemChange : function(table){
                self.selectedTable = table;
                validate();
            },
            querySearch:function(txt){
                return queryTablesSearch(txt);
            }
        }


        /**
         * lookup and find the respective Nifi Property objects that map to the custom property keys
         */
        function initPropertyLookup() {
            self.tableProperty = findProperty(self.SOURCE_TABLE_PROPERTY_KEY);
            self.fieldsProperty = findProperty(self.SOURCE_FIELDS_PROPERTY_KEY);
            self.loadStrategyProperty = findProperty(self.LOAD_STRATEGY_PROPERTY_KEY);
            if(self.loadStrategyProperty && (self.loadStrategyProperty.value == null || self.loadStrategyProperty.value == undefined)){
                self.loadStrategyProperty.value = self.defaultLoadStrategyValue;
            }


            self.retentionPeriodProperty = findProperty(self.RETENTION_PERIOD_PROPERTY_KEY);
            self.archiveUnitProperty = findProperty(self.ARCHIVE_UNIT_PROPERTY_KEY);

            self.deleteSourceProperty = {value:'false',key:'Delete Source'};
            self.incrementalFieldProperty = findProperty(self.INCREMENTALPROPERTY_KEY);
        }


        /**
         * Check to see if the property is in the list of custom ones.
         * if so it will bypass the nifi-property directive for rendering
         * @param property
         * @returns {boolean|*}
         */
        this.isCustomProperty = function(property){
            return _.contains(customPropertyKeys,property.key);
        }

        /**
         * Determine if the incoming value or if the current selected LoadStrategy is of type incremental
         * Incremental properties need additional option to define the field used for incrementing
         * @param val
         * @returns {*|{}}
         */
        this.isIncrementalLoadStrategy = function (val) {
            var checkValue = val;
            if (checkValue == undefined) {
                checkValue = (self.loadStrategyProperty) ? self.loadStrategyProperty.value : undefined;
            }

            return checkValue && _.find(self.loadStrategyOptions, function (v) {
                    return v.strategy == checkValue && v.incremental == true;
                });
        }

        /**
         * Change listener when the user changes the controller service in the UI
         * @param dbConnectionProperty
         */
        this.onDbConnectionPropertyChanged = function (dbConnectionProperty) {
            if (self.mode != 'edit') {
            //clear out rest of the model
            self.selectedTable = undefined;
            self.model.table.sourceTableIncrementalDateField = null;
            self.databaseConnectionError = false;
        }

        }

        /**
         * Change listener when the user updates a NiFi property in UI
         *
         * Currently, this handles the output type property specified by the user for a database ingest feed
         * This is used to set the Hive table's format

         * @param nifiPropertyChanged
         */
        this.onNiFiPropertyChanged = function (nifiPropertyChanged) {
            if (self.mode == 'create') {
                if (nifiPropertyChanged.key == 'Output Type') {
                    if (nifiPropertyChanged.value == 'AVRO') {
                        self.model.table.feedFormat = 'STORED AS AVRO'
                    }
                    else if (nifiPropertyChanged.value == 'DELIMITED') {
                        self.model.table.feedFormat = FeedService.getNewCreateFeedModel().table.feedFormat
                    }
                }
            }
        };

        /**
         * Finds the correct NiFi processor Property associated with the incoming key.
         *
         * @param key
         * @param clone
         * @returns {*}
         */
        function findProperty(key, clone) {
            //get all the props for this input

            if (clone == undefined) {
                clone = false;
            }
            var matchingProperty = _.find(self.processor.allProperties, function (property) {
                return property.key == key;
            });

            //on edit mode the model only has the props saved for that type.
            //need to find the prop associated against the other input type
            if((matchingProperty == undefined || matchingProperty == null)&& self.model.allInputProcessorProperties != undefined){
                var props = self.model.allInputProcessorProperties[self.processor.processorId];
                if(props){
                    matchingProperty = _.find(props,function(property){
                        return property.key == key;
                    });
                }
            }
            if (matchingProperty == null) {
                //  console.log('UNABLE TO GET MATCHING PROPERTY FOR ',key,'model ',self.model, self.processor)
            } else {
                if (clone) {
                    return angular.copy(matchingProperty);
                }
            }

            return matchingProperty;
        }

        /**
         * match query term case insensitive
         * @param query
         * @returns {Function}
         */
        function createFilterForTable(query) {
            var lowercaseQuery = angular.lowercase(query);
            return function filterFn(item) {
                return (item.fullNameLower.indexOf(lowercaseQuery) != -1 );
            };
        }

        /**
         * return the list of tables for the selected Service ID
         * @param query
         * @returns {*}
         */
       function queryTablesSearch (query) {
            var dbcpProperty = self.dbConnectionProperty;
           if(dbcpProperty != null && dbcpProperty.value != null) {
               var serviceId = dbcpProperty.value;
               var serviceNameValue = _.find(dbcpProperty.propertyDescriptor.allowableValues,function(allowableValue) {
                  return allowableValue.value == serviceId;
               });
               var serviceName = serviceNameValue != null && serviceNameValue != undefined ?serviceNameValue.displayName : '';

               if (query == null) {
                   query = '';
               }
               //add % if not ends with

               if (self.allTables[serviceId] == undefined) {
                   var deferred = $q.defer();
                   var tableNameQuery = "%" + query + "%";
                   var tables = $http.get(DBCPTableSchemaService.LIST_TABLES_URL(serviceId), {params: {serviceName: serviceName, tableName: tableNameQuery}}).then(function (response) {
                       if(_.isArray(response.data)) {
                           self.databaseConnectionError = false;
                           var tables = parseTableResponse(response.data);
                           // Dont cache .. uncomment to cache results
                           // self.allTables[serviceId] = parseTableResponse(response.data);
                           var results = query ? tables.filter(createFilterForTable(query)) : tables;
                           deferred.resolve(results);
                       }
                       else {
                           self.databaseConnectionError = true;
                           deferred.reject(response);
                       }
                   }, function (err) {
                       self.databaseConnectionError = true;
                       deferred.reject(err);

                   });
                   return deferred.promise;
               }
               else {
                   var results = query ? self.allTables[serviceId].filter(createFilterForTable(query)) : [];

                   return results;
               }
           }
           else {
               return [];
           }
        }

        /**
         * Turn the schema.table string into an object for template display
         * @param response
         * @returns {Array}
         */
        function parseTableResponse(response){
            var allTables =  [];
            if(response) {
                angular.forEach(response,function(table){
                    var schema = table.substr(0,table.indexOf("."));
                    var tableName= table.substr(table.indexOf(".")+1);
                    allTables.push({schema:schema,tableName:tableName, fullName:table,fullNameLower:table.toLowerCase()});
                })
            }
            return allTables;
        }




        /**
         * Callback from saving/edit feed
         */
        function onSaveSuccessEditNifiProperties(model) {
            //update the model with the properties
        }
        /**
         * on edit describe the table for incremental load to populate the tableField options
         */
        function editIncrementalLoadDescribeTable() {
            //get the property that stores the DBCPController Service
            var dbcpProperty = self.dbConnectionProperty;
            if (dbcpProperty != null && dbcpProperty.value != null && self.selectedTable != null) {
                var successFn = function (response) {
                    self.databaseConnectionError = false;
                    self.tableSchema = response.data;
                    self.tableFields = self.tableSchema.fields;
                };

                var serviceId = dbcpProperty.value;
                var serviceNameValue = _.find(dbcpProperty.propertyDescriptor.allowableValues,function(allowableValue) {
                    return allowableValue.value == serviceId;
                });
                var serviceName = serviceNameValue != null && serviceNameValue != undefined ?serviceNameValue.displayName : '';
                var promise = $http.get(DBCPTableSchemaService.DESCRIBE_TABLE_URL(serviceId,self.selectedTable.tableName),{params:{schema:self.selectedTable.schema, serviceName:serviceName}})
                promise.then(successFn, function (err) {
                    self.databaseConnectionError = true;
                });
                return promise;
            }
        }

        /**
         * Describe the table
         * This is called once a user selects a table from the autocomplete
         * This will setup the model populating the destination table fields
         * @returns {HttpPromise}
         */
        function describeTable(){
            //get the property that stores the DBCPController Service
            var dbcpProperty = self.dbConnectionProperty;
            if(dbcpProperty != null && dbcpProperty.value != null && self.selectedTable != null) {
                var successFn = function (response) {
                    self.databaseConnectionError = false;

                    self.tableSchema = response.data;
                    self.tableFields = self.tableSchema.fields;
                    self.originalTableFields = angular.copy(self.tableSchema.fields);
                    self.tableFieldsDirty = false;

                    self.model.table.sourceTableSchema.fields=self.originalTableFields;

                    FeedService.setTableFields(self.tableSchema.fields);
                    self.model.table.method = 'EXISTING_TABLE';

                    if(self.tableSchema.schemaName != null){
                        self.model.table.existingTableName = self.tableSchema.schemaName+"."+self.tableSchema.name;
                    }
                    else {
                        self.model.table.existingTableName = self.tableSchema.name;
                    }
                    self.model.table.sourceTableSchema.name=self.model.table.existingTableName;
                }

                var serviceId = dbcpProperty.value;
                var serviceNameValue = _.find(dbcpProperty.propertyDescriptor.allowableValues,function(allowableValue) {
                    return allowableValue.value == serviceId;
                });
                var serviceName = serviceNameValue != null && serviceNameValue != undefined ?serviceNameValue.displayName : '';
                var promise = $http.get(DBCPTableSchemaService.DESCRIBE_TABLE_URL(serviceId,self.selectedTable.tableName),{params:{schema:self.selectedTable.schema, serviceName:serviceName}})
                promise.then(successFn, function (err) {
                    self.databaseConnectionError = true;
                });
                return promise;

            }
        }



        this.onManualTableNameChange = function () {
            if (self.model.table.method != 'EXISTING_TABLE') {
                self.model.table.method = 'EXISTING_TABLE';
            }
            self.model.table.sourceTableSchema.name = self.tableProperty.value
            self.model.table.existingTableName = self.tableProperty.value;
        }

        this.onManualFieldNameChange = function () {
            if (self.model.table.method != 'EXISTING_TABLE') {
                self.model.table.method = 'EXISTING_TABLE';
            }
            var fields = [];
            var val = self.fieldsProperty.value;
            var fieldNames = [];
            _.each(val.split(","), function (field) {
                var col = FeedService.newTableFieldDefinition();
                col.name = field.trim();
                col.derivedDataType = 'string';
                fields.push(col);
                fieldNames.push(col.name);
            });
            self.model.table.sourceTableSchema.fields = angular.copy(fields);
            FeedService.setTableFields(fields);

            self.model.table.sourceFieldsCommaString = fieldNames.join(",")
            self.model.table.sourceFields = fieldNames.join("\n")

        }

        /**
         * Filter for fields that are Date types
         * @param field
         * @returns {boolean}
         */
        this.filterFieldDates = function(field){
            return field.derivedDataType == 'date' || field.derivedDataType == 'timestamp';
        }


        this.onIncrementalDateFieldChange = function(){
            var prop = self.incrementalFieldProperty;
            if(prop != null) {
                prop.value =  self.model.table.sourceTableIncrementalDateField;
                prop.displayValue = prop.value;
            }
        }

        /**
         * Validates the autocomplete has a selected table
         */
        function validate() {
            if (self.theForm != undefined && self.theForm.tableAutocompleteInput) {

                if (self.selectedTable == undefined) {
                    self.theForm.tableAutocompleteInput.$setValidity("required", false);
                }
                else {
                    self.theForm.tableAutocompleteInput.$setValidity("required", true);
                }
            }
        }


        function initializeAutoComplete(){
            var processorTableName = self.model.table.existingTableName;
            self.tablesAutocomplete.selectedTable = self.model.table.existingTableName;
            if(processorTableName != null) {
                var schemaName = processorTableName.substring(0, processorTableName.indexOf("."));
                var tableName = processorTableName.substring(processorTableName.indexOf(".")+1);
                var fullNameLower = processorTableName.toLowerCase();
                self.selectedTable = self.tablesAutocomplete.selectedTable = {
                    schema: schemaName,
                    tableName: tableName,
                    fullName: processorTableName,
                    fullNameLower: fullNameLower
                };
            }
        }

        /**
         * Initialize the controller
         */
        this.init = function() {

            self.initializing = true;
            /**
             * lookup and find the respective Nifi Property objects that map to the custom property keys
             */
            initPropertyLookup();

            function setupClonedFeedTableFields(){
                self.databaseConnectionError = false;
                self.tableSchema = self.model.table.tableSchema;
                self.tableFields = self.tableSchema.fields;
                self.originalTableFields = self.model.table.sourceTableSchema;
                self.tableFieldsDirty = false;

                //   FeedService.setTableFields(self.tableSchema.fields);
                self.model.table.method = 'EXISTING_TABLE';

            }

            /**
             * if we are editing or working with the cloned feed then get the selectedTable saved on this model.
             */
            if(self.mode == 'edit'){
                initializeAutoComplete();
                if(self.isIncrementalLoadStrategy()){
                    editIncrementalLoadDescribeTable();
                }
            }
            else if(self.mode == 'create' && self.model.cloned == true) {
             //if we are cloning and creating a new feed setup the autocomplete
                initializeAutoComplete();
                setupClonedFeedTableFields();
            }


            /**
             * Watch for changes on the table to refresh the schema
             */
            $scope.$watch(function(){
                return self.selectedTable
            },function(newVal, oldVal){
                var tableProperty = self.tableProperty
                validate();
                if(tableProperty && newVal != undefined) {

                    if(self.useTableNameOnly){
                        tableProperty.value = newVal.tableName;
                    }
                    else {
                        tableProperty.value = newVal.fullName;
                    }


                    if (newVal != null && newVal != undefined) {
                        if(self.mode == 'create') {
                            //only describe on the Create as the Edit will be disabled and we dont want to change the field data.
                            //If we are working with a cloned feed we should attempt to get the field information from the cloned model
                            if(!self.model.cloned || (self.model.cloned && (angular.isUndefined(oldVal) || (angular.isDefined(oldVal) && newVal.fullName != oldVal.fullName) || self.model.table.tableSchema.fields.length ==0))) {
                                describeTable();
                            }
                        }
                    }
                    else {
                        self.tableSchema = null;
                    }
                }
            });


            /**
             * If there is a LOAD_STRATEGY property then watch for changes to show/hide additional options
             */
            if(self.loadStrategyProperty){
                $scope.$watch(function () {
                    return self.loadStrategyProperty.value
                }, function (newVal,oldValue) {
                    self.loadStrategyProperty.displayValue = newVal
                    if(newVal == 'FULL_LOAD'){
                        self.model.table.tableType = 'SNAPSHOT';
                        self.restrictIncrementalToDateOnly = false;
                    }
                    else if (self.isIncrementalLoadStrategy(newVal)) {
                        self.model.table.tableType = 'DELTA';
                        //reset the date field
                        if(oldValue != undefined && oldValue != null && newVal != oldValue) {
                            self.model.table.sourceTableIncrementalDateField = '';
                        }
                        var option = _.find(self.loadStrategyOptions, function (opt) {
                            return opt.strategy == newVal
                        });
                        if (option) {
                            self.restrictIncrementalToDateOnly = option.restrictDates != undefined ? option.restrictDates : false;
                        }
                        if(newVal !== oldValue){
                            editIncrementalLoadDescribeTable();
                        }
                    }

                });
            }

            self.initializing = false;

        }



        //Initialize the Controller
        this.init();



    };

    var moduleName = "kylo.plugin.processor-template.tabledata";
    angular.module(moduleName, [])
    angular.module(moduleName).controller('GetTableDataPropertiesController',["$scope","$q","$http","$mdToast","RestUrlService","FeedService","EditFeedNifiPropertiesService","DBCPTableSchemaService", controller]);

    angular.module(moduleName)
        .directive('thinkbigGetTableDataProperties', directive);

});


