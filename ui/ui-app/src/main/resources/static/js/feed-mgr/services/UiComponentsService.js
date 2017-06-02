/**
 * @typedef {Object} TemplateTableOption
 * @property {string} description - gets a human-readable summary of this option
 * @property {string} displayName - gets a human-readable title of this option
 * @property {string} feedDetailsTemplateUrl - gets the template URL containing sections for viewing or editing a feed
 * @property {string} stepperTemplateUrl - gets the template URL containing steps for creating or editing a feed
 * @property {number} totalSteps - gets the number of additional steps for creating or editing a feed
 * @property {string} type - gets a unique identifier for this option
 */

define(["angular", "feed-mgr/module-name"], function (angular, moduleName) {
    angular.module(moduleName).service("UiComponentsService", ["$http", "$q", "RestUrlService", function ($http, $q, RestUrlService) {

        /**
         * Manages the pluggable UI components.
         */
        var UiComponentsService = {
            /**
             * Cache of template table options.
             * @private
             * @type {(Array.<TemplateTableOption>|null)}
             */
            TEMPLATE_TABLE_OPTIONS: null,

            /**
             * Gets the template table option with the specified type.
             * @param {string} type - the unique identifier for the table option
             * @returns {Promise} resolves to the TemplateTableOption
             */
            getTemplateTableOption: function (type) {
                return UiComponentsService.getTemplateTableOptions()
                    .then(function (tableOptions) {
                        var selected = _.find(tableOptions, function (tableOption) {
                            return tableOption.type === type;
                        });

                        var result = $q.defer();
                        if (angular.isDefined(selected)) {
                            result.resolve(selected);
                        } else {
                            result.reject();
                        }
                        return result.promise;
                    });
            },

            /**
             * Gets the list of template table option plugins.
             * @returns {Promise} resolves to the list of TemplateTableOption objects
             */
            getTemplateTableOptions: function () {
                if (UiComponentsService.TEMPLATE_TABLE_OPTIONS === null) {
                    return $http.get(RestUrlService.UI_TEMPLATE_TABLE_OPTIONS)
                        .then(function (response) {
                            UiComponentsService.TEMPLATE_TABLE_OPTIONS = response.data;
                            return UiComponentsService.TEMPLATE_TABLE_OPTIONS;
                        });
                } else {
                    var result = $q.defer();
                    result.resolve(UiComponentsService.TEMPLATE_TABLE_OPTIONS);
                    return result.promise;
                }
            }
        };

        return UiComponentsService;
    }]);
});
