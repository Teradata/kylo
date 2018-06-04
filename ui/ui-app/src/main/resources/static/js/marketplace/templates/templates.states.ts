import {Ng2StateDeclaration} from "@uirouter/angular";

import {TemplatesComponent} from "./templates.component";
import { TemplateService } from "./services/template.service";

export const marketplaceTemplateStates: Ng2StateDeclaration[] = [
    {
        name: "marketplace",
        url: "/marketplace",
        views: {
            "content": {
                component: TemplatesComponent
            }
        },
        resolve: [
            {token: "templates", deps: [TemplateService], resolveFn: (templateService: TemplateService) => templateService.getTemplates().toPromise()}
        ],
        data: {
            breadcrumbRoot: true,
            displayName: "Templates"
        }
    }
];
