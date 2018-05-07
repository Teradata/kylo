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
define(["require", "exports", "angular", "underscore"], function (require, exports, angular, _) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    var moduleName = require('feed-mgr/module-name');
    var UiComponentsService = /** @class */ (function () {
        function UiComponentsService($http, $q, $templateRequest, RestUrlService) {
            this.$http = $http;
            this.$q = $q;
            this.$templateRequest = $templateRequest;
            this.RestUrlService = RestUrlService;
            /**
             * Cache of template table options.
             * @private
             * @type {(Array.<TemplateTableOption>|null)}
             */
            this.TEMPLATE_TABLE_OPTIONS = null;
            /**
             * Cache of the processor templates
             */
            this.PROCESSOR_TEMPLATES = null;
            /**
             * Map of the type, {progress}
             */
            this.stepperTemplateRenderProgress = {};
            /**
             * Flag to indicate if we are already fetching
             */
            this.initialProcesorTemplatePromise = null;
            /**
             * Flag to indicate we are already fetching the table options
             * @type {null}
             */
            this.initialTemplateTableOptionsPromise = null;
        }
        UiComponentsService.prototype.startStepperTemplateRender = function (tableOption, callback) {
            var type = tableOption.type;
            var requests = tableOption.totalSteps == tableOption.totalPreSteps ? 1 : (tableOption.totalPreSteps > 0 ? 2 : 1);
            this.stepperTemplateRenderProgress[type] = { total: requests, complete: 0, callback: callback };
        };
        UiComponentsService.prototype.completeStepperTemplateRender = function (type) {
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
        };
        /**
         * Gets the template table option with the specified type.
         * @param {string} type - the unique identifier for the table option
         * @returns {Promise} resolves to the TemplateTableOption
         */
        UiComponentsService.prototype.getTemplateTableOption = function (type) {
            var _this = this;
            return this.getTemplateTableOptions()
                .then(function (tableOptions) {
                var selected = _.find(tableOptions, function (tableOption) {
                    return tableOption.type === type;
                });
                var result = _this.$q.defer();
                if (angular.isDefined(selected)) {
                    result.resolve(selected);
                }
                else {
                    result.reject();
                }
                return result.promise;
            });
        };
        UiComponentsService.prototype.getTableOptionAndCacheTemplates = function (type) {
            var _this = this;
            var defer = this.$q.defer();
            // Loads the table option template
            this.getTemplateTableOption(type)
                .then(function (tableOption) {
                var requests = {};
                if (angular.isDefined(tableOption.stepperTemplateUrl) && tableOption.stepperTemplateUrl) {
                    requests['stepperTemplateUrl'] = _this.$templateRequest(tableOption.stepperTemplateUrl);
                }
                if (angular.isDefined(tableOption.preStepperTemplateUrl) && tableOption.preStepperTemplateUrl != null) {
                    requests['preStepperTemplateUrl'] = _this.$templateRequest(tableOption.preStepperTemplateUrl);
                }
                _this.$q.when(requests).then(function (response) {
                    defer.resolve(tableOption);
                });
            });
            return defer.promise;
        };
        /**
         * Gets the metadata properties for the specified table option.
         *
         * @param {string} type - the unique identifier for the table option
         * @returns {Promise} resolves to the list of metadata properties
         */
        UiComponentsService.prototype.getTemplateTableOptionMetadataProperties = function (type) {
            return this.getTemplateTableOption(type)
                .then(function (tableOption) {
                return tableOption.metadataProperties.map(function (property) {
                    return {
                        key: "metadata.tableOption." + property.name,
                        value: "",
                        dataType: property.dataType,
                        description: property.description,
                        type: "metadata"
                    };
                });
            });
        };
        /**
         * Gets the list of template table option plugins.
         * @returns {Promise} resolves to the list of TemplateTableOption objects
         */
        UiComponentsService.prototype.getTemplateTableOptions = function () {
            var _this = this;
            var result = null;
            if (this.TEMPLATE_TABLE_OPTIONS === null) {
                if (this.initialTemplateTableOptionsPromise == null) {
                    result = this.$q.defer();
                    this.initialTemplateTableOptionsPromise = result.promise;
                    this.$http.get(this.RestUrlService.UI_TEMPLATE_TABLE_OPTIONS)
                        .then(function (response) {
                        _this.TEMPLATE_TABLE_OPTIONS = response.data;
                        result.resolve(_this.TEMPLATE_TABLE_OPTIONS);
                    });
                }
                return this.initialTemplateTableOptionsPromise;
            }
            else {
                result = this.$q.defer();
                result.resolve(this.TEMPLATE_TABLE_OPTIONS);
                return result.promise;
            }
        };
        /**
         * Gets the list of template table option plugins.
         * @returns {Promise} resolves to the list of TemplateTableOption objects
         */
        UiComponentsService.prototype.getProcessorTemplates = function () {
            var _this = this;
            var result = null;
            if (this.PROCESSOR_TEMPLATES === null) {
                if (this.initialProcesorTemplatePromise == null) {
                    result = this.$q.defer();
                    this.initialProcesorTemplatePromise = result.promise;
                    this.$http.get(this.RestUrlService.UI_PROCESSOR_TEMPLATES)
                        .then(function (response) {
                        _this.PROCESSOR_TEMPLATES = response.data;
                        result.resolve(_this.PROCESSOR_TEMPLATES);
                    });
                }
                return this.initialProcesorTemplatePromise;
            }
            else {
                result = this.$q.defer();
                result.resolve(this.PROCESSOR_TEMPLATES);
                return result.promise;
            }
        };
        UiComponentsService.$inject = ["$http", "$q", "$templateRequest", "RestUrlService"];
        return UiComponentsService;
    }());
    exports.UiComponentsService = UiComponentsService;
    angular.module(moduleName).service("UiComponentsService", UiComponentsService);
});
//# sourceMappingURL=UiComponentsService.js.map