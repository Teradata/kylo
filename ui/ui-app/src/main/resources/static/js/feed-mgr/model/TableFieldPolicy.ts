import * as angular from "angular";
import * as _ from "underscore";
import {DomainType} from "../services/DomainTypesService";

export class TableFieldPolicy {

    public static OBJECT_TYPE:string = 'TableFieldPolicy'

    public objectType:string = TableFieldPolicy.OBJECT_TYPE;
    /**
     * the name of the column defintion
     */
    name: string = '';

    feedFieldName?: string;

    fieldName:string = '';

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

    static forName(name:string) :TableFieldPolicy {
        return new this({name:name});
    }



    domainTypeId: string

    constructor(model:Partial<TableFieldPolicy>) {
        Object.assign(this, model);
        if((this.fieldName == undefined || this.fieldName == '') && this.name != undefined && this.name != '') {
            this.fieldName = this.name;
        }
        if((this.name == undefined || this.name == '') && this.fieldName != undefined && this.fieldName != '') {
            this.name = this.fieldName;
        }

    }


}