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
import {moduleName} from "../module-name";;
import * as angular from 'angular';
import * as _ from "underscore";

/**
 * Defines the domain type (zip, phone, credit card) of a column.
 */
export interface DomainType {

    /**
     * The unique identifier.
     */
    id?: string;

    /**
     * A human-readable description.
     */
    description: string;

    /**
     * The field.
     */
    field: any;

    /**
     * Regular expression flags for matching field names.
     */
    fieldNameFlags: string;

    /**
     * Regular expression pattern for matching field names.
     */
    fieldNamePattern: string;

    /**
     * The field policy.
     */
    fieldPolicy: any;

    /**
     * The name of the icon.
     */
    icon: string;

    /**
     * The icon color.
     */
    iconColor: string;

    /**
     * Regular expression flags for matching sample values.
     */
    regexFlags: string;

    /**
     * Regular expression pattern for matching sample values.
     */
    regexPattern: string;

    /**
     * A human-readable title.
     */
    title: string;
}

     /**
         * Gets the specified RegExp.
         */
    function getRegExp(pattern: any, flags: any) {
            var safeFlags = angular.isString(flags) ? flags : "";
            return (angular.isString(pattern) && pattern.length > 0) ? new RegExp(pattern, safeFlags) : null;
        }

        /**
         * Gets the specified domain type's regular expression for matching field names.
         */
    function getFieldNameRegExp(domainType: any){
            if (angular.isUndefined(domainType.$fieldNameRegexp)) {
                domainType.$fieldNameRegexp = getRegExp(domainType.fieldNamePattern, domainType.fieldNameFlags);
            }
            return domainType.$fieldNameRegexp;
        }

        /**
         * Gets the specified domain type's regular expression for matching sample values.
         */
    function getSampleDataRegExp(domainType: any){
            if (angular.isUndefined(domainType.$regexp)) {
                domainType.$regexp = getRegExp(domainType.regexPattern, domainType.regexFlags);
            }
            return domainType.$regexp;
        }

        /**
         * Indicates if the specified value exactly matches the regular expression.
         */
     function matches(regexp: any, value: any){
            var result = regexp.exec(value);
            return (result !== null && result.index === 0 && result[0].length === value.length);
        }

export class DomainTypesService{
        /**
         * Interacts with the Domain Types REST API.
         * @constructor
         */
 constructor (private $http?: any,
                private $q?: any,
                private RestUrlService?: any) {

      // angular.extend(DomainTypesService.prototype, {

      //  });
        //return new DomainTypesService();
    }
           /**
             * Deletes the domain type with the specified id.
             *
             * @param {string} id the domain type id
             * @returns {Promise} for when the domain type is deleted
             */
            deleteById=(id: any)=>{
                return this.$http({
                    method: "DELETE",
                    url: this.RestUrlService.DOMAIN_TYPES_BASE_URL + "/" + encodeURIComponent(id)
                });
            }

            /**
             * Detects the appropriate domain type for the specified values.
             *
             * @param {{name: string, sampleValues: (string|string[])}} columnDef - the column definition
             * @param {DomainType[]} domainTypes - the list of domain types
             * @returns {DomainType|null} - the matching domain type or null if none match
             */
            detectDomainType=(columnDef: any, domainTypes: any)=>{
                // Remove empty values
                var valueArray: any;
                if (columnDef.sampleValues != null && columnDef.sampleValues.length > 0) {
                    var source = angular.isArray(columnDef.sampleValues) ? columnDef.sampleValues : [columnDef.sampleValues];
                    valueArray = source.filter((value: any)=> {
                        return value != null && value.toString().trim().length > 0;
                    });
                    valueArray = valueArray.map((value: any) => {
                         return value.toString();
                    });

                } else {
                    valueArray = [];
                }

                // Will be true if matches 80% of the values if we have a large enough sample size
                var fuzzyValueMatch = (valueArray.length < 10 ? 1 : .8);

                // Find matching domain type
                var matchingDomainType = domainTypes.find((domainType: any)=> {
                    var match = null;

                    // Match field name
                    var fieldNameRegexp = getFieldNameRegExp(domainType);
                    if (fieldNameRegexp !== null) {
                        match = matches(fieldNameRegexp, columnDef.name);
                    }

                    // Match sample values
                    var sampleValuesRegexp = getSampleDataRegExp(domainType);
                    if (sampleValuesRegexp !== null && valueArray.length > 0 && match !== false) {
                        var found = valueArray.filter(matches.bind(null, sampleValuesRegexp));
                        match = (found && (found.length >= (valueArray.length * fuzzyValueMatch)));
                    }

                    return (match === true);
                });
                return angular.isObject(matchingDomainType) ? matchingDomainType : null;
            }

            /**
             * Finds all domain types.
             *
             * @returns {Promise} with the list of domain types
             */
            findAll=()=> {
                return this.$http.get(this.RestUrlService.DOMAIN_TYPES_BASE_URL)
                    .then((response: any)=> {
                        return response.data;
                    });
            }

            /**
             * Finds the domain type with the specified id.
             *
             * @param {string} id the domain type id
             * @returns {Promise} with the domain type
             */
            findById=(id: any)=>{
                return this.$http.get(this.RestUrlService.DOMAIN_TYPES_BASE_URL + "/" + encodeURIComponent(id))
                    .then((response: any)=> {
                        return response.data;
                    });
            }

            /**
             * Gets the specified domain type's regular expression for matching sample values.
             *
             * @param {DomainType} domainType the domain type
             * @returns {(RegExp|null)} the regular expression
             */
            getRegExp: any= getSampleDataRegExp;

            /**
             * Indicates if the specified field properties match the specified domain type.
             *
             * @param domainType - the domain type
             * @param field - the field
             * @returns {boolean} true if the field and domain type match, otherwise false
             */
            matchesField=(domainType: any, field: any)=>{
                if (domainType.field == null) {
                    return true;
                }
                return (domainType.field.name == null || domainType.field.name === field.name)
                       && (domainType.field.derivedDataType == null || domainType.field.derivedDataType === field.derivedDataType);
            }

            /**
             * Creates a new domain type.
             *
             * @returns {DomainType} the domain type
             */
            newDomainType= ()=>{
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
            }
            /**
             * Saves the specified domain type.
             *
             * @param {DomainType} domainType the domain type to be saved
             * @returns {Promise} with the updated domain type
             */
            save=(domainType: any)=>{
                return this.$http.post(this.RestUrlService.DOMAIN_TYPES_BASE_URL, domainType)
                    .then((response: any)=>{
                        return response.data;
                    });
            }
    emptyArray: any[]= [];
    nullVar: any = null;
}





