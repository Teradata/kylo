import * as angular from "angular";

import {Common} from "../../common/CommonTypes";

declare namespace Templates {

    export interface ReusableTemplateConnectionInfo {
        reusableTemplateFeedName?:string;
        feedOutputPortName: string;
        reusableTemplateInputPortName: string;
        inputPortDisplayName?: string;
        reusableTemplateProcessGroupName?: string;
    }

    export interface PropertyAndProcessors {
        properties: Property[];
        processors: Processor[];
    }

    export interface Property {
        key:string;
        nameKey?:string;
        idKey?:string;
        selected: boolean;
        value: string;
        processor: Processor;
        processorId:string;
        processorType:string;
        processGroupId:string;
        processGroupName:string;
        renderOptions:Common.Map<any>;
        selectOptions?:any;
        processorName: string;
        processorOrigName?: string;
        firstProperty?: boolean;
        renderType?:string;
        userEditable:boolean;
        templateProperty?:Property;
        templateValue:string;
        renderWithCodeMirror?:boolean;
        customProperty?:boolean;
        inputProperty:boolean;
        mentioId?:string;
        sensitive?:boolean;
        encryptedValue?:string;
        displayName?:string;
        displayValue?:string;
        propertyDescriptor?:any;
        hidden?:false;

    }

    export interface MetadataProperty {
        name?:string;
        key:string;
        value:string;
        description:string;
        dataType:string;
        type:string;
        annotation?:string
    }

    export interface Processor {
        processorId:string;
        id: string;
        name: string;
        topIndex?: number;
        sortIndex?:number;
        /**
         * All the properties for the Processor
         */
        allProperties?:Property[];
        /**
         * Only those properties that are userEditable
         */
        properties?:Property[];

        feedPropertiesUrl?:string;

        type:string;
        groupId:string;
        groupName:string;

    }

    export interface PropertyRenderType {
        label:string;
        type:string;
        codemirror?:boolean;
    }



}