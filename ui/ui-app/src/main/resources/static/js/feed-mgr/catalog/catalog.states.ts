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
import {AdminConnectorsComponent} from "./connectors/admin-connectors.component";
import {AdminConnectorComponent} from "./connector/admin-connector.component";

export function resolveConnectors(catalog: CatalogService, state: StateService, loading: TdLoadingService) {
    loading.register(ConnectorsComponent.LOADER);
    return catalog.getConnectors()
        .pipe(finalize(() => loading.resolve(ConnectorsComponent.LOADER)))
        .pipe(catchError(() => {
            return state.go("catalog")
        }))
        .toPromise();
}

export function resolveNewDatasource(catalog: CatalogService, state: StateService, loading: TdLoadingService) {
    loading.register(ConnectorComponent.LOADER);
    const datasourceId = state.transition.params().datasourceId;
    if (datasourceId === undefined) {
        return undefined;
    }
    return catalog.getDataSource(datasourceId)
        .pipe(finalize(() => loading.resolve(ConnectorComponent.LOADER)))
        .pipe(catchError(() => {
            console.log("error getting datasource with id " + datasourceId);
            return state.go("catalog.connectors")
        }))
        .toPromise();
}

export function resolveConnector(catalog: CatalogService, state: StateService, loading: TdLoadingService) {
    loading.register(ConnectorComponent.LOADER);
    return catalog.getConnector(state.transition.params().connectorId)
        .pipe(finalize(() => loading.resolve(ConnectorComponent.LOADER)))
        .pipe(catchError(() => {
            return state.go("catalog.connectors")
        }))
        .toPromise();
}

export function resolvePluginOfConnector(catalog: CatalogService, state: StateService, loading: TdLoadingService) {
    return catalog.getPluginOfConnector(state.transition.params().connectorId)
        .pipe(catchError(() => {
            return state.go("catalog.connectors")
        }))
        .toPromise();
}

export function resolveDatasources(catalog: CatalogService, state: StateService, loading: TdLoadingService) {
    loading.register(DataSourcesComponent.LOADER);
    return catalog.getDataSources()
        .pipe(finalize(() => loading.resolve(DataSourcesComponent.LOADER)))
        .pipe(catchError((err) => {
            console.error('Failed to load catalog', err);
            return [];
        }))
        .toPromise();
}

export function resolveDatasource(catalog: CatalogService, state: StateService, loading: TdLoadingService) {
    loading.register(DatasourceComponent.LOADER);
    let datasourceId = state.transition.params().datasourceId;
    return catalog.getDataSource(datasourceId)
        .pipe(finalize(() => loading.resolve(DatasourceComponent.LOADER)))
        .pipe(catchError(() => {
            return state.go("catalog")
        }))
        .toPromise();
}

export function resolveDatasourceConnectorPlugin(catalog: CatalogService, state: StateService, loading: TdLoadingService) {
    let datasourceId = state.transition.params().datasourceId;
    return catalog.getDataSourceConnectorPlugin(datasourceId)
        .pipe(catchError(() => {
            return state.go("catalog")
        }))
        .toPromise();
}



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
            displayName: "Catalog",
            permissionsKey:"CATALOG"
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
                resolveFn: resolveConnectors
            }
        ],
        data:{
            permissionsKey: "EDIT_DATASOURCES"
        }
    },
    {
        name: "catalog.new-datasource",
        url: "/connectors/:connectorId?datasourceId=:ds",
        component: ConnectorComponent,
        resolve: [
            {
                token: "datasource",
                deps: [CatalogService, StateService, TdLoadingService],
                resolveFn: resolveNewDatasource
            },
            {
                token: "connector",
                deps: [CatalogService, StateService, TdLoadingService],
                resolveFn: resolveConnector
            },
            {
                token: "connectorPlugin",
                deps: [CatalogService, StateService, TdLoadingService],
                resolveFn: resolvePluginOfConnector
            }
        ],
        data:{
            permissionsKey:"EDIT_DATASOURCES"
        }
    },
    {
        name: "catalog.datasources",
        url: "/datasource",
        component: DataSourcesComponent,
        resolve: [
            {
                token: "datasources",
                deps: [CatalogService, StateService, TdLoadingService],
                resolveFn: resolveDatasources
            }
        ],
        data:{
            permissionsKey:"DATASOURCES"

        }
    },
    {
        name: "catalog.datasource",
        url: "/datasource/:datasourceId",
        component: DatasourceComponent,
        resolve: [
            {
                token: "datasource",
                deps: [CatalogService, StateService, TdLoadingService],
                resolveFn: resolveDatasource
            },
            {
                token: "connectorPlugin",
                deps: [CatalogService, StateService, TdLoadingService],
                resolveFn: resolveDatasourceConnectorPlugin
            }
        ],
        data:{
            permissionsKey:"DATASOURCES"
        }
    },
    {
        name: "catalog.admin-connectors",
        url: "/admin-connectors",
        component: AdminConnectorsComponent,
        resolve: [
            {
                token: "connectors",
                deps: [CatalogService, StateService, TdLoadingService],
                resolveFn: resolveConnectors
            }
        ],
        data:{
            permissionsKey:"ADMIN_CONNECTORS",
            breadcrumbRoot: true,
            displayName: "Manage Connectors",
            module:"ADMIN",
            menuLink:"catalog.admin-connectors"
        }
    },
    {
        name: "catalog.admin-connector",
        url: "/admin-connector/:connectorId",
        component: AdminConnectorComponent,
        resolve: [
            {
                token: "connector",
                deps: [CatalogService, StateService, TdLoadingService],
                resolveFn: resolveConnector
            }
        ],
        data:{
            permissionsKey:"ADMIN_CONNECTORS",
            displayName: "Manage Connector",
            module:"ADMIN",
            menuLink:"catalog.admin-connectors"
        }
    },


    {
        name: "catalog.datasource.preview.**",
        url: "/preview",
        loadChildren: "./datasource/preview-schema/preview-schema.module#PreviewSchemaRouterModule"
    },
    {
        name: "catalog.datasource.upload.**",
        url: "/upload",
        loadChildren: "./datasource/upload/upload.module#UploadRouterModule"
    },
    {
        name: "catalog.datasource.browse.**",
        url: "/browse",
        loadChildren: "./datasource/files/remote-files.module#RemoteFilesRouterModule"
    },
    {
        name: "catalog.datasource.connection.**",
        url: "/tables",
        loadChildren: "./datasource/tables/tables.module#TablesRouterModule"
    }
];
