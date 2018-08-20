import AccessConstants from "../../constants/AccessConstants";
import { Ng2StateDeclaration } from "@uirouter/angular";
import { RegisteredTemplatesController } from "./RegisteredTemplatesController.component";
import { RegisterTemplateController } from "./template-stepper/RegisterTemplateController.component";
import { RegisterNewTemplateController } from "./new-template/RegisterNewTemplateController.component";
import { ImportTemplateController } from "./import-template/ImportTemplateController.component";
import { RegisterTemplateCompleteController } from "./template-stepper/register-template/register-template-step.component";

export const templateStates: Ng2StateDeclaration[] = [ 
    { 
        name: AccessConstants.UI_STATES.REGISTERED_TEMPLATES.state, 
        url: "/registered-templates", 
        views: { 
            "content": { 
                component: RegisteredTemplatesController 
            } 
        }, 
        data:{
            breadcrumbRoot:true,
            displayName:'Templates',
            permissions:AccessConstants.UI_STATES.REGISTERED_TEMPLATES.permissions
        }
    },
    {
        name: 'register-template',
        url :"/register-template",
        params:{
            nifiTemplateId:null,
            registeredTemplateId:null
        },
        views: {
            "content": {
                component: RegisterTemplateController
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Register Template',
            permissions:AccessConstants.TEMPLATES_EDIT
        }
    },
    {
        name: 'register-template-complete',
        url :"/register-template-complete",
        params: {
            message: '',
            templateModel: null

        },
        views: {
            "content": {
                component: RegisterTemplateCompleteController
            }
        },
        data: {
            breadcrumbRoot: false,
            displayName: 'Register Template',
            permissions:AccessConstants.TEMPLATES_EDIT
        }
    },
    {
        name: AccessConstants.UI_STATES.REGISTER_NEW_TEMPLATE.state,
        url :"/register-new-template",
        views: {
            "content": {
                component: RegisterNewTemplateController
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Register Template',
            permissions:AccessConstants.UI_STATES.REGISTER_NEW_TEMPLATE.permissions
        }
    }
]; 
