import AccessConstants from "../../constants/AccessConstants";
import { Ng2StateDeclaration } from "@uirouter/angular";
import { AlertDetailsComponent } from "./alert-details.component";
import { AlertsComponent } from "./alerts-table.component";

export const alertStates: Ng2StateDeclaration[] = [ 
    { 
        name: AccessConstants.UI_STATES.ALERTS.state, 
        url: "/alerts", 
        params: {
            query: null
        },
        views: {
            'content': {
                component: AlertsComponent,
            }
        },
        data:{
            displayName:'Alerts',
            permissions:AccessConstants.UI_STATES.ALERTS.permissions
        }
    },
    { 
        name: AccessConstants.UI_STATES.ALERT_DETAILS.state, 
        url:"/alert-details/{alertId}",
            views: {
                'content': {
                    component: AlertDetailsComponent,
                }
            },
            params: {
                alertId: null
            },
            data:{
                displayName:'Alert Details',
                permissions:AccessConstants.UI_STATES.ALERT_DETAILS.permissions
            }
    },
]; 