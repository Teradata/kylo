/**
 * Defines the domain type (zip, phone, credit card) of a column.
 *
 * @typedef {Object} DomainType
 * @property {string} [id] the unique identifier
 * @property {string} description a human-readable description
 * @property {FieldPolicy} fieldPolicy the field policy
 * @property {string} icon the name of the icon
 * @property {string} iconColor the icon color
 * @property {string} regex the regular expression for matching sample data
 * @property {string} title a human-readable title
 */

define(["angular", "feed-mgr/module-name"], function (angular, moduleName) {
    angular.module(moduleName).factory("DomainTypesService", ["$http", "$q", "RestUrlService", function ($http, $q, RestUrlService) {

        /**
         * Interacts with the Domain Types REST API.
         * @constructor
         */
        function DomainTypesService() {
        }

        angular.extend(DomainTypesService.prototype, {
            /**
             * Deletes the domain type with the specified id.
             *
             * @param {string} id the domain type id
             * @returns {Promise} for when the domain type is deleted
             */
            deleteById: function (id) {
                return $http({
                    method: "DELETE",
                    url: RestUrlService.DOMAIN_TYPES_BASE_URL + "/" + encodeURIComponent(id)
                });
            },

            /**
             * Detects the appropriate domain type for the specified values.
             *
             * @param {(string|string[])} values the values to test
             * @param {DomainType[]} domainTypes the list of domain types
             * @returns {DomainType|null} the matching domain type or null if none match
             */
            detectDomainType: function (values, domainTypes) {
                var valueArray = angular.isArray(values) ? values : [values];
                var matchingDomainType = _.find(domainTypes, function (domainType) {
                    if (angular.isUndefined(domainType.$regexp)) {
                        domainType.$regexp = (angular.isString(domainType.regex) && domainType.regex.length > 0) ? new RegExp(domainType.regex) : null;
                    }
                    if (domainType.$regexp === null) {
                        return false;
                    }
                    return valueArray.every(function (value) {
                        var result = domainType.$regexp.exec(value);
                        return (result !== null && result.index === 0 && result[0].length === value.length);
                    });
                });
                return angular.isDefined(matchingDomainType) ? matchingDomainType : null;
            },

            /**
             * Finds all domain types.
             *
             * @returns {Promise} with the list of domain types
             */
            findAll: function () {
                return $http.get(RestUrlService.DOMAIN_TYPES_BASE_URL)
                    .then(function (response) {
                        return response.data;
                    });
            },

            /**
             * Finds the domain type with the specified id.
             *
             * @param {string} id the domain type id
             * @returns {Promise} with the domain type
             */
            findById: function (id) {
                return $http.get(RestUrlService.DOMAIN_TYPES_BASE_URL + "/" + encodeURIComponent(id))
                    .then(function (response) {
                        return response.data;
                    });
            },

            /**
             * Creates a new domain type.
             *
             * @returns {DomainType} the domain type
             */
            newDomainType: function () {
                return {
                    description: "",
                    fieldPolicy: {
                        standardization: [],
                        validation: []
                    },
                    icon: null,
                    iconColor: null,
                    regex: null,
                    title: ""
                };
            },

            /**
             * Saves the specified domain type.
             *
             * @param {DomainType} domainType the domain type to be saved
             * @returns {Promise} with the updated domain type
             */
            save: function (domainType) {
                return $http.post(RestUrlService.DOMAIN_TYPES_BASE_URL, domainType)
                    .then(function (response) {
                        return response.data;
                    });
            }
        });

        return new DomainTypesService();
    }]);
});
