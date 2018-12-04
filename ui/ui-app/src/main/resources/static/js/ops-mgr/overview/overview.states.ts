import AccessConstants from "../../constants/AccessConstants";
import { Ng2StateDeclaration } from "@uirouter/angular";
import {OverviewComponent} from "./OverviewComponent";

export const overviewStates: Ng2StateDeclaration[] = [ 
    { 
        name: AccessConstants.UI_STATES.DASHBOARD.state, 
        url: "/dashboard", 
        views: {
            'content': {
                component: OverviewComponent,
            }
        },
        data:{
            breadcrumbRoot:true,
            displayName:'Dashboard',
            permissions:AccessConstants.UI_STATES.DASHBOARD.permissions
        }
    },
]; 
