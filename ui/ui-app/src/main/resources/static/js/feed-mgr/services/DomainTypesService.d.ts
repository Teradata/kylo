import * as angular from "angular";

/**
 * Defines the domain type (zip, phone, credit card) of a column.
 */
declare interface DomainType {

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
 * * Interacts with the Domain Types REST API.
 */
declare class DomainTypesService {

    /**
     * Deletes the domain type with the specified id.
     *
     * @param {string} id the domain type id
     * @returns {Promise} for when the domain type is deleted
     */
    deleteById(id: string): angular.IPromise<DomainType>;

    /**
     * Detects the appropriate domain type for the specified values.
     *
     * @param {{name: string, sampleValues: (string|string[])}} columnDef - the column definition
     * @param {DomainType[]} domainTypes - the list of domain types
     * @returns {DomainType|null} - the matching domain type or null if none match
     */
    detectDomainType(columnDef: { name: string, sampleValues: string | string[] }, domainTypes: DomainType[]): DomainType | null;

    /**
     * Finds all domain types.
     *
     * @returns {Promise} with the list of domain types
     */
    findAll(): angular.IPromise<DomainType[]>;

    /**
     * Finds the domain type with the specified id.
     *
     * @param {string} id the domain type id
     * @returns {Promise} with the domain type
     */
    findById(id: string): angular.IPromise<DomainType>;

    /**
     * Gets the specified domain type's regular expression for matching sample values.
     *
     * @param {DomainType} domainType the domain type
     * @returns {(RegExp|null)} the regular expression
     */
    getRegExp(domainType: DomainType): RegExp | null;

    /**
     * Indicates if the specified field properties match the specified domain type.
     *
     * @param domainType - the domain type
     * @param field - the field
     * @returns {boolean} true if the field and domain type match, otherwise false
     */
    matchesField(domainType: DomainType, field: any): boolean;

    /**
     * Creates a new domain type.
     *
     * @returns {DomainType} the domain type
     */
    newDomainType(): DomainType;

    /**
     * Saves the specified domain type.
     *
     * @param {DomainType} domainType the domain type to be saved
     * @returns {Promise} with the updated domain type
     */
    save(domainType: DomainType): angular.IPromise<DomainType>;
}
