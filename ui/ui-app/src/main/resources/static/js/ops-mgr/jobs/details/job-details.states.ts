import AccessConstants from "../../../constants/AccessConstants";
import { Ng2StateDeclaration } from "@uirouter/angular";
import { JobDetailsController } from "./JobDetailsController.component";

export const jobDetailsStates: Ng2StateDeclaration[] = [ 
    { 
        name: AccessConstants.UI_STATES.JOB_DETAILS.state, 
        url: "/job-details/{executionId}", 
        params: {
            executionId:null
        },
        views: {
            'content': {
                component: JobDetailsController
            }
        },
        data:{
            displayName:'Job Details',
            permissions:AccessConstants.UI_STATES.JOB_DETAILS.permissions
        }
    },
]; 