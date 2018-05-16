import * as angular from "angular";
import * as _ from "underscore";
import {DomainType} from "../services/DomainTypesService";

export class TableFieldPolicy {
    /**
     * the name of the column defintion
     */
    name: string = '';

    /**
     * is this a partition field or not
     */
    partition: boolean;

    /**
     * Should this column be profiled
     * @type {boolean}
     */
    profile: boolean = true;

    /**
     * Standardization rules
     * @type {null}
     */
    standardization: any[] = null;

    /**
     * validation rules
     * @type {null}
     */
    validation: any[] = null;


    $currentDomainType: DomainType;



    domainTypeId: string

    constructor(name: string) {
        this.name = name;
    }


}