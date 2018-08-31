import AccessConstants from "../../constants/AccessConstants";
import { Ng2StateDeclaration } from "@uirouter/angular";
import { FeedDetailsComponent } from "./FeedDetailsComponent";

export const feedStates: Ng2StateDeclaration[] = [ 
    { 
        name: AccessConstants.UI_STATES.OPS_FEED_DETAILS.state, 
        url: "/ops-feed-details/{feedName}", 
        params: {
            filter: null,
            tab:null
        },
        views: {
            'content': {
                component: FeedDetailsComponent
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Feed Details',
            permissions:AccessConstants.UI_STATES.OPS_FEED_DETAILS.permissions
        }
    },
]; 