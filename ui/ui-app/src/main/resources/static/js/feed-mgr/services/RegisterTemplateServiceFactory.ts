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

import * as angular from 'angular';
import * as _ from "underscore";
import {Dictionary} from "underscore";
import 'pascalprecht.translate';
import {Templates} from "./TemplateTypes";
import {FeedPropertyService} from "./FeedPropertyService";
import {Common} from "../../common/CommonTypes";
import {AccessControl} from "../../services/AccessControl";
import {RegisteredTemplateService} from "./RegisterTemplateService";

const moduleName = require('feed-mgr/module-name');
import Property = Templates.Property;
import PropertyRenderType = Templates.PropertyRenderType;
import PropertyAndProcessors = Templates.PropertyAndProcessors;
import Processor = Templates.Processor;
import MetadataProperty = Templates.MetadataProperty;
import ReusableTemplateConnectionInfo = Templates.ReusableTemplateConnectionInfo;

export class RegisterTemplateServiceFactory implements RegisteredTemplateService {


    static $inject = ["$http", "$q", "$mdDialog", "RestUrlService"
        , "FeedInputProcessorOptionsFactory", "FeedDetailsProcessorRenderingHelper"
        , "FeedPropertyService", "AccessControlService", "EntityAccessControlService", "$filter"]

    constructor(private $http: angular.IHttpService, private $q: angular.IQService, private $mdDialog: angular.material.IDialogService, private RestUrlService: any
        , private FeedInputProcessorOptionsFactory: any, private FeedDetailsProcessorRenderingHelper: any
        , private FeedPropertyService: FeedPropertyService, private AccessControlService: any
        , private EntityAccessControlService: any, private $filter: angular.IFilterService) {
        this.init();

    }

    private escapeRegExp(str: string): string {
        return str.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
    }

    /**
     * for a given processor assign the feedPropertiesUrl so it can be rendered correctly
     * @param processor
     */
    private setRenderTemplateForProcessor(processor: Processor, mode: any) {
        if (processor.feedPropertiesUrl == undefined) {
            processor.feedPropertiesUrl = null;
        }
        if (processor.feedPropertiesUrl == null) {
            this.FeedInputProcessorOptionsFactory.setFeedProcessingTemplateUrl(processor, mode);
        }
    }

    /**
     * Properties that require custom Rendering, separate from the standard Nifi Property (key  value) rendering
     * This is used in conjunction with the method {@code this.isCustomPropertyRendering(key)} to determine how to render the property to the end user
     */
    customPropertyRendering: string[] = ["metadata.table.targetFormat", "metadata.table.feedFormat"];

    codemirrorTypes: Common.Map<string> = null;

    /**
     * Avaliable types that a user can select for property rendering
     */
    propertyRenderTypes: PropertyRenderType[] = [{type: 'text', 'label': 'Text'}, {type: 'password', 'label': 'Password'}, {type: 'number', 'label': 'Number', codemirror: false},
        {type: 'textarea', 'label': 'Textarea', codemirror: false}, {type: 'select', label: 'Select', codemirror: false},
        {type: 'checkbox-custom', 'label': 'Checkbox', codemirror: false}];

    trueFalseRenderTypes: PropertyRenderType[] = [{type: 'checkbox-true-false', 'label': 'Checkbox', codemirror: false},
        {type: 'select', label: 'Select', codemirror: false}]


    selectRenderType: PropertyRenderType[] = [{type: 'select', 'label': 'Select', codemirror: false},
        {type: 'radio', label: 'Radio Button', codemirror: false}]

    codeMirrorRenderTypes: any[] = []

    configurationProperties: Common.Map<string> = {}

    metadataProperties: MetadataProperty[] = []

    propertyList: MetadataProperty[] = []

    configurationPropertyMap: Common.Map<string> = {};

    model: any = null;

    emptyModel: any = {
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
        roleMemberships: [],
        owner: null,
        roleMembershipsUpdated: false
    }

    newModel() {
        this.model = angular.copy(this.emptyModel);
    }

    resetModel() {
        angular.extend(this.model, this.emptyModel);
        this.model.icon = {title: null, color: null}
    }

    /**
     * Initialize the service for an empty template model
     */
    init() {
        this.newModel();
        this.fetchConfigurationProperties();
        this.fetchMetadataProperties();
        this.getCodeMirrorTypes();
    }

    /**
     * Gets the model object with attributes setup for the backend when saving a template
     * @return {{properties: any; id; description; defineTable: true | any | boolean; allowPreconditions: false | any | boolean; dataTransformation: false | any | {chartViewModel: null; datasourceIds: null; datasources: null; dataTransformScript: null; sql: null; states: Array} | {chartViewModel; datasourceIds; datasources; dataTransformScript; sql; states}; nifiTemplateId: any | string; templateName; icon; iconColor; reusableTemplate: false | any; needsReusableTemplate: false | any; reusableTemplateConnections: [] | any | Import.ReusableTemplateConnectionInfo[]; state; isStream: false | any; roleMemberships: [] | any | Array; owner; roleMembershipsUpdated: false | any; templateTableOption: any; timeBetweenStartingBatchJobs: any}}
     */
    getModelForSave() {
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

        }
    }

    /**
     * Return the reusable connection info object
     * @return {{reusableTemplateFeedName: string; feedOutputPortName: string; reusableTemplateInputPortName: string}[]}
     */
    newReusableConnectionInfo(): ReusableTemplateConnectionInfo[] {
        return [{reusableTemplateFeedName: '', feedOutputPortName: '', reusableTemplateInputPortName: ''}];
    }

    /**
     * Is a probpery selected
     * @param property
     * @return {boolean | any}
     */
    isSelectedProperty(property: Property): boolean {
        var selected = (property.selected || (property.value != null && property.value != undefined && (property.value.includes("${metadata") || property.value.includes("${config."))));
        if (selected) {
            property.selected = true;
        }
        return selected;
    }

    /**
     * Returns the properties that are selected and also
     * @return {Templates.Property[]}
     */
    getSelectedProperties(): Property[] {
        let selectedProperties: Property[] = [];

        angular.forEach(this.model.inputProperties, (property: Property) => {
            if (this.isSelectedProperty(property)) {
                selectedProperties.push(property)
                if (property.processor && property.processor.topIndex != undefined) {
                    delete property.processor.topIndex;
                }
                if (property.processorOrigName != undefined && property.processorOrigName != null) {
                    property.processorName = property.processorOrigName;
                }

                this.FeedPropertyService.initSensitivePropertyForSaving(property);
            }
        });

        angular.forEach(this.model.additionalProperties, (property: Property) => {
            if (this.isSelectedProperty(property)) {
                selectedProperties.push(property);
                if (property.processor && property.processor.topIndex != undefined) {
                    delete property.processor.topIndex;
                }
                if (property.processorOrigName != undefined && property.processorOrigName != null) {
                    property.processorName = property.processorOrigName;
                }
                this.FeedPropertyService.initSensitivePropertyForSaving(property);
            }
        });

        return selectedProperties;
    }

    sortPropertiesForDisplay(properties: Property[]): PropertyAndProcessors {
        let propertiesAndProcessors: PropertyAndProcessors = {properties: [], processors: []};

        //sort them by processor name and property key
        var arr = _.chain(properties).sortBy('key').sortBy('processorName').value();
        propertiesAndProcessors.properties = arr;
        //set the initial processor flag for the heading to print
        var lastProcessorId: string = null;
        _.each(arr, (property, i) => {
            if ((angular.isUndefined(property.hidden) || property.hidden == false) && (lastProcessorId == null || property.processor.id != lastProcessorId)) {
                property.firstProperty = true;
                propertiesAndProcessors.processors.push(property.processor);
                property.processor.topIndex = i;
            }
            else {
                property.firstProperty = false;
            }
            if(!property.hidden) {
                lastProcessorId = property.processor.id;
            }
        });
        return propertiesAndProcessors;
    }

    /**
     *
     * @param {(response: angular.IHttpResponse<any>) => any} successFn
     * @param {(err: any) => any} errorFn
     */
    fetchConfigurationProperties(successFn?: (response: angular.IHttpResponse<any>) => any, errorFn?: (err: any) => any): angular.IPromise<angular.IHttpResponse<Common.Map<string>>> | undefined {


        if (Object.keys(this.configurationProperties).length == 0) {
            let _successFn = (response: angular.IHttpResponse<Common.Map<string>>) => {
                this.configurationProperties = response.data;
                angular.forEach(response.data, (value: any, key: any) => {
                    this.propertyList.push({key: key, value: value, description: null, dataType: null, type: 'configuration'});
                    this.configurationPropertyMap[key] = value;
                })
                if (successFn) {
                    successFn(response);
                }
            }
            let _errorFn = (err: any) => {
                if (errorFn) {
                    errorFn(err)
                }
            }

            var promise = <angular.IPromise<angular.IHttpResponse<Common.Map<string>>>>  this.$http.get(this.RestUrlService.CONFIGURATION_PROPERTIES_URL);
            promise.then(_successFn, _errorFn);
            return promise;
        }

    }

    /**
     * If the propertyList is empty, then find the metadata properties available.
     * If the propertyList has already been populated this will result in a noop.
     * @param {(response: angular.IHttpResponse<any>) => any} successFn
     * @param {(err: any) => any} errorFn
     * @return {angular.IPromise<any> | undefined}
     */
    fetchMetadataProperties(successFn?: (response: angular.IHttpResponse<any>) => any, errorFn?: (err: any) => any): angular.IPromise<angular.IHttpResponse<any>> | undefined {

        if (this.metadataProperties.length == 0) {
            let _successFn = (response: angular.IHttpResponse<any>) => {
                this.metadataProperties = response.data;
                angular.forEach(response.data, (annotatedProperty: MetadataProperty, i: any) => {
                    this.propertyList.push({
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
            let _errorFn = (err: any) => {
                if (errorFn) {
                    errorFn(err)
                }
            }

            var promise = this.$http.get(this.RestUrlService.METADATA_PROPERTY_NAMES_URL);
            promise.then(_successFn, _errorFn);
            return promise;
        }

    }

    /**
     * Find the input ports associated with the reusable templates to make connections
     * @return {angular.IPromise<any>}
     */
    fetchRegisteredReusableFeedInputPorts(): angular.IPromise<angular.IHttpResponse<any>> {

        let successFn = (response: angular.IHttpResponse<any>) => {
        }
        var errorFn = (err: any) => {

        }
        var promise = this.$http.get(this.RestUrlService.ALL_REUSABLE_FEED_INPUT_PORTS);
        promise.then(successFn, errorFn);
        return promise;
    }

    /**
     * Fetch the input PortDTO objects on the Root process group
     * @return {angular.IPromise<angular.IHttpResponse<any>>}
     */
    fetchRootInputPorts(): angular.IPromise<angular.IHttpResponse<any>> {
       return this.$http.get(this.RestUrlService.ROOT_INPUT_PORTS);
    }

    replaceAll(str: string, find: string, replace: string): string {
        return str.replace(new RegExp(this.escapeRegExp(find), 'g'), replace);
    }


    deriveExpression(expression: string, configOnly: boolean): string {

        var replaced = false;
        if (expression != null && expression != '') {
            var variables = expression.match(/\$\{(.*?)\}/gi);
            if (variables && variables.length) {
                angular.forEach(variables, (variable: any) => {
                    var varNameMatches = variable.match(/\$\{(.*)\}/);
                    var varName = null;
                    if (varNameMatches.length > 1) {
                        varName = varNameMatches[1];
                    }
                    if (varName) {
                        let value = this.configurationPropertyMap[varName];
                        if (value) {
                            expression = this.replaceAll(expression, variable, value);
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
    }


    getCodeMirrorTypes(): angular.IPromise<any> {

        if (this.codemirrorTypes == null) {
            let successFn = (response: angular.IHttpResponse<any>) => {
                this.codemirrorTypes = response.data;
                angular.forEach(this.codemirrorTypes, (label: string, type: string) => {
                    this.propertyRenderTypes.push({type: type, label: label, codemirror: true});
                });
            }
            var errorFn = (err: angular.IHttpResponse<any>) => {

            }
            var promise = <angular.IPromise<angular.IHttpResponse<Common.Map<string>>>> this.$http.get(this.RestUrlService.CODE_MIRROR_TYPES_URL);
            promise.then(successFn, errorFn);
            return promise;
        }
        return this.$q.when(this.codemirrorTypes);
    }


    /**
     * Do we render the incoming property with codemirror?
     * @param {Templates.Property} property
     * @return {boolean}
     */
    isRenderPropertyWithCodeMirror(property: Property): boolean {
        return this.codemirrorTypes[property.renderType] !== undefined;
    }

    /**
     * Feed Processors can setup separate Templates to have special rendering done for a processors properties.
     * @see /js/define-feed/feed-details/get-table-data-properties.
     * @param key
     * @returns {boolean}
     */
    isCustomPropertyRendering(key: any): boolean {

        var custom = _.find(this.customPropertyRendering, (customKey) => {
            return key == customKey;
        });
        return custom !== undefined;
    }


    /**
     * Gets all templates registered or not.  (looks at Nifi)
     * id property will ref NiFi id
     * registeredTemplateId property will be populated if registered
     * @returns {HttpPromise}
     */
    getTemplates(): angular.IPromise<any> {
        let successFn = (response: angular.IHttpResponse<any>) => {

        }
        let errorFn = (err: angular.IHttpResponse<any>) => {

        }

        var promise = this.$http.get(this.RestUrlService.GET_TEMPLATES_URL);
        promise.then(successFn, errorFn);
        return promise;
    }

    /**
     * Gets the Registered Templates
     * @returns {HttpPromise}
     */
    getRegisteredTemplates(): angular.IPromise<any> {
        let successFn = (response: angular.IHttpResponse<any>) => {

        }
        let errorFn = (err: angular.IHttpResponse<any>) => {

        }

        var promise = this.$http.get(this.RestUrlService.GET_REGISTERED_TEMPLATES_URL);
        promise.then(successFn, errorFn);
        return promise;
    }

    /**
     * Remove any processor properties that are not 'userEditable = true'
     * @param {Templates.Processor[]} processorArray
     * @param {boolean} keepProcessorIfEmpty
     * @return {Processor[]}
     */
    removeNonUserEditableProperties(processorArray: Processor[], keepProcessorIfEmpty: boolean): Processor[] {
        //only keep those that are userEditable:true
        var validProcessors: any = [];
        var processorsToRemove: any = [];
        //temp placeholder until Register Templates allows for user defined input processor selection

        _.each(processorArray, (processor: Processor, i: number) => {
            processor.allProperties = processor.properties;

            var validProperties = _.reject(processor.properties, (property: Property) => {
                return !property.userEditable;
            });


            processor.properties = validProperties;
            if (validProperties != null && validProperties.length > 0) {
                validProcessors.push(processor);
            }
            if (this.FeedDetailsProcessorRenderingHelper.isGetTableDataProcessor(processor) || this.FeedDetailsProcessorRenderingHelper.isWatermarkProcessor(processor)) {
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

    }

    /**
     * Updates the feedProcessingTemplateUrl for each processor in the model
     * @param model
     */
    setProcessorRenderTemplateUrl(model: any, mode: any): void {
        _.each(model.inputProcessors, (processor: Processor) => {
            processor.feedPropertiesUrl = null;
            //ensure the processorId attr is set
            processor.processorId = processor.id
            this.setRenderTemplateForProcessor(processor, mode);
        });
        _.each(model.nonInputProcessors, (processor: Processor) => {
            processor.feedPropertiesUrl = null;
            //ensure the processorId attr is set
            processor.processorId = processor.id
            this.setRenderTemplateForProcessor(processor, mode);
        });

    }


    /**
     * Setup the inputProcessor and nonInputProcessor and their properties on the registeredTemplate object
     * used in Feed creation and feed details to render the nifi input fields
     * @param template
     */
    initializeProperties(template: any, mode: any, feedProperties: Property[]): void {
        //get the saved properties

        /**
         * Propert.idKey to value
         * @type {{}}
         */
        let savedProperties: Common.Map<string> = {};

        if (feedProperties) {
            _.each(feedProperties, (property: any) => {
                if (property.userEditable && property.templateProperty) {
                    savedProperties[property.templateProperty.idKey] = property;
                }
            });
        }


        let updateProperties = (processor: any, properties: any) => {

            _.each(properties, (property: Property) => {
                //set the value if its saved
                if (savedProperties[property.idKey] != undefined) {
                    property.value == savedProperties[property.idKey]
                }
                //mark as not selected
                property.selected = false;

                property.value = this.deriveExpression(property.value, false);
                property.renderWithCodeMirror = this.isRenderPropertyWithCodeMirror(property);

                //if it is a custom render property then dont allow the default editing.
                //the other fields are coded to look for these specific properties
                //otherwise check to see if it is editable
                if (this.isCustomPropertyRendering(property.key)) {
                    property.customProperty = true;
                    property.userEditable = false;
                } else if (property.userEditable == true) {
                    processor.userEditable = true;
                }

                //if it is sensitive treat the value as encrypted... store it off and use it later when saving/posting back if the value has not changed
                this.FeedPropertyService.initSensitivePropertyForEditing(property);

                this.FeedPropertyService.updateDisplayValue(property);

            })

        }

        _.each(template.inputProcessors, (processor: Processor) => {
            //ensure the processorId attr is set
            processor.processorId = processor.id
            updateProperties(processor, processor.properties)
            this.setRenderTemplateForProcessor(processor, mode);
        });
        _.each(template.nonInputProcessors, (processor: Processor) => {
            //ensure the processorId attr is set
            processor.processorId = processor.id
            updateProperties(processor, processor.properties)
            this.setRenderTemplateForProcessor(processor, mode);
        });

    }

    disableTemplate(templateId: string): angular.IHttpPromise<any> {

        var promise = this.$http.post(this.RestUrlService.DISABLE_REGISTERED_TEMPLATE_URL(templateId), null);
        promise.then((response: angular.IHttpResponse<any>) => {
            this.model.state = response.data.state
            if (this.model.state == 'ENABLED') {
                this.model.stateIcon = 'check_circle'
            }
            else {
                this.model.stateIcon = 'block'
            }
        });
        return promise;
    }


    /**
     *
     * @param templateId
     * @returns {*}
     */
    enableTemplate(templateId: any): angular.IHttpPromise<any> {

        var promise = this.$http.post(this.RestUrlService.ENABLE_REGISTERED_TEMPLATE_URL(templateId), null);
        promise.then((response: angular.IHttpResponse<any>) => {
            this.model.state = response.data.state
            if (this.model.state == 'ENABLED') {
                this.model.stateIcon = 'check_circle'
            }
            else {
                this.model.stateIcon = 'block'
            }
        });
        return promise;

    }

    deleteTemplate(templateId: string): angular.IPromise<any> {
        var deferred = this.$q.defer();
        this.$http.delete(this.RestUrlService.DELETE_REGISTERED_TEMPLATE_URL(templateId)).then((response: angular.IHttpResponse<any>) => {
            deferred.resolve(response);
        }, (response: any) => {
            deferred.reject(response);
        });
        return deferred.promise;
    }

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
    getNiFiTemplateFlowInformation(nifiTemplateId: string, reusableTemplateConnections: ReusableTemplateConnectionInfo[]): angular.IPromise<any> {
        var deferred = this.$q.defer();
        if (nifiTemplateId != null) {
            //build the request
            var flowRequest: any = {};
            flowRequest.connectionInfo = reusableTemplateConnections;

            this.$http.post(this.RestUrlService.TEMPLATE_FLOW_INFORMATION(nifiTemplateId), flowRequest).then((response: angular.IHttpResponse<any>) => {
                deferred.resolve(response);
            }, (response: any) => {
                deferred.reject(response);
            });
        }
        else {
            deferred.resolve({data: []});
        }
        return deferred.promise;

    }

    /**
     * Warn if the model has multiple processors with the same name
     */
    warnInvalidProcessorNames(): void {
        if (!this.model.validTemplateProcessorNames) {
            this.$mdDialog.hide();
            this.$mdDialog.show(
                this.$mdDialog.alert()
                    .ariaLabel("Template processor name warning")
                    .clickOutsideToClose(true)
                    .htmlContent("Warning the template contains multiple processors with the same name.  It is advised you fix this template in NiFi before registering")
                    .ok("Got it!")
                    .parent(document.body)
                    .title("Template processor name warning"));
        }
    }

    accessDeniedDialog($filter: angular.IFilterService): void {
        this.$mdDialog.show(
            this.$mdDialog.alert()
                .clickOutsideToClose(true)
                .title($filter('translate')('views.main.registerService-accessDenied'))
                .textContent($filter('translate')('views.main.registerService-accessDenied2'))
                .ariaLabel($filter('translate')('views.main.registerService-accessDenied3'))
                .ok("OK")
        );
    }


    /**
     * Check access to the current template returning a promise object resovled to {allowEdit:{true/false},allowAdmin:{true,false},isValid:{true/false}}
     */
    checkTemplateAccess(model: any): angular.IPromise<AccessControl.EntityAccessCheck> {
        if (model == undefined) {
            model = this.model;
        }
        model.errorMessage = '';

        var entityAccessControlled = model.id != null && this.AccessControlService.isEntityAccessControlled();
        var deferred = <angular.IDeferred<AccessControl.EntityAccessCheck>> this.$q.defer();
        var requests = {
            entityEditAccess: entityAccessControlled == true ? this.hasEntityAccess(this.EntityAccessControlService.ENTITY_ACCESS.TEMPLATE.EDIT_TEMPLATE, model) : true,
            entityAdminAccess: entityAccessControlled == true ? this.hasEntityAccess(this.AccessControlService.ENTITY_ACCESS.TEMPLATE.DELETE_TEMPLATE, model) : true,
            functionalAccess: this.AccessControlService.getUserAllowedActions()
        }

        this.$q.all(requests).then((response: any) => {

            let allowEditAccess = this.AccessControlService.hasAction(this.AccessControlService.TEMPLATES_EDIT, response.functionalAccess.actions);
            let allowAdminAccess = this.AccessControlService.hasAction(this.AccessControlService.TEMPLATES_ADMIN, response.functionalAccess.actions);

            let allowEdit = response.entityEditAccess && allowEditAccess
            let allowAdmin = response.entityEditAccess && response.entityAdminAccess && allowAdminAccess;
            let allowAccessControl = response.entityEditAccess && response.entityAdminAccess && allowEdit;
            let accessAllowed = allowEdit || allowAdmin;
            let result: AccessControl.EntityAccessCheck = {allowEdit: allowEdit, allowAdmin: allowAdmin, isValid: model.valid && accessAllowed, allowAccessControl: allowAccessControl};
            if (!result.isValid) {
                if (!accessAllowed) {
                    model.errorMessage = "Access Denied.  You are unable to edit the template. ";
                    this.accessDeniedDialog(this.$filter);
                }
                else {
                    model.errorMessage = "Unable to proceed";
                }
            }
            deferred.resolve(result);

        });
        return deferred.promise;
    }

    /**
     * Assigns the model properties and render types
     * Returns a promise
     * @returns {*}
     */
    loadTemplateWithProperties(registeredTemplateId: string, nifiTemplateId: string, templateName: string): angular.IPromise<any> {
        var isValid = true;


        /**
         * Assign the render types to the properties
         * @param property
         */
        let assignPropertyRenderType = (property: any) => {

            var allowableValues = property.propertyDescriptor.allowableValues;
            if (allowableValues !== undefined && allowableValues !== null && allowableValues.length > 0) {
                if (allowableValues.length == 2) {
                    var list = _.filter(allowableValues, (value: any) => {
                        return (value.value.toLowerCase() == 'false' || value.value.toLowerCase() == 'true');
                    });
                    if (list != undefined && list.length == 2) {
                        property.renderTypes = this.trueFalseRenderTypes;
                    }
                }
                if (property.renderTypes == undefined) {
                    property.renderTypes = this.selectRenderType;
                }
                property.renderType = property.renderType == undefined ? 'select' : property.renderType;
            }
            else {
                property.renderTypes = this.propertyRenderTypes;
                property.renderType = property.renderType == undefined ? 'text' : property.renderType;
            }
        }

        /**
         * groups properties into processor groups
         * @param properties
         */
        let transformPropertiesToArray = (properties: Property) => {
            var inputProperties: any = [];
            var additionalProperties: any = [];
            var inputProcessors = [];
            var additionalProcessors = [];
            angular.forEach(properties, (property: Property, i: any) => {
                if (property.processor == undefined) {
                    property.processor = {
                        processorId: property.processorId,
                        id: property.processorId,
                        name: property.processorName,
                        type: property.processorType,
                        groupId: property.processGroupId,
                        groupName: property.processGroupName
                    }
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

                this.FeedPropertyService.initSensitivePropertyForEditing(property);

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
            var inputPropertiesAndProcessors = this.sortPropertiesForDisplay(inputProperties);
            inputProperties = inputPropertiesAndProcessors.properties;
            inputProcessors = inputPropertiesAndProcessors.processors;

            var additionalPropertiesAndProcessors = this.sortPropertiesForDisplay(additionalProperties);
            additionalProperties = additionalPropertiesAndProcessors.properties;
            additionalProcessors = additionalPropertiesAndProcessors.processors;

            this.model.inputProperties = inputProperties;
            this.model.additionalProperties = additionalProperties;
            this.model.inputProcessors = inputProcessors;
            this.model.additionalProcessors = additionalProcessors;

        }

        /**
         * Change the processor names for those that are duplicates
         * @param properties
         */
        let fixDuplicateProcessorNames = (properties: Property[]) => {
            let processorGroups = _.groupBy(properties, 'processorName')
            _.each(processorGroups, (processorProps, processorName) => {
                var processorMap = <Dictionary<Property[]>> _.groupBy(processorProps, 'processorId');
                if (Object.keys(processorMap).length > 1) {
                    //update the names
                    var lastId: any = null;
                    var idx = 0;
                    _.each(processorMap, (props, processorId) => {
                        if (lastId == null || lastId != processorId) {
                            idx++;
                            _.each(props, (prop: Property) => {
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
         * If not it will set the validTemplate property on this.model to false
         */
        let validateTemplateProcessorNames = (inputProperties: Property[], additionalProperties: Property[]) => {
            this.model.validTemplateProcessorNames = true;
            //validate the processor names are unique in the flow, if not warn the user
            var groups = _.groupBy(inputProperties, 'nameKey');
            var multiple = _.find(groups, (arr, key) => {
                return arr.length > 1
            });
            if (multiple != undefined) {
                this.model.validTemplateProcessorNames = false;
            }

            //validate the processor names are unique in the flow, if not warn the user
            var groups = _.groupBy(additionalProperties, 'nameKey');
            var multiple = _.find(groups, (arr, key) => {
                return arr.length > 1
            });
            if (multiple != undefined) {
                this.model.validTemplateProcessorNames = false;
            }
        }

        let validate = () => {
            if (this.model.reusableTemplate) {
                this.model.valid = false;
                var errorMessage =
                    "This is a reusable template and cannot be registered as it starts with an input port.  You need to create and register a template that has output ports that connect to this template";
                this.$mdDialog.show(
                    this.$mdDialog.alert()
                        .ariaLabel("Error loading the template")
                        .clickOutsideToClose(true)
                        .htmlContent(errorMessage)
                        .ok("Got it!")
                        .parent(document.body)
                        .title("Error loading the template"));
                return false;
            }
            else {
                this.model.valid = true;
                return true;
            }
        }

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
            let successFn = (response: angular.IHttpResponse<any>) => {
                var templateData = response.data;
                transformPropertiesToArray(templateData.properties);
                this.model.exportUrl = this.RestUrlService.ADMIN_EXPORT_TEMPLATE_URL + "/" + templateData.id;
                var nifiTemplateId = templateData.nifiTemplateId != null ? templateData.nifiTemplateId : this.model.nifiTemplateId;
                this.model.nifiTemplateId = nifiTemplateId;
                //this.nifiTemplateId = nifiTemplateId;
                this.model.templateName = templateData.templateName;
                this.model.defineTable = templateData.defineTable;
                this.model.state = templateData.state;
                this.model.id = templateData.id;
                if (this.model.id == null) {
                    this.model.state = 'NOT REGISTERED'
                }
                this.model.updateDate = templateData.updateDate;
                this.model.feedsCount = templateData.feedsCount;
                this.model.allowPreconditions = templateData.allowPreconditions;
                this.model.dataTransformation = templateData.dataTransformation;
                this.model.description = templateData.description;

                this.model.icon.title = templateData.icon;
                this.model.icon.color = templateData.iconColor;
                this.model.reusableTemplate = templateData.reusableTemplate;
                this.model.reusableTemplateConnections = templateData.reusableTemplateConnections;
                this.model.needsReusableTemplate = templateData.reusableTemplateConnections != undefined && templateData.reusableTemplateConnections.length > 0;
                this.model.registeredDatasourceDefinitions = templateData.registeredDatasourceDefinitions;
                this.model.isStream = templateData.isStream;
                this.model.owner = templateData.owner;
                this.model.allowedActions = templateData.allowedActions;
                this.model.roleMemberships = templateData.roleMemberships;
                this.model.templateTableOption = templateData.templateTableOption;
                this.model.timeBetweenStartingBatchJobs = templateData.timeBetweenStartingBatchJobs
                if (templateData.state == 'ENABLED') {
                    this.model.stateIcon = 'check_circle'
                }
                else {
                    this.model.stateIcon = 'block'
                }
                validate();
                this.model.loading = false;
            }
            let errorFn = (err: any) => {
                this.model.loading = false;
            }
            var id = registeredTemplateId != undefined && registeredTemplateId != null ? registeredTemplateId : this.model.nifiTemplateId;
            var promise = this.$http.get(this.RestUrlService.GET_REGISTERED_TEMPLATE_URL(id), {params: {allProperties: true, templateName: templateName}});
            promise.then(successFn, errorFn);
            return promise;
        }
        else {
            var deferred = this.$q.defer();
            deferred.resolve([]);
            return deferred.promise;
        }

    }


    /**
     * check if the user has access on an entity
     * @param permissionsToCheck an Array or a single string of a permission/action to check against this entity and current user
     * @param entity the entity to check. if its undefined it will use the current template in the model
     * @returns {*} a promise, or a true/false.  be sure to wrap this with a $q().then()
     */
    hasEntityAccess(permissionsToCheck: any, entity: any): boolean {
        if (entity == undefined) {
            entity = this.model;
        }
        return this.AccessControlService.hasEntityAccess(permissionsToCheck, entity, this.EntityAccessControlService.entityRoleTypes.TEMPLATE);
    }

    static factory() {
        let instance = ($http: angular.IHttpService, $q: angular.IQService, $mdDialog: angular.material.IDialogService, RestUrlService: any
            , FeedInputProcessorOptionsFactory: any, FeedDetailsProcessorRenderingHelper: any
            , FeedPropertyService: FeedPropertyService, AccessControlService: any
            , EntityAccessControlService: any, $filter: angular.IFilterService) =>
             new RegisterTemplateServiceFactory($http, $q, $mdDialog, RestUrlService, FeedInputProcessorOptionsFactory, FeedDetailsProcessorRenderingHelper, FeedPropertyService, AccessControlService, EntityAccessControlService, $filter);


        return instance;
    }
}


angular.module(moduleName).factory('RegisterTemplateService',  RegisterTemplateServiceFactory.factory());