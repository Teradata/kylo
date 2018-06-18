import {Ng2StateDeclaration} from "@uirouter/angular";
import {RepositoryComponent} from "./repository.component";
import {TemplateService} from "./services/template.service";

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
];
