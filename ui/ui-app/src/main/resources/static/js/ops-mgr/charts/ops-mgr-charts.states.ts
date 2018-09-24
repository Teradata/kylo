import AccessConstants from "../../constants/AccessConstants";
import { Ng2StateDeclaration } from "@uirouter/angular";
import { ChartsComponent } from "./ChartsComponet";

export const chartStates: Ng2StateDeclaration[] = [ 
    { 
        name: AccessConstants.UI_STATES.CHARTS.state, 
        url: "/charts", 
        views: {
            'content': {
                component: ChartsComponent
            }
        },
        data:{
            breadcrumbRoot:true,
            displayName:'Charts',
            permissions:AccessConstants.UI_STATES.CHARTS.permissions
        }
    },
]; 