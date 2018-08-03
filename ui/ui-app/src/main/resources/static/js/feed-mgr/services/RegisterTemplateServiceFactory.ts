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
import { Dictionary } from "underscore";
import 'pascalprecht.translate';
import { Templates } from "./TemplateTypes";
import { FeedPropertyService } from "./FeedPropertyService";
import { Common } from "../../common/CommonTypes";
import { AccessControl } from "../../services/AccessControl";
import { RegisteredTemplateService } from "./RegisterTemplateService";


import Property = Templates.Property;
import PropertyRenderType = Templates.PropertyRenderType;
import PropertyAndProcessors = Templates.PropertyAndProcessors;
import Processor = Templates.Processor;
import MetadataProperty = Templates.MetadataProperty;
import ReusableTemplateConnectionInfo = Templates.ReusableTemplateConnectionInfo;
import AccessControlService from '../../services/AccessControlService';
import { EmptyTemplate, ExtendedTemplate, SaveAbleTemplate } from '../model/template-models';
import { EntityAccessControlService } from '../shared/entity-access-control/EntityAccessControlService';
import { Injectable, Inject } from '@angular/core';
import { RestUrlService } from './RestUrlService';
import { Subject } from 'rxjs/Subject';
import { DefaultFeedPropertyService } from './DefaultFeedPropertyService';
import { FeedInputProcessorPropertiesTemplateService } from "./FeedInputProcessorPropertiesTemplateService";
import { HttpClient, HttpParams } from "@angular/common/http";
import { FeedDetailsProcessorRenderingHelper } from "./FeedDetailsProcessorRenderingHelper";
import { TdDialogService } from "@covalent/core/dialogs";

@Injectable()
export class RegisterTemplateServiceFactory implements RegisteredTemplateService {

    constructor(
                private http: HttpClient,
                private RestUrlService: RestUrlService, 
                private FeedPropertyService: DefaultFeedPropertyService, 
                private accessControlService: AccessControlService,
                private entityAccessControlService: EntityAccessControlService,
                private feedInputProcessorPropertiesTemplateService: FeedInputProcessorPropertiesTemplateService,
                private feedDetailsProcessorRenderingHelper: FeedDetailsProcessorRenderingHelper,
                private _dialogService: TdDialogService) {

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
            this.feedInputProcessorPropertiesTemplateService.setFeedProcessingTemplateUrl(processor, mode);
        }
    }

    /**
     * Properties that require custom Rendering, separate from the standard Nifi Property (key  value) rendering
     * This is used in conjunction with the method {@code this.isCustomPropertyRendering(key)} to determine how to render the property to the end user
     */
    customPropertyRendering: string[] = ["metadata.table.targetFormat", "metadata.table.feedFormat"];

    public codemirrorTypes: Common.Map<string> = null;

    codeMirrorTypesObserver = new Subject<any>();

    /**
     * Avaliable types that a user can select for property rendering
     */
    propertyRenderTypes: PropertyRenderType[] = [{ type: 'text', 'label': 'Text' }, { type: 'password', 'label': 'Password' }, { type: 'number', 'label': 'Number', codemirror: false },
    { type: 'textarea', 'label': 'Textarea', codemirror: false }, { type: 'select', label: 'Select', codemirror: false },
    { type: 'checkbox-custom', 'label': 'Checkbox', codemirror: false }];

    trueFalseRenderTypes: PropertyRenderType[] = [{ type: 'checkbox-true-false', 'label': 'Checkbox', codemirror: false },
    { type: 'select', label: 'Select', codemirror: false }]


    selectRenderType: PropertyRenderType[] = [{ type: 'select', 'label': 'Select', codemirror: false },
    { type: 'radio', label: 'Radio Button', codemirror: false }]

    codeMirrorRenderTypes: any[] = []

    configurationProperties: Common.Map<string> = {}

    metadataProperties: MetadataProperty[] = []

    propertyList: MetadataProperty[] = []

    configurationPropertyMap: Common.Map<string> = {};

    model: ExtendedTemplate = null;

    modelLoadingObserver = new Subject<any>();

    modelTemplateTableOptionObserver = new Subject<any>();

    modelInputObserver = new Subject<any>();

    modelNifiTemplateIdObserver = new Subject<any>();

    newModel() {
        this.model = {...this.model,...new EmptyTemplate()};
    }

    resetModel() {
        this.model = {...this.model,...new EmptyTemplate()};
        this.model.icon = { title: null, color: null }
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

        this.model.inputProperties.forEach((property: Property) => {
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

        this.model.additionalProperties.forEach((property: Property) => {
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
        let propertiesAndProcessors: PropertyAndProcessors = { properties: [], processors: [] };

        //sort them by processor name and property key
        var arr = _.chain(properties).sortBy('key').sortBy('processorName').value();
        propertiesAndProcessors.properties = arr;
        //set the initial processor flag for the heading to print
        var lastProcessorId: string = null;
        _.each(arr, (property, i) => {
            if ((typeof property.hidden === 'undefined' || property.hidden == false) && (lastProcessorId == null || property.processor.id != lastProcessorId)) {
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
    }

    /**
     *
     * @param {(response:any) => any} successFn
     * @param {(err: any) => any} errorFn
     */
    fetchConfigurationProperties(successFn?: (response: any) => any, errorFn?: (err: any) => any): Promise<any> {


        if (Object.keys(this.configurationProperties).length == 0) {
            let _successFn = (response: any) => {
                this.configurationProperties = response;
                Object.keys(response).forEach( (key: any) => {
                    this.propertyList.push({ key: key, value: response[key], description: null, dataType: null, type: 'configuration' });
                    this.configurationPropertyMap[key] = response[key];
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

            var promise = this.http.get(this.RestUrlService.CONFIGURATION_PROPERTIES_URL).toPromise();
            promise.then(_successFn, _errorFn);
            return promise;
        }

    }

    /**
     * If the propertyList is empty, then find the metadata properties available.
     * If the propertyList has already been populated this will result in a noop.
     * @param {(response: any) => any} successFn
     * @param {(err: any) => any} errorFn
     * @return {Promise<any>}
     */
    fetchMetadataProperties(successFn?: (response: any) => any, errorFn?: (err: any) => any): any {

        if (this.metadataProperties.length == 0) {
            let _successFn = (response: any) => {
                this.metadataProperties = response;
                response.forEach((annotatedProperty: MetadataProperty) => {
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

            var promise = this.http.get(this.RestUrlService.METADATA_PROPERTY_NAMES_URL).toPromise();
            promise.then(_successFn, _errorFn);
            return promise;
        }

    }

    /**
     * Find the input ports associated with the reusable templates to make connections
     * @return {Promise<any>}
     */
    fetchRegisteredReusableFeedInputPorts(): any{

        let successFn = (response: any) => {
        }
        var errorFn = (err: any) => {

        }
        var promise = this.http.get(this.RestUrlService.ALL_REUSABLE_FEED_INPUT_PORTS).toPromise();
        promise.then(successFn, errorFn);
        return promise;
    }

    /**
     * Fetch the input PortDTO objects on the Root process group
     * @return {any}
     */
    fetchRootInputPorts(): Promise<any> {
        return this.http.get(this.RestUrlService.ROOT_INPUT_PORTS).toPromise();
    }

    replaceAll(str: string, find: string, replace: string): string {
        return str.replace(new RegExp(this.escapeRegExp(find), 'g'), replace);
    }


    deriveExpression(expression: string, configOnly: boolean): string {

        var replaced = false;
        if (expression != null && expression != '') {
            var variables = expression.match(/\$\{(.*?)\}/gi);
            if (variables && variables.length) {
                variables.forEach((variable: any) => {
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


    getCodeMirrorTypes(): Promise<any> {

        if (this.codemirrorTypes == null) {
            let successFn = (response: any) => {
                this.codemirrorTypes = response;
                this.codeMirrorTypesObserver.next(this.codemirrorTypes);
                Object.keys(this.codemirrorTypes).forEach( (key: string) => {
                    this.propertyRenderTypes.push({ type: key, label: this.codemirrorTypes[key], codemirror: true });
                });
            }
            var errorFn = (err: angular.IHttpResponse<any>) => {

            }
            var promise = this.http.get(this.RestUrlService.CODE_MIRROR_TYPES_URL).toPromise();
            promise.then(successFn, errorFn);
            return promise;
        }
        return Promise.resolve(this.codemirrorTypes);
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
     * @returns {Promise}
     */
    getTemplates(): Promise<any> {
        let successFn = (response: any) => {

        }
        let errorFn = (err: any) => {

        }

        var promise = this.http.get(this.RestUrlService.GET_TEMPLATES_URL).toPromise();
        promise.then(successFn, errorFn);
        return promise;
    }

    /**
     * Gets the Registered Templates
     * @returns {HttpPromise}
     */
    getRegisteredTemplates(): Promise<any> {
        let successFn = (response: Promise<any>) => {

        }
        let errorFn = (err: Promise<any>) => {

        }

        var promise = this.http.get(this.RestUrlService.GET_REGISTERED_TEMPLATES_URL).toPromise();
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
            if (this.feedDetailsProcessorRenderingHelper.isGetTableDataProcessor(processor) || this.feedDetailsProcessorRenderingHelper.isWatermarkProcessor(processor)) {
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
    initializeProperties(template: any, mode: any, feedProperties ?: Property[]): void {
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

    disableTemplate(templateId: string): Promise<any> {

        var promise = this.http.post(this.RestUrlService.DISABLE_REGISTERED_TEMPLATE_URL(templateId), null).toPromise();
        promise.then((response: any) => {
            this.model.state = response.state
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
    enableTemplate(templateId: any): Promise<any> {

        var promise = this.http.post(this.RestUrlService.ENABLE_REGISTERED_TEMPLATE_URL(templateId), null).toPromise();
        promise.then((response: any) => {
            this.model.state = response.state
            if (this.model.state == 'ENABLED') {
                this.model.stateIcon = 'check_circle'
            }
            else {
                this.model.stateIcon = 'block'
            }
        });
        return promise;

    }

    deleteTemplate(templateId: string): Promise<any> {
        var promise = this.http.delete(this.RestUrlService.DELETE_REGISTERED_TEMPLATE_URL(templateId)).toPromise();
        promise.then((response: any) => {
            // promise.resolve(response);
        }, (response: any) => {
            // promise.reject(response);
        });
        return promise;
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
    getNiFiTemplateFlowInformation(nifiTemplateId: string, reusableTemplateConnections: ReusableTemplateConnectionInfo[]): Promise<any> {
        var promise : Promise<any>;
        if (nifiTemplateId != null) {
            //build the request
            var flowRequest: any = {};
            flowRequest.connectionInfo = reusableTemplateConnections;

            promise = this.http.post(this.RestUrlService.TEMPLATE_FLOW_INFORMATION(nifiTemplateId), flowRequest).toPromise();
            promise.then((response: any) => {
            }, (response: any) => {
            });
        }
        else {
            promise.then(() => { return { data: [] } },()=>{});
        }
        return promise;

    }

    /**
     * Warn if the model has multiple processors with the same name
     */
    warnInvalidProcessorNames(): void {
        if (!this.model.validTemplateProcessorNames) {
            this._dialogService.closeAll();
            this._dialogService.openAlert({
                ariaLabel : "Template processor name warning",
                disableClose : false,
                message : "Warning the template contains multiple processors with the same name.  It is advised you fix this template in NiFi before registering",
                closeButton : "Got it!",
                title : "Template processor name warning"
            });
        }
    }

    accessDeniedDialog(): void {
        this._dialogService.openAlert({
            disableClose : false,
            title : "",
            ariaLabel : "",
            closeButton : "ok",
            message : ""
        });
        // .title(this.$injector.get("$filter")('translate')('views.main.registerService-accessDenied'))
        // .textContent(this.$injector.get("$filter")('translate')('views.main.registerService-accessDenied2'))
        // .ariaLabel(this.$injector.get("$filter")('translate')('views.main.registerService-accessDenied3'))
    }


    /**
     * Check access to the current template returning a promise object resovled to {allowEdit:{true/false},allowAdmin:{true,false},isValid:{true/false}}
     */
    checkTemplateAccess(model?: any): Promise<any> {
        if (model == undefined) {
            model = this.model;
        }
        model.errorMessage = '';

        
        // var deferred = <angular.IDeferred<AccessControl.EntityAccessCheck>>this.$injector.get("$q").defer();
        // var requests = {
        //     entityEditAccess: entityAccessControlled == true ? this.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.TEMPLATE.EDIT_TEMPLATE, model) : true,
        //     entityAdminAccess: entityAccessControlled == true ? this.hasEntityAccess(AccessControlService.ENTITY_ACCESS.TEMPLATE.DELETE_TEMPLATE, model) : true,
        //     functionalAccess: this.accessControlService.getUserAllowedActions()
        // }
        return new Promise((resolve, reject) => { 
            
        this.accessControlService.getUserAllowedActions().then((functionalAccess: any) => {

            let allowEditAccess = this.accessControlService.hasAction(AccessControlService.TEMPLATES_EDIT, functionalAccess.actions);
            let allowAdminAccess = this.accessControlService.hasAction(AccessControlService.TEMPLATES_ADMIN, functionalAccess.actions);

            var entityAccessControlled = model.id != null && this.accessControlService.isEntityAccessControlled();

            let entityEditAccess = entityAccessControlled == true ? this.hasEntityAccess(EntityAccessControlService.ENTITY_ACCESS.TEMPLATE.EDIT_TEMPLATE, model) : true;
            let entityAdminAccess = entityAccessControlled == true ? this.hasEntityAccess(AccessControlService.ENTITY_ACCESS.TEMPLATE.DELETE_TEMPLATE, model) : true;
            

            let allowEdit = entityEditAccess && allowEditAccess
            let allowAdmin = entityEditAccess && entityAdminAccess && allowAdminAccess;
            let allowAccessControl = entityEditAccess && entityAdminAccess && allowEdit;
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
            resolve(result);

        });
        });
    }

    /**
     * Assigns the model properties and render types
     * Returns a promise
     * @returns {*}
     */
    loadTemplateWithProperties(registeredTemplateId: string, nifiTemplateId: string, templateName?: string): Promise<any> {
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
            angular.forEach(properties, (property: Property, i:any) => {
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
                        if(typeof property.renderOptions['selectOptions'] === 'undefined' && property.renderOptions['selectOptions'] !== null && typeof property.renderOptions['selectOptions'] === 'string'){
                            property.selectOptions = JSON.parse(property.renderOptions['selectOptions']);
                        }
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
                this._dialogService.openAlert({
                    message : errorMessage,
                    disableClose : false,
                    closeButton : "Got it!",
                    title : "Error loading the template"
                });
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
            this.modelNifiTemplateIdObserver.next(this.model.nifiTemplateId);
        }
        if (this.model.nifiTemplateId != null) {
            this.model.loading = true;
            this.modelLoadingObserver.next(true);
            let successFn = (response: any) => {
                var templateData = response;
                transformPropertiesToArray(templateData.properties);
                this.model.exportUrl = this.RestUrlService.ADMIN_EXPORT_TEMPLATE_URL + "/" + templateData.id;
                var nifiTemplateId = templateData.nifiTemplateId != null ? templateData.nifiTemplateId : this.model.nifiTemplateId;
                this.model.nifiTemplateId = nifiTemplateId;
                this.modelNifiTemplateIdObserver.next(this.model.nifiTemplateId);
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
                this.modelTemplateTableOptionObserver.next(this.model.templateTableOption);

                this.model.timeBetweenStartingBatchJobs = templateData.timeBetweenStartingBatchJobs
                if (templateData.state == 'ENABLED') {
                    this.model.stateIcon = 'check_circle'
                }
                else {
                    this.model.stateIcon = 'block'
                }
                validate();
                this.model.loading = false;
                this.modelInputObserver.next();
                this.modelLoadingObserver.next(false);
            }
            let errorFn = (err: any) => {
                this.model.loading = false;
                this.modelLoadingObserver.next(false);
            }
            var id = registeredTemplateId != undefined && registeredTemplateId != null ? registeredTemplateId : this.model.nifiTemplateId;
            
            let params = new HttpParams();
            params = params.append('allProperties', "true");
            params = params.append('templateName', templateName);

            var promise = this.http.get(this.RestUrlService.GET_REGISTERED_TEMPLATE_URL(id), {params : params}).toPromise();
            promise.then(successFn, errorFn);
            return promise;
        }
        else {
            return Promise.resolve([]);
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