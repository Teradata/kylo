import {Templates} from "../services/TemplateTypes";
import processors = Templates.Processor; 
import properties = Templates.Property;
import ReusableTemplateConnectionInfo = Templates.ReusableTemplateConnectionInfo;

export interface Template {
    id: string,
    nifiTemplateId: string,
    templateName: string,
    description: string,
    defineTable: boolean,
    allowPreconditions: boolean,
    dataTransformation: boolean,
    reusableTemplate: boolean,
    needsReusableTemplate: boolean,
    reusableTemplateConnections: ReusableTemplateConnectionInfo[],  //[{reusableTemplateFeedName:'', feedOutputPortName: '', reusableTemplateInputPortName: ''}]
    icon: any,
    isStream: boolean,
    roleMemberships: any[],
    owner: any,
    roleMembershipsUpdated: boolean
}
export class SaveAbleTemplate implements Template {
    id: string;
    properties : properties[];
    nifiTemplateId: string;
    templateName: string;
    description: string;
    defineTable: boolean;
    allowPreconditions: boolean;
    dataTransformation: boolean;
    reusableTemplate: boolean;
    needsReusableTemplate: boolean;
    reusableTemplateConnections: Templates.ReusableTemplateConnectionInfo[];
    icon: string;
    iconColor: string;
    state: string;
    isStream: boolean;
    roleMemberships: any[];
    owner: any;
    roleMembershipsUpdated: boolean;
    templateTableOption : any;
    timeBetweenStartingBatchJobs : any;
    templateOrder : any[];
    order : number;
    registeredDatasourceDefinitions : any;
};
export class EmptyTemplate implements Template {
    id: string = null;
    nifiTemplateId: string = null;
    templateName: string = '';
    description: string = '';
    processors: any[];
    inputProperties: any[];
    additionalProperties: any[];
    inputProcessors: any[];
    nonInputProcessors: any[];
    defineTable: boolean = true;
    allowPreconditions: boolean = false;
    dataTransformation: boolean = false;
    reusableTemplate: boolean = false;
    needsReusableTemplate: boolean = false;
    ports: any[];
    reusableTemplateConnections: ReusableTemplateConnectionInfo[];
    icon: { title: string; color: string; };
    state: string = 'NOT REGISTERED';
    updateDate: any = null;
    feedsCount: number = 0;
    registeredDatasources: any[];
    isStream: boolean = false;
    validTemplateProcessorNames: boolean = true;
    roleMemberships: any[];
    owner: any;
    roleMembershipsUpdated: boolean = false;
}
export class ExtendedTemplate implements Template {
    id: string = null;
    nifiTemplateId: string = null;
    templateName: string ='';
    description: string = '';
    processors: processors[];
    inputProperties: properties[];
    additionalProperties: properties[];
    nonInputProcessors: processors[];
    defineTable: boolean = true;
    allowPreconditions: boolean = false;
    dataTransformation: boolean = false;
    reusableTemplate: boolean = false;
    needsReusableTemplate: boolean = false;
    ports: any[];
    reusableTemplateConnections: ReusableTemplateConnectionInfo[];  //[{reusableTemplateFeedName:'', feedOutputPortName: '', reusableTemplateInputPortName: ''}]
    icon: { title: null, color: null };
    state: string;
    stateIcon: string;
    updateDate: any = null;
    feedsCount: 0;
    registeredDatasources: any[];
    isStream: boolean = false;
    validTemplateProcessorNames: boolean = true;
    roleMemberships: any[];
    owner: any = null;
    roleMembershipsUpdated: boolean = false;
    templateTableOption:any;
    timeBetweenStartingBatchJobs: any;
    inputProcessors :processors[];
    additionalProcessors:processors[];
    loading : boolean = true;
    exportUrl : string = "";
    registeredDatasourceDefinitions : any;
    allowedActions : any;
    valid : boolean = false;
}
