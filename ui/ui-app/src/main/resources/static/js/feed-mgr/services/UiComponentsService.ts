/**
 * @typedef {Object} TemplateTableOption
 * @property {string} description - a human-readable summary of this option
 * @property {string} displayName - a human-readable title of this option
 * @property {(string|null)} feedDetailsTemplateUrl - the template URL containing sections for viewing or editing a feed
 * @property {Array.<{name: string, description: string}>} metadataProperties - the list of metadata properties that can be used in NiFi property expressions
 * @property {(string|null)} stepperTemplateUrl - the template URL containing steps for creating or editing a feed
 * @property {number} totalSteps - the number of additional steps for creating or editing a feed
 * @property {string} type - a unique identifier for this option
 */

import * as angular from 'angular';
import * as _ from "underscore";

const moduleName = require('feed-mgr/module-name');


export class UiComponentsService {

    /**
     * Cache of template table options.
     * @private
     * @type {(Array.<TemplateTableOption>|null)}
     */
    TEMPLATE_TABLE_OPTIONS: any = null;

    /**
     * Cache of the processor templates
     */
    PROCESSOR_TEMPLATES: any = null;

    /**
     * Map of the type, {progress}
     */
    stepperTemplateRenderProgress: any = {};


    /**
     * Flag to indicate if we are already fetching
     */
    initialProcesorTemplatePromise: angular.IPromise<any> = null;


    /**
     * Flag to indicate we are already fetching the table options
     * @type {null}
     */
    initialTemplateTableOptionsPromise: angular.IPromise<any> = null;


    static $inject = ["$http", "$q", "$templateRequest", "RestUrlService"];

    constructor(private $http: angular.IHttpService, private  $q: angular.IQService, private $templateRequest: any, private RestUrlService: any) {

    }

    startStepperTemplateRender(tableOption: any, callback: any) {
        var type = tableOption.type;
        var requests = tableOption.totalSteps == tableOption.totalPreSteps ? 1 : (tableOption.totalPreSteps > 0 ? 2 : 1);
        this.stepperTemplateRenderProgress[type] = {total: requests, complete: 0, callback: callback};
    }

    completeStepperTemplateRender(type: any) {
        var complete = true;
        if (angular.isDefined(this.stepperTemplateRenderProgress[type])) {
            var progress = this.stepperTemplateRenderProgress[type];
            progress.complete += 1;
            complete = progress.complete == progress.total;
            if (complete && angular.isDefined(progress.callback) && angular.isFunction(progress.callback)) {
                progress.callback(type);
            }
        }

        return complete;
    }

    /**
     * Gets the template table option with the specified type.
     * @param {string} type - the unique identifier for the table option
     * @returns {Promise} resolves to the TemplateTableOption
     */
    getTemplateTableOption(type: any) {
        return this.getTemplateTableOptions()
            .then((tableOptions: any) => {
                var selected = _.find(tableOptions, (tableOption: any) => {
                    return tableOption.type === type;
                });

                var result = this.$q.defer();
                if (angular.isDefined(selected)) {
                    result.resolve(selected);
                } else {
                    result.reject();
                }
                return result.promise;
            });
    }

    getTableOptionAndCacheTemplates(type: any) {
        var defer = this.$q.defer();
        // Loads the table option template
        this.getTemplateTableOption(type)
            .then((tableOption: any) => {

                var requests = {};
                if (angular.isDefined(tableOption.stepperTemplateUrl) && tableOption.stepperTemplateUrl) {
                    requests['stepperTemplateUrl'] = this.$templateRequest(tableOption.stepperTemplateUrl);
                }
                if (angular.isDefined(tableOption.preStepperTemplateUrl) && tableOption.preStepperTemplateUrl != null) {
                    requests['preStepperTemplateUrl'] = this.$templateRequest(tableOption.preStepperTemplateUrl);
                }

                this.$q.when(requests).then((response: any) => {
                    defer.resolve(tableOption);
                });
            })

        return defer.promise;
    }

    /**
     * Gets the metadata properties for the specified table option.
     *
     * @param {string} type - the unique identifier for the table option
     * @returns {Promise} resolves to the list of metadata properties
     */
    getTemplateTableOptionMetadataProperties(type: any) {
        return this.getTemplateTableOption(type)
            .then((tableOption: any) => {
                return tableOption.metadataProperties.map((property: any) => {
                    return {
                        key: "metadata.tableOption." + property.name,
                        value: "",
                        dataType: property.dataType,
                        description: property.description,
                        type: "metadata"
                    };
                })
            });
    }

    /**
     * Gets the list of template table option plugins.
     * @returns {Promise} resolves to the list of TemplateTableOption objects
     */
    getTemplateTableOptions() {
        var result: angular.IDeferred<any> = null;
        if (this.TEMPLATE_TABLE_OPTIONS === null) {
            if (this.initialTemplateTableOptionsPromise == null) {
                result = this.$q.defer();
                this.initialTemplateTableOptionsPromise = result.promise;
                this.$http.get(this.RestUrlService.UI_TEMPLATE_TABLE_OPTIONS)
                    .then((response: angular.IHttpResponse<any>) => {
                        this.TEMPLATE_TABLE_OPTIONS = response.data;
                        result.resolve(this.TEMPLATE_TABLE_OPTIONS);
                    });
            }
            return this.initialTemplateTableOptionsPromise;

        } else {
            result = this.$q.defer();
            result.resolve(this.TEMPLATE_TABLE_OPTIONS);
            return result.promise;
        }
    }

    /**
     * Gets the list of template table option plugins.
     * @returns {Promise} resolves to the list of TemplateTableOption objects
     */
    getProcessorTemplates(): angular.IPromise<any> {
        var result: angular.IDeferred<any> = null;
        if (this.PROCESSOR_TEMPLATES === null) {
            if (this.initialProcesorTemplatePromise == null) {
                result = this.$q.defer();
                this.initialProcesorTemplatePromise = result.promise;
                this.$http.get(this.RestUrlService.UI_PROCESSOR_TEMPLATES)
                    .then((response: angular.IHttpResponse<any>) => {
                        this.PROCESSOR_TEMPLATES = response.data;
                        result.resolve(this.PROCESSOR_TEMPLATES);
                    });
            }
            return this.initialProcesorTemplatePromise;

        } else {
            result = this.$q.defer();
            result.resolve(this.PROCESSOR_TEMPLATES);
            return result.promise;
        }

    }

}

angular.module(moduleName).service("UiComponentsService", UiComponentsService);