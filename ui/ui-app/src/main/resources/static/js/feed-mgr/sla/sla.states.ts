import AccessConstants from "../../constants/AccessConstants";
import { Ng2StateDeclaration } from "@uirouter/angular";
import ServiceLevelAgreementInit from "./ServiceLevelAgreementInit.component";
import {SlaEmailTemplates} from "./sla-email-templates/SlaEmailTemplates.component";
import {SlaEmailTemplate} from "./sla-email-templates/SlaEmailTemplate.component";

export const slaStates: Ng2StateDeclaration[] = [ 
    { 
        name: AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENTS.state, 
        url: "/service-level-agreements/:slaId", 
        views: { 
            "content": { 
                component: ServiceLevelAgreementInit
            } 
        }, 
        params: {
            slaId: null
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Service Level Agreements',
            permissions:AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENTS.permissions
        }
    },
    {
        name: AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATES.state,
        url :"/sla-email-templates",
        views: {
            "content": {
                component: SlaEmailTemplates
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'SLA Email Templates',
            permissions:AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATES.permissions
        }
    },
    {
        name: "sla-email-template",
        url :"/sla-email-template/:emailTemplateId",
        params:{
            emailTemplateId:null
        },
        views: {
            "content": {
                component: SlaEmailTemplate
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'SLA Email Template',
            permissions:AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATES.permissions
        }
    }
]; 