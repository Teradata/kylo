import {Ng2StateDeclaration} from "@uirouter/angular";
import {RepositoryComponent} from "./repository.component";
import {TemplateService} from "./services/template.service";
import {TemplateInfoComponent} from "./template-info/template-info.component";

export const repositoryStates: Ng2StateDeclaration[] = [
    {
        name: "repository",
        url: "/repository",
        views: {
            "content": {
                component: RepositoryComponent
            }
        },
        resolve: [
            {token: "templates", deps: [TemplateService], resolveFn: (templateService: TemplateService) => templateService.getTemplates().toPromise()}
        ],
        data: {
            breadcrumbRoot: true,
            displayName: "Repository"
        }
    }
    /*,
    {
        name: "template.info",
        url: "/template-info",
        params: {
            registeredTemplateId: null,
            nifiTemplateId: null
        },
        views: {
            "content": {
                component: TemplateInfoComponent
            }
        },
        data: {
            breadcrumbRoot: true,
            displayName: "Repository"
        }
    }
    */
];
