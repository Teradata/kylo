import {Templates} from "../../../lib/feed-mgr/services/TemplateTypes";
import processors = Templates.Processor; 
import properties = Templates.Property;
import NiFiRemoteProcessGroup = Templates.NiFiRemoteProcessGroup;
import ReusableTemplateConnectionInfo = Templates.ReusableTemplateConnectionInfo;
import TemplateProcessorDatasourceDefinition = Templates.TemplateProcessorDatasourceDefinition;
import { Property } from "estree";



export interface SaveTemplate {

    properties: properties[];
    nonInputProcessors: processors[];
    inputProcessors: processors[];
    remoteProcessGroups: NiFiRemoteProcessGroup[];

    id: string;
    nifiTemplateId: string;
    templateName: string;

    updateDate: Date;
    createDate: Date;

    icon: string;
    iconColor: string;
    description: string;
    state: string;

    defineTable: boolean;
    allowPreconditions: boolean;
    dataTransformation: boolean;
    reusableTemplate: boolean;

    reusableTemplateConnections: ReusableTemplateConnectionInfo[];
    registeredDatasourceDefinitions: TemplateProcessorDatasourceDefinition[];
    
    order: number;
    templateOrder: string[];
    isStream: boolean;
    feedNames: Set<string>;
    feedsCount: number;

    nifiTemplate: any;

    updated: boolean;
    templateTableOption: string;
    timeBetweenStartingBatchJobs: number;
}

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
    roleMembershipsUpdated: boolean,
    templateTableOption:string
}
export class SaveAbleTemplate implements SaveTemplate {
    id: string;

    properties : properties[];
    nonInputProcessors: processors[] = null;
    inputProcessors:processors[] =null;
    remoteProcessGroups: NiFiRemoteProcessGroup[] = null;

    updateDate: Date = null;
    createDate: Date = null;
    
    feedNames: Set<string> = null;
    feedsCount: number = null;
    
    nifiTemplate:any = null;

    updated: boolean = null;
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
    changeComment: string;
    changeComments: any[];
    
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
    templateTableOption:string= '';
    changeComment:string= '';
    changeComments: any[] = [];
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
    changeComment: string;
    changeComments: any[];
}
