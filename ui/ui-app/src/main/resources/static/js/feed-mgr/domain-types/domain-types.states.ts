import AccessConstants from "../../constants/AccessConstants";
import { Ng2StateDeclaration } from "@uirouter/angular";
import { DomainTypesComponent } from "./DomainTypesController";
import { DomainTypeDetailsComponent } from "./details/details.component";

export const domainTypeStates: Ng2StateDeclaration[] = [ 
    { 
        name: AccessConstants.UI_STATES.DOMAIN_TYPES.state, 
        url: "/domain-types", 
        params: {},
        views: {
            "content": {
                component : DomainTypesComponent
            }
        },
        data: {
            breadcrumbRoot: true,
            displayName: "Domain Types",
            permissions: AccessConstants.UI_STATES.DOMAIN_TYPES.permissions
        }
    },
    {
        name: AccessConstants.UI_STATES.DOMAIN_TYPE_DETAILS.state,
        url: "/domain-type-details/{domainTypeId}",
        params: {
            domainTypeId: null
        },
        views: {
            "content": {
                component: DomainTypeDetailsComponent
            }
        },
        data: {
            breadcrumbRoot: false,
            displayName: "Domain Type Details",
            permissions: AccessConstants.UI_STATES.DOMAIN_TYPE_DETAILS.permissions
        }
    }
]; 