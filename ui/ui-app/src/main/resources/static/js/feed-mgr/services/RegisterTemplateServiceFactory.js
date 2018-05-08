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
define(["require", "exports", "angular", "underscore", "pascalprecht.translate"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/module-name');
    var RegisterTemplateServiceFactory = /** @class */ (function () {
        function RegisterTemplateServiceFactory($http, $q, $mdDialog, RestUrlService, FeedInputProcessorOptionsFactory, FeedDetailsProcessorRenderingHelper, FeedPropertyService, AccessControlService, EntityAccessControlService, $filter) {
            this.$http = $http;
            this.$q = $q;
            this.$mdDialog = $mdDialog;
            this.RestUrlService = RestUrlService;
            this.FeedInputProcessorOptionsFactory = FeedInputProcessorOptionsFactory;
            this.FeedDetailsProcessorRenderingHelper = FeedDetailsProcessorRenderingHelper;
            this.FeedPropertyService = FeedPropertyService;
            this.AccessControlService = AccessControlService;
            this.EntityAccessControlService = EntityAccessControlService;
            this.$filter = $filter;
            /**
             * Properties that require custom Rendering, separate from the standard Nifi Property (key  value) rendering
             * This is used in conjunction with the method {@code this.isCustomPropertyRendering(key)} to determine how to render the property to the end user
             */
            this.customPropertyRendering = ["metadata.table.targetFormat", "metadata.table.feedFormat"];
            this.codemirrorTypes = null;
            /**
             * Avaliable types that a user can select for property rendering
             */
            this.propertyRenderTypes = [{ type: 'text', 'label': 'Text' }, { type: 'password', 'label': 'Password' }, { type: 'number', 'label': 'Number', codemirror: false },
                { type: 'textarea', 'label': 'Textarea', codemirror: false }, { type: 'select', label: 'Select', codemirror: false },
                { type: 'checkbox-custom', 'label': 'Checkbox', codemirror: false }];
            this.trueFalseRenderTypes = [{ type: 'checkbox-true-false', 'label': 'Checkbox', codemirror: false },
                { type: 'select', label: 'Select', codemirror: false }];
            this.selectRenderType = [{ type: 'select', 'label': 'Select', codemirror: false },
                { type: 'radio', label: 'Radio Button', codemirror: false }];
            this.codeMirrorRenderTypes = [];
            this.configurationProperties = {};
            this.metadataProperties = [];
            this.propertyList = [];
            this.configurationPropertyMap = {};
            this.model = null;
            this.emptyModel = {
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
                reusableTemplateConnections: [],
                icon: { title: null, color: null },
                state: 'NOT REGISTERED',
                updateDate: null,
                feedsCount: 0,
                registeredDatasources: [],
                isStream: false,
                validTemplateProcessorNames: true,
                roleMemberships: [],
                owner: null,
                roleMembershipsUpdated: false
            };
            this.init();
        }
        RegisterTemplateServiceFactory.prototype.escapeRegExp = function (str) {
            return str.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
        };
        /**
         * for a given processor assign the feedPropertiesUrl so it can be rendered correctly
         * @param processor
         */
        RegisterTemplateServiceFactory.prototype.setRenderTemplateForProcessor = function (processor, mode) {
            if (processor.feedPropertiesUrl == undefined) {
                processor.feedPropertiesUrl = null;
            }
            if (processor.feedPropertiesUrl == null) {
                this.FeedInputProcessorOptionsFactory.setFeedProcessingTemplateUrl(processor, mode);
            }
        };
        RegisterTemplateServiceFactory.prototype.newModel = function () {
            this.model = angular.copy(this.emptyModel);
        };
        RegisterTemplateServiceFactory.prototype.resetModel = function () {
            angular.extend(this.model, this.emptyModel);
            this.model.icon = { title: null, color: null };
        };
        /**
         * Initialize the service for an empty template model
         */
        RegisterTemplateServiceFactory.prototype.init = function () {
            this.newModel();
            this.fetchConfigurationProperties();
            this.fetchMetadataProperties();
            this.getCodeMirrorTypes();
        };
        /**
         * Gets the model object with attributes setup for the backend when saving a template
         * @return {{properties: any; id; description; defineTable: true | any | boolean; allowPreconditions: false | any | boolean; dataTransformation: false | any | {chartViewModel: null; datasourceIds: null; datasources: null; dataTransformScript: null; sql: null; states: Array} | {chartViewModel; datasourceIds; datasources; dataTransformScript; sql; states}; nifiTemplateId: any | string; templateName; icon; iconColor; reusableTemplate: false | any; needsReusableTemplate: false | any; reusableTemplateConnections: [] | any | Import.ReusableTemplateConnectionInfo[]; state; isStream: false | any; roleMemberships: [] | any | Array; owner; roleMembershipsUpdated: false | any; templateTableOption: any; timeBetweenStartingBatchJobs: any}}
         */
        RegisterTemplateServiceFactory.prototype.getModelForSave = function () {
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
                roleMemberships: this.model.roleMemberships,
                owner: this.model.owner,
                roleMembershipsUpdated: this.model.roleMembershipsUpdated,
                templateTableOption: this.model.templateTableOption,
                timeBetweenStartingBatchJobs: this.model.timeBetweenStartingBatchJobs
            };
        };
        /**
         * Return the reusable connection info object
         * @return {{reusableTemplateFeedName: string; feedOutputPortName: string; reusableTemplateInputPortName: string}[]}
         */
        RegisterTemplateServiceFactory.prototype.newReusableConnectionInfo = function () {
            return [{ reusableTemplateFeedName: '', feedOutputPortName: '', reusableTemplateInputPortName: '' }];
        };
        /**
         * Is a probpery selected
         * @param property
         * @return {boolean | any}
         */
        RegisterTemplateServiceFactory.prototype.isSelectedProperty = function (property) {
            var selected = (property.selected || (property.value != null && property.value != undefined && (property.value.includes("${metadata") || property.value.includes("${config."))));
            if (selected) {
                property.selected = true;
            }
            return selected;
        };
        /**
         * Returns the properties that are selected and also
         * @return {Templates.Property[]}
         */
        RegisterTemplateServiceFactory.prototype.getSelectedProperties = function () {
            var _this = this;
            var selectedProperties = [];
            angular.forEach(this.model.inputProperties, function (property) {
                if (_this.isSelectedProperty(property)) {
                    selectedProperties.push(property);
                    if (property.processor && property.processor.topIndex != undefined) {
                        delete property.processor.topIndex;
                    }
                    if (property.processorOrigName != undefined && property.processorOrigName != null) {
                        property.processorName = property.processorOrigName;
                    }
                    _this.FeedPropertyService.initSensitivePropertyForSaving(property);
                }
            });
            angular.forEach(this.model.additionalProperties, function (property) {
                if (_this.isSelectedProperty(property)) {
                    selectedProperties.push(property);
                    if (property.processor && property.processor.topIndex != undefined) {
                        delete property.processor.topIndex;
                    }
                    if (property.processorOrigName != undefined && property.processorOrigName != null) {
                        property.processorName = property.processorOrigName;
                    }
                    _this.FeedPropertyService.initSensitivePropertyForSaving(property);
                }
            });
            return selectedProperties;
        };
        RegisterTemplateServiceFactory.prototype.sortPropertiesForDisplay = function (properties) {
            var propertiesAndProcessors = { properties: [], processors: [] };
            //sort them by processor name and property key
            var arr = _.chain(properties).sortBy('key').sortBy('processorName').value();
            propertiesAndProcessors.properties = arr;
            //set the initial processor flag for the heading to print
            var lastProcessorId = null;
            _.each(arr, function (property, i) {
                if ((angular.isUndefined(property.hidden) || property.hidden == false) && (lastProcessorId == null || property.processor.id != lastProcessorId)) {
                    property.firstProperty = true;
                    propertiesAndProcessors.processors.push(property.processor);
                    property.processor.topIndex = i;
                }
                else {
                    property.firstProperty = false;
                }
                if (!property.hidden) {
                    lastProcessorId = property.processor.id;
                }
            });
            return propertiesAndProcessors;
        };
        /**
         *
         * @param {(response: angular.IHttpResponse<any>) => any} successFn
         * @param {(err: any) => any} errorFn
         */
        RegisterTemplateServiceFactory.prototype.fetchConfigurationProperties = function (successFn, errorFn) {
            var _this = this;
            if (Object.keys(this.configurationProperties).length == 0) {
                var _successFn = function (response) {
                    _this.configurationProperties = response.data;
                    angular.forEach(response.data, function (value, key) {
                        _this.propertyList.push({ key: key, value: value, description: null, dataType: null, type: 'configuration' });
                        _this.configurationPropertyMap[key] = value;
                    });
                    if (successFn) {
                        successFn(response);
                    }
                };
                var _errorFn = function (err) {
                    if (errorFn) {
                        errorFn(err);
                    }
                };
                var promise = this.$http.get(this.RestUrlService.CONFIGURATION_PROPERTIES_URL);
                promise.then(_successFn, _errorFn);
                return promise;
            }
        };
        /**
         * If the propertyList is empty, then find the metadata properties available.
         * If the propertyList has already been populated this will result in a noop.
         * @param {(response: angular.IHttpResponse<any>) => any} successFn
         * @param {(err: any) => any} errorFn
         * @return {angular.IPromise<any> | undefined}
         */
        RegisterTemplateServiceFactory.prototype.fetchMetadataProperties = function (successFn, errorFn) {
            var _this = this;
            if (this.metadataProperties.length == 0) {
                var _successFn = function (response) {
                    _this.metadataProperties = response.data;
                    angular.forEach(response.data, function (annotatedProperty, i) {
                        _this.propertyList.push({
                            key: annotatedProperty.name,
                            value: '',
                            dataType: annotatedProperty.dataType,
                            description: annotatedProperty.description,
                            type: 'metadata'
                        });
                    });
                    if (successFn) {
                        successFn(response);
                    }
                };
                var _errorFn = function (err) {
                    if (errorFn) {
                        errorFn(err);
                    }
                };
                var promise = this.$http.get(this.RestUrlService.METADATA_PROPERTY_NAMES_URL);
                promise.then(_successFn, _errorFn);
                return promise;
            }
        };
        /**
         * Find the input ports associated with the reusable templates to make connections
         * @return {angular.IPromise<any>}
         */
        RegisterTemplateServiceFactory.prototype.fetchRegisteredReusableFeedInputPorts = function () {
            var successFn = function (response) {
            };
            var errorFn = function (err) {
            };
            var promise = this.$http.get(this.RestUrlService.ALL_REUSABLE_FEED_INPUT_PORTS);
            promise.then(successFn, errorFn);
            return promise;
        };
        /**
         * Fetch the input PortDTO objects on the Root process group
         * @return {angular.IPromise<angular.IHttpResponse<any>>}
         */
        RegisterTemplateServiceFactory.prototype.fetchRootInputPorts = function () {
            return this.$http.get(this.RestUrlService.ROOT_INPUT_PORTS);
        };
        RegisterTemplateServiceFactory.prototype.replaceAll = function (str, find, replace) {
            return str.replace(new RegExp(this.escapeRegExp(find), 'g'), replace);
        };
        RegisterTemplateServiceFactory.prototype.deriveExpression = function (expression, configOnly) {
            var _this = this;
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
                            var value = _this.configurationPropertyMap[varName];
                            if (value) {
                                expression = _this.replaceAll(expression, variable, value);
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
        };
        RegisterTemplateServiceFactory.prototype.getCodeMirrorTypes = function () {
            var _this = this;
            if (this.codemirrorTypes == null) {
                var successFn = function (response) {
                    _this.codemirrorTypes = response.data;
                    angular.forEach(_this.codemirrorTypes, function (label, type) {
                        _this.propertyRenderTypes.push({ type: type, label: label, codemirror: true });
                    });
                };
                var errorFn = function (err) {
                };
                var promise = this.$http.get(this.RestUrlService.CODE_MIRROR_TYPES_URL);
                promise.then(successFn, errorFn);
                return promise;
            }
            return this.$q.when(this.codemirrorTypes);
        };
        /**
         * Do we render the incoming property with codemirror?
         * @param {Templates.Property} property
         * @return {boolean}
         */
        RegisterTemplateServiceFactory.prototype.isRenderPropertyWithCodeMirror = function (property) {
            return this.codemirrorTypes[property.renderType] !== undefined;
        };
        /**
         * Feed Processors can setup separate Templates to have special rendering done for a processors properties.
         * @see /js/define-feed/feed-details/get-table-data-properties.
         * @param key
         * @returns {boolean}
         */
        RegisterTemplateServiceFactory.prototype.isCustomPropertyRendering = function (key) {
            var custom = _.find(this.customPropertyRendering, function (customKey) {
                return key == customKey;
            });
            return custom !== undefined;
        };
        /**
         * Gets all templates registered or not.  (looks at Nifi)
         * id property will ref NiFi id
         * registeredTemplateId property will be populated if registered
         * @returns {HttpPromise}
         */
        RegisterTemplateServiceFactory.prototype.getTemplates = function () {
            var successFn = function (response) {
            };
            var errorFn = function (err) {
            };
            var promise = this.$http.get(this.RestUrlService.GET_TEMPLATES_URL);
            promise.then(successFn, errorFn);
            return promise;
        };
        /**
         * Gets the Registered Templates
         * @returns {HttpPromise}
         */
        RegisterTemplateServiceFactory.prototype.getRegisteredTemplates = function () {
            var successFn = function (response) {
            };
            var errorFn = function (err) {
            };
            var promise = this.$http.get(this.RestUrlService.GET_REGISTERED_TEMPLATES_URL);
            promise.then(successFn, errorFn);
            return promise;
        };
        /**
         * Remove any processor properties that are not 'userEditable = true'
         * @param {Templates.Processor[]} processorArray
         * @param {boolean} keepProcessorIfEmpty
         * @return {Processor[]}
         */
        RegisterTemplateServiceFactory.prototype.removeNonUserEditableProperties = function (processorArray, keepProcessorIfEmpty) {
            var _this = this;
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
                if (_this.FeedDetailsProcessorRenderingHelper.isGetTableDataProcessor(processor) || _this.FeedDetailsProcessorRenderingHelper.isWatermarkProcessor(processor)) {
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
        };
        /**
         * Updates the feedProcessingTemplateUrl for each processor in the model
         * @param model
         */
        RegisterTemplateServiceFactory.prototype.setProcessorRenderTemplateUrl = function (model, mode) {
            var _this = this;
            _.each(model.inputProcessors, function (processor) {
                processor.feedPropertiesUrl = null;
                //ensure the processorId attr is set
                processor.processorId = processor.id;
                _this.setRenderTemplateForProcessor(processor, mode);
            });
            _.each(model.nonInputProcessors, function (processor) {
                processor.feedPropertiesUrl = null;
                //ensure the processorId attr is set
                processor.processorId = processor.id;
                _this.setRenderTemplateForProcessor(processor, mode);
            });
        };
        /**
         * Setup the inputProcessor and nonInputProcessor and their properties on the registeredTemplate object
         * used in Feed creation and feed details to render the nifi input fields
         * @param template
         */
        RegisterTemplateServiceFactory.prototype.initializeProperties = function (template, mode, feedProperties) {
            //get the saved properties
            var _this = this;
            /**
             * Propert.idKey to value
             * @type {{}}
             */
            var savedProperties = {};
            if (feedProperties) {
                _.each(feedProperties, function (property) {
                    if (property.userEditable && property.templateProperty) {
                        savedProperties[property.templateProperty.idKey] = property;
                    }
                });
            }
            var updateProperties = function (processor, properties) {
                _.each(properties, function (property) {
                    //set the value if its saved
                    if (savedProperties[property.idKey] != undefined) {
                        property.value == savedProperties[property.idKey];
                    }
                    //mark as not selected
                    property.selected = false;
                    property.value = _this.deriveExpression(property.value, false);
                    property.renderWithCodeMirror = _this.isRenderPropertyWithCodeMirror(property);
                    //if it is a custom render property then dont allow the default editing.
                    //the other fields are coded to look for these specific properties
                    //otherwise check to see if it is editable
                    if (_this.isCustomPropertyRendering(property.key)) {
                        property.customProperty = true;
                        property.userEditable = false;
                    }
                    else if (property.userEditable == true) {
                        processor.userEditable = true;
                    }
                    //if it is sensitive treat the value as encrypted... store it off and use it later when saving/posting back if the value has not changed
                    _this.FeedPropertyService.initSensitivePropertyForEditing(property);
                    _this.FeedPropertyService.updateDisplayValue(property);
                });
            };
            _.each(template.inputProcessors, function (processor) {
                //ensure the processorId attr is set
                processor.processorId = processor.id;
                updateProperties(processor, processor.properties);
                _this.setRenderTemplateForProcessor(processor, mode);
            });
            _.each(template.nonInputProcessors, function (processor) {
                //ensure the processorId attr is set
                processor.processorId = processor.id;
                updateProperties(processor, processor.properties);
                _this.setRenderTemplateForProcessor(processor, mode);
            });
        };
        RegisterTemplateServiceFactory.prototype.disableTemplate = function (templateId) {
            var _this = this;
            var promise = this.$http.post(this.RestUrlService.DISABLE_REGISTERED_TEMPLATE_URL(templateId), null);
            promise.then(function (response) {
                _this.model.state = response.data.state;
                if (_this.model.state == 'ENABLED') {
                    _this.model.stateIcon = 'check_circle';
                }
                else {
                    _this.model.stateIcon = 'block';
                }
            });
            return promise;
        };
        /**
         *
         * @param templateId
         * @returns {*}
         */
        RegisterTemplateServiceFactory.prototype.enableTemplate = function (templateId) {
            var _this = this;
            var promise = this.$http.post(this.RestUrlService.ENABLE_REGISTERED_TEMPLATE_URL(templateId), null);
            promise.then(function (response) {
                _this.model.state = response.data.state;
                if (_this.model.state == 'ENABLED') {
                    _this.model.stateIcon = 'check_circle';
                }
                else {
                    _this.model.stateIcon = 'block';
                }
            });
            return promise;
        };
        RegisterTemplateServiceFactory.prototype.deleteTemplate = function (templateId) {
            var deferred = this.$q.defer();
            this.$http.delete(this.RestUrlService.DELETE_REGISTERED_TEMPLATE_URL(templateId)).then(function (response) {
                deferred.resolve(response);
            }, function (response) {
                deferred.reject(response);
            });
            return deferred.promise;
        };
        /*
        getTemplateProcessorDatasourceDefinitions(nifiTemplateId:string, inputPortIds:string[]) {
            var deferred = $q.defer();
            if (nifiTemplateId != null) {
                $http.get(this.RestUrlService.TEMPLATE_PROCESSOR_DATASOURCE_DEFINITIONS(nifiTemplateId), {params: {inputPorts: inputPortIds}}).then((response:any) =>{
                    deferred.resolve(response);
                }, function (response:any) {
                    deferred.reject(response);
                });
            }
            else {
                deferred.resolve({data: []});
            }
            return deferred.promise;
    
        }
        */
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
        RegisterTemplateServiceFactory.prototype.getNiFiTemplateFlowInformation = function (nifiTemplateId, reusableTemplateConnections) {
            var deferred = this.$q.defer();
            if (nifiTemplateId != null) {
                //build the request
                var flowRequest = {};
                flowRequest.connectionInfo = reusableTemplateConnections;
                this.$http.post(this.RestUrlService.TEMPLATE_FLOW_INFORMATION(nifiTemplateId), flowRequest).then(function (response) {
                    deferred.resolve(response);
                }, function (response) {
                    deferred.reject(response);
                });
            }
            else {
                deferred.resolve({ data: [] });
            }
            return deferred.promise;
        };
        /**
         * Warn if the model has multiple processors with the same name
         */
        RegisterTemplateServiceFactory.prototype.warnInvalidProcessorNames = function () {
            if (!this.model.validTemplateProcessorNames) {
                this.$mdDialog.hide();
                this.$mdDialog.show(this.$mdDialog.alert()
                    .ariaLabel("Template processor name warning")
                    .clickOutsideToClose(true)
                    .htmlContent("Warning the template contains multiple processors with the same name.  It is advised you fix this template in NiFi before registering")
                    .ok("Got it!")
                    .parent(document.body)
                    .title("Template processor name warning"));
            }
        };
        RegisterTemplateServiceFactory.prototype.accessDeniedDialog = function ($filter) {
            this.$mdDialog.show(this.$mdDialog.alert()
                .clickOutsideToClose(true)
                .title($filter('translate')('views.main.registerService-accessDenied'))
                .textContent($filter('translate')('views.main.registerService-accessDenied2'))
                .ariaLabel($filter('translate')('views.main.registerService-accessDenied3'))
                .ok("OK"));
        };
        /**
         * Check access to the current template returning a promise object resovled to {allowEdit:{true/false},allowAdmin:{true,false},isValid:{true/false}}
         */
        RegisterTemplateServiceFactory.prototype.checkTemplateAccess = function (model) {
            var _this = this;
            if (model == undefined) {
                model = this.model;
            }
            model.errorMessage = '';
            var entityAccessControlled = model.id != null && this.AccessControlService.isEntityAccessControlled();
            var deferred = this.$q.defer();
            var requests = {
                entityEditAccess: entityAccessControlled == true ? this.hasEntityAccess(this.EntityAccessControlService.ENTITY_ACCESS.TEMPLATE.EDIT_TEMPLATE, model) : true,
                entityAdminAccess: entityAccessControlled == true ? this.hasEntityAccess(this.AccessControlService.ENTITY_ACCESS.TEMPLATE.DELETE_TEMPLATE, model) : true,
                functionalAccess: this.AccessControlService.getUserAllowedActions()
            };
            this.$q.all(requests).then(function (response) {
                var allowEditAccess = _this.AccessControlService.hasAction(_this.AccessControlService.TEMPLATES_EDIT, response.functionalAccess.actions);
                var allowAdminAccess = _this.AccessControlService.hasAction(_this.AccessControlService.TEMPLATES_ADMIN, response.functionalAccess.actions);
                var allowEdit = response.entityEditAccess && allowEditAccess;
                var allowAdmin = response.entityEditAccess && response.entityAdminAccess && allowAdminAccess;
                var allowAccessControl = response.entityEditAccess && response.entityAdminAccess && allowEdit;
                var accessAllowed = allowEdit || allowAdmin;
                var result = { allowEdit: allowEdit, allowAdmin: allowAdmin, isValid: model.valid && accessAllowed, allowAccessControl: allowAccessControl };
                if (!result.isValid) {
                    if (!accessAllowed) {
                        model.errorMessage = "Access Denied.  You are unable to edit the template. ";
                        _this.accessDeniedDialog(_this.$filter);
                    }
                    else {
                        model.errorMessage = "Unable to proceed";
                    }
                }
                deferred.resolve(result);
            });
            return deferred.promise;
        };
        /**
         * Assigns the model properties and render types
         * Returns a promise
         * @returns {*}
         */
        RegisterTemplateServiceFactory.prototype.loadTemplateWithProperties = function (registeredTemplateId, nifiTemplateId, templateName) {
            var _this = this;
            var isValid = true;
            /**
             * Assign the render types to the properties
             * @param property
             */
            var assignPropertyRenderType = function (property) {
                var allowableValues = property.propertyDescriptor.allowableValues;
                if (allowableValues !== undefined && allowableValues !== null && allowableValues.length > 0) {
                    if (allowableValues.length == 2) {
                        var list = _.filter(allowableValues, function (value) {
                            return (value.value.toLowerCase() == 'false' || value.value.toLowerCase() == 'true');
                        });
                        if (list != undefined && list.length == 2) {
                            property.renderTypes = _this.trueFalseRenderTypes;
                        }
                    }
                    if (property.renderTypes == undefined) {
                        property.renderTypes = _this.selectRenderType;
                    }
                    property.renderType = property.renderType == undefined ? 'select' : property.renderType;
                }
                else {
                    property.renderTypes = _this.propertyRenderTypes;
                    property.renderType = property.renderType == undefined ? 'text' : property.renderType;
                }
            };
            /**
             * groups properties into processor groups
             * @param properties
             */
            var transformPropertiesToArray = function (properties) {
                var inputProperties = [];
                var additionalProperties = [];
                var inputProcessors = [];
                var additionalProcessors = [];
                angular.forEach(properties, function (property, i) {
                    if (property.processor == undefined) {
                        property.processor = {
                            processorId: property.processorId,
                            id: property.processorId,
                            name: property.processorName,
                            type: property.processorType,
                            groupId: property.processGroupId,
                            groupName: property.processGroupName
                        };
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
                    assignPropertyRenderType(property);
                    _this.FeedPropertyService.initSensitivePropertyForEditing(property);
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
                var inputPropertiesAndProcessors = _this.sortPropertiesForDisplay(inputProperties);
                inputProperties = inputPropertiesAndProcessors.properties;
                inputProcessors = inputPropertiesAndProcessors.processors;
                var additionalPropertiesAndProcessors = _this.sortPropertiesForDisplay(additionalProperties);
                additionalProperties = additionalPropertiesAndProcessors.properties;
                additionalProcessors = additionalPropertiesAndProcessors.processors;
                _this.model.inputProperties = inputProperties;
                _this.model.additionalProperties = additionalProperties;
                _this.model.inputProcessors = inputProcessors;
                _this.model.additionalProcessors = additionalProcessors;
            };
            /**
             * Change the processor names for those that are duplicates
             * @param properties
             */
            var fixDuplicateProcessorNames = function (properties) {
                var processorGroups = _.groupBy(properties, 'processorName');
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
                                    prop.processorName += " " + idx;
                                });
                            }
                            lastId = processorId;
                        });
                    }
                });
            };
            /**
             * Validates the processor names in the template are unique.
             * If not it will set the validTemplate property on this.model to false
             */
            var validateTemplateProcessorNames = function (inputProperties, additionalProperties) {
                _this.model.validTemplateProcessorNames = true;
                //validate the processor names are unique in the flow, if not warn the user
                var groups = _.groupBy(inputProperties, 'nameKey');
                var multiple = _.find(groups, function (arr, key) {
                    return arr.length > 1;
                });
                if (multiple != undefined) {
                    _this.model.validTemplateProcessorNames = false;
                }
                //validate the processor names are unique in the flow, if not warn the user
                var groups = _.groupBy(additionalProperties, 'nameKey');
                var multiple = _.find(groups, function (arr, key) {
                    return arr.length > 1;
                });
                if (multiple != undefined) {
                    _this.model.validTemplateProcessorNames = false;
                }
            };
            var validate = function () {
                if (_this.model.reusableTemplate) {
                    _this.model.valid = false;
                    var errorMessage = "This is a reusable template and cannot be registered as it starts with an input port.  You need to create and register a template that has output ports that connect to this template";
                    _this.$mdDialog.show(_this.$mdDialog.alert()
                        .ariaLabel("Error loading the template")
                        .clickOutsideToClose(true)
                        .htmlContent(errorMessage)
                        .ok("Got it!")
                        .parent(document.body)
                        .title("Error loading the template"));
                    return false;
                }
                else {
                    _this.model.valid = true;
                    return true;
                }
            };
            if (registeredTemplateId != null) {
                this.resetModel();
                //get the templateId for the registeredTemplateId
                this.model.id = registeredTemplateId;
            }
            if (nifiTemplateId != null) {
                this.model.nifiTemplateId = nifiTemplateId;
            }
            if (this.model.nifiTemplateId != null) {
                this.model.loading = true;
                var successFn = function (response) {
                    var templateData = response.data;
                    transformPropertiesToArray(templateData.properties);
                    _this.model.exportUrl = _this.RestUrlService.ADMIN_EXPORT_TEMPLATE_URL + "/" + templateData.id;
                    var nifiTemplateId = templateData.nifiTemplateId != null ? templateData.nifiTemplateId : _this.model.nifiTemplateId;
                    _this.model.nifiTemplateId = nifiTemplateId;
                    //this.nifiTemplateId = nifiTemplateId;
                    _this.model.templateName = templateData.templateName;
                    _this.model.defineTable = templateData.defineTable;
                    _this.model.state = templateData.state;
                    _this.model.id = templateData.id;
                    if (_this.model.id == null) {
                        _this.model.state = 'NOT REGISTERED';
                    }
                    _this.model.updateDate = templateData.updateDate;
                    _this.model.feedsCount = templateData.feedsCount;
                    _this.model.allowPreconditions = templateData.allowPreconditions;
                    _this.model.dataTransformation = templateData.dataTransformation;
                    _this.model.description = templateData.description;
                    _this.model.icon.title = templateData.icon;
                    _this.model.icon.color = templateData.iconColor;
                    _this.model.reusableTemplate = templateData.reusableTemplate;
                    _this.model.reusableTemplateConnections = templateData.reusableTemplateConnections;
                    _this.model.needsReusableTemplate = templateData.reusableTemplateConnections != undefined && templateData.reusableTemplateConnections.length > 0;
                    _this.model.registeredDatasourceDefinitions = templateData.registeredDatasourceDefinitions;
                    _this.model.isStream = templateData.isStream;
                    _this.model.owner = templateData.owner;
                    _this.model.allowedActions = templateData.allowedActions;
                    _this.model.roleMemberships = templateData.roleMemberships;
                    _this.model.templateTableOption = templateData.templateTableOption;
                    _this.model.timeBetweenStartingBatchJobs = templateData.timeBetweenStartingBatchJobs;
                    if (templateData.state == 'ENABLED') {
                        _this.model.stateIcon = 'check_circle';
                    }
                    else {
                        _this.model.stateIcon = 'block';
                    }
                    validate();
                    _this.model.loading = false;
                };
                var errorFn = function (err) {
                    _this.model.loading = false;
                };
                var id = registeredTemplateId != undefined && registeredTemplateId != null ? registeredTemplateId : this.model.nifiTemplateId;
                var promise = this.$http.get(this.RestUrlService.GET_REGISTERED_TEMPLATE_URL(id), { params: { allProperties: true, templateName: templateName } });
                promise.then(successFn, errorFn);
                return promise;
            }
            else {
                var deferred = this.$q.defer();
                deferred.resolve([]);
                return deferred.promise;
            }
        };
        /**
         * check if the user has access on an entity
         * @param permissionsToCheck an Array or a single string of a permission/action to check against this entity and current user
         * @param entity the entity to check. if its undefined it will use the current template in the model
         * @returns {*} a promise, or a true/false.  be sure to wrap this with a $q().then()
         */
        RegisterTemplateServiceFactory.prototype.hasEntityAccess = function (permissionsToCheck, entity) {
            if (entity == undefined) {
                entity = this.model;
            }
            return this.AccessControlService.hasEntityAccess(permissionsToCheck, entity, this.EntityAccessControlService.entityRoleTypes.TEMPLATE);
        };
        RegisterTemplateServiceFactory.factory = function () {
            var instance = function ($http, $q, $mdDialog, RestUrlService, FeedInputProcessorOptionsFactory, FeedDetailsProcessorRenderingHelper, FeedPropertyService, AccessControlService, EntityAccessControlService, $filter) {
                return new RegisterTemplateServiceFactory($http, $q, $mdDialog, RestUrlService, FeedInputProcessorOptionsFactory, FeedDetailsProcessorRenderingHelper, FeedPropertyService, AccessControlService, EntityAccessControlService, $filter);
            };
            return instance;
        };
        RegisterTemplateServiceFactory.$inject = ["$http", "$q", "$mdDialog", "RestUrlService",
            "FeedInputProcessorOptionsFactory", "FeedDetailsProcessorRenderingHelper",
            "FeedPropertyService", "AccessControlService", "EntityAccessControlService", "$filter"];
        return RegisterTemplateServiceFactory;
    }());
    exports.RegisterTemplateServiceFactory = RegisterTemplateServiceFactory;
    angular.module(moduleName).factory('RegisterTemplateService', RegisterTemplateServiceFactory.factory());
});
//# sourceMappingURL=RegisterTemplateServiceFactory.js.map