import {Common} from '../../../lib/common/CommonTypes';


export interface FieldPolicySelectableValue{
    label:string;
    value:string;
    hint:string;
    properties:any;
}

export interface FieldPolicyProperty{
    name:string;
    displayName:string;
    value:string;
    values:any;
    placeholder:string;
    type:string;
    hint:string;
    objectProperty:string;
    selectableValues?:FieldPolicySelectableValue[];
    required:boolean;
    group:string;
    groupOrder:number;
    layout:string;
    hidden:boolean;
    pattern:string;
    patternInvalidMessage:string;
    additionalProperties?:Common.LabelValue[]
    formKey?:string;
    patternRegExp?:string;

}

export interface FieldPolicy {
    /**
     * system name
     */
    name: string;
    /**
     * display name on the ui
     */
    displayName: string;
    /**
     *
     */
    description: string;
    /**
     *
     */
    shortDescription: string;
    /**
     * arbitrary props
     */
    properties: FieldPolicyProperty[];

    objectClassType: string;

    objectShortClassType: string;

    propertyValuesDisplayString: string;

    sequence: number;
}

export enum SchemaParserType {
    CSV="CSV",JSON="JSON",TEXT="TEXT"
}


export interface SchemaParser extends FieldPolicy{

    supportsBinary:boolean;

    generatesHiveSerde:boolean;

    tags:string[];

    clientHelper:string;
    allowSkipHeader:boolean;
    usesSpark:string;
    primary:boolean;
    mimeTypes: string[];
    sparkFormat:string;

}
