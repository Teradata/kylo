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
     * The flags for the regular expression.
     */
    regexFlags: string;

    /**
     * The regular expression for matching sample data.
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
     * @param {(string|string[])} values the values to test
     * @param {DomainType[]} domainTypes the list of domain types
     * @returns {DomainType|null} the matching domain type or null if none match
     */
    detectDomainType(values: string | string[], domainTypes: DomainType[]): DomainType | null;

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
     * Gets the RegExp for the specified domain type.
     *
     * @param {DomainType} domainType the domain type
     * @returns {(RegExp|null)} the regular expression
     */
    getRegExp(domainType: DomainType): RegExp | null;

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
