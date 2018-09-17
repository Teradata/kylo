import { Ng2StateDeclaration } from "@uirouter/angular";
import { SearchComponent } from "./common/SearchComponent";
import AccessConstants from "../constants/AccessConstants";

export const searchStates: Ng2StateDeclaration[] = [ 
    { 
        name: AccessConstants.UI_STATES.SEARCH.state, 
        url: "/search", 
        params: {
            bcExclude_globalSearchResetPaging: null
        },
        views: {
            'content': {
                component: SearchComponent,
            }
        },
        data:{
            breadcrumbRoot:false,
            displayName:'Search',
            permissions:AccessConstants.UI_STATES.SEARCH.permissions
        }
    }
]; 