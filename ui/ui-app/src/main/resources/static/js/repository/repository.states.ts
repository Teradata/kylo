import {Ng2StateDeclaration} from "@uirouter/angular";
import {RepositoryComponent} from "./repository.component";
import {TemplateService} from "./services/template.service";
import {TemplateInfoComponent} from "./template-info/template-info.component";
import AccessConstants from "../constants/AccessConstants";
import {ImportTemplateComponent} from "./ng5-import-template.component";
import {Lazy} from '../kylo-utils/LazyLoadUtil';

export const repositoryStates: Ng2StateDeclaration[] = [
    {
        name: "repository",
        url: "/repository",
        views: {
            "content": {
                component: RepositoryComponent
            }
        },
        data: {
            breadcrumbRoot: true,
            displayName: "Resource Repository"
        }
    },
    {
        name: "import-template",
        url: "/import-template",
        params: {
            template: null
        },
        views: {
            "content": {
                component: ImportTemplateComponent
            }
        },
        resolve: {
            loadMyCtrl: ['$ocLazyLoad', ($ocLazyLoad: any) => {
                const onModuleLoad = () => {
                    return import(/* webpackChunkName: "feeds.import-template.controller" */ '../feed-mgr/templates/import-template/ImportTemplateController')
                        .then(Lazy.onModuleImport($ocLazyLoad));
                };

                return import(/* webpackChunkName: "feed-mgr.module-require" */ "../feed-mgr/module-require").then(Lazy.onModuleImport($ocLazyLoad)).then(onModuleLoad);
            }]
        },
        data: {
            breadcrumbRoot: false,
            displayName: "Import Template",
            permissions: AccessConstants.TEMPLATES_IMPORT
        }
    },
    {
        name: "template-info",
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
            displayName: "Template Details"
        }
    }
];
