define(["require", "exports", "angular"], function (require, exports, angular) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    /**
     * Defines the domain type (zip, phone, credit card) of a column.
     *
     * @typedef {Object} DomainType
     * @property {string} [id] the unique identifier
     * @property {string} description a human-readable description
     * @property {Field} field the field
     * @property {string} fieldNameFlags - regular expression flags for matching field names
     * @property {string} fieldNamePattern - regular expression pattern for matching field names
     * @property {FieldPolicy} fieldPolicy the field policy
     * @property {string} icon the name of the icon
     * @property {string} iconColor the icon color
     * @property {string} regexFlags - regular expression flags for matching sample values
     * @property {string} regexPattern - regular expression pattern for matching sample values
     * @property {string} title a human-readable title
     */
    var moduleName = require('feed-mgr/module-name');
    var DomainTypesService = /** @class */ (function () {
        function DomainTypesService($http, $q, RestUrlService) {
            var _this = this;
            this.$http = $http;
            this.$q = $q;
            this.RestUrlService = RestUrlService;
            /**
               * Gets the specified RegExp.
               */
            this.getRegExp = function (pattern, flags) {
                var safeFlags = angular.isString(flags) ? flags : "";
                return (angular.isString(pattern) && pattern.length > 0) ? new RegExp(pattern, safeFlags) : null;
            };
            /**
             * Gets the specified domain type's regular expression for matching field names.
             */
            this.getFieldNameRegExp = function (domainType) {
                if (angular.isUndefined(domainType.$fieldNameRegexp)) {
                    domainType.$fieldNameRegexp = _this.getRegExp(domainType.fieldNamePattern, domainType.fieldNameFlags);
                }
                return domainType.$fieldNameRegexp;
            };
            /**
             * Gets the specified domain type's regular expression for matching sample values.
             */
            this.getSampleDataRegExp = function (domainType) {
                if (angular.isUndefined(domainType.$regexp)) {
                    domainType.$regexp = _this.getRegExp(domainType.regexPattern, domainType.regexFlags);
                }
                return domainType.$regexp;
            };
            /**
             * Indicates if the specified value exactly matches the regular expression.
             */
            this.matches = function (regexp, value) {
                var result = regexp.exec(value);
                return (result !== null && result.index === 0 && result[0].length === value.length);
            };
            this.emptyArray = [];
            this.nullVar = null;
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
                 * @param {{name: string, sampleValues: (string|string[])}} columnDef - the column definition
                 * @param {DomainType[]} domainTypes - the list of domain types
                 * @returns {DomainType|null} - the matching domain type or null if none match
                 */
                detectDomainType: function (columnDef, domainTypes) {
                    var _this = this;
                    // Remove empty values
                    var valueArray;
                    if (columnDef.sampleValues != null && columnDef.sampleValues.length > 0) {
                        var source = angular.isArray(columnDef.sampleValues) ? columnDef.sampleValues : [columnDef.sampleValues];
                        valueArray = source.filter(function (value) {
                            return angular.isString(value) && value.trim().length > 0;
                        });
                    }
                    else {
                        valueArray = [];
                    }
                    // Find matching domain type
                    var matchingDomainType = domainTypes.find(function (domainType) {
                        var match = null;
                        // Match field name
                        var fieldNameRegexp = _this.getFieldNameRegExp(domainType);
                        if (fieldNameRegexp !== null) {
                            match = _this.matches(fieldNameRegexp, columnDef.name);
                        }
                        // Match sample values
                        var sampleValuesRegexp = _this.getSampleDataRegExp(domainType);
                        if (sampleValuesRegexp !== null && valueArray.length > 0 && match !== false) {
                            match = valueArray.every(_this.matches.bind(null, sampleValuesRegexp));
                        }
                        return (match === true);
                    });
                    return angular.isObject(matchingDomainType) ? matchingDomainType : null;
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
                 * Gets the specified domain type's regular expression for matching sample values.
                 *
                 * @param {DomainType} domainType the domain type
                 * @returns {(RegExp|null)} the regular expression
                 */
                getRegExp: this.getSampleDataRegExp,
                /**
                 * Indicates if the specified field properties match the specified domain type.
                 *
                 * @param domainType - the domain type
                 * @param field - the field
                 * @returns {boolean} true if the field and domain type match, otherwise false
                 */
                matchesField: function (domainType, field) {
                    if (domainType.field == null) {
                        return true;
                    }
                    return (domainType.field.name == null || domainType.field.name === field.name)
                        && (domainType.field.derivedDataType == null || domainType.field.derivedDataType === field.derivedDataType);
                },
                /**
                 * Creates a new domain type.
                 *
                 * @returns {DomainType} the domain type
                 */
                newDomainType: function () {
                    return {
                        description: "",
                        field: {
                            tags: this.emptyArray
                        },
                        fieldPolicy: {
                            standardization: this.emptyArray,
                            validation: this.emptyArray
                        },
                        icon: this.nullVar,
                        iconColor: this.nullVar,
                        regexFlags: "",
                        regexPattern: "",
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
        }
        /**
         * Interacts with the Domain Types REST API.
         * @constructor
         */
        DomainTypesService.prototype.DomainTypesService = function () {
        };
        return DomainTypesService;
    }());
    exports.default = DomainTypesService;
    angular.module(moduleName).factory("DomainTypesService", ["$http", "$q", "RestUrlService", DomainTypesService]);
});
//# sourceMappingURL=DomainTypesService.js.map