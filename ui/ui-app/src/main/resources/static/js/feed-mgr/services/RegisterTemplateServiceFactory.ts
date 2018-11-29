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
import {Templates} from "../../../lib/feed-mgr/services/TemplateTypes";
import {FeedPropertyService} from "./FeedPropertyService";
import {AccessControl} from "../../services/AccessControl";
import {RegisteredTemplateService} from "./RegisterTemplateService";


import {moduleName} from "../module-name";
import {AccessControlService} from '../../services/AccessControlService';
import {EmptyTemplate, ExtendedTemplate, SaveAbleTemplate} from '../model/template-models';
import {EntityAccessControlService} from '../shared/entity-access-control/EntityAccessControlService';
import {FeedInputProcessorPropertiesTemplateService} from "./FeedInputProcessorPropertiesTemplateService";
import {RegisterTemplatePropertyService} from "./RegisterTemplatePropertyService";
import {FeedDetailsProcessorRenderingHelper} from "./FeedDetailsProcessorRenderingHelper";

import Property = Templates.Property;
import PropertyAndProcessors = Templates.PropertyAndProcessors;
import Processor = Templates.Processor;
import ReusableTemplateConnectionInfo = Templates.ReusableTemplateConnectionInfo;
import {Common} from '../../../lib/common/CommonTypes';

export class RegisterTemplateServiceFactory implements RegisteredTemplateService {


    static $inject = ["$http", "$q", "$mdDialog", "RestUrlService"
        , "FeedInputProcessorPropertiesTemplateService", "FeedDetailsProcessorRenderingHelper"
        , "FeedPropertyService", "AccessControlService", "EntityAccessControlService", "$filter","RegisterTemplatePropertyService"]

    constructor(private $http: angular.IHttpService, private $q: angular.IQService, private $mdDialog: angular.material.IDialogService, private RestUrlService: any
        , private feedInputProcessorPropertiesTemplateService: FeedInputProcessorPropertiesTemplateService, private feedDetailsProcessorRenderingHelper: FeedDetailsProcessorRenderingHelper
        , private feedPropertyService: FeedPropertyService, private accessControlService: AccessControlService
        , private entityAccessControlService: EntityAccessControlService, private $filter: angular.IFilterService,
        private registerTemplatePropertyService :RegisterTemplatePropertyService) {
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
      return this.registerTemplatePropertyService.setProcessorRenderTemplateUrl(processor,mode)
    }


    model: ExtendedTemplate = null;


    newModel() {
        this.model = angular.extend(new EmptyTemplate());
    }

    resetModel() {
        angular.extend(this.model, new EmptyTemplate());
        this.model.icon = { title: null, color: null }
    }

    /**
     * Initialize the service for an empty template model
     */
    init() {
        this.newModel();
    }

    /**
     * Gets the model object with attributes setup for the backend when saving a template
     * @return {{properties: any; id; description; defineTable: true | any | boolean; allowPreconditions: false | any | boolean; dataTransformation: false | any | {chartViewModel: null; datasourceIds: null; datasources: null; dataTransformScript: null; sql: null; states: Array} | {chartViewModel; datasourceIds; datasources; dataTransformScript; sql; states}; nifiTemplateId: any | string; templateName; icon; iconColor; reusableTemplate: false | any; needsReusableTemplate: false | any; reusableTemplateConnections: [] | any | Import.ReusableTemplateConnectionInfo[]; state; isStream: false | any; roleMemberships: [] | any | Array; owner; roleMembershipsUpdated: false | any; templateTableOption: any; timeBetweenStartingBatchJobs: any}}
     */
    getModelForSave(): SaveAbleTemplate {
        var saveAbleTemplate = new SaveAbleTemplate();
        saveAbleTemplate.properties = this.getSelectedProperties();
        saveAbleTemplate.id = this.model.id;
        saveAbleTemplate.description = this.model.description;
        saveAbleTemplate.defineTable = this.model.defineTable;
        saveAbleTemplate.allowPreconditions = this.model.allowPreconditions;
        saveAbleTemplate.dataTransformation = this.model.dataTransformation;
        saveAbleTemplate.nifiTemplateId = this.model.nifiTemplateId;
        saveAbleTemplate.templateName = this.model.templateName;
        saveAbleTemplate.icon = this.model.icon.title;
        saveAbleTemplate.iconColor = this.model.icon.color;
        saveAbleTemplate.reusableTemplate = this.model.reusableTemplate;
        saveAbleTemplate.needsReusableTemplate = this.model.needsReusableTemplate;
        saveAbleTemplate.reusableTemplateConnections = this.model.reusableTemplateConnections;
        saveAbleTemplate.state = this.model.state;
        saveAbleTemplate.isStream = this.model.isStream;
        saveAbleTemplate.roleMemberships = this.model.roleMemberships;
        saveAbleTemplate.owner = this.model.owner;
        saveAbleTemplate.roleMembershipsUpdated = this.model.roleMembershipsUpdated;
        saveAbleTemplate.templateTableOption = this.model.templateTableOption;
        saveAbleTemplate.timeBetweenStartingBatchJobs = this.model.timeBetweenStartingBatchJobs;
        saveAbleTemplate.changeComment = this.model.changeComment;
        return saveAbleTemplate;
    }

    /**
     * Return the reusable connection info object
     * @return {{reusableTemplateFeedName: string; feedOutputPortName: string; reusableTemplateInputPortName: string}[]}
     */
    newReusableConnectionInfo(): ReusableTemplateConnectionInfo[] {
        return [{ reusableTemplateFeedName: '', feedOutputPortName: '', reusableTemplateInputPortName: '' }];
    }

    /**
     * Is a property selected
     * @param property
     * @return {boolean | any}
     */
    isSelectedProperty(property: Property): boolean {
        return this.registerTemplatePropertyService.isSelectedProperty(property);
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

                this.feedPropertyService.initSensitivePropertyForSaving(property);
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
                this.feedPropertyService.initSensitivePropertyForSaving(property);
            }
        });

        return selectedProperties;
    }

    sortPropertiesForDisplay(properties: Property[]): PropertyAndProcessors {
      return this.registerTemplatePropertyService.sortPropertiesForDisplay(properties)
    }

    /**
     *
     * @param {(response: angular.IHttpResponse<any>) => any} successFn
     * @param {(err: any) => any} errorFn
     */
    fetchConfigurationProperties(successFn?: (response: angular.IHttpResponse<any>) => any, errorFn?: (err: any) => any): angular.IPromise<angular.IHttpResponse<Common.Map<string>>> | undefined {
return this.registerTemplatePropertyService.fetchConfigurationProperties(successFn, errorFn);

    }

    /**
     * If the propertyList is empty, then find the metadata properties available.
     * If the propertyList has already been populated this will result in a noop.
     * @param {(response: angular.IHttpResponse<any>) => any} successFn
     * @param {(err: any) => any} errorFn
     * @return {angular.IPromise<any> | undefined}
     */
    fetchMetadataProperties(successFn?: (response: angular.IHttpResponse<any>) => any, errorFn?: (err: any) => any): angular.IPromise<angular.IHttpResponse<any>> | undefined {
return this.registerTemplatePropertyService.fetchMetadataProperties(successFn, errorFn);

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

       return this.registerTemplatePropertyService.deriveExpression(expression, configOnly);
    }



    /**
     * Do we render the incoming property with codemirror?
     * @param {Templates.Property} property
     * @return {boolean}
     */
    isRenderPropertyWithCodeMirror(property: Property): boolean {
        return this.registerTemplatePropertyService.isRenderPropertyWithCodeMirror(property);
    }

    /**
     * Feed Processors can setup separate Templates to have special rendering done for a processors properties.
     * @see /js/define-feed/feed-details/get-table-data-properties.
     * @param key
     * @returns {boolean}
     */
    isCustomPropertyRendering(key: any): boolean {

      return this.registerTemplatePropertyService.isCustomPropertyRendering(key);
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
      return this.registerTemplatePropertyService.removeNonUserEditableProperties(processorArray,keepProcessorIfEmpty)

    }

    /**
     * Updates the feedProcessingTemplateUrl for each processor in the model
     * @param model
     */
    setProcessorRenderTemplateUrl(model: any, mode: any): void {
      this.registerTemplatePropertyService.setProcessorRenderTemplateUrl(model,mode)

    }


    /**
     * Setup the inputProcessor and nonInputProcessor and their properties on the registeredTemplate object
     * used in Feed creation and feed details to render the nifi input fields
     * @param template
     */
    initializeProperties(template: any, mode: any, feedProperties?: Property[]): void {
        this.registerTemplatePropertyService.initializeProperties(template,mode,feedProperties)

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
            deferred.resolve({ data: [] });
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

    accessDeniedDialog(): void {
        this.$mdDialog.show(
            this.$mdDialog.alert()
                .clickOutsideToClose(true)
                .title(this.$filter('translate')('views.main.registerService-accessDenied'))
                .textContent(this.$filter('translate')('views.main.registerService-accessDenied2'))
                .ariaLabel(this.$filter('translate')('views.main.registerService-accessDenied3'))
                .ok("OK")
        );
    }


    /**
     * Check access to the current template returning a promise object resovled to {allowEdit:{true/false},allowAdmin:{true,false},isValid:{true/false}}
     */
    checkTemplateAccess(model?: any): angular.IPromise<AccessControl.EntityAccessCheck> {
        if (model == undefined) {
            model = this.model;
        }
        model.errorMessage = '';

        var entityAccessControlled = model.id != null && this.accessControlService.isEntityAccessControlled();
        var deferred = <angular.IDeferred<AccessControl.EntityAccessCheck>>this.$q.defer();
        var requests = {
            entityEditAccess: entityAccessControlled == true ? this.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.TEMPLATE.EDIT_TEMPLATE, model) : true,
            entityAdminAccess: entityAccessControlled == true ? this.hasEntityAccess(AccessControlService.ENTITY_ACCESS.TEMPLATE.DELETE_TEMPLATE, model) : true,
            functionalAccess: this.accessControlService.getUserAllowedActions()
        }

        this.$q.all(requests).then((response: any) => {

            let allowEditAccess = this.accessControlService.hasAction(AccessControlService.TEMPLATES_EDIT, response.functionalAccess.actions);
            let allowAdminAccess = this.accessControlService.hasAction(AccessControlService.TEMPLATES_ADMIN, response.functionalAccess.actions);

            let allowEdit = response.entityEditAccess && allowEditAccess
            let allowAdmin = response.entityEditAccess && response.entityAdminAccess && allowAdminAccess;
            let allowAccessControl = response.entityEditAccess && response.entityAdminAccess && allowEdit;
            let accessAllowed = allowEdit || allowAdmin;
            let result: AccessControl.EntityAccessCheck = { allowEdit: allowEdit, allowAdmin: allowAdmin, isValid: model.valid && accessAllowed, allowAccessControl: allowAccessControl };
            if (!result.isValid) {
                if (!accessAllowed) {
                    model.errorMessage = "Access Denied.  You are unable to edit the template. ";
                    this.accessDeniedDialog();
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
    loadTemplateWithProperties(registeredTemplateId: string, nifiTemplateId: string, templateName?: string): angular.IPromise<any> {
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
                        property.renderTypes = RegisterTemplatePropertyService.trueFalseRenderTypes;
                    }
                }
                if (property.renderTypes == undefined) {
                    property.renderTypes = RegisterTemplatePropertyService.selectRenderType;
                }
                property.renderType = property.renderType == undefined ? 'select' : property.renderType;
            }
            else {
                property.renderTypes = this.registerTemplatePropertyService.propertyRenderTypes;
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

                this.feedPropertyService.initSensitivePropertyForEditing([property]);

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

            var additionalPropertiesAndProcessors: PropertyAndProcessors = this.sortPropertiesForDisplay(additionalProperties);
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
                var processorMap = <Dictionary<Property[]>>_.groupBy(processorProps, 'processorId');
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
                this.model.timeBetweenStartingBatchJobs = templateData.timeBetweenStartingBatchJobs;
                this.model.changeComments = templateData.changeComments;

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
            var promise = this.$http.get(this.RestUrlService.GET_REGISTERED_TEMPLATE_URL(id), { params: { allProperties: true, templateName: templateName } });
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
    hasEntityAccess(permissionsToCheck: any, entity?: any): boolean {
        if (entity == undefined) {
            entity = this.model;
        }
        return this.accessControlService.hasEntityAccess(permissionsToCheck, entity, EntityAccessControlService.entityRoleTypes.TEMPLATE);
    }
}


angular.module(moduleName).service('RegisterTemplateService', RegisterTemplateServiceFactory);
