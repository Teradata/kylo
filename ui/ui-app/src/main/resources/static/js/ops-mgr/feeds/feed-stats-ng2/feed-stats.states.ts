import AccessConstants from "../../../constants/AccessConstants";
import { Ng2StateDeclaration } from "@uirouter/angular";
import { FeedStatsComponent } from "./feed-stats.component";

export const feedStatsStates: Ng2StateDeclaration[] = [
    {
        name: AccessConstants.UI_STATES.FEED_STATS.state,
        url: "/feed-stats/{feedName}",
        params:{
            feedName:null
        },
        views: {
            'content': {
                component: FeedStatsComponent
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Feed Stats',
            permissions:AccessConstants.UI_STATES.FEED_STATS.permissions
        }
    },
];