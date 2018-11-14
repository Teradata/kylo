import {Ng2StateDeclaration, StateService} from "@uirouter/angular";
import {SlaEmailTemplateComponent} from "./sla-email-template.component";
import {SlaEmailTemplatesComponent} from "./sla-email-templates.component";
import {SlaEmailTemplateContainerComponment} from "./sla-email-template-container.componment";

export const SLA_EMAIL_TEMPLATE_ROOT_STATE = "sla-email-template";

export const slaEmailTemplateStates: Ng2StateDeclaration[] = [


    {
        name: SLA_EMAIL_TEMPLATE_ROOT_STATE,
        url: "/sla-email-template",
        redirectTo: SLA_EMAIL_TEMPLATE_ROOT_STATE+".list",
        views: {
            "content": {
                component: SlaEmailTemplateContainerComponment
            }
        },
        data: {
            breadcrumbRoot: true,
            displayName: "",
            permissionsKey:"SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATES"
        }
    },
    {
        name: SLA_EMAIL_TEMPLATE_ROOT_STATE+".list",
        url: "/list",
        component: SlaEmailTemplatesComponent,
        data: {
            permissionsKey:"SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATES"
        }
    },
    {
        name: SLA_EMAIL_TEMPLATE_ROOT_STATE+".edit",
        url: "/edit/:templateId",
        component: SlaEmailTemplateComponent,
        params:{templateId:null},
        resolve: [
            {
                token: 'templateId',
                deps: [StateService],
                resolveFn: resolveTemplateId
            }
        ],
        data: {
            permissionsKey:"SERVICE_LEVEL_AGREEMENT_EMAIL_TEMPLATES"
        }
    }
];
export function resolveParams(state: StateService) {
    return state.transition.params();
}

export function resolveTemplateId(state: StateService) {
    return state.transition.params().templateId;
}