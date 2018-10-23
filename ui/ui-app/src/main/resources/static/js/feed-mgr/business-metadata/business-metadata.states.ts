import AccessConstants from "../../constants/AccessConstants";
import { Ng2StateDeclaration } from "@uirouter/angular";
import { BusinessMetadataComponent } from "./BusinessMetadataComponent";

export const businessMetadataStates: Ng2StateDeclaration[] = [ 
    { 
        name: AccessConstants.UI_STATES.BUSINESS_METADATA.state, 
        url: "/business-metadata", 
        views: { 
            "content": { 
                component: BusinessMetadataComponent 
            } 
        }, 
        data: {
            breadcrumbRoot: false,
            displayName: 'Business Metadata',
            permissions:AccessConstants.UI_STATES.BUSINESS_METADATA.permissions
        }
    }
]; 