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
import * as _ from "underscore";
import { Dictionary } from "underscore";
import 'pascalprecht.translate';
import { Templates } from "./TemplateTypes";
import { Common } from "../../common/CommonTypes";
import { AccessControl } from "../../services/AccessControl";
import { RegisteredTemplateService } from "./RegisterTemplateService";


import {moduleName} from "../module-name";;
import Property = Templates.Property;
import PropertyRenderType = Templates.PropertyRenderType;
import PropertyAndProcessors = Templates.PropertyAndProcessors;
import Processor = Templates.Processor;
import MetadataProperty = Templates.MetadataProperty;
import ReusableTemplateConnectionInfo = Templates.ReusableTemplateConnectionInfo;
import AccessControlService from '../../services/AccessControlService';
import { EmptyTemplate, ExtendedTemplate, SaveAbleTemplate } from '../model/template-models';
import { EntityAccessControlService } from '../shared/entity-access-control/EntityAccessControlService';
import {FeedInputProcessorPropertiesTemplateService} from "./FeedInputProcessorPropertiesTemplateService";
import {FeedDetailsProcessorRenderingHelper} from "./FeedDetailsProcessorRenderingHelper";
import {Observable} from "rxjs/Observable";
import "rxjs/add/observable/empty";
import "rxjs/add/observable/of";
import { DefaultFeedPropertyService } from "./DefaultFeedPropertyService";
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Subject } from "rxjs/Subject";

@Injectable()
export class RegisterTemplatePropertyService {

   CONFIGURATION_PROPERTIES_URL :string =  "/proxy/v1/feedmgr/nifi/configuration/properties";

    METADATA_PROPERTY_NAMES_URL :string = "/proxy/v1/feedmgr/metadata-properties";

    CODE_MIRROR_TYPES_URL :string = "/proxy/v1/feedmgr/util/codemirror-types";

    GET_REGISTERED_TEMPLATES_URL :string = "/proxy/v1/feedmgr/templates/registered"

    constructor(private http: HttpClient,private feedInputProcessorPropertiesTemplateService :FeedInputProcessorPropertiesTemplateService,
                private feedDetailsProcessorRenderingHelper:FeedDetailsProcessorRenderingHelper,
                private feedPropertyService: DefaultFeedPropertyService) {
        this.init();

    }

    private escapeRegExp(str: string): string {
        return str.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
    }

    /**
     * Properties that require custom Rendering, separate from the standard Nifi Property (key  value) rendering
     * This is used in conjunction with the method {@code this.isCustomPropertyRendering(key)} to determine how to render the property to the end user
     */
    static customPropertyRendering: string[] = ["metadata.table.targetFormat", "metadata.table.feedFormat"];

    /**
     * Avaliable types that a user can select for property rendering
     */
    propertyRenderTypes: PropertyRenderType[] = [{ type: 'text', 'label': 'Text' }, { type: 'password', 'label': 'Password' }, { type: 'number', 'label': 'Number', codemirror: false },
    { type: 'textarea', 'label': 'Textarea', codemirror: false }, { type: 'select', label: 'Select', codemirror: false },
    { type: 'checkbox-custom', 'label': 'Checkbox', codemirror: false }];

    static trueFalseRenderTypes: PropertyRenderType[] = [{ type: 'checkbox-true-false', 'label': 'Checkbox', codemirror: false },
    { type: 'select', label: 'Select', codemirror: false }]


    static selectRenderType: PropertyRenderType[] = [{ type: 'select', 'label': 'Select', codemirror: false },
    { type: 'radio', label: 'Radio Button', codemirror: false }]

    /**
     * Map of available CodeMirror types
     * @type {null}
     */
    public codemirrorTypes: Common.Map<string> = null;

    public codeMirrorTypesObserver = new Subject<Common.Map<string>>();

    /**
     * Map of all configuration properties
     * values are the same as the configurationProperties list..
     *
     * @type {{}}
     */
    configurationPropertyMap: Common.Map<string> = {};


    /**
     * list of all configuration properties
     *
     *  @TODO CONSIDER REMOVING
     * @type {{}}
     */
    configurationProperties: Common.Map<string> = {}


    /**
     * List of all the ${metadata. properties
     * @type {any[]}
     */
    metadataProperties: MetadataProperty[] = []


    /**
     * List of all ${metadata. and ${config. properties
     *
     * @type {any[]}
     */
    propertyList: MetadataProperty[] = []


    /**
     * Initialize the service for an empty template model
     */
    init() {
        this.getCodeMirrorTypes();
        this.fetchConfigurationProperties();
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
    private   replaceAll(str: string, find: string, replace: string): string {
        return str.replace(new RegExp(this.escapeRegExp(find), 'g'), replace);
    }

    /**
     * Sort the incoming list of properties by processor and then by property
     * @param {Property[]} properties
     * @return {PropertyAndProcessors}
     */
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
     * @param {(response: any) => any} successFn
     * @param {(err: any) => any} errorFn
     */
    fetchConfigurationProperties(successFn?: (response: any) => any, errorFn?: (err: any) => any) {


        if (Object.keys(this.configurationProperties).length == 0) {
            let _successFn = (response: any) => {
                this.configurationProperties = response;
                Object.keys(response).forEach((key: any) => {
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

            var promise = this.http.get(this.CONFIGURATION_PROPERTIES_URL).toPromise();
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
    fetchMetadataProperties(successFn?: (response: any) => any, errorFn?: (err: any) => any) {

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

            var promise = this.http.get(this.METADATA_PROPERTY_NAMES_URL).toPromise();
            promise.then(_successFn, _errorFn);
            return promise;
        }

    }


    /**
     * For a given ${config expression attempt to resolve the properties from the configurationPropertyMap
     *
     * @param {string} expression
     * @param {boolean} configOnly
     * @return {string}
     */
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


    getCodeMirrorTypes(): void {

        if (this.codemirrorTypes == null) {
            let successFn = (response: any) => {
                this.codemirrorTypes = response;
                this.codeMirrorTypesObserver.next(this.codemirrorTypes);
                Object.keys(this.codemirrorTypes).forEach( (label: string) => {
                    this.propertyRenderTypes.push({ type: this.codemirrorTypes[label], label: label, codemirror: true });
                });
            }
            var errorFn = (err: angular.IHttpResponse<any>) => {

            }
            var promise = this.http.get(this.CODE_MIRROR_TYPES_URL).toPromise();
            promise.then(successFn, errorFn);
          //  return promise;
        }
      //  return Observable.of(this.codemirrorTypes).toPromise();
      //  return this.$q.when(this.codemirrorTypes);
    }


    /**
     * Do we render the incoming property with codemirror?
     * @param {Templates.Property} property
     * @return {boolean}
     */
    isRenderPropertyWithCodeMirror(property: Property): boolean {
        return this.codemirrorTypes && this.codemirrorTypes[property.renderType] !== undefined;
    }

    /**
     * Is a property selected
     * @param property
     * @return {boolean | any}
     */
    isSelectedProperty(property: Templates.Property): boolean {
        var selected = (property.selected || (property.value != null && property.value != undefined && (property.value.includes("${metadata") || property.value.includes("${config."))));
        if (selected) {
            property.selected = true;
        }
        return selected;
    }

    /**
     * Feed Processors can setup separate Templates to have special rendering done for a processors properties.
     * @see /js/define-feed/feed-details/get-table-data-properties.
     * @param key
     * @returns {boolean}
     */
    isCustomPropertyRendering(key: any): boolean {

        var custom = _.find(RegisterTemplatePropertyService.customPropertyRendering, (customKey) => {
            return key == customKey;
        });
        return custom !== undefined;
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
    initializeProperties(template: any, mode: any, feedProperties?: Property[]): void {
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
                this.feedPropertyService.initSensitivePropertyForEditing(property);

                this.feedPropertyService.updateDisplayValue(property);

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

}


