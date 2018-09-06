import AccessConstants from "../../constants/AccessConstants";
import { Ng2StateDeclaration } from "@uirouter/angular";
import { serviceLevelAssessmentsComponent } from "./service-level-assessments.component";
import { serviceLevelAssessmentComponent } from "./service-level-assessment.component";

export const slaStates: Ng2StateDeclaration[] = [ 
    { 
        name: AccessConstants.UI_STATES.SERVICE_LEVEL_ASSESSMENTS.state, 
        url: "/service-level-assessments", 
        params: {
            filter: null
        },
        views: {
            'content': {
                component: serviceLevelAssessmentsComponent
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Service Level Assessments',
            permissions:AccessConstants.UI_STATES.SERVICE_LEVEL_ASSESSMENTS.permissions
        }
    },
    { 
        name: AccessConstants.UI_STATES.SERVICE_LEVEL_ASSESSMENT.state,
        url: "/service-level-assessment/{assessmentId}", 
        params: {
            assessmentId:null
        },
        views: {
            'content': {
                component: serviceLevelAssessmentComponent
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Service Level Assessment',
            permissions:AccessConstants.UI_STATES.SERVICE_LEVEL_ASSESSMENT.permissions
        }
    }
]; 