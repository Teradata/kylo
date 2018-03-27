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
    /**
        * Gets the specified RegExp.
        */
    function getRegExp(pattern, flags) {
        var safeFlags = angular.isString(flags) ? flags : "";
        return (angular.isString(pattern) && pattern.length > 0) ? new RegExp(pattern, safeFlags) : null;
    }
    /**
     * Gets the specified domain type's regular expression for matching field names.
     */
    function getFieldNameRegExp(domainType) {
        if (angular.isUndefined(domainType.$fieldNameRegexp)) {
            domainType.$fieldNameRegexp = this.getRegExp(domainType.fieldNamePattern, domainType.fieldNameFlags);
        }
        return domainType.$fieldNameRegexp;
    }
    /**
     * Gets the specified domain type's regular expression for matching sample values.
     */
    function getSampleDataRegExp(domainType) {
        if (angular.isUndefined(domainType.$regexp)) {
            domainType.$regexp = this.getRegExp(domainType.regexPattern, domainType.regexFlags);
        }
        return domainType.$regexp;
    }
    /**
     * Indicates if the specified value exactly matches the regular expression.
     */
    function matches(regexp, value) {
        var result = regexp.exec(value);
        return (result !== null && result.index === 0 && result[0].length === value.length);
    }
    var DomainTypesService = /** @class */ (function () {
        /**
         * Interacts with the Domain Types REST API.
         * @constructor
         */
        function DomainTypesService($http, $q, RestUrlService) {
            // angular.extend(DomainTypesService.prototype, {
            var _this = this;
            this.$http = $http;
            this.$q = $q;
            this.RestUrlService = RestUrlService;
            /**
              * Deletes the domain type with the specified id.
              *
              * @param {string} id the domain type id
              * @returns {Promise} for when the domain type is deleted
              */
            this.deleteById = function (id) {
                return _this.$http({
                    method: "DELETE",
                    url: _this.RestUrlService.DOMAIN_TYPES_BASE_URL + "/" + encodeURIComponent(id)
                });
            };
            /**
             * Detects the appropriate domain type for the specified values.
             *
             * @param {{name: string, sampleValues: (string|string[])}} columnDef - the column definition
             * @param {DomainType[]} domainTypes - the list of domain types
             * @returns {DomainType|null} - the matching domain type or null if none match
             */
            this.detectDomainType = function (columnDef, domainTypes) {
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
                    var fieldNameRegexp = getFieldNameRegExp(domainType);
                    if (fieldNameRegexp !== null) {
                        match = matches(fieldNameRegexp, columnDef.name);
                    }
                    // Match sample values
                    var sampleValuesRegexp = getSampleDataRegExp(domainType);
                    if (sampleValuesRegexp !== null && valueArray.length > 0 && match !== false) {
                        match = valueArray.every(matches.bind(null, sampleValuesRegexp));
                    }
                    return (match === true);
                });
                return angular.isObject(matchingDomainType) ? matchingDomainType : null;
            };
            /**
             * Finds all domain types.
             *
             * @returns {Promise} with the list of domain types
             */
            this.findAll = function () {
                return _this.$http.get(_this.RestUrlService.DOMAIN_TYPES_BASE_URL)
                    .then(function (response) {
                    return response.data;
                });
            };
            /**
             * Finds the domain type with the specified id.
             *
             * @param {string} id the domain type id
             * @returns {Promise} with the domain type
             */
            this.findById = function (id) {
                return _this.$http.get(_this.RestUrlService.DOMAIN_TYPES_BASE_URL + "/" + encodeURIComponent(id))
                    .then(function (response) {
                    return response.data;
                });
            };
            /**
             * Gets the specified domain type's regular expression for matching sample values.
             *
             * @param {DomainType} domainType the domain type
             * @returns {(RegExp|null)} the regular expression
             */
            this.getRegExp = getSampleDataRegExp;
            /**
             * Indicates if the specified field properties match the specified domain type.
             *
             * @param domainType - the domain type
             * @param field - the field
             * @returns {boolean} true if the field and domain type match, otherwise false
             */
            this.matchesField = function (domainType, field) {
                if (domainType.field == null) {
                    return true;
                }
                return (domainType.field.name == null || domainType.field.name === field.name)
                    && (domainType.field.derivedDataType == null || domainType.field.derivedDataType === field.derivedDataType);
            };
            /**
             * Creates a new domain type.
             *
             * @returns {DomainType} the domain type
             */
            this.newDomainType = function () {
                return {
                    description: "",
                    field: {
                        tags: _this.emptyArray
                    },
                    fieldPolicy: {
                        standardization: _this.emptyArray,
                        validation: _this.emptyArray
                    },
                    icon: _this.nullVar,
                    iconColor: _this.nullVar,
                    regexFlags: "",
                    regexPattern: "",
                    title: ""
                };
            };
            /**
             * Saves the specified domain type.
             *
             * @param {DomainType} domainType the domain type to be saved
             * @returns {Promise} with the updated domain type
             */
            this.save = function (domainType) {
                return _this.$http.post(_this.RestUrlService.DOMAIN_TYPES_BASE_URL, domainType)
                    .then(function (response) {
                    return response.data;
                });
            };
            this.emptyArray = [];
            this.nullVar = null;
            //  });
            //return new DomainTypesService();
        }
        return DomainTypesService;
    }());
    exports.default = DomainTypesService;
    angular.module(moduleName).factory("DomainTypesService", ["$http", "$q", "RestUrlService",
        function ($http, $q, RestUrlService) {
            return new DomainTypesService($http, $q, RestUrlService);
        }
    ]);
});
//# sourceMappingURL=DomainTypesService.js.map