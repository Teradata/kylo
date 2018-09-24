import AccessConstants from "../../constants/AccessConstants";
import { Ng2StateDeclaration } from "@uirouter/angular";
import { SchedulerComponent } from "./SchedulerComponent";

export const schedulerStates: Ng2StateDeclaration[] = [ 
    { 
        name: AccessConstants.UI_STATES.SCHEDULER.state, 
        url: "/scheduler", 
        views: {
            'content': {
                component: SchedulerComponent
            }
        },
        data:{
            breadcrumbRoot:true,
            displayName:'Tasks',
            permissions:AccessConstants.UI_STATES.SCHEDULER.permissions
        }
    },
]; 