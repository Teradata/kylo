import {Ng2StateDeclaration, StateService} from "@uirouter/angular";
import {catchError} from "rxjs/operators/catchError";
import {finalize} from "rxjs/operators/finalize";

import {CatalogService} from "./api/services/catalog.service";
import {DatasetComponent} from "./dataset/dataset.component";
import {CatalogComponent} from "./catalog.component";
import {ConnectorTypesComponent} from './connector-types/connector-types.component';
import {TdLoadingService} from '@covalent/core/loading';

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
        name: "catalog.connector-types",
        url: "/connector-types",
        component: ConnectorTypesComponent,
        resolve: [
            {
                token: "connectorTypes",
                deps: [CatalogService, StateService, TdLoadingService],
                resolveFn: (catalog: CatalogService, state: StateService, loading: TdLoadingService) => {
                    loading.register(ConnectorTypesComponent.LOADER);
                    return catalog.getConnectorTypes()
                        .pipe(finalize(() => loading.resolve(ConnectorTypesComponent.LOADER)))
                        .pipe(catchError(() => state.go("catalog")))
                        .toPromise();
                }
            }
        ]
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
    },
    {
        name: "catalog.dataset.browse.**",
        url: "/browse",
        loadChildren: "feed-mgr/catalog/dataset/files/remote-files.module#RemoteFilesModule"
    }
];
