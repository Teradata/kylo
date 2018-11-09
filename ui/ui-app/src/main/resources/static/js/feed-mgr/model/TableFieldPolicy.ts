import * as angular from "angular";
import * as _ from "underscore";
import {DomainType} from "../services/DomainTypesService";
import {KyloObject} from "../../../lib/common/common.model";
import {TableColumnDefinition} from "./TableColumnDefinition";
import {CloneUtil} from "../../common/utils/clone-util";

export class TableFieldPolicy implements KyloObject{

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
     * Should this column be indexed for global search
     * @type {boolean}
     */
    index:boolean = false;

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

    field?:TableColumnDefinition;

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

    copy() :TableFieldPolicy{
        let field = this.field;
        this.field = null;
        let copy = CloneUtil.deepCopy(this);
        copy.field = field;
        return copy;
    }



}
