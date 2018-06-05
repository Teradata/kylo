import {Ng2StateDeclaration, StateService} from "@uirouter/angular";
import {catchError} from "rxjs/operators/catchError";
import {finalize} from "rxjs/operators/finalize";

import {CatalogService} from "./api/services/catalog.service";
import {DatasourceComponent} from "./datasource/datasource.component";
import {CatalogComponent} from "./catalog.component";
import {ConnectorsComponent} from './connectors/connectors.component';
import {TdLoadingService} from '@covalent/core/loading';
import {ConnectorComponent} from './connector/connector.component';
import {DataSourcesComponent} from './datasources/datasources.component';

export const catalogStates: Ng2StateDeclaration[] = [
    {
        name: "catalog",
        url: "/catalog",
        redirectTo: "catalog.datasources",
        views: {
            "content": {
                component: CatalogComponent
            }
        },
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
                        .pipe(catchError(() => {
                            return state.go("catalog")
                        }))
                        .toPromise();
                }
            }
        ]
    },
    {
        name: "catalog.new-datasource",
        url: "/connectors/:connectorId",
        component: ConnectorComponent,
        resolve: [
            {
                token: "connector",
                deps: [CatalogService, StateService, TdLoadingService],
                resolveFn: (catalog: CatalogService, state: StateService, loading: TdLoadingService) => {
                    loading.register(ConnectorComponent.LOADER);
                    return catalog.getConnector(state.transition.params().connectorId)
                        .pipe(finalize(() => loading.resolve(ConnectorComponent.LOADER)))
                        .pipe(catchError(() => {
                            return state.go("catalog.connectors")
                        }))
                        .toPromise();
                }
            }
        ]
    },
    {
        name: "catalog.datasources",
        url: "/datasource",
        component: DataSourcesComponent,
        resolve: [
            {
                token: "datasources",
                deps: [CatalogService, StateService, TdLoadingService],
                resolveFn: (catalog: CatalogService, state: StateService, loading: TdLoadingService) => {
                    loading.register(DataSourcesComponent.LOADER);
                    return catalog.getDataSources()
                        .pipe(finalize(() => loading.resolve(DataSourcesComponent.LOADER)))
                        .pipe(catchError(() => {
                            return state.go("catalog")
                        }))
                        .toPromise();
                }
            }
        ]
    },
    {
        name: "catalog.datasource",
        url: "/datasource/:datasourceId",
        component: DatasourceComponent,
        resolve: [
            {
                token: "datasource",
                deps: [CatalogService, StateService, TdLoadingService],
                resolveFn: (catalog: CatalogService, state: StateService, loading: TdLoadingService) => {
                    loading.register(DatasourceComponent.LOADER);
                    let datasourceId = state.transition.params().datasourceId;
                    return catalog.getDataSource(datasourceId)
                        .pipe(finalize(() => loading.resolve(DatasourceComponent.LOADER)))
                        .pipe(catchError(() => {
                            return state.go("catalog")
                        }))
                        .toPromise();
                }
            }
        ]
    },
    {
        name: "catalog.datasource.preview.**",
        url: "/preview",
        loadChildren: "feed-mgr/catalog/datasource/preview-schema/preview-schema.module#PreviewSchemaModule"
    },
    {
        name: "catalog.datasource.upload.**",
        url: "/upload",
        loadChildren: "feed-mgr/catalog/datasource/upload/upload.module#UploadModule"
    },
    {
        name: "catalog.datasource.browse.**",
        url: "/browse",
        loadChildren: "feed-mgr/catalog/datasource/files/remote-files.module#RemoteFilesModule"
    },
    {
        name: "catalog.datasource.connection.**",
        url: "/tables",
        loadChildren: "feed-mgr/catalog/datasource/jdbc/jdbc.module#JdbcModule"
    }
];
