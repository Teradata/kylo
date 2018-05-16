import {Ng2StateDeclaration, StateService} from "@uirouter/angular";
import {catchError} from "rxjs/operators/catchError";

import {CatalogService} from "./api/services/catalog.service";
import {DatasetComponent} from "./dataset/dataset.component";
import {CatalogComponent} from "./catalog.component";

export const catalogStates: Ng2StateDeclaration[] = [
    {
        name: "catalog",
        url: "/catalog",
        views: {
            "content": {
                component: CatalogComponent
            }
        },
        resolve: [
            {token: "connectors", deps: [CatalogService], resolveFn: (catalog: CatalogService) => catalog.getConnectors().toPromise()}
        ],
        data: {
            breadcrumbRoot: true,
            displayName: "Catalog"
        }
    },
    {
        name: "catalog.dataset",
        url: "/:dataSetId",
        component: DatasetComponent,
        resolve: [
            {
                token: "dataSet",
                deps: [CatalogService, StateService],
                resolveFn: (catalog: CatalogService, state: StateService) => {
                    return catalog.getDataSet(state.transition.params().dataSetId)
                        .pipe(catchError(() => state.go("catalog")))
                        .toPromise();
                }
            }
        ]
    },
    {
        name: "catalog.dataset.preview.**",
        url: "/preview",
        loadChildren: "feed-mgr/catalog/dataset/preview-schema/preview-schema.module#PreviewSchemaModule"
    },
    {
        name: "catalog.dataset.upload.**",
        url: "/upload",
        loadChildren: "feed-mgr/catalog/dataset/upload/upload.module#UploadModule"
    }
];
