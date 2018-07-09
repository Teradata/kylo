import AccessConstants from "../../constants/AccessConstants";
import { Ng2StateDeclaration } from "@uirouter/angular";
import ServiceLevelAgreementInitComponent from "./ServiceLevelAgreementInitController.component";
import {SlaEmailTemplatesController} from "./sla-email-templates/SlaEmailTemplatesController.component";
import {SlaEmailTemplateController} from "./sla-email-templates/SlaEmailTemplateController.component";

export const slaStates: Ng2StateDeclaration[] = [ 
    { 
        name: AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENTS.state, 
        url: "/service-level-agreements/:slaId", 
        views: { 
            "content": { 
                component: ServiceLevelAgreementInitComponent 
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
                component: SlaEmailTemplatesController
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
                component: SlaEmailTemplateController
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'SLA Email Template',
            permissions:AccessConstants.UI_STATES.SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATES.permissions
        }
    }
]; 