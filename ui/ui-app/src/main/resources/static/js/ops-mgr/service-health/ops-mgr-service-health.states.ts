import AccessConstants from "../../constants/AccessConstants";
import { Ng2StateDeclaration } from "@uirouter/angular";
import { ServiceHealthComponent } from "./ServiceHealthComponent";
import { ServiceHealthDetailsComponent } from "./ServiceHealthDetailsComponent";
import { ServiceComponentHealthDetailsComponent } from "./ServiceComponentHealthDetailsComponent";

export const serviceHealthStates: Ng2StateDeclaration[] = [ 
    { 
        name: AccessConstants.UI_STATES.SERVICE_HEALTH.state, 
        url: '/service-health', 
        views: {
            'content': {
                component: ServiceHealthComponent
            }
        },
        data:{
            breadcrumbRoot:true,
            displayName:'Service Health',
            permissions:AccessConstants.UI_STATES.SERVICE_HEALTH.permissions
        }
    },
    { 
        name: AccessConstants.UI_STATES.SERVICE_DETAILS.state, 
        url:'/service-details/:serviceName', 
        params: {
            serviceName: null
        },
        views: {
            'content': {
                component: ServiceHealthDetailsComponent
            }
        },
        data:{
            displayName:'Service Details',
            permissions:AccessConstants.UI_STATES.SERVICE_DETAILS.permissions
        }
    },
    { 
        name: AccessConstants.UI_STATES.SERVICE_COMPONENT_DETAILS.state,
        url:'/service-details/{serviceName}/{componentName}', 
        params: {
            serviceName: null
        },
        views: {
            'content': {
                component: ServiceComponentHealthDetailsComponent
            }
        },
        data:{
            displayName:'Service Component',
            permissions:AccessConstants.UI_STATES.SERVICE_COMPONENT_DETAILS.permissions
        }
    },
]; 