import AccessConstants from "../../constants/AccessConstants";
import { Ng2StateDeclaration } from "@uirouter/angular";
import { JobsPageController } from "./JobsPageController.component";

export const jobStates: Ng2StateDeclaration[] = [ 
    { 
        name: AccessConstants.UI_STATES.JOBS.state, 
        url: "/jobs", 
        params: {
            filter: null,
            tab:null
        },
        views: {
            'content': {
                component: JobsPageController
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Jobs',
            permissions:AccessConstants.UI_STATES.JOBS.permissions
        }
    },
]; 