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
define(['angular','feed-mgr/module-name'], function (angular,moduleName) {
    angular.module(moduleName).factory('RegisterTemplateService', ["$http","$q","$mdDialog","RestUrlService","FeedInputProcessorOptionsFactory","FeedDetailsProcessorRenderingHelper","FeedPropertyService","AccessControlService","EntityAccessControlService",function ($http, $q, $mdDialog, RestUrlService, FeedInputProcessorOptionsFactory, FeedDetailsProcessorRenderingHelper,FeedPropertyService,AccessControlService,EntityAccessControlService) {

        function escapeRegExp(str) {
            return str.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
        }

        /**
         * for a given processor assign the feedPropertiesUrl so it can be rendered correctly
         * @param processor
         */
        function setRenderTemplateForProcessor(processor, mode) {
            if (processor.feedPropertiesUrl == undefined) {
                processor.feedPropertiesUrl = null;
            }
            if (processor.feedPropertiesUrl == null) {
                FeedInputProcessorOptionsFactory.setFeedProcessingTemplateUrl(processor, mode);
            }
        }

        var data = {
            /**
             * Properties that require custom Rendering, separate from the standard Nifi Property (key  value) rendering
             * This is used in conjunction with the method {@code this.isCustomPropertyRendering(key)} to determine how to render the property to the end user
             */
            customPropertyRendering: ["metadata.table.targetFormat", "metadata.table.feedFormat"],

            codemirrorTypes: null,
            propertyRenderTypes: [{type: 'text', 'label': 'Text'}, {type: 'password', 'label': 'Password'},{type: 'number', 'label': 'Number', codemirror: false},
                {type: 'textarea', 'label': 'Textarea', codemirror: false}, {type: 'select', label: 'Select', codemirror: false},
                {type: 'checkbox-custom', 'label': 'Checkbox', codemirror: false}],
            trueFalseRenderTypes: [{type: 'checkbox-true-false', 'label': 'Checkbox', codemirror: false},
                {type: 'select', label: 'Select', codemirror: false}],
            selectRenderType: [{type: 'select', 'label': 'Select', codemirror: false},
                {type: 'radio', label: 'Radio Button', codemirror: false}],
            codeMirrorRenderTypes: [],
            configurationProperties: [],
            metadataProperties: [],
            propertyList: [],
            configurationPropertyMap: {},
            model: null,
            emptyModel: {
                id: null,
                nifiTemplateId: null,
                templateName: '',
                description: '',
                processors: [],
                inputProperties: [],
                additionalProperties: [],
                inputProcessors: [],
                nonInputProcessors: [],
                defineTable: true,
                allowPreconditions: false,
                dataTransformation: false,
                reusableTemplate: false,
                needsReusableTemplate: false,
                ports: [],
                reusableTemplateConnections: [],  //[{reusableTemplateFeedName:'', feedOutputPortName: '', reusableTemplateInputPortName: ''}]
                icon: {title: null, color: null},
                state: 'NOT REGISTERED',
                updateDate: null,
                feedsCount: 0,
                registeredDatasources: [],
                isStream: false,
                validTemplateProcessorNames: true,
                roleMemberships:[],
                owner:null,
                roleMembershipsUpdated:false
            },
            newModel: function () {
                this.model = angular.copy(this.emptyModel);
            },
            resetModel: function () {
                angular.extend(this.model, this.emptyModel);
                this.model.icon = {title: null, color: null}
            },
            init: function () {
                this.newModel();
                this.fetchConfigurationProperties();
                this.fetchMetadataProperties();
                this.getCodeMirrorTypes();
            },
            getModelForSave: function () {
                return {
                    properties: this.getSelectedProperties(),
                    id: this.model.id,
                    description: this.model.description,
                    defineTable: this.model.defineTable,
                    allowPreconditions: this.model.allowPreconditions,
                    dataTransformation: this.model.dataTransformation,
                    nifiTemplateId: this.model.nifiTemplateId,
                    templateName: this.model.templateName,
                    icon: this.model.icon.title,
                    iconColor: this.model.icon.color,
                    reusableTemplate: this.model.reusableTemplate,
                    needsReusableTemplate: this.model.needsReusableTemplate,
                    reusableTemplateConnections: this.model.reusableTemplateConnections,
                    state: this.model.state,
                    isStream: this.model.isStream,
                    roleMemberships:this.model.roleMemberships,
                    owner: this.model.owner,
                    roleMembershipsUpdated: this.model.roleMembershipsUpdated,
                    templateTableOption: this.model.templateTableOption,
                    timeBetweenStartingBatchJobs: this.model.timeBetweenStartingBatchJobs

                }
            },
            newReusableConnectionInfo: function () {
                return [{reusableTemplateFeedName: '', feedOutputPortName: '', reusableTemplateInputPortName: ''}];
            },
            isSelectedProperty: function (property) {
                var selected = (property.selected || ( property.value != null && property.value != undefined && (property.value.includes("${metadata") || property.value.includes("${config."))) );
                if (selected) {
                    property.selected = true;
                }
                return selected;
            },
            getSelectedProperties: function () {
                var self = this;
                var selectedProperties = [];

                angular.forEach(self.model.inputProperties, function (property) {
                    if (data.isSelectedProperty(property)) {
                        selectedProperties.push(property)
                        if (property.processor && property.processor.topIndex != undefined) {
                            delete property.processor.topIndex;
                        }
                        if (property.processorOrigName != undefined && property.processorOrigName != null) {
                            property.processorName = property.processorOrigName;
                        }

                        FeedPropertyService.initSensitivePropertyForSaving(property);
                    }
                });

                angular.forEach(self.model.additionalProperties, function (property) {
                    if (data.isSelectedProperty(property)) {
                        selectedProperties.push(property);
                        if (property.processor && property.processor.topIndex != undefined) {
                            delete property.processor.topIndex;
                        }
                        if (property.processorOrigName != undefined && property.processorOrigName != null) {
                            property.processorName = property.processorOrigName;
                        }
                        FeedPropertyService.initSensitivePropertyForSaving(property);
                    }
                });

                return selectedProperties;
            },
            sortPropertiesForDisplay: function (properties) {
                var propertiesAndProcessors = {properties: [], processors: []};

                //sort them by processor name and property key
                var arr = _.chain(properties).sortBy('key').sortBy('processorName').value();
                propertiesAndProcessors.properties = arr;
                //set the initial processor flag for the heading to print
                var lastProcessor = null;
                _.each(arr, function (property, i) {
                    if (lastProcessor == null || property.processor.id != lastProcessor) {
                        property.firstProperty = true;
                        propertiesAndProcessors.processors.push(property.processor);
                        property.processor.topIndex = i;
                    }
                    else {
                        property.firstProperty = false;
                    }
                    lastProcessor = property.processor.id;
                });
                return propertiesAndProcessors;
            },
            fetchConfigurationProperties: function (successFn, errorFn) {

                var self = this;
                if (self.configurationProperties.length == 0) {
                    var _successFn = function (response) {
                        self.configurationProperties = response.data;
                        angular.forEach(response.data, function (value, key) {
                            self.propertyList.push({key: key, value: value, description: null, dataType: null, type: 'configuration'});
                            self.configurationPropertyMap[key] = value;
                        })
                        if (successFn) {
                            successFn(response);
                        }
                    }
                    var _errorFn = function (err) {
                        if (errorFn) {
                            errorFn(err)
                        }
                    }

                    var promise = $http.get(RestUrlService.CONFIGURATION_PROPERTIES_URL);
                    promise.then(_successFn, _errorFn);
                    return promise;
                }

            },
            fetchMetadataProperties: function (successFn, errorFn) {
                var self = this;
                if (self.metadataProperties.length == 0) {
                    var _successFn = function (response) {
                        self.metadataProperties = response.data;
                        angular.forEach(response.data, function (annotatedProperty, i) {
                            self.propertyList.push({
                                key: annotatedProperty.name,
                                value: '',
                                dataType: annotatedProperty.dataType,
                                description: annotatedProperty.description,
                                type: 'metadata'
                            });
                        })
                        if (successFn) {
                            successFn(response);
                        }
                    }
                    var _errorFn = function (err) {
                        if (errorFn) {
                            errorFn(err)
                        }
                    }

                    var promise = $http.get(RestUrlService.METADATA_PROPERTY_NAMES_URL);
                    promise.then(_successFn, _errorFn);
                    return promise;
                }

            },
            fetchRegisteredReusableFeedInputPorts: function () {

                var successFn = function (response) {
                    self.feedInputPortMap = response.data;
                }
                var errorFn = function (err) {

                }
                var promise = $http.get(RestUrlService.ALL_REUSABLE_FEED_INPUT_PORTS);
                promise.then(successFn, errorFn);
                return promise;

            },
            replaceAll: function (str, find, replace) {
                return str.replace(new RegExp(escapeRegExp(find), 'g'), replace);
            },
            deriveExpression: function (expression, configOnly) {
                var self = this;
                var replaced = false;
                if (expression != null && expression != '') {
                    var variables = expression.match(/\$\{(.*?)\}/gi);
                    if (variables && variables.length) {
                        angular.forEach(variables, function (variable) {
                            var varNameMatches = variable.match(/\$\{(.*)\}/);
                            var varName = null;
                            if (varNameMatches.length > 1) {
                                varName = varNameMatches[1];
                            }
                            if (varName) {
                                var value = self.configurationPropertyMap[varName];
                                if (value) {
                                    expression = self.replaceAll(expression, variable, value);
                                    replaced = true;
                                }

                            }
                        });
                    }
                }
                if (configOnly == true && !replaced) {
                    expression = '';
                }
                return expression;
            },
            getCodeMirrorTypes: function () {
                var self = this;
                if (this.codemirrorTypes == null) {
                    var successFn = function (response) {
                        self.codemirrorTypes = response.data;
                        angular.forEach(self.codemirrorTypes, function (label, type) {
                            self.propertyRenderTypes.push({type: type, label: label, codemirror: true});
                        });
                    }
                    var errorFn = function (err) {

                    }
                    var promise = $http.get(RestUrlService.CODE_MIRROR_TYPES_URL);
                    promise.then(successFn, errorFn);
                    return promise;
                }
                return $q.when(self.codemirrorTypes);
            },
            isRenderPropertyWithCodeMirror: function (property) {
                return this.codemirrorTypes[property.renderType] !== undefined;
            },
            /**
             * Feed Processors can setup separate Templates to have special rendering done for a processors properties.
             * @see /js/define-feed/feed-details/get-table-data-properties.
             *  This
             * @param key
             * @returns {boolean}
             */
            isCustomPropertyRendering: function (key) {
                var self = this;
                var custom = _.find(this.customPropertyRendering, function (customKey) {
                    return key == customKey;
                });
                return custom !== undefined;
            },

            /**
             * Gets all templates registered or not.  (looks at Nifi)
             * id property will ref NiFi id
             * registeredTemplateId property will be populated if registered
             * @returns {HttpPromise}
             */
            getTemplates: function () {
                var successFn = function (response) {

                }
                var errorFn = function (err) {

                }

                var promise = $http.get(RestUrlService.GET_TEMPLATES_URL);
                promise.then(successFn, errorFn);
                return promise;
            },

            /**
             * Gets the Registered Templates
             * @returns {HttpPromise}
             */
            getRegisteredTemplates: function () {
                var successFn = function (response) {

                }
                var errorFn = function (err) {

                }

                var promise = $http.get(RestUrlService.GET_REGISTERED_TEMPLATES_URL);
                promise.then(successFn, errorFn);
                return promise;
            },
            removeNonUserEditableProperties: function (processorArray, keepProcessorIfEmpty) {
                //only keep those that are userEditable:true
                var validProcessors = [];
                var processorsToRemove = [];
                //temp placeholder until Register Templates allows for user defined input processor selection

                _.each(processorArray, function (processor, i) {
                    processor.allProperties = processor.properties;

                    var validProperties = _.reject(processor.properties, function (property) {
                        return !property.userEditable;
                    });


                    processor.properties = validProperties;
                    if (validProperties != null && validProperties.length > 0) {
                        validProcessors.push(processor);
                    }
                    if (FeedDetailsProcessorRenderingHelper.isGetTableDataProcessor(processor) || FeedDetailsProcessorRenderingHelper.isWatermarkProcessor(processor)) {
                        processor.sortIndex = 0;
                    }
                    else {
                        processor.sortIndex = i;
                    }
                });
                var arr = null;

                if (keepProcessorIfEmpty != undefined && keepProcessorIfEmpty == true) {
                    arr = processorArray;
                }
                else {
                    arr = validProcessors;
                }
                // sort it
                return _.sortBy(arr, 'sortIndex');

            },
            /**
             * Updates the feedProcessingTemplateUrl for each processor in the model
             * @param model
             */
            setProcessorRenderTemplateUrl : function(model, mode){
                _.each(model.inputProcessors, function (processor) {
                    processor.feedPropertiesUrl = null;
                    //ensure the processorId attr is set
                    processor.processorId = processor.id
                    setRenderTemplateForProcessor(processor, mode);
                });
                _.each(model.nonInputProcessors, function (processor) {
                    processor.feedPropertiesUrl = null;
                    //ensure the processorId attr is set
                    processor.processorId = processor.id
                    setRenderTemplateForProcessor(processor, mode);
                });

            },
             /**
             * Setup the inputProcessor and nonInputProcessor and their properties on the registeredTemplate object
             * used in Feed creation and feed details to render the nifi input fields
             * @param template
             */
            initializeProperties: function (template, mode, feedProperties) {
                //get the saved properties

                var savedProperties = {};
                if (feedProperties) {
                    _.each(feedProperties, function (property) {
                        if (property.userEditable && property.templateProperty) {
                            savedProperties[property.templateProperty.idKey] = property;
                        }
                    });
                }


                function updateProperties(processor, properties) {

                    _.each(properties, function (property) {
                        //set the value if its saved
                        if (savedProperties[property.idKey] != undefined) {
                            property.value == savedProperties[property.idKey]
                        }
                        //mark as not selected
                        property.selected = false;

                        property.value = data.deriveExpression(property.value, false);
                        property.renderWithCodeMirror = data.isRenderPropertyWithCodeMirror(property);

                        //if it is a custom render property then dont allow the default editing.
                        //the other fields are coded to look for these specific properties
                        //otherwise check to see if it is editable
                        if (data.isCustomPropertyRendering(property.key)) {
                            property.customProperty = true;
                            property.userEditable = false;
                        } else if (property.userEditable == true) {
                            processor.userEditable = true;
                        }

                        //if it is sensitive treat the value as encrypted... store it off and use it later when saving/posting back if the value has not changed
                        FeedPropertyService.initSensitivePropertyForEditing(property);

                        FeedPropertyService.updateDisplayValue(property);

                    })

                }

                _.each(template.inputProcessors, function (processor) {
                    //ensure the processorId attr is set
                    processor.processorId = processor.id
                    updateProperties(processor, processor.properties)
                    setRenderTemplateForProcessor(processor, mode);
                });
                _.each(template.nonInputProcessors, function (processor) {
                    //ensure the processorId attr is set
                    processor.processorId = processor.id
                    updateProperties(processor, processor.properties)
                    setRenderTemplateForProcessor(processor, mode);
                });

            },
            disableTemplate: function (templateId) {
                var self = this;
                var promise = $http.post(RestUrlService.DISABLE_REGISTERED_TEMPLATE_URL(templateId)).then(function (response) {
                    self.model.state = response.data.state
                    if (self.model.state == 'ENABLED') {
                        self.model.stateIcon = 'check_circle'
                    }
                    else {
                        self.model.stateIcon = 'block'
                    }
                });
                return promise;
            },
            /**
             *
             * @param templateId
             * @returns {*}
             */
            enableTemplate: function (templateId) {
                var self = this;
                var promise = $http.post(RestUrlService.ENABLE_REGISTERED_TEMPLATE_URL(templateId)).then(function (response) {
                    self.model.state = response.data.state
                    if (self.model.state == 'ENABLED') {
                        self.model.stateIcon = 'check_circle'
                    }
                    else {
                        self.model.stateIcon = 'block'
                    }
                });
                return promise;

            },
            deleteTemplate: function (templateId) {
                var deferred = $q.defer();
                $http.delete(RestUrlService.DELETE_REGISTERED_TEMPLATE_URL(templateId)).then(function (response) {
                    deferred.resolve(response);
                }, function (response) {
                    deferred.reject(response);
                });
                return deferred.promise;
            },
            getTemplateProcessorDatasourceDefinitions: function (nifiTemplateId, inputPortIds) {
                var deferred = $q.defer();
                if (nifiTemplateId != null) {
                    $http.get(RestUrlService.TEMPLATE_PROCESSOR_DATASOURCE_DEFINITIONS(nifiTemplateId), {params: {inputPorts: inputPortIds}}).then(function (response) {
                        deferred.resolve(response);
                    }, function (response) {
                        deferred.reject(response);
                    });
                }
                else {
                    deferred.resolve({data: []});
                }
                return deferred.promise;

            },

            /**
             * Walks the NiFi template and its related connections(if any) to the reusable flow and returns data about the graph, its processors, and any Datasource definitions
             *
             * @param nifiTemplateId
             * @param reusableTemplateConnections
             * @returns {processors:[{type:"",name:"",id:"",flowId:"",isLeaf:true/false},...],
             *                        templateProcessorDatasourceDefinitions:[{processorName:"",processorType:"",
             *                                                                 datasourceDefinition:{identityString:"",title:"",description:""}},...],
             *            request:{connectionInfo:reusableTemplateConnections}}
             */
            getNiFiTemplateFlowInformation: function (nifiTemplateId, reusableTemplateConnections) {
                var deferred = $q.defer();
                if (nifiTemplateId != null) {
                    //build the request
                    var flowRequest = {};
                    flowRequest.connectionInfo = reusableTemplateConnections;

                    $http.post(RestUrlService.TEMPLATE_FLOW_INFORMATION(nifiTemplateId), flowRequest).then(function (response) {
                        deferred.resolve(response);
                    }, function (response) {
                        deferred.reject(response);
                    });
                }
                else {
                    deferred.resolve({data: []});
                }
                return deferred.promise;

            },

            /**
             * Warn if the model has multiple processors with the same name
             */
            warnInvalidProcessorNames: function () {
                if (!this.model.validTemplateProcessorNames) {
                    $mdDialog.hide();
                    $mdDialog.show(
                        $mdDialog.alert()
                            .ariaLabel("Template processor name warning")
                            .clickOutsideToClose(true)
                            .htmlContent("Warning the template contains multiple processors with the same name.  It is advised you fix this template in NiFi before registering")
                            .ok("Got it!")
                            .parent(document.body)
                            .title("Template processor name warning"));
                }
            },

            accessDeniedDialog:function() {
                $mdDialog.show(
                    $mdDialog.alert()
                        .clickOutsideToClose(true)
                        .title("Access Denied")
                        .textContent("You do not have access to edit templates.")
                        .ariaLabel("Access denied to edit templates")
                        .ok("OK")
                );
            },

            /**
             * Check access to the current template returning a promise object resovled to {allowEdit:{true/false},allowAdmin:{true,false},isValid:{true/false}}
             */
            checkTemplateAccess:function(model){
                var self = data;
                if(model == undefined){
                    model = self.model;
                }
                model.errorMessage = '';

                var entityAccessControlled = model.id != null && AccessControlService.isEntityAccessControlled();
                var deferred = $q.defer();
                var requests = {
                    entityEditAccess: entityAccessControlled == true ? self.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.TEMPLATE.EDIT_TEMPLATE,model) : true,
                    entityAdminAccess: entityAccessControlled == true ? self.hasEntityAccess(AccessControlService.ENTITY_ACCESS.TEMPLATE.DELETE_TEMPLATE,model) : true,
                    functionalAccess: AccessControlService.getUserAllowedActions()
                }

                $q.all(requests).then(function (response) {

                    var allowEditAccess = AccessControlService.hasAction(AccessControlService.TEMPLATES_EDIT, response.functionalAccess.actions);
                    var allowAdminAccess = AccessControlService.hasAction(AccessControlService.TEMPLATES_ADMIN, response.functionalAccess.actions);

                    var allowEdit = response.entityEditAccess && allowEditAccess
                    var allowAdmin = response.entityEditAccess && response.entityAdminAccess && allowAdminAccess;
                    var allowAccessControl = response.entityEditAccess && response.entityAdminAccess && allowEdit;
                    var accessAllowed = allowEdit || allowAdmin;
                    var result = {allowEdit: allowEdit,allowAdmin:allowAdmin,isValid:model.valid && accessAllowed,allowAccessControl:allowAccessControl};
                    if(!result.isValid){
                        if(!accessAllowed) {
                            model.errorMessage = "Access Denied.  You are unable to edit the template. ";
                            self.accessDeniedDialog();
                        }
                        else {
                            model.errorMessage = "Unable to proceed";
                        }
                    }
                    deferred.resolve(result);

                });
                return deferred.promise;
            },

            /**
             * Assigns the model properties and render types
             * Returns a promise
             * @returns {*}
             */
            loadTemplateWithProperties: function (registeredTemplateId, nifiTemplateId, templateName) {
                var isValid = true;

                var self = this;

                /**
                 * Assign the render types to the properties
                 * @param property
                 */
                function assignPropertyRenderType(property) {

                    var allowableValues = property.propertyDescriptor.allowableValues;
                    if (allowableValues !== undefined && allowableValues !== null && allowableValues.length > 0) {
                        if (allowableValues.length == 2) {
                            var list = _.filter(allowableValues, function (value) {
                                return (value.value.toLowerCase() == 'false' || value.value.toLowerCase() == 'true');
                            });
                            if (list != undefined && list.length == 2) {
                                property.renderTypes = self.trueFalseRenderTypes;
                            }
                        }
                        if (property.renderTypes == undefined) {
                            property.renderTypes = self.selectRenderType;
                        }
                        property.renderType = property.renderType == undefined ? 'select' : property.renderType;
                    }
                    else {
                        property.renderTypes = self.propertyRenderTypes;
                        property.renderType = property.renderType == undefined ? 'text' : property.renderType;
                    }
                }

                /**
                 * groups properties into processor groups
                 * @param properties
                 */
                function transformPropertiesToArray(properties) {
                    var inputProperties = [];
                    var additionalProperties = [];
                    var inputProcessors = [];
                    var additionalProcessors = [];
                    angular.forEach(properties, function (property, i) {
                        if (property.processor == undefined) {
                            property.processor = {};
                            property.processor.id = property.processorId;
                            property.processor.name = property.processorName;
                            property.processor.type = property.processorType;
                            property.processor.groupId = property.processGroupId;
                            property.processor.groupName = property.processGroupName;
                        }

                        if (property.selected == undefined) {
                            property.selected = false;
                        }
                        if (property.renderOptions == undefined) {
                            property.renderOptions = {};
                        }

                        // convert the saved Select options store as JSON to the array for the chips to work.
                        if (property.renderOptions['selectCustom'] == 'true') {
                            if (property.renderOptions['selectOptions']) {
                                property.selectOptions = angular.fromJson(property.renderOptions['selectOptions']);
                            }
                            else {
                                property.selectOptions = [];
                            }
                        }

                        assignPropertyRenderType(property)

                        FeedPropertyService.initSensitivePropertyForEditing(property);

                        property.templateValue = property.value;
                        property.userEditable = (property.userEditable == undefined || property.userEditable == null) ? true : property.userEditable;

                        if (property.inputProperty) {
                            property.mentioId = 'inputProperty' + property.processorName + '_' + i;
                        }
                        else {
                            property.mentioId = 'processorProperty_' + property.processorName + '_' + i;
                        }

                        if (property.inputProperty) {
                            inputProperties.push(property);
                        }
                        else {
                            additionalProperties.push(property);
                        }
                    });
                    validateTemplateProcessorNames(inputProperties, additionalProperties);
                    fixDuplicateProcessorNames(inputProperties);
                    fixDuplicateProcessorNames(additionalProperties);
                    //sort them by processor name and property key
                    var inputPropertiesAndProcessors = self.sortPropertiesForDisplay(inputProperties);
                    inputProperties = inputPropertiesAndProcessors.properties;
                    inputProcessors = inputPropertiesAndProcessors.processors;

                    var additionalPropertiesAndProcessors = self.sortPropertiesForDisplay(additionalProperties);
                    additionalProperties = additionalPropertiesAndProcessors.properties;
                    additionalProcessors = additionalPropertiesAndProcessors.processors;

                    self.model.inputProperties = inputProperties;
                    self.model.additionalProperties = additionalProperties;
                    self.model.inputProcessors = inputProcessors;
                    self.model.additionalProcessors = additionalProcessors;

                }

                /**
                 * Change the processor names for those that are duplicates
                 * @param properties
                 */
                function fixDuplicateProcessorNames(properties) {
                    var processorGroups = _.groupBy(properties, 'processorName')
                    _.each(processorGroups, function (processorProps, processorName) {
                        var processorMap = _.groupBy(processorProps, 'processorId');
                        if (Object.keys(processorMap).length > 1) {
                            //update the names
                            var lastId = null;
                            var idx = 0;
                            _.each(processorMap, function (props, processorId) {
                                if (lastId == null || lastId != processorId) {
                                    idx++;
                                    _.each(props, function (prop) {
                                        prop.processorOrigName = prop.processorName;
                                        prop.processorName += " " + idx
                                    });
                                }
                                lastId = processorId;
                            })
                        }
                    });
                }

                /**
                 * Validates the processor names in the template are unique.
                 * If not it will set the validTemplate property on self.model to false
                 */
                function validateTemplateProcessorNames(inputProperties, additionalProperties) {
                    self.model.validTemplateProcessorNames = true;
                    //validate the processor names are unique in the flow, if not warn the user
                    var groups = _.groupBy(inputProperties, 'nameKey');
                    var multiple = _.find(groups, function (arr, key) {
                        return arr.length > 1
                    });
                    if (multiple != undefined) {
                        self.model.validTemplateProcessorNames = false;
                    }

                    //validate the processor names are unique in the flow, if not warn the user
                    var groups = _.groupBy(additionalProperties, 'nameKey');
                    var multiple = _.find(groups, function (arr, key) {
                        return arr.length > 1
                    });
                    if (multiple != undefined) {
                        self.model.validTemplateProcessorNames = false;
                    }
                }

                function validate() {
                    if (self.model.reusableTemplate) {
                        self.model.valid = false;
                        var errorMessage =
                            "This is a reusable template and cannot be registered as it starts with an input port.  You need to create and register a template that has output ports that connect to this template";
                        $mdDialog.show(
                            $mdDialog.alert()
                                .ariaLabel("Error loading the template")
                                .clickOutsideToClose(true)
                                .htmlContent(errorMessage)
                                .ok("Got it!")
                                .parent(document.body)
                                .title("Error loading the template"));
                        return false;
                    }
                    else {
                        self.model.valid = true;
                        return true;
                    }
                }

                if (registeredTemplateId != null) {
                    self.resetModel();
                    //get the templateId for the registeredTemplateId
                    self.model.id = registeredTemplateId;
                }
                if (nifiTemplateId != null) {
                    self.model.nifiTemplateId = nifiTemplateId;
                }
                if (self.model.nifiTemplateId != null) {
                    self.model.loading = true;
                    var successFn = function (response) {
                        var templateData = response.data;
                        transformPropertiesToArray(templateData.properties);
                        self.model.exportUrl = RestUrlService.ADMIN_EXPORT_TEMPLATE_URL + "/" + templateData.id;
                        var nifiTemplateId = templateData.nifiTemplateId != null ? templateData.nifiTemplateId : self.model.nifiTemplateId;
                        self.model.nifiTemplateId = nifiTemplateId;
                        self.nifiTemplateId = nifiTemplateId;
                        self.model.templateName = templateData.templateName;
                        self.model.defineTable = templateData.defineTable;
                        self.model.state = templateData.state;
                        self.model.id = templateData.id;
                        if (self.model.id == null) {
                            self.model.state = 'NOT REGISTERED'
                        }
                        self.model.updateDate = templateData.updateDate;
                        self.model.feedsCount = templateData.feedsCount;
                        self.model.allowPreconditions = templateData.allowPreconditions;
                        self.model.dataTransformation = templateData.dataTransformation;
                        self.model.description = templateData.description;

                        self.model.icon.title = templateData.icon;
                        self.model.icon.color = templateData.iconColor;
                        self.model.reusableTemplate = templateData.reusableTemplate;
                        self.model.reusableTemplateConnections = templateData.reusableTemplateConnections;
                        self.model.needsReusableTemplate = templateData.reusableTemplateConnections != undefined && templateData.reusableTemplateConnections.length > 0;
                        self.model.registeredDatasourceDefinitions = templateData.registeredDatasourceDefinitions;
                        self.model.isStream = templateData.isStream;
                        self.model.owner = templateData.owner;
                        self.model.allowedActions = templateData.allowedActions;
                        self.model.roleMemberships = templateData.roleMemberships;
                        self.model.templateTableOption = templateData.templateTableOption;
                        self.model.timeBetweenStartingBatchJobs = templateData.timeBetweenStartingBatchJobs
                        if (templateData.state == 'ENABLED') {
                            self.model.stateIcon = 'check_circle'
                        }
                        else {
                            self.model.stateIcon = 'block'
                        }
                        validate();
                        self.model.loading = false;
                    }
                    var errorFn = function (err) {
                        self.model.loading = false;
                    }
                    var id = registeredTemplateId != undefined && registeredTemplateId != null ? registeredTemplateId : self.model.nifiTemplateId;
                    var promise = $http.get(RestUrlService.GET_REGISTERED_TEMPLATE_URL(id), {params: {allProperties: true, templateName: templateName}});
                    promise.then(successFn, errorFn);
                    return promise;
                }
                else {
                    var deferred = $q.defer();
                    self.properties = [];
                    deferred.resolve(self.properties);
                    return deferred.promise;
                }

            },
            /**
             * check if the user has access on an entity
             * @param permissionsToCheck an Array or a single string of a permission/action to check against this entity and current user
             * @param entity the entity to check. if its undefined it will use the current template in the model
             * @returns {*} a promise, or a true/false.  be sure to wrap this with a $q().then()
             */
            hasEntityAccess:function(permissionsToCheck,entity) {
                if(entity == undefined){
                    entity = data.model;
                }
                return  AccessControlService.hasEntityAccess(permissionsToCheck,entity,EntityAccessControlService.entityRoleTypes.TEMPLATE);
            }

        };
        data.init();
        return data;

    }]);
});
