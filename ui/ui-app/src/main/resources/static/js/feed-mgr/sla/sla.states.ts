import {SlaComponent} from "./sla.componment";
import {Ng2StateDeclaration, StateService} from "@uirouter/angular";
import {SlaListComponent} from "./list/sla-list.componment";
import {SlaDetailsComponent} from "./details/sla-details.componment";

export const SLA_ROOT_STATE = "sla";

export const slaStates: Ng2StateDeclaration[] = [

    {
        name: SLA_ROOT_STATE,
        url: "/sla",
        redirectTo: SLA_ROOT_STATE+".list",
        views: {
            "content": {
                component: SlaComponent
            }
        },
        data: {
            breadcrumbRoot: true,
            displayName: "",
            permissionsKey:"SERVICE_LEVEL_AGREEMENTS"
        }
    },
    {
        name: SLA_ROOT_STATE+".list",
        url: "/list",
        component: SlaListComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            }
        ],
        data: {
            permissionsKey:"SERVICE_LEVEL_AGREEMENTS"
        }
    },
    {
        name: SLA_ROOT_STATE+".new",
        url: "/new",
        component: SlaDetailsComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            }
        ],
        data: {
            permissionsKey:"EDIT_SERVICE_LEVEL_AGREEMENTS"
        }
    },
    {
        name: SLA_ROOT_STATE+".edit",
        url: "/:slaId",
        component: SlaDetailsComponent,
        resolve: [
            {
                token: 'stateParams',
                deps: [StateService],
                resolveFn: resolveParams
            }
        ],
        data: {
            permissionsKey:"EDIT_SERVICE_LEVEL_AGREEMENTS"
        }
    }
];

export function resolveParams(state: StateService) {
    return state.transition.params();
}