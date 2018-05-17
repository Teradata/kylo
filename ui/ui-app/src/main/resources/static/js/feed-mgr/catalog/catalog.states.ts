import {Ng2StateDeclaration, StateService} from "@uirouter/angular";
import {catchError} from "rxjs/operators/catchError";
import {finalize} from "rxjs/operators/finalize";

import {CatalogService} from "./api/services/catalog.service";
import {DatasetComponent} from "./dataset/dataset.component";
import {CatalogComponent} from "./catalog.component";
import {ConnectorsComponent} from './connectors/connectors.component';
import {TdLoadingService} from '@covalent/core/loading';
import {DataSourcesComponent} from './datasources/datasources.component';

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
            {token: "datasources", deps: [CatalogService], resolveFn: (catalog: CatalogService) => catalog.getDataSources().toPromise()}
        ],
        data: {
            breadcrumbRoot: true,
            displayName: "Catalog"
        }
    },
    {
        name: "catalog.connectors",
        url: "/connectors",
        component: ConnectorsComponent,
        resolve: [
            {
                token: "connectors",
                deps: [CatalogService, StateService, TdLoadingService],
                resolveFn: (catalog: CatalogService, state: StateService, loading: TdLoadingService) => {
                    loading.register(ConnectorsComponent.LOADER);
                    return catalog.getConnectors()
                        .pipe(finalize(() => loading.resolve(ConnectorsComponent.LOADER)))
                        .pipe(catchError(() => state.go("catalog")))
                        .toPromise();
                }
            }
        ]
    },
    // {
    //     name: "catalog.new-datasource",
    //     url: "/datasource/:connectorId",
    //     component: ConnectorsComponent,
    //     resolve: [
    //         {
    //             token: "connectors",
    //             deps: [CatalogService, StateService, TdLoadingService],
    //             resolveFn: (catalog: CatalogService, state: StateService, loading: TdLoadingService) => {
    //                 loading.register(ConnectorsComponent.LOADER);
    //                 return catalog.getConnectors()
    //                     .pipe(finalize(() => loading.resolve(ConnectorsComponent.LOADER)))
    //                     .pipe(catchError(() => state.go("catalog")))
    //                     .toPromise();
    //             }
    //         }
    //     ]
    // },
    {
        name: "catalog.dataset",
        url: "/:datasourceId",
        component: DatasetComponent,
        resolve: [
            {
                token: "dataSet",
                deps: [CatalogService, StateService, TdLoadingService],
                resolveFn: (catalog: CatalogService, state: StateService, loading: TdLoadingService) => {
                    loading.register(DataSourcesComponent.LOADER);
                    return catalog.getDataSet(state.transition.params().datasourceId)
                        .pipe(finalize(() => this.loading.resolve(DataSourcesComponent.LOADER)))
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
