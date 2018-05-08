import {Ng2StateDeclaration, StateService} from "@uirouter/angular";
import {catchError} from "rxjs/operators/catchError";

import {CatalogService} from "./catalog/services/catalog.service";
import {DatasetComponent} from "./dataset/dataset.component";
import {ExplorerComponent} from "./explorer.component";

export const explorerStates: Ng2StateDeclaration[] = [
    {
        name: "explorer",
        url: "/explorer",
        views: {
            "content": {
                component: ExplorerComponent
            }
        },
        resolve: [
            {token: "connectors", deps: [CatalogService], resolveFn: (catalog: CatalogService) => catalog.getConnectors().toPromise()}
        ],
        data: {
            breadcrumbRoot: true,
            displayName: "Explorer"
        }
    },
    {
        name: "explorer.dataset",
        url: "/:dataSetId",
        component: DatasetComponent,
        resolve: [
            {
                token: "dataSet",
                deps: [CatalogService, StateService],
                resolveFn: (catalog: CatalogService, state: StateService) => {
                    return catalog.getDataSet(state.transition.params().dataSetId)
                        .pipe(catchError(() => state.go("explorer")))
                        .toPromise();
                }
            }
        ]
    },
    {
        name: "explorer.dataset.preview.**",
        url: "/preview",
        loadChildren: "feed-mgr/explorer/dataset/preview-schema/preview-schema.module#PreviewSchemaModule"
    },
    {
        name: "explorer.dataset.upload.**",
        url: "/upload",
        loadChildren: "feed-mgr/explorer/dataset/upload/upload.module#UploadModule"
    }
];
